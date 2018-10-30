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
package com.oneops.capacity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class TektonClient {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static Logger logger = Logger.getLogger(TektonClient.class);

    private Gson gson = new Gson();
    private String tektonBaseUrl = System.getProperty("tekton.base.url", "http://localhost:9000");
    private boolean strictReservation = !System.getProperty("tekton.reservation.strict", "true").equalsIgnoreCase("false");
    private String authHeader = Base64.getEncoder().encodeToString(System.getProperty("tekton.auth.token", "").getBytes());

    public Map<String, String> precheckReservation(Map<String, Map<String, Integer>> capacity, String nsPath, String createdBy) {
        List<Map<String, Object>> reservations = buildReservationRequest(capacity, nsPath, createdBy);

        int responseCode = 0;
        String responseBody = null;
        try {
            RequestBody body = RequestBody.create(JSON, gson.toJson(reservations));
            Request request = new Request.Builder()
                    .url(tektonBaseUrl + (strictReservation ? "/api/v2/quota/precheck" : "/api/v1/quota/precheck"))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", authHeader)
                    .post(body)
                    .build();

            Response response = new OkHttpClient().newCall(request).execute();
            responseCode = response.code();
            responseBody = response.body().string();
        } catch (Exception e) {
            logger.warn(nsPath + " - Error to precheck capacity: " + reservations +
                                 ". HTTP response code: " + (responseCode > 0 ? responseCode : "N/A") +
                                 ". Response: " + (responseBody == null ? "N/A" : responseBody), e);
        }

        if (responseCode >= 200 && responseCode < 300 && responseBody != null && !responseBody.isEmpty()) {
            ReservationBulkResponse reservationsResponse = gson.fromJson(responseBody, ReservationBulkResponse.class);
            String status = reservationsResponse.getStatus();
            if (status.equalsIgnoreCase("ok")) {
                logger.info(nsPath + "- Successfully prechecked capacity " + reservations);
                return new HashMap<>();
            } else if (status.equalsIgnoreCase("subscription_not_setup")) {
                logger.info(nsPath + " - Subscription is not set up to enformce capacity pre-check: " + reservations);
                return new HashMap<>();
            } else if (status.equalsIgnoreCase("quota_not_setup")) {
                // This should not happen when using V2 (strict quota enforcement).
                logger.info(nsPath + " - No quotas are set up to pre-check capacity: " + reservations);
                return new HashMap<>();
            } else if (status.equalsIgnoreCase("not_enough_capacity")) {
                logger.info(nsPath + " - No enough capacity in pre-checking capacity: " + reservations);
                return reservationsResponse.getReservations().stream()
                        .filter(r -> r.getStatus().equalsIgnoreCase("not_enough_capacity"))
                        .collect(toMap(r -> getSubscriptionIdFromReservationId(r.getExtReservationId()), r -> r.getMessage()));
            } else {
                logger.error(nsPath + " - Failed to pre-check: " + reservations + ". Response: " + responseBody);
            }
        } else {
            logger.error(nsPath + " - Error to pre-check: " + reservations + ". HTTP response code: " + responseCode + ". Response: " + responseBody);
        }
        return null;
    }

    public void reserveQuota(Map<String, Map<String, Integer>> capacity, String nsPath, String createdBy) throws ReservationException {
        List<Map<String, Object>> reservations = buildReservationRequest(capacity, nsPath, createdBy);

        int responseCode = 0;
        String responseBody = null;
        try {
            RequestBody body = RequestBody.create(JSON, gson.toJson(reservations));
            Request request = new Request.Builder()
                    .url(tektonBaseUrl + (strictReservation ? "/api/v2/quota/reserve" : "/api/v1/quota/bulkReservation/"))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", authHeader)
                    .post(body)
                    .build();

            Response response = new OkHttpClient().newCall(request).execute();
            responseBody = response.body().string();
            responseCode = response.code();
        } catch (Exception e) {
            logger.error(nsPath + " - Error to reserve capacity: " + reservations +
                                 ". HTTP response code: " + (responseCode > 0 ? responseCode : "N/A") +
                                 ". Response: " + (responseBody == null ? "N/A" : responseBody), e);
            throw new RuntimeException("Failed to reserve capacity: " + e.getMessage());
        }

        if (responseCode >= 200 && responseCode < 300 && responseBody != null && !responseBody.isEmpty()) {
            ReservationBulkResponse reservationsResponse = gson.fromJson(responseBody, ReservationBulkResponse.class);
            String status = reservationsResponse.getStatus();
            if (status.equalsIgnoreCase("ok")) {
                logger.info(nsPath + "- Successfully reserved capacity " + reservations);
            } else if (status.equalsIgnoreCase("subscription_not_setup")) {
                logger.info(nsPath + " - Subscription is not set up to enforce capacity reservation: " + reservations);
            } else if (status.equalsIgnoreCase("quota_not_setup")) {
                // This should not happen when using V2 (strict quota enforcement).
                logger.info(nsPath + " - No quotas are set up to reserve: " + reservations);
            } else if (status.equalsIgnoreCase("not_enough_capacity")) {
                logger.info(nsPath + " - No enough capacity to reserve: " + reservations);
                throw new ReservationException(reservationsResponse.getReservations().stream()
                                                       .filter(r -> r.getStatus().equalsIgnoreCase("not_enough_capacity"))
                                                       .collect(toMap(r -> getSubscriptionIdFromReservationId(r.getExtReservationId()), r -> r.getMessage())));
            } else {
                logger.error(nsPath + " - Failed to reserve: " + reservations + ". Response: " + responseBody);
                throw new ReservationException("Can not reserve capacity, status: " + status);
            }
        } else {
            logger.error(nsPath + " - Error to reserve: " + reservations + ". HTTP response code: " + responseCode + ". Response: " + responseBody);
            throw new RuntimeException("Failed to reserve capacity. HTTP response code: " + responseCode);
        }
    }

    public void commitReservation(Map<String, Integer> capacity, String nsPath, String subscriptinoId) {
        String reservationId = composeReservationId(nsPath, subscriptinoId);
        int responseCode = 0;
        String responseBody = null;
        try {
            RequestBody body = RequestBody.create(JSON, gson.toJson(capacity));
            Request request = new Request.Builder()
                    .url(tektonBaseUrl + "/api/v1/quota/reservation/" + URLEncoder.encode(reservationId, Charset.defaultCharset().name()) + "/commit")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", authHeader)
                    .post(body)
                    .build();

            Response response = new OkHttpClient().newCall(request).execute();
            responseBody = response.body().string();
            responseCode = response.code();
        } catch (Exception e) {
            logger.error("Error to commit " + capacity + " for " + reservationId +
                                 ". HTTP response code: " + (responseCode > 0 ? responseCode : "N/A") +
                                 ". Response: " + (responseBody == null ? "N/A" : responseBody), e);
            throw new RuntimeException("Failed to commit capacity: " + e.getMessage());
        }

        if (responseCode >= 200 && responseCode < 300) {
            logger.info("Successfully committed " + capacity + " for " + reservationId);
        } else {
            logger.info("Failed to commit " + capacity + " for " + reservationId +
                                ". HTTP response code: " + responseCode + ". Response: " + responseBody);
            throw new RuntimeException("Failed to commit capacity. HTTP response code: " + responseCode);
        }
    }

    public void releaseResources(Map<String, Integer> capacity, String nsPath, String subscriptionId) {
        String subscriptionAndEntity = subscriptionId + "/" + nsPath.split("/")[1];
        int responseCode = 0;
        String responseBody = null;
        try {
            RequestBody body = RequestBody.create(JSON, gson.toJson(capacity));
            Request request = new Request.Builder()
                    .url(tektonBaseUrl + "/api/v1/quota/release/" + subscriptionAndEntity)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", authHeader)
                    .post(body)
                    .build();

            Response response = new OkHttpClient().newCall(request).execute();
            responseBody = response.body().string();
            responseCode = response.code();
        } catch (Exception e) {
            logger.error("Error to release " + capacity + " for " + subscriptionAndEntity +
                                 ". HTTP response code: " + (responseCode > 0 ? responseCode : "N/A") +
                                 ". Response: " + (responseBody == null ? "N/A" : responseBody), e);
            throw new RuntimeException("Failed to release capacity: " + e.getMessage());
        }

        if (responseCode >= 200 && responseCode < 300) {
            logger.info("Successfully released " + capacity + " for " + subscriptionAndEntity);
        } else {
            logger.info("Failed to release " + capacity + " for " + subscriptionAndEntity +
                                ". HTTP response code: " + responseCode + ". Response: " + responseBody);
            throw new RuntimeException("Failed to release capacity. HTTP response code: " + responseCode);
        }
    }

    public void deleteReservations(String nsPath, Set<String> subscriptionIds) {
        OkHttpClient client = new OkHttpClient();
        List<String> reservationIds = subscriptionIds.stream()
                .map(s -> composeReservationId(nsPath, s))
                .collect(toList());
        int responseCode = 0;
        String responseBody = null;
        try {
            RequestBody body = RequestBody.create(JSON, gson.toJson(reservationIds));
            Request request = new Request.Builder()
                    .url(tektonBaseUrl + "/api/v1/quota/reservation")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", authHeader)
                    .delete(body)
                    .build();

            Response response = client.newCall(request).execute();
            responseBody = response.body().string();
            responseCode = response.code();
        } catch (Exception e) {
            logger.error("Error to delete reservations: " + reservationIds +
                                 ". HTTP response code: " + (responseCode > 0 ? responseCode : "N/A") +
                                 ". Response: " + (responseBody == null ? "N/A" : responseBody), e);
            throw new RuntimeException("Failed to delete reservation: " + e.getMessage());
        }

        if (responseCode >= 200 && responseCode < 300 && responseBody != null && responseBody.length() > 0) {
            Map<String, String> content = gson.fromJson(responseBody, (new TypeToken<HashMap<String, Object>>() {}).getType());
            String status = content.get("status");
            if (status.equalsIgnoreCase("ok")) {
                logger.info("Successfully deleted reservation for reservations: " + reservationIds);
            } else if (status.equalsIgnoreCase("not_found")) {
                logger.info("Some reservations in " + reservationIds + " are not found to be deleted: " + content.get("message"));
            } else {
                logger.info("Failed to delete reservations:" + reservationIds + ". Response: " + responseBody);
            }
        } else {
            logger.error("Error to delete reservations: " + reservationIds +
                    ". Response code: " + responseCode + ". Response: " + responseBody);
            throw new RuntimeException("Error to delete reservations. HTTP response code: " + responseCode);
        }
    }

    private List<Map<String, Object>> buildReservationRequest(Map<String, Map<String, Integer>> capacity, String nsPath, String createdBy) {
        String entity = nsPath.split("/")[1];
        return capacity.entrySet().stream()
                .map(e -> {
                    String subscriptionId = e.getKey();
                    Map<String, Object> reservation = new HashMap<>();
                    reservation.put("entity", entity);
                    reservation.put("subscription", subscriptionId);
                    reservation.put("extReservationId", composeReservationId(nsPath, subscriptionId));
                    reservation.put("createdBy", createdBy);
                    reservation.put("quota", e.getValue());
                    return reservation;
                })
                .collect(toList());
    }

    private String composeReservationId(String nsPath, String subsciptionId) {
        return nsPath + ":" + subsciptionId;
    }

    private String getSubscriptionIdFromReservationId(String reservationId) {
        return reservationId.substring(reservationId.indexOf(":") + 1);
    }

    private class ReservationBulkResponse {
        private List<ReservationResponse> reservations;
        private String status;
        private String message;

        List<ReservationResponse> getReservations() {
            return reservations;
        }

        String getStatus() {
            return status;
        }

        String getMessage() {
            return message;
        }
    }

    private class ReservationResponse {
        private String extReservationId;
        private String status;
        private String message;

        String getExtReservationId() {
            return extReservationId;
        }

        String getStatus() {
            return status;
        }

        String getMessage() {
            return message;
        }
    }
}
