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
import com.oneops.controller.sensor.SensorClient;
import com.oneops.controller.util.ControllerUtil;
import com.oneops.controller.workflow.WorkflowController;
import com.oneops.sensor.client.SensorClientException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.activiti.engine.ActivitiException;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.IndentPrinter;
import org.apache.log4j.Logger;


/**
 * The listener interface for receiving inductor events.
 * The class that is interested in processing a inductor
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addInductorListener<code> method. When
 * the inductor event occurs, that object's appropriate
 * method is invoked.
 *
 */
public class InductorListener implements MessageListener {

  private static final Logger logger = Logger.getLogger(InductorListener.class);
  private static final String OK_RESPONSE = "200";
  final private Gson gson = new Gson();
  private final String ctrlrQueueName = "controller.response";
  private Connection connection = null;
  private Session session = null;
  private ActiveMQConnectionFactory connFactory;
  private WorkflowController wfController;
  private WoPublisher woPublisher;
  private SensorClient sensorClient;
  private ControllerUtil controllerUtil;

  public SensorClient getSensorClient() {
    return sensorClient;
  }

  public void setSensorClient(SensorClient sensorClient) {
    this.sensorClient = sensorClient;
  }

  public void setControllerUtil(ControllerUtil controllerUtil) {
    this.controllerUtil = controllerUtil;
  }

  /**
   * Sets the wf controller.
   *
   * @param wfController the new wf controller
   */
  public void setWfController(WorkflowController wfController) {
    this.wfController = wfController;
  }

  /**
   * Sets the conn factory.
   *
   * @param connFactory the new conn factory
   */
  public void setConnFactory(ActiveMQConnectionFactory connFactory) {
    this.connFactory = connFactory;
  }

  /**
   *
   * @param woPublisher
   */
  public void setWoPublisher(WoPublisher woPublisher) {
    this.woPublisher = woPublisher;
  }

  /**
   * Inits the.
   *
   * @throws JMSException the jMS exception
   */
  public void init() throws JMSException {

    connection = connFactory.createConnection();
    // lets make it transactional
    session = connection.createSession(true, Session.SESSION_TRANSACTED);
    Queue controllerQueue = session.createQueue(ctrlrQueueName);

    MessageConsumer consumer = session.createConsumer(controllerQueue);

    consumer.setMessageListener(this);
    connection.start();

    logger.info(">>>>>>>>>>>>>>>>Inductor Listener Waiting for messages...");
  }

    /* (non-Javadoc)
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */

  /**
   * handles the message; designed for TextMessage types
   */
  public void onMessage(Message message) {
    try {
      if (message instanceof TextMessage) {
        if (logger.isDebugEnabled()) {
          logger.debug("got message: " + ((TextMessage) message).getText());
        }
        try {
          processResponseMessage((TextMessage) message);
        } catch (ActivitiException ae) {
          logger.error("ActivityException in onMessage ", ae);
          logger.error("Will retry in 10 seconds \n" + ((TextMessage) message).getText());
          throw ae;
        }
      }
    } catch (JMSException e) {
      logger.error("JMSException in onMessage", e);
    }
  }

  private void processResponseMessage(TextMessage msg) throws JMSException {
    long startTime = System.currentTimeMillis();
    String corelationId = msg.getJMSCorrelationID();
    if (corelationId == null) {
      corelationId = msg.getStringProperty("task_id");
    }
    String[] props = corelationId.split("!");
    String processId = props[0];
    String executionId = props[1];
    //String taskName = props[2];

    String woTaskResult = msg.getStringProperty("task_result_code");
    if (logger.isDebugEnabled()) {
      logger.debug("Inductor response >>>>>>" + ((TextMessage) msg).getText());
    }

    String type = msg.getStringProperty("type");
    Map<String, Object> params = new HashMap<>();
    //noinspection UnusedAssignment
    CmsWorkOrderSimpleBase wo = null;
    CmsWorkOrderSimple strippedWo = null;

    if ("opsprocedure".equalsIgnoreCase(type)) {
      wo = gson.fromJson(((TextMessage) msg).getText(), CmsActionOrderSimple.class);
    } else if ("deploybom".equalsIgnoreCase(type)) {
      wo = gson.fromJson(((TextMessage) msg).getText(), CmsWorkOrderSimple.class);
      strippedWo = controllerUtil.stripWO((CmsWorkOrderSimple) wo);
      if (woTaskResult.equalsIgnoreCase(OK_RESPONSE)) {
        try {
          sensorClient.processMonitors((CmsWorkOrderSimple) wo);
        } catch (SensorClientException e) {
          logger.error("Exception occurred in creating monitors",e);
        }
      }
    } else {
      throw new JMSException("the type property of the received msg is unknown - " + type);
    }

    if (strippedWo != null) {
      params.put("wo", strippedWo);
    } else {
      params.put("wo", wo);
    }

    if (woTaskResult.equalsIgnoreCase("200")) {
      params.put("wostate", "complete");
    } else {
      params.put("wostate", "failed");
    }
    String woCorelationId = processId + executionId;
    wfController.pokeSubProcess(processId, executionId, params);
    final long processTime = System.currentTimeMillis() - startTime;
    wo.getSearchTags().put("cProcessTime",String.valueOf(processTime));
    setWoTimeStamps(wo);
    woPublisher.publishMessage(wo, type, woCorelationId);
    logger.info("Processed iResponse with id "+ corelationId + " result" + woTaskResult+" took(ms) " +processTime);

  }

  /**
   * Set the time stamps in the wo/ao for search/analytics
   */
  private <T> void setWoTimeStamps(CmsWorkOrderSimpleBase<T> wo) {
    SimpleDateFormat format = new SimpleDateFormat(CmsConstants.SEARCH_TS_PATTERN);
    String responseDequeTs = format.format(new Date());
    wo.getSearchTags().put(CmsConstants.RESPONSE_DEQUE_TS, responseDequeTs);

    String closeTime, totalTime;
    try {

      closeTime = String.valueOf((format.parse(responseDequeTs).getTime() -
          format.parse(wo.getSearchTags().get(CmsConstants.RESPONSE_ENQUE_TS)).getTime()) / 1000.0);
      wo.getSearchTags().put(CmsConstants.CLOSE_TIME, closeTime);
      totalTime = String.valueOf((format.parse(responseDequeTs).getTime() -
          format.parse(wo.getSearchTags().get(CmsConstants.REQUEST_ENQUE_TS)).getTime()) / 1000.0);
      wo.getSearchTags().put(CmsConstants.TOTAL_TIME, totalTime);
    } catch (Exception e) {
      logger.error("Exception occurred while parsing date " + e);
    }
  }

  /**
   * Gets the connection stats.
   *
   * @return the connection stats
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

  private void closeConnection() {
    try {
      session.close();
      connection.close();
    } catch (Exception ignore) {
    }
  }

}
