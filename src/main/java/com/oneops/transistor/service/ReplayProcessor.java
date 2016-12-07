package com.oneops.transistor.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmProcessor;
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
    private CmsCmProcessor cmProcessor;

    public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
        this.rfcProcessor = rfcProcessor;
    }

    public void setRfcMrgProcessor(CmsCmRfcMrgProcessor rfcMrgProcessor) {
        this.rfcMrgProcessor = rfcMrgProcessor;
    }

    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
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


    private void restoreCi(Map<Long, RelationLink> idMap, List<String> errors, CmsRfcCI rfcToReplay) {
        CmsCI existingCi = getCmsCI(rfcToReplay);
        if (ADD.equalsIgnoreCase(rfcToReplay.getRfcAction()) && existingCi != null) { 
            CmsRfcCI existingRfc = getCmsRfcCI(rfcToReplay);
            if (existingRfc != null && DELETE.equalsIgnoreCase(existingRfc.getRfcAction())) { // special case get rid of existing RFC delete
                rfcProcessor.rmRfcCiFromRelease(existingRfc.getRfcId());
            }
        }
        long oldCiId = rfcToReplay.getCiId();
        rfcToReplay.setReleaseId(0);
        rfcToReplay.setRfcId(0);
        rfcToReplay.setCiId(existingCi == null ? 0 : existingCi.getCiId());
        try {
            logger.info(rfcToReplay.getRfcAction() + ":" + rfcToReplay.getCiName() + "@" + rfcToReplay.getNsPath());
            rfcToReplay = rfcMrgProcessor.upsertCiRfc(rfcToReplay, REPLAY);
            if (oldCiId != rfcToReplay.getCiId()) {
                idMap.put(oldCiId, new RelationLink(rfcToReplay.getCiId(), rfcToReplay.getRfcId()));
            }
        } catch (Exception e) {
            String message = "RFC CI restore failure:" + e.getMessage();
            logger.warn(message, e);
            errors.add(message);
        }
    }

    private CmsCI getCmsCI(CmsRfcCI clone) {
        CmsCI existingCi = null;
        List<CmsCI> list = cmProcessor.getCiBy3(clone.getNsPath(), clone.getCiClassName(), clone.getCiName());
        if (list != null && list.size() > 0) {
            existingCi = list.get(0);
        }
        return existingCi;
    }

    private CmsRfcCI getCmsRfcCI(CmsRfcCI clone) {
        List<CmsRfcCI> list = rfcProcessor.getOpenRfcCIByClazzAndName(clone.getNsPath(), clone.getCiClassName(), clone.getCiName());
        CmsRfcCI cmsRfcCI = null;
        if (list != null && list.size() > 0) {
            cmsRfcCI = list.get(0);
        }
        return cmsRfcCI;
    }


    private void restoreReleaseRelations(Map<Long, RelationLink> idMap, List<String> errors, List<CmsRfcRelation> rels) {
        if (rels == null) return;
        for (CmsRfcRelation relation : rels) {
            relation.setRfcId(0);
            relation.setCiRelationId(0);
            relation.setReleaseId(0);
            Long fromCiId = relation.getFromCiId();
            if (idMap.containsKey(fromCiId)) {
                relation.setFromCiId(idMap.get(fromCiId).getId());
            }
            Long toCiId = relation.getToCiId();
            if (idMap.containsKey(toCiId)) {
                relation.setToCiId(idMap.get(toCiId).getId());
            }
            if (ADD.equalsIgnoreCase(relation.getRfcAction())){
                CmsRfcRelation existingRfc = rfcMrgProcessor.getExisitngRelationRfcMerged(relation.getFromCiId(), relation.getRelationName(), relation.getToCiId(), "df");
                if (existingRfc!=null && DELETE.equalsIgnoreCase(existingRfc.getRfcAction())){ // special case get rid of RFC delete
                    rfcProcessor.rmRfcRelationFromRelease(existingRfc.getRfcId());
                }
            }
            try {
                logger.info(relation.getRfcAction()+" relation:"+relation.getRelationName()+"@"+relation.getNsPath());
                rfcMrgProcessor.upsertRelationRfc(relation, REPLAY);
            } catch (Exception e) {
                String message = "Relation restore failure:" + e.getMessage();
                logger.warn(message);
                errors.add(message);
            }
        }
    }


    List<String> replay(long fromReleaseId, long toReleaseId, String nsPath) {
        return replay(fromReleaseId, toReleaseId, nsPath, new HashMap<>());
    }
}
