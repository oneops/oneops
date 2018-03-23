package com.oneops.crawler.plugins.hadr;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.google.gson.Gson;
import com.oneops.Cloud;
import com.oneops.Environment;
import com.oneops.Organization;
import com.oneops.Platform;
import com.oneops.crawler.SearchDal;




public class PlatformHADRCrawlerPluginTest {

  private final Logger log = LoggerFactory.getLogger(getClass());
  PlatformHADRCrawlerPlugin plugin;

  @BeforeMethod
  public void init() {
    System.setProperty("hadr.plugin.enabled", "true");
    System.setProperty("hadr.es.enabled", "true");
    System.setProperty("hadr.prod.datacenters.list", "dc1~dc2~dc3-dc4");
    System.setProperty("hadr.oo.baseurl", "https://oneops.prod.org.com");
  }

  @Test(enabled = true)
  public void test_ReadConfig() {
    plugin = new PlatformHADRCrawlerPlugin();

    assertEquals(plugin.isHadrPluginEnabled(), true);
    assertEquals(plugin.isHadrEsEnabled(), true);
    assertEquals(plugin.getProdDataCentersList(), "dc1~dc2~dc3-dc4");

  }

  @Test(enabled = true)
  private void test_IsDR() {
    plugin = new PlatformHADRCrawlerPlugin();
    Platform platform = new Platform();
    List<String> activeClouds = new ArrayList<String>();
    activeClouds.add("dc1-TestCloud1");
    activeClouds.add("dc1-TestCloud2");
    activeClouds.add("dc2-TestCloud1");
    activeClouds.add("dc2-TestCloud2");
    platform.setActiveClouds(activeClouds);
    assertEquals(plugin.IsDR(platform), "DR");

  }

  @Test(enabled = true)
  private void test_IsNonDR() {
    plugin = new PlatformHADRCrawlerPlugin();
    Platform platform = new Platform();
    List<String> activeClouds = new ArrayList<String>();
    activeClouds.add("dc1-TestCloud1");
    activeClouds.add("dc1-TestCloud2");
    platform.setActiveClouds(activeClouds);
    assertEquals(plugin.IsDR(platform), "Non-DR");

  }

  @Test(enabled = true)
  private void test_IsHA() {
    plugin = new PlatformHADRCrawlerPlugin();
    Platform platform = new Platform();
    List<String> activeClouds = new ArrayList<String>();
    activeClouds.add("dc1-TestCloud1");
    activeClouds.add("dc1-TestCloud2");

    platform.setActiveClouds(activeClouds);
    assertEquals(plugin.IsHA(platform), "HA");

  }

  @Test(enabled = true)
  private void test_IsNonHA() {
    plugin = new PlatformHADRCrawlerPlugin();
    Platform platform = new Platform();
    List<String> activeClouds = new ArrayList<String>();
    activeClouds.add("dc1-TestCloud1");
    platform.setActiveClouds(activeClouds);
    assertEquals(plugin.IsDR(platform), "Non-DR");

  }

  @Test(enabled = true)
  private void test_parseAssemblyNameFromNsPath() {
    plugin = new PlatformHADRCrawlerPlugin();
    String nsPath = "/orgname/assemblyname/platformname/bom/env-dev/1";
    assertEquals(plugin.parseAssemblyNameFromNsPath(nsPath), "assemblyname");

  }

  @Test(enabled = true)
  private void test_getOOURL() {
    System.setProperty("hadr.oo.baseurl", "https://oneops.prod.org.com");
    plugin = new PlatformHADRCrawlerPlugin();
    String nsPath = "/orgname/assemblyname/platformname/bom/env-dev/1";
    String expectedOOURLString =
        "https://oneops.prod.org.com/r/ns?path=/orgname/assemblyname/platformname/bom/env-dev/1";
    assertEquals(plugin.getOOURL(nsPath), expectedOOURLString);

  }

  @Test(enabled = true)
  private void test_getOOURL_DefaultToBlank() {
    System.clearProperty("hadr.oo.baseurl");
    plugin = new PlatformHADRCrawlerPlugin();
    String nsPath = "/orgname/assemblyname/platformname/bom/env-dev/1";
    String expectedOOURLString = "/r/ns?path=/orgname/assemblyname/platformname/bom/env-dev/1";
    assertEquals(plugin.getOOURL(nsPath), expectedOOURLString);

  }

  // TODO: utility to send test data Elastic Search directly, keep test case false while code checkin
  @Test(enabled = false)
  private void SaveToElasticSearch() {

    System.setProperty("hadr.es.enabled", "true");
    System.setProperty("es.host", "localhost");
    plugin = new PlatformHADRCrawlerPlugin();

    PlatformHADRRecord platformHADRRecord = new PlatformHADRRecord();
    platformHADRRecord.setTotalCores(4);
    platformHADRRecord.setTotalComputes(2);
    platformHADRRecord.setNsPath("Test-nsPath");
    platformHADRRecord.setPlatform("Test-platform");
    platformHADRRecord.setOoUrl("Test-ooUrl");
    platformHADRRecord.setAssembly("test-assembly");
    platformHADRRecord.setCreatedTS(new Date());
    platformHADRRecord.setEnv("Test-env");
    platformHADRRecord.setPack("Test-pack");
    platformHADRRecord.setOrg("test-org");
    platformHADRRecord.setPackVersion("Test-packVersion");
    platformHADRRecord.setIsDR("DR");
    platformHADRRecord.setSource("Test-source");
    platformHADRRecord.setSourcePack("Test-sourcePack");
    platformHADRRecord.setIsHA("HA");
    Gson gson = new Gson();
    String jsonInString = gson.toJson(platformHADRRecord);
    log.info("jsonInString: " + jsonInString);
    Organization organization = new Organization();
    organization.setFull_name("Test-full_name");
    organization.setOwner("Test-owner");
    organization.setDescription("Test-description");


    Map<String, String> tags =new HashMap<String, String>();
    tags.put("CCCID", "Test-cCCID2");
    tags.put("pillar", "test-pillar2");
    tags.put("VP", "test-vP2");
    tags.put("dept", "Test-dept");
    tags.put("costcenter", "test-costcenter2");
    tags.put("CTOdirect", "test-cTOdirect2");
    tags.put("CTO", "Test-cTO2");
    
    organization.setTags(tags);


    platformHADRRecord.setOrganization(organization);
    platformHADRRecord.setAutoReplaceEnabled(true);
    platformHADRRecord.setAutoRepairEnabled(false); 

    plugin.saveToElasticSearch(platformHADRRecord, "1");

  }
  
  @Test(enabled = true)
  private void testProcessPlatformForProdEnv() {

    SearchDal searchDal = mock(SearchDal.class);
    PlatformHADRCrawlerPlugin platformHADRCrawlerPlugin = new PlatformHADRCrawlerPlugin();
    
    // PlatformHADRCrawlerPlugin platformHADRCrawlerPlugin = mock(PlatformHADRCrawlerPlugin.class);
    
    platformHADRCrawlerPlugin.setSearchDal(searchDal);
    Platform platform = new Platform();
    platform.setId(111);
    platform.setPack("tomcat");
    platform.setPackVersion("1");
    platform.setSource("oneops");
    platform.setEnable("enabled");
    platform.setTotalComputes(4);
    platform.setTotalCores(4);

    List<String> activeClouds = new ArrayList<String>();
    activeClouds.add("dc1-TestCloud1");
    activeClouds.add("dc2-TestCloud1");
    
    platform.setActiveClouds(activeClouds);

    Map<String, Cloud> cloudsMap= new HashMap<String, Cloud>();
    Cloud cloud1 = new Cloud();
    cloud1.setId("dc1-TestCloud1");
    cloud1.setPriority(1);
    cloud1.setAdminstatus("active");
    cloud1.setDeploymentorder(1);
    cloud1.setScalepercentage(100);;
   
    Cloud cloud2 = new Cloud();
    cloud2.setId("dc2-TestCloud1");
    cloud2.setPriority(1);
    cloud2.setAdminstatus("active");
    cloud2.setDeploymentorder(1);
    cloud2.setScalepercentage(100);;
   
    
    cloudsMap.put("dc1-TestCloud1", cloud1);
    cloudsMap.put("dc2-TestCloud1", cloud1);
    
    List<Cloud> clouds= new ArrayList<Cloud>();
    clouds.add(cloud1);
    clouds.add(cloud2);
    platform.setClouds(clouds);
    platform.setAutoRepairEnabled(true);
    platform.setAutoReplaceEnabled(true);
    Environment env = new Environment();
   
    env.setClouds(cloudsMap);
    env.addPlatform(platform);
    
    Map<String, Organization> organizationsMapCache = new HashMap<String, Organization>();
    Organization organization = new Organization();
    organization.setFull_name("Test-full_name");
    organization.setOwner("Test-owner");
    organization.setDescription("Test-description");

    Map<String, String> tags =new HashMap<String, String>();
    tags.put("CCCID", "Test-cCCID2");
    tags.put("pillar", "test-pillar2");
    tags.put("VP", "test-vP2");
    tags.put("dept", "Test-dept");
    tags.put("costcenter", "test-costcenter2");
    tags.put("CTOdirect", "test-cTOdirect2");
    tags.put("CTO", "Test-cTO2");
    
    organization.setTags(tags);
    organizationsMapCache.put("Test-full_name", organization);
   
    platformHADRCrawlerPlugin.processPlatformForProdEnv(platform, env, organizationsMapCache);
    try {
      Mockito.verify(searchDal, Mockito.times(1)).put(eq(platformHADRCrawlerPlugin.getHadrElasticSearchIndexName()), eq("platform"), any(), eq("111"));
    } catch (Exception e) {
      fail();
    }
    
  }
  
  @Test(enabled = true)
  private void testHadrIndexNameAndMappings() {
 
  PlatformHADRCrawlerPlugin platformHADRCrawlerPlugin = new PlatformHADRCrawlerPlugin();
  assertEquals(platformHADRCrawlerPlugin.getHadrElasticSearchIndexName(), "hadr");  
  assertEquals(platformHADRCrawlerPlugin.getHadrElasticSearchIndexMappings(), "hadrIndexMappings.json");  
  
  }


}
