package com.oneops.controller.workflow;

import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_ACTIVE;
import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_COMPLETE;
import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_FAILED;
import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_PAUSED;
import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_PENDING;
import static com.oneops.controller.cms.CMSClient.CANCELLED;
import static com.oneops.controller.cms.CMSClient.COMPLETE;
import static com.oneops.controller.cms.CMSClient.FAILED;
import static com.oneops.controller.cms.CMSClient.INPROGRESS;
import static com.oneops.controller.cms.CMSClient.ONEOPS_SYSTEM_USER;
import static com.oneops.controller.cms.CMSClient.PENDING;

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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deployer executes the deployment workflow from the deployment plan, the important aspects of this
 * workflow include <ul> <li>dispatching workorders to inductor queue</li> <li>handling response
 * from inductor</li> <li>proceeding to next step after converge</li> <li>updating deployment
 * state</li> </ul>
 */
@Component(value = "deployerImpl")
public class DeployerImpl implements Deployer {

  @Value("${oo.controller.wo.assembler.pool.size:10}")
  private int woAssemblerPoolSize;

  @Value("${oo.controller.wo.async.threshold:300}")
  private int woAsyncDispatchThreshold;

  @Autowired
  private WoDispatcher woDispatcher;

  @Autowired
  private CmsDpmtProcessor dpmtProcessor;

  @Autowired
  private WorkflowPublisher workflowPublisher;

  @Autowired
  private CMSClient cmsClient;

  private ThreadPoolExecutor woDispatchExecutor;

  static Logger logger = Logger.getLogger(DeployerImpl.class);


  @PostConstruct
  public void init() {
    woDispatchExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(woAssemblerPoolSize);
  }

  @Override
  public void deploy(CmsDeployment dpmt) {
    logger.info("Deployer started for dpmt : " + dpmt.getDeploymentId());
    if (!isComplete(dpmt)) {
      cmsClient.updateDeploymentAndNotify(dpmt, null, "Deployer");
    }
    processWorkOrders(dpmt.getDeploymentId(), true);
  }

  @Override
  public void processWorkflow(WorkflowMessage wfMessage) {
    processWorkOrders(wfMessage.getDpmtId(), true);
  }

  public void processWorkOrders(long dpmtId, boolean wait4WoDispatch) {
    DeployerContext dpmtContext = fetchPendingWorkOrders(dpmtId, wait4WoDispatch);
    if (!dpmtContext.completed && dpmtContext.dpmtStep != null) {
      dispatchWorkOrders(dpmtContext.dpmt, dpmtContext.woList, dpmtContext.dpmtStep,
          wait4WoDispatch || isBacklogHigh());
    }
  }

  /**
   * retrieves pending workorders to be dispatched for the deployment by acquiring lock on dpmtId,
   * uses a separate executor for assembling and sending workorders
   *
   * Updates the deployment state in these cases 1. sets state to paused if we reach a step where
   * auto pause is enabled 2. sets state to complete/failed if all workorders are finished
   *
   * @param dpmtId deployment id
   * @param wait4WoDispatch if set waits for all workorders to be dispatched
   */
  @Transactional
  public DeployerContext fetchPendingWorkOrders(long dpmtId, boolean wait4WoDispatch) {
    logger.info("fetchPendingWorkOrders for deployment " + dpmtId);
    long startTs = System.currentTimeMillis();
    CmsDeployment deployment = dpmtProcessor.getDeployment(dpmtId);
    boolean dpmtFinished = false;
    boolean needsAutoPause = false;
    DeployerContext dpmtContext = new DeployerContext(deployment);

    if (isActive(deployment)) {
      try {
        int currentStep = deployment.getCurrentStep();
        logger.info("deployment " + dpmtId + " active @ step " + currentStep);
        List<CmsWorkOrderSimple> list = Collections.emptyList();

        //loop until either we have a list of workorders or the end of deployment
        while (list.isEmpty()) {
          list = cmsClient.getWorkOrderIdsNoLimit(deployment, currentStep);
          if (list.isEmpty()) {
            currentStep++;
            if (currentStep > deployment.getMaxExecOrder()) {
              //no workorder pending for this deployment, mark it as finished,
              //final state of deployment could be completed/failed based on all the workorders state
              dpmtFinished = true;
              break;
            }
            needsAutoPause = autoPauseIfRequired(deployment, currentStep);
          }
        }

        if (!dpmtFinished) {
          if (needsAutoPause) {
            DeploymentStep dStep = new DeploymentStep(currentStep);
            updateDeploymentWithStep(deployment, dStep, null);
          } else if (!list.isEmpty()) {
            //we have pending workorders to be dispatched, update the deployment's current_step
            //the actual dispatch will happen after releasing the dpmt lock
            DeploymentStep dStep = new DeploymentStep(currentStep, list.size());
            updateDeploymentWithStep(deployment, dStep, list);
            dpmtContext.dpmtStep = dStep;
            dpmtContext.woList = list;
          }
        }

      } catch (RuntimeException e) {
        logger.error("error in processWorkOrders ", e);
        throw e;
      }
      logger.info("processWo for deployment " + dpmtId + " took " + timeElapsed(startTs) + "ms");
      if (dpmtFinished) {
        logger.info("deployment finished, updating deployment end state " + dpmtId);
        updateDeploymentEndState(deployment);
      }
    }
    else {
      logger.info("deployment " + dpmtId + " not active");
    }
    dpmtContext.completed = dpmtFinished;
    return dpmtContext;
  }

  private boolean isBacklogHigh() {
    return woDispatchExecutor.getActiveCount() > woAsyncDispatchThreshold;
  }

  private void updateDeploymentWithStep(CmsDeployment dpmt, DeploymentStep dpmtStep,
      List<CmsWorkOrderSimple> list) {
    int newStep = dpmtStep.getStep();
    logger.info("processWo updating deployment " + dpmt.getDeploymentId() + " with step " + newStep);
    if (dpmt.getCurrentStep() != newStep) {
      dpmt.setCurrentStep(newStep);
      dpmtProcessor.updateDeploymentStep(dpmt);
    }
    if (list != null && !list.isEmpty()) {
      dpmtProcessor.createDeploymentExec(dpmt.getDeploymentId(), dpmtStep.getStep(), INPROGRESS);
    }
  }

  private void updateDeploymentEndState(CmsDeployment dpmt) {
    if (isActive(dpmt)) {
      cmsClient.updateDpmtState(dpmt, DPMT_STATE_COMPLETE);
    }
  }

  private boolean autoPauseIfRequired(CmsDeployment dpmt, int step) {
    Set<Integer> autoPauseExecOrders = dpmt.getAutoPauseExecOrders();
    if (autoPauseExecOrders != null && autoPauseExecOrders.contains(step)) {
      logger.info("auto pausing deployment " + dpmt.getDeploymentId() + " before step " + step);
      dpmt.setDeploymentState(DPMT_STATE_PAUSED);
      dpmt.setUpdatedBy(ONEOPS_SYSTEM_USER);
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
    return (dpmt != null) && DPMT_STATE_ACTIVE.equals(dpmt.getDeploymentState());
  }

  private boolean isComplete(CmsDeployment dpmt) {
    return (dpmt != null) && DPMT_STATE_COMPLETE.equals(dpmt.getDeploymentState());
  }

  private boolean isPaused(CmsDeployment dpmt) {
    return DPMT_STATE_PAUSED.equals(dpmt.getDeploymentState());
  }

  private boolean isPending(CmsDpmtRecord dpmtRecord) {
    return (dpmtRecord != null) && DPMT_STATE_PENDING.equals(dpmtRecord.getDpmtRecordState());
  }

  private long timeElapsed(long time) {
    return (System.currentTimeMillis() - time);
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
      WorkOrderContext context = new WorkOrderContext(wo, dpmtStep.getStep());
      woDispatchExecutor.submit(() -> {
        dispatchAndUpdate(dpmt, context, latch);
      });
    });
    try {
      latch.await();
    } catch (InterruptedException e) {
      logger.error("Exception waiting for latch in dispatchWorkOrders ", e);
    }
    logger.info(
        "dispatchWorkorders for dpmt " + dpmt.getDeploymentId() + ", step " + dpmtStep.getStep()
            + " took " + timeElapsed(startTs) + "ms");
  }

  private void dispatchWorkOrdersNoWait(CmsDeployment dpmt, List<CmsWorkOrderSimple> list,
      DeploymentStep dpmtStep) {
    list.forEach(wo -> {
      WorkOrderContext context = new WorkOrderContext(wo, dpmtStep.getStep());
      woDispatchExecutor.submit(() -> {
        dispatchAndUpdate(dpmt, context);
      });
    });
  }

  private void dispatchAndUpdate(CmsDeployment dpmt, WorkOrderContext context,
      CountDownLatch latch) {
    dispatchAndUpdate(dpmt, context);
    latch.countDown();
  }

  @Transactional
  public void dispatchAndUpdate(CmsDeployment dpmt, WorkOrderContext context) {
    CmsWorkOrderSimple wo = context.getWoSimple();
    CmsDpmtRecord dpmtRecord = dpmtProcessor.getDeploymentRecord(wo.getDpmtRecordId());
    if (isPending(dpmtRecord)) {
      logger.info(">>>>>>>>>>> dispatching workorder dpmtId : " + dpmt.getDeploymentId() + " rfc : "
          + dpmtRecord.getRfcId());
      woDispatcher.dispatchAndUpdate(dpmt, context);
    } else {
      logger.info(
          "workorder not in pending state dpmtId : " + dpmt.getDeploymentId() + " rfcId : " + wo
              .getRfcId() + " state : " + dpmtRecord.getDpmtRecordState());
    }
  }

  /**
   * updates wo state based on inductor response, if the state is successful the rfc would be
   * promoted to ci, also checks if we need to move to next step
   */
  @Override
  public void handleInductorResponse(CmsWorkOrderSimpleBase wo, Map<String, Object> params)
      throws JMSException {
    CmsWorkOrderSimple woResponse = (CmsWorkOrderSimple) wo;
    long dpmtId = woResponse.getDeploymentId();
    long rfcId = woResponse.getRfcId();
    String logPrefix = "dpmtId " + dpmtId + " rfc " + rfcId;
    logger.info(logPrefix + "<<<<<<<<<<< deployer handle inductor response");
    updateWoState(dpmtId, woResponse, params);
    logger.info(logPrefix + "ci updated from inductor response");
    int step = woResponse.rfcCi.getExecOrder();
    if (canConverge(dpmtId, rfcId, step)) {
      //send a jms message to controller.workflow queue to proceed to next step
      sendMessageToProceed(dpmtId, rfcId);
    }
    logger.info(logPrefix + " inductor response processing finished");
  }

  /**
   * checks if step converge can happen [no workorders in pending/in-progress state for this step].
   */
  @Override
  @Transactional
  public boolean canConverge(long dpmtId, long rfcId, int step) {
    long startTs = System.currentTimeMillis();
    String logPrefix = "dpmtId:" + dpmtId + " step:" + step + " rfc:" + rfcId + " :: ";
    boolean canConverge = false;
    Map<String, Integer> woCountMap = dpmtProcessor.getWorkordersCountByState(dpmtId, step);
    logger.info(logPrefix + "workorders state count: " + woCountMap);
    if (anyPendingOrActiveWo(woCountMap)) {
      logger.info(logPrefix + "can't converge as there are some workorders in pending/active state");
    }
    else {
      int updated = 0;
      if (anyFailedWo(woCountMap)) {
        logger.info(logPrefix + "trying to update step as failed");
        updated = dpmtProcessor.getAndUpdateStepState(dpmtId, step, FAILED);
        if (updated > 0) {
          CmsDeployment deployment = dpmtProcessor.getDeployment(dpmtId);
          if (!deployment.getContinueOnFailure()) {
            deployment.setDeploymentState(DPMT_STATE_FAILED);
            //if any of the wo has failed then update the deployment to failed
            dpmtProcessor.updateDeployment(deployment);
          }
        }
      }
      else if (!anyCancelledWo(woCountMap)) {
        logger.info(logPrefix + "trying to update step as complete");
        updated = dpmtProcessor.getAndUpdateStepState(dpmtId, step, COMPLETE);
        canConverge = updated > 0;
      }
      logger.info(logPrefix + "getAndUpdateStepState updated count " + updated);
    }
    logger.info(logPrefix + "canConverge took " + timeElapsed(startTs) + " ms");
    return canConverge;
  }

  private CmsDeployment deployment(long dpmtId, String state) {
    CmsDeployment dpmt = new CmsDeployment();
    dpmt.setDeploymentId(dpmtId);
    dpmt.setDeploymentState(state);
    return dpmt;
  }

  private boolean anyCancelledWo(Map<String, Integer> woCountMap) {
    return getCount(woCountMap, CANCELLED) > 0;
  }

  private boolean anyFailedWo(Map<String, Integer> woCountMap) {
    return getCount(woCountMap, FAILED) > 0;
  }

  private boolean anyPendingOrActiveWo(Map<String, Integer> woCountMap) {
    return getCount(woCountMap, PENDING) > 0 ||
        getCount(woCountMap, INPROGRESS) > 0;
  }

  private int getCount(Map<String, Integer> woCountMap, String state) {
    Integer count = woCountMap.get(state);
    return count != null ? count : 0;
  }

  private void sendMessageToProceed(long dpmtId, long rfcId) throws JMSException {
    try {
      logger.info(
          "dpmtId " + dpmtId + " rfc " + rfcId + ": inductor response converging to next step");
      sendJMSMessageToProceed(dpmtId);
    } catch (JMSException e) {
      logger.error("JMSException processing the response ", e);
      throw e;
    }
  }

  @Transactional
  public void updateWoState(long dpmtId, CmsWorkOrderSimple woResponse,
      Map<String, Object> params) {
    CmsDeployment dpmt = dpmtProcessor.getDeployment(dpmtId);
    String state = (String) params.get(CmsConstants.WORK_ORDER_STATE);
    logger.info(
        "updating workorder state dpmt " + dpmtId + " rfc " + woResponse.getRfcId() + " state "
            + state);
    cmsClient.updateWoState(dpmt, woResponse, state, null);
  }

  private void sendJMSMessageToProceed(long dpmtId) throws JMSException {
    workflowPublisher.sendWorkflowMessage(dpmtId, null);
  }

  public void destroy() {
    woDispatchExecutor.shutdown();
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


}
