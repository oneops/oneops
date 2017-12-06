package com.oneops.controller.workflow;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class ProcedureContext implements ExecutionContext {

  CmsOpsProcedure procedure;
  boolean isCompleted;
  List<CmsActionOrderSimple> aoList;
  Map<Long, CmsCI> manifestToTemplateMap;
  CountDownLatch latch;

  ProcedureContext(CmsOpsProcedure procedure) {
    this.procedure = procedure;
    manifestToTemplateMap = new ConcurrentHashMap<>();
  }

  @Override
  public int getMaxSteps() {
    return procedure.getMaxExecOrder();
  }

  @Override
  public int getCurrentStep() {
    return procedure.getCurrentStep();
  }

  @Override
  public long getExecutionId() {
    return procedure.getProcedureId();
  }

  @Override
  public String getType() {
    return "procedure";
  }

}
