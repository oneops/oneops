package com.oneops.search.msg.processor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.util.CmsConstants;
import com.oneops.search.msg.index.Indexer;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private static Logger logger = Logger.getLogger(ReleaseMessageProcessor.class);
    @Autowired
    private Indexer indexer;
    @Value("${designSnapshotURL}")
    private String designSnapshotURL;
    @Value("${manifestSnapshotURL}")
    private String manifestSnapshotURL;
    private static final String RELEASE = "release";

    @Override
    public void processMessage(String message, String msgType, String msgId) {
        CmsRelease release = GSON.fromJson(message, CmsRelease.class);
        String releaseMsg = GSON_ES.toJson(release);
        indexer.indexEvent(RELEASE, releaseMsg);
        indexer.index(String.valueOf(release.getReleaseId()), RELEASE, releaseMsg);
        try {
            if ("closed".equalsIgnoreCase(release.getReleaseState())) {
                if (release.getNsPath().endsWith("bom")) return; // ignore bom release
                
                String url = (release.getNsPath().endsWith("manifest")?manifestSnapshotURL:designSnapshotURL) + release.getNsPath();  // set snapshot URL based on release type
                
                logger.info("Retrieving snapshot for:" + url + " expected release:" + release.getReleaseId());
                message = Request.Get(url).addHeader("Content-Type", "application/json").execute().returnContent().asString();
                long releaseId = new JsonParser().parse(message).getAsJsonObject().get("release").getAsLong();
                if (releaseId > release.getReleaseId()) {
                    logger.warn("Snapshot is dirty, so discarding. Was expecting release:" + release.getReleaseId() + " snapshot has rfcs from release:" + releaseId);
                } else {
                    indexer.index(String.valueOf(release.getReleaseId()), "snapshot", message);
                    logger.info("Snapshot indexed for release ID: " + release.getReleaseId());
                }
            } else {
                logger.info("Release is not closed. Won't do snapshot");
            }
        } catch (Exception e) {
            logger.error("Error while retrieving snapshot" + e.getMessage(), e);
        }
    }
}
