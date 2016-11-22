package com.oneops.transistor.service;

import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.domain.CmsRfcRelationBasic;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ReplayProcessor {
    private static Logger logger = Logger.getLogger(ReplayProcessor.class);

    private static final String REPLAY = "replay";
    private static final String ADD = "add";
    private static final String DELETE = "delete";
    private CmsRfcProcessor rfcProcessor;
    private CmsCmRfcMrgProcessor rfcMrgProcessor;

    public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
        this.rfcProcessor = rfcProcessor;
    }

    public void setRfcMrgProcessor(CmsCmRfcMrgProcessor rfcMrgProcessor) {
        this.rfcMrgProcessor = rfcMrgProcessor;
    }


    List<String> replay(Long fromReleaseId, Long toReleaseId, String nsPath, Map<Long, RelationLink> idMap) {
        List<String> errors = new ArrayList<>();
        List<CmsRfcCI> cis = rfcProcessor.getRfcCIsAppliedBetweenTwoReleases(nsPath, fromReleaseId, toReleaseId);
        List<CmsRfcRelation> relations = rfcProcessor.getRfcRelationsAppliedBetweenTwoReleases(nsPath, fromReleaseId, toReleaseId);
        Map<Long, List<CmsRfcRelation>> relationsByRelease = relations.stream().collect(Collectors.groupingBy(CmsRfcRelationBasic::getReleaseId));
        long currentReleaseId = 0;
        for (CmsRfcCI ci : cis) {
            if (currentReleaseId != ci.getReleaseId()) {
                restoreReleaseRelations(idMap, errors, relationsByRelease.get(currentReleaseId));
                currentReleaseId = ci.getReleaseId();
            }
            restoreCi(idMap, errors, ci);
        }
        restoreReleaseRelations(idMap, errors, relationsByRelease.get(currentReleaseId)); // we need to call restore release relations one last time for last release
        return errors;
    }


    /**
     * 1. Add -> Clear ciId and replay 
     * 2. Update for -> 
     *     2.1 existing CI - simple replay
     *     2.2 newly added RFC - update RFC
     * 3. Delete for ->
     *     3.1. existing CI - simple replay
     *     3.2. newly added RFC  - delete RFC
     */
    private void restoreCi(Map<Long, RelationLink> idMap, List<String> errors, CmsRfcCI ci) {
        CmsRfcCI clone = TransUtil.cloneRfc(ci);
        clone.setRfcId(0);
        clone.setReleaseId(0);
        long oldCiId = clone.getCiId();
        if (ADD.equalsIgnoreCase(clone.getRfcAction())) {
            List<CmsRfcCI> list = rfcProcessor.getOpenRfcCIByClazzAndName(clone.getNsPath(), clone.getCiClassName(), clone.getCiName());
            if (list.size()>0){ // check special case, existing CI, pending RFC delete. We just need to remove pending delete
                CmsRfcCI cmsRfcCI = list.get(0);
                if (DELETE.equalsIgnoreCase(cmsRfcCI.getRfcAction())){
                    rfcProcessor.rmRfcCiFromRelease(cmsRfcCI.getRfcId());
                    return;
                }
            }
            clone.setCiId(0);
        }
        if (idMap.containsKey(oldCiId)) {
            RelationLink relationLink = idMap.get(oldCiId);
            if (DELETE.equalsIgnoreCase(clone.getRfcAction())) {  // special case delete for RFC added during replay or snapshot restore
                rfcProcessor.rmRfcCiFromRelease(relationLink.getRfcId());   
                return;
            }
            clone.setRfcId(getSafeValue(relationLink.getRfcId()));
            clone.setCiId(relationLink.getId());
        }
        
        clone = upsertAndCollectErrors(clone, errors);
        if (oldCiId!= clone.getCiId()) {
            idMap.put(oldCiId, new RelationLink(clone.getCiId(), clone.getRfcId()));
        }
    }

    private CmsRfcCI upsertAndCollectErrors(CmsRfcCI clone, List<String> errors) {
        try {
            clone = rfcMrgProcessor.upsertCiRfc(clone, REPLAY);
        } catch (Exception e) {
            String message = "RFC CI restore failure:" + e.getMessage();
            logger.warn(message, e);
            errors.add(message);
        }
        return clone;
    }

    private void restoreReleaseRelations(Map<Long, RelationLink> idMap, List<String> errors, List<CmsRfcRelation> rels) {
        if (rels == null) return;
        for (CmsRfcRelation rel : rels) {
            CmsRfcRelation clone = TransUtil.cloneRfcRelation(rel);
            clone.setRfcId(0);
            clone.setCiRelationId(0);
            clone.setReleaseId(0);
            Long fromCiId = clone.getFromCiId();
            if (idMap.containsKey(fromCiId)) {
                clone.setFromRfcId(getSafeValue(idMap.get(fromCiId).getRfcId(), clone.getFromRfcId()));
                clone.setFromCiId(idMap.get(fromCiId).getId());
            }
            Long toCiId = clone.getToCiId();
            if (idMap.containsKey(toCiId)) {
                clone.setToRfcId(getSafeValue(idMap.get(toCiId).getRfcId(), clone.getToRfcId()));
                clone.setToCiId(idMap.get(toCiId).getId());
            }
            upsertAndCollectErrors(clone, errors);
        }
    }

    private Long getSafeValue(Long rfcId, Long defaultValue) {
        return rfcId==null?defaultValue:rfcId;
    }

    private void upsertAndCollectErrors(CmsRfcRelation clonedRelation, List<String> errors) {
        try {
            rfcMrgProcessor.upsertRelationRfc(clonedRelation, REPLAY);
        } catch (Exception e) {
            String message = "Relation restore failure:" + e.getMessage();
            logger.warn(message);
            errors.add(message);
        }
    }

    private long getSafeValue(Long rfcId) {
        return rfcId == null ? 0 : rfcId;
    }

    List<String> replay(long fromReleaseId, long toReleaseId, String nsPath) {
        return replay(fromReleaseId, toReleaseId, nsPath, new HashMap<>());
    }
}
