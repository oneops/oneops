package com.oneops.transistor.service;


import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.util.CmsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.oneops.cms.util.CmsConstants.MANIFEST_ESCORTED_BY;
import static com.oneops.cms.util.CmsConstants.MANIFEST_LOGGED_BY;
import static com.oneops.cms.util.CmsConstants.MANIFEST_WATCHED_BY;

public class PlatformManifest {
    CmsCI platform;

    List<CmsCI> components;
    List<CmsCIRelation> dependsOns;
    List<CmsCI> monitors;
    List<CmsCI> attachments;
    List<CmsCI> logs;

    Map<String, String> vars;

    Map<Long, List<CmsCIRelation>> dependsOnsFromMap;
    Map<Long, List<CmsCIRelation>> dependsOnsToMap;

    PlatformManifest(CmsCI platformCi, CmsCmProcessor cmProcessor, CmsUtil cmsUtil) {
        String nsPath = platformCi.getNsPath();
        platform = platformCi;
        components = cmProcessor.getToCIs(platformCi.getCiId(), null, "Requires", null);
        vars = cmsUtil.getLocalVars(platformCi);

        dependsOns = new ArrayList<>();
        dependsOnsFromMap = new HashMap<>();
        dependsOnsToMap = new HashMap<>();
        dependsOns.addAll(cmProcessor.getCIRelations(nsPath, null, "DependsOn", null, null));
        for (CmsCIRelation doRel : dependsOns) {
            dependsOnsFromMap.computeIfAbsent(doRel.getFromCiId(), k -> new ArrayList<>());
            dependsOnsFromMap.get(doRel.getFromCiId()).add(doRel);
            dependsOnsToMap.computeIfAbsent(doRel.getToCiId(), k -> new ArrayList<>());
            dependsOnsToMap.get(doRel.getToCiId()).add(doRel);
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
    }
}
