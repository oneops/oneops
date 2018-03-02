package com.oneops.gslb;

import com.google.gson.Gson;
import com.oneops.cms.execution.ComponentWoExecutor;
import com.oneops.cms.execution.Response;
import com.oneops.cms.execution.Result;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FqdnExecutor implements ComponentWoExecutor {

  private static String FQDN_CLASS = "bom.oneops.1.Fqdn";
  private static final String SERVICE_TYPE_TORBIT = "torbit";
  private static final String ATTRIBUTE_USER = "user_name";
  private static final String ATTRIBUTE_ENDPOINT = "endpoint";
  private static final String ATTRIBUTE_AUTH_KEY = "auth_key";
  private static final String ATTRIBUTE_GROUP_ID = "group_id";
  private static final String ATTRIBUTE_GDNS = "global_dns";

  static final String ATTRIBUTE_SERVICE_TYPE = "service_type";

  private static final String TORBIT_SERVICE_CLASS = "cloud.service.oneops.1.Torbit";
  private static final String PAYLOAD_ENVIRONMENT = "Environment";

  private static final Logger logger = Logger.getLogger(FqdnExecutor.class);

  @Autowired
  MtdHandler mtdHandler;

  @Autowired
  WoHelper woHelper;

  @Autowired
  FqdnVerifier verifier;

  @Autowired
  DnsHandler dnsHandler;

  @Autowired
  Gson gsonPretty;

  @Override
  public List<String> getComponentClasses() {
    return Arrays.asList(FQDN_CLASS);
  }

  @Override
  public Response execute(CmsWorkOrderSimple wo, String dataDir) {
    String logKey = woHelper.getLogKey(wo);
    if (wo.getClassName().equals(FQDN_CLASS) && isLocalWo(wo) && isGdnsEnabled(wo) && isTorbitServiceType(wo)) {
      Context context = getContext(wo, logKey);
      Config config = getTorbitConfig(wo, logKey);
      if (config != null) {
        try {
          String fileName = dataDir + "/" + wo.getDpmtRecordId() + ".json";
          writeRequest(gsonPretty.toJson(wo), fileName);
          logger.info(logKey + "FqdnExecutor executing workorder dpmt " + wo.getDeploymentId() + " action : " + wo.rfcCi.getRfcAction());
          mtdHandler.setupTorbitGdns(wo, config, context);
          if (!woHelper.isFailed(wo)) {
            dnsHandler.setupDnsEntries(wo, context);
          }
        } catch (Exception e) {
          woHelper.failWo(wo, logKey, "Exception setting up fqdn ", e);
        }
        return woHelper.formResponse(wo, logKey);
      }
    }
    logger.info(logKey + "not executing by FqdnExecutor as these conditions are not met :: "
        + "[fqdn service_type set as torbit && gdns enabled for env && local workorder && torbit cloud service configured]");
    return Response.getNotMatchingResponse();
  }

  private boolean isLocalWo(CmsWorkOrderSimple wo) {
    return !wo.isPayLoadEntryPresent("ManagedVia");
  }

  @Override
  public Response verify(CmsWorkOrderSimple wo, Response response) {
    Config config = getTorbitConfig(wo, "");
    if (config != null) {
      return verifier.verify(wo, response, config);
    }
    Response failResp = new Response();
    failResp.setResult(Result.FAILED);
    return failResp;
  }

  private boolean isTorbitServiceType(CmsWorkOrderSimple wo) {
    CmsRfcCISimple realizedAs = woHelper.getRealizedAs(wo);
    if (realizedAs != null) {
      String serviceType = realizedAs.getCiAttributes().get(ATTRIBUTE_SERVICE_TYPE);
      logger.info(wo.getCiId() + " : fqdn service type  " + serviceType);
      return "torbit".equals(serviceType) &&
          wo.getServices() != null && wo.getServices().containsKey(SERVICE_TYPE_TORBIT);
    }
    return false;
  }

  private boolean isGdnsEnabled(CmsWorkOrderSimple wo) {
    if (wo.isPayLoadEntryPresent(PAYLOAD_ENVIRONMENT)) {
      CmsRfcCISimple env = wo.getPayLoadEntryAt(PAYLOAD_ENVIRONMENT, 0);
      Map<String, String> attrs = env.getCiAttributes();
      if (attrs.containsKey(ATTRIBUTE_GDNS)) {
        return "true".equals(attrs.get(ATTRIBUTE_GDNS));
      }
    }
    return false;
  }

  Config getTorbitConfig(CmsWorkOrderSimple wo, String logKey) {
    Map<String, Map<String, CmsCISimple>> services = wo.getServices();
    if (services != null && services.containsKey(SERVICE_TYPE_TORBIT)) {
      Map<String, CmsCISimple> gdnsService = services.get(SERVICE_TYPE_TORBIT);
      CmsCISimple gdns;
      //proceed only if the gdns service has torbit and there is lb payload
      if ((gdns = gdnsService.get(wo.cloud.getCiName())) != null && wo.getPayLoad().containsKey(WoHelper.LB_PAYLOAD)) {
        if (TORBIT_SERVICE_CLASS.equals(gdns.getCiClassName())) {
          return getTorbitConfig(gdns);
        }
      }
      logger.info(logKey + "workorder does not have required elements - lb, torbit gdns");
    }
    else {
      logger.info(logKey + "torbit service not found in workorder");
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

  private Context getContext(CmsWorkOrderSimple wo, String logKey) {
    Map<String, List<CmsRfcCISimple>> payload = wo.getPayLoad();
    Context context = new Context();
    context.setAssembly(payload.get("Assembly").get(0).getCiName());
    context.setPlatform(wo.getBox().getCiName().toLowerCase());
    CmsRfcCISimple env = payload.get("Environment").get(0);
    context.setEnvironment(env.getCiName());
    context.setOrg(payload.get("Organization").get(0).getCiName());
    String subdomain = env.getCiAttributes().get("subdomain");
    context.setSubdomain(StringUtils.isNotBlank(subdomain) ? subdomain :
        String.join(".", context.getEnvironment(), context.getAssembly(), context.getOrg()));

    context.setCloud(wo.cloud.getCiName());
    context.setBaseGslbDomain(wo.services.get(SERVICE_TYPE_TORBIT).get(context.getCloud()).getCiAttributes().get("gslb_base_domain"));
    context.setLogKey(logKey);

    logger.info(logKey + "Context - assembly : " + context.getAssembly() + ", platform : " + context.getPlatform() + ", env : " +
        context.getEnvironment() + ", org : " + context.getOrg() + ", subdomain : " + context.getSubdomain() + ", cloud : " +
        context.getCloud() + ", baseGslbDomain : " + context.getBaseGslbDomain());
    return context;
  }

  @Override
  public Response execute(CmsActionOrderSimple ao) {
    return Response.getNotMatchingResponse();
  }

}
