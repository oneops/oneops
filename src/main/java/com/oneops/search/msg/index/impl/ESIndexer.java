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
		index(id, type, getIndexByType(type), message);
	}

	public String getIndexByType(String type) {
		if (("ci".equals(type) || "relation".equals(type))) {
			return indexName;
		} else {
			return "cms" + "-" + dt.format(new Date());
		}
	}

	private void index(String id, String type, String index, String message){
		IndexQueryBuilder indexQueryBuilder = new IndexQueryBuilder().withIndexName(index);
		if (id != null) indexQueryBuilder.withId(String.valueOf(id));
		IndexQuery query = indexQueryBuilder.withType(type).withSource(message).build();
		String docId = template.index(query);
		logger.info("Indexed message id  " + docId + " of type " + type+ " index:"+ index);
	}


	public void indexEvent(String type,String message){
		index(null, type, "event" + "-" + dt.format(new Date()), message);
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