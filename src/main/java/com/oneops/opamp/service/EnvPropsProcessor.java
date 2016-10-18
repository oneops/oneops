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
package com.oneops.opamp.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.domain.CmsVar;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * provides lookups ,and info about enable
 * of autorepair autoscale
 *
 */
public class EnvPropsProcessor {

	private static Logger logger = Logger.getLogger(EnvPropsProcessor.class);
	
	private CmsCmProcessor cmProcessor;
	private CmsRfcProcessor rfcProcessor;
	private static final long COOLOFF_PREIOD_4_RELEASE = 180000;
	private static final String OPAMP_SUSPENDED = "IS_OPAMP_SUSPENDED";
	private static final String HEARTBEAT_ALARMS_SUSPENDED = "HEARTBEAT_ALARMS_SUSPENDED";
	private static final String EXPONENTIAL_REPAIR_DELAY_VAR = "EXPONENTIAL_REPAIR_DELAY";
	
	/**
	 * Sets the cm processor.
	 *
	 * @param cmProcessor the new cm processor
	 */
	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}
    

	public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
		this.rfcProcessor = rfcProcessor;
	}

	/**
	 * Gets the env4 bom.
	 *
	 * @param ciId the ci id
	 * @return the env4 bom
	 */
	public CmsCI getEnv4Bom(long ciId) {
		
		List<CmsCIRelation> manifestCiRels = cmProcessor.getToCIRelationsNakedNoAttrs(ciId, "base.RealizedAs",null, null);
		
		if (manifestCiRels.size()>0) {
			long manifestCiId = manifestCiRels.get(0).getFromCiId();
			List<CmsCIRelation> manifestPlatRels = cmProcessor.getToCIRelationsNakedNoAttrs(manifestCiId, "manifest.Requires",null, "manifest.Platform");
			
			if (manifestPlatRels.size()>0) {
				long platId = manifestPlatRels.get(0).getFromCiId();
				List<CmsCIRelation> envRels = cmProcessor.getToCIRelations(platId, "manifest.ComposedOf",null, "manifest.Environment");
				
				if (envRels.size()>0) {
					return envRels.get(0).getFromCi(); 
				}
			}
			
		}
	
		return null;
	}

	/**
	 * Gets the platform for bom.
	 *
	 * @param ciId the ci id
	 * @return the platform ci
	 */
	public CmsCI getPlatform4Bom(long ciId) {
		
		List<CmsCIRelation> manifestCiRels = cmProcessor.getToCIRelationsNakedNoAttrs(ciId, "base.RealizedAs",null, null);
		
		if (manifestCiRels.size()>0) {
			long manifestCiId = manifestCiRels.get(0).getFromCiId();
			List<CmsCIRelation> manifestPlatRels = cmProcessor.getToCIRelations(manifestCiId, "manifest.Requires",null, "manifest.Platform");
			
			if (manifestPlatRels != null && manifestPlatRels.size()>0) {
				return manifestPlatRels.get(0).getFromCi();
			}
		}
	
		return null;
	}

	public boolean isAutorepairEnabled(long ciId) {
		return isAttributeEnabled(ciId, "autorepair");
	}

	private boolean isAttributeEnabled(long ciId, String attributeName) {
        CmsCI platform = getPlatform4Bom(ciId);
        if (platform == null) {
            logger.error("can not get platform for ciid "+ ciId);
            return false;
        }
        return isAttributeEnabled(platform, attributeName);
    }

    private boolean isAttributeEnabled(CmsCI platform, String attributeName) {
        CmsCIAttribute ciAttrib = platform.getAttribute(attributeName);
        return ciAttrib != null && "true".equalsIgnoreCase(ciAttrib.getDfValue());
    }

	public boolean isAutoscaleEnabled(long ciId) {
		return isAttributeEnabled(ciId, "autoscale");
	}

	/**
	 * Check if the ci cloud is in active state.
	 *
	 * @param ciId the ci id
	 * @return true is the cloud is in active state
	 */
	public boolean isCloudActive4Bom(long ciId) {
		List<CmsCIRelation> deployedToRels = cmProcessor.getFromCIRelationsNakedNoAttrs(ciId, "base.DeployedTo",null, "account.Cloud");
		return isCloudActive4Bom(ciId, deployedToRels);
	}

    /**
	 * Check if the ci cloud is in active state.
	 *
	 * @param ciId the ci id
	 * @param deployedToRels list of deployedTo relations
	 * @return true is the cloud is in active state
	 */
	public boolean isCloudActive4Bom(long ciId , List<CmsCIRelation> deployedToRels) {
		
		if (deployedToRels.size()>0) {
			CmsCIRelation ciRelation = deployedToRels.get(0);
			long cloudId = ciRelation.getToCiId();
			List<CmsCIRelation> manifestCiRels = cmProcessor.getToCIRelationsNakedNoAttrs(ciId, "base.RealizedAs",null, null);
			if (manifestCiRels.size() > 0) {
				long manifestCiId = manifestCiRels.get(0).getFromCiId();
				List<CmsCIRelation> manifestPlatRels = cmProcessor.getToCIRelationsNakedNoAttrs(manifestCiId, "manifest.Requires",null, "manifest.Platform");
				if (manifestPlatRels.size()>0) {
					long platId = manifestPlatRels.get(0).getFromCiId();

					List<CmsCIRelation> platformCloudRels = cmProcessor.getFromToCIRelationsNaked(platId, "base.Consumes", cloudId);
					if (platformCloudRels.size() >0) {
                        if (isAdminStatusActive(platformCloudRels.get(0))) return true;
                    }
				}
		    }
		}
		return false;
	}

    public boolean isAdminStatusActive(CmsCIRelation platformCloudRel) {
        return platformCloudRel.getAttribute("adminstatus") == null || CmsConstants.CLOUD_STATE_ACTIVE.equals(platformCloudRel.getAttribute("adminstatus").getDjValue());
    }

    public boolean isOpenRelease4Env(CmsCI env) {
		String envNsPath = env.getNsPath() + "/" + env.getCiName();		
		List<CmsRelease> manReleases = rfcProcessor.getLatestRelease(envNsPath + "/manifest", null);
		if (manReleases.size() >0 ) {
			if ("open".equals(manReleases.get(0).getReleaseState())) {
				return true;	
			}
		}
		String bomNs = envNsPath + "/bom";
		List<CmsRelease> bomReleases = rfcProcessor.getLatestRelease(bomNs, null);

		if (bomReleases.size() >0 ) {
			if (! "closed".equals(bomReleases.get(0).getReleaseState())) {
				return true;
			} else if ((System.currentTimeMillis() - bomReleases.get(0).getUpdated().getTime()) < COOLOFF_PREIOD_4_RELEASE) {
				return true;
			} else {
				List<CmsRelease> latestClosedManifestReleases = rfcProcessor.getLatestRelease(envNsPath + "/manifest", "closed");
				if (latestClosedManifestReleases.size() == 0) {
					logger.info("Env " + envNsPath + " has no committed release.");
					return true;
				}
				if (bomReleases.get(0).getParentReleaseId() != latestClosedManifestReleases.get(0).getReleaseId()) {
					//latest bom release is closed (deployed) and it's parent manifest release id is the same as latest manifest release id
					//This means there are changes which are committed but not deployed yet by the user.
					logger.info("Env namespace " + envNsPath + " has changes which are committed but not deployed.");
					return true;
				}
			}
		}
		
		return false;
	}

	public CmsCI getEnv4Platform(CmsCI platform) {
		if (platform == null) {
			logger.error("platform is null, can not get environment for it");
			return null;
		}

		List<CmsCIRelation> envRels = cmProcessor.getToCIRelations(platform.getCiId(), "manifest.ComposedOf",null, "manifest.Environment");
		
		if (envRels.size()>0) {
			return envRels.get(0).getFromCi(); 
		}
		return null;
	}

	public boolean isFirstAfterBomReleaseClosed(CmsCI env, long eventTimestamp, long coolOff) {
		String bomNs = env.getNsPath() + "/" + env.getCiName() + "/bom";

		List<CmsRelease> bomReleases = rfcProcessor.getLatestRelease(bomNs, null);

		if (bomReleases.size() >0 ) {
			long bomReleaseTime = bomReleases.get(0).getUpdated().getTime() + COOLOFF_PREIOD_4_RELEASE;
			long bomReleaseAndCoolOff = bomReleaseTime + (coolOff * 60000) + 59000;
			if (bomReleaseTime < eventTimestamp && eventTimestamp < bomReleaseAndCoolOff) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public List<CmsCI> getPlatformsForEnv(long ciId) {
		List<CmsCI> platforms = new ArrayList<>();
		List<CmsCIRelation> envToPlatformsRels = cmProcessor.getFromCIRelations(ciId, "manifest.ComposedOf",null, "manifest.Platform");
		if (envToPlatformsRels != null) {
			for (CmsCIRelation rel : envToPlatformsRels) {
				platforms.add(rel.getToCi());
			}
		}
		return platforms;
	}
	
	/**
	 * Returns true if the global semaphore flag for suspending 
	 * OpAmp is set
	 * 
	 * @return
	 */
	public boolean isOpAmpSuspended(){
		if (getBooleanVariable(OPAMP_SUSPENDED)) {
			logger.warn("OpAmp is suspended temporarily. All notifications and auto-repair/auto-replace actions are suspended.");
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the cloud semaphore flag for suspending 
	 * OpAmp is set
	 * @param deployedToRels list of deployedTo relations
	 * @return
	 */
	public boolean isOpAmpSuspendedForCloud(List<CmsCIRelation> deployedToRels) {
		if (deployedToRels.size() > 0) {
			CmsCIRelation ciRelation = deployedToRels.get(0);
			String cloudCiName = ciRelation.getToCi().getCiName();
			long ciId = ciRelation.getFromCiId();
			String opampCloudFlag = OPAMP_SUSPENDED + "_CLOUD_" + cloudCiName.toUpperCase();
			if (getBooleanVariable(opampCloudFlag)) {
				logger.warn("OpAmp is suspended temporarily for cloud " + cloudCiName + ", ciId:" + ciId + ". All notifications and auto-repair/auto-replace actions are suspended.");
				return true;
			}
		}
		return false;
	}

	public boolean getBooleanVariable(String cloudCiName) {
		CmsVar repairStatus = cmProcessor.getCmSimpleVar(cloudCiName);
		return repairStatus != null && Boolean.TRUE.toString().equals(repairStatus.getValue());
	}

	/**
	 * Fetch list of deployedTo relations for the given ciId
	 * @param ciId
	 * @return
	 */
	public List<CmsCIRelation> fetchDeployedToRelations(long ciId){
		return cmProcessor.getFromCIRelations(ciId, "base.DeployedTo",null, "account.Cloud");
	}

    public boolean isHeartbeatAlarmSuspended() {
        return getBooleanVariable(HEARTBEAT_ALARMS_SUSPENDED);
    }

    public boolean isRepairDelayEnabled(CmsCI platform) {
        if (platform == null) {
            logger.error("Platform is null, can not get auto-repair delay flag ");
            return false;
        }
        return isAttributeEnabled(platform, "autorepair_exponential_backoff") && isGlobalRepairDelayEnabled();
    }

    public boolean isGlobalRepairDelayEnabled() {
        if (!getBooleanVariable(EXPONENTIAL_REPAIR_DELAY_VAR)) {
            logger.warn("Exponential delay of auto-repair procedures is disabled globally !");
            return false;
        }
        return true;
    }

    public boolean isAutoReplaceEnabled(CmsCI platform) {
        return isAttributeEnabled(platform, "autoreplace");
    }
}
