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
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.cms.util.domain.CmsVar;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The Class CmsCmManagerImpl.
 */
public class CmsCmManagerImpl implements CmsCmManager {

    private CmsCmProcessor cmProcessor;
    private CmsRfcProcessor rfcProcessor;

    /**
     * Sets the rfc processor.
     *
     * @param rfcProcessor the new rfc processor
     */

    public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
        this.rfcProcessor = rfcProcessor;
    }

    /**
     * Sets the cm processor.
     *
     * @param cmProcessor the new cm processor
     */
    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#createCI(com.oneops.cms.cm.domain.CmsCI)
     */
    @Override
    public CmsCI createCI(CmsCI ci) {
        return cmProcessor.createCI(ci);
    }


    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCiByGoid(java.lang.String)
     */
    @Override
    public CmsCI getCiByGoid(String goid) {
        return cmProcessor.getCiByGoid(goid);
    }


    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCiBy3(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<CmsCI> getCiBy3(String ns, String clazzName, String ciName) {
        return cmProcessor.getCiBy3(ns, clazzName, ciName);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCountBy3(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public long getCountBy3(String ns, String clazzName, String ciName, boolean recursive) {
        return cmProcessor.getCountBy3(ns, clazzName, ciName, recursive);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCiBy3NsLike(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<CmsCI> getCiBy3NsLike(String ns, String clazzName, String ciName) {
        return cmProcessor.getCiBy3NsLike(ns, clazzName, ciName);
    }


    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCiByAttributes(java.lang.String, java.lang.String, java.util.List, boolean)
     */
    @Override
    public List<CmsCI> getCiByAttributes(String ns, String clazz, List<AttrQueryCondition> attrs, boolean recursive) {
        return cmProcessor.getCiByAttributes(ns, clazz, attrs, recursive);
    }

    
    @Override
    public List<CmsCI> getCiByAttributes(String ns, String clazz, List<AttrQueryCondition> attrs, boolean recursive, String altNs, String tag) {
        return cmProcessor.getCiByAttributes(ns, clazz, attrs, recursive, altNs, tag);
    }


    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCiById(long)
     */
    @Override
    public CmsCI getCiById(long id) {
        return cmProcessor.getCiById(id);
    }


    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#updateCI(com.oneops.cms.cm.domain.CmsCI)
     */
    @Override
    public CmsCI updateCI(CmsCI ci) {
        return cmProcessor.updateCI(ci);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#deleteCI(long)
     */
    @Override
    public void deleteCI(long ciId, String userId) {
        cmProcessor.deleteCI(ciId, userId);
    }


    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#createRelation(com.oneops.cms.cm.domain.CmsCIRelation)
     */
    @Override
    public CmsCIRelation createRelation(CmsCIRelation relation) {
        return cmProcessor.createRelation(relation);
    }


    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getRelationById(long)
     */
    @Override
    public CmsCIRelation getRelationById(long relId) {
        return cmProcessor.getRelationById(relId);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getFromCIRelations(long, java.lang.String, java.lang.String)
     */
    @Override
    public List<CmsCIRelation> getFromCIRelations(long fromId,
                                                  String relationName, String toClazzName) {
        return cmProcessor.getFromCIRelations(fromId, relationName, toClazzName);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getFromCIRelations(long, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<CmsCIRelation> getFromCIRelations(long fromId,
                                                  String relationName, String shortRelationName, String toClazzName) {
        return cmProcessor.getFromCIRelations(fromId, relationName, shortRelationName, toClazzName);
    }


    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getToCIRelations(long, java.lang.String, java.lang.String)
     */
    @Override
    public List<CmsCIRelation> getToCIRelations(long toId, String relationName,
                                                String fromClazzName) {
        return cmProcessor.getToCIRelations(toId, relationName, fromClazzName);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getToCIRelations(long, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<CmsCIRelation> getToCIRelations(long toId, String relationName,
                                                String shortRelationName, String fromClazzName) {
        return cmProcessor.getToCIRelations(toId, relationName, shortRelationName, fromClazzName);
    }


    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getAllCIRelations(long)
     */
    @Override
    public List<CmsCIRelation> getAllCIRelations(long ciId) {
        return cmProcessor.getAllCIRelations(ciId);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#deleteRelation(long)
     */
    @Override
    public void deleteRelation(long relId) {
        cmProcessor.deleteRelation(relId);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#updateRelation(com.oneops.cms.cm.domain.CmsCIRelation)
     */
    @Override
    public CmsCIRelation updateRelation(CmsCIRelation relation) {
        return cmProcessor.updateRelation(relation);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCIRelations(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<CmsCIRelation> getCIRelations(String nsPath,
                                              String relationName, String shortRelName, String fromClazzName,
                                              String toClazzName) {
        return cmProcessor.getCIRelationsNaked(nsPath, relationName, shortRelName, fromClazzName, toClazzName);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCIRelationsNsLike(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<CmsCIRelation> getCIRelationsNsLike(String nsPath,
                                                    String relationName, String shortRelName, String fromClazzName,
                                                    String toClazzName) {
        return cmProcessor.getCIRelationsNsLikeNaked(nsPath, relationName, shortRelName, fromClazzName, toClazzName);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCiByName(java.lang.String, java.lang.String)
     */
    @Override
    public List<CmsCI> getCiByName(String name, String oper) {
        return cmProcessor.getCiByName(name, oper);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getFromCIRelations(long, java.lang.String, java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public List<CmsCIRelation> getFromCIRelations(long fromId,
                                                  String relationName, String shortRelationName, String toClazzName,
                                                  List<AttrQueryCondition> attrs) {
        return cmProcessor.getFromCIRelationsByAttrs(fromId, relationName, shortRelationName, toClazzName, attrs);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getToCIRelations(long, java.lang.String, java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public List<CmsCIRelation> getToCIRelations(long toId, String relationName,
                                                String shortRelationName, String fromClazzName,
                                                List<AttrQueryCondition> attrs) {
        return cmProcessor.getToCIRelationsByAttrs(toId, relationName, shortRelationName, fromClazzName, attrs);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getEnvState(long)
     */
    @Override
    public Map<Long, List<Long>> getEnvState(long envId) {
        return cmProcessor.getEnvState(envId);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getFromCIRelations(long, java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public List<CmsCIRelation> getFromCIRelations(long fromId,
                                                  String relationName, String shortRelationName, List<Long> toCiIds) {
        return cmProcessor.getFromCIRelationsByToCiIds(fromId, relationName, shortRelationName, toCiIds);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getToCIRelations(long, java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public List<CmsCIRelation> getToCIRelations(long toId, String relationName,
                                                String shortRelationName, List<Long> fromCiIds) {
        return cmProcessor.getToCIRelationsByFromCiIds(toId, relationName, shortRelationName, fromCiIds);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCountBy3GroupByNs(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Map<String, Long> getCountBy3GroupByNs(String ns, String clazz,
                                                  String ci) {
        return cmProcessor.getCountBy3GroupByNs(ns, clazz, ci);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCountFromCIRelationsByNS(long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public long getCountFromCIRelationsByNS(long fromId, String relationName,
                                            String shortRelName, String toClazzName, String toNsPath,
                                            boolean recursive) {
        return cmProcessor.getCountFromCIRelationsByNS(fromId, relationName, shortRelName, toClazzName, toNsPath, recursive);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCountToCIRelationsByNS(long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public long getCountToCIRelationsByNS(long toId, String relationName,
                                          String shortRelName, String toClazzName, String toNsPath,
                                          boolean recursive) {
        return cmProcessor.getCountToCIRelationsByNS(toId, relationName, shortRelName, toClazzName, toNsPath, recursive);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCountFromCIRelationsGroupByNs(long, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Map<String, Long> getCountFromCIRelationsGroupByNs(long fromId,
                                                              String relationName, String shortRelName, String toClazzName,
                                                              String toNsPath) {
        return cmProcessor.getCountFromCIRelationsGroupByNs(fromId, relationName, shortRelName, toClazzName, toNsPath);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCountToCIRelationsGroupByNs(long, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Map<String, Long> getCountToCIRelationsGroupByNs(long toId,
                                                            String relationName, String shortRelName, String toClazzName,
                                                            String toNsPath) {
        return cmProcessor.getCountToCIRelationsGroupByNs(toId, relationName, shortRelName, toClazzName, toNsPath);
    }

    @Override
    public Map<Long, Long> getCounCIRelationsGroupByFromCiId(
            String relationName, String shortRelName, String toClazzName,
            String nsPath) {
        return cmProcessor.getCounCIRelationsGroupByFromCiId(relationName, shortRelName, toClazzName, nsPath);
    }

    @Override
    public Map<Long, Long> getCounCIRelationsGroupByToCiId(String relationName,
                                                           String shortRelName, String fromClazzName, String nsPath) {
        return cmProcessor.getCountCIRelationsGroupByToCiId(relationName, shortRelName, fromClazzName, nsPath);
    }


    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCiByIdList(java.util.List)
     */
    @Override
    public List<CmsCI> getCiByIdList(List<Long> ids) {
        return cmProcessor.getCiByIdList(ids);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmManager#getCiByIdListNaked(java.util.List)
     */
    @Override
    public List<CmsCI> getCiByIdListNaked(List<Long> ids) {
        return cmProcessor.getCiByIdListNaked(ids);
    }

    @Override
    public CmsCI updateCiState(long ciId, String ciState, String user) {
        return updateCiState(ciId, ciState, false, user);
    }

    private CmsCI updateCiState(long ciId, String ciState, boolean checkRfc, String user) {
        if (checkRfc) {
            CmsRfcCI rfc = rfcProcessor.getOpenRfcCIByCiId(ciId);
            if (rfc != null) {
                throw new CIValidationException(CmsError.CMS_EXCEPTION,
                        "There is an open rfc for this CI ci_id = " + ciId);
            }
        }
        return cmProcessor.updateCiState(ciId, ciState, user);
    }


    /**
     * Update ci state in bulk mode. Needs to be perf improved later
     * This method does relationship traversal and updates all related cis with new state
     *
     * @param ids, String new ciState  String relName, String direction, boolean recursive
     * @return void
     */

    @Override
    public void updateCiStateBulk(Long[] ids, String ciState, String relName,
                                  String direction, boolean recursive, String user) {

        List<Long> idList = Arrays.asList(ids);

        List<CmsRfcCI> openRfcs = rfcProcessor.getOpenRfcCIByCiIdListNoAttrs(idList);

        if (openRfcs.size() > 0) {
            List<String> badCiIds = new ArrayList<String>(openRfcs.size());
            for (CmsRfcCI rfc : openRfcs) {
                badCiIds.add(String.valueOf(rfc.getCiId()));
            }
            throw new CIValidationException(CmsError.CMS_EXCEPTION,
                    "There are an open rfc for cis: " + badCiIds.toString());
        }

        for (long ciId : ids) {
            updateCiState(ciId, ciState, relName, direction, recursive, false, user);
        }
    }

    /**
     * Update ci state.
     * This method does relationship traversal and updates all related cis with new state
     *
     * @param ciId, String new ciState  String relName, String direction, boolean recursive
     * @return void
     */
    public void updateCiState(long ciId, String ciState, String relName,
                              String direction, boolean recursive, String user) {
        updateCiState(ciId, ciState, relName, direction, recursive, true, user);
    }

    /**
     * Update the cm_ci_state of the given <code>ciId</code> to new <code>ciState</code>.
     *
     * @param ciId
     * @param ciState
     * @param relName
     * @param direction
     * @param recursive
     * @param checkRfc
     */
    private void updateCiState(long ciId, String ciState, String relName,
                               String direction, boolean recursive, boolean checkRfc, String user) {

        updateCiState(ciId, ciState, checkRfc, user);
        if (relName != null) {
            if (direction == null || "to".equals(direction)) {
                List<CmsCIRelation> rels = cmProcessor.getToCIRelationsNakedNoAttrs(ciId, relName, null, null);
                for (CmsCIRelation rel : rels) {
                    if (recursive) {
                        updateCiState(rel.getFromCiId(), ciState, relName, direction, true, checkRfc, user);
                    } else {
                        updateCiState(rel.getFromCiId(), ciState, checkRfc, user);
                    }
                }
            } else {
                List<CmsCIRelation> rels = cmProcessor.getFromCIRelationsNakedNoAttrs(ciId, relName, null, null);
                for (CmsCIRelation rel : rels) {
                    if (recursive) {
                        updateCiState(rel.getToCiId(), ciState, relName, direction, true, checkRfc, user);
                    } else {
                        updateCiState(rel.getToCiId(), ciState, checkRfc, user);
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmProcessor#getFromCIRelationsByNs(long fromId,
            String relationName, String shortRelName, String toClazzName,
            String toNsPath)
     */
    @Override
    public List<CmsCIRelation> getFromCIRelationsByNs(long fromId,
                                                      String relationName, String shortRelName, String toClazzName,
                                                      String toNsPath) {
        return cmProcessor.getFromCIRelationsByNs(fromId, relationName, shortRelName, toClazzName, toNsPath);
    }

    /* (non-Javadoc)
     * @see com.oneops.cms.cm.service.CmsCmProcessor#getToCIRelationsByNs(long toId,
            String relationName, String shortRelName, String fromClazzName,
            String fromNsPath)
     */
    @Override
    public List<CmsCIRelation> getToCIRelationsByNs(long toId,
                                                    String relationName, String shortRelName, String fromClazzName,
                                                    String fromNsPath) {
        return cmProcessor.getToCIRelationsByNs(toId, relationName, shortRelName, fromClazzName, fromNsPath);
    }

    @Override
    public void populateRelCis(List<CmsCIRelation> rels, boolean fromCis,
                               boolean toCis) {
        cmProcessor.populateRelCis(rels, fromCis, toCis);
    }

    @Override
    public void updateCmSimpleVar(String varName, String varValue, String criteria, String updatedBy) {
        cmProcessor.updateCmSimpleVar(varName, varValue, criteria, updatedBy);
    }

    @Override
    public CmsVar getCmSimpleVar(String varName) {
        return cmProcessor.getCmSimpleVar(varName);
    }

    @Override
    public CmsVar getCmSimpleVar(String varName, String criteria) {
        List<CmsVar> vars = cmProcessor.getCmVarByLongestMatchingCriteria(varName, criteria);
        return vars == null || vars.size() == 0 ? null : vars.get(0);
    }

    @Override
    public void deleteAltNs(long nsId, long ciId) {
        cmProcessor.deleteAltNs(nsId, ciId);
    }

    public void createAltNs(CmsAltNs cmsAltNs, CmsCI ci) {
        cmProcessor.createAltNs(cmsAltNs, ci.getCiId());
    }

    @Override
    public List<CmsAltNs> getAltNsByCiAndTag(long ciId, String tag) {
        return cmProcessor.getAltNsByCiAndTag(ciId, tag);
    }

    public List<CmsCI> getCmCIByAltNsAndTag(String nsPath,
                                            String clazzName,
                                            String altNsPath, String tag,
                                            boolean recursive) {
        return cmProcessor.getCmCIByAltNsAndTag(nsPath, clazzName, altNsPath, tag, recursive);
    }

    @Override
    public void updateCiAltNs(long ciId, Map<String, Set<String>> desiredAltNsMap) {

        Map<String, List<CmsAltNs>> currentAltNsMap = getAltNsByCiAndTag(ciId, null).stream().collect(Collectors.groupingBy(CmsAltNs::getTag)); // all CI altNSs grouped by tag
        for (String tag : desiredAltNsMap.keySet()) {
            Set<String> desiredTagAltNss = desiredAltNsMap.get(tag);
            List<CmsAltNs> actualTagAltNss = currentAltNsMap.remove(tag);
            if (actualTagAltNss!=null) {
                for (CmsAltNs altNs : actualTagAltNss) {
                    if (!desiredTagAltNss.contains(altNs.getNsPath())) { // delete obsolete
                        cmProcessor.deleteAltNs(altNs.getNsId(), ciId);
                    } else {
                        desiredTagAltNss.remove(altNs.getNsPath());
                    }
                }
                addAllAltNss(ciId, tag, desiredTagAltNss);
            } else {
                addAllAltNss(ciId, tag, desiredTagAltNss);
            }
        }

        for (String tag : currentAltNsMap.keySet()) { // delete obsolete
            for (CmsAltNs altNs : currentAltNsMap.get(tag)) {
                cmProcessor.deleteAltNs(altNs.getNsId(), ciId);
            }
        }


    }

    private void addAllAltNss(long ciId, String tag, Set<String> desiredTagAltNss) {
        for (String altNsPath : desiredTagAltNss) {
            addNewAltNs(ciId, tag, altNsPath);
        }
    }

    private void addNewAltNs(long ciId, String tag, String altNs) {
        CmsAltNs cmsAltNs = new CmsAltNs();
        cmsAltNs.setTag(tag);
        cmsAltNs.setNsPath(altNs);
        cmProcessor.createAltNs(cmsAltNs, ciId);
    }

    @Override
    public CmsVar getCmVarByLongestMatchingCriteria(String varNameLike, String criteria) {
        List<CmsVar> list = cmProcessor.getCmVarByLongestMatchingCriteria(varNameLike, criteria);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}
