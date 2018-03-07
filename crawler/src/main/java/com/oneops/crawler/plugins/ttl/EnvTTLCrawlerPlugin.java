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
import com.oneops.Platform;
import com.oneops.crawler.*;
import com.oneops.notification.NotificationMessage;
import com.oneops.notification.NotificationSeverity;
import com.oneops.notification.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EnvTTLCrawlerPlugin extends AbstractCrawlerPlugin {

    private int gracePeriodDays = 7;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private int noDeploymentDays = 90;
    String ttlBotName = "OneOps-TTL-Bot";
    private boolean ttlEnabled;
    private OneOpsFacade ooFacade;
    private SearchDal searchDal;
    private boolean saveToES = false;
    private EnvTTLConfig config;

    private int totalComputesTTLed = 0;
    private int notificationFrequencyDays = 0;
    private String prodCloudRegex;

    public EnvTTLCrawlerPlugin() {
        readConfig();
        ooFacade = new OneOpsFacade();
        searchDal = new SearchDal();

        searchDal.createIndex("oottl", "{\n" +
                "  \"mappings\": {\n" +
                "        \"platform\" : {\n" +
                "                \"properties\" : {\n" +
                "                        \"nsPath\" : {\n" +
                "                                \"type\" : \"string\",\n" +
                "                                \"index\" : \"not_analyzed\"\n" +
                "                        }\n" +
                "                }\n" +
                "        }\n" +
                "   }\n" +
                "}\n");
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
            saveToES = true;
        } else {
            saveToES = false;
        }
        String gracePeriodDays = System.getProperty("ttl.grace.period.days", "7");
        this.gracePeriodDays = Integer.valueOf(gracePeriodDays);

        String configString =  System.getProperty("ttl.config");
        log.info("ttl config string: " + configString);
        config = new Gson().fromJson(configString, EnvTTLConfig.class);

        String frequency = System.getProperty("ttl.notification.frequency.days", "0");
        this.notificationFrequencyDays = Integer.valueOf(frequency);
        log.info("Notification frequency days: " + notificationFrequencyDays);

        prodCloudRegex = System.getProperty("ttl.prod.clouds.regex", ".*prod.*");
        log.info("regex for production clouds: [" + prodCloudRegex + "]");
    }

    @Override
    public void processEnvironment(Environment env, List<Deployment> deployments) {
        List<Long> eligiblePlatformIds = getEligiblePlatformIds(env, deployments);
        if (eligiblePlatformIds != null && eligiblePlatformIds.size() > 0) {
            //log.info("Processing TTL for Env: " + env + " " + env.getNsPath());
            for (Platform platform : env.getPlatforms().values()) {
                if (eligiblePlatformIds.contains(platform.getId()) && platform.getTotalComputes() > 0) {
                    EnvironmentTTLRecord ttlRecord = new EnvironmentTTLRecord();
                    ttlRecord.setEnvironmentProfile(env.getProfile());
                    ttlRecord.setEnvironmentId(env.getId());
                    ttlRecord.setPlatform(platform);

                    EnvironmentTTLRecord existingRecord =
                            (EnvironmentTTLRecord) searchDal.get("oottl",
                                    "platform", ttlRecord, "" + platform.getId());
                    if (existingRecord != null) {
                        log.info("Existing ttl record: " + new Gson().toJson(existingRecord));
                        if (existingRecord.getLastProcessedAt() != null
                                && (System.currentTimeMillis() - existingRecord.getLastProcessedAt().getTime()
                                < notificationFrequencyDays * 24 * 60 * 60 * 1000)) {
                            //its been less than configured notification interval time since last processed this env
                            log.info("Not yet " + notificationFrequencyDays
                                    + " day(s) since last processed. Skipping this platform: " + platform.getPath()
                                    + "/" + platform.getName());
                            return;
                        }
                    }
                    totalComputesTTLed += platform.getTotalComputes();
                    log.info("Total computes TTLed till now: " + totalComputesTTLed);

                    if (! ttlEnabled) ttlRecord.setScanOnly(true); //by default, the scan-only is true
                    if (existingRecord != null && existingRecord.getPlannedDestroyDate() != null) {

                        if (Calendar.getInstance().getTime().compareTo(existingRecord.getPlannedDestroyDate()) >= 0) {
                            //current date is greater than or equal to "plannedDestroyDate" - meaning user was sent multiple notifications
                            //go ahead with destroy
                            log.info("Time is up for the platform: " + platform.getPath() + "/" + platform.getName()
                                    + " platform ci id: " + platform.getId() + " total computes: " + platform.getTotalComputes());
                            if (! ttlEnabled) {
                                log.info("TTL plugin is disabled, will not disable the platform " + platform.getName());
                            } else {
                                ttlRecord.setScanOnly(false);
                                ooFacade.disablePlatform(platform, ttlBotName);
                                log.warn("!!!!!! TTL Plugin is Enabled. Doing a force deploy !!!!!!");
                                ooFacade.forceDeploy(env, platform, ttlBotName);
                                ttlRecord.setActualDestroyDate(new Date());
                                break; //Deploy only one platform at a time
                            }
                        }
                    }
                    setDates(ttlRecord, existingRecord);
                    if (ttlEnabled) { // in grace period, send notification
                        sendTtlNotification(ttlRecord);
                        if (existingRecord != null) {
                            ttlRecord.setUserNotifiedTimes(existingRecord.getUserNotifiedTimes() + 1);
                        }
                    }
                    if (saveToES) {
                        searchDal.push("oottl", "platform", ttlRecord, "" + platform.getId());
                    }
                }
            }
        } else {
            log.info("There are no eligible platforms in this env: " + env.getPath() + "/" + env.getName());
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

            for (String cloud : platform.getActiveClouds()) {
                if (cloud.toLowerCase().matches(prodCloudRegex)) {
                    log.info(platform.getId() + " Platform not eligible because of prod clouds: "
                            + platform.getPath());
                    continue platforms;
                }
            }
            eligiblePlatforms.add(platform.getId());
        }

        Deployment lastDeploy = findLastDeploymentByUser(deployments);
        if (lastDeploy == null) {
            return null;
        }
        Date lastDeployDate = lastDeploy.getCreatedAt();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0 - noDeploymentDays);
        Date noDeploymentDate = cal.getTime();
        Object envProfile = env.getProfile();
        log.info(env.getPath() + " this env " + env + " with profile ["
                + env.getProfile() + "] last deployed on : " + lastDeploy.getCreatedAt());
        if ( envProfile != null
                && ! envProfile.toString().toLowerCase().contains("prod")
                && lastDeployDate.compareTo(noDeploymentDate) < 0
                && ! deployments.get(0).getState().equalsIgnoreCase("active")
                ) {
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

    private void setDates(EnvironmentTTLRecord ttlRecord, EnvironmentTTLRecord existingTtlRecord) {
        ttlRecord.setLastProcessedAt(new Date());
        if (! ttlEnabled) {
            ttlRecord.setPlannedDestroyDate(null);
            return;
        }
        if (existingTtlRecord == null || existingTtlRecord.getPlannedDestroyDate() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, gracePeriodDays);
            Date destroyDate = calendar.getTime();
            ttlRecord.setPlannedDestroyDate(destroyDate);
        }
    }

    private void sendTtlNotification(EnvironmentTTLRecord ttlRecord) {
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

        int ooResponse = ooFacade.sendNotification(msg);
        log.info("notification msg to be posted: " + new Gson().toJson(msg));
        if (ooResponse != 200) {
            log.warn("Notification could not be sent for platform " + platform.getId()
                    + ". Error code from OO: " + ooResponse);
        } else {
            log.info("##### Notification sent for platform id: " + ttlRecord.getPlatform().getId());
        }
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
