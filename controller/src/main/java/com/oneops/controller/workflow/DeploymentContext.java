package com.oneops.controller.workflow;

import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DeploymentContext implements ExecutionContext {

  CmsDeployment dpmt;
  List<CmsWorkOrderSimple> woList;
  boolean completed;
  CountDownLatch latch;

  DeploymentContext(CmsDeployment dpmt) {
    this.dpmt = dpmt;
  }

  @Override
  public int getMaxSteps() {
    return dpmt.getMaxExecOrder();
  }

  @Override
  public int getCurrentStep() {
    return dpmt.getCurrentStep();
  }

  @Override
  public long getExecutionId() {
    return dpmt.getDeploymentId();
  }

  @Override
  public String getType() {
    return ExecutionType.DEPLOYMENT.getName();
  }

}
