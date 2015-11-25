package com.oneops.cms.domain;

import java.util.Map;

import com.oneops.cms.simple.domain.CmsCISimple;

/**
 * The Interface CmsWorkOrderSimpleBase.
 */
public interface CmsWorkOrderSimpleBase {

	CmsCISimple getBox();

	void setBox(CmsCISimple box);

	CmsCISimple getResultCi();

	void setResultCi(CmsCISimple resultCi);

	CmsCISimple getCloud();

	void setCloud(CmsCISimple token);
	
	Map<String,Map<String, CmsCISimple>> getServices(); 

	void setServices(Map<String,Map<String, CmsCISimple>> services);
	
	Map<String, String> getSearchTags();
	
	void setSearchTags(Map<String, String> searchTags);

}
