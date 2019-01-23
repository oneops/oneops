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
package com.oneops.cms.dj.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.crypto.CmsCrypto;
import com.oneops.cms.dj.domain.*;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.exceptions.MDException;
import com.oneops.cms.md.domain.CmsClazzRelation;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.util.CmsDJValidator;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.domain.AttrQueryCondition;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Class CmsCmRfcMrgProcessor.
 */
public class CmsCmRfcMrgProcessor {
    private static Logger logger = Logger.getLogger(CmsCmRfcMrgProcessor.class);

    private CmsRfcProcessor rfcProcessor;
    private CmsCmProcessor cmProcessor;
    private CmsDJValidator djValidator;
    private CmsMdProcessor mdProcessor;
    private CmsRfcUtil rfcUtil;
    private static final Set<String> DUMMY_RELS = Stream.of("manifest.WatchedBy").collect(Collectors.toSet());

    /**
     * Sets the md processor.
     *
     * @param mdProcessor the new md processor
     */
    public void setMdProcessor(CmsMdProcessor mdProcessor) {
        this.mdProcessor = mdProcessor;
    }

    /**
     * Sets the dj validator.
     *
     * @param djValidator the new dj validator
     */
    public void setDjValidator(CmsDJValidator djValidator) {
        this.djValidator = djValidator;
    }

    /**
     * Sets the rfc util.
     *
     * @param rfcUtil the new rfc util
     */
    public void setRfcUtil(CmsRfcUtil rfcUtil) {
        this.rfcUtil = rfcUtil;
    }

    /**
     * Gets the release by name space.
     *
     * @param nsPath the ns path
     * @return the release by name space
     */
    public CmsRelease getReleaseByNameSpace(String nsPath) {
        List<CmsRelease> relList = rfcProcessor.getReleaseBy3(nsPath, null, null);
        if (relList.size() == 0) {
            return null;
        } else {
            return relList.get(0);
        }
    }

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

    /**
     * Upsert ci rfc.
     *
     * @param rfcCi  the rfc ci
     * @param userId the user id
     * @return the cms rfc ci
     */
    public CmsRfcCI upsertCiRfc(CmsRfcCI rfcCi, String userId) {
        return upsertRfcCILocal(rfcCi, userId, null);
    }

    /**
     * Upsert ci rfc.
     *
     * @param rfcCi     the rfc ci
     * @param userId    the user id
     * @param attrValue the attr value
     * @return the cms rfc ci
     */
    public CmsRfcCI upsertCiRfc(CmsRfcCI rfcCi, String userId, String attrValue) {
        return upsertRfcCILocal(rfcCi, userId, attrValue);
    }


    /**
     * Upsert relation rfc.
     *
     * @param rel    the rel
     * @param userId the user id
     * @return the cms rfc relation
     */
    public CmsRfcRelation upsertRelationRfc(CmsRfcRelation rel, String userId) {
        return upsertRelationRfc(rel, userId, null);
    }

    /**
     * Upsert relation rfc.
     *
     * @param rel       the rel
     * @param userId    the user id
     * @param attrValue the attr value
     * @return the cms rfc relation
     */
    public CmsRfcRelation upsertRelationRfc(CmsRfcRelation rel, String userId, String attrValue) {

        CmsRfcCI fromRfcCi = rel.getFromRfcCi();
        CmsRfcCI newFromRfcCi = null;
        if (fromRfcCi != null) {
            fromRfcCi.setCreatedBy(rel.getCreatedBy());
            fromRfcCi.setUpdatedBy(rel.getUpdatedBy());
            newFromRfcCi = upsertCiRfc(fromRfcCi, userId, attrValue);
            rel.setFromCiId(newFromRfcCi.getCiId());
        }

        CmsRfcCI toRfcCi = rel.getToRfcCi();
        CmsRfcCI newToRfcCi = null;
        if (toRfcCi != null) {
            toRfcCi.setCreatedBy(rel.getCreatedBy());
            toRfcCi.setUpdatedBy(rel.getUpdatedBy());
            newToRfcCi = upsertCiRfc(toRfcCi, userId, attrValue);
            rel.setToCiId(newToRfcCi.getCiId());
        }

        CmsRfcRelation newRel = upsertRfcRelationLocal(rel, userId, attrValue);
        newRel.setFromRfcCi(newFromRfcCi);
        newRel.setToRfcCi(newToRfcCi);
        // the special handling for dummy updates
        checkForDummyUpdatesNeeds(newRel, userId);
        return newRel;
    }

    private void checkForDummyUpdatesNeeds(CmsRfcRelation rel, String userId) {
        //if this is a manifest.WatchedBy relation we need to generate dummy update rfc
        if (rel.getRelationName().equals("manifest.WatchedBy") &&
                (rel.getToRfcId() != null || rel.getToRfcCi() != null)) {
            createDummyUpdateRfc(rel.getFromCiId(), rel.getReleaseType(), 0, userId, RfcHint.MONITOR);
        } else if (rel.getRelationName().equals("manifest.EscortedBy") &&
                (rel.getToRfcId() != null || rel.getToRfcCi() != null)) {
            createDummyUpdateRfc(rel.getFromCiId(), rel.getReleaseType(), 0, userId, RfcHint.ATTACHMENT);
        } else if (rel.getRelationName().equals("manifest.LoggedBy") &&
                (rel.getToRfcId() != null || rel.getToRfcCi() != null)) {
            createDummyUpdateRfc(rel.getFromCiId(), rel.getReleaseType(), 0, userId, RfcHint.LOG);
        }
    }

    private void checkForDummyUpdatesNeeds(CmsRfcCI rfc, String userId) {
        //if this is a monitor update we need to generate dummy update rfc
        if (rfc.getCiClassName().equals("manifest.Monitor")
                && (rfc.getRfcAction().equals("update") || rfc.getRfcAction().equals("delete"))) {
            for (CmsCIRelation rel : cmProcessor.getToCIRelationsNakedNoAttrs(rfc.getCiId(), "manifest.WatchedBy", null, null)) {
                createDummyUpdateRfc(rel.getFromCiId(), rfc.getReleaseType(), 0, userId, RfcHint.MONITOR);
            }
        } else if (rfc.getCiClassName().equals("manifest.Log")
                && (rfc.getRfcAction().equals("update") || rfc.getRfcAction().equals("delete"))) {
            for (CmsCIRelation rel : cmProcessor.getToCIRelationsNakedNoAttrs(rfc.getCiId(), "manifest.LoggedBy", null, null)) {
                createDummyUpdateRfc(rel.getFromCiId(), rfc.getReleaseType(), 0, userId, RfcHint.LOG);
            }
        }
        //this is temporary hack until we implement traversal relation logic
//        else if (rfc.getCiClassName().equals("bom.Compute")
//                && rfc.getRfcAction().equals("replace")) {
//            for (CmsCIRelation rel : cmProcessor.getToCIRelationsNakedNoAttrs(rfc.getCiId(), "bom.DependsOn", null, "bom.Lb")) {
//                createDummyUpdateRfc(rel.getFromCiId(), rfc.getReleaseType(), rfc.getExecOrder() + 1, userId);
//            }
//            for (CmsCIRelation rel : cmProcessor.getToCIRelationsNakedNoAttrs(rfc.getCiId(), "bom.DependsOn", null, "bom.Fqdn")) {
//                createDummyUpdateRfc(rel.getFromCiId(), rfc.getReleaseType(), rfc.getExecOrder() + 1, userId);
//            }
//        }
    }


    /**
     * Request ci delete.
     *
     * @param ciId   the ci id
     * @param userId the user id
     * @return the cms rfc ci
     */
    public CmsRfcCI requestCiDelete(long ciId, String userId) {
        return requestCiDeleteCascade(ciId, userId, null, 0);
    }

    /**
     * Request ci delete.
     *
     * @param ciId      the ci id
     * @param userId    the user id
     * @param execOrder the exec order
     * @return the cms rfc ci
     */
    public CmsRfcCI requestCiDelete(long ciId, String userId, int execOrder) {
        return requestCiDeleteCascade(ciId, userId, null, execOrder);
    }

    /**
     * Delete the ci with given {@code ciId} by traversing all the relations recursively.
     *
     * @param ciId      ciId to delete
     * @param userId    user performing delete action
     * @param targets   class relation target map to be deleted for he current iteration.
     * @param execOrder execute order.
     * @return rfc for the {@code ciId} to be deleted.
     */
    private CmsRfcCI requestCiDeleteCascade(long ciId, String userId, Map<String, CmsClazzRelation> targets, int execOrder) {

        CmsCI ci = cmProcessor.getCiById(ciId);
        CmsRfcCI rfcCi = rfcProcessor.getOpenRfcCIByCiId(ciId);
        if (ci == null && rfcCi == null) return null;

        String fromClazz = null;
        if (rfcCi != null) {
            fromClazz = rfcCi.getCiClassName();
        }
        if (ci != null) {
            fromClazz = ci.getCiClassName();
        }

        // First lets submit delete rfcs for the ci rels
        List<CmsRfcRelation> fromRels = getFromCIRelations(ciId, null, null, null);
        for (CmsRfcRelation rel : fromRels) {
            if (rel.getToRfcCi() != null) {
                if (targets == null) targets = new HashMap<>();
                String key = fromClazz + rel.getRelationName() + rel.getToRfcCi().getCiClassName();

                if (!targets.containsKey(key)) {
                    for (CmsClazzRelation target : mdProcessor.getTargets(rel.getRelationId())) {
                        String targetFromClazz = target.getFromClassName().equals("Component") ? fromClazz : target.getFromClassName();
                        String targetToClazz = target.getToClassName().equals("Component") ? rel.getToRfcCi().getCiClassName() : target.getToClassName();
                        String newKey = targetFromClazz + target.getRelationName() + targetToClazz;
                        targets.put(newKey, target);
                    }
                    if (!targets.containsKey(key)) {
                        throw new MDException(CmsError.MD_TARGET_IS_MISSING_ERROR, "Target is missing:" + key);
                    }
                }
                if (targets.get(key).getIsStrong()) {
                    requestCiDeleteCascade(rel.getToRfcCi().getCiId(), userId, targets, execOrder);
                }
            }

            // If this is cross layer relation (like manifest to bom) do not submit
            // an rfc since it will generate another release in bom namespace it will
            // gets deleted when master release gets complete.
            if (!rel.getRelationName().startsWith("base.")) {
                requestRelationDelete(rel.getCiRelationId(), userId, execOrder);
            } else if (ci != null && rel.getNsPath().equals(ci.getNsPath())) {
                requestRelationDelete(rel.getCiRelationId(), userId, execOrder);
            }
        }

        List<CmsRfcRelation> toRels = getToCIRelationsNakedNoAttrs(ciId, null, null, null);
        for (CmsRfcRelation rel : toRels) {
            requestRelationDelete(rel.getCiRelationId(), userId, execOrder);
        }

        CmsRfcCI rfc;
        if (rfcCi != null) {
            rfc = rfcCi;
        } else {
            // we need to check again since some propagations might happened
            rfc = rfcProcessor.getOpenRfcCIByCiId(ciId);
        }
        if (rfc != null) {
            if ("delete".equalsIgnoreCase(rfc.getRfcAction())) return rfc;
            rfcProcessor.rmRfcCiFromRelease(rfc.getRfcId());
        }

        CmsRfcCI newRfc = null;

        if (ci != null) {
            newRfc = generateNewDeleteCiRfc(ci);
            newRfc.setExecOrder(execOrder);
            newRfc.setCreatedBy(userId);
            newRfc.setUpdatedBy(userId);
            rfcProcessor.createRfcCI(newRfc, userId);
            checkForDummyUpdatesNeeds(newRfc, userId);
        }

        return newRfc;
    }

    public CmsRfcCI requestCiDeleteCascadeNoRelsRfcs(long ciId, String userId, int execOrder) {
        return requestCiDeleteCascadeNoRelsRfcs(ciId, userId, null, execOrder);
    }

    public CmsRfcCI requestCiDeleteCascadeNoRelsRfcs(long ciId, String userId, Map<String, CmsClazzRelation> targets, int execOrder) {
        CmsCI ci = cmProcessor.getCiById(ciId);
        CmsRfcCI rfcCi = rfcProcessor.getOpenRfcCIByCiId(ciId);
        if (ci == null && rfcCi == null) return null;

        String fromClazz = null;
        if (rfcCi != null) {
            fromClazz = rfcCi.getCiClassName();
        }
        if (ci != null) {
            fromClazz = ci.getCiClassName();
        }

        List<CmsRfcRelation> fromRels = getFromCIRelations(ciId, null, null, null);
        for (CmsRfcRelation rel : fromRels) {
            if (rel.getToRfcCi() != null) {
                if (targets == null) targets = new HashMap<>();
                String key = fromClazz + rel.getRelationName() + rel.getToRfcCi().getCiClassName();
                if (!targets.containsKey(key)) {
                    for (CmsClazzRelation target : mdProcessor.getTargets(rel.getRelationId())) {
                        String targetFromClazz = target.getFromClassName().equals("Component") ? fromClazz : target.getFromClassName();
                        String targetToClazz = target.getToClassName().equals("Component") ? rel.getToRfcCi().getCiClassName() : target.getToClassName();
                        String newKey = targetFromClazz + target.getRelationName() + targetToClazz;
                        targets.put(newKey, target);
                    }
                    if (!targets.containsKey(key)) {
                        throw new MDException(CmsError.MD_TARGET_IS_MISSING_ERROR, "Target is missing:" + key);
                    }
                }
                if (targets.get(key).getIsStrong()) {
                    requestCiDeleteCascadeNoRelsRfcs(rel.getToRfcCi().getCiId(), userId, targets, execOrder);
                }
            }
        }


        CmsRfcCI rfc;
        if (rfcCi != null) {
            rfc = rfcCi;
        } else {
            //we need to check again since some propagations might happened
            rfc = rfcProcessor.getOpenRfcCIByCiId(ciId);
        }
        if (rfc != null) {
            if ("delete".equalsIgnoreCase(rfc.getRfcAction())) return rfc;
            rfcProcessor.rmRfcCiFromRelease(rfc.getRfcId());
        }

        CmsRfcCI newRfc = null;
        if (ci != null) {
            newRfc = generateNewDeleteCiRfc(ci);
            newRfc.setExecOrder(execOrder);
            newRfc.setCreatedBy(userId);
            newRfc.setUpdatedBy(userId);
            rfcProcessor.createRfcCI(newRfc, userId);
            checkForDummyUpdatesNeeds(newRfc, userId);
        }

        return newRfc;
    }

    /**
     * Request relation delete.
     *
     * @param ciRelationId the ci relation id
     * @param userId       the user id
     */
    public void requestRelationDelete(long ciRelationId, String userId) {
        requestRelationDelete(ciRelationId, userId, 0);
    }

    /**
     * Request relation delete.
     *
     * @param ciRelationId the ci relation id
     * @param userId       the user id
     * @param execOrder    the exec order
     */
    public void requestRelationDelete(long ciRelationId, String userId, int execOrder) {
        CmsCIRelation ciRel = cmProcessor.getRelationById(ciRelationId);
        CmsRfcRelation rfcRel = rfcProcessor.getOpenRfcRelationByCiRelId(ciRelationId);

        if (rfcRel != null) {
            if ("delete".equalsIgnoreCase(rfcRel.getRfcAction())) return;

            //this is total hack
            if ("base.RealizedAs".equalsIgnoreCase(rfcRel.getRelationName())
                    && "add".equalsIgnoreCase(rfcRel.getRfcAction())) {
                //do nothing for now
            } else {
                rfcProcessor.rmRfcRelationFromRelease(rfcRel.getRfcId());
            }
        }

        if (ciRel != null) {
            CmsRfcRelation newRfc = generateNewDeleteRelationRfc(ciRel);
            newRfc.setExecOrder(execOrder);
            newRfc.setCreatedBy(userId);
            newRfc.setUpdatedBy(userId);
            rfcProcessor.createRfcRelation(newRfc, userId);
        }
    }

    private CmsRfcCI generateNewDeleteCiRfc(CmsCI ci) {

        CmsRfcCI newRfc = new CmsRfcCI();

        newRfc.setCiId(ci.getCiId());
        newRfc.setCiClassId(ci.getCiClassId());
        newRfc.setCiClassName(ci.getCiClassName());
        newRfc.setCiGoid(ci.getCiGoid());
        newRfc.setCiName(ci.getCiName());
        newRfc.setComments("deleting");
        newRfc.setNsId(ci.getNsId());
        newRfc.setNsPath(ci.getNsPath());
        newRfc.setRfcAction("delete");

        return newRfc;
    }

    private CmsRfcRelation generateNewDeleteRelationRfc(CmsCIRelation ciRel) {

        CmsRfcRelation newRfc = new CmsRfcRelation();

        newRfc.setCiRelationId(ciRel.getCiRelationId());
        newRfc.setFromCiId(ciRel.getFromCiId());
        newRfc.setToCiId(ciRel.getToCiId());
        newRfc.setNsId(ciRel.getNsId());
        newRfc.setNsPath(ciRel.getNsPath());
        newRfc.setRelationGoid(ciRel.getRelationGoid());
        newRfc.setRelationId(ciRel.getRelationId());
        newRfc.setRelationName(ciRel.getRelationName());
        newRfc.setComments("deleting");
        newRfc.setRfcAction("delete");

        return newRfc;
    }


    /**
     * Gets the ci by id.
     *
     * @param ciId the ci id
     * @return the ci by id
     */
    public CmsRfcCI getCiById(long ciId) {
        return getCiById(ciId, null);
    }


    /**
     * Gets the ci by id.
     *
     * @param ciId        the ci id
     * @param cmAttrValue the cm attr value
     * @return the ci by id
     */
    public CmsRfcCI getCiById(long ciId, String cmAttrValue) {
        CmsCI ci = cmProcessor.getCiById(ciId);
        CmsRfcCI rfcCi = rfcProcessor.getOpenRfcCIByCiId(ciId);
        if (cmAttrValue == null) cmAttrValue = "df";
        return rfcUtil.mergeRfcAndCi(rfcCi, ci, cmAttrValue);
    }

    /**
     * Gets the cis by List id.
     *
     * @param ciIds       the ci id
     * @param cmAttrValue the cm attr value
     * @return the ci by id
     */
    public List<CmsRfcCI> getCiByIdList(List<Long> ciIds, String cmAttrValue) {
        List<CmsCI> ciList = cmProcessor.getCiByIdList(ciIds);
        List<CmsRfcCI> rfcList = rfcProcessor.getOpenRfcCIByCiIdList(ciIds);

        if (cmAttrValue == null) cmAttrValue = "df";
        Map<Long, Cis> rfcciMap = buildCiMap(ciList, rfcList);
        return mergeRfcCiList(rfcciMap.values(), cmAttrValue);
    }

    /**
     * Gets the df dj ci.
     *
     * @param nsPath      the ns path
     * @param clazzName   the clazz name
     * @param ciName      the ci name
     * @param cmAttrValue the cm attr value
     * @return the df dj ci
     */
    public List<CmsRfcCI> getDfDjCi(String nsPath, String clazzName, String ciName, String cmAttrValue) {
        List<CmsCI> ciList = cmProcessor.getCiBy3(nsPath, clazzName, ciName);
        List<CmsRfcCI> rfcList = rfcProcessor.getOpenRfcCIByClazzAndName(nsPath, clazzName, ciName);
        if (cmAttrValue == null) cmAttrValue = "df";
        Map<Long, Cis> rfcciMap = buildCiMap(ciList, rfcList);
        return mergeRfcCiList(rfcciMap.values(), cmAttrValue);
    }

    /**
     * Gets the df dj ci naked lower.
     *
     * @param nsPath      the ns path
     * @param clazzName   the clazz name
     * @param ciName      the ci name
     * @param cmAttrValue the cm attr value
     * @return the df dj ci naked lower
     */
    public List<CmsRfcCI> getDfDjCiNakedLower(String nsPath, String clazzName, String ciName, String cmAttrValue) {
        List<CmsCI> ciList = cmProcessor.getCiBy3NakedLower(nsPath, clazzName, ciName);
        List<CmsRfcCI> rfcList = rfcProcessor.getOpenRfcCIByClazzAndNameLowerNoAttr(nsPath, clazzName, ciName);
        if (cmAttrValue == null) cmAttrValue = "df";
        Map<Long, Cis> rfcciMap = buildCiMap(ciList, rfcList);
        return mergeRfcCiList(rfcciMap.values(), cmAttrValue);
    }


    /**
     * Gets the df dj ci ns like.
     *
     * @param nsPath      the ns path
     * @param clazzName   the clazz name
     * @param ciName      the ci name
     * @param cmAttrValue the cm attr value
     * @return the df dj ci ns like
     */
    public List<CmsRfcCI> getDfDjCiNsLike(String nsPath, String clazzName, String ciName, String cmAttrValue) {
        List<CmsCI> ciList = cmProcessor.getCiBy3NsLike(nsPath, clazzName, ciName);
        List<CmsRfcCI> rfcList = rfcProcessor.getOpenRfcCIByNsLike(nsPath, clazzName, ciName);
        if (cmAttrValue == null) cmAttrValue = "df";
        Map<Long, Cis> rfcciMap = buildCiMap(ciList, rfcList);
        return mergeRfcCiList(rfcciMap.values(), cmAttrValue);
    }


    /**
     * Gets the df dj ci.
     *
     * @param nsPath      the ns path
     * @param clazzName   the clazz name
     * @param ciName      the ci name
     * @param cmAttrValue the cm attr value
     * @param attrConds   the attr conds
     * @return the df dj ci
     */
    public List<CmsRfcCI> getDfDjCi(String nsPath, String clazzName, String ciName, String cmAttrValue, List<AttrQueryCondition> attrConds) {
        List<CmsCI> ciList = cmProcessor.getCiBy3(nsPath, clazzName, ciName);
        List<CmsRfcCI> rfcList = rfcProcessor.getOpenRfcCIByClazzAndName(nsPath, clazzName, ciName);
        if (cmAttrValue == null) cmAttrValue = "df";
        Map<Long, Cis> rfcciMap = buildCiMap(ciList, rfcList);
        List<CmsRfcCI> resultList = mergeRfcCiList(rfcciMap.values(), cmAttrValue);

        return filterRfcCis(resultList, attrConds);
    }

    private List<CmsRfcCI> filterRfcCis(List<CmsRfcCI> rfcs, List<AttrQueryCondition> attrConds) {
        List<CmsRfcCI> resultList = new ArrayList<>();
        for (CmsRfcCI rfc : rfcs) {
            if (satisfies(rfc, attrConds)) {
                resultList.add(rfc);
            }
        }
        return resultList;
    }

    private List<CmsRfcCI> mergeRfcCiList(Collection<Cis> cisList, String cmAttrValue) {
        List<CmsRfcCI> rfcCis = new ArrayList<>();
        for (Cis cis : cisList) {
            CmsRfcCI rfcCi = rfcUtil.mergeRfcAndCi(cis.getRfcCi(), cis.getCi(), cmAttrValue);
            if (rfcCi != null) {
                rfcCis.add(rfcCi);
            }
        }
        return rfcCis;
    }

    private Map<Long, Cis> buildCiMap(List<CmsCI> ciList, List<CmsRfcCI> rfcList) {
        Map<Long, Cis> cisMap = new HashMap<>();
        for (CmsCI ci : ciList) {
            Cis cis = new Cis();
            cis.setCi(ci);
            cisMap.put(ci.getCiId(), cis);
        }

        if (rfcList != null) {
            for (CmsRfcCI rfcCi : rfcList) {
                if (cisMap.containsKey(rfcCi.getCiId())) {
                    cisMap.get(rfcCi.getCiId()).setRfcCi(rfcCi);
                } else {
                    Cis cis = new Cis();
                    cis.setRfcCi(rfcCi);
                    cisMap.put(rfcCi.getCiId(), cis);
                }
            }
        }
        return cisMap;
    }

    /**
     * Creates the dummy update rfc.
     *
     * @param ciId        the ci id
     * @param releaseType the release type
     * @param execOrder   the exec order
     * @param userId      the user id
     * @param hint        hint
     * @return the cms rfc ci
     */
    public CmsRfcCI createDummyUpdateRfc(long ciId, String releaseType, int execOrder, String userId, String hint) {
        //first lets check if there is an rfc already
        CmsRfcCI existingRfc = rfcProcessor.getOpenRfcCIByCiId(ciId);
        if (existingRfc != null) {
            if (hint != null && !hint.equals(existingRfc.getHint())) {
                existingRfc.setHint(hint);
                rfcProcessor.updateRfcExecOrder(existingRfc);
            }
            return existingRfc;
        }

        CmsCI ci = cmProcessor.getCiByIdNaked(ciId);
        if (ci == null) return null;

        CmsRfcCI rfcCi = newRfcCi(ci, releaseType, execOrder, userId);
        if (hint != null) rfcCi.setHint(hint);
        return createRfc(rfcCi, userId);
    }

    private CmsRfcCI newRfcCi(CmsCI ci, String releaseType, int execOrder, String userId) {
        CmsRfcCI rfcCi = new CmsRfcCI();
        rfcCi.setCiId(ci.getCiId());
        rfcCi.setCiClassName(ci.getCiClassName());
        rfcCi.setCiClassId(ci.getCiClassId());

        rfcCi.setCiName(ci.getCiName());
        rfcCi.setNsPath(ci.getNsPath());
        rfcCi.setCiGoid(ci.getCiGoid());
        rfcCi.setComments(ci.getComments());
        rfcCi.setCreated(ci.getCreated());
        rfcCi.setExecOrder(execOrder);
        rfcCi.setReleaseType(releaseType);

        rfcCi.setRfcAction("update");
        rfcCi.setCreatedBy(userId);
        rfcCi.setUpdatedBy(userId);
        return rfcCi;
    }

    private CmsRfcCI createRfc(CmsRfcCI rfcCi, String userId) {
        long newRfcId = rfcProcessor.createRfcCI(rfcCi, userId);
        return rfcProcessor.getRfcCIById(newRfcId);
    }

    /**
     * Gets the df dj relations.
     *
     * @param relationName  the relation name
     * @param shortRelName  the short rel name
     * @param nsPath        the ns path
     * @param fromClazzName the from clazz name
     * @param toClazzName   the to clazz name
     * @param cmAttrValue   the cm attr value
     * @return the df dj relations
     */
    public List<CmsRfcRelation> getDfDjRelations(
            String relationName, String shortRelName, String nsPath, String fromClazzName, String toClazzName, String cmAttrValue) {
        return getDfDjRelationsWithCIs(relationName, shortRelName, nsPath, fromClazzName, toClazzName, cmAttrValue, false, false, null);

    }

    /**
     * Gets the df dj relations with c is.
     *
     * @param relationName   the relation name
     * @param shortRelName   the short rel name
     * @param nsPath         the ns path
     * @param fromClazzName  the from clazz name
     * @param toClazzName    the to clazz name
     * @param cmAttrValue    the cm attr value
     * @param includeFromCi  the include from ci
     * @param includeToCi    the include to ci
     * @param attrConditions the attr conditions
     * @return the df dj relations with c is
     */
    public List<CmsRfcRelation> getDfDjRelationsWithCIs(
            String relationName, String shortRelName, String nsPath, String fromClazzName, String toClazzName, String cmAttrValue, boolean includeFromCi, boolean includeToCi, List<AttrQueryCondition> attrConditions) {

        if (cmAttrValue == null) cmAttrValue = "df";

        List<CmsCIRelation> cmRelations = cmProcessor.getCIRelationsNaked(nsPath, relationName, shortRelName, fromClazzName, toClazzName);
        List<CmsRfcRelation> rfcRelations = rfcProcessor.getOpenRfcRelationsNaked(relationName, shortRelName, nsPath, fromClazzName, toClazzName);

        Map<Long, Relations> relsMap = buildRelationsMap(cmRelations, rfcRelations);

        return mergeRelations(relsMap.values(), cmAttrValue, includeFromCi, includeToCi, attrConditions);

    }

    //this call just for backwards compatibility

    /**
     * Gets the df dj relations ns like.
     *
     * @param relationName  the relation name
     * @param shortRelName  the short rel name
     * @param nsPath        the ns path
     * @param fromClazzName the from clazz name
     * @param toClazzName   the to clazz name
     * @param cmAttrValue   the cm attr value
     * @return the df dj relations ns like
     */
    public List<CmsRfcRelation> getDfDjRelationsNsLike(
            String relationName, String shortRelName, String nsPath, String fromClazzName, String toClazzName, String cmAttrValue) {

        return getDfDjRelationsNsLikeWithCIs(relationName, shortRelName, nsPath, fromClazzName, toClazzName, cmAttrValue, false, false, null);
    }

    /**
     * Gets the df dj relations ns like with c is.
     *
     * @param relationName   the relation name
     * @param shortRelName   the short rel name
     * @param nsPath         the ns path
     * @param fromClazzName  the from clazz name
     * @param toClazzName    the to clazz name
     * @param cmAttrValue    the cm attr value
     * @param includeFromCi  the include from ci
     * @param includeToCi    the include to ci
     * @param attrConditions the attr conditions
     * @return the df dj relations ns like with c is
     */
    public List<CmsRfcRelation> getDfDjRelationsNsLikeWithCIs(
            String relationName, String shortRelName, String nsPath, String fromClazzName, String toClazzName, String cmAttrValue, boolean includeFromCi, boolean includeToCi, List<AttrQueryCondition> attrConditions) {

        if (cmAttrValue == null) cmAttrValue = "df";

        List<CmsCIRelation> cmRelations = cmProcessor.getCIRelationsNsLikeNaked(nsPath, relationName, shortRelName, fromClazzName, toClazzName);
        List<CmsRfcRelation> rfcRelations = rfcProcessor.getOpenRfcRelationsNsLikeNaked(relationName, shortRelName, nsPath, fromClazzName, toClazzName);

        Map<Long, Relations> relsMap = buildRelationsMap(cmRelations, rfcRelations);

        return mergeRelations(relsMap.values(), cmAttrValue, includeFromCi, includeToCi, attrConditions);
    }


    /**
     * Gets the from ci relations.
     *
     * @param fromId       the from id
     * @param relationName the relation name
     * @param toClazzName  the to clazz name
     * @param cmAttrValue  the cm attr value
     * @return the from ci relations
     */
    public List<CmsRfcRelation> getFromCIRelations(long fromId,
                                                   String relationName, String toClazzName, String cmAttrValue) {
        return getFromCIRelations(fromId, relationName, null, toClazzName, cmAttrValue);
    }

    /**
     * Gets the from ci relations.
     *
     * @param fromId            the from id
     * @param relationName      the relation name
     * @param shortRelationName the short relation name
     * @param toClazzName       the to clazz name
     * @param cmAttrValue       the cm attr value
     * @return the from ci relations
     */
    public List<CmsRfcRelation> getFromCIRelations(long fromId,
                                                   String relationName, String shortRelationName, String toClazzName, String cmAttrValue) {

        return getFromCIRelationsByAttrs(fromId, relationName, shortRelationName, toClazzName, cmAttrValue, null);
    }

    /**
     * gets the ci relations by nsPath
     */
    public List<CmsRfcRelation> getCIRelations(String nsPath, String relationName, String shortRelationName, 
    		String fromClazzName, String toClazzName, String cmAttrValue) {
        return getCIRelationsByAttrs(nsPath, relationName, shortRelationName, fromClazzName, toClazzName, cmAttrValue, null);
    }

    /**
     * Gets the from ci relations by attrs.
     *
     * @param fromId            the from id
     * @param relationName      the relation name
     * @param shortRelationName the short relation name
     * @param toClazzName       the to clazz name
     * @param cmAttrValue       the cm attr value
     * @param attrConditions    the attr conditions
     * @return the from ci relations by attrs
     */
    public List<CmsRfcRelation> getFromCIRelationsByAttrs(
            long fromId, String relationName, String shortRelationName, String toClazzName, String cmAttrValue, List<AttrQueryCondition> attrConditions) {

        if (cmAttrValue == null) cmAttrValue = "df";

        List<CmsCIRelation> cmRelations = cmProcessor.getFromCIRelationsNaked(fromId, relationName, shortRelationName, toClazzName);
        List<CmsRfcRelation> rfcRelations = (toClazzName != null) ?
                rfcProcessor.getOpenFromRfcRelationByTargetClazz(fromId, relationName, shortRelationName, toClazzName) : rfcProcessor.getOpenRfcRelationBy2(fromId, null, relationName, shortRelationName);

        Map<Long, Relations> relsMap = buildRelationsMap(cmRelations, rfcRelations);

        return mergeRelations(relsMap.values(), cmAttrValue, false, true, attrConditions);
    }

    public List<CmsRfcRelation> getCIRelationsByAttrs(
            String nsPath, String relationName, String shortRelationName, String fromClazzName, String toClazzName, String cmAttrValue, List<AttrQueryCondition> attrConditions) {

        if (cmAttrValue == null) cmAttrValue = "df";

        List<CmsCIRelation> cmRelations = cmProcessor.getCIRelationsNaked(nsPath, relationName, shortRelationName, fromClazzName, toClazzName);
        List<CmsRfcRelation> rfcRelations = (toClazzName != null) ?
                rfcProcessor.getOpenRfcRelationsNaked(relationName, shortRelationName, nsPath, fromClazzName, toClazzName) : 
                rfcProcessor.getOpenRfcRelationBy2(null, null, relationName, shortRelationName);

        Map<Long, Relations> relsMap = buildRelationsMap(cmRelations, rfcRelations);

        return mergeRelations(relsMap.values(), cmAttrValue, false, true, attrConditions);
    }


    private Map<Long, Relations> buildRelationsMap(List<CmsCIRelation> cmRelations, List<CmsRfcRelation> rfcRelations) {
        Map<Long, Relations> relsMap = new HashMap<>();
        for (CmsCIRelation ciRel : cmRelations) {
            Relations rels = new Relations();
            rels.setCiRelation(ciRel);
            relsMap.put(ciRel.getCiRelationId(), rels);
        }

        if (rfcRelations != null) {
            for (CmsRfcRelation rfcRel : rfcRelations) {
                if (relsMap.containsKey(rfcRel.getCiRelationId())) {
                    relsMap.get(rfcRel.getCiRelationId()).setRfcRelation(rfcRel);
                } else {
                    Relations rels = new Relations();
                    rels.setRfcRelation(rfcRel);
                    relsMap.put(rfcRel.getCiRelationId(), rels);
                }
            }
        }
        return relsMap;
    }

    /**
     * Gets the from ci relations naked.
     *
     * @param fromId            the from id
     * @param relationName      the relation name
     * @param shortRelationName the short relation name
     * @param toClazzName       the to clazz name
     * @return the from ci relations naked
     */
    public List<CmsRfcRelation> getFromCIRelationsNaked(long fromId,
                                                        String relationName, String shortRelationName, String toClazzName) {

        List<CmsCIRelation> cmRelations = cmProcessor.getFromCIRelationsNaked(fromId, relationName, shortRelationName, toClazzName);
        List<CmsRfcRelation> rfcRelations = (toClazzName != null) ?
                rfcProcessor.getOpenFromRfcRelationByTargetClazz(fromId, relationName, shortRelationName, toClazzName) : rfcProcessor.getOpenRfcRelationBy2(fromId, null, relationName, shortRelationName);

        Map<Long, Relations> relsMap = buildRelationsMap(cmRelations, rfcRelations);
        List<CmsRfcRelation> result = new ArrayList<>();
        for (Relations rels : relsMap.values()) {
            CmsRfcRelation mergedRel = rfcUtil.mergeRfcRelAndCiRel(rels.getRfcRelation(), rels.getCiRelation(), "df");
            if (mergedRel != null) {
                result.add(mergedRel);
            }
        }
        return result;
    }


    /**
     * Gets the from ci relations naked no attrs.
     *
     * @param fromId            the from id
     * @param relationName      the relation name
     * @param shortRelationName the short relation name
     * @param toClazzName       the to clazz name
     * @return the from ci relations naked no attrs
     */
    public List<CmsRfcRelation> getFromCIRelationsNakedNoAttrs(long fromId,
                                                               String relationName, String shortRelationName, String toClazzName) {

        List<CmsCIRelation> cmRelations = cmProcessor.getFromCIRelationsNakedNoAttrs(fromId, relationName, shortRelationName, toClazzName);
        List<CmsRfcRelation> rfcRelations = (toClazzName != null) ?
                rfcProcessor.getOpenFromRfcRelationByTargetClazz(fromId, relationName, shortRelationName, toClazzName) : rfcProcessor.getOpenRfcRelationBy2(fromId, null, relationName, shortRelationName);

        Map<Long, Relations> relsMap = buildRelationsMap(cmRelations, rfcRelations);
        List<CmsRfcRelation> result = new ArrayList<>();
        for (Relations rels : relsMap.values()) {
            CmsRfcRelation mergedRel = rfcUtil.mergeRfcRelAndCiRel(rels.getRfcRelation(), rels.getCiRelation(), "df");
            if (mergedRel != null) {
                result.add(mergedRel);
            }
        }
        return result;
    }

    /**
     * Gets the exisitng rfc relation.
     *
     * @param fromCiId the from ci id
     * @param relName  the rel name
     * @param toCiId   the to ci id
     * @return the exisitng rfc relation
     */
    public CmsRfcRelation getExisitngRfcRelation(long fromCiId, String relName, long toCiId) {
        List<CmsRfcRelation> rfcList = rfcProcessor.getOpenRfcRelationBy2NoAttrs(fromCiId, toCiId, relName, null);
        if (rfcList.size() > 0) {
            return rfcList.get(0);
        } else {
            return null;
        }
    }

    /**
     * Gets the to ci relations naked.
     *
     * @param toId              the to id
     * @param relationName      the relation name
     * @param shortRelationName the short relation name
     * @param fromClazzName     the from clazz name
     * @return the to ci relations naked
     */
    public List<CmsRfcRelation> getToCIRelationsNaked(long toId,
                                                      String relationName, String shortRelationName, String fromClazzName) {

        List<CmsCIRelation> cmRelations = cmProcessor.getToCIRelationsNaked(toId, relationName, shortRelationName, fromClazzName);
        List<CmsRfcRelation> rfcRelations = (fromClazzName != null) ?
                rfcProcessor.getOpenToRfcRelationByTargetClazz(toId, relationName, shortRelationName, fromClazzName) : rfcProcessor.getOpenRfcRelationBy2(null, toId, relationName, shortRelationName);

        Map<Long, Relations> relsMap = buildRelationsMap(cmRelations, rfcRelations);
        List<CmsRfcRelation> result = new ArrayList<>();
        for (Relations rels : relsMap.values()) {
            CmsRfcRelation mergedRel = rfcUtil.mergeRfcRelAndCiRel(rels.getRfcRelation(), rels.getCiRelation(), "df");
            if (mergedRel != null) {
                result.add(mergedRel);
            }
        }
        return result;
    }

    /**
     * Gets the to ci relations naked no attrs.
     *
     * @param toId              the to id
     * @param relationName      the relation name
     * @param shortRelationName the short relation name
     * @param fromClazzName     the from clazz name
     * @return the to ci relations naked no attrs
     */
    public List<CmsRfcRelation> getToCIRelationsNakedNoAttrs(long toId,
                                                             String relationName, String shortRelationName, String fromClazzName) {

        List<CmsCIRelation> cmRelations = cmProcessor.getToCIRelationsNakedNoAttrs(toId, relationName, shortRelationName, fromClazzName);
        List<CmsRfcRelation> rfcRelations = (fromClazzName != null) ?
                rfcProcessor.getOpenToRfcRelationByTargetClazz(toId, relationName, shortRelationName, fromClazzName) : rfcProcessor.getOpenRfcRelationBy2(null, toId, relationName, shortRelationName);

        Map<Long, Relations> relsMap = buildRelationsMap(cmRelations, rfcRelations);
        List<CmsRfcRelation> result = new ArrayList<>();
        for (Relations rels : relsMap.values()) {
            CmsRfcRelation mergedRel = rfcUtil.mergeRfcRelAndCiRel(rels.getRfcRelation(), rels.getCiRelation(), "df");
            if (mergedRel != null) {
                result.add(mergedRel);
            }
        }
        return result;
    }


    /**
     * Gets the to ci relations.
     *
     * @param toId          the to id
     * @param relationName  the relation name
     * @param fromClazzName the from clazz name
     * @param cmAttrValue   the cm attr value
     * @return the to ci relations
     */
    public List<CmsRfcRelation> getToCIRelations(long toId,
                                                 String relationName, String fromClazzName, String cmAttrValue) {

        return getToCIRelations(toId, relationName, null, fromClazzName, cmAttrValue);

    }

    /**
     * Gets the to ci relations.
     *
     * @param toId              the to id
     * @param relationName      the relation name
     * @param shortRelationName the short relation name
     * @param fromClazzName     the from clazz name
     * @param cmAttrValue       the cm attr value
     * @return the to ci relations
     */
    public List<CmsRfcRelation> getToCIRelations(
            long toId, String relationName, String shortRelationName, String fromClazzName, String cmAttrValue) {

        return getToCIRelationsByAttrs(toId, relationName, shortRelationName, fromClazzName, cmAttrValue, null);
    }

    //seems like it's easier to just filter in memmory the attributes
    //rather then filter it on DB level since there are lots if tricky usecases

    /**
     * Gets the to ci relations by attrs.
     *
     * @param toId              the to id
     * @param relationName      the relation name
     * @param shortRelationName the short relation name
     * @param fromClazzName     the from clazz name
     * @param cmAttrValue       the cm attr value
     * @param attrConditions    the attr conditions
     * @return the to ci relations by attrs
     */
    public List<CmsRfcRelation> getToCIRelationsByAttrs(
            long toId, String relationName, String shortRelationName, String fromClazzName, String cmAttrValue, List<AttrQueryCondition> attrConditions) {

        if (cmAttrValue == null) cmAttrValue = "df";

        List<CmsCIRelation> cmRelations = cmProcessor.getToCIRelationsNaked(toId, relationName, shortRelationName, fromClazzName);
        List<CmsRfcRelation> rfcRelations = (fromClazzName != null) ?
                rfcProcessor.getOpenToRfcRelationByTargetClazz(toId, relationName, shortRelationName, fromClazzName) : rfcProcessor.getOpenRfcRelationBy2(null, toId, relationName, shortRelationName);

        Map<Long, Relations> relsMap = buildRelationsMap(cmRelations, rfcRelations);

        return mergeRelations(relsMap.values(), cmAttrValue, true, false, attrConditions);
    }


    /**
     * Gets the cI relation.
     *
     * @param ciRelationId the ci relation id
     * @param ciAttrValue  the ci attr value
     * @return the cI relation
     */
    public CmsRfcRelation getCIRelation(long ciRelationId, String ciAttrValue) {
        CmsCIRelation cmRelation = cmProcessor.getRelationById(ciRelationId);
        CmsRfcRelation rfcRelation = rfcProcessor.getOpenRfcRelationByCiRelId(ciRelationId);
        return rfcUtil.mergeRfcRelAndCiRel(rfcRelation, cmRelation, ciAttrValue);
    }

    private List<CmsRfcRelation> mergeRelations(
            Collection<Relations> relsList, String cmAttrValue, boolean includeFromCi, boolean includeToCi, List<AttrQueryCondition> attrConditions) {
        List<CmsRfcRelation> resultRels = new ArrayList<>();
        for (Relations twopl : relsList) {
            CmsRfcRelation rel = rfcUtil.mergeRfcRelAndCiRel(twopl.getRfcRelation(), twopl.getCiRelation(), cmAttrValue);
            if (rel != null) {
                if (attrConditions != null) {
                    //seems like it's easier to just filter in memory the attributes
                    //rather then filter it on DB level since there are lots if tricky use cases
                    if (satisfies(rel, attrConditions)) {
                        resultRels.add(rel);
                    }
                } else {
                    resultRels.add(rel);
                }
            }
        }

        populateRelCis(resultRels, includeFromCi, includeToCi, cmAttrValue);
        return resultRels;
    }


    private void populateRelCis(List<CmsRfcRelation> rels, boolean fromCis, boolean toCis, String cmAttrValue) {

        if (rels.size() == 0) {
            return;
        }

        if (toCis) {
            Set<Long> toCiIds = new HashSet<>();
            for (CmsRfcRelation rel : rels) {
                toCiIds.add(rel.getToCiId());
            }
            Map<Long, CmsRfcCI> ciMap = new HashMap<>();
            for (CmsRfcCI rfc : getCiByIdList(new ArrayList<>(toCiIds), cmAttrValue)) {
                ciMap.put(rfc.getCiId(), rfc);
            }
            for (CmsRfcRelation rel : rels) {
                rel.setToRfcCi(ciMap.get(rel.getToCiId()));
            }
        }
        if (fromCis) {
            Set<Long> fromCiIds = new HashSet<>();
            for (CmsRfcRelation rel : rels) {
                fromCiIds.add(rel.getFromCiId());
            }
            Map<Long, CmsRfcCI> ciMap = new HashMap<>();
            for (CmsRfcCI ci : getCiByIdList(new ArrayList<>(fromCiIds), cmAttrValue)) {
                ciMap.put(ci.getCiId(), ci);
            }
            for (CmsRfcRelation rel : rels) {
                rel.setFromRfcCi(ciMap.get(rel.getFromCiId()));
            }
        }
    }

	
/*
    private void populateRelCIs(List<CmsRfcRelation> relsList, String cmAttrValue, boolean includeFromCi, boolean includeToCi) {
		if (includeFromCi) {
			for (CmsRfcRelation rel : relsList) {
				rel.setFromRfcCi(getCiById(rel.getFromCiId(),cmAttrValue));
			}
		} 

		if (includeToCi) {
			for (CmsRfcRelation rel : relsList) {
				rel.setToRfcCi(getCiById(rel.getToCiId(),cmAttrValue));
			}
		} 
	}
	
	
	private List<CmsRfcRelation> mergeAndPopulate(Collection<Relations> relsList, String direction, String cmAttrValue, List<AttrQueryCondition> attrConditions) {
		List<CmsRfcRelation> result = mergeRelations(relsList, cmAttrValue, attrConditions); 

		for (CmsRfcRelation rel : result) {
			if ("from".equalsIgnoreCase(direction)) {
				CmsRfcCI rfcCi = getCiById(rel.getToCiId(),cmAttrValue);
				rel.setToRfcCi(rfcCi);
			} else {
				CmsRfcCI rfcCi = getCiById(rel.getFromCiId(),cmAttrValue);
				rel.setFromRfcCi(rfcCi);
			}
		}
		return result;
	}
	
	/*
	private List<CmsRfcRelation> mergeFilterAndPopulate(Collection<Relations> relsList, String direction, String cmAttrValue, List<AttrQueryCondition> attrConditions) {
		List<CmsRfcRelation> result = new ArrayList<CmsRfcRelation>();
		for (Relations twopl : relsList) {
			CmsRfcRelation rel = rfcUtil.mergeRfcRelAndCiRel(twopl.getRfcRelation(), twopl.getCiRelation(),cmAttrValue);
			if (rel != null) {
				if (satisfies(rel,attrConditions)) {
					if ("from".equalsIgnoreCase(direction)) {
						CmsRfcCI rfcCi = getCiById(rel.getToCiId(),cmAttrValue);
						rel.setToRfcCi(rfcCi);
					} else {
						CmsRfcCI rfcCi = getCiById(rel.getFromCiId(),cmAttrValue);
						rel.setFromRfcCi(rfcCi);
					}
					result.add(rel);
				}
			}
		}
		return result;
	}
	*/

    private boolean satisfies(CmsRfcRelation rel, List<AttrQueryCondition> attrConditions) {
        boolean result = false;
        for (AttrQueryCondition cond : attrConditions) {
            if (rel.getAttribute(cond.getAttributeName()) == null) {
                return false;
            }
            if ("eq".equalsIgnoreCase(cond.getCondition())) {
                result = cond.getAvalue().equalsIgnoreCase(rel.getAttribute(cond.getAttributeName()).getNewValue());
            } else if ("neq".equalsIgnoreCase(cond.getCondition())) {
                result = !cond.getAvalue().equalsIgnoreCase(rel.getAttribute(cond.getAttributeName()).getNewValue());
            } else if (">".equalsIgnoreCase(cond.getCondition())) {
                result = Integer.valueOf(rel.getAttribute(cond.getAttributeName()).getNewValue()) > Integer.valueOf(cond.getAvalue());
            } else if ("<".equalsIgnoreCase(cond.getCondition())) {
                result = Integer.valueOf(rel.getAttribute(cond.getAttributeName()).getNewValue()) < Integer.valueOf(cond.getAvalue());
            }
            if (!result) {
                return false;
            }
        }
        return true;
    }

    private boolean satisfies(CmsRfcCI rfc, List<AttrQueryCondition> attrConditions) {
        for (AttrQueryCondition cond : attrConditions) {
            if (rfc.getAttribute(cond.getAttributeName()) == null) {
                return false;
            }
            if ("eq".equalsIgnoreCase(cond.getCondition())) {
                return cond.getAvalue().equalsIgnoreCase(rfc.getAttribute(cond.getAttributeName()).getNewValue());
            } else if ("neq".equalsIgnoreCase(cond.getCondition())) {
                return !cond.getAvalue().equalsIgnoreCase(rfc.getAttribute(cond.getAttributeName()).getNewValue());
            } else if (">".equalsIgnoreCase(cond.getCondition())) {
                return Integer.valueOf(rfc.getAttribute(cond.getAttributeName()).getNewValue()) > Integer.valueOf(cond.getAvalue());
            } else if ("<".equalsIgnoreCase(cond.getCondition())) {
                return Integer.valueOf(rfc.getAttribute(cond.getAttributeName()).getNewValue()) < Integer.valueOf(cond.getAvalue());
            }
        }
        return true;
    }

    private CmsRfcCI upsertRfcCILocal(CmsRfcCI rfcCi, String userId, String attrValue) {
        if (attrValue == null) attrValue = "df";
        CmsRfcCI resultRfc;
        if (rfcCi.getCiId() == 0 && rfcCi.getRfcId() == 0) {
            //this is brand new ci
            //lets check if the ci or rfc already exists and throw an exception
            List<CmsRfcCI> existingRfcList = getDfDjCi(rfcCi.getNsPath(), rfcCi.getCiClassName(), rfcCi.getCiName(), "dj");
            if (existingRfcList.size() > 0) {
                String errMsg = "the ci/rfc with this ci_name already exists in this namesapce: ci_name = " + rfcCi.getCiName() + "; ns = " + rfcCi.getNsPath() + "; class = " + rfcCi.getCiClassName();
                logger.error(errMsg);
                throw new DJException(CmsError.DJ_CI_RFC_WITH_THIS_NAME_ALREADY_EXIST_ERROR, errMsg);
            }
            rfcCi.setRfcAction("add");
            //proceed to create "add" rfc
            long newRfcId = rfcProcessor.createRfcCINoCheck(rfcCi, userId);
            resultRfc = rfcProcessor.getRfcCIById(newRfcId);
        } else if (rfcCi.getRfcId() > 0 && rfcCi.getCiId() == 0) {
            //this should never happen raise an error
            String errMsg = "the ci_id needs to be provided for the rfc_id = " + rfcCi.getRfcId();
            logger.error(errMsg);
            throw new DJException(CmsError.DJ_CI_ID_IS_NEED_ERROR, errMsg);
        } else if (rfcCi.getRfcId() == 0 && rfcCi.getCiId() > 0) {
            //this should be an new "update" rfc lets figure out delta
            rfcCi.setRfcAction("update");
            CmsRfcCI baseCi = getCiById(rfcCi.getCiId(), attrValue);
            CmsRfcCI updRfc = generateNewRfc(rfcCi, baseCi);
            if (updRfc != null) {
                rfcProcessor.createRfcCINoCheck(updRfc, userId);
                resultRfc = getCiById(rfcCi.getCiId(), attrValue);
            } else {
                resultRfc = baseCi;
            }
        } else {
            //we need to update exisitng rfc
            CmsRfcCI baseCi = getCiById(rfcCi.getCiId(), attrValue);
            CmsRfcCI updRfc = generateNewRfc(rfcCi, baseCi);
            if (updRfc != null) {
                rfcProcessor.updateRfcCI(updRfc);
                resultRfc = getCiById(rfcCi.getCiId(), attrValue);
            } else {
                resultRfc = baseCi;
            }
        }

        if (resultRfc != null && resultRfc.getRfcId() > 0) {
            checkForDummyUpdatesNeeds(resultRfc, userId);
        }

        return resultRfc;
    }

    public CmsRfcCI upsertRfcCINoChecks(CmsRfcCI rfcCi, String userId, String attrValue) {
        if (attrValue == null) attrValue = "df";
        CmsRfcCI resultRfc;
        if (rfcCi.getCiId() == 0 && rfcCi.getRfcId() == 0) {
            //this is brand new ci
            //lets check if the ci or rfc already exists and throw an exception
            rfcCi.setRfcAction("add");
            //proceed to create "add" rfc
            long newRfcId = rfcProcessor.createRfcCINoCheck(rfcCi, userId);
            resultRfc = rfcProcessor.getRfcCIByIdNoAttrs(newRfcId);
        } else if (rfcCi.getRfcId() > 0 && rfcCi.getCiId() == 0) {
            //this should never happen raise an error
            String errMsg = "the ci_id needs to be provided for the rfc_id = " + rfcCi.getRfcId();
            logger.error(errMsg);
            throw new DJException(CmsError.DJ_CI_ID_IS_NEED_ERROR, errMsg);
        } else if (rfcCi.getRfcId() == 0 && rfcCi.getCiId() > 0) {
            //this should be an new "update" rfc lets figure out delta
            rfcCi.setRfcAction("update");
            CmsRfcCI baseCi = getCiById(rfcCi.getCiId(), attrValue);
            CmsRfcCI updRfc = generateNewRfc(rfcCi, baseCi);
            if (updRfc != null) {
                long newRfcId = rfcProcessor.createRfcCINoCheck(updRfc, userId);
                //resultRfc = getCiById(rfcCi.getCiId(), attrValue);
                resultRfc = rfcProcessor.getRfcCIByIdNoAttrs(newRfcId);
            } else {
                resultRfc = baseCi;
            }
        } else {
            //we need to update exisitng rfc
            CmsRfcCI baseCi = getCiById(rfcCi.getCiId(), attrValue);
            CmsRfcCI updRfc = generateNewRfc(rfcCi, baseCi);
            if (updRfc != null) {
                long newRfcId = rfcProcessor.updateRfcCI(updRfc);
                //resultRfc = getCiById(rfcCi.getCiId(), attrValue);
                resultRfc = rfcProcessor.getRfcCIByIdNoAttrs(newRfcId);
            } else {
                resultRfc = baseCi;
            }
        }

        if (resultRfc != null && resultRfc.getRfcId() > 0) {
            checkForDummyUpdatesNeeds(resultRfc, userId);
        }

        return resultRfc;
    }


    private CmsRfcCI generateNewRfc(CmsRfcCI rfcCi, CmsRfcCI baseCi) {

        boolean needUpdate = false;
        CmsRfcCI newRfc = new CmsRfcCI();
        if (baseCi == null) {
            //this should never happen raise an error
            String errMsg = "the is no ci or rfc for ci_id = " + rfcCi.getCiId();
            logger.error(errMsg);
            throw new DJException(CmsError.DJ_NO_CI_WITH_GIVEN_ID_ERROR, errMsg);
        }

        newRfc.setRfcId(rfcCi.getRfcId());
        newRfc.setCiId(rfcCi.getCiId());
        newRfc.setCiClassName(rfcCi.getCiClassName());
        newRfc.setCiGoid(rfcCi.getCiGoid());
        newRfc.setCiName(rfcCi.getCiName());
        newRfc.setComments(rfcCi.getComments());
        newRfc.setExecOrder(rfcCi.getExecOrder());
        newRfc.setNsPath(rfcCi.getNsPath());
        newRfc.setLastAppliedRfcId(rfcCi.getLastAppliedRfcId());
        newRfc.setReleaseId(rfcCi.getReleaseId());
        if ("replace".equals(baseCi.getCiState())) {
            newRfc.setRfcAction("replace");
            needUpdate = true;
        } else {
            newRfc.setRfcAction(rfcCi.getRfcAction());
        }
        newRfc.setCreatedBy(rfcCi.getCreatedBy());
        newRfc.setUpdatedBy(rfcCi.getUpdatedBy());

        needUpdate = (!(newRfc.getCiName().equalsIgnoreCase(baseCi.getCiName()))) || needUpdate;

        for (CmsRfcAttribute attr : rfcCi.getAttributes().values()) {
            CmsRfcAttribute existingAttr = baseCi.getAttribute(attr.getAttributeName());
            if (CmsCrypto.ENC_DUMMY.equals(attr.getNewValue())) {
                attr.setNewValue(existingAttr.getNewValue());
            }
            if (!(djValidator.rfcAttrsEqual(attr, existingAttr))) {
                newRfc.addAttribute(attr);
                needUpdate = true;
            }
        }

        if (!needUpdate) return null;
        return newRfc;
    }

    private CmsRfcRelation upsertRfcRelationLocal(CmsRfcRelation rfcRelation, String userId, String attrValue) {

        if (attrValue == null) attrValue = "df";

        //lets check if there is existing RFC for this type of rel if rfcId is not specified
        if (rfcRelation.getRfcId() == 0) {
            CmsRfcRelation existingRfc = getExisitngRelationRfcMerged(rfcRelation.getFromCiId(), rfcRelation.getRelationName(), rfcRelation.getToCiId(), attrValue);
            if (existingRfc != null) {
                rfcRelation.setRfcId(existingRfc.getRfcId());
                rfcRelation.setCiRelationId(existingRfc.getCiRelationId());
                rfcRelation.setReleaseId(existingRfc.getReleaseId());
            }
        }

        if (rfcRelation.getCiRelationId() == 0 && rfcRelation.getRfcId() == 0) {
            //this is brand new relation
            rfcRelation.setRfcAction("add");
            //proceed to create "add" rfc
            long newRfcId = rfcProcessor.createRfcRelationNoCheck(rfcRelation, userId);
            return rfcProcessor.getRfcRelationById(newRfcId);
        } else if (rfcRelation.getRfcId() > 0 && rfcRelation.getCiRelationId() == 0) {
            //this should never happen raise an error
            String errMsg = "the ci_id needs to be provided for the rfc_id = " + rfcRelation.getRfcId();
            logger.error(errMsg);
            throw new DJException(CmsError.DJ_CI_ID_IS_NEED_ERROR, errMsg);
        } else if (rfcRelation.getRfcId() == 0 && rfcRelation.getCiRelationId() > 0) {
            //this should be an new "update" rfc lets figure out delta
            rfcRelation.setRfcAction("update");
            CmsRfcRelation baseRel = getCIRelation(rfcRelation.getCiRelationId(), attrValue);
            CmsRfcRelation updRfcRel = generateNewRelationRfc(rfcRelation, baseRel, attrValue);
            if (updRfcRel != null) {
                rfcProcessor.createRfcRelationNoCheck(updRfcRel, userId);
                return getCIRelation(rfcRelation.getCiRelationId(), attrValue);
            }
            return baseRel;

        } else {
            //we need to update exisitng rfc
            CmsRfcRelation baseRel = getCIRelation(rfcRelation.getCiRelationId(), attrValue);
            CmsRfcRelation updRfcRel = generateNewRelationRfc(rfcRelation, baseRel, attrValue);
            if (updRfcRel != null) {
                updRfcRel.setUpdatedBy(userId);
                rfcProcessor.updateRfcRelation(updRfcRel);
                return getCIRelation(rfcRelation.getCiRelationId(), attrValue);
            }
            return baseRel;
        }
    }

    public CmsRfcRelation upsertRfcRelationNoCheck(CmsRfcRelation rfcRelation, String userId, String attrValue) {

        if (attrValue == null) attrValue = "df";

        if (rfcRelation.getCiRelationId() == 0 && rfcRelation.getRfcId() == 0) {
            //this is brand new relation
            rfcRelation.setRfcAction("add");
            //proceed to create "add" rfc
            long newRfcId = rfcProcessor.createRfcRelationNoCheck(rfcRelation, userId);
            return rfcProcessor.getRfcRelationById(newRfcId);
        } else if (rfcRelation.getRfcId() > 0 && rfcRelation.getCiRelationId() == 0) {
            //this should never happen raise an error
            String errMsg = "the ci_id needs to be provided for the rfc_id = " + rfcRelation.getRfcId();
            logger.error(errMsg);
            throw new DJException(CmsError.DJ_CI_ID_IS_NEED_ERROR, errMsg);
        } else if (rfcRelation.getRfcId() == 0 && rfcRelation.getCiRelationId() > 0) {
            //this should be an new "update" rfc lets figure out delta
            rfcRelation.setRfcAction("update");
            CmsRfcRelation baseRel = getCIRelation(rfcRelation.getCiRelationId(), attrValue);
            CmsRfcRelation updRfcRel = generateNewRelationRfc(rfcRelation, baseRel, attrValue);
            if (updRfcRel != null) {
                rfcProcessor.createRfcRelationNoCheck(updRfcRel, userId);
                return getCIRelation(rfcRelation.getCiRelationId(), attrValue);
            }
            return baseRel;

        } else {
            //we need to update exisitng rfc
            CmsRfcRelation baseRel = getCIRelation(rfcRelation.getCiRelationId(), attrValue);
            CmsRfcRelation updRfcRel = generateNewRelationRfc(rfcRelation, baseRel, attrValue);
            if (updRfcRel != null) {
                updRfcRel.setUpdatedBy(userId);
                rfcProcessor.updateRfcRelation(updRfcRel);
                return getCIRelation(rfcRelation.getCiRelationId(), attrValue);
            }
            return baseRel;
        }
    }


    /**
     * Gets the exisitng relation rfc merged.
     *
     * @param fromCiId the from ci id
     * @param relName  the rel name
     * @param toCiId   the to ci id
     * @param value    the value
     * @return the exisitng relation rfc merged
     */
    public CmsRfcRelation getExisitngRelationRfcMerged(long fromCiId, String relName, long toCiId, String value) {
        List<CmsRfcRelation> rfcList = rfcProcessor.getOpenRfcRelationBy2(fromCiId, toCiId, relName, null);
        List<CmsCIRelation> ciRelList = cmProcessor.getFromToCIRelations(fromCiId, relName, toCiId);

        if (rfcList.size() > 0) {
            if (ciRelList.size() > 0) {
                return rfcUtil.mergeRfcRelAndCiRel(rfcList.get(0), ciRelList.get(0), value);
            } else {
                return rfcList.get(0);
            }
        } else {
            if (ciRelList.size() > 0) {
                return rfcUtil.mergeRfcRelAndCiRel(null, ciRelList.get(0), value);
            } else {
                return null;
            }
        }
    }


    private CmsRfcRelation generateNewRelationRfc(CmsRfcRelation rel, CmsRfcRelation baseRel, String attrValue) {

        boolean needUpdate = false;
        CmsRfcRelation newRfcRel = new CmsRfcRelation();

        //CmsRfcRelation baseRel = getCIRelation(rel.getCiRelationId(), attrValue);
        if (baseRel == null) {
            //this should never happen raise an error
            String errMsg = "the is no ci or rfc for ci_id = " + rel.getCiRelationId();
            logger.error(errMsg);
            throw new DJException(CmsError.DJ_NO_CI_WITH_GIVEN_ID_ERROR, errMsg);
        }

        newRfcRel.setRfcId(rel.getRfcId());
        newRfcRel.setReleaseId(rel.getReleaseId());
        newRfcRel.setReleaseNsPath(rel.getReleaseNsPath());
        //the order is important here since if releaseNsPath is null setNsPath will set it correctly
        newRfcRel.setNsPath(rel.getNsPath());
        newRfcRel.setCiRelationId(rel.getCiRelationId());
        newRfcRel.setFromCiId(rel.getFromCiId());
        newRfcRel.setFromRfcId(rel.getFromRfcId());
        newRfcRel.setRelationName(rel.getRelationName());
        newRfcRel.setToCiId(rel.getToCiId());
        newRfcRel.setToRfcId(rel.getToRfcId());
        newRfcRel.setRelationGoid(rel.getRelationGoid());
        newRfcRel.setRfcAction(rel.getRfcAction());
        newRfcRel.setExecOrder(rel.getExecOrder());
        newRfcRel.setLastAppliedRfcId(rel.getLastAppliedRfcId());
        newRfcRel.setComments(rel.getComments());

        //needUpdate = newRfcRel.getComments() != baseRel.getComments();

        for (CmsRfcAttribute attr : rel.getAttributes().values()) {
            CmsRfcAttribute existingAttr = baseRel.getAttribute(attr.getAttributeName());
            if (!(djValidator.rfcAttrsEqual(attr, existingAttr))) {
                newRfcRel.addAttribute(attr);
                needUpdate = true;
            }
        }
        if (DUMMY_RELS.contains(rel.getRelationName())) needUpdate = true;

        if (!needUpdate) return null;
        return newRfcRel;
    }

    private class Relations {
        private CmsCIRelation ciRelation;
        private CmsRfcRelation rfcRelation;

        public void setCiRelation(CmsCIRelation ciRelation) {
            this.ciRelation = ciRelation;
        }

        public CmsCIRelation getCiRelation() {
            return ciRelation;
        }

        public void setRfcRelation(CmsRfcRelation rfcRelation) {
            this.rfcRelation = rfcRelation;
        }

        public CmsRfcRelation getRfcRelation() {
            return rfcRelation;
        }
    }

    private class Cis {
        private CmsCI ci;
        private CmsRfcCI rfc;

        public void setCi(CmsCI ci) {
            this.ci = ci;
        }

        public CmsCI getCi() {
            return ci;
        }

        public void setRfcCi(CmsRfcCI rfc) {
            this.rfc = rfc;
        }

        public CmsRfcCI getRfcCi() {
            return rfc;
        }
    }
}
