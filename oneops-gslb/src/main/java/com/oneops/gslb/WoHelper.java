package com.oneops.gslb;

import com.google.gson.Gson;
import com.oneops.cms.execution.Response;
import com.oneops.cms.execution.Result;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class WoHelper {

  private static final String REALIZED_AS = "RealizedAs";
  public static final String LB_PAYLOAD = "lb";
  public static final String CLOUDS_PAYLOAD = "fqdnclouds";
  public static final String FAILED = "failed";

  private static final String ACTION_ADD = "add";
  private static final String ACTION_DELETE = "delete";

  private Gson gson = new Gson();

  private static final Logger logger = Logger.getLogger(WoHelper.class);

  public CmsRfcCISimple getRealizedAs(CmsWorkOrderSimple wo) {
    if (wo.getPayLoad().containsKey(REALIZED_AS)) {
      return wo.getPayLoad().get(REALIZED_AS).get(0);
    }
    return null;
  }

  public String getLogKey(CmsWorkOrderSimple wo) {
    return wo.getDpmtRecordId() + ":" + wo.getRfcCi().getCiId() + " - ";
  }

  public String getVerifyLogKey(CmsWorkOrderSimple wo) {
    return wo.getDpmtRecordId() + ":" + wo.getRfcCi().getCiId() + " - verify -> ";
  }

  public void failWo(CmsWorkOrderSimple wo, String logKey, String message, Exception e) {
    String logMsg = (e != null) ? logKey + message + " : " + e.getMessage() : logKey + message;
    logger.error(logMsg, e);
    wo.setDpmtRecordState(FAILED);
    wo.setComments(message +  (e != null ? " caused by - " + e.getMessage() : ""));
  }

  public boolean isFailed(CmsWorkOrderSimple wo) {
    return FAILED.equals(wo.getDpmtRecordState());
  }

  public Response formResponse(CmsWorkOrderSimple wo, String logKey) {
    Response response = new Response();
    Map<String, String> map = new HashMap<>();
    map.put("body", gson.toJson(wo));
    String responseCode = "200";
    response.setResponseMap(map);

    if (FAILED.equals(wo.getDpmtRecordState())) {
      logger.warn(logKey + "FAIL: " + wo.getDpmtRecordId() + " state:" + wo.getDpmtRecordState());
      response.setResult(Result.FAILED);
      responseCode = "500";
    }
    else {
      logger.info(logKey + "Workorder execution successful");
      response.setResult(Result.SUCCESS);
    }
    map.put("task_result_code", responseCode);
    return response;
  }

  public boolean isAddAction(CmsWorkOrderSimple wo) {
    return ACTION_ADD.equals(wo.getAction());
  }

  public boolean isDeleteAction(CmsWorkOrderSimple wo) {
    return ACTION_DELETE.equals(wo.getAction());
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

}
