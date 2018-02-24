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

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.oneops.Environment;
import com.oneops.Platform;
import com.oneops.notification.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class OneOpsFacade {
    private String cmsApiHost ;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Gson gson;

    public OneOpsFacade() {
        readConfig();
        gson = new Gson();
    }

    public void setCmsApiHost(String cmsApiHost) {
        this.cmsApiHost = cmsApiHost;
    }

    void readConfig() {
        cmsApiHost = System.getProperty("cms.api.host");
    }

    public void forceDeploy(Environment env, Platform platform, String userName) {
        Map<String, Platform> platformMap = env.getPlatforms();
        if (platformMap == null || platformMap.size() == 0) {
            return;
        }

        StringBuilder excludePlatforms = new StringBuilder();
        if (platform != null) {
            for (String platformName : platformMap.keySet()) {

                if (! platformName.equals(platformName)) {
                    if (excludePlatforms.length() > 0) excludePlatforms.append(",");
                    excludePlatforms.append(platformMap.get(platformName).getId());
                }
            }
        }
        HashMap<String, String> params = new HashMap<>();
        params.put("exclude", excludePlatforms.toString());

        log.info("deploying environment id: " + env.getId());
        HttpRequest request = HttpRequest.post("http://transistor." + cmsApiHost
                + "/transistor/rest/environments/" + env.getId() + "/deployments/deploy")
                .contentType("application/json").header("X-Cms-User", userName).send(gson.toJson(params));;
        String response = request.body();
        log.info("OO response : " + response);
    }

    public void disablePlatform(Platform platform, String userName) {
        log.info("disabling platform id: " + platform.getId());

        String response = HttpRequest.put("http://transistor." + cmsApiHost
                + "/transistor/rest/platforms/" + platform.getId() + "/disable")
                .contentType("application/json").header("X-Cms-User", userName).
                        body();
        log.info("OO response : " + response);

    }

    public int sendNotification(NotificationMessage msg) {
        log.info("sending notification: " + gson.toJson(msg));
        String antennaUrl = "http://antenna." + cmsApiHost + ":8080/antenna/rest/notify/";
        log.info("sending notification on " + antennaUrl);
        HttpRequest request = HttpRequest.post(antennaUrl)
                .contentType("application/json").send(gson.toJson(msg));
        return request.code();
    }
}
