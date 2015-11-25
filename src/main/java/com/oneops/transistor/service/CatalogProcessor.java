package com.oneops.transistor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.domain.CatalogExport;
import com.oneops.transistor.exceptions.TransistorException;

public class CatalogProcessor {
	
	static Logger logger = Logger.getLogger(CatalogProcessor.class);
	
	private TransUtil trUtil;
	private CmsCmProcessor cmProcessor;

	public void setTrUtil(TransUtil trUtil) {
		this.trUtil = trUtil;
	}

	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	public long saveAssemblyAsCatalog(CmsCI targetCatalog, long fromAssemblyId, String userId, String scope) {

		CmsCI org = trUtil.getOrgByScope(scope);
		if (org == null) {
			String errMsg = "Can not find org by the scope = " + scope + ";"; 
			logger.error(errMsg);
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_ORG_BY_SCOPE, errMsg);
		}

		String catalogNS = "/" + org.getCiName() + "/_catalogs";
		//String catalogNS = "/public/packer/catalogs";
		trUtil.verifyAndCreateNS(catalogNS);
		targetCatalog.setNsPath(catalogNS);
		trUtil.verifyScope(targetCatalog, scope);
		CmsCI fromAssembly = cmProcessor.getCiById(fromAssemblyId);
		if (targetCatalog.getAttribute("description").getDfValue() == null) {
			String desc = "Created from " + fromAssembly.getCiName();
			targetCatalog.getAttribute("description").setDfValue(desc);
			targetCatalog.getAttribute("description").setDjValue(desc);
		}
		targetCatalog.setCreatedBy(userId);
		targetCatalog.setUpdatedBy(userId);
		
		CmsCI catalog = cmProcessor.createCI(targetCatalog);
		Map<Long,Long> source2catalog = new HashMap<Long,Long>();
		
		List<CmsCIRelation> composedOfs = cmProcessor.getFromCIRelationsNaked(fromAssemblyId, "base.ComposedOf", "catalog.Platform");
		for (CmsCIRelation composedOf : composedOfs) {
			long sourcePlatId = composedOf.getToCiId();
			long catalogPlatId = clonePlatform(catalog,sourcePlatId, composedOf, userId, scope );
			source2catalog.put(sourcePlatId, catalogPlatId);
		}
		//process linksTo relations
		String platsNS = catalog.getNsPath() + "/" + catalog.getCiName();
		processAssemblyInternalRels(source2catalog, platsNS, userId);
		return catalog.getCiId();
	}

	public long importCatalog(CatalogExport catExp, String userId, String scope) {

		CmsCI org = trUtil.getOrgByScope(scope);
		if (org == null) {
			String errMsg = "Can not find org by the scope = " + scope + ";"; 
			logger.error(errMsg);
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_ORG_BY_SCOPE, errMsg);
		}
		
		String orgCatalogNs = "/" + org.getCiName() + "/_catalogs";
		trUtil.verifyAndCreateNS(orgCatalogNs);
		CmsCI catalogRequest = bootstrapCatalogCI(catExp, orgCatalogNs);
		catalogRequest.setCreatedBy(userId);
		catalogRequest.setUpdatedBy(userId);
		
		CmsCI catalog = cmProcessor.createCI(catalogRequest);
		Map<Long,Long> source2catalog = new HashMap<Long,Long>();
		source2catalog.put(catExp.getCatalogId(), catalog.getCiId());
		
		String catalogNs = catalog.getNsPath() + "/" + catalog.getCiName(); 
		for (CmsCI expCi : catExp.getCis()) {
			long oldCiId = expCi.getCiId();
			String ciNs = catalogNs + expCi.getNsPath();
			trUtil.verifyAndCreateNS(ciNs);	
			expCi.setNsPath(ciNs);
			expCi.setCiId(0);
			CmsCI ci = cmProcessor.createCI(expCi);
			source2catalog.put(oldCiId, ci.getCiId());
		}
		
		for (CmsCIRelation expRel : catExp.getRelations()) {
			expRel.setNsPath(catalogNs + expRel.getNsPath());
			expRel.setFromCiId(source2catalog.get(expRel.getFromCiId()));
			expRel.setToCiId(source2catalog.get(expRel.getToCiId()));
			cmProcessor.createRelation(expRel);
		}
		
		return catalog.getCiId();
	}
	
	private CmsCI bootstrapCatalogCI(CatalogExport catExp, String nsPath) {
		CmsCI catalog = new CmsCI();
		catalog.setCiName(catExp.getCatalogName());
		catalog.setCiClassName("account.Design");
		catalog.setNsPath(nsPath);
		CmsCIAttribute desc = new CmsCIAttribute();
		desc.setAttributeName("description");
		desc.setDfValue(catExp.getDescription());
		desc.setDjValue(catExp.getDescription());
		catalog.addAttribute(desc);
		return catalog;
	}
	
	public CatalogExport exportCatalog(long catalogCiId, String scope) {
		CmsCI catalog = cmProcessor.getCiById(catalogCiId);
		trUtil.verifyScope(catalog, scope);
		CatalogExport catExport = new CatalogExport();
		catExport.setCatalogName(catalog.getCiName());
		catExport.setCatalogId(catalog.getCiId());
		if (catalog.getAttribute("description") != null) {
			catExport.setDescription(catalog.getAttribute("description").getDfValue());
		}
		String catalogNs = catalog.getNsPath()+"/" + catalog.getCiName();
		List<CmsCI> catalogCIs = cmProcessor.getCiBy3(catalogNs, null, null);
		
		
		catalogCIs.addAll(cmProcessor.getCiBy3NsLike(catalogNs+"/_design", null, null));
		for (CmsCI ci : catalogCIs) {
			purgeCiForExport(ci);
		}
		catExport.setCis(catalogCIs);
		List<CmsCIRelation> catalogRels = cmProcessor.getCIRelationsNsLikeNaked(catalogNs, null, null, null, null);
		for (CmsCIRelation rel : catalogRels) {
			purgeRelationForExport(rel);
		}
		catExport.setRelations(catalogRels);
		return catExport;
	}
	
	private void purgeCiForExport(CmsCI ci) {
		ci.setCiClassId(0);
		ci.setCiGoid(null);
		ci.setCiState(null);
		ci.setCiStateId(0);
		ci.setCreated(null);
		ci.setCreatedBy(null);
		ci.setLastAppliedRfcId(0);
		ci.setNsId(0);
		ci.setUpdatedBy(null);
		ci.setUpdated(null);
		int designPosition = ci.getNsPath().indexOf("/_design"); 
		if ( designPosition >0) {
			ci.setNsPath(ci.getNsPath().substring(designPosition));
		} else {
			ci.setNsPath("");
		}
	}

	private void purgeRelationForExport(CmsCIRelation rel) {
		rel.setCiRelationId(0);
		rel.setCreated(null);
		rel.setCreatedBy(null);
		rel.setLastAppliedRfcId(0);
		rel.setNsId(0);
		rel.setRelationGoid(null);
		rel.setRelationId(0);
		rel.setRelationState(null);
		rel.setRelationStateId(0);
		rel.setUpdated(null);
		rel.setUpdatedBy(null);
		int designPosition = rel.getNsPath().indexOf("/_design"); 
		if ( designPosition >0) {
			rel.setNsPath(rel.getNsPath().substring(designPosition));
		} else {
			rel.setNsPath("");
		}
	}
	
	private void processAssemblyInternalRels(Map<Long,Long> source2catalog, String nsPath, String userId) {
		for (Long sourcePlatId : source2catalog.keySet()) {
			List<CmsCIRelation> linkesTos = cmProcessor.getFromCIRelationsNaked(sourcePlatId, "catalog.LinksTo", "catalog.Platform");
			for (CmsCIRelation linkedTo : linkesTos) {
				CmsCIRelation catalogInternalRel = trUtil.cloneCIRelationBasic(linkedTo);
				catalogInternalRel.setFromCiId(source2catalog.get(linkedTo.getFromCiId()));
				catalogInternalRel.setToCiId(source2catalog.get(linkedTo.getToCiId()));
				catalogInternalRel.setNsPath(nsPath);
				catalogInternalRel.setCreatedBy(userId);
				catalogInternalRel.setUpdatedBy(userId);
				
				cmProcessor.createRelation(catalogInternalRel);
			}
		}
	}

	public long clonePlatform(CmsCI catalog, long sourcePlatId, CmsCIRelation sourceComposedOf, String userId, String scope) {
		
		CmsCI sourcePlatform = cmProcessor.getCiById(sourcePlatId);

		CmsCI catalogPlatform = trUtil.cloneCIBasic(sourcePlatform);
		catalogPlatform.setNsPath(catalog.getNsPath() + "/" + catalog.getCiName());
		catalogPlatform.getAttribute("major_version").setDfValue("1");
		catalogPlatform.getAttribute("major_version").setDjValue("1");
		catalogPlatform.setCreatedBy(userId);
		catalogPlatform.setUpdatedBy(userId);

		trUtil.verifyScope(catalogPlatform, scope);

		CmsCI catalogPlatformCI = cmProcessor.createCI(catalogPlatform);
		//lets process required components
		String platNsPath = catalogPlatform.getNsPath() + "/_design/" + catalogPlatform.getCiName();
		trUtil.verifyAndCreateNS(platNsPath);
		clonePlatformComponents(catalogPlatformCI, platNsPath, sourcePlatform, userId);
		
		//create catalog -> compoedOf->platform relation
		CmsCIRelation composedOf = trUtil.cloneCIRelationBasic(sourceComposedOf);
		composedOf.setNsPath(catalogPlatform.getNsPath());
		composedOf.setCreatedBy(userId);
		composedOf.setUpdatedBy(userId);
		composedOf.setFromCiId(catalog.getCiId());
		composedOf.setToCiId(catalogPlatformCI.getCiId());

		cmProcessor.createRelation(composedOf);
		
		return catalogPlatformCI.getCiId();
	}
	
	private void clonePlatformComponents(CmsCI catalogPlatform, String catalogPlatNsPath, CmsCI sourcePlatform, String userId) {
		String sourcePlatNsPath = sourcePlatform.getNsPath() + "/_design/" + sourcePlatform.getCiName();
		List<CmsCI> sourceComponents = cmProcessor.getCiBy3(sourcePlatNsPath, null, null);
		Map<Long,Long> source2catalog = new HashMap<Long,Long>();
		source2catalog.put(sourcePlatform.getCiId(), catalogPlatform.getCiId());
		for (CmsCI sourceComponent : sourceComponents) {
			CmsCI catalogComponent = trUtil.cloneCIBasic(sourceComponent);
			catalogComponent.setNsPath(catalogPlatNsPath);
			catalogComponent.setCreatedBy(userId);
			catalogComponent.setUpdatedBy(userId);
			CmsCI catalogComponentCI = cmProcessor.createCI(catalogComponent);
			source2catalog.put(sourceComponent.getCiId(), catalogComponentCI.getCiId());
		}
	
		// now lets process the relations
		List<CmsCIRelation> sourceRels = cmProcessor.getCIRelationsNaked(sourcePlatNsPath, null, null, null, null);
		for (CmsCIRelation sourceRel : sourceRels) {
			CmsCIRelation catalogRel = trUtil.cloneCIRelationBasic(sourceRel);
			catalogRel.setNsPath(catalogPlatNsPath);
			catalogRel.setCreatedBy(userId);
			catalogRel.setUpdatedBy(userId);
			catalogRel.setFromCiId(source2catalog.get(sourceRel.getFromCiId()));
			catalogRel.setToCiId(source2catalog.get(sourceRel.getToCiId()));
			cmProcessor.createRelation(catalogRel);
		}
	}
	
}
