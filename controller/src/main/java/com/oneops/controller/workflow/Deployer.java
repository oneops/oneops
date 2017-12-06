package com.oneops.controller.workflow;

import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.workflow.WorkflowMessage;
import java.util.Map;
import javax.jms.JMSException;

public interface Deployer {

  public DeploymentContext deploy(CmsDeployment dpmt);

  public DeploymentContext processWorkflow(WorkflowMessage wfMessage);

  public void handleInductorResponse(CmsWorkOrderSimpleBase wo, Map<String, Object> params);

  public void convergeIfNeeded(CmsWorkOrderSimpleBase wo) throws JMSException;

}
