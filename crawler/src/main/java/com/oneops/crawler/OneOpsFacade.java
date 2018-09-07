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
import com.oneops.Platform;
import com.oneops.notification.NotificationMessage;
import com.squareup.okhttp.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OneOpsFacade {
    private String transistorBaseUrl ;

    private String adapterBaseUrl ;
    private String antennaBaseUrl ;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Gson gson;
    OkHttpClient client = new OkHttpClient();

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public OneOpsFacade() {
        readConfig();
        gson = new Gson();
    }

    public void setTransistorBaseUrl(String transistorBaseUrl) {
        this.transistorBaseUrl = transistorBaseUrl;
    }

    public void setAdapterBaseUrl(String adapterBaseUrl) {
        this.adapterBaseUrl = adapterBaseUrl;
    }

    public void setAntennaBaseUrl(String antennaBaseUrl) {
        this.antennaBaseUrl = antennaBaseUrl;
    }

    void readConfig() {
        transistorBaseUrl = System.getProperty("transistor.base.url");
        adapterBaseUrl = System.getProperty("adapter.base.url");
        antennaBaseUrl = System.getProperty("antenna.base.url");
    }

    public int forceDeploy(Environment env, Platform platform, String userName) throws IOException, OneOpsException {
        Map<String, Platform> platformMap = env.getPlatforms();
        if (platformMap == null || platformMap.size() == 0) {
            return 400;
        }

        StringBuilder excludePlatforms = new StringBuilder();
        if (platform != null) {
            for (String platformName : platformMap.keySet()) {

                if (! platformName.equals(platform.getName())) {
                    if (excludePlatforms.length() > 0) excludePlatforms.append(",");
                    excludePlatforms.append(platformMap.get(platformName).getId());
                }
            }
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("exclude", excludePlatforms.toString());

        log.info("deploying environment id: " + env.getId());
        RequestBody body = RequestBody.create(JSON, gson.toJson(params));

        String url = transistorBaseUrl + "/transistor/rest/environments/" + env.getId() + "/deployments/deploy";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Cms-User", userName)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        log.info("OO response body: " + responseBody + ", code: " + responseCode);
        if (responseCode >= 300) {
            throw new OneOpsException("Error while doing deployment. Response from OneOps: " + responseBody
            + " ResponseCode : " + responseCode);
        }
        return responseCode;
    }

    public int disablePlatform(Platform platform, String userName) throws IOException, OneOpsException {
        log.info("disabling platform id: " + platform.getId());

        String url = transistorBaseUrl + "/transistor/rest/platforms/" + platform.getId() + "/disable";

        RequestBody body = RequestBody.create(JSON, "");

        Request request = new Request.Builder()
                .url(url)
                .header("X-Cms-User", userName)
                .addHeader("Content-Type", "application/json")
                .put(body)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        Map<String, Double> release = gson.fromJson(responseBody, Map.class);
        log.info("Response from OneOps for disable-platform api: " + release);
        int responseCode = response.code();
        if (responseCode >= 300) {
            throw new OneOpsException("Could not disable platform, response from OneOps: " + responseBody
            + ", response code: " + responseCode);
        }
        return responseCode;
    }

    public int sendNotification(NotificationMessage msg) throws IOException, OneOpsException {
        String url = antennaBaseUrl + "/antenna/rest/notify/";
        log.info(url + " sending notification: " + gson.toJson(msg));
        RequestBody body = RequestBody.create(JSON, gson.toJson(msg));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        if (responseCode >= 300) {
            throw new OneOpsException("Error while sending notification. Response from OneOps: " + responseBody
                    + " ResponseCode : " + responseCode);
        }

        log.info("OO response body from antenna notify request: " + responseBody + ", code: " + responseCode);
        return responseCode;
    }

    public int cancelDeployment(Deployment deployment, String userName) throws IOException {
        String url = adapterBaseUrl + "/adapter/rest/dj/simple/deployments/" + deployment.getDeploymentId() + "/cancel";
        log.info("calling cancel deploy api: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        log.info("OO response body for cance deployment: " + responseBody + ", code: " + responseCode);
        return responseCode;
    }

    public Deployment scaleDown(long platformId, int scaleDownByNumber, int minComputesInEachCloud, String userId)
            throws IOException, OneOpsException {
        HashMap<String, String> params = new HashMap<>();
        params.put("scaleDownBy", String.valueOf(scaleDownByNumber));
        params.put("minComputesInEachCloud", String.valueOf(minComputesInEachCloud));

        RequestBody body = RequestBody.create(JSON, gson.toJson(params));
        String url = transistorBaseUrl + "/transistor/rest/platforms/" +  + platformId + "/deployments/scaledown";
        log.info("scaling down platform id: {} , url: {}", platformId, url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Cms-User", userId)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        log.info("OO response body: {}, code: {}", responseBody, responseCode);
        if (responseCode >= 300) {
            throw new OneOpsException("Error while scaling down platform: " + platformId
                    + ". Response from OneOps: " + responseBody + " ResponseCode : " + responseCode);
        }
        if (! StringUtils.isEmpty(responseBody)) {
            return gson.fromJson(responseBody, Deployment.class);
        }
        return null;
    }

    public int disableVerify(Environment env, String userName) throws IOException, OneOpsException {

        String envCiJson = getCiJson(env.getId());
        envCiJson = envCiJson.replace("\"verify\":\"default\"", "\"verify\":\"false\"");
        envCiJson = envCiJson.replace("\"verify\":\"true\"", "\"verify\":\"false\"");

        RequestBody body = RequestBody.create(JSON, envCiJson);

        String url = adapterBaseUrl + "/adapter/rest/cm/simple/cis/" + env.getId();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Cms-User", userName)
                .addHeader("Content-Type", "application/json")
                .put(body)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        log.info("OO response body: " + responseBody + ", code: " + responseCode);
        if (! response.isSuccessful()) {
            throw new OneOpsException("Error while disabling verify. Response from OneOps: " + responseBody
                    + " ResponseCode : " + responseCode);
        }
        return responseCode;
    }

    public String getCiJson(long ciId) throws IOException {
        String url = adapterBaseUrl + "/adapter/rest/cm/simple/cis/" + ciId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        if (! response.isSuccessful()) {
            throw new RuntimeException("Error in getCi api. Response from OneOps: " + responseBody
                    + " ResponseCode : " + responseCode);
        }
        return responseBody;
    }
}

