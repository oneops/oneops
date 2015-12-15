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
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.oneops.search.msg.index.Indexer;
import com.oneops.search.msg.index.impl.ESIndexer;

public class NSMessageProcessor {
	
	private static Logger logger = Logger.getLogger(NSMessageProcessor.class);
	RelationMsgProcessor relationMsgProcessor;
	private Client client;
	
	/**
	 * 
	 * @param nsId
	 * @param indexer
	 */
	public void processNSDeleteMsg(String nsId,Indexer indexer){
		
		logger.info("Processing ns delete event for nsId " + nsId);
		//Fetch and delete CIs for given nsId
		fetchAndDeleteRecords("ci", nsId, indexer);
		//Fetch and delete releases for given nsId
		fetchAndDeleteRecords("release", nsId, indexer);
		
	}

	/**
	 * 
	 * @param type
	 * @param nsId
	 * @param indexer
	 */
	private void fetchAndDeleteRecords(String type, String nsId, Indexer indexer) {
		ESIndexer esIndexer = ((ESIndexer)indexer);
		
		SearchResponse scrollResp = client.prepareSearch(esIndexer.getIndexName())
                .setSearchType(SearchType.SCAN)
                .setTypes(type)
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.termQuery("nsId", nsId))
                .setSize(100).execute().actionGet(); //100 hits per shard will be returned for each scroll
        //Scroll until no hits are returned
        while (true) {

        	for (SearchHit hit : scrollResp.getHits()){
        		esIndexer.getTemplate().delete(esIndexer.getIndexName(), type, String.valueOf(hit.getId()));
    			logger.info("Deleted message with id::"+ hit.getId() +" and type::"+type+" from ES for nsId " + nsId);
    			if("ci".equals(type)){
    				relationMsgProcessor.processRelationDeleteMsg(hit.getId(), esIndexer);
    			}
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

	public RelationMsgProcessor getRelationMsgProcessor() {
		return relationMsgProcessor;
	}

	public void setRelationMsgProcessor(RelationMsgProcessor relationMsgProcessor) {
		this.relationMsgProcessor = relationMsgProcessor;
	}

}
