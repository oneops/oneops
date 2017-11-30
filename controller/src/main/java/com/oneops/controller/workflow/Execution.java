package com.oneops.controller.workflow;

import static com.oneops.controller.cms.CMSClient.CANCELLED;
import static com.oneops.controller.cms.CMSClient.FAILED;
import static com.oneops.controller.cms.CMSClient.INPROGRESS;
import static com.oneops.controller.cms.CMSClient.PENDING;

import com.oneops.workflow.WorkflowPublisher;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.jms.JMSException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class Execution<T> {

  static Logger logger = Logger.getLogger(Execution.class);

  @Autowired
  WorkflowPublisher workflowPublisher;

  public ExecutionContext pendingOrders(ExecutionContext context) {
    boolean isExecFinished = false;
    boolean needsAutoPause = false;
    String logPrefix = getType() + ":: id : " + context.getExecutionId();
    try {
      List<T> list = Collections.emptyList();;
      int step = context.getCurrentStep();

      logger.info(logPrefix + " current step before getting orders " + step);
      //loop until either we have a list of orders or the end of execution
      while (list.isEmpty() && !isExecFinished) {
        list = getOrdersForStep(context, step);
        if (list.isEmpty()) {
          step++;
          if (step > context.getMaxSteps()) {
            logger.info(logPrefix + " reached end of execution");
            //no orders pending for this execution, mark it as finished,
            //final state of the execution could be completed/failed based on all the orders state
            isExecFinished = true;
          }
          else {
            needsAutoPause = needsAutoPause(context, step);
          }
        }
      }

      if (!isExecFinished) {
        if (needsAutoPause) {
          logger.info(logPrefix + " auto pause @" + step);
          autoPause(context, step);
        } else if (!list.isEmpty()) {
          logger.info(logPrefix + " step " + step + ":: orders size for dispatching " + list.size());
          //we have pending orders to be dispatched, update the execution's current_step
          updateExecutionWithStep(context, step, list);
        }
      }
    } catch(RuntimeException e) {
      logger.error("error in pendingOrders ", e);
      throw e;
    }

    if (isExecFinished) {
      finishExecution(context);
    }
    return context;
  }

  protected boolean anyCancelled(Map<String, Integer> orderStateCountMap) {
    return getCount(orderStateCountMap, CANCELLED) > 0;
  }

  protected boolean anyFailed(Map<String, Integer> orderStateCountMap) {
    return getCount(orderStateCountMap, FAILED) > 0;
  }

  protected boolean anyPendingOrActiveOrder(Map<String, Integer> orderStateCountMap) {
    return getCount(orderStateCountMap, PENDING) > 0 ||
        getCount(orderStateCountMap, INPROGRESS) > 0;
  }

  protected int getCount(Map<String, Integer> orderStateCountMap, String state) {
    Integer count = orderStateCountMap.get(state);
    return count != null ? count : 0;
  }

  protected void sendMessageToProceed(String type, long executionId) throws JMSException {
    try {
      workflowPublisher.sendWorkflowMessage(type, executionId, null);
    } catch (JMSException e) {
      logger.error("JMSException processing the response ", e);
      throw e;
    }
  }

  protected long timeElapsed(long time) {
    return (System.currentTimeMillis() - time);
  }

  protected abstract String getType();

  protected abstract void dispatch(ExecutionContext context, T order, CountDownLatch latch);

  protected abstract void updateExecutionWithStep(ExecutionContext context, int step, List<T> list);

  protected abstract void autoPause(ExecutionContext context, int step);

  protected abstract boolean needsAutoPause(ExecutionContext context, int step);

  protected abstract void finishExecution(ExecutionContext context);

  protected abstract List<T> getOrdersForStep(ExecutionContext context, int step);

  public void setWorkflowPublisher(WorkflowPublisher workflowPublisher) {
    this.workflowPublisher = workflowPublisher;
  }

}
