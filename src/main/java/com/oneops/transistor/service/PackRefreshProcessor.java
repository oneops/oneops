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
package com.oneops.transistor.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAttribute;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.md.domain.CmsRelationAttribute;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.util.CmsDJValidator;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.exceptions.DesignExportException;
import com.oneops.transistor.exceptions.TransistorException;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Processor class to sync the design components with the latest pack changes
 *
 * @author ranand
 *
 */
public class PackRefreshProcessor {

    private static final String OPEN_RELEASE_ERROR_MSG = "Platform already has changes in current release. Please commit/discard the changes before doing pack refresh.";
    static Logger logger = Logger.getLogger(PackRefreshProcessor.class);

    private CmsCmProcessor cmProcessor;
    private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
    private CmsRfcProcessor rfcProcessor;
    private CmsMdProcessor mdProcessor;
    private CmsDJValidator djValidator;
    private TransUtil trUtil;

    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }

    public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
        this.rfcProcessor = rfcProcessor;
    }

    public void setTrUtil(TransUtil trUtil) {
        this.trUtil = trUtil;
    }

    public void setCmRfcMrgProcessor(CmsCmRfcMrgProcessor cmRfcMrgProcessor) {
        this.cmRfcMrgProcessor = cmRfcMrgProcessor;
    }

    public void setMdProcessor(CmsMdProcessor mdProcessor) {
        this.mdProcessor = mdProcessor;
    }

    public void setDjValidator(CmsDJValidator djValidator) {
        this.djValidator = djValidator;
    }

    public long refreshPack(long platformId ,String userId, String scope){
        CmsCI designPlatform = cmProcessor.getCiById(platformId);
        if(designPlatform == null){
            String errMsg = "Can not find platform for id = " + platformId + ";";
            logger.error(errMsg);
            throw new TransistorException(CmsError.TRANSISTOR_CANNOT_CORRESPONDING_OBJECT, errMsg);
        }

        String designPlatformNsPath = designPlatform.getNsPath() + "/_design/" + designPlatform.getCiName();
        if (rfcProcessor.getRfcCountByNs(designPlatformNsPath) > 0) {
            throw new TransistorException(DesignExportException.DJ_OPEN_RELEASE_FOR_NAMESPACE_ERROR, OPEN_RELEASE_ERROR_MSG);
        }

        String mgmtTemplNsPath = "/public/" + designPlatform.getAttribute("source").getDfValue()
                + "/packs/" + designPlatform.getAttribute("pack").getDfValue()
                + "/" + designPlatform.getAttribute("version").getDfValue();

        List<CmsCI> templatePlatforms = cmProcessor.getCiBy3(mgmtTemplNsPath, "mgmt.catalog.Platform", null);
        if (templatePlatforms.size()==0) {
            String err = "Cannot find corresponding mgmt platform object :" + mgmtTemplNsPath;
            logger.error(err);
            throw new TransistorException(CmsError.TRANSISTOR_CANNOT_CORRESPONDING_OBJECT,err);

        }
        CmsCI templatePlatform = templatePlatforms.get(0);

        Map<String, Map<String, CmsCIRelation>> existingCatalogPlatRels = getExistingCatalogPlatRels(designPlatformNsPath);

        processPlatform(templatePlatform, designPlatform, existingCatalogPlatRels, userId, designPlatformNsPath);

        //Check if there is an open release after the pack sync and return the corresponding release id
        CmsRelease release = cmRfcMrgProcessor.getReleaseByNameSpace(designPlatform.getNsPath());
        if (release != null && "open".equals(release.getReleaseState())) {
            return release.getReleaseId();
        } else {
            return 0;
        }
    }

    private void processPlatform(CmsCI templatePlatform, CmsCI designPlatform, Map<String, Map<String,CmsCIRelation>> existingCatalogPlatRels, String userId, String designPlatformNsPath){

        String releaseNsPath = designPlatform.getNsPath();

        List<CmsCIRelation> templateRels = cmProcessor.getFromCIRelations(templatePlatform.getCiId(), null, "Requires", null);
        List<CmsCIRelation> designRels = cmProcessor.getFromCIRelations(designPlatform.getCiId(), null, "Requires", null);

        List<CmsCIRelation> existingDependsOnRels =  new ArrayList<>(existingCatalogPlatRels.get("catalog.DependsOn").values());

        List<CmsCIRelation> templInternalRels = new ArrayList<CmsCIRelation>();
        Map<String, Edge> edges = new HashMap<String, Edge>();
        for (CmsCIRelation templateRel:templateRels) {
            Edge edge = new Edge();
            edge.templateRel = templateRel;
            String key = trUtil.getLongShortClazzName(templatePlatform.getCiClassName()) + "-Requires-" + templateRel.getToCi().getCiName();
            edges.put(key, edge);

            List<CmsCIRelation> ciRels = cmProcessor.getFromCIRelations(templateRel.getToCi().getCiId(), null, null);
            templInternalRels.addAll(ciRels);

        }

        for (CmsCIRelation userRel : designRels) {
            String key = trUtil.getLongShortClazzName(designPlatform.getCiClassName()) + "-Requires-" + userRel.getAttribute("template").getDfValue();
            if (edges.containsKey(key)) {
                edges.get(key).userRels.add(userRel);
            } else {
                Edge edge = new Edge();
                edge.userRels.add(userRel);
                edges.put(key, edge);
            }
        }

        //add call to processEdges
        Map<Long, List<Long>> templateIdsMap = processEdges(edges , designPlatform, designPlatformNsPath, releaseNsPath, userId);

        Set<String> newRelIds = processPackInterRelations(templInternalRels, templateIdsMap, designPlatformNsPath , releaseNsPath, existingCatalogPlatRels, userId);

        for (CmsCIRelation existingDpOn : existingDependsOnRels) {
            String source = existingDpOn.getAttributes().get("source") != null ? existingDpOn.getAttribute("source").getDfValue(): "";
            if (!"user".equals(source) && !newRelIds.contains(existingDpOn.getRelationGoid())) {
                    //call to create the delete relation rfc
                    cmRfcMrgProcessor.requestRelationDelete(existingDpOn.getCiRelationId(), userId);
            }
        }

        processLocalVars(templatePlatform,designPlatform, designPlatformNsPath, releaseNsPath, userId);
        
        updatePackDigest(designPlatform, userId);

    }

    private void updatePackDigest(CmsCI designPlatform, String userId) {
        String nsPrefix = "/public/" + designPlatform.getAttribute("source").getDfValue()
                + "/packs/" + designPlatform.getAttribute("pack").getDfValue();
        List<CmsCI> versions = cmProcessor.getCiBy3(nsPrefix, "mgmt.Version", designPlatform.getAttribute("version").getDfValue());
        for (CmsCI version: versions) {
            String digest = version.getAttribute("commit").getDfValue();
            CmsRfcCI plat = trUtil.cloneRfc(cmRfcMrgProcessor.getCiById(designPlatform.getCiId(), "dj"));
            plat.getAttribute("pack_digest").setNewValue(digest);
            cmRfcMrgProcessor.upsertCiRfc(plat, userId);
        }
    }

    private void processLocalVars(CmsCI templatePlatform,CmsCI designPlatform, String platNsPath, String releaseNsPath, String userId) {

        List<CmsCIRelation> localVarPackRels = cmProcessor.getToCIRelations(templatePlatform.getCiId(), "mgmt.catalog.ValueFor",null, "mgmt.catalog.Localvar");

        for (CmsCIRelation localVarPackRel : localVarPackRels) {
            CmsCI packVar = localVarPackRel.getFromCi();
            CmsRfcCI designVarRfc = trUtil.mergeCis(null, packVar, "catalog", platNsPath, releaseNsPath);
            setCiId(designVarRfc,null);
            designVarRfc.setCreatedBy(userId);
            designVarRfc.setUpdatedBy(userId);
            designVarRfc = cmRfcMrgProcessor.upsertCiRfc(designVarRfc, userId);

            List<CmsCIRelation> existingVar2Palt = cmProcessor.getFromToCIRelations(designVarRfc.getCiId(), "catalog.ValueFor",designPlatform.getCiId());
            if (existingVar2Palt.size() == 0) {
                CmsRfcRelation designLVRfcRelation = mergeRelations(localVarPackRel,platNsPath, releaseNsPath,"catalog.");
                designLVRfcRelation.setFromRfcId(designVarRfc.getRfcId());
                designLVRfcRelation.setFromCiId(designVarRfc.getCiId());
                designLVRfcRelation.setToCiId(designPlatform.getCiId());
                designLVRfcRelation.setCreatedBy(userId);
                designLVRfcRelation.setUpdatedBy(userId);
                cmRfcMrgProcessor.upsertRelationRfc(designLVRfcRelation, userId);
            }
        }

    }


    private Map<Long, List<Long>> processEdges(Map<String, Edge> edges , CmsCI designPlatform, String platformNsPath , String releaseNsPath , String userId) {

        Map<Long, List<Long>> templateIdsMap =  new HashMap<>();

        for (Edge edge : edges.values()) {
            if (edge.userRels.size()>0) {
                List<Long> catalogCiIds = new ArrayList<Long>();
                CmsCI templLeafCi = (edge.templateRel != null) ? edge.templateRel.getToCi() : null;
                for (CmsCIRelation userRel : edge.userRels) {
                    CmsRfcCI leafRfc = mergeCis(templLeafCi , userRel.getToCi(), "catalog", platformNsPath, releaseNsPath);

                    if (templLeafCi == null || "pending_deletion".equals(templLeafCi.getCiState())) {
                        leafRfc.setRfcAction("delete");
                    }

                    setCiId(leafRfc,userRel.getToCi());
                    leafRfc.setCreatedBy(userId);
                    leafRfc.setUpdatedBy(userId);
                    if("delete".equals(leafRfc.getRfcAction())){
                        CmsRfcCI deleteRfc = cmRfcMrgProcessor.requestCiDelete(leafRfc.getCiId(), userId,0);
                        logger.debug("new delete ci rfc id = " + deleteRfc.getRfcId());
                        continue;
                    }

                    CmsRfcCI newLeafRfc = cmRfcMrgProcessor.upsertCiRfc(leafRfc, userId);
                    logger.debug("new ci rfc id = " + newLeafRfc.getRfcId());
                    catalogCiIds.add(newLeafRfc.getCiId());


                    CmsRfcRelation leafRfcRelation = mergeRelations(edge.templateRel, platformNsPath, releaseNsPath, null);
                    leafRfcRelation.setFromCiId(designPlatform.getCiId());

                    if(newLeafRfc.getRfcId() > 0) leafRfcRelation.setToRfcId(newLeafRfc.getRfcId());
                    leafRfcRelation.setToCiId(leafRfc.getCiId());

                    leafRfcRelation.setCreatedBy(userId);
                    leafRfcRelation.setUpdatedBy(userId);
                    CmsRfcRelation newLeafRfcRelation = cmRfcMrgProcessor.upsertRelationRfc(leafRfcRelation, userId);
                    logger.debug("Created new relation rfc - " + newLeafRfcRelation.getRfcId());

                    if (templLeafCi != null)  templateIdsMap.put(templLeafCi.getCiId(), catalogCiIds);

                }

            } else {
                CmsCI templateCi = edge.templateRel.getToCi();
                if (!"pending_deletion".equals(templateCi.getCiState())) {
                    String cardinality = edge.templateRel.getAttribute("constraint").getDfValue();
                    if (cardinality != null && cardinality.startsWith("1..")) {
                        List<Long> catalogCiIds = new ArrayList<>();
                        CmsRfcCI leafRfc = mergeCis(templateCi, null, "catalog", platformNsPath, releaseNsPath);
                        setCiId(leafRfc, null);
                        leafRfc.setCreatedBy(userId);
                        leafRfc.setUpdatedBy(userId);
                        CmsRfcCI newLeafRfc = cmRfcMrgProcessor.upsertCiRfc(leafRfc, userId);
                        catalogCiIds.add(newLeafRfc.getCiId());
                        logger.debug("new ci rfc id = " + newLeafRfc.getRfcId());
                        templateIdsMap.put(templateCi.getCiId(), catalogCiIds);

                        CmsRfcRelation leafRfcRelation = mergeRelations(edge.templateRel, platformNsPath, releaseNsPath, null);

                        leafRfcRelation.setFromCiId(designPlatform.getCiId());
                        if (newLeafRfc.getRfcId() > 0) leafRfcRelation.setToRfcId(newLeafRfc.getRfcId());
                        leafRfcRelation.setToCiId(newLeafRfc.getCiId());
                        setCiRelationId(leafRfcRelation);
                        leafRfcRelation.setCreatedBy(userId);
                        leafRfcRelation.setUpdatedBy(userId);
                        CmsRfcRelation newLeafRfcRelation = cmRfcMrgProcessor.upsertRelationRfc(leafRfcRelation, userId);
                        logger.debug("Created new relation rfc - " + newLeafRfcRelation.getRfcId());
                    }
                }
            }
        }

        return templateIdsMap;
    }

    private Set<String> processPackInterRelations(List<CmsCIRelation> internalRels, Map<Long, List<Long>> ciIdsMap, String platNsPath , String releaseNsPath , Map<String, Map<String,CmsCIRelation>> existingCatalogPlatRels, String userId) {

        Set<String> newRelsGoids = new HashSet<String>();
        for (CmsCIRelation ciRel : internalRels) {
            Long fromPackCiId = ciRel.getFromCiId();
            Long toPackCiId = ciRel.getToCiId();
            if (ciIdsMap.containsKey(fromPackCiId)){
                for (Long fromCatalogRfcCiId : ciIdsMap.get(fromPackCiId)) {
                    if (ciIdsMap.containsKey(toPackCiId)) {
                        for (Long toCatalogRfcCiId : ciIdsMap.get(toPackCiId)) {
                            CmsRfcRelation rfcRelation = mergeRelations(ciRel,platNsPath, releaseNsPath, "catalog.");
                            rfcRelation.setFromCiId(fromCatalogRfcCiId);
                            rfcRelation.setToCiId(toCatalogRfcCiId);
                            setCiRelationId(rfcRelation);
                            rfcRelation.setCreatedBy(userId);
                            rfcRelation.setUpdatedBy(userId);

                            CmsRfcRelation newRfcRelation = null;
                            CmsCIRelation existingCIRel = existingCatalogPlatRels.get(rfcRelation.getRelationName())!=null?
                                    existingCatalogPlatRels.get(rfcRelation.getRelationName()).get(rfcRelation.getFromCiId() + ":" + rfcRelation.getToCiId()):null;
                            if ("delete".equals(rfcRelation.getRfcAction())){
                                if (existingCIRel!=null) {
                                    cmRfcMrgProcessor.requestRelationDelete(rfcRelation.getRelationId(), userId);
                                    logger.info("removing relation id = " + rfcRelation.getRelationName()+"#"+rfcRelation.getFromRfcId()+"->"+rfcRelation.getToRfcId());
                                } else {
                                    logger.info("Relation removed from the pack, but is not a part of the platform. Won't do anything");
                                }

                            } else if(needUpdateRfcRelation(rfcRelation, existingCIRel)){
                                newRfcRelation = cmRfcMrgProcessor.upsertRelationRfc(rfcRelation, userId);
                                logger.debug("new relation rfc id = " + newRfcRelation.getRfcId());
                                newRelsGoids.add(newRfcRelation.getRelationGoid());
                            }else{
                                newRelsGoids.add(existingCIRel.getRelationGoid());
                            }
                        }
                    }
                }
            }
        }

        return newRelsGoids;
    }


    private CmsRfcRelation mergeRelations(CmsCIRelation mgmtCiRelation, String nsPath, String releaseNsPath, String prefix) {

        CmsRfcRelation newRfc = new CmsRfcRelation();
        newRfc.setNsPath(nsPath);
        newRfc.setReleaseNsPath(releaseNsPath);
        if(prefix == null) prefix = "base.";

        String targetRelationName = prefix + trUtil.getLongShortClazzName(mgmtCiRelation.getRelationName());
        CmsRelation targetRelation = mdProcessor.getRelation(targetRelationName);

        newRfc.setRelationId(targetRelation.getRelationId());
        newRfc.setRelationName(targetRelation.getRelationName());

        //bootstrap the default values from Class definition
        Map<String, CmsRelationAttribute> relAttrs = new HashMap<String, CmsRelationAttribute>();
        for (CmsRelationAttribute relAttr : targetRelation.getMdAttributes()) {
            if (relAttr.getDefaultValue() != null) {
                CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
                rfcAttr.setAttributeId(relAttr.getAttributeId());
                rfcAttr.setAttributeName(relAttr.getAttributeName());
                rfcAttr.setNewValue(relAttr.getDefaultValue());
                newRfc.addAttribute(rfcAttr);
            }
            relAttrs.put(relAttr.getAttributeName(), relAttr);
        }
        if ("pending_deletion".equals(mgmtCiRelation.getRelationState())){
            newRfc.setRfcAction("delete");
        }

        trUtil.applyRelationToRfc(newRfc, mgmtCiRelation, relAttrs, true, null);

        return newRfc;
    }

    private void setCiId(CmsRfcCI rfc, CmsCI userCi) {
        CmsCI ci = null;
        if(userCi != null){
            ci = userCi;
        }
        else{
            List<CmsCI> existingCis = null;
            existingCis = cmProcessor.getCiBy3(rfc.getNsPath(), rfc.getCiClassName(), rfc.getCiName());
            if (existingCis.size()>0) {
                ci = existingCis.get(0);
            }
        }

        if(ci != null) {
            rfc.setCiId(ci.getCiId());
            for (String attrName : ci.getAttributes().keySet()) {
                CmsCIAttribute existingAttr = ci.getAttribute(attrName);
                if (existingAttr != null && existingAttr.getOwner() != null && existingAttr.getOwner().equalsIgnoreCase("design")) {
                    rfc.getAttributes().remove(attrName);
                }
            }
        }
    }

    private void setCiRelationId(CmsRfcRelation rfc) {
        List<CmsCIRelation> existingRels = cmProcessor.getFromToCIRelations(rfc.getFromCiId(),rfc.getRelationName(), rfc.getToCiId());

        if (existingRels.size()>0) {
                CmsCIRelation rel = existingRels.get(0);
                rfc.setCiRelationId(rel.getCiRelationId());
                for (String attrName : rel.getAttributes().keySet()) {
                    CmsCIRelationAttribute existingAttr = rel.getAttribute(attrName);
                    if (existingAttr != null && existingAttr.getOwner() != null && existingAttr.getOwner().equalsIgnoreCase("design")) {
                        rfc.getAttributes().remove(attrName);
                    }
                }
            }
        }


    private boolean needUpdateRfcRelation(CmsRfcRelation rfcRel, CmsCIRelation baseRel) {

        boolean needUpdate = false;

        if (baseRel == null ){
            rfcRel.setRfcAction("delete");
            return true;
        }


        Set<String> equalAttrs = new HashSet<String>( rfcRel.getAttributes().size());
        for (CmsRfcAttribute attr : rfcRel.getAttributes().values()){
            CmsCIRelationAttribute existingAttr = baseRel.getAttribute(attr.getAttributeName());
            if (djValidator.equalStrs(attr.getNewValue(), existingAttr.getDjValue())) {
                equalAttrs.add(attr.getAttributeName());
            } else {
                needUpdate = true;
            }
        }

        if (needUpdate) {
            for (String attrName : equalAttrs) {
                rfcRel.getAttributes().remove(attrName);
            }

        }
        return needUpdate;
    }

    private CmsRfcCI mergeCis(CmsCI templateCi, CmsCI userCi, String targetPrefix, String nsPath, String releaseNsPath) {

        CmsRfcCI newRfc = new CmsRfcCI();
        newRfc.setNsPath(nsPath);
        newRfc.setReleaseNsPath(releaseNsPath);

        String targetClazzName = null;

        if( templateCi != null) {
            targetClazzName = targetPrefix + "." + trUtil.getLongShortClazzName(templateCi.getCiClassName());
        }else{
            targetClazzName = targetPrefix + "." + trUtil.getLongShortClazzName(userCi.getCiClassName());
        }

        CmsClazz targetClazz = mdProcessor.getClazz(targetClazzName);
        newRfc.setCiClassId(targetClazz.getClassId());
        newRfc.setCiClassName(targetClazz.getClassName());

        //bootstrap the default values from Class definition and populate map for checks
        Map<String, CmsClazzAttribute> clazzAttrs = new HashMap<String, CmsClazzAttribute>();
        for (CmsClazzAttribute clAttr : targetClazz.getMdAttributes()) {
            if (clAttr.getDefaultValue() != null) {
                CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
                rfcAttr.setAttributeId(clAttr.getAttributeId());
                rfcAttr.setAttributeName(clAttr.getAttributeName());
                rfcAttr.setNewValue(clAttr.getDefaultValue());
                newRfc.addAttribute(rfcAttr);
            }
            clazzAttrs.put(clAttr.getAttributeName(), clAttr);
        }

        //populate values from template obj if it's not null
        trUtil.applyCiToRfc(newRfc, templateCi, clazzAttrs, true, false);

        mergeUserCiToRfc(newRfc, userCi, clazzAttrs, true, false);

        return newRfc;
    }


    private void mergeUserCiToRfc(CmsRfcCI newRfc, CmsCI userCi, Map<String, CmsClazzAttribute> mdAttrs, boolean setComments, boolean checkExpression) {
        if (userCi != null) {
            newRfc.setCiName(userCi.getCiName());
            if (setComments) newRfc.setComments(userCi.getComments());
            for (CmsCIAttribute designAttr : userCi.getAttributes().values()) {
                if (mdAttrs.containsKey(designAttr.getAttributeName())) {
                    if (designAttr.getDfValue() != null &&
                            designAttr.getOwner() != null && "design".equals(designAttr.getOwner())) {
                        if (newRfc.getAttribute(designAttr.getAttributeName()) != null) {
                            newRfc.getAttribute(designAttr.getAttributeName()).setNewValue(designAttr.getDfValue());
                            newRfc.getAttribute(designAttr.getAttributeName()).setComments(designAttr.getComments());
                        } else {
                            if (mdAttrs.get(designAttr.getAttributeName()) != null) {
                                CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
                                rfcAttr.setAttributeId(mdAttrs.get(designAttr.getAttributeName()).getAttributeId());
                                rfcAttr.setAttributeName(designAttr.getAttributeName());
                                rfcAttr.setNewValue(designAttr.getDfValue());
                                newRfc.addAttribute(rfcAttr);
                            }
                        }
                    }
                }
            }
        }
    }

    private Map<String, Map<String,CmsCIRelation>> getExistingCatalogPlatRels(String nsPath) {
        List<CmsCIRelation> catalogPlatRels = cmProcessor.getCIRelationsNaked(nsPath, null, null, null, null);
        Map<String, Map<String,CmsCIRelation>> catalogPlatRelsMap = new HashMap<String, Map<String,CmsCIRelation>>();
        for (CmsCIRelation rel : catalogPlatRels) {
            if (!catalogPlatRelsMap.containsKey(rel.getRelationName())) {
                catalogPlatRelsMap.put(rel.getRelationName(), new HashMap<String,CmsCIRelation>());
            }
            catalogPlatRelsMap.get(rel.getRelationName()).put(rel.getFromCiId() + ":" + rel.getToCiId(), rel);
        }
        return catalogPlatRelsMap;
    }

    private class Edge {
        CmsCIRelation templateRel;
        List<CmsCIRelation> userRels = new ArrayList<CmsCIRelation>();
    }

}
