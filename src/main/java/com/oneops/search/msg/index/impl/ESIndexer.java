package com.oneops.search.msg.index.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;

import com.oneops.search.msg.index.Indexer;

public class ESIndexer implements Indexer{
	
	private static Logger logger = Logger.getLogger(ESIndexer.class);
	private String indexName;
	private ElasticsearchTemplate template;
	private SimpleDateFormat dt = new SimpleDateFormat( "yyyy-'w'ww" );

	@Override
	public void index(String id, String type, String message) {
		if(id == null){
			index(type , message);
		}
		else {
		String index = getIndex(type);
		IndexQuery query = new IndexQueryBuilder().withIndexName(index).withId(String.valueOf(id)).withType(type).withSource(message).build();
		String docId = template.index(query);
		logger.info("Indexed message id  " + docId + " of type " + type);
		}
	}
	
	
	@Override
	public void index(String type, String message) {
		String index = getIndex(type);
		IndexQuery query = new IndexQueryBuilder().withIndexName(index).withType(type).withSource(message).build();
		String docId = template.index(query);
		logger.info("Indexed message id  " + docId + " of type " + type);
		
	}
	
	@Override
	public void indexEvent(String type,String message){
		String index = "event" + "-" + dt.format(new Date());
		IndexQuery query = new IndexQueryBuilder().withIndexName(index).withType(type).withSource(message).build();
		String docId = template.index(query);
		logger.info("Indexed event message with id  " + docId + " of type " + type);
	}
	
	/**
	 * Get daily index name
	 * @param type
	 * @return
	 */
	private String getIndex(String type) {
		String index;
		if(!("ci".equals(type) || "release".equals(type) || "relation".equals(type))){
			 index = "cms" + "-" + dt.format(new Date());
		}
		else{
			index = indexName;
		}
		return index;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public ElasticsearchTemplate getTemplate() {
		return template;
	}

	public void setTemplate(ElasticsearchTemplate template) {
		this.template = template;
	}
	
}