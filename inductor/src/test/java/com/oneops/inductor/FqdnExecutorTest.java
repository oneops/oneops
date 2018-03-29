package com.oneops.inductor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.oneops.cms.execution.Response;
import com.oneops.cms.execution.Result;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.gslb.GslbExecutor;
import com.oneops.gslb.Status;
import com.oneops.gslb.domain.Action;
import com.oneops.gslb.domain.Cloud;
import com.oneops.gslb.domain.GslbRequest;
import com.oneops.gslb.domain.GslbResponse;
import com.oneops.gslb.domain.InfobloxConfig;
import com.oneops.gslb.domain.TorbitConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class FqdnExecutorTest {

  FqdnExecutor fqdnExecutor = new FqdnExecutor();
  GslbExecutor mock = mock(GslbExecutor.class);

  @Before
  public void setup() {
    fqdnExecutor.gson = new Gson();
    when(mock.execute(any())).thenReturn(successResponse());
    fqdnExecutor.gslbExecutor = mock;
    WoHelper woHelper = new WoHelper();
    woHelper.gson = fqdnExecutor.gson;
    fqdnExecutor.woHelper = woHelper;
  }

  @Test
  public void addGslbRequestWithTwoClouds() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getRfcCi().setRfcAction("add");
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<GslbRequest> argument = ArgumentCaptor.forClass(GslbRequest.class);
    verify(mock).execute(argument.capture());

    GslbRequest request = argument.getValue();
    assertThat(request.action(), is(Action.add));
    assertThat("plt", is(request.platform()));
    assertThat("stg", is(request.environment()));
    assertThat("coll", is(request.assembly()));
    assertThat("org", is(request.org()));
    assertThat(request.platformEnabled(), is(true));
    assertThat(request.cloud().name(), is("cl1"));

    List<Cloud> platformClouds = request.platformClouds();
    assertThat(platformClouds.size(), is(2));
    assertThat(platformClouds.get(0).adminStatus4Platform(), is("active"));
    assertThat(platformClouds.get(0).priority(), is("1"));
    assertThat(platformClouds.get(1).adminStatus4Platform(), is("active"));
    assertThat(platformClouds.get(1).priority(), is("1"));

    InfobloxConfig ibConfig = request.cloud().infobloxConfig();
    assertThat(ibConfig.host(), is("https://localhost:8123"));
    assertThat(ibConfig.user(), is("test-usr"));
    assertThat(ibConfig.zone(), is("stg.cloud.xyz.com"));

    TorbitConfig torbitConfig = request.cloud().torbitConfig();
    assertThat(torbitConfig.url(), is("https://localhost:8443"));
    assertThat(torbitConfig.user(), is("test-oo"));
    assertThat(torbitConfig.groupId(), is(101));
    assertThat(torbitConfig.gslbBaseDomain(), is("xyz.com"));

    assertThat(request.fqdn().aliasesJson(), is("[p1]"));
    assertThat(request.fqdn().fullAliasesJson(), is("[p1.e1.a1.org.xyz.com]"));
    assertThat(request.lbConfig().listenerJson(), is("['http 80 http 80']"));
    assertThat(request.lbConfig().ecvMapJson(), is("{'80':'GET /'}"));
    assertThat(request.deployedLbs().size(), is(2));
    assertThat(request.deployedLbs().get(0).dnsRecord(), is("1.1.1.0"));
    assertThat(request.deployedLbs().get(1).dnsRecord(), is("1.1.1.1"));
  }

  @Test
  public void updateWithOnePrimaryOneSecondaryClouds() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    //make one cloud secondary
    wo.getPayLoadEntry("fqdnclouds").get(1).addCiAttribute("base.Consumes.priority", "2");
    wo.getRfcCi().setRfcAction("update");
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<GslbRequest> argument = ArgumentCaptor.forClass(GslbRequest.class);
    verify(mock).execute(argument.capture());

    GslbRequest request = argument.getValue();
    assertThat(request.action(), is(Action.update));
    List<Cloud> platformClouds = request.platformClouds();
    assertThat(platformClouds.size(), is(2));
    assertThat(platformClouds.get(0).adminStatus4Platform(), is("active"));
    assertThat(platformClouds.get(0).priority(), is("1"));
    assertThat(platformClouds.get(1).adminStatus4Platform(), is("active"));
    assertThat(platformClouds.get(1).priority(), is("2"));
    assertThat(request.deployedLbs().size(), is(2));
    assertThat(request.deployedLbs().get(0).dnsRecord(), is("1.1.1.0"));
    assertThat(request.deployedLbs().get(1).dnsRecord(), is("1.1.1.1"));
  }

  @Test
  public void deleteWithShutdownCloud() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    //make one cloud secondary
    wo.getPayLoadEntry("fqdnclouds").get(0).addCiAttribute("base.Consumes.adminstatus", "inactive");
    wo.getRfcCi().setRfcAction("delete");
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<GslbRequest> argument = ArgumentCaptor.forClass(GslbRequest.class);
    verify(mock).execute(argument.capture());

    GslbRequest request = argument.getValue();
    assertThat(request.action(), is(Action.delete));
    assertThat(request.platformEnabled(), is(true));
    List<Cloud> platformClouds = request.platformClouds();
    assertThat(platformClouds.size(), is(2));
    assertThat(platformClouds.get(0).adminStatus4Platform(), is("inactive"));
    assertThat(platformClouds.get(0).priority(), is("1"));
    assertThat(platformClouds.get(1).adminStatus4Platform(), is("active"));
    assertThat(platformClouds.get(1).priority(), is("1"));
    assertThat(request.deployedLbs().size(), is(2));
    assertThat(request.deployedLbs().get(0).dnsRecord(), is("1.1.1.0"));
    assertThat(request.deployedLbs().get(1).dnsRecord(), is("1.1.1.1"));
  }

  @Test
  public void deleteWithPlatformDisable() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    //make one cloud secondary
    wo.getBox().addCiAttribute("is_platform_enabled", "false");
    wo.getRfcCi().setRfcAction("delete");
    fqdnExecutor.execute(wo, "/tmp");
    ArgumentCaptor<GslbRequest> argument = ArgumentCaptor.forClass(GslbRequest.class);
    verify(mock).execute(argument.capture());

    GslbRequest request = argument.getValue();
    assertThat(request.action(), is(Action.delete));
    assertThat(request.platformEnabled(), is(false));
    List<Cloud> platformClouds = request.platformClouds();
    assertThat(platformClouds.size(), is(2));
    assertThat(platformClouds.get(0).adminStatus4Platform(), is("active"));
    assertThat(platformClouds.get(0).priority(), is("1"));
    assertThat(platformClouds.get(1).adminStatus4Platform(), is("active"));
    assertThat(platformClouds.get(1).priority(), is("1"));
    assertThat(request.deployedLbs().size(), is(2));
    assertThat(request.deployedLbs().get(0).dnsRecord(), is("1.1.1.0"));
    assertThat(request.deployedLbs().get(1).dnsRecord(), is("1.1.1.1"));
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
    ArgumentCaptor<GslbRequest> argument = ArgumentCaptor.forClass(GslbRequest.class);
    verify(mock).execute(argument.capture());

    GslbRequest request = argument.getValue();
    assertThat(request.action(), is(Action.replace));
    List<Cloud> platformClouds = request.platformClouds();
    assertThat(platformClouds.size(), is(2));
    assertThat(platformClouds.get(0).adminStatus4Platform(), is("active"));
    assertThat(platformClouds.get(0).priority(), is("1"));
    assertThat(platformClouds.get(1).adminStatus4Platform(), is("active"));
    assertThat(platformClouds.get(1).priority(), is("1"));
    assertThat(request.deployedLbs().size(), is(2));
    assertThat(request.deployedLbs().get(0).dnsRecord(), is("1.1.1.0"));
    assertThat(request.deployedLbs().get(1).dnsRecord(), is("1.1.1.1"));
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
    ArgumentCaptor<GslbRequest> argument = ArgumentCaptor.forClass(GslbRequest.class);
    verify(mock).execute(argument.capture());

    GslbRequest request = argument.getValue();
    assertThat(request.action(), is(Action.update));
    assertThat(request.fqdn().aliasesJson(), is("[p1]"));
    assertThat(request.fqdn().fullAliasesJson(), is("[p1.e1.a1.org.xyz.com]"));
    assertThat(request.oldFqdn().aliasesJson(), is("[olda1]"));
    assertThat(request.oldFqdn().fullAliasesJson(), is("[oldfa1.xyz.com]"));
  }

  @Test
  public void envWithCustomSubDomain() {
    CmsWorkOrderSimple wo = woWith2Clouds();
    wo.getPayLoadEntry("Environment").get(0).addCiAttribute("subdomain", "test1.e2.a1.o1");
    wo.getRfcCi().setRfcAction("update");
    fqdnExecutor.execute(wo, "/tmp");

    ArgumentCaptor<GslbRequest> argument = ArgumentCaptor.forClass(GslbRequest.class);
    verify(mock).execute(argument.capture());
    GslbRequest request = argument.getValue();
    assertThat(request.action(), is(Action.update));
    assertThat(request.customSubdomain(), is("test1.e2.a1.o1"));
  }

  private GslbResponse successResponse() {
    GslbResponse response = new GslbResponse();
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

  private void addCloudService(CmsWorkOrderSimple wo) {
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


}
