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
import com.oneops.cms.crypto.CmsCrypto;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.dj.service.CmsRfcUtil;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.exceptions.MDException;
import com.oneops.cms.md.domain.*;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsDJValidator;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.domain.ManifestRfcContainer;
import com.oneops.transistor.domain.ManifestRfcRelationTriplet;
import com.oneops.transistor.domain.ManifestRootRfcContainer;
import com.oneops.transistor.exceptions.TransistorException;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ManifestRfcBulkProcessor {

	static Logger logger = Logger.getLogger(ManifestRfcBulkProcessor.class);
	
	private CmsCmProcessor cmProcessor;
	private CmsMdProcessor mdProcessor;
	private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
	private CmsRfcProcessor rfcProcessor;
	private CmsDJValidator djValidator;
	private CmsRfcUtil rfcUtil;
	private TransUtil trUtil;
	
	private static final String MGMT_MANIFEST_WATCHEDBY = "mgmt.manifest.WatchedBy";
	private static final String MANIFEST_WATCHEDBY = "manifest.WatchedBy";
	private static final String MANIFEST_MONITOR = "manifest.Monitor";
	private static final String PACK_SOURCE_ATTRIBUTE = "source";
	private static final String PACK_NAME_ATTRIBUTE = "pack";
	private static final String PACK_VERSION_ATTRIBUTE = "version";
	
	
	private static final Set<String> DUMMY_RELS = initSet();

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


	void processDeletedPlatforms(Collection<CmsRfcCI> mfstPlats, CmsCI env, String nsPath, String userId) {
		Set<String> newPlats = new HashSet<>();
		for (CmsRfcCI plat : mfstPlats) newPlats.add(plat.getCiName());
		
		List<CmsCIRelation> existingEnv2Platrels = cmProcessor.getFromCIRelations(env.getCiId(), "manifest.ComposedOf", null, "manifest.Platform"); 

		
		for (CmsCIRelation existingRel : existingEnv2Platrels) {
			if (!newPlats.contains(existingRel.getToCi().getCiName())) {
				
				deleteManifestPlatformLight(existingRel.getToCi(), env.getNsPath()+"/"+env.getCiName()+"/manifest", userId);
			}
		}
	}
	
	private void deleteManifestPlatformLight(CmsCI manifestPlatform, String nsPath, String userId){
		List<CmsCI> platComponents = cmProcessor.getCiBy3(manifestPlatform.getNsPath(), null, null);
		Context context = new Context();
		context.nsPath = nsPath;
		context.processed = new ArrayList<>();
		context.user = userId;
		
		for (CmsCI component : platComponents) {
			requestCiDeleteCascadeNoRelsRfcs(component, 0, context);
		}
		requestCiDeleteCascadeNoRelsRfcs(manifestPlatform, 0, context);
	}

	class Context {
		String user;
		String nsPath;
		Long releaseId;
		List<Long> processed;
		Map<String, CmsClazzRelation> targets;

		private Long ensureReleaseId() {
			if (releaseId == null) {
				releaseId = rfcProcessor.getOpenReleaseIdByNs(nsPath, null, user);
			}
			return releaseId;
		}
	}

	void requestCiDeleteCascadeNoRelsRfcs(long ciId, int execOrder, Context context) {
		this.requestCiDeleteCascadeNoRelsRfcs(cmProcessor.getCiByIdNaked(ciId), execOrder, context);
	}

	void requestCiDeleteCascadeNoRelsRfcs(CmsCI ci, int execOrder, Context context) {
		if (ci == null  || context.processed.contains(ci.getCiId())) return;
		context.processed.add(ci.getCiId());
		
		
		if (context.targets == null) context.targets = new HashMap<>();
		String fromClazz = ci.getCiClassName();
		List<CmsCIRelation> fromRels = cmProcessor.getFromCIRelations(ci.getCiId(), null, null, null);
		for (CmsCIRelation rel : fromRels) {
			if (rel.getToCi() != null) {
				String key = fromClazz + rel.getRelationName() + rel.getToCi().getCiClassName();
				if (!context.targets.containsKey(key)) {
					for (CmsClazzRelation target : mdProcessor.getTargets(rel.getRelationId())) {
						String newKey = (target.getFromClassName().equals("Component") ? fromClazz : target.getFromClassName()) + target.getRelationName() + (target.getToClassName().equals("Component") ? rel.getToCi().getCiClassName() : target.getToClassName());
						context.targets.put(newKey, target);
					}
					if (!context.targets.containsKey(key)) {
						throw new MDException(CmsError.MD_TARGET_IS_MISSING_ERROR, "Target is missing:" + key);
					}
				}
				if (context.targets.get(key).getIsStrong()) {
					requestCiDeleteCascadeNoRelsRfcs(rel.getToCi(), execOrder, context);
				}
			}
		}

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
		newRfc.setExecOrder(execOrder);
		newRfc.setCreatedBy(context.user);
		newRfc.setUpdatedBy(context.user);
		newRfc.setReleaseId(context.ensureReleaseId());
		rfcProcessor.createRfcCINoCheck(newRfc, context.user);
//		cmRfcMrgProcessor.checkForDummyUpdatesNeeds(newRfc, context.user);
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
		Timing t = new Timing();
		t.start();
		ManifestRfcContainer platformRfcs = new ManifestRfcContainer();
		String platNsPath = nsPath + "/" + designPlatform.getCiName() + "/" + designPlatform.getAttribute("major_version").getDfValue();
		logger.info("Started working on: " + platNsPath);
		
		String packSource = designPlatform.getAttribute(PACK_SOURCE_ATTRIBUTE).getDfValue(); 
		String packName = designPlatform.getAttribute(PACK_NAME_ATTRIBUTE).getDfValue();
		String packVersion = designPlatform.getAttribute(PACK_VERSION_ATTRIBUTE).getDfValue();
		

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
		
		String mgmtTemplNsPath = "/public/" + packSource 
								+ "/packs/" + packName
								+ "/" + packVersion
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
		
		DesignPullContext context = new DesignPullContext();
		context.userId = userId;
		context.platNsPath = platNsPath;
		context.envNsPath = nsPath;
		context.availMode = manifestAvailMode;
		context.existingManifestCIs = existingManifestCIs;
		context.existingManifestPlatRels = existingManifestPlatRels;
		context.setActive = setActive;
		context.existingGoIds = new HashSet<>();
//		context.existingRfcCi = rfcProcessor.getOpenRfcCIByClazzAndName(platNsPath, null, null);
//		context.existingRfcRelationCi = rfcProcessor.getOpenRfcRelationsByNs(platNsPath);
		t.stop("context  preload");
		
		CmsRfcCI manifestPlatRfc = processTouple(templatePlatform, designPlatform, manifestPlat, context, platformRfcs);

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
		
		if (manifestPlat != null && !packVersion.equals(manifestPlat.getAttribute(PACK_VERSION_ATTRIBUTE).getNewValue())) {
			if (manifestPlat.getRfcAction() == null) {
				// need to remove all attrs except version, otherwise the full rfc will be created
				CmsRfcAttribute versionAttr = manifestPlat.getAttribute(PACK_VERSION_ATTRIBUTE); 
				manifestPlat.getAttributes().clear();
				manifestPlat.addAttribute(versionAttr);
				manifestPlat.setRfcAction("update");
			}
			
			manifestPlat.getAttribute(PACK_VERSION_ATTRIBUTE).setOldValue(manifestPlat.getAttribute(PACK_VERSION_ATTRIBUTE).getNewValue());
			manifestPlat.getAttribute(PACK_VERSION_ATTRIBUTE).setNewValue(packVersion);
		}

		
		
		platformRfcs.setManifestPlatformRfc(manifestPlatRfc);
		t.stop("Done creating rfc's for: " + manifestPlatRfc.getNsPath()+" completed in ");
		return platformRfcs;
	}
	
	public long disablePlatform(long platformCiId, String userId) {
		List<CmsRfcRelation> composedOfRels = cmRfcMrgProcessor.getToCIRelationsNaked(platformCiId, "manifest.ComposedOf", null, "manifest.Environment");
		long releaseId = 0;
		for (CmsRfcRelation composedOfRel : composedOfRels ) {
			CmsRfcRelation newRfc = TransUtil.cloneRfcRelation(composedOfRel);
			newRfc.getAttribute("enabled").setNewValue("false");
			newRfc.getAttribute("enabled").setOwner("manifest");
			CmsRfcRelation rfc = cmRfcMrgProcessor.upsertRelationRfc(newRfc, userId);
			releaseId = rfc.getReleaseId();
		}
		return releaseId;
	}
	
	public long enablePlatform(long platformCiId, String userId) {
		long releaseId = 0;
		List<CmsRfcRelation> composedOfRels = cmRfcMrgProcessor.getToCIRelationsNaked(platformCiId, "manifest.ComposedOf", null, "manifest.Environment");
		for (CmsRfcRelation composedOfRel : composedOfRels) {
			CmsRfcRelation newRfc = TransUtil.cloneRfcRelation(composedOfRel);
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
		CmsRfcCI plat = TransUtil.cloneRfc(cmRfcMrgProcessor.getCiById(platCiId, "dj"));
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
				CmsRfcCI otherPlatRfc = TransUtil.cloneRfc(otherPlat);
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
		
		DesignPullContext context = new DesignPullContext();
		context.platNsPath = iaasNsPath;
		context.envNsPath = nsPath;
		context.userId = userId;
		context.existingGoIds = new HashSet<>();
		MergeResult mrgResult = procesEdges(edges, iaasRfc, context, null);
		
		processPlatformInterRelations(templInternalRels, mrgResult.templateIdsMap, mrgResult.rfcMap, context, null);
		
		processEntryPoint(templIaas, iaasRfc, mrgResult.templateIdsMap, mrgResult.rfcMap , context, null);
		
		return iaasRfc;
	}
	
	private static class Timing{
		private long start;

		public Timing() {
			start();
		}

		public void start(){
			start=System.currentTimeMillis();
		}
		public void stop(String message){
			logger.info(message+ " -- "+(System.currentTimeMillis()-start));
			start();
		}
	}
	
	private CmsRfcCI processTouple(CmsCI templatePlatform, CmsCI designPlatform, CmsRfcCI existingManifestPlat, 
			DesignPullContext context, ManifestRfcContainer platformRfcs) {
		Timing t= new Timing();
		List<CmsCIRelation> templateRels = cmProcessor.getFromCIRelations(templatePlatform.getCiId(), null, "Requires", null);
		t.stop("Template relationship load");
		
		List<CmsCIRelation> userRels = cmProcessor.getFromCIRelations(designPlatform.getCiId(), null, "Requires", null);
		t.stop("User relationship load");

		List<CmsCIRelation> existingDependsOnRels = cmProcessor.getCIRelations(context.platNsPath, "manifest.DependsOn", null,  null, null);
		t.stop("Depends relationship load");
		
		List<CmsCIRelation> templInternalRels = new ArrayList<CmsCIRelation>();
		Map<String, Edge> edges = new HashMap<String, Edge>();
		Map<CmsCI, CmsCI> designToTemplateCiMap = new HashMap<>();

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
			ciRels = ciRels.stream().
					filter(rel->!CmsConstants.CI_STATE_PENDING_DELETION.equals(rel.getRelationState())).
					map(rel -> {
						rel.setFromCi(templateRel.getToCi());
						return rel;
					}).
					collect(Collectors.toList());
			templInternalRels.addAll(ciRels);
			
		}
		t.stop("ciRel collection");
		
		List<CmsCIRelation> designInternalRels = new ArrayList<CmsCIRelation>();
		List<CmsCIRelation> designEscortRels = new ArrayList<CmsCIRelation>();
		List<CmsCIRelation> designMonitorRels = new ArrayList<CmsCIRelation>();

		String designNamespace = designPlatform.getNsPath() + "/_design/" + designPlatform.getCiName();
		//cmProcessor.getCIRelations(designNamespace, "catalog.DependsOn", null, null, null);
		Map<Long, List<CmsCIRelation>> existingUserDependsOn = cmProcessor.getCIRelations(designNamespace, "catalog.DependsOn", null, null, null)
					 .stream()
					 .filter(rel->"user".equals(rel.getAttribute("source").getDjValue()))
						.collect(Collectors.groupingBy(CmsCIRelation::getFromCiId));
		Map<Long, List<CmsCIRelation>> existingEscortedBy = cmProcessor.getCIRelations(designNamespace,CmsConstants.CATALOG_ESCORTED_BY, null, null, null).stream()
				.collect(Collectors.groupingBy(CmsCIRelation::getFromCiId));

		Map<Long, List<CmsCIRelation>> existingWatchedBy = cmProcessor.getCIRelations(designNamespace,CmsConstants.CATALOG_WATCHED_BY, null, null, null).stream()
				.collect(Collectors.groupingBy(CmsCIRelation::getFromCiId));

		t.stop("design rel load");
		for (CmsCIRelation userRel : userRels) {
			String key = trUtil.getLongShortClazzName(designPlatform.getCiClassName()) + "-Requires-" + userRel.getAttribute("template").getDfValue();
			if (edges.containsKey(key)) {
				CmsCIRelation templateRel = edges.get(key).templateRel;
				if (templateRel != null) {
					designToTemplateCiMap.put(userRel.getToCi(), edges.get(key).templateRel.getToCi());
				}
				edges.get(key).userRels.add(userRel);
			} else {
				Edge edge = new Edge();
				edge.userRels.add(userRel);
				edges.put(key, edge);
			}

			long ciId = userRel.getToCi().getCiId();
			List<CmsCIRelation> ciRels = existingUserDependsOn.get(ciId);
			if (ciRels!=null) {
				designInternalRels.addAll(ciRels);
			}
			
			List<CmsCIRelation> escortedBy = existingEscortedBy.get(ciId);
			if (escortedBy!=null){
				designEscortRels.addAll(escortedBy);
			}
			List<CmsCIRelation> watchedBy = existingWatchedBy.get(ciId);
			if (watchedBy!=null){
				designMonitorRels.addAll(watchedBy);
				for (CmsCIRelation rel: watchedBy){
						rel.setFromCi(userRel.getToCi());
				}
			}
		}
		t.stop("Relations map");
		
		CmsRfcCI manifestPlatform = null;
		if (existingManifestPlat == null) {
			CmsRfcCI rootRfc = trUtil.mergeCis(templatePlatform,designPlatform, "manifest", context.platNsPath, context.envNsPath);
			//setCiId(rootRfc, templatePlatform.getCiName());
	        if (context.setActive) {
	        	rootRfc.getAttribute("is_active").setNewValue("true");
	        	rootRfc.getAttribute("is_active").setOwner("manifest");
	        }
	    	rootRfc.getAttribute("availability").setNewValue(context.availMode);
	        
			setCiId(rootRfc, rootRfc.getCiName());
			
			populateTemplateLookup(rootRfc, templatePlatform.getCiName());
	        rootRfc.setCreatedBy(context.userId);
	        rootRfc.setUpdatedBy(context.userId);
			manifestPlatform = rootRfc;//cmRfcMrgProcessor.upsertCiRfc(rootRfc, userId);
			platformRfcs.getRootRfcRelTouple().setRfcCI(needUpdateRfc(rootRfc, null));
		} else {
			manifestPlatform = existingManifestPlat;
		}
		
		MergeResult mrgResult = procesEdges(edges, manifestPlatform, context, platformRfcs);
		
		Set<Long> deletedCis = procesPlatformDeletions(manifestPlatform, mrgResult.templateIdsMap, context.userId);
		platformRfcs.getDeleteCiIdList().addAll(deletedCis);
		t.stop("process edges");
		
		
		processMonitors(designPlatform, designToTemplateCiMap, mrgResult, templInternalRels, designMonitorRels, manifestPlatform, context, platformRfcs);
		t.stop("total monitors processing");
		
		Set<String> newRels = processPackInterRelations(templInternalRels, mrgResult.templateIdsMap, mrgResult.rfcMap, manifestPlatform, context, platformRfcs);
		t.stop("pack relations processing");
		
		newRels.addAll(processPlatformInterRelations(designInternalRels, mrgResult.designIdsMap, mrgResult.rfcDesignMap, context,platformRfcs));
		t.stop("plat relations processing");
		
		processEscortRelations(designEscortRels, mrgResult.designIdsMap, mrgResult.rfcDesignMap, context, platformRfcs);
		t.stop("escort relations processing");
		
		for (CmsCIRelation existingDpOn : existingDependsOnRels) {
			if (!newRels.contains(existingDpOn.getRelationGoid())) {
				if (!(deletedCis.contains(existingDpOn.getFromCiId()) ||  deletedCis.contains(existingDpOn.getToCiId()))) {
					logger.info("Deleting an existing relation: " + existingDpOn.getRelationGoid());
					platformRfcs.getDeleteRelationList().add(existingDpOn);
				}
			}
		}
		t.stop("existing relations");

		processEntryPoint(templatePlatform, manifestPlatform, mrgResult.templateIdsMap, mrgResult.rfcMap, context, platformRfcs);
		t.stop("process entry points");
		//TODO change this to use env attribute
		return manifestPlatform;
	}
	
	private void processMonitors(CmsCI designPlatform, Map<CmsCI, CmsCI> designToTemplateCiMap, MergeResult mrgMap, List<CmsCIRelation> templRels, 
			List<CmsCIRelation> designMonitorRels, CmsRfcCI manifestPlat, DesignPullContext context, ManifestRfcContainer platformRfcs) {

		List<CmsCIRelation> templMonitorRels = templRels.stream().
				filter(rel -> MGMT_MANIFEST_WATCHEDBY.equals(rel.getRelationName()) && 
						!(CmsConstants.CI_STATE_PENDING_DELETION.equals(rel.getToCi().getCiState()))).
				collect(Collectors.toList());
		Map<String, CmsCI> templateMonitorMap = new HashMap<>();

		Map<String, Edge> monitorEdges = new HashMap<>();
		templMonitorRels.stream().forEach(templMonRel -> {
			Edge edge = new Edge();
			edge.templateRel = templMonRel;
			CmsCI templateMonitor = templMonRel.getToCi();
			String key = templMonRel.getFromCi().getCiName() + "-WatchedBy-" + templateMonitor.getCiName();
			templateMonitorMap.put(templateMonitor.getCiName(), templateMonitor);
			monitorEdges.put(key, edge);
		});

		designMonitorRels.stream().forEach(designMonRel -> {
			String key = null;
			//there wont be any template monitor for custom monitors
			if (!isCustomMonitor(designMonRel.getToCi())) {
				CmsCI templateFromCi = designToTemplateCiMap.get(designMonRel.getFromCi());
				if (templateFromCi != null) {
					String tmplName = extractTmplMonitorNameFromDesignMonitor(manifestPlat, designMonRel.getFromCi().getCiName(), designMonRel.getToCi().getCiName());
					key = templateFromCi.getCiName() + "-WatchedBy-" + tmplName;
				}
			}

			if (key != null && monitorEdges.containsKey(key)) {
				monitorEdges.get(key).userRels.add(designMonRel);
			} else {
				//this should happen only for custom monitors added in design
				Edge edge = new Edge();
				edge.userRels.add(designMonRel);
				key = designMonRel.getFromCi().getCiName() + "-WatchedBy-" + designMonRel.getToCi().getCiName();
				monitorEdges.put(key, edge);
			}
		});
		mergeMonitorRelations(monitorEdges, mrgMap, manifestPlat, context, platformRfcs);

	}

	private void mergeMonitorRelations(Map<String, Edge> monitorEdges, MergeResult mrgMap, CmsRfcCI manifestPlat, 
			DesignPullContext context, ManifestRfcContainer platformRfcs) {
		logger.info("processing monitors for platform " + manifestPlat.getCiName());
		Timing t= new Timing();
		t.start();
		List<CmsCIRelation> existingMonitorRelations = cmProcessor.getCIRelations(context.platNsPath, MANIFEST_WATCHEDBY, null, null, MANIFEST_MONITOR);
		Map<String, CmsCIRelation> existingMonitorsMap = existingMonitorRelations.stream().
				collect(Collectors.toMap(reln->reln.getToCi().getCiName(), Function.identity()));

		t.stop("-- monitors load");
		List<String> rfcNames = platformRfcs.getRfcList().stream().map(CmsRfcCI::getCiName).collect(Collectors.toList());
		
		t.start();
		monitorEdges.values().forEach(edge -> {
			CmsCIRelation tmplRelation = edge.templateRel;
			Set<String> processedManifests = new HashSet<>();
			if (!edge.userRels.isEmpty()) {
				for (CmsCIRelation designRelation : edge.userRels) {
					CmsRfcCI monitorFromRfc;
					long designFromCiId = designRelation.getFromCiId();
					if (mrgMap.rfcDesignMap.containsKey(designFromCiId)) {
						monitorFromRfc = mrgMap.rfcDesignMap.get(designFromCiId).get(0);
					}
					else {
						List<Long> manifestFromCiIds = mrgMap.designIdsMap.get(designFromCiId);
						if (manifestFromCiIds == null || manifestFromCiIds.size() < 1) {
							continue;
						}
						long manifestFromCiId = manifestFromCiIds.get(0);
						monitorFromRfc = cmRfcMrgProcessor.getCiById(manifestFromCiId, "df");
					}
					processMonitor(tmplRelation, designRelation, manifestPlat, context, platformRfcs, monitorFromRfc, existingMonitorsMap, rfcNames);
					processedManifests.add(monitorFromRfc.getCiName());
				}
			}

			if (tmplRelation != null) {
				long templateFromCiId = tmplRelation.getFromCiId();
				if (mrgMap.templateIdsMap.containsKey(templateFromCiId)) {
					mrgMap.templateIdsMap.get(templateFromCiId).forEach(manifestCiId -> {
						CmsRfcCI monitorFromRfc = cmRfcMrgProcessor.getCiById(manifestCiId, "df");
						if (!processedManifests.contains(monitorFromRfc.getCiName())) {
							processMonitor(tmplRelation, null, manifestPlat, context, platformRfcs, monitorFromRfc, existingMonitorsMap, rfcNames);
						}
					});
				}

				if (mrgMap.rfcMap.containsKey(templateFromCiId)) {
					mrgMap.rfcMap.get(templateFromCiId).forEach(manifestFromRfc -> {
						if (!processedManifests.contains(manifestFromRfc.getCiName())) {
							processMonitor(tmplRelation, null, manifestPlat, context, platformRfcs, manifestFromRfc, existingMonitorsMap, rfcNames);
						}
					});
				}

			}
		});
		t.stop("-- monitors processing");
		//remove obsolete monitors
		existingMonitorsMap.values().stream().
			filter(this::canMonitorBeDeleted).
			forEach(obsoleteMonitor -> {
				CmsCI monitor = obsoleteMonitor.getToCi();
				logger.info("delete monitor ci [" + monitor.getCiId() + "] " + monitor.getCiName() + " in " + monitor.getNsPath());
				cmRfcMrgProcessor.requestCiDelete(monitor.getCiId(), context.userId);
			});
		t.stop("-- obsolete monitors removal");
	}

	private boolean canMonitorBeDeleted(CmsCIRelation watchedByRel) {
		//monitors are eligible for deletion only if there are deleted from pack for pack monitors
		//in case of custom monitors, if they are added in design and deleted from design then they can be deleted
		//to support backward compatibility don't allow deletion of custom monitors directly added in transition
		boolean isCustomMonitor = isCustomMonitor(watchedByRel.getToCi());
		return (!isCustomMonitor || (isCustomMonitor && isMonitorSourceDesign(watchedByRel)));
	}

	private boolean isMonitorSourceDesign(CmsCIRelation watchedByRel) {
		CmsCIRelationAttribute sourceAttr = watchedByRel.getAttribute(CmsConstants.ATTR_NAME_SOURCE);
		return (sourceAttr != null && CmsConstants.ATTR_SOURCE_VALUE_DESIGN.equalsIgnoreCase(sourceAttr.getDfValue()));
	}

	private void processMonitor(CmsCIRelation tmplRelation, CmsCIRelation designRelation, CmsRfcCI manifestPlat, DesignPullContext context,
			ManifestRfcContainer platformRfcs, CmsRfcCI monitorFromRfc, Map<String, CmsCIRelation> existingMonitorsMap, List<String> rfcNames) {

		CmsCI templateCi = tmplRelation != null ? tmplRelation.getToCi() : null;
		CmsCI designCi = designRelation != null ? designRelation.getToCi() : null;

		String monitorName = null;
		if (designRelation == null) {
			CmsCI templateCiClone = new CmsCI();
            BeanUtils.copyProperties(templateCi, templateCiClone);
            templateCi = templateCiClone;
            monitorName = getMonitorName(manifestPlat, monitorFromRfc.getCiName(), tmplRelation.getToCi().getCiName());
			//change the template monitor CI name to target name if there is no design CI 
			//as we need to find a match in existing manifest CIs below using this name
			templateCi.setCiName(monitorName);
		}
		else {
			monitorName = designRelation.getToCi().getCiName();
		}

		CmsRfcCI monitorRfc = mergeCi(templateCi, designCi, context);
		existingMonitorsMap.remove(monitorName);

		CmsCI existingCI = context.existingManifestCIs.get(monitorRfc.getCiId());
		CmsRfcCI newMonitorRfc = needUpdateRfc(monitorRfc, existingCI);
		boolean monitorCiNeedsUpdate = (newMonitorRfc != null);
		if(newMonitorRfc == null){
			newMonitorRfc = rfcUtil.mergeRfcAndCi(newMonitorRfc, existingCI, CmsConstants.ATTR_VALUE_TYPE_DF);
		}

		CmsRfcRelation rfcWatchRelation = newMergedManfestRfcRelation(tmplRelation, designRelation, context);
		mergeRelationCI(context, monitorFromRfc, newMonitorRfc, rfcWatchRelation);
		CmsRfcRelation newRfcRelation = rfcWatchRelation;
		CmsCIRelation existingWatchedByRel = null;
		boolean watchedByRelationNeedsUpdate = true;
		if (existingCI != null) {
			Map<String, CmsCIRelation> watchRels = context.existingManifestPlatRels.get(rfcWatchRelation.getRelationName());
			if (watchRels != null) {
				existingWatchedByRel = watchRels.get(rfcWatchRelation.getFromCiId() + ":" + rfcWatchRelation.getToCiId());
				newRfcRelation = needUpdateRfcRel(rfcWatchRelation, existingWatchedByRel);
				watchedByRelationNeedsUpdate = (newRfcRelation != null);
			}
		}

		if (monitorCiNeedsUpdate || watchedByRelationNeedsUpdate) {
			if (existingWatchedByRel == null || existingCI == null) {
				//new monitor, so create a new triplet
				platformRfcs.getRfcRelTripletList().add(newManifestRfcRelTriplet(rfcWatchRelation, monitorFromRfc, newMonitorRfc));
			}
			else {
				if (monitorCiNeedsUpdate) {
					platformRfcs.getRfcList().add(newMonitorRfc);
				}
				if (watchedByRelationNeedsUpdate) {
					platformRfcs.getRfcRelationList().add(newRfcRelation);
				}
			}
			//create dummy update on the component if there is an update on the monitor and there is no rfc already for the component
			if (monitorCiNeedsUpdate && (monitorFromRfc.getRfcId() == 0)) {
				if (!rfcNames.contains(monitorFromRfc.getCiName())) {
					cmRfcMrgProcessor.createDummyUpdateRfc(monitorFromRfc.getCiId(), null, 0, context.userId);
				}
			}
		}
	}

	private boolean isCustomMonitor(CmsRfcCI monitorRfc) {
		CmsRfcAttribute customAttr = monitorRfc.getAttribute(CmsConstants.MONITOR_CUSTOM_ATTR);
		return (customAttr != null && "true".equalsIgnoreCase(customAttr.getNewValue()));
	}

	private boolean isCustomMonitor(CmsCI monitorCi) {
		CmsCIAttribute customAttr = monitorCi.getAttribute(CmsConstants.MONITOR_CUSTOM_ATTR);
		return (customAttr != null && "true".equalsIgnoreCase(customAttr.getDfValue()));
	}

	private String getMonitorName(CmsRfcCI manifestPlat, String componentName, String monitorName) {
		return manifestPlat.getCiName() + "-" + componentName + "-" + monitorName;
	}

	private String extractTmplMonitorNameFromDesignMonitor(CmsRfcCI manifestPlat, String componentName, String designMonitorName) {
		String prefix = manifestPlat.getCiName() + "-" + componentName + "-";
		if (designMonitorName.length() > prefix.length())
			return designMonitorName.substring(designMonitorName.indexOf(prefix) + prefix.length());
		else
			return designMonitorName;
	}

	private Set<String> processPlatformInterRelations(List<CmsCIRelation> internalRels,Map<Long, List<Long>> ciIdsMap,
			Map<Long, List<CmsRfcCI>> newRfcDesignMap, DesignPullContext context, ManifestRfcContainer platformRfcs) {
	
		Set<String> newRelsGoids = new HashSet<String>();
		for (CmsCIRelation ciRel : internalRels) {
			processRelations(ciRel, ciIdsMap, newRfcDesignMap, context, platformRfcs, newRelsGoids);
		}
		return newRelsGoids;
	}

	private Set<Long> procesPlatformDeletions(CmsRfcCI manifestPlatform, Map<Long, List<Long>> newIdsMap, String userId) {
		Set<Long> newCiIds = new HashSet<Long>();
		Set<Long> deletedCiIds = new HashSet<>();
		for (List<Long> manifestComponentCis : newIdsMap.values()) {
			newCiIds.addAll(manifestComponentCis);
		}
		List<CmsCIRelation> oldManifestRels = cmProcessor.getFromCIRelationsNakedNoAttrs(manifestPlatform.getCiId(), null, "Requires", null);
		for (CmsCIRelation oldRels : oldManifestRels) {
			if (!newCiIds.contains(oldRels.getToCiId())) {
				deletedCiIds.add(oldRels.getToCiId());
				//cmRfcMrgProcessor.requestCiDelete(oldRels.getToCiId(), userId);
			}
		}
		return deletedCiIds;
	}
	
	private void processEntryPoint(CmsCI templatePlatform, CmsRfcCI manifestPlatform, Map<Long, List<Long>> ciIdsMap, Map<Long, List<CmsRfcCI>> newRfcsMap, 
			DesignPullContext context, ManifestRfcContainer platformRfcs) {
		List<CmsCIRelation> entryPointRels = cmProcessor.getFromCIRelationsNaked(templatePlatform.getCiId(), "mgmt.Entrypoint", null);
		for (CmsCIRelation entryPointRel : entryPointRels) {
			long entryPointTemplateCiId = entryPointRel.getToCiId();
			 if (ciIdsMap.containsKey(entryPointTemplateCiId)){
				for (Long manifestCiId : ciIdsMap.get(entryPointTemplateCiId)) {
					CmsRfcRelation manifestEntryPointRel = bootstrapRelationRfc(manifestPlatform.getCiId(),manifestCiId.longValue(), "manifest.Entrypoint", context.platNsPath, context.envNsPath);
					manifestEntryPointRel.setCreatedBy(context.userId);
					manifestEntryPointRel.setUpdatedBy(context.userId);
					
					CmsCIRelation existingCIRel = context.existingManifestPlatRels.get(manifestEntryPointRel.getRelationName())!=null?
							context.existingManifestPlatRels.get(manifestEntryPointRel.getRelationName()).get(manifestEntryPointRel.getFromCiId() + ":" + manifestEntryPointRel.getToCiId()):null;
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
					CmsRfcRelation manifestEntryPointRel = bootstrapRelationRfc(0 , 0 , "manifest.Entrypoint", context.platNsPath, context.envNsPath);
					manifestEntryPointRel.setCreatedBy(context.userId);
					manifestEntryPointRel.setUpdatedBy(context.userId);
					
					ManifestRfcRelationTriplet entryPointRfcRel = new ManifestRfcRelationTriplet();
					//entryPointRfcRel.setFromRfcCI(platformRfcs.getRootRfcRelTouple().getRfcCI());
					entryPointRfcRel.setToRfcCI(manifestRfcCI);
					entryPointRfcRel.setRfcRelation(manifestEntryPointRel);
					platformRfcs.getRfcRelTripletList().add(entryPointRfcRel);
					
				}
			}
		}
	}

	
	private Set<String> processPackInterRelations(List<CmsCIRelation> internalRels, Map<Long, List<Long>> ciIdsMap, Map<Long, List<CmsRfcCI>> newRfcsMap, 
			CmsRfcCI manifestPlat, DesignPullContext context, ManifestRfcContainer platformRfcs) {
		Set<String> newRelsGoids = new HashSet<String>();
		internalRels.stream().
			filter(ciRel -> !ciRel.getRelationName().equals(MGMT_MANIFEST_WATCHEDBY)).
			forEach(ciRel -> processRelations(ciRel, ciIdsMap, newRfcsMap, context, platformRfcs, newRelsGoids));
		return newRelsGoids;
	}


	private void processRelations(CmsCIRelation ciRel, Map<Long, List<Long>> ciIdsMap,  Map<Long, List<CmsRfcCI>> newRfcsMap, 
			DesignPullContext context, ManifestRfcContainer platformRfcs, Set<String> newRelsGoids) {
		Long fromCiId = ciRel.getFromCiId();
		Long toCiId = ciRel.getToCiId();
		if (ciIdsMap.containsKey(fromCiId)){
			for (Long fromManifestRfcCiId : ciIdsMap.get(fromCiId)) {
				if (ciIdsMap.containsKey(toCiId)) {
					for (Long toManifestRfcCiId : ciIdsMap.get(toCiId)) {
						CmsRfcRelation rfcRelation = newMergedManfestRfcRelation(ciRel, null, context);
						rfcRelation.setFromCiId(fromManifestRfcCiId);
						rfcRelation.setToCiId(toManifestRfcCiId);
						 mergeRelationCI(context, rfcRelation);
						
						//setCiRelationId(rfcRelation);
						rfcRelation.setRelationGoid(fromManifestRfcCiId + "-" + rfcRelation.getRelationId()+"-" + toManifestRfcCiId);

						CmsCIRelation existingCIRel = context.existingManifestPlatRels.get(rfcRelation.getRelationName())!=null?
								context.existingManifestPlatRels.get(rfcRelation.getRelationName()).get(rfcRelation.getFromCiId() + ":" + rfcRelation.getToCiId()):null;
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
				if (newRfcsMap.containsKey(toCiId)) {
					for (CmsRfcCI toManifestRfc : newRfcsMap.get(toCiId)) {
						CmsRfcRelation rfcRelation = newMergedManfestRfcRelation(ciRel, null, context);
						rfcRelation.setFromCiId(fromManifestRfcCiId);
						addTripplet(platformRfcs.getRfcRelTripletList(), newManifestRfcRelTriplet(rfcRelation, null, toManifestRfc), context.existingGoIds);
					}
				}
			}
		}
		if (newRfcsMap.containsKey(fromCiId)){
			for (CmsRfcCI fromManifestRfc  : newRfcsMap.get(fromCiId)) {
				if (newRfcsMap.containsKey(toCiId)) {
					for (CmsRfcCI toManifestRfc : newRfcsMap.get(toCiId)) {
						CmsRfcRelation rfcRelation = newMergedManfestRfcRelation(ciRel, null, context);
						addTripplet( platformRfcs.getRfcRelTripletList(), newManifestRfcRelTriplet(rfcRelation, fromManifestRfc, toManifestRfc), context.existingGoIds);
					}
				}
				if (ciIdsMap.containsKey(toCiId)){
					for (Long toManifestCiId : ciIdsMap.get(toCiId)) {
						CmsRfcRelation rfcRelation = newMergedManfestRfcRelation(ciRel, null, context);
						rfcRelation.setToCiId(toManifestCiId);
						addTripplet(platformRfcs.getRfcRelTripletList(), newManifestRfcRelTriplet(rfcRelation, fromManifestRfc, null), context.existingGoIds);
					}
				}
			}
		}
	}

	protected void addTripplet(List<ManifestRfcRelationTriplet> rfcRelTripletList, ManifestRfcRelationTriplet manifestRfcRelationTriplet, Set<String> existingGoIds) {
		String goId = getGoId(manifestRfcRelationTriplet);
		if (!existingGoIds.contains(goId)){
			rfcRelTripletList.add(manifestRfcRelationTriplet);
			existingGoIds.add(goId);
		} else {
			logger.info("Skipping duplicate: "+ goId);
					
		}
	}

	private String getGoId(ManifestRfcRelationTriplet tr) {
		
		return  (tr.getFromRfcCI()!=null?(tr.getFromRfcCI().getNsPath()+"@"+tr.getFromRfcCI().getCiName()):"") + "-" + tr.getRfcRelation().getRelationId() + "-" + (tr.getToRfcCI()!=null?(tr.getToRfcCI().getNsPath()+"@"+tr.getToRfcCI().getCiName()):"");
	}


	private CmsRfcRelation newMergedManfestRfcRelation(CmsCIRelation templateRel, CmsCIRelation designRelation, DesignPullContext context) {
		CmsRfcRelation rfcRelation = mergeRelations(templateRel,designRelation,context.platNsPath, context.envNsPath);
		rfcRelation.setCreatedBy(context.userId);
		rfcRelation.setUpdatedBy(context.userId);
		return rfcRelation;
	}

	private ManifestRfcRelationTriplet newManifestRfcRelTriplet(CmsRfcRelation rfcRelation, CmsRfcCI fromManifestRfc, CmsRfcCI toManifestRfc) {
		ManifestRfcRelationTriplet manifestRfcRelTriplet = new ManifestRfcRelationTriplet();
		manifestRfcRelTriplet.setRfcRelation(rfcRelation);
		manifestRfcRelTriplet.setFromRfcCI(fromManifestRfc);
		manifestRfcRelTriplet.setToRfcCI(toManifestRfc);
		return manifestRfcRelTriplet;
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
	
	private void processEscortRelations(List<CmsCIRelation> designEscortRels, Map<Long, List<Long>> ciIdsMap, Map<Long, List<CmsRfcCI>> newRfcsMap, 
			DesignPullContext context, ManifestRfcContainer platformRfcs) {

		Set<Long> existingAttachments = new HashSet<Long>();
		for (CmsRfcRelation existingAttachmentRel : cmRfcMrgProcessor.getDfDjRelations("manifest.EscortedBy", null, context.platNsPath, null, null, null)) {
			existingAttachments.add(existingAttachmentRel.getToCiId());
		}
	
		
		for (CmsCIRelation escortRel : designEscortRels) {
			Long fromCiId = escortRel.getFromCiId();
			if (ciIdsMap.containsKey(fromCiId)){
				for (Long manifestRfcCiId : ciIdsMap.get(fromCiId)) {
					
					CmsRfcCI attachRfc = trUtil.mergeCis(null, escortRel.getToCi(), "manifest", context.platNsPath, context.envNsPath);
					setCiId(attachRfc);
					attachRfc.setCreatedBy(context.userId);
					attachRfc.setUpdatedBy(context.userId);
					
					CmsCI existingCI = context.existingManifestCIs.get(attachRfc.getCiId());
					CmsRfcCI manifestAttachfRfc = needUpdateRfc(attachRfc, existingCI);
					if(existingCI != null &&manifestAttachfRfc != null){
						platformRfcs.getRfcList().add(manifestAttachfRfc);
						logger.debug("new attach rfc id = " + manifestAttachfRfc.getRfcId());
					}else {
						manifestAttachfRfc = rfcUtil.mergeRfcAndCi(attachRfc, existingCI, "df");
					}
					
					existingAttachments.remove(manifestAttachfRfc.getCiId());
					CmsRfcRelation escortRfcRelation = mergeRelations(escortRel, null, context.platNsPath, context.envNsPath);
					CmsRfcCI manifestFromRfc = cmRfcMrgProcessor.getCiById(manifestRfcCiId, "df"); 
					
					mergeRelationCI(context, manifestFromRfc, manifestAttachfRfc, escortRfcRelation);
					
					if(context.existingManifestPlatRels.get(escortRfcRelation.getRelationName()) == null || (context.existingManifestPlatRels.get(escortRfcRelation.getRelationName()).
								get(escortRfcRelation.getFromCiId() + ":" + escortRfcRelation.getToCiId()) == null)){
						
						ManifestRfcRelationTriplet manifestRfcRelTriplet = new ManifestRfcRelationTriplet();
						manifestRfcRelTriplet.setFromRfcCI(manifestFromRfc);
						manifestRfcRelTriplet.setToRfcCI(manifestAttachfRfc);
						manifestRfcRelTriplet.setRfcRelation(escortRfcRelation);
						platformRfcs.getRfcRelTripletList().add(manifestRfcRelTriplet);
						logger.debug("new attach relation rfc id = " + escortRfcRelation.getRfcId());
					}else{
						CmsRfcRelation rfcRelation = needUpdateRfcRel(escortRfcRelation, context.existingManifestPlatRels.get(escortRfcRelation.getRelationName()).
								get(escortRfcRelation.getFromCiId() + ":" + escortRfcRelation.getToCiId()));
						if(rfcRelation != null){
							platformRfcs.getRfcRelationList().add(rfcRelation);
							logger.debug("existing attach relation rfc id = " + rfcRelation.getRfcId());
						}
					}
					
					
					if (manifestFromRfc.getRfcId() == 0 && manifestAttachfRfc.getRfcId() > 0) {
						cmRfcMrgProcessor.createDummyUpdateRfc(manifestFromRfc.getCiId(), null, 0, context.userId);
					}
				}
			}
			
			
			if(newRfcsMap.containsKey(fromCiId)){
				for (CmsRfcCI manifestRfcCI : newRfcsMap.get(fromCiId)) {
					
					CmsRfcCI attachRfc = trUtil.mergeCis(null, escortRel.getToCi(), "manifest", context.platNsPath, context.envNsPath);
					setCiId(attachRfc);
					attachRfc.setCreatedBy(context.userId);
					attachRfc.setUpdatedBy(context.userId);
					CmsRfcRelation escortRfcRelation = mergeRelations(escortRel, null, context.platNsPath, context.envNsPath);
					
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
			cmProcessor.deleteCI(deleteAttachCiId, true, context.userId);
			//and now remove the rfc if any
			cmRfcMrgProcessor.requestCiDelete(deleteAttachCiId, context.userId);
		}
	}
	
	
	private MergeResult procesEdges(Map<String, Edge> edges, CmsRfcCI newRootRfc, DesignPullContext context, ManifestRfcContainer platformRfcs) {
		
		MergeResult mrgMaps = new MergeResult();
		
		for (Edge edge : edges.values()) {
			if (edge.templateRel == null) {
				// this design component does not belong in this env (i.e. LB in single env) skip
				continue;
			}
			if (edge.userRels.size()>0) {
				processEdge(edge, newRootRfc, context, platformRfcs, mrgMaps);
			} else {
					CmsCI templateResource = edge.templateRel.getToCi();
					if (templateResource != null && CmsConstants.CI_STATE_PENDING_DELETION.equals(templateResource.getCiState())) {
						logger.info("template resource " + templateResource.getCiName() + " with cid: " 
								+ templateResource.getCiId() + " is marked for pending deletion.");
						continue;
					}
					String cardinality = edge.templateRel.getAttribute("constraint").getDfValue();
					if ("1..1".equalsIgnoreCase(cardinality) ||
						"1..*".equalsIgnoreCase(cardinality)) {
						List<Long> manifestCiIds = new ArrayList<Long>();
						List<CmsRfcCI> newManifestRfcs = new ArrayList<>();
						CmsRfcCI leafRfc = mergeCi(edge.templateRel.getToCi(), null, context);
						//leafRfc.setCiName(designPlatName + "-" + leafRfc.getCiName());

						//hack here for keypairs
						processSshKeys(leafRfc);
						
						CmsCI existingCI = context.existingManifestCIs.get(leafRfc.getCiId());
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
						
						CmsRfcRelation leafRfcRelation = mergeRelations(edge.templateRel,null, context.platNsPath, context.envNsPath);
						mergeRelationCI(context, newRootRfc, newLeafRfc, leafRfcRelation);
						
						CmsCIRelation baseExistingRel = null;
						if(context.existingManifestPlatRels.get(leafRfcRelation.getRelationName()) == null){
							platformRfcs.getRfcRelationList().add(leafRfcRelation);
						}else{
							baseExistingRel = context.existingManifestPlatRels.get(leafRfcRelation.getRelationName()).
									get(leafRfcRelation.getFromCiId() + ":" + leafRfcRelation.getToCiId());
							CmsRfcRelation rfcRelation = needUpdateRfcRel(leafRfcRelation, baseExistingRel);
							if(rfcRelation != null){
								platformRfcs.getRfcRelationList().add(rfcRelation);
							}
						}
						
						if(existingCI == null && baseExistingRel == null){
							ManifestRootRfcContainer rfcRelTouple = newManifestRootRfcContainer(newLeafRfc, edge.templateRel.getToCi(), leafRfcRelation);
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

	private void mergeRelationCI(DesignPullContext context, CmsRfcCI fromRfc, CmsRfcCI toRfc, CmsRfcRelation rfc) {
		if (fromRfc.getRfcId()>0) rfc.setFromRfcId(fromRfc.getRfcId());
		rfc.setFromCiId(fromRfc.getCiId());
		rfc.setFromRfcCi(fromRfc);
		if (toRfc.getRfcId() > 0 ) rfc.setToRfcId(toRfc.getRfcId());
		rfc.setToCiId(toRfc.getCiId());
		rfc.setToRfcCi(toRfc);
		//setCiRelationId(rfc);
		mergeRelationCI(context, rfc);
	}

	private void mergeRelationCI(DesignPullContext context, CmsRfcRelation rfc) {
		Map<String, CmsCIRelation> ciRelationMap = context.existingManifestPlatRels.get(rfc.getRelationName());
		if (ciRelationMap!=null) {
			CmsCIRelation rel = ciRelationMap.get(rfc.getFromCiId() + ":" + rfc.getToCiId());
			if (rel != null) {
				rfc.setCiRelationId(rel.getCiRelationId());
				for (String attrName : rel.getAttributes().keySet()) {
					CmsCIRelationAttribute existingAttr = rel.getAttribute(attrName);
					if (existingAttr != null && existingAttr.getOwner() != null && existingAttr.getOwner().equalsIgnoreCase("manifest")) {
						rfc.getAttributes().remove(attrName);
					}
				}
			}
		}

		rfc.setCreatedBy(context.userId);
		rfc.setUpdatedBy(context.userId);
	}


	private void processEdge(Edge edge, CmsRfcCI newRootRfc, DesignPullContext context, ManifestRfcContainer platformRfcs, MergeResult mrgMaps) {
		CmsCI templLeafCi = edge.templateRel.getToCi();

		List<Long> manifestCiIds = new ArrayList<Long>();
		List<CmsRfcCI> newManifestRfcs = new ArrayList<>();
		for (CmsCIRelation userRel : edge.userRels) {

			CmsRfcCI leafRfc = mergeCi(templLeafCi, userRel.getToCi(), context);
			CmsCI existingCI = context.existingManifestCIs.get(leafRfc.getCiId());
			CmsRfcCI newLeafRfc = needUpdateRfc(leafRfc, existingCI);
			if(newLeafRfc != null){
				platformRfcs.getRfcList().add(newLeafRfc);
			} else {
				newLeafRfc = rfcUtil.mergeRfcAndCi(null , existingCI, CmsConstants.ATTR_VALUE_TYPE_DF);
			}

			if(newLeafRfc.getCiId() > 0){
				manifestCiIds.add(newLeafRfc.getCiId());
			}else{
				newManifestRfcs.add(newLeafRfc);
			}

			CmsRfcRelation leafRfcRelation = newMergedManfestRfcRelation(edge.templateRel, userRel, context);

			mergeRelationCI(context, newRootRfc, newLeafRfc, leafRfcRelation);


			CmsCIRelation baseExistingRel = null;
			if(context.existingManifestPlatRels.get(leafRfcRelation.getRelationName()) == null){
				leafRfcRelation.setRfcAction("add");
				platformRfcs.getRfcRelationList().add(leafRfcRelation);
			}else{
				baseExistingRel = context.existingManifestPlatRels.get(leafRfcRelation.getRelationName()).
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
				if(leafRfcRelation.getRfcAction() == null){
					leafRfcRelation.setRfcAction("add");
				}
				ManifestRootRfcContainer rfcRelTouple = newManifestRootRfcContainer(newLeafRfc, templLeafCi, leafRfcRelation);
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
	}

	private ManifestRootRfcContainer newManifestRootRfcContainer(CmsRfcCI newLeafRfc, CmsCI templLeafCi, CmsRfcRelation leafRfcRelation) {
		ManifestRootRfcContainer rfcRelTouple = new ManifestRootRfcContainer();
		rfcRelTouple.setRfcCI(newLeafRfc);
		rfcRelTouple.getTemplateCis().add(templLeafCi.getCiId());
		rfcRelTouple.getToRfcRelation().add(leafRfcRelation);
		return rfcRelTouple;
	}

	private CmsRfcCI mergeCi(CmsCI templateCi, CmsCI userCi, DesignPullContext context) {
		CmsRfcCI mergeRfc = trUtil.mergeCis(templateCi, userCi, "manifest", context.platNsPath, context.envNsPath);
		mergeRfc.setCreatedBy(context.userId);
		mergeRfc.setUpdatedBy(context.userId);

		for (CmsCI ci:context.existingManifestCIs.values()){
			if (ci.getCiName().equals(mergeRfc.getCiName()) && ci.getCiClassName().equals(mergeRfc.getCiClassName())) {
				mergeRfc.setCiId(ci.getCiId());
				for (String attrName : ci.getAttributes().keySet()) {
					CmsCIAttribute existingAttr = ci.getAttribute(attrName);
					if (existingAttr != null && existingAttr.getOwner() != null && existingAttr.getOwner().equalsIgnoreCase("manifest")) {
						mergeRfc.getAttributes().remove(attrName);
					}
				}
				break;
			}
		}
		return mergeRfc;
	}

	private CmsRfcRelation mergeRelations(CmsCIRelation mgmtCiRelation, CmsCIRelation designCiRelation, String nsPath, String releaseNsPath) {
		
		CmsRfcRelation newRfc = new CmsRfcRelation();
		newRfc.setNsPath(nsPath);
		newRfc.setReleaseNsPath(releaseNsPath);
		
		String srcRelationName = (mgmtCiRelation != null) ? mgmtCiRelation.getRelationName() : designCiRelation.getRelationName();
		String targetRelationName = "manifest." + trUtil.getLongShortClazzName(srcRelationName);
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
	    trUtil.applyRelationToRfc(newRfc, mgmtCiRelation, relAttrs, true, owner, true);
	    
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
		public String toString() {
			return "Edge [tmpl: " + templateRel + ", userRels: " + userRels + "]";
		}
	}
	
	private class MergeResult {
		Map<Long, List<Long>> templateIdsMap = new HashMap<Long, List<Long>>();
		Map<Long, List<Long>> designIdsMap = new HashMap<Long, List<Long>>();
		Map<Long, List<CmsRfcCI>> rfcMap = new HashMap<>();
		Map<Long, List<CmsRfcCI>> rfcDesignMap = new HashMap<>();
	}

	private class DesignPullContext {
		String userId;
		String envNsPath;
		String platNsPath;
		String availMode;
		Map<Long, CmsCI> existingManifestCIs;
		Map<String, Map<String, CmsCIRelation>> existingManifestPlatRels;
		boolean setActive;
		Set<String> existingGoIds;
	}

}
