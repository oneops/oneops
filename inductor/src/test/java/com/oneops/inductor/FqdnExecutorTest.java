package com.oneops.inductor;

import static org.hamcrest.CoreMatchers.is;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class FqdnExecutorTest {

  FqdnExecutor fqdnExecutor = new FqdnExecutor();
  GslbProvider mock = mock(GslbProvider.class);

  @Before
  public void setup() {
    fqdnExecutor.gson = new Gson();
    when(mock.create(any())).thenReturn(successProvisionResponse());
    fqdnExecutor.gslbProvider = mock;
    WoHelper woHelper = new WoHelper();
    woHelper.gson = fqdnExecutor.gson;
    fqdnExecutor.jsonParser = new JsonParser();
    fqdnExecutor.woHelper = woHelper;
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
    assertThat(healthCheck.protocol(), is(Protocol.HTTP));
    assertThat(healthCheck.port(), is(80));
    assertThat(healthCheck.path(), is("/"));

    Set<String> cnames = request.cnames().stream().collect(Collectors.toSet());
    assertThat(cnames.size(), is(3));
    assertTrue(cnames.contains("plt.stg.coll.org.stg.cloud.xyz.com"));
    assertTrue(cnames.contains("p1.e1.a1.org.xyz.com"));
    assertTrue(cnames.contains("p1.stg.coll.org.stg.cloud.xyz.com"));

    List<CloudARecord> cloudARecords = request.cloudARecords();
    assertThat(cloudARecords.size(), is(1));
    CloudARecord cloudARecord = cloudARecords.get(0);
    assertThat(cloudARecord.cloud(), is("cl1"));
    assertThat(cloudARecord.aRecord(), is("plt.stg.coll.org.cl1.stg.cloud.xyz.com"));
  }

  @Test
  public void updateWithOnePrimaryOneSecondaryClouds() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    //make one cloud secondary
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
    //make one cloud secondary
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
    //make one cloud secondary
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

    Set<String> cnames = request.cnames().stream().collect(Collectors.toSet());
    System.out.println(cnames);
    assertThat(cnames.size(), is(3));
    assertTrue(cnames.contains("plt.stg.coll.org.stg.cloud.xyz.com"));
    assertTrue(cnames.contains("p1.e1.a1.org.xyz.com"));
    assertTrue(cnames.contains("p1.stg.coll.org.stg.cloud.xyz.com"));

    List<CloudARecord> cloudARecords = request.cloudARecords();
    assertThat(cloudARecords.size(), is(1));
    CloudARecord cloudARecord = cloudARecords.get(0);
    assertThat(cloudARecord.cloud(), is("cl1"));
    assertThat(cloudARecord.aRecord(), is("plt.stg.coll.org.cl1.stg.cloud.xyz.com"));
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
  public void failForMissingGdnsConfig() {
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
    Set<String> cnames = request.cnames().stream().collect(Collectors.toSet());
    assertThat(cnames.size(), is(3));
    assertTrue(cnames.contains("plt.stg.coll.org.stg.cloud.xyz.com"));
    assertTrue(cnames.contains("p1.e1.a1.org.xyz.com"));
    assertTrue(cnames.contains("p1.stg.coll.org.stg.cloud.xyz.com"));

    Set<String> obsoleteCnames = request.obsoleteCnames().stream().collect(Collectors.toSet());
    System.out.println(obsoleteCnames);
    assertThat(obsoleteCnames.size(), is(2));
    assertTrue(obsoleteCnames.contains("oldfa1.xyz.com"));
    assertTrue(obsoleteCnames.contains("olda1.stg.coll.org.stg.cloud.xyz.com"));

    List<CloudARecord> cloudARecords = request.cloudARecords();
    assertThat(cloudARecords.size(), is(1));
    CloudARecord cloudARecord = cloudARecords.get(0);
    assertThat(cloudARecord.cloud(), is("cl1"));
    assertThat(cloudARecord.aRecord(), is("plt.stg.coll.org.cl1.stg.cloud.xyz.com"));
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

  private GslbProvisionResponse successProvisionResponse() {
    GslbProvisionResponse response = new GslbProvisionResponse();
    response.setGlb("ad.stg.coll.xyz.com");
    response.setMtdDeploymentId("100");
    response.setMtdVersion("101");
    response.setMtdBaseId("50");
    response.setStatus(Status.SUCCESS);
    return response;
  }

  private CmsWorkOrderSimple woWith2Clouds() {
    CmsWorkOrderSimple wo = woBase();
    addLbPayload(wo);
    addCloudService(wo);
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

  private void addRealizedAs(CmsWorkOrderSimple wo) {
    CmsRfcCISimple manifest = new CmsRfcCISimple();
    manifest.setCiClassName("manifest.oneops.1.Fqdn");
    manifest.setCiId(110);
    manifest.addCiAttribute("distribution", "proximity");
    manifest.addCiAttribute("service_type", "torbit");
    wo.addPayLoadEntry("RealizedAs", manifest);
  }

  private void addLbPayload(CmsWorkOrderSimple wo) {
    CmsRfcCISimple lb1 = new CmsRfcCISimple();
    lb1.setCiName("lb-101-1");
    lb1.addCiAttribute("dns_record", "1.1.1.0");
    lb1.setCiClassName("bom.oneops.1.Lb");
    lb1.setCiId(650);

    CmsRfcCISimple lb2 = new CmsRfcCISimple();
    lb2.setCiName("lb-102-1");
    lb2.addCiAttribute("dns_record", "1.1.1.1");
    lb2.setCiClassName("bom.oneops.1.Lb");
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

    Map<String, CmsCISimple> dnsService = new HashMap<>();
    CmsCISimple dns = new CmsCISimple();
    dns.setCiClassName("cloud.service.Infoblox");
    dns.addCiAttribute("host", "https://localhost:8123");
    dns.addCiAttribute("username", "test-usr");
    dns.addCiAttribute("password", "pwd1");
    dns.addCiAttribute("zone", "stg.cloud.xyz.com");
    dnsService.put("cl1", dns);
    services.put("dns", dnsService);
  }

  private void addManagedVia(CmsWorkOrderSimple wo) {
    wo.addPayLoadEntry("ManagedVia", new CmsRfcCISimple());
  }


  @Test
  public void dontMatchAoWithDifferentServiceType() {
    CmsActionOrderSimple ao = ao();
    ao.getCi().getCiAttributes().put("service_type", "netscaler");
    Response response = fqdnExecutor.execute(ao, "/tmp");
    assertThat(response.getResult(), is(Result.NOT_MATCHED));
  }


  @Test
  public void failAoForMissingGdnsService() {
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
    assertThat(healthCheck.protocol(), is(Protocol.HTTP));
    assertThat(healthCheck.port(), is(80));
    assertThat(healthCheck.path(), is("/"));

    Set<String> cnames = request.cnames().stream().collect(Collectors.toSet());
    assertThat(cnames.size(), is(3));
    assertTrue(cnames.contains("plt.stg.coll.org.stg.cloud.xyz.com"));
    assertTrue(cnames.contains("p1.e1.a1.org.xyz.com"));
    assertTrue(cnames.contains("p1.stg.coll.org.stg.cloud.xyz.com"));

    List<CloudARecord> cloudARecords = request.cloudARecords();
    assertThat(cloudARecords.size(), is(1));
    CloudARecord cloudARecord = cloudARecords.get(0);
    assertThat(cloudARecord.cloud(), is("cl1"));
    assertThat(cloudARecord.aRecord(), is("plt.stg.coll.org.cl1.stg.cloud.xyz.com"));
  }

  private CmsActionOrderSimple ao() {
    CmsActionOrderSimple wo = aoBase();
    addLbPayload(wo);
    addCloudService(wo);
    addCloudsPayload(wo);
    addRealizedAs(wo);
    addDependsOn(wo);
    return wo;
  }

  private CmsActionOrderSimple aoBase() {
    CmsActionOrderSimple aoBase = new CmsActionOrderSimple();
    aoBase.setActionName("gslbstatus");
    CmsCISimple ci = new CmsCISimple();
    ci.setCiId(4001l);
    ci.setCiName("test-gslb");
    ci.setCiClassName("bom.oneops.1.Fqdn");
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

  private void addLbPayload(CmsActionOrderSimple wo) {
    CmsCISimple lb1 = new CmsCISimple();
    lb1.setCiName("lb-101-1");
    lb1.addCiAttribute("dns_record", "1.1.1.0");
    lb1.setCiClassName("bom.oneops.1.Lb");

    CmsCISimple lb2 = new CmsCISimple();
    lb2.setCiName("lb-102-1");
    lb2.addCiAttribute("dns_record", "1.1.1.1");
    lb2.setCiClassName("bom.oneops.1.Lb");

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

  private void addRealizedAs(CmsActionOrderSimple wo) {
    CmsCISimple manifest = new CmsCISimple();
    manifest.setCiClassName("manifest.oneops.1.Fqdn");
    manifest.setCiId(110);
    manifest.addCiAttribute("distribution", "proximity");
    manifest.addCiAttribute("service_type", "torbit");
    wo.addPayLoadEntry("RealizedAs", manifest);
  }

  private void addDependsOn(CmsActionOrderSimple wo) {
    CmsCISimple bomLb = new CmsCISimple();
    bomLb.setCiClassName("bom.oneops.1.Lb");
    bomLb.setCiId(650l);
    bomLb.addCiAttribute("listeners", "['http 80 http 80']");
    bomLb.addCiAttribute("ecv_map", "{'80':'GET /'}");
    wo.addPayLoadEntry("DependsOn", bomLb);
  }

}
