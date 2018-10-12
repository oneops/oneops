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
package com.oneops.cms.cm.service;

import com.oneops.cms.cm.domain.CmsAltNs;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.cms.util.domain.CmsVar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Interface CmsCmManager.
 */
@Transactional
public interface CmsCmManager {
	CmsCI createCI(CmsCI ci);
	CmsCI updateCI(CmsCI ci);
	void updateCiState(long ciId, String ciState, String relName, String direction, boolean recursive, String user);
	void updateCiStateBulk(Long[] ids, String ciState, String relName, String direction, boolean recursive, String user);
	CmsCI updateCiState(long ciId, String ciState, String user);
	void deleteCI(long ciId, String userId);
	CmsCI getCiByGoid(String goid);
	CmsCI getCiById(long id);
	List<CmsCI> getCiByIdList(List<Long> ids);
	List<CmsCI> getCiByIdListNaked(List<Long> ids);
	List<CmsCI> getCiBy3(String ns, String clazz, String ci);
	List<CmsCI> getCiBy3NsLike(String ns, String clazz, String ci);
	List<CmsCI> getCiByAttributes(String ns, String clazz, List<AttrQueryCondition> attrs, boolean recursive);
	List<CmsCI> getCiByName(String name, String oper);
	long getCountBy3(String ns, String clazz, String ci, boolean recursive);
	Map<String, Long> getCountBy3GroupByNs(String ns, String clazz, String ci);
	

	CmsCIRelation createRelation(CmsCIRelation relation);
	CmsCIRelation updateRelation(CmsCIRelation relation);
	CmsCIRelation getRelationById(long relId);
    void deleteRelation(long relId);
	List<CmsCIRelation> getAllCIRelations(long fromId);
	List<CmsCIRelation> getFromCIRelations(long fromId, String relationName, String toClazzName);
	List<CmsCIRelation> getFromCIRelations(long fromId, String relationName, String shortRelationName,List<Long> toCiIds);
	List<CmsCIRelation> getFromCIRelations(long fromId, String relationName, String shortRelationName, String toClazzName);
	List<CmsCIRelation> getFromCIRelations(long fromId, String relationName, String shortRelationName, String toClazzName, List<AttrQueryCondition> attrs);
	List<CmsCIRelation> getFromCIRelationsByNs(long fromId, String relationName, String shortRelName, String toClazzName, String toNsPath);
	List<CmsCIRelation> getToCIRelations(long toId, String relationName, String fromClazzName);
	List<CmsCIRelation> getToCIRelations(long toId, String relationName, String shortRelationName, String fromClazzName);
	List<CmsCIRelation> getToCIRelations(long toId, String relationName, String shortRelationName, List<Long> fromCiIds);
	List<CmsCIRelation> getToCIRelations(long toId, String relationName, String shortRelationName, String fromClazzName, List<AttrQueryCondition> attrs);
	List<CmsCIRelation> getToCIRelationsByNs(long toId, String relationName, String shortRelName, String fromClazzName, String fromNsPath);
	List<CmsCIRelation> getCIRelations(String nsPath, String relationName, String shortRelName, String fromClazzName, String toClazzName);
	List<CmsCIRelation> getCIRelationsNsLike(String nsPath, String relationName, String shortRelName, String fromClazzName, String toClazzName);
	void populateRelCis(List<CmsCIRelation> rels, boolean fromCis, boolean toCis);
	
	
	long getCountFromCIRelationsByNS(long fromId,String relationName, String shortRelName, String toClazzName, String toNsPath, boolean recursive);
	long getCountToCIRelationsByNS(long toId,String relationName, String shortRelName, String toClazzName, String toNsPath, boolean recursive);
	Map<String, Long> getCountFromCIRelationsGroupByNs(long fromId, String relationName, String shortRelName, String toClazzName, String toNsPath);
	Map<String, Long> getCountToCIRelationsGroupByNs(long toId, String relationName, String shortRelName, String toClazzName, String toNsPath);
	Map<Long, Long> getCounCIRelationsGroupByFromCiId(String relationName, String shortRelName, String toClazzName, String nsPath);
	Map<Long, Long> getCounCIRelationsGroupByToCiId(String relationName, String shortRelName, String fromClazzName, String nsPath);
	
	
	
	Map<Long,List<Long>> getEnvState(long envId);
	
	void updateCmSimpleVar(String varName, String varValue, String criteria, String updatedBy);
	CmsVar getCmSimpleVar(String varName);
	CmsVar getCmSimpleVar(String varName, String criteria);

    void createAltNs(CmsAltNs cmsAltNs, CmsCI ci);
    void deleteAltNs(long nsId, long ciId);

    List<CmsCI> getCmCIByAltNsAndTag(String nsPath,
                                     String clazzName,
                                     String altNsPath, String tag,
                                     boolean recursive);
	List<CmsAltNs> getAltNsByCiAndTag(long ciId, String tag);

	void updateCiAltNs(long ciId, Map<String, Set<String>> altNs);

	List<CmsCI> getCiByAttributes(String nsPath, String clazzName, List<AttrQueryCondition> attrConds, boolean recursive, String altNs, String tag);

	CmsVar getCmVarByLongestMatchingCriteria(String varNameLike, String criteria);
}
