package com.oneops.controller.jms;

import com.google.gson.Gson;
import com.oneops.controller.workflow.Deployer;
import com.oneops.workflow.WorkflowMessage;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class WorkflowListener implements MessageListener {

  private final Logger logger = Logger.getLogger(WorkflowListener.class);
  private Gson gson = new Gson();
  private Deployer deployer;

  @Override
  public void onMessage(Message message) {
    if (message instanceof TextMessage) {
      TextMessage textMessage = (TextMessage) message;
      processMessage(textMessage);
    }
  }

  private void processMessage(TextMessage message) {
    try {
      WorkflowMessage wfMessage = gson.fromJson(message.getText(), WorkflowMessage.class);
      logger.info("processWorkflow using Deployer " + wfMessage.getDpmtId());
      deployer.processWorkflow(wfMessage);
    } catch (JMSException e) {
      logger.error("Exception in processMessage ", e);
    }
  }

  public void setDeployer(Deployer deployer) {
    this.deployer = deployer;
  }

}
