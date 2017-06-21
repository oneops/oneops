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

import com.oneops.cms.cm.domain.*;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.dj.service.CmsRfcUtil;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.exceptions.DesignExportException;
import com.oneops.transistor.exceptions.TransistorException;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.oneops.cms.util.CmsConstants.*;

/**
 * Processor class to update platform to a given pack version.
 *
 * @author lkhusid
 */
public class PackUpdateProcessor {

    private static final String OPEN_RELEASE_ERROR_MSG = "Platform already has changes in current release. Please commit/discard the changes before proceeding.";
    static Logger logger = Logger.getLogger(PackUpdateProcessor.class);

    private CmsCmProcessor cmProcessor;
    private CmsRfcProcessor rfcProcessor;
    private DesignRfcProcessor designRfcProcessor;
    private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
    private TransUtil trUtil;

    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }

    public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
        this.rfcProcessor = rfcProcessor;
    }

    public void setDesignRfcProcessor(DesignRfcProcessor designRfcProcessor) {
        this.designRfcProcessor = designRfcProcessor;
    }

    public void setTrUtil(TransUtil trUtil) {
        this.trUtil = trUtil;
    }

    public void setCmRfcMrgProcessor(CmsCmRfcMrgProcessor cmRfcMrgProcessor) {
        this.cmRfcMrgProcessor = cmRfcMrgProcessor;
    }

    public long updateFromPack(long platformId, String packVersion, String userId, String scope) {
        CmsCI designPlatform = cmProcessor.getCiById(platformId);
        if (designPlatform == null) {
            String errMsg = "Can not find platform for id = " + platformId + ";";
            logger.error(errMsg);
            throw new TransistorException(CmsError.TRANSISTOR_CANNOT_CORRESPONDING_OBJECT, errMsg);
        }

        trUtil.verifyScope(designPlatform, scope);

        logger.info("Processing pack update to version " + packVersion + " for platform " + designPlatform.getCiName() + " in " + designPlatform.getNsPath());
        long t = System.currentTimeMillis();

        String designPlatformNsPath = designPlatform.getNsPath() + "/_design/" + designPlatform.getCiName();
        if (rfcProcessor.getRfcCountByNs(designPlatformNsPath) > 0) {
            throw new TransistorException(DesignExportException.DJ_OPEN_RELEASE_FOR_NAMESPACE_ERROR, OPEN_RELEASE_ERROR_MSG);
        }

        if (packVersion == null) {
            packVersion = designPlatform.getAttribute("version").getDfValue();
        }

        String packNsPath = "/public/" + designPlatform.getAttribute("source").getDfValue()
                + "/packs/" + designPlatform.getAttribute("pack").getDfValue();
        List<CmsCI> packVersions = cmProcessor.getCiBy3(packNsPath, MGMT_PACK_VERSION_CLASS, packVersion);
        if (packVersions.size() == 0) {
            String err = "Cannot find corresponding pack version " + packVersion + " in " + packNsPath;
            logger.error(err);
            throw new TransistorException(CmsError.TRANSISTOR_CANNOT_CORRESPONDING_OBJECT, err);
        }

        String versionNsPath = packNsPath + "/" + packVersion;
        List<CmsCI> templatePlatforms = cmProcessor.getCiBy3(versionNsPath, MGMT_CATALOG_PLATFORM_CLASS, null);
        if (templatePlatforms.size() == 0) {
            String err = "Cannot find corresponding mgmt platform CI in " + versionNsPath;
            logger.error(err);
            throw new TransistorException(CmsError.TRANSISTOR_CANNOT_CORRESPONDING_OBJECT, err);
        }

        Context context = new Context();
        context.user = userId;
        context.designPlatformNsPath = designPlatformNsPath;
        context.designPlatform = designPlatform;
        context.packVersionCi = packVersions.get(0);
        context.templatePlatform = templatePlatforms.get(0);

        processComponents(context);
        processDependsOn(context);
        processMonitors(context);
        processLocalVars(context);

        String newPackDigest = context.packVersionCi.getAttribute("commit").getDfValue();
        if (!designPlatform.getAttribute("version").getDfValue().equals(packVersion) ||
                !designPlatform.getAttribute("pack_digest").getDfValue().equals(newPackDigest)) {
            Map<String, String> changes = new HashMap<>();
            changes.put("version", packVersion);
            changes.put("pack_digest", newPackDigest);
            submitRfcCi(new CmsRfcCI(designPlatform, context.user, changes), context);
        }

        logger.info("Finished pack update to version " + packVersion + " for platform " + designPlatform.getCiName() + " in " + designPlatform.getNsPath() + " in " + (System.currentTimeMillis() - t) + "ms. " + (context.releaseId == null ? "No changes." : "Created release id: " + context.releaseId));

        return context.releaseId == null ? 0 : context.releaseId;
    }

    private void processComponents(Context context) {
        Map<String, List<CmsCIRelation>> existingRelMap = cmProcessor.getFromCIRelations(context.designPlatform.getCiId(), null, "Requires", null)
                .stream()
                .collect(Collectors.groupingBy(r -> r.getAttribute("template").getDfValue()));

        // Go though all pack defined components and reconcile changes with existing platform components.
        cmProcessor.getFromCIRelations(context.templatePlatform.getCiId(), null, "Requires", null)
                .stream()
                .filter(r -> !"pending_deletion".equals(r.getRelationState()) && !"pending_deletion".equals(r.getToCi().getCiState()))
                .forEach(packRel -> {
                            CmsCI packCi = packRel.getToCi();
                            List<CmsCIRelation> existingRels = existingRelMap.remove(packCi.getCiName());
                            if (existingRels != null && existingRels.size() > 0) {
                                for (CmsCIRelation existingRel : existingRels) {
                                    CmsRfcCI componentRfc = updateComponentIfNeeded(existingRel.getToCi(), packCi, context);
                                    context.addComponent(packCi.getCiId(), componentRfc);
                                    updateRelationIfNeeded(existingRel, packRel, context);
                                }
                            } else {
                                String cardinality = packRel.getAttribute("constraint").getDfValue();
                                if (cardinality != null && cardinality.startsWith("1..")) {
                                    CmsRfcCI componentRfc = createRfcCi(packCi, "catalog", packCi.getCiName(), context);
                                    context.addComponent(packCi.getCiId(), componentRfc);
                                    createRfcRelation(packRel, "base", context, context.designPlatform, componentRfc);
                                }
                            }
                        }
                );

        // Any existing components left are obsolete, delete them.
        existingRelMap.values()
                .forEach(obsoleteComponentsList -> obsoleteComponentsList.forEach(r -> cmRfcMrgProcessor.requestCiDelete(r.getToCiId(), context.user)));
    }

    private void processDependsOn(Context context) {
        Map<String, CmsCIRelation> existingRelMap = cmProcessor.getCIRelations(context.designPlatformNsPath, CATALOG_DEPENDS_ON, null, null, null)
                .stream()
                .collect(Collectors.toMap(r -> r.getFromCiId() + "--" + r.getToCiId(), Function.identity()));
        cmProcessor.getCIRelations(context.templatePlatform.getNsPath(), MGMT_CATALOG_DEPENDS_ON, null, null, null)
                .stream()
                .filter(r -> !"pending_deletion".equals(r.getRelationState()))
                .forEach(packRel -> {
                    List<CmsRfcCI> existingFromCis = context.componentMap.get(packRel.getFromCiId());
                    List<CmsRfcCI> existingToCis = context.componentMap.get(packRel.getToCiId());
                    if (existingFromCis != null && existingToCis != null) {
                        for (CmsRfcCI fromCi : existingFromCis) {
                            for (CmsRfcCI toCi : existingToCis) {
                                CmsCIRelation existingRel = existingRelMap.remove(fromCi.getCiId() + "--" + toCi.getCiId());
                                if (existingRel == null) {
                                    createRfcRelation(packRel, "catalog", context, fromCi, toCi);
                                } else {
                                    updateRelationIfNeeded(existingRel, packRel, context);
                                }
                            }
                        }
                    }
                });

        // Remaining dependsOn relations are obsolete unless they are added by user directly.
        existingRelMap.values().stream()
                .filter(r -> !"user".equalsIgnoreCase(r.getAttribute("source").getDfValue()))
                .forEach(r -> {
                    CmsRfcRelation rfc = new CmsRfcRelation(r, context.user);
                    rfc.setRfcAction("delete");
                    submitRfcRelation(rfc, context);
                });
    }

    private void processMonitors(Context context) {
        Map<Long, List<CmsCIRelation>> existingMonitorMap = cmProcessor.getCIRelations(context.designPlatformNsPath, CATALOG_WATCHED_BY, null, null, CATALOG_MONITOR_CLASS)
                .stream()
                //ignore custom monitors
                .filter(relation -> {
                    CmsCIAttribute customAttr = relation.getToCi().getAttribute(MONITOR_CUSTOM_ATTR);
                    return !((customAttr != null && "true".equalsIgnoreCase(customAttr.getDfValue())));
                })
                .collect(Collectors.groupingBy(CmsCIRelation::getFromCiId));

        String platformName = context.designPlatform.getCiName();
        List<CmsCIRelation> obsoleteMonitorRels = new ArrayList<>();

        cmProcessor.getCIRelations(context.templatePlatform.getNsPath(), MGMT_CATALOG_WATCHEDBY, null, null, MGMT_CATALOG_MONITOR_CLASS)
                .stream()
                .filter(r -> !"pending_deletion".equals(r.getRelationState()) && !"pending_deletion".equals(r.getToCi().getCiState()))
                .collect(Collectors.groupingBy(CmsCIRelation::getFromCiId))
                .entrySet()
                .forEach(entry -> {
                    Long packComponentCiId = entry.getKey();
                    List<CmsCIRelation> packComponentMonitorRels = entry.getValue();

                    List<CmsRfcCI> components = context.componentMap.get(packComponentCiId);
                    if (components != null) {
                        for (CmsRfcCI component : components) {
                            Map<String, CmsCIRelation> existingComponentMonitorRelMap = getExistingComponentMonitorMap(existingMonitorMap.get(component.getCiId()), platformName);
                            for (CmsCIRelation packMonitorRel : packComponentMonitorRels) {
                                CmsCI packMonitorCi = packMonitorRel.getToCi();
                                CmsCIRelation existingRel = existingComponentMonitorRelMap.remove(packMonitorCi.getCiName());
                                if (existingRel == null) {
                                    CmsRfcCI newMonitorCi = createRfcCi(packMonitorCi, "catalog", getDesignMonitorName(platformName, component.getCiName(), packMonitorCi.getCiName()), context);
                                    createRfcRelation(packMonitorRel, "catalog", context, component, newMonitorCi);
                                } else {
                                    updateCiIfNeeded(existingRel.getToCi(), packMonitorCi, context);
                                    updateRelationIfNeeded(existingRel, packMonitorRel, context);
                                }
                            }
                            obsoleteMonitorRels.addAll(existingComponentMonitorRelMap.values());
                        }
                    }
                });

        obsoleteMonitorRels.forEach(r -> cmRfcMrgProcessor.requestCiDelete(r.getToCiId(), context.user, 0));
    }

    private void processLocalVars(Context context) {
        Map<String, CmsCI> existingVarMap = cmProcessor.getToCIRelations(context.designPlatform.getCiId(), CATALOG_VALUE_FOR, null, CATALOG_LOCALVAR_CLASS)
                .stream()
                .map(CmsCIRelation::getFromCi)
                .collect(Collectors.toMap(CmsCI::getCiName, Function.identity()));

        cmProcessor.getToCIRelations(context.templatePlatform.getCiId(), MGMT_CATALOG_VALUE_FOR, null, MGMT_CATALOG_LOCALVAR_CLASS)
                .stream()
                .filter(r -> !"pending_deletion".equals(r.getRelationState()) && !"pending_deletion".equals(r.getFromCi().getCiState()))
                .forEach(packRel -> {
                            CmsCI packCi = packRel.getFromCi();
                            CmsCI existingCi = existingVarMap.get(packCi.getCiName());
                            if (existingCi == null) {
                                CmsRfcCI newCi = createRfcCi(packCi, "catalog", packCi.getCiName(), context);
                                createRfcRelation(packRel, "catalog", context, newCi, context.designPlatform);
                            } else {
                                updateCiIfNeeded(existingCi, packCi, context);
                            }
                        }

                );
    }

    private Map<String, CmsCIRelation> getExistingComponentMonitorMap(List<CmsCIRelation> ciRelations, String platformName) {
        Map<String, CmsCIRelation> relationsMap;
        if (ciRelations == null) {
            relationsMap = Collections.emptyMap();
        } else {
            relationsMap = ciRelations.stream().
                    collect(Collectors.toMap(r -> getPackMonitorName(platformName, r.getFromCi().getCiName(), r.getToCi().getCiName()), Function.identity()));
        }
        return relationsMap;
    }

    private String getDesignMonitorName(String platformName, String componentName, String monitorName) {
        return platformName + "-" + componentName + "-" + monitorName;
    }

    private String getPackMonitorName(String platformName, String componentName, String designMonitorName) {
        String prefix = platformName + "-" + componentName + "-";
        int start = designMonitorName.indexOf(prefix);
        return start == -1 ? designMonitorName : designMonitorName.substring(start + prefix.length());
    }

    private CmsRfcCI createRfcCi(CmsCI template, String classNamePackage, String ciName, Context context) {
        CmsRfcCI rfc = designRfcProcessor.popRfcCiFromTemplate(template, classNamePackage, context.designPlatformNsPath, context.designPlatform.getNsPath());
        rfc.setRfcAction("add");
        rfc.setCiName(ciName);
        submitRfcCi(rfc, context);
        return rfc;
    }

    private CmsRfcCI updateComponentIfNeeded(CmsCI existing, CmsCI template, Context context) {
        Map<String, String> changes = extractChangedAttributes(existing.getAttributes(), template.getAttributes());
        CmsRfcCI rfc = new CmsRfcCI(existing, context.user, changes);
        if (changes.size() > 0) {
            submitRfcCi(rfc, context);
        }
        return rfc;
    }

    private void updateCiIfNeeded(CmsCI existing, CmsCI template, Context context) {
        Map<String, String> changes = extractChangedAttributes(existing.getAttributes(), template.getAttributes());
        if (changes.size() > 0) {
            submitRfcCi(new CmsRfcCI(existing, context.user, changes), context);
        }
    }

    private void submitRfcCi(CmsRfcCI rfc, Context context) {
        rfc.setCreatedBy(context.user);
        rfc.setUpdatedBy(context.user);
        rfc.setReleaseId(context.ensureReleaseId());
        rfcProcessor.createRfcCINoCheck(rfc, context.user);
    }

    private CmsRfcRelation createRfcRelation(CmsCIRelation template, String relationNamePackage, Context context, CmsCI fromCi, CmsRfcCI toCi) {
        long toCiRfcId = toCi.getRfcId();
        return createRfcRelation(template, relationNamePackage, context, fromCi.getCiId(), null, toCi.getCiId(), toCiRfcId > 0 ? toCiRfcId : null);
    }

    private CmsRfcRelation createRfcRelation(CmsCIRelation template, String relationNamePackage, Context context, CmsRfcCI fromCi, CmsCI toCi) {
        long fromCiRfcId = fromCi.getRfcId();
        return createRfcRelation(template, relationNamePackage, context, fromCi.getCiId(), fromCiRfcId > 0 ? fromCiRfcId : null, toCi.getCiId(), null);
    }

    private CmsRfcRelation createRfcRelation(CmsCIRelation template, String relationNamePackage, Context context, CmsRfcCI fromCi, CmsRfcCI toCi) {
        long fromCiRfcId = fromCi.getRfcId();
        long toCiRfcId = toCi.getRfcId();
        return createRfcRelation(template, relationNamePackage, context, fromCi.getCiId(), fromCiRfcId > 0 ? fromCiRfcId : null, toCi.getCiId(), toCiRfcId > 0 ? toCiRfcId : null);
    }

    private CmsRfcRelation createRfcRelation(CmsCIRelation template, String relationNamePackage, Context context, Long fromCiId, Long fromRfcCiId, Long toCiId, Long toRfcCiId) {
        CmsRfcRelation rfc = designRfcProcessor.popRfcRelFromTemplate(template, relationNamePackage, context.designPlatformNsPath, context.designPlatform.getNsPath());
        rfc.setRfcAction("add");
        rfc.setFromCiId(fromCiId);
        rfc.setFromRfcId(fromRfcCiId);
        rfc.setToCiId(toCiId);
        rfc.setToRfcId(toRfcCiId);
        submitRfcRelation(rfc, context);
        return rfc;
    }

    private void updateRelationIfNeeded(CmsCIRelation existing, CmsCIRelation template, Context context) {
        Map<String, String> changes = extractChangedAttributes(existing.getAttributes(), template.getAttributes());
        if (changes.size() > 0) {
            submitRfcRelation(new CmsRfcRelation(existing, context.user, changes), context);
        }
    }

    private void submitRfcRelation(CmsRfcRelation rfc, Context context) {
        rfc.setCreatedBy(context.user);
        rfc.setUpdatedBy(context.user);
        rfc.setReleaseId(context.ensureReleaseId());
        rfcProcessor.createRfcRelationNoCheck(rfc, context.user);
    }

    private Map<String, String> extractChangedAttributes(Map<String, ? extends CmsBasicAttribute> existing, Map<String, ? extends CmsBasicAttribute> template) {
        Map<String, String> result = new HashMap<>();
        for (String attrName : template.keySet()) {
            String templateValue = template.get(attrName).getDfValue();
            if (shouldUpdateAttribute(existing.get(attrName), templateValue)) {
                result.put(attrName, templateValue);
            }
        }
        return result;
    }

    private boolean shouldUpdateAttribute(CmsBasicAttribute attr, String newValue) {
        if (attr == null || "design".equalsIgnoreCase(attr.getOwner())) return false;
        String value = attr.getDfValue();
        return newValue == null ? (value != null && value.length() > 0) : !newValue.equals(value);
    }

    private class Context {
        String user;
        CmsCI packVersionCi;
        CmsCI templatePlatform;
        String designPlatformNsPath;
        CmsCI designPlatform;
        Long releaseId;
        Map<Long, List<CmsRfcCI>> componentMap = new HashMap<>();

        private Long ensureReleaseId() {
            if (releaseId == null) {
                releaseId = rfcProcessor.getOpenReleaseIdByNs(designPlatform.getNsPath(), null, user);
            }

            return releaseId;
        }

        private void addComponent(Long packCiId, CmsRfcCI component) {
            List<CmsRfcCI> components = componentMap.get(packCiId);
            if (components == null) {
                components = new ArrayList<>();
                componentMap.put(packCiId, components);
            }
            components.add(component);
        }
    }
}
