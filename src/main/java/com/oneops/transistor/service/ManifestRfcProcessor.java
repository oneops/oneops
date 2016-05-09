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

import org.apache.log4j.Logger;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAttribute;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.md.domain.CmsRelationAttribute;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.transistor.exceptions.TransistorException;

public class ManifestRfcProcessor {

	static Logger logger = Logger.getLogger(ManifestRfcProcessor.class);
	
	private CmsCmProcessor cmProcessor;
	private CmsMdProcessor mdProcessor;
	private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
	private CmsRfcProcessor rfcProcessor;
	//private ExpEvaluator expEval;
	private TransUtil trUtil;
	
	private static final String MGMT_MANIFEST_WATCHEDBY = "mgmt.manifest.WatchedBy";
	private static final String MANIFEST_WATCHEDBY = "manifest.WatchedBy";
	private static final String MANIFEST_MONITOR = "manifest.Monitor";	
	
	public void setTrUtil(TransUtil trUtil) {
		this.trUtil = trUtil;
	}

	/*
	public void setExpEval(ExpEvaluator expEval) {
		this.expEval = expEval;
	}
	*/
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
			cmRfcMrgProcessor.requestCiDelete(component.getCiId(), userId);
		}
		CmsRfcCI deleteRfc = cmRfcMrgProcessor.requestCiDelete(manifestPlatform.getCiId(), userId);
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
	
	public CmsRfcCI processPlatform(CmsCI designPlatform, CmsCI env, String nsPath, String userId, String availMode) {

		logger.info("Started working on: " + designPlatform.getCiName());

		String platNsPath = nsPath + "/" + designPlatform.getCiName() + "/" + designPlatform.getAttribute("major_version").getDfValue();
		
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
		
		//process designPlatform templatePlatform
		boolean setActive = shouldSetActive(nsPath + "/" + designPlatform.getCiName(), designPlatform.getCiName());
		CmsRfcCI manifestPlatRfc = processTouple(templatePlatform, designPlatform, manifestPlat, platNsPath, nsPath, env, userId, setActive, manifestAvailMode);

		CmsRfcRelation compOfRel = cmRfcMrgProcessor.getExisitngRelationRfcMerged(env.getCiId(), "manifest.ComposedOf", manifestPlatRfc.getCiId(), null);
		//if (existingEnv2Platrels.size() == 0) {
		if (compOfRel == null) {	
			CmsRfcRelation envToPlatRel = bootstrapRelationRfc(env.getCiId(), manifestPlatRfc.getCiId(), "manifest.ComposedOf", platNsPath, nsPath);
			envToPlatRel.setToRfcId(manifestPlatRfc.getRfcId());
			envToPlatRel.setCreatedBy(userId);
			envToPlatRel.setUpdatedBy(userId);
			cmRfcMrgProcessor.upsertRelationRfc(envToPlatRel, userId);
		}
		
		processLocalVars(designPlatform.getCiId(), manifestPlatRfc.getCiId(),platNsPath, nsPath, userId);
		processClouds(env,manifestPlatRfc, platNsPath, nsPath, userId);
		// if cloud does not provides all the required services for this platform - disable it
		Set<String> missingSrvs = getMissingServices(manifestPlatRfc.getCiId());
		if (missingSrvs.size() > 0) {
			logger.info(">>>>> Not all services available for platform: " + manifestPlatRfc.getCiName() + ", the missing services: " + missingSrvs.toString());
			disablePlatform(manifestPlatRfc.getCiId(), userId);
		}
		
		logger.info("New release id = " + manifestPlatRfc.getReleaseId());
		logger.info("Done working on platform " + designPlatform.getCiName());
		return manifestPlatRfc;
		
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
		Set<String> missingServices = getMissingServices(platformCiId);
		if (missingServices.size() == 0) {
			List<CmsRfcRelation> composedOfRels = cmRfcMrgProcessor.getToCIRelationsNaked(platformCiId, "manifest.ComposedOf", null, "manifest.Environment");
			for (CmsRfcRelation composedOfRel : composedOfRels ) {
				CmsRfcRelation newRfc = trUtil.cloneRfcRelation(composedOfRel);
				newRfc.getAttribute("enabled").setNewValue("true");
				newRfc.getAttribute("enabled").setOwner("manifest");
				CmsRfcRelation rfc = cmRfcMrgProcessor.upsertRelationRfc(newRfc, userId);
				releaseId = rfc.getReleaseId();
			}
		} else {
			String error = ">>>>> Not all services available for platform: " + platformCiId + ", the missing services:" + missingServices.toString();
			logger.error(error);
			throw new TransistorException(CmsError.TRANSISTOR_EXCEPTION, error);
		}
		
		return releaseId;
	}

	
	public Set<String> getMissingServices(long manifestPlatCiId) {
		List<CmsRfcRelation> requiresList = cmRfcMrgProcessor.getFromCIRelationsNaked(manifestPlatCiId, "manifest.Requires", null, null);
		Set<String> requiredServices = new HashSet<String>();
		for (CmsRfcRelation requires : requiresList) {
			CmsRfcAttribute servicesAttr = requires.getAttribute("services");
			if (servicesAttr != null && servicesAttr.getNewValue() != null && servicesAttr.getNewValue().length() > 0) {
				String[] ciRequiredServices = servicesAttr.getNewValue().split(",");
				for (String service : ciRequiredServices) {
					//* means optional service
					if (!service.startsWith("*")) {
						if (!requiredServices.contains(service)) {
							requiredServices.add(service);
						}
					}
				}		
			}
		}
		
		Set<String> provideServices = new HashSet<String>();
		List<CmsRfcRelation> cloudRels = cmRfcMrgProcessor.getFromCIRelations(manifestPlatCiId, "base.Consumes", "account.Cloud", "dj");
		for (CmsRfcRelation cloudRel : cloudRels) {
			List<CmsCIRelation> cloudServiceRels = cmProcessor.getFromCIRelationsNaked(cloudRel.getToCiId(),  "base.Provides", null);
			for (CmsCIRelation serviceRel : cloudServiceRels) {
				CmsCIRelationAttribute serviceAttr = serviceRel.getAttribute("service");
				if (serviceAttr != null && serviceAttr.getDjValue() != null) {
					provideServices.add(serviceAttr.getDjValue());
				}
			}
		}
		logger.info("Platform " + manifestPlatCiId + " requires:" + requiredServices.toString());
		logger.info("Available services: " + provideServices.toString());
		for (String availableService : provideServices) {
			requiredServices.remove(availableService);
		}
		
		return requiredServices;
	}
	
	public void processClouds(CmsCI env, CmsRfcCI manifestPlatRfc, String platNsPath, String releasePath, String userId) {
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
				cmRfcMrgProcessor.upsertRelationRfc(platCloudRel, userId);
				
				//we also need to touch an entrypoint
				generateDummyEntrypointUpdates(manifestPlatRfc.getCiId(), userId);
			}
		}
	}
	
	
	private void processLocalVars(long designPlatId, long manifestPlatId, String platNsPath, String releaseNsPath, String userId) {
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
			manifestVarRfc = cmRfcMrgProcessor.upsertCiRfc(manifestVarRfc, userId);

			List<CmsCIRelation> existingVar2Palt = cmProcessor.getFromToCIRelations(manifestVarRfc.getCiId(), "manifest.ValueFor",manifestPlatId);
			if (existingVar2Palt.size() == 0) {
				CmsRfcRelation varToPlat = bootstrapRelationRfc(manifestVarRfc.getCiId(), manifestPlatId, "manifest.ValueFor", platNsPath, releaseNsPath);
				varToPlat.setFromRfcId(manifestVarRfc.getRfcId());
				varToPlat.setCreatedBy(userId);
				varToPlat.setUpdatedBy(userId);
				cmRfcMrgProcessor.upsertRelationRfc(varToPlat, userId);
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
				//generateDummyEntrypointUpdates(otherPlat.getCiId(),userId);
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
		
		MergeResult mrgResult = procesEdges(edges, iaasRfc, iaasNsPath, nsPath, templIaas.getCiName(), userId);
		
		processPlatformInterRelations(templInternalRels, mrgResult.templateIdsMap, iaasNsPath, nsPath, userId);
		
		processEntryPoint(templIaas, iaasRfc, mrgResult.templateIdsMap, iaasNsPath, nsPath, userId);
		
		return iaasRfc;
	}
	
	
	
	private CmsRfcCI processTouple(CmsCI templatePlatform, CmsCI designPlatform, CmsRfcCI existingManifestPlat, String platNsPath, String envNsPath, CmsCI env, String userId, boolean setActive, String availMode) {
		
		List<CmsCIRelation> templateRels = cmProcessor.getFromCIRelations(templatePlatform.getCiId(), null, "Requires", null);
		List<CmsCIRelation> userRels = cmProcessor.getFromCIRelations(designPlatform.getCiId(), null, "Requires", null);
		
		List<CmsRfcRelation> existingDependsOnRels = cmRfcMrgProcessor.getDfDjRelations("manifest.DependsOn", null, platNsPath, null, null, null);
		
		List<CmsCIRelation> templInternalRels = new ArrayList<CmsCIRelation>();
		Map<String, Edge> edges = new HashMap<String, Edge>();
		for (CmsCIRelation templateRel:templateRels) {
			Edge edge = new Edge();
			edge.templateRel = templateRel;
			String key = trUtil.getLongShortClazzName(templatePlatform.getCiClassName()) + "-Requires-" + templateRel.getToCi().getCiName();
			edges.put(key, edge);
			
			List<CmsCIRelation> ciRels = cmProcessor.getFromCIRelations(templateRel.getToCi().getCiId(), null, null);
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
			manifestPlatform = cmRfcMrgProcessor.upsertCiRfc(rootRfc, userId);
		} else {
			manifestPlatform = existingManifestPlat;
		}
		
		MergeResult mrgResult = procesEdges(edges, manifestPlatform, platNsPath, envNsPath, designPlatform.getCiName(), userId);
		
		Set<Long> deletedCiIds = procesPlatformDeletions(manifestPlatform, mrgResult.templateIdsMap, userId);
		
		Set<String> newRels = processPackInterRelations(templInternalRels, mrgResult.templateIdsMap, platNsPath, envNsPath, manifestPlatform, userId);
		newRels.addAll(processPlatformInterRelations(designInternalRels, mrgResult.designIdsMap, platNsPath, envNsPath, userId));
		processEscortRelations(designEscortRels, mrgResult.designIdsMap, platNsPath, envNsPath, userId);
		for (CmsRfcRelation existingDpOn : existingDependsOnRels) {
			if (!newRels.contains(existingDpOn.getRelationGoid())) {
				if (!(deletedCiIds.contains(existingDpOn.getFromCiId()) ||  deletedCiIds.contains(existingDpOn.getToCiId()))) {
					if (existingDpOn.getCiRelationId() >0) {
						cmProcessor.deleteRelation(existingDpOn.getCiRelationId());
					}
					cmRfcMrgProcessor.requestRelationDelete(existingDpOn.getCiRelationId(), userId);
				}
			}
		}
		
		processEntryPoint(templatePlatform, manifestPlatform, mrgResult.templateIdsMap, platNsPath, envNsPath, userId);
		//TODO change this to use env attribute
		return manifestPlatform;
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
				cmRfcMrgProcessor.requestCiDelete(oldRels.getToCiId(), userId);
			}
		}
		return deletedCiIds;
	}
	
	private void processEntryPoint(CmsCI templatePlatform, CmsRfcCI manifestPlatform, Map<Long, List<Long>> ciIdsMap, String nsPath, String envNsPath, String userId) {
		List<CmsCIRelation> entryPointRels = cmProcessor.getFromCIRelationsNaked(templatePlatform.getCiId(), "mgmt.Entrypoint", null);
		for (CmsCIRelation entryPointRel : entryPointRels) {
			long entryPointTemplateCiId = entryPointRel.getToCiId();
			for (Long manifestCiId : ciIdsMap.get(entryPointTemplateCiId)) {
				CmsRfcRelation manifestEntryPointRel = bootstrapRelationRfc(manifestPlatform.getCiId(),manifestCiId.longValue(), "manifest.Entrypoint", nsPath, envNsPath);
				manifestEntryPointRel.setCreatedBy(userId);
				manifestEntryPointRel.setUpdatedBy(userId);
				CmsRfcRelation newManifestEntryPointRel = cmRfcMrgProcessor.upsertRelationRfc(manifestEntryPointRel, userId);
				logger.debug("new EntryPoint relation rfc id = " + newManifestEntryPointRel.getRfcId());
			}
		}
	}

	/*
	private void processInterRelations(List<CmsCIRelation> internalRels, Map<Long, List<Long>> ciIdsMap, String platNsPath, String envNsPath, String userId) {
		processInterRelations(internalRels, ciIdsMap, platNsPath,envNsPath, null, null, userId);
	}	
	*/
	
	private Set<String> processPlatformInterRelations(List<CmsCIRelation> internalRels, Map<Long, List<Long>> ciIdsMap, String platNsPath, String envNsPath, String userId) {

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
							CmsRfcRelation newRfcRelation = cmRfcMrgProcessor.upsertRelationRfc(rfcRelation, userId);
							newRelsGoids.add(newRfcRelation.getRelationGoid());
							logger.debug("new relation rfc id = " + newRfcRelation.getRfcId());
						}
					} 
				}
			}
		}
		return newRelsGoids;
	}

	
/*	
	private Set<String> _processInterRelations(List<CmsCIRelation> internalRels, Map<Long, List<Long>> ciIdsMap, String platNsPath, String envNsPath, CmsCI env, CmsRfcCI manifestPlat, String userId) {

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
							CmsRfcRelation newRfcRelation = cmRfcMrgProcessor.upsertRelationRfc(rfcRelation, userId);
							newRelsGoids.add(newRfcRelation.getRelationGoid());
							logger.debug("new relation rfc id = " + newRfcRelation.getRfcId());
						}
					} else if (env != null && ciRel.getRelationName().equals(MGMT_MANIFEST_WATCHEDBY)) { //this is special case for monitors
						
						CmsRfcCI manifestRfc = cmRfcMrgProcessor.getCiById(fromCiId, "df"); 
						String monCiName = manifestPlat.getCiName() + "-" + manifestRfc.getCiName() + "-" + ciRel.getToCi().getCiName();
						
						CmsCI oldPlatMon = getOldPlatMonitor(manifestPlat,monCiName);
						
						CmsRfcCI monRfc = trUtil.mergeCis(ciRel.getToCi(), oldPlatMon, "manifest", platNsPath, envNsPath);
						monRfc.setCiName(monCiName);
						List<CmsCI> existingMons = cmProcessor.getCiBy3(monRfc.getNsPath(), monRfc.getCiClassName(),monCiName); 
							//rfcProcessor.getOpenRfcCIByClazzAnd2Names(monRfc.getNsPath(), monRfc.getCiClassName(), monRfc.getCiName(), monCiName); 
						//if there is an existing monitor - don't override it - skip
						if (existingMons.size() == 0) {
							setCiId(monRfc, monCiName);
							monRfc.setCreatedBy(userId);
							monRfc.setUpdatedBy(userId);
							CmsRfcCI newMonRfc = cmRfcMrgProcessor.upsertCiRfc(monRfc, userId);
							CmsRfcRelation rfcWatchRelation = mergeRelations(ciRel,null,platNsPath, envNsPath);
							rfcWatchRelation.setFromCiId(fromRfcCiId);
							rfcWatchRelation.setToCiId(newMonRfc.getCiId());
							setCiRelationId(rfcWatchRelation);
							rfcWatchRelation.setCreatedBy(userId);
							rfcWatchRelation.setUpdatedBy(userId);
							CmsRfcRelation newRfcRelation = cmRfcMrgProcessor.upsertRelationRfc(rfcWatchRelation, userId);
							logger.debug("new relation rfc id = " + newRfcRelation.getRfcId());
						}
					}
				}
			}
		}
		return newRelsGoids;
	}
*/
	
	private Set<String> processPackInterRelations(List<CmsCIRelation> internalRels, Map<Long, List<Long>> ciIdsMap, String platNsPath, String envNsPath, CmsRfcCI manifestPlat, String userId) {

		Set<String> newRelsGoids = new HashSet<String>();
		Map<Long,List<CmsCIRelation>> watchedByRels = new HashMap<Long, List<CmsCIRelation>>(); 
		for (CmsCIRelation ciRel : internalRels) {
			Long fromPackCiId = ciRel.getFromCiId();
			Long toPackCiId = ciRel.getToCiId();
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
							CmsRfcRelation newRfcRelation = cmRfcMrgProcessor.upsertRelationRfc(rfcRelation, userId);
							newRelsGoids.add(newRfcRelation.getRelationGoid());
							logger.debug("new relation rfc id = " + newRfcRelation.getRfcId());
						}
					}
				}
				if (ciRel.getRelationName().equals(MGMT_MANIFEST_WATCHEDBY)) { //this is special case for monitors
					if (!watchedByRels.containsKey(ciRel.getFromCiId())) {
						watchedByRels.put(ciRel.getFromCiId(), new ArrayList<CmsCIRelation>());
					}
					watchedByRels.get(ciRel.getFromCiId()).add(ciRel);
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
						
						CmsRfcCI manifestRfc = cmRfcMrgProcessor.getCiById(manifestCiId, "df"); 
						String monCiName = manifestPlat.getCiName() + "-" + manifestRfc.getCiName() + "-" + newMonRel.getToCi().getCiName();
						//CmsCI oldPlatMon = getOldPlatMonitor(manifestPlat,monCiName);
						
						//if there is an existing monitor - don't override it - skip
						if (!oldManifestMons.containsKey(monCiName)) {
							
							CmsRfcCI monRfc = trUtil.mergeCis(newMonRel.getToCi(), null, "manifest", platNsPath, envNsPath);
							monRfc.setCiName(monCiName);
							setCiId(monRfc, monCiName);
							monRfc.setCreatedBy(userId);
							monRfc.setUpdatedBy(userId);
							CmsRfcCI newMonRfc = cmRfcMrgProcessor.upsertCiRfc(monRfc, userId);

							CmsRfcRelation rfcWatchRelation = mergeRelations(newMonRel,null,platNsPath, envNsPath);
							rfcWatchRelation.setFromCiId(manifestCiId);
							rfcWatchRelation.setToCiId(newMonRfc.getCiId());
							setCiRelationId(rfcWatchRelation);
							rfcWatchRelation.setCreatedBy(userId);
							rfcWatchRelation.setUpdatedBy(userId);
							CmsRfcRelation newRfcRelation = cmRfcMrgProcessor.upsertRelationRfc(rfcWatchRelation, userId);
							logger.debug("new relation rfc id = " + newRfcRelation.getRfcId());
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
	
	private void processEscortRelations(List<CmsCIRelation> designEscortRels, Map<Long, List<Long>> ciIdsMap, String platNsPath, String envNsPath, String userId) {

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
					CmsRfcCI manifestAttachfRfc = cmRfcMrgProcessor.upsertCiRfc(attachRfc, userId);
					existingAttachments.remove(manifestAttachfRfc.getCiId());
					logger.debug("new attach rfc id = " + manifestAttachfRfc.getRfcId());
					
					CmsRfcRelation escortRfcRelation = mergeRelations(escortRel, null, platNsPath, envNsPath);
					CmsRfcCI manifestFromRfc = cmRfcMrgProcessor.getCiById(manifestRfcCiId, "df"); 
					
					if (manifestFromRfc.getRfcId() > 0) escortRfcRelation.setFromRfcId(manifestFromRfc.getRfcId());
					escortRfcRelation.setFromCiId(manifestFromRfc.getCiId());
					
					if (manifestAttachfRfc.getRfcId() > 0) escortRfcRelation.setToRfcId(manifestAttachfRfc.getRfcId());
					escortRfcRelation.setToCiId(manifestAttachfRfc.getCiId());
					
					setCiRelationId(escortRfcRelation);
					escortRfcRelation.setCreatedBy(userId);
					escortRfcRelation.setUpdatedBy(userId);
					CmsRfcRelation newEscortRfcRelation = cmRfcMrgProcessor.upsertRelationRfc(escortRfcRelation, userId);
					logger.debug("new relation rfc id = " + newEscortRfcRelation.getRfcId());
					
					if (manifestFromRfc.getRfcId() == 0 && manifestAttachfRfc.getRfcId() > 0) {
						cmRfcMrgProcessor.createDummyUpdateRfc(manifestFromRfc.getCiId(), null, 0, userId);
					}
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
	
	
	private MergeResult procesEdges(Map<String, Edge> edges, CmsRfcCI newRootRfc, String plstNsPath, String envNsPath, String designPlatName, String userId) {
		
		MergeResult mrgMaps = new MergeResult();
		
		for (Edge edge : edges.values()) {
			if (edge.userRels.size()>0) {
				CmsCI templLeafCi = (edge.templateRel != null) ? edge.templateRel.getToCi() : null;  
				List<Long> manifestCiIds = new ArrayList<Long>();
				for (CmsCIRelation userRel : edge.userRels) {
					CmsRfcCI leafRfc = trUtil.mergeCis(templLeafCi, userRel.getToCi(), "manifest", plstNsPath, envNsPath);
					setCiId(leafRfc);//, templLeafCi.getCiName());
					leafRfc.setCreatedBy(userId);
					leafRfc.setUpdatedBy(userId); 
					CmsRfcCI newLeafRfc = cmRfcMrgProcessor.upsertCiRfc(leafRfc, userId);
					logger.debug("new ci rfc id = " + newLeafRfc.getRfcId());
					manifestCiIds.add(newLeafRfc.getCiId());
					
					CmsRfcRelation leafRfcRelation = mergeRelations(edge.templateRel,userRel, plstNsPath, envNsPath);
					if (newRootRfc.getRfcId() > 0) leafRfcRelation.setFromRfcId(newRootRfc.getRfcId());
					leafRfcRelation.setFromCiId(newRootRfc.getCiId());
					
					if (newLeafRfc.getRfcId() > 0) leafRfcRelation.setToRfcId(newLeafRfc.getRfcId());
					leafRfcRelation.setToCiId(newLeafRfc.getCiId());
					
					setCiRelationId(leafRfcRelation);
					leafRfcRelation.setCreatedBy(userId);
					leafRfcRelation.setUpdatedBy(userId);
					CmsRfcRelation newLeafRfcRelation = cmRfcMrgProcessor.upsertRelationRfc(leafRfcRelation, userId);
					logger.debug("Cretated new relation rfc - " + newLeafRfcRelation.getRfcId());
					
					List<Long> manifestCiId = new ArrayList<Long>();
					manifestCiId.add(newLeafRfc.getCiId());
					mrgMaps.designIdsMap.put(userRel.getToCiId(),manifestCiId);
				}
				if (templLeafCi != null)  mrgMaps.templateIdsMap.put(templLeafCi.getCiId(), manifestCiIds);
				
			} else {
					String cardinality = edge.templateRel.getAttribute("constraint").getDfValue();
					if ("1..1".equalsIgnoreCase(cardinality) ||
						"1..*".equalsIgnoreCase(cardinality)) {
						List<Long> manifestCiIds = new ArrayList<Long>();
						CmsRfcCI leafRfc = trUtil.mergeCis(edge.templateRel.getToCi(), null, "manifest", plstNsPath, envNsPath);
						//leafRfc.setCiName(designPlatName + "-" + leafRfc.getCiName());
						setCiId(leafRfc);
						leafRfc.setCreatedBy(userId);
						leafRfc.setUpdatedBy(userId);
						//hack here for keypairs
						processSshKeys(leafRfc);
						CmsRfcCI newLeafRfc = cmRfcMrgProcessor.upsertCiRfc(leafRfc, userId);
						logger.debug("new ci rfc id = " + newLeafRfc.getRfcId());
						manifestCiIds.add(newLeafRfc.getCiId());
						
						CmsRfcRelation leafRfcRelation = mergeRelations(edge.templateRel,null, plstNsPath, envNsPath);
						if (newRootRfc.getRfcId()>0) leafRfcRelation.setFromRfcId(newRootRfc.getRfcId());
						leafRfcRelation.setFromCiId(newRootRfc.getCiId());
						if (newLeafRfc.getRfcId() > 0 ) leafRfcRelation.setToRfcId(newLeafRfc.getRfcId());
						leafRfcRelation.setToCiId(newLeafRfc.getCiId());
						setCiRelationId(leafRfcRelation);
						leafRfcRelation.setCreatedBy(userId);
						leafRfcRelation.setUpdatedBy(userId);
						CmsRfcRelation newLeafRfcRelation = cmRfcMrgProcessor.upsertRelationRfc(leafRfcRelation, userId);
						logger.debug("Cretated new relation rfc - " + newLeafRfcRelation.getRfcId());

						mrgMaps.templateIdsMap.put(edge.templateRel.getToCi().getCiId(), manifestCiIds);
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
	    setCiRelationId(newRfc);
		return newRfc;
		
	}

	public CmsRfcRelation bootstrapRelationRfcWithAttrs(long fromCiId, long toCiId, String relName, String nsPath, String releaseNsPath, Set<String> attrs) {
	    CmsRfcRelation newRfc = trUtil.bootstrapRelationRfc(fromCiId, toCiId, relName, nsPath, releaseNsPath, attrs);
	    setCiRelationId(newRfc);
		return newRfc;
		
	}
	
	/*
	private void applyRelationToRfc(CmsRfcRelation newRfc, CmsCIRelation ciRel, Map<String, CmsRelationAttribute> mdAttrs, boolean checkExpression, boolean setOwner) {

		if (ciRel != null) {
	    	newRfc.setComments(ciRel.getComments());
	    	Map<String,String> expressions = new HashMap<String,String>();
	    	for (CmsCIRelationAttribute mgmtAttr : ciRel.getAttributes().values()) {
	    		if (mdAttrs.containsKey(mgmtAttr.getAttributeName())) {
			    	if (mgmtAttr.getDjValue() != null && checkExpression) {
			    		//TODO process expression
			    		expressions.put(mgmtAttr.getAttributeName(), mgmtAttr.getDjValue());
			    	} 
			    	if (mgmtAttr.getDfValue() != null) {
			    		if (newRfc.getAttribute(mgmtAttr.getAttributeName()) != null) {
			    			newRfc.getAttribute(mgmtAttr.getAttributeName()).setNewValue(mgmtAttr.getDfValue());
			    			newRfc.getAttribute(mgmtAttr.getAttributeName()).setComments(mgmtAttr.getComments());
			    			if (setOwner) newRfc.getAttribute(mgmtAttr.getAttributeName()).setOwner("manifest");
			    			
			    		} else {
				    		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
				    		rfcAttr.setAttributeId(mgmtAttr.getAttributeId());
				    		rfcAttr.setAttributeName(mgmtAttr.getAttributeName());
				    		rfcAttr.setNewValue(mgmtAttr.getDfValue());
				    		if (setOwner) rfcAttr.setOwner("manifest");
				    		newRfc.addAttribute(rfcAttr);
			    		}
			    	}
	    		}
		    }
	    	if (expressions.size() > 0 ) {
	    		expEval.processExpressions(expressions, newRfc);
	    	}
	    }
	}
	*/
	private void setCiId(CmsRfcCI rfc) {
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
	
	private void setCiRelationId(CmsRfcRelation rfc) {
		List<CmsRfcRelation> existingRfcs = rfcProcessor.getOpenRfcRelationBy2(rfc.getFromCiId(), rfc.getToCiId(), rfc.getRelationName(), null);
		List<CmsCIRelation> existingRels = cmProcessor.getFromToCIRelations(rfc.getFromCiId(),rfc.getRelationName(), rfc.getToCiId());

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
	
	
	private class Edge {
		CmsCIRelation templateRel;
		List<CmsCIRelation> userRels = new ArrayList<CmsCIRelation>(); 
	}
	
	private class MergeResult {
		Map<Long, List<Long>> templateIdsMap = new HashMap<Long, List<Long>>();
		Map<Long, List<Long>> designIdsMap = new HashMap<Long, List<Long>>();
	}
}
