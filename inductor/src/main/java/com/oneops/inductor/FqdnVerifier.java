package com.oneops.inductor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oneops.cms.execution.Response;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.gslb.GslbVerifier;
import com.oneops.gslb.Status;
import com.oneops.gslb.domain.GslbRequest;
import com.oneops.gslb.domain.GslbResponse;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FqdnVerifier {

  @Autowired
  WoHelper woHelper;

  @Autowired
  JsonParser jsonParser;

  @Autowired
  GslbVerifier verifier;

  private static final Logger logger = Logger.getLogger(FqdnVerifier.class);

  public Response verify(GslbRequest gslbRequest, CmsWorkOrderSimple wo, Response response) {
    try {
      GslbResponse verifyResponse = verifier.verify(gslbRequest, getGslbRespone(wo));
      if (verifyResponse == null || verifyResponse.getStatus() == Status.FAILED) {
        woHelper.failWo(wo, gslbRequest.logContextId(),
            "Failed while verifying : " + verifyResponse != null ? verifyResponse.getFailureMessage() : "", null);
        return woHelper.formResponse(wo, gslbRequest.logContextId());
      }
    } catch (Exception e) {
      woHelper.failWo(wo, gslbRequest.logContextId(), "Failed while verifying : ", e);
      return woHelper.formResponse(wo, gslbRequest.logContextId());
    }
    return response;
  }

  private GslbResponse getGslbRespone(CmsWorkOrderSimple wo) {
    GslbResponse gslbResponse = new GslbResponse();
    CmsCISimple resultCi = wo.getResultCi();
    if (resultCi != null) {
      Map<String, String> attrs = resultCi.getCiAttributes();
      if (attrs.containsKey("gslb_map")) {
        JsonElement element = jsonParser.parse(attrs.get("gslb_map"));
        if (element instanceof JsonObject) {
          JsonObject root = (JsonObject) element;
          gslbResponse.setMtdBaseId(getElementAsString(root, "mtd_base_id"));
          gslbResponse.setMtdVersion(getElementAsString(root, "mtd_version"));
          gslbResponse.setMtdDeploymentId(getElementAsString(root, "deploy_id"));
          gslbResponse.setGlb(getElementAsString(root, "glb"));

        }
      }
      if (attrs.containsKey("entries")) {
        JsonElement element = jsonParser.parse(attrs.get("entries"));
        if (element instanceof JsonObject) {
          JsonObject root = (JsonObject) element;
          gslbResponse.setDnsEntries(root.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getAsString())));
        }
      }
    }
    return gslbResponse;
  }

  private String getElementAsString(JsonObject root, String key) {
    JsonElement element = root.get(key);
    return element != null ? element.getAsString() : null;
  }

}
