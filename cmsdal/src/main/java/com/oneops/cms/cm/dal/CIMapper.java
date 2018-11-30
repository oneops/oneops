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
package com.oneops.cms.cm.dal;

import com.oneops.cms.cm.domain.*;
import com.oneops.cms.util.domain.AttrQueryCondition;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CIMapper {
	long getNextCmId();
	long getCIidByGoid(String goid);
	Integer getCiStateId(String stateName);
	
	void createCI(CmsCI ci);
	void updateCI(CmsCI ci);
	void deleteCI(@Param("ciId") long ciId, @Param("delete4real") boolean delete4real, @Param("deletedBy") String userId);

	void addCIAttribute(CmsCIAttribute attr);
    void addCIAttributeAndPublish(CmsCIAttribute attr);
	void updateCIAttribute(CmsCIAttribute attr);

	void createRelation(CmsCIRelation rel);
	void updateRelation(CmsCIRelation rel);
	void addRelationAttribute(CmsCIRelationAttribute attr);
    void addRelationAttributeAndPublish(CmsCIRelationAttribute attr);
	void deleteRelation(@Param("ciRelationId") long ciId, @Param("delete4real") boolean delete4real);
    void updateCIRelationAttribute(CmsCIRelationAttribute attr);
    
    void resetDeletionsByNsLike(@Param("ns") String ns, @Param("nsLike") String nsLike);
    void resetRelDeletionsByNsLike(@Param("ns") String ns, @Param("nsLike") String nsLike);
	
	CmsCI getCIbyGoid(String goid);
	CmsCI getCIById(long id);
	List<CmsCI> getCIByIdList(@Param("ciIds") List<Long> ciIds);
	List<CmsCI> getCIby3(@Param("ns") String ns, @Param("clazz") String clazz, @Param("shortClazz") String shortClazz, @Param("name") String name);
	List<CmsCI> getCIby3lower(@Param("ns") String ns, @Param("clazz") String clazz, @Param("shortClazz") String shortClazz, @Param("name") String name);
	List<CmsCI> getCIby3NsLike(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("clazz") String clazz, @Param("shortClazz") String shortClazz, @Param("name") String name);
	List<CmsCI> getCIbyStateNsLike(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("clazz") String clazz, @Param("state") String state);
	List<CmsCI> getCIby3with2Names(@Param("ns") String ns, @Param("clazz") String clazz, @Param("name") String name, @Param("altName") String altName);
	List<CmsCI> getCIbyAttributes(@Param("ns") String ns, @Param("clazz") String clazz, @Param("shortClazz") String shortName, @Param("attrList") List<AttrQueryCondition> attrList);
	List<CmsCI> getCIbyAttributesNsLike(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("clazz") String clazz, @Param("shortClazz") String shortName, @Param("attrList") List<AttrQueryCondition> attrList);


	List<CmsCI> getCIbyAttributesWithAltNs(@Param("ns") String ns, @Param("clazz") String clazz, @Param("shortClazz") String shortName, @Param("attrList") List<AttrQueryCondition> attrList, @Param("altNs")String altNs, @Param("tag")String tag);
	List<CmsCI> getCIbyAttributesNsLikeWithAltNs(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("clazz") String clazz, @Param("shortClazz") String shortName, @Param("attrList") List<AttrQueryCondition> attrList, @Param("altNs")String altNs, @Param("tag")String tag);


	List<CmsCIAttribute> getCIAttrs(long ciId); 
	List<CmsCIAttribute> getCIAttrsNaked(long ciId);
	List<CmsCIAttribute> getCIAttrsNakedByCiIdList(@Param("ciIds") List<Long> ciIds);
	
	long getCountBy3(@Param("ns") String ns, @Param("clazz") String clazz, @Param("name") String name);
	long getCountBy3NsLike(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("clazz") String clazz, @Param("name") String name);
	List<Map<String,Object>> getCountBy3NsLikeGroupByNs(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("clazz") String clazz, @Param("name") String name);
	
	CmsCIRelation getCIRelation(long ciRelationId);
	List<CmsCIRelation> getCIRelations(@Param("ns") String nsPath, @Param("nsLike") String nsLike, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName, @Param("fromShortClazzName") String fromShortClazzName, @Param("toClazzName") String toClazzName, @Param("toShortClazzName") String toShortClazzName, @Param("conditions") List<AttrQueryCondition> attrList);
	List<CmsCIRelation> getCIRelationsByState(@Param("nsPath") String nsPath, @Param("relationName") String relationName, @Param("ciState") String ciState, @Param("fromClazzName") String fromClazzName, @Param("toClazzName") String toClazzName);
	List<CmsCIRelation> getCIRelationsByStateNsLike(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("relationNames") List<String> relationNames, @Param("ciState") String ciState, @Param("fromClazzName") String fromClazzName, @Param("toClazzName") String toClazzName);
	
	List<CmsCIRelation> getFromCIRelations(@Param("fromId") long fromId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toClazzName") String toClazzName, @Param("toShortClazzName") String toShortClazzName);
	List<CmsCIRelation> getFromCIRelationsByToClassAndCiName(@Param("fromId") long fromId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toClazzName") String toClazzName,@Param("toCiName") String toCiName);
	List<CmsCIRelation> getFromCIRelationsByToCiIDs(@Param("fromId") long fromId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toCiIds") List<Long> toCiIds);
	List<CmsCIRelation> getFromCIRelationsByMultiRelationNames(@Param("fromId") long fromId, @Param("relationNames") List<String> relationNames, @Param("shortRelNames") List<String> shortRelNames);
	long getCountFromCIRelationsByNS(@Param("fromId") long fromId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toClazzName") String toClazzName, @Param("toNsPath") String toNsPath);
	long getCountFromCIRelationsByNSLike(@Param("fromId") long fromId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toClazzName") String toClazzName, @Param("toNsPath") String toNsPath, @Param("toNsPathLike") String toNsPathLike);

	
	List<CmsCIRelation> getToCIRelations(@Param("toId") long toId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName, @Param("fromShortClazzName") String fromShortClazzName);
	List<CmsCIRelation> getToCIRelationsByFromCiIDs(@Param("toId") long toId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromCiIds") List<Long> fromCiIds);
	List<CmsCIRelation> getToCIRelationsByNS(@Param("toId") long toId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName, @Param("fromShortClazzName") String fromShortClazzName, @Param("fromNsPath") String fromNsPath);
	List<CmsCIRelation> getToCIRelationsByNSLike(@Param("toId") long toId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName, @Param("fromShortClazzName") String fromShortClazzName, @Param("fromNsPath") String fromNsPath, @Param("fromNsPathLike") String fromNsPathLike);
	long getCountToCIRelationsByNS(@Param("toId") long toId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName, @Param("fromNsPath") String fromNsPath);
	long getCountToCIRelationsByNSLike(@Param("toId") long toId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName, @Param("fromNsPath") String fromNsPath, @Param("fromNsPathLike") String fromNsPathLike);

	List<Map<String, Object>> getRelationCounts(@Param("relationName") String relationName,
												@Param("shortRelName") String shortRelName,
												@Param("ns") String ns,
												@Param("nsLike") String nsLike,
												@Param("fromCiId") Long fromCiId,
												@Param("toCiId") Long toCiId,
												@Param("fromClazzName") String fromClazzName,
												@Param("fromShortClazzName") String fromShortClazzName,
												@Param("toClazzName") String toClazzName,
												@Param("toShortClazzName") String toShortClazzName,
												@Param("groupBy") String groupBy,
												@Param("conditions") List<AttrQueryCondition> attrList);

	List<CmsCIRelationAttribute> getCIRelationAttrs(long ciRelId);
	List<CmsCIRelationAttribute> getCIRelationAttrsNaked(long ciRelId);

	List<CmsCIRelation> getCIRelationsByFromCiIDs(@Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromCiIds") List<Long> fromCiIds);
	List<CmsCIRelation> getCIRelationsByToCiIDs(@Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toCiIds") List<Long> toCiIds);

	List<CmsCIRelationAttribute> getCIRelationAttrsNakedByRelIdList(@Param("relIds") List<Long> relIds);
	
	
	List<CmsCIRelation> getFromCIRelationsByAttrs(
			@Param("fromId") long fromId, 
			@Param("relationName") String relationName, 
			@Param("shortRelName") String shortRelName, 
			@Param("toClazzName") String toClazzName, 
			@Param("attrList") List<AttrQueryCondition> attrList);

	List<CmsCIRelation> getToCIRelationsByAttrs(
			@Param("toId") long toId, 
			@Param("relationName") String relationName, 
			@Param("shortRelName") String shortRelName, 
			@Param("fromClazzName") String fromClazzName, 
			@Param("attrList") List<AttrQueryCondition> attrList);

	List<CmsCIRelation> getFromToCIRelations(@Param("fromId") long fromId, @Param("relationName") String relationName, @Param("toId") long toId);
	
	List<CmsCI> getCiByName(@Param("pvalue") String pvalue, @Param("oper") String oper);

	List<HashMap<String, Object>> getEnvState(String nsPath);


	void createAltNs(@Param("nsId")long nsId, @Param("tag")String tag, @Param("ciId")long ciId);

	long deleteAltNs(@Param("nsId")long nsId, @Param("ciId")long ciId);

	List<CmsCI> getCmCIByAltNsAndTag(@Param("ns") String path, 
                                     @Param("clazzName") String clazzName,
									 @Param("shortClazz") String shortName,
									 @Param("altNs") String altNsPath,
									 @Param("tag") String tag);


	List<CmsCI> getCmCIByAltNsAndTagNsLike(@Param("nsLike") String nsLike, 
                                           @Param("ns") String ns, 
                                           @Param("clazzName") String clazzName,
										   @Param("shortClazz") String shortName,
										   @Param("altNs") String altNsPath,
										   @Param("tag") String tag);

	List<CmsAltNs> getAltNsByCiAndTag(@Param("ciId") long ciId, @Param("tag") String tag);

	long getPlatformCiCount4PackTemplate(@Param("platformClass") String platformClass, @Param("platAttrList") List<AttrQueryCondition> platformAttrList,
			@Param("requiresRelation") String requiresRelation, @Param("tmplCiName") String tmplCiName);

	long getPlatformRelCount4PackRel(@Param("platformClass") String platformClass, @Param("platAttrList") List<AttrQueryCondition> platformAttrList,
			@Param("requiresRelation") String requiresRelation, @Param("fromTmplCiName") String fromTmplCiName, 
			@Param("toTmplCiName") String toTmplCiName);
}
