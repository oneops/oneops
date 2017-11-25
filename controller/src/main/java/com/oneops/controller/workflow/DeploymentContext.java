package com.oneops.controller.workflow;

import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import java.util.List;

public class DeploymentContext implements ExecutionContext {

  CmsDeployment dpmt;
  List<CmsWorkOrderSimple> woList;
  boolean completed;

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
    return Deployer.DEPLOYMENT_TYPE;
  }

  @Override
  public void setCompleted(boolean completed) {
    this.completed = true;
  }
}
