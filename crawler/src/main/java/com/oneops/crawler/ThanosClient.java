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
package com.oneops.crawler;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.squareup.okhttp.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class ThanosClient {
    public static final String STATUS_EXECUTED = "executed";
    private String baseUrl ;
    private OkHttpClient client;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Gson gson;
    private String authToken;

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public ThanosClient() {
        readConfig();
        gson = new Gson();
        client = new OkHttpClient();
    }

    public void setBaseUrl(String transistorBaseUrl) {
        this.baseUrl = transistorBaseUrl;
    }

    void readConfig() {
        baseUrl = System.getProperty("thanos.base.url");
    }

    public ArrayList<CloudResourcesUtilizationStats> getStats(String path) throws IOException {
        String url = baseUrl + "?nspath=" + path;
        log.info("Calling thanos api: " + url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        log.info("Thanos api response code: " + responseCode);
        if (responseCode >= 300) {
            throw new RuntimeException("Error while calling Thanos api. Response from Thanos: " + responseBody
                    + " ResponseCode : " + responseCode);
        }

        ArrayList<CloudResourcesUtilizationStats> cloudResourcesUtilizationStats = new ArrayList<>();
        if (StringUtils.isEmpty(responseBody)) {
            return cloudResourcesUtilizationStats;
        }
        log.info("response body: " + responseBody);
        ThanosServerResponse thanosServerResponse = gson.fromJson(responseBody, ThanosServerResponse.class);
        return thanosServerResponse.getCloudResourcesUtilizationStats();
    }

    public void updateStatus(String path, String status) throws IOException {
        HashMap payload = new HashMap();
        payload.put("nspath", path);
        payload.put("status", status);

        RequestBody body = RequestBody.create(JSON, gson.toJson(payload));

        Request request = new Request.Builder()
                .url(baseUrl)
                .addHeader("Authorization", "Bearer " + getAuthToken())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        log.info("Thanos response body: " + responseBody + ", code: " + responseCode);
        if (responseCode >= 300) {
            throw new RuntimeException("Error while updating status on thanos. Response: " + responseBody
                    + " ResponseCode : " + responseCode);
        }
    }

    public String getAuthToken() {
        return authToken;
    }

    public void configure(Properties props) {
        authToken = props.getProperty("thanos.auth.token");
    }

    public static class ThanosServerResponse implements Serializable {
        @SerializedName("server-response")
        ArrayList<CloudResourcesUtilizationStats> cloudResourcesUtilizationStats;

        public ArrayList<CloudResourcesUtilizationStats> getCloudResourcesUtilizationStats() {
            return cloudResourcesUtilizationStats;
        }

        public void setCloudResourcesUtilizationStats(ArrayList<CloudResourcesUtilizationStats> cloudResourcesUtilizationStats) {
            this.cloudResourcesUtilizationStats = cloudResourcesUtilizationStats;
        }
    }

    public static class CloudResourcesUtilizationStats implements Serializable {
        @SerializedName("cloud_name")
        String cloudName;

        @SerializedName("reclaim_vms")
        int reclaimVms;

        @SerializedName("reclaim_cores")
        int reclaimCores;

        @SerializedName("min_cluster_size")
        int minClusterSize;

        @SerializedName("cloud_vms")
        int Vms;

        @SerializedName("reclaim_status")
        String reclaimStatus;

        String reclaim;

        public String getCloudName() {
            return cloudName;
        }

        public void setCloudName(String cloudName) {
            this.cloudName = cloudName;
        }

        public int getReclaimVms() {
            return reclaimVms;
        }

        public void setReclaimVms(int reclaimVms) {
            this.reclaimVms = reclaimVms;
        }

        public int getMinClusterSize() {
            return minClusterSize;
        }

        public void setMinClusterSize(int minClusterSize) {
            this.minClusterSize = minClusterSize;
        }

        public int getReclaimCores() {
            return reclaimCores;
        }

        public void setReclaimCores(int reclaimCores) {
            this.reclaimCores = reclaimCores;
        }

        public int getVms() {
            return Vms;
        }

        public void setVms(int vms) {
            Vms = vms;
        }

        public String getReclaimStatus() {
            return reclaimStatus;
        }

        public void setReclaimStatus(String reclaimStatus) {
            this.reclaimStatus = reclaimStatus;
        }

        public String getReclaim() {
            return reclaim;
        }

        public void setReclaim(String reclaim) {
            this.reclaim = reclaim;
        }

        public boolean shouldReclaim() {
            return "yes".equalsIgnoreCase(reclaim)?true:false;
        }
    }
}

