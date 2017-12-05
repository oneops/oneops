package com.oneops.controller.workflow;

import static com.oneops.cms.cm.ops.domain.OpsProcedureState.active;

import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.workflow.WorkflowMessage;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.jms.JMSException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static com.oneops.controller.workflow.ExecutionType.DEPLOYMENT;
import static com.oneops.controller.workflow.ExecutionType.PROCEDURE;

@Component(value = "executionManager")
public class ExecutionManager {

  @Autowired
  ProcedureRunner procedureRunner;

  @Autowired
  Deployer deployer;

  private static Logger logger = Logger.getLogger(ExecutionManager.class);

  public void execute(CmsDeployment dpmt) {
    DeploymentContext context = deployer.deploy(dpmt);
    waitOnLatch(context, context.latch, DEPLOYMENT.getName());
  }

  public void processDpmtWorkflow(WorkflowMessage wfMessage) {
    DeploymentContext context = deployer.processWorkflow(wfMessage);
    waitOnLatch(context, context.latch, DEPLOYMENT.getName());
  }

  public void handleWOResponse(CmsWorkOrderSimple wo, Map<String, Object> params) throws JMSException {
    deployer.handleInductorResponse(wo, params);
    deployer.convergeIfNeeded(wo);
  }

  public void execute(CmsOpsProcedure procedure) {
    if (isActive(procedure)) {
      executeProcedure(procedure.getProcedureId());
    }
  }

  private void waitOnLatch(ExecutionContext context, CountDownLatch latch, String type) {
    if (latch != null) {
      long start = System.currentTimeMillis();
      try {
        latch.await();
      } catch (InterruptedException e) {
        logger.error("InterruptedException waiting for latch ", e);
      }
      logger.info("dispatchOrders for " + type +  ":" + context.getExecutionId() + ", step:" + context.getCurrentStep()
          + " took:" + timeElapsed(start) + "ms");
    }
  }

  private void executeProcedure(long procedureId) {
    ProcedureContext context = procedureRunner.executeProcedure(procedureId);
    waitOnLatch(context, context.latch, PROCEDURE.getName());
  }

  public void processProcWorkflow(WorkflowMessage wfMessage) {
    ProcedureContext context = procedureRunner.executeProcedure(wfMessage.getExecutionId());
    waitOnLatch(context, context.latch, PROCEDURE.getName());
  }

  private long timeElapsed(long time) {
    return (System.currentTimeMillis() - time);
  }

  private boolean isActive(CmsOpsProcedure procedure) {
    return active.equals(procedure.getProcedureState());
  }


  public void handleAOResponse(CmsActionOrderSimple ao, Map<String, Object> params) throws JMSException {
    procedureRunner.handleInductorResponse(ao, params);
    procedureRunner.convergeIfNeeded(ao);
  }

  public void setProcedureRunner(ProcedureRunner procedureRunner) {
    this.procedureRunner = procedureRunner;
  }

  public void setDeployer(Deployer deployer) {
    this.deployer = deployer;
  }
}
