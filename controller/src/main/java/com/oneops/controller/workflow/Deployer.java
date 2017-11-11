package com.oneops.controller.workflow;

import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.workflow.WorkflowMessage;
import java.util.Map;
import javax.jms.JMSException;

public interface Deployer {

  public void deploy(CmsDeployment dpmt);

  public void processWorkflow(WorkflowMessage wfMessage);

  public DeployerContext fetchPendingWorkOrders(long dpmtId, boolean wait4WoDispatch);

  public void handleInductorResponse(CmsWorkOrderSimpleBase wo, Map<String, Object> params) throws JMSException;

  public boolean canConverge(long dpmtId, long rfcId, int step);

}
