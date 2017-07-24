/*******************************************************************************
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
package com.oneops.search.msg.processor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsUtil;
import com.oneops.search.domain.CmsCISearch;
import com.oneops.search.msg.index.Indexer;
import com.oneops.search.msg.processor.ci.DeploymentPlanProcessor;
import com.oneops.search.msg.processor.ci.PolicyProcessor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

@Service
public class CIMessageProcessor implements MessageProcessor {
    private static final int EXPANSION_LEVEL_MAX = 2;
    private static Logger logger = Logger.getLogger(CIMessageProcessor.class);
    private static final String SUCCESS_PREFIX = "SUCCESS:";
    private static final int RETRY_COUNT = 5;
    private static final long TIME_TO_WAIT = 5000;
    private static final String EXPJSON_SUFFIX = "_json";


    private Client client;
    private Indexer indexer;
    private PolicyProcessor policyProcessor;
    private DeploymentPlanProcessor deploymentPlanProcessor;
    private RelationMessageProcessor relationMessageProcessor;
    private CmsUtil cmsUtil;

    @Autowired
    public void setClient(Client client) {
        this.client = client;
    }

    @Autowired
    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }

    @Autowired
    public void setPolicyProcessor(PolicyProcessor policyProcessor) {
        this.policyProcessor = policyProcessor;
    }

    @Autowired
    public void setDeploymentPlanProcessor(DeploymentPlanProcessor deploymentPlanProcessor) {
        this.deploymentPlanProcessor = deploymentPlanProcessor;
    }

    @Autowired
    public void setRelationMessageProcessor(RelationMessageProcessor relationMessageProcessor) {
        this.relationMessageProcessor = relationMessageProcessor;
    }

    @Autowired
    public void setCmsUtil(CmsUtil cmsUtil) {
        this.cmsUtil = cmsUtil;
    }

    @Override
    public void processMessage(String message, String msgType, String msgId) {
        CmsCI ci = GSON.fromJson(message, CmsCI.class);
        CmsCISimple simpleCI = cmsUtil.custCI2CISimple(ci, "df");
        indexer.indexEvent("ci", GSON_ES.toJson(simpleCI));
        //For plan generation metrics
        if ("manifest.Environment".equals(ci.getCiClassName()) && StringUtils.isNotEmpty(ci.getComments()) && ci.getComments().startsWith(SUCCESS_PREFIX)) {
            deploymentPlanProcessor.process(simpleCI);
        } else if ("account.Policy".equals(ci.getCiClassName()) || "mgmt.manifest.Policy".equals(ci.getCiClassName())) {
            policyProcessor.process(simpleCI);
        }


        //add wo to all bom cis
        if (ci.getCiClassName().startsWith("bom")) {
            message = this.process(simpleCI);
        } else {
            message = GSON_ES.toJson(simpleCI);
        }
		indexer.index(String.valueOf(simpleCI.getCiId()), "ci", message);
        relationMessageProcessor.processRelationForCi(message);
    }



  
    /**
     * @param ci
     * @return
     */
    private String process(CmsCISimple ci) {
        CmsCISearch ciSearch = new CmsCISearch();
        BeanUtils.copyProperties(ci, ciSearch);
        long ciId = ciSearch.getCiId();
        CmsWorkOrderSimple wos = null;
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                SearchResponse response = client.prepareSearch("cms-2*")
                        .setTypes("workorder")
                        .setQuery(queryStringQuery("rfcCi.ciId:"+ciId+" AND dpmtRecordState:complete"))
                        .addSort("searchTags.responseDequeTS", SortOrder.DESC)
                        .setSize(1)
                        .execute()
                        .actionGet();

                if (response.getHits().getHits().length > 0) {
                    String cmsWo = response.getHits().getHits()[0].getSourceAsString();
                    wos = GSON.fromJson(cmsWo, CmsWorkOrderSimple.class);
                    logger.info("WO found for ci id " + ciId + " on retry " + i);
                    ciSearch.setWorkorder(wos);
                    break;
                } else {
                    Thread.sleep(TIME_TO_WAIT); //wait for TIME_TO_WAIT ms and retry
                }
            } catch (Exception e) {
                logger.error("Error in retrieving WO for ci " + ciId);
                e.printStackTrace();
            }
        }
        if (wos == null) {
            logger.info("WO not found for ci " + ci.getCiId() + " of type " + ci.getCiClassName());
        }
        return GSON_ES.toJson(ciSearch);
    }
}
