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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MtdVerifier {

  private static final Logger logger = Logger.getLogger(MtdVerifier.class);

  @Autowired
  WoHelper woHelper;

  private JsonParser jsonParser = new JsonParser();

  public Response verify(CmsWorkOrderSimple wo, Response response, Config config) {
    String logKey = woHelper.getVerifyLogKey(wo);
    try {
      VerifyContext context = getContext(wo, config);
      context.logKey = logKey;

      if (woHelper.isDeleteAction(wo)) {
        verifyDelete(wo, context);
      }
      else {
        verifyMtdHost(wo, context);
      }

    } catch (Exception e) {
      woHelper.failWo(wo, logKey, "wo failed during verify", e);
      response = woHelper.formResponse(wo, logKey);
    }
    return response;
  }

  private void verifyDelete(CmsWorkOrderSimple wo, VerifyContext context) throws Exception {
    TorbitClient client = context.torbitClient;
    TorbitApi torbit = context.torbit;
    Resp<MtdBaseResponse> resp = client.execute(torbit.getMTDBase(context.mtdBaseHost), MtdBaseResponse.class);
    logger.info(context.logKey + "verifying mtd host not exists ");
    if (resp.isSuccessful()) {
      logger.info(context.logKey + "mtd base exists, trying to get mtd host");
      MtdBase mtdBase = resp.getBody().getMtdBase();
      Resp<MtdHostResponse> hostResp = client.execute(torbit.getMTDHost(mtdBase.getMtdBaseId(), context.platform), MtdHostResponse.class);
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
    MtdBase mtdBase = resp.getBody().getMtdBase();
    verify(() -> context.mtdBaseHost.equals(mtdBase.getMtdBaseName()), "mtd base name match");

    Resp<MtdHostResponse> hostResp = client.execute(torbit.getMTDHost(mtdBase.getMtdBaseId(), context.platform), MtdHostResponse.class);
    logger.info(context.logKey + "verifying mtd host version exists");
    verify(() -> hostResp.isSuccessful(), "mtd host version exists");

    MtdHost host = hostResp.getBody().getMtdHost();
    logger.info(context.logKey + "verifying mtd host targets");
    List<MtdTarget> targets = host.getMtdTargets();
    logger.info(context.logKey + "configured mtd targets " + targets.stream().
        map(MtdTarget::getMtdTargetHost).
        collect(Collectors.joining(",")));
    Map<String, MtdTarget> map = targets.stream().collect(Collectors.toMap(MtdTarget::getMtdTargetHost, Function.identity()));
    List<Lb> lbList = getLbVips(wo);
    logger.info(context.logKey + "expected targets " +
        lbList.stream().map(l -> l.vip).collect(Collectors.joining(",")));
    for (Lb lb : lbList) {
      verify(() -> map.containsKey(lb.vip), "lb vip present in MTD target");
      MtdTarget target = map.get(lb.vip);
      verify(() -> lb.isPrimary ? target.getEnabled() : !target.getEnabled(), "mtd target enabled/disabled based on cloud status");
    }

    logger.info(context.logKey + "verifying mtd health checks");
    List<MtdHostHealthCheck> healthChecks = host.getMtdHealthChecks();
    Map<Integer, EcvListener> expectedChecksMap = getHealthChecks(wo, context).stream().
        collect(Collectors.toMap(e -> e.port, Function.identity()));
    verify(() -> ((healthChecks != null ? healthChecks.size() : 0) ==
            (expectedChecksMap != null ? expectedChecksMap.size() : 0)),
        "all health checks are configured");
    if (healthChecks != null) {
      for (MtdHostHealthCheck healthCheck : healthChecks) {
        verify(() -> expectedChecksMap.containsKey(healthCheck.getPort()), "mtd health check available for port");
        EcvListener listener = expectedChecksMap.get(healthCheck.getPort());
        verify(() -> listener.protocol.equals(healthCheck.getProtocol()), "mtd health protocol matches");
        if (listener.ecv != null && !listener.ecv.isEmpty()) {
          verify(() -> listener.ecv.equals(healthCheck.getTestObjectPath()), "mtd health ecv matches");
        }
      }
    }
  }

  private List<Lb> getLbVips(CmsWorkOrderSimple wo) {
    Map<String, List<CmsRfcCISimple>> map = wo.getPayLoad();
    List<CmsRfcCISimple> lbs = map.get(WoHelper.LB_PAYLOAD);
    Map<Long, CmsRfcCISimple> cloudCiMap = map.get("clouds").stream()
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
        int port = Integer.parseInt(config[1]);
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

  private String getMtdBase(CmsWorkOrderSimple wo) {
    String mtdBaseHost;
    Map<String, List<CmsRfcCISimple>> payload = wo.getPayLoad();
    String assembly = payload.get("Assembly").get(0).getCiName();
    CmsRfcCISimple env = payload.get("Environment").get(0);
    String environment = env.getCiName();
    String org = payload.get("Organization").get(0).getCiName();
    String subdomain = env.getCiAttributes().get("subdomain");
    String cloud = wo.cloud.getCiName();
    String baseGslbDomain = wo.services.get("gdns").get(cloud).getCiAttributes().get("gslb_base_domain");
    if (subdomain != null)
      mtdBaseHost = "." + subdomain + "." + baseGslbDomain;
    else
      mtdBaseHost = "." + environment + "." + assembly + "." + org + "." + baseGslbDomain;
    return mtdBaseHost;
  }

  private VerifyContext getContext(CmsWorkOrderSimple wo, Config config) throws Exception {
    VerifyContext context = new VerifyContext();
    context.torbitClient = new TorbitClient(config);
    context.torbit = context.torbitClient.getTorbit();
    context.mtdBaseHost = getMtdBase(wo);
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
  }

  class VerifyContext {
    TorbitClient torbitClient;
    TorbitApi torbit;
    String mtdBaseHost;
    String platform;
    String logKey;
  }

}
