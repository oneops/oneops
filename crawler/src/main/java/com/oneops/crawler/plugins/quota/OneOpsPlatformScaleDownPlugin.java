/*******************************************************************************
 *
 *   Copyright 2018 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
package com.oneops.crawler.plugins.quota;

import com.google.gson.Gson;
import com.oneops.Deployment;
import com.oneops.Environment;
import com.oneops.Organization;
import com.oneops.Platform;
import com.oneops.crawler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class OneOpsPlatformScaleDownPlugin extends AbstractCrawlerPlugin {
    public static final String SCALE_DOWN_USER_ID = "OneOps-ScaleDown";
    private final Logger log = LoggerFactory.getLogger(getClass());
    String pluginName = "OneOps-ScaleDown";
    private OneOpsFacade ooFacade;
    private SearchDal searchDal;
    private ThanosClient thanosClient;
    private boolean esEnabled = false;
    private String indexName = "ooscaledown";

    public OneOpsPlatformScaleDownPlugin() {
        setPluginName(pluginName);
        ooFacade = new OneOpsFacade();
        searchDal = new SearchDal();
        thanosClient = new ThanosClient();
        init();
        if (!isEnabled()) {
            return;
        }
    }

    public void init() {
        readConfig();
        if (isEsEnabled() && isEnabled() ) {
            createIndex(); // do not proceed to create index if esEnabled is false
        }
    }

    @Override
    public void cleanup() {
        try {
            searchDal.flush(indexName);
        } catch (IOException e) {
            log.error("Error in ES flush api", e);
        }
    }

    void createIndex() {
        try {
            searchDal.createIndex(getIndexName(), CommonsUtil.getFileContent("scaleDownIndexMapping.json"));
        } catch (IOException e) {
            log.error("Error while creating ES index", e);
            throw new RuntimeException("Error while trying to create ES index for OO Scale Down plugin", e);
        }
    }

    public void setOoFacade(OneOpsFacade ooFacade) {
        this.ooFacade = ooFacade;
    }

    public void setSearchDal(SearchDal searchDal) {
        this.searchDal = searchDal;
    }

    void readConfig() {
        this.esEnabled = new Boolean(System.getProperty("scaledown.es.enabled", "false"));
        String configJson =  System.getProperty("scaledown.plugin.config");
        log.info("scale down config string: " + configJson);
        readConfig(configJson);

        indexName = System.getProperty("scaledown.index.name", "ooscaledown");
    }

    @Override
    public void configureSecrets(Properties props) {
        thanosClient.configure(props);
    }

    @Override
    public void processEnvironment(Environment env, Map<String, Organization> organizations) {
        log.info("Got environment : " + env.getId() + " path: " + env.getPath() + "/" + env.getName()
                + " Total platforms: " + env.getPlatforms().size());
        if (esEnabled) analyzeLastScaleDownRun(env);
        List<Long> eligiblePlatformIds = getEligiblePlatformIds(env);
        if (eligiblePlatformIds != null && eligiblePlatformIds.size() > 0) {
            for (Platform platform : env.getPlatforms().values()) {
                log.info("Processing platform # " + platform.getId());
                try {
                    String manifestNsPath = platform.getPath().replace("/bom/", "/manifest/");
                    ArrayList<ThanosClient.CloudResourcesUtilizationStats> cloudResourcesUtilizationStats
                            = thanosClient.getStats(manifestNsPath);
                    processCloudStats(platform, cloudResourcesUtilizationStats);
                } catch (Exception e) {
                    log.error("Error while processing platform: " + platform.getPath() + " id: " + platform.getId(), e);
                }
            }
        } else {
            log.info("There are no eligible platforms in this env: " + env.getPath() + "/" + env.getName());
        }
    }

    @Override
    protected boolean isPlatformEligible(Platform platform) {
        if (shouldCheckForAutoReplace()) {
            log.info("platform {} auto-replace flag is {}", platform.getPath(), platform.isAutoReplaceEnabled());
            return platform.isAutoReplaceEnabled();
        }
        return true;
    }

    private void processCloudStats(Platform platform,
                                   ArrayList<ThanosClient.CloudResourcesUtilizationStats> cloudResourcesUtilizationStats)
            throws IOException, OneOpsException {

        if (cloudResourcesUtilizationStats == null || cloudResourcesUtilizationStats.size() == 0) {
            log.info(platform.getId() + " platform has no cloudStats to process");
            return;
        }

        int scaleDownByNumber = 0;
        int scaleDownByCores = 0;
        int totalReclaimVms = 0;
        int totalReclaimCores = 0;
        int minComputesInEachCloud = 0;

        log.info("processing cloudStats: " + new Gson().toJson(cloudResourcesUtilizationStats));
        //Now for each cloud in that platform, process the stats and call scale-down oo api if eligible
        for (ThanosClient.CloudResourcesUtilizationStats stats : cloudResourcesUtilizationStats) {

            if (! stats.shouldReclaim()) {
                log.info("no reclaim becuase thanos api response says not to. For platform: " + platform.getPath());
                return;
            }
            if (ThanosClient.STATUS_EXECUTED.equalsIgnoreCase(stats.getReclaimStatus())) {
                log.info("scale down already executed, not doing again: {} {}", platform.getPath(), platform.getId());
                return;
            }
            int reclaimCountForThisCloud = (int) Math.ceil(stats.getReclaimVms());
            int reclaimCoresForThisCloud = (int) Math.ceil(stats.getReclaimCores());
            int minComputesForThisCloud = (int) Math.ceil(stats.getMinClusterSize());

            if ( reclaimCountForThisCloud > 0) {
                if (scaleDownByNumber == 0) {
                    scaleDownByNumber = reclaimCountForThisCloud;
                    scaleDownByCores = reclaimCoresForThisCloud;
                } else if (reclaimCountForThisCloud < scaleDownByNumber) {
                    log.info("The reclaim count is not even for this platform: "
                            + platform.getPath() + " id: " + platform.getId() + ". Will use min of all");
                    scaleDownByNumber = reclaimCountForThisCloud;
                    scaleDownByCores = reclaimCoresForThisCloud;
                }
                totalReclaimVms = totalReclaimVms + scaleDownByNumber;
                totalReclaimCores = totalReclaimCores + scaleDownByCores;
            }
            if (minComputesForThisCloud > minComputesInEachCloud) {
                minComputesInEachCloud = minComputesForThisCloud;
            }
        }
        if (scaleDownByNumber > 0) {
            scaleDown(scaleDownByNumber, minComputesInEachCloud, totalReclaimVms, totalReclaimCores,
                    platform, cloudResourcesUtilizationStats);
        }
    }

    private void scaleDown(int scaleDownByNumber, int minComputesInEachCloud, int totalReclaimVms, int totalReclaimCores,
                           Platform platform,
                           ArrayList<ThanosClient.CloudResourcesUtilizationStats> cloudResourcesUtilizationStats)
            throws IOException, OneOpsException {

        if (scaleDownByNumber != 0) {
            log.info("will scale down for this platform: " + platform.getPath() + " id: " + platform.getId());
            ScaleDownDetails record = new ScaleDownDetails();
            record.setCloudResourcesUtilizationStats(cloudResourcesUtilizationStats);
            record.setPlatform(platform);
            record.setVmReclaimCount(totalReclaimVms);
            record.setCoresReclaimCount(totalReclaimCores);
            if (isScaleDownEnabled()) {
                log.warn("Doing actual scale down for platform " + platform.getId());
                Deployment deployment = ooFacade.scaleDown(platform.getId(), scaleDownByNumber,
                        minComputesInEachCloud, SCALE_DOWN_USER_ID);
                if (deployment != null && deployment.getDeploymentId() > 0) {
                    log.info("Deployment submitted for platform {} id: {} , deployment id: "
                            + platform.getPath(), platform.getId(), deployment.getDeploymentId());
                    thanosClient.updateStatus(platform.getPath(), ThanosClient.STATUS_EXECUTED);
                } else {
                    throw new RuntimeException("Deployment id not valid or deployment not submitted for platform: "
                            + platform.getPath());
                }
            } else {
                searchDal.post(getIndexName(), "scaleDownDetails", record);
            }
        }
    }

    private boolean isScaleDownEnabled() {
        HashMap<String, String> customConfigs = getConfig().getCustomConfigs();
        if (customConfigs != null) {
            String scaleDownEnabled = customConfigs.get("scaleDownEnabled");
            if (scaleDownEnabled != null) {
                return Boolean.parseBoolean(scaleDownEnabled);
            }
        }
        return false;
    }

    private boolean shouldCheckForAutoReplace() {
        HashMap<String, String> customConfigs = getConfig().getCustomConfigs();
        if (customConfigs != null) {
            String autoReplaceCheck = customConfigs.get("checkAutoReplace");
            if (autoReplaceCheck != null) {
                return Boolean.parseBoolean(autoReplaceCheck);
            }
        }
        return true;
    }

    private void analyzeLastScaleDownRun(Environment env) {

    }

    public String getIndexName() {
        return indexName;
    }

    public boolean isEsEnabled() {
        return esEnabled;
    }

    public void setEsEnabled(boolean esEnabled) {
        this.esEnabled = esEnabled;
    }

    public OneOpsFacade getOoFacade() {
        return ooFacade;
    }

    public SearchDal getSearchDal() {
        return searchDal;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    public ThanosClient getThanosClient() {
        return thanosClient;
    }

    public void setThanosClient(ThanosClient thanosClient) {
        this.thanosClient = thanosClient;
    }
}
