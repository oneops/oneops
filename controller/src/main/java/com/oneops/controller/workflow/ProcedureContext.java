package com.oneops.controller.workflow;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcedureContext implements ExecutionContext {

  CmsOpsProcedure procedure;
  boolean isCompleted;
  List<CmsActionOrder> aoList;
  Map<Long, CmsCI> manifestToTemplateMap;

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

  @Override
  public void setCompleted(boolean completed) {
    isCompleted = true;
  }
}
