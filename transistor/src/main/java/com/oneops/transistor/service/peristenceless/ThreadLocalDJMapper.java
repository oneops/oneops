package com.oneops.transistor.service.peristenceless;

import com.oneops.cms.cm.domain.CmsAltNs;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.dj.domain.*;
import com.oneops.cms.util.TimelineQueryParam;

import java.util.Date;
import java.util.List;
import java.util.Set;

/*******************************************************************************
 *
 *   Copyright 2016 Walmart, Inc.
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
public class ThreadLocalDJMapper implements DJMapper{
     private ThreadLocal<InMemoryDJMapper> mapper = new ThreadLocal<>();
    
     
     public InMemoryDJMapper get(){
         return mapper.get();
     }
     
     public void set(InMemoryDJMapper mapper){
         this.mapper.set(mapper);
     }
    
    @Override
    public long getNextDjId() {
        return get().getNextDjId();
    }

    @Override
    public long getNextCiId() {
        return get().getNextCiId();
    }

    @Override
    public Integer getReleaseStateId(String stateName) {
        return get().getReleaseStateId(stateName);
    }

    @Override
    public Integer getRfcCiActionId(String stateName) {
        return get().getRfcCiActionId(stateName);
    }

    @Override
    public void createRelease(CmsRelease release) {
        get().createRelease(release);
    }

    @Override
    public void brushReleaseExecOrder(long releaseId) {
        get().brushReleaseExecOrder(releaseId);
    }

    @Override
    public CmsRelease getReleaseById(long releaseId) {
        return get().getReleaseById(releaseId);
    }

    @Override
    public List<CmsRelease> getReleaseBy3(String nsPath, String releaseName, String releaseState) {
        return get().getReleaseBy3(nsPath, releaseName, releaseState);
    }

    public List<CmsRfcCI> getRfcCIByReleaseAndClass(long releaseId, String className) {
        return get().getRfcCIByReleaseAndClass(releaseId, className);
    }

    @Override
    public List<CmsRelease> getLatestRelease(String nsPath, String releaseState) {
        return get().getLatestRelease(nsPath, releaseState);
    }

    @Override
    public int updateRelease(CmsRelease release) {
        return get().updateRelease(release);
    }

    @Override
    public int deleteRelease(long releaseId) {
        return get().deleteRelease(releaseId);
    }

    @Override
    public void commitRelease(long releaseId, Boolean setDfValue, Integer newCiState, boolean delete4real, String userId, String desc) {
        get().commitRelease(releaseId, setDfValue, newCiState, delete4real, userId, desc);
    }

    @Override
    public void createRfcCI(CmsRfcCI rfcCi) {
        get().createRfcCI(rfcCi);
    }

    @Override
    public void createRfcLog(CmsRfcCI rfcCi) {
        get().createRfcLog(rfcCi);
    }

    @Override
    public int rmRfcCIfromRelease(long rfcId) {
        return get().rmRfcCIfromRelease(rfcId);
    }

    @Override
    public void updateRfcCI(CmsRfcCI rfcCI) {
        get().updateRfcCI(rfcCI);
    }

    @Override
    public void updateRfcLog(CmsRfcCI rfcCI) {
        get().updateRfcLog(rfcCI);
    }

    @Override
    public void insertRfcCIAttribute(CmsRfcAttribute attr) {
        get().insertRfcCIAttribute(attr);
    }

    @Override
    public void updateRfcCIAttribute(CmsRfcAttribute attr) {
        get().updateRfcCIAttribute(attr);
    }

    @Override
    public CmsRfcCI getRfcCIById(long rfcId) {
        return get().getRfcCIById(rfcId);
    }

    @Override
    public CmsRfcCI getOpenRfcCIByCiId(long ciId) {
        return get().getOpenRfcCIByCiId(ciId);
    }

    @Override
    public CmsRfcCI getOpenRfcCIByCiIdNoAttrs(long ciId) {
        return get().getOpenRfcCIByCiIdNoAttrs(ciId);
    }

    public List<CmsRfcCI> getOpenRfcCIByCiIdList(List<Long> ciIds) {
        return get().getOpenRfcCIByCiIdList(ciIds);
    }

    @Override
    public List<CmsRfcCI> getRfcCIBy3(long releaseId, Boolean isActive, Long ciId) {
        return get().getRfcCIBy3(releaseId, isActive, ciId);
    }

    @Override
    public List<CmsRfcCI> getRfcCIByNsPathDateRangeClassName(String ns, String nsLike, Date startDate, Date endDate, String ciClassName) {
        return get().getRfcCIByNsPathDateRangeClassName(ns, nsLike, startDate, endDate, ciClassName);
    }

    @Override
    public List<CmsRfcCI> getRfcCIByClazzAndName(String nsPath, String clazzName, String ciName, Boolean isActive, String state) {
        return get().getRfcCIByClazzAndName(nsPath, clazzName, ciName, isActive, state);
    }

    @Override
    public List<CmsRfcCI> getOpenRfcCIByClazzAndNameLower(String nsPath, String clazzName, String ciName) {
        return get().getOpenRfcCIByClazzAndNameLower(nsPath, clazzName, ciName);
    }

    @Override
    public List<CmsRfcCI> getOpenRfcCIByNsLike(String ns, String nsLike, String clazzName, String ciName) {
        return get().getOpenRfcCIByNsLike(ns, nsLike, clazzName, ciName);
    }

    @Override
    public List<CmsRfcCI> getOpenRfcCIByClazzAnd2Names(String nsPath, String clazzName, String ciName, String altCiName) {
        return get().getOpenRfcCIByClazzAnd2Names(nsPath, clazzName, ciName, altCiName);
    }

    @Override
    public List<CmsRfcCI> getClosedRfcCIByCiId(long ciId) {
        return get().getClosedRfcCIByCiId(ciId);
    }

    @Override
    public List<CmsRfcAttribute> getRfcCIAttributes(long rfcId) {
        return get().getRfcCIAttributes(rfcId);
    }

    @Override
    public List<CmsRfcAttribute> getRfcCIAttributesByRfcIdList(Set<Long> rfcIds) {
        return get().getRfcCIAttributesByRfcIdList(rfcIds);
    }

    @Override
    public void createRfcRelation(CmsRfcRelation rel) {
        get().createRfcRelation(rel);
    }

    @Override
    public void createRfcRelationLog(CmsRfcRelation rel) {
        get().createRfcRelationLog(rel);
    }

    @Override
    public int rmRfcRelationfromRelease(long rfcId) {
        return get().rmRfcRelationfromRelease(rfcId);
    }

    @Override
    public int updateRfcRelation(CmsRfcRelation rel) {
        return get().updateRfcRelation(rel);
    }

    @Override
    public void updateRfcRelationLog(CmsRfcRelation rel) {
        get().updateRfcRelationLog(rel);
    }

    @Override
    public void insertRfcRelationAttribute(CmsRfcAttribute attr) {
        get().insertRfcRelationAttribute(attr);
    }

    @Override
    public void updateRfcRelationAttribute(CmsRfcAttribute attr) {
        get().updateRfcRelationAttribute(attr);
    }

    @Override
    @Deprecated
    public void upsertRfcRelationAttribute(CmsRfcAttribute attr) {
        get().upsertRfcRelationAttribute(attr);
    }

    @Override
    public CmsRfcRelation getRfcRelationById(long rfcId) {
        return get().getRfcRelationById(rfcId);
    }

    @Override
    public CmsRfcRelation getOpenRfcRelationByCiRelId(long ciRelationId) {
        return get().getOpenRfcRelationByCiRelId(ciRelationId);
    }

    @Override
    public List<CmsRfcRelation> getRfcRelationByReleaseId(long releaseId) {
        return get().getRfcRelationByReleaseId(releaseId);
    }

    @Override
    public List<CmsRfcRelation> getClosedRfcRelationByCiId(long ciId) {
        return get().getClosedRfcRelationByCiId(ciId);
    }

    @Override
    public List<CmsRfcRelation> getRfcRelationsByNs(String nsPath, Boolean isActive, String state) {
        return get().getRfcRelationsByNs(nsPath, isActive, state);
    }

    @Override
    public List<CmsRfcRelation> getRfcRelationBy4(long releaseId, Boolean isActive, Long fromCiId, Long toCiId) {
        return get().getRfcRelationBy4(releaseId, isActive, fromCiId, toCiId);
    }

    @Override
    public List<CmsRfcRelation> getOpenRfcRelationBy2(Long fromCiId, Long toCiId, String relName, String shortRelName) {
        return get().getOpenRfcRelationBy2(fromCiId, toCiId, relName, shortRelName);
    }

    @Override
    public List<CmsRfcRelation> getOpenFromRfcRelationByTargetClass(long fromCiId, String relName, String shortRelName, String targetClassName) {
        return get().getOpenFromRfcRelationByTargetClass(fromCiId, relName, shortRelName, targetClassName);
    }

    @Override
    public List<CmsRfcRelation> getOpenFromRfcRelationByAttrs(long fromCiId, String relName, String shortRelName, String targetClassName, List<CmsRfcBasicAttribute> attrList) {
        return get().getOpenFromRfcRelationByAttrs(fromCiId, relName, shortRelName, targetClassName, attrList);
    }

    @Override
    public List<CmsRfcRelation> getOpenToRfcRelationByTargetClass(long toCiId, String relName, String shortRelName, String targetClassName) {
        return get().getOpenToRfcRelationByTargetClass(toCiId, relName, shortRelName, targetClassName);
    }

    @Override
    public List<CmsRfcRelation> getOpenToRfcRelationByAttrs(long toCiId, String relName, String shortRelName, String targetClassName, List<CmsRfcBasicAttribute> attrList) {
        return get().getOpenToRfcRelationByAttrs(toCiId, relName, shortRelName, targetClassName, attrList);
    }

    @Override
    public List<CmsRfcRelation> getRfcRelationBy3(long releaseId, Boolean isActive, Long ciRelationId) {
        return get().getRfcRelationBy3(releaseId, isActive, ciRelationId);
    }

    @Override
    public List<CmsRfcRelation> getOpenRfcRelations(String relationName, String shortRelName, String nsPath, String fromClazzName, String toClazzName) {
        return get().getOpenRfcRelations(relationName, shortRelName, nsPath, fromClazzName, toClazzName);
    }

    @Override
    public List<CmsRfcRelation> getOpenRfcRelationsNsLike(String relationName, String shortRelName, String ns, String nsLike, String fromClazzName, String toClazzName) {
        return get().getOpenRfcRelationsNsLike(relationName, shortRelName, ns, nsLike, fromClazzName, toClazzName);
    }

    @Override
    public List<CmsRfcRelation> getOpenRfcRelationByCiIds(String relationName, String shortRelName, List<Long> fromCiIds, List<Long> toCiIds) {
        return get().getOpenRfcRelationByCiIds(relationName, shortRelName, fromCiIds, toCiIds);
    }

    @Override
    public List<CmsRfcRelation> getRfcRelationByReleaseAndClass(long releaseId, String relationName, String shortRelName) {
        return get().getRfcRelationByReleaseAndClass(releaseId, relationName, shortRelName);
    }

    @Override
    public List<CmsRfcAttribute> getRfcRelationAttributes(long rfcId) {
        return get().getRfcRelationAttributes(rfcId);
    }

    @Override
    public List<CmsRfcAttribute> getRfcRelationAttributesByRfcIdList(Set<Long> rfcIds) {
        return get().getRfcRelationAttributesByRfcIdList(rfcIds);
    }

    @Override
    public List<Long> getLinkedRfcRelationId(long releaseId, Boolean isActive, long rfcId) {
        return get().getLinkedRfcRelationId(releaseId, isActive, rfcId);
    }

    @Override
    public long countCiRfcByReleaseId(long releaseId) {
        return get().countCiRfcByReleaseId(releaseId);
    }

    @Override
    public long countRelationRfcByReleaseId(long releaseId) {
        return get().countRelationRfcByReleaseId(releaseId);
    }

    @Override
    public long countOpenRfcCisByNs(String nsPath) {
        return get().countOpenRfcCisByNs(nsPath);
    }

    @Override
    public long countOpenRfcRelationsByNs(String nsPath) {
        return get().countOpenRfcRelationsByNs(nsPath);
    }

    @Override
    public void rmRfcsByNs(String nsPath) {
        get().rmRfcsByNs(nsPath);
    }

    @Override
    public void rmToRelByNs(String nsPath) {
        get().rmToRelByNs(nsPath);
    }

    @Override
    public void rmFromRelByNs(String nsPath) {
        get().rmFromRelByNs(nsPath);
    }

    @Override
    public long countCiNotUpdatedByRfc(long fromCiId, String relationName, String shortRelName, long rfcId) {
        return get().countCiNotUpdatedByRfc(fromCiId, relationName, shortRelName, rfcId);
    }

    @Override
    public List<TimelineRelease> getReleasesByCiFilter(TimelineQueryParam queryParam) {
        return get().getReleasesByCiFilter(queryParam);
    }

    @Override
    public List<TimelineRelease> getReleasesByRelationFilter(TimelineQueryParam queryParam) {
        return get().getReleasesByRelationFilter(queryParam);
    }

    @Override
    public List<TimelineRelease> getReleasesByNsPath(TimelineQueryParam queryParam) {
        return get().getReleasesByNsPath(queryParam);
    }

    @Override
    public List<CmsRfcCI> getAppliedRfcCIsAfterRfcId(Long ciId, Long afterRfcId, Long toRfcId) {
        return get().getAppliedRfcCIsAfterRfcId(ciId, afterRfcId, toRfcId);
    }

    @Override
    public List<CmsRfcCI> getRfcCIsAppliedBetweenTwoReleases(String nsPath, Long fromReleaseId, Long toReleaseId) {
        return get().getRfcCIsAppliedBetweenTwoReleases(nsPath, fromReleaseId, toReleaseId);
    }

    @Override
    public List<CmsRfcRelation> getRfcRelationsAppliedBetweenTwoReleases(String nsPath, Long fromReleaseId, Long toReleaseId) {
        return get().getRfcRelationsAppliedBetweenTwoReleases(nsPath, fromReleaseId, toReleaseId);
    }

    @Override
    public Long getTagId(String tag) {
        return get().getTagId(tag);
    }

    @Override
    public void createTag(String tag) {
        get().createTag(tag);
    }

    @Override
    public void createAltNs(long nsId, long tagId, long rfcId) {
        get().createAltNs(nsId, tagId, rfcId);
    }

    @Override
    public void deleteAltNs(long nsId, long rfcId) {
        get().deleteAltNs(nsId, rfcId);
    }

    @Override
    public List<CmsRfcCI> getRfcCIByAltNsAndTag(String nsPath, String tag, Long releaseId, boolean isActive, Long ciId) {
        return get().getRfcCIByAltNsAndTag(nsPath, tag, releaseId, isActive, ciId);
    }

    @Override
    public List<CmsAltNs> getAltNsBy(long rfcCI) {
        return get().getAltNsBy(rfcCI);
    }

    @Override
    public List<Integer> getDeploymentDistinctStepsTotal(long deploymentId) { return get().getDeploymentDistinctStepsTotal(deploymentId); }
}
