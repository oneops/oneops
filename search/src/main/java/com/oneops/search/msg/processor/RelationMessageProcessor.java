package com.oneops.search.msg.processor; /*******************************************************************************
 *
 *   Copyright 2015 Walmart, Inc.
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

import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.simple.domain.CmsCIRelationSimple;
import com.oneops.cms.util.CmsUtil;
import com.oneops.search.domain.CmsCISearch;
import com.oneops.search.msg.index.Indexer;
import org.apache.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RelationMessageProcessor implements MessageProcessor {
    private Logger logger = Logger.getLogger(this.getClass());
    @Autowired
    private CmsUtil cmsUtil;
    @Autowired
    private Indexer indexer;
    @Autowired
    private Client client;

    @Override
    public void processMessage(String message, String msgType, String msgId) {
        try {
            Thread.sleep(3000);//wait for CI events to get processed
        } catch (InterruptedException ignore) {
        }
        CmsCIRelationSimple relation = cmsUtil.custCIRelation2CIRelationSimple(GSON.fromJson(message, CmsCIRelation.class), "df", false);
        relation = processRelationMsg(relation, indexer.getIndexName());
        String releaseMsg = GSON_ES.toJson(relation);
        indexer.indexEvent("relation", releaseMsg);
        indexer.index(String.valueOf(relation.getCiRelationId()), "relation", releaseMsg);
    }


    private CmsCIRelationSimple processRelationMsg(CmsCIRelationSimple relation, String index) {
        CmsCISearch fromCI = fetchCI(index, String.valueOf(relation.getFromCiId()));
        if (fromCI != null) {
            relation.setFromCi(fromCI);
        }
        CmsCISearch toCI = fetchCI(index, String.valueOf(relation.getToCiId()));
        if (toCI != null) {
            relation.setToCi(toCI);
        }

        return relation;
    }


    private CmsCISearch fetchCI(String index, String ciId) {
        GetResponse response = client.prepareGet(index, "ci", String.valueOf(ciId))
                .execute()
                .actionGet();

        if (response.getSourceAsString() == null || response.getSourceAsString().isEmpty()) {
            return null;
        }

        CmsCISearch ci = GSON_ES.fromJson(response.getSourceAsString(), CmsCISearch.class);
        if (ci.getWorkorder() != null) {
            ci.setWorkorder(null);
        }
        if (ci.getOps() != null) {
            ci.setOps(null);
        }

        return ci;
    }


    /**
     * @param ciMsg
     */
    void processRelationForCi(String ciMsg) {
        CmsCISearch ci = GSON_ES.fromJson(ciMsg, CmsCISearch.class);

        SearchResponse scrollResp = client.prepareSearch(indexer.getIndexName())
                .setSearchType(SearchType.SCAN)
                .setTypes("relation")
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.termQuery("fromCiId", ci.getCiId()))
                .setSize(100).execute().actionGet();

        //Scroll until no hits are returned
        while (true) {

            for (SearchHit hit : scrollResp.getHits()) {
                CmsCIRelationSimple relation = GSON_ES.fromJson(hit.getSourceAsString(), CmsCIRelationSimple.class);
                relation.setFromCi(ci);
                indexer.index(String.valueOf(relation.getCiRelationId()), "relation", GSON_ES.toJson(relation));
            }

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
            //Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }

        scrollResp = client.prepareSearch(indexer.getIndexName())
                .setSearchType(SearchType.SCAN)
                .setTypes("relation")
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.termQuery("toCiId", ci.getCiId()))
                .setSize(100).execute().actionGet();

        //Scroll until no hits are returned
        while (true) {

            for (SearchHit hit : scrollResp.getHits()) {
                CmsCIRelationSimple relation = GSON_ES.fromJson(hit.getSourceAsString(), CmsCIRelationSimple.class);
                relation.setToCi(ci);
                indexer.index(String.valueOf(relation.getCiRelationId()), "relation", GSON_ES.toJson(relation));
            }

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
            //Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }

    }

    public void processRelationDeleteMsg(String ciId) {

        logger.info("Deleting from/to relations for ciId " + ciId);
        //Fetch and delete relation for given fromCI
        fetchAndDeleteRecords("fromCiId", ciId);
        //Fetch and delete relation for given toCI
        fetchAndDeleteRecords("toCiId", ciId);

    }

    /**
     */
    private void fetchAndDeleteRecords(String field, String ciId) {
        SearchResponse scrollResp = client.prepareSearch(indexer.getIndexName())
                .setSearchType(SearchType.SCAN)
                .setTypes("relation")
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.termQuery(field, ciId))
                .setSize(100).execute().actionGet(); //100 hits per shard will be returned for each scroll
        //Scroll until no hits are returned
        while (true) {

            for (SearchHit hit : scrollResp.getHits()) {
                indexer.getTemplate().delete(indexer.getIndexName(), "relation", String.valueOf(hit.getId()));
                logger.info("Deleted message with id::" + hit.getId() + " and type::relation" + " from ES for " + field + " " + ciId);
            }

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
            //Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }
    }
}
