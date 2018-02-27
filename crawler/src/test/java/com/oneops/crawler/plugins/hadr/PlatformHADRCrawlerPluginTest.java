package com.oneops.crawler.plugins.hadr;

import static org.testng.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.google.gson.Gson;
import com.oneops.Platform;
import com.oneops.crawler.plugins.hadr.PlatformHADRCrawlerPlugin;

public class PlatformHADRCrawlerPluginTest {

  private final Logger log = LoggerFactory.getLogger(getClass());
  PlatformHADRCrawlerPlugin plugin;

  @BeforeMethod
  public void init() {
    System.setProperty("hadr.plugin.enabled", "true");
    System.setProperty("hadr.es.enabled", "true");
    System.setProperty("hadr.prod.datacenters.list", "dal~dfw~oo-test");
    System.setProperty("hadr.oo.baseurl", "https://oneops.prod.walmart.com");
  }

  @Test(enabled = true)
  public void testPlatformHADRCrawlerPlugin_readConfig() {
    plugin = new PlatformHADRCrawlerPlugin();

    assertEquals(plugin.isHadrPluginEnabled(), true);
    assertEquals(plugin.isHadrEsEnabled(), true);
    assertEquals(plugin.getProdDataCentersList(), "dal~dfw~oo-test");

  }

  @Test(enabled = true)
  private void testPlatformHADRCrawlerPlugin_IsPlatformDRCompliant_YES() {
    plugin = new PlatformHADRCrawlerPlugin();
    Platform platform = new Platform();
    List<String> activeClouds = new ArrayList<String>();
    activeClouds.add("dal-TestCloud1");
    activeClouds.add("dal-TestCloud2");
    activeClouds.add("dfw-TestCloud1");
    activeClouds.add("dfw-TestCloud2");
    platform.setActiveClouds(activeClouds);
    assertEquals(plugin.IsPlatformDRCompliant(platform), "DR");

  }

  @Test(enabled = true)
  private void testPlatformHADRCrawlerPlugin_IsPlatformDRCompliant_NO() {
    plugin = new PlatformHADRCrawlerPlugin();
    Platform platform = new Platform();
    List<String> activeClouds = new ArrayList<String>();
    activeClouds.add("dal-TestCloud1");
    activeClouds.add("dal-TestCloud2");
    platform.setActiveClouds(activeClouds);
    assertEquals(plugin.IsPlatformDRCompliant(platform), "Non-DR");

  }

  @Test(enabled = true)
  private void testPlatformHADRCrawlerPlugin_IsPlatformHACompliant_YES() {
    plugin = new PlatformHADRCrawlerPlugin();
    Platform platform = new Platform();
    List<String> activeClouds = new ArrayList<String>();
    activeClouds.add("dal-TestCloud1");
    activeClouds.add("dal-TestCloud2");

    platform.setActiveClouds(activeClouds);
    assertEquals(plugin.IsPlatformHACompliant(platform), "HA");

  }

  @Test(enabled = true)
  private void testPlatformHADRCrawlerPlugin_IsPlatformHACompliant_NO() {
    plugin = new PlatformHADRCrawlerPlugin();
    Platform platform = new Platform();
    List<String> activeClouds = new ArrayList<String>();
    activeClouds.add("dal-TestCloud1");
    platform.setActiveClouds(activeClouds);
    assertEquals(plugin.IsPlatformDRCompliant(platform), "Non-DR");

  }

  @Test(enabled = true)
  private void testPlatformHADRCrawlerPlugin_parseAssemblyNameFromNsPath() {
    plugin = new PlatformHADRCrawlerPlugin();
    String nsPath = "/tessrs/Palantir/palantir/bom/leviathan-dev/1";
    assertEquals(plugin.parseAssemblyNameFromNsPath(nsPath), "Palantir");

  }

  @Test(enabled = true)
  private void testPlatformHADRCrawlerPlugin_getOOURL() {
    System.setProperty("hadr.oo.baseurl", "https://oneops.prod.walmart.com");
    plugin = new PlatformHADRCrawlerPlugin();
    String nsPath = "/tessrs/Palantir/palantir/bom/leviathan-dev/1";
    String expectedOOURLString =
        "https://oneops.prod.walmart.com/r/ns?path=/tessrs/Palantir/palantir/bom/leviathan-dev/1";
    assertEquals(plugin.getOOURL(nsPath), expectedOOURLString);

  }

  @Test(enabled = true)
  private void testPlatformHADRCrawlerPlugin_getOOURL_DefaultToBlank() {
    System.clearProperty("hadr.oo.baseurl");
    plugin = new PlatformHADRCrawlerPlugin();
    String nsPath = "/tessrs/Palantir/palantir/bom/leviathan-dev/1";
    String expectedOOURLString = "/r/ns?path=/tessrs/Palantir/palantir/bom/leviathan-dev/1";
    assertEquals(plugin.getOOURL(nsPath), expectedOOURLString);

  }

  // TODO: Pending for converting Elastic search calls to a mock object
  @Test(enabled = false)
  private void testPlatformHADRCrawlerPlugin_SaveToElasticSearch() {
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

    plugin.saveToElasticSearch(platformHADRRecord, "1");

  }

}
