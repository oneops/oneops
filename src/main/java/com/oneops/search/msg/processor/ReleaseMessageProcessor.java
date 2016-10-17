package com.oneops.search.msg.processor;

import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.search.msg.index.Indexer;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private Indexer indexer;


    private static final String RELEASE = "release";

    @Override
    public void processMessage(String message, String msgType, String msgId) {
        CmsRelease release = GSON.fromJson(message, CmsRelease.class);
        String releaseMsg = GSON_ES.toJson(release);
        indexer.indexEvent(RELEASE, releaseMsg);
        indexer.index(String.valueOf(release.getReleaseId()), RELEASE, releaseMsg);
    }
}
