package com.oneops.gslb;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oneops.cms.execution.ComponentWoExecutor;
import com.oneops.cms.execution.Response;
import com.oneops.cms.execution.Result;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.gslb.v2.domain.Cloud;
import com.oneops.gslb.v2.domain.CreateMTDBaseRequest;
import com.oneops.gslb.v2.domain.DataCenter;
import com.oneops.gslb.v2.domain.DataCentersResponse;
import com.oneops.gslb.v2.domain.DeployMTDConfig;
import com.oneops.gslb.v2.domain.DeployMTDConfigRequest;
import com.oneops.gslb.v2.domain.DeploymentResponse;
import com.oneops.gslb.v2.domain.MTDBHostsVersion;
import com.oneops.gslb.v2.domain.MTDBHostsVersionRequest;
import com.oneops.gslb.v2.domain.MTDBase;
import com.oneops.gslb.v2.domain.MTDBaseHostRequest;
import com.oneops.gslb.v2.domain.MTDBaseHostResponse;
import com.oneops.gslb.v2.domain.MTDBaseRequest;
import com.oneops.gslb.v2.domain.MTDBaseResponse;
import com.oneops.gslb.v2.domain.MTDDeployment;
import com.oneops.gslb.v2.domain.MTDHost;
import com.oneops.gslb.v2.domain.MTDHostHealthCheck;
import com.oneops.gslb.v2.domain.MTDHosts;
import com.oneops.gslb.v2.domain.MTDTarget;
import com.oneops.gslb.v2.domain.ResponseError;
import com.oneops.gslb.v2.domain.Version;
import com.oneops.gslb.v2.domain.VersionRequest;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
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
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Converter;

@Component
public class FqdnExecutor implements ComponentWoExecutor {

  private static String FQDN_CLASS = "bom.oneops.1.Fqdn";
  private static final String GDNS_SERVICE = "gdns";
  private static final String ATTRIBUTE_USER = "user_name";
  private static final String ATTRIBUTE_ENDPOINT = "endpoint";
  private static final String ATTRIBUTE_AUTH_KEY = "auth_key";
  private static final String ATTRIBUTE_GROUP_ID = "group_id";
  private static final String ACTION_ADD = "add";
  private static final String ACTION_DELETE = "delete";

  private static final String GSLB_PREFIX = "c1";
  private static final String DEPLOYMENT_TYPE_LIVE = "live";
  private static final String MTDB_TYPE_GSLB = "GSLB";
  private static final String ATTRIBUTE_SERVICE_TYPE = "service_type";

  private static final String CLOUD_STATUS_ACTIVE = "active";
  private static final String ATTRIBUTE_CLOUD_STATUS = "base.Consumes.adminstatus";
  private static final String ATTRIBUTE_CLOUD_PRIORITY = "base.Consumes.priority";
  private static final String ATTRIBUTE_DNS_RECORD = "dns_record";
  private static final String TORBIT_SERVICE_CLASS = "cloud.service.oneops.1.Torbit";
  private static final String LB_PAYLOAD = "lb";
  private static final String REALIZED_AS = "RealizedAs";

  public static final String COMPLETE = "complete";
  public static final String FAILED = "failed";

  private static final String MTD_BASE_EXISTS_ERROR = "DB_UNIQUENESS_VIOLATION";
  private static final String MTD_HOST_EXISTS_ERROR = "MTD_HOST_EXISTS_ON_MTD_BASE";
  private static final String MTD_HOST_NOT_EXISTS_ERROR = "COULD_NOT_FIND_MTD_HOST";


  private static final Logger logger = Logger.getLogger(FqdnExecutor.class);

  private Gson gson = new Gson();
  private JsonParser jsonParser = new JsonParser();

  @Override
  public List<String> getComponentClasses() {
    return Arrays.asList(FQDN_CLASS);
  }

  private ConcurrentMap<String, Cloud> cloudMap = new ConcurrentHashMap<>();

  @Override
  public Response execute(CmsWorkOrderSimple wo) {

    if (wo.getClassName().equals(FQDN_CLASS) && isTorbitServiceType(wo)) {
      TorbitConfig config = getTorbitConfig(wo);
      if (config != null) {
        return setupTorbitGdns(wo, config);
      }
    }
    logger.info("wo " + wo.rfcCi.getRfcId() + " deployment " + wo.getDeploymentId() + " - fqdn does not have torbit service type");
    return Response.getNotMatchingResponse();
  }

  private boolean isTorbitServiceType(CmsWorkOrderSimple wo) {
    CmsRfcCISimple realizedAs = getRealizedAs(wo);
    if (realizedAs != null) {
      String serviceType = realizedAs.getCiAttributes().get(ATTRIBUTE_SERVICE_TYPE);
      logger.info("fqdn service type  " + serviceType);
      return "torbit".equals(serviceType);
    }
    return false;
  }

  private TorbitConfig getTorbitConfig(CmsWorkOrderSimple wo) {
    Map<String, Map<String, CmsCISimple>> services = wo.getServices();
    if (services != null && services.containsKey(GDNS_SERVICE)) {
      Map<String, CmsCISimple> gdnsService = services.get(GDNS_SERVICE);
      CmsCISimple gdns;
      //proceed only if the gdns service has torbit and there is lb payload
      if ((gdns = gdnsService.get(wo.cloud.getCiName())) != null && wo.getPayLoad().containsKey(LB_PAYLOAD)) {
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

  private void loadDataCenters(TorbitApi torbitApi) throws Exception {
    DataCentersResponse dcResponse = execute(torbitApi.getDataCenters());
    List<DataCenter> dataCenters= dcResponse.getDataCenters();
    dataCenters.stream().flatMap(d -> d.getClouds().stream()).forEach(c -> cloudMap.put(c.getName(), c));
  }

  private TorbitConfig getTorbitConfig(CmsCISimple torbitCI) {
    Map<String, String> attributes = torbitCI.getCiAttributes();
    TorbitConfig config = null;
    if (attributes.containsKey(ATTRIBUTE_ENDPOINT) && attributes.containsKey(ATTRIBUTE_AUTH_KEY)
        && attributes.containsKey(ATTRIBUTE_USER)) {
      config = new TorbitConfig().
          url(attributes.get(ATTRIBUTE_ENDPOINT)).
          user(attributes.get(ATTRIBUTE_USER)).
          authKey(attributes.get(ATTRIBUTE_AUTH_KEY)).
          groupId(Integer.parseInt(attributes.get(ATTRIBUTE_GROUP_ID)));
    }
    return config;
  }

  private Response setupTorbitGdns(CmsWorkOrderSimple wo, TorbitConfig config) {
    String logKey = getLogKey(wo);
    try {
      Context context = getContext(wo, config, logKey);
      context.logKey = logKey;
      logger.info(logKey + "FQDNExecutor executing workorder dpmt " + wo.getDeploymentId() + " action : " + wo.rfcCi.getRfcAction());
      if (ACTION_ADD.equals(wo.getAction())) {
        addGslb(wo, context);
      }
      else if(ACTION_DELETE.equals(wo.getAction())) {
        deleteGslb(wo, context);
      }
      else {
        //update/replace actions
        updateGslb(wo, context);
      }
    } catch(Exception e) {
      failWo(wo, logKey,"Exception performing " + wo.getAction() + " GSLB ", e);
    }
    return formResponse(wo, logKey);
  }

  private void failWo(CmsWorkOrderSimple wo, String logKey, String message, Exception e) {
    logger.error(logKey + message, e);
    wo.setDpmtRecordState(FAILED);
    wo.setComments(message +  (e != null ? " caused by - " + e.getMessage() : ""));
  }

  private Response formResponse(CmsWorkOrderSimple wo, String logKey) {
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

  private void deleteGslb(CmsWorkOrderSimple wo, Context context) {
    try {
      TorbitApi torbit = context.torbit.getTorbit();
      MTDBase mtdBase = getMTDBase(context);
      if (mtdBase != null) {
        retrofit2.Response<MTDBaseHostResponse> response = torbit.deletetMTDHost(mtdBase.getMtdBaseId(), context.platform).execute();
        if (response.isSuccessful()) {
          MTDBaseHostResponse hostResponse = response.body();
          logger.info(context.logKey + "delete MTDHost response " + hostResponse);
        }
        else {
          logger.info(context.logKey + "delete MTDHost response code " + response.code() + " message " + response.message() + " error " + response.errorBody());
          MTDBaseHostResponse errorResp = getErrorResponse(MTDBaseHostResponse.class, context, response);
          if (errorMatches(errorResp.getErrors(), MTD_HOST_NOT_EXISTS_ERROR)) {
            logger.info(context.logKey + "MTDHost does not exist.");
          }
          else {
            String error = getErrorMessage(errorResp.getErrors());
            logger.info(context.logKey + "deleteMTDHost failed with  error " + error);
            failWo(wo, context.logKey, "delete operation failed", null);
          }
        }
      }
      else {
        logger.info(context.logKey + "MTDBase not found for " + context.mtdBaseHost);
      }
    } catch (Exception e) {
      failWo(wo, context.logKey, "Exception deleting GSLB ", e);
    }
  }

  private void updateGslb(CmsWorkOrderSimple wo, Context context) {
    try {
      MTDBase mtdBase = getMTDBase(context);
      if (mtdBase != null) {
        updateMTDHost(context, wo, mtdBase);
      }
      else {
        failWo(wo, context.logKey, "MTDBase doesn't exist", null);
      }

    } catch (Exception e) {
      failWo(wo, context.logKey, "Exception updating GSLB ", e);
    }
  }

  private void addGslb(CmsWorkOrderSimple wo, Context context) {
    try {
      MTDBase mtdBase = createMTDBaseWithRetry(context);
      if (mtdBase != null) {
        createMTDHost(context, wo, mtdBase);
      }
      else {
        failWo(wo, context.logKey, "MTDBase could not be created", null);
      }
    } catch (Exception e) {
      failWo(wo, context.logKey, "Exception adding GSLB ", e);
    }
  }

  private MTDBaseHostRequest mtdBaseHostRequest(Context context, CmsWorkOrderSimple wo) throws Exception {
    List<MTDTarget> targets = getMTDTargets(wo, context);
    List<MTDHostHealthCheck> healthChecks = getHealthChecks(wo, context);
    MTDHost mtdHost = new MTDHost().mtdHostName(context.platform).
        mtdTargets(targets).mtdHealthChecks(healthChecks).
        loadBalancingDistribution(1).isDcFailover(true);
    return new MTDBaseHostRequest().mtdHost(mtdHost);
  }

  private TorbitApi getClient(Context context) {
    return context.torbit.getTorbit();
  }

  private void createMTDHost(Context context, CmsWorkOrderSimple wo, MTDBase mtdBase) throws Exception {
    MTDBaseHostRequest mtdbHostRequest = mtdBaseHostRequest(context, wo);
    logger.info(context.logKey + "create host request " + mtdbHostRequest);
    retrofit2.Response<MTDBaseHostResponse> response = getClient(context).createMTDHost(mtdbHostRequest, mtdBase.getMtdBaseId()).execute();
    MTDBaseHostResponse hostResponse = null;
    if (!response.isSuccessful()) {
      if (isHostExisting(context, response)) {
        logger.info(context.logKey + "MTDHost already existing, so trying to update");
        hostResponse = updateMTDHost(context, mtdbHostRequest, mtdBase);
      }
    }
    else {
      hostResponse = response.body();
      logger.info(context.logKey + "create MTDHost response  " + hostResponse);
    }
    if (hostResponse != null) {
      updateWoResult(wo, mtdBase, hostResponse);
    }
  }

  private void  updateWoResult(CmsWorkOrderSimple wo, MTDBase mtdBase, MTDBaseHostResponse response) {
    Map<String, String> resultAttrs = getResultCiAttributes(wo);
    Map<String, String> mtdMap = new HashMap<>();
    mtdMap.put("mtd_base_id", Integer.toString(mtdBase.getMtdBaseId()));
    Version version = response.getVersion();
    if (version != null) {
      mtdMap.put("mtd_version", Integer.toString(version.getVersionId()));
    }
    MTDDeployment deployment = response.getDeployment();
    if (deployment != null) {
      mtdMap.put("deploy_id", Integer.toString(deployment.getDeploymentId()));
    }
    resultAttrs.put("gslb_map", gson.toJson(mtdMap));
  }

  private <T> T getErrorResponse(Type type, Context context, retrofit2.Response<T> response) throws IOException {
    Converter<ResponseBody, T> converter = context.torbit.getRetrofit().responseBodyConverter(type, new Annotation[0]);
    return converter.convert(response.errorBody());
  }

  private boolean isHostExisting(Context context, retrofit2.Response<MTDBaseHostResponse> response) throws Exception {
    MTDBaseHostResponse mtdResponse = getErrorResponse(MTDBaseHostResponse.class, context, response);
    logger.info(context.logKey + "create MTDHost error response " + mtdResponse);
    return errorMatches(mtdResponse.getErrors(), MTD_HOST_EXISTS_ERROR);
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

  private MTDBaseHostResponse updateMTDHost(Context context, MTDBaseHostRequest mtdbHostRequest, MTDBase mtdBase) throws Exception {
    logger.info(context.logKey + " update host request " + mtdbHostRequest);
    retrofit2.Response<MTDBaseHostResponse> response = getClient(context).
        updateMTDHost(mtdbHostRequest, mtdBase.getMtdBaseId(), mtdbHostRequest.getMtdHost().getMtdHostName()).execute();
    if (response.isSuccessful()) {
      MTDBaseHostResponse hostResponse = response.body();
      logger.info(context.logKey + "update MTDHost response " + hostResponse);
      return hostResponse;
    }
    else {
      logger.info(context.logKey + "update MTDHost response code " + response.code() + " message " + response.message() + " error " + response.errorBody());
      MTDBaseHostResponse errorResp = getErrorResponse(MTDBaseHostResponse.class, context, response);
      String error = getErrorMessage(errorResp.getErrors());
      throw new ExecutionException("updateMTDHost failed with " + error);
    }
  }

  private String getErrorMessage(List<ResponseError> errors) {
    String message = null;
    if (errors != null) {
      message = errors.stream().map(ResponseError::getErrorCode).collect(Collectors.joining(" | "));
    }
    return message != null ? message : "unknown error";
  }

  private void updateMTDHost(Context context, CmsWorkOrderSimple wo, MTDBase mtdBase) throws Exception {
    MTDBaseHostRequest mtdbHostRequest = mtdBaseHostRequest(context, wo);
    MTDBaseHostResponse hostResponse = updateMTDHost(context, mtdbHostRequest, mtdBase);
    updateWoResult(wo, mtdBase, hostResponse);
  }

  private MTDBase getMTDBase(Context context) throws IOException {
    MTDBase mtdBase = null;
    MTDBaseResponse mtdBaseResponse = execute(getClient(context).getMTDBase(context.mtdBaseHost));
    if (mtdBaseResponse == null) {
      logger.info(context.logKey + "MTDBase could not be read for " + context.mtdBaseHost);
    }
    else {
      mtdBase = mtdBaseResponse.getMtdBase();
    }
    logger.info(context.logKey + "mtdBase for host " + context.mtdBaseHost + " " + mtdBase);
    return mtdBase;
  }

  private MTDBase createMTDBaseWithRetry(Context context) throws Exception {
    int totalRetries = 2;
    MTDBase mtdBase = null;
    for (int retry = 0 ; mtdBase == null && retry < totalRetries ; retry++) {
      if (retry > 0) {
        Thread.sleep(5000);
      }
      try {
        mtdBase = createOrGetMTDBase(context);
      } catch (IOException e) {
        logger.error(context.logKey + "MTDBase creation failed for " + context.mtdBaseHost + ", retry count " + retry, e);
      }
    }
    return mtdBase;
  }

  private MTDBase createOrGetMTDBase(Context context) throws IOException {
    CreateMTDBaseRequest request = new CreateMTDBaseRequest().mtdBase(new MTDBaseRequest().mtdBaseName(context.mtdBaseHost).type(MTDB_TYPE_GSLB));
    logger.info(context.logKey + "MTDBase create request " + request);
    retrofit2.Response<MTDBaseResponse> response = getClient(context).createMTDBase(request, context.config.getGroupId()).execute();
    MTDBase mtdBase = null;
    if (!response.isSuccessful()) {

      if (isMTDBaseExisting(context, response)) {
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
      mtdBase = response.body().getMtdBase();
    }
    return mtdBase;
  }

  private boolean isMTDBaseExisting(Context context, retrofit2.Response<MTDBaseResponse> response) throws IOException {
    MTDBaseResponse mtdResponse = getErrorResponse(MTDBaseResponse.class, context, response);
    logger.info(context.logKey + "create MTDBase error response " + mtdResponse);
    return errorMatches(mtdResponse.getErrors(), MTD_BASE_EXISTS_ERROR);
  }

  private <T> T execute(Call<T> call) throws IOException {
    retrofit2.Response<T> response = call.execute();
    return response.body();
  }

  private void draftVersionAndDeploy(Context context, CmsWorkOrderSimple wo, MTDBase mtdBase) throws Exception {
    CmsRfcCISimple rfc = wo.rfcCi;
    VersionRequest version = new VersionRequest().description(
        StringUtils.join(new Long[]{rfc.getCiId(), rfc.getReleaseId(), rfc.getRfcId()}, "-"));
    TorbitApi torbit = getClient(context);

    List<MTDTarget> targets = getMTDTargets(wo, context);
    List<MTDHostHealthCheck> healthChecks = getHealthChecks(wo, context);
    MTDHost mtdHost = new MTDHost().mtdHostName(GSLB_PREFIX).
        mtdTargets(targets).mtdHealthChecks(healthChecks).
        loadBalancingDistribution(1).isDcFailover(true);
    MTDHosts hosts = new MTDHosts().addMtdHostItem(mtdHost);

    MTDBHostsVersionRequest versionRequest = new MTDBHostsVersionRequest().
        version(version).
        mtdBaseId(mtdBase.getMtdBaseId()).
        mtdHosts(hosts);

    logger.info(context.logKey + "draft version request : " + versionRequest);

    MTDBHostsVersion versionResponse = execute(torbit.createMTDHostsVersion(versionRequest));
    if (versionResponse == null) {
      failWo(wo, context.logKey,"MTD Host Version creation failed ", null);
    }
    else {
      logger.info(context.logKey + "draft MTD host version response " + versionResponse);
      int versionId = versionResponse.getVersion().getVersionId();

      DeployMTDConfig deployRequest = new DeployMTDConfig().
          deploymentType(DEPLOYMENT_TYPE_LIVE).
          mtdBaseId(mtdBase.getMtdBaseId()).
          versionId(versionId);
      logger.info(context.logKey + "MTD version Deploy request " + deployRequest);
      DeploymentResponse deployResponse = execute(torbit.deployMTDConfig(
          new DeployMTDConfigRequest().deploy(deployRequest)));
      if (deployResponse == null) {
        failWo(wo, context.logKey, "MTD Host Version Deployment failed  ", null);
      }
      else {
        logger.info(context.logKey + "deployment response " + deployResponse);
        Map<String, String> resultAttrs = getResultCiAttributes(wo);
        Map<String, String> mtdMap = new HashMap<>();
        mtdMap.put("mtd_base_id", Integer.toString(mtdBase.getMtdBaseId()));
        mtdMap.put("mtd_version", Integer.toString(versionId));
        mtdMap.put("deploy_id", Integer.toString(deployResponse.getDeployment().getDeploymentId()));
        resultAttrs.put("gslb_map", gson.toJson(mtdMap));
      }
    }
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

  private List<MTDHostHealthCheck> getHealthChecks(CmsWorkOrderSimple wo, Context context) {
    List<CmsRfcCISimple> dependsOn = wo.getPayLoad().get("DependsOn");
    List<MTDHostHealthCheck> hcList = new ArrayList<>();
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
                    MTDHostHealthCheck healthCheck = newHealthCheck("gslb-" + protocol + "-" + port, protocol, port);
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

  private MTDHostHealthCheck newHealthCheck(String name, String protocol, int port) {
    MTDHostHealthCheck healthCheck = new MTDHostHealthCheck().name(name).protocol(protocol).port(port);
    //TODO: make the following configurable
    healthCheck.failsForDown(10).pass(true);
    return healthCheck;
  }

  private CmsRfcCISimple getRealizedAs(CmsWorkOrderSimple wo) {
    if (wo.getPayLoad().containsKey(REALIZED_AS)) {
      return wo.getPayLoad().get(REALIZED_AS).get(0);
    }
    return null;
  }

  private List<MTDTarget> getMTDTargets(CmsWorkOrderSimple wo, Context context) throws Exception {
    List<LbCloud> lbClouds = getLbCloudMerged(wo);
    TorbitApi torbit = getClient(context);
    if (lbClouds != null && !lbClouds.isEmpty()) {
      Map<Integer, List<LbCloud>> map = lbClouds.stream().collect(Collectors.groupingBy(l -> l.isPrimary ? 1 : 2));
      List<LbCloud> primaryClouds = map.get(1);
      List<LbCloud> secondaryClouds = map.get(2);
      CmsRfcCISimple manifestFqdn = getRealizedAs(wo);
      String distribution = manifestFqdn.getCiAttributes().get("distribution");
      boolean isProximity = "proximity".equals(distribution);
      logger.info(context.logKey + "distribution config for fqdn : " + distribution);
      int weight = 0;
      int reminder = 0;
      if (!isProximity) {
        weight = 100/primaryClouds.size();
        reminder = 100 - (weight * primaryClouds.size());
      }

      List<MTDTarget> targetList = new ArrayList<>();
      if (primaryClouds != null) {
        for (LbCloud lbCloud : primaryClouds) {
          MTDTarget target = baseTarget(lbCloud, torbit).enabled(true);
          if (!isProximity) {
            target.setWeightPercent(weight + (reminder > 0 ? 1 : 0));
            reminder--;
          }
          targetList.add(target);
        }
      }

      if (secondaryClouds != null) {
        for (LbCloud lbCloud : secondaryClouds) {
          MTDTarget target = baseTarget(lbCloud, torbit);
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

  private MTDTarget baseTarget(LbCloud lbCloud, TorbitApi torbitApi) throws Exception {
    if (!cloudMap.containsKey(lbCloud.cloud)) {
      loadDataCenters(torbitApi);
    }
    Cloud cloud = cloudMap.get(lbCloud.cloud);
    return new MTDTarget().mtdTargetHost(lbCloud.dnsRecord).cloudId(cloud.getId()).dataCenterId(cloud.getDataCenterId());
  }

  private List<LbCloud> getLbCloudMerged(CmsWorkOrderSimple wo) throws Exception {
    Map<String, List<CmsRfcCISimple>> map = wo.getPayLoad();
    List<CmsRfcCISimple> lbs = map.get(LB_PAYLOAD);
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
    lc.dnsRecord = cloudCi.getCiAttributes().get(ATTRIBUTE_DNS_RECORD);

    Map<String, String> cloudAttributes = cloudCi.getCiAttributes();
    lc.isPrimary = "1".equals(cloudAttributes.get(ATTRIBUTE_CLOUD_PRIORITY)) &&
        CLOUD_STATUS_ACTIVE.equals(cloudAttributes.get(ATTRIBUTE_CLOUD_STATUS));
    return lc;
  }

  private Context getContext(CmsWorkOrderSimple wo, TorbitConfig config, String logKey) throws Exception {
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
    TorbitClient torbitClient = TorbitClient.getClient(config);
    context.torbit = torbitClient;
    context.config = config;

    logger.info(logKey + "Context - assembly : " + context.assembly + ", platform : " + context.platform + ", env : " +
        context.environment + ", org : " + context.org + ", subdomain : " + context.subdomain + ", cloud : " +
        context.cloud + ", baseGslbDomain : " + context.baseGslbDomain + ", mtdBaseHost : " + context.mtdBaseHost);
    return context;
  }

  private String getLogKey(CmsWorkOrderSimple wo) {
    return wo.getDpmtRecordId() + ":" + wo.getRfcCi().getCiId() + " - ";
  }

  class Context {
    String platform;
    String environment;
    String assembly;
    String org;
    String cloud;
    String subdomain;
    String baseGslbDomain;
    String mtdBaseHost;
    TorbitClient torbit;
    TorbitConfig config;
    String logKey;
  }

  class LbCloud {
    String dnsRecord;
    String cloud;
    boolean isPrimary;
  }
}
