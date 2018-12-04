package com.oneops.cms.transmitter;

import com.oneops.cms.cm.dal.CIMapper;
import com.oneops.cms.cm.domain.CmsAltNs;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.util.domain.AttrQueryCondition;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CIMapperImpl implements CIMapper {

  Map<Long, CmsCI> ciMap = new HashMap<>();

  Map<Long, List<CmsCIAttribute>> ciAttributesMap = new HashMap<>();

  public void addCI(CmsCI ci, List<CmsCIAttribute> attributes) {
    ciMap.put(ci.getCiId(), ci);
    ciAttributesMap.put(ci.getCiId(), attributes);
  }

  @Override
  public long getNextCmId() {
    return 0;
  }

  @Override
  public long getCIidByGoid(String goid) {
    return 0;
  }

  @Override
  public Integer getCiStateId(String stateName) {
    return null;
  }

  @Override
  public void createCI(CmsCI ci) {

  }

  @Override
  public void updateCI(CmsCI ci) {

  }

  @Override
  public void deleteCI(long ciId, boolean delete4real, String userId) {

  }

  @Override
  public void addCIAttribute(CmsCIAttribute attr) {

  }

  @Override
  public void addCIAttributeAndPublish(CmsCIAttribute attr) {

  }

  @Override
  public void updateCIAttribute(CmsCIAttribute attr) {

  }

  @Override
  public void createRelation(CmsCIRelation rel) {

  }

  @Override
  public void updateRelation(CmsCIRelation rel) {

  }

  @Override
  public void addRelationAttribute(CmsCIRelationAttribute attr) {

  }

  @Override
  public void addRelationAttributeAndPublish(CmsCIRelationAttribute attr) {

  }

  @Override
  public void deleteRelation(long ciId, boolean delete4real) {

  }

  @Override
  public void updateCIRelationAttribute(CmsCIRelationAttribute attr) {

  }

  @Override
  public void resetDeletionsByNsLike(String ns, String nsLike) {

  }

  @Override
  public void resetRelDeletionsByNsLike(String ns, String nsLike) {

  }

  @Override
  public CmsCI getCIbyGoid(String goid) {
    return null;
  }

  @Override
  public CmsCI getCIById(long id) {
    return ciMap.get(id);
  }

  @Override
  public List<CmsCI> getCIByIdList(List<Long> ciIds) {
    return null;
  }

  @Override
  public List<CmsCI> getCIby3(String ns, String clazz, String shortClazz, String name) {
    return ciMap.entrySet().stream()
        .map(Map.Entry::getValue)
        .filter(ci -> (ns == null || ns.equals(ci.getNsPath())) && (clazz == null || clazz.equals(ci.getCiClassName()))
          && (name.equals(ci.getCiName())))
        .collect(Collectors.toList());
  }

  @Override
  public List<CmsCI> getCIby3lower(String ns, String clazz, String shortClazz, String name) {
    return null;
  }

  @Override
  public List<CmsCI> getCIby3NsLike(String ns, String nsLike, String clazz, String shortClazz,
      String name) {
    return null;
  }

  @Override
  public List<CmsCI> getCIbyStateNsLike(String ns, String nsLike, String clazz, String state) {
    return null;
  }

  @Override
  public List<CmsCI> getCIby3with2Names(String ns, String clazz, String name, String altName) {
    return null;
  }

  @Override
  public List<CmsCI> getCIbyAttributes(String ns, String clazz, String shortName,
      List<AttrQueryCondition> attrList) {
    return null;
  }

  @Override
  public List<CmsCI> getCIbyAttributesNsLike(String ns, String nsLike, String clazz,
      String shortName, List<AttrQueryCondition> attrList) {
    return null;
  }

  @Override
  public List<CmsCI> getCIbyAttributesWithAltNs(String ns, String clazz, String shortName,
      List<AttrQueryCondition> attrList, String altNs, String tag) {
    return null;
  }

  @Override
  public List<CmsCI> getCIbyAttributesNsLikeWithAltNs(String ns, String nsLike, String clazz,
      String shortName, List<AttrQueryCondition> attrList, String altNs, String tag) {
    return null;
  }

  @Override
  public List<CmsCIAttribute> getCIAttrs(long ciId) {
    return ciAttributesMap.get(ciId);
  }

  @Override
  public List<CmsCIAttribute> getCIAttrsNaked(long ciId) {
    return null;
  }

  @Override
  public List<CmsCIAttribute> getCIAttrsNakedByCiIdList(List<Long> ciIds) {
    return null;
  }

  @Override
  public long getCountBy3(String ns, String clazz, String name) {
    return 0;
  }

  @Override
  public long getCountBy3NsLike(String ns, String nsLike, String clazz, String name) {
    return 0;
  }

  @Override
  public List<Map<String, Object>> getCountBy3NsLikeGroupByNs(String ns, String nsLike,
      String clazz, String name) {
    return null;
  }

  @Override
  public CmsCIRelation getCIRelation(long ciRelationId) {
    return null;
  }

  @Override
  public List<CmsCIRelation> getCIRelations(String nsPath, String nsLike, String relationName, String shortRelName, String fromClazzName, String fromShortClazzName, String toClazzName, String toShortClazzName, List<AttrQueryCondition> attrList) {
    return null;
  }

  @Override
  public List<CmsCIRelation> getCIRelationsByState(String nsPath, String relationName,
      String ciState, String fromClazzName, String toClazzName) {
    return null;
  }

  @Override
  public List<CmsCIRelation> getCIRelationsByStateNsLike(String ns, String nsLike,
      List<String> relationNames, String ciState, String fromClazzName, String toClazzName) {
    return null;
  }

  @Override
  public List<CmsCIRelation> getFromCIRelations(long fromId, String relationName,
      String shortRelName, String toClazzName, String toShortClazzName) {
    return null;
  }

  @Override
  public List<CmsCIRelation> getFromCIRelationsByToClassAndCiName(long fromId, String relationName,
      String shortRelName, String toClazzName, String toCiName) {
    return null;
  }

  @Override
  public List<CmsCIRelation> getFromCIRelationsByToCiIDs(long fromId, String relationName,
      String shortRelName, List<Long> toCiIds) {
    return null;
  }

  @Override
  public List<CmsCIRelation> getFromCIRelationsByMultiRelationNames(long fromId,
      List<String> relationNames, List<String> shortRelNames) {
    return null;
  }

  @Override
  public long getCountFromCIRelationsByNS(long fromId, String relationName, String shortRelName,
      String toClazzName, String toNsPath) {
    return 0;
  }

  @Override
  public long getCountFromCIRelationsByNSLike(long fromId, String relationName, String shortRelName,
      String toClazzName, String toNsPath, String toNsPathLike) {
    return 0;
  }

  @Override
  public List<CmsCIRelation> getToCIRelations(long toId, String relationName, String shortRelName,
      String fromClazzName, String fromShortClazzName) {
    return null;
  }

  @Override
  public List<CmsCIRelation> getToCIRelationsByFromCiIDs(long toId, String relationName,
      String shortRelName, List<Long> fromCiIds) {
    return null;
  }

  @Override
  public List<CmsCIRelation> getToCIRelationsByNS(long toId, String relationName,
      String shortRelName, String fromClazzName, String fromShortClazzName, String fromNsPath) {
    return null;
  }

  @Override
  public List<CmsCIRelation> getToCIRelationsByNSLike(long toId, String relationName,
      String shortRelName, String fromClazzName, String fromShortClazzName, String fromNsPath,
      String fromNsPathLike) {
    return null;
  }

  @Override
  public long getCountToCIRelationsByNS(long toId, String relationName, String shortRelName,
      String fromClazzName, String fromNsPath) {
    return 0;
  }

  @Override
  public long getCountToCIRelationsByNSLike(long toId, String relationName, String shortRelName,
      String fromClazzName, String fromNsPath, String fromNsPathLike) {
    return 0;
  }

  @Override
  public List<Map<String, Object>> getRelationCounts(String relationName,
                                                     String shortRelName,
                                                     String ns,
                                                     String nsLike,
                                                     Long fromCiId,
                                                     Long toCiId,
                                                     String fromClazzName,
                                                     String fromShortClazzName,
                                                     String toClazzName,
                                                     String toShortClazzName,
                                                     String groupBy,
                                                     List<AttrQueryCondition> attrList) {
    return null;
  }

  @Override
  public List<CmsCIRelationAttribute> getCIRelationAttrs(long ciRelId) {
    return null;
  }

  @Override
  public List<CmsCIRelationAttribute> getCIRelationAttrsNaked(long ciRelId) {
    return null;
  }

  @Override
  public List<CmsCIRelationAttribute> getCIRelationAttrsNakedByRelIdList(List<Long> relIds) {
    return null;
  }

  @Override
  public List<CmsCIRelation> getFromCIRelationsByAttrs(long fromId, String relationName,
      String shortRelName, String toClazzName, List<AttrQueryCondition> attrList) {
    return null;
  }

  @Override
  public List<CmsCIRelation> getToCIRelationsByAttrs(long toId, String relationName,
      String shortRelName, String fromClazzName, List<AttrQueryCondition> attrList) {
    return null;
  }

  @Override
  public List<CmsCIRelation> getFromToCIRelations(long fromId, String relationName, long toId) {
    return null;
  }

  @Override
  public List<CmsCI> getCiByName(String pvalue, String oper) {
    return null;
  }

  @Override
  public List<HashMap<String, Object>> getEnvState(String nsPath) {
    return null;
  }

  @Override
  public void createAltNs(long nsId, String tag, long ciId) {

  }

  @Override
  public long deleteAltNs(long nsId, long ciId) {
    return 0;
  }

  @Override
  public List<CmsCI> getCmCIByAltNsAndTag(String path, String clazzName, String shortName,
      String altNsPath, String tag) {
    return null;
  }

  @Override
  public List<CmsCI> getCmCIByAltNsAndTagNsLike(String nsLike, String ns, String clazzName,
      String shortName, String altNsPath, String tag) {
    return null;
  }

  @Override
  public List<CmsAltNs> getAltNsByCiAndTag(long ciId, String tag) {
    return null;
  }

  @Override
  public long getPlatformCiCount4PackTemplate(String platformClass,
      List<AttrQueryCondition> platformAttrList, String requiresRelation, String tmplCiName) {
    return 0;
  }

  @Override
  public long getPlatformRelCount4PackRel(String platformClass,
      List<AttrQueryCondition> platformAttrList, String requiresRelation, String fromTmplCiName,
      String toTmplCiName) {
    return 0;
  }

  @Override
  public List<CmsCIRelation> getCIRelationsByFromCiIDs(@Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("fromCiIds") List<Long> fromCiIds) {
    return null;
  }

  @Override
  public List<CmsCIRelation> getCIRelationsByToCiIDs(@Param("relationName") String relationName, @Param("shortRelName") String shortRelName, @Param("toCiIds") List<Long> toCiIds) {
    return null;
  }
}
