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

/*import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;*/

public class PlatformHADRCrawlerPluginTest {

	private final Logger log = LoggerFactory.getLogger(getClass());
	PlatformHADRCrawlerPlugin plugin;

	@BeforeMethod
	public void init() {
		System.setProperty("hadr.plugin.enabled", "true");
		System.setProperty("hadr.es.enabled", "true");
		System.setProperty("hadr.prod.datacenters.list", "dal~dfw~oo-test");

	}

	@Test(enabled = true)
	public void PlatformHADRCrawlerPlugin_readConfigTest() {
		plugin = new PlatformHADRCrawlerPlugin();
	

		assertEquals(plugin.isHadrPluginEnabled, true);
		assertEquals(plugin.isHadrEsEnabled(), true);
		assertEquals(plugin.getProdDataCentersList(), "dal~dfw~oo-test");

	}

	@Test(enabled = true)
	private void PlatformHADRCrawlerPlugin_IsPlatformDRCompliant_YES() {
		plugin = new PlatformHADRCrawlerPlugin();
		Platform platform = new Platform();
		List<String> activeClouds = new ArrayList<String>();
		activeClouds.add("dal-TestCloud1");
		activeClouds.add("dal-TestCloud2");
		activeClouds.add("dfw-TestCloud1");
		activeClouds.add("dfw-TestCloud2");
		platform.setActiveClouds(activeClouds);
		assertEquals(plugin.IsPlatformDRCompliant(platform), true);

	}

	@Test(enabled = true)
	private void PlatformHADRCrawlerPlugin_IsPlatformDRCompliant_NO() {
		plugin = new PlatformHADRCrawlerPlugin();
		Platform platform = new Platform();
		List<String> activeClouds = new ArrayList<String>();
		activeClouds.add("dal-TestCloud1");
		activeClouds.add("dal-TestCloud2");
		platform.setActiveClouds(activeClouds);
		assertEquals(plugin.IsPlatformDRCompliant(platform), false);

	}

	@Test(enabled = true)
	private void PlatformHADRCrawlerPlugin_IsPlatformHACompliant_YES() {
		plugin = new PlatformHADRCrawlerPlugin();
		Platform platform = new Platform();
		List<String> activeClouds = new ArrayList<String>();
		activeClouds.add("dal-TestCloud1");
		activeClouds.add("dal-TestCloud2");
		activeClouds.add("dfw-TestCloud1");
		activeClouds.add("dfw-TestCloud2");
		platform.setActiveClouds(activeClouds);
		assertEquals(plugin.IsPlatformHACompliant(platform), true);

	}

	@Test(enabled = true)
	private void PlatformHADRCrawlerPlugin_IsPlatformHACompliant_NO() {
		plugin = new PlatformHADRCrawlerPlugin();
		Platform platform = new Platform();
		List<String> activeClouds = new ArrayList<String>();
		activeClouds.add("dal-TestCloud1");
		platform.setActiveClouds(activeClouds);
		assertEquals(plugin.IsPlatformDRCompliant(platform), false);

	}

	@Test(enabled = true)
	private void PlatformHADRCrawlerPlugin_parseAssemblyNameFromNsPath() {
		plugin = new PlatformHADRCrawlerPlugin();
		String nsPath = "/tessrs/Palantir/palantir/bom/leviathan-dev/1";
		assertEquals(plugin.parseAssemblyNameFromNsPath(nsPath), "Palantir");

	}

	@Test(enabled = true)
	private void PlatformHADRCrawlerPlugin_getOOURL() {
		System.setProperty("hadr.oo.baseurl", "https://oneops.prod.walmart.com");
		plugin = new PlatformHADRCrawlerPlugin();
		String nsPath = "/tessrs/Palantir/palantir/bom/leviathan-dev/1";
		String expectedOOURLString = "https://oneops.prod.walmart.com/r/ns?path=/tessrs/Palantir/palantir/bom/leviathan-dev/1";
		assertEquals(plugin.getOOURL(nsPath), expectedOOURLString);

	}

	@Test(enabled = true)
	private void PlatformHADRCrawlerPlugin_getOOURL_DefaultToBlank() {
		System.clearProperty("hadr.oo.baseurl");
		plugin = new PlatformHADRCrawlerPlugin();
		String nsPath = "/tessrs/Palantir/palantir/bom/leviathan-dev/1";
		String expectedOOURLString = "/r/ns?path=/tessrs/Palantir/palantir/bom/leviathan-dev/1";
		assertEquals(plugin.getOOURL(nsPath), expectedOOURLString);

		// setting property back to original value for other test cases
		System.setProperty("hadr.oo.baseurl", "https://oneops.prod.walmart.com");

	}

	
	@Test(enabled = false)
	private void PlatformHADRCrawlerPlugin_SaveToElasticSearch() {
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
		platformHADRRecord.setSClouds("test-sClouds");
		platformHADRRecord.setCreatedTS(new Date());
		platformHADRRecord.setEnv("Test-env");
		platformHADRRecord.setPack("Test-pack");
		platformHADRRecord.setOrg("test-org");
		platformHADRRecord.setPackVersion("Test-packVersion");
		platformHADRRecord.setIsDR(true);
		platformHADRRecord.setSource("Test-source");
		platformHADRRecord.setSourcePack("Test-sourcePack");
		platformHADRRecord.setIsHA(false);
		Gson gson = new Gson();
		String jsonInString = gson.toJson(platformHADRRecord);
		log.info("jsonInString: " + jsonInString);

		plugin.saveToElasticSearch(platformHADRRecord, "1");

	}

}
