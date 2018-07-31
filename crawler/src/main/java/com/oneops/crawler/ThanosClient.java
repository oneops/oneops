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
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ThanosClient {
    private String baseUrl ;
    private OkHttpClient client;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Gson gson;

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
        if (StringUtils.isEmpty(baseUrl)) {
            throw new RuntimeException("System property not found: thanos.base.url ");
        }
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
        ArrayList<CloudResourcesUtilizationStats> cloudResourcesUtilizationStats = new ArrayList<>();
        Type listType = new TypeToken<ArrayList<CloudResourcesUtilizationStats>>(){}.getType();
        return gson.fromJson(responseBody, listType);
    }

    public static class CloudResourcesUtilizationStats implements Serializable {
        String cloud_name;
        int reclaim_vms;
        String org;
        String assembly;
        String environment;
        String platform;
        String envtype;
        int Vms;
        String nsPath;
        String pack;

        public String getNsPath() {
            return nsPath;
        }

        public void setNsPath(String nsPath) {
            this.nsPath = nsPath;
        }

        public String getPack() {
            return pack;
        }

        public void setPack(String pack) {
            this.pack = pack;
        }

        public String getCloud_name() {
            return cloud_name;
        }

        public void setCloud_name(String cloud_name) {
            this.cloud_name = cloud_name;
        }

        public int getReclaim_vms() {
            return reclaim_vms;
        }

        public void setReclaim_vms(int reclaim_vms) {
            this.reclaim_vms = reclaim_vms;
        }

        public String getOrg() {
            return org;
        }

        public void setOrg(String org) {
            this.org = org;
        }

        public String getAssembly() {
            return assembly;
        }

        public void setAssembly(String assembly) {
            this.assembly = assembly;
        }

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getEnvtype() {
            return envtype;
        }

        public void setEnvtype(String envtype) {
            this.envtype = envtype;
        }

        public int getVms() {
            return Vms;
        }

        public void setVms(int vms) {
            this.Vms = vms;
        }
    }
}

