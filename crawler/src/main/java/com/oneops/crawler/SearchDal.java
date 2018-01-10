package com.oneops.crawler;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class SearchDal {
    private String esHost ;
    private final Logger log = LoggerFactory.getLogger(getClass());
    Gson gson = null;

    public SearchDal() {
        gson = new GsonBuilder().create();
        readConfig();
    }

    public void setCmsApiHost(String esHost) {
        this.esHost = esHost;
    }

    void readConfig() {
        esHost = System.getProperty("es.host");
    }

    public void createIndex(String indexName, String indexJson) {
        if (esHost == null) {
            log.warn("ES host not set as system property");
            return;
        }
        log.info("checking if the search index exists for name: " + indexName);
        int responseCode = HttpRequest.get("http://" + esHost + ":9200/" + indexName).code();
        if (responseCode != 404) {
            log.info("index already setup");
            return;
        }
        log.info("Creating search index with name: " + indexName);
        String response = HttpRequest.put("http://" + esHost
                + ":9200/" + indexName)
                .contentType("application/json").send(indexJson)
                        .body();
    }

    public void push(String indexName, String type, Serializable object, String id) {
        if (esHost == null) {
            log.warn("ES host not set as system property");
            return;
        }
        String response = HttpRequest.put("http://" + esHost
                + ":9200/" + indexName + "/" + type + "/" + id)
                .contentType("application/json").send(gson.toJson(object))
                .body();
    }

    public Object get(String indexName, String type, Serializable object, String id) {
        if (esHost == null) {
            log.warn("ES host not set as system property");
            return null;
        }
        String response = HttpRequest.get("http://" + esHost
                + ":9200/" + indexName + "/" + type + "/" + id + "/_source")
                .contentType("application/json").body();
        return gson.fromJson(response, object.getClass());
    }
}
