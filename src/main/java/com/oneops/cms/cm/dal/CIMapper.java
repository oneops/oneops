package com.oneops.cms.cm.dal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.domain.CmsLink;
import com.oneops.cms.util.domain.AttrQueryCondition;

public interface CIMapper {
	long getNextCmId();
	long getCIidByGoid(String goid);
	Integer getCiStateId(String stateName);
	
	void createCI(CmsCI ci);
	void updateCI(CmsCI ci);
	void deleteCI(@Param("ciId") long ciId, @Param("delete4real") boolean delete4real);

	void addCIAttribute(CmsCIAttribute attr);
    void addCIAttributeAndPublish(CmsCIAttribute attr);
	void updateCIAttribute(CmsCIAttribute attr);

	void createRelation(CmsCIRelation rel);
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
	List<CmsCI> getCIbyAttributes(@Param("ns") String ns, @Param("clazz") String clazz, @Param("attrList") List<AttrQueryCondition> attrList);
	List<CmsCI> getCIbyAttributesNsLike(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("clazz") String clazz, @Param("attrList") List<AttrQueryCondition> attrList);

	List<CmsCIAttribute> getCIAttrs(long ciId); 
	List<CmsCIAttribute> getCIAttrsByCiIdList(@Param("ciIds") List<Long> ciIds);
	List<CmsCIAttribute> getCIAttrsNaked(long ciId);
	List<CmsCIAttribute> getCIAttrsNakedByCiIdList(@Param("ciIds") List<Long> ciIds);
	
	long getCountBy3(@Param("ns") String ns, @Param("clazz") String clazz, @Param("name") String name);
	long getCountBy3NsLike(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("clazz") String clazz, @Param("name") String name);
	List<Map<String,Object>> getCountBy3NsLikeGroupByNs(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("clazz") String clazz, @Param("name") String name);
	
	CmsCIRelation getCIRelation(long ciRelationId);
	List<CmsCIRelation> getCIRelations(@Param("nsPath") String nsPath, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName, @Param("fromShortClazzName") String fromShortClazzName, @Param("toClazzName") String toClazzName, @Param("toShortClazzName") String toShortClazzName);
	List<CmsCIRelation> getCIRelationsByState(@Param("nsPath") String nsPath, @Param("relationName") String relationName, @Param("ciState") String ciState, @Param("fromClazzName") String fromClazzName, @Param("toClazzName") String toClazzName);
	List<CmsCIRelation> getCIRelationsNsLike(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName, @Param("fromShortClazzName") String fromShortClazzName, @Param("toClazzName") String toClazzName, @Param("toShortClazzName") String toShortClazzName);
	
	List<CmsCIRelation> getFromCIRelations(@Param("fromId") long fromId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toClazzName") String toClazzName, @Param("toShortClazzName") String toShortClazzName);
	List<CmsCIRelation> getFromCIRelationsByToClassAndCiName(@Param("fromId") long fromId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toClazzName") String toClazzName,@Param("toCiName") String toCiName);
	List<CmsCIRelation> getFromCIRelationsByToCiIDs(@Param("fromId") long fromId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toCiIds") List<Long> toCiIds);
	List<CmsCIRelation> getFromCIRelationsByNS(@Param("fromId") long fromId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toClazzName") String toClazzName, @Param("toShortClazzName") String toShortClazzName, @Param("toNsPath") String toNsPath);
	List<CmsCIRelation> getFromCIRelationsByNSLike(@Param("fromId") long fromId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toClazzName") String toClazzName, @Param("toShortClazzName") String toShortClazzName, @Param("toNsPath") String toNsPath, @Param("toNsPathLike") String toNsPathLike);
	//List<CmsCIRelation> getFromCIRelationsShortName(@Param("fromId") long fromId, @Param("shortRelName") String shortRelName, @Param("toClazzName") String toClazzName);
	long getCountFromCIRelationsByNS(@Param("fromId") long fromId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toClazzName") String toClazzName, @Param("toNsPath") String toNsPath);
	long getCountFromCIRelationsByNSLike(@Param("fromId") long fromId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toClazzName") String toClazzName, @Param("toNsPath") String toNsPath, @Param("toNsPathLike") String toNsPathLike);
	List<Map<String,Object>> getCountFromCIRelationsByNSLikeGroupByNs(@Param("fromId") long fromId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toClazzName") String toClazzName, @Param("toNsPath") String toNsPath, @Param("toNsPathLike") String toNsPathLike);
	List<Map<String,Object>> getCountCIRelationsByNSLikeGroupByFromCiId(@Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toClazzName") String toClazzName, @Param("ns") String ns, @Param("nsLike") String nsLike);
	
	
	List<CmsCIRelation> getToCIRelations(@Param("toId") long toId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName, @Param("fromShortClazzName") String fromShortClazzName);
	List<CmsCIRelation> getToCIRelationsByFromCiIDs(@Param("toId") long toId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromCiIds") List<Long> fromCiIds);
	List<CmsCIRelation> getToCIRelationsByNS(@Param("toId") long toId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName, @Param("fromShortClazzName") String fromShortClazzName, @Param("fromNsPath") String fromNsPath);
	List<CmsCIRelation> getToCIRelationsByNSLike(@Param("toId") long toId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName, @Param("fromShortClazzName") String fromShortClazzName, @Param("fromNsPath") String fromNsPath, @Param("fromNsPathLike") String fromNsPathLike);
	//List<CmsCIRelation> getToCIRelationsShortName(@Param("toId") long toId, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName);
	long getCountToCIRelationsByNS(@Param("toId") long toId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName, @Param("fromNsPath") String fromNsPath);
	long getCountToCIRelationsByNSLike(@Param("toId") long toId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName, @Param("fromNsPath") String fromNsPath, @Param("fromNsPathLike") String fromNsPathLike);
	List<Map<String,Object>> getCountToCIRelationsByNSLikeGroupByNs(@Param("toId") long toId, @Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String fromClazzName, @Param("fromNsPath") String fromNsPath, @Param("fromNsPathLike") String fromNsPathLike);
	List<Map<String,Object>> getCountCIRelationsByNSLikeGroupByToCiId(@Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromClazzName") String toClazzName, @Param("ns") String ns, @Param("nsLike") String nsLike);
	
	List<CmsCIRelationAttribute> getCIRelationAttrs(long ciRelId);
	List<CmsCIRelationAttribute> getCIRelationAttrsNaked(long ciRelId);
	
	List<CmsCIRelationAttribute> getCIRelationAttrsByRelIdList(@Param("relIds") List<Long> relIds);
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
	List<CmsLink> getLinks(@Param("nsPath") String nsPath, @Param("relName") String relName);
	
}
