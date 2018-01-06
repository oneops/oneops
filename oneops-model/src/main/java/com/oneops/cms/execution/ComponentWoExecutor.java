package com.oneops.cms.execution;

import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import java.util.List;

public interface ComponentWoExecutor {

  public List<String> getComponentClasses();

  public Response execute(CmsWorkOrderSimple wo);

}