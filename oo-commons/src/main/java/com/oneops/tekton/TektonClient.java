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
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class TektonClient {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private Gson gson = new Gson();
    private static Logger logger = Logger.getLogger(TektonClient.class);
    private String tektonBaseUrl = System.getProperty("tekton.base.url", "http://localhost:9000");
    private String authHeader = Base64.getEncoder().encodeToString(System.getProperty("tekton.auth.token").getBytes());

    public void reserveQuota(Map<String, Map<String, Integer>> quotaNeeded, long deploymentId, String entity,
                             String createdBy) throws IOException {
        Set<String> reservedSubscriptions = new HashSet<>();
        try {
            for (String subscriptionId : quotaNeeded.keySet()) {
                Map<String, Integer> reservation = quotaNeeded.get(subscriptionId);
                RequestBody body = RequestBody.create(JSON, gson.toJson(reservation));

                String reservationId = composeReservationId(deploymentId, subscriptionId);
                String url = tektonBaseUrl + "/api/v1/quota/reservation?subscription=" + subscriptionId + "&entity=" + entity
                        + "&reservationId=" + reservationId + "&createdBy=" + createdBy;
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", authHeader)
                        .post(body)
                        .build();

                OkHttpClient client = new OkHttpClient();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                int responseCode = response.code();

                if (responseCode >= 200 && responseCode < 300 && responseBody != null && responseBody.length() > 0) {
                    Map<String, String> content = gson.fromJson(responseBody, (new TypeToken<HashMap<String, Object>>() {}).getType());
                    String status = content.get("status");
                    if (status.equalsIgnoreCase("ok")) {
                        logger.info("Successfully reserved capacity " + reservation + " for reservationId: " + reservationId + ", entity: " + entity);
                        reservedSubscriptions.add(subscriptionId);
                    }
                    else if(status.equalsIgnoreCase("quota_not_setup")) {
                        logger.info("Quota not set up for subscription: " + subscriptionId + ", entity: " + entity);
                    }
                    else {
                        logger.info("Not enough capacity to reserve " + reservation + " for reservationId: " + reservationId +
                                            ", entity: " + entity + ". Response: " + responseBody);
                        throw new ReservationException(subscriptionId + ": " + content.get("message"));
                    }
                }
                else {
                    String message = "Error to reserve capacity " + reservation + " for reservationId: " + reservationId +
                            ", entity: " + entity + ". Response: " + responseBody + ". Response code: " + responseCode;
                    logger.error(message);
                    throw new ReservationException(message);
                }
            }
        } catch (Exception e) {
            // Try delete successful reservations.
            if (reservedSubscriptions.size() > 0) {
                deleteReservations(deploymentId, reservedSubscriptions);
            }
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public int commitReservation(Map<String, Integer> resourceNumbers, long deploymentId, String subscriptinoId) throws IOException {
        String reservationId = composeReservationId(deploymentId, subscriptinoId);
        String url = tektonBaseUrl + "/api/v1/quota/reservation/" + reservationId + "/commit";
        RequestBody body = RequestBody.create(JSON, gson.toJson(resourceNumbers));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", authHeader)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        logger.info("Commit quota for reservationId: " + reservationId + " with resources: " + resourceNumbers
                            + " Tekton api response body: " + responseBody + ", code: " + responseCode);

        if (responseCode >= 300 && responseCode != 404) {
            throw new RuntimeException("Error while committing reservation. Response from Tekton: " + responseBody
                                               + " ResponseCode : " + responseCode + " for reservationId: " + reservationId);
        }
        return responseCode;
    }

    public int rollbackReservation(Map<String, Integer> resourceNumbers, long deploymentId, String subscriptinoId) throws IOException {
        String reservationId = composeReservationId(deploymentId, subscriptinoId);
        String url = tektonBaseUrl + "/api/v1/quota/reservation/" + reservationId + "/rollback";
        RequestBody body = RequestBody.create(JSON, gson.toJson(resourceNumbers));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", authHeader)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();

        logger.info("Rollback quota for reservationId: " + reservationId + " with resources: " + resourceNumbers
                            + " Tekton api response body: " + responseBody + ", code: " + responseCode);

        if (responseCode >= 300 && responseCode != 404) {
            throw new RuntimeException("Error in rollback for reservation. Response from Tekton: " + responseBody
                                               + " ResponseCode : " + responseCode + " for reservationId: " + reservationId);
        }

        return responseCode;
    }

    public int releaseResources(String entity, String subscriptionId, Map<String, Integer> resourceNumbers) throws IOException {
        String url = tektonBaseUrl + "/api/v1/quota/release/" + subscriptionId + "/" + entity;
        RequestBody body = RequestBody.create(JSON, gson.toJson(resourceNumbers));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", authHeader)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        logger.info("Release resources for entity : " + entity + " for resources: " + resourceNumbers
                            + " Tekton api response body: " + responseBody + ", code: " + responseCode);

        if (responseCode >= 300 && responseCode != 404) {
            throw new RuntimeException("Error while releasing resources for soft quota. Response from Tekton: " + responseBody
                                               + " ResponseCode : " + responseCode + " for entity: " + entity);
        }

        return responseCode;
    }

    public void deleteReservations(long deploymentId, Set<String> subscriptionIds) throws IOException {
        OkHttpClient client = new OkHttpClient();
        for (String subscriptionId : subscriptionIds) {
            String reservationId = composeReservationId(deploymentId, subscriptionId);
            String url = tektonBaseUrl + "/api/v1/quota/reservation/" + reservationId;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", authHeader)
                    .delete()
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            int responseCode = response.code();

            if (responseCode >= 200 && responseCode < 300 && responseBody != null && responseBody.length() > 0) {
                Map<String, String> content = gson.fromJson(responseBody, (new TypeToken<HashMap<String, Object>>() {}).getType());
                String status = content.get("status");
                if (status.equalsIgnoreCase("ok")) {
                    logger.info("Successfully deleted reservation for reservationId: " + reservationId);
                }
                else if(status.equalsIgnoreCase("not_found")) {
                    logger.info("Not found reservation to delete for reservationId:" + reservationId);
                }
                else {
                    logger.info("Failed to delete reservation for reservationId:" + reservationId + ". Response: " + responseBody);
                }
            }
            else {
                String message = "Error to delete reservation for reservationId: " + reservationId +
                        ". Response: " + responseBody + ". Response code: " + responseCode;
                logger.error(message);
                throw new RuntimeException(message);
            }
        }
    }

    private String composeReservationId(long deploymentId, String subsciptionId) {
        return deploymentId + ":" + subsciptionId;
    }

    class ReservationException extends Exception {
        public ReservationException(String message) {
            super(message);
        }
    }
}
