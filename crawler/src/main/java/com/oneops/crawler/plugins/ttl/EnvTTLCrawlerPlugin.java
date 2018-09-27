/*******************************************************************************
 *
 *   Copyright 2017 Walmart, Inc.
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
package com.oneops.crawler.plugins.ttl;

import com.google.gson.Gson;
import com.oneops.Deployment;
import com.oneops.Environment;
import com.oneops.Organization;
import com.oneops.Platform;
import com.oneops.crawler.*;
import com.oneops.notification.NotificationMessage;
import com.oneops.notification.NotificationSeverity;
import com.oneops.notification.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;

public class EnvTTLCrawlerPlugin extends AbstractCrawlerPlugin {

    private int gracePeriodDays = 7;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private int noDeploymentDays = 90;
    String ttlBotName = "OneOps-TTL-Bot";
    private boolean ttlEnabled;
    private OneOpsFacade ooFacade;
    private SearchDal searchDal;
    private boolean esEnabled = false;
    private EnvTTLConfig config;
    private boolean retryTtlOnFailure;

    private int totalComputesTTLed = 0;
    private int notificationFrequencyDays = 0;
    private int minUserNotifications = 0;
    private String prodCloudRegex;
    private String indexName = "oottl";

    public EnvTTLCrawlerPlugin() {
        ooFacade = new OneOpsFacade();
        searchDal = new SearchDal();
        init();
    }

    public void init() {
        readConfig();
        if (esEnabled) {
            createIndex(); // do not proceed to create index if esEnabled is false
        }
    }

    public void cleanup() {
        try {
            searchDal.flush(indexName);
        } catch (IOException e) {
            log.error("Error in ES flush api", e);
        }
    }

    void createIndex() {
        try {
            searchDal.createIndex(indexName, "{\n" +
                    "  \"mappings\": {\n" +
                    "        \"platform\" : {\n" +
                    "                \"properties\" : {\n" +
                    "                        \"nsPath\" : {\n" +
                    "                                \"type\" : \"string\",\n" +
                    "                                \"index\" : \"not_analyzed\"\n" +
                    "                        },\n" +
                    "                        \"activeClouds\" : {\n" +
                    "                                \"type\" : \"string\",\n" +
                    "                                \"index\" : \"not_analyzed\"\n" +
                    "                        },\n" +
                    "                        \"plannedDestroyDate\" : {\n" +
                    "                                \"type\" : \"date\"\n" +
                    "                        },\n" +
                    "                        \"lastProcessedAt\" : {\n" +
                    "                                \"type\" : \"date\"\n" +
                    "                        },\n" +
                    "                        \"actualDestroyDate\" : {\n" +
                    "                                \"type\" : \"date\"\n" +
                    "                        }\n" +
                    "                }\n" +
                    "        }\n" +
                    "   }\n" +
                    "}\n");
        } catch (IOException e) {
            log.error("Error while creating ES index", e);
            throw new RuntimeException("Error while trying to create ES index for TTL plugin", e);
        }
    }

    public void setNoDeploymentDays(int noDeploymentDays) {
        this.noDeploymentDays = noDeploymentDays;
    }

    public void setTtlBotName(String ttlBotName) {
        this.ttlBotName = ttlBotName;
    }

    public void setTtlEnabled(boolean ttlEnabled) {
        this.ttlEnabled = ttlEnabled;
    }

    public void setOoFacade(OneOpsFacade ooFacade) {
        this.ooFacade = ooFacade;
    }

    public void setSearchDal(SearchDal searchDal) {
        this.searchDal = searchDal;
    }

    public void setGracePeriodDays(int gracePeriodDays) {
        this.gracePeriodDays = gracePeriodDays;
    }

    void readConfig() {
        String noDeploymentDaysProperty = System.getProperty("ttl.deployed.before.days", "60");
        noDeploymentDays = Integer.valueOf(noDeploymentDaysProperty);
        String ttlEnabledString = System.getProperty("ttl.plugin.enabled");
        if ("true".equalsIgnoreCase(ttlEnabledString)) {
            ttlEnabled = true;
            log.info("TTL plugin is enabled !");
        }
        String esEnabled = System.getProperty("ttl.es.enabled");
        if ("true".equalsIgnoreCase(esEnabled)) {
            this.esEnabled = true;
        } else {
            this.esEnabled = false;
        }
        String gracePeriodDays = System.getProperty("ttl.grace.period.days", "7");
        this.gracePeriodDays = Integer.valueOf(gracePeriodDays);

        String configString =  System.getProperty("ttl.config");
        log.info("ttl config string: " + configString);
        config = new Gson().fromJson(configString, EnvTTLConfig.class);

        String frequency = System.getProperty("ttl.notification.frequency.days", "0");
        this.notificationFrequencyDays = Integer.valueOf(frequency);
        log.info("Notification frequency days: " + notificationFrequencyDays);

        String userNotifications = System.getProperty("ttl.notification.min", "2");
        this.minUserNotifications = Integer.valueOf(userNotifications);
        log.info("Minimum User Notifications: " + minUserNotifications);

        prodCloudRegex = System.getProperty("ttl.prod.clouds.regex", ".*prod.*");
        log.info("regex for production clouds: [" + prodCloudRegex + "]");

        String retryTtlOnFailureConfig = System.getProperty("ttl.plugin.retryonfail", "false");
        retryTtlOnFailure = Boolean.valueOf(retryTtlOnFailureConfig);

        indexName = System.getProperty("ttl.index.name", "oottl");
    }

    @Override
    public void processEnvironment(Environment env, List<Deployment> deployments, Map<String, Organization> organizations) {
        log.info("Got environment : " + env.getId() + " path: " + env.getPath() + "/" + env.getName()
                + " Total platforms: " + env.getPlatforms().size());
        if (esEnabled) analyzeLastTtlRun(env, deployments);
        List<Long> eligiblePlatformIds = getEligiblePlatformIds(env, deployments);
        if (eligiblePlatformIds != null) log.info("total eligible platforms: " + eligiblePlatformIds.size());
        if (eligiblePlatformIds != null && eligiblePlatformIds.size() > 0) {
            for (Platform platform : env.getPlatforms().values()) {
                log.info("Processing platform # " + platform.getId());
                boolean disabledPlatform = false;
                if (eligiblePlatformIds.contains(platform.getId()) && platform.getTotalComputes() > 0) {
                    if ("disable".equalsIgnoreCase(platform.getEnable())) {
                        try {
                            ESRecord esRecord = searchEarlierTTL(platform, env);
                            //Disable state of the platform is because of the TTL happened for this platform earlier
                            //do not try the TTL again unless configured otherwise
                            if (retryTtlOnFailure) {
                                decommissionPlatform(platform, env, deployments);
                            } else {
                                if (esRecord != null) continue;
                            }
                        } catch (Exception e) {
                            log.error("Error while searching for earlier ttl attempt: ", e);
                            continue;
                        }
                    }

                    EnvironmentTTLRecord ttlRecord = new EnvironmentTTLRecord();
                    ttlRecord.setEnvironmentProfile(env.getProfile());
                    ttlRecord.setEnvironmentId(env.getId());
                    ttlRecord.setPlatform(platform);

                    EnvironmentTTLRecord existingRecord = null;
                    ESRecord esRecord;
                    try {
                        esRecord = searchExistingRecord(ttlRecord, false);
                        if (esRecord != null) {
                            existingRecord = (EnvironmentTTLRecord) esRecord.getSource();
                        }
                    } catch (Exception e) {
                        log.error("error while querying ES record for platform : " + new Gson().toJson(platform), e);
                        return;
                    }
                    if (existingRecord != null) {
                        log.info("Existing ttl record: " + new Gson().toJson(existingRecord));
                        ttlRecord = existingRecord;
                        if (ttlRecord.getLastProcessedAt() != null
                                && (System.currentTimeMillis() - existingRecord.getLastProcessedAt().getTime()
                                < notificationFrequencyDays * 24 * 60 * 60 * 1000)) {
                            //its been less than configured notification interval time since last processed this env
                            log.info("Not yet " + notificationFrequencyDays
                                    + " day(s) since last processed. Skipping this platform: " + platform.getPath()
                                    + "/" + platform.getName());
                            return;
                        }
                    } else {
                        log.info("No existing TTL record");
                    }
                    totalComputesTTLed += platform.getTotalComputes();

                    if (! ttlEnabled) ttlRecord.setScanOnly(true); //by default, the scan-only is true
                    if (ttlRecord.getPlannedDestroyDate() != null
                            && Calendar.getInstance().getTime().compareTo(ttlRecord.getPlannedDestroyDate()) >= 0
                            && ttlRecord.getUserNotifiedTimes() >= minUserNotifications) {

                            //current date is greater than or equal to "plannedDestroyDate" - meaning user was sent multiple notifications
                            //go ahead with destroy
                            log.info("Time is up for the platform: " + platform.getPath() + "/" + platform.getName()
                                    + " platform ci id: " + platform.getId() + " total computes: " + platform.getTotalComputes());
                            if (! ttlEnabled) {
                                log.info("TTL plugin is disabled, will not disable the platform " + platform.getName());
                            } else {
                                ttlRecord.setScanOnly(false);
                                try {
                                    decommissionPlatform(platform, env, deployments);
                                    ttlRecord.setTtlDeploymentSubmitted(true);
                                } catch (Exception e) {
                                    log.error("Error while disabling/deploying platform : " + platform.getId()
                                            + " Error from OneOps: " + e.getMessage(), e);
                                    continue;
                                }
                                disabledPlatform = true;
                            }
                    }
                    setDates(ttlRecord, disabledPlatform);
                    if (ttlEnabled && ttlRecord.getActualDestroyDate() == null) { // in grace period, send notification
                        try {
                            sendTtlNotification(ttlRecord);
                        } catch (Exception e) {
                            log.error("Error while sending TTL notification: " + e.getMessage(), e);
                            return;
                        }
                        ttlRecord.setUserNotifiedTimes(ttlRecord.getUserNotifiedTimes() + 1);
                    }
                    String orginzationName = CommonsUtil.parseOrganizationNameFromNsPath(platform.getPath());
                    ttlRecord.setOrganization(organizations.get(orginzationName));

                    if (esEnabled) {
                        try {
                            if (existingRecord != null) {
                                searchDal.put(indexName, "platform", ttlRecord, esRecord.getId());
                            } else {
                                log.info("for platform id: " + platform.getId() + " user notified: "
                                        + ttlRecord.getUserNotifiedTimes()
                                + " cores reclaimed: " + ttlRecord.getReclaimedCores() );
                                searchDal.post(indexName, "platform", ttlRecord);
                            }
                        } catch (IOException e) {
                            log.error("Error while saving platform to ES : " + new Gson().toJson(platform), e);
                            return;
                        }
                    }
                    if (disabledPlatform) {
                        break; //Deploy only one platform at a time
                    }
                }
            }
        } else {
            log.info("There are no eligible platforms in this env: " + env.getPath() + "/" + env.getName());
        }
    }

    private ESRecord searchEarlierTTL(Platform platform, Environment env) throws IOException, URISyntaxException {
        EnvironmentTTLRecord ttlRecord = new EnvironmentTTLRecord();
        ttlRecord.setEnvironmentProfile(env.getProfile());
        ttlRecord.setEnvironmentId(env.getId());
        ttlRecord.setPlatform(platform);

        return searchExistingRecord(ttlRecord, true);
    }

    private void analyzeLastTtlRun(Environment env, List<Deployment> deployments) {
        for (Platform platform : env.getPlatforms().values()) {
            EnvironmentTTLRecord ttlRecord = new EnvironmentTTLRecord();
            ttlRecord.setEnvironmentProfile(env.getProfile());
            ttlRecord.setEnvironmentId(env.getId());
            ttlRecord.setPlatform(platform);

            ESRecord esRecord = null;
            //first set the actual cores reclaimed
            if ( "disable".equalsIgnoreCase(platform.getEnable())) {
                log.info("platform is in disabled state: " + platform.getId());
                try {
                    esRecord = searchExistingRecord(ttlRecord, true);
                    if (esRecord == null) {
                        log.info("There is no previous ttl run");
                        return;
                    } else {
                        log.info("found previous ttl run for platform: " + platform.getId());
                    }
                    ttlRecord = (EnvironmentTTLRecord) esRecord.getSource();
                } catch (Exception e) {
                    log.error("Error while searching deploymentSubmitted record for platform: " + platform.getId(), e);
                }
                if (esRecord == null) {
                    continue;
                }
                //platform is disabled and ttlRecord shows that the ttl was attempted
                //calculate and set the correct reclaimed # of cores
                int originalCores = ttlRecord.getPlatform().getTotalCores();
                int currentCores = platform.getTotalCores();
                int reclaimedCoresTillNow = ttlRecord.getReclaimedCores();
                log.info("platform id : " + platform.getId() + " original cores: " + originalCores
                + " current cores: " + currentCores );
                if (originalCores - currentCores > reclaimedCoresTillNow) {
                    ttlRecord.setReclaimedCores(originalCores - currentCores);
                    log.info("set reclaimed cores for platform id " + platform.getId() + " to " + ttlRecord.getReclaimedCores());
                }
                //now check for reclaimed computes:
                int originalComputes = ttlRecord.getPlatform().getTotalComputes();
                int currentComputes = platform.getTotalComputes();
                int reclaimedComputesTillNow = ttlRecord.getReclaimedComputes();
                log.info("platform id : " + platform.getId() + " original computes: " + originalComputes
                        + " current computes: " + currentComputes );
                if (originalComputes - currentComputes > reclaimedComputesTillNow) {
                    ttlRecord.setReclaimedComputes(originalComputes - currentComputes);
                    log.info("set reclaimed computes for platform id " + platform.getId() + " to " + ttlRecord.getReclaimedCores());
                }

                if (ttlRecord.getReclaimedCores() != originalCores) {
                    //this means ttl deployment could not finish successfully
                    ttlRecord.setTtlFailed(true);
                } else {
                    ttlRecord.setTtlFailed(false);
                }
                try {
                    if (esEnabled) searchDal.put(indexName, "platform", ttlRecord, esRecord.getId());
                } catch (Exception e) {
                    log.error("AnalyzeLastRun: Error while saving updated record ", e);
                }
            }
        }
    }

    private ESRecord searchExistingRecord(EnvironmentTTLRecord record, boolean ttlDeploymentSubmitted)
            throws URISyntaxException, IOException {
        Stream<String> lines = null;

        try {
            StringBuilder query = new StringBuilder();
            lines = new BufferedReader(new InputStreamReader(ClassLoader
                    .getSystemResourceAsStream("activeTtlRecordQuery.json"))).lines();
            lines.forEach(line -> query.append(line).append(System.lineSeparator()));
            String queryJson = query.toString()
                    .replace("<platformId>", "" + record.getPlatform().getId())
                    .replace("<ttlDeploymentSubmitted>", String.valueOf(ttlDeploymentSubmitted));
            log.debug("search query json : " + queryJson);
            List<ESRecord> result =  searchDal.search(indexName,"platform", record, queryJson);
            if (result == null || result.size() == 0) {
                return null;
            }
            ESRecord existingRecord = result.get(0);
            log.info("ES returned this: " + new Gson().toJson(existingRecord));
            return existingRecord;
        } finally {
            if (lines != null) lines.close();
        }
    }

    private void decommissionPlatform(Platform platform, Environment env, List<Deployment> deployments)
            throws OneOpsException, IOException {

        if (deployments != null && deployments.size() > 0) {
            Deployment lastDeploy = deployments.get(0);
            try {
                if (lastDeploy.getState().equalsIgnoreCase("failed")
                        && (! ttlBotName.equalsIgnoreCase(lastDeploy.getCreatedBy()) || retryTtlOnFailure)) {
                    log.info("last deployment is in failed state, cancelling it. Deployment id: "
                            + lastDeploy.getDeploymentId());
                    ooFacade.cancelDeployment(lastDeploy, ttlBotName);
                }
                ooFacade.disablePlatform(platform, ttlBotName);
                log.warn("!!!!!! TTL Plugin is Enabled. Doing a force deploy !!!!!!");
                ooFacade.disableVerify(env, ttlBotName);
                ooFacade.forceDeploy(env, platform, ttlBotName);
            } catch (IOException e) {
                log.error("Error while decommissioning platform", e);
                throw e;
            }
        }
    }

    private ArrayList<Long> getEligiblePlatformIds(Environment env, List<Deployment> deployments) {
        if (deployments.size() == 0) {
            return null;
        }

        if (config != null && config.getOrgs() != null) {
            boolean orgToBeProcessed = false;

            //Check if the org of this env enabled for ttl
            for (String org : config.getOrgs()) {
                if (env.getPath().startsWith("/" + org + "/")) {
                    orgToBeProcessed = true;
                    break;
                }
            }
            if (! orgToBeProcessed) {
                log.info("org not configured for ttl: " + env.getPath());
                return null;
            }
        }

        ArrayList<Long> eligiblePlatforms = new ArrayList<>();

        platforms: for (Platform platform : env.getPlatforms().values()) {
            if (config != null && config.getPacks() != null) {
                boolean packToBeProcessed = false;

                //Check if the pack is enabled for ttl
                for (String pack : config.getPacks()) {
                    if (platform.getPack().toLowerCase().equals(pack)) {
                        packToBeProcessed = true;
                        break;
                    }
                }
                if (! packToBeProcessed) {
                    log.info("pack not configured for ttl: " + platform.getPack());
                    continue;
                }
            }

            List<String> activeClouds = platform.getActiveClouds();
            if (activeClouds == null || activeClouds.size() == 0 ) {
                log.info("no active clouds for platform id: " + platform.getId());
                continue platforms;
            }
            for (String cloud : platform.getActiveClouds()) {
                if (cloud.toLowerCase().matches(prodCloudRegex)) {
                    log.info(platform.getId() + " Platform not eligible because of prod clouds: "
                            + platform.getPath());
                    continue platforms;
                }
            }
            eligiblePlatforms.add(platform.getId());
        }

        Deployment lastDeployByUser = findLastDeploymentByUser(deployments);
        if (lastDeployByUser == null) {
            return null;
        }
        Date lastDeployDate = lastDeployByUser.getCreatedAt();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0 - noDeploymentDays);
        Date noDeploymentDate = cal.getTime();
        Object envProfile = env.getProfile();
        log.info(env.getPath() + " this env " + env + " with profile ["
                + env.getProfile() + "] last deployed on : " + lastDeployByUser.getCreatedAt());
        Deployment lastDeploy = deployments.get(0);
        if ( envProfile != null
                && ! envProfile.toString().toLowerCase().contains("prod")
                && lastDeployDate.compareTo(noDeploymentDate) < 0) {
            if (! lastDeploy.getState().equalsIgnoreCase("complete")
                    && ! lastDeploy.getState().equalsIgnoreCase("failed")
                    && ! lastDeploy.getState().equalsIgnoreCase("canceled")) {
                log.warn("Deployment is in " + lastDeploy.getState() + " state for too long: ");
                return null;
            }
            return eligiblePlatforms;
        }
        return null;
    }

    private Deployment findLastDeploymentByUser(List<Deployment> deployments) {
        Deployment lastDeployment = null;
        for (Deployment deployment : deployments) {
            if (! deployment.getCreatedBy().equalsIgnoreCase(ttlBotName)) {
                lastDeployment = deployment;
                break;
            }
        }
        return lastDeployment;
    }

    private void setDates(EnvironmentTTLRecord ttlRecord, boolean disabledPlatform) {
        ttlRecord.setLastProcessedAt(new Date());
        if (! ttlEnabled) {
            ttlRecord.setPlannedDestroyDate(null);
            return;
        }
        if (ttlRecord.getPlannedDestroyDate() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, gracePeriodDays);
            Date destroyDate = calendar.getTime();
            ttlRecord.setPlannedDestroyDate(destroyDate);
        } else if (disabledPlatform) {
            ttlRecord.setActualDestroyDate(new Date());
        }
    }

    private void sendTtlNotification(EnvironmentTTLRecord ttlRecord) throws IOException, OneOpsException {
        Platform platform = ttlRecord.getPlatform();
        NotificationMessage msg = new NotificationMessage();
        msg.setSubject("Critical: Upcoming Deletion of Your OneOps Environment");
        msg.setText("The referenced OneOps Environment Platform was detected to be "
            + "inactive for a long time. The automated decommissioning of the enviroment "
            + "will be performed on  " + ttlRecord.getPlannedDestroyDate() + ". "
            + "Please contact OneOps Support, if you have any objections.");
        msg.setNsPath(platform.getPath());
        msg.setCmsId(ttlRecord.getEnvironmentId());
        msg.setType(NotificationType.deployment);
//        msg.setSource("deployment");
        msg.setTimestamp(System.currentTimeMillis());
        msg.setSeverity(NotificationSeverity.critical);

        int ooResponse = 0;
        ooResponse = ooFacade.sendNotification(msg);
        log.info("notification msg to be posted: " + new Gson().toJson(msg));
        if (ooResponse >= 300) {
            log.warn("Notification could not be sent for platform " + platform.getId()
                    + ". Error code from OO: " + ooResponse);
        } else {
            log.info("##### Notification sent for platform id: " + ttlRecord.getPlatform().getId());
        }
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
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
}

class EnvTTLConfig {
    String[] orgs;
    String[] packs;

    public String[] getOrgs() {
        return orgs;
    }

    public void setOrgs(String[] orgs) {
        this.orgs = orgs;
    }

    public String[] getPacks() {
        return packs;
    }

    public void setPacks(String[] packs) {
        this.packs = packs;
    }
}
