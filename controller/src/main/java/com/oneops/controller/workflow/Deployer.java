package com.oneops.controller.workflow;

import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsDpmtRecord;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.exceptions.CmsBaseException;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.controller.cms.CMSClient;
import com.oneops.workflow.WorkflowMessage;
import com.oneops.workflow.WorkflowPublisher;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class Deployer {

  @Value("${oo.controller.wo.assembler.pool.size:10}")
  private int woAssemblerPoolSize;

  @Value("${oo.controller.wo.async.threshold:300}")
  private int woAsyncDispatchThreshold;

  @Value("${oo.controller.dpmt.time.between.runs:60}")
  private int dpmtWaitTimeBetweenRuns;

  @Autowired(required = false)
  private DeploymentCache dpmtCache;

  @Autowired
  private WoDispatcher woDispatcher;

  @Autowired
  private CmsDpmtProcessor dpmtProcessor;

  @Autowired
  private WorkflowPublisher workflowPublisher;

  @Autowired
  private CMSClient cmsClient;

  private ThreadPoolExecutor woDispatchExecutor;

  static Logger logger = Logger.getLogger(Deployer.class);


  @PostConstruct
  public void init() {
    woDispatchExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(woAssemblerPoolSize);
  }

  public void deploy(CmsDeployment dpmt) {
    logger.info("Deployer started for dpmt : " + dpmt.getDeploymentId());
    DeploymentExecution dpmtExec = dpmtCache.getDeploymentFromMap(dpmt.getDeploymentId());
    if (dpmtExec == null) {
      dpmtExec = new DeploymentExecution();
      dpmtExec.setDeploymentId(dpmt.getDeploymentId());
      dpmtExec.setCurrentStep(1);
      dpmtCache.updateDeploymentMap(dpmt.getDeploymentId(), dpmtExec);
    }
    processWorkOrders(dpmt.getDeploymentId(), false, true);
  }

  public void processWorkflow(WorkflowMessage wfMessage) {
    processWorkOrders(wfMessage.getDpmtId(), wfMessage.isCheckProcessDelay(), true);
  }

  @Transactional
  public void processWorkOrders(long dpmtId, boolean checkProcessDelay, boolean wait4WoDispatch) {
    CmsDeployment dpmt = dpmtProcessor.getDeployment(dpmtId);
    boolean dpmtFinished = false;
    boolean isAutoPaused = false;
    StepDispatch stepDispatch = null;
    if (isActive(dpmt)) {
      dpmtCache.lockDpmt(dpmtId);
      try {
        DeploymentExecution dpmtExec = dpmtCache.getDeploymentFromMap(dpmtId);
        if (dpmtExec.getCurrentStep() < dpmt.getCurrentStep()) {
          dpmtExec.setCurrentStep(dpmt.getCurrentStep());
        }
        int currentStep = dpmtExec.getCurrentStep();

        DeploymentStep currStep = dpmtExec.getStepMap().get(currentStep);
        if (currStep != null) {
          if (checkProcessDelay &&
              timeElapsedInSecs(currStep.getScheduledTs()) < dpmtWaitTimeBetweenRuns) {
            //dont proceed if now()-stepDispatchTs < dpmtWaitTimeBetweenRuns
            return;
          }
        }

        List<CmsWorkOrderSimple> list = Collections.emptyList();

        while (list.isEmpty()) {
          list = cmsClient.getWorkOrderIdsNoLimit(dpmt, currentStep);
          //there are no workorders in this step, so increment the current step and continue
          if (list.isEmpty()) {
            DeploymentStep dpmtStep = dpmtExec.getStepMap().get(currentStep);
            if (dpmtStep != null) {
              dpmtStep.setCompleted(true);
            }
            currentStep++;
            if (currentStep > dpmt.getMaxExecOrder()) {
              dpmtFinished = true;
              break;
            }
            isAutoPaused = autoPauseIfRequired(dpmt, currentStep);
          }
        }

        if (!dpmtFinished) {
          if (isAutoPaused) {
            DeploymentStep dStep = new DeploymentStep(currentStep);
            updateDeploymentWithStep(dpmt, dpmtExec, dStep, null);
          }
          else if (!list.isEmpty()) {
            DeploymentStep dStep = new DeploymentStep(currentStep, list.size(), System.currentTimeMillis());
            updateDeploymentWithStep(dpmt, dpmtExec, dStep, list);
            stepDispatch = new StepDispatch(dStep, list);
          }
        }

      } catch (RuntimeException e) {
        logger.error("error in processWorkOrders ", e);
        throw e;
      } finally {
        dpmtCache.unlockDpmt(dpmtId);
      }
      if (dpmtFinished) {
        updateDeploymentEndState(dpmt);
        dpmtCache.removeDeploymentFromMap(dpmtId);
      } else if (stepDispatch != null) {
        dispatchWorkOrders(dpmt, stepDispatch.list, stepDispatch.dpmtStep,
            wait4WoDispatch || isBacklogHigh());
      }
    } else {
      dpmtCache.removeDeploymentFromMap(dpmtId);
    }
  }

  private boolean isBacklogHigh() {
    return woDispatchExecutor.getActiveCount() > woAsyncDispatchThreshold;
  }

  private void updateDeploymentWithStep(CmsDeployment dpmt, DeploymentExecution dpmtExec,
      DeploymentStep dpmtStep, List<CmsWorkOrderSimple> list) {
    dpmtExec.setCurrentStep(dpmtStep.getStep());
    if (list != null && !list.isEmpty()) {
      dpmtExec.getStepMap().computeIfAbsent(dpmtStep.getStep(), (k) -> dpmtStep);
    }
    dpmtCache.updateDeploymentMap(dpmtExec.getDeploymentId(), dpmtExec);
    if (dpmt.getCurrentStep() != dpmtExec.getCurrentStep()) {
      dpmt.setCurrentStep(dpmtExec.getCurrentStep());
      dpmtProcessor.updateDeploymentStep(dpmt);
    }
  }

  private void updateDeploymentEndState(CmsDeployment dpmt) {
    if (isActive(dpmt)) {
      cmsClient.updateDpmtState(dpmt, CmsDpmtProcessor.DPMT_STATE_COMPLETE);
    }
  }

  private boolean autoPauseIfRequired(CmsDeployment dpmt, int step) {
    Set<Integer> autoPauseExecOrders = dpmt.getAutoPauseExecOrders();
    if (autoPauseExecOrders != null && autoPauseExecOrders.contains(step)) {
      logger.info("pausing deployment " + dpmt.getDeploymentId() + " before step " + step);
      dpmt.setDeploymentState(CmsDpmtProcessor.DPMT_STATE_PAUSED);
      dpmt.setUpdatedBy(CMSClient.ONEOPS_SYSTEM_USER);
      dpmt.setComments("deployment paused at step " + step + " on " + new Date());
      try {
        dpmtProcessor.updateDeployment(dpmt);
        return true;
      } catch (CmsBaseException e) {
        logger.error("CmsBaseException in autoPauseIfRequired", e);
        throw e;
      }
    }
    return false;
  }

  private boolean isActive(CmsDeployment dpmt) {
    return CmsDpmtProcessor.DPMT_STATE_ACTIVE.equals(dpmt.getDeploymentState());
  }

  private boolean isPaused(CmsDeployment dpmt) {
    return CmsDpmtProcessor.DPMT_STATE_PAUSED.equals(dpmt.getDeploymentState());
  }

  private boolean isPending(CmsDpmtRecord dpmtRecord) {
    return CmsDpmtProcessor.DPMT_STATE_PENDING.equals(dpmtRecord.getDpmtRecordState());
  }

  private long timeElapsedInSecs(long time) {
    return (System.currentTimeMillis() - time) / 1000;
  }

  private void dispatchWorkOrders(CmsDeployment dpmt, List<CmsWorkOrderSimple> list,
      DeploymentStep dpmtStep, boolean wait4WoDispatch) {
    logger.info("Deployer : " + dpmt.getDeploymentId() + ", step " + dpmtStep.getStep());
    if (wait4WoDispatch) {
      dispatchWorkOrdersWithWait(dpmt, list, dpmtStep);
    } else {
      dispatchWorkOrdersNoWait(dpmt, list, dpmtStep);
    }
  }

  private void dispatchWorkOrdersWithWait(CmsDeployment dpmt, List<CmsWorkOrderSimple> list,
      DeploymentStep dpmtStep) {
    CountDownLatch latch = new CountDownLatch(list.size());
    list.forEach(wo -> {
      WorkOrderContext context = updateRfcMapAndGetWoContext(dpmt, wo, dpmtStep);
      woDispatchExecutor.submit(() -> {
        dispatchAndUpdate(dpmt, context, latch);
      });
    });
    try {
      latch.await();
    } catch (InterruptedException e) {
      logger.error("Exception waiting for latch in dispatchWorkOrders ", e);
    }
  }

  private void dispatchWorkOrdersNoWait(CmsDeployment dpmt, List<CmsWorkOrderSimple> list,
      DeploymentStep dpmtStep) {
    list.forEach(wo -> {
      WorkOrderContext context = updateRfcMapAndGetWoContext(dpmt, wo, dpmtStep);
      woDispatchExecutor.submit(() -> {
        dispatchAndUpdate(dpmt, context);
      });
    });
  }

  private WorkOrderContext updateRfcMapAndGetWoContext(CmsDeployment dpmt, CmsWorkOrderSimple wo,
      DeploymentStep dpmtStep) {
    String rfcKey = rfcKey(dpmt.getDeploymentId(), wo.getRfcId());
    DeploymentRfc rfc = dpmtCache.getRfcFromMap(rfcKey);
    if (rfc == null) {
      dpmtCache.updateRfcMap(rfcKey, constructRfc(wo, "SCHEDULED_DISPATCH", dpmtStep.getStep()));
    }
    WorkOrderContext context = new WorkOrderContext(wo, dpmtStep.getStep());
    return context;
  }

  private void dispatchAndUpdate(CmsDeployment dpmt, WorkOrderContext context,
      CountDownLatch latch) {
    dispatchAndUpdate(dpmt, context);
    latch.countDown();
  }

  @Transactional
  void dispatchAndUpdate(CmsDeployment dpmt, WorkOrderContext context) {
    CmsWorkOrderSimple wo = context.getWoSimple();
    String rfcKey = rfcKey(dpmt.getDeploymentId(), wo.getRfcId());
    dpmtCache.lockRfc(rfcKey);
    try {
      DeploymentRfc dpmtRfc = dpmtCache.getRfcFromMap(rfcKey);
      CmsDpmtRecord dpmtRecord = dpmtProcessor.getDeploymentRecord(wo.getDpmtRecordId());
      if (isPending(dpmtRecord)) {
        logger.info(
            ">>>>>>>>>>> dispatching workorder dpmtId : " + dpmt.getDeploymentId() + " rfc : "
                + dpmtRecord.getRfcId());
        woDispatcher.dispatchAndUpdate(dpmt, context);
        dpmtRfc.setState("SUBMITTED_TO_INDUCTOR");
        dpmtCache.updateRfcMap(rfcKey, dpmtRfc);
      } else {
        logger.info("workorder not in pending state dpmtId : " + dpmt.getDeploymentId() +
            " rfcId : " + wo.getRfcId() + " state : " + dpmtRecord.getDpmtRecordState());
      }
    } finally {
      dpmtCache.unlockRfc(rfcKey);
    }
  }

  private String rfcKey(long dpmtId, long rfcId) {
    return dpmtId + ":" + rfcId;
  }

  private DeploymentRfc constructRfc(CmsWorkOrderSimple wo, String state, int step) {
    return new DeploymentRfc(wo.getRfcId(), state, step);
  }

  public void handleInductorResponse(CmsWorkOrderSimpleBase wo, Map<String, Object> params)
      throws JMSException {
    CmsWorkOrderSimple woResponse = (CmsWorkOrderSimple) wo;
    long dpmtId = woResponse.getDeploymentId();
    long rfcId = woResponse.getRfcId();
    logger.info("<<<<<<<<<<< deployer handle inductor response deployment " + dpmtId + " rfc " + rfcId);
    String rfcKey = rfcKey(dpmtId, rfcId);
    DeploymentRfc dpmtRfc = dpmtCache.getRfcFromMap(rfcKey);
    updateWoState(dpmtId, woResponse, params);
    if (dpmtRfc != null) {
      dpmtCache.removeRfcFromMap(rfcKey);
    }
    logger.info("ci updated deployment " + dpmtId + " rfc " + rfcId);
    if (canContinueAfterWo(woResponse)) {
      checkForConverge(woResponse, dpmtRfc);
    }
    logger.info("inductor response finished  deployment " + dpmtId + " rfc " + rfcId);
  }

  private void checkForConverge(CmsWorkOrderSimple woResponse, DeploymentRfc dpmtRfc)
      throws JMSException {
    long dpmtId = woResponse.getDeploymentId();
    long rfcId = woResponse.getRfcId();
    String logPrefix = "dpmtId " + dpmtId + " rfc " + rfcId;
    long wosPending = dpmtProcessor.getUnfinishedWorkordersCount(dpmtId, dpmtRfc.getStep());
    logger.info(logPrefix + ": unfinished workorders count " + wosPending);
    if (wosPending == 0) {
      logger.info(logPrefix + ": trying to lock on dpmtId");
      dpmtCache.lockDpmt(dpmtId);
      try {
        logger.info(logPrefix + ": got lock in inductor response ");
        DeploymentExecution dpmtExec = dpmtCache.getDeploymentFromMap(dpmtId);
        DeploymentStep dpmtStep = dpmtExec.getStepMap().get(dpmtRfc.getStep());
        if (dpmtExec.getCurrentStep() == dpmtRfc.getStep() && !dpmtStep.isCompleted()) {
          logger.info(logPrefix + ": inductor response converging to next step");
          sendJMSMessageToProceed(dpmtId);
          dpmtStep.setCompleted(true);
        }
        dpmtCache.updateDeploymentMap(dpmtId, dpmtExec);
        logger.info(logPrefix + ": inductor response updated dpmtMap");

      } catch (JMSException e) {
        logger.error("JMSException processing the response ", e);
        throw e;
      } finally {
        dpmtCache.unlockDpmt(dpmtId);
        logger.info(logPrefix + ": unlocked dpmtId");
      }
    }
  }

  @Transactional
  void updateWoState(long dpmtId, CmsWorkOrderSimple woResponse,
      Map<String, Object> params) {
    CmsDeployment dpmt = dpmtProcessor.getDeployment(dpmtId);
    String state = (String) params.get(CmsConstants.WORK_ORDER_STATE);
    logger.info("updating workorder state dpmt " + dpmtId + " rfc " + woResponse.getRfcId() + " state "
        + state);
    cmsClient.updateWoState(dpmt, woResponse, state, "workorder execution failed");
  }

  private void sendJMSMessageToProceed(long dpmtId) throws JMSException {
    workflowPublisher.sendWorkflowMessage(dpmtId, false, null);
  }

  private boolean canContinueAfterWo(CmsWorkOrderSimple wo) {
    return true;
  }

  public void destroy() {
    woDispatchExecutor.shutdown();
  }

  public void setDpmtCache(DeploymentCache dpmtCache) {
    this.dpmtCache = dpmtCache;
  }

  public void setWoDispatcher(WoDispatcher woDispatcher) {
    this.woDispatcher = woDispatcher;
  }

  public void setDpmtProcessor(CmsDpmtProcessor dpmtProcessor) {
    this.dpmtProcessor = dpmtProcessor;
  }

  public void setCmsClient(CMSClient cmsClient) {
    this.cmsClient = cmsClient;
  }

  public void setWoDispatchExecutor(ThreadPoolExecutor woDispatchExecutor) {
    this.woDispatchExecutor = woDispatchExecutor;
  }

  public void setWoAssemblerPoolSize(int woAssemblerPoolSize) {
    this.woAssemblerPoolSize = woAssemblerPoolSize;
  }

  public void setWoAsyncDispatchThreshold(int woAsyncDispatchThreshold) {
    this.woAsyncDispatchThreshold = woAsyncDispatchThreshold;
  }

  public void setWorkflowPublisher(WorkflowPublisher workflowPublisher) {
    this.workflowPublisher = workflowPublisher;
  }

  class StepDispatch {

    DeploymentStep dpmtStep;
    List<CmsWorkOrderSimple> list;

    StepDispatch(DeploymentStep dpmtStep, List<CmsWorkOrderSimple> list) {
      this.dpmtStep = dpmtStep;
      this.list = list;
    }
  }
}
