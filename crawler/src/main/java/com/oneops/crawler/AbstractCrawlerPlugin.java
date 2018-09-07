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
package com.oneops.crawler;

import com.google.gson.Gson;
import com.oneops.Deployment;
import com.oneops.Environment;
import com.oneops.Organization;
import com.oneops.Platform;
import com.oneops.crawler.plugins.Config;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractCrawlerPlugin {
    protected abstract Logger getLogger();

    private Config config;

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    private String pluginName;

    public void processEnvironment(Environment env, List<Deployment> deployments, Map<String, Organization> organizations) {
        //default empty impl
    }
    public void processEnvironment(Environment env, Map<String, Organization> organizations) {
        //default empty impl
    }
    public void init() {
        //default empty impl
    }
    public void cleanup() {
        //default empty impl
    }
    
    protected Config readConfig(String json) {
        if (StringUtils.isEmpty(json)) {
            this.config = new Config();
        } else {
            this.config = new Gson().fromJson(json, Config.class);
        }

        return config;
    }

    public Config getConfig() {
        return config;
    }

    public List<Long> getEligiblePlatformIds(Environment env) {
        if (config != null && config.getOrgs() != null && config.getOrgs().length > 0) {
            boolean orgToBeProcessed = false;

            //Check if the org of this env enabled for ttl
            for (String org : config.getOrgs()) {
                if (env.getPath().startsWith("/" + org + "/")) {
                    orgToBeProcessed = true;
                    break;
                }
            }
            if (! orgToBeProcessed) {
                getLogger().info(getPluginName() + " org not configured for this plugin: " + env.getPath());
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
                    getLogger().info("pack not configured for processing: " + platform.getPack());
                    continue;
                }
            }

            if (isPlatformEligible(platform)) {
                eligiblePlatforms.add(platform.getId());
            }
        }
        return eligiblePlatforms;
    }

    protected boolean isPlatformEligible(Platform platform) {
        return true;
    }

    public boolean isEnabled() {
        return config.isEnabled();
    }

    public void configureSecrets(Properties props) {

    }
}


