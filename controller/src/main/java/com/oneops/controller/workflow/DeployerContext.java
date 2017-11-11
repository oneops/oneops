package com.oneops.controller.workflow;

import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import java.util.List;

public class DeployerContext {

  CmsDeployment dpmt;
  DeploymentStep dpmtStep;
  List<CmsWorkOrderSimple> woList;
  boolean completed;

  DeployerContext(CmsDeployment dpmt) {
    this.dpmt = dpmt;
  }

  DeployerContext(CmsDeployment dpmt, DeploymentStep dpmtStep, List<CmsWorkOrderSimple> woList) {
    this.dpmt = dpmt;
    this.dpmtStep = dpmtStep;
    this.woList = woList;
  }

}
