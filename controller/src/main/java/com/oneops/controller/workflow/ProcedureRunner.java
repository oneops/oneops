package com.oneops.controller.workflow;

import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import java.util.Map;
import javax.jms.JMSException;

public interface ProcedureRunner {

  public ProcedureContext executeProcedure(long procedureId);

  public void handleInductorResponse(CmsActionOrderSimple ao, Map<String, Object> params);

  public void convergeIfNeeded(CmsActionOrderSimple ao) throws JMSException;

}
