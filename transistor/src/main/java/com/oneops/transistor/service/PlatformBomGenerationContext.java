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

    PlatformBomGenerationContext(CmsCI platformCi, EnvBomGenerationContext envContext, CmsCmProcessor cmProcessor, CmsUtil cmsUtil) {
        long t = System.currentTimeMillis();

        platform = platformCi;

        String nsSuffix = "/" + platformCi.getCiName() + "/" + platformCi.getAttribute("major_version").getDjValue();
        manifestNsPath = envContext.getManifestNsPath() + nsSuffix;
        bomNsPath = envContext.getBomNsPath() + nsSuffix;

        components = cmProcessor.getToCIs(platformCi.getCiId(), null, "Requires", null);
        Map<Long, CmsCI> componentMap = components.stream().collect(Collectors.toMap(CmsCI::getCiId, Function.identity()));

        variables = cmsUtil.getLocalVars(platformCi);

        dependsOns = cmProcessor.getCIRelations(manifestNsPath, null, "DependsOn", null, null);
        dependsOnFromMap = new HashMap<>();
        dependsOnToMap = new HashMap<>();
        for (CmsCIRelation rel : dependsOns) {
            dependsOnFromMap.computeIfAbsent(rel.getFromCiId(), k -> new ArrayList<>());
            dependsOnFromMap.get(rel.getFromCiId()).add(rel);
            dependsOnToMap.computeIfAbsent(rel.getToCiId(), k -> new ArrayList<>());
            dependsOnToMap.get(rel.getToCiId()).add(rel);
        }

        List<Long> ids = cmProcessor.getCIRelationsNakedNoAttrs(manifestNsPath, MANIFEST_WATCHED_BY, null, null, null).stream()
                .map(CmsCIRelation::getToCiId).collect(Collectors.toList());
        monitors = cmProcessor.getCiByIdList(ids);

        ids = cmProcessor.getCIRelationsNakedNoAttrs(manifestNsPath, MANIFEST_ESCORTED_BY, null, null, null).stream()
                .map(CmsCIRelation::getToCiId).collect(Collectors.toList());
        attachments = cmProcessor.getCiByIdList(ids);

        ids = cmProcessor.getCIRelationsNakedNoAttrs(manifestNsPath, MANIFEST_LOGGED_BY, null, null, null).stream()
                .map(CmsCIRelation::getToCiId).collect(Collectors.toList());
        logs = cmProcessor.getCiByIdList(ids);

        securedByMap = cmProcessor.getCIRelationsNakedNoAttrs(manifestNsPath, null, SECURED_BY, null, null).stream()
                .peek(r -> {
                    r.setFromCi(componentMap.get(r.getFromCiId()));
                    r.setToCi(componentMap.get(r.getToCiId()));
                })
                .collect(Collectors.groupingBy(CmsCIRelation::getFromCiId));

        managedViaMap = cmProcessor.getCIRelationsNakedNoAttrs(manifestNsPath, null, MANAGED_VIA, null, null).stream()
                .peek(r -> {
                    r.setFromCi(componentMap.get(r.getFromCiId()));
                    r.setToCi(componentMap.get(r.getToCiId()));
                }).collect(Collectors.groupingBy(CmsCIRelation::getFromCiId));

        entryPoints = cmProcessor.getFromCIRelationsNakedNoAttrs(platformCi.getCiId(), null, ENTRYPOINT, null);
        entryPoints.forEach(r -> {
            r.setFromCi(platformCi);
            r.setToCi(componentMap.get(r.getToCiId()));
        });

        bomRelations = cmProcessor.getCIRelations(bomNsPath, null, null, null, null);

        logger.info(manifestNsPath + " >>> Loaded platform bom generation context in " + (System.currentTimeMillis() - t) + " ms.");
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
