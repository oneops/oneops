package com.oneops.crawler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchDal {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private String esHost;
    private String esPort;
    private final Logger log = LoggerFactory.getLogger(getClass());
    Gson gson = null;
    private OkHttpClient client;


    public SearchDal() {
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
        client = new OkHttpClient();
        readConfig();
    }

    public void setCmsApiHost(String esHost) {
        this.esHost = esHost;
    }

    void readConfig() {
        esHost = System.getProperty("es.host");
        esPort = System.getProperty("es.port", "9200");
    }

    public int createIndex(String indexName, String indexJson) throws IOException {
        if (esHost == null) {
            log.warn("ES host not set as system property, can not create index: " + indexName);
            return 400;
        }

        log.info("checking if the search index exists for name: " + indexName);

        String url = "http://" + esHost + ":" + esPort + "/" + indexName;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        log.info("ES response body: " + responseBody + ", code: " + responseCode);
        if (responseCode != 404) {
            log.info("index already setup");
            return 200;
        }

        log.info("Creating search index with name: " + indexName);

        RequestBody body = RequestBody.create(JSON, indexJson);

        request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        response = client.newCall(request).execute();
        responseBody = response.body().string();
        responseCode = response.code();
        log.info("OO response body: " + responseBody + ", code: " + responseCode);
        return responseCode;
    }

    public int put(String indexName, String type, Serializable object, String id) throws IOException {
        String url = "http://" + esHost + ":" + esPort + "/" + indexName + "/" + type + "/" + id;
        if (esHost == null) {
            log.warn("ES host not set as system property");
            return 400;
        }

        RequestBody body = RequestBody.create(JSON, gson.toJson(object));
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        log.info("ES response body for PUT: " + responseBody + ", code: " + responseCode);

        if (responseCode >= 300) {
            log.error("Error for url : " + url + " body: " + gson.toJson(object));
            throw new IOException("Error code returned by ES: " + responseCode + " with response body: " + responseBody);
        }

        return responseCode;
    }

    public int post(String indexName, String type, Serializable object) throws IOException {
        String url = "http://" + esHost + ":" + esPort + "/" + indexName + "/" + type;
        if (esHost == null) {
            log.warn("ES host not set as system property");
            return 400;
        }

        RequestBody body = RequestBody.create(JSON, gson.toJson(object));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        if (responseCode >= 300) {
            log.error("Error for url : " + url + " body: " + gson.toJson(object));
            throw new IOException("Error code returned by ES: " + responseCode + " with response body: " + responseBody);
        }

        log.info("ES response body for post: " + responseBody + ", code: " + responseCode);
        return responseCode;
    }

    public Object get(String indexName, String type, Serializable object, String id) throws IOException {
        if (esHost == null) {
            log.warn("ES host not set as system property");
            return null;
        }
        String url = "http://" + esHost + ":" + esPort + "/" + indexName + "/" + type + "/" + id + "/_source";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        log.info("OO response code: " + responseCode);
        return gson.fromJson(responseBody, object.getClass());
    }

    public List<ESRecord> search(String indexName, String type, Serializable object, String queryJson) throws IOException {
        if (esHost == null) {
            log.warn("ES host not set as system property");
            return null;
        }
        String url = "http://" + esHost + ":" + esPort + "/" + indexName + "/" + type + "/_search?filter_path=hits.hits";
        RequestBody body = RequestBody.create(JSON, queryJson);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        log.info("response body from ES: " + responseBody);
        if (responseCode >= 300) {
            log.error("Error for url : " + url + " body: " + queryJson);
            throw new IOException("Error code returned by ES: " + responseCode + " with response body: " + responseBody);
        }
        if (responseBody.equalsIgnoreCase("{}")) {
            return null;
        }
        Map<String,Object> result = gson.fromJson(responseBody, Map.class);
        Map<String,Object> hits = (Map<String, Object>) result.get("hits");
        ArrayList<Object> hits1 = (ArrayList<Object>) hits.get("hits");
        List<ESRecord> results = new ArrayList<>();
        for (Object hit : hits1) {
            Map<String, Object> document = (Map<String, Object>) hit;
            ESRecord esRecord = new ESRecord();
            esRecord.setSource(gson.fromJson(gson.toJson(document.get("_source")), object.getClass()));
            String id = (String) document.get("_id");
            log.info("setting id: " + id);
            esRecord.setId(id);
            results.add(esRecord);
        }
        return results;
    }

    public void flush(String index) throws IOException {
        String url = "http://" + esHost + ":" + esPort + "/" + index + "/_flush";
        RequestBody body = RequestBody.create(JSON, "{}");
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        int responseCode = response.code();
        log.info("response body from ES: " + responseBody);
        if (responseCode >= 300) {
            throw new IOException("Error code returned by ES for flush api: " + responseCode + " with response body: " + responseBody);
        }
    }

    public String getEsHost() {
        return esHost;
    }

    public void setEsHost(String esHost) {
        this.esHost = esHost;
    }

    public String getEsPort() {
        return esPort;
    }

    public void setEsPort(String esPort) {
        this.esPort = esPort;
    }
}
