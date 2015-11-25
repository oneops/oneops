package com.oneops.search.domain;

import org.springframework.data.elasticsearch.annotations.Document;

import com.oneops.cms.dj.domain.CmsRelease;

@Document(indexName = "cms")
public class CmsReleaseSearch extends CmsRelease {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

}
