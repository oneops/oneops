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

/**
 * Deployer executes the deployment workflow from the deployment plan,
 * the important aspects of this workflow include
 * <ul>
 * <li>dispatching workorders to inductor queue</li>
 * <li>handling response from inductor</li>
 * <li>proceeding to next step after converge</li>
 * <li>updating deployment state</li>
 * </ul>
 * This class uses a distributed map for maintaining deployment state, deployment locks
 */
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
      dpmtCache.updateDeploymentMap(dpmt.getDeploymentId(), newDpmtExecution(dpmt, 1));
    }
    cmsClient.updateDeploymentAndNotify(dpmt, null, "Deployer Execution");
    processWorkOrders(dpmt.getDeploymentId(), false, true);
  }

  private DeploymentExecution newDpmtExecution(CmsDeployment dpmt, int step) {
    DeploymentExecution dpmtExec = new DeploymentExecution();
    dpmtExec.setDeploymentId(dpmt.getDeploymentId());
    dpmtExec.setCurrentStep(step);
    return dpmtExec;
  }

  public void processWorkflow(WorkflowMessage wfMessage) {
    processWorkOrders(wfMessage.getDpmtId(), wfMessage.isCheckProcessDelay(), true);
  }

  /**
   * identifies pending workorders to be dispatched for the deployment by acquiring lock on dpmtId,
   * uses a separate executor for assembling and sending workorders, also updates the state in the deployment distributed map
   *
   * @param dpmtId deployment id
   * @param checkProcessDelay if set the execution will continue only if the last run was before dpmtWaitTimeBetweenRuns
   * @param wait4WoDispatch if set waits for all workorders to be dispatched
   */
  @Transactional
  public void processWorkOrders(long dpmtId, boolean checkProcessDelay, boolean wait4WoDispatch) {
    logger.info("processWo for deployment " + dpmtId);
    CmsDeployment dpmt = dpmtProcessor.getDeployment(dpmtId);
    boolean dpmtFinished = false;
    boolean isAutoPaused = false;
    StepDispatch stepDispatch = null;
    long startTs = System.currentTimeMillis();
    if (isActive(dpmt)) {
      ensureDpmtInCache(dpmt);
      dpmtCache.lockDpmt(dpmtId);
      try {
        DeploymentExecution dpmtExec = dpmtCache.getDeploymentFromMap(dpmtId);
        logger.info("processWo - dpmtExec from cache : " + dpmtExec);
        //use the current_step from db if there is a mismatch between cache and db
        if (dpmtExec.getCurrentStep() < dpmt.getCurrentStep()) {
          dpmtExec.setCurrentStep(dpmt.getCurrentStep());
        }
        int currentStep = dpmtExec.getCurrentStep();

        DeploymentStep currStep = dpmtExec.getStepMap().get(currentStep);
        if (currStep != null) {
          if (checkProcessDelay &&
              timeElapsedInSecs(currStep.getScheduledTs()) < dpmtWaitTimeBetweenRuns) {
            //don't proceed if now()-stepDispatchTs < dpmtWaitTimeBetweenRuns
            return;
          }
        }

        List<CmsWorkOrderSimple> list = Collections.emptyList();

        //loop until either we have a list of workorders or the end of deployment
        while (list.isEmpty()) {
          list = cmsClient.getWorkOrderIdsNoLimit(dpmt, currentStep);
          if (list.isEmpty()) {
            //there are no workorders in this step, so increment the current step and continue
            DeploymentStep dpmtStep = dpmtExec.getStepMap().get(currentStep);
            if (dpmtStep != null) {
              dpmtStep.setCompleted(true);
            }
            currentStep++;
            if (currentStep > dpmt.getMaxExecOrder()) {
              //no workorder pending for this deployment, mark it as finished,
              //final state of deployment could be completed/failed based on all the workorders state
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
            //we have pending workorders to be dispatched, update the deployment's current_step
            //the actual dispatch will happen after releasing the lock on dpmtId
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

      logger.info("processWo for deployment " + dpmtId + " took " + (System.currentTimeMillis() - startTs) + "ms");
      if (dpmtFinished) {
        logger.info("deployment finished, removing from cache " + dpmtId);
        updateDeploymentEndState(dpmt);
        dpmtCache.removeDeploymentFromMap(dpmtId);
      } else if (stepDispatch != null) {
        dispatchWorkOrders(dpmt, stepDispatch.list, stepDispatch.dpmtStep,
            wait4WoDispatch || isBacklogHigh());
      }
    } else {
      logger.info("deployment not active, removing from cache " + dpmtId);
      dpmtCache.removeDeploymentFromMap(dpmtId);
    }
  }

  private boolean isBacklogHigh() {
    return woDispatchExecutor.getActiveCount() > woAsyncDispatchThreshold;
  }

  private void updateDeploymentWithStep(CmsDeployment dpmt, DeploymentExecution dpmtExec,
      DeploymentStep dpmtStep, List<CmsWorkOrderSimple> list) {
    int oldStep = dpmtExec.getCurrentStep();
    int newStep = dpmtStep.getStep();
    dpmtExec.setCurrentStep(newStep);
    logger.info("processWo updating deployment " + dpmt.getDeploymentId() + " with step " + newStep);
    if (list != null && !list.isEmpty()) {
      Map<Integer, DeploymentStep> stepMap = dpmtExec.getStepMap();
      stepMap.computeIfAbsent(newStep, (k) -> dpmtStep);
      if (oldStep < newStep) {
        stepMap.remove(oldStep);
      }
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
      logger.info("auto pausing deployment " + dpmt.getDeploymentId() + " before step " + step);
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
    return (dpmt != null) && CmsDpmtProcessor.DPMT_STATE_ACTIVE.equals(dpmt.getDeploymentState());
  }

  private boolean isPaused(CmsDeployment dpmt) {
    return CmsDpmtProcessor.DPMT_STATE_PAUSED.equals(dpmt.getDeploymentState());
  }

  private boolean isPending(CmsDpmtRecord dpmtRecord) {
    return (dpmtRecord != null) && CmsDpmtProcessor.DPMT_STATE_PENDING.equals(dpmtRecord.getDpmtRecordState());
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
    long startTs = System.currentTimeMillis();
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
    logger.info("dispatchWorkorders for dpmt " + dpmt.getDeploymentId() + ", step " + dpmtStep.getStep()
        + " took " + (System.currentTimeMillis() - startTs) + "ms");
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

  /**
   * updates wo state based on inductor response, if the state is successful the rfc would be promoted to ci,
   * also checks if we need to move to next step
   */
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

  /**
   * checks if step converge can happen [all workorders are not in pending/in-progress state for this step].
   * if so then sends a jms message to controller.workflow queue to proceed to next step after acquiring lock on dpmtId
   */
  private void checkForConverge(CmsWorkOrderSimple woResponse, DeploymentRfc dpmtRfc)
      throws JMSException {
    long dpmtId = woResponse.getDeploymentId();
    long rfcId = woResponse.getRfcId();
    int step = dpmtRfc != null ? dpmtRfc.getStep() : woResponse.rfcCi.getExecOrder();
    String logPrefix = "dpmtId " + dpmtId + " rfc " + rfcId;
    long wosPending = dpmtProcessor.getUnfinishedWorkordersCount(dpmtId, step);
    logger.info(logPrefix + ": unfinished workorders count " + wosPending);
    if (wosPending == 0) {
      if (ensureDpmtInCache(dpmtId, step)) {
        logger.info(logPrefix + ": trying to lock on dpmtId");
        long startTs = System.currentTimeMillis();
        dpmtCache.lockDpmt(dpmtId);
        try {
          logger.info(logPrefix + ": got lock in inductor response ");
          DeploymentExecution dpmtExec = dpmtCache.getDeploymentFromMap(dpmtId);
          logger.info(logPrefix + " dpmtExec from cache : " + dpmtExec);
          DeploymentStep dpmtStep = dpmtExec.getStepMap().get(step);
          if (dpmtExec.getCurrentStep() == step && !dpmtStep.isCompleted()) {
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
        logger.info(logPrefix + ": converge took " + (System.currentTimeMillis() - startTs) + " ms");
      }
    }
  }

  private boolean ensureDpmtInCache(long dpmtId, int step) {
    if (dpmtCache.getDeploymentFromMap(dpmtId) == null) {
      logger.info("deployment not available in cache " + dpmtId + " step " + step);
      CmsDeployment dpmt = dpmtProcessor.getDeployment(dpmtId);
      if (isActive(dpmt) && dpmt.getCurrentStep() == step) {
        logger.info("deployment " + dpmtId + " still active and step matches, so adding to cache");
        initCacheWithDeployment(dpmt);
      }
      else {
        logger.info("deployment " + dpmtId + " not active/step not matching step : " + step);
        return false;
      }
    }
    else {
      logger.info("deployment available in cache " + dpmtId);
    }
    return true;
  }

  private void ensureDpmtInCache(CmsDeployment dpmt) {
    if (dpmtCache.getDeploymentFromMap(dpmt.getDeploymentId()) == null) {
      initCacheWithDeployment(dpmt);
    }
  }

  private void initCacheWithDeployment(CmsDeployment dpmt) {
    DeploymentExecution dpmtExec = newDpmtExecution(dpmt, dpmt.getCurrentStep());
    dpmtExec.getStepMap().put(dpmt.getCurrentStep(), new DeploymentStep(dpmt.getCurrentStep()));
    dpmtCache.updateDeploymentMap(dpmt.getDeploymentId(), dpmtExec);
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
