package com.oneops.transistor.service;


import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.util.CmsUtil;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.oneops.cms.util.CmsConstants.*;

public class PlatformBomGenerationContext {
    private static Logger logger = Logger.getLogger(PlatformBomGenerationContext.class);

    private CmsCI platform;

    private String manifestNsPath;
    private String bomNsPath;

    private Map<String, List<CmsCIRelation>> manifestRelationMap;

    private List<CmsCI> components;
    private List<CmsCI> monitors;
    private List<CmsCI> attachments;
    private List<CmsCI> logs;

    private List<CmsCIRelation> dependsOns;
    private List<CmsCIRelation> entryPoints;

    private Map<String, String> variables;

    private Map<Long, List<CmsCIRelation>> dependsOnFromMap;
    private Map<Long, List<CmsCIRelation>> dependsOnToMap;
    private Map<Long, List<CmsCIRelation>> securedByMap;
    private Map<Long, List<CmsCIRelation>> managedViaMap;

    private List<CmsCIRelation> bomRelations;

    PlatformBomGenerationContext(PlatformBomGenerationContext pc, CmsCmProcessor cmProcessor) {
        long t = System.currentTimeMillis();
        platform = pc.platform;
        manifestNsPath = pc.manifestNsPath;
        bomNsPath = pc.bomNsPath;
        variables = pc.variables;
        manifestRelationMap = pc.manifestRelationMap;
        loadManifestCIs(cmProcessor);   // have to reload manifest CIs because they could be "dirty" after variable interpolation
        bomRelations = pc.bomRelations;

        logger.info(manifestNsPath + " >>> Reloaded platform bom generation context in " + (System.currentTimeMillis() - t) + " ms.");
    }

    PlatformBomGenerationContext(CmsCI platformCi, EnvBomGenerationContext envContext, CmsCmProcessor cmProcessor, CmsUtil cmsUtil) {
        long t = System.currentTimeMillis();

        platform = platformCi;

        String nsSuffix = "/" + platformCi.getCiName() + "/" + platformCi.getAttribute("major_version").getDjValue();
        manifestNsPath = envContext.getManifestNsPath() + nsSuffix;
        bomNsPath = envContext.getBomNsPath() + nsSuffix;

        variables = cmsUtil.getLocalVars(platformCi);

        manifestRelationMap = cmProcessor.getCIRelationsNaked(manifestNsPath, null, null, null, null).stream()
                .collect(Collectors.groupingBy(CmsCIRelation::getRelationName, Collectors.toList()));

        loadManifestCIs(cmProcessor);

        bomRelations = cmProcessor.getCIRelations(bomNsPath, null, null, null, null);

        logger.info(manifestNsPath + " >>> Loaded platform bom generation context in " + (System.currentTimeMillis() - t) + " ms.");
    }

    private void loadManifestCIs(CmsCmProcessor cmProcessor) {
        List<CmsCIRelation> cmsCIRelations = manifestRelationMap.get(MANIFEST_REQUIRES);
        List<Long> ids = cmsCIRelations == null ? new ArrayList<>() : cmsCIRelations.stream().map(CmsCIRelation::getToCiId).collect(Collectors.toList());
        components = cmProcessor.getCiByIdList(ids);
        Map<Long, CmsCI> componentMap = components.stream().collect(Collectors.toMap(CmsCI::getCiId, Function.identity()));

        dependsOns = manifestRelationMap.computeIfAbsent(MANIFEST_DEPENDS_ON, k -> new ArrayList<>());
        dependsOns.forEach(r -> {
            r.setFromCi(componentMap.get(r.getFromCiId()));
            r.setToCi(componentMap.get(r.getToCiId()));
        });
        dependsOnFromMap = new HashMap<>();
        dependsOnToMap = new HashMap<>();
        for (CmsCIRelation rel : dependsOns) {
            dependsOnFromMap.computeIfAbsent(rel.getFromCiId(), k -> new ArrayList<>());
            dependsOnFromMap.get(rel.getFromCiId()).add(rel);
            dependsOnToMap.computeIfAbsent(rel.getToCiId(), k -> new ArrayList<>());
            dependsOnToMap.get(rel.getToCiId()).add(rel);
        }

        ids = manifestRelationMap.computeIfAbsent(MANIFEST_WATCHED_BY, k -> new ArrayList<>()).stream()
                .map(CmsCIRelation::getToCiId).collect(Collectors.toList());
        monitors = cmProcessor.getCiByIdList(ids);

        ids = manifestRelationMap.computeIfAbsent(MANIFEST_ESCORTED_BY, k -> new ArrayList<>()).stream()
                .map(CmsCIRelation::getToCiId).collect(Collectors.toList());
        attachments = cmProcessor.getCiByIdList(ids);

        ids = manifestRelationMap.computeIfAbsent(MANIFEST_LOGGED_BY, k -> new ArrayList<>()).stream()
                .map(CmsCIRelation::getToCiId).collect(Collectors.toList());
        logs = cmProcessor.getCiByIdList(ids);

        securedByMap = manifestRelationMap.computeIfAbsent(MANIFEST_SECURED_BY, k -> new ArrayList<>()).stream()
                .peek(r -> {
                    r.setFromCi(componentMap.get(r.getFromCiId()));
                    r.setToCi(componentMap.get(r.getToCiId()));
                })
                .collect(Collectors.groupingBy(CmsCIRelation::getFromCiId));

        managedViaMap = manifestRelationMap.computeIfAbsent(MANIFEST_MANAGED_VIA, k -> new ArrayList<>()).stream()
                .peek(r -> {
                    r.setFromCi(componentMap.get(r.getFromCiId()));
                    r.setToCi(componentMap.get(r.getToCiId()));
                }).collect(Collectors.groupingBy(CmsCIRelation::getFromCiId));

        entryPoints = manifestRelationMap.computeIfAbsent(MANIFEST_ENTRYPOINT, k -> new ArrayList<>());
        entryPoints.forEach(r -> {
            r.setFromCi(platform);
            r.setToCi(componentMap.get(r.getToCiId()));
        });
    }

    String getManifestNsPath() {
        return manifestNsPath;
    }

    String getBomNsPath() {
        return bomNsPath;
    }

    CmsCI getPlatform() {
        return platform;
    }

    List<CmsCI> getComponents() {
        return components;
    }

    List<CmsCI> getMonitors() {
        return monitors;
    }

    List<CmsCI> getAttachments() {
        return attachments;
    }

    List<CmsCI> getLogs() {
        return logs;
    }

    Map<String, String> getVariables() {
        return variables;
    }

    List<CmsCIRelation> getDependsOns() {
        return dependsOns;
    }

    List<CmsCIRelation> getEntryPoints() {
        return entryPoints;
    }

    Map<Long, List<CmsCIRelation>> getDependsOnFromMap() {
        return dependsOnFromMap;
    }

    Map<Long, List<CmsCIRelation>> getDependsOnToMap() {
        return dependsOnToMap;
    }

    Map<Long, List<CmsCIRelation>> getSecuredByMap() {
        return securedByMap;
    }

    Map<Long, List<CmsCIRelation>> getManagedViaMap() {
        return managedViaMap;
    }

    List<CmsCI> getBomCIs(long cloudId) {
        return getBomRelations().stream()
                .filter(r -> r.getRelationName().equals(BASE_DEPLOYED_TO) && r.getToCiId() == cloudId)
                .map(CmsCIRelation::getFromCi)
                .collect(Collectors.toList());
    }

    List<CmsCIRelation> getBomRelations() {
        return bomRelations;
    }
}
