package com.oneops.crawler.plugins.hadr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oneops.Deployment;
import com.oneops.Environment;
import com.oneops.Platform;
import com.oneops.crawler.AbstractCrawlerPlugin;
import com.oneops.crawler.SearchDal;

public class EnvHADRCrawlerPlugin extends AbstractCrawlerPlugin {

	private final Logger log = LoggerFactory.getLogger(getClass());
	boolean isHadrPluginEnabled;
	boolean isHadrEsEnabled;
	String prodDataCentersList;
	String[] dataCentersArr;

	final String hadrElasticSearchIndexName = "hadr-test";// TODO: remove -test from the value after testing is complete
	private SearchDal searchDal;

	public EnvHADRCrawlerPlugin() {
		readConfig();

	}

	public void readConfig() {
		searchDal = new SearchDal();
		isHadrPluginEnabled = new Boolean(System.getProperty("hadr.plugin.enabled"));
		log.info("isHadrPluginEnabled: " + isHadrPluginEnabled);

		isHadrEsEnabled = new Boolean(System.getProperty("hadr.es.enabled"));
		log.info("isHadrEsEnabled: " + isHadrEsEnabled);

		prodDataCentersList = System.getProperty("hadr.prod.datacenters.list");
		log.info("production clouds list: [" + prodDataCentersList + "]");

		dataCentersArr = prodDataCentersList.split("~");
	}

	public void processEnvironment(Environment env, List<Deployment> deployments) {

		if (isHadrPluginEnabled) {
			processOnlyProdEnvs(env);
		} else {
			log.warn("HadrPlugin is not enabled ");
		}
	}

	public void processOnlyProdEnvs(Environment env) {

		String environmentProfileName = env.getProfile();
		if (environmentProfileName != null && environmentProfileName != ""
				&& environmentProfileName.toLowerCase().contains("dev")) {
			log.info("Eligible environment for processing envId: {}, profile: {}, envName: {}, envPath: {}",
					env.getId(), env.getProfile(), env.getName(), env.getPath());
			Collection<Platform> platforms = env.getPlatforms().values();
			for (Platform platform : platforms) {
				processPlatformForProdEnv(platform, env);
				log.info("Platform for environmentId: {} ,environmetProfile: {},  platformId: {}, platformName {}, platform.getEnable {}",
						env.getId(), env.getProfile(), platform.getId(), platform.getName(), platform.getEnable());
			}
		}
		else {
			log.info("Ignoring environment processing for envId: {}, envProfile: {}, envName: {}, envPath: {}",
					env.getId(), env.getProfile(), env.getName(), env.getPath());
		}
	}

	public void processPlatformForProdEnv(Platform platform, Environment env) {
		PlatformHADRRecord platformHADRRecord = new PlatformHADRRecord();
		platformHADRRecord.setIsDR(IsPlatformDRCompliant(platform));
		platformHADRRecord.setIsHA(IsPlatformHACompliant(platform));
		platformHADRRecord.setEnv(env.getName());

		if (isHadrEsEnabled) {
			log.info("Send compliance record to Elastic Search");
			saveToElasticSearch(platformHADRRecord, platform);
		}

	}

	public boolean IsPlatformDRCompliant(Platform platform) {

		String activeCloudsListForPlatform = platform.getActiveClouds().toString();
		log.info("activeCloudsListForPlatform: " + activeCloudsListForPlatform.toString());
		int i = 0;
		for (String dataCenter : dataCentersArr) {
			if (activeCloudsListForPlatform.contains(dataCenter)) {
				i++;
			}
		}
		if (i >= 2) {
			return true;
		}
		return false;

	}

	public boolean IsPlatformHACompliant(Platform platform) {

		if (platform.getActiveClouds().size() >= 2) {
			return true;
		}
		return false;

	}

	public void saveToElasticSearch(PlatformHADRRecord platformHADRRecord, Platform platform) {
		// TODO create a new ES Index named "hadr"
		// TODO create a new Type for index named "platform or hadrprodor hadr"

		log.info("data sent to elastic search");

		// searchDal.push("hadrElasticSearchIndexName", "platform", platformHADRRecord,
		// String.valueOf(platform.getId()));

	}

	public boolean isHadrPluginEnabled() {
		return isHadrPluginEnabled;
	}

	public boolean isHadrEsEnabled() {
		return isHadrEsEnabled;
	}

	public String getProdDataCentersList() {
		return prodDataCentersList;
	}

	public String getHadrElasticSearchIndexName() {
		return hadrElasticSearchIndexName;
	}

	public String[] getDataCentersArr() {
		return dataCentersArr;
	}
}
