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
package com.oneops.cms.admin.service;


import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.util.domain.CmsStuckDpmtCollection;

@Transactional
public interface CmsManager {
	List<CmsClazz> getClazzes();
	CmsClazz getClazz(String clazzName);
	CmsRelation getRelation(String relationName);
	List<CmsCI> getCiList(String nsPath, String className, String ciName);
	CmsCI getCI(long ciId);
	List<CmsCIAttribute> getCIAttributes(long ciId);
	List<CmsCIRelation> getFromRelation(long ciId);
	List<CmsCIRelation> getToRelation(long ciId);
	CmsCIRelation getCIRelation(long relId);
	List<CmsCIRelationAttribute> getCIRelationAttributes(long relId);
	void flushCache();
	CmsStuckDpmtCollection getStuckDpmts();
	List<CmsCI> getPendingDeletePackCIs();
	List<CmsCIRelation> getPendingDeletePackRelations();
//	List<CmsStuckDpmt> getInProgressStuckDpmts();
//	List<CmsStuckDpmt> getPausedStuckDpmts();
	
}
