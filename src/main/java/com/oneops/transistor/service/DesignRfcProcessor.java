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

import com.oneops.cms.dj.service.CmsRfcProcessor;
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
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.exceptions.TransistorException;

public class DesignRfcProcessor {

	static Logger logger = Logger.getLogger(DesignRfcProcessor.class);
	
	private CmsMdProcessor mdProcessor;
	private TransUtil trUtil;
	private CmsCmProcessor cmProcessor;
	private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
    private CmsRfcProcessor rfcProcessor;


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
		
		
		CmsCI assembly = null;
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

	public long cloneAssembly(CmsCI targetAssembly, long fromAssemblyId, String userId, String scope) {

		CmsCI fromOrg = trUtil.getOrgByScope(scope);
		
		if (fromOrg == null) {
			String errMsg = "Can not find org by the scope = " + scope + ";"; 
			logger.error(errMsg);
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_ORG_BY_SCOPE, errMsg);
		}
		
		CmsCI toOrg = null;
		if (targetAssembly.getNsPath() == null) {
			String assemblyNS = "/" + fromOrg.getCiName();
			targetAssembly.setNsPath(assemblyNS);
			toOrg = fromOrg;
		} else {
			toOrg = trUtil.getOrgByScope(targetAssembly.getNsPath());
			if (toOrg == null) {
				String errMsg = "Can not find org by the nsPath = " + targetAssembly.getNsPath() + ";"; 
				logger.error(errMsg);
				throw new TransistorException(CmsError.TRANSISTOR_CANNOT_ORG_BY_SCOPE, errMsg);
			}
		}
		
		CmsCI fromAssembly = cmProcessor.getCiById(fromAssemblyId);
		if (targetAssembly.getAttribute("description").getDfValue() == null) {
			String desc = "Created from " + fromAssembly.getCiName();
			targetAssembly.getAttribute("description").setDfValue(desc);
			targetAssembly.getAttribute("description").setDjValue(desc);
		}
		CmsCI assembly = cmProcessor.createCI(targetAssembly);
		// create org -> base.Manages -> assembly rel
		CmsCIRelation manages = trUtil.bootstrapRelation(toOrg.getCiId(), assembly.getCiId(), "base.Manages", targetAssembly.getNsPath());
		cmProcessor.createRelation(manages);
		Map<Long,Long> source2design = new HashMap<Long,Long>();
		List<CmsRfcRelation> composedOfs = cmRfcMrgProcessor.getFromCIRelations(fromAssemblyId, "base.ComposedOf", "catalog.Platform", "dj");
		for (CmsRfcRelation composedOf : composedOfs) {
			long sourcePlatId = composedOf.getToCiId();
			long designPlatId = clonePlatform(null, assembly.getCiId(),sourcePlatId,userId, assembly.getNsPath());
			source2design.put(sourcePlatId, designPlatId);
		}
		//process linksTo relations
		String platsNS = assembly.getNsPath() + "/" + assembly.getCiName();
		processAssemblyInternalRels(source2design, platsNS, userId);
		return assembly.getCiId();
	}
	
	private void processAssemblyInternalRels(Map<Long,Long> source2design, String nsPath, String userId) {
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
		Map<Long,Long> source2design = new HashMap<Long,Long>();
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
		Map<Long,Long> tmpl2design = new HashMap<Long,Long>();
		for (CmsCIRelation tmplReqRel : tmplRequires) {
			
			CmsCI tmplComponent = tmplReqRel.getToCi();
			CmsRfcCI designComponent = popRfcCiFromTemplate(tmplComponent, "catalog", platNsPath, releaseNsPath);
			designComponent.setReleaseId(designPlatformRfc.getReleaseId());
			designComponent.setCreatedBy(userId);
			designComponent.setUpdatedBy(userId);
			CmsRfcCI designComponentRfc = cmRfcMrgProcessor.upsertCiRfc(designComponent, userId);
			tmpl2design.put(tmplComponent.getCiId(), designComponentRfc.getCiId());

			//we can create a design requires rel now
			CmsRfcRelation designReqRel = popRfcRelFromTemplate(tmplReqRel, "base", platNsPath, releaseNsPath);
			designReqRel.setFromCiId(designPlatformRfc.getCiId());
			designReqRel.setFromRfcId(designPlatformRfc.getRfcId());
			designReqRel.setToCiId(designComponentRfc.getCiId());
			designReqRel.setToRfcId(designComponentRfc.getRfcId());
			designReqRel.setReleaseId(designPlatformRfc.getReleaseId());
			designReqRel.setCreatedBy(userId);
			designReqRel.setUpdatedBy(userId);
			
			cmRfcMrgProcessor.upsertRelationRfc(designReqRel, userId);
		}
		
		// now lets process internal tmpl relations
		processTmplInternalRels(tmpl2design, designPlatformRfc.getReleaseId(), platNsPath, releaseNsPath, userId);
	}
	
	private void processTmplInternalRels(Map<Long,Long> tmpl2design, long releaseId, String platNsPath, String releaseNsPath, String userId) {
		List<CmsCIRelation> tmplIntenralRels = getInternalTmplRelations(tmpl2design.keySet());
		for (CmsCIRelation tmplIntenralRel : tmplIntenralRels) {
			if (tmpl2design.containsKey(tmplIntenralRel.getToCiId()) && !CmsConstants.CI_STATE_PENDING_DELETION.equals(tmplIntenralRel.getRelationState())) {
				CmsRfcRelation designInternalRel = popRfcRelFromTemplate(tmplIntenralRel, "catalog", platNsPath, releaseNsPath);
				
				designInternalRel.setFromCiId(tmpl2design.get(tmplIntenralRel.getFromCiId()));
				designInternalRel.setToCiId(tmpl2design.get(tmplIntenralRel.getToCiId()));
				designInternalRel.setReleaseId(releaseId);
				designInternalRel.setCreatedBy(userId);
				designInternalRel.setUpdatedBy(userId);
	
				cmRfcMrgProcessor.upsertRelationRfc(designInternalRel, userId);
			}
		}
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
		List<CmsCIRelation> requiresList = new ArrayList<CmsCIRelation>();
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
		List<CmsCIRelation> internalList = new ArrayList<CmsCIRelation>();

		for (Long tmplId : tmplIds) {
			List<CmsCIRelation> tmplInternals = cmProcessor.getFromCIRelationsNaked(tmplId, null, null);
			internalList.addAll(tmplInternals);
		}	
		return internalList;
	}

	private List<CmsRfcRelation> getInternalPlatRelations(Set<Long> componentIds) {
		List<CmsRfcRelation> internalList = new ArrayList<CmsRfcRelation>();

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
		Map<String, CmsClazzAttribute> clazzAttrs = new HashMap<String, CmsClazzAttribute>();
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
        rfcProcessor.rmRfcs(getPlatformNs(platformRfc));
        if (platformRfc.getIsActiveInRelease()){
            rfcProcessor.rmRfcCiFromRelease(platformRfc.getRfcId());
        }
        return platId;
    }


    public long commitReleaseForPlatform(long platId, String desc, String userId) {
        CmsRfcCI platformRfc= cmRfcMrgProcessor.getCiById(platId);
        return rfcProcessor.commitReleaseForPlatform(platformRfc, desc, userId);
    }
}
