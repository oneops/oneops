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
package com.oneops.tekton;

import com.google.gson.Gson;
import okhttp3.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TektonClient {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private Gson gson = new Gson();
    static Logger logger = Logger.getLogger(TektonClient.class);
    private String tektonBaseUrl = System.getProperty("tekton.base.url", "http://localhost:9000");

    public void reserveQuota(Map<String, Map<String, Integer>> quotaNeeded, String reservationId, String entity,
                              String createdBy) throws IOException {
        for (String provider : quotaNeeded.keySet()) {
            Map<String, Integer> resources = quotaNeeded.get(provider);
            for (String resource : resources.keySet()) {
                int resourceNumber = resources.get(resource);
                HashMap<String, Integer> reservation = new HashMap<>();
                reservation.put(resource, resourceNumber);
                RequestBody body = RequestBody.create(JSON, gson.toJson(reservation));

                String url = tektonBaseUrl + "/api/quota/reservation?provider=" + provider + "&entity=" + entity
                        + "&reservationId=" + reservationId + "&createdBy=" + createdBy;
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                OkHttpClient client = new OkHttpClient();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                int responseCode = response.code();
                logger.info("Tekton api response body: " + responseBody + ", code: " + responseCode);
                if (responseCode >= 300) {
                    throw new RuntimeException("Error while reserving quota. Response from Tekton: " + responseBody
                                    + " ResponseCode : " + responseCode);
                }
            }
        }
    }

}
