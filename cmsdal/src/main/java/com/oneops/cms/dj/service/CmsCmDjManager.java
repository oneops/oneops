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
package com.oneops.cms.dj.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.util.domain.AttrQueryCondition;

/**
 * The Interface CmsCmDjManager.
 */
@Transactional
public interface CmsCmDjManager {
	
	CmsRfcCI getCiById(long ciId, String ciAttrValue);
	void deleteCi(long ciId, String userId);
	CmsRfcCI upsertCiRfc(CmsRfcCI rfcCi, String userId);
	CmsRfcCI touchCi(CmsRfcCI rfcCi, String userId);

	List<CmsRfcCI> getDfDjCi(String nsPath, String clazzName, String ciName, String cmAttrValue);
	List<CmsRfcCI> getDfDjCiNsLike(String nsPath, String clazzName, String ciName, String cmAttrValue);
	List<CmsRfcCI> getDfDjCi(String nsPath, String clazzName, String ciName, String cmAttrValue, List<AttrQueryCondition> attrConds);
	
	List<CmsRfcRelation> getFromCIRelations(long fromId,
			String relationName, String toClazzName, String ciAttrValue); 
	List<CmsRfcRelation> getFromCIRelations(long fromId,
			String relationName, String shortRelationName, String toClazzName, String ciAttrValue); 
	List<CmsRfcRelation> getFromCIRelations(long fromId,
			String relationName, String shortRelationName, String toClazzName, String ciAttrValue, List<AttrQueryCondition> attrConds); 
	
	List<CmsRfcRelation> getToCIRelations( long toId,
			String relationName, String fromClazzName, String ciAttrValue); 
	List<CmsRfcRelation> getToCIRelations( long toId,
			String relationName, String shortRelationName, String fromClazzName, String ciAttrValue); 
	List<CmsRfcRelation> getToCIRelations( long toId,
			String relationName, String shortRelationName, String fromClazzName, String ciAttrValue, List<AttrQueryCondition> attrConds); 
	
	List<CmsRfcRelation> getDfDjRelations(
			String relationName, String shortRelName, String nsPath, String fromClazzName, String toClazzName, String cmAttrValue);

	List<CmsRfcRelation> getDfDjRelationsWithCIs(
			String relationName, String shortRelName, String nsPath, String fromClazzName, String toClazzName, String cmAttrValue, boolean includeFromCi, boolean includeToCi, List<AttrQueryCondition> attrConditions);
	
	
	List<CmsRfcRelation> getDfDjRelationsNsLike(
			String relationName, String shortRelName, String nsPath, String fromClazzName, String toClazzName, String cmAttrValue);

	List<CmsRfcRelation> getDfDjRelationsNsLikeWithCIs(
			String relationName, String shortRelName, String nsPath, String fromClazzName, String toClazzName, String cmAttrValue, boolean includeFromCi, boolean includeToCi, List<AttrQueryCondition> attrConditions);
	
	CmsRfcRelation getCIRelation(long ciRelationId, String ciAttrValue);
	CmsRfcRelation upsertRelationRfc(CmsRfcRelation rel, String userId);	
	void deleteRelation(long ciRelationId, String userId);

}
