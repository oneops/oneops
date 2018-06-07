package com.oneops.inductor;


import static org.apache.commons.lang.StringUtils.isNotBlank;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.execution.ComponentWoExecutor;
import com.oneops.cms.execution.Response;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.simple.domain.Instance;
import com.oneops.gslb.GslbProvider;
import com.oneops.gslb.Status;
import com.oneops.gslb.domain.CloudARecord;
import com.oneops.gslb.domain.Distribution;
import com.oneops.gslb.domain.Gslb;
import com.oneops.gslb.domain.GslbProvisionResponse;
import com.oneops.gslb.domain.GslbResponse;
import com.oneops.gslb.domain.HealthCheck;
import com.oneops.gslb.domain.InfobloxConfig;
import com.oneops.gslb.domain.Lb;
import com.oneops.gslb.domain.Protocol;
import com.oneops.gslb.domain.ProvisionedGslb;
import com.oneops.gslb.domain.TorbitConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FqdnExecutor implements ComponentWoExecutor {

  private static String ONEOPS_FQDN_CLASS = "bom.oneops.1.Fqdn";
  private static String BASE_FQDN_CLASS = "bom.Fqdn";
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

  private static final String CLOUD_STATUS_ACTIVE = "active";
  private static final String CLOUD_STATUS_INACTIVE = "inactive";

  private static final Logger logger = Logger.getLogger(FqdnExecutor.class);

  @Autowired
  WoHelper woHelper;

  @Autowired
  Gson gson;

  @Autowired
  FqdnVerifier fqdnVerifier;

  @Autowired
  JsonParser jsonParser;

  Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();

  GslbProvider gslbProvider;
  private Map<String, Distribution> distributionMap;
  private Map<String, Protocol> protocolMap;

  public FqdnExecutor() {
    gslbProvider = new GslbProvider();
    distributionMap = new HashMap<>();
    distributionMap.put("proximity", Distribution.PROXIMITY);
    distributionMap.put("roundrobin", Distribution.ROUND_ROBIN);
    protocolMap = new HashMap<>();
    protocolMap.put("tcp", Protocol.TCP);
    protocolMap.put("http", Protocol.HTTP);
    protocolMap.put("https", Protocol.TCP);
  }

  @Override
  public List<String> getComponentClasses() {
    return Arrays.asList(BASE_FQDN_CLASS, ONEOPS_FQDN_CLASS);
  }

  @Override
  public Response execute(CmsWorkOrderSimple wo, String dataDir) {
    String logKey = woHelper.getLogKey(wo);
    if (isFqdnInstance(wo) && isLocalWo(wo) && isGdnsEnabled(wo) && isTorbitServiceType(wo)) {
      executeInternal(wo, logKey, "deployment", dataDir, (t, i) -> {
        if (isGslbDeleteAction(wo)) {
          GslbResponse response = gslbProvider.delete(provisionedGslb(wo, t, i, logKey));
          updateWoResult(wo, response, logKey);
        }
        else {
          GslbProvisionResponse response = gslbProvider.create(getGslbRequestFromWo(wo, t, i, logKey));
          updateWoResult(wo, response, logKey);
        }
      });
      return woHelper.formResponse(wo, logKey);
    }
    logger.info(logKey + "not executing by FqdnExecutor as these conditions are not met :: "
        + "[fqdn service_type set as torbit && gdns enabled for env && local workorder && torbit cloud service configured]");
    return Response.getNotMatchingResponse();
  }

  private boolean isFqdnInstance(CmsWorkOrderSimpleBase wo) {
    return BASE_FQDN_CLASS.equals(wo.getClassName()) || ONEOPS_FQDN_CLASS.equals(wo.getClassName());
  }

  private boolean isGslbDeleteAction(CmsWorkOrderSimple wo) {
    return woHelper.isDeleteAction(wo) && isPlatformDisabled(wo);
  }

  private ProvisionedGslb provisionedGslb(CmsWorkOrderSimple wo, TorbitConfig torbitConfig, InfobloxConfig infobloxConfig, String logKey) {
    Context context = context(wo, infobloxConfig);
    List<String> aliases = getAliasesWithDefault(context, wo.getRfcCi().getCiAttributes()).stream()
        .collect(Collectors.toList());
    List<CloudARecord> cloudEntries = getCloudDnsEntry(context, wo.getCloud());
    return ProvisionedGslb.builder()
        .torbitConfig(torbitConfig)
        .infobloxConfig(infobloxConfig)
        .app(context.platform)
        .subdomain(context.subdomain)
        .cnames(aliases)
        .cloudARecords(cloudEntries)
        .logContextId(logKey)
        .build();
  }

  @Override
  public Response execute(CmsActionOrderSimple ao, String dataDir) {
    String logKey = woHelper.getLogKey(ao);
    if (isTorbitGslb(ao)) {
      executeInternal(ao, logKey, "procedure", dataDir, (t, i) -> {
        GslbResponse response = gslbProvider.checkStatus((getGslbRequestFromAo(ao, t, i, logKey)));
        updateAoResult(ao, response, logKey);
      });
      return woHelper.formResponse(ao, logKey);
    }
    return Response.getNotMatchingResponse();
  }

  private void executeInternal(CmsWorkOrderSimpleBase wo, String logKey, String execType, String dataDir,
      BiConsumer<TorbitConfig, InfobloxConfig> consumer) {
    try {
      String fileName = dataDir + "/" + wo.getRecordId() + ".json";
      writeRequest(gsonPretty.toJson(wo), fileName);
      logger.info(logKey + "wo file written to " + fileName);
      TorbitConfig torbitConfig = getTorbitConfig(wo, logKey);
      InfobloxConfig infobloxConfig = getInfobloxConfig(wo);
      if (torbitConfig != null && infobloxConfig != null) {
        logger.info(logKey + "FqdnExecutor executing " + execType + " : " + wo.getExecutionId() + " action : " + wo.getAction());
        consumer.accept(torbitConfig, infobloxConfig);
      }
      else {
        woHelper.failWo(wo, logKey, "TorbitConfig/InfobloxConfig could not be obtained, Please check cloud service configuration", null);
      }
    }
    catch (Exception e) {
      woHelper.failWo(wo, logKey, "Exception setting up fqdn ", e);
    }
  }

  @Override
  public Response verify(CmsWorkOrderSimple wo, Response response) {
    String logKey = woHelper.getLogKey(wo);
    TorbitConfig torbitConfig = getTorbitConfig(wo, logKey);
    InfobloxConfig infobloxConfig = getInfobloxConfig(wo);
    Response responseAfterVerify;
    if (isGslbDeleteAction(wo)) {
      responseAfterVerify = fqdnVerifier.verifyDelete(provisionedGslb(wo, torbitConfig, infobloxConfig, logKey), wo, response);
    }
    else {
      responseAfterVerify = fqdnVerifier.verifyCreate(getGslbRequestFromWo(wo, torbitConfig, infobloxConfig, logKey), wo, response);
    }
    return responseAfterVerify;
  }

  private void updateWoResult(CmsWorkOrderSimple wo, GslbProvisionResponse response, String logKey) {
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

  private void updateWoResult(CmsWorkOrderSimple wo, GslbResponse response, String logKey) {
    if (response != null) {
      if (response.getStatus() != Status.SUCCESS) {
        woHelper.failWo(wo, logKey, response.getFailureMessage(), null);
      }
    }
  }

  private void updateAoResult(CmsActionOrderSimple wo, GslbResponse response, String logKey) {
    if (response != null) {
      if (response.getStatus() != Status.SUCCESS) {
        woHelper.failWo(wo, logKey, response.getFailureMessage(), null);
      }
    }
  }

  private Gslb getGslbRequestFromWo(CmsWorkOrderSimple wo, TorbitConfig torbitConfig, InfobloxConfig infobloxConfig, String logKey) {
    Context context = context(wo, infobloxConfig);
    Map<String, String> fqdnAttrs = wo.getRfcCi().getCiAttributes();
    Map<String, String> fqdnBaseAttrs = wo.getRfcCi().getCiBaseAttributes();

    Set<String> aliases = getAliasesWithDefault(context, fqdnAttrs);
    List<CloudARecord> cloudEntries = getCloudDnsEntry(context, wo.getCloud());
    List<String> oldAliases = getAliases(context, fqdnBaseAttrs).stream()
        .filter(a -> !aliases.contains(a))
        .collect(Collectors.toList());
    List<CloudARecord> newEntries = null;
    List<CloudARecord> obsoleteEntries = null;
    if (woHelper.isDeleteAction(wo)) {
      obsoleteEntries = cloudEntries;
    }
    else {
      newEntries = cloudEntries;
    }

    Gslb gslb = Gslb.builder()
        .app(context.platform)
        .subdomain(context.subdomain)
        .distribution(distribution(fqdnAttrs))
        .torbitConfig(torbitConfig)
        .infobloxConfig(infobloxConfig)
        .logContextId(logKey)
        .lbs(lbTargets(wo, context))
        .healthChecks(healthChecks(context, logKey))
        .cnames(aliases.stream().collect(Collectors.toList()))
        .cloudARecords(newEntries)
        .obsoleteCnames(oldAliases)
        .obsoleteCloudARecords(obsoleteEntries)
        .build();
    return gslb;
  }

  private Gslb getGslbRequestFromAo(CmsActionOrderSimple ao, TorbitConfig torbitConfig, InfobloxConfig infobloxConfig, String logKey) {
    Context context = context(ao, infobloxConfig);
    Map<String, String> fqdnAttrs = ao.getCi().getCiAttributes();

    List<String> aliases = getAliasesWithDefault(context, fqdnAttrs).stream().collect(Collectors.toList());
    List<CloudARecord> cloudEntries = getCloudDnsEntry(context, ao.getCloud());

    Gslb gslb = Gslb.builder()
        .app(context.platform)
        .subdomain(context.subdomain)
        .distribution(distribution(fqdnAttrs))
        .torbitConfig(torbitConfig)
        .infobloxConfig(infobloxConfig)
        .logContextId(logKey)
        .lbs(lbTargets(ao))
        .healthChecks(healthChecks(context, logKey))
        .cnames(aliases.stream().collect(Collectors.toList()))
        .cloudARecords(cloudEntries)
        .build();
    return gslb;
  }

  private Set<String> getAliasesWithDefault(Context context, Map<String, String> fqdnAttrs) {
    Set<String> currentAliases = new HashSet<>();
    String defaultAlias = getFullAlias(context.platform, context);
    currentAliases.add(defaultAlias);
    addAliases(context, fqdnAttrs, currentAliases);
    return currentAliases;
  }

  private Set<String> getAliases(Context context, Map<String, String> fqdnAttrs) {
    Set<String> currentAliases = new HashSet<>();
    addAliases(context, fqdnAttrs, currentAliases);
    return currentAliases;
  }

  private void addAliases(Context context, Map<String, String> fqdnAttrs, Set<String> currentAliases) {
    String fullAliases = fqdnAttrs.get(ATTRIBUTE_FULL_ALIAS);
    String aliases = fqdnAttrs.get(ATTRIBUTE_ALIAS);
    addAlias(aliases, currentAliases, t -> (getFullAlias(t, context)));
    addAlias(fullAliases, currentAliases, Function.identity());
  }

  private String getFullAlias(String alias, Context context) {
    return String.join(".", alias, context.subdomain, context.infobloxConfig.zone());
  }

  private void addAlias(String attrValue, Set<String> aliases, Function<String, String> mapper) {
    if (isNotBlank(attrValue)) {
      JsonArray aliasArray = (JsonArray) jsonParser.parse(attrValue);
      for (JsonElement alias : aliasArray) {
        aliases.add(mapper.apply(alias.getAsString()));
      }
    }
  }

  private List<CloudARecord> getCloudDnsEntry(Context context, CmsCISimple cloud) {
    CloudARecord cloudARecord = CloudARecord.create(cloud.getCiName(),
        String.join(".", context.platform, context.subdomain,
            cloud.getCiName(), context.infobloxConfig.zone()).toLowerCase());
    return Collections.singletonList(cloudARecord);
  }

  private List<Lb> lbTargets(CmsWorkOrderSimple wo, Context context) {
    Map<Long, Cloud> cloudMap = getPlatformClouds(wo);
    List<CmsRfcCISimple> deployedLbs = wo.getPayLoad().get(LB_PAYLOAD);
    if (woHelper.isDeleteAction(wo)) {
      Instance currentLb = context.lb;
      deployedLbs = deployedLbs.stream()
          .filter(lb -> (lb.getCiId() != currentLb.getCiId()))
          .collect(Collectors.toList());
    }
    if (deployedLbs == null) {
      throw new RuntimeException("Lb payload not available in workorder");
    }
    return deployedLbs.stream()
        .filter(lb -> isNotBlank(lb.getCiAttributes().get(ATTRIBUTE_DNS_RECORD)))
        .map(lb -> lbTarget(lb, cloudMap))
        .collect(Collectors.toList());
  }

  private List<Lb> lbTargets(CmsActionOrderSimple ao) {
    Map<Long, Cloud> cloudMap = getPlatformClouds(ao);
    List<CmsCISimple> deployedLbs = ao.getPayLoad().get(LB_PAYLOAD);
    return deployedLbs.stream()
        .filter(lb -> isNotBlank(lb.getCiAttributes().get(ATTRIBUTE_DNS_RECORD)))
        .map(lb -> lbTarget(lb, cloudMap))
        .collect(Collectors.toList());
  }

  private Lb lbTarget(Instance lbCi, Map<Long, Cloud> cloudMap) {
    String lbName = lbCi.getCiName();
    String[] elements = lbName.split("-");
    String cloudId = elements[elements.length - 2];
    Cloud cloud = cloudMap.get(Long.parseLong(cloudId));
    return Lb.create(cloud.name, lbCi.getCiAttributes().get(ATTRIBUTE_DNS_RECORD), isEnabledForTraffic(cloud), null);
  }

  private boolean isEnabledForTraffic(Cloud cloud) {
    return "1".equals(cloud.priority) &&
        (CLOUD_STATUS_ACTIVE.equals(cloud.adminStatus) || CLOUD_STATUS_INACTIVE.equals(cloud.adminStatus));
  }

  private InfobloxConfig getInfobloxConfig(CmsWorkOrderSimpleBase wo) {
    Map<String, CmsCISimple> dnsServices = (Map<String, CmsCISimple>) wo.getServices().get("dns");
    if (dnsServices != null) {
      CmsCISimple dns = dnsServices.get(wo.getCloud().getCiName());
      if (dns != null) {
        Map<String, String> attributes = dns.getCiAttributes();
        String host = attributes.get(ATTRIBUTE_DNS_HOST);
        String user = attributes.get(ATTRIBUTE_DNS_USER_NAME);
        String pwd = attributes.get(ATTRIBUTE_DNS_PASSWORD);
        String zone = attributes.get(ATTRIBUTE_DNS_ZONE);
        if (isNotBlank(host) && isNotBlank(user)) {
          return InfobloxConfig.create(host, user, pwd, zone);
        }
      }
    }
    return null;
  }

  private boolean isPlatformDisabled(CmsWorkOrderSimpleBase wo) {
    boolean isPlatformDisabled = false;
    Map<String, String> platformAttributes = wo.getBox().getCiAttributes();
    if (platformAttributes.containsKey(ATTRIBUTE_PLATFORM_ENABLED)) {
      isPlatformDisabled = "false".equals(platformAttributes.get(ATTRIBUTE_PLATFORM_ENABLED));
    }
    return isPlatformDisabled;
  }

  private List<HealthCheck> healthChecks(Context context, String logKey) {
    Instance lb = context.lb;
    if (lb == null) {
      throw new RuntimeException("DependsOn Lb is empty");
    }
    List<HealthCheck> hcList = new ArrayList<>();
    String listenerJson = lb.getCiAttributes().get(ATTRIBUTE_LISTENERS);
    String ecvMapJson = lb.getCiAttributes().get(ATTRIBUTE_ECV_MAP);
    if (isNotBlank(listenerJson) && isNotBlank(ecvMapJson)) {
      JsonElement element = jsonParser.parse(ecvMapJson);
      if (element instanceof JsonObject) {
        JsonObject root = (JsonObject) element;
        Set<Entry<String, JsonElement>> set = root.entrySet();
        Map<Integer, String> ecvMap = set.stream().
            collect(Collectors.toMap(s -> Integer.parseInt(s.getKey()), s -> s.getValue().getAsString()));
        logger.info(logKey + "listeners "  + listenerJson);
        JsonArray listeners = (JsonArray) jsonParser.parse(listenerJson);
        listeners.forEach(s -> {
          String listener = s.getAsString();
          //listeners are generally in this format 'http <lb-port> http <app-port>', gslb needs to use the lb-port for health checks
          //ecv map is configured as '<app-port> : <ecv-url>', so we need to use the app-port from listener configuration to lookup the ecv config from ecv map
          String[] config = listener.split(" ");
          if (config.length >= 2) {
            String protocol = config[0];
            int lbPort = Integer.parseInt(config[1]);
            int ecvPort = Integer.parseInt(config[config.length-1]);
            String healthConfig = ecvMap.get(ecvPort);
            if (healthConfig != null) {
              if ((protocol.equals("http"))) {
                String path = healthConfig.substring(healthConfig.indexOf(" ")+1);
                logger.info(logKey +  "healthConfig : " + healthConfig + ", health check configuration, protocol: " + protocol + ", port: " + lbPort
                    + ", path " + path);
                hcList.add(newHealthCheck(protocol, lbPort, path));
              } else {
                logger.info(logKey + "health check configuration, protocol: " + protocol + ", port: " + lbPort);
                hcList.add(newHealthCheck(protocol, lbPort, null ));
              }
            }
          }
        });
      }
    }
    return hcList;
  }

  private HealthCheck newHealthCheck(String protocol, int port, String testObjectPath) {
    return HealthCheck.builder()
        .protocol(protocolMap.get(protocol))
        .port(port)
        .path(testObjectPath)
        .build();
  }

  private Map<Long, Cloud> getPlatformClouds(CmsWorkOrderSimple wo) {
    System.out.println(wo.getPayLoad().get(CLOUDS_PAYLOAD));
    List<Cloud> clouds = getPlatformClouds(wo.getPayLoad().get(CLOUDS_PAYLOAD));
    return clouds.stream().collect(Collectors.toMap(c -> c.getCiId(), Function.identity()));
  }

  private Map<Long, Cloud> getPlatformClouds(CmsActionOrderSimple ao) {
    List<Cloud> clouds = getPlatformClouds(ao.getPayLoad().get(CLOUDS_PAYLOAD));
    return clouds.stream().collect(Collectors.toMap(c -> c.getCiId(), Function.identity()));
  }

  private List<Cloud> getPlatformClouds(List<? extends Instance> clouds) {
    return clouds.stream().map(this::cloudFromCi).collect(Collectors.toList());
  }

  private Cloud cloudFromCi(Instance cloudCi) {
    Map<String, String> attrs = cloudCi.getCiAttributes();
    return new Cloud(cloudCi.getCiId(), cloudCi.getCiName(),
        attrs.get(ATTRIBUTE_CLOUD_PRIORITY), attrs.get(ATTRIBUTE_CLOUD_STATUS));
  }

  private boolean isLocalWo(CmsWorkOrderSimpleBase wo) {
    return !wo.isPayLoadEntryPresent("ManagedVia");
  }

  private boolean isTorbitServiceType(CmsWorkOrderSimple wo) {
    CmsRfcCISimple realizedAs = woHelper.getRealizedAs(wo);
    if (realizedAs != null) {
      String serviceType = realizedAs.getCiAttributes().get(ATTRIBUTE_SERVICE_TYPE);
      logger.info(wo.getCiId() + " : fqdn service type  " + serviceType);
      return SERVICE_TYPE_TORBIT.equals(serviceType);
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

  private TorbitConfig getTorbitConfig(CmsWorkOrderSimpleBase wo, String logKey) {
    Map<String, Map<String, CmsCISimple>> services = wo.getServices();
    if (services != null && services.containsKey(SERVICE_TYPE_TORBIT)) {
      Map<String, CmsCISimple> gdnsService = services.get(SERVICE_TYPE_TORBIT);
      CmsCISimple gdns;
      if ((gdns = gdnsService.get(wo.getCloud().getCiName())) != null) {
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

  private boolean isTorbitGslb(CmsActionOrderSimple ao) {
    CmsCISimple ci = ao.getCi();
    Map<String, String> attributes = ci.getCiAttributes();
    return isFqdnInstance(ao) && isLocalWo(ao) &&
        attributes.containsKey(ATTRIBUTE_SERVICE_TYPE) && SERVICE_TYPE_TORBIT.equals(attributes.get(ATTRIBUTE_SERVICE_TYPE)) &&
        attributes.containsKey("gslb_map");
  }


  private Distribution distribution(Map<String, String> fqdnAttributes) {
    String dist = fqdnAttributes.get(ATTRIBUTE_DISTRIBUTION);
    if (distributionMap.containsKey(dist)) {
      return distributionMap.get(dist);
    }
    else
      return distributionMap.get("proximity");
  }

  private Context context(CmsWorkOrderSimple wo, InfobloxConfig infobloxConfig) {
    Context context = baseContext(wo);
    context.infobloxConfig = infobloxConfig;
    Map<String, List<CmsRfcCISimple>> payload = wo.getPayLoad();
    CmsRfcCISimple env = payload.get("Environment").get(0);
    String org = payload.get("Organization").get(0).getCiName();
    String assembly = payload.get("Assembly").get(0).getCiName();
    String customSubDomain = env.getCiAttributes().get("subdomain");
    context.subdomain = isNotBlank(customSubDomain) ? customSubDomain :
        String.join(".", env.getCiName(), assembly, org);
    context.lb = woHelper.getLbFromDependsOn(wo);
    return context;
  }

  private Context context(CmsActionOrderSimple ao, InfobloxConfig infobloxConfig) {
    Context context = baseContext(ao);
    context.infobloxConfig = infobloxConfig;
    Map<String, List<CmsCISimple>> payload = ao.getPayLoad();
    Instance env = payload.get("Environment").get(0);
    String gslbMap = ao.getCi().getCiAttributes().get("gslb_map");
    JsonObject root = (JsonObject) gson.fromJson(gslbMap, JsonElement.class);
    String glb = root.get("glb").getAsString();

    if (isNotBlank(glb)) {
      String[] elements = glb.split("\\.");
      String org = elements[3];
      String assembly = elements[2];
      String customSubDomain = env.getCiAttributes().get("subdomain");
      context.subdomain = isNotBlank(customSubDomain) ? customSubDomain :
          String.join(".", env.getCiName(), assembly, org);
      context.lb = woHelper.getLbFromDependsOn(ao);
    }
    else {
      throw new RuntimeException("glb value could not be obtained form gslb_map attribute");
    }
    return context;
  }

  private Context baseContext(CmsWorkOrderSimpleBase woBase) {
    Context context = new Context();
    context.platform = woBase.getBox().getCiName();
    return context;
  }

  class Context {
    String platform;
    String subdomain;
    InfobloxConfig infobloxConfig;
    Instance lb;
  }

  class Cloud {
    long ciId;
    String name;
    String priority;
    String adminStatus;

    Cloud(long ciId, String name, String priority, String adminStatus) {
      this.ciId = ciId;
      this.name = name;
      this.priority = priority;
      this.adminStatus = adminStatus;
    }

    long getCiId() {
      return ciId;
    }
  }

}
