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

import org.apache.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oneops.cms.simple.domain.CmsCIRelationSimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.search.domain.CmsCISearch;
import com.oneops.search.msg.index.Indexer;
import com.oneops.search.msg.index.impl.ESIndexer;

public class RelationMsgProcessor {

	private static Logger logger = Logger.getLogger(RelationMsgProcessor.class);
	private Client client;
	final private Gson searchGson = new GsonBuilder().setDateFormat(CmsConstants.SEARCH_TS_PATTERN).create();
	
	/**
	 * 
	 * @param nsId
	 * @param indexer
	 */
	public void processRelationDeleteMsg(String ciId,Indexer indexer){
		
		logger.info("Deleting from/to relations for ciId " + ciId);
		//Fetch and delete relation for given fromCI
		fetchAndDeleteRecords("fromCiId", ciId, indexer);
		//Fetch and delete relation for given toCI
		fetchAndDeleteRecords("toCiId", ciId, indexer);
		
	}

	/**
	 * 
	 * @param type
	 * @param nsId
	 * @param indexer
	 */
	private void fetchAndDeleteRecords(String field, String ciId, Indexer indexer) {
		ESIndexer esIndexer = ((ESIndexer)indexer);
		
		SearchResponse scrollResp = client.prepareSearch(esIndexer.getIndexName())
                .setSearchType(SearchType.SCAN)
                .setTypes("relation")
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.termQuery(field, ciId))
                .setSize(100).execute().actionGet(); //100 hits per shard will be returned for each scroll
        //Scroll until no hits are returned
        while (true) {

        	for (SearchHit hit : scrollResp.getHits()){
        		esIndexer.getTemplate().delete(esIndexer.getIndexName(), "relation", String.valueOf(hit.getId()));
    			logger.info("Deleted message with id::"+ hit.getId() +" and type::relation"+" from ES for " + field +" " + ciId);
        	}
			
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
            //Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }
		
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public CmsCIRelationSimple processRelationMsg(CmsCIRelationSimple relation, String index) {
		CmsCISearch fromCI = fetchCI(index,String.valueOf(relation.getFromCiId()));
		if(fromCI != null){
			relation.setFromCi(fromCI);
		}
		CmsCISearch toCI = fetchCI(index,String.valueOf(relation.getToCiId()));
		if(toCI != null){
			relation.setToCi(toCI);
		}
		
		return relation;
	}

	
	private CmsCISearch fetchCI(String index,String ciId) {
		GetResponse response = client.prepareGet(index, "ci", String.valueOf(ciId))
		        .execute()
		        .actionGet();
		
		if(response.getSourceAsString() == null || response.getSourceAsString().isEmpty()){
			return null;
		}
		
		CmsCISearch ci = searchGson.fromJson(response.getSourceAsString(),  CmsCISearch.class);
		if(ci.getWorkorder() != null){ci.setWorkorder(null);}
		if(ci.getOps() != null){ci.setOps(null);}
		
		return ci;
	}
	
	
	/**
	 * 
	 * @param ciMsg
	 * @param indexer
	 */
	public void processRelationForCi(String ciMsg, Indexer indexer) {
		ESIndexer esIndexer = ((ESIndexer)indexer);
		CmsCISearch ci = searchGson.fromJson(ciMsg, CmsCISearch.class);
		
		SearchResponse scrollResp = client.prepareSearch(esIndexer.getIndexName())
                .setSearchType(SearchType.SCAN)
                .setTypes("relation")
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.termQuery("fromCiId", ci.getCiId()))
                .setSize(100).execute().actionGet();
		
		 //Scroll until no hits are returned
        while (true) {

        	for (SearchHit hit : scrollResp.getHits()){
        		CmsCIRelationSimple relation = searchGson.fromJson(hit.getSourceAsString(), CmsCIRelationSimple.class);
        		relation.setFromCi(ci);
        		indexer.index(String.valueOf(relation.getCiRelationId()), "relation", searchGson.toJson(relation));
        	}
			
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
            //Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }
        
        scrollResp = client.prepareSearch(esIndexer.getIndexName())
                .setSearchType(SearchType.SCAN)
                .setTypes("relation")
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.termQuery("toCiId", ci.getCiId()))
                .setSize(100).execute().actionGet();
		
		 //Scroll until no hits are returned
        while (true) {

        	for (SearchHit hit : scrollResp.getHits()){
        		CmsCIRelationSimple relation = searchGson.fromJson(hit.getSourceAsString(), CmsCIRelationSimple.class);
        		relation.setToCi(ci);
        		indexer.index(String.valueOf(relation.getCiRelationId()), "relation", searchGson.toJson(relation));
        	}
			
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
            //Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }
		
	}
}
