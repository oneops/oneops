package com.oneops.gslb;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oneops.cms.execution.Response;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.gslb.v2.domain.BaseResponse;
import com.oneops.gslb.v2.domain.Cloud;
import com.oneops.gslb.v2.domain.CreateMtdBaseRequest;
import com.oneops.gslb.v2.domain.DataCenter;
import com.oneops.gslb.v2.domain.DataCentersResponse;
import com.oneops.gslb.v2.domain.MtdBase;
import com.oneops.gslb.v2.domain.MtdBaseHostRequest;
import com.oneops.gslb.v2.domain.MtdBaseHostResponse;
import com.oneops.gslb.v2.domain.MtdBaseRequest;
import com.oneops.gslb.v2.domain.MtdBaseResponse;
import com.oneops.gslb.v2.domain.MtdDeployment;
import com.oneops.gslb.v2.domain.MtdHost;
import com.oneops.gslb.v2.domain.MtdHostHealthCheck;
import com.oneops.gslb.v2.domain.MtdTarget;
import com.oneops.gslb.v2.domain.ResponseError;
import com.oneops.gslb.v2.domain.Version;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Call;

@Component
public class MtdHandler {

  private static final String MTDB_TYPE_GSLB = "GSLB";

  private static final String CLOUD_STATUS_ACTIVE = "active";
  private static final String ATTRIBUTE_CLOUD_STATUS = "base.Consumes.adminstatus";
  private static final String ATTRIBUTE_CLOUD_PRIORITY = "base.Consumes.priority";
  private static final String ATTRIBUTE_DNS_RECORD = "dns_record";

  private static final String MTD_BASE_EXISTS_ERROR = "DB_UNIQUENESS_VIOLATION";
  private static final String MTD_HOST_EXISTS_ERROR = "MTD_HOST_EXISTS_ON_MTD_BASE";
  private static final String MTD_HOST_NOT_EXISTS_ERROR = "COULD_NOT_FIND_MTD_HOST";

  private Gson gson = new Gson();
  private JsonParser jsonParser = new JsonParser();

  private static final Logger logger = Logger.getLogger(MtdHandler.class);

  ConcurrentMap<String, Cloud> cloudMap = new ConcurrentHashMap<>();

  @Autowired
  WoHelper woHelper;

  public Response setupTorbitGdns(CmsWorkOrderSimple wo, Config config) {
    String logKey = woHelper.getLogKey(wo);
    try {
      Context context = getContext(wo, config, logKey);
      context.logKey = logKey;
      logger.info(logKey + "FQDNExecutor executing workorder dpmt " + wo.getDeploymentId() + " action : " + wo.rfcCi.getRfcAction());
      if (woHelper.isAddAction(wo)) {
        addGslb(wo, context);
      }
      else if(woHelper.isDeleteAction(wo)) {
        deleteGslb(wo, context);
      }
      else {
        //update/replace actions
        updateGslb(wo, context);
      }
    } catch(Exception e) {
      woHelper.failWo(wo, logKey,"Exception performing " + wo.getAction() + " GSLB ", e);
    }
    return woHelper.formResponse(wo, logKey);
  }

  private <T extends BaseResponse> Resp<T> execute(Context context, Call<T> call, Class<T> respType) throws IOException, ExecutionException {
    return context.torbitClient.execute(call, respType);
  }

  private void loadDataCenters(Context context) throws Exception {
    Resp<DataCentersResponse> response = execute(context, context.torbitApi.getDataCenters(), DataCentersResponse.class);
    if (response.isSuccessful()) {
      List<DataCenter> dataCenters= response.getBody().getDataCenters();
      dataCenters.stream().flatMap(d -> d.getClouds().stream()).forEach(c -> cloudMap.put(c.getName(), c));
    }
    else {
      throw new ExecutionException("Failed while loading data centers " + getErrorMessages(response.getBody()));
    }
  }

  private void deleteGslb(CmsWorkOrderSimple wo, Context context) {
    try {
      TorbitApi torbit = context.torbitClient.getTorbit();
      MtdBase mtdBase = getMTDBase(context);
      if (mtdBase != null) {
        Resp<MtdBaseHostResponse> response = execute(context, torbit.deletetMTDHost(mtdBase.getMtdBaseId(), context.platform), MtdBaseHostResponse.class);
        if (response.isSuccessful()) {
          MtdBaseHostResponse hostResponse = response.getBody();
          logger.info(context.logKey + "delete MTDHost response " + hostResponse);
        }
        else {
          MtdBaseHostResponse errorResp = response.getBody();
          logger.info(context.logKey + "delete MTDHost response code " + response.getCode() + " message " + response.getBody());
          if (errorMatches(errorResp.getErrors(), MTD_HOST_NOT_EXISTS_ERROR)) {
            logger.info(context.logKey + "MTDHost does not exist.");
          }
          else {
            String error = getErrorMessage(errorResp.getErrors());
            logger.info(context.logKey + "deleteMTDHost failed with  error " + error);
            woHelper.failWo(wo, context.logKey, "delete operation failed", null);
          }
        }
      }
      else {
        logger.info(context.logKey + "MTDBase not found for " + context.mtdBaseHost);
      }
    } catch (Exception e) {
      woHelper.failWo(wo, context.logKey, "Exception deleting GSLB ", e);
    }
  }

  private void updateGslb(CmsWorkOrderSimple wo, Context context) {
    try {
      MtdBase mtdBase = getMTDBase(context);
      if (mtdBase != null) {
        updateMTDHost(context, wo, mtdBase);
      }
      else {
        woHelper.failWo(wo, context.logKey, "MTDBase doesn't exist", null);
      }

    } catch (Exception e) {
      woHelper.failWo(wo, context.logKey, "Exception updating GSLB ", e);
    }
  }

  private void addGslb(CmsWorkOrderSimple wo, Context context) {
    try {
      MtdBase mtdBase = createMTDBaseWithRetry(context);
      if (mtdBase != null) {
        createMTDHost(context, wo, mtdBase);
      }
      else {
        woHelper.failWo(wo, context.logKey, "MTDBase could not be created", null);
      }
    } catch (Exception e) {
      woHelper.failWo(wo, context.logKey, "Exception adding GSLB ", e);
    }
  }

  private MtdBaseHostRequest mtdBaseHostRequest(Context context, CmsWorkOrderSimple wo) throws Exception {
    List<MtdTarget> targets = getMTDTargets(wo, context);
    List<MtdHostHealthCheck> healthChecks = getHealthChecks(wo, context);
    MtdHost mtdHost = new MtdHost().mtdHostName(context.platform).
        mtdTargets(targets).mtdHealthChecks(healthChecks).
        loadBalancingDistribution(1).isDcFailover(true);
    return new MtdBaseHostRequest().mtdHost(mtdHost);
  }


  private void createMTDHost(Context context, CmsWorkOrderSimple wo, MtdBase mtdBase) throws Exception {
    MtdBaseHostRequest mtdbHostRequest = mtdBaseHostRequest(context, wo);
    logger.info(context.logKey + "create host request " + mtdbHostRequest);
    Resp<MtdBaseHostResponse> response = execute(context, context.torbitApi.createMTDHost(mtdbHostRequest, mtdBase.getMtdBaseId()), MtdBaseHostResponse.class);
    MtdBaseHostResponse hostResponse = response.getBody();
    if (!response.isSuccessful()) {
      logger.info(context.logKey + "create MTDHost error response " + hostResponse);
      if (errorMatches(hostResponse.getErrors(), MTD_HOST_EXISTS_ERROR)) {
        logger.info(context.logKey + "MTDHost already existing, so trying to update");
        hostResponse = updateMTDHost(context, mtdbHostRequest, mtdBase);
      }
      else {
        String error = getErrorMessage(hostResponse.getErrors());
        throw new ExecutionException("createMTDHost failed with " + error);
      }
    }
    else {
      logger.info(context.logKey + "create MTDHost response  " + hostResponse);
    }
    if (hostResponse != null) {
      updateWoResult(wo, context, mtdBase, hostResponse);
    }
  }

  private void  updateWoResult(CmsWorkOrderSimple wo, Context context, MtdBase mtdBase, MtdBaseHostResponse response) {
    Map<String, String> resultAttrs = getResultCiAttributes(wo);
    Map<String, String> mtdMap = new HashMap<>();
    mtdMap.put("mtd_base_id", Integer.toString(mtdBase.getMtdBaseId()));
    Version version = response.getVersion();
    if (version != null) {
      mtdMap.put("mtd_version", Integer.toString(version.getVersionId()));
    }
    MtdDeployment deployment = response.getDeployment();
    if (deployment != null) {
      mtdMap.put("deploy_id", Integer.toString(deployment.getDeploymentId()));
    }
    mtdMap.put("glb", context.platform + mtdBase.getMtdBaseName());
    resultAttrs.put("gslb_map", gson.toJson(mtdMap));
    resultAttrs.put(FqdnExecutor.ATTRIBUTE_SERVICE_TYPE, "torbit");
  }

  private MtdBaseHostResponse updateMTDHost(Context context, MtdBaseHostRequest mtdbHostRequest, MtdBase mtdBase) throws Exception {
    logger.info(context.logKey + " update host request " + mtdbHostRequest);
    Resp<MtdBaseHostResponse> response = execute(context,
        context.torbitApi.updateMTDHost(mtdbHostRequest, mtdBase.getMtdBaseId(), mtdbHostRequest.getMtdHost().getMtdHostName()), MtdBaseHostResponse.class);
    MtdBaseHostResponse hostResponse = response.getBody();
    if (response.isSuccessful()) {
      logger.info(context.logKey + "update MTDHost response " + hostResponse);
      return hostResponse;
    }
    else {
      logger.info(context.logKey + "update MTDHost response code " + response.getCode() + " message " + hostResponse);
      String error = getErrorMessage(hostResponse.getErrors());
      throw new ExecutionException("updateMTDHost failed with " + error);
    }
  }

  private void updateMTDHost(Context context, CmsWorkOrderSimple wo, MtdBase mtdBase) throws Exception {
    MtdBaseHostRequest mtdbHostRequest = mtdBaseHostRequest(context, wo);
    MtdBaseHostResponse hostResponse = updateMTDHost(context, mtdbHostRequest, mtdBase);
    updateWoResult(wo, context, mtdBase, hostResponse);
  }

  private MtdBase getMTDBase(Context context) throws IOException, ExecutionException {
    MtdBase mtdBase = null;
    Resp<MtdBaseResponse> response = execute(context, context.torbitApi.getMTDBase(context.mtdBaseHost), MtdBaseResponse.class);
    if (!response.isSuccessful()) {
      logger.info(context.logKey + "MTDBase could not be read for " + context.mtdBaseHost + " error " + getErrorMessages(response.getBody()));
    }
    else {
      mtdBase = response.getBody().getMtdBase();
    }
    logger.info(context.logKey + "mtdBase for host " + context.mtdBaseHost + " " + mtdBase);
    return mtdBase;
  }

  private MtdBase createMTDBaseWithRetry(Context context) throws Exception {
    int totalRetries = 2;
    MtdBase mtdBase = null;
    for (int retry = 0 ; mtdBase == null && retry < totalRetries ; retry++) {
      if (retry > 0) {
        Thread.sleep(5000);
      }
      try {
        mtdBase = createOrGetMTDBase(context);
      } catch (ExecutionException e) {
        throw e;
      } catch (Exception e) {
        logger.error(context.logKey + "MTDBase creation failed for " + context.mtdBaseHost + ", retry count " + retry, e);
      }
    }
    return mtdBase;
  }

  private MtdBase createOrGetMTDBase(Context context) throws Exception {
    CreateMtdBaseRequest request = new CreateMtdBaseRequest().mtdBase(new MtdBaseRequest().mtdBaseName(context.mtdBaseHost).type(MTDB_TYPE_GSLB));
    logger.info(context.logKey + "MTDBase create request " + request);
    Resp<MtdBaseResponse> response = execute(context, context.torbitApi.createMTDBase(request, context.config.getGroupId()), MtdBaseResponse.class);
    MtdBase mtdBase = null;
    if (!response.isSuccessful()) {
      MtdBaseResponse mtdBaseResponse = response.getBody();
      logger.info(context.logKey + "create MTDBase error response " + mtdBaseResponse);
      if (errorMatches(mtdBaseResponse.getErrors(), MTD_BASE_EXISTS_ERROR)) {
        logger.info(context.logKey + "create MTDBase failed with unique violation. try to get it.");
        //check if a MTDBase record exists already, probably created by another FQDN instance running parallel
        mtdBase = getMTDBase(context);
      }
      else {
        logger.info(context.logKey + "create MTDBase request failed with unknown error");
      }
    }
    else {
      logger.info(context.logKey + "MTDBase create response " + response);
      mtdBase = response.getBody().getMtdBase();
    }
    return mtdBase;
  }

  private Map<String, String> getResultCiAttributes(CmsWorkOrderSimple wo) {
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

  List<MtdHostHealthCheck> getHealthChecks(CmsWorkOrderSimple wo, Context context) {
    List<CmsRfcCISimple> dependsOn = wo.getPayLoad().get("DependsOn");
    List<MtdHostHealthCheck> hcList = new ArrayList<>();
    if (dependsOn != null) {
      Optional<CmsRfcCISimple> opt = dependsOn.stream().filter(rfc -> "bom.oneops.1.Lb".equals(rfc.getCiClassName())).findFirst();
      if (opt.isPresent()) {
        CmsRfcCISimple lb = opt.get();
        Map<String, String> attributes = lb.getCiAttributes();

        if (attributes.containsKey("listeners") && (attributes.containsKey("ecv_map"))) {
          String ecv = attributes.get("ecv_map");
          JsonElement element = jsonParser.parse(ecv);

          if (element instanceof JsonObject) {
            JsonObject root = (JsonObject) element;
            Set<Entry<String, JsonElement>> set = root.entrySet();
            Map<Integer, String> ecvMap = set.stream().
                collect(Collectors.toMap(s -> Integer.parseInt(s.getKey()), s -> s.getValue().getAsString()));
            logger.info(context.logKey + "listeners " + attributes.get("listeners"));
            JsonArray listeners = (JsonArray) jsonParser.parse(attributes.get("listeners"));

            listeners.forEach(s -> {
              String listener = s.getAsString();
              String[] config = listener.split(" ");
              if (config.length >= 2) {
                String protocol = config[0];
                int port = Integer.parseInt(config[1]);
                String healthConfig = ecvMap.get(port);

                if ((protocol.startsWith("http"))) {
                  if (healthConfig != null) {
                    logger.info(context.logKey + "healthConfig : " + healthConfig);
                    String path = healthConfig.substring(healthConfig.indexOf(" ")+1);
                    MtdHostHealthCheck healthCheck = newHealthCheck("gslb-" + protocol + "-" + port, protocol, port);
                    healthCheck.testObjectPath(path).expectedStatus(200);
                    hcList.add(healthCheck);
                  }
                }
                else if ("tcp".equals(protocol)) {
                  hcList.add(newHealthCheck("gslb-" + protocol + "-" + port, protocol, port));
                }
              }
            });
          }
        }
      }
    }
    return hcList;
  }

  private MtdHostHealthCheck newHealthCheck(String name, String protocol, int port) {
    MtdHostHealthCheck healthCheck = new MtdHostHealthCheck().name(name).protocol(protocol).port(port);
    //TODO: make the following configurable
    healthCheck.failsForDown(10).pass(true);//.interval("20s").retryDelay("30s").timeout("10s");
    return healthCheck;
  }

  List<MtdTarget> getMTDTargets(CmsWorkOrderSimple wo, Context context) throws Exception {
    List<LbCloud> lbClouds = getLbCloudMerged(wo);

    if (lbClouds != null && !lbClouds.isEmpty()) {
      Map<Integer, List<LbCloud>> map = lbClouds.stream().collect(Collectors.groupingBy(l -> l.isPrimary ? 1 : 2));
      List<LbCloud> primaryClouds = map.get(1);
      List<LbCloud> secondaryClouds = map.get(2);
      CmsRfcCISimple manifestFqdn = woHelper.getRealizedAs(wo);
      String distribution = manifestFqdn.getCiAttributes().get("distribution");
      boolean isProximity = "proximity".equals(distribution);
      logger.info(context.logKey + "distribution config for fqdn : " + distribution);
      int weight = 0;
      if (!isProximity) {
        weight = 100;
      }

      List<MtdTarget> targetList = new ArrayList<>();
      if (primaryClouds != null) {
        for (LbCloud lbCloud : primaryClouds) {
          MtdTarget target = baseTarget(lbCloud, context).enabled(true);
          if (!isProximity) {
            target.setWeightPercent(weight);
          }
          targetList.add(target);
        }
      }

      if (secondaryClouds != null) {
        for (LbCloud lbCloud : secondaryClouds) {
          MtdTarget target = baseTarget(lbCloud, context);
          target.setEnabled(false);
          target.setWeightPercent(0);
          targetList.add(target);
        }
      }
      return targetList;
    }
    else {
      throw new ExecutionException("Can't get cloud VIPs from workorder");
    }

  }

  private MtdTarget baseTarget(LbCloud lbCloud, Context context) throws Exception {
    if (!cloudMap.containsKey(lbCloud.cloud)) {
      loadDataCenters(context);
    }
    Cloud cloud = cloudMap.get(lbCloud.cloud);
    return new MtdTarget().mtdTargetHost(lbCloud.dnsRecord).cloudId(cloud.getId()).dataCenterId(cloud.getDataCenterId());
  }

  private List<LbCloud> getLbCloudMerged(CmsWorkOrderSimple wo) throws Exception {
    Map<String, List<CmsRfcCISimple>> map = wo.getPayLoad();
    List<CmsRfcCISimple> lbs = map.get(WoHelper.LB_PAYLOAD);
    if (map.containsKey("clouds")) {
      Map<Long, CmsRfcCISimple> cloudCiMap = map.get("clouds").stream()
          .collect(Collectors.toMap(c -> c.getCiId(), Function.identity()));
      List<LbCloud> list = lbs.stream().map(lb -> getLbWithCloud(lb, cloudCiMap)).collect(Collectors.toList());
      return list;
    }
    else {
      logger.error("clouds payload not found in workorder");
      throw new ExecutionException("clouds payload not found in workorder");
    }
  }

  private LbCloud getLbWithCloud(CmsRfcCISimple lb, Map<Long, CmsRfcCISimple> cloudCiMap) {
    LbCloud lc = new LbCloud();
    String lbName = lb.getCiName();
    String[] elements = lbName.split("-");
    String cloudId = elements[elements.length - 2];
    CmsRfcCISimple cloudCi = cloudCiMap.get(Long.parseLong(cloudId));
    lc.cloud = cloudCi.getCiName();
    lc.dnsRecord = lb.getCiAttributes().get(ATTRIBUTE_DNS_RECORD);

    Map<String, String> cloudAttributes = cloudCi.getCiAttributes();
    lc.isPrimary = "1".equals(cloudAttributes.get(ATTRIBUTE_CLOUD_PRIORITY)) &&
        CLOUD_STATUS_ACTIVE.equals(cloudAttributes.get(ATTRIBUTE_CLOUD_STATUS));
    return lc;
  }

  private String getErrorMessages(BaseResponse response) {
    if (response != null) {
      return getErrorMessage(response.getErrors());
    }
    return null;
  }

  private String getErrorMessage(List<ResponseError> errors) {
    String message = null;
    if (errors != null) {
      message = errors.stream().map(ResponseError::getErrorCode).collect(Collectors.joining(" | "));
    }
    return message != null ? message : "unknown error";
  }

  private boolean errorMatches(List<ResponseError> responseError, String error) {
    if (responseError != null) {
      Optional<ResponseError> matchinError = responseError.stream().
          filter(r -> error.equals(r.getErrorCode())).findFirst();
      if (matchinError.isPresent()) {
        return true;
      }
    }
    return false;
  }

  private Context getContext(CmsWorkOrderSimple wo, Config config, String logKey) throws Exception {
    Map<String, List<CmsRfcCISimple>> payload = wo.getPayLoad();
    Context context = new Context();
    context.assembly = payload.get("Assembly").get(0).getCiName();
    context.platform = wo.getBox().getCiName();
      CmsRfcCISimple env = payload.get("Environment").get(0);
    context.environment = env.getCiName();
    context.org = payload.get("Organization").get(0).getCiName();
    context.subdomain = env.getCiAttributes().get("subdomain");
    context.cloud = wo.cloud.getCiName();
    context.baseGslbDomain = wo.services.get("gdns").get(context.cloud).getCiAttributes().get("gslb_base_domain");
    if (context.subdomain != null)
      context.mtdBaseHost = "." + context.subdomain + "." + context.baseGslbDomain;
    else
      context.mtdBaseHost = String.join(".", "." + context.environment, context.assembly,
          context.org, context.baseGslbDomain);
    TorbitClient torbitClient = new TorbitClient(config);
    context.torbitClient = torbitClient;
    context.torbitApi = torbitClient.getTorbit();
    context.config = config;

    logger.info(logKey + "Context - assembly : " + context.assembly + ", platform : " + context.platform + ", env : " +
        context.environment + ", org : " + context.org + ", subdomain : " + context.subdomain + ", cloud : " +
        context.cloud + ", baseGslbDomain : " + context.baseGslbDomain + ", mtdBaseHost : " + context.mtdBaseHost);
    return context;
  }

  static class Context {
    String platform;
    String environment;
    String assembly;
    String org;
    String cloud;
    String subdomain;
    String baseGslbDomain;
    String mtdBaseHost;
    TorbitClient torbitClient;
    TorbitApi torbitApi;
    Config config;
    String logKey;
  }

  class LbCloud {
    String dnsRecord;
    String cloud;
    boolean isPrimary;
  }

}
