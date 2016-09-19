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
package com.oneops.cms.dj.dal;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcBasicAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcLink;
import com.oneops.cms.dj.domain.CmsRfcRelation;

/**
 * The Interface DJMapper.
 */
public interface DJMapper {
	long getNextDjId();
	long getNextCiId();
	Integer getReleaseStateId(String stateName);
	Integer getRfcCiActionId(String stateName);
	
	void createRelease(CmsRelease release);
	void brushReleaseExecOrder(long releaseId);
	CmsRelease getReleaseById(long releaseId);
	List<CmsRelease> getReleaseBy3(@Param("nsPath") String nsPath,@Param("releaseName") String releaseName,@Param("releaseState") String releaseState);
	List<CmsRelease> getLatestRelease(@Param("nsPath") String nsPath,@Param("releaseState") String releaseState);
	int updateRelease(CmsRelease release);
	int deleteRelease(long releaseId);
	void commitRelease(@Param("releaseId") long releaseId,@Param("setDfValue") Boolean setDfValue,@Param("newCiState") Integer newCiState, @Param("delete4real") boolean delete4real, @Param("userId") String userId, @Param("desc") String desc);
	
	void createRfcCI(CmsRfcCI rfcCi);
	int rmRfcCIfromRelease(long rfcId);
	int updateRfcCI(CmsRfcCI rfcCi);
	void upsertRfcCIAttribute(CmsRfcAttribute attr);
	CmsRfcCI getRfcCIById(long rfcId);
	CmsRfcCI getOpenRfcCIByCiId(long ciId);
	List<CmsRfcCI> getOpenRfcCIByCiIdList(@Param("ciIds") List<Long> ciIds);
	List<CmsRfcCI> getRfcCIBy3(@Param("releaseId") long releaseId,@Param("isActive") Boolean isActive,@Param("ciId") Long ciId);
    List<CmsRfcCI> getRfcCIByClazzAndName(@Param("nsPath") String nsPath, @Param("clazzName") String clazzName, @Param("ciName") String ciName, @Param("isActive")Boolean isActive, @Param("state")String state);
	List<CmsRfcCI> getOpenRfcCIByClazzAndNameLower(@Param("nsPath") String nsPath, @Param("clazzName") String clazzName, @Param("ciName") String ciName);
	List<CmsRfcCI> getOpenRfcCIByNsLike(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("clazzName") String clazzName, @Param("ciName") String ciName);
	List<CmsRfcCI> getOpenRfcCIByClazzAnd2Names(@Param("nsPath") String nsPath, @Param("clazzName") String clazzName, @Param("ciName") String ciName, @Param("altCiName") String altCiName);
    List<CmsRfcCI> getClosedRfcCIByCiId(long ciId);
    List<CmsRfcCI> getRollUpRfc(@Param("ciId")long ciId, @Param("rfcId") long rfcId);

	List<CmsRfcAttribute> getRfcCIAttributes(long rfcId);
	List<CmsRfcAttribute> getRfcCIAttributesByRfcIdList(@Param("rfcIds") Set<Long> rfcIds);
    
    void createRfcRelation(CmsRfcRelation rel);
	int rmRfcRelationfromRelease(long rfcId);
	int updateRfcRelation(CmsRfcRelation rel);
	void upsertRfcRelationAttribute(CmsRfcAttribute attr);
	CmsRfcRelation getRfcRelationById(long rfcId);
	CmsRfcRelation getOpenRfcRelationByCiRelId(long ciRelationId);
	List<CmsRfcRelation> getRfcRelationByReleaseId(long releaseId);
	List<CmsRfcRelation> getClosedRfcRelationByCiId(long ciId);
	List<CmsRfcRelation> getRfcRelationsByNs(@Param("nsPath") String nsPath, @Param("isActive")Boolean isActive, @Param("state")String state);
	List<CmsRfcRelation> getRfcRelationBy4(@Param("releaseId") long releaseId,@Param("isActive") Boolean isActive,@Param("fromCiId") Long fromCiId, @Param("toCiId") Long toCiId);
	List<CmsRfcRelation> getOpenRfcRelationBy2(@Param("fromCiId") Long fromCiId, @Param("toCiId") Long toCiId, @Param("relName") String relName,@Param("shortRelName") String shortRelName);
	List<CmsRfcRelation> getOpenFromRfcRelationByTargetClass(@Param("fromCiId") long fromCiId, @Param("relName") String relName, @Param("shortRelName") String shortRelName, @Param("targetClassName") String targetClassName);
	List<CmsRfcRelation> getOpenFromRfcRelationByAttrs(
			@Param("fromCiId") long fromCiId, 
			@Param("relName") String relName, 
			@Param("shortRelName") String shortRelName, 
			@Param("targetClassName") String targetClassName,
			@Param("attrList") List<CmsRfcBasicAttribute> attrList);
	
	
	List<CmsRfcRelation> getOpenToRfcRelationByTargetClass(@Param("toCiId") long toCiId, @Param("relName") String relName, @Param("shortRelName") String shortRelName, @Param("targetClassName") String targetClassName);
	List<CmsRfcRelation> getOpenToRfcRelationByAttrs(
			@Param("toCiId") long toCiId, 
			@Param("relName") String relName, 
			@Param("shortRelName") String shortRelName, 
			@Param("targetClassName") String targetClassName,
			@Param("attrList") List<CmsRfcBasicAttribute> attrList);
	
	
	List<CmsRfcRelation> getRfcRelationBy3(@Param("releaseId") long releaseId,@Param("isActive") Boolean isActive,@Param("ciRelationId") Long ciRelationId);
	List<CmsRfcRelation> getOpenRfcRelations(@Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("nsPath") String nsPath, @Param("fromClazzName") String fromClazzName, @Param("toClazzName") String toClazzName);
	List<CmsRfcRelation> getOpenRfcRelationsNsLike(@Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("ns") String ns, @Param("nsLike") String nsLike, @Param("fromClazzName") String fromClazzName, @Param("toClazzName") String toClazzName);
	List<CmsRfcRelation> getRfcRelationByReleaseAndClass(@Param("releaseId") long releaseId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName);
	
	List<CmsRfcAttribute> getRfcRelationAttributes(long rfcId);
	List<CmsRfcAttribute> getRfcRelationAttributesByRfcIdList(@Param("rfcIds") Set<Long> rfcIds);
	
	List<Long> getLinkedRfcRelationId(@Param("releaseId") long releaseId,@Param("isActive") Boolean isActive,@Param("rfcId") long rfcId);
	List<CmsRfcLink> getOpenRfcLinks(@Param("nsPath") String nsPath, @Param("relName") String relName);
	
	long countCiRfcByReleaseId(long releaseId);
	long countOpenRfcCisByNs(String nsPath);
	long countOpenRfcRelationsByNs(String nsPath);
    void rmRfcs(Map<String,Object> params);
    long countCiNotUpdatedByRfc(@Param("fromCiId") long fromCiId, @Param("relationName") String relationName,
			@Param("shortRelName") String shortRelName, @Param("rfcId") long rfcId);
}
