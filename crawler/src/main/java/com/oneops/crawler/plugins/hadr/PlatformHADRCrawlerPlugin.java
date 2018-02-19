package com.oneops.crawler.plugins.hadr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oneops.Deployment;
import com.oneops.Environment;
import com.oneops.Platform;
import com.oneops.crawler.AbstractCrawlerPlugin;
import com.oneops.crawler.SearchDal;


public class PlatformHADRCrawlerPlugin extends AbstractCrawlerPlugin {

	private final Logger log = LoggerFactory.getLogger(getClass());
	boolean isHadrPluginEnabled;
	boolean isHadrEsEnabled;
	String prodDataCentersList;
	String[] dataCentersArr;
	String oo_baseUrl;
	final String hadrElasticSearchIndexName = "hadr_test";// TODO: remove -test from the value after testing is complete
	private SearchDal searchDal;
	private int index_number_of_shards;
	private int index_number_of_replicas;
	

	public SearchDal getSearchDal() {
		return searchDal;
	}

	public void setSearchDal(SearchDal searchDal) {
		this.searchDal = searchDal;
	}

	public PlatformHADRCrawlerPlugin() {
		readConfig();

	}

	public void readConfig() {
		setSearchDal( new SearchDal());
		isHadrPluginEnabled = new Boolean(System.getProperty("hadr.plugin.enabled"));
		log.info("isHadrPluginEnabled: " + isHadrPluginEnabled);

		isHadrEsEnabled = new Boolean(System.getProperty("hadr.es.enabled"));
		log.info("isHadrEsEnabled: " + isHadrEsEnabled);

		prodDataCentersList = System.getProperty("hadr.prod.datacenters.list");
		log.info("production clouds list: [" + prodDataCentersList + "]");

		dataCentersArr = prodDataCentersList.split("~");
		
		oo_baseUrl= System.getProperty("hadr.oo.baseurl", "");
		log.info("oo_baseUrl: "+oo_baseUrl);
		
		
		index_number_of_shards=new Integer(System.getProperty("hadr.index.number_of_shards", "1")); //TODO: move these properties to SearchDal
		index_number_of_replicas=new Integer(System.getProperty("hadr.index.number_of_replicas", "1"));//TODO: move these properties to SearchDal
		if (isHadrEsEnabled) {
			createIndexInElasticSearch();
		}
		
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
		//TODO: replace "dev" key word with prod
		if (environmentProfileName != null && environmentProfileName != ""
				&& environmentProfileName.toLowerCase().contains("dev")) {
			log.info("Eligible environment for processing envId: {}, profile: {}, envName: {}, envPath: {}",
					env.getId(), env.getProfile(), env.getName(), env.getPath());
			Collection<Platform> platforms = env.getPlatforms().values();
			for (Platform platform : platforms) {
				log.info("Platform for environmentId: {} ,environmetProfile: {},  platformId: {}, platformName {}, platform.getEnable {}",
						env.getId(), env.getProfile(), platform.getId(), platform.getName(), platform.getEnable());
				
				processPlatformForProdEnv(platform, env);
			
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
		platformHADRRecord.setTotal(0); //TODO: check with team what is the significance of this field
		platformHADRRecord.setPack(platform.getPack());
		platformHADRRecord.setPackVersion(platform.getPackVersion());
		platformHADRRecord.setSource(platform.getSource());
		platformHADRRecord.setPlat(platform.getName());// TODO: need to check if we need plat since platform holds same value
		platformHADRRecord.setPlatform(platform.getName()); 
		platformHADRRecord.setAssembly(parseAssemblyNameFromNsPath(platform.getPath())); // org/assembly/env/platform   get assembly from 
		platformHADRRecord.setOoUrl(getOOURL(platform.getPath()));
		platformHADRRecord.setSourcePack(platform.getSource()+"-"+platform.getPack());
		platformHADRRecord.setCreatedTS(new Date());
		
		
		
		//kloopzcm.cm_ci_attributes table
		
		//organization[ci][ciAttributes][tags]
		// /oneops/lookup/ci_lookup this should give assembly details
		/*
		// get platform : ciId, "manifest.ComposedOf",null, "manifest.Platform"
		 * 
		 *
		 	     
		     platformID is connected to clouds via base.consumes relations
		     
		     
		         "primaryClouds": [
		      "prod-dal4"
		    ],
		      "secondaryClouds": [], GET them from relations
		      
		      
		    "ctoOrg": "Clay Johnson",
		    "ctoDirect": "Kerry Kilker",
		    "vp": "Jerry Geisler III",
		    "org": "tessrs",
		
			"pClouds": "prod-dal4", // remove them but take a note
		    "sClouds": "",// remove them but take a note
		    "envsInAssembly": 1, // Note it down and skip it.
		    "total": 0, // number of computes
		    "cCount": {
		      "prod-dal4": 1
		    }
		   
		  }
 */
		if (isHadrEsEnabled) {
			log.info("Sending compliance record to Elastic Search");
			saveToElasticSearch(platformHADRRecord, String.valueOf(platform.getId()));
		}

	}

	public String getOOURL(String path) {
		return oo_baseUrl + "/r/ns?path=" + path;
	}

	public String parseAssemblyNameFromNsPath(String path) {
		
		if (path!=null && !path.isEmpty()) {
			String[] parsedArray=path.split("/");
			return parsedArray[2];
		} else {
			return "";
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

	public void saveToElasticSearch(PlatformHADRRecord platformHADRRecord, String platformCId) {

		log.info("Sending data record to elastic search");
		searchDal.push(this.hadrElasticSearchIndexName, "platform", platformHADRRecord, platformCId);
		log.info("Sent data record to elastic search");

	}


	public void createIndexInElasticSearch() {
		
		 searchDal.createIndex(this.hadrElasticSearchIndexName, gethadrIndexMappigs());
		  
		
	}


	public String gethadrIndexMappigs()  {
		String fileAsString = new String();
		try {
			
		
		InputStream is = ClassLoader.getSystemResourceAsStream("hadrIndexMappings.json");
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		String line = buf.readLine();
		StringBuilder sb = new StringBuilder();
		while (line != null) {
			sb.append(line).append("\n");
			line = buf.readLine();
		}
		fileAsString = sb.toString();
		log.info("Contents : " + fileAsString);
		buf.close();
		} catch(Exception e) {
			log.error("Error while reading <hadrIndexMappings.json> file from class path: ", e);
			
		}
		return fileAsString;
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
