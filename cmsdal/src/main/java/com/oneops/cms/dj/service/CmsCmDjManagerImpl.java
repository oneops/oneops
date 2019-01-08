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

import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.domain.RfcHint;
import com.oneops.cms.util.domain.AttrQueryCondition;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * The Class CmsCmDjManagerImpl.
 */
public class CmsCmDjManagerImpl implements CmsCmDjManager {

	static Logger logger = Logger.getLogger(CmsCmDjManagerImpl.class);
	private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
	
	/**
	 * Sets the cm rfc mrg processor.
	 *
	 * @param cmRfcMrgProcessor the new cm rfc mrg processor
	 */
	public void setCmRfcMrgProcessor(CmsCmRfcMrgProcessor cmRfcMrgProcessor) {
		this.cmRfcMrgProcessor = cmRfcMrgProcessor;
	}

	/**
	 * Upsert ci rfc.
	 *
	 * @param rfcCi the rfc ci
	 * @param userId the user id
	 * @return the cms rfc ci
	 */
	@Override
	public CmsRfcCI upsertCiRfc(CmsRfcCI rfcCi, String userId) {
		return cmRfcMrgProcessor.upsertCiRfc(rfcCi, userId); 
	}

	/**
	 * Touch ci.
	 *
	 * @param rfcCi the rfc ci
	 * @param userId the user id
	 * @return the cms rfc ci
	 */
	@Override
	public CmsRfcCI touchCi(CmsRfcCI rfcCi, String userId) {
		CmsRfcCI rfc = cmRfcMrgProcessor.createDummyUpdateRfc(rfcCi.getCiId(), rfcCi.getReleaseType(), 0, userId, RfcHint.TOUCH);
		return rfc;
	}
	
	
	/**
	 * Upsert relation rfc.
	 *
	 * @param rel the rel
	 * @param userId the user id
	 * @return the cms rfc relation
	 */
	@Override
	public CmsRfcRelation upsertRelationRfc(CmsRfcRelation rel, String userId) {
		return cmRfcMrgProcessor.upsertRelationRfc(rel, userId);
	}
	
	
	/**
	 * Gets the ci by id.
	 *
	 * @param ciId the ci id
	 * @param cmAttrValue the cm attr value
	 * @return the ci by id
	 */
	@Override
	public CmsRfcCI getCiById(long ciId, String cmAttrValue) {
		return cmRfcMrgProcessor.getCiById(ciId, cmAttrValue);
	}

	/**
	 * Gets the from ci relations.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param toClazzName the to clazz name
	 * @param cmAttrValue the cm attr value
	 * @return the from ci relations
	 */
	@Override
	public List<CmsRfcRelation> getFromCIRelations(long fromId,
		String relationName, String toClazzName, String cmAttrValue) {
		return cmRfcMrgProcessor.getFromCIRelations(fromId, relationName, toClazzName, cmAttrValue);
	}

	/**
	 * Gets the from ci relations.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelationName the short relation name
	 * @param toClazzName the to clazz name
	 * @param cmAttrValue the cm attr value
	 * @return the from ci relations
	 */
	@Override
	public List<CmsRfcRelation> getFromCIRelations(long fromId,
		String relationName, String shortRelationName, String toClazzName, String cmAttrValue) {
		return cmRfcMrgProcessor.getFromCIRelations(fromId, relationName, shortRelationName, toClazzName, cmAttrValue);
	}

	/**
	 * Gets the from ci relations.
	 *
	 * @param fromId the from id
	 * @param relationName the relation name
	 * @param shortRelationName the short relation name
	 * @param toClazzName the to clazz name
	 * @param ciAttrValue the ci attr value
	 * @param attrConds the attr conds
	 * @return the from ci relations
	 */
	@Override
	public List<CmsRfcRelation> getFromCIRelations(long fromId,
			String relationName, String shortRelationName, String toClazzName,
			String ciAttrValue, List<AttrQueryCondition> attrConds) {
		return cmRfcMrgProcessor.getFromCIRelationsByAttrs(
				fromId, relationName, shortRelationName, toClazzName, ciAttrValue, attrConds);
	}

	
	/**
	 * Gets the to ci relations.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param toClazzName the to clazz name
	 * @param cmAttrValue the cm attr value
	 * @return the to ci relations
	 */
	@Override
	public List<CmsRfcRelation> getToCIRelations(long toId,
		String relationName, String toClazzName, String cmAttrValue) {
		return cmRfcMrgProcessor.getToCIRelations(toId, relationName, toClazzName, cmAttrValue);
	}

	/**
	 * Gets the to ci relations.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelationName the short relation name
	 * @param toClazzName the to clazz name
	 * @param cmAttrValue the cm attr value
	 * @return the to ci relations
	 */
	@Override
	public List<CmsRfcRelation> getToCIRelations(long toId,
		String relationName, String shortRelationName, String toClazzName, String cmAttrValue) {
		return cmRfcMrgProcessor.getToCIRelations(toId, relationName, shortRelationName, toClazzName, cmAttrValue);
	}

	/**
	 * Gets the to ci relations.
	 *
	 * @param toId the to id
	 * @param relationName the relation name
	 * @param shortRelationName the short relation name
	 * @param fromClazzName the from clazz name
	 * @param ciAttrValue the ci attr value
	 * @param attrConds the attr conds
	 * @return the to ci relations
	 */
	@Override
	public List<CmsRfcRelation> getToCIRelations(long toId,
			String relationName, String shortRelationName, String fromClazzName,
			String ciAttrValue, List<AttrQueryCondition> attrConds) {
		return cmRfcMrgProcessor.getToCIRelationsByAttrs(toId, relationName, shortRelationName, fromClazzName, ciAttrValue, attrConds);
	}
	
	
	/**
	 * Gets the cI relation.
	 *
	 * @param ciRelationId the ci relation id
	 * @param ciAttrValue the ci attr value
	 * @return the cI relation
	 */
	@Override
	public CmsRfcRelation getCIRelation(long ciRelationId, String ciAttrValue) {
		return cmRfcMrgProcessor.getCIRelation(ciRelationId, ciAttrValue);
	}

	/**
	 * Gets the df dj relations.
	 *
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param nsPath the ns path
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @param cmAttrValue the cm attr value
	 * @return the df dj relations
	 */
	@Override
	public List<CmsRfcRelation> getDfDjRelations(String relationName,
			String shortRelName, String nsPath, String fromClazzName,
			String toClazzName, String cmAttrValue) {
		return cmRfcMrgProcessor.getDfDjRelations(relationName, shortRelName, nsPath, fromClazzName, toClazzName, cmAttrValue);
	}

	/**
	 * Gets the df dj relations with c is.
	 *
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param nsPath the ns path
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @param cmAttrValue the cm attr value
	 * @param includeFromCi the include from ci
	 * @param includeToCi the include to ci
	 * @param attrConditions the attr conditions
	 * @return the df dj relations with c is
	 */
	@Override
	public List<CmsRfcRelation> getDfDjRelationsWithCIs(String relationName,
			String shortRelName, String nsPath, String fromClazzName,
			String toClazzName, String cmAttrValue, boolean includeFromCi, boolean includeToCi, List<AttrQueryCondition> attrConditions) {
		return cmRfcMrgProcessor.getDfDjRelationsWithCIs(relationName, shortRelName, nsPath, fromClazzName, toClazzName, cmAttrValue, includeFromCi, includeToCi, attrConditions);
	}
	
	
	/**
	 * Gets the df dj relations ns like.
	 *
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param nsPath the ns path
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @param cmAttrValue the cm attr value
	 * @return the df dj relations ns like
	 */
	@Override
	public List<CmsRfcRelation> getDfDjRelationsNsLike(String relationName,
			String shortRelName, String nsPath, String fromClazzName,
			String toClazzName, String cmAttrValue) {
		return cmRfcMrgProcessor.getDfDjRelationsNsLike(relationName, shortRelName, nsPath, fromClazzName, toClazzName, cmAttrValue);
	}
	
	/**
	 * Gets the df dj relations ns like with c is.
	 *
	 * @param relationName the relation name
	 * @param shortRelName the short rel name
	 * @param nsPath the ns path
	 * @param fromClazzName the from clazz name
	 * @param toClazzName the to clazz name
	 * @param cmAttrValue the cm attr value
	 * @param includeFromCi the include from ci
	 * @param includeToCi the include to ci
	 * @param attrConditions the attr conditions
	 * @return the df dj relations ns like with c is
	 */
	@Override
	public List<CmsRfcRelation> getDfDjRelationsNsLikeWithCIs(String relationName,
			String shortRelName, String nsPath, String fromClazzName,
			String toClazzName, String cmAttrValue, boolean includeFromCi, boolean includeToCi, List<AttrQueryCondition> attrConditions) {
		return cmRfcMrgProcessor.getDfDjRelationsNsLikeWithCIs(relationName, shortRelName, nsPath, fromClazzName, toClazzName, cmAttrValue, includeFromCi, includeToCi, attrConditions);
	}
	
	/**
	 * Gets the df dj ci.
	 *
	 * @param nsPath the ns path
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @param cmAttrValue the cm attr value
	 * @return the df dj ci
	 */
	@Override
	public List<CmsRfcCI> getDfDjCi(String nsPath, String clazzName,
			String ciName, String cmAttrValue) {
		return cmRfcMrgProcessor.getDfDjCi(nsPath, clazzName, ciName, cmAttrValue);
	}

	/**
	 * Gets the df dj ci ns like.
	 *
	 * @param nsPath the ns path
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @param cmAttrValue the cm attr value
	 * @return the df dj ci ns like
	 */
	@Override
	public List<CmsRfcCI> getDfDjCiNsLike(String nsPath, String clazzName,
			String ciName, String cmAttrValue) {
		return cmRfcMrgProcessor.getDfDjCiNsLike(nsPath, clazzName, ciName, cmAttrValue);
	}

	
	/**
	 * Gets the df dj ci.
	 *
	 * @param nsPath the ns path
	 * @param clazzName the clazz name
	 * @param ciName the ci name
	 * @param cmAttrValue the cm attr value
	 * @param attrConds the attr conds
	 * @return the df dj ci
	 */
	@Override
	public List<CmsRfcCI> getDfDjCi(String nsPath, String clazzName,
			String ciName, String cmAttrValue,
			List<AttrQueryCondition> attrConds) {
		return cmRfcMrgProcessor.getDfDjCi(nsPath, clazzName, ciName, cmAttrValue, attrConds);
	}


	/**
	 * Delete ci.
	 *
	 * @param ciId the ci id
	 * @param userId the user id
	 */
	@Override
	public void deleteCi(long ciId, String userId) {
		cmRfcMrgProcessor.requestCiDelete(ciId, userId);
	}

	/**
	 * Delete relation.
	 *
	 * @param ciRelationId the ci relation id
	 * @param userId the user id
	 */
	@Override
	public void deleteRelation(long ciRelationId, String userId) {
		cmRfcMrgProcessor.requestRelationDelete(ciRelationId, userId);
	}
	
}
