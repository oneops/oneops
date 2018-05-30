package com.oneops.inductor;

import static com.oneops.inductor.InductorConstants.DELETE;

import com.google.gson.Gson;
import com.oneops.cms.cm.ops.domain.OpsActionState;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.execution.Response;
import com.oneops.cms.execution.Result;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.simple.domain.Instance;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WoHelper {

  private static final String REALIZED_AS = "RealizedAs";
  public static final String FAILED = "failed";

  private static final Logger logger = Logger.getLogger(WoHelper.class);

  @Autowired
  Gson gson;

  public CmsRfcCISimple getRealizedAs(CmsWorkOrderSimple wo) {
    if (wo.getPayLoad().containsKey(REALIZED_AS)) {
      return wo.getPayLoad().get(REALIZED_AS).get(0);
    }
    return null;
  }

  public String getLogKey(CmsWorkOrderSimpleBase wo) {
    return wo.getRecordId() + ":" + wo.getCiId() + " - ";
  }

  public String getVerifyLogKey(CmsWorkOrderSimple wo) {
    return wo.getDpmtRecordId() + ":" + wo.getRfcCi().getCiId() + " - verify -> ";
  }

  public void failWo(CmsWorkOrderSimpleBase wo, String logKey, String message, Exception e) {
    String logMsg = (e != null) ? logKey + message + " : " + e.getMessage() : logKey + message;
    logger.error(logMsg, e);
    if (wo instanceof  CmsWorkOrderSimple) {
      ((CmsWorkOrderSimple)wo).setDpmtRecordState(FAILED);
    }
    else {
      ((CmsActionOrderSimple)wo).setActionState(OpsActionState.failed);
    }
    wo.setComments(message +  (e != null ? " caused by - " + e.getMessage() : ""));
  }

  public boolean isFailed(CmsWorkOrderSimple wo) {
    return FAILED.equals(wo.getDpmtRecordState());
  }

  public Response formResponse(CmsWorkOrderSimple wo, String logKey) {
    Response response = new Response();
    Map<String, String> map = new HashMap<>();
    String responseCode = "200";
    if (FAILED.equals(wo.getDpmtRecordState())) {
      logger.warn(logKey + "FAIL: " + wo.getDpmtRecordId() + " state:" + wo.getDpmtRecordState());
      response.setResult(Result.FAILED);
      responseCode = "500";
    }
    else {
      mergeRfcWithResult(wo.getRfcCi(), wo.getResultCi() != null ? wo.getResultCi() : new CmsCISimple());
      logger.info(logKey + "Workorder execution successful");
      response.setResult(Result.SUCCESS);
    }
    map.put("task_result_code", responseCode);
    logger.info(logKey + "wo restult ci " + gson.toJson(wo.getResultCi()));
    map.put("body", gson.toJson(wo));
    response.setResponseMap(map);
    return response;
  }

  public Response formResponse(CmsActionOrderSimple ao, String logKey) {
    Response response = new Response();
    Map<String, String> map = new HashMap<>();
    String responseCode = "200";
    if (ao.getActionState() == OpsActionState.failed) {
      logger.warn(logKey + "FAIL: " + ao.getProcedureId() + " state:" + ao.getActionState());
      response.setResult(Result.FAILED);
      responseCode = "500";
    }
    else {
      logger.info(logKey + "Actionorder execution successful");
      response.setResult(Result.SUCCESS);
    }
    map.put("task_result_code", responseCode);
    map.put("body", gson.toJson(ao));
    response.setResponseMap(map);
    return response;
  }

  private void mergeRfcWithResult(CmsRfcCISimple rfc, CmsCISimple result) {
    result.getAttrProps().putAll(rfc.getCiAttrProps());

    Map<String, String> rfcAttrs = rfc.getCiAttributes();

    if (result.getCiAttributes() == null) {
      result.setCiAttributes(new HashMap<>());
    }
    Map<String, String> resultAttrs = result.getCiAttributes();
    for (String key : rfcAttrs.keySet()) {
      if (!resultAttrs.containsKey(key)) {
        resultAttrs.put(key, rfcAttrs.get(key));
      }
    }
  }

  public Map<String, String> getResultCiAttributes(CmsWorkOrderSimple wo) {
    if (wo.resultCi == null) {
      CmsCISimple ci = new CmsCISimple();
      CmsRfcCISimple rfc = wo.getRfcCi();
      ci.setCiId(rfc.getCiId());
      ci.setCiName(rfc.getCiName());
      ci.setCiClassName(rfc.getCiClassName());
      wo.setResultCi(ci);
    }
    return wo.resultCi.getCiAttributes();
  }

  public Instance getLbFromDependsOn(CmsWorkOrderSimple wo) {
    return getLb(wo.getPayLoad().get("DependsOn"));
  }

  public Instance getLbFromDependsOn(CmsActionOrderSimple ao) {
    return getLb(ao.getPayLoad().get("DependsOn"));
  }

  private Instance getLb(List<? extends Instance> dependsOn) {
    if (dependsOn != null) {
      Optional<? extends Instance> opt = dependsOn.stream().filter(rfc -> "bom.oneops.1.Lb".equals(rfc.getCiClassName())).findFirst();
      if (opt.isPresent()) {
        return opt.get();
      }
    }
    return null;
  }

  public boolean isDeleteAction(CmsWorkOrderSimple wo) {
    return DELETE.equals(wo.getAction());
  }

}