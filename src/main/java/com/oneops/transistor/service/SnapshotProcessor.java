package com.oneops.transistor.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.*;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.exceptions.DesignExportException;
import com.oneops.transistor.exceptions.TransistorException;
import com.oneops.transistor.snapshot.domain.*;
import org.apache.log4j.Logger;

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
public class SnapshotProcessor {
    private static final String SNAPSHOT_RESTORE = "restore";
    private static final String UPDATE = "update";
    private static Logger logger = Logger.getLogger(SnapshotProcessor.class);
    private CmsCmProcessor cmProcessor;
    private CmsRfcProcessor rfcProcessor;
    private CmsMdProcessor mdProcessor;
    private CmsCmRfcMrgProcessor rfcMrgProcessor;
    private ReplayProcessor replayProcessor;

    public void setMdProcessor(CmsMdProcessor mdProcessor) {
        this.mdProcessor = mdProcessor;
    }

    public void setReplayProcessor(ReplayProcessor replayProcessor) {
        this.replayProcessor = replayProcessor;
    }

    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }

    Snapshot exportSnapshot(String[] namespaces, String[] classNames, Boolean[] recursiveArray) {
        Snapshot snapshot = new Snapshot();
        for (int i = 0; i < namespaces.length; i++) {
            String namespace = namespaces[i];
            String className = (classNames == null || classNames.length - 1 < i) ? null : classNames[i];
            Boolean recursive = (recursiveArray == null || recursiveArray.length - 1 < i) ? false : recursiveArray[i];

            Part part = new Part(namespace, className);
            part.setRecursive(recursive);
            List<CmsCI> cis = recursive ? cmProcessor.getCiBy3NsLike(namespace, className, null) : cmProcessor.getCiBy3(namespace, className, null);
            for (CmsCI ci : cis) {
                part.addExportCi(ci.getNsPath(), new ExportCi(ci));
                snapshot.updateLastAppliedCiRfc(ci.getLastAppliedRfcId());
            }

            List<CmsCIRelation> relations = recursive ? cmProcessor.getCIRelationsNsLikeNaked(namespace, null, null, className, null) : cmProcessor.getCIRelationsNaked(namespace, null, null, className, null);
            relations.addAll(recursive ? cmProcessor.getCIRelationsNsLikeNaked(namespace, null, null, null, className) : cmProcessor.getCIRelationsNaked(namespace, null, null, null, className));
            for (CmsCIRelation rel : relations) {
                part.addExportRelation(rel.getNsPath(), new ExportRelation(rel));
                snapshot.updateLastAppliedRelationRfc(rel.getLastAppliedRfcId());
            }
            snapshot.add(part);
        }
        snapshot.setRelease(calculateReleaseId(snapshot));
        return snapshot;
    }

    private long calculateReleaseId(Snapshot snapshot) {
        CmsRfcCI rfcCIById = rfcProcessor.getRfcCIById(snapshot.getLastAppliedCiRfc());
        CmsRfcRelation rfcRelationById = rfcProcessor.getRfcRelationById(snapshot.getLastAppliedRelationRfc());
        long releaseIdCi = rfcCIById == null ? 0 : rfcCIById.getReleaseId();
        long releaseIdRel = rfcRelationById == null ? 0 : rfcRelationById.getReleaseId();
        return Math.max(releaseIdCi, releaseIdRel);
    }


    List<String> importSnapshotAndReplayTo(Snapshot snapshot, Long releaseId) {
        validateReleaseId(releaseId);

        HashMap<Long, RelationLink> oldToNewCiIdsMap = new HashMap<>();

        List<String> errors = importSnapshot(snapshot, oldToNewCiIdsMap);
        if (releaseId != null && releaseId > snapshot.getRelease()) {
            CmsRelease release = rfcProcessor.getReleaseById(snapshot.getRelease());
            errors.addAll(replayProcessor.replay(snapshot.getRelease(), releaseId, release.getNsPath(), oldToNewCiIdsMap));
        }

        List<CmsRelease> relList = rfcProcessor.getLatestRelease(snapshot.getNamespace(), "open");
        if (!relList.isEmpty()) {
            CmsRelease release = relList.get(0);
            List<CmsRfcCI> rfcCis = rfcProcessor.getRfcCIBy3(release.getReleaseId(), true, null);
            cleanUpRfcCis(rfcCis);
            List<CmsRfcRelation> rfcRels = rfcProcessor.getRfcRelationByReleaseId(release.getReleaseId());
            cleanUpRfcRels(rfcRels);
            // clean up redundant release
            if (rfcCis.size() == 0 && rfcRels.size() == 0) {                 // remove release if replay triggered no rfc's.
                logger.info("No release because rfc count is 0. Cleaning up release.");
                rfcProcessor.deleteRelease(release.getReleaseId());
            } else {
                release.setDescription("Restore from snapshot for releaseId:" + snapshot.getRelease() + " (and replay to releaseId:" + releaseId + ") completed with " + errors.size() + " warnings");
                rfcProcessor.updateRelease(release);
            }
        }
        return errors;
    }

    private void cleanUpRfcRels(List<CmsRfcRelation> rfcRels) { // clean up redundant Rel updates
        Iterator<CmsRfcRelation> itR = rfcRels.iterator();
        while (itR.hasNext()) {
            CmsRfcRelation rfcRel = itR.next();
            if (UPDATE.equalsIgnoreCase(rfcRel.getRfcAction())) {
                if (!needToUpdate(rfcRel.getAttributes().values())) {
                    rfcProcessor.rmRfcRelationFromRelease(rfcRel.getRfcId());
                    itR.remove();
                }
            }
        }
    }

    private void cleanUpRfcCis(List<CmsRfcCI> rfcCis) { // clean up redundant CI updates
        Iterator<CmsRfcCI> it = rfcCis.iterator();
        while (it.hasNext()) {
            CmsRfcCI rfcCi = it.next();
            if (UPDATE.equalsIgnoreCase(rfcCi.getRfcAction())) {
                if (!needToUpdate(rfcCi.getAttributes().values())) {
                    rfcProcessor.rmRfcCiFromRelease(rfcCi.getRfcId());
                    it.remove();
                }
            }
        }
    }

    private boolean needToUpdate(Collection<CmsRfcAttribute> attributes) {
        boolean needUpdate = false;
        for (CmsRfcAttribute attr : attributes) {
            if (!attr.getOldValue().equals(attr.getNewValue())) {
                needUpdate = true;
                break;
            }
        }
        return needUpdate;
    }

    private void validateReleaseId(Long releaseId) {
        if (releaseId != null) {
            CmsRelease targetRelease = rfcProcessor.getReleaseById(releaseId);
            if (targetRelease == null) {
                throw new TransistorException(CmsError.TRANSISTOR_CANNOT_CORRESPONDING_OBJECT, "ReplayProcessor cannot find target release: " + releaseId);
            }
        }
    }


    List<String> importSnapshot(Snapshot snapshot) {
        return importSnapshot(snapshot, new HashMap<>());
    }

    private List<String> importSnapshot(Snapshot snapshot, Map<Long, RelationLink> oldToNewCiIdsMap) {
        logger.info("Restoring:" + snapshot.getRelease());
        for (String ns : snapshot.allNamespaces()) {  // there shouldn't be any "open" releases for snapshot namespaces
            List<CmsRelease> openReleases = rfcProcessor.getLatestRelease(ns, "open");
            if (openReleases.size() > 0) {
                throw new DesignExportException(DesignExportException.DJ_OPEN_RELEASE_FOR_NAMESPACE_ERROR, "There is an open release for namespace: " + ns + " please discard or commit first");
            }
        }
        List<String> errors = new ArrayList<>();
        snapshot.getParts().forEach((part) -> restoreCis(part, oldToNewCiIdsMap, errors));    // we need to restore relations first, before we attempt to restore relations
        snapshot.getParts().forEach((part) -> restoreRelations(part, oldToNewCiIdsMap, errors));
        return errors;
    }

    private void restoreRelations(Part part, Map<Long, RelationLink> linkMap, List<String> errors) {
        logger.info("processing part:" + part.getClassName() + "@" + part.getNs());
        List<CmsCIRelation> existingRelations;
        if (part.isRecursive()) {
            existingRelations = cmProcessor.getCIRelationsNsLikeNaked(part.getNs(), null, null, part.getClassName(), null);
            addMissing(existingRelations, cmProcessor.getCIRelationsNsLikeNaked(part.getNs(), null, null, null, part.getClassName()));
        } else {
            existingRelations = cmProcessor.getCIRelationsNaked(part.getNs(), null, null, part.getClassName(), null);
            addMissing(existingRelations, cmProcessor.getCIRelationsNaked(part.getNs(), null, null, null, part.getClassName()));
        }
        for (String actualNs : part.getRelations().keySet()) {
            for (ExportRelation exportRelation : part.getRelations().get(actualNs)) {
                try {
                    RelationLink fromLink = linkMap.get(exportRelation.getFrom());
                    RelationLink toLink = linkMap.get(exportRelation.getTo());
                    if (toLink == null) {
                        toLink = new RelationLink(exportRelation.getTo(), null); // external to link
                    }
                    if (fromLink == null) {
                        fromLink = new RelationLink(exportRelation.getFrom(), null); // external from link
                    }
                    CmsCIRelation relation = findMatchingRelation(actualNs, fromLink, toLink, exportRelation.getType(), existingRelations);
                    if (relation == null) { // relation doesn't exist
                        addRelation(actualNs, exportRelation, fromLink, toLink, errors);
                    } else {
                        existingRelations.remove(relation); // we need to remove match
                        updateRelation(exportRelation, relation, errors);
                    }
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                    errors.add(e.getMessage());
                }
            }
        }
        for (CmsCIRelation relation : existingRelations) { // remove relations that aren't a part of the snapshot
            logger.info("Removing relation:" + relation.getRelationName() + "@" + relation.getNsPath() + " " + relation.getFromCiId() + "->" + relation.getToCiId());
            rfcMrgProcessor.requestRelationDelete(relation.getCiRelationId(), SNAPSHOT_RESTORE);
        }
    }

    private void addMissing(List<CmsCIRelation> existingRelations, List<CmsCIRelation> relationsToCheck) {
        relationsToCheck.stream().filter(relation -> findMatchingRelation(relation.getNsPath(), new RelationLink(relation.getFromCiId(), null), new RelationLink(relation.getToCiId(), null), relation.getRelationName(), existingRelations) == null).forEach(existingRelations::add);
    }

    private void updateRelation(ExportRelation exportRelation, CmsCIRelation relation, List<String> errors) {
        CmsRfcRelation rel = new CmsRfcRelation();
        rel.setNsPath(relation.getNsPath());
        rel.setToCiId(relation.getToCiId());
        rel.setFromCiId(relation.getFromCiId());
        rel.setRelationName(relation.getRelationName());
        Map<String, String> snapshotAttributes = exportRelation.getAttributes();
        Map<String, CmsCIRelationAttribute> existingAttributes = relation.getAttributes();
        relation.setRelationId(relation.getRelationId());
        for (String key : snapshotAttributes.keySet()) {
            CmsCIRelationAttribute ciAttribute = existingAttributes.remove(key);
            String value = snapshotAttributes.get(key);
            if (ciAttribute == null) {
                String message = "Existing snapshot attribute " + relation.getRelationName() + "->" + key + " is no longer CI attribute. Won't try to update";
                logger.info(message);
                errors.add(message);
            } else if (ciAttribute.getDfValue() == null || (ciAttribute.getDfValue() != null && !ciAttribute.getDfValue().equals(value))) {
                value = value == null ? "" : value; // This is request request to reset not required attribute that wasn't set in snapshot but was set later. Null is not a valid value due to not null constraint but empty string is behavior consistent with UI.  
                rel.addAttribute(RfcUtil.getAttributeRfc(key, value, exportRelation.getOwner(key)));
            }
        }
        if (!rel.getAttributes().isEmpty()) {
            logger.info("Updating relation:" + relation.getRelationName() + "@" + relation.getNsPath());
            rfcMrgProcessor.upsertRelationRfc(rel, SNAPSHOT_RESTORE);
        }
    }


    private void addRelation(String ns, ExportRelation exportRelation, RelationLink fromLink, RelationLink toLink, List<String> errors) {
        CmsRfcRelation rel = new CmsRfcRelation();
        rel.setNsPath(ns);
        rel.setRelationName(exportRelation.getType());
        if (fromLink == null) {
            rel.setFromCiId(exportRelation.getFrom());
        } else {
            rel.setFromRfcId(fromLink.getRfcId());
            rel.setFromCiId(fromLink.getId());
        }
        if (toLink == null) {
            rel.setToCiId(exportRelation.getTo());
        } else {
            rel.setToRfcId(toLink.getRfcId());
            rel.setToCiId(toLink.getId());
        }
        processRelationAttributes(exportRelation, rel, rel.getRelationName(), errors);
        logger.info("adding relation:" + rel.getRelationName() + "@" + rel.getNsPath() + " " + rel.getFromCiId() + "->" + rel.getToCiId());
        rfcMrgProcessor.upsertRfcRelationNoCheck(rel, SNAPSHOT_RESTORE, null);
    }


    private void processRelationAttributes(BaseEntity exportRelation, CmsRfcRelation rel, String className, List<String> errors) {
        processSnapshotAttributes(exportRelation, rel);
        RfcUtil.bootstrapNewMandatoryAttributesFromMetadataDefaults(rel, mdProcessor.getRelation(className), errors);
    }

    private void processClassAttributes(BaseEntity exportRelation, CmsRfcCI rel, String className, List<String> errors) {
        processSnapshotAttributes(exportRelation, rel);
        RfcUtil.bootstrapNewMandatoryAttributesFromMetadataDefaults(rel, mdProcessor.getClazz(className), errors);
    }

    private void processSnapshotAttributes(BaseEntity entity, CmsRfcContainer rel) {

        if (entity.getAttributes() != null) {
            for (Map.Entry<String, String> attr : entity.getAttributes().entrySet()) {
                CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
                if (attr.getValue() != null) {
                    rfcAttr.setAttributeName(attr.getKey());
                    rfcAttr.setNewValue(attr.getValue());
                    rfcAttr.setOwner(entity.getOwner(attr.getKey()));
                    rel.addAttribute(rfcAttr);
                }
            }
        }
    }


    private static CmsCIRelation findMatchingRelation(String ns, RelationLink fromLink, RelationLink toLink, String type, List<CmsCIRelation> existingRelations) {
        for (CmsCIRelation rel : existingRelations) {
            if (rel.getNsPath().equals(ns) && rel.getRelationName().equals(type) && rel.getFromCiId() == fromLink.getId() && rel.getToCiId() == toLink.getId()) {
                return rel;
            }
        }
        return null;
    }

    private void restoreCis(Part part, Map<Long, RelationLink> idsMap, List<String> errors) {
        List<CmsCI> existingCis = part.isRecursive() ?
                cmProcessor.getCiBy3NsLike(part.getNs(), part.getClassName(), null) :
                cmProcessor.getCiBy3(part.getNs(), part.getClassName(), null);
        for (String actualNs : part.getCis().keySet()) {
            for (ExportCi eci : part.getCis().get(actualNs)) {
                try {
                    CmsCI ci = findMatchingCi(actualNs, eci, existingCis);
                    if (ci == null) {
                        CmsRfcCI rfcCi = addCi(actualNs, eci, errors);
                        idsMap.put(eci.getId(), new RelationLink(rfcCi.getCiId(), rfcCi.getRfcId()));
                    } else {
                        existingCis.remove(ci);
                        idsMap.put(eci.getId(), new RelationLink(ci.getCiId(), null));
                        updateCi(ci, eci, errors);
                    }
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                    errors.add(e.getMessage());
                }
            }
        }
        existingCis.forEach(this::remove);     // remove remaining CIs that aren't a part of the snapshot
    }

    private CmsRfcCI addCi(String ns, ExportCi eci, List<String> errors) {
        CmsRfcCI rfc = newFromExportCiWithoutAttr(ns, eci);
        processClassAttributes(eci, rfc, rfc.getCiClassName(), errors);
        logger.info("adding ci:" + rfc.getCiName() + "@" + rfc.getNsPath());
        return rfcMrgProcessor.upsertCiRfc(rfc, SNAPSHOT_RESTORE);
    }

    private static CmsRfcCI newFromExportCiWithoutAttr(String ns, ExportCi eCi) {
        CmsRfcCI rfc = new CmsRfcCI();
        rfc.setCiName(eCi.getName());
        rfc.setCiClassName(eCi.getType());
        rfc.setNsPath(ns);
        return rfc;
    }

    private void remove(CmsCI ci) {
        logger.info("removing ci:" + ci.getCiName() + "@" + ci.getNsPath());
        rfcMrgProcessor.requestCiDelete(ci.getCiId(), "restore");
    }

    private void updateCi(CmsCI ci, ExportCi eci, List<String> errors) {
        Map<String, CmsCIAttribute> existingAttributes = ci.getAttributes();
        Map<String, String> snapshotAttributes = eci.getAttributes();
        CmsRfcCI rfcCI = newFromExportCiWithoutAttr(ci.getNsPath(), eci);
        rfcCI.setCiId(ci.getCiId());
        for (String key : snapshotAttributes.keySet()) {
            CmsCIAttribute ciAttribute = existingAttributes.remove(key);
            String value = snapshotAttributes.get(key);

            if (ciAttribute == null) {
                String message = "Existing snapshot attribute " + ci.getCiName() + "->" + key + " is no longer CI attribute. Won't try to update";
                logger.info(message);
                errors.add(message);
            } else if (ciAttribute.getDfValue() == null || (ciAttribute.getDfValue() != null && !ciAttribute.getDfValue().equals(value))) {
                value = value == null ? "" : value; // This is request request to reset not required attribute that wasn't set in snapshot but was set later. Null is not a valid value due to not null constraint but empty string is behavior consistent with UI.
                rfcCI.addAttribute(RfcUtil.getAttributeRfc(key, value, eci.getOwner(key)));
            }
        }
        if (!rfcCI.getAttributes().isEmpty()) {
            logger.info("Updating:" + ci.getCiName() + "@" + ci.getNsPath());
            rfcMrgProcessor.upsertRfcCINoChecks(rfcCI, SNAPSHOT_RESTORE, null);
        } else {
            logger.info("Not Updating:" + ci.getCiName() + "@" + ci.getNsPath());
        }
    }


    private static CmsCI findMatchingCi(String ns, ExportCi eci, List<CmsCI> cis) {
        for (CmsCI ci : cis) {
            if (eci.getName().equals(ci.getCiName()) && eci.getType().equals(ci.getCiClassName()) && ns.equals(ci.getNsPath())) {
                return ci;
            }
        }
        return null;
    }


    public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
        this.rfcProcessor = rfcProcessor;
    }

    public void setRfcMrgProcessor(CmsCmRfcMrgProcessor rfcMrgProcessor) {
        this.rfcMrgProcessor = rfcMrgProcessor;
    }

    List<String> replay(long fromReleaseId, long toReleaseId, String nsPath) {
        return replayProcessor.replay(fromReleaseId, toReleaseId, nsPath);
    }
}

class RelationLink {
    private long id;
    private Long rfcId;

    Long getRfcId() {
        return rfcId;
    }

    RelationLink(long id, Long rfcId) {
        this.id = id;
        this.rfcId = rfcId;
    }

    long getId() {
        return id;
    }
}