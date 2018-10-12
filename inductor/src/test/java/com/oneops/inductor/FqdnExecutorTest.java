package com.oneops.inductor;

import static com.oneops.gslb.domain.Protocol.HTTP;
import static com.oneops.gslb.domain.Protocol.HTTPS;
import static com.oneops.gslb.domain.Protocol.TCP;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.execution.Response;
import com.oneops.cms.execution.Result;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.gslb.GslbProvider;
import com.oneops.gslb.Status;
import com.oneops.gslb.domain.CloudARecord;
import com.oneops.gslb.domain.DcARecord;
import com.oneops.gslb.domain.Distribution;
import com.oneops.gslb.domain.Gslb;
import com.oneops.gslb.domain.GslbProvisionResponse;
import com.oneops.gslb.domain.HealthCheck;
import com.oneops.gslb.domain.InfobloxConfig;
import com.oneops.gslb.domain.Lb;
import com.oneops.gslb.domain.Protocol;
import com.oneops.gslb.domain.ProvisionedGslb;
import com.oneops.gslb.domain.TorbitConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class FqdnExecutorTest {

  FqdnExecutor fqdnExecutor = new FqdnExecutor();
  GslbProvider mock = mock(GslbProvider.class);

  Map<String, Map<String, String>> classesMap = new HashMap<>();

  private static String ONEOPS_CLASS = "oneops";
  private static String BASE_CLASS = "base";

  private static String BOM_LB = "bom_lb";
  private static String MANIFEST_LB = "manifest_lb";
  private static String BOM_FQDN = "bom_fqdn";
  private static String MANIFEST_FQDN = "manifest_fqdn";

  @Before
  public void setup() {
    fqdnExecutor.gson = new Gson();
    when(mock.create(any())).thenReturn(successProvisionResponse());
    fqdnExecutor.gslbProvider = mock;
    WoHelper woHelper = new WoHelper();
    woHelper.gson = fqdnExecutor.gson;
    fqdnExecutor.jsonParser = new JsonParser();
    fqdnExecutor.woHelper = woHelper;
    initClasses();
  }

  private void initClasses() {
    Map<String, String> map = new HashMap<>();
    map.put(BOM_LB, "bom.Lb");
    map.put(MANIFEST_LB, "manifest.Lb");
    map.put(BOM_FQDN, "bom.Fqdn");
    map.put(MANIFEST_FQDN, "manifest.Fqdn");
    classesMap.put(BASE_CLASS, map);

    map = new HashMap<>();
    map.put(BOM_LB, "bom.oneops.1.Lb");
    map.put(MANIFEST_LB, "manifest.oneops.1.Lb");
    map.put(BOM_FQDN, "bom.oneops.1.Fqdn");
    map.put(MANIFEST_FQDN, "manifest.oneops.1.Fqdn");
    classesMap.put(ONEOPS_CLASS, map);
  }

  private Map<String, String> oneops_class() {
    return classesMap.get(ONEOPS_CLASS);
  }

  private Map<String, String> base_class() {
    return classesMap.get(BASE_CLASS);
  }

  private CmsWorkOrderSimple woWith2Clouds() {
    return woWith2Clouds(oneops_class());
  }

  @Test
  public void addGslbRequestWithTwoClouds() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getRfcCi().setRfcAction("add");
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).create(argument.capture());

    Gslb request = argument.getValue();
    assertThat(request.app(), is("plt"));
    assertThat(request.subdomain(), is("stg.coll.org"));
    assertThat(request.distribution(), is(Distribution.PROXIMITY));

    List<Lb> lbs = request.lbs();
    assertThat(lbs.size(), is(2));
    assertThat(lbs.get(0).vip(), is("1.1.1.0"));
    assertThat(lbs.get(0).cloud(), is("cl1"));
    assertThat(lbs.get(0).enabledForTraffic(), is(true));
    assertNull(lbs.get(0).weightPercent());

    assertThat(lbs.get(1).vip(), is("1.1.1.1"));
    assertThat(lbs.get(1).cloud(), is("cl2"));
    assertThat(lbs.get(1).enabledForTraffic(), is(true));
    assertNull(lbs.get(1).weightPercent());

    InfobloxConfig ibConfig = request.infobloxConfig();
    assertThat(ibConfig.host(), is("https://localhost:8123"));
    assertThat(ibConfig.user(), is("test-usr"));
    assertThat(ibConfig.zone(), is("stg.cloud.xyz.com"));

    TorbitConfig torbitConfig = request.torbitConfig();
    assertThat(torbitConfig.url(), is("https://localhost:8443"));
    assertThat(torbitConfig.user(), is("test-oo"));
    assertThat(torbitConfig.groupId(), is(101));
    assertThat(torbitConfig.gslbBaseDomain(), is("xyz.com"));

    List<HealthCheck> healthChecks = request.healthChecks();
    assertThat(healthChecks.size(), is(1));
    HealthCheck healthCheck = healthChecks.get(0);
    assertThat(healthCheck.protocol(), is(HTTP));
    assertThat(healthCheck.port(), is(80));
    assertThat(healthCheck.path(), is("/"));

    Set<String> cnames = new HashSet<>(request.cnames());
    assertThat(cnames.size(), is(3));
    assertTrue(cnames.contains("plt.stg.coll.org.stg.cloud.xyz.com"));
    assertTrue(cnames.contains("p1.e1.a1.org.xyz.com"));
    assertTrue(cnames.contains("p1.stg.coll.org.stg.cloud.xyz.com"));

    List<CloudARecord> cloudARecords = request.cloudARecords();
    assertThat(cloudARecords.size(), is(1));
    CloudARecord cloudARecord = cloudARecords.get(0);
    assertThat(cloudARecord.vip(), is("1.1.1.0"));
    assertThat(cloudARecord.aRecord(), is("plt.stg.coll.org.cloud1.stg.cloud.xyz.com"));
  }

  @Test
  public void gslbRequestWithEqualWeights() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getRfcCi().setRfcAction("add");
    Map<String, String> map = new HashMap<>();
    map.put("weights", "{\"cl1\": 50, \"cl2\" : 50}");
    wo.setConfig(map);
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).create(argument.capture());

    Gslb request = argument.getValue();
    assertThat(request.lbs().get(0).weightPercent(), is(50));
    assertThat(request.lbs().get(1).weightPercent(), is(50));
  }

  @Test
  public void gslbRequestWithDifferentWeights() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getRfcCi().setRfcAction("add");
    Map<String, String> map = new HashMap<>();
    map.put("weights", "{\"cl1\": 60, \"cl2\" : 40}");
    wo.setConfig(map);
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).create(argument.capture());

    Gslb request = argument.getValue();
    assertThat(request.lbs().get(0).weightPercent(), is(60));
    assertThat(request.lbs().get(1).weightPercent(), is(40));
  }

  @Test
  public void gslbRequestWithMissingWeights() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getRfcCi().setRfcAction("add");
    Map<String, String> map = new HashMap<>();
    map.put("weights", "{\"cl1\": 100}");
    wo.setConfig(map);
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).create(argument.capture());

    Gslb request = argument.getValue();
    assertThat(request.lbs().get(0).weightPercent(), is(100));
    assertThat(request.lbs().get(1).weightPercent(), is(0));
  }

  @Test
  public void dcEntryWithNetscalerGdns() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getRfcCi().setRfcAction("add");
    addGdnsService(wo);
    addLbVnames(wo, "10.100.100.101");
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).create(argument.capture());

    Gslb request = argument.getValue();

    Set<String> cnames = new HashSet<>(request.cnames());
    assertThat(cnames.size(), is(3));
    assertTrue(cnames.contains("plt.stg.coll.org.stg.cloud.xyz.com"));
    assertTrue(cnames.contains("p1.e1.a1.org.xyz.com"));
    assertTrue(cnames.contains("p1.stg.coll.org.stg.cloud.xyz.com"));

    List<CloudARecord> cloudARecords = request.cloudARecords();
    assertThat(cloudARecords.size(), is(1));
    CloudARecord cloudARecord = cloudARecords.get(0);
    assertThat(cloudARecord.vip(), is("1.1.1.0"));
    assertThat(cloudARecord.aRecord(), is("plt.stg.coll.org.cloud1.stg.cloud.xyz.com"));

    List<DcARecord> dcARecords = request.dcARecords();
    assertThat(dcARecords.size(), is(1));
    DcARecord dcARecord = dcARecords.get(0);
    assertThat(dcARecord.aRecord(), is("plt.stg.coll.org.c1.stg.cloud.xyz.com"));
    assertThat(dcARecord.vip(), is("10.100.100.101"));
  }

  @Test
  public void lbWithCloudVipEnabled() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getRfcCi().setRfcAction("add");
    addGdnsService(wo);
    addLbVnames(wo, "10.100.100.102");
    setLbVnamesForCloudVips(wo, "10.100.100.102", "10.100.101.103");
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).create(argument.capture());

    Gslb request = argument.getValue();

    Set<String> cnames = new HashSet<>(request.cnames());
    assertThat(cnames.size(), is(3));
    assertTrue(cnames.contains("plt.stg.coll.org.stg.cloud.xyz.com"));
    assertTrue(cnames.contains("p1.e1.a1.org.xyz.com"));
    assertTrue(cnames.contains("p1.stg.coll.org.stg.cloud.xyz.com"));

    List<Lb> lbs = request.lbs();
    assertThat(lbs.size(), is(2));
    assertThat(lbs.get(0).vip(), is("10.100.100.102"));
    assertThat(lbs.get(0).cloud(), is("cl1"));
    assertThat(lbs.get(0).enabledForTraffic(), is(true));

    assertThat(lbs.get(1).vip(), is("10.100.101.103"));
    assertThat(lbs.get(1).cloud(), is("cl2"));
    assertThat(lbs.get(1).enabledForTraffic(), is(true));

    List<CloudARecord> cloudARecords = request.cloudARecords();
    assertThat(cloudARecords.size(), is(1));
    CloudARecord cloudARecord = cloudARecords.get(0);
    assertThat(cloudARecord.vip(), is("1.1.1.0"));
    assertThat(cloudARecord.aRecord(), is("plt.stg.coll.org.cloud1.stg.cloud.xyz.com"));

    List<DcARecord> dcARecords = request.dcARecords();
    assertThat(dcARecords.size(), is(1));
    DcARecord dcARecord = dcARecords.get(0);
    assertThat(dcARecord.aRecord(), is("plt.stg.coll.org.c1.stg.cloud.xyz.com"));
    assertThat(dcARecord.vip(), is("10.100.100.102"));
  }

  @Test
  public void lbWithCloudVipEnabledAndDifferentSubdomain() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getRfcCi().setRfcAction("add");
    addGdnsService(wo);
    addLbVnames(wo, "10.100.100.102");
    setLbVnamesForCloudVips(wo, "10.100.100.102", "10.100.101.103");
    wo.getPayLoadEntry("Environment").get(0).addCiAttribute("subdomain", "coll.org");
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).create(argument.capture());

    Gslb request = argument.getValue();

    Set<String> cnames = new HashSet<>(request.cnames());
    assertThat(cnames.size(), is(3));
    assertTrue(cnames.contains("plt.coll.org.stg.cloud.xyz.com"));
    assertTrue(cnames.contains("p1.e1.a1.org.xyz.com"));
    assertTrue(cnames.contains("p1.coll.org.stg.cloud.xyz.com"));

    List<Lb> lbs = request.lbs();
    assertThat(lbs.size(), is(2));
    assertThat(lbs.get(0).vip(), is("10.100.100.102"));
    assertThat(lbs.get(0).cloud(), is("cl1"));
    assertThat(lbs.get(0).enabledForTraffic(), is(true));

    assertThat(lbs.get(1).vip(), is("10.100.101.103"));
    assertThat(lbs.get(1).cloud(), is("cl2"));
    assertThat(lbs.get(1).enabledForTraffic(), is(true));

    List<CloudARecord> cloudARecords = request.cloudARecords();
    assertThat(cloudARecords.size(), is(1));
    CloudARecord cloudARecord = cloudARecords.get(0);
    assertThat(cloudARecord.vip(), is("1.1.1.0"));
    assertThat(cloudARecord.aRecord(), is("plt.coll.org.cloud1.stg.cloud.xyz.com"));

    List<DcARecord> dcARecords = request.dcARecords();
    assertThat(dcARecords.size(), is(1));
    DcARecord dcARecord = dcARecords.get(0);
    assertThat(dcARecord.aRecord(), is("plt.coll.org.c1.stg.cloud.xyz.com"));
    assertThat(dcARecord.vip(), is("10.100.100.102"));
  }

  private void setLbVnamesForCloudVips(CmsWorkOrderSimple wo, String... vips) {
    List<CmsRfcCISimple> lbs = wo.payLoad.get("lb");
    int i = 0;
    for (CmsRfcCISimple lb : lbs) {
      Map<String, String> attributes = lb.getCiAttributes();
      attributes.put("create_cloud_level_vips", "true");
      String attribute =
          "{\"plt.stg.coll.org.c1.stg.cloud.xyz.com-HTTP_80tcp-123214-lb\":\"" + vips[i++] + "\"}";
      attributes.put("vnames", attribute);
    }
  }

  private void addLbVnames(CmsWorkOrderSimple wo, String dcVip) {
    Map<String, String> attributes = wo.getPayLoadEntry("DependsOn").get(0).getCiAttributes();
    String attribute =
        "{\"plt.stg.coll.org.c1.stg.cloud.xyz.com-HTTP_80tcp-123214-lb\":\"" + dcVip + "\"}";
    attributes.put("vnames", attribute);
  }

  private void addGdnsService(CmsWorkOrderSimpleBase wo) {
    Map<String, Map<String, CmsCISimple>> services = wo.getServices();
    wo.setServices(services);
    Map<String, CmsCISimple> gdnsService = new HashMap<>();
    CmsCISimple gdns = new CmsCISimple();
    gdns.setCiClassName("cloud.service.Netscaler");
    gdns.setCiId(1100L);
    gdns.addCiAttribute("gslb_site_dns_id", "c1");
    gdns.setCiName("gdns");
    gdnsService.put("cl1", gdns);
    services.put("gdns", gdnsService);
  }

  @Test
  public void updateWithOnePrimaryOneSecondaryClouds() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    // make one cloud secondary
    wo.getPayLoadEntry("fqdnclouds").get(1).addCiAttribute("base.Consumes.priority", "2");
    wo.getRfcCi().setRfcAction("update");
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).create(argument.capture());

    Gslb request = argument.getValue();
    List<Lb> lbs = request.lbs();
    assertThat(lbs.size(), is(2));
    assertThat(lbs.get(0).cloud(), is("cl1"));
    assertThat(lbs.get(0).enabledForTraffic(), is(true));
    assertThat(lbs.get(0).vip(), is("1.1.1.0"));

    assertThat(lbs.get(1).cloud(), is("cl2"));
    assertThat(lbs.get(1).enabledForTraffic(), is(false));
    assertThat(lbs.get(1).vip(), is("1.1.1.1"));
  }

  @Test
  public void deleteWithShutdownCloud() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    // make one cloud secondary
    wo.getPayLoadEntry("fqdnclouds").get(0).addCiAttribute("base.Consumes.adminstatus", "inactive");
    wo.getRfcCi().setRfcAction("delete");
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).create(argument.capture());

    Gslb request = argument.getValue();
    List<Lb> lbs = request.lbs();
    assertThat(lbs.size(), is(1));
    assertThat(lbs.get(0).cloud(), is("cl2"));
    assertThat(lbs.get(0).enabledForTraffic(), is(true));
    assertThat(lbs.get(0).vip(), is("1.1.1.1"));
  }

  @Test
  public void deleteWithPlatformDisable() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    // make one cloud secondary
    wo.getBox().addCiAttribute("is_platform_enabled", "false");
    wo.getRfcCi().setRfcAction("delete");
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<ProvisionedGslb> argument = ArgumentCaptor.forClass(ProvisionedGslb.class);
    verify(mock).delete(argument.capture());

    ProvisionedGslb request = argument.getValue();
    assertThat(request.app(), is("plt"));
    assertThat(request.subdomain(), is("stg.coll.org"));

    InfobloxConfig ibConfig = request.infobloxConfig();
    assertThat(ibConfig.host(), is("https://localhost:8123"));
    assertThat(ibConfig.user(), is("test-usr"));
    assertThat(ibConfig.zone(), is("stg.cloud.xyz.com"));

    TorbitConfig torbitConfig = request.torbitConfig();
    assertThat(torbitConfig.url(), is("https://localhost:8443"));
    assertThat(torbitConfig.user(), is("test-oo"));
    assertThat(torbitConfig.groupId(), is(101));
    assertThat(torbitConfig.gslbBaseDomain(), is("xyz.com"));

    Set<String> cnames = new HashSet<>(request.cnames());
    System.out.println(cnames);
    assertThat(cnames.size(), is(3));
    assertTrue(cnames.contains("plt.stg.coll.org.stg.cloud.xyz.com"));
    assertTrue(cnames.contains("p1.e1.a1.org.xyz.com"));
    assertTrue(cnames.contains("p1.stg.coll.org.stg.cloud.xyz.com"));

    List<CloudARecord> cloudARecords = request.cloudARecords();
    assertThat(cloudARecords.size(), is(1));
    CloudARecord cloudARecord = cloudARecords.get(0);
    assertThat(cloudARecord.vip(), is("1.1.1.0"));
    assertThat(cloudARecord.aRecord(), is("plt.stg.coll.org.cloud1.stg.cloud.xyz.com"));
  }

  @Test
  public void shouldNotMatchIfGdnsDisabled() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getPayLoadEntry("Environment").get(0).addCiAttribute("global_dns", "false");
    Response response = fqdnExecutor.execute(wo, "/tmp");
    assertThat(response.getResult(), is(Result.NOT_MATCHED));
  }

  @Test
  public void shouldNotMatchForLocalWo() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getPayLoadEntry("Environment").get(0).addCiAttribute("global_dns", "false");
    wo.addPayLoadEntry("ManagedVia", new CmsRfcCISimple());
    Response response = fqdnExecutor.execute(wo, "/tmp");
    assertThat(response.getResult(), is(Result.NOT_MATCHED));
  }

  @Test
  public void replaceWith2Clouds() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getRfcCi().setRfcAction("replace");
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).create(argument.capture());

    Gslb request = argument.getValue();
    assertThat("plt", is(request.app()));
    assertThat("stg.coll.org", is(request.subdomain()));
    assertThat(request.distribution(), is(Distribution.PROXIMITY));

    List<Lb> lbs = request.lbs();
    assertThat(lbs.size(), is(2));
    assertThat(lbs.get(0).vip(), is("1.1.1.0"));
    assertThat(lbs.get(0).cloud(), is("cl1"));
    assertThat(lbs.get(0).enabledForTraffic(), is(true));

    assertThat(lbs.get(1).vip(), is("1.1.1.1"));
    assertThat(lbs.get(1).cloud(), is("cl2"));
    assertThat(lbs.get(1).enabledForTraffic(), is(true));
  }

  @Test
  public void failForMissingTorbitConfig() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getRfcCi().setRfcAction("update");
    wo.services.remove("torbit");
    Response response = fqdnExecutor.execute(wo, "/tmp");
    assertThat(response.getResult(), is(Result.FAILED));
  }

  @Test
  public void failForMissingDnsConfig() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.services.remove("dns");
    Response response = fqdnExecutor.execute(wo, "/tmp");
    assertThat(response.getResult(), is(Result.FAILED));
  }

  @Test
  public void updateWithNewCnames() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getRfcCi().setRfcAction("update");
    wo.getRfcCi().addCiBaseAttribute("aliases", "[olda1]");
    wo.getRfcCi().addCiBaseAttribute("full_aliases", "[oldfa1.xyz.com]");
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).create(argument.capture());

    Gslb request = argument.getValue();
    Set<String> cnames = new HashSet<>(request.cnames());
    assertThat(cnames.size(), is(3));
    assertTrue(cnames.contains("plt.stg.coll.org.stg.cloud.xyz.com"));
    assertTrue(cnames.contains("p1.e1.a1.org.xyz.com"));
    assertTrue(cnames.contains("p1.stg.coll.org.stg.cloud.xyz.com"));

    Set<String> obsoleteCnames = new HashSet<>(request.obsoleteCnames());
    System.out.println(obsoleteCnames);
    assertThat(obsoleteCnames.size(), is(2));
    assertTrue(obsoleteCnames.contains("oldfa1.xyz.com"));
    assertTrue(obsoleteCnames.contains("olda1.stg.coll.org.stg.cloud.xyz.com"));

    List<CloudARecord> cloudARecords = request.cloudARecords();
    assertThat(cloudARecords.size(), is(1));
    CloudARecord cloudARecord = cloudARecords.get(0);
    assertThat(cloudARecord.vip(), is("1.1.1.0"));
    assertThat(cloudARecord.aRecord(), is("plt.stg.coll.org.cloud1.stg.cloud.xyz.com"));
  }

  @Test
  public void envWithCustomSubDomain() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getPayLoadEntry("Environment").get(0).addCiAttribute("subdomain", "test1.e2.a1.o1");
    wo.getRfcCi().setRfcAction("update");
    fqdnExecutor.execute(wo, "/tmp");

    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).create(argument.capture());
    Gslb request = argument.getValue();
    assertThat(request.app(), is("plt"));
    assertThat(request.subdomain(), is("test1.e2.a1.o1"));
  }

  @Test
  public void gslbRequestWithTwoCloudsAndBaseFqdn() {
    CmsWorkOrderSimple wo = woWith2Clouds(base_class());
    wo.getRfcCi().setRfcAction("add");
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).create(argument.capture());

    Gslb request = argument.getValue();
    assertThat(request.app(), is("plt"));
    assertThat(request.subdomain(), is("stg.coll.org"));
    assertThat(request.distribution(), is(Distribution.PROXIMITY));

    List<Lb> lbs = request.lbs();
    assertThat(lbs.size(), is(2));
    assertThat(lbs.get(0).vip(), is("1.1.1.0"));
    assertThat(lbs.get(0).cloud(), is("cl1"));
    assertThat(lbs.get(0).enabledForTraffic(), is(true));

    assertThat(lbs.get(1).vip(), is("1.1.1.1"));
    assertThat(lbs.get(1).cloud(), is("cl2"));
    assertThat(lbs.get(1).enabledForTraffic(), is(true));

    InfobloxConfig ibConfig = request.infobloxConfig();
    assertThat(ibConfig.host(), is("https://localhost:8123"));
    assertThat(ibConfig.user(), is("test-usr"));
    assertThat(ibConfig.zone(), is("stg.cloud.xyz.com"));

    TorbitConfig torbitConfig = request.torbitConfig();
    assertThat(torbitConfig.url(), is("https://localhost:8443"));
    assertThat(torbitConfig.user(), is("test-oo"));
    assertThat(torbitConfig.groupId(), is(101));
    assertThat(torbitConfig.gslbBaseDomain(), is("xyz.com"));

    List<HealthCheck> healthChecks = request.healthChecks();
    assertThat(healthChecks.size(), is(1));
    HealthCheck healthCheck = healthChecks.get(0);
    assertThat(healthCheck.protocol(), is(HTTP));
    assertThat(healthCheck.port(), is(80));
    assertThat(healthCheck.path(), is("/"));

    Set<String> cnames = new HashSet<>(request.cnames());
    assertThat(cnames.size(), is(3));
    assertTrue(cnames.contains("plt.stg.coll.org.stg.cloud.xyz.com"));
    assertTrue(cnames.contains("p1.e1.a1.org.xyz.com"));
    assertTrue(cnames.contains("p1.stg.coll.org.stg.cloud.xyz.com"));

    List<CloudARecord> cloudARecords = request.cloudARecords();
    assertThat(cloudARecords.size(), is(1));
    CloudARecord cloudARecord = cloudARecords.get(0);
    assertThat(cloudARecord.vip(), is("1.1.1.0"));
    assertThat(cloudARecord.aRecord(), is("plt.stg.coll.org.cloud1.stg.cloud.xyz.com"));
  }

  private GslbProvisionResponse successProvisionResponse() {
    GslbProvisionResponse response = new GslbProvisionResponse();
    response.setGlb("ad.stg.coll.xyz.com");
    response.setMtdDeploymentId("100");
    response.setMtdVersion("101");
    response.setMtdBaseId("50");
    response.setStatus(Status.SUCCESS);
    return response;
  }

  private CmsWorkOrderSimple woWith2Clouds(Map<String, String> classMap) {
    CmsWorkOrderSimple wo = woBase(classMap);
    addLbPayload(wo, classMap);
    addCloudService(wo);
    addCloudsPayload(wo);
    addRealizedAs(wo, classMap);
    addDependsOn(wo, classMap);
    return wo;
  }

  private void addDependsOn(CmsWorkOrderSimple wo, Map<String, String> classMap) {
    CmsRfcCISimple bomLb;
    List<CmsRfcCISimple> list = wo.getPayLoadEntry("lb");
    bomLb = (list != null && !list.isEmpty()) ? list.get(0) : new CmsRfcCISimple();
    bomLb.addCiAttribute("listeners", "['http 80 http 80']");
    bomLb.addCiAttribute("ecv_map", "{'80':'GET /'}");
    wo.addPayLoadEntry("DependsOn", bomLb);
  }

  private CmsWorkOrderSimple woBase(Map<String, String> classMap) {
    CmsWorkOrderSimple woBase = new CmsWorkOrderSimple();
    CmsRfcCISimple rfc = new CmsRfcCISimple();
    rfc.setRfcId(4001L);
    rfc.setCiName("test-gslb");
    rfc.setCiClassName(classMap.get(BOM_FQDN));
    rfc.addCiAttribute("aliases", "[p1]");
    rfc.addCiAttribute("full_aliases", "[p1.e1.a1.org.xyz.com]");
    rfc.addCiAttribute("distribution", "proximity");
    rfc.addCiAttribute("service_type", "torbit");
    woBase.setRfcCi(rfc);

    CmsCISimple cloud = new CmsCISimple();
    cloud.setCiName("cl1");
    woBase.setCloud(cloud);

    CmsCISimple box = new CmsCISimple();
    box.setCiName("plt");
    box.addCiAttribute("is_platform_enabled", "true");
    woBase.setBox(box);

    CmsRfcCISimple assmbly = new CmsRfcCISimple();
    assmbly.setCiName("coll");
    woBase.addPayLoadEntry("Assembly", assmbly);

    CmsRfcCISimple org = new CmsRfcCISimple();
    org.setCiName("org");
    woBase.addPayLoadEntry("Organization", org);

    CmsRfcCISimple env = new CmsRfcCISimple();
    env.setCiName("stg");
    env.addCiAttribute("global_dns", "true");
    woBase.addPayLoadEntry("Environment", env);
    return woBase;
  }

  private void addRealizedAs(CmsWorkOrderSimple wo, Map<String, String> classMap) {
    CmsRfcCISimple manifest = new CmsRfcCISimple();
    manifest.setCiClassName(classMap.get(MANIFEST_FQDN));
    manifest.setCiId(110);
    manifest.addCiAttribute("distribution", "proximity");
    manifest.addCiAttribute("service_type", "torbit");
    wo.addPayLoadEntry("RealizedAs", manifest);
  }

  private void addLbPayload(CmsWorkOrderSimple wo, Map<String, String> classMap) {
    String clazz = classMap.get(BOM_LB);
    CmsRfcCISimple lb1 = new CmsRfcCISimple();
    lb1.setCiName("lb-101-1");
    lb1.addCiAttribute("dns_record", "1.1.1.0");
    lb1.setCiClassName(clazz);
    lb1.setCiId(650);

    CmsRfcCISimple lb2 = new CmsRfcCISimple();
    lb2.setCiName("lb-102-1");
    lb2.addCiAttribute("dns_record", "1.1.1.1");
    lb2.setCiClassName(clazz);
    lb2.setCiId(655);

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

  private void addCloudService(CmsWorkOrderSimpleBase wo) {
    Map<String, Map<String, CmsCISimple>> services = new HashMap<>();
    wo.setServices(services);
    Map<String, CmsCISimple> torbitService = new HashMap<>();
    CmsCISimple torbit = new CmsCISimple();
    torbit.setCiClassName("cloud.service.oneops.1.Torbit");
    torbit.setCiId(102L);
    torbit.addCiAttribute("endpoint", "https://localhost:8443");
    torbit.addCiAttribute("auth_key", "test_auth");
    torbit.addCiAttribute("group_id", "101");
    torbit.addCiAttribute("user_name", "test-oo");
    torbit.addCiAttribute("gslb_base_domain", "xyz.com");
    torbit.setCiName("torbit");
    torbitService.put("cl1", torbit);
    services.put("torbit", torbitService);

    Map<String, CmsCISimple> dnsService = new HashMap<>();
    CmsCISimple dns = new CmsCISimple();
    dns.setCiClassName("cloud.service.Infoblox");
    dns.addCiAttribute("host", "https://localhost:8123");
    dns.addCiAttribute("username", "test-usr");
    dns.addCiAttribute("password", "pwd1");
    dns.addCiAttribute("zone", "stg.cloud.xyz.com");
    dns.addCiAttribute("cloud_dns_id", "cloud1");
    dnsService.put("cl1", dns);
    services.put("dns", dnsService);
  }

  @Test
  public void dontMatchAoWithDifferentServiceType() {
    CmsActionOrderSimple ao = ao();
    ao.getCi().getCiAttributes().put("service_type", "netscaler");
    Response response = fqdnExecutor.execute(ao, "/tmp");
    assertThat(response.getResult(), is(Result.NOT_MATCHED));
  }

  @Test
  public void failAoForMissingTorbitService() {
    CmsActionOrderSimple ao = ao();
    ao.getServices().remove("torbit");
    Response response = fqdnExecutor.execute(ao, "/tmp");
    assertThat(response.getResult(), is(Result.FAILED));
  }

  @Test
  public void failAoWithMissingDnsService() {
    CmsActionOrderSimple ao = ao();
    ao.getServices().remove("dns");
    Response response = fqdnExecutor.execute(ao, "/tmp");
    assertThat(response.getResult(), is(Result.FAILED));
  }

  @Test
  public void aoStatusAction() {
    CmsActionOrderSimple ao = ao();
    fqdnExecutor.execute(ao, "/tmp");
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).checkStatus(argument.capture());

    Gslb request = argument.getValue();
    assertThat(request.app(), is("plt"));
    assertThat(request.subdomain(), is("stg.coll.org"));
    assertThat(request.distribution(), is(Distribution.PROXIMITY));

    List<Lb> lbs = request.lbs();
    assertThat(lbs.size(), is(2));
    assertThat(lbs.get(0).vip(), is("1.1.1.0"));
    assertThat(lbs.get(0).cloud(), is("cl1"));
    assertThat(lbs.get(0).enabledForTraffic(), is(true));

    assertThat(lbs.get(1).vip(), is("1.1.1.1"));
    assertThat(lbs.get(1).cloud(), is("cl2"));
    assertThat(lbs.get(1).enabledForTraffic(), is(true));

    InfobloxConfig ibConfig = request.infobloxConfig();
    assertThat(ibConfig.host(), is("https://localhost:8123"));
    assertThat(ibConfig.user(), is("test-usr"));
    assertThat(ibConfig.zone(), is("stg.cloud.xyz.com"));

    TorbitConfig torbitConfig = request.torbitConfig();
    assertThat(torbitConfig.url(), is("https://localhost:8443"));
    assertThat(torbitConfig.user(), is("test-oo"));
    assertThat(torbitConfig.groupId(), is(101));
    assertThat(torbitConfig.gslbBaseDomain(), is("xyz.com"));

    List<HealthCheck> healthChecks = request.healthChecks();
    assertThat(healthChecks.size(), is(1));
    HealthCheck healthCheck = healthChecks.get(0);
    assertThat(healthCheck.protocol(), is(HTTP));
    assertThat(healthCheck.port(), is(80));
    assertThat(healthCheck.path(), is("/"));

    Set<String> cnames = new HashSet<>(request.cnames());
    assertThat(cnames.size(), is(3));
    assertTrue(cnames.contains("plt.stg.coll.org.stg.cloud.xyz.com"));
    assertTrue(cnames.contains("p1.e1.a1.org.xyz.com"));
    assertTrue(cnames.contains("p1.stg.coll.org.stg.cloud.xyz.com"));

    List<CloudARecord> cloudARecords = request.cloudARecords();
    assertThat(cloudARecords.size(), is(1));
    CloudARecord cloudARecord = cloudARecords.get(0);
    assertThat(cloudARecord.vip(), is("1.1.1.0"));
    assertThat(cloudARecord.aRecord(), is("plt.stg.coll.org.cloud1.stg.cloud.xyz.com"));
  }

  @Test
  public void aoStatusActionForBaseClass() {
    CmsActionOrderSimple ao = ao(base_class());
    fqdnExecutor.execute(ao, "/tmp");
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    verify(mock).checkStatus(argument.capture());

    Gslb request = argument.getValue();
    assertThat(request.app(), is("plt"));
    assertThat(request.subdomain(), is("stg.coll.org"));
    assertThat(request.distribution(), is(Distribution.PROXIMITY));

    List<Lb> lbs = request.lbs();
    assertThat(lbs.size(), is(2));
    assertThat(lbs.get(0).vip(), is("1.1.1.0"));
    assertThat(lbs.get(0).cloud(), is("cl1"));
    assertThat(lbs.get(0).enabledForTraffic(), is(true));

    assertThat(lbs.get(1).vip(), is("1.1.1.1"));
    assertThat(lbs.get(1).cloud(), is("cl2"));
    assertThat(lbs.get(1).enabledForTraffic(), is(true));

    InfobloxConfig ibConfig = request.infobloxConfig();
    assertThat(ibConfig.host(), is("https://localhost:8123"));
    assertThat(ibConfig.user(), is("test-usr"));
    assertThat(ibConfig.zone(), is("stg.cloud.xyz.com"));

    TorbitConfig torbitConfig = request.torbitConfig();
    assertThat(torbitConfig.url(), is("https://localhost:8443"));
    assertThat(torbitConfig.user(), is("test-oo"));
    assertThat(torbitConfig.groupId(), is(101));
    assertThat(torbitConfig.gslbBaseDomain(), is("xyz.com"));

    List<HealthCheck> healthChecks = request.healthChecks();
    assertThat(healthChecks.size(), is(1));
    HealthCheck healthCheck = healthChecks.get(0);
    assertThat(healthCheck.protocol(), is(HTTP));
    assertThat(healthCheck.port(), is(80));
    assertThat(healthCheck.path(), is("/"));

    Set<String> cnames = new HashSet<>(request.cnames());
    assertThat(cnames.size(), is(3));
    assertTrue(cnames.contains("plt.stg.coll.org.stg.cloud.xyz.com"));
    assertTrue(cnames.contains("p1.e1.a1.org.xyz.com"));
    assertTrue(cnames.contains("p1.stg.coll.org.stg.cloud.xyz.com"));

    List<CloudARecord> cloudARecords = request.cloudARecords();
    assertThat(cloudARecords.size(), is(1));
    CloudARecord cloudARecord = cloudARecords.get(0);
    assertThat(cloudARecord.vip(), is("1.1.1.0"));
    assertThat(cloudARecord.aRecord(), is("plt.stg.coll.org.cloud1.stg.cloud.xyz.com"));
  }

  private CmsActionOrderSimple ao() {
    return ao(oneops_class());
  }

  private CmsActionOrderSimple ao(Map<String, String> classMap) {
    CmsActionOrderSimple ao = aoBase(classMap);
    addLbPayload(ao, classMap);
    addCloudService(ao);
    addCloudsPayload(ao);
    addRealizedAs(ao, classMap);
    addDependsOn(ao, classMap);
    return ao;
  }

  private CmsActionOrderSimple aoBase(Map<String, String> classMap) {
    CmsActionOrderSimple aoBase = new CmsActionOrderSimple();
    aoBase.setActionName("gslbstatus");
    CmsCISimple ci = new CmsCISimple();
    ci.setCiId(4001L);
    ci.setCiName("test-gslb");
    ci.setCiClassName(classMap.get(BOM_FQDN));
    ci.addCiAttribute("aliases", "[p1]");
    ci.addCiAttribute("full_aliases", "[p1.e1.a1.org.xyz.com]");
    ci.addCiAttribute("distribution", "proximity");
    ci.addCiAttribute("service_type", "torbit");
    ci.addCiAttribute("gslb_map", "{'glb' : 'plt.stg.coll.org.xyz.com'}");
    aoBase.setCi(ci);

    CmsCISimple cloud = new CmsCISimple();
    cloud.setCiName("cl1");
    aoBase.setCloud(cloud);

    CmsCISimple box = new CmsCISimple();
    box.setCiName("plt");
    box.addCiAttribute("is_platform_enabled", "true");
    aoBase.setBox(box);

    CmsCISimple env = new CmsCISimple();
    env.setCiName("stg");
    env.addCiAttribute("global_dns", "true");
    aoBase.addPayLoadEntry("Environment", env);
    return aoBase;
  }

  private void addLbPayload(CmsActionOrderSimple wo, Map<String, String> classMap) {
    String clazz = classMap.get(BOM_LB);
    CmsCISimple lb1 = new CmsCISimple();
    lb1.setCiName("lb-101-1");
    lb1.addCiAttribute("dns_record", "1.1.1.0");
    lb1.setCiClassName(clazz);

    CmsCISimple lb2 = new CmsCISimple();
    lb2.setCiName("lb-102-1");
    lb2.addCiAttribute("dns_record", "1.1.1.1");
    lb2.setCiClassName(clazz);

    wo.putPayLoadEntry("lb", Arrays.asList(lb1, lb2));
  }

  private void addCloudsPayload(CmsActionOrderSimple wo) {
    CmsCISimple cl1 = new CmsCISimple();
    cl1.setCiName("cl1");
    cl1.setCiId(101);
    cl1.addCiAttribute("base.Consumes.adminstatus", "active");
    cl1.addCiAttribute("base.Consumes.priority", "1");
    cl1.setCiClassName("account.Cloud");

    CmsCISimple cl2 = new CmsCISimple();
    cl2.setCiName("cl2");
    cl2.setCiId(102);
    cl2.addCiAttribute("base.Consumes.adminstatus", "active");
    cl2.addCiAttribute("base.Consumes.priority", "1");
    cl2.setCiClassName("account.Cloud");

    wo.putPayLoadEntry("fqdnclouds", Arrays.asList(cl1, cl2));
  }

  private void addRealizedAs(CmsActionOrderSimple wo, Map<String, String> classMap) {
    CmsCISimple manifest = new CmsCISimple();
    manifest.setCiClassName(classMap.get(MANIFEST_FQDN));
    manifest.setCiId(110);
    manifest.addCiAttribute("distribution", "proximity");
    manifest.addCiAttribute("service_type", "torbit");
    wo.addPayLoadEntry("RealizedAs", manifest);
  }

  private void addDependsOn(CmsActionOrderSimple wo, Map<String, String> classMap) {
    CmsCISimple bomLb;
    List<CmsCISimple> list = wo.getPayLoadEntry("lb");
    bomLb = (list != null && !list.isEmpty()) ? list.get(0) : new CmsCISimple();
    bomLb.addCiAttribute("listeners", "['http 80 http 80']");
    bomLb.addCiAttribute("ecv_map", "{'80':'GET /'}");
    wo.addPayLoadEntry("DependsOn", bomLb);
  }

  @Test
  public void failDeploymentWhenPTREnabled() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getRfcCi().addCiAttribute("ptr_enabled", "true");
    wo.getRfcCi().setRfcAction("add");
    Response response = fqdnExecutor.execute(wo, "/tmp");
    assertThat(response.getResult(), is(Result.FAILED));

    wo = woWith2Clouds();
    wo.getRfcCi().getCiAttributes().put("ptr_enabled", "false");
    response = fqdnExecutor.execute(wo, "/tmp");
    assertThat(response.getResult(), is(Result.SUCCESS));
  }

  @Test
  public void woGslbHealthTest() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    ArgumentCaptor<Gslb> argument = ArgumentCaptor.forClass(Gslb.class);
    CmsRfcCISimple bomLb = wo.getPayLoadEntry("DependsOn").get(0);

    // 1. Http protocol
    bomLb.addCiAttribute("listeners", "['http 80 http 80','http 8080 http 8080']");
    bomLb.addCiAttribute("ecv_map", "{'80':'GET /'}");

    fqdnExecutor.execute(wo, "/tmp");
    verify(mock, Mockito.atLeast(1)).create(argument.capture());
    List<HealthCheck> hcList = argument.getValue().healthChecks();
    assertEquals(2, hcList.size());

    HealthCheck hc11 = newHealthCheck(HTTP, 80, "/", false, 200);
    HealthCheck hc12 = newHealthCheck(HTTP, 8080, "", false, 200);
    assertTrue(hcList.containsAll(Arrays.asList(hc11, hc12)));

    // 2. No listener and ecv
    bomLb.addCiAttribute("listeners", "");
    bomLb.addCiAttribute("ecv_map", "");

    fqdnExecutor.execute(wo, "/tmp");
    verify(mock, Mockito.atLeast(1)).create(argument.capture());
    hcList = argument.getValue().healthChecks();
    assertEquals(0, hcList.size());

    // 3. Null listener and ecv
    bomLb.addCiAttribute("listeners", null);
    bomLb.addCiAttribute("ecv_map", "[]");

    fqdnExecutor.execute(wo, "/tmp");
    verify(mock, Mockito.atLeast(1)).create(argument.capture());
    hcList = argument.getValue().healthChecks();
    assertEquals(0, hcList.size());

    // 4. Https ECV
    bomLb.addCiAttribute(
        "listeners", "['http 80 http 80','https 443 https 8443','https 8443 http 80']");
    bomLb.addCiAttribute("ecv_map", "{'80':'GET /health'}");

    fqdnExecutor.execute(wo, "/tmp");
    verify(mock, Mockito.atLeast(1)).create(argument.capture());
    hcList = argument.getValue().healthChecks();
    assertEquals(3, hcList.size());

    HealthCheck hc21 = newHealthCheck(HTTP, 80, "/health", false, 200);
    HealthCheck hc22 = newHealthCheck(HTTPS, 443, "", true, 200);
    HealthCheck hc23 = newHealthCheck(HTTPS, 8443, "/health", true, 200);
    assertTrue(hcList.containsAll(Arrays.asList(hc21, hc22, hc23)));

    // 5. SSL_BRIDGE and TCP
    bomLb.addCiAttribute(
        "listeners",
        "['ssl_bridge 443 ssl_bridge 443','ssl_bridge 1443 ssl_bridge 8443','https 8443 https 8443','tcp 8444 tcp 8444','tcp 8445 tcp 8444','tcp 8446 tcp 8446']");
    bomLb.addCiAttribute("ecv_map", "{'8443':'GET /health','8444':'GET /NOTUSED'}");

    fqdnExecutor.execute(wo, "/tmp");
    verify(mock, Mockito.atLeast(1)).create(argument.capture());
    hcList = argument.getValue().healthChecks();
    assertEquals(6, hcList.size());

    HealthCheck hc31 = newHealthCheck(TCP, 443, "", true, 0);
    HealthCheck hc32 = newHealthCheck(TCP, 1443, "", true, 0);
    HealthCheck hc33 = newHealthCheck(HTTPS, 8443, "/health", true, 200);
    HealthCheck hc34 = newHealthCheck(TCP, 8444, "", false, 0);
    HealthCheck hc35 = newHealthCheck(TCP, 8445, "", false, 0);
    HealthCheck hc36 = newHealthCheck(TCP, 8446, "", false, 0);
    assertTrue(hcList.containsAll(Arrays.asList(hc31, hc32, hc33, hc34, hc35, hc36)));

    // 6. TLS
    bomLb.addCiAttribute("listeners", "['tls 443 tls 443','tls 1443 tls 8443']");
    bomLb.addCiAttribute("ecv_map", "{'443':'GET /health','1443':'GET /NOTUSED'}");

    fqdnExecutor.execute(wo, "/tmp");
    verify(mock, Mockito.atLeast(1)).create(argument.capture());
    hcList = argument.getValue().healthChecks();
    assertEquals(2, hcList.size());

    HealthCheck hc41 = newHealthCheck(TCP, 443, "", true, 0);
    HealthCheck hc42 = newHealthCheck(TCP, 1443, "", true, 0);
    assertTrue(hcList.containsAll(Arrays.asList(hc41, hc42)));

    // 7. Invalid ECV
    bomLb.addCiAttribute("listeners", "['https 443 http 8080','http 80 http 8085']");
    bomLb.addCiAttribute("ecv_map", "{'8080':'/health','8085':'GET'}");

    fqdnExecutor.execute(wo, "/tmp");
    verify(mock, Mockito.atLeast(1)).create(argument.capture());
    hcList = argument.getValue().healthChecks();
    assertEquals(2, hcList.size());

    HealthCheck hc51 = newHealthCheck(HTTPS, 443, "", true, 200);
    HealthCheck hc52 = newHealthCheck(HTTP, 80, "", false, 200);
    assertTrue(hcList.containsAll(Arrays.asList(hc51, hc52)));
  }

  @Test
  public void shouldNotMatchIfLbIsMissing() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.payLoad.remove("DependsOn");
    Response response = fqdnExecutor.execute(wo, "/tmp");
    assertThat(response.getResult(), is(Result.NOT_MATCHED));
  }

  /** A helper method to create HealthCheck value class. */
  private HealthCheck newHealthCheck(
      Protocol proto, int port, String path, boolean tls, int status) {
    return HealthCheck.builder()
        .protocol(proto)
        .port(port)
        .path(path)
        .tls(tls)
        .expectedStatus(status)
        .build();
  }
}
