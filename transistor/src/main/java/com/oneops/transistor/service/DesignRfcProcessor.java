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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.exceptions.DJException;

import org.apache.log4j.Logger;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAttribute;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.md.domain.CmsRelationAttribute;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsRfcRelationSimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.transistor.exceptions.TransistorException;

public class DesignRfcProcessor {

	static Logger logger = Logger.getLogger(DesignRfcProcessor.class);
	
	private CmsMdProcessor mdProcessor;
	private TransUtil trUtil;
	private CmsCmProcessor cmProcessor;
	private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
    private CmsRfcProcessor rfcProcessor;
    private CmsUtil cmsUtil;

    private static final String ATTR_NAME_TEMPLATE = "template";
    private static final String MGMT_PREFIX = "mgmt.";


    public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
        this.rfcProcessor = rfcProcessor;
    }

    public void setTrUtil(TransUtil trUtil) {
		this.trUtil = trUtil;
	}
	
	public void setMdProcessor(CmsMdProcessor mdProcessor) {
		this.mdProcessor = mdProcessor;
	}

	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	public void setCmRfcMrgProcessor(CmsCmRfcMrgProcessor cmRfcMrgProcessor) {
		this.cmRfcMrgProcessor = cmRfcMrgProcessor;
	}
	
	
	public CmsRfcCI generatePlatFromTmpl(CmsRfcCI designPlatform, long assemblyId, String userId, String scope) {

		CmsCI assembly = cmProcessor.getCiById(assemblyId);
		designPlatform.setNsPath(assembly.getNsPath() + "/" + assembly.getCiName());
		
		trUtil.verifyScope(designPlatform, scope);
		
		throwExceptionIfAlreadyExists(designPlatform);

		String platNsPath = designPlatform.getNsPath() + "/_design/" + designPlatform.getCiName();
		trUtil.cleanAndCreatePlatformNS(platNsPath);


		String nsPrefix = "/public/" + designPlatform.getAttribute("source").getNewValue()
				+ "/packs/" + designPlatform.getAttribute("pack").getNewValue();
        List<CmsCI> versions = cmProcessor.getCiBy3(nsPrefix, "mgmt.Version", designPlatform.getAttribute("version").getNewValue());
        for (CmsCI version : versions) {
            designPlatform.getAttribute("pack_digest").setNewValue(version.getAttribute("commit").getDfValue());
        }


		String mgmtTemplNsPath = nsPrefix
				+ "/" + designPlatform.getAttribute("version").getNewValue();
		
		List<CmsCI> templatePlatforms = cmProcessor.getCiBy3(mgmtTemplNsPath, "mgmt.catalog.Platform", null);
		if (templatePlatforms.size()==0) {
			String error =  "Can not find coresponding mgmt platform object" + mgmtTemplNsPath;
			logger.error(error);
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_CORRESPONDING_OBJECT,
                    error);
		}
		CmsCI templatePlatform = templatePlatforms.get(0);

		CmsRfcCI designPlatformRfc = cmRfcMrgProcessor.upsertCiRfc(designPlatform, userId);
		
		//lets process required components
		List<CmsCIRelation> tmplRequires = getRequiredTmplComponents(templatePlatform);
		processTmplPlatformComponents(designPlatformRfc, tmplRequires, platNsPath, designPlatform.getNsPath(), userId);
	
		//create relation assebly -> ComposedOf -> platform
		CmsRfcRelation composedOf = trUtil.bootstrapRelationRfc(assemblyId, designPlatformRfc.getCiId(), "base.ComposedOf", designPlatform.getNsPath(), designPlatform.getNsPath(), null);
		cmRfcMrgProcessor.upsertRelationRfc(composedOf, userId);
		processLocalVars(templatePlatform.getCiId(), designPlatformRfc.getCiId(), platNsPath, designPlatform.getNsPath(), userId);
		return designPlatformRfc;
	}

	private void throwExceptionIfAlreadyExists(CmsRfcCI designPlatform) {
		List<CmsRfcCI> existingPlat = cmRfcMrgProcessor.getDfDjCiNakedLower(designPlatform.getNsPath(), designPlatform.getCiClassName(), designPlatform.getCiName(), null);
        if (existingPlat.size()>0) {
			CmsRfcCI existingCi = existingPlat.get(0);
            String errMsg = "the ci of this ns/class/ci-name already exists " + existingCi.getNsPath() + ';' + existingCi.getCiClassName() + ';' + existingCi.getCiName();
            logger.error(errMsg);
            throw new TransistorException(CmsError.CMS_CI_OF_NS_CLASS_NAME_ALREADY_EXIST_ERROR, errMsg);
        }
	}

	public long deletePlatform(long designPlatformId, String userId, String scope) {

		CmsRfcCI designPlatform = cmRfcMrgProcessor.getCiById(designPlatformId, null);
		if (designPlatform == null) {
			logger.error("There is no platform with id = " + designPlatformId);
			return 0;
		}
		
		trUtil.verifyScope(designPlatform, scope);
		String platNsPath = designPlatform.getNsPath() + "/_design/" + designPlatform.getCiName();
		
		List<CmsRfcCI> platComponents = cmRfcMrgProcessor.getDfDjCi(platNsPath, null, null, "dj");
		for (CmsRfcCI component : platComponents) {
			cmRfcMrgProcessor.requestCiDeleteCascadeNoRelsRfcs(component.getCiId(), userId, 0);
		}
		cmRfcMrgProcessor.requestCiDeleteCascadeNoRelsRfcs(designPlatformId, userId, 0);
		return designPlatform.getCiId();
	}

	
	private void processLocalVars(long templatePlatId, long designPlatId, String platNsPath, String releaseNsPath, String userId) {
		List<CmsCIRelation> lvTemplateRels = cmProcessor.getToCIRelations(templatePlatId, "mgmt.catalog.ValueFor", null, "mgmt.catalog.Localvar");

		for (CmsCIRelation lvTemplateRel : lvTemplateRels) {
			CmsCI templateVar = lvTemplateRel.getFromCi();
			CmsRfcCI designVarRfc = trUtil.mergeCis(null, templateVar, "catalog", platNsPath, releaseNsPath);
			//setCiId(manifestVarRfc, manifestVarRfc.getCiName());
			designVarRfc.setCreatedBy(userId);
			designVarRfc.setUpdatedBy(userId);
			designVarRfc = cmRfcMrgProcessor.upsertCiRfc(designVarRfc, userId);

			CmsRfcRelation varToPlat = trUtil.bootstrapRelationRfc(designVarRfc.getCiId(), designPlatId, "catalog.ValueFor", platNsPath, releaseNsPath, null); 
			varToPlat.setFromRfcId(designVarRfc.getRfcId());
			varToPlat.setCreatedBy(userId);
			varToPlat.setUpdatedBy(userId);
			cmRfcMrgProcessor.upsertRelationRfc(varToPlat, userId);
		}
		
	}	
	
	public long clonePlatform(CmsRfcCI designPlatformRequest, Long targetAssemblyId, long sourcePlatId, String userId, String scope) {
		
		
		CmsCI assembly;
		if (targetAssemblyId != null) {
			assembly = cmProcessor.getCiById(targetAssemblyId);
		} else {
			assembly = trUtil.getAssemblyByPlatformNsPath(designPlatformRequest.getNsPath()); 
		}
		
		if (assembly == null) {
			String errMsg = "Can not find assembly by the nsPath = " + designPlatformRequest.getNsPath() + ";"; 
			logger.error(errMsg);
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_FIND_ASSEMBLY, errMsg);
		}

		
		CmsRfcCI sourcePlatform = cmRfcMrgProcessor.getCiById(sourcePlatId, "df");

		CmsRfcCI designPlatform = trUtil.cloneRfcCIBasic(sourcePlatform);
		
		designPlatform.setNsPath(assembly.getNsPath() + "/" + assembly.getCiName());
		designPlatform.setReleaseNsPath(assembly.getNsPath() + "/" + assembly.getCiName());
		designPlatform.getAttribute("major_version").setNewValue("1");
		
		if (designPlatformRequest != null) {
			designPlatform.setCiName(designPlatformRequest.getCiName());
			if (designPlatformRequest.getAttribute("description") != null) {
				designPlatform.getAttribute("description").setNewValue(designPlatformRequest.getAttribute("description").getNewValue());
			}
		}
		trUtil.verifyScope(designPlatform, scope);
		
		
		throwExceptionIfAlreadyExists(designPlatform);

		
		String platNsPath = designPlatform.getNsPath() + "/_design/" + designPlatform.getCiName();
		trUtil.verifyAndCreateNS(platNsPath);

		CmsRfcCI designPlatformRfc = cmRfcMrgProcessor.upsertCiRfc(designPlatform, userId);
		
		//lets process required components
		List<CmsRfcRelation> sourceRequires = cmRfcMrgProcessor.getFromCIRelations(sourcePlatId, "base.Requires", null, null); 
		clonePlatformComponents(designPlatformRfc, sourceRequires, platNsPath, designPlatform.getNsPath(), userId);
		
		//Process platform local vars
		List<CmsRfcRelation> sourceValueFor = cmRfcMrgProcessor.getToCIRelations(sourcePlatId, "catalog.ValueFor", null, null);
		clonePlatformLocalVars(designPlatformRfc, sourceValueFor, platNsPath, designPlatform.getNsPath(),userId);
		
		//create relation assebly -> ComposedOf -> platform
		CmsRfcRelation composedOf = trUtil.bootstrapRelationRfc(assembly.getCiId(), designPlatformRfc.getCiId(), "base.ComposedOf", designPlatform.getNsPath(), designPlatform.getNsPath(), null);
		cmRfcMrgProcessor.upsertRelationRfc(composedOf, userId);
		return designPlatformRfc.getCiId();
	}

	/**
	 * OneOps assembly `copy` implementation. The following steps are
	 * involved in the assembly copy action.
	 * <p>
	 * 1. Set target assembly description if from description is empty or null.
	 * 2. Create the target assembly ci.
	 * 3. Set org relation (org -> base.Manages -> assembly) to the newly created assembly
	 * 4. Copy all the platform (assembly -> base.ComposedOf -> catalog.Platform) from `from` assembly.
	 * 5. Copy all the global (Global var -> base.ValueFor -> account.Assembly) variables.
	 * 6. Restore LinksTo (platform -> catalog.LinksTo -> catalog.Platform) relation between platforms.
	 *
	 * @param targetAssembly target assembly ci to be created
	 * @param fromAssemblyId assembly ciid from where it to be copied.
	 * @param userId         id of the user performing this action.
	 * @param scope          scope is the nspath of the assembly.
	 * @return cid of the created assembly.
	 */
	public long cloneAssembly(CmsCI targetAssembly, long fromAssemblyId, String userId, String scope) {

		CmsCI fromOrg = trUtil.getOrgByScope(scope);
		if (fromOrg == null) {
			String errMsg = "Can not find org by the scope = " + scope;
			logger.error(errMsg);
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_ORG_BY_SCOPE, errMsg);
		}

		CmsCI toOrg;
		if (targetAssembly.getNsPath() == null) {
			String assemblyNS = "/" + fromOrg.getCiName();
			targetAssembly.setNsPath(assemblyNS);
			toOrg = fromOrg;
		} else {
			toOrg = trUtil.getOrgByScope(targetAssembly.getNsPath());
			if (toOrg == null) {
				String errMsg = "Can not find org by the nsPath = " + targetAssembly.getNsPath();
				logger.error(errMsg);
				throw new TransistorException(CmsError.TRANSISTOR_CANNOT_ORG_BY_SCOPE, errMsg);
			}
		}

		CmsCI fromAssembly = cmProcessor.getCiById(fromAssemblyId);
		CmsCIAttribute newDesc = targetAssembly.getAttribute("description");
		if (newDesc.getDfValue() == null || newDesc.getDfValue().trim().isEmpty()) {
			String desc = "Created from " + fromAssembly.getCiName();
			newDesc.setDfValue(desc);
			newDesc.setDjValue(desc);
		}

		// Creates the relation to org.
		CmsCI assembly = cmProcessor.createCI(targetAssembly);
		CmsCIRelation manages = trUtil.bootstrapRelation(toOrg.getCiId(), assembly.getCiId(), "base.Manages", targetAssembly.getNsPath());
		cmProcessor.createRelation(manages);

		// Clone all the platforms
		Map<Long, Long> source2design = new HashMap<>();
		List<CmsRfcRelation> composedOfs = cmRfcMrgProcessor.getFromCIRelations(fromAssemblyId, "base.ComposedOf", "catalog.Platform", "dj");
		for (CmsRfcRelation composedOf : composedOfs) {
			long sourcePlatId = composedOf.getToCiId();
			long designPlatId = clonePlatform(null, assembly.getCiId(), sourcePlatId, userId, assembly.getNsPath());
			source2design.put(sourcePlatId, designPlatId);
		}

		// Process  global vars
		String platsNS = assembly.getNsPath() + "/" + assembly.getCiName();
		cloneGlobalVars(fromAssemblyId, assembly.getCiId(), platsNS, userId);

		// Process linksTo relations
		processAssemblyInternalRels(source2design, platsNS, userId);
		return assembly.getCiId();
	}

	/**
	 * Clone the global variables into target assembly with given id.
	 *
	 * @param fromAssemblyId   assembly id from which global vars to be copied.
	 * @param targetAssemblyId target assembly cid to copy variables.
	 * @param nsPath           assembly nspath.
	 * @param userId           id of the user performing this action.
	 */
	private void cloneGlobalVars(long fromAssemblyId, long targetAssemblyId, String nsPath, String userId) {
		List<CmsRfcRelation> sourceValueFor = cmRfcMrgProcessor.getToCIRelations(fromAssemblyId, "base.ValueFor", null, null);
		for (CmsRfcRelation srcRel : sourceValueFor) {
			CmsRfcCI srcVar = srcRel.getFromRfcCi();

			CmsRfcCI designVar = trUtil.cloneRfcCIBasic(srcVar);
			designVar.setNsPath(nsPath);
			designVar.setReleaseNsPath(nsPath);
			designVar.setCreatedBy(userId);
			designVar.setUpdatedBy(userId);
			CmsRfcCI designVarRfc = cmRfcMrgProcessor.upsertCiRfc(designVar, userId);

			CmsRfcRelation designValForRel = trUtil.cloneRfcRelationBasic(srcRel);
			designValForRel.setToCiId(targetAssemblyId);
			designValForRel.setFromCiId(designVarRfc.getCiId());
			designValForRel.setNsPath(nsPath);
			designValForRel.setReleaseNsPath(nsPath);
			designValForRel.setCreatedBy(userId);
			designValForRel.setUpdatedBy(userId);
			cmRfcMrgProcessor.upsertRelationRfc(designValForRel, userId);
		}
	}

	private void processAssemblyInternalRels(Map<Long, Long> source2design, String nsPath, String userId) {
		for (Long sourcePlatId : source2design.keySet()) {
			List<CmsRfcRelation> linkesTos = cmRfcMrgProcessor.getFromCIRelations(sourcePlatId, "catalog.LinksTo", "catalog.Platform", "dj");
			for (CmsRfcRelation linkedTo : linkesTos) {
				CmsRfcRelation designInternalRel = trUtil.cloneRfcRelationBasic(linkedTo);

				designInternalRel.setFromCiId(source2design.get(linkedTo.getFromCiId()));
				designInternalRel.setToCiId(source2design.get(linkedTo.getToCiId()));
				designInternalRel.setNsPath(nsPath);
				designInternalRel.setReleaseNsPath(nsPath);
				designInternalRel.setCreatedBy(userId);
				designInternalRel.setUpdatedBy(userId);
				cmRfcMrgProcessor.upsertRelationRfc(designInternalRel, userId);
			}
		}
	}


	/*
	private void populatePackInfo(CmsRfcCI designPlatformRfc, CmsRfcCI sourcePlatform) {
		designPlatformRfc.getAttribute("version").setNewValue(sourcePlatform.getAttribute("version").getNewValue());
		designPlatformRfc.getAttribute("pack").setNewValue(sourcePlatform.getAttribute("pack").getNewValue());
		designPlatformRfc.getAttribute("source").setNewValue(sourcePlatform.getAttribute("source").getNewValue());
		designPlatformRfc.getAttribute("major_version").setNewValue("1");
	}
	*/
	
	private void clonePlatformComponents(CmsRfcCI designPlatformRfc, List<CmsRfcRelation> sourceRequires, String platNsPath, String releaseNsPath, String userId) {
		//1st lets create all the components and have a map of new Design IDs to tmpl IDs
		Map<Long,Long> source2design = new HashMap<>();
		for (CmsRfcRelation sourceReqRel : sourceRequires) {
			
			CmsRfcCI sourceComponent = sourceReqRel.getToRfcCi();
			CmsRfcCI designComponent = trUtil.cloneRfcCIBasic(sourceComponent);
			
			designComponent.setNsPath(platNsPath);
			designComponent.setReleaseNsPath(releaseNsPath);
			designComponent.setReleaseId(designPlatformRfc.getReleaseId());
			designComponent.setCreatedBy(userId);
			designComponent.setUpdatedBy(userId);
			CmsRfcCI designComponentRfc = cmRfcMrgProcessor.upsertCiRfc(designComponent, userId);

			source2design.put(sourceComponent.getCiId(), designComponentRfc.getCiId());
			
			//we can create a design requires rel now
			CmsRfcRelation designReqRel = trUtil.cloneRfcRelationBasic(sourceReqRel);
			designReqRel.setFromCiId(designPlatformRfc.getCiId());
			designReqRel.setFromRfcId(designPlatformRfc.getRfcId());
			designReqRel.setToCiId(designComponentRfc.getCiId());
			designReqRel.setToRfcId(designComponentRfc.getRfcId());
			designReqRel.setNsPath(platNsPath);
			designReqRel.setReleaseNsPath(releaseNsPath);
			designReqRel.setReleaseId(designPlatformRfc.getReleaseId());
			designReqRel.setCreatedBy(userId);
			designReqRel.setUpdatedBy(userId);
			
			cmRfcMrgProcessor.upsertRelationRfc(designReqRel, userId);
		}
		
		// now lets process internal tmpl relations
		clonePlatInternalRels(source2design, designPlatformRfc.getReleaseId(), platNsPath, releaseNsPath, userId);
	}

	private void clonePlatformLocalVars(CmsRfcCI designPlatformRfc, List<CmsRfcRelation> sourceValueFor, String platNsPath, String releaseNsPath, String userId) {

		for (CmsRfcRelation sourceValueForRel : sourceValueFor) {
			CmsRfcCI sourceVar = sourceValueForRel.getFromRfcCi();

			CmsRfcCI designVar = trUtil.cloneRfcCIBasic(sourceVar);
			designVar.setNsPath(platNsPath);
			designVar.setReleaseNsPath(releaseNsPath);
			designVar.setReleaseId(designPlatformRfc.getReleaseId());
			designVar.setCreatedBy(userId);
			designVar.setUpdatedBy(userId);
			CmsRfcCI designVarRfc = cmRfcMrgProcessor.upsertCiRfc(designVar, userId);

			CmsRfcRelation designValueForRel = trUtil.cloneRfcRelationBasic(sourceValueForRel);
			designValueForRel.setToCiId(designPlatformRfc.getCiId());
			designValueForRel.setToRfcId(designPlatformRfc.getRfcId());
			designValueForRel.setFromCiId(designVarRfc.getCiId());
			designValueForRel.setFromRfcId(designVarRfc.getRfcId());
			designValueForRel.setNsPath(platNsPath);
			designValueForRel.setReleaseNsPath(releaseNsPath);
			designValueForRel.setReleaseId(designPlatformRfc.getReleaseId());
			designValueForRel.setCreatedBy(userId);
			designValueForRel.setUpdatedBy(userId);
			cmRfcMrgProcessor.upsertRelationRfc(designValueForRel, userId);
		}

	}

	
	private void processTmplPlatformComponents(CmsRfcCI designPlatformRfc, List<CmsCIRelation> tmplRequires, String platNsPath, String releaseNsPath, String userId) {
		//1st lets create all the components and have a map of new Design IDs to tmpl IDs
		Map<Long, CmsRfcCI> tmpl2design = new HashMap<Long, CmsRfcCI>();
		for (CmsCIRelation tmplReqRel : tmplRequires) {
			
			CmsCI tmplComponent = tmplReqRel.getToCi();
			CmsRfcCI designComponent = popRfcCiFromTemplate(tmplComponent, "catalog", platNsPath, releaseNsPath);
			designComponent.setReleaseId(designPlatformRfc.getReleaseId());
			designComponent.setCreatedBy(userId);
			designComponent.setUpdatedBy(userId);
			CmsRfcCI designComponentRfc = cmRfcMrgProcessor.upsertCiRfc(designComponent, userId);
			tmpl2design.put(tmplComponent.getCiId(), designComponentRfc);

			//we can create a design requires rel now
			CmsRfcRelation designReqRel = popRfcRelFromTemplate(tmplReqRel, "base", platNsPath, releaseNsPath);
			setRfcRelationData(designReqRel, designPlatformRfc.getCiId(), designComponentRfc.getCiId(), designPlatformRfc.getReleaseId(), userId);
			designReqRel.setFromRfcId(designPlatformRfc.getRfcId());
			designReqRel.setToRfcId(designComponentRfc.getRfcId());
			
			cmRfcMrgProcessor.upsertRelationRfc(designReqRel, userId);
		}
		
		// now lets process internal tmpl relations
		processTmplInternalRels(tmpl2design, designPlatformRfc, platNsPath, releaseNsPath, userId);
	}
	
	private void processTmplInternalRels(Map<Long, CmsRfcCI> tmpl2design, CmsRfcCI designPlatformRfc, String platNsPath, String releaseNsPath, String userId) {
		List<CmsCIRelation> tmplIntenralRels = getInternalTmplRelations(tmpl2design.keySet());
		List<CmsCIRelation> tmplMonitorRels = new ArrayList<>();
		for (CmsCIRelation tmplIntenralRel : tmplIntenralRels) {
			if (!CmsConstants.CI_STATE_PENDING_DELETION.equals(tmplIntenralRel.getRelationState())) {
				if (tmpl2design.containsKey(tmplIntenralRel.getToCiId())) {
					CmsRfcRelation designInternalRel = popRfcRelFromTemplate(tmplIntenralRel, "catalog", platNsPath, releaseNsPath);
					setRfcRelationData(designInternalRel, tmpl2design.get(tmplIntenralRel.getFromCiId()).getCiId(), 
							tmpl2design.get(tmplIntenralRel.getToCiId()).getCiId(), designPlatformRfc.getReleaseId(), userId);
					cmRfcMrgProcessor.upsertRelationRfc(designInternalRel, userId);
				}
				else if (CmsConstants.MGMT_CATALOG_WATCHEDBY.equals(tmplIntenralRel.getRelationName())) {
					tmplMonitorRels.add(tmplIntenralRel);
				}
			}
		}
		processTmplMonitors(tmpl2design, tmplMonitorRels, designPlatformRfc, platNsPath, releaseNsPath, userId);
	}

	private void processTmplMonitors(Map<Long, CmsRfcCI> tmpl2design, List<CmsCIRelation> tmplMonitorRels, CmsRfcCI designPlatformRfc, 
			String platNsPath, String releaseNsPath, String userId) {
		List<Long> tmplMonitorCiIds = tmplMonitorRels.stream().map(CmsCIRelation::getToCiId).collect(Collectors.toList());
		List<CmsCI> tmplMonitorToCis = cmProcessor.getCiByIdList(tmplMonitorCiIds);
		if (tmplMonitorToCis != null) {
			Map<Long, CmsCI> tmplMonitorCiMap = tmplMonitorToCis.stream().collect(Collectors.toMap(CmsCI::getCiId, Function.identity()));
			tmplMonitorRels.stream().forEach(monitorRel -> {
				CmsCI tmplMonitorCi = tmplMonitorCiMap.get(monitorRel.getToCiId());
				processMonitor(monitorRel, tmplMonitorCi, tmpl2design.get(monitorRel.getFromCiId()),  designPlatformRfc.getCiName(),
						designPlatformRfc.getReleaseId(), platNsPath, releaseNsPath, userId);
			});
		}
	}

	private void processMonitor(CmsCIRelation tmplMonitorRel, CmsCI tmplMonitorCi, CmsRfcCI fromCi, String platformName, 
			long releaseId, String platNsPath, String releaseNsPath, String userId) {
		CmsRfcCI designMonitor = popRfcCiFromTemplate(tmplMonitorCi, "catalog", platNsPath, releaseNsPath);
		designMonitor.setReleaseId(releaseId);
		designMonitor.setCreatedBy(userId);
		designMonitor.setUpdatedBy(userId);
		String monCiName = getMonitorName(platformName, fromCi.getCiName(), tmplMonitorCi.getCiName());
		designMonitor.setCiName(monCiName);
		CmsRfcCI designComponentRfc = cmRfcMrgProcessor.upsertCiRfc(designMonitor, userId);

		CmsRfcRelation designWatchedByRel = popRfcRelFromTemplate(tmplMonitorRel, "catalog", platNsPath, releaseNsPath);
		//set the source attribute on the watched by relation to design
		CmsRfcAttribute sourceAttrbute = designWatchedByRel.getAttribute(CmsConstants.ATTR_NAME_SOURCE);
		sourceAttrbute.setNewValue(CmsConstants.ATTR_SOURCE_VALUE_DESIGN);
		sourceAttrbute.setOwner(CmsConstants.ATTR_OWNER_VALUE_DESIGN);

		setRfcRelationData(designWatchedByRel, fromCi.getCiId(), designComponentRfc.getCiId(), releaseId, userId);
		cmRfcMrgProcessor.upsertRelationRfc(designWatchedByRel, userId);
	}

	private String getMonitorName(String platformName, String componentName, String monitorName) {
		return platformName + "-" + componentName + "-" + monitorName;
	}

	private void setRfcRelationData(CmsRfcRelation designRel, long fromCiId, long toCiId, long releaseId, String userId) {
		designRel.setFromCiId(fromCiId);
		designRel.setToCiId(toCiId);
		designRel.setReleaseId(releaseId);
		designRel.setCreatedBy(userId);
		designRel.setUpdatedBy(userId);
	}

	private void clonePlatInternalRels(Map<Long,Long> source2design, long releaseId, String platNsPath, String releaseNsPath, String userId) {
		List<CmsRfcRelation> sourceIntenralRels = getInternalPlatRelations(source2design.keySet());
		
		for (CmsRfcRelation sourceIntenralRel : sourceIntenralRels) {
			if (!source2design.containsKey(sourceIntenralRel.getToCiId())) {
				//this is internal component that is not a "required" component
				CmsRfcCI sourceComponent = cmRfcMrgProcessor.getCiById(sourceIntenralRel.getToCiId(), "df");
				CmsRfcCI designComponent = trUtil.cloneRfcCIBasic(sourceComponent);
				
				designComponent.setNsPath(platNsPath);
				designComponent.setReleaseNsPath(releaseNsPath);
				designComponent.setReleaseId(releaseId);
				designComponent.setCreatedBy(userId);
				designComponent.setUpdatedBy(userId);
				CmsRfcCI designComponentRfc = cmRfcMrgProcessor.upsertCiRfc(designComponent, userId);

				source2design.put(sourceComponent.getCiId(), designComponentRfc.getCiId());
			}
			//now create a relation
			CmsRfcRelation designInternalRel = trUtil.cloneRfcRelationBasic(sourceIntenralRel); 
			
			designInternalRel.setFromCiId(source2design.get(sourceIntenralRel.getFromCiId()));
			designInternalRel.setToCiId(source2design.get(sourceIntenralRel.getToCiId()));
			designInternalRel.setNsPath(platNsPath);
			designInternalRel.setReleaseNsPath(releaseNsPath);
			
			designInternalRel.setReleaseId(releaseId);
			designInternalRel.setCreatedBy(userId);
			designInternalRel.setUpdatedBy(userId);

			cmRfcMrgProcessor.upsertRelationRfc(designInternalRel, userId);
		}
	}
	

	private List<CmsCIRelation> getRequiredTmplComponents(CmsCI templatePlatform) {
		List<CmsCIRelation> tmplRequires = cmProcessor.getFromCIRelationsNaked(templatePlatform.getCiId(), "mgmt.Requires", null);
		List<CmsCIRelation> requiresList = new ArrayList<>();
		for (CmsCIRelation rel : tmplRequires) {
			if (rel.getAttribute("constraint").getDfValue().matches("1..1|1..*")) {
				CmsCI component = cmProcessor.getCiById(rel.getToCiId());
				if (! CmsConstants.CI_STATE_PENDING_DELETION.equals(component.getCiState())) {
					rel.setToCi(component);
					requiresList.add(rel);
				}
			}
		}
		
		return requiresList;
	}

	
	private List<CmsCIRelation> getInternalTmplRelations(Set<Long> tmplIds) {
		List<CmsCIRelation> internalList = new ArrayList<>();

		for (Long tmplId : tmplIds) {
			List<CmsCIRelation> tmplInternals = cmProcessor.getFromCIRelationsNaked(tmplId, null, null);
			internalList.addAll(tmplInternals);
		}	
		return internalList;
	}

	private List<CmsRfcRelation> getInternalPlatRelations(Set<Long> componentIds) {
		List<CmsRfcRelation> internalList = new ArrayList<>();

		for (Long componentId : componentIds) {
			List<CmsRfcRelation> tmplInternals = cmRfcMrgProcessor.getFromCIRelationsNaked(componentId, null, null, null); 
			internalList.addAll(tmplInternals);
		}	
		return internalList;
	}
	
	
	public CmsRfcCI popRfcCiFromTemplate(CmsCI templCi, String targetClassPrefix, String nsPath, String releaseNsPath) {
		
		CmsRfcCI newRfc = new CmsRfcCI();
		newRfc.setNsPath(nsPath);
		newRfc.setReleaseNsPath(releaseNsPath);
		
		String	targetClazzName = targetClassPrefix +  "." + trUtil.getLongShortClazzName(templCi.getCiClassName());
		
		CmsClazz targetClazz = mdProcessor.getClazz(targetClazzName);
		
		newRfc.setCiClassId(targetClazz.getClassId());
		newRfc.setCiClassName(targetClazz.getClassName());
		
		//bootstrap the default values from Class definition and populate map for checks
		Map<String, CmsClazzAttribute> clazzAttrs = new HashMap<>();
	    for (CmsClazzAttribute clAttr : targetClazz.getMdAttributes()) {
	    	if (clAttr.getDefaultValue() != null) {
	    		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
	    		rfcAttr.setAttributeId(clAttr.getAttributeId());
	    		rfcAttr.setAttributeName(clAttr.getAttributeName());
	    		rfcAttr.setNewValue(clAttr.getDefaultValue());
	    		newRfc.addAttribute(rfcAttr);
	    	}
	    	clazzAttrs.put(clAttr.getAttributeName(), clAttr);
	    }
	    
	    //populate values from template ci
	    trUtil.applyCiToRfc(newRfc, templCi, clazzAttrs, false, true);
	    
		return newRfc;
	}
	
	public CmsRfcRelation popRfcRelFromTemplate(CmsCIRelation mgmtCiRelation, String relPrefix, String nsPath, String releaseNsPath) {
		
		CmsRfcRelation newRfc = new CmsRfcRelation();
		newRfc.setNsPath(nsPath);
		newRfc.setReleaseNsPath(releaseNsPath);
		
		String targetRelationName = relPrefix + "." + trUtil.getLongShortClazzName(mgmtCiRelation.getRelationName());
		CmsRelation targetRelation = mdProcessor.getRelation(targetRelationName);
		
		newRfc.setRelationId(targetRelation.getRelationId());
		newRfc.setRelationName(targetRelation.getRelationName());
		
		//bootstrap the default values from Class definition
		Map<String, CmsRelationAttribute> relAttrs = new HashMap<>();
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
	    
	    //populate values from template obj
	    trUtil.applyRelationToRfc(newRfc, mgmtCiRelation, relAttrs, true, null);
	    
		return newRfc;
	}


    private static String getPlatformNs(CmsRfcCI platform) {
        return platform.getNsPath()+"/_design/"+platform.getCiName();
    }


    public Map<String, List<?>> getPlatformRfcs(long platId, String scope) {
		CmsRfcCI platform = cmRfcMrgProcessor.getCiById(platId);
		trUtil.verifyScope(platform, scope);

		String nsPath = getPlatformNs(platform);
		Map<String, List<?>> map = new HashMap<>();
		map.put("relations", rfcProcessor.getOpenRfcRelationsByNs(nsPath));
		map.put("cis", rfcProcessor.getOpenRfcCIByClazzAndName(nsPath, null, null));
        return map;
    }

    public long discardReleaseForPlatform(long platId) {
		CmsRfcCI platformRfc= cmRfcMrgProcessor.getCiById(platId);
        return rfcProcessor.discardReleaseForPlatform(platformRfc);
    }


    public long commitReleaseForPlatform(long platId, String desc, String userId) {
        CmsRfcCI platformRfc= cmRfcMrgProcessor.getCiById(platId);
        return rfcProcessor.commitReleaseForPlatform(platformRfc, desc, userId);
    }

    public CmsRfcRelationSimple createComponent(long platId, CmsRfcRelationSimple relSimple, String userId, String scope) {
        CmsRfcCI designPlatform = cmRfcMrgProcessor.getCiById(platId);
		trUtil.verifyScope(designPlatform, scope);
		validateForNewComponent(relSimple);

		CmsRfcRelation relationRfc = cmsUtil.custRfcRelSimple2RfcRel(relSimple);
		String[] attrProps = null;
		if (relSimple.getRelationAttrProps().size() >0) {
			attrProps = relSimple.getRelationAttrProps().keySet().toArray(new String[relSimple.getRelationAttrProps().size()]);
		}
		setRelationRfcCIFromRelSimple(relSimple, relationRfc, userId);
		throwExceptionIfAlreadyExists(relationRfc.getToRfcCi());

		CmsRfcRelation newRfcRelation = cmRfcMrgProcessor.upsertRelationRfc(relationRfc, userId);
		CmsRfcRelationSimple newRelSimple = cmsUtil.custRfcRel2RfcRelSimple(newRfcRelation,attrProps);
		if (newRfcRelation.getFromRfcCi() != null) {
			newRelSimple.setFromCi(cmsUtil.custRfcCI2RfcCISimple(newRfcRelation.getFromRfcCi(),attrProps));
		}

		if (newRfcRelation.getToRfcCi() != null) {
			newRelSimple.setToCi(cmsUtil.custRfcCI2RfcCISimple(newRfcRelation.getToRfcCi(), attrProps));
		}
		processOtherRelations(designPlatform, newRfcRelation, userId);
		return newRelSimple;
    }

    private void processOtherRelations(CmsRfcCI designPlatform, CmsRfcRelation newRfcRelation, String userId) {
        CmsRfcAttribute templateAttribute = newRfcRelation.getAttribute(ATTR_NAME_TEMPLATE);
        String mgmtTemplNsPath = "/public/" + designPlatform.getAttribute("source").getNewValue()
                + "/packs/" + designPlatform.getAttribute("pack").getNewValue()
                + "/" + designPlatform.getAttribute("version").getNewValue();
        String mgmtClassName =  newRfcRelation.getToRfcCi() != null ? MGMT_PREFIX + newRfcRelation.getToRfcCi().getCiClassName() : null;
        List<CmsCI> list = cmProcessor.getCiBy3(mgmtTemplNsPath, mgmtClassName, templateAttribute.getNewValue());
        if (list.isEmpty()) {
            throw new DJException(CmsError.DJ_BAD_TEMPLATE_NAME_ERROR, 
                    "no template ci found for the template name " + templateAttribute.getNewValue());
        }
        CmsCI templateCi = list.get(0);

        List<CmsCIRelation> tmplRelations = cmProcessor.getAllCIRelations(templateCi.getCiId());
        List<CmsCIRelation> tmplMonitorRelations = new ArrayList<>();
        List<CmsCIRelation> fromRelations = new ArrayList<>();
        List<CmsCIRelation> toRelations = new ArrayList<>();

        for (CmsCIRelation tmplRelation : tmplRelations) {
            if (!CmsConstants.CI_STATE_PENDING_DELETION.equals(tmplRelation.getRelationState())) {
                if (CmsConstants.MGMT_CATALOG_WATCHEDBY.equals(tmplRelation.getRelationName())) {
                    tmplMonitorRelations.add(tmplRelation);
                }
                else if (tmplRelation.getFromCiId() == templateCi.getCiId()) {
                    fromRelations.add(tmplRelation);
                }
                else if (!"mgmt.Requires".equals(tmplRelation.getRelationName())) {
                    toRelations.add(tmplRelation);
                }
            }
        }
        List<CmsCIRelation> designRequiresRelations = cmProcessor.getFromCIRelations(designPlatform.getCiId(), CmsConstants.BASE_REQUIRES, null);
        Map<String, List<CmsCIRelation>> tmplName2DesignRelations = designRequiresRelations.stream().
    	        collect(Collectors.groupingBy(relation -> relation.getAttribute(ATTR_NAME_TEMPLATE).getDfValue()));

        for (CmsCIRelation tmplRelation : fromRelations) {
            CmsCI tmplToCi = tmplRelation.getToCi();
            if (tmplName2DesignRelations.containsKey(tmplToCi.getCiName())) {
                for (CmsCIRelation designRelations : tmplName2DesignRelations.get(tmplToCi.getCiName())) {
                    CmsRfcRelation designInternalRel = popRfcRelFromTemplate(tmplRelation, "catalog", newRfcRelation.getNsPath(), null);
                    setRfcRelationData(designInternalRel, newRfcRelation.getToCiId(), designRelations.getToCi().getCiId(), newRfcRelation.getReleaseId(), userId);
                    cmRfcMrgProcessor.upsertRelationRfc(designInternalRel, userId);
                }
            }
        }

        for (CmsCIRelation tmplRelation : toRelations) {
            CmsCI tmplFromCi = tmplRelation.getFromCi();
            if (tmplName2DesignRelations.containsKey(tmplFromCi.getCiName())) {
                for (CmsCIRelation designRelations : tmplName2DesignRelations.get(tmplFromCi.getCiName())) {
                    CmsRfcRelation designInternalRel = popRfcRelFromTemplate(tmplRelation, "catalog", newRfcRelation.getNsPath(), null);
                    setRfcRelationData(designInternalRel, designRelations.getToCi().getCiId(), newRfcRelation.getToCiId(), newRfcRelation.getReleaseId(), userId);
                    cmRfcMrgProcessor.upsertRelationRfc(designInternalRel, userId);
                }
            }
        }
        for (CmsCIRelation tmplRelation : tmplMonitorRelations) {
            processMonitor(tmplRelation, tmplRelation.getToCi(), newRfcRelation.getToRfcCi(), designPlatform.getCiName(),
                    newRfcRelation.getReleaseId(), newRfcRelation.getNsPath(), null, userId);
        }
    }

    private void setRelationRfcCIFromRelSimple(CmsRfcRelationSimple relSimple, CmsRfcRelation rel, String userId) {
    	CmsRfcCISimple fromRfcCi = relSimple.getFromCi();
		if (fromRfcCi != null) rel.setFromRfcCi(cmsUtil.custRfcCISimple2RfcCI(fromRfcCi));
		CmsRfcCISimple toRfcCi = relSimple.getToCi();
		rel.setToRfcCi(cmsUtil.custRfcCISimple2RfcCI(toRfcCi));
		rel.setCreatedBy(userId);
		rel.setUpdatedBy(userId);
    }

    private void validateForNewComponent(CmsRfcRelationSimple relSimple) {
        String error = null;
        if (!CmsConstants.BASE_REQUIRES.equals(relSimple.getRelationName())) {
            error = "relation for new component should be base.Requires";
        }
        if (relSimple.getToCi() == null) {
            error = "the new component rfc is null";
        }
        if (relSimple.getRelationAttributes() == null || !relSimple.getRelationAttributes().containsKey(ATTR_NAME_TEMPLATE)) {
            error = "template attribute missing in base.Requires relation";
        }
        if (error != null)
            throw new DJException(CmsError.DJ_INVALID_COMPONENT_ERROR, error);
    }

	public void setCmsUtil(CmsUtil cmsUtil) {
		this.cmsUtil = cmsUtil;
	}

}
