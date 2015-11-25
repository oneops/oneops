package com.oneops.search.msg.processor;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.util.CmsConstants;

public class OfferingsMessageProcessor {

	private static Logger logger = Logger.getLogger(OfferingsMessageProcessor.class);
	private Client client;
	String indexName;
	final private Gson searchGson = new GsonBuilder().setDateFormat(CmsConstants.SEARCH_TS_PATTERN).create();
	
	public void processMessage(CmsCISimple offeringCI){
		String queryString = offeringCI.getCiAttributes().get("criteria");
		try {
			client.prepareIndex(indexName, ".percolator", String.valueOf(offeringCI.getCiId()))
			.setSource(getQuery(queryString,offeringCI))
			.setRefresh(true) // Needed when the query shall be available immediately
			.execute().actionGet();
			logger.info("registered percolator for offering ci:" + offeringCI.getCiId());
		} catch (Exception e) {
			logger.error("Error in registering percolator for offering CI "+offeringCI.getCiId(), e);
		} 
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
	
	private XContentBuilder getQuery(String queryString,CmsCISimple offeringCI) {
		XContentBuilder docBuilder = null;
		try {
			docBuilder = XContentFactory.jsonBuilder().startObject();
			docBuilder.field("query").startObject().field("query_string").startObject().field("query",queryString).endObject().endObject();
			docBuilder.rawField("ci", searchGson.toJson(offeringCI).getBytes());
			docBuilder.endObject();
		
		logger.info(docBuilder.string());
		} catch (IOException e) {
			logger.error("Error in forming percolator query ",e);
		}
		return docBuilder;
	}
}
