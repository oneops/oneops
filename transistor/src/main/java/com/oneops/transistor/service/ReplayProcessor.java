package com.oneops.transistor.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.domain.CmsRfcRelationBasic;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAttribute;
import com.oneops.cms.md.service.CmsMdProcessor;
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
    private static final String UPDATE = "update";
    private CmsRfcProcessor rfcProcessor;
    private CmsCmRfcMrgProcessor rfcMrgProcessor;
    private CmsCmProcessor cmProcessor;
    private CmsMdProcessor mdProcessor;

    public void setMdProcessor(CmsMdProcessor mdProcessor) {
        this.mdProcessor = mdProcessor;
    }

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
        logger.info("Cis to replay: " + cis.size());
        List<CmsRfcRelation> relations = rfcProcessor.getRfcRelationsAppliedBetweenTwoReleases(nsPath, fromReleaseId, toReleaseId);
        logger.info("Relations to replay: " + relations.size());
        
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

    private void restoreReleaseRelations(Map<Long, RelationLink> idMap, List<String> errors, List<CmsRfcRelation> rels) {
        if (rels == null) return;
        for (CmsRfcRelation relation : rels) {
            restoreRelation(idMap, errors, relation);
        }
    }

    private void restoreCi(Map<Long, RelationLink> idMap, List<String> errors, CmsRfcCI rfcToReplay) {
        CmsCI existingCi = getCmsCI(rfcToReplay);
        CmsRfcCI existingRfc = getCmsRfcCI(rfcToReplay);
        logger.info(rfcToReplay.getRfcAction() + ":" + rfcToReplay.getCiName() + "@" + rfcToReplay.getNsPath());

        long oldCiId = rfcToReplay.getCiId();

        rfcToReplay.setRfcId(existingRfc == null ? 0 : existingRfc.getRfcId());
        rfcToReplay.setCiId(existingCi == null ? 0 : existingCi.getCiId());
        rfcToReplay.setReleaseId(existingRfc == null ? 0 : existingRfc.getReleaseId());
        try {
            if (DELETE.equalsIgnoreCase(rfcToReplay.getRfcAction())) {
                if (existingCi != null) {
                    rfcProcessor.createRfcCI(getRemoveRfc(existingCi), REPLAY);
                } else if (existingRfc != null) {
                    rfcProcessor.rmRfcCiFromRelease(existingRfc.getRfcId());
                }
                return;
            } else if (ADD.equalsIgnoreCase(rfcToReplay.getRfcAction())) {
                if (existingCi != null && existingRfc != null && DELETE.equalsIgnoreCase(existingRfc.getRfcAction())) { // special case get rid of existing RFC delete
                    rfcProcessor.rmRfcCiFromRelease(existingRfc.getRfcId());
                }
                RfcUtil.bootstrapNewMandatoryAttributesFromMetadataDefaults(rfcToReplay, mdProcessor.getClazz(rfcToReplay.getCiClassName()));
            } else if (UPDATE.equalsIgnoreCase(rfcToReplay.getRfcAction()) && existingCi == null && existingRfc == null) {
                errors.add("Replay inconsistency. Attempt to update missing component.");
            }
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

    private CmsRfcCI getRemoveRfc(CmsCI existingCi) {
        CmsRfcCI newRfc = new CmsRfcCI();

        newRfc.setCiId(existingCi.getCiId());
        newRfc.setCiClassId(existingCi.getCiClassId());
        newRfc.setCiClassName(existingCi.getCiClassName());
        newRfc.setCiGoid(existingCi.getCiGoid());
        newRfc.setCiName(existingCi.getCiName());

        newRfc.setNsId(existingCi.getNsId());
        newRfc.setNsPath(existingCi.getNsPath());
        newRfc.setComments("deleting");
        newRfc.setRfcAction("delete");


        newRfc.setExecOrder(0);
        newRfc.setCreatedBy(REPLAY);
        newRfc.setUpdatedBy(REPLAY);
        return newRfc;
    }


    private CmsCIRelation getCmsRelation(CmsRfcRelation clone) {
        CmsCIRelation existingRelation = null;
        List<CmsCIRelation> list = cmProcessor.getFromToCIRelationsNaked(clone.getFromCiId(), clone.getRelationName(), clone.getToCiId());
        if (list != null && list.size() > 0) {
            existingRelation = list.get(0);
        }
        return existingRelation;
    }

    private CmsRfcRelation getCmsRfcRelationCI(CmsRfcRelation clone) {
        List<CmsRfcRelation> list = rfcProcessor.getOpenRfcRelationBy2(clone.getFromCiId(), clone.getToCiId(), clone.getRelationName(), null);
        CmsRfcRelation cmsRfcRelation = null;
        if (list != null && list.size() > 0) {
            cmsRfcRelation = list.get(0);
        }
        return cmsRfcRelation;
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


    private void restoreRelation(Map<Long, RelationLink> idMap, List<String> errors, CmsRfcRelation relation) {
        Long fromCiId = relation.getFromCiId();
        if (idMap.containsKey(fromCiId)) {
            relation.setFromCiId(idMap.get(fromCiId).getId());
        }
        Long toCiId = relation.getToCiId();
        if (idMap.containsKey(toCiId)) {
            relation.setToCiId(idMap.get(toCiId).getId());
        }
        CmsCIRelation existingRel = getCmsRelation(relation);
        CmsRfcRelation existingRfcRel = getCmsRfcRelationCI(relation);
        relation.setRfcId(existingRfcRel == null ? 0 : existingRfcRel.getRfcId());
        relation.setCiRelationId(existingRel == null ? (existingRfcRel==null?0:existingRfcRel.getCiRelationId()) : existingRel.getCiRelationId());
        relation.setReleaseId(existingRfcRel == null ? 0 : existingRfcRel.getReleaseId());
        logger.info(relation.getRfcAction() + " relation:" + relation.getRelationName() + "@" + relation.getNsPath());


        try {
            if (DELETE.equalsIgnoreCase(relation.getRfcAction())) {
                if (existingRel != null) {
                    rfcProcessor.createRfcRelation(getRemoveRfc(existingRel), REPLAY);
                } else if (existingRfcRel != null) {
                    rfcProcessor.rmRfcRelationFromRelease(existingRfcRel.getRfcId());
                }
                return;
            } else if (ADD.equalsIgnoreCase(relation.getRfcAction())) {
                if (existingRfcRel != null && DELETE.equalsIgnoreCase(existingRfcRel.getRfcAction())) { // special case get rid of RFC delete
                    rfcProcessor.rmRfcRelationFromRelease(existingRfcRel.getRfcId());
                }
                // if action is "add" then bootstrap class definition default attribute value in case replay is for old metadata and there are new required attributes
                RfcUtil.bootstrapNewMandatoryAttributesFromMetadataDefaults(relation, mdProcessor.getRelation(relation.getRelationName()));
            } else if (UPDATE.equalsIgnoreCase(relation.getRfcAction()) && existingRel == null && existingRfcRel == null) {
                errors.add("Replay inconsistency. Attempt to update missing relation.");
            }

            rfcMrgProcessor.upsertRelationRfc(relation, REPLAY);
        } catch (Exception e) {
            String message = "Relation restore failure:" + e.getMessage();
            logger.warn(message);
            errors.add(message);
        }
    }

    private CmsRfcRelation getRemoveRfc(CmsCIRelation existingRel) {
        CmsRfcRelation newRfc = new CmsRfcRelation();
        newRfc.setCiRelationId(existingRel.getCiRelationId());
        newRfc.setFromCiId(existingRel.getFromCiId());
        newRfc.setToCiId(existingRel.getToCiId());
        newRfc.setNsId(existingRel.getNsId());
        newRfc.setNsPath(existingRel.getNsPath());
        newRfc.setRelationGoid(existingRel.getRelationGoid());
        newRfc.setRelationId(existingRel.getRelationId());
        newRfc.setRelationName(existingRel.getRelationName());
        newRfc.setComments("deleting");
        newRfc.setRfcAction("delete");
        newRfc.setExecOrder(0);
        newRfc.setCreatedBy(REPLAY);
        newRfc.setUpdatedBy(REPLAY);
        return newRfc;
    }


    List<String> replay(long fromReleaseId, long toReleaseId, String nsPath) {
        return replay(fromReleaseId, toReleaseId, nsPath, new HashMap<>());
    }
}
