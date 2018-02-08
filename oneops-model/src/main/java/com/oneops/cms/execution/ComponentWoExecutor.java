package com.oneops.cms.execution;

import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import java.util.List;

public interface ComponentWoExecutor {

  public List<String> getComponentClasses();

  public Response execute(CmsWorkOrderSimple wo);

  public Response verify(CmsWorkOrderSimple wo, Response response);

  public default Response executeAndVerify(CmsWorkOrderSimple wo) {
    Response response = execute(wo);
    if (response.getResult() == Result.SUCCESS) {
      response = verify(wo, response);
    }
    return response;
  }

  public Response execute(CmsActionOrderSimple ao);

}