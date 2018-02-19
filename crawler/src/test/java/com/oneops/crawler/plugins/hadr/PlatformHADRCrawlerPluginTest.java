package com.oneops.crawler.plugins.hadr;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.oneops.Environment;
import com.oneops.Platform;

public class PlatformHADRCrawlerPluginTest {

	private final Logger log = LoggerFactory.getLogger(getClass());
	PlatformHADRCrawlerPlugin plugin;


	@BeforeTest
	public void init() {
		System.setProperty("hadr.plugin.enabled", "true");
		System.setProperty("hadr.es.enabled", "true");
		System.setProperty("hadr.prod.datacenters.list", "dal~dfw~oo-test");

	}

	@Test(enabled = false)
	public void PlatformHADRCrawlerPlugin_readConfigTest() {
		plugin = new PlatformHADRCrawlerPlugin();
		plugin.readConfig();

		assertEquals(plugin.isHadrPluginEnabled, true);
		assertEquals(plugin.isHadrEsEnabled(), true);
		assertEquals(plugin.getProdDataCentersList(), "dal~dfw~oo-test");

	}

	@Test(enabled = false)
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

	@Test(enabled = false)
	private void PlatformHADRCrawlerPlugin_IsPlatformDRCompliant_NO() {
		plugin = new PlatformHADRCrawlerPlugin();
		Platform platform = new Platform();
		List<String> activeClouds = new ArrayList<String>();
		activeClouds.add("dal-TestCloud1");
		activeClouds.add("dal-TestCloud2");
		platform.setActiveClouds(activeClouds);
		assertEquals(plugin.IsPlatformDRCompliant(platform), false);

	}

	@Test(enabled = false)
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

	@Test(enabled = false)
	private void PlatformHADRCrawlerPlugin_IsPlatformHACompliant_NO() {
		plugin = new PlatformHADRCrawlerPlugin();
		Platform platform = new Platform();
		List<String> activeClouds = new ArrayList<String>();
		activeClouds.add("dal-TestCloud1");
		platform.setActiveClouds(activeClouds);
		assertEquals(plugin.IsPlatformDRCompliant(platform), false);

	}

	@Test(enabled = false)
	private void PlatformHADRCrawlerPlugin_parseAssemblyNameFromNsPath() {
		plugin = new PlatformHADRCrawlerPlugin();
		String nsPath = "/tessrs/Palantir/palantir/bom/leviathan-dev/1";
		assertEquals(plugin.parseAssemblyNameFromNsPath(nsPath), "Palantir");

	}

	@Test(enabled = false)
	private void PlatformHADRCrawlerPlugin_getOOURL() {
		System.setProperty("hadr.oo.baseurl", "https://oneops.prod.walmart.com");
		plugin = new PlatformHADRCrawlerPlugin();
		String nsPath = "/tessrs/Palantir/palantir/bom/leviathan-dev/1";
		String expectedOOURLString = "https://oneops.prod.walmart.com/r/ns?path=/tessrs/Palantir/palantir/bom/leviathan-dev/1";
		assertEquals(plugin.getOOURL(nsPath), expectedOOURLString);

	}

	@Test(enabled = false)
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
	private void PlatformHADRCrawlerPlugin_processPlatformForProdEnv_isHadrEsDisabled() {
		try
		{
			
		System.setProperty("hadr.es.enabled", "false");
		System.setProperty("es.host", "http://localhosttestserver.server.com");
		plugin = new PlatformHADRCrawlerPlugin();
		
		Platform platform = mock(Platform.class);
		Environment env = mock(Environment.class);
		plugin.processPlatformForProdEnv(platform, env);

		}
		catch(Exception e) {
			log.error("exception while processing easltic search record",e);
			log.info("exception.getCause().getMessage(): "+e.getCause().getMessage());
		
			if(e.getCause() != null && e.getCause() instanceof UnknownHostException) {
				log.error("Elastic Search is disabled, plugin should not try connecting to Elastic Search Server");
			fail();	
			}
		} finally {
			System.clearProperty("es.host");
		}
	}
	
	@Test(enabled = true)
	private void PlatformHADRCrawlerPlugin_saveToElasticSearch() {
		
		System.setProperty("hadr.es.enabled", "true");
		System.setProperty("es.host", "localhost");
		plugin = new PlatformHADRCrawlerPlugin();
	
		PlatformHADRRecord platformHADRRecord=new PlatformHADRRecord();
		Platform platform = mock(Platform.class);
	
		plugin.saveToElasticSearch(platformHADRRecord,platform);
		
		

	
	}
}
