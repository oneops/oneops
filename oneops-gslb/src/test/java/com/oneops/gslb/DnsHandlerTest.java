package com.oneops.gslb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

public class DnsHandlerTest {

  DnsHandler dnsHandler = new DnsHandler();
  DnsMock dnsMock = new DnsMock();

  @Before
  public void setup() {
    dnsHandler.woHelper = new WoHelper();
    dnsHandler.jsonParser = new JsonParser();
    Gson gson = new Gson();
    dnsHandler.gson = gson;
    dnsHandler.woHelper.gson = gson;
    InfobloxClientProvider mock = mock(InfobloxClientProvider.class);
    dnsHandler.infobloxClientProvider = mock;
    when(mock.getInfobloxClient(any(), any(), any())).thenReturn(dnsMock);
  }

  @Test
  public void addCnames() {
    CmsWorkOrderSimple wo = wo("[test1]", "[Test1.xyz.com]", "10.1.1.10", "prod.xyz.com");
    Context context = context("Plt", ".Env.a1.org.gslb.xyz.com", "Env.a1.org", "c1");
    dnsHandler.setupDnsEntries(wo, context);
    Map<String, String> cnames = dnsMock.getNewCnames();
    assertEquals(cnames.size(), 3);
    String cname = "plt.env.a1.org.gslb.xyz.com";
    assertEquals(cname, cnames.get("plt.env.a1.org.prod.xyz.com"));
    assertEquals(cname, cnames.get("test1.env.a1.org.prod.xyz.com"));
    assertEquals(cname, cnames.get("test1.xyz.com"));
    assertEquals("10.1.1.10", dnsMock.getNewArecs().get("plt.env.a1.org.c1.prod.xyz.com"));
  }

  @Test
  public void modifyCnames() {
    CmsWorkOrderSimple wo = wo("[test2]", "[test2.xyz.com]", "10.1.1.20", "prod.xyz.com");
    wo.getRfcCi().addCiBaseAttribute("aliases", "[test1]");
    wo.getRfcCi().addCiBaseAttribute("full_aliases", "[test1.xyz.com]");
    Context context = context("plt", ".env.a1.org.gslb.xyz.com", "env.a1.org", "c2");
    dnsHandler.setupDnsEntries(wo, context);
    Map<String, String> cnames = dnsMock.getNewCnames();
    assertEquals(cnames.size(), 3);
    String cname = "plt.env.a1.org.gslb.xyz.com";
    assertEquals(cname, cnames.get("plt.env.a1.org.prod.xyz.com"));
    assertEquals(cname, cnames.get("test2.env.a1.org.prod.xyz.com"));
    assertEquals(cname, cnames.get("test2.xyz.com"));
    Set<String> delCnames = dnsMock.getDeleteCnames().stream().collect(Collectors.toSet());
    assertTrue(delCnames.contains("test1.env.a1.org.prod.xyz.com"));
    assertTrue(delCnames.contains("test1.xyz.com"));
    assertEquals("10.1.1.20", dnsMock.getNewArecs().get("plt.env.a1.org.c2.prod.xyz.com"));
  }

  @Test
  public void shutdownCloud() {
    CmsWorkOrderSimple wo = wo("[test3]", "[test3.xyz.com]", "10.1.1.30", "prod.xyz.com");
    wo.getRfcCi().setRfcAction("delete");
    Context context = context("plt", ".env.a1.org.gslb.xyz.com", "env.a1.org", "c2");
    dnsHandler.setupDnsEntries(wo, context);
    Map<String, String> cnames = dnsMock.getNewCnames();
    assertEquals(cnames.size(), 0);
    assertEquals(dnsMock.getDeleteCnames().size(), 0);
    List<String> arecs = dnsMock.getDeleteArecs();
    assertTrue(arecs.size() == 1 && "plt.env.a1.org.c2.prod.xyz.com".equals(arecs.get(0)));
  }

  @Test
  public void platformDisable() {
    CmsWorkOrderSimple wo = wo("[test4]", "[test4.xyz.com]", "10.1.1.40", "prod.xyz.com");
    wo.getRfcCi().setRfcAction("delete");
    Context context = context("Plt4", ".Env.a1.org.gslb.xyz.com", "Env.a1.org", "c2");
    context.setPlatformDisabled(true);
    dnsHandler.setupDnsEntries(wo, context);
    Map<String, String> cnames = dnsMock.getNewCnames();
    assertEquals(cnames.size(), 0);
    Set<String> delCnames = dnsMock.getDeleteCnames().stream().collect(Collectors.toSet());
    assertTrue(delCnames.contains("test4.env.a1.org.prod.xyz.com"));
    assertTrue(delCnames.contains("test4.xyz.com"));
    assertTrue(delCnames.contains("plt4.env.a1.org.prod.xyz.com"));
    List<String> arecs = dnsMock.getDeleteArecs();
    assertTrue(arecs.size() == 1 && "plt4.env.a1.org.c2.prod.xyz.com".equals(arecs.get(0)));
  }


  private Context context(String platform, String mtdBaseHost, String subDomian, String cloud) {
    Context context = new Context();
    context.setMtdBaseHost(mtdBaseHost);
    context.setPlatform(platform);
    context.setSubdomain(subDomian);
    context.setCloud(cloud);
    Map<String, String> map = new HashMap<>();
    context.setDnsAttrs(map);
    return context;
  }


  private CmsWorkOrderSimple wo(String shortAliases, String fullAliases, String lbVip, String zone) {
    CmsWorkOrderSimple wo = new CmsWorkOrderSimple();
    CmsRfcCISimple rfc = new CmsRfcCISimple();
    rfc.addCiAttribute("aliases", shortAliases);
    rfc.addCiAttribute("full_aliases", fullAliases);
    wo.setRfcCi(rfc);
    Map<String, CmsCISimple> dnsSevices = new HashMap<>();
    CmsCISimple service = new CmsCISimple();
    service.addCiAttribute("host", "localhost");
    service.addCiAttribute("username", "user1");
    service.addCiAttribute("password", "pwd");
    service.addCiAttribute("zone", zone);
    dnsSevices.put("c1", service);
    dnsSevices.put("c2", service);
    Map<String, Map<String, CmsCISimple>> allServices = new HashMap<>();
    allServices.put("dns", dnsSevices);
    wo.setServices(allServices);
    CmsRfcCISimple lb = new CmsRfcCISimple();
    lb.setCiClassName("bom.oneops.1.Lb");
    lb.addCiAttribute("dns_record", lbVip);
    wo.addPayLoadEntry("DependsOn", lb);
    return wo;
  }



}
