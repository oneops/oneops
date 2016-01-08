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

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsUtil;

/**
 * JMS publisher class which publishes both work-orders and action-orders
 * to the search stream queue.
 * 
 * @author ranand
 *
 */
public class WoPublisher {
	
	private static Logger logger = Logger.getLogger(WoPublisher.class);
	
	private Connection connection = null;
    private Session session = null; 
    final private Gson gson = new Gson();
    private String queueName;
    private boolean isPubEnabled;
    
    private final String SEARCH_FLAG = "IS_SEARCH_ENABLED";

    private ActiveMQConnectionFactory connFactory;
    private MessageProducer producer;
    
    /**
     *
     * @throws JMSException
     */
    public void init() throws JMSException {
        connection = connFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
        logger.info(">>>>WOPublisher initalized...");
        initProducer();
        isPubEnabled = "true".equals(System.getenv(SEARCH_FLAG));
    }
    
    private void initProducer() throws JMSException {
    	 // Create the session
        Destination destination = session.createQueue(queueName);
        // Create the producer.
        producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
    }
    
    
    /**
     * 
     * @param workOrder
     * @throws JMSException
     */
    public void publishMessage(CmsWorkOrderSimpleBase cmsWoSimpleBase,String type,String id) throws JMSException {
    	if(isPubEnabled){
    		cmsWoSimpleBase = CmsUtil.maskSecuredFields(cmsWoSimpleBase,type);
	    	TextMessage message = session.createTextMessage(gson.toJson(cmsWoSimpleBase));
	    	message.setStringProperty("type", getType(type));
	    	message.setStringProperty("msgId", id);
	    	producer.send(message);
	    	if (cmsWoSimpleBase instanceof CmsWorkOrderSimple) {
	    		logger.info("WO published to search stream queue for RfcId: "+((CmsWorkOrderSimple)cmsWoSimpleBase).getRfcId());
	    	} else if (cmsWoSimpleBase instanceof CmsActionOrderSimple) {
	    		logger.info("AO published to search stream queue for procedureId/actionId: " 
	    				    + ((CmsActionOrderSimple)cmsWoSimpleBase).getProcedureId() + "/" 
	    				    + ((CmsActionOrderSimple)cmsWoSimpleBase).getActionId());
	    	}
	    	logger.debug("WO published to search stream queue: "+message.getText());
    	}
    }
    
    
	/**
	 * 
	 * @param type
	 * @return
	 */
	private String getType(String type) {
    	if(CmsUtil.WORK_ORDER_TYPE.equals(type))
    		return "workorder";
    	else if(CmsUtil.ACTION_ORDER_TYPE.equals(type))
    		return "actionorder";
    	
		return null;
	}

	/**
     * Sets the conn factory.
     *
     * @param connFactory the new conn factory
     */
    public void setConnFactory(ActiveMQConnectionFactory connFactory) {
		this.connFactory = connFactory;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	

}
