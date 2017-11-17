package com.oneops.transistor.service.peristenceless;

import com.oneops.cms.cm.domain.CmsAltNs;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.dj.domain.*;
import com.oneops.cms.util.TimelineQueryParam;

import java.util.*;

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
    private Map<Long, CmsRfcCI> rfcs = new HashMap<>();
    private Map<Long, CmsRfcRelation> relations = new HashMap<>();
    private Map<Long, Long> rfcIdByCiId = new HashMap<>();
    private Map<Long, Long> relationRfcIdByCiId = new HashMap<>();
    private CmsRelease release;

    public InMemoryDJMapper() {
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
    public List<CmsRelease> getLatestRelease(String nsPath, String releaseState) {
        ArrayList<CmsRelease> cmsReleases = new ArrayList<>();
        cmsReleases.add(release);
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
        long ciId = rfcCi.getCiId();
        if (!rfcIdByCiId.containsKey(ciId)) {  // we need this in case there are multiple rfc updates for the same CI
            long rfcId = rfcCi.getRfcId();
            rfcIdByCiId.put(ciId, rfcId);
            rfcs.put(rfcId, rfcCi);
        } else {
            long rfcId = rfcIdByCiId.get(ciId);
            rfcCi.setRfcId(rfcId);
            rfcs.put(rfcId, rfcCi);
        }
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
        rfcs.put(rfcCI.getRfcId(), rfcCI);
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
        return rfcs.get(rfcId);
    }

    @Override
    public CmsRfcCI getOpenRfcCIByCiId(long ciId) {
        for (CmsRfcCI rfc:rfcs.values()){
            if ( (ciId==rfc.getCiId()) && (rfc.getIsActiveInRelease())) return rfc;
        }
        return null;
    }

    @Override
    public List<CmsRfcCI> getOpenRfcCIByCiIdList(List<Long> ciIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcCI> getRfcCIBy3(long releaseId, Boolean isActive, Long ciId) {
        ArrayList<CmsRfcCI> cmsRfcCIS = new ArrayList<>();
        for (CmsRfcCI rfc:rfcs.values()){
            if ( (ciId!=null && ciId.equals(rfc.getCiId())) && (isActive!=null && isActive==rfc.getIsActiveInRelease())) cmsRfcCIS.add(rfc);
        }
        return cmsRfcCIS;
    }

    @Override
    public List<CmsRfcCI> getRfcCIByClazzAndName(String nsPath, String clazzName, String ciName, Boolean isActive, String state) {
        ArrayList<CmsRfcCI> cmsRfcCIS = new ArrayList<>();
        for (CmsRfcCI rfc:rfcs.values()){
            if ((nsPath!=null && nsPath.equals(rfc.getNsPath())) && (clazzName!=null && clazzName.equals(rfc.getCiClassName())) && (ciName!=null && ciName.equals(rfc.getCiName())) && (isActive!=null && isActive==rfc.getIsActiveInRelease()) && (state!=null && state.equals(rfc.getCiState()))) cmsRfcCIS.add(rfc);
        }
        return cmsRfcCIS;
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
    public List<CmsRfcCI> getRollUpRfc(long ciId, long rfcId) {
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
    public void createRfcRelation(CmsRfcRelation rel) {
        long ciRelationId = rel.getCiRelationId();
        if (!relationRfcIdByCiId.containsKey(ciRelationId)) {
            relationRfcIdByCiId.put(rel.getRfcId(), ciRelationId);
            relations.put(rel.getRfcId(), rel);
        } else {
            Long rfcId = relationRfcIdByCiId.get(ciRelationId);
            relations.put(rfcId, rel);
            rel.setRfcId(rfcId);
        }
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
        relations.put(rel.getRfcId(), rel);
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
        for (CmsRfcRelation rel: relations.values()){
            if (rel.getCiRelationId()== ciRelationId) return rel;
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
        return new ArrayList<>(relations.values());
    }

    @Override
    public List<CmsRfcRelation> getRfcRelationBy4(long releaseId, Boolean isActive, Long fromCiId, Long toCiId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmsRfcRelation> getOpenRfcRelationBy2(Long fromCiId, Long toCiId, String relName, String shortRelName) {
        List<CmsRfcRelation> cmsRfcLinks = new ArrayList<>();
        for (CmsRfcRelation rel: relations.values()){
            if ((relName!=null && rel.getRelationName().equals(relName)) && (fromCiId!=null && fromCiId.equals(rel.getFromCiId())) && (toCiId!=null && toCiId.equals(rel.getToCiId())) && (shortRelName!=null && rel.getRelationName().endsWith(shortRelName))){
                
                cmsRfcLinks.add(rel);
            }
        }

        return cmsRfcLinks;
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
        throw new UnsupportedOperationException();
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
    public List<CmsRfcLink> getOpenRfcLinks(String nsPath, String relName) {
        List<CmsRfcLink> cmsRfcLinks = new ArrayList<>();
        for (CmsRfcRelation rel: relations.values()){
            if ((relName!=null && rel.getRelationName().equals(relName)) && (nsPath!=null && nsPath.equals(rel.getNsPath()))){
                CmsRfcLink link = new CmsRfcLink();
                link.setAction(rel.getRfcAction());
                link.setRfcId(rel.getRfcId());
                link.setFromCiId(rel.getFromCiId());
                link.setRelationName(rel.getRelationName());
                link.setToCiId(rel.getToCiId());
                link.setToClazzName(rel.getToRfcCi()!=null? rel.getToRfcCi().getCiClassName():"");
                cmsRfcLinks.add(link);
            }
        }
        
        return cmsRfcLinks;
    }

    @Override
    public long countCiRfcByReleaseId(long releaseId) {
        return rfcs.size();
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
    public List<TimelineRelease> getReleaseByFilter(TimelineQueryParam queryParam) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TimelineRelease> getReleaseWithOnlyRelationsByFilter(TimelineQueryParam queryParam) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TimelineRelease> getReleaseByNsPath(TimelineQueryParam queryParam) {
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
    public String toString() {
        return "InMemoryDJMapper{" + "djId=" + djId +
                ", ciId=" + ciId +
                ", rfcs=" + rfcs.size() +
                ", relations=" + relations.size() +
                ", release=" + release.getReleaseId() +
                '}';
    }
}
