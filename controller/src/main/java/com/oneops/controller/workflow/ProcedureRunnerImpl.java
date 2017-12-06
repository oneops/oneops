package com.oneops.controller.workflow;

import static com.oneops.cms.cm.ops.domain.OpsActionState.inprogress;
import static com.oneops.cms.cm.ops.domain.OpsProcedureState.active;
import static com.oneops.cms.cm.ops.domain.OpsProcedureState.complete;
import static com.oneops.cms.cm.ops.domain.OpsProcedureState.failed;
import static com.oneops.controller.workflow.ExecutionType.PROCEDURE;

import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsActionState;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.cm.ops.service.OpsProcedureProcessor;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.controller.cms.CMSClient;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

@Component(value = "procedureRunner")
public class ProcedureRunnerImpl extends Execution<CmsActionOrderSimple> implements ProcedureRunner {

  @Autowired
  OpsProcedureProcessor procProcessor;

  @Autowired
  CMSClient cmsClient;

  @Autowired
  WoDispatcher woDispatcher;

  @Value("${oo.controller.ao.assembler.pool.size:2}")
  private int aoAssemblerPoolSize;

  private ThreadPoolExecutor aoDispatchExecutor;

  private static Logger logger = Logger.getLogger(ProcedureRunnerImpl.class);

  private static final String ACTION_ORDER_TPYE = "opsprocedure";

  @PostConstruct
  public void init() {
    aoDispatchExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(aoAssemblerPoolSize);
  }


  @Override
  @Transactional
  public ProcedureContext executeProcedure(long procedureId) {
    ProcedureContext context = pendingActions(procedureId);
    if (!context.isCompleted && context.aoList != null && !context.aoList.isEmpty()) {
      CmsOpsProcedure procedure = procedure(context);
      logger.info("dispatching aos - procedure " + procedureId + " step " + procedure.getCurrentStep() + " ao size " + context.aoList.size());
      context.latch = dispatchOrders(context, context.aoList);
    }
    return context;
  }

  protected CountDownLatch dispatchOrders(ProcedureContext context, List<CmsActionOrderSimple> ordersList) {
    CountDownLatch latch = new CountDownLatch(ordersList.size());
    ordersList.forEach(o -> {
      dispatch(context, o, latch);
    });
    return latch;
  }


  private ProcedureContext pendingActions(long procedureId) {
    CmsOpsProcedure procedure = procProcessor.getCmsOpsProcedure(procedureId, false);
    ProcedureContext context = new ProcedureContext(procedure);
    if (procedure.getProcedureState() == OpsProcedureState.active) {
      pendingOrders(context);
    }
    return context;
  }

  @Override
  protected void dispatch(ExecutionContext context, CmsActionOrderSimple order,
      CountDownLatch latch) {
    aoDispatchExecutor.submit(() -> dispatchAsync(context, order, latch));
  }

  private void dispatchAsync(ExecutionContext context, CmsActionOrderSimple aoAssembled, CountDownLatch latch) {
    try {
      aoAssembled.getSearchTags().put(CmsConstants.DEPLOYMENT_MODEL, CmsConstants.DEPLOYMENT_MODEL_DEPLOYER);
      logger.info(">>>>>>>>>>> dispatching actionorder dpmtId : " + context.getExecutionId() + " ci : "
          + aoAssembled.getCiId());
      woDispatcher.publishMessage(aoAssembled, ACTION_ORDER_TPYE);
      cmsClient.updateActionOrderState(aoAssembled, inprogress);
    } catch (Exception e) {
      logger.error("exception in dispatchAsync procedure " + aoAssembled.getProcedureId() + " action " + aoAssembled.getActionId(), e);
      //cmsClient.failActionOrder(ao);
    }
    latch.countDown();
  }

  @Override
  @Transactional
  public void handleInductorResponse(CmsActionOrderSimple ao, Map<String, Object> params) {
    long procId = ao.getProcedureId();
    long ciId = ao.getCiId();
    String logPrefix = "procedure " + procId + " ci " + ciId;
    logger.info(logPrefix + "<<<<<<<<<<< ProcRunner handle inductor response");
    updateAoState(ao, params);
  }

  @Override
  @Transactional
  public void convergeIfNeeded(CmsActionOrderSimple ao) throws JMSException {
    long procId = ao.getProcedureId();
    long ciId = ao.getCiId();
    String logPrefix = "procedure " + procId + " ci " + ciId;
    int step = ao.getExecOrder();
    if (canConverge(ao.getProcedureId(), ao.getCiId(), step)) {
      //send a jms message to controller.workflow queue to proceed to next step
      logger.info("procedure " + procId + " ciId " + ciId + ": inductor response converging to next step");
      sendMessageToProceed(PROCEDURE.getName(), procId);
    }
    logger.info(logPrefix + " inductor response processing finished");
  }

  /**
   * checks if step converge can happen [no actionorders in pending/in-progress state for this step].
   */
  private boolean canConverge(long procId, long ciId, int step) {
    long startTs = System.currentTimeMillis();
    String logPrefix = "procId:" + procId + " step " + step + " ciId:" + ciId+ " :: ";
    boolean canConverge = false;
    Map<String, Integer> aoCountMap = procProcessor.getActionsCountByState(procId, step);
    logger.info(logPrefix + "actions state count: " + aoCountMap);
    if (anyPendingOrActiveOrder(aoCountMap)) {
      logger.info(logPrefix + "can't converge as there are some action orders in pending/active state");
    }
    else {
      int updated;
      if (anyFailed(aoCountMap)) {
        logger.info(logPrefix + "trying to update step as failed");
        updated = procProcessor.getAndUpdateStepState(procId, step, failed.getName());
        if (updated > 0) {
          //if any of the ao has failed then update the procedure to failed
          procProcessor.updateProcedureState(procId, OpsProcedureState.failed);
        }
      }
      else {
        logger.info(logPrefix + "trying to update step as complete");
        updated = procProcessor.getAndUpdateStepState(procId, step, complete.getName());
        canConverge = updated > 0;
      }
      logger.info(logPrefix + "getAndUpdateStepState updated count " + updated);
    }
    logger.info(logPrefix + "canConverge took " + timeElapsed(startTs) + " ms");
    return canConverge;
  }


  private void updateAoState(CmsActionOrderSimple aoResponse, Map<String, Object> params) {
    String state = (String) params.get(CmsConstants.WORK_ORDER_STATE);
    logger.info(
        "updating procedure state proc " + aoResponse.getProcedureId() + " action " + aoResponse.getActionId() + " state "
            + state);
    cmsClient.updateActionOrderState(aoResponse, OpsActionState.valueOf(state));
  }


  @PreDestroy
  public void destroy() {
    aoDispatchExecutor.shutdown();
  }

  @Override
  protected void updateExecutionWithStep(ExecutionContext context, int newStep,
      List<CmsActionOrderSimple> list) {
    CmsOpsProcedure procedure = procedure(context);
    logger.info("updating procedure " + procedure.getProcedureId() + " with current step " + newStep);
    if (procedure.getCurrentStep() != newStep) {
      procedure.setCurrentStep(newStep);
      procProcessor.updateProcedureCurrentStep(procedure);
    }
    if (list != null && !list.isEmpty()) {
      procProcessor.createProcedureExec(procedure.getProcedureId(), newStep, active.getName());
    }
    ((ProcedureContext)context).aoList = list;
  }

  @Override
  protected void autoPause(ExecutionContext context, int step) {

  }

  @Override
  protected boolean needsAutoPause(ExecutionContext context, int step) {
    return false;
  }

  @Override
  protected void finishExecution(ExecutionContext context) {
    CmsOpsProcedure procedure = procedure(context);
    if (procedure.getProcedureState() == OpsProcedureState.active) {
      procProcessor.updateProcedureState(procedure.getProcedureId(), OpsProcedureState.complete);
    }
  }

  private CmsOpsProcedure procedure(ExecutionContext context) {
    return ((ProcedureContext)context).procedure;
  }

  @Override
  protected List<CmsActionOrderSimple> getOrdersForStep(ExecutionContext context, int step) {
    CmsOpsProcedure procedure = procedure(context);
    try {
      return cmsClient.getActionOrders(procedure, step);
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
    return Collections.emptyList();
  }

  @Override
  protected String getType() {
    return PROCEDURE.getName();
  }

}
