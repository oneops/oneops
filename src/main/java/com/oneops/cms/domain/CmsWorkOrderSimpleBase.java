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
