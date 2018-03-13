package com.oneops.inductor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oneops.cms.execution.ComponentWoExecutor;
import com.oneops.cms.execution.Response;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.gslb.domain.Action;
import com.oneops.gslb.domain.Cloud;
import com.oneops.gslb.domain.DeployedLb;
import com.oneops.gslb.domain.Fqdn;
import com.oneops.gslb.GslbExecutor;
import com.oneops.gslb.domain.GslbRequest;
import com.oneops.gslb.domain.GslbResponse;
import com.oneops.gslb.domain.InfobloxConfig;
import com.oneops.gslb.domain.LbConfig;
import com.oneops.gslb.Status;
import com.oneops.gslb.domain.TorbitConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
  private static final String ATTRIBUTE_GSLB_BASE_DOMAIN = "gslb_base_domain";
  private static final String ATTRIBUTE_GDNS = "global_dns";
  private static final String ATTRIBUTE_PLATFORM_ENABLED = "is_platform_enabled";

  static final String ATTRIBUTE_SERVICE_TYPE = "service_type";

  private static final String TORBIT_SERVICE_CLASS = "cloud.service.oneops.1.Torbit";
  private static final String PAYLOAD_ENVIRONMENT = "Environment";

  public static final String LB_PAYLOAD = "lb";
  public static final String CLOUDS_PAYLOAD = "fqdnclouds";
  public static final String ATTRIBUTE_DNS_RECORD = "dns_record";
  private static final String ATTRIBUTE_CLOUD_STATUS = "base.Consumes.adminstatus";
  private static final String ATTRIBUTE_CLOUD_PRIORITY = "base.Consumes.priority";
  private static final String ATTRIBUTE_LISTENERS = "listeners";
  private static final String ATTRIBUTE_ECV_MAP = "ecv_map";

  private static final String ATTRIBUTE_DNS_HOST = "host";
  private static final String ATTRIBUTE_DNS_USER_NAME = "username";
  private static final String ATTRIBUTE_DNS_PASSWORD = "password";
  private static final String ATTRIBUTE_DNS_ZONE = "zone";

  private static final String ATTRIBUTE_ALIAS = "aliases";
  private static final String ATTRIBUTE_FULL_ALIAS = "full_aliases";
  private static final String ATTRIBUTE_DISTRIBUTION = "distribution";

  private static final Logger logger = Logger.getLogger(FqdnExecutor.class);

  @Autowired
  GslbExecutor gslbExecutor;

  @Autowired
  WoHelper woHelper;

  @Autowired
  Gson gson;

  @Autowired
  FqdnVerifier fqdnVerifier;

  Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();

  @Override
  public List<String> getComponentClasses() {
    return Arrays.asList(FQDN_CLASS);
  }

  @Override
  public Response execute(CmsWorkOrderSimple wo, String dataDir) {
    String logKey = woHelper.getLogKey(wo);
    GslbResponse response;
    if (wo.getClassName().equals(FQDN_CLASS) && isLocalWo(wo) && isGdnsEnabled(wo) && isTorbitServiceType(wo)) {
      try {
        TorbitConfig torbitConfig = getTorbitConfig(wo, logKey);
        InfobloxConfig infobloxConfig = getInfobloxConfig(wo);
        if (torbitConfig != null && infobloxConfig != null) {
          logger.info(logKey + "FqdnExecutor executing workorder dpmt " + wo.getDeploymentId() + " action : " + wo.rfcCi.getRfcAction());
          String fileName = dataDir + "/" + wo.getDpmtRecordId() + ".json";
          writeRequest(gsonPretty.toJson(wo), fileName);
          response = gslbExecutor.execute(getGslbRequestFromWo(wo, torbitConfig, infobloxConfig, logKey));
          updateWoResult(wo, response, logKey);
        }
        else {
          woHelper.failWo(wo, logKey, "TorbitConfig/InfobloxConfig could not be obtained, Please check cloud service configuration", null);
        }
      }
      catch (Exception e) {
        woHelper.failWo(wo, logKey, "Exception setting up fqdn ", e);
      }
      return woHelper.formResponse(wo, logKey);
    }
    logger.info(logKey + "not executing by FqdnExecutor as these conditions are not met :: "
        + "[fqdn service_type set as torbit && gdns enabled for env && local workorder && torbit cloud service configured]");
    return Response.getNotMatchingResponse();
  }

  @Override
  public Response verify(CmsWorkOrderSimple wo, Response response) {
    String logKey = woHelper.getLogKey(wo);
    TorbitConfig torbitConfig = getTorbitConfig(wo, logKey);
    InfobloxConfig infobloxConfig = getInfobloxConfig(wo);
    return fqdnVerifier.verify(getGslbRequestFromWo(wo, torbitConfig, infobloxConfig, logKey), wo, response);
  }

  private void updateWoResult(CmsWorkOrderSimple wo, GslbResponse response, String logKey) {
    if (response != null) {
      if (response.getStatus() == Status.SUCCESS) {
        Map<String, String> resultAttrs = woHelper.getResultCiAttributes(wo);
        Map<String, String> mtdMap = new HashMap<>();
        mtdMap.put("mtd_base_id", response.getMtdBaseId());
        mtdMap.put("mtd_version", response.getMtdVersion());
        mtdMap.put("deploy_id", response.getMtdDeploymentId());
        mtdMap.put("glb", response.getGlb());
        resultAttrs.put("gslb_map", gson.toJson(mtdMap));
        resultAttrs.put("entries", gson.toJson(response.getDnsEntries()));
      }
      else {
        woHelper.failWo(wo, logKey, response.getFailureMessage(), null);
      }
    }
  }

  private GslbRequest getGslbRequestFromWo(CmsWorkOrderSimple wo, TorbitConfig torbitConfig, InfobloxConfig infobloxConfig, String logKey) {

    List<DeployedLb> deployedLbs = getDeployedLb(wo);
    List<Cloud> platformClouds = getPlatformClouds(wo);
    Map<String, List<CmsRfcCISimple>> payload = wo.getPayLoad();
    CmsRfcCISimple env = payload.get("Environment").get(0);
    CmsCISimple cloud = wo.getCloud();
    Map<String, String> fqdnAttrs = wo.getRfcCi().getCiAttributes();
    Map<String, String> fqdnBaseAttrs = wo.getRfcCi().getCiBaseAttributes();
    GslbRequest request = GslbRequest.builder().
        action(Action.valueOf(wo.getAction())).
        platform(wo.getBox().getCiName()).
        environment(env.getCiName()).
        assembly(payload.get("Assembly").get(0).getCiName()).
        org(payload.get("Organization").get(0).getCiName()).
        customSubdomain(env.getCiAttributes().get("subdomain")).
        deployedLbs(deployedLbs).
        platformClouds(platformClouds).
        lbConfig(getLbConfig(wo)).
        logContextId(logKey).
        cloud(Cloud.create(cloud.getCiId(), cloud.getCiName(), null, null, torbitConfig, infobloxConfig)).
        platformEnabled(isPlatformEnabled(wo)).
        fqdn(Fqdn.create(fqdnAttrs.get(ATTRIBUTE_ALIAS), fqdnAttrs.get(ATTRIBUTE_FULL_ALIAS), fqdnAttrs.get(ATTRIBUTE_DISTRIBUTION))).
        oldFqdn(fqdnBaseAttrs != null ?
            Fqdn.create(fqdnBaseAttrs.get(ATTRIBUTE_ALIAS), fqdnBaseAttrs.get(ATTRIBUTE_FULL_ALIAS), fqdnBaseAttrs.get(ATTRIBUTE_DISTRIBUTION)) :
            null).build();
    return request;
  }


  private InfobloxConfig getInfobloxConfig(CmsWorkOrderSimple wo) {
    Map<String, CmsCISimple> dnsServices = wo.getServices().get("dns");
    if (dnsServices != null) {
      CmsCISimple dns = dnsServices.get(wo.getCloud().getCiName());
      if (dns != null) {
        Map<String, String> attributes = dns.getCiAttributes();
        String host = attributes.get(ATTRIBUTE_DNS_HOST);
        String user = attributes.get(ATTRIBUTE_DNS_USER_NAME);
        String pwd = attributes.get(ATTRIBUTE_DNS_PASSWORD);
        String zone = attributes.get(ATTRIBUTE_DNS_ZONE);
        if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(user)) {
          return InfobloxConfig.create(host, user, pwd, zone);
        }
      }
    }
    return null;
  }

  private boolean isPlatformEnabled(CmsWorkOrderSimple wo) {
    boolean isPlatformDisabled = false;
    Map<String, String> platformAttributes = wo.getBox().getCiAttributes();
    if (platformAttributes.containsKey(ATTRIBUTE_PLATFORM_ENABLED)) {
      isPlatformDisabled = "false".equals(platformAttributes.get(ATTRIBUTE_PLATFORM_ENABLED));
    }
    return !isPlatformDisabled;
  }

  private LbConfig getLbConfig(CmsWorkOrderSimple wo) {
    CmsRfcCISimple lb = woHelper.getLbFromDependsOn(wo);
    if (lb == null) {
      throw new RuntimeException("DependsOn Lb is empty");
    }
    return LbConfig.create(lb.getCiAttributes().get(ATTRIBUTE_LISTENERS), lb.getCiAttributes().get(ATTRIBUTE_ECV_MAP));
  }

  private List<DeployedLb> getDeployedLb(CmsWorkOrderSimple wo) {
    List<CmsRfcCISimple> lbs = wo.getPayLoad().get(LB_PAYLOAD);
    return lbs.stream().
        map(lb -> DeployedLb.create(lb.getCiName(), lb.getCiAttributes().get(ATTRIBUTE_DNS_RECORD))).collect(
        Collectors.toList());
  }

  private List<Cloud> getPlatformClouds(CmsWorkOrderSimple wo) {
    List<CmsRfcCISimple> clouds = wo.getPayLoad().get(CLOUDS_PAYLOAD);
    return clouds.stream().map(this::cloudFromCi).collect(Collectors.toList());
  }

  private Cloud cloudFromCi(CmsRfcCISimple cloudCi) {
    Map<String, String> attrs = cloudCi.getCiAttributes();
    return Cloud.create(cloudCi.getCiId(), cloudCi.getCiName(),
        attrs.get(ATTRIBUTE_CLOUD_PRIORITY), attrs.get(ATTRIBUTE_CLOUD_STATUS), null, null);
  }

  private boolean isLocalWo(CmsWorkOrderSimple wo) {
    return !wo.isPayLoadEntryPresent("ManagedVia");
  }

  private boolean isTorbitServiceType(CmsWorkOrderSimple wo) {
    CmsRfcCISimple realizedAs = woHelper.getRealizedAs(wo);
    if (realizedAs != null) {
      String serviceType = realizedAs.getCiAttributes().get(ATTRIBUTE_SERVICE_TYPE);
      logger.info(wo.getCiId() + " : fqdn service type  " + serviceType);
      return "torbit".equals(serviceType);
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

  private TorbitConfig getTorbitConfig(CmsWorkOrderSimple wo, String logKey) {
    Map<String, Map<String, CmsCISimple>> services = wo.getServices();
    if (services != null && services.containsKey(SERVICE_TYPE_TORBIT)) {
      Map<String, CmsCISimple> gdnsService = services.get(SERVICE_TYPE_TORBIT);
      CmsCISimple gdns;
      if ((gdns = gdnsService.get(wo.cloud.getCiName())) != null) {
        if (TORBIT_SERVICE_CLASS.equals(gdns.getCiClassName())) {
          Map<String, String> attributes = gdns.getCiAttributes();
          if (attributes.containsKey(ATTRIBUTE_ENDPOINT) && attributes.containsKey(ATTRIBUTE_AUTH_KEY)
              && attributes.containsKey(ATTRIBUTE_USER)) {
            return TorbitConfig.create(attributes.get(ATTRIBUTE_ENDPOINT), attributes.get(ATTRIBUTE_USER),
                attributes.get(ATTRIBUTE_AUTH_KEY), Integer.parseInt(attributes.get(ATTRIBUTE_GROUP_ID)),
                attributes.get(ATTRIBUTE_GSLB_BASE_DOMAIN));
          }
        }
      }
      logger.info(logKey + "workorder does not have required elements - torbit gdns service");
    }
    else {
      logger.info(logKey + "torbit service not found in workorder");
    }
    return null;
  }

  @Override
  public Response execute(CmsActionOrderSimple ao) {
    return Response.getNotMatchingResponse();
  }

}
