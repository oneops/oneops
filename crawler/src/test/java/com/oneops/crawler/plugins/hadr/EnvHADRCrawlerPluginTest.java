package com.oneops.crawler.plugins.hadr;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


import com.oneops.Platform;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvHADRCrawlerPluginTest {

	private final Logger log = LoggerFactory.getLogger(getClass());
	EnvHADRCrawlerPlugin plugin;
	Platform platform;
	
	@BeforeTest
	public void init() {
		System.setProperty("hadr.plugin.enabled", "true");
		System.setProperty("hadr.es.enabled", "true");
		System.setProperty("hadr.prod.datacenters.list", "dal~dfw~oo-test");
		
	}
	
	@BeforeMethod
	public void setup() {
		//platform= mock(Platform.class);
		
	}
	
	@Test
	public void envHADRCrawlerPluginTest_readConfigTest() {
		plugin = new EnvHADRCrawlerPlugin();
		plugin.readConfig();

		assertEquals( plugin.isHadrPluginEnabled, true);
		assertEquals(plugin.isHadrEsEnabled(), true);
		assertEquals(plugin.getProdDataCentersList(), "dal~dfw~oo-test");
		
	}
	
	@Test
	private void envHADRCrawlerPluginTest_IsPlatformDRCompliant_YES() {
		plugin = new EnvHADRCrawlerPlugin();
		Platform platform = new Platform();
		List<String> activeClouds= new ArrayList<String>();
		activeClouds.add("dal-TestCloud1");
		activeClouds.add("dal-TestCloud2");
		activeClouds.add("dfw-TestCloud1");
		activeClouds.add("dfw-TestCloud2");
		platform.setActiveClouds(activeClouds);
		assertEquals( plugin.IsPlatformDRCompliant(platform), true);
		
	}
	@Test
	private void envHADRCrawlerPluginTest_IsPlatformDRCompliant_NO() {
		plugin = new EnvHADRCrawlerPlugin();
		Platform platform = new Platform();
		List<String> activeClouds= new ArrayList<String>();
		activeClouds.add("dal-TestCloud1");
		activeClouds.add("dal-TestCloud2");
		platform.setActiveClouds(activeClouds);
		assertEquals( plugin.IsPlatformDRCompliant(platform), false);
		
		
	}

	@Test
	private void envHADRCrawlerPluginTest_IsPlatformHACompliant_YES() {
		plugin = new EnvHADRCrawlerPlugin();
		Platform platform = new Platform();
		List<String> activeClouds= new ArrayList<String>();
		activeClouds.add("dal-TestCloud1");
		activeClouds.add("dal-TestCloud2");
		activeClouds.add("dfw-TestCloud1");
		activeClouds.add("dfw-TestCloud2");
		platform.setActiveClouds(activeClouds);
		assertEquals( plugin.IsPlatformHACompliant(platform), true);
		
	}

	@Test
	private void envHADRCrawlerPluginTest_IsPlatformHACompliant_NO() {
		plugin = new EnvHADRCrawlerPlugin();
		Platform platform = new Platform();
		List<String> activeClouds= new ArrayList<String>();
		activeClouds.add("dal-TestCloud1");
		platform.setActiveClouds(activeClouds);
		assertEquals( plugin.IsPlatformDRCompliant(platform), false);
		
	}
}
