package com.oneops.crawler.plugins.hadr;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import com.oneops.crawler.SearchDal;

public class PlatformHADRCrawlerPlugin extends AbstractCrawlerPlugin {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private boolean isHadrPluginEnabled;
  private boolean isHadrEsEnabled;
  private String prodDataCentersList;
  private String[] dataCentersArr;
  private String oo_baseUrl;
  final String hadrElasticSearchIndexName = "hadr"; 
  private SearchDal searchDal;
  private String environmentProfileFilter;

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

    environmentProfileFilter = System.getProperty("hadr.env.profile.regex", "prod").toLowerCase();
    log.info("environmentProfileFilter: " + environmentProfileFilter);

    if (isHadrEsEnabled) {
      createIndexInElasticSearch();
    }

  }

  public void processEnvironment(Environment env, Map<String, Organization> organizationsMapCache) {

    if (isHadrPluginEnabled) {

      processOnlyProdEnvs(env, organizationsMapCache);
    } else {
      log.warn("HadrPlugin is not enabled ");
    }
  }

  public void processOnlyProdEnvs(Environment env,
      Map<String, Organization> organizationsMapCache) {

    String environmentProfileName = env.getProfile();

    if (environmentProfileName != null && environmentProfileName != ""
        && environmentProfileName.toLowerCase().contains(environmentProfileFilter)) {
      log.info(
          "Eligible environment for processing envId: {}, profile: {}, envName: {}, envPath: {}",
          env.getId(), env.getProfile(), env.getName(), env.getPath());
      Collection<Platform> platforms = env.getPlatforms().values();
      for (Platform platform : platforms) {
        log.info(
            "Platform for environmentId: {} ,environmetProfile: {},  platformId: {}, platformName {}, platform.getEnable {}",
            env.getId(), env.getProfile(), platform.getId(), platform.getName(),
            platform.getEnable());

        processPlatformForProdEnv(platform, env, organizationsMapCache);

      }
    } else {
      log.info(
          "Ignoring environment processing for envId: {}, envProfile: {}, envName: {}, envPath: {}",
          env.getId(), env.getProfile(), env.getName(), env.getPath());
    }
  }

  public void processPlatformForProdEnv(Platform platform, Environment env,
      Map<String, Organization> organizationsMapCache) {
    PlatformHADRRecord platformHADRRecord = new PlatformHADRRecord();
    platformHADRRecord.setIsDR(IsPlatformDRCompliant(platform));
    platformHADRRecord.setIsHA(IsPlatformHACompliant(platform));
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
    platformHADRRecord = setCloudCategories(platformHADRRecord, platformHADRRecord.getCloudsMap());
    String orginzationName = parseOrganizationNameFromNsPath(platform.getPath());
    platformHADRRecord.setOrg(orginzationName);
    platformHADRRecord.setOrganization(organizationsMapCache.get(orginzationName));

    if (isHadrEsEnabled) {
      log.info("Sending compliance record to Elastic Search");
      saveToElasticSearch(platformHADRRecord, String.valueOf(platform.getId()));

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

  public String parseOrganizationNameFromNsPath(String path) {

    if (path != null && !path.isEmpty()) {
      String[] parsedArray = path.split("/");
      return parsedArray[1];
    } else {
      return "";
    }
  }

  public String IsPlatformDRCompliant(Platform platform) {

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

  public String IsPlatformHACompliant(Platform platform) {

    if (platform.getActiveClouds().size() >= 2) {
      return "HA";
    }
    return "Non-HA";

  }

  public void saveToElasticSearch(PlatformHADRRecord platformHADRRecord, String platformCId) {

    log.info("Sending data record to elastic search");
    searchDal.push(this.hadrElasticSearchIndexName, "platform", platformHADRRecord, platformCId);
    log.info("Sent data record to elastic search");
    log.debug("JsonfiedString: " + new Gson().toJson(platformHADRRecord));

  }

  public void createIndexInElasticSearch() {

    searchDal.createIndex(this.hadrElasticSearchIndexName, gethadrIndexMappigs());

  }

  public PlatformHADRRecord setCloudCategories(PlatformHADRRecord platformHADRRecord,
      Map<String, Cloud> clouds) {

    List<String> activeClouds = new ArrayList<String>();
    List<String> primaryClouds = new ArrayList<String>();
    List<String> secondaryClouds = new ArrayList<String>();
    List<String> offlineClouds = new ArrayList<String>();

    for (String cloudName : clouds.keySet()) {
      Cloud cloud = clouds.get(cloudName);

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

    }
    platformHADRRecord.setActiveClouds(activeClouds);
    platformHADRRecord.setPrimaryClouds(primaryClouds);
    platformHADRRecord.setSecondaryClouds(secondaryClouds);

    return platformHADRRecord;

  }

  public String gethadrIndexMappigs() {
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
    } catch (Exception e) {
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

  public void setHadrEsEnabled(boolean isHadrEsEnabled) {
    this.isHadrEsEnabled = isHadrEsEnabled;
  }

}
