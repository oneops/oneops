package com.oneops.controller.workflow;

import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_ACTIVE;
import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_COMPLETE;
import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_FAILED;
import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_PAUSED;
import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_PENDING;
import static com.oneops.controller.cms.CMSClient.COMPLETE;
import static com.oneops.controller.cms.CMSClient.FAILED;
import static com.oneops.controller.cms.CMSClient.INPROGRESS;
import static com.oneops.controller.cms.CMSClient.ONEOPS_SYSTEM_USER;
import static com.oneops.controller.workflow.ExecutionType.DEPLOYMENT;

import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsDpmtRecord;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.exceptions.CmsBaseException;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.controller.cms.CMSClient;
import com.oneops.workflow.WorkflowMessage;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
public class DeployerImpl extends Execution<CmsWorkOrderSimple> implements Deployer {

  @Value("${oo.controller.wo.assembler.pool.size:10}")
  private int woAssemblerPoolSize;

  @Value("${oo.controller.wo.async.threshold:300}")
  private int woAsyncDispatchThreshold;

  @Autowired
  private WoDispatcher woDispatcher;

  @Autowired
  private CmsDpmtProcessor dpmtProcessor;

  @Autowired
  private CMSClient cmsClient;

  private ThreadPoolExecutor woDispatchExecutor;

  static Logger logger = Logger.getLogger(DeployerImpl.class);


  @PostConstruct
  public void init() {
    woDispatchExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(woAssemblerPoolSize);
  }

  @Override
  @Transactional
  public DeploymentContext deploy(CmsDeployment dpmt) {
    DeploymentContext context = null;
    logger.info("Deployer started for dpmt : " + dpmt.getDeploymentId());
    if (!isComplete(dpmt)) {
      cmsClient.updateDeploymentAndNotify(dpmt, null, "Deployer");
    }
    if (isActive(dpmt)) {
      context = processWorkOrders(dpmt.getDeploymentId());
    }
    else {
        logger.info("deployment " + dpmt.getDeploymentId() + " is not active");
    }
    if (context == null) {
      context = new DeploymentContext(dpmt);
    }
    return context;
  }

  @Override
  @Transactional
  public DeploymentContext processWorkflow(WorkflowMessage wfMessage) {
    return processWorkOrders(wfMessage.getExecutionId());
  }

  DeploymentContext processWorkOrders(long dpmtId) {
    DeploymentContext dpmtContext = pendingWorkOrders(dpmtId);
    if (!dpmtContext.completed && dpmtContext.woList != null && !dpmtContext.woList.isEmpty()) {
      dispatchWorkOrders(dpmtContext);
    }
    return dpmtContext;
  }

  /**
   * retrieves pending workorders to be dispatched for the deployment by acquiring lock on dpmtId,
   * uses a separate executor for assembling and sending workorders
   *
   * Updates the deployment state in these cases 1. sets state to paused if we reach a step where
   * auto pause is enabled 2. sets state to complete/failed if all workorders are finished
   *
   * @param dpmtId deployment id
   */
  private DeploymentContext pendingWorkOrders(long dpmtId) {
    logger.info("pendingWorkOrders for deployment " + dpmtId);
    DeploymentContext context = getContext(dpmtId);
    CmsDeployment deployment = context.dpmt;
    if (isActive(deployment)) {
      pendingOrders(context);
    }
    return context;
  }

  protected DeploymentContext getContext(long dpmtId) {
    CmsDeployment deployment = dpmtProcessor.getDeployment(dpmtId);
    return new DeploymentContext(deployment);
  }


  @Override
  protected List<CmsWorkOrderSimple> getOrdersForStep(ExecutionContext context, int step) {
    return cmsClient.getWorkOrderIdsNoLimit(deployment(context), step);
  }

  private CmsDeployment deployment(ExecutionContext context) {
    return ((DeploymentContext) context).dpmt;
  }

  @Override
  protected void updateExecutionWithStep(ExecutionContext context, int newStep, List<CmsWorkOrderSimple> list) {
    CmsDeployment dpmt = deployment(context);
    logger.info("updating deployment " + dpmt.getDeploymentId() + " with current step " + newStep);
    if (dpmt.getCurrentStep() != newStep) {
      dpmt.setCurrentStep(newStep);
      dpmtProcessor.updateDeploymentStep(dpmt);
    }
    if (list != null && !list.isEmpty()) {
      dpmtProcessor.createDeploymentExec(dpmt.getDeploymentId(), newStep, INPROGRESS);
    }
    ((DeploymentContext)context).woList = list;
  }

  @Override
  protected void finishExecution(ExecutionContext context) {
    CmsDeployment dpmt = deployment(context);
    if (isActive(dpmt)) {
      cmsClient.updateDpmtState(dpmt, DPMT_STATE_COMPLETE);
    }
  }

  @Override
  protected boolean needsAutoPause(ExecutionContext context, int step) {
    CmsDeployment dpmt = deployment(context);
    Set<Integer> autoPauseExecOrders = dpmt.getAutoPauseExecOrders();
    return (autoPauseExecOrders != null && autoPauseExecOrders.contains(step));
  }

  @Override
  protected void autoPause(ExecutionContext context, int step) {
    CmsDeployment dpmt = deployment(context);
    updateExecutionWithStep(context, step, null);
    dpmt.setDeploymentState(DPMT_STATE_PAUSED);
    dpmt.setCurrentStep(step);
    dpmt.setUpdatedBy(ONEOPS_SYSTEM_USER);
    dpmt.setComments("deployment paused at step " + step + " on " + new Date());
    try {
      dpmtProcessor.updateDeployment(dpmt);
    } catch (CmsBaseException e) {
      logger.error("CmsBaseException in autoPauseIfRequired", e);
      throw e;
    }
  }


  private boolean isActive(CmsDeployment dpmt) {
    return (dpmt != null) && DPMT_STATE_ACTIVE.equals(dpmt.getDeploymentState());
  }

  private boolean isComplete(CmsDeployment dpmt) {
    return (dpmt != null) && DPMT_STATE_COMPLETE.equals(dpmt.getDeploymentState());
  }

  private boolean isPending(CmsDpmtRecord dpmtRecord) {
    return (dpmtRecord != null) && DPMT_STATE_PENDING.equals(dpmtRecord.getDpmtRecordState());
  }

  private void dispatchWorkOrders(DeploymentContext context) {
    CmsDeployment dpmt = context.dpmt;
    logger.info("dispatching wos - dpmtId : " + dpmt.getDeploymentId() + ", step " + dpmt.getCurrentStep() + " wo size " + context.woList);
    dispatchOrders(context, context.woList);
  }


  private void dispatchOrders(DeploymentContext context, List<CmsWorkOrderSimple> ordersList) {
    CountDownLatch latch = new CountDownLatch(ordersList.size());
    ordersList.forEach(o -> {
      dispatch(context, o, latch);
    });
    context.latch = latch;
  }

  @Override
  protected void dispatch(ExecutionContext context, CmsWorkOrderSimple wo, CountDownLatch latch) {
    woDispatchExecutor.submit(() -> assembleAndDispatchAsync(context, wo, latch));
  }

  private void assembleAndDispatchAsync(ExecutionContext context, CmsWorkOrderSimple wo, CountDownLatch latch) {
    CmsDeployment dpmt = deployment(context);
    WorkOrderContext woContext = new WorkOrderContext(wo, dpmt.getCurrentStep());
    CmsDpmtRecord dpmtRecord = dpmtProcessor.getDeploymentRecord(wo.getDpmtRecordId());
    if (isPending(dpmtRecord)) {
      logger.info(">>>>>>>>>>> dispatching workorder dpmtId : " + dpmt.getDeploymentId() + " rfc : "
          + dpmtRecord.getRfcId());
      woDispatcher.dispatchAndUpdate(dpmt, woContext);
    } else {
      logger.info(
          "workorder not in pending state dpmtId : " + dpmt.getDeploymentId() + " rfcId : " + wo
              .getRfcId() + " state : " + dpmtRecord.getDpmtRecordState());
    }
    latch.countDown();
  }

  /**
   * updates wo state based on inductor response, if the state is successful the rfc would be
   * promoted to ci, also checks if we need to move to next step
   */
  @Override
  @Transactional
  public void handleInductorResponse(CmsWorkOrderSimpleBase wo, Map<String, Object> params) {
    CmsWorkOrderSimple woResponse = (CmsWorkOrderSimple) wo;
    long dpmtId = woResponse.getDeploymentId();
    long rfcId = woResponse.getRfcId();
    String logPrefix = "dpmtId " + dpmtId + " rfc " + rfcId;
    logger.info(logPrefix + "<<<<<<<<<<< deployer handle inductor response");
    
    updateWoState(dpmtId, woResponse, params);
    logger.info(logPrefix + "ci updated from inductor response");

    logger.info(logPrefix + " inductor response processing finished");
  }

  @Override
  @Transactional
  public void convergeIfNeeded(CmsWorkOrderSimpleBase wo) throws JMSException {
    CmsWorkOrderSimple woResponse = (CmsWorkOrderSimple) wo;
    long dpmtId = woResponse.getDeploymentId();
    long rfcId = woResponse.getRfcId();
    int step = woResponse.rfcCi.getExecOrder();
    if (canConverge(dpmtId, rfcId, step)) {
      //send a jms message to controller.workflow queue to proceed to next step
      logger.info("dpmtId " + dpmtId + " rfc " + rfcId + ": inductor response converging to next step");
      sendMessageToProceed(DEPLOYMENT.getName(), dpmtId);
    }
  }

  /**
   * checks if step converge can happen [no workorders in pending/in-progress state for this step].
   */
  private boolean canConverge(long dpmtId, long rfcId, int step) {
    long startTs = System.currentTimeMillis();
    String logPrefix = "dpmtId:" + dpmtId + " step:" + step + " rfc:" + rfcId + " :: ";
    boolean canConverge = false;
    Map<String, Integer> woCountMap = dpmtProcessor.getWorkordersCountByState(dpmtId, step);
    logger.info(logPrefix + "workorders state count: " + woCountMap);
    if (anyPendingOrActiveOrder(woCountMap)) {
      logger.info(logPrefix + "can't converge as there are some workorders in pending/active state");
    }
    else {
      int updated = 0;
      if (anyFailed(woCountMap)) {
        logger.info(logPrefix + "trying to update step as failed");
        updated = dpmtProcessor.getAndUpdateStepState(dpmtId, step, FAILED);
        if (updated > 0) {
          CmsDeployment deployment = dpmtProcessor.getDeployment(dpmtId);
          if (!deployment.getContinueOnFailure()) {
            deployment.setDeploymentState(DPMT_STATE_FAILED);
            //if any of the wo has failed then update the deployment to failed
            dpmtProcessor.updateDeployment(deployment);
          }
          else {
            canConverge = true;
          }
        }
      }
      else if (!anyCancelled(woCountMap)) {
        logger.info(logPrefix + "trying to update step as complete");
        updated = dpmtProcessor.getAndUpdateStepState(dpmtId, step, COMPLETE);
        canConverge = updated > 0;
      }
      logger.info(logPrefix + "getAndUpdateStepState updated count " + updated);
    }
    logger.info(logPrefix + "canConverge took " + timeElapsed(startTs) + " ms");
    return canConverge;
  }

  public void updateWoState(long dpmtId, CmsWorkOrderSimple woResponse,
      Map<String, Object> params) {
    CmsDeployment dpmt = dpmtProcessor.getDeployment(dpmtId);
    String state = (String) params.get(CmsConstants.WORK_ORDER_STATE);
    logger.info(
        "updating workorder state dpmt " + dpmtId + " rfc " + woResponse.getRfcId() + " state "
            + state);
    cmsClient.updateWoState(dpmt, woResponse, state, null);
  }

  @PreDestroy
  public void destroy() {
    woDispatchExecutor.shutdown();
  }

  @Override
  protected String getType() {
    return DEPLOYMENT.getName();
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

}
