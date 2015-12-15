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
package com.oneops.es.offerings.percolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.action.percolate.PercolateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.util.CmsConstants;

/**
 * 
 * @author ranand
 * 
 */
public class OfferingsMatcher {

	private static Logger logger = Logger.getLogger(OfferingsMatcher.class);
	private String indexName;
	private String indexType;
	private Long clientTimeout;
	Client client;
	Gson gson = new GsonBuilder().setDateFormat(CmsConstants.SEARCH_TS_PATTERN)
			.setExclusionStrategies(new ExclusionStrategy() {
				@Override
				public boolean shouldSkipField(FieldAttributes field) {
				if (field.getName().equals("rfcCreated") || field.getName().equals("rfcUpdated")) {
				return true;
				}
				return false;
				}

				@Override
				public boolean shouldSkipClass(Class<?> clazz) {
				return false;
				}
				}).create();;

	public List<String> getEligbleOfferings(CmsRfcCISimple rfcCi, String nsPathFilter) {
		
		String ciDoc = getCiDocWithFilters(rfcCi,nsPathFilter);
		PercolateResponse response = null;
		try {
			response = client.preparePercolate()
					.setIndices(indexName).setDocumentType(indexType)
					.setSource(ciDoc)
					.get(TimeValue.timeValueSeconds(clientTimeout));
		} catch (Exception e) {
			logger.error("Unable to percolate offerings for ci " + rfcCi.getCiId(),e);
			return Arrays.asList(new String[]{});
		}
		
		logger.info("Percolated ci "+ rfcCi.getCiId() +" with " + response.getCount() +" matching queries(offerings) found.");
		return getMatchingIds(response);

	}

	private List<String> getMatchingIds(PercolateResponse response) {
		List<String> matchingIds = new ArrayList<String>();
		for(PercolateResponse.Match match : response){
			matchingIds.add(match.getId().string());
		}
		return matchingIds;
	}


	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getIndexType() {
		return indexType;
	}

	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}

	public Long getClientTimeout() {
		return clientTimeout;
	}

	public void setClientTimeout(Long clientTimeout) {
		this.clientTimeout = clientTimeout;
	}
	
	private String getCiDocWithFilters(CmsRfcCISimple rfcCi, String nsPathFilter) {

		StringBuffer doc = new StringBuffer("{\"doc\":" );
		doc.append(gson.toJson(rfcCi))
		   .append(",")
		   .append("\"filter\": {\"bool\" : {\"must\" : [{ \"term\": {")
		   .append("\"ci.ciClassName.keyword\": \"cloud.Offering\"")
		   .append("}},{\"term\": {")
		   .append("\"ci.nsPath.keyword\":")
		   .append("\""+nsPathFilter+"\"")
		   .append(" }}] }}}");
		
		   return doc.toString();
	}
	
}
