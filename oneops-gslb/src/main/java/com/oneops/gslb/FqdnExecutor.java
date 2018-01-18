package com.oneops.gslb;

import com.oneops.cms.execution.ComponentWoExecutor;
import com.oneops.cms.execution.Response;
import com.oneops.cms.execution.Result;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FqdnExecutor implements ComponentWoExecutor {

  private static String FQDN_CLASS = "bom.oneops.1.Fqdn";
  private static final String GDNS_SERVICE = "gdns";
  private static final String ATTRIBUTE_USER = "user_name";
  private static final String ATTRIBUTE_ENDPOINT = "endpoint";
  private static final String ATTRIBUTE_AUTH_KEY = "auth_key";
  private static final String ATTRIBUTE_GROUP_ID = "group_id";

  static final String ATTRIBUTE_SERVICE_TYPE = "service_type";

  private static final String TORBIT_SERVICE_CLASS = "cloud.service.oneops.1.Torbit";

  private static final Logger logger = Logger.getLogger(FqdnExecutor.class);

  @Autowired
  MtdHandler mtdHandler;

  @Autowired
  WoHelper woHelper;

  @Autowired
  MtdVerifier mtdVerifier;

  @Override
  public List<String> getComponentClasses() {
    return Arrays.asList(FQDN_CLASS);
  }

  @Override
  public Response execute(CmsWorkOrderSimple wo) {
    if (wo.getClassName().equals(FQDN_CLASS) && isTorbitServiceType(wo)) {
      Config config = getTorbitConfig(wo);
      if (config != null) {
        return mtdHandler.setupTorbitGdns(wo, config);
      }
    }
    logger.info("wo " + wo.rfcCi.getRfcId() + " deployment " + wo.getDeploymentId() + " - fqdn does not have torbit service type");
    return Response.getNotMatchingResponse();
  }

  @Override
  public Response verify(CmsWorkOrderSimple wo, Response response) {
    Config config = getTorbitConfig(wo);
    if (config != null) {
      return mtdVerifier.verify(wo, response, config);
    }
    Response failResp = new Response();
    failResp.setResult(Result.FAILED);
    return failResp;
  }

  private boolean isTorbitServiceType(CmsWorkOrderSimple wo) {
    CmsRfcCISimple realizedAs = woHelper.getRealizedAs(wo);
    if (realizedAs != null) {
      String serviceType = realizedAs.getCiAttributes().get(ATTRIBUTE_SERVICE_TYPE);
      logger.info("fqdn service type  " + serviceType);
      return "torbit".equals(serviceType);
    }
    return false;
  }

  Config getTorbitConfig(CmsWorkOrderSimple wo) {
    Map<String, Map<String, CmsCISimple>> services = wo.getServices();
    if (services != null && services.containsKey(GDNS_SERVICE)) {
      Map<String, CmsCISimple> gdnsService = services.get(GDNS_SERVICE);
      CmsCISimple gdns;
      //proceed only if the gdns service has torbit and there is lb payload
      if ((gdns = gdnsService.get(wo.cloud.getCiName())) != null && wo.getPayLoad().containsKey(WoHelper.LB_PAYLOAD)) {
        if (TORBIT_SERVICE_CLASS.equals(gdns.getCiClassName())) {
          return getTorbitConfig(gdns);
        }
      }
      logger.info(wo.getCiId() + " - workorder does not have required elements - lb, torbit gdns");
    }
    else {
      logger.info(wo.getCiId() + " - gdns service not found in workorder");
    }
    return null;
  }


  private Config getTorbitConfig(CmsCISimple torbitCI) {
    Map<String, String> attributes = torbitCI.getCiAttributes();
    Config config = null;
    if (attributes.containsKey(ATTRIBUTE_ENDPOINT) && attributes.containsKey(ATTRIBUTE_AUTH_KEY)
        && attributes.containsKey(ATTRIBUTE_USER)) {
      config = new Config().
          url(attributes.get(ATTRIBUTE_ENDPOINT)).
          user(attributes.get(ATTRIBUTE_USER)).
          authKey(attributes.get(ATTRIBUTE_AUTH_KEY)).
          groupId(Integer.parseInt(attributes.get(ATTRIBUTE_GROUP_ID)));
    }
    return config;
  }

}
