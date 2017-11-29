package com.oneops.transistor.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationBasic;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.util.CmsUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.oneops.cms.util.CmsConstants.MANIFEST_COMPOSED_OF;

class EnvBomGenerationContext {
    private static final Logger logger = Logger.getLogger(EnvBomGenerationContext.class);
    private final Set<Long> excludePlats;

    private CmsCmProcessor cmProcessor;
    private CmsUtil cmsUtil;
    private CmsRfcProcessor rfcProcessor;

    private String userId;

    private CmsCI environment;
    private String manifestNsPath;
    private String bomNsPath;
    private List<CmsCI> platforms;
    private Set<Long> disabledPlatformIds;
    private Map<String, String> globalVariables;

    private Map<Long, Map<String, String>> cloudVariableMap = new HashMap<>();
    private Map<Long, PlatformBomGenerationContext> platformContextMap = new HashMap<>();

    private Long releaseId = null;

    EnvBomGenerationContext(Long envId, String userId, CmsCmProcessor cmProcessor, CmsUtil cmsUtil, CmsRfcProcessor rfcProcessor) {
        this(envId, null, userId, cmProcessor, cmsUtil, rfcProcessor);
    }

    EnvBomGenerationContext(Long envId, Set<Long> excludePlats, String userId, CmsCmProcessor cmProcessor, CmsUtil cmsUtil, CmsRfcProcessor rfcProcessor) {
        this.cmProcessor = cmProcessor;
        this.cmsUtil = cmsUtil;
        this.rfcProcessor = rfcProcessor;

        this.userId = userId;

        environment = cmProcessor.getCiById(envId);
        manifestNsPath = environment.getNsPath() + "/" + environment.getCiName() + "/manifest";
        bomNsPath = environment.getNsPath() + "/" + environment.getCiName() + "/bom";
        this.excludePlats = excludePlats;
    }

    public void load() {
        long t = System.currentTimeMillis();
        List<CmsCIRelation> rels = cmProcessor.getFromCIRelations(environment.getCiId(), MANIFEST_COMPOSED_OF, null, "manifest.Platform");

        platforms = rels.stream()
                        .filter(r -> excludePlats == null || !excludePlats.contains(r.getToCiId()))
                        .map(CmsCIRelation::getToCi)
                        .collect(Collectors.toList());
        disabledPlatformIds = rels.stream()
                                  .filter(r -> r.getAttribute("enabled") != null && r.getAttribute("enabled").getDjValue().equalsIgnoreCase("false"))
                                  .map(CmsCIRelationBasic::getToCiId)
                                  .collect(Collectors.toSet());

        globalVariables = cmsUtil.getGlobalVars(environment);
        logger.info(bomNsPath + " >>> Loaded bom generation context in " + (System.currentTimeMillis() - t) + " ms.");
    }


    public String getUserId() {
        return userId;
    }

    Long getReleaseId() {
        if (releaseId == null) {
            releaseId  = rfcProcessor.getOpenReleaseIdByNs(bomNsPath, null, userId);
        }
        return releaseId;
    }

    CmsCI getEnvironment() {
        return environment;
    }

    String getManifestNsPath() {
        return manifestNsPath;
    }

    String getBomNsPath() {
        return bomNsPath;
    }

    List<CmsCI> getPlatforms() {
        return platforms;
    }

    Set<Long> getDisabledPlatformIds() {
        return disabledPlatformIds;
    }

    Map<String, String> getGlobalVariables() {
        return globalVariables;
    }

    Map<String, String> getCloudVariables(CmsCI cloud) {
        long cloudId = cloud.getCiId();
        Map<String, String> vars = cloudVariableMap.get(cloudId);
        if (vars == null) {
            vars = cmsUtil.getCloudVars(cloud);
            cloudVariableMap.put(cloudId, vars);
        }
        return vars;
    }

    PlatformBomGenerationContext getPlatformContext(CmsCI platform) {
        long platformId = platform.getCiId();
        PlatformBomGenerationContext pc = platformContextMap.get(platformId);
        if (pc == null) {
            pc = new PlatformBomGenerationContext(platform, this, cmProcessor, cmsUtil);
            platformContextMap.put(platformId, pc);
        }
        return pc;
    }
}
