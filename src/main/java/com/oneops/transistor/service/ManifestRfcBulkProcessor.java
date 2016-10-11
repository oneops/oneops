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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.oneops.transistor.util.CloudUtil;
import org.apache.log4j.Logger;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.crypto.CmsCrypto;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.dj.service.CmsRfcUtil;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAttribute;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.md.domain.CmsRelationAttribute;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsDJValidator;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.transistor.domain.ManifestRfcContainer;
import com.oneops.transistor.domain.ManifestRootRfcContainer;
import com.oneops.transistor.domain.ManifestRfcRelationTriplet;
import com.oneops.transistor.exceptions.TransistorException;


public class ManifestRfcBulkProcessor {

	static Logger logger = Logger.getLogger(ManifestRfcBulkProcessor.class);
	
	private CmsCmProcessor cmProcessor;
	private CmsMdProcessor mdProcessor;
	private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
	private CmsRfcProcessor rfcProcessor;
	private CmsDJValidator djValidator;
	private CmsRfcUtil rfcUtil;
	//private ExpEvaluator expEval;
	private TransUtil trUtil;
	private CloudUtil cloudUtil;
	
	private static final String MGMT_MANIFEST_WATCHEDBY = "mgmt.manifest.WatchedBy";
	private static final String MANIFEST_WATCHEDBY = "manifest.WatchedBy";
	private static final String MANIFEST_MONITOR = "manifest.Monitor";	
	
	private static final Set<String> DUMMY_RELS = initSet(MANIFEST_WATCHEDBY);

	private static Set<String> initSet(String... strings) {
        HashSet<String> set = new HashSet<String>();

        for (String s : strings) {
            set.add(s);
        }
        return set;
    }
	
	public void setTrUtil(TransUtil trUtil) {
		this.trUtil = trUtil;
	}

	public void setMdProcessor(CmsMdProcessor mdProcessor) {
		this.mdProcessor = mdProcessor;
	}

	public void setCmRfcMrgProcessor(CmsCmRfcMrgProcessor cmRfcMrgProcessor) {
		this.cmRfcMrgProcessor = cmRfcMrgProcessor;
	}

	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}
	
	public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
		this.rfcProcessor = rfcProcessor;
	}
	
	public void setDjValidator(CmsDJValidator djValidator) {
		this.djValidator = djValidator;
	}

	public void setRfcUtil(CmsRfcUtil rfcUtil) {
		this.rfcUtil = rfcUtil;
	}

    public void setCloudUtil(CloudUtil cloudUtil) {
        this.cloudUtil = cloudUtil;
    }

	public void processDeletedPlatforms(Collection<CmsRfcCI> mfstPlats, CmsCI env, String nsPath, String userId) {
		Set<String> newPlats = new HashSet<String>();
		for (CmsRfcCI plat : mfstPlats) newPlats.add(plat.getCiName());

		
		List<CmsRfcRelation> existingEnv2Platrels = cmRfcMrgProcessor.getFromCIRelations(env.getCiId(), "manifest.ComposedOf", "manifest.Platform", null); 
		/*List<CmsRfcCI> mfstExistingPlats = cmRfcMrgProcessor.getDfDjCi(nsPath, "manifest.Platform", null, null);
		for (CmsRfcCI existingPlat : mfstExistingPlats) {
			if (!newPlats.contains(existingPlat.getCiId())) {
				cmRfcMrgProcessor.requestCiDelete(existingPlat.getCiId(), userId);
			}
		} */

		for (CmsRfcRelation existingRel : existingEnv2Platrels) {
			if (!newPlats.contains(existingRel.getToRfcCi().getCiName())) {
				deleteManifestPlatform(existingRel.getToRfcCi(), userId);
			}
		}
	}
	
	public long deleteManifestPlatform(CmsRfcCI manifestPlatform, String userId) {
		List<CmsRfcCI> platComponents = cmRfcMrgProcessor.getDfDjCi(manifestPlatform.getNsPath(), null, null, "dj");
		for (CmsRfcCI component : platComponents) {
			cmRfcMrgProcessor.requestCiDeleteCascadeNoRelsRfcs(component.getCiId(), userId,0);
		}
		CmsRfcCI deleteRfc = cmRfcMrgProcessor.requestCiDeleteCascadeNoRelsRfcs(manifestPlatform.getCiId(), userId,0);
		return deleteRfc.getReleaseId();
	}

	
	public void processLinkedTo(Map<Long, CmsRfcCI> design2manifestPlatMap, String nsPath, String userId) {
		List<CmsRfcRelation> existingPlatRels = cmRfcMrgProcessor.getDfDjRelationsWithCIs("manifest.LinksTo", null, nsPath, "manifest.Platform", "manifest.Platform", "dj", true, true, null);
		Map<String,Long> existingRelGoids = new HashMap<String,Long>();
		for (CmsRfcRelation rel : existingPlatRels) {
			existingRelGoids.put(rel.getFromRfcCi().getCiName() + "::" + rel.getToRfcCi().getCiName(), rel.getCiRelationId());
		}
		for (Long designPlatCiId : design2manifestPlatMap.keySet()) {
			List<CmsCIRelation> platRels = cmProcessor.getFromCIRelationsNaked(designPlatCiId.longValue(), null,"LinksTo", "catalog.Platform");
			for (CmsCIRelation catalogLinksTo : platRels) {
				
				CmsRfcCI fromManifestPlatfrom = design2manifestPlatMap.get(designPlatCiId);
				CmsRfcCI toManifestPlatfrom = design2manifestPlatMap.get(catalogLinksTo.getToCiId());
				
				CmsRfcRelation manifestLinksToRfc = bootstrapRelationRfc(fromManifestPlatfrom.getCiId(), toManifestPlatfrom.getCiId(),"manifest.LinksTo", nsPath, nsPath);
				manifestLinksToRfc.setCreatedBy(userId);
				manifestLinksToRfc.setUpdatedBy(userId);
				CmsRfcRelation newManifestLinksToRfc = cmRfcMrgProcessor.upsertRelationRfc(manifestLinksToRfc, userId);
				String relKey = fromManifestPlatfrom.getCiName() + "::" + toManifestPlatfrom.getCiName(); 
				if (existingRelGoids.containsKey(relKey)) {
					existingRelGoids.remove(relKey);
				}
				logger.debug("Created LinkedTo RFC with rfcid: " + newManifestLinksToRfc.getRfcId());
			}
		}
		for (Map.Entry<String, Long> absoleteRel : existingRelGoids.entrySet()) {
			cmRfcMrgProcessor.requestRelationDelete(absoleteRel.getValue(), userId);
		}
	}

	public void processGlobalVars(long assemblyId, CmsCI env, String nsPath, String userId) {
		CmsRfcCI manifestVarRfc = null;
		List<CmsCIRelation> gvCatlogRels = cmProcessor.getToCIRelations(assemblyId, "base.ValueFor",null, "catalog.Globalvar");
		List<CmsCIRelation> gvMfstRels = cmProcessor.getToCIRelationsNaked(env.getCiId(), "manifest.ValueFor",null, "manifest.Globalvar");
		Set<Long> existingGVs = new HashSet<Long>();
		for (CmsCIRelation gvMfstRel : gvMfstRels) {
			existingGVs.add(gvMfstRel.getFromCiId());
		}
		
		for (CmsCIRelation gvCatlogRel : gvCatlogRels) {
			CmsCI catalogVar = gvCatlogRel.getFromCi();
			manifestVarRfc = trUtil.mergeCis(null, catalogVar, "manifest", nsPath, nsPath);
			setCiId(manifestVarRfc, manifestVarRfc.getCiName());
			manifestVarRfc.setCreatedBy(userId);
			manifestVarRfc.setUpdatedBy(userId);
			manifestVarRfc = cmRfcMrgProcessor.upsertCiRfc(manifestVarRfc, userId);

			List<CmsCIRelation> existingVar2Env = cmProcessor.getFromToCIRelations(manifestVarRfc.getCiId(), "manifest.ValueFor",env.getCiId());
			if (existingVar2Env.size() == 0) {
				CmsRfcRelation varToEnv = bootstrapRelationRfc(manifestVarRfc.getCiId(), env.getCiId(), "manifest.ValueFor", nsPath, nsPath);
				varToEnv.setFromRfcId(manifestVarRfc.getRfcId());
				varToEnv.setCreatedBy(userId);
				varToEnv.setUpdatedBy(userId);
				cmRfcMrgProcessor.upsertRelationRfc(varToEnv, userId);
			}
			existingGVs.remove(manifestVarRfc.getCiId());
		}
		
		for (Long ciId2delete : existingGVs) {
			cmRfcMrgProcessor.requestCiDelete(ciId2delete, userId);
		}
	}	
	
	public ManifestRfcContainer processPlatform(CmsCI designPlatform, CmsCI env, String nsPath, String userId, String availMode) {
		long  t1 = System.currentTimeMillis();
		ManifestRfcContainer platformRfcs = new ManifestRfcContainer();
		String platNsPath = nsPath + "/" + designPlatform.getCiName() + "/" + designPlatform.getAttribute("major_version").getDfValue();
		logger.info("Started working on: " + platNsPath);

		String manifestAvailMode = null;
		
		CmsRfcCI manifestPlat = null;
		//lets figure out availability mode from existing manifest platform
		List<CmsRfcCI> existingManifestPlats = cmRfcMrgProcessor.getDfDjCi(platNsPath, "manifest.Platform", designPlatform.getCiName(), "dj");
		if (existingManifestPlats.size()>0) {
			manifestPlat = existingManifestPlats.get(0);
			CmsRfcAttribute availAttr = manifestPlat.getAttribute("availability");
			if (availAttr != null) {
				manifestAvailMode = availAttr.getNewValue();
			} 
			if (manifestAvailMode == null || manifestAvailMode.equals("default")) {
				manifestAvailMode = env.getAttribute("availability").getDfValue();
			}
		} else {
		    // Clean the existing name-space for new manifest platforms
			trUtil.cleanAndCreatePlatformNS(platNsPath);
		}
		
		if (manifestAvailMode == null) {
			if (availMode == null || availMode.equals("default")) {
				manifestAvailMode = env.getAttribute("availability").getDfValue();
			} else {
				manifestAvailMode = availMode;
			}
		}
		
		if (manifestAvailMode == null) {
			String err = "Can not figure out availability mode for platform " + designPlatform.getCiName();
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_AVAILABILITY_MODE, err);
		}
		
		String mgmtTemplNsPath = "/public/" + designPlatform.getAttribute("source").getDfValue() 
								+ "/packs/" + designPlatform.getAttribute("pack").getDfValue()
								+ "/" + designPlatform.getAttribute("version").getDfValue()
								+ "/" + manifestAvailMode;
		
		List<CmsCI> templatePlatforms = cmProcessor.getCiBy3(mgmtTemplNsPath, "mgmt.manifest.Platform", null);
		if (templatePlatforms.size()==0) {
			String err = "Can not find coresponding mgmt platform object :" + mgmtTemplNsPath;
			logger.error(err);
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_CORRESPONDING_OBJECT,err);
			
		}
		CmsCI templatePlatform = templatePlatforms.get(0);
		
		Map<Long, CmsCI> existingManifestCIs = new HashMap<Long, CmsCI>();
		Map<String, Map<String,CmsCIRelation>> existingManifestPlatRels = new HashMap<String, Map<String,CmsCIRelation>>();
		//Load existing CIs and Relations for existing manifest platform
		if(manifestPlat != null){
			existingManifestCIs = getExistingCis(manifestPlat.getNsPath());
			existingManifestPlatRels = getExistingManifestPlatRels(manifestPlat.getNsPath());
		}

		//process designPlatform templatePlatform
		boolean setActive = shouldSetActive(nsPath + "/" + designPlatform.getCiName(), designPlatform.getCiName());
		CmsRfcCI manifestPlatRfc = processTouple(templatePlatform, designPlatform, manifestPlat, platNsPath, nsPath, env, userId, setActive, 
				manifestAvailMode, existingManifestCIs, existingManifestPlatRels, platformRfcs);

		CmsRfcRelation compOfRel = cmRfcMrgProcessor.getExisitngRelationRfcMerged(env.getCiId(), "manifest.ComposedOf", manifestPlatRfc.getCiId(), null);
		//if (existingEnv2Platrels.size() == 0) {
		if (compOfRel == null) {	
			CmsRfcRelation envToPlatRel = bootstrapRelationRfc(env.getCiId(), manifestPlatRfc.getCiId(), "manifest.ComposedOf", platNsPath, nsPath);
			envToPlatRel.setToRfcId(manifestPlatRfc.getRfcId());
			envToPlatRel.setCreatedBy(userId);
			envToPlatRel.setUpdatedBy(userId);
			platformRfcs.getRootRfcRelTouple().getToRfcRelation().add(needUpdateRfcRel(envToPlatRel, null));
			//cmRfcMrgProcessor.upsertRelationRfc(envToPlatRel, userId);
		}
		
		processLocalVars(designPlatform.getCiId(), manifestPlatRfc.getCiId(),platNsPath, nsPath, userId, existingManifestCIs, existingManifestPlatRels, platformRfcs);
		processClouds(env,manifestPlatRfc, platNsPath, nsPath, userId, existingManifestCIs, existingManifestPlatRels,platformRfcs);
		
		platformRfcs.setManifestPlatformRfc(manifestPlatRfc);
		long  t2 = System.currentTimeMillis();
		logger.info("Done creating rfc's for: " + manifestPlatRfc.getNsPath()+" completed in " +(t2-t1)+" milliseconds");
		return platformRfcs;

	}
	
	public long disablePlatform(long platformCiId, String userId) {
		List<CmsRfcRelation> composedOfRels = cmRfcMrgProcessor.getToCIRelationsNaked(platformCiId, "manifest.ComposedOf", null, "manifest.Environment");
		long releaseId = 0;
		for (CmsRfcRelation composedOfRel : composedOfRels ) {
			CmsRfcRelation newRfc = trUtil.cloneRfcRelation(composedOfRel);
			newRfc.getAttribute("enabled").setNewValue("false");
			newRfc.getAttribute("enabled").setOwner("manifest");
			CmsRfcRelation rfc = cmRfcMrgProcessor.upsertRelationRfc(newRfc, userId);
			releaseId = rfc.getReleaseId();
		}
		return releaseId;
	}
	
	public long enablePlatform(long platformCiId, String userId) {
		long releaseId = 0;
		cloudUtil.check4missingServices(platformCiId);
		List<CmsRfcRelation> composedOfRels = cmRfcMrgProcessor.getToCIRelationsNaked(platformCiId, "manifest.ComposedOf", null, "manifest.Environment");
		for (CmsRfcRelation composedOfRel : composedOfRels) {
			CmsRfcRelation newRfc = trUtil.cloneRfcRelation(composedOfRel);
			newRfc.getAttribute("enabled").setNewValue("true");
			newRfc.getAttribute("enabled").setOwner("manifest");
			CmsRfcRelation rfc = cmRfcMrgProcessor.upsertRelationRfc(newRfc, userId);
			releaseId = rfc.getReleaseId();
		}

		return releaseId;
	}


	
	public void processClouds(CmsCI env, CmsRfcCI manifestPlatRfc, String platNsPath, String releasePath, String userId, Map<Long, CmsCI> existingManifestCIs, Map<String, Map<String, CmsCIRelation>> existingManifestPlatRels, ManifestRfcContainer platformRfcs) {
		List<CmsCIRelation> envCloudRels = cmProcessor.getFromCIRelationsNaked(env.getCiId(), "base.Consumes", "account.Cloud");
		for (CmsCIRelation envCloudRel : envCloudRels) {
			CmsRfcRelation existingPlatCloudRel = cmRfcMrgProcessor.getExisitngRelationRfcMerged(manifestPlatRfc.getCiId(), "base.Consumes", envCloudRel.getToCiId(), "dj");
			if (existingPlatCloudRel == null) {
				CmsRfcRelation platCloudRel = trUtil.bootstrapRelationRfcWithAttrs(manifestPlatRfc.getCiId(), envCloudRel.getToCiId(), "base.Consumes", platNsPath, releasePath, envCloudRel.getAttributes());
				if (manifestPlatRfc.getRfcId() > 0) {
					platCloudRel.setFromRfcId(manifestPlatRfc.getRfcId());
				}
				platCloudRel.setCreatedBy(userId);
				platCloudRel.setUpdatedBy(userId);
				
				setCiRelationId(platCloudRel);
				
				if(platformRfcs == null){
					cmRfcMrgProcessor.upsertRelationRfc(platCloudRel, userId);
				}else{
					if(existingManifestPlatRels ==null || (existingManifestPlatRels.get(platCloudRel.getRelationName()) == null)){
						platformRfcs.getRfcRelationList().add(platCloudRel);
					}else{
						CmsCIRelation existingCIRel = existingManifestPlatRels.get(platCloudRel.getRelationName()).get(platCloudRel.getFromCiId() + ":" + platCloudRel.getToCiId());
						CmsRfcRelation newRfcRel = needUpdateRfcRel(platCloudRel, existingCIRel); 
						if(newRfcRel != null){
							platformRfcs.getRfcRelationList().add(newRfcRel);
						}
					}
				}		
						
				//we also need to touch an entrypoint
				generateDummyEntrypointUpdates(manifestPlatRfc.getCiId(), userId);
			}
		}
	}
	
	
	private void processLocalVars(long designPlatId, long manifestPlatId, String platNsPath, String releaseNsPath, String userId, 
			Map<Long, CmsCI> existingManifestCIs, Map<String, Map<String, CmsCIRelation>> existingManifestPlatRels, ManifestRfcContainer platformRfcs) {
		//CmsRfcCI manifestVarRfc = null;
		List<CmsCIRelation> lvDesignRels = cmProcessor.getToCIRelations(designPlatId, "catalog.ValueFor",null, "catalog.Localvar");
		List<CmsCIRelation> lvMfstRels = cmProcessor.getToCIRelationsNaked(manifestPlatId, "manifest.ValueFor",null, "manifest.Localvar");
		Set<Long> existingLVs = new HashSet<Long>();
		for (CmsCIRelation lvMfstRel : lvMfstRels) {
			existingLVs.add(lvMfstRel.getFromCiId());
		}
		
		for (CmsCIRelation lvDesignRel : lvDesignRels) {
			CmsCI designVar = lvDesignRel.getFromCi();
			CmsRfcCI manifestVarRfc = trUtil.mergeCis(null, designVar, "manifest", platNsPath, releaseNsPath);
			setCiId(manifestVarRfc, manifestVarRfc.getCiName());
			manifestVarRfc.setCreatedBy(userId);
			manifestVarRfc.setUpdatedBy(userId);
			
			CmsCI existingCI = existingManifestCIs.get(manifestVarRfc.getCiId());
			CmsRfcCI newRfcCI = needUpdateRfc(manifestVarRfc, existingCI);
			if(newRfcCI != null){
				platformRfcs.getRfcList().add(newRfcCI);
			}else {
				newRfcCI = rfcUtil.mergeRfcAndCi(null, existingCI, "df");
			}

			List<CmsCIRelation> existingVar2Palt = cmProcessor.getFromToCIRelations(manifestVarRfc.getCiId(), "manifest.ValueFor",manifestPlatId);
			if (existingVar2Palt.size() == 0) {
				CmsRfcRelation varToPlat = bootstrapRelationRfc(manifestVarRfc.getCiId(), manifestPlatId, "manifest.ValueFor", platNsPath, releaseNsPath);
				varToPlat.setFromRfcId(manifestVarRfc.getRfcId());
				varToPlat.setCreatedBy(userId);
				varToPlat.setUpdatedBy(userId);
				
				CmsCIRelation existingCIRel = existingManifestPlatRels.get(varToPlat.getRelationName())!=null?
						existingManifestPlatRels.get(varToPlat.getRelationName()).get(varToPlat.getFromCiId() + ":" + varToPlat.getToCiId()):null;

				if(existingCIRel == null){
					ManifestRootRfcContainer rfcRelTouple = new ManifestRootRfcContainer();
					rfcRelTouple.setRfcCI(manifestVarRfc);
					rfcRelTouple.getFromRfcRelation().add(varToPlat);
					platformRfcs.getRfcRelToupleList().add(rfcRelTouple);
				}else {
				    CmsRfcRelation newRfcRel = needUpdateRfcRel(varToPlat, existingCIRel);			
					if(newRfcRel != null){
						//cmRfcMrgProcessor.upsertRelationRfc(varToPlat, userId);
						platformRfcs.getRfcRelationList().add(newRfcRel);
					}
				}
			}
			existingLVs.remove(manifestVarRfc.getCiId());
		}
		
		for (Long ciId2delete : existingLVs) {
			cmRfcMrgProcessor.requestCiDelete(ciId2delete, userId);
		}
	}	
	
	
	public long setPlatformActive(long platCiId, String userId) {
		CmsRfcCI plat = trUtil.cloneRfc(cmRfcMrgProcessor.getCiById(platCiId, "dj"));
		plat.getAttribute("is_active").setNewValue("true");
		plat.getAttribute("is_active").setOwner("manifest");
		cmRfcMrgProcessor.upsertCiRfc(plat, userId);
		//upsert faker update on entrypoint
		generateDummyEntrypointUpdates(platCiId,userId);
		
		//String platNs = envNsPath + "/" + plat.getCiName();
		String platBaseNs = trUtil.getPlatformBaseNS(plat.getNsPath());
		List<CmsRfcCI> existingPlats = cmRfcMrgProcessor.getDfDjCiNsLike(platBaseNs, "manifest.Platform", plat.getCiName(), "dj");
		
		for (CmsRfcCI otherPlat : existingPlats) {
			String platVersion = plat.getAttribute("major_version").getNewValue();
			String otherPlatVersion = otherPlat.getAttribute("major_version").getNewValue();
			if (!platVersion.equals(otherPlatVersion)) {
				CmsRfcCI otherPlatRfc = trUtil.cloneRfc(otherPlat);
				otherPlatRfc.getAttribute("is_active").setNewValue("false");
				otherPlatRfc.getAttribute("is_active").setOwner("manifest");
				cmRfcMrgProcessor.upsertCiRfc(otherPlatRfc, userId);
				// dummy rfc for inactive platform versions to delete their fqdn entries
				generateDummyEntrypointUpdates(otherPlat.getCiId(),userId);
			}
		}
		
		return plat.getReleaseId();
	}
	
	public void updateCloudAdminStatus(long cloudId, long envId, String adminstatus, String userId) {
		CmsCI env = cmProcessor.getCiById(envId);
		String platsNsPath = env.getNsPath() + "/" + env.getCiName() + "/manifest";
		CmsRfcRelation envConsumes = cmRfcMrgProcessor.getExisitngRelationRfcMerged(envId, "base.Consumes", cloudId, "dj");
		envConsumes.setReleaseNsPath(platsNsPath);
		if (envConsumes.getAttribute("adminstatus") != null && !adminstatus.equals(envConsumes.getAttribute("adminstatus").getNewValue())) {
			envConsumes.getAttribute("adminstatus").setNewValue(adminstatus);
			cmRfcMrgProcessor.upsertRelationRfc(envConsumes, userId, "dj");
		}
		List<CmsRfcRelation> consumesRels = cmRfcMrgProcessor.getDfDjRelationsNsLike("base.Consumes", null, platsNsPath, "manifest.Platform", "account.Cloud", "dj");
		
		for (CmsRfcRelation consumes : consumesRels) {
			if (consumes.getToCiId() == cloudId) {
				if (consumes.getAttribute("adminstatus") != null && !adminstatus.equals(consumes.getAttribute("adminstatus").getNewValue())) {
					consumes.getAttribute("adminstatus").setNewValue(adminstatus);
					cmRfcMrgProcessor.upsertRelationRfc(consumes, userId, "dj");
				}
			}
		}
	}

	public void updatePlatfomCloudStatus(CmsRfcRelation cloudRel, String userId) {

		CmsCIRelation existingConsumesRel = cmProcessor.getRelationById(cloudRel.getCiRelationId());
		if (existingConsumesRel != null) {
			if (cloudRel.getAttribute("priority") != null 
				&& !existingConsumesRel.getAttribute("priority").getDjValue().equals(cloudRel.getAttribute("priority").getNewValue())) {
					generateDummyEntrypointUpdates(cloudRel.getFromCiId(), userId);
			}
		}
		cmRfcMrgProcessor.upsertRelationRfc(cloudRel, userId);
	}
	
	private void generateDummyEntrypointUpdates(long platCiId, String userId) {
		List<CmsRfcRelation> managedViaRels = cmRfcMrgProcessor.getFromCIRelationsNakedNoAttrs(platCiId, "manifest.Entrypoint", null, null);
		for (CmsRfcRelation managedVia : managedViaRels) {
			cmRfcMrgProcessor.createDummyUpdateRfc(managedVia.getToCiId(), null, 0, userId);
		}
	}
	
	private boolean shouldSetActive(String nsPath, String ciName) {
		List<CmsRfcCI> plats = cmRfcMrgProcessor.getDfDjCiNsLike(nsPath, "manifest.Platform", ciName, "dj");
		return plats.size() == 0;
	}
	
	private void populateTemplateLookup(CmsRfcCI plat, String value) {
		if (plat.getAttribute("template") != null) {
			plat.getAttribute("template").setNewValue(value);
		} else {
			CmsClazz platClazz = mdProcessor.getClazz(plat.getCiClassId());

			for (CmsClazzAttribute clAttr : platClazz.getMdAttributes()) {
		    	if (clAttr.getAttributeName().equalsIgnoreCase("template")) {
		    		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
		    		rfcAttr.setAttributeId(clAttr.getAttributeId());
		    		rfcAttr.setAttributeName(clAttr.getAttributeName());
		    		rfcAttr.setNewValue(value);
		    		plat.addAttribute(rfcAttr);
		    	}
		    }
		}
	}
	
	public CmsRfcCI processIaas(CmsCI templIaas, long envId, String nsPath, String userId) {
		
		List<CmsCIRelation> templateRels = cmProcessor.getFromCIRelations(templIaas.getCiId(), null, "Requires", null);
		
		List<CmsCIRelation> templInternalRels = new ArrayList<CmsCIRelation>();
		Map<String, Edge> edges = new HashMap<String, Edge>();
		for (CmsCIRelation templateRel:templateRels) {
			Edge edge = new Edge();
			edge.templateRel = templateRel;
			String key = trUtil.getLongShortClazzName(templIaas.getCiClassName()) + "-Requires-" + templateRel.getToCi().getCiName();
			edges.put(key, edge);
			
			List<CmsCIRelation> ciRels = cmProcessor.getFromCIRelations(templateRel.getToCi().getCiId(), null, null);
			templInternalRels.addAll(ciRels);
			
		}
		
		String iaasNsPath = nsPath + "/" + templIaas.getCiName();
		trUtil.verifyAndCreateNS(iaasNsPath);
		
		CmsRfcCI iaasRfc = trUtil.mergeCis(templIaas, null, "manifest", iaasNsPath, nsPath);
		//setCiId(iaasRfc, templIaas.getCiName());
		setCiId(iaasRfc, iaasRfc.getCiName());
		
		populateTemplateLookup(iaasRfc, templIaas.getCiName());
		
		iaasRfc.setCreatedBy(userId);
		iaasRfc.setUpdatedBy(userId);
		iaasRfc = cmRfcMrgProcessor.upsertCiRfc(iaasRfc, userId);

		List<CmsCIRelation> existingEnv2Platrels = cmProcessor.getFromToCIRelations(envId, "manifest.ComposedOf", iaasRfc.getCiId());
		if (existingEnv2Platrels.size() == 0) {
			CmsRfcRelation envToPlatRel = bootstrapRelationRfc(envId, iaasRfc.getCiId(), "manifest.ComposedOf", iaasNsPath, nsPath);
			envToPlatRel.setToRfcId(iaasRfc.getRfcId());
			envToPlatRel.setCreatedBy(userId);
			envToPlatRel.setUpdatedBy(userId);
			cmRfcMrgProcessor.upsertRelationRfc(envToPlatRel, userId);
		}
		
		MergeResult mrgResult = procesEdges(edges, iaasRfc, iaasNsPath, nsPath, templIaas.getCiName(), userId, null, null, null);
		
		processPlatformInterRelations(templInternalRels, mrgResult.templateIdsMap, mrgResult.rfcMap, iaasNsPath, nsPath, userId, null, null, null);
		
		processEntryPoint(templIaas, iaasRfc, mrgResult.templateIdsMap, mrgResult.rfcMap , iaasNsPath, nsPath, userId, null, null, null);
		
		return iaasRfc;
	}
	
	
	
	private CmsRfcCI processTouple(CmsCI templatePlatform, CmsCI designPlatform, CmsRfcCI existingManifestPlat, String platNsPath, String envNsPath, CmsCI env, String userId, boolean setActive, 
			String availMode, Map<Long, CmsCI> existingManifestCIs, Map<String, Map<String, CmsCIRelation>> existingManifestPlatRels, ManifestRfcContainer platformRfcs) {
		
		List<CmsCIRelation> templateRels = cmProcessor.getFromCIRelations(templatePlatform.getCiId(), null, "Requires", null);
		List<CmsCIRelation> userRels = cmProcessor.getFromCIRelations(designPlatform.getCiId(), null, "Requires", null);
		
		List<CmsRfcRelation> existingDependsOnRels = cmRfcMrgProcessor.getDfDjRelations("manifest.DependsOn", null, platNsPath, null, null, null);
		
		List<CmsCIRelation> templInternalRels = new ArrayList<CmsCIRelation>();
		Map<String, Edge> edges = new HashMap<String, Edge>();
		for (CmsCIRelation templateRel:templateRels) {
			CmsCI templatePlatformResource = templateRel.getToCi();
			if (CmsConstants.CI_STATE_PENDING_DELETION.equals(templatePlatformResource.getCiState())) {
				int index = templatePlatformResource.getNsPath().lastIndexOf("/");
				String catalogPlatformNsPath = templatePlatformResource.getNsPath().substring(0, index);
				List<CmsCI> catalogTemplateCis = cmProcessor.getCiBy3(catalogPlatformNsPath, 
						templatePlatformResource.getCiClassName().replaceAll("mgmt\\.manifest\\.", "mgmt.catalog."), templatePlatformResource.getCiName());
				if (catalogTemplateCis != null && catalogTemplateCis.size() == 0) {//It is "manifest-only" resource that is now pending for deletion 
					logger.info(templatePlatformResource.getCiName() + " template resource with ciId " 
				+ templatePlatformResource.getCiId() +  " is marked for deletion");
					continue;	//skip this rel because the resource is pending deletion
				}
			}
			Edge edge = new Edge();
			edge.templateRel = templateRel;
			String key = trUtil.getLongShortClazzName(templatePlatform.getCiClassName()) + "-Requires-" + templateRel.getToCi().getCiName();
			edges.put(key, edge);
			
			List<CmsCIRelation> ciRels = cmProcessor.getFromCIRelations(templateRel.getToCi().getCiId(), null, null);
			ciRels = ciRels.stream().filter(rel->!CmsConstants.CI_STATE_PENDING_DELETION.equals(rel.getRelationState())).collect(Collectors.toList());
			templInternalRels.addAll(ciRels);
			
		}
		
		List<CmsCIRelation> designInternalRels = new ArrayList<CmsCIRelation>();
		List<CmsCIRelation> designEscortRels = new ArrayList<CmsCIRelation>();
		for (CmsCIRelation userRel : userRels) {
			String key = trUtil.getLongShortClazzName(designPlatform.getCiClassName()) + "-Requires-" + userRel.getAttribute("template").getDfValue();
			if (edges.containsKey(key)) {
				edges.get(key).userRels.add(userRel);
			} else {
				Edge edge = new Edge();
				edge.userRels.add(userRel);
				edges.put(key, edge);
			}

			AttrQueryCondition attrquery = new AttrQueryCondition();
			attrquery.setAttributeName("source");
			attrquery.setCondition("eq");
			attrquery.setAvalue("user");
			List<AttrQueryCondition> attrs = new ArrayList<AttrQueryCondition>();
			attrs.add(attrquery);
			List<CmsCIRelation> ciRels = cmProcessor.getFromCIRelationsByAttrs(userRel.getToCi().getCiId(), "catalog.DependsOn", null, null, attrs);
			designInternalRels.addAll(ciRels);
			List<CmsCIRelation> ciEscortRels = cmProcessor.getFromCIRelations(userRel.getToCi().getCiId(), "catalog.EscortedBy", null, null);
			designEscortRels.addAll(ciEscortRels);
		}
		
		CmsRfcCI manifestPlatform = null;
		if (existingManifestPlat == null) {
			CmsRfcCI rootRfc = trUtil.mergeCis(templatePlatform,designPlatform, "manifest", platNsPath, envNsPath);
			//setCiId(rootRfc, templatePlatform.getCiName());
	        if (setActive) {
	        	rootRfc.getAttribute("is_active").setNewValue("true");
	        	rootRfc.getAttribute("is_active").setOwner("manifest");
	        }
	    	rootRfc.getAttribute("availability").setNewValue(availMode);
	        
			setCiId(rootRfc, rootRfc.getCiName());
			
			populateTemplateLookup(rootRfc, templatePlatform.getCiName());
	        rootRfc.setCreatedBy(userId);
	        rootRfc.setUpdatedBy(userId);
			manifestPlatform = rootRfc;//cmRfcMrgProcessor.upsertCiRfc(rootRfc, userId);
			platformRfcs.getRootRfcRelTouple().setRfcCI(needUpdateRfc(rootRfc, null));
		} else {
			manifestPlatform = existingManifestPlat;
		}
		
		MergeResult mrgResult = procesEdges(edges, manifestPlatform, platNsPath, envNsPath, designPlatform.getCiName(), userId, existingManifestCIs, existingManifestPlatRels, platformRfcs);
		
		Set<Long> deletedCiIds = procesPlatformDeletions(manifestPlatform, mrgResult.templateIdsMap, userId);
		platformRfcs.getDeleteCiIdList().addAll(deletedCiIds);
		
		Set<String> newRels = processPackInterRelations(templInternalRels, mrgResult.templateIdsMap, mrgResult.rfcMap, platNsPath, envNsPath, manifestPlatform, userId,existingManifestCIs, existingManifestPlatRels, platformRfcs);
		newRels.addAll(processPlatformInterRelations(designInternalRels, mrgResult.designIdsMap, mrgResult.rfcDesignMap,  platNsPath, envNsPath, userId,existingManifestCIs, existingManifestPlatRels,platformRfcs));
		processEscortRelations(designEscortRels, mrgResult.designIdsMap, mrgResult.rfcDesignMap, platNsPath, envNsPath, userId, existingManifestCIs, existingManifestPlatRels, platformRfcs);
		
		for (CmsRfcRelation existingDpOn : existingDependsOnRels) {
			if (!newRels.contains(existingDpOn.getRelationGoid())) {
				if (!(deletedCiIds.contains(existingDpOn.getFromCiId()) ||  deletedCiIds.contains(existingDpOn.getToCiId()))) {
					logger.info("Deleting an existing relation: " + existingDpOn.getRelationGoid());
					platformRfcs.getRfcDeleteRelationList().add(existingDpOn);
				}
			}
		}

		processEntryPoint(templatePlatform, manifestPlatform, mrgResult.templateIdsMap, mrgResult.rfcMap, platNsPath, envNsPath, userId, existingManifestCIs, existingManifestPlatRels,platformRfcs);
		//TODO change this to use env attribute
		return manifestPlatform;
	}
	
	private Set<String> processPlatformInterRelations(List<CmsCIRelation> internalRels,
			Map<Long, List<Long>> ciIdsMap, Map<Long, List<CmsRfcCI>> newRfcDesignMap, String platNsPath, String envNsPath, String userId,
			Map<Long, CmsCI> existingManifestCIs, Map<String, Map<String, CmsCIRelation>> existingManifestPlatRels, ManifestRfcContainer platformRfcs) {
		
			Set<String> newRelsGoids = new HashSet<String>();
			
			for (CmsCIRelation ciRel : internalRels) {
				Long fromCiId = ciRel.getFromCiId();
				Long toCiId = ciRel.getToCiId();
				if (ciIdsMap.containsKey(fromCiId)){
					for (Long fromRfcCiId : ciIdsMap.get(fromCiId)) {
						if (ciIdsMap.containsKey(toCiId)) {
							for (Long toRfcCiId : ciIdsMap.get(toCiId)) {
								CmsRfcRelation rfcRelation = mergeRelations(ciRel,null,platNsPath, envNsPath);
								rfcRelation.setFromCiId(fromRfcCiId);
								rfcRelation.setToCiId(toRfcCiId);
								setCiRelationId(rfcRelation);
								rfcRelation.setCreatedBy(userId);
								rfcRelation.setUpdatedBy(userId);
								
								
								CmsCIRelation existingCIRel = existingManifestPlatRels.get(rfcRelation.getRelationName())!=null?
										existingManifestPlatRels.get(rfcRelation.getRelationName()).get(rfcRelation.getFromCiId() + ":" + rfcRelation.getToCiId()):null;
								CmsRfcRelation newRfcRelation = needUpdateRfcRel(rfcRelation, existingCIRel);		
								if(newRfcRelation != null){
									platformRfcs.getRfcRelationList().add(newRfcRelation);
									logger.debug("new relation rfc id = " + newRfcRelation.getRfcId());
								}else{
									newRfcRelation = rfcUtil.mergeRfcRelAndCiRel(null , existingCIRel, "df");
								}
								newRelsGoids.add(newRfcRelation.getRelationGoid());
							}
						} 
					}
				}
				
				if (newRfcDesignMap.containsKey(fromCiId)){
					for (CmsRfcCI fromRfcCi : newRfcDesignMap.get(fromCiId)) {
						if(newRfcDesignMap.containsKey(toCiId)) {
							for (CmsRfcCI toRfcCi : newRfcDesignMap.get(toCiId)) {
								CmsRfcRelation rfcRelation = mergeRelations(ciRel,null,platNsPath, envNsPath);
								rfcRelation.setCreatedBy(userId);
								rfcRelation.setUpdatedBy(userId);
								
								ManifestRfcRelationTriplet manifestRfcRelTriplet = new ManifestRfcRelationTriplet();
								manifestRfcRelTriplet.setFromRfcCI(fromRfcCi);
								manifestRfcRelTriplet.setToRfcCI(toRfcCi);
								manifestRfcRelTriplet.setRfcRelation(rfcRelation);
								platformRfcs.getRfcRelTripletList().add(manifestRfcRelTriplet);
							}
						}else if(ciIdsMap.containsKey(toCiId)){
							for (Long toRfcCiId : ciIdsMap.get(toCiId)) {
								CmsRfcRelation rfcRelation = mergeRelations(ciRel,null,platNsPath, envNsPath);
								ManifestRfcRelationTriplet manifestRfcRelTriplet = new ManifestRfcRelationTriplet();
								manifestRfcRelTriplet.setFromRfcCI(fromRfcCi);
								rfcRelation.setToCiId(toRfcCiId);
								rfcRelation.setCreatedBy(userId);
								rfcRelation.setUpdatedBy(userId);
								manifestRfcRelTriplet.setRfcRelation(rfcRelation);
								platformRfcs.getRfcRelTripletList().add(manifestRfcRelTriplet);
							}
						}
					}
				}
			}
			return newRelsGoids;
		
	}

	private Set<Long> procesPlatformDeletions(CmsRfcCI manifestPlatform, Map<Long, List<Long>> newIdsMap, String userId) {
		Set<Long> newCiIds = new HashSet<Long>();
		Set<Long> deletedCiIds = new HashSet<Long>();
		for (List<Long> manifestComponentCis : newIdsMap.values()) {
			for (Long ciId : manifestComponentCis) {
				newCiIds.add(ciId);
			}
		}
		List<CmsRfcRelation> oldManifestRels = cmRfcMrgProcessor.getFromCIRelationsNakedNoAttrs(manifestPlatform.getCiId(), null, "Requires", null);
		for (CmsRfcRelation oldRels : oldManifestRels) {
			if (!newCiIds.contains(oldRels.getToCiId())) {
				deletedCiIds.add(oldRels.getToCiId());
				//cmRfcMrgProcessor.requestCiDelete(oldRels.getToCiId(), userId);
			}
		}
		return deletedCiIds;
	}
	
	private void processEntryPoint(CmsCI templatePlatform, CmsRfcCI manifestPlatform, Map<Long, List<Long>> ciIdsMap, Map<Long, List<CmsRfcCI>> newRfcsMap, String nsPath, String envNsPath, String userId, 
			Map<Long, CmsCI> existingManifestCIs, Map<String, Map<String, CmsCIRelation>> existingManifestPlatRels,ManifestRfcContainer platformRfcs) {
		List<CmsCIRelation> entryPointRels = cmProcessor.getFromCIRelationsNaked(templatePlatform.getCiId(), "mgmt.Entrypoint", null);
		for (CmsCIRelation entryPointRel : entryPointRels) {
			long entryPointTemplateCiId = entryPointRel.getToCiId();
			 if (ciIdsMap.containsKey(entryPointTemplateCiId)){
				for (Long manifestCiId : ciIdsMap.get(entryPointTemplateCiId)) {
					CmsRfcRelation manifestEntryPointRel = bootstrapRelationRfc(manifestPlatform.getCiId(),manifestCiId.longValue(), "manifest.Entrypoint", nsPath, envNsPath);
					manifestEntryPointRel.setCreatedBy(userId);
					manifestEntryPointRel.setUpdatedBy(userId);
					
					CmsCIRelation existingCIRel = existingManifestPlatRels.get(manifestEntryPointRel.getRelationName())!=null?
							existingManifestPlatRels.get(manifestEntryPointRel.getRelationName()).get(manifestEntryPointRel.getFromCiId() + ":" + manifestEntryPointRel.getToCiId()):null;
					CmsRfcRelation newRfcRel = needUpdateRfcRel(manifestEntryPointRel, existingCIRel);		
					if(newRfcRel != null){
						    platformRfcs.getRfcRelationList().add(newRfcRel);
							//CmsRfcRelation newManifestEntryPointRel = cmRfcMrgProcessor.upsertRelationRfc(manifestEntryPointRel, userId);
							logger.debug("new EntryPoint relation rfc id = " + newRfcRel.getRfcId());
				}
			  }
			 }
			else if(newRfcsMap.containsKey(entryPointTemplateCiId)){
				for (CmsRfcCI manifestRfcCI : newRfcsMap.get(entryPointTemplateCiId)) {
					CmsRfcRelation manifestEntryPointRel = bootstrapRelationRfc(0 , 0 , "manifest.Entrypoint", nsPath, envNsPath);
					manifestEntryPointRel.setCreatedBy(userId);
					manifestEntryPointRel.setUpdatedBy(userId);
					
					ManifestRfcRelationTriplet entryPointRfcRel = new ManifestRfcRelationTriplet();
					//entryPointRfcRel.setFromRfcCI(platformRfcs.getRootRfcRelTouple().getRfcCI());
					entryPointRfcRel.setToRfcCI(manifestRfcCI);
					entryPointRfcRel.setRfcRelation(manifestEntryPointRel);
					platformRfcs.getRfcRelTripletList().add(entryPointRfcRel);
					
				}
			}
		}
	}

	
	private Set<String> processPackInterRelations(List<CmsCIRelation> internalRels, Map<Long, List<Long>> ciIdsMap, Map<Long, List<CmsRfcCI>> newRfcsMap, String platNsPath, String envNsPath, CmsRfcCI manifestPlat, 
			String userId, Map<Long, CmsCI> existingManifestCIs, Map<String, Map<String, CmsCIRelation>> existingManifestPlatRels, ManifestRfcContainer platformRfcs) {
		Set<String> newRelsGoids = new HashSet<String>();
		Map<Long,List<CmsCIRelation>> watchedByRels = new HashMap<Long, List<CmsCIRelation>>(); 
		for (CmsCIRelation ciRel : internalRels) {
			Long fromPackCiId = ciRel.getFromCiId();
			Long toPackCiId = ciRel.getToCiId();
			
			if (ciRel.getRelationName().equals(MGMT_MANIFEST_WATCHEDBY)) { //this is special case for monitors
				if (!watchedByRels.containsKey(ciRel.getFromCiId())) {
					watchedByRels.put(ciRel.getFromCiId(), new ArrayList<CmsCIRelation>());
				}
				watchedByRels.get(ciRel.getFromCiId()).add(ciRel);
			}
			
			if (ciIdsMap.containsKey(fromPackCiId)){
				for (Long fromManifestRfcCiId : ciIdsMap.get(fromPackCiId)) {
					if (ciIdsMap.containsKey(toPackCiId)) {
						for (Long toManifestRfcCiId : ciIdsMap.get(toPackCiId)) {
							CmsRfcRelation rfcRelation = mergeRelations(ciRel,null,platNsPath, envNsPath);
							rfcRelation.setFromCiId(fromManifestRfcCiId);
							rfcRelation.setToCiId(toManifestRfcCiId);
							setCiRelationId(rfcRelation);
							rfcRelation.setCreatedBy(userId);
							rfcRelation.setUpdatedBy(userId);
							rfcRelation.setRelationGoid(fromManifestRfcCiId + "-" + rfcRelation.getRelationId()+"-" + toManifestRfcCiId);
							
							CmsCIRelation existingCIRel = existingManifestPlatRels.get(rfcRelation.getRelationName())!=null?
									existingManifestPlatRels.get(rfcRelation.getRelationName()).get(rfcRelation.getFromCiId() + ":" + rfcRelation.getToCiId()):null;
						    CmsRfcRelation newRfcRelation = needUpdateRfcRel(rfcRelation, existingCIRel);
							if(newRfcRelation != null){
								platformRfcs.getRfcRelationList().add(newRfcRelation);
								logger.debug("new relation rfc id = " + newRfcRelation.getRfcId());
							}else{
								newRfcRelation = rfcUtil.mergeRfcRelAndCiRel(null , existingCIRel, "df");
							}
							newRelsGoids.add(newRfcRelation.getRelationGoid());
						}
					}
				}
			}
			if(newRfcsMap.containsKey(fromPackCiId)){
				for (CmsRfcCI fromManifestRfc  : newRfcsMap.get(fromPackCiId)) {
					if (newRfcsMap.containsKey(toPackCiId)) {
						for (CmsRfcCI toManifestRfc : newRfcsMap.get(toPackCiId)) {
							CmsRfcRelation rfcRelation = mergeRelations(ciRel,null,platNsPath, envNsPath);
							ManifestRfcRelationTriplet manifestRfcRelTriplet = new ManifestRfcRelationTriplet();
							manifestRfcRelTriplet.setFromRfcCI(fromManifestRfc);
							manifestRfcRelTriplet.setToRfcCI(toManifestRfc);
							//setCiRelationId(rfcRelation);
							rfcRelation.setCreatedBy(userId);
							rfcRelation.setUpdatedBy(userId);
							manifestRfcRelTriplet.setRfcRelation(rfcRelation);
							platformRfcs.getRfcRelTripletList().add(manifestRfcRelTriplet);
						}
					}else if(ciIdsMap.containsKey(toPackCiId)){
						for (Long toManifestCiId : ciIdsMap.get(toPackCiId)) {
							CmsRfcRelation rfcRelation = mergeRelations(ciRel,null,platNsPath, envNsPath);
							ManifestRfcRelationTriplet manifestRfcRelTriplet = new ManifestRfcRelationTriplet();
							manifestRfcRelTriplet.setFromRfcCI(fromManifestRfc);
							rfcRelation.setToCiId(toManifestCiId);
							//setCiRelationId(rfcRelation);
							rfcRelation.setCreatedBy(userId);
							rfcRelation.setUpdatedBy(userId);
							manifestRfcRelTriplet.setRfcRelation(rfcRelation);
							platformRfcs.getRfcRelTripletList().add(manifestRfcRelTriplet);
						}
					}
				}	
			}
		}
		
		//now lets process the monitors
		for (Long packCiId : ciIdsMap.keySet()) {
			if (watchedByRels.containsKey(packCiId)) {
				for (Long manifestCiId : ciIdsMap.get(packCiId)) {

					List<CmsRfcRelation> oldMonRels = cmRfcMrgProcessor.getFromCIRelations(manifestCiId, MANIFEST_WATCHEDBY, null, MANIFEST_MONITOR);
					Map<String, CmsRfcCI> oldManifestMons = new HashMap<String, CmsRfcCI>();
					//convert o map
					for (CmsRfcRelation oldMonRel : oldMonRels) {
						oldManifestMons.put(oldMonRel.getToRfcCi().getCiName(), oldMonRel.getToRfcCi());
					}

					for (CmsCIRelation newMonRel : watchedByRels.get(packCiId)) {
						
						CmsRfcCI manifestRfc = null;
						if(manifestCiId == 0){
							 manifestRfc = cmRfcMrgProcessor.getCiById(newMonRel.getFromCiId(), "df");
						}else {
							CmsCI ci = existingManifestCIs.get(manifestCiId);
							if(ci != null){
								manifestRfc = rfcUtil.mergeRfcAndCi(null,ci, "df");
							}else {
								manifestRfc = cmRfcMrgProcessor.getCiById(manifestCiId, "df");
							}
						}
						String monCiName = manifestPlat.getCiName() + "-" + manifestRfc.getCiName() + "-" + newMonRel.getToCi().getCiName();
						//CmsCI oldPlatMon = getOldPlatMonitor(manifestPlat,monCiName);
						
						//if there is an existing monitor - don't override it - skip
						if (!oldManifestMons.containsKey(monCiName)) {
							
							CmsRfcCI monRfc = trUtil.mergeCis(newMonRel.getToCi(), null, "manifest", platNsPath, envNsPath);
							monRfc.setCiName(monCiName);
							setCiId(monRfc, monCiName);
							monRfc.setCreatedBy(userId);
							monRfc.setUpdatedBy(userId);
							
							CmsCI existingCI = existingManifestCIs.get(monRfc.getCiId());
							CmsRfcCI newMonRfc = needUpdateRfc(monRfc, existingCI);
							if(newMonRfc != null){
								//platformRfcs.getRfcList().add(newMonRfc);
							}else{
								newMonRfc = rfcUtil.mergeRfcAndCi(monRfc, existingCI, "df");
							}

							CmsRfcRelation rfcWatchRelation = mergeRelations(newMonRel,null,platNsPath, envNsPath);
							rfcWatchRelation.setFromCiId(manifestCiId);
							rfcWatchRelation.setToCiId(newMonRfc.getCiId());
							setCiRelationId(rfcWatchRelation);
							rfcWatchRelation.setCreatedBy(userId);
							rfcWatchRelation.setUpdatedBy(userId);
							
							if(existingCI == null){
								ManifestRfcRelationTriplet manifestRfcRelTriplet = new ManifestRfcRelationTriplet();
								manifestRfcRelTriplet.setFromRfcCI(manifestRfc);
								manifestRfcRelTriplet.setToRfcCI(newMonRfc);
								manifestRfcRelTriplet.setRfcRelation(rfcWatchRelation);
								platformRfcs.getRfcRelTripletList().add(manifestRfcRelTriplet);
							}else{
								CmsRfcRelation newRfcRelation = needUpdateRfcRel(rfcWatchRelation, existingManifestPlatRels.get(rfcWatchRelation.getRelationName()).get(rfcWatchRelation.getFromCiId() + ":" + rfcWatchRelation.getToCiId()));
								if(newRfcRelation != null){
									platformRfcs.getRfcRelationList().add(newRfcRelation);
									logger.debug("new watchedby relation rfc id = " + newRfcRelation.getRfcId());
								}
							}
						} else {
							// the monitor with the same name already exists will rmove from the map to detect obsolete mons
							oldManifestMons.remove(monCiName);
						}
					}
					//remove old obsolete monitors
					for (CmsRfcCI oldMon : oldManifestMons.values()) {
						//remove monitor if it is not custom (user created)
						if (oldMon.getAttribute("custom") == null || oldMon.getAttribute("custom").getNewValue().equalsIgnoreCase("false")) {
							cmRfcMrgProcessor.requestCiDelete(oldMon.getCiId(), userId);
						}
					}
				}	
				
			} else {
				//lets check if we have any old monitors that needs to be cleaned up
				for (Long manifestCiId : ciIdsMap.get(packCiId)) {
					List<CmsRfcRelation> oldMonRels = cmRfcMrgProcessor.getFromCIRelations(manifestCiId, MANIFEST_WATCHEDBY, null, MANIFEST_MONITOR);
					for (CmsRfcRelation oldMonRel : oldMonRels ) {
						//remove monitor if it is not custom (user created)
						if (oldMonRel.getToRfcCi().getAttribute("custom") == null || oldMonRel.getToRfcCi().getAttribute("custom").getNewValue().equalsIgnoreCase("false")) {
							cmRfcMrgProcessor.requestCiDelete(oldMonRel.getToCiId(), userId);
						}

					}
				}
			}
		}
		
		//process newRfcs map
		for (Long packCiId : newRfcsMap.keySet()) {
			if (watchedByRels.containsKey(packCiId)) {
				for (CmsRfcCI manifestRfc : newRfcsMap.get(packCiId)) {
			
					for (CmsCIRelation newMonRel : watchedByRels.get(packCiId)) {
					String monCiName = manifestPlat.getCiName() + "-" + manifestRfc.getCiName() + "-" + newMonRel.getToCi().getCiName();
					CmsRfcCI monRfc = trUtil.mergeCis(newMonRel.getToCi(), null, "manifest", platNsPath, envNsPath);
					monRfc.setCiName(monCiName);
					setCiId(monRfc, monCiName);
					monRfc.setCreatedBy(userId);
					monRfc.setUpdatedBy(userId);
					
					CmsRfcRelation rfcWatchRelation = mergeRelations(newMonRel,null,platNsPath, envNsPath);
					ManifestRfcRelationTriplet manifestRfcRelTriplet = new ManifestRfcRelationTriplet();
					manifestRfcRelTriplet.setFromRfcCI(manifestRfc);
					manifestRfcRelTriplet.setToRfcCI(monRfc);
					rfcWatchRelation.setCreatedBy(userId);
					rfcWatchRelation.setUpdatedBy(userId);
					manifestRfcRelTriplet.setRfcRelation(rfcWatchRelation);
					platformRfcs.getRfcRelTripletList().add(manifestRfcRelTriplet);
				}
			}
		  }
		}
		
		return newRelsGoids;
	}
	
/*	
	private CmsCI getOldPlatMonitor(CmsRfcCI newPlat, String monCiName) {
		int platMajVersion = Integer.valueOf(newPlat.getAttribute("major_version").getNewValue()).intValue(); 
		if ( platMajVersion > 1) {
			String oldPlatNSPath = trUtil.getPlatformBaseNS(newPlat.getNsPath()) + "/" + (platMajVersion-1);
			//String monClazzName = "manifest." + trUtil.getShortClazzName(tmplMon.getCiClassName());
			List<CmsCI> existingMons = cmProcessor.getCiBy3(oldPlatNSPath, MANIFEST_MONITOR, monCiName);
			if (existingMons.size()>0) {
				return existingMons.get(0);
			}
		}
		return null;
	}
*/
	
	private void processEscortRelations(List<CmsCIRelation> designEscortRels, Map<Long, List<Long>> ciIdsMap, Map<Long, List<CmsRfcCI>> newRfcsMap, String platNsPath, String envNsPath, String userId, 
			Map<Long, CmsCI> existingManifestCIs, Map<String, Map<String, CmsCIRelation>> existingManifestPlatRels, ManifestRfcContainer platformRfcs) {

		Set<Long> existingAttachments = new HashSet<Long>();
		for (CmsRfcRelation existingAttachmentRel : cmRfcMrgProcessor.getDfDjRelations("manifest.EscortedBy", null, platNsPath, null, null, null)) {
			existingAttachments.add(existingAttachmentRel.getToCiId());
		}
	
		
		for (CmsCIRelation escortRel : designEscortRels) {
			Long fromCiId = escortRel.getFromCiId();
			if (ciIdsMap.containsKey(fromCiId)){
				for (Long manifestRfcCiId : ciIdsMap.get(fromCiId)) {
					
					CmsRfcCI attachRfc = trUtil.mergeCis(null, escortRel.getToCi(), "manifest", platNsPath, envNsPath);
					setCiId(attachRfc);
					attachRfc.setCreatedBy(userId);
					attachRfc.setUpdatedBy(userId);
					
					CmsCI existingCI = existingManifestCIs.get(attachRfc.getCiId());
					CmsRfcCI manifestAttachfRfc = needUpdateRfc(attachRfc, existingCI);
					if(existingCI != null &&manifestAttachfRfc != null){
						platformRfcs.getRfcList().add(manifestAttachfRfc);
						logger.debug("new attach rfc id = " + manifestAttachfRfc.getRfcId());
					}else {
						manifestAttachfRfc = rfcUtil.mergeRfcAndCi(attachRfc, existingCI, "df");
					}
					
					existingAttachments.remove(manifestAttachfRfc.getCiId());
					CmsRfcRelation escortRfcRelation = mergeRelations(escortRel, null, platNsPath, envNsPath);
					CmsRfcCI manifestFromRfc = cmRfcMrgProcessor.getCiById(manifestRfcCiId, "df"); 
					
					if (manifestFromRfc.getRfcId() > 0) escortRfcRelation.setFromRfcId(manifestFromRfc.getRfcId());
					escortRfcRelation.setFromCiId(manifestFromRfc.getCiId());
					
					if (manifestAttachfRfc.getRfcId() > 0) escortRfcRelation.setToRfcId(manifestAttachfRfc.getRfcId());
					escortRfcRelation.setToCiId(manifestAttachfRfc.getCiId());
					
					setCiRelationId(escortRfcRelation);
					escortRfcRelation.setCreatedBy(userId);
					escortRfcRelation.setUpdatedBy(userId);
					
					if(existingManifestPlatRels.get(escortRfcRelation.getRelationName()) == null || (existingManifestPlatRels.get(escortRfcRelation.getRelationName()).
								get(escortRfcRelation.getFromCiId() + ":" + escortRfcRelation.getToCiId()) == null)){
						
						ManifestRfcRelationTriplet manifestRfcRelTriplet = new ManifestRfcRelationTriplet();
						manifestRfcRelTriplet.setFromRfcCI(manifestFromRfc);
						manifestRfcRelTriplet.setToRfcCI(manifestAttachfRfc);
						manifestRfcRelTriplet.setRfcRelation(escortRfcRelation);
						platformRfcs.getRfcRelTripletList().add(manifestRfcRelTriplet);
						logger.debug("new attach relation rfc id = " + escortRfcRelation.getRfcId());
					}else{
						CmsRfcRelation rfcRelation = needUpdateRfcRel(escortRfcRelation, existingManifestPlatRels.get(escortRfcRelation.getRelationName()).
								get(escortRfcRelation.getFromCiId() + ":" + escortRfcRelation.getToCiId()));
						if(rfcRelation != null){
							platformRfcs.getRfcRelationList().add(rfcRelation);
							logger.debug("existing attach relation rfc id = " + rfcRelation.getRfcId());
						}
					}
					
					
					if (manifestFromRfc.getRfcId() == 0 && manifestAttachfRfc.getRfcId() > 0) {
						cmRfcMrgProcessor.createDummyUpdateRfc(manifestFromRfc.getCiId(), null, 0, userId);
					}
				}
			}
			
			
			if(newRfcsMap.containsKey(fromCiId)){
				for (CmsRfcCI manifestRfcCI : newRfcsMap.get(fromCiId)) {
					
					CmsRfcCI attachRfc = trUtil.mergeCis(null, escortRel.getToCi(), "manifest", platNsPath, envNsPath);
					setCiId(attachRfc);
					attachRfc.setCreatedBy(userId);
					attachRfc.setUpdatedBy(userId);
					CmsRfcRelation escortRfcRelation = mergeRelations(escortRel, null, platNsPath, envNsPath);
					
					ManifestRfcRelationTriplet manifestRfcRelTriplet = new ManifestRfcRelationTriplet();
					manifestRfcRelTriplet.setFromRfcCI(manifestRfcCI);
					manifestRfcRelTriplet.setToRfcCI(attachRfc);
					manifestRfcRelTriplet.setRfcRelation(escortRfcRelation);
					platformRfcs.getRfcRelTripletList().add(manifestRfcRelTriplet);
				}
			}
		}

		//remove deleted attachements
		for (Long deleteAttachCiId : existingAttachments) {
			//need to remove ci here since there is no mob objects for attachement
			cmProcessor.deleteCI(deleteAttachCiId, true, userId);
			//and now remove the rfc if any
			cmRfcMrgProcessor.requestCiDelete(deleteAttachCiId, userId);
		}
	}
	
	
	private MergeResult procesEdges(Map<String, Edge> edges, CmsRfcCI newRootRfc, String plstNsPath, String envNsPath, String designPlatName, 
			String userId, Map<Long, CmsCI> existingManifestCIs, Map<String, Map<String, CmsCIRelation>> existingManifestPlatRels, ManifestRfcContainer platformRfcs) {
		
		MergeResult mrgMaps = new MergeResult();
		
		for (Edge edge : edges.values()) {
			if (edge.templateRel == null) {
				// this design component does not belong in this env (i.e. LB in single env) skip
				continue;
			}
			if (edge.userRels.size()>0) {
				CmsCI templLeafCi = edge.templateRel.getToCi();
				
				List<Long> manifestCiIds = new ArrayList<Long>();
				List<CmsRfcCI> newManifestRfcs = new ArrayList<>();
				for (CmsCIRelation userRel : edge.userRels) {
					CmsRfcCI leafRfc = trUtil.mergeCis(templLeafCi, userRel.getToCi(), "manifest", plstNsPath, envNsPath);
					setCiId(leafRfc);
					leafRfc.setCreatedBy(userId);
					leafRfc.setUpdatedBy(userId);
					
					CmsCI existingCI = existingManifestCIs.get(leafRfc.getCiId());
					CmsRfcCI newLeafRfc = needUpdateRfc(leafRfc, existingCI);
					if(newLeafRfc != null){
						platformRfcs.getRfcList().add(newLeafRfc);
					} else {
						newLeafRfc = rfcUtil.mergeRfcAndCi(null , existingCI, "df");
					} 
					
					if(newLeafRfc.getCiId() > 0){
						manifestCiIds.add(newLeafRfc.getCiId());
					}else{
						newManifestRfcs.add(newLeafRfc);
					}
					
					CmsRfcRelation leafRfcRelation = mergeRelations(edge.templateRel,userRel, plstNsPath, envNsPath);
					if (newRootRfc.getRfcId() > 0) leafRfcRelation.setFromRfcId(newRootRfc.getRfcId());
					leafRfcRelation.setFromCiId(newRootRfc.getCiId());
					
					if (newLeafRfc.getRfcId() > 0) leafRfcRelation.setToRfcId(newLeafRfc.getRfcId());
					leafRfcRelation.setToCiId(newLeafRfc.getCiId());
					
					setCiRelationId(leafRfcRelation);
					leafRfcRelation.setCreatedBy(userId);
					leafRfcRelation.setUpdatedBy(userId);
					
					
					CmsCIRelation baseExistingRel = null;
					if(existingManifestPlatRels.get(leafRfcRelation.getRelationName()) == null){
						leafRfcRelation.setRfcAction("add");
						platformRfcs.getRfcRelationList().add(leafRfcRelation);
					}else{
						baseExistingRel = existingManifestPlatRels.get(leafRfcRelation.getRelationName()).
								get(leafRfcRelation.getFromCiId() + ":" + leafRfcRelation.getToCiId());
						CmsRfcRelation rfcRelation = needUpdateRfcRel(leafRfcRelation, baseExistingRel);
						if(rfcRelation != null){
							platformRfcs.getRfcRelationList().add(rfcRelation);
						}
					}
					
					if(newLeafRfc.getCiId() > 0){
						List<Long> manifestCiId = new ArrayList<Long>();
						manifestCiId.add(newLeafRfc.getCiId());
						mrgMaps.designIdsMap.put(userRel.getToCiId(),manifestCiId);
					}else{
						List<CmsRfcCI> manifestRfcs = new ArrayList<>();
						manifestRfcs.add(newLeafRfc);
						mrgMaps.rfcDesignMap.put(userRel.getToCiId(),manifestRfcs);
					}
					
					if(existingCI == null && baseExistingRel == null){
						ManifestRootRfcContainer rfcRelTouple = new ManifestRootRfcContainer();
						rfcRelTouple.setRfcCI(newLeafRfc);
						rfcRelTouple.getTemplateCis().add(templLeafCi.getCiId());
						if(leafRfcRelation.getRfcAction() == null){
							leafRfcRelation.setRfcAction("add");
						}
						rfcRelTouple.getToRfcRelation().add(leafRfcRelation);
						platformRfcs.getRfcRelToupleList().add(rfcRelTouple);
						
						platformRfcs.getRfcList().remove(newLeafRfc);
						platformRfcs.getRfcRelationList().remove(leafRfcRelation);
					}
				}
				if(!manifestCiIds.isEmpty()){
					mrgMaps.templateIdsMap.put(templLeafCi.getCiId(), manifestCiIds);
				}
				if(!newManifestRfcs.isEmpty()){
					mrgMaps.rfcMap.put(templLeafCi.getCiId(), newManifestRfcs);
				}
			} else {
					String cardinality = edge.templateRel.getAttribute("constraint").getDfValue();
					if ("1..1".equalsIgnoreCase(cardinality) ||
						"1..*".equalsIgnoreCase(cardinality)) {
						List<Long> manifestCiIds = new ArrayList<Long>();
						List<CmsRfcCI> newManifestRfcs = new ArrayList<>();
						CmsRfcCI leafRfc = trUtil.mergeCis(edge.templateRel.getToCi(), null, "manifest", plstNsPath, envNsPath);
						//leafRfc.setCiName(designPlatName + "-" + leafRfc.getCiName());
						setCiId(leafRfc);
						leafRfc.setCreatedBy(userId);
						leafRfc.setUpdatedBy(userId);
						//hack here for keypairs
						processSshKeys(leafRfc);
						
						CmsCI existingCI = existingManifestCIs.get(leafRfc.getCiId());
						CmsRfcCI newLeafRfc = needUpdateRfc(leafRfc, existingCI);
						if(newLeafRfc != null){
							platformRfcs.getRfcList().add(newLeafRfc);
						} else {
							newLeafRfc = rfcUtil.mergeRfcAndCi(null , existingCI, "df");
						}
						
						if(newLeafRfc.getCiId() > 0){
							manifestCiIds.add(newLeafRfc.getCiId());
						}else{
							newManifestRfcs.add(newLeafRfc);
						}
						
						CmsRfcRelation leafRfcRelation = mergeRelations(edge.templateRel,null, plstNsPath, envNsPath);
						if (newRootRfc.getRfcId()>0) leafRfcRelation.setFromRfcId(newRootRfc.getRfcId());
						leafRfcRelation.setFromCiId(newRootRfc.getCiId());
						if(newLeafRfc != null){
							if (newLeafRfc.getRfcId() > 0 ) leafRfcRelation.setToRfcId(newLeafRfc.getRfcId());
							leafRfcRelation.setToCiId(newLeafRfc.getCiId());
							setCiRelationId(leafRfcRelation);
							leafRfcRelation.setCreatedBy(userId);
							leafRfcRelation.setUpdatedBy(userId);
						}
						
						CmsCIRelation baseExistingRel = null;
						if(existingManifestPlatRels.get(leafRfcRelation.getRelationName()) == null){
							platformRfcs.getRfcRelationList().add(leafRfcRelation);
						}else{
							baseExistingRel = existingManifestPlatRels.get(leafRfcRelation.getRelationName()).
									get(leafRfcRelation.getFromCiId() + ":" + leafRfcRelation.getToCiId());
							CmsRfcRelation rfcRelation = needUpdateRfcRel(leafRfcRelation, baseExistingRel);
							if(rfcRelation != null){
								platformRfcs.getRfcRelationList().add(rfcRelation);
							}
						}
						
						if(existingCI == null && baseExistingRel == null){
							ManifestRootRfcContainer rfcRelTouple = new ManifestRootRfcContainer();
							rfcRelTouple.setRfcCI(newLeafRfc);
							rfcRelTouple.getTemplateCis().add(edge.templateRel.getToCi().getCiId());
							rfcRelTouple.getToRfcRelation().add(leafRfcRelation);
							platformRfcs.getRfcRelToupleList().add(rfcRelTouple);
							
							platformRfcs.getRfcList().remove(newLeafRfc);
							platformRfcs.getRfcRelationList().remove(leafRfcRelation);
						}
						
						if(!manifestCiIds.isEmpty()){
							mrgMaps.templateIdsMap.put(edge.templateRel.getToCi().getCiId(), manifestCiIds);
						}
						if(!newManifestRfcs.isEmpty()) mrgMaps.rfcMap.put(edge.templateRel.getToCi().getCiId(), newManifestRfcs);
				}
			}
		}
		return mrgMaps;
	}
	
	private CmsRfcRelation mergeRelations(CmsCIRelation mgmtCiRelation, CmsCIRelation designCiRelation, String nsPath, String releaseNsPath) {
		
		CmsRfcRelation newRfc = new CmsRfcRelation();
		newRfc.setNsPath(nsPath);
		newRfc.setReleaseNsPath(releaseNsPath);
		
		String targetRelationName = "manifest." + trUtil.getLongShortClazzName(mgmtCiRelation.getRelationName());
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
	    
	    //populate values from manifest template obj if it's not null
	    String owner = (designCiRelation==null) ? "manifest" : null;
	    trUtil.applyRelationToRfc(newRfc, mgmtCiRelation, relAttrs, true, owner);
	    
	    //populate values from design ci if not null;
	    trUtil.applyRelationToRfc(newRfc, designCiRelation, relAttrs, false, null);
	    
		return newRfc;
	}

	private CmsRfcRelation bootstrapRelationRfc(long fromCiId, long toCiId, String relName, String nsPath, String releaseNsPath) {
	    CmsRfcRelation newRfc = trUtil.bootstrapRelationRfc(fromCiId, toCiId, relName, nsPath, releaseNsPath, null);
	    if(newRfc.getFromCiId() > 0 && newRfc.getToCiId() > 0){
	    	setCiRelationId(newRfc);
	    }
		return newRfc;
		
	}

	public CmsRfcRelation bootstrapRelationRfcWithAttrs(long fromCiId, long toCiId, String relName, String nsPath, String releaseNsPath, Set<String> attrs) {
	    CmsRfcRelation newRfc = trUtil.bootstrapRelationRfc(fromCiId, toCiId, relName, nsPath, releaseNsPath, attrs);
	    setCiRelationId(newRfc);
		return newRfc;
		
	}
	
	public void setCiId(CmsRfcCI rfc) {
		setCiId(rfc, null);
	}	
	private void setCiId(CmsRfcCI rfc, String altCiName) {
		List<CmsCI> existingCis = null;
		if (altCiName != null) {
			existingCis = cmProcessor.getCiBy3with2Names(rfc.getNsPath(), rfc.getCiClassName(), rfc.getCiName(), altCiName);
		} else {
			existingCis = cmProcessor.getCiBy3(rfc.getNsPath(), rfc.getCiClassName(), rfc.getCiName());
		}
		
		List<CmsRfcCI> existingRfcs = null;
		if (altCiName != null) {
			existingRfcs = rfcProcessor.getOpenRfcCIByClazzAnd2Names(rfc.getNsPath(), rfc.getCiClassName(), rfc.getCiName(), altCiName);
		} else {	
			existingRfcs = rfcProcessor.getOpenRfcCIByClazzAndName(rfc.getNsPath(), rfc.getCiClassName(), rfc.getCiName());
		}
		if (existingRfcs.size()>0) {
			CmsRfcCI existingRfc = existingRfcs.get(0);
			rfc.setCiId(existingRfc.getCiId());
			rfc.setRfcId(existingRfc.getRfcId());
			if (existingCis.size()>0) {
				CmsCI ci = existingCis.get(0);
				for (String attrName : ci.getAttributes().keySet()) {
					CmsCIAttribute existingAttr = ci.getAttribute(attrName);
					if (existingAttr != null && existingAttr.getOwner() != null && existingAttr.getOwner().equalsIgnoreCase("manifest")) {
						rfc.getAttributes().remove(attrName);
					}
				}
			} else{
				for (String attrName : existingRfc.getAttributes().keySet()) {
					CmsRfcAttribute existingAttr = existingRfc.getAttribute(attrName);
					if (existingAttr != null && existingAttr.getOwner() != null && existingAttr.getOwner().equalsIgnoreCase("manifest")) {
						rfc.getAttributes().remove(attrName);
					}
				}
			}
		} else {
			if (existingCis.size()>0) {
				CmsCI ci = existingCis.get(0);
				rfc.setCiId(ci.getCiId());
				for (String attrName : ci.getAttributes().keySet()) {
					CmsCIAttribute existingAttr = ci.getAttribute(attrName);
					if (existingAttr != null && existingAttr.getOwner() != null && existingAttr.getOwner().equalsIgnoreCase("manifest")) {
						rfc.getAttributes().remove(attrName);
					}
				}
			}
		}
	}
	
	public void setCiRelationId(CmsRfcRelation rfc) {
		List<CmsRfcRelation> existingRfcs = rfcProcessor.getOpenRfcRelationBy2(rfc.getFromCiId(), rfc.getToCiId(), rfc.getRelationName(), null);
		List<CmsCIRelation> existingRels = cmProcessor.getFromToCIRelations(rfc.getFromCiId()!=null?rfc.getFromCiId():0,rfc.getRelationName(), rfc.getToCiId()!=null?rfc.getToCiId():0);

		if (existingRfcs.size()>0) {
			CmsRfcRelation existingRfc = existingRfcs.get(0);
			rfc.setCiRelationId(existingRfc.getCiRelationId());
			rfc.setRfcId(existingRfc.getRfcId()); 
			if (existingRels.size()>0) { // if there is existing ci relation, don't create attribute rfcs for manifest-owned attributes
				CmsCIRelation rel = existingRels.get(0);
				for (String attrName : rel.getAttributes().keySet()) {
					CmsCIRelationAttribute existingAttr = rel.getAttribute(attrName);
					if (existingAttr != null && existingAttr.getOwner() != null && existingAttr.getOwner().equalsIgnoreCase("manifest")) {
						rfc.getAttributes().remove(attrName);
					}
				}
			} else {
				for (String attrName : existingRfc.getAttributes().keySet()) {
					CmsRfcAttribute existingAttr = existingRfc.getAttribute(attrName);
					if (existingAttr != null && existingAttr.getOwner() != null && existingAttr.getOwner().equalsIgnoreCase("manifest")) {
						rfc.getAttributes().remove(attrName);
					}
				}
			}
		} else {
			if (existingRels.size()>0) {
				CmsCIRelation rel = existingRels.get(0);
				rfc.setCiRelationId(rel.getCiRelationId());
				for (String attrName : rel.getAttributes().keySet()) {
					CmsCIRelationAttribute existingAttr = rel.getAttribute(attrName);
					if (existingAttr != null && existingAttr.getOwner() != null && existingAttr.getOwner().equalsIgnoreCase("manifest")) {
						rfc.getAttributes().remove(attrName);
					}
				}
			}
		}
	}

	private void processSshKeys(CmsRfcCI keyPairRfc) {
		if (keyPairRfc.getCiClassName().equals("manifest.Keypair")
			&& keyPairRfc.getCiName().equals("sshkeys")
			&& keyPairRfc.getCiId() == 0) {
			//there is no keys yet, so lets create one
			Map<String,String> keys = trUtil.keyGen(null, "oneops");
			keyPairRfc.getAttribute("private").setNewValue(keys.get("private"));
			keyPairRfc.getAttribute("private").setOwner("manifest");
			keyPairRfc.getAttribute("public").setNewValue(keys.get("public"));
			keyPairRfc.getAttribute("public").setOwner("manifest");
		}	
	}
	
	private Map<Long, CmsCI> getExistingCis(String nsPath) {
		List<CmsCI> manifestPlatCiList = cmProcessor.getCiBy3NsLike(nsPath, null,  null);
		Map<Long, CmsCI> manifestPlatCIs = new HashMap<Long, CmsCI>();
		for (CmsCI manifestCI : manifestPlatCiList) {
			Long key = manifestCI.getCiId();
			manifestPlatCIs.put(key, manifestCI);
		}
		return manifestPlatCIs;
	}

	private Map<String, Map<String,CmsCIRelation>> getExistingManifestPlatRels(String nsPath) {
		List<CmsCIRelation> manifestPlatRels = cmProcessor.getCIRelationsNaked(nsPath, null, null, null, null);
		Map<String, Map<String,CmsCIRelation>> manifestPlatRelsMap = new HashMap<String, Map<String,CmsCIRelation>>();
		for (CmsCIRelation rel : manifestPlatRels) {
			if (!manifestPlatRelsMap.containsKey(rel.getRelationName())) {
				manifestPlatRelsMap.put(rel.getRelationName(), new HashMap<String,CmsCIRelation>());
			}
			manifestPlatRelsMap.get(rel.getRelationName()).put(rel.getFromCiId() + ":" + rel.getToCiId(), rel);
		}
		return manifestPlatRelsMap;
	}
	
	private CmsRfcCI needUpdateRfc(CmsRfcCI rfcCi, CmsCI existingCi){
		
		boolean needUpdate = false;
		
		if(existingCi == null || (rfcCi.getCiId() == 0 && rfcCi.getRfcId() == 0)) {
			rfcCi.setRfcAction("add");
			return rfcCi;
		}else if(rfcCi.getRfcId()>0 && rfcCi.getCiId() == 0){
			//this should never happen raise an error
			String errMsg = "the ci_id needs to be provided for the rfc_id = " + rfcCi.getRfcId();
			logger.error(errMsg);
			throw new DJException(CmsError.DJ_CI_ID_IS_NEED_ERROR, errMsg);
		}else if (rfcCi.getRfcId()==0 && rfcCi.getCiId()>0) {
			rfcCi.setRfcAction("update");
		}else if ("replace".equals(existingCi.getCiState())) {
			rfcCi.setRfcAction("replace");
			needUpdate = true;
		}
		
		if(existingCi != null){
			needUpdate = (!(rfcCi.getCiName().equalsIgnoreCase(existingCi.getCiName()))) || needUpdate  ;
			// process attributes
			List<String> equalAttrs = new ArrayList<String>();
			for (CmsRfcAttribute attr : rfcCi.getAttributes().values()){
				CmsCIAttribute existingAttr = existingCi.getAttribute(attr.getAttributeName());
				if(!(djValidator.equalStrs(attr.getNewValue(), existingAttr.getDjValue())) && !CmsCrypto.ENC_DUMMY.equals(attr.getNewValue())) {
					needUpdate = true;
				} else {
					//attrs equal - will remove from RFC
					equalAttrs.add(attr.getAttributeName());
				}
			}
			for (String eqAttr : equalAttrs) {
				rfcCi.getAttributes().remove(eqAttr);
			}
		}
		
		if(needUpdate){ 
			return rfcCi;
		}else{
			return null;
		}
	}
	
	private CmsRfcRelation needUpdateRfcRel(CmsRfcRelation rel, CmsCIRelation baseRel) {
		
		boolean needUpdate = false;
		
 		//brand new relation
		if (baseRel == null || (rel.getCiRelationId() == 0 && rel.getRfcId() == 0)){
			rel.setRfcAction("add");
			return rel;
		}else if (rel.getRfcId()>0 && rel.getCiRelationId() == 0) {
			//this should never happen raise an error
			String errMsg = "the ci_id needs to be provided for the rfc_id = " + rel.getRfcId();
			logger.error(errMsg);
			throw new DJException(CmsError.DJ_CI_ID_IS_NEED_ERROR, errMsg);
		} else if (rel.getRfcId()==0 && rel.getCiRelationId()>0) {
			//this should be an new "update" rfc lets figure out delta
			rel.setRfcAction("update");
		}
		
		if(baseRel != null){
			for (CmsRfcAttribute attr : rel.getAttributes().values()){
				CmsCIRelationAttribute existingAttr = baseRel.getAttribute(attr.getAttributeName());
				if(!(djValidator.equalStrs(attr.getNewValue(), existingAttr.getDjValue()))) {
					needUpdate = true;
					break;
				}	
			}
		}
		
		if (DUMMY_RELS.contains(rel.getRelationName())) needUpdate = true;
		
		if(needUpdate){
			return rel;
		}
		else{
			return null;
		}
	}





	private class Edge {
		CmsCIRelation templateRel;
		List<CmsCIRelation> userRels = new ArrayList<CmsCIRelation>(); 
	}
	
	private class MergeResult {
		Map<Long, List<Long>> templateIdsMap = new HashMap<Long, List<Long>>();
		Map<Long, List<Long>> designIdsMap = new HashMap<Long, List<Long>>();
		Map<Long, List<CmsRfcCI>> rfcMap = new HashMap<>();
		Map<Long, List<CmsRfcCI>> rfcDesignMap = new HashMap<>();
	}

}
