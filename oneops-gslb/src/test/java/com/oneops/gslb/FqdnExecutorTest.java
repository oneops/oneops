package com.oneops.gslb;

import static org.mockito.Mockito.mock;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.gslb.v2.domain.Cloud;
import com.oneops.gslb.v2.domain.MtdHostHealthCheck;
import com.oneops.gslb.v2.domain.MtdTarget;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FqdnExecutorTest {

  MtdHandler handler = new MtdHandler();
  FqdnExecutor executor = new FqdnExecutor();

  @Before
  public void init() {
    loadCloudMap();
    handler.woHelper = new WoHelper();
    handler.jsonParser = new JsonParser();
    Gson gson = new Gson();
    handler.gson = gson;
    handler.woHelper.gson = gson;
    handler.interval = "3s";
    handler.timeOut = "2s";
    handler.failureCountToMarkDown = 3;
    handler.retryDelay = "20s";

  }

  private void loadCloudMap() {
    handler.cloudMap.put("cl1", Cloud.create(10, "cl1", 5, null));
    handler.cloudMap.put("cl2", Cloud.create(12, "cl2", 5, null));
  }

  @Test
  public void testTorbitConfig() {
    CmsWorkOrderSimple wo = woBase();
    Config torbitConfig = executor.getTorbitConfig(wo, "");
    Assert.assertNull(torbitConfig);
    addLbPayload(wo);
    torbitConfig = executor.getTorbitConfig(wo, "");
    Assert.assertNull(torbitConfig);
    addGdnsService(wo);
    torbitConfig = executor.getTorbitConfig(wo, "");
    Assert.assertNotNull(torbitConfig);
    Map<String, String> attributes = wo.getServices().get("torbit").get(wo.getCloud().getCiName()).getCiAttributes();
    Assert.assertEquals(torbitConfig.getUrl(), attributes.get("endpoint"));
    Assert.assertEquals(torbitConfig.getAuthKey(), attributes.get("auth_key"));
    Assert.assertEquals(torbitConfig.getUser(), attributes.get("user_name"));
  }

  @Test
  public void testMtdTargets() {
    CmsWorkOrderSimple wo = wo();
    Context context = getContext();
    try {
      List<MtdTarget> mtdTargets = handler.getMtdTargets(wo, context);
      Assert.assertEquals(2, mtdTargets.size());
      MtdTarget target1 = mtdTargets.get(0);
      Assert.assertEquals(Long.valueOf(10), new Long(target1.cloudId()));
      Assert.assertEquals(true, target1.enabled());
      Assert.assertEquals("1.1.1.0",target1.mtdTargetHost());

      MtdTarget target2 = mtdTargets.get(1);
      Assert.assertEquals(Long.valueOf(12), new Long(target2.cloudId()));
      Assert.assertEquals(true, target2.enabled());
      Assert.assertEquals("1.1.1.1",target2.mtdTargetHost());

    } catch(Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testMtdHealthCheck() {
    CmsWorkOrderSimple wo = wo();
    Context context = getContext();
    try {
      List<MtdHostHealthCheck> healthChecks = handler.getHealthChecks(wo, context);
      Assert.assertEquals(1, healthChecks.size());
      MtdHostHealthCheck healthCheck = healthChecks.get(0);
      Assert.assertEquals(80, healthCheck.port().longValue());
      Assert.assertEquals("http", healthCheck.protocol());
      Assert.assertEquals("/", healthCheck.testObjectPath());
    } catch(Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  private Context getContext() {
    Context context = new Context();
    context.setTorbitClient(mock(TorbitClient.class));
    return context;
  }

  private CmsWorkOrderSimple wo() {
    CmsWorkOrderSimple wo = woBase();
    addLbPayload(wo);
    addGdnsService(wo);
    addCloudsPayload(wo);
    addRealizedAs(wo);
    addDependsOn(wo);
    return wo;
  }

  private void addDependsOn(CmsWorkOrderSimple wo) {
    CmsRfcCISimple bomLb = new CmsRfcCISimple();
    bomLb.setCiClassName("bom.oneops.1.Lb");
    bomLb.setCiId(650l);
    bomLb.addCiAttribute("listeners", "['http 80 http 80']");
    bomLb.addCiAttribute("ecv_map", "{'80':'GET /'}");
    wo.addPayLoadEntry("DependsOn", bomLb);
  }

  private CmsWorkOrderSimple woBase() {
    CmsWorkOrderSimple woBase = new CmsWorkOrderSimple();
    CmsRfcCISimple rfc = new CmsRfcCISimple();
    rfc.setRfcId(4001l);
    rfc.setCiName("test-gslb");
    rfc.setCiClassName("bom.oneops.1.Fqdn");
    //rfc.setCiAttributes();
    woBase.setRfcCi(rfc);

    CmsCISimple cloud = new CmsCISimple();
    cloud.setCiName("cl1");
    woBase.setCloud(cloud);

    return woBase;
  }

  private void addRealizedAs(CmsWorkOrderSimple wo) {
    CmsRfcCISimple manifest = new CmsRfcCISimple();
    manifest.setCiClassName("manifest.oneops.1.Fqdn");
    manifest.setCiId(110);
    manifest.addCiAttribute("distribution", "proximity");
    wo.addPayLoadEntry("RealizedAs", manifest);
  }

  private void addLbPayload(CmsWorkOrderSimple wo) {
    CmsRfcCISimple lb1 = new CmsRfcCISimple();
    lb1.setCiName("lb-101-1");
    lb1.addCiAttribute("dns_record", "1.1.1.0");
    lb1.setCiClassName("bom.oneops.1.Lb");

    CmsRfcCISimple lb2 = new CmsRfcCISimple();
    lb2.setCiName("lb-102-1");
    lb2.addCiAttribute("dns_record", "1.1.1.1");
    lb2.setCiClassName("bom.oneops.1.Lb");

    wo.putPayLoadEntry("lb", Arrays.asList(lb1, lb2));
  }

  private void addCloudsPayload(CmsWorkOrderSimple wo) {
    CmsRfcCISimple cl1 = new CmsRfcCISimple();
    cl1.setCiName("cl1");
    cl1.setCiId(101);
    cl1.addCiAttribute("base.Consumes.adminstatus", "active");
    cl1.addCiAttribute("base.Consumes.priority", "1");
    cl1.setCiClassName("account.Cloud");

    CmsRfcCISimple cl2 = new CmsRfcCISimple();
    cl2.setCiName("cl2");
    cl2.setCiId(102);
    cl2.addCiAttribute("base.Consumes.adminstatus", "active");
    cl2.addCiAttribute("base.Consumes.priority", "1");
    cl2.setCiClassName("account.Cloud");

    wo.putPayLoadEntry("fqdnclouds", Arrays.asList(cl1, cl2));
  }

  private void addGdnsService(CmsWorkOrderSimple wo) {
    Map<String, Map<String, CmsCISimple>> services = new HashMap<>();
    Map<String, CmsCISimple> gdnsService = new HashMap<>();
    CmsCISimple gdns = new CmsCISimple();
    gdns.setCiClassName("cloud.service.oneops.1.Torbit");
    gdns.setCiId(102l);
    gdns.addCiAttribute("endpoint", "https://localhost:8443");
    gdns.addCiAttribute("auth_key", "test_auth");
    gdns.addCiAttribute("group_id", "101");
    gdns.addCiAttribute("user_name", "test-oo");
    gdns.addCiAttribute("gslb_base_domain", "xyz.com");
    gdns.setCiName("torbit");
    gdnsService.put("cl1", gdns);
    services.put("torbit", gdnsService);
    wo.setServices(services);
  }

}
