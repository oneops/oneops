package com.oneops.cms.domain;

import java.util.Map;

import com.oneops.cms.cm.domain.CmsCI;

/**
 * The Interface CmsWorkOrderBase.
 */
public interface CmsWorkOrderBase {

	CmsCI getBox();

	void setBox(CmsCI box);

	CmsCI getResultCi();

	void setResultCi(CmsCI resultCi);

	CmsCI getCloud();

	void setCloud(CmsCI cloud);
	
	Map<String,Map<String, CmsCI>> getServices(); 

	void setServices(Map<String,Map<String, CmsCI>> services);
}
