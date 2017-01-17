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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

@Service
public class CIMessageProcessor implements MessageProcessor {
	private static Logger logger = Logger.getLogger(CIMessageProcessor.class);
	private static final String SUCCESS_PREFIX = "SUCCESS:";
	private static final int RETRY_COUNT = 5 ;
	private static final long TIME_TO_WAIT = 5000 ;


	@Autowired
	private Client client;
	@Autowired
	private Indexer indexer;
	@Autowired
	private PolicyProcessor policyProcessor;
	@Autowired
	private DeploymentPlanProcessor deploymentPlanProcessor;
	@Autowired
	private RelationMessageProcessor relationMessageProcessor;
	@Autowired
	private CmsUtil cmsUtil;


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
		JsonParser parser = new JsonParser();
		JsonElement jsonElement = parser.parse(message);
		if (jsonElement.isJsonObject()) {
			expand(jsonElement.getAsJsonObject());
		}

		indexer.index(String.valueOf(simpleCI.getCiId()), "ci", GSON_ES.toJson(jsonElement));

		relationMessageProcessor.processRelationForCi(message);
	}

	private void expand(JsonObject currentElement) {
		Map<String, JsonElement> map = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry: currentElement.entrySet()){
			JsonElement value = entry.getValue();
			if (value.isJsonObject()){
            	expand(value.getAsJsonObject()); // expand recursively
			} else if (value.isJsonPrimitive()){
				String valueAsString = value.getAsString();
				if (valueAsString.endsWith("}") && valueAsString.startsWith("{") && valueAsString.length()>2){
					try {
						JsonElement object = new JsonParser().parse(valueAsString);
						map.put(entry.getKey()+MessageProcessor.EXPJSON_SUFFIX, object);
					} catch (Exception ignore){
					}
				}
			}
        }
        for (Map.Entry<String, JsonElement> element:map.entrySet()){
			currentElement.add(element.getKey(),element.getValue());
		}
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
						.setQuery(queryStringQuery(String.valueOf(ciId)).field("rfcCi.ciId"))
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
