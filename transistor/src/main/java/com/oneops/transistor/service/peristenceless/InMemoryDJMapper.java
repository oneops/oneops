package com.oneops.transistor.service.peristenceless;

import com.oneops.cms.cm.domain.CmsAltNs;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.dj.domain.*;
import com.oneops.cms.util.TimelineQueryParam;

import java.util.*;
import java.util.stream.Collectors;

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
public class InMemoryDJMapper implements DJMapper{
    private long djId = 1;
    private long ciId = 1;
    private Map<Long, CmsRfcCI> cis = new HashMap<>();
    private Map<Long, CmsRfcRelation> relations = new HashMap<>();
    private CmsRelease release;

    public InMemoryDJMapper() {
    }

    public Map<Long, CmsRfcCI> getCis() {
        return cis;
    }

    public Map<Long, CmsRfcRelation> getRelations() {
        return relations;
    }

    public CmsRelease getRelease() {
        return release;
    }

    @Override
    public long getNextDjId() {
        return djId++;
    }

    @Override
    public long getNextCiId() {
        return ciId++;
    }

    @Override
    public Integer getReleaseStateId(String stateName) {
        return 1;
    }

    @Override
    public Integer getRfcCiActionId(String stateName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createRelease(CmsRelease release) {
        this.release = release;
    }

    @Override
    public void brushReleaseExecOrder(long releaseId) {
        List<CmsRfcCI> orderedCIs = cis.values().stream()
                .sorted(Comparator.comparingInt(CmsRfcCIBasic::getExecOrder))
                .collect(Collectors.toList());
        int lastExecOrder = 0;
        int newExecOrder = 0;
        for (CmsRfcCI ci : orderedCIs) {
            if (ci.getExecOrder() > lastExecOrder) {
                newExecOrder++;
                lastExecOrder = ci.getExecOrder();
            }
            ci.setExecOrder(newExecOrder);
        }
    }

    @Override
    public CmsRelease getReleaseById(long releaseId) {
        return release;
    }

    @Override
    public List<CmsRelease> getReleaseBy3(String nsPath, String releaseName, String releaseState) {
        return new ArrayList<>();
    }

    @Override
    public List<CmsRfcCI> getRfcCIByNsPathDateRangeClassName(String ns, String nsLike, Date startDate, Date endDate, String ciClassName){
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcCI> getRfcCIByReleaseAndClass(long releaseId, String className) {
        return cis.values().stream()
                .filter(ci -> ci.getReleaseId() == releaseId &&
                        (className == null || ci.getCiClassName().equals(className) || ci.getCiClassName().endsWith(className)))
                .collect(Collectors.toList());
    }

    @Override
    public List<CmsRelease> getLatestRelease(String nsPath, String releaseState) {
        ArrayList<CmsRelease> cmsReleases = new ArrayList<>();
        if (release != null) {
            cmsReleases.add(release);
        }
        return cmsReleases;
    }

    @Override
    public int updateRelease(CmsRelease release) {
        this.release = release;
        return release.getReleaseStateId();
    }

    @Override
    public int deleteRelease(long releaseId) {
       // this.release = null;
        return 0;
    }

    @Override
    public void commitRelease(long releaseId, Boolean setDfValue, Integer newCiState, boolean delete4real, String userId, String desc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createRfcCI(CmsRfcCI rfcCi) {
        rfcCi.setIsActiveInRelease(true);
        cis.put(rfcCi.getRfcId(), rfcCi);
    }

    @Override
    public void createRfcLog(CmsRfcCI rfcCi) {
    }

    @Override
    public int rmRfcCIfromRelease(long rfcId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRfcCI(CmsRfcCI rfcCI) {
        cis.put(rfcCI.getRfcId(), rfcCI);
    }

    @Override
    public void updateRfcLog(CmsRfcCI rfcCI) {
    }

    @Override
    public void insertRfcCIAttribute(CmsRfcAttribute attr) {
    }

    @Override
    public void updateRfcCIAttribute(CmsRfcAttribute attr) {
    }

    @Override
    public CmsRfcCI getRfcCIById(long rfcId) {
        return cis.get(rfcId);
    }

    @Override
    public CmsRfcCI getOpenRfcCIByCiId(long ciId) {
        for (CmsRfcCI rfc : cis.values()) {
            if ((ciId == rfc.getCiId()) && rfc.getIsActiveInRelease()) return rfc;
        }
        return null;
    }

    @Override

    public CmsRfcCI getOpenRfcCIByCiIdNoAttrs(long ciId) {
        return getOpenRfcCIByCiId(ciId);
    }

    @Override
    public List<CmsRfcCI> getOpenRfcCIByCiIdList(List<Long> ciIds) {
        return cis.values().stream()
                .filter(r -> (ciIds.contains(r.getCiId())) )
                .collect(Collectors.toList());
    }

    @Override
    public List<CmsRfcCI> getRfcCIBy3(long releaseId, Boolean isActive, Long ciId) {
        return cis.values().stream()
                  .filter(r -> (ciId == null || ciId.equals(r.getCiId())) &&
                                (isActive == null || isActive == r.getIsActiveInRelease()))
                  .collect(Collectors.toList());
    }

    @Override
    public List<CmsRfcCI> getRfcCIByClazzAndName(String nsPath, String clazzName, String ciName, Boolean isActive, String state) {
        return cis.values().stream()
                  .filter(r -> (nsPath == null || nsPath.equals(r.getNsPath())) &&
                                (clazzName == null || clazzName.equals(r.getCiClassName())) &&
                                (ciName == null || ciName.equals(r.getCiName())) &&
                                (isActive == null || isActive == r.getIsActiveInRelease()) &&
                                (state == null || state.equals(r.getCiState())))
                  .collect(Collectors.toList());
    }

    @Override
    public List<CmsRfcCI> getOpenRfcCIByClazzAndNameLower(String nsPath, String clazzName, String ciName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcCI> getOpenRfcCIByNsLike(String ns, String nsLike, String clazzName, String ciName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcCI> getOpenRfcCIByClazzAnd2Names(String nsPath, String clazzName, String ciName, String altCiName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcCI> getClosedRfcCIByCiId(long ciId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcAttribute> getRfcCIAttributes(long rfcId) {
        return new ArrayList<>();
    }

    @Override
    public List<CmsRfcAttribute> getRfcCIAttributesByRfcIdList(Set<Long> rfcIds) {
          return new ArrayList<>();
    }

    @Override
    public void createRfcRelation(CmsRfcRelation rfcRelation) {
        rfcRelation.setIsActiveInRelease(true);
        relations.put(rfcRelation.getRfcId(), rfcRelation);
    }

    @Override
    public void createRfcRelationLog(CmsRfcRelation rel) {
    }

    @Override
    public int rmRfcRelationfromRelease(long rfcId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int updateRfcRelation(CmsRfcRelation rel) {
        long rfcId = rel.getRfcId();
        relations.put(rfcId, rel);
        return 0;
    }

    @Override
    public void updateRfcRelationLog(CmsRfcRelation rel) {
    }
           
    @Override
    public void insertRfcRelationAttribute(CmsRfcAttribute attr) {
    }

    @Override
    public void updateRfcRelationAttribute(CmsRfcAttribute attr) {
    }

    @Override
    public void upsertRfcRelationAttribute(CmsRfcAttribute attr) {
    }

    @Override
    public CmsRfcRelation getRfcRelationById(long rfcId) {
        return relations.get(rfcId);
    }

    @Override
    public CmsRfcRelation getOpenRfcRelationByCiRelId(long ciRelationId) {
        for (CmsRfcRelation rel : relations.values()) {
            if (rel.getCiRelationId() == ciRelationId && rel.getIsActiveInRelease()) return rel;
        }
        return null;
    }

    @Override
    public List<CmsRfcRelation> getRfcRelationByReleaseId(long releaseId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcRelation> getClosedRfcRelationByCiId(long ciId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcRelation> getRfcRelationsByNs(String nsPath, Boolean isActive, String state) {
        return relations.values().stream()
                        .filter(r -> r.getNsPath().equals(nsPath) &&
                                (isActive == null || r.getIsActiveInRelease()))
                        .collect(Collectors.toList());
    }

    @Override
    public List<CmsRfcRelation> getRfcRelationBy4(long releaseId, Boolean isActive, Long fromCiId, Long toCiId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcRelation> getOpenRfcRelationBy2(Long fromCiId, Long toCiId, String relName, String shortRelName) {
        return relations.values().stream()
                        .filter(r -> (relName == null || r.getRelationName().equals(relName)) &&
                                     (fromCiId == null || fromCiId.equals(r.getFromCiId())) &&
                                     (toCiId == null || toCiId.equals(r.getToCiId())) &&
                                     (shortRelName == null || r.getRelationName().endsWith(shortRelName)))
                        .collect(Collectors.toList());
    }

    @Override
    public List<CmsRfcRelation> getOpenFromRfcRelationByTargetClass(long fromCiId, String relName, String shortRelName, String targetClassName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcRelation> getOpenFromRfcRelationByAttrs(long fromCiId, String relName, String shortRelName, String targetClassName, List<CmsRfcBasicAttribute> attrList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcRelation> getOpenToRfcRelationByTargetClass(long toCiId, String relName, String shortRelName, String targetClassName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcRelation> getOpenToRfcRelationByAttrs(long toCiId, String relName, String shortRelName, String targetClassName, List<CmsRfcBasicAttribute> attrList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcRelation> getRfcRelationBy3(long releaseId, Boolean isActive, Long ciRelationId) {
        return relations.values().stream()
                        .filter(r -> (ciRelationId == null || ciRelationId.equals(r.getCiRelationId())) &&
                                     (isActive == null || isActive == r.getIsActiveInRelease()))
                        .collect(Collectors.toList());
    }

    @Override
    public List<CmsRfcRelation> getOpenRfcRelations(String relationName, String shortRelName, String nsPath, String fromClazzName, String toClazzName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcRelation> getOpenRfcRelationsNsLike(String relationName, String shortRelName, String ns, String nsLike, String fromClazzName, String toClazzName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcRelation> getOpenRfcRelationByCiIds(String relName, String shortRelName, List<Long> fromCiIds, List<Long> toCiIds) {
        return relations.values().stream()
                .filter(r -> (relName == null || r.getRelationName().equals(relName)) &&
                        (shortRelName == null || r.getRelationName().endsWith(shortRelName)) &&
                        (fromCiIds == null || fromCiIds.contains(r.getFromCiId())) &&
                        (toCiIds == null || toCiIds.contains(r.getToCiId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<CmsRfcRelation> getRfcRelationByReleaseAndClass(long releaseId, String relationName, String shortRelName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcAttribute> getRfcRelationAttributes(long rfcId) {
        return new ArrayList<>();
    }

    @Override
    public List<CmsRfcAttribute> getRfcRelationAttributesByRfcIdList(Set<Long> rfcIds) {
        return new ArrayList<>();
    }

    @Override
    public List<Long> getLinkedRfcRelationId(long releaseId, Boolean isActive, long rfcId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long countCiRfcByReleaseId(long releaseId) {
        return cis.size();
    }

    @Override
    public long countRelationRfcByReleaseId(long releaseId) {
        return relations.size();
    }

    @Override
    public long countOpenRfcCisByNs(String nsPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long countOpenRfcRelationsByNs(String nsPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rmRfcsByNs(String nsPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rmToRelByNs(String nsPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rmFromRelByNs(String nsPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long countCiNotUpdatedByRfc(long fromCiId, String relationName, String shortRelName, long rfcId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TimelineRelease> getReleasesByCiFilter(TimelineQueryParam queryParam) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TimelineRelease> getReleasesByRelationFilter(TimelineQueryParam queryParam) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TimelineRelease> getReleasesByNsPath(TimelineQueryParam queryParam) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcCI> getAppliedRfcCIsAfterRfcId(Long ciId, Long afterRfcId, Long toRfcId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcCI> getRfcCIsAppliedBetweenTwoReleases(String nsPath, Long fromReleaseId, Long toReleaseId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcRelation> getRfcRelationsAppliedBetweenTwoReleases(String nsPath, Long fromReleaseId, Long toReleaseId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getTagId(String tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createTag(String tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createAltNs(long nsId, long tagId, long rfcId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAltNs(long nsId, long rfcId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcCI> getRfcCIByAltNsAndTag(String nsPath, String tag, Long releaseId, boolean isActive, Long ciId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsAltNs> getAltNsBy(long rfcCI) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getDeploymentDistinctStepsTotal(long deploymentId) {
        throw new UnsupportedOperationException();
    }

    public BomData getBOM(){
        return new BomData(release, cis.values(), relations.values());
    }

    @Override
    public String toString() {
        return "InMemoryDJMapper{" + "djId=" + djId +
                ", ciId=" + ciId +
                ", cis=" + cis.size() +
                ", relations=" + relations.size() +
                ", release=" + (release == null ? 0 : release.getReleaseId()) +
                '}';
    }
}
