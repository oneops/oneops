package com.oneops.crawler.plugins.hadr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.oneops.Cloud;
import com.oneops.Environment;
import com.oneops.Organization;
import com.oneops.Platform;
import com.oneops.crawler.AbstractCrawlerPlugin;
import com.oneops.crawler.CommonsUtil;
import com.oneops.crawler.SearchDal;

public class PlatformHADRCrawlerPlugin extends AbstractCrawlerPlugin {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private boolean isHadrPluginEnabled;
  private boolean isHadrEsEnabled;
  private String prodDataCentersList;
  private String[] dataCentersArr;
  private String oo_baseUrl;
  final String hadrElasticSearchIndexName = "hadr_test"; 
  final String hadrElasticSearchIndexMappings = "hadrIndexMappings.json"; 
  private SearchDal searchDal;
  private String environmentProdProfileFilter;

  private String[] produtionCloudsArr; 
  private String stageCloudFilter;
  final private String isHALabel="isHA";
  final private String isDRLabel="isDR";
  final private String isAutoRepairEnabledLabel="isAutoRepairEnabled";
  final private String isAutoReplaceEnabledLabel="isAutoReplaceEnabled";
  final private String isProdCloudInNonProdEnvLabel="isProdCloudInNonProdEnv";
  final  private String prodProfileWithStageCloudsLabel="prodProfileWithStageClouds";
  
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
    setSearchDal(new SearchDal());
    isHadrPluginEnabled = new Boolean(System.getProperty("hadr.plugin.enabled"));
    log.info("isHadrPluginEnabled: " + isHadrPluginEnabled);

    isHadrEsEnabled = new Boolean(System.getProperty("hadr.es.enabled"));
    log.info("isHadrEsEnabled: " + isHadrEsEnabled);

    prodDataCentersList = System.getProperty("hadr.prod.datacenters.list");
    log.info("production clouds list: [" + prodDataCentersList + "]");

    dataCentersArr = prodDataCentersList.split("~");

    oo_baseUrl = System.getProperty("hadr.oo.baseurl", "");
    log.info("oo_baseUrl: " + oo_baseUrl);

    environmentProdProfileFilter = System.getProperty("hadr.env.profile.regex", "prod").toLowerCase();
    log.info("environmentProdProfileFilter: " + environmentProdProfileFilter);

    produtionCloudsArr = System.getProperty("produtionCloudsList").toLowerCase().split("~");
    
    stageCloudFilter=System.getProperty("hadr.stageCloudFilter.regex", "stg").toLowerCase();
    log.info("stageCloudFilter: " + stageCloudFilter);
    
    if (isHadrEsEnabled) {
      try {
        createIndexInElasticSearch();
      } catch (IOException e) {
        throw new RuntimeException("Could not create ES index for HADR plugin", e);
      }
    }

  }

  public void processEnvironment(Environment env, Map<String, Organization> organizationsMapCache) {

    if (isHadrPluginEnabled) {

      processPlatformsInEnv(env, organizationsMapCache);
    } else {
      log.warn("HadrPlugin is not enabled ");
    }
  }


  public void processPlatformsInEnv(Environment env,
      Map<String, Organization> organizationsMapCache) {

    log.info("Starting processing for environment envId: {}, profile: {}, envName: {}, envPath: {}",
        env.getId(), env.getProfile(), env.getName(), env.getPath());
    Collection<Platform> platforms = env.getPlatforms().values();

    for (Platform platform : platforms) {

      PlatformHADRRecord platformHADRRecord = new PlatformHADRRecord();

      platformHADRRecord.setEnv(env.getName());
      platformHADRRecord.setPack(platform.getPack());
      platformHADRRecord.setPackVersion(platform.getPackVersion());
      platformHADRRecord.setSource(platform.getSource());
      platformHADRRecord.setTotalCores(platform.getTotalCores());
      platformHADRRecord.setTotalComputes(platform.getTotalComputes());
      platformHADRRecord.setPlatform(platform.getName());
      platformHADRRecord.setAssembly(parseAssemblyNameFromNsPath(platform.getPath()));
      platformHADRRecord.setOoUrl(getOOURL(platform.getPath()));
      platformHADRRecord.setCreatedTS(new Date());
      platformHADRRecord.setCloudsMap(platform.getCloudsMap());
      platformHADRRecord.setSourcePack(platform.getSource() + "-" + platform.getPack());
      platformHADRRecord.setNsPath(platform.getPath());
      platformHADRRecord.setClouds(platform.getClouds());
      // TODO: write a utility to transform cloud data
      platformHADRRecord =
          setCloudCategories(platformHADRRecord, platformHADRRecord.getCloudsMap());
      String orginzationName = CommonsUtil.parseOrganizationNameFromNsPath(platform.getPath());
      platformHADRRecord.setOrg(orginzationName);
      platformHADRRecord.setOrganization(organizationsMapCache.get(orginzationName));
      platformHADRRecord.setEnvProfile(env.getProfile());
      platformHADRRecord.setTechDebt(getPlatformTechDebtForEnvironment(platform, env));


      if (isHadrEsEnabled) {
        log.info("Sending compliance record to Elastic Search");
        saveToElasticSearch(platformHADRRecord, String.valueOf(platform.getId()));

      }
    }

  }

  public String getOOURL(String path) {
    return oo_baseUrl + "/r/ns?path=" + path;
  }

  public String parseAssemblyNameFromNsPath(String path) {

    if (path != null && !path.isEmpty()) {
      String[] parsedArray = path.split("/");
      return parsedArray[2];
    } else {
      return "";
    }
  }

  public String IsDR(Platform platform) {

    String activeCloudsListForPlatform = platform.getActiveClouds().toString().toLowerCase();
    log.info("activeCloudsListForPlatform: " + activeCloudsListForPlatform.toString());
    int i = 0;
    for (String dataCenter : dataCentersArr) {
      if (activeCloudsListForPlatform.contains(dataCenter)) {
        i++;
      }
    }
    if (i >= 2) {
      return "DR";
    }
    return "Non-DR";

  }

  public String IsHA(Platform platform) {

    if (platform.getActiveClouds().size() >= 2) {
      return "HA";
    }
    return "Non-HA";

  }

  public void saveToElasticSearch(PlatformHADRRecord platformHADRRecord, String platformCId) {

    log.info("Sending data record to elastic search");
    try {
      searchDal.put(this.hadrElasticSearchIndexName, "platform", platformHADRRecord, platformCId);
    } catch (IOException e) {
      log.error("Error saving hadr record to ES ", e);
      return;
    }
    log.info("Sent data record to elastic search");
    log.debug("JsonfiedString: " + new Gson().toJson(platformHADRRecord));

  }

  public void createIndexInElasticSearch() throws IOException {

    searchDal.createIndex(this.hadrElasticSearchIndexName, CommonsUtil.getFileContent(hadrElasticSearchIndexMappings));

  }

  public PlatformHADRRecord setCloudCategories(PlatformHADRRecord platformHADRRecord,
      Map<String, Cloud> clouds) {

    List<String> activeClouds = new ArrayList<String>();
    List<String> primaryClouds = new ArrayList<String>();
    List<String> secondaryClouds = new ArrayList<String>();
    List<String> offlineClouds = new ArrayList<String>();

    for (String cloudName : clouds.keySet()) {
      Cloud cloud = clouds.get(cloudName);

      try {
      switch (cloud.getAdminstatus()) {

        case "active":
          activeClouds.add(cloudName);
          break;

        case "offline":
          offlineClouds.add(cloudName);
          break;

      }
      switch (cloud.getPriority()) {

        case 1:
          primaryClouds.add(cloudName);
          break;
        case 2:
          secondaryClouds.add(cloudName);
          break;

      }

    } catch (Exception e) {
        log.error("Error while setting cloud categories for cloud {} :",cloudName,e);
        
      }
    }
    platformHADRRecord.setActiveClouds(activeClouds);
    platformHADRRecord.setPrimaryClouds(primaryClouds);
    platformHADRRecord.setSecondaryClouds(secondaryClouds);
    platformHADRRecord.setOfflineClouds(offlineClouds);
    

    return platformHADRRecord;

  }

  public Map<String, Object> getPlatformTechDebtForEnvironment(Platform platform, Environment env) {
    Map<String, Object> techDebt = new HashMap<String, Object>();

    String environmentProfileName = env.getProfile();

    if (environmentProfileName != null && environmentProfileName != ""
        && environmentProfileName.toLowerCase().contains(environmentProdProfileFilter)) {
      techDebt.put(isDRLabel, IsDR(platform));
      techDebt.put(isHALabel, IsHA(platform));
      if (prodProfileWithStageClouds(platform.getCloudsMap())) {
        techDebt.put(prodProfileWithStageCloudsLabel, true);
      }

    } else {
      techDebt.put(this.isProdCloudInNonProdEnvLabel,
          isNonProdEnvUsingProdutionClouds(platform.getCloudsMap()));

    }
    // tech debt irrespective of environment
    techDebt.put(isAutoRepairEnabledLabel, platform.isAutoRepairEnabled());
    techDebt.put(isAutoReplaceEnabledLabel, platform.isAutoReplaceEnabled());


    return techDebt;

  }

  public boolean isNonProdEnvUsingProdutionClouds(Map<String, Cloud> cloudsMap) {

    for (String prodCloudName : produtionCloudsArr) {
      return cloudsMap.keySet().toString().toLowerCase().contains(prodCloudName);
    }

    return false;

  }

  public boolean prodProfileWithStageClouds(Map<String, Cloud> cloudsMap) {

    if (cloudsMap.keySet().toString().toLowerCase().contains(stageCloudFilter)) {
      return true;
    }

    return false;

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

  public void setHadrEsEnabled(boolean isHadrEsEnabled) {
    this.isHadrEsEnabled = isHadrEsEnabled;
  }

  public String getHadrElasticSearchIndexMappings() {
    return hadrElasticSearchIndexMappings;
  }

  public String getIsHALabel() {
    return isHALabel;
  }

  public String getIsDRLabel() {
    return isDRLabel;
  }

  public String getIsAutoRepairEnabledLabel() {
    return isAutoRepairEnabledLabel;
  }

  public String getIsAutoReplaceEnabledLabel() {
    return isAutoReplaceEnabledLabel;
  }

  public String getIsProdCloudInNonProdEnvLabel() {
    return isProdCloudInNonProdEnvLabel;
  }

  public String getProdProfileWithStageCloudsLabel() {
    return prodProfileWithStageCloudsLabel;
  }

}
