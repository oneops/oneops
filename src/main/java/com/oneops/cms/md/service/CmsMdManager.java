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
package com.oneops.cms.md.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsRelation;

/**
 * The Interface CmsMdManager.
 */
@Transactional
public interface CmsMdManager {
	void flushCache();
    CmsClazz createClazz(CmsClazz clazz);
    CmsClazz updateClazz(CmsClazz clazz);
    void deleteClazz(int clazzId, boolean deleteAll);
    void deleteClazz(String clazzName, boolean deleteAll);
    CmsRelation createRelation(CmsRelation relation);
    CmsRelation updateRelation(CmsRelation relation);
    void deleteRelation(int relationId, boolean deleteAll);
    void deleteRelation(String relationName, boolean deleteAll);
	List<CmsClazz> getClazzes();
	List<CmsClazz> getClazzesByPackage(String packagePrefix);
	CmsClazz getClazz(String clazzName);
	CmsClazz getClazz(int clazzId);
    CmsClazz getClazz(String clazzName, boolean eager);
    CmsClazz getClazz(int clazzId, boolean eager);
	CmsRelation getRelation(String relationName);
	CmsRelation getRelationWithTargets(String relationName);
	CmsRelation getRelationWithTargets(String relationName, int fromClassId, int toClassId);
	List<CmsRelation> getAllRelations();
	List<String> getSubClazzes(String clsName);
	void invalidateCache();
}
