/*******************************************************************************
 *
 * Copyright 2015 Walmart, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.oneops.controller.jms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.oneops.util.Version;
import org.activiti.engine.delegate.DelegateExecution;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.IndentPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsConstants;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class InductorPublisher.
 */
public class InductorPublisher {
	private static final String DEFAULT_VERSION = "1.0.0";
	private static Logger logger = Logger.getLogger(InductorPublisher.class);
	
	//private long timeToLive;
    private Map<String, MessageProducer> bindingQueusMap = new ConcurrentHashMap<>();
    private static final String QUEUE_SUFFIX = ".ind-wo";
    private static final String SHARED_QUEUE = "shared" + QUEUE_SUFFIX;
    private static final String USE_SHARED_FLAG = "com.oneops.controller.use-shared-queue";
    private Connection connection = null;
    private Session session = null; 
    final private Gson gson = new Gson();
    private final Object lock = new Object();

    private ActiveMQConnectionFactory connFactory;
    private WoPublisher woPublisher;
    
    /**
     * Sets the conn factory.
     *
     * @param connFactory the new conn factory
     */
    public void setConnFactory(ActiveMQConnectionFactory connFactory) {
		this.connFactory = connFactory;
	}
    
    /**
     * Inits the.
     *
     * @throws JMSException the jMS exception
     */
    public void init() throws JMSException {
            connection = connFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();
            logger.info(">>>>>>>>>>>>>>InductorPublisher initialized...");
    }

    
    /**
     * Publish message.
     *
     * @param exec the exec
     * @param waitTaskName the wait task name
     * @param woType the wo type
     * @throws JMSException the jMS exception
     */
    public void publishMessage(DelegateExecution exec, String waitTaskName, String woType) throws JMSException {
    	String processId = exec.getProcessInstanceId();
    	String execId = exec.getId();

    	CmsWorkOrderSimpleBase wo = (CmsWorkOrderSimpleBase)exec.getVariable("wo");
    	publishMessage(processId, execId, wo, waitTaskName, woType);
    }

    @Autowired
	Version version;
    /**
     * Publish message.
     *
     * @param waitTaskName the wait task name
     * @param woType the wo type
     * @throws JMSException the jMS exception
     */
    public void publishMessage(String processId, String execId, CmsWorkOrderSimpleBase wo, String waitTaskName, String woType) throws JMSException {
		    SimpleDateFormat format=  new SimpleDateFormat(CmsConstants.SEARCH_TS_PATTERN);
    	    wo.getSearchTags().put(CmsConstants.REQUEST_ENQUE_TS, format.format(new Date()));
		    //guarantee non empty-value for searchMap
    	    if(version!=null && StringUtils.isNotBlank(version.getGitVersion())){
		    	wo.getSearchTags().put("ctrlrVersion", version.getGitVersion());
			}else{
				wo.getSearchTags().put("ctrlrVersion", DEFAULT_VERSION);
			}
	    	TextMessage message = session.createTextMessage(gson.toJson(wo));
	    	String corelationId = processId + "!" + execId + "!" + waitTaskName;
	    	message.setJMSCorrelationID(corelationId);
	    	message.setStringProperty("task_id", corelationId);
	    	message.setStringProperty("type", woType);
	    	String queueName;
	    	if ("true".equals(System.getProperty(USE_SHARED_FLAG))) {
	    		queueName = SHARED_QUEUE;
	    	} else {
	    		queueName = (wo.getCloud().getCiAttributes().get("location").replaceAll("/", ".") + QUEUE_SUFFIX).substring(1);
	    	}
	    	bindingQueusMap.putIfAbsent(queueName,newMessageProducer(queueName)).send(message);
	    	if (wo instanceof CmsWorkOrderSimple) {
	    		logger.info("Posted wo for the inductor dpmtId=" + ((CmsWorkOrderSimple)wo).getDeploymentId() 
	    				  + "; rfcId=" + ((CmsWorkOrderSimple)wo).getRfcId() 
	    				  + "; step=" + ((CmsWorkOrderSimple)wo).rfcCi.getExecOrder());
	    	} else if (wo instanceof CmsActionOrderSimple) {
	    		logger.info("Posted action order for the inductor ciId=" + ((CmsActionOrderSimple)wo).getCiId());
	    	}
	    	
	    	String woCorelationId = processId + execId;
	    	woPublisher.publishMessage(wo,woType,woCorelationId);
	    	
	    	logger.debug("Published: "+message.getText());
    }
    
	private MessageProducer newMessageProducer(String queueName) throws JMSException {
        // Create the session
        Destination destination = session.createQueue(queueName);
        // Create the producer.
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
    	logger.info("Created message producer for queue "+queueName);
        return producer;
    }
	
    
    /**
     * Gets the connection stats.
     *
     */
    public void getConnectionStats() {
        ActiveMQConnection c = (ActiveMQConnection) connection;
        c.getConnectionStats().dump(new IndentPrinter());
    }

    /**
     * Cleanup.
     */
    public void cleanup() {
    	logger.info("Closing AMQ connection");
    	closeConnection();
    }
    
    /**
     * Close connection.
     */
    public void closeConnection() {
        try {
        	for (MessageProducer producer : bindingQueusMap.values()) {
        		producer.close();
        	}
        	session.close();
        	connection.close();
        } catch (Exception ignore) {
        }
    }
    
    /**
	 * 
	 * @param woPublisher setter
	 */
	public void setWoPublisher(WoPublisher woPublisher) {
		this.woPublisher = woPublisher;
	}

//	public void setTimeToLive(long timeToLive) {
//		this.timeToLive = timeToLive;
//	}

}
