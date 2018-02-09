package com.oneops.gslb;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oneops.cms.execution.Response;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.gslb.v2.domain.MtdBase;
import com.oneops.gslb.v2.domain.MtdBaseResponse;
import com.oneops.gslb.v2.domain.MtdHost;
import com.oneops.gslb.v2.domain.MtdHostHealthCheck;
import com.oneops.gslb.v2.domain.MtdHostResponse;
import com.oneops.gslb.v2.domain.MtdTarget;
import com.oneops.infoblox.InfobloxClient;
import com.oneops.infoblox.model.cname.CNAME;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FqdnVerifier {

  private static final Logger logger = Logger.getLogger(FqdnVerifier.class);

  @Autowired
  WoHelper woHelper;

  private JsonParser jsonParser = new JsonParser();

  public Response verify(CmsWorkOrderSimple wo, Response response, Config config) {
    String logKey = woHelper.getVerifyLogKey(wo);
    try {
      VerifyContext context = getContext(wo, config);
      context.logKey = logKey;

      if (woHelper.isDeleteAction(wo)) {
        verifyMtdDelete(wo, context);
        verifyInfobloxDelete(wo, context);
      }
      else {
        verifyMtdHost(wo, context);
        verifyInfoblox(wo, context);
      }

    } catch (Exception e) {
      woHelper.failWo(wo, logKey, "wo failed during verify", e);
      response = woHelper.formResponse(wo, logKey);
    }
    return response;
  }

  private void verifyInfoblox(CmsWorkOrderSimple wo, VerifyContext context) throws Exception {
    InfobloxClient infobloxClient = getInfoBloxClient(wo);
    String cname = context.platform + context.mtdBaseHost;
    for (String alias : getAliases(wo, context)) {
      List<CNAME> cnames = infobloxClient.getCNameRec(alias);
      verify(() ->cnames != null && cnames.size() == 1 && cnames.get(0).canonical().equals(cname), "cname verify failed " + alias);
    }
  }


  private void verifyInfobloxDelete(CmsWorkOrderSimple wo, VerifyContext context) throws Exception {
    InfobloxClient infobloxClient = getInfoBloxClient(wo);
    for (String alias : getAliases(wo, context)) {
      List<CNAME> cnames = infobloxClient.getCNameRec(alias);
      verify(() ->cnames == null || cnames.isEmpty(), "cname delete verify failed");
    };
  }

  private List<String> getAliases(CmsWorkOrderSimple wo, VerifyContext context) {
    List<String> list = new ArrayList<>();
    String suffix = getDomainSuffix(wo, context);
    list.add(context.platform + suffix);

    String aliasesContent = wo.getRfcCi().getCiBaseAttributes().get("aliases");
    if (StringUtils.isNotBlank(aliasesContent)) {
      JsonArray aliasArray = (JsonArray) jsonParser.parse(aliasesContent);

      for (JsonElement alias : aliasArray) {
        list.add(alias.getAsString() + suffix);
      }
    }

    aliasesContent = wo.getRfcCi().getCiBaseAttributes().get("full_aliases");
    if (StringUtils.isNotBlank(aliasesContent)) {
      JsonArray aliasArray = (JsonArray) jsonParser.parse(aliasesContent);
      for (JsonElement alias : aliasArray) {
        list.add(alias.getAsString());
      }
    }
    return list;
  }

  private String getDomainSuffix(CmsWorkOrderSimple wo, VerifyContext context) {
    String cloud = wo.getCloud().getCiName();
    Map<String, String> dnsAttrs = wo.getServices().get("dns").get(cloud).getCiAttributes();
    return "." + context.subDomain + "." + dnsAttrs.get("zone");
  }


  private InfobloxClient getInfoBloxClient(CmsWorkOrderSimple wo) {
    String cloud = wo.getCloud().getCiName();
    Map<String, String> dnsAttrs = wo.getServices().get("dns").get(cloud).getCiAttributes();
    return InfobloxClient.builder().endPoint(dnsAttrs.get("host")).
        userName(dnsAttrs.get("username")).
        password(dnsAttrs.get("password")).
        tlsVerify(false).
        build();
  }

  private void verifyMtdDelete(CmsWorkOrderSimple wo, VerifyContext context) throws Exception {
    TorbitClient client = context.torbitClient;
    TorbitApi torbit = context.torbit;
    Resp<MtdBaseResponse> resp = client.execute(torbit.getMTDBase(context.mtdBaseHost), MtdBaseResponse.class);
    logger.info(context.logKey + "verifying mtd host not exists ");
    if (resp.isSuccessful()) {
      logger.info(context.logKey + "mtd base exists, trying to get mtd host");
      MtdBase mtdBase = resp.getBody().mtdBase();
      Resp<MtdHostResponse> hostResp = client.execute(torbit.getMTDHost(mtdBase.mtdBaseId(), context.platform), MtdHostResponse.class);
      verify(() -> !hostResp.isSuccessful(), "mtd host is not available");
    }
  }

  private void verifyMtdHost(CmsWorkOrderSimple wo, VerifyContext context) throws Exception {
    TorbitClient client = context.torbitClient;
    TorbitApi torbit = context.torbit;

    logger.info(context.logKey + "verifying for platform " + context.platform);
    Resp<MtdBaseResponse> resp = client.execute(torbit.getMTDBase(context.mtdBaseHost), MtdBaseResponse.class);
    logger.info(context.logKey + "verifying mtd base ");
    verify(() -> resp.isSuccessful(), "mtd base exists");
    MtdBase mtdBase = resp.getBody().mtdBase();
    verify(() -> context.mtdBaseHost.equals(mtdBase.mtdBaseName()), "mtd base name match");

    Resp<MtdHostResponse> hostResp = client.execute(torbit.getMTDHost(mtdBase.mtdBaseId(), context.platform), MtdHostResponse.class);
    logger.info(context.logKey + "verifying mtd host version exists");
    verify(() -> hostResp.isSuccessful(), "mtd host version exists");

    MtdHost host = hostResp.getBody().mtdHost();
    logger.info(context.logKey + "verifying mtd host targets");
    List<MtdTarget> targets = host.mtdTargets();
    logger.info(context.logKey + "configured mtd targets " + targets.stream().
        map(MtdTarget::mtdTargetHost).
        collect(Collectors.joining(",")));
    Map<String, MtdTarget> map = targets.stream().collect(Collectors.toMap(MtdTarget::mtdTargetHost, Function.identity()));
    List<Lb> lbList = getLbVips(wo);
    logger.info(context.logKey + "expected targets " +
        lbList.stream().map(l -> l.vip).collect(Collectors.joining(",")));
    for (Lb lb : lbList) {
      verify(() -> map.containsKey(lb.vip), "lb vip present in MTD target");
      MtdTarget target = map.get(lb.vip);
      verify(() -> lb.isPrimary ? target.enabled() : !target.enabled(), "mtd target enabled/disabled based on cloud status");
    }

    logger.info(context.logKey + "verifying mtd health checks");
    List<MtdHostHealthCheck> healthChecks = host.mtdHealthChecks();
    Map<Integer, EcvListener> expectedChecksMap = getHealthChecks(wo, context).stream().
        collect(Collectors.toMap(e -> e.port, Function.identity()));
    logger.info(context.logKey + "expectedChecksMap : " + expectedChecksMap.size() + " " + expectedChecksMap);
    logger.info(context.logKey + "actual health checks : " + healthChecks);
    verify(() -> ((healthChecks != null ? healthChecks.size() : 0) ==
            (expectedChecksMap != null ? expectedChecksMap.size() : 0)),
        "all health checks are configured");
    if (healthChecks != null) {
      for (MtdHostHealthCheck healthCheck : healthChecks) {
        verify(() -> expectedChecksMap.containsKey(healthCheck.port()), "mtd health check available for port");
        EcvListener listener = expectedChecksMap.get(healthCheck.port());
        verify(() -> listener.protocol.equals(healthCheck.protocol()), "mtd health protocol matches");
        if (listener.ecv != null && !listener.ecv.isEmpty()) {
          verify(() -> listener.ecv.equals(healthCheck.testObjectPath()), "mtd health ecv matches");
        }
      }
    }
  }

  private List<Lb> getLbVips(CmsWorkOrderSimple wo) {
    Map<String, List<CmsRfcCISimple>> map = wo.getPayLoad();
    List<CmsRfcCISimple> lbs = map.get(WoHelper.LB_PAYLOAD);
    Map<Long, CmsRfcCISimple> cloudCiMap = map.get(WoHelper.CLOUDS_PAYLOAD).stream()
        .collect(Collectors.toMap(c -> c.getCiId(), Function.identity()));
    List<Lb> list = lbs.stream().map(lb -> getLbWithCloud(lb, cloudCiMap)).collect(Collectors.toList());
    return list;
  }

  private Lb getLbWithCloud(CmsRfcCISimple lbCi, Map<Long, CmsRfcCISimple> cloudCiMap) {
    Lb lb = new Lb();
    String lbName = lbCi.getCiName();
    String[] elements = lbName.split("-");
    String cloudId = elements[elements.length - 2];
    CmsRfcCISimple cloudCi = cloudCiMap.get(Long.parseLong(cloudId));
    lb.cloud = cloudCi.getCiName();
    lb.vip = lbCi.getCiAttributes().get("dns_record");

    Map<String, String> cloudAttributes = cloudCi.getCiAttributes();
    lb.isPrimary = "1".equals(cloudAttributes.get("base.Consumes.priority")) &&
        "active".equals(cloudAttributes.get("base.Consumes.adminstatus"));
    return lb;
  }

  private List<EcvListener> getHealthChecks(CmsWorkOrderSimple wo, VerifyContext context) {
    List<CmsRfcCISimple> dependsOn = wo.getPayLoad().get("DependsOn");
    List<EcvListener> ecvListeners = new ArrayList<>();
    Optional<CmsRfcCISimple> opt = dependsOn.stream().filter(rfc -> "bom.oneops.1.Lb".equals(rfc.getCiClassName())).findFirst();
    CmsRfcCISimple lb = opt.get();
    Map<String, String> attributes = lb.getCiAttributes();
    String ecv = attributes.get("ecv_map");
    logger.info(context.logKey + "ecv_map " + ecv);
    JsonElement element = jsonParser.parse(ecv);
    JsonObject root = (JsonObject) element;
    Set<Entry<String, JsonElement>> set = root.entrySet();
    Map<Integer, String> ecvMap = set.stream().
        collect(Collectors.toMap(s -> Integer.parseInt(s.getKey()), s -> s.getValue().getAsString()));
    JsonArray listeners = (JsonArray) jsonParser.parse(attributes.get("listeners"));
    logger.info(context.logKey + "listeners " + attributes.get("listeners"));
    listeners.forEach(s -> {
      String listener = s.getAsString();
      String[] config = listener.split(" ");
        String protocol = config[0];
        int port = Integer.parseInt(config[config.length-1]);
        String healthConfig = ecvMap.get(port);
        if (healthConfig != null && !healthConfig.isEmpty()) {
          EcvListener ecvListener = new EcvListener();
          if ((protocol.startsWith("http"))) {
            String path = healthConfig.substring(healthConfig.indexOf(" ")+1);
            ecvListener.port = port;
            ecvListener.protocol = protocol;
            ecvListener.ecv = path;
          }
          else {
            ecvListener.port = port;
            ecvListener.protocol = protocol;
          }
          ecvListeners.add(ecvListener);
        }
    });
    return ecvListeners;
  }

  private void verify(Condition condition, String message) throws Exception {
    if (!condition.test()) {
      throw new Exception("Verification failed for : " + message);
    }
  }

  private String getPlatform(CmsWorkOrderSimple wo) {
    return wo.getBox().getCiName();
  }

  private void loadContext(CmsWorkOrderSimple wo, VerifyContext context) {
    Map<String, List<CmsRfcCISimple>> payload = wo.getPayLoad();
    String assembly = payload.get("Assembly").get(0).getCiName();
    CmsRfcCISimple env = payload.get("Environment").get(0);
    String environment = env.getCiName();
    String org = payload.get("Organization").get(0).getCiName();
    String subdomain = env.getCiAttributes().get("subdomain");
    String cloud = wo.cloud.getCiName();
    String baseGslbDomain = wo.services.get("torbit").get(cloud).getCiAttributes().get("gslb_base_domain");
    context.subDomain = subdomain != null ? subdomain : environment + "." + assembly + "." + org;
    if (subdomain != null)
      context.mtdBaseHost = "." + context.subDomain + "." + baseGslbDomain;
    else
      context.mtdBaseHost = "." + context.subDomain + "." + baseGslbDomain;
  }


  private VerifyContext getContext(CmsWorkOrderSimple wo, Config config) throws Exception {
    VerifyContext context = new VerifyContext();
    context.torbitClient = new TorbitClient(config);
    context.torbit = context.torbitClient.getTorbit();
    loadContext(wo, context);
    context.platform = getPlatform(wo);
    return context;
  }

  class Lb {
    String vip;
    boolean isPrimary;
    String cloud;
  }

  class EcvListener {
    int port;
    String protocol;
    String ecv;

    public String toString() {
      return "[protocol: " +  protocol + ", port: " + port + ", ecv: " + ecv + "]";
    }
  }

  class VerifyContext {
    TorbitClient torbitClient;
    TorbitApi torbit;
    String mtdBaseHost;
    String platform;
    String logKey;
    String subDomain;
  }

}
