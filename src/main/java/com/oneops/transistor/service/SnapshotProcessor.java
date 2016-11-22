package com.oneops.transistor.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.*;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.transistor.exceptions.DesignExportException;
import com.oneops.transistor.snapshot.domain.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static Logger logger = Logger.getLogger(SnapshotProcessor.class);
    private CmsCmProcessor cmProcessor;
    private CmsRfcProcessor rfcProcessor;
    private CmsCmRfcMrgProcessor rfcMrgProcessor;
    private ReplayProcessor replayProcessor;

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
        HashMap<Long, RelationLink> oldToNewCiIdsMap = new HashMap<>();
        List<String> errors = importSnapshot(snapshot, oldToNewCiIdsMap);
        if (releaseId != null && releaseId > snapshot.getRelease()) {
            CmsRelease release = rfcProcessor.getReleaseById(snapshot.getRelease());
            errors.addAll(replayProcessor.replay(snapshot.getRelease(), releaseId, release.getNsPath(), oldToNewCiIdsMap));
        }
        return errors;
    }


    List<String> importSnapshot(Snapshot snapshot) {
        return importSnapshot(snapshot, new HashMap<>());
    }

    private List<String> importSnapshot(Snapshot snapshot, Map<Long, RelationLink> oldToNewCiIdsMap) {
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
                        addRelation(actualNs, exportRelation, fromLink, toLink);
                    } else {
                        existingRelations.remove(relation); // we need to remove match
                        updateRelation(exportRelation, relation);
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

    private void updateRelation(ExportRelation exportRelation, CmsCIRelation relation) {
        CmsRfcRelation rel = new CmsRfcRelation();
        rel.setNsPath(relation.getNsPath());
        rel.setToCiId(relation.getToCiId());
        rel.setFromCiId(relation.getFromCiId());
        Map<String, String> snapshotAttributes = exportRelation.getAttributes();
        Map<String, CmsCIRelationAttribute> existingAttributes = relation.getAttributes();
        relation.setRelationId(relation.getRelationId());
        for (String key : snapshotAttributes.keySet()) {
            CmsCIRelationAttribute ciAttribute = existingAttributes.remove(key);
            String value = snapshotAttributes.get(key);
            if (ciAttribute == null || (ciAttribute.getDfValue() == null && value != null) || (ciAttribute.getDfValue() != null && !ciAttribute.getDfValue().equals(value))) {
                rel.addAttribute(createRfcAttribute(key, value, exportRelation.getOwner(key)));
            }
        }
        if (!rel.getAttributes().isEmpty()) {
            logger.info("Updating relation:" + relation.getRelationName() + "@" + relation.getNsPath());
            rfcMrgProcessor.upsertRelationRfc(rel, SNAPSHOT_RESTORE);
        }
    }

    private static CmsRfcAttribute createRfcAttribute(String key, String value, String owner) {
        CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
        rfcAttr.setAttributeName(key);
        rfcAttr.setNewValue(value);
        rfcAttr.setOwner(owner);
        return rfcAttr;
    }

    private void addRelation(String ns, ExportRelation exportRelation, RelationLink fromLink, RelationLink toLink) {
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
        processAttributes(exportRelation, rel);
        logger.info("adding relation:" + rel.getRelationName() + "@" + rel.getNsPath() + " " + rel.getFromCiId() + "->" + rel.getToCiId());
        rfcMrgProcessor.upsertRfcRelationNoCheck(rel, SNAPSHOT_RESTORE, null);
    }

    private void processAttributes(BaseEntity exportRelation, CmsRfcContainer rel) {
        if (exportRelation.getAttributes() != null) {
            for (Map.Entry<String, String> attr : exportRelation.getAttributes().entrySet()) {
                CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
                if (attr.getValue() != null) {
                    rfcAttr.setAttributeName(attr.getKey());
                    rfcAttr.setNewValue(attr.getValue());
                    rfcAttr.setOwner(exportRelation.getOwner(attr.getKey()));
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
        List<CmsCI> existingCis;
        if (part.isRecursive()) {
            existingCis = cmProcessor.getCiBy3NsLike(part.getNs(), part.getClassName(), null);
        } else {
            existingCis = cmProcessor.getCiBy3(part.getNs(), part.getClassName(), null);
        }
        for (String actualNs : part.getCis().keySet()) {
            for (ExportCi eci : part.getCis().get(actualNs)) {
                try {
                    CmsCI ci = findMatchingCi(actualNs, eci, existingCis);
                    if (ci == null) {
                        CmsRfcCI rfcCi = addCi(actualNs, eci);
                        idsMap.put(eci.getId(), new RelationLink(rfcCi.getCiId(), rfcCi.getRfcId()));
                    } else {
                        existingCis.remove(ci);
                        idsMap.put(eci.getId(), new RelationLink(ci.getCiId(), null));
                        updateCi(ci, eci);
                    }
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                    errors.add(e.getMessage());
                }
            }
        }
        existingCis.forEach(this::remove);     // remove remaining CIs that aren't a part of the snapshot
    }

    private CmsRfcCI addCi(String ns, ExportCi eci) {
        CmsRfcCI rfc = newFromExportCiWithoutAttr(ns, eci);
        processAttributes(eci, rfc);
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

    private void updateCi(CmsCI ci, ExportCi eci) {
        Map<String, CmsCIAttribute> existingAttributes = ci.getAttributes();
        Map<String, String> snapshotAttributes = eci.getAttributes();
        CmsRfcCI rfcCI = newFromExportCiWithoutAttr(ci.getNsPath(), eci);
        rfcCI.setCiId(ci.getCiId());
        for (String key : snapshotAttributes.keySet()) {
            CmsCIAttribute ciAttribute = existingAttributes.remove(key);
            String value = snapshotAttributes.get(key);

            if (ciAttribute == null || (ciAttribute.getDfValue() == null && value != null) || (ciAttribute.getDfValue() != null && !ciAttribute.getDfValue().equals(value))) {
                rfcCI.addAttribute(createRfcAttribute(key, value, eci.getOwner(key)));
            }
        }
        if (!rfcCI.getAttributes().isEmpty()) {
            logger.info("Updating:" + ci.getCiName() + "@" + ci.getNsPath());
            rfcMrgProcessor.upsertRfcCINoChecks(rfcCI, SNAPSHOT_RESTORE, null);

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