package com.oneops.search.msg.processor;

import com.google.gson.JsonParser;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.search.msg.index.Indexer;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

/*******************************************************************************
 *
 *   Copyright 2016 Walmart, Inc.
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

@Service
public class ReleaseMessageProcessor implements MessageProcessor {
    private static final String SNAPSHOT = "snapshot";
    private static Logger logger = Logger.getLogger(ReleaseMessageProcessor.class);
    private final Indexer indexer;
    private final Client client;
    @Value("${designSnapshotURL}")
    private String designSnapshotURL;
    @Value("${manifestSnapshotURL}")
    private String manifestSnapshotURL;
    private static final String RELEASE = "release";

    @Autowired
    public ReleaseMessageProcessor(Indexer indexer, Client client) {
        this.indexer = indexer;
        this.client = client;
    }

    @Override
    public void processMessage(String message, String msgType, String msgId) {
        CmsRelease release = GSON.fromJson(message, CmsRelease.class);
        String releaseMsg = GSON_ES.toJson(release);
        indexer.indexEvent(RELEASE, releaseMsg);
        indexer.index(String.valueOf(release.getReleaseId()), RELEASE, releaseMsg);
        indexSnapshot(release);
    }

    private void indexSnapshot(CmsRelease release) {


        try {
            if (!"closed".equalsIgnoreCase(release.getReleaseState())) {
                logger.info("Release is not closed. Won't do snapshot");
                return;
            }
            if (release.getNsPath().endsWith("bom")) return; // ignore bom release


            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.WEEK_OF_YEAR, -2);
            long snapshotTimestamp = cal.getTime().getTime();
            long hits = client.prepareSearch("cms-201*")
                    .setSearchType(SearchType.COUNT)
                    .setTypes(SNAPSHOT)
                    .setQuery(boolQuery()
                            .must(QueryBuilders.rangeQuery("timestamp").from(snapshotTimestamp))
                            .must(QueryBuilders.termQuery("namespace.keyword", release.getNsPath()))
                    )
                    .execute().actionGet().getHits().getTotalHits();
            logger.info("hits:" + hits);
            if (hits > 0) {
                logger.info("there was a snapshot done recently, so won'd do another one. ");
                return;
            }

            String url = (release.getNsPath().endsWith("manifest") ? manifestSnapshotURL : designSnapshotURL) + release.getNsPath();  // set snapshot URL based on release type
            logger.info("Retrieving snapshot for:" + url + " expected release:" + release.getReleaseId());
            String message = Request.Get(url).addHeader("Content-Type", "application/json").execute().returnContent().asString();
            long releaseId = new JsonParser().parse(message).getAsJsonObject().get("release").getAsLong();
            if (releaseId > release.getReleaseId()) {
                logger.warn("Snapshot is dirty, so discarding. Was expecting release:" + release.getReleaseId() + " snapshot has rfcs from release:" + releaseId);
            } else {
                indexer.index(String.valueOf(release.getReleaseId()), SNAPSHOT, message);
                logger.info("Snapshot indexed for release ID: " + release.getReleaseId());
            }

        } catch (Exception e) {
            logger.error("Error while retrieving snapshot" + e.getMessage(), e);
        }

    }
}
