package com.oneops.transistor.service;


import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.util.CmsUtil;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.oneops.cms.util.CmsConstants.*;

public class PlatformManifest {
    static Logger logger = Logger.getLogger(BomRfcBulkProcessor.class);

    CmsCI platform;

    List<CmsCI> components;
    List<CmsCI> monitors;
    List<CmsCI> attachments;
    List<CmsCI> logs;

    List<CmsCIRelation> dependsOns;
    List<CmsCIRelation> entryPoints;

    Map<String, String> vars;

    Map<Long, List<CmsCIRelation>> dependsOnFromMap;
    Map<Long, List<CmsCIRelation>> dependsOnToMap;
    Map<Long, List<CmsCIRelation>> securedByMap;
    Map<Long, List<CmsCIRelation>> managedViaMap;


    PlatformManifest(CmsCI platformCi, CmsCmProcessor cmProcessor, CmsUtil cmsUtil) {
        long t = System.currentTimeMillis();
        String nsPath = platformCi.getNsPath();
        platform = platformCi;
        components = cmProcessor.getToCIs(platformCi.getCiId(), null, "Requires", null);
        vars = cmsUtil.getLocalVars(platformCi);

        dependsOns = new ArrayList<>();
        dependsOnFromMap = new HashMap<>();
        dependsOnToMap = new HashMap<>();
        dependsOns.addAll(cmProcessor.getCIRelations(nsPath, null, "DependsOn", null, null));
        for (CmsCIRelation rel : dependsOns) {
            dependsOnFromMap.computeIfAbsent(rel.getFromCiId(), k -> new ArrayList<>());
            dependsOnFromMap.get(rel.getFromCiId()).add(rel);
            dependsOnToMap.computeIfAbsent(rel.getToCiId(), k -> new ArrayList<>());
            dependsOnToMap.get(rel.getToCiId()).add(rel);
        }

        List<Long> ids = cmProcessor.getCIRelationsNakedNoAttrs(nsPath, MANIFEST_WATCHED_BY, null, null, null).stream()
                .map(CmsCIRelation::getToCiId).collect(Collectors.toList());
        monitors = cmProcessor.getCiByIdList(ids);

        ids = cmProcessor.getCIRelationsNakedNoAttrs(nsPath, MANIFEST_ESCORTED_BY, null, null, null).stream()
                .map(CmsCIRelation::getToCiId).collect(Collectors.toList());
        attachments = cmProcessor.getCiByIdList(ids);

        ids = cmProcessor.getCIRelationsNakedNoAttrs(nsPath, MANIFEST_LOGGED_BY, null, null, null).stream()
                .map(CmsCIRelation::getToCiId).collect(Collectors.toList());
        logs = cmProcessor.getCiByIdList(ids);

        securedByMap = cmProcessor.getCIRelationsNakedNoAttrs(nsPath, null, SECURED_BY, null, null).stream()
                                  .collect(Collectors.groupingBy(CmsCIRelation::getFromCiId));

        managedViaMap = cmProcessor.getCIRelationsNakedNoAttrs(nsPath, null, MANAGED_VIA, null, null).stream()
                                   .collect(Collectors.groupingBy(CmsCIRelation::getFromCiId));

        entryPoints = cmProcessor.getFromCIRelationsNakedNoAttrs(platformCi.getCiId(), null, ENTRYPOINT, null);

        logger.info(nsPath + " >>> Loaded platform manifest in " + (System.currentTimeMillis() - t) + " ms.");
    }
}
