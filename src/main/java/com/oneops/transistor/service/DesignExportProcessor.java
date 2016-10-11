package com.oneops.transistor.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.oneops.cms.cm.dal.CIMapper;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.transistor.exceptions.DesignExportException;
import com.oneops.transistor.export.domain.ComponentExport;
import com.oneops.transistor.export.domain.DesignExportSimple;
import com.oneops.transistor.export.domain.ExportCi;
import com.oneops.transistor.export.domain.PlatformExport;

public class DesignExportProcessor {
	
	private CmsCmProcessor cmProcessor;
	private DesignRfcProcessor designRfcProcessor;
	private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
	private CmsRfcProcessor rfcProcessor;
	private TransUtil trUtil;
	private CIMapper ciMapper;
	
	private static final String BAD_ASSEMBLY_ID_ERROR_MSG = "Assmbly does not exists with id=";
	private static final String OPEN_RELEASE_ERROR_MSG = "Design have open release. Please commit/discard before import.";
	private static final String BAD_TEMPLATE_ERROR_MSG = "Can not find template for pack: ";
	private static final String CANT_FIND_REQUIRES_ERROR_MSG = "Can not find Requires relation for ci id=";
	private static final String CANT_FIND_PLATFORM_BY_NAME_ERROR_MSG = "Can not find platform with name: $toPlaform, used in links of platform $fromPlatform";
	private static final String CANT_FIND_COMPONENT_BY_NAME_ERROR_MSG = "Can not find component with name: $toComponent, used in depends of component $fromComponent";
	private static final String IMPORT_ERROR_PLAT_COMP = "Platform/Component - ";
	private static final String IMPORT_ERROR_PLAT_COMP_ATTACH = "Platform/Component/Attachment - ";

	
	private static final String GLOBAL_VAR_RELATION = "base.ValueFor";
	private static final String LOCAL_VAR_RELATION = "catalog.ValueFor";
	private static final String COMPOSED_OF_RELATION = "base.ComposedOf";
	private static final String LINKS_TO_RELATION = "catalog.LinksTo";
	private static final String REQUIRES_RELATION = "base.Requires";
	private static final String MGMT_REQUIRES_RELATION = "mgmt.Requires";
	private static final String ESCORTED_RELATION = "catalog.EscortedBy";
	private static final String DEPENDS_ON_RELATION = "catalog.DependsOn";
	private static final String MGMT_DEPENDS_ON_RELATION = "mgmt.catalog.DependsOn";
	
	private static final String GLOBAL_VAR_CLASS = "catalog.Globalvar";
	private static final String LOCAL_VAR_CLASS = "catalog.Localvar";
	private static final String DESIGN_PLATFORM_CLASS = "catalog.Platform";
	private static final String DESIGN_ATTACHMENT_CLASS = "catalog.Attachment";
	private static final String MGMT_PREFIX = "mgmt.";
	private static final String OWNER_DESIGN = "design";
	private static final String ATTR_SECURE = "secure";
	private static final String ATTR_VALUE = "value";
	private static final String ATTR_ENC_VALUE = "encrypted_value";
	
	private static final String DUMMY_ENCRYPTED_IMP_VALUE = "CHANGE ME!!!";
	private static final String ENCRYPTED_PREFIX = "::ENCRYPTED::";
	private static final String DUMMY_ENCRYPTED_EXP_VALUE = ENCRYPTED_PREFIX;

	public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
		this.rfcProcessor = rfcProcessor;
	}
	
	public void setTrUtil(TransUtil trUtil) {
		this.trUtil = trUtil;
	}

	public void setCiMapper(CIMapper ciMapper) {
		this.ciMapper = ciMapper;
	}

	public void setCmRfcMrgProcessor(CmsCmRfcMrgProcessor cmRfcMrgProcessor) {
		this.cmRfcMrgProcessor = cmRfcMrgProcessor;
	}

	public void setDesignRfcProcessor(DesignRfcProcessor designRfcProcessor) {
		this.designRfcProcessor = designRfcProcessor;
	}

	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	public DesignExportSimple exportDesign(long assemblyId, Long[] platformIds, String scope) {
		CmsCI assembly = cmProcessor.getCiById(assemblyId);
		if (assembly == null) {
			throw new DesignExportException(DesignExportException.CMS_NO_CI_WITH_GIVEN_ID_ERROR, BAD_ASSEMBLY_ID_ERROR_MSG + assemblyId);
		}

		trUtil.verifyScope(assembly, scope);

		DesignExportSimple des = new DesignExportSimple();

		if (platformIds == null || platformIds.length == 0) {

			//get the global vars
			List<CmsCIRelation> globalVarRels = cmProcessor.getToCIRelations(assemblyId, GLOBAL_VAR_RELATION, GLOBAL_VAR_CLASS);

			for (CmsCIRelation gvRel : globalVarRels) {
				String[] var = checkVar4Export(gvRel.getFromCi(), false);
				if (var != null) {
					des.addVariable(var[0],var[1]);
				}
			}
		}

		//do platforms
		addPlatformsToExport(assemblyId, platformIds, des);
		
		return des;
	}
	
	private void addPlatformsToExport(long assemblyId, Long[] platformIds, DesignExportSimple des) {
		List<CmsCIRelation> composedOfs;
		if (platformIds == null || platformIds.length == 0) {
			composedOfs = cmProcessor.getFromCIRelations(assemblyId, COMPOSED_OF_RELATION, DESIGN_PLATFORM_CLASS);
		}
		else {
			composedOfs = cmProcessor.getFromCIRelationsByToCiIds(assemblyId, COMPOSED_OF_RELATION, null, Arrays.asList(platformIds));
		}

		for (CmsCIRelation composedOf : composedOfs) {
			CmsCI platform = composedOf.getToCi();
			
			//always export platform ci
			PlatformExport pe = stripAndSimplify(PlatformExport.class, platform, true); 
			//pe.setPlatform(stripAndSimplify(platform, true));
			
			//check for linksTo rels
			List<CmsCIRelation> linksTos = cmProcessor.getFromCIRelations(platform.getCiId(), LINKS_TO_RELATION, DESIGN_PLATFORM_CLASS);
			for (CmsCIRelation linksTo : linksTos) {
				String linksToPlatformName = linksTo.getToCi().getCiName();
				pe.addLink(linksToPlatformName);
			}
			
			//local vars
			List<CmsCIRelation> localVarRels = cmProcessor.getToCIRelations(platform.getCiId(), LOCAL_VAR_RELATION, LOCAL_VAR_CLASS);
			
			for (CmsCIRelation lvRel : localVarRels) {
				String[] var = checkVar4Export(lvRel.getFromCi(), true);
				if (var != null) {
					pe.addVariable(var[0],var[1]);
				}
			}
			
			//components
			addComponentsToPlatformExport(platform, pe);
			des.addPlatformExport(pe);
		}
	}

	private void addComponentsToPlatformExport(CmsCI platform, PlatformExport pe) {
		List<CmsCIRelation> requiresRels = cmProcessor.getFromCIRelations(platform.getCiId(), REQUIRES_RELATION, null);
		for (CmsCIRelation requires : requiresRels) {
			CmsCI component = requires.getToCi();
			
			boolean isOptional = requires.getAttribute("constraint").getDjValue().startsWith("0.");
			String template = requires.getAttribute("template").getDjValue();
			//always export optionals components or with attachments
			List<CmsCIRelation> attachmentRels =  cmProcessor.getFromCIRelations(component.getCiId(), ESCORTED_RELATION, DESIGN_ATTACHMENT_CLASS);
			ComponentExport eCi = stripAndSimplify(ComponentExport.class, component, false, (attachmentRels.size() > 0 || isOptional));
			if (eCi != null) {
				eCi.setTemplate(template);
				for (CmsCIRelation attachmentRel : attachmentRels) {
					eCi.addAttachment(stripAndSimplify(ExportCi.class, attachmentRel.getToCi(),true));
				}
				// need to work out dependsOn logic for now just include all within the resource
				List<CmsCIRelation> dependsOns =  cmProcessor.getFromCIRelations(component.getCiId(), DEPENDS_ON_RELATION, component.getCiClassName());
				for (CmsCIRelation dependsOn : dependsOns) {
					eCi.addDepends(dependsOn.getToCi().getCiName());
				}
				pe.addComponent(eCi);
			}
		}
	}
	
	private <T extends ExportCi> T stripAndSimplify(Class<T> expType, CmsCI ci, boolean force) {	
		return stripAndSimplify(expType, ci, force, false);
	}	

	private <T extends ExportCi> T stripAndSimplify(Class<T> expType, CmsCI ci, boolean force, boolean ignoreNoAttrs) {
		
		//List<String> attrsToExport = getAttrsToExportByPack(ci, force, packNsPath);

		List<String> attrsToExport = new ArrayList<String>();

		for (Map.Entry<String, CmsCIAttribute> entry : ci.getAttributes().entrySet()) {
			if (force || OWNER_DESIGN.equals(entry.getValue().getOwner())) {
				attrsToExport.add(entry.getKey());
			}
		}

		if (attrsToExport.isEmpty() && !force && !ignoreNoAttrs) {
			return null;
		}
		try {
			T exportCi = expType.newInstance();
			exportCi.setName(ci.getCiName());
			exportCi.setType(ci.getCiClassName());
			exportCi.setComments(ci.getComments());
			
			for (String attrName : attrsToExport) {
				String attrValue = ci.getAttribute(attrName).getDjValue();
				if (attrValue != null) {
					if (attrValue.startsWith(ENCRYPTED_PREFIX)) {
						attrValue = DUMMY_ENCRYPTED_EXP_VALUE;
					}
					exportCi.addAttribute(attrName, attrValue);
				}
			}
			return exportCi;

		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new DesignExportException(DesignExportException.TRANSISTOR_EXCEPTION, e.getMessage()); 
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new DesignExportException(DesignExportException.TRANSISTOR_EXCEPTION, e.getMessage()); 
		}
	}

	@SuppressWarnings("unused")
	private List<String>  getAttrsToExportByPack(CmsCI ci, boolean force, String packNsPath) {
		
		List<String> attrsToExport = new ArrayList<String>();
		if (force) {
			attrsToExport.addAll(ci.getAttributes().keySet());
			return attrsToExport;
		}
		
		List<CmsCI> mgmtCis = cmProcessor.getCiBy3(packNsPath, MGMT_PREFIX + ci.getCiClassName(), ci.getCiName());
		
		if (mgmtCis.size() == 0) {
			//this is optional component without mgmt ref, all attrs need to be exported
			attrsToExport.addAll(ci.getAttributes().keySet());
		} else {
			CmsCI mgmtCi = mgmtCis.get(0);
			for (String attName : ci.getAttributes().keySet()) {
				String ciValue = ci.getAttribute(attName).getDjValue();
				String mgmtValue = mgmtCi.getAttribute(attName).getDjValue();
				if (!isAttrValuesEqual(ciValue, mgmtValue)) {
					attrsToExport.add(attName);
				}
			}
		}
		return attrsToExport;
	}
	
	public long importDesign(long assemblyId, String userId, String scope, DesignExportSimple des) {
		CmsCI assembly = cmProcessor.getCiById(assemblyId);
		if (assembly == null) {
			throw new DesignExportException(DesignExportException.CMS_NO_CI_WITH_GIVEN_ID_ERROR, BAD_ASSEMBLY_ID_ERROR_MSG + assemblyId);
		}
		
		String designNsPath = assembly.getNsPath() + "/" + assembly.getCiName();
		
		List<CmsRelease> openReleases = rfcProcessor.getLatestRelease(designNsPath, "open");
		if (openReleases.size()>0) {
			throw new DesignExportException(DesignExportException.DJ_OPEN_RELEASE_FOR_NAMESPACE_ERROR, OPEN_RELEASE_ERROR_MSG);
		}
		
		if (des.getVariables() != null && !des.getVariables().isEmpty()) {
			importGlobalVars(assemblyId,designNsPath, des.getVariables(), userId);
		}
		for (PlatformExport platformExp : des.getPlatforms()) {
			
			CmsRfcCI platformRfc = newFromExportCiWithMdAttrs(platformExp, designNsPath, designNsPath, new HashSet<String>(Arrays.asList("description")));
			List<CmsRfcCI> existingPlatRfcs = cmRfcMrgProcessor.getDfDjCi(designNsPath, platformRfc.getCiClassName(), platformRfc.getCiName(), null);
			CmsRfcCI designPlatform = null;
			if (existingPlatRfcs.size()>0) {
				CmsRfcCI existingPlat = existingPlatRfcs.get(0);
				boolean needUpdate = false;
				if (platformExp.getAttributes() != null) {
					if (platformExp.getAttributes().containsKey("major_version")
						&& !existingPlat.getAttribute("major_version").getNewValue().equals(platformExp.getAttributes().get("major_version"))) {
						
						existingPlat.getAttribute("major_version").setNewValue(platformExp.getAttributes().get("major_version"));
						needUpdate = true;
					}
					if (platformExp.getAttributes().containsKey("description")
							&& !existingPlat.getAttribute("description").getNewValue().equals(platformExp.getAttributes().get("description"))) {
							
							existingPlat.getAttribute("description").setNewValue(platformExp.getAttributes().get("description"));
							needUpdate = true;
						}

				}
				if (needUpdate) {
					designPlatform = cmRfcMrgProcessor.upsertCiRfc(existingPlat, userId);
				} else {
					designPlatform = existingPlat;
				}
			} else {
				if (platformRfc.getAttribute("description").getNewValue() == null) {
					platformRfc.getAttribute("description").setNewValue("");
				}
				designPlatform = designRfcProcessor.generatePlatFromTmpl(platformRfc, assemblyId, userId, scope);
			}
			String platNsPath = designPlatform.getNsPath() + "/_design/" + designPlatform.getCiName();
			//local vars
			if (platformExp.getVariables() != null) {
				importLocalVars(designPlatform.getCiId(), platNsPath, designNsPath, platformExp.getVariables(), userId);
			}
			if (platformExp.getComponents() != null) {
				Set<Long> componentIds = new HashSet<>(); 
				for (ComponentExport componentExp : platformExp.getComponents()) {
					componentIds.add(importComponent(designPlatform, componentExp, platNsPath, designNsPath, userId));
				}
				importDepends(platformExp.getComponents(),platNsPath, designNsPath,userId);
				//if its existing platform - process absolete components
				if (existingPlatRfcs.size()>0) {
					procesObsoleteOptionalComponents(designPlatform.getCiId(),componentIds, userId);
				}
			}
		}
		
		//process LinkTos
		importLinksTos(des, designNsPath, userId);	

		CmsRelease release = cmRfcMrgProcessor.getReleaseByNameSpace(designNsPath);
		if (release != null) {
			return release.getReleaseId();
		} else {
			return 0;
		}
	}
	
	private void procesObsoleteOptionalComponents(long platformId, Set<Long> importedCiIds, String userId) {
		List<CmsCIRelation> requiresRels = cmProcessor.getFromCIRelations(platformId, REQUIRES_RELATION, null);
		for (CmsCIRelation requires : requiresRels) {
			if (requires.getAttribute("constraint").getDjValue().startsWith("0.")) {
				if (!importedCiIds.contains(requires.getToCiId())) {
					//this is absolete optional component that does not exists in export - remove from design
					cmRfcMrgProcessor.requestCiDelete(requires.getToCiId(), userId);
				}
			}
		}	
	}
	
	private void importLinksTos(DesignExportSimple des, String designNsPath, String userId)  {
		Map<String, CmsRfcCI> platforms = new HashMap<String, CmsRfcCI>();
		List<CmsRfcCI> existingPlatRfcs = cmRfcMrgProcessor.getDfDjCi(designNsPath, DESIGN_PLATFORM_CLASS, null, "dj");
		for (CmsRfcCI platformRfc : existingPlatRfcs) {
			platforms.put(platformRfc.getCiName(), platformRfc);
		}
		for (PlatformExport platformExp : des.getPlatforms()) {
			if (platformExp.getLinks() != null && !platformExp.getLinks().isEmpty()) {
				for (String toPlatformName : platformExp.getLinks()) {
					CmsRfcCI toPlatform = platforms.get(toPlatformName);
					if (toPlatform == null) {
						String errorMsg = CANT_FIND_PLATFORM_BY_NAME_ERROR_MSG.replace("$toPlatform", toPlatformName).replace("$fromPlatform", platformExp.getName());
						throw new DesignExportException(DesignExportException.CMS_NO_CI_WITH_GIVEN_ID_ERROR, errorMsg);
					}
					CmsRfcCI fromPlatform = platforms.get(platformExp.getName());
					CmsRfcRelation LinksTo = trUtil.bootstrapRelationRfc(fromPlatform.getCiId(), toPlatform.getCiId(), LINKS_TO_RELATION, designNsPath, designNsPath, null);
					upsertRelRfc(LinksTo, fromPlatform, toPlatform, 0, userId);
				}
			}
		}	
	}

	private void importDepends(List<ComponentExport> componentExports, String platformNsPath, String designNsPath, String userId)  {
		for (ComponentExport ce : componentExports) {
			if (ce.getDepends() != null && !ce.getDepends().isEmpty()) {
				Map<String, CmsRfcCI> components = new HashMap<String, CmsRfcCI>();
				List<CmsRfcCI> existingComponentRfcs = cmRfcMrgProcessor.getDfDjCi(platformNsPath, ce.getType(), null, "dj");
				for (CmsRfcCI componentRfc : existingComponentRfcs) {
					components.put(componentRfc.getCiName(), componentRfc);
				}
				for (String toComponentName : ce.getDepends()) {
					CmsRfcCI toComponent = components.get(toComponentName);
					if (toComponent == null) {
						String errorMsg = CANT_FIND_COMPONENT_BY_NAME_ERROR_MSG.replace("$toPlatform", toComponentName).replace("$fromPlatform", ce.getName());
						throw new DesignExportException(DesignExportException.CMS_NO_CI_WITH_GIVEN_ID_ERROR, errorMsg);
					}
					CmsRfcCI fromComponent = components.get(ce.getName());
					if (fromComponent.getCiClassName().equals(toComponent.getCiClassName())) {
						Map<String, CmsCIRelationAttribute> attrs = new HashMap<String, CmsCIRelationAttribute>();
						CmsCIRelationAttribute attr = new CmsCIRelationAttribute();
						attr.setAttributeName("source");
						attr.setDjValue("user");
						attrs.put(attr.getAttributeName(), attr);
						CmsRfcRelation dependsOn = trUtil.bootstrapRelationRfcWithAttrs(fromComponent.getCiId(), toComponent.getCiId(), DEPENDS_ON_RELATION, platformNsPath, designNsPath, attrs);
						upsertRelRfc(dependsOn, fromComponent, toComponent, 0, userId);
					}
				}
			}
		}
	}
	
	
	private void importGlobalVars(long assemblyId, String designNsPath,Map<String,String> globalVars, String userId) {
		
		for (Map.Entry<String, String> var : globalVars.entrySet()) {
			List<CmsRfcCI> existingVars = cmRfcMrgProcessor.getDfDjCiNakedLower(designNsPath, GLOBAL_VAR_CLASS, var.getKey(), null);
			Set<String> attrsToBootstrap = new HashSet<String>();
			CmsRfcCI varBaseRfc = null;
			if (var.getValue().startsWith(ENCRYPTED_PREFIX)) {
				attrsToBootstrap.add(ATTR_SECURE);
				attrsToBootstrap.add(ATTR_ENC_VALUE);
				varBaseRfc = trUtil.bootstrapRfc(var.getKey(), GLOBAL_VAR_CLASS, designNsPath, designNsPath, attrsToBootstrap);
				varBaseRfc.getAttribute(ATTR_SECURE).setNewValue("true");
				varBaseRfc.getAttribute(ATTR_ENC_VALUE).setNewValue(parseEncryptedImportValue(var.getValue()));
			} else {
				attrsToBootstrap.add(ATTR_VALUE);
				varBaseRfc = trUtil.bootstrapRfc(var.getKey(), GLOBAL_VAR_CLASS, designNsPath, designNsPath, attrsToBootstrap);
				varBaseRfc.getAttribute(ATTR_VALUE).setNewValue(var.getValue());
			}
			
			if (existingVars.isEmpty()) {
				CmsRfcCI varRfc = cmRfcMrgProcessor.upsertCiRfc(varBaseRfc, userId);
				CmsRfcRelation valueForRel = trUtil.bootstrapRelationRfc(varRfc.getCiId(), assemblyId, GLOBAL_VAR_RELATION, designNsPath, designNsPath, null);
				valueForRel.setFromRfcId(varRfc.getRfcId());
				cmRfcMrgProcessor.upsertRelationRfc(valueForRel, userId);
			} else {
				CmsRfcCI existingVar = existingVars.get(0);
				varBaseRfc.setCiId(existingVar.getCiId());
				varBaseRfc.setRfcId(existingVar.getRfcId());
				cmRfcMrgProcessor.upsertCiRfc(varBaseRfc, userId);
			}
		}
	}

	private String parseEncryptedImportValue(String encValue) {
		String value =encValue.substring(ENCRYPTED_PREFIX.length());
		if (value.length() == 0) {
			value = DUMMY_ENCRYPTED_IMP_VALUE; 
		}
		return value;
	}
	
	private void importLocalVars(long platformId, String platformNsPath, String releaseNsPath,Map<String,String> localVars, String userId) {
		
		for (Map.Entry<String, String> var : localVars.entrySet()) {
			List<CmsRfcCI> existingVars = cmRfcMrgProcessor.getDfDjCiNakedLower(platformNsPath, LOCAL_VAR_CLASS, var.getKey(), null);
			Set<String> attrsToBootstrap = new HashSet<String>();
			CmsRfcCI varBaseRfc = null;
			String varValue = null;
			if (var.getValue() == null) {
				varValue = "";
			} else {
				varValue = var.getValue(); 
			}
			
			if (varValue.startsWith(ENCRYPTED_PREFIX)) {
				attrsToBootstrap.add(ATTR_SECURE);
				attrsToBootstrap.add(ATTR_ENC_VALUE);
				varBaseRfc = trUtil.bootstrapRfc(var.getKey(), LOCAL_VAR_CLASS, platformNsPath, releaseNsPath, attrsToBootstrap);
				varBaseRfc.getAttribute(ATTR_SECURE).setNewValue("true");
				varBaseRfc.getAttribute(ATTR_ENC_VALUE).setNewValue(parseEncryptedImportValue(varValue));
				varBaseRfc.getAttribute(ATTR_ENC_VALUE).setOwner(OWNER_DESIGN);
				
			} else {
				attrsToBootstrap.add(ATTR_VALUE);
				varBaseRfc = trUtil.bootstrapRfc(var.getKey(), LOCAL_VAR_CLASS, platformNsPath, releaseNsPath, attrsToBootstrap);
				varBaseRfc.getAttribute(ATTR_VALUE).setNewValue(varValue);
				varBaseRfc.getAttribute(ATTR_VALUE).setOwner(OWNER_DESIGN);
			}
			
			if (existingVars.isEmpty()) {
				CmsRfcCI varRfc = cmRfcMrgProcessor.upsertCiRfc(varBaseRfc, userId);
				CmsRfcRelation valueForRel = trUtil.bootstrapRelationRfc(varRfc.getCiId(), platformId, LOCAL_VAR_RELATION, platformNsPath, releaseNsPath, null);
				valueForRel.setFromRfcId(varRfc.getRfcId());
				cmRfcMrgProcessor.upsertRelationRfc(valueForRel, userId);
			} else {
				CmsRfcCI existingVar = existingVars.get(0);
				varBaseRfc.setCiId(existingVar.getCiId());
				varBaseRfc.setRfcId(existingVar.getRfcId());
				cmRfcMrgProcessor.upsertCiRfc(varBaseRfc, userId);
			}
		}
	}
	
	
	private long importComponent(CmsRfcCI designPlatform, ComponentExport compExpCi, String platNsPath, String releaseNsPath, String userId) {
		List<CmsRfcCI> existingComponent = cmRfcMrgProcessor.getDfDjCiNakedLower(platNsPath, compExpCi.getType(), compExpCi.getName(), null);
		CmsRfcCI componentRfc = null;
		try {
			if (existingComponent.size() > 0) {
				CmsRfcCI existingRfc = existingComponent.get(0);
				CmsRfcCI component = newFromExportCi(compExpCi);
				component.setNsPath(platNsPath);
				component.setRfcId(existingRfc.getRfcId());
				component.setCiId(existingRfc.getCiId());
				component.setReleaseNsPath(releaseNsPath);
				componentRfc = cmRfcMrgProcessor.upsertCiRfc(component, userId);
			} else {
				//this is optional component lets find template
				String packNsPath = getPackNsPath(designPlatform);
				List<CmsCI> mgmtComponents = cmProcessor.getCiBy3(packNsPath, MGMT_PREFIX + compExpCi.getType(), compExpCi.getTemplate());
				if (mgmtComponents.isEmpty()) {
					//can not find template - abort
					throw new DesignExportException(DesignExportException.CMS_CANT_FIGURE_OUT_TEMPLATE_FOR_MANIFEST_ERROR, BAD_TEMPLATE_ERROR_MSG + packNsPath + ";" + compExpCi.getType() + ";" + compExpCi.getTemplate());
				}
				
				CmsCI template = mgmtComponents.get(0);
				CmsRfcCI component = designRfcProcessor.popRfcCiFromTemplate(template, "catalog", platNsPath, releaseNsPath);
				applyExportCiToTemplateRfc(compExpCi, component);
				componentRfc = cmRfcMrgProcessor.upsertCiRfc(component, userId);
				createRequires(designPlatform, template, componentRfc,userId);
				processMgmtDependsOnRels(designPlatform, template, componentRfc, userId);
			}
		} catch (DJException dje) {
			//missing required attributes
			throw new DesignExportException(dje.getErrorCode(),IMPORT_ERROR_PLAT_COMP 
											+ designPlatform.getCiName() 
											+ "/" + compExpCi.getName() + ":" + dje.getMessage());  
		}

		if (compExpCi.getAttachments() != null) {
			for (ExportCi attachmentExp : compExpCi.getAttachments()) {
				try {
				importAttachements(componentRfc, attachmentExp, releaseNsPath, userId);
				} catch (DJException dje) {
					throw new DesignExportException(dje.getErrorCode(),IMPORT_ERROR_PLAT_COMP_ATTACH 
													+ designPlatform.getCiName() 
													+ "/" + compExpCi.getName() 
													+ "/" + attachmentExp.getName() + ":" + dje.getMessage());  
				}
			}
		}
		return componentRfc.getCiId();
	}

	private void importAttachements(CmsRfcCI componentRfc, ExportCi attachmentExp, String releaseNsPath, String userId) {
		List<CmsRfcCI> existingAttachments = cmRfcMrgProcessor.getDfDjCiNakedLower(componentRfc.getNsPath(), DESIGN_ATTACHMENT_CLASS, attachmentExp.getName(), null);
		CmsRfcCI attachmentRfc = null;
		if (!existingAttachments.isEmpty()) {
			CmsRfcCI existingRfc = existingAttachments.get(0);
			CmsRfcCI attachment = newFromExportCi(attachmentExp);
			attachment.setNsPath(existingRfc.getNsPath());
			attachment.setRfcId(existingRfc.getRfcId());
			attachment.setCiId(existingRfc.getCiId());
			attachment.setReleaseNsPath(releaseNsPath);
			attachmentRfc = cmRfcMrgProcessor.upsertCiRfc(attachment, userId);
		} else {
			CmsRfcCI attachment = newFromExportCi(attachmentExp);
			attachment.setNsPath(componentRfc.getNsPath());
			attachment.setReleaseNsPath(releaseNsPath);
			attachmentRfc = cmRfcMrgProcessor.upsertCiRfc(attachment, userId);
			CmsRfcRelation escortedBy = trUtil.bootstrapRelationRfc(componentRfc.getCiId(), attachmentRfc.getCiId(), ESCORTED_RELATION, componentRfc.getNsPath(), releaseNsPath, null);
			if (componentRfc.getRfcId() > 0) {
				escortedBy.setFromRfcId(componentRfc.getRfcId());
			}
			escortedBy.setToRfcId(attachmentRfc.getRfcId());
			cmRfcMrgProcessor.upsertRelationRfc(escortedBy, userId);
		}
	}
	
	private void createRequires(CmsRfcCI designPlatform, CmsCI template, CmsRfcCI componentRfc, String userId) {
		List<CmsCIRelation> mgmtRequiresRels = cmProcessor.getToCIRelationsNaked(template.getCiId(), MGMT_REQUIRES_RELATION, MGMT_PREFIX + designPlatform.getCiClassName());
		if (mgmtRequiresRels.isEmpty()) {
			//can not find template relation - abort
			throw new DesignExportException(DesignExportException.CMS_CANT_FIND_REQUIRES_FOR_CI_ERROR, CANT_FIND_REQUIRES_ERROR_MSG + template.getCiId());
		}
		
		CmsRfcRelation designReqRel = designRfcProcessor.popRfcRelFromTemplate(mgmtRequiresRels.get(0), "base", componentRfc.getNsPath(), componentRfc.getReleaseNsPath());
		upsertRelRfc(designReqRel, designPlatform, componentRfc, componentRfc.getReleaseId(), userId);
	}
	
	private void processMgmtDependsOnRels(CmsRfcCI designPlatform, CmsCI mgmtCi, CmsRfcCI componentRfc, String userId) {
		//first do from
		List<CmsCIRelation> mgmtDependsOnFromRels = cmProcessor.getFromCIRelations(mgmtCi.getCiId(), MGMT_DEPENDS_ON_RELATION, null);
		for (CmsCIRelation mgmtDependsOn :mgmtDependsOnFromRels ) {
			//lets find corresponding design component
			CmsCI targetMgmtCi = mgmtDependsOn.getToCi();
			List<AttrQueryCondition> attrConditions = new ArrayList<AttrQueryCondition>();
			AttrQueryCondition condition = new AttrQueryCondition();
			condition.setAttributeName("template");
			condition.setCondition("eq");
			condition.setAvalue(targetMgmtCi.getCiName());
			attrConditions.add(condition);
			List<CmsRfcRelation> designRequiresRels = cmRfcMrgProcessor.getFromCIRelationsByAttrs(
					designPlatform.getCiId(), 
					REQUIRES_RELATION, 
					null, 
					"catalog." + trUtil.getLongShortClazzName(targetMgmtCi.getCiClassName()), "dj", attrConditions);
			//now we need to create all dependsOn rels for these guys, if any
			for (CmsRfcRelation requires : designRequiresRels) {
				CmsRfcRelation designDependsOnRel = designRfcProcessor.popRfcRelFromTemplate(mgmtDependsOn, "catalog", componentRfc.getNsPath(), componentRfc.getReleaseNsPath());
				upsertRelRfc(designDependsOnRel, componentRfc, requires.getToRfcCi(), componentRfc.getReleaseId(), userId);
			}
		}
		//Now "To" 
		List<CmsCIRelation> mgmtDependsOnToRels = cmProcessor.getToCIRelations(mgmtCi.getCiId(), MGMT_DEPENDS_ON_RELATION, null);
		for (CmsCIRelation mgmtDependsOn :mgmtDependsOnToRels ) {
			//lets find corresponding design component
			CmsCI targetMgmtCi = mgmtDependsOn.getFromCi();
			List<AttrQueryCondition> attrConditions = new ArrayList<AttrQueryCondition>();
			AttrQueryCondition condition = new AttrQueryCondition();
			condition.setAttributeName("template");
			condition.setCondition("eq");
			condition.setAvalue(targetMgmtCi.getCiName());
			attrConditions.add(condition);
			List<CmsRfcRelation> designRequiresRels = cmRfcMrgProcessor.getFromCIRelationsByAttrs(
					designPlatform.getCiId(), 
					REQUIRES_RELATION, 
					null, 
					"catalog." + trUtil.getLongShortClazzName(targetMgmtCi.getCiClassName()), "dj", attrConditions);
			//now we need to create all dependsOn rels for these guys, if any
			for (CmsRfcRelation requires : designRequiresRels) {
				CmsRfcRelation designDependsOnRel = designRfcProcessor.popRfcRelFromTemplate(mgmtDependsOn, "catalog", componentRfc.getNsPath(), componentRfc.getReleaseNsPath());
				upsertRelRfc(designDependsOnRel, requires.getToRfcCi(), componentRfc, componentRfc.getReleaseId(), userId);
			}
		}

		
	}
	
	private void upsertRelRfc(CmsRfcRelation relRfc, CmsRfcCI fromRfc, CmsRfcCI toRfc, long releaseId, String userId) {
		relRfc.setToCiId(toRfc.getCiId());
		if (toRfc.getRfcId() > 0) {
			relRfc.setToRfcId(toRfc.getRfcId());
		}
		relRfc.setFromCiId(fromRfc.getCiId());
		if (fromRfc.getRfcId() > 0) {
			relRfc.setFromRfcId(fromRfc.getRfcId());
		}
		if (releaseId > 0) {
			relRfc.setReleaseId(releaseId);
		}
		relRfc.setCreatedBy(userId);
		relRfc.setUpdatedBy(userId);
		cmRfcMrgProcessor.upsertRelationRfc(relRfc, userId);
		
	}
	
	private CmsRfcCI newFromExportCi(ExportCi eCi) {
		CmsRfcCI rfc = new CmsRfcCI();
		rfc.setCiName(eCi.getName());
		rfc.setCiClassName(eCi.getType());
		if (eCi.getAttributes() != null) {
			for (Map.Entry<String, String> attr : eCi.getAttributes().entrySet()) {
				CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
				rfcAttr.setAttributeName(attr.getKey());
				rfcAttr.setNewValue(attr.getValue());
				rfcAttr.setOwner(OWNER_DESIGN);
				rfc.addAttribute(rfcAttr);
			}
		}
		return rfc;
	}

	private CmsRfcCI newFromExportCiWithMdAttrs(ExportCi eCi, String nsPath, String releaseNsPath, Set<String> attrsToBootstrap) {
		CmsRfcCI rfc = trUtil.bootstrapRfc(eCi.getName(), eCi.getType(), nsPath, releaseNsPath, attrsToBootstrap);
		rfc.setCiName(eCi.getName());
		rfc.setCiClassName(eCi.getType());
		if (eCi.getAttributes() != null) {
			for (Map.Entry<String, String> attr : eCi.getAttributes().entrySet()) {
				CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
				rfcAttr.setAttributeName(attr.getKey());
				rfcAttr.setNewValue(attr.getValue());
				rfcAttr.setOwner(OWNER_DESIGN);
				rfc.addAttribute(rfcAttr);
			}
		}
		return rfc;
	}
	
	private void applyExportCiToTemplateRfc(ExportCi eCi, CmsRfcCI rfc) {
		rfc.setCiName(eCi.getName());
		rfc.setCiClassName(eCi.getType());
		if (eCi.getAttributes() != null) {
			for (Map.Entry<String, String> attr : eCi.getAttributes().entrySet()) {
				String newValue = attr.getValue();
				if (newValue != null &&  newValue.startsWith(ENCRYPTED_PREFIX)) {
					newValue = parseEncryptedImportValue(newValue);
				}
				if (rfc.getAttribute(attr.getKey()) != null) {
					rfc.getAttribute(attr.getKey()).setNewValue(newValue);
					rfc.getAttribute(attr.getKey()).setOwner(OWNER_DESIGN);
				} else {CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
					rfcAttr.setAttributeName(attr.getKey());
					rfcAttr.setNewValue(newValue);
					rfcAttr.setOwner(OWNER_DESIGN);
					rfc.addAttribute(rfcAttr);
				}
			}
		}
	}
	
	
	private boolean isAttrValuesEqual (String attr1, String attr2) {
		if (attr1 == null && (attr2 == null || attr2.length() == 0)) {
			return true;
		}
		if (attr2 == null && (attr1 == null || attr1.length() == 0)) {
			return true;
		}
		if (attr1 != null && attr2 != null) {
			String str1 = attr1.replaceAll("(\\r|\\n)", "").trim();
			String str2 = attr2.replaceAll("(\\r|\\n)", "").trim();
			return str1.equals(str2);
		}
		return false;
	}
	
	private String[] checkVar4Export(CmsCI var, boolean checkLock) {
		String name = var.getCiName();
		String value = null;
		if ("true".equals(var.getAttribute(ATTR_SECURE).getDjValue())) {
			if (checkLock && !OWNER_DESIGN.equals(var.getAttribute(ATTR_ENC_VALUE).getOwner())) {
				return null;
			}
			value = DUMMY_ENCRYPTED_EXP_VALUE;
		} else {
			if (checkLock && !OWNER_DESIGN.equals(var.getAttribute(ATTR_VALUE).getOwner())) {
				return null;
			}
			value = var.getAttribute(ATTR_VALUE).getDjValue();
		}
		String[] result = {name, value};
		return result;
	}
	
	public void populateOwnerAttribute(long assemblyId) {
		
		CmsCI assembly = cmProcessor.getCiById(assemblyId);
		if (assembly == null) {
			throw new DesignExportException(DesignExportException.CMS_NO_CI_WITH_GIVEN_ID_ERROR, BAD_ASSEMBLY_ID_ERROR_MSG + assemblyId);
		}
		
		//we always export global vars
		//List<CmsCIRelation> globalVarRels = cmProcessor.getToCIRelations(assemblyId, GLOBAL_VAR_RELATION, GLOBAL_VAR_CLASS);
        
		List<CmsCIRelation> composedOfs = cmProcessor.getFromCIRelations(assemblyId, COMPOSED_OF_RELATION, DESIGN_PLATFORM_CLASS);
		
		for (CmsCIRelation composedOf : composedOfs) {
			CmsCI platform = composedOf.getToCi();
			String packNsPath = getPackNsPath(platform); 
			
			//local vars
			List<CmsCIRelation> localVarRels = cmProcessor.getToCIRelations(platform.getCiId(), LOCAL_VAR_RELATION, LOCAL_VAR_CLASS);
			for (CmsCIRelation lvRel : localVarRels) {
				CmsCI var = lvRel.getFromCi();
				List<CmsCI> mgmtVars = cmProcessor.getCiBy3(packNsPath, MGMT_PREFIX + var.getCiClassName(), var.getCiName());
				if (mgmtVars.isEmpty()) {
					updateOwners(var, null); 
				} else {
					updateOwners(var, mgmtVars.get(0));
				}
			}
			
			//components
			List<CmsCIRelation> requiresRels = cmProcessor.getFromCIRelations(platform.getCiId(), REQUIRES_RELATION, null);
			for (CmsCIRelation requires : requiresRels) {
				CmsCI component = requires.getToCi();
				String template = requires.getAttribute("template").getDjValue();
				List<CmsCI> mgmtComponents = cmProcessor.getCiBy3(packNsPath, MGMT_PREFIX + component.getCiClassName(), template);
				if (!mgmtComponents.isEmpty()) {
					updateOwners(component, mgmtComponents.get(0));
				}
			}	
		}	
	}

	private void updateOwners(CmsCI designCi, CmsCI mgmtCi) {
		if (mgmtCi == null) {
			//this is user var mar all attr owners 
			for (CmsCIAttribute attrToUpdate : designCi.getAttributes().values()) {
				if (attrToUpdate.getCiAttributeId() > 0) {
					attrToUpdate.setOwner(OWNER_DESIGN);
					attrToUpdate.setCiId(designCi.getCiId());
					ciMapper.updateCIAttribute(attrToUpdate);
				}
			}
		} else {
			for (String attName : designCi.getAttributes().keySet()) {
				String designValue = designCi.getAttribute(attName).getDjValue();
				String mgmtValue = mgmtCi.getAttribute(attName).getDjValue();
				if (!isAttrValuesEqual(designValue, mgmtValue)) {
					CmsCIAttribute attrToUpdate = designCi.getAttribute(attName);
					if (attrToUpdate.getCiAttributeId() > 0) {
						attrToUpdate.setOwner(OWNER_DESIGN);
						attrToUpdate.setCiId(designCi.getCiId());
						ciMapper.updateCIAttribute(attrToUpdate);
					}
				}
			}
		}
	}
	
	private String getPackNsPath(CmsCI platform) {
		return 	"/public/" + 
				platform.getAttribute("source").getDjValue() + "/packs/" +
				platform.getAttribute("pack").getDjValue() + "/" +
				platform.getAttribute("version").getDjValue();
	}

	private String getPackNsPath(CmsRfcCI platform) {
		return 	"/public/" + 
				platform.getAttribute("source").getNewValue() + "/packs/" +
				platform.getAttribute("pack").getNewValue() + "/" +
				platform.getAttribute("version").getNewValue();
	}

}
