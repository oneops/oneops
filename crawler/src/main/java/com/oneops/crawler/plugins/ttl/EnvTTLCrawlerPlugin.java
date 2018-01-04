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

    public EnvTTLCrawlerPlugin() {
        readConfig();
        ooFacade = new OneOpsFacade();
        searchDal = new SearchDal();

        searchDal.createIndex("oottl", "{\n" +
                "  \"mappings\": {\n" +
                "        \"environment\" : {\n" +
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
    }

    @Override
    public void processEnvironment(Environment env, List<Deployment> deployments) {
        if (isEligible(env, deployments)) {
            //log.info("Processing TTL for Env: " + env + " " + env.getNsPath());
            boolean disabledAnyPlatform = false;
            EnvironmentTTLRecord ttlRecord = null;
            ttlRecord = new EnvironmentTTLRecord();
            ttlRecord.setEnvironment(env);
            EnvironmentTTLRecord existingRecord =
                    (EnvironmentTTLRecord) searchDal.get("oottl", "environment", ttlRecord, "" + env.getId());
            if (existingRecord != null) {
                log.info("Existing ttl record: " + new Gson().toJson(existingRecord));
            }
            boolean envHasComputes = false;
            for (Platform platform : env.getPlatforms().values()) {
                if (platform.getTotalComputes() > 0) {
                    envHasComputes = true;
                    totalComputesTTLed += platform.getTotalComputes();
                    log.info("Total computes TTLed till now: " + totalComputesTTLed);

                    ttlRecord.setScanOnly(true); //by default, the scan-only is true
                    if (existingRecord != null
                            && existingRecord.getPlannedDestroyDate() != null
                            && Calendar.getInstance().getTime().compareTo(existingRecord.getPlannedDestroyDate()) >= 0) {
                        //current date is greater than or equal to "plannedDestroyDate" - meaning user was sent multiple notifications
                        //go ahead with destroy
                        log.info("Time is up for the env: " + env.getPath() + " env ci id: " + env.getId());
                        if (! ttlEnabled) {
                            log.info("TTL plugin is disabled, will not disable the platform " + platform.getName());
                        } else {
                            ttlRecord.setScanOnly(false);
                            ooFacade.disablePlatform(platform, ttlBotName);
                        }
                        disabledAnyPlatform = true;
                    }
                }
            }
            if (disabledAnyPlatform) {
                if (! ttlEnabled) {
                    log.info("TTL plugin is disabled, will not trigger the disable-deployment");
                } else {
                    log.warn("!!!!!! TTL Plugin is Enabled. Doing a force deploy !!!!!!");
                    ooFacade.forceDeploy(env, ttlBotName);
                }
            } else if (envHasComputes) { //since no disable/destroy deployment happend, notify env owner and save to ES
                if (ttlEnabled) {
                    sendTtlNotification(ttlRecord);
                }

                if (existingRecord != null) {
                    ttlRecord.setUserNotifiedTimes(existingRecord.getUserNotifiedTimes() + 1);
                }
            }
            if (ttlRecord != null && envHasComputes) {
                setDates(ttlRecord, existingRecord);
                if (saveToES) searchDal.push("oottl", "environment", ttlRecord, "" + env.getId());
            }
        }
    }

    private boolean isEligible(Environment env, List<Deployment> deployments) {
        if (deployments.size() == 0) {
            return false;
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
                return false;
            }
        }

        for (Platform platform : env.getPlatforms().values()) {
            if (config != null && config.getPacks() != null) {
                boolean packToBeProcessed = false;

                //Check if the org of this env enabled for ttl
                for (String pack : config.getPacks()) {
                    if (platform.getPack().toLowerCase().equals(pack)) {
                        packToBeProcessed = true;
                        break;
                    }
                }
                if (! packToBeProcessed) {
                    log.info("pack not configured for ttl: " + platform.getPack());
                    return false;
                }
            }

            for (String cloud : platform.getActiveClouds()) {
                if (cloud.toLowerCase().contains("prod")) {
                    log.info(platform.getId() + " Platform in Env not eligible because of prod clouds: " + platform.getPath());
                    return false;
                }
            }
        }

        Deployment lastDeploy = deployments.get(0);
        Date lastDeployDate = lastDeploy.getCreatedAt();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0 - noDeploymentDays);
        Date noDeploymentDate = cal.getTime();
        Object envProfile = env.getProfile();
        log.info(env.getPath() + " this env " + env + "with profile ["
                + env.getProfile() + "] last deployed on : " + lastDeploy.getCreatedAt());
        if ( envProfile != null
                && ! envProfile.toString().toLowerCase().contains("prod")
                && lastDeployDate.compareTo(noDeploymentDate) < 0
                && ! lastDeploy.getCreatedBy().equalsIgnoreCase(ttlBotName)
                ) {
            return true;
        }
        return false;
    }

    private void setDates(EnvironmentTTLRecord ttlRecord, EnvironmentTTLRecord existingTtlRecord) {
        ttlRecord.setLastProcessedAt(new Date(System.currentTimeMillis()));

        if (existingTtlRecord == null || existingTtlRecord.getPlannedDestroyDate() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, gracePeriodDays);
            Date destroyDate = calendar.getTime();
            ttlRecord.setPlannedDestroyDate(destroyDate);
        }
    }

    private void sendTtlNotification(EnvironmentTTLRecord ttlRecord) {
        Environment environment = ttlRecord.getEnvironment();
        NotificationMessage msg = new NotificationMessage();
        msg.setSubject("This Environment will be soon decommissioned : "
                + environment.getPath() + "/" + environment.getName());
        msg.setText("The environment " + environment.getPath()
                + " seems inactive for long time and will be auto-decommissioned by OneOps after "
                + ttlRecord.getPlannedDestroyDate());
        msg.setNsPath(environment.getPath() + "/" + environment.getName());
        msg.setCmsId(environment.getId());
        msg.setType(NotificationType.deployment);
//        msg.setSource("deployment");
        msg.setTimestamp(System.currentTimeMillis());
        msg.setSeverity(NotificationSeverity.critical);

        int ooResponse = ooFacade.sendNotification(msg);
        log.info("notification msg to be posted: " + new Gson().toJson(msg));
        if (ooResponse != 200) {
            log.warn("Notification could not be sent for env " + environment.getId()
                    + ". Error code from OO: " + ooResponse);
        } else {
            log.info("############# Notification sent for env id: " + ttlRecord.getEnvironment().getId());
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
