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

import com.google.gson.Gson;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.util.Version;
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
import org.activiti.engine.delegate.DelegateExecution;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.IndentPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class InductorPublisher.
 */
public class InductorPublisher {
    private static final String DEFAULT_VERSION = "1.0.0";
    private static final String QUEUE_SUFFIX = ".ind-wo";
    private static final String SHARED_QUEUE = "shared" + QUEUE_SUFFIX;
    private static final String USE_SHARED_FLAG = "com.oneops.controller.use-shared-queue";
    private static final String SHARED_QUEUE_PREFIX = "com.oneops.controller.queue.prefix.";
    private static final String CONTROLLLER_VERSION_SEARCH_TAG = "cVersion";
    private static Logger logger = Logger.getLogger(InductorPublisher.class);
    final private Gson gson = new Gson();
    @Autowired
    Version version;
    //private long timeToLive;
    private Map<String, MessageProducer> bindingQueusMap = new ConcurrentHashMap<>();
    private Connection connection = null;
    private Session session = null;
    private ActiveMQConnectionFactory connFactory;

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
     * @param exec         the exec
     * @param waitTaskName the wait task name
     * @param woType       the wo type
     * @throws JMSException the jMS exception
     */
    public void publishMessage(DelegateExecution exec, String waitTaskName, String woType) throws JMSException {
        String processId = exec.getProcessInstanceId();
        String execId = exec.getId();

        CmsWorkOrderSimpleBase wo = (CmsWorkOrderSimpleBase) exec.getVariable("wo");
        publishMessage(processId, execId, wo, waitTaskName, woType);
    }

    /**
     * Publish message.
     *
     * @param waitTaskName the wait task name
     * @param woType       the wo type
     * @throws JMSException the jMS exception
     */
    public void publishMessage(String processId, String execId, CmsWorkOrderSimpleBase wo, String waitTaskName, String woType) throws JMSException {
        SimpleDateFormat format = new SimpleDateFormat(CmsConstants.SEARCH_TS_PATTERN);
        wo.getSearchTags().put(CmsConstants.REQUEST_ENQUE_TS, format.format(new Date()));
        //guarantee non empty-value for searchMap
        if (version != null && StringUtils.isNotBlank(version.getGitVersion())) {
            wo.getSearchTags().put(CONTROLLLER_VERSION_SEARCH_TAG, version.getGitVersion());
        } else {
            wo.getSearchTags().put(CONTROLLLER_VERSION_SEARCH_TAG, DEFAULT_VERSION);
        }
        TextMessage message = session.createTextMessage(gson.toJson(wo));
        String corelationId = processId + "!" + execId + "!" + waitTaskName+"!"+getCtxtId(wo);
        message.setJMSCorrelationID(corelationId);
        message.setStringProperty("task_id", corelationId);
        message.setStringProperty("type", woType);

        String queueName = getQueue(wo);
        bindingQueusMap.computeIfAbsent(queueName, k -> {
            try {
                return newMessageProducer(k);
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }).send(message);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Published: " + message.getText());
        }

        logger.info("Posted message with id "+ corelationId +" to q: "+queueName);

    }

    String getQueue(CmsWorkOrderSimpleBase wo) {
        String queueName = null;
        String location = wo.getCloud().getCiAttributes().get("location");
        if ("true".equals(System.getProperty(USE_SHARED_FLAG))) {
            String cloudName = StringUtils.substringAfterLast(location, "/");
            if (StringUtils.isNotBlank(cloudName)) {
                String prefix = StringUtils.substringBefore(cloudName, "-");
                String queuePrefix = System.getProperty(SHARED_QUEUE_PREFIX + prefix);
                if (StringUtils.isNotBlank(queuePrefix)) {
                    queueName = queuePrefix + "." + SHARED_QUEUE;
                }
            }
            if (queueName == null)
                queueName = SHARED_QUEUE;
        } else {
            queueName = (location.replaceAll("/", ".") + QUEUE_SUFFIX).substring(
                1);
        }
        return queueName;
    }

    protected String getCtxtId(CmsWorkOrderSimpleBase wo) {
      String ctxtId = "";
      if (wo instanceof CmsWorkOrderSimple) {
        CmsWorkOrderSimple woSimple = CmsWorkOrderSimple.class.cast(wo);
        ctxtId = "d-" + woSimple.getDeploymentId()
            + "-" + woSimple.rfcCi.getRfcId()
            + "-" + woSimple.rfcCi.getExecOrder()
            + "-" + woSimple.rfcCi.getCiId();

      } else if (wo instanceof CmsActionOrderSimple) {
        CmsActionOrderSimple ao = CmsActionOrderSimple.class.cast(wo);
        ctxtId = "a-" + ao.getProcedureId() + "-" + ao.getActionId() + "-" + ao.getCiId();
      }
      return ctxtId;
    }

    private MessageProducer newMessageProducer(String queueName) throws JMSException {
        // Create the session
        Destination destination = session.createQueue(queueName);
        // Create the producer.
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        logger.info("Created message producer for queue " + queueName);
        return producer;
    }


    /**
     * Gets the connection stats.
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

}
