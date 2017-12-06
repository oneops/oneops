package com.oneops.controller.jms;

import static com.oneops.controller.workflow.ExecutionType.DEPLOYMENT;
import static com.oneops.controller.workflow.ExecutionType.PROCEDURE;

import com.google.gson.Gson;
import com.oneops.controller.workflow.ExecutionManager;
import com.oneops.workflow.WorkflowMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;

public class WorkflowListener implements MessageListener {

  private final Logger logger = Logger.getLogger(WorkflowListener.class);
  private Gson gson = new Gson();
  private ExecutionManager executionManager;

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

      if (DEPLOYMENT.getName().equals(wfMessage.getType())) {
        logger.info("processWorkflow using ExecutionManager " + wfMessage.getExecutionId());
        executionManager.processDpmtWorkflow(wfMessage);
      }
      else if (PROCEDURE.getName().equals(wfMessage.getType())) {
        logger.info("processWorkflow using ExecutionManager " + wfMessage.getExecutionId());
        executionManager.processProcWorkflow(wfMessage);
      }
      else {
        logger.info("got unknown message " + wfMessage.getType() + " execId " + wfMessage.getExecutionId());
      }

    } catch (JMSException e) {
      logger.error("Exception in processMessage ", e);
    }
  }

  public void setExecutionManager(ExecutionManager executionManager) {
    this.executionManager = executionManager;
  }
}
