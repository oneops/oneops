package com.oneops.controller.workflow;

import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.workflow.WorkflowMessage;
import java.util.Map;
import javax.jms.JMSException;

public interface
ProcedureRunner {

  public static final String PROCEDURE_TYPE = "procedure";

  public void execute(CmsOpsProcedure procedure);

  public void processWorkflow(WorkflowMessage wfMessage);

  public ProcedureContext getPendingActions(long procedureId);

  public void handleInductorResponse(CmsActionOrderSimple ao, Map<String, Object> params) throws JMSException;

  public boolean canConverge(long procId, long ciId, int step);

}
