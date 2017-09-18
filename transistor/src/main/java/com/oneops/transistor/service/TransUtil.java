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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAttribute;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.md.domain.CmsRelationAttribute;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.ns.domain.CmsNamespace;
import com.oneops.cms.ns.service.CmsNsManager;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.transistor.exceptions.TransistorException;
import com.oneops.transistor.util.ExpEvaluator;

public class TransUtil {

	static Logger logger = Logger.getLogger(TransUtil.class);
	
	private CmsCmProcessor cmProcessor;
	private CmsNsManager nsManager;
	private ExpEvaluator expEval;
	private CmsMdProcessor mdProcessor;
	private CmsUtil cmsUtil;

	public void setCmsUtil(CmsUtil cmsUtil) {
		this.cmsUtil = cmsUtil;
	}
	
	public void setExpEval(ExpEvaluator expEval) {
		this.expEval = expEval;
	}
	
	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	public void setNsManager(CmsNsManager nsManager) {
		this.nsManager = nsManager;
	}

	public void setMdProcessor(CmsMdProcessor mdProcessor) {
		this.mdProcessor = mdProcessor;
	}
	
	public long verifyAndCreateNS(String nsPath) {
		CmsNamespace ns = nsManager.getNs(nsPath);
		if (ns == null) {
			ns = new CmsNamespace();
			ns.setNsPath(nsPath);
			return nsManager.createNs(ns).getNsId();
		} else {
			return ns.getNsId();
		}
	}
	
	/**
	 * Clean existing platform NS and create new NS
	 * @param nsPath
	 * @return
	 */
	public long cleanAndCreatePlatformNS(String nsPath) {
		CmsNamespace ns = nsManager.getNs(nsPath);
		if (ns != null) {
			nsManager.deleteNsById(ns.getNsId());
		} 
		ns = new CmsNamespace();
		ns.setNsPath(nsPath);
		return nsManager.createNs(ns).getNsId();
	}
	
	public void deleteNs(String nsPath) {
		nsManager.deleteNs(nsPath);
	}
	
	public String getShortClazzName(String fullClazzName) {
		return cmsUtil.getShortClazzName(fullClazzName);
	}

	public String getLongShortClazzName(String fullClazzName) {
		return cmsUtil.getLongShortClazzName(fullClazzName);
	}
	
	
	/*
	public String getShortClazzName(String fullClazzName) {
		if (fullClazzName.startsWith("account")) {
			String[] nameParts = fullClazzName.split("\\.");
			return nameParts[nameParts.length-1];
		} else {
			return fullClazzName.replaceAll("base.|mgmt.catalog.|catalog.|mgmt.manifest.|manifest.|bom.|mgmt.|", "");
		}
	}
	*/
	
	public void applyCiToRfc(CmsRfcCI newRfc, CmsCI ci, Map<String, CmsClazzAttribute> mdAttrs, boolean setComments, boolean checkExpression) {
	    if (ci != null) {
	    	newRfc.setCiName(ci.getCiName());
	    	if (setComments) newRfc.setComments(ci.getComments());
	    	Map<String,String> expressions = new HashMap<String,String>();
	    	for (CmsCIAttribute mgmtAttr : ci.getAttributes().values()) {
	    		if (mdAttrs.containsKey(mgmtAttr.getAttributeName())) {
			    	if (mgmtAttr.getDjValue() != null && checkExpression) {
			    		//TODO add this to the expressions map
			    		expressions.put(mgmtAttr.getAttributeName(), mgmtAttr.getDjValue());
			    	}	
	    			if (mgmtAttr.getDfValue() != null) {
			    		if (newRfc.getAttribute(mgmtAttr.getAttributeName()) != null) {
			    			newRfc.getAttribute(mgmtAttr.getAttributeName()).setNewValue(mgmtAttr.getDfValue());
			    			newRfc.getAttribute(mgmtAttr.getAttributeName()).setComments(mgmtAttr.getComments());
			    		} else {
			    			if (mdAttrs.get(mgmtAttr.getAttributeName()) != null) {
					    		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
					    		rfcAttr.setAttributeId(mdAttrs.get(mgmtAttr.getAttributeName()).getAttributeId());
					    		rfcAttr.setAttributeName(mgmtAttr.getAttributeName());
					    		rfcAttr.setNewValue(mgmtAttr.getDfValue());
					    		newRfc.addAttribute(rfcAttr);
			    			}
			    		}
			    	}
	    		}
		    }
	    	if (expressions.size() > 0 ) {
	    		expEval.processExpressions(expressions, newRfc);
	    	}
	    }
	}

	
	public static CmsRfcCI cloneRfcCIBasic(CmsRfcCI rfcCi) {
		
		CmsRfcCI newRfc = new CmsRfcCI();
		
		newRfc.setCiClassName(rfcCi.getCiClassName());
		newRfc.setCiName(rfcCi.getCiName());
		newRfc.setComments(rfcCi.getComments());
		newRfc.setNsPath(rfcCi.getNsPath());
		
		for (CmsRfcAttribute attr : rfcCi.getAttributes().values()){
			CmsRfcAttribute newAttr = new CmsRfcAttribute();
			newAttr.setAttributeId(attr.getAttributeId());
			newAttr.setAttributeName(attr.getAttributeName());
			newAttr.setComments(attr.getComments());
			newAttr.setNewValue(attr.getNewValue());
			newAttr.setOwner(attr.getOwner());
			newRfc.addAttribute(newAttr);
		}
		
		return newRfc;
	}

	public static CmsRfcCI cloneRfc(CmsRfcCI rfcCi) {
		
		CmsRfcCI newRfc = new CmsRfcCI();
		
		newRfc.setRfcId(rfcCi.getRfcId());
		newRfc.setCiId(rfcCi.getCiId());
		newRfc.setCiClassName(rfcCi.getCiClassName());
		newRfc.setCiGoid(rfcCi.getCiGoid());
		newRfc.setCiName(rfcCi.getCiName());
		newRfc.setComments(rfcCi.getComments());
		newRfc.setExecOrder(rfcCi.getExecOrder());
		newRfc.setNsPath(rfcCi.getNsPath());
		newRfc.setLastAppliedRfcId(rfcCi.getLastAppliedRfcId());
		newRfc.setReleaseId(rfcCi.getReleaseId());
		newRfc.setRfcAction(rfcCi.getRfcAction());
		newRfc.setCreatedBy(rfcCi.getCreatedBy());
		newRfc.setUpdatedBy(rfcCi.getUpdatedBy());
		
		for (CmsRfcAttribute attr : rfcCi.getAttributes().values()){
			CmsRfcAttribute newAttr = new CmsRfcAttribute();
			newAttr.setAttributeId(attr.getAttributeId());
			newAttr.setAttributeName(attr.getAttributeName());
			newAttr.setComments(attr.getComments());
			newAttr.setNewValue(attr.getNewValue());
			newAttr.setOldValue(attr.getOldValue());
			newAttr.setOwner(attr.getOwner());
			newAttr.setRfcAttributeId(attr.getRfcAttributeId());
			newAttr.setRfcId(attr.getRfcId());
			newRfc.addAttribute(newAttr);
		}
		
		return newRfc;
	}

	
	public CmsRfcCI mergeCis(CmsCI baseCi, CmsCI overrideCi, String targetPrefix, String nsPath, String releaseNsPath) {
		
		CmsRfcCI newRfc = new CmsRfcCI();
		newRfc.setNsPath(nsPath);
		newRfc.setReleaseNsPath(releaseNsPath);
		
		String targetClazzName = null;
		if (baseCi != null) {
			targetClazzName = targetPrefix + "." + getLongShortClazzName(baseCi.getCiClassName());
		} else {
			targetClazzName = targetPrefix + "." + getLongShortClazzName(overrideCi.getCiClassName());
		}
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
	    
	    //populate values from manifest template obj if it's not null
	    applyCiToRfc(newRfc, baseCi, clazzAttrs, false, true);
	    
	    //populate vlues from design ci if not null;
	    applyCiToRfc(newRfc, overrideCi, clazzAttrs, true, false);
	    
		return newRfc;
	}
	
	
	public static CmsRfcRelation cloneRfcRelationBasic(CmsRfcRelation baseRfc) {

		CmsRfcRelation newRfc = new CmsRfcRelation();
		
		newRfc.setRelationId(baseRfc.getRelationId());
		newRfc.setRelationName(baseRfc.getRelationName());
	    
	    for (CmsRfcAttribute baseAttr : baseRfc.getAttributes().values()) {
	    	if (baseAttr.getNewValue() != null) {
	    		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
	    		rfcAttr.setAttributeId(baseAttr.getAttributeId());
	    		rfcAttr.setAttributeName(baseAttr.getAttributeName());
	    		rfcAttr.setNewValue(baseAttr.getNewValue());
	    		rfcAttr.setOwner(baseAttr.getOwner());
	    		newRfc.addAttribute(rfcAttr);
	    	}
	    }
	    
	    return newRfc;
	}

	public static CmsRfcRelation cloneRfcRelation(CmsRfcRelation baseRfc) {

		CmsRfcRelation newRfc = new CmsRfcRelation();
		
		newRfc.setRfcId(baseRfc.getRfcId());
		
		newRfc.setCiRelationId(baseRfc.getCiRelationId());
		newRfc.setComments(baseRfc.getComments());
		newRfc.setFromCiId(baseRfc.getFromCiId());
		newRfc.setFromRfcId(baseRfc.getFromRfcId());
		newRfc.setFromRfcCi(baseRfc.getFromRfcCi());
		
		newRfc.setToCiId(baseRfc.getToCiId());
		newRfc.setToRfcId(baseRfc.getToRfcId());
		newRfc.setToRfcCi(baseRfc.getToRfcCi());
		
		newRfc.setNsId(baseRfc.getNsId());
		newRfc.setNsPath(baseRfc.getNsPath());
		
		newRfc.setRelationGoid(baseRfc.getRelationGoid());
		newRfc.setReleaseId(baseRfc.getReleaseId());
		newRfc.setReleaseNsPath(baseRfc.getReleaseNsPath());
		
		newRfc.setRelationId(baseRfc.getRelationId());
		newRfc.setRelationName(baseRfc.getRelationName());

	    for (CmsRfcAttribute baseAttr : baseRfc.getAttributes().values()) {
	    	if (baseAttr.getNewValue() != null) {
	    		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
	    		rfcAttr.setAttributeId(baseAttr.getAttributeId());
	    		rfcAttr.setAttributeName(baseAttr.getAttributeName());
	    		rfcAttr.setNewValue(baseAttr.getNewValue());
	    		rfcAttr.setOwner(baseAttr.getOwner());
	    		newRfc.addAttribute(rfcAttr);
	    	}
	    }
	    
	    return newRfc;
	}
	
	
	public CmsCI cloneCIBasic(CmsCI baseCi) {
		
		CmsCI newCi = new CmsCI();
		
		newCi.setCiClassName(baseCi.getCiClassName());
		newCi.setCiName(baseCi.getCiName());
		newCi.setComments(baseCi.getComments());
		
		for (CmsCIAttribute attr : baseCi.getAttributes().values()){
			CmsCIAttribute newAttr = new CmsCIAttribute();
			newAttr.setAttributeId(attr.getAttributeId());
			newAttr.setAttributeName(attr.getAttributeName());
			newAttr.setComments(attr.getComments());
			newAttr.setDfValue(attr.getDfValue());
			newAttr.setDjValue(attr.getDjValue());
			newCi.addAttribute(newAttr);
		}
		
		return newCi;
	}
	
	public CmsCI cloneCI(CmsCI baseCi) {
		
		CmsCI newCi = new CmsCI();
		
		newCi.setCiId(baseCi.getCiId());
		newCi.setCiClassId(baseCi.getCiClassId());
		newCi.setCiClassName(baseCi.getCiClassName());
		newCi.setCiName(baseCi.getCiName());
		newCi.setComments(baseCi.getComments());
		newCi.setCiGoid(baseCi.getCiGoid());
		newCi.setCiState(baseCi.getCiState());
		newCi.setCiStateId(baseCi.getCiStateId());
		newCi.setImpl(baseCi.getImpl());
		newCi.setCreated(baseCi.getCreated());
		newCi.setCreatedBy(baseCi.getCreatedBy());
		newCi.setLastAppliedRfcId(baseCi.getLastAppliedRfcId());
		newCi.setNsId(baseCi.getNsId());
		newCi.setNsPath(baseCi.getNsPath());
		newCi.setUpdated(baseCi.getUpdated());
		newCi.setUpdatedBy(baseCi.getUpdatedBy());
		
		for (CmsCIAttribute attr : baseCi.getAttributes().values()){
			CmsCIAttribute newAttr = new CmsCIAttribute();
			newAttr.setAttributeId(attr.getAttributeId());
			newAttr.setAttributeName(attr.getAttributeName());
			newAttr.setComments(attr.getComments());
			newAttr.setDfValue(attr.getDfValue());
			newAttr.setDjValue(attr.getDjValue());
			newCi.addAttribute(newAttr);
		}
		
		return newCi;
	}

	
	
	public CmsCIRelation cloneCIRelationBasic(CmsCIRelation baseRel) {

		CmsCIRelation newRrel = new CmsCIRelation();
		
		newRrel.setRelationId(baseRel.getRelationId());
		newRrel.setRelationName(baseRel.getRelationName());
	    
	    for (CmsCIRelationAttribute baseAttr : baseRel.getAttributes().values()) {
	    	CmsCIRelationAttribute relAttr = new CmsCIRelationAttribute();
	    	relAttr.setAttributeId(baseAttr.getAttributeId());
	    	relAttr.setAttributeName(baseAttr.getAttributeName());
	    	relAttr.setDfValue(baseAttr.getDfValue());
	    	relAttr.setDjValue(baseAttr.getDjValue());
	    	relAttr.setOwner(relAttr.getOwner());
	    	newRrel.addAttribute(relAttr);
	    }
	    
	    return newRrel;
	}
	
	
	public void applyRelationToRfc(CmsRfcRelation newRfc, CmsCIRelation ciRel, Map<String, CmsRelationAttribute> mdAttrs, boolean checkExpression, String owner) {

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
			    			if (owner != null) newRfc.getAttribute(mgmtAttr.getAttributeName()).setOwner(owner);
			    			
			    		} else {
				    		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
				    		rfcAttr.setAttributeId(mgmtAttr.getAttributeId());
				    		rfcAttr.setAttributeName(mgmtAttr.getAttributeName());
				    		rfcAttr.setNewValue(mgmtAttr.getDfValue());
				    		if (owner != null) rfcAttr.setOwner(owner);
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

	public CmsRfcRelation bootstrapRelationRfc(long fromCiId, long toCiId, String relName, String nsPath, String releaseNsPath, Set<String> attrs) {
		CmsRfcRelation newRfc = new CmsRfcRelation();
		newRfc.setNsPath(nsPath);
		newRfc.setReleaseNsPath(releaseNsPath);
		
		CmsRelation targetRelation = mdProcessor.getRelation(relName);
		
		newRfc.setRelationId(targetRelation.getRelationId());
		newRfc.setRelationName(targetRelation.getRelationName());
		
		//bootstrap the default values from Class definition
	    for (CmsRelationAttribute relAttr : targetRelation.getMdAttributes()) {
	    	if (relAttr.getDefaultValue() != null || (attrs != null && attrs.contains(relAttr.getAttributeName()))) {
	    		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
	    		rfcAttr.setAttributeId(relAttr.getAttributeId());
	    		rfcAttr.setAttributeName(relAttr.getAttributeName());
	    		rfcAttr.setNewValue(relAttr.getDefaultValue());
	    		newRfc.addAttribute(rfcAttr);
	    	}
	    }
    
	    newRfc.setFromCiId(fromCiId);
	    newRfc.setToCiId(toCiId);
		return newRfc;
		
	}

	public CmsRfcCI bootstrapRfc(String ciName,String className, String nsPath, String releaseNsPath, Set<String> attrsToBootstrap) {
		
		CmsRfcCI newRfc = new CmsRfcCI();
		newRfc.setNsPath(nsPath);
		newRfc.setCiName(ciName);
		newRfc.setReleaseNsPath(releaseNsPath);
		
		CmsClazz targetClazz = mdProcessor.getClazz(className);
		
		newRfc.setCiClassId(targetClazz.getClassId());
		newRfc.setCiClassName(className);
		
	    for (CmsClazzAttribute clAttr : targetClazz.getMdAttributes()) {
	    	if (clAttr.getDefaultValue() != null || 
	    			(attrsToBootstrap!=null && attrsToBootstrap.contains(clAttr.getAttributeName()))) {
	    		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
	    		rfcAttr.setAttributeId(clAttr.getAttributeId());
	    		rfcAttr.setAttributeName(clAttr.getAttributeName());
	    		rfcAttr.setNewValue(clAttr.getDefaultValue());
	    		newRfc.addAttribute(rfcAttr);
	    	}
	    }
	    
		return newRfc;
	}

	
	
	public CmsRfcRelation bootstrapRelationRfcWithAttrs(long fromCiId, long toCiId, String relName, String nsPath, String releaseNsPath, Map<String, CmsCIRelationAttribute> baseAttrs) {
		CmsRfcRelation newRfc = new CmsRfcRelation();
		newRfc.setNsPath(nsPath);
		newRfc.setReleaseNsPath(releaseNsPath);
		
		CmsRelation targetRelation = mdProcessor.getRelation(relName);
		
		newRfc.setRelationId(targetRelation.getRelationId());
		newRfc.setRelationName(targetRelation.getRelationName());
		
		//bootstrap the default values from Class definition
	    for (CmsRelationAttribute relAttr : targetRelation.getMdAttributes()) {
	    	if (relAttr.getDefaultValue() != null || (baseAttrs != null && baseAttrs.containsKey(relAttr.getAttributeName()))) {
	    		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
	    		rfcAttr.setAttributeId(relAttr.getAttributeId());
	    		rfcAttr.setAttributeName(relAttr.getAttributeName());
	    		if (baseAttrs != null && baseAttrs.containsKey(relAttr.getAttributeName())) {
	    			rfcAttr.setNewValue(baseAttrs.get(relAttr.getAttributeName()).getDjValue());
	    		} else {
	    			rfcAttr.setNewValue(relAttr.getDefaultValue());
	    		}
	    		newRfc.addAttribute(rfcAttr);
	    	}
	    }
    
	    newRfc.setFromCiId(fromCiId);
	    newRfc.setToCiId(toCiId);
		return newRfc;
		
	}
	
	public CmsCIRelation bootstrapRelation(long fromCiId, long toCiId, String relName, String nsPath) {
		CmsCIRelation newRel = new CmsCIRelation();
		newRel.setNsPath(nsPath);
		
		CmsRelation targetRelation = mdProcessor.getRelation(relName);
		
		newRel.setRelationId(targetRelation.getRelationId());
		newRel.setRelationName(targetRelation.getRelationName());
		
		//bootstrap the default values from Class definition
	    for (CmsRelationAttribute mdRelAttr : targetRelation.getMdAttributes()) {
	    	if (mdRelAttr.getDefaultValue() != null) {
	    		CmsCIRelationAttribute relAttr = new CmsCIRelationAttribute();
	    		relAttr.setAttributeId(mdRelAttr.getAttributeId());
	    		relAttr.setAttributeName(mdRelAttr.getAttributeName());
	    		relAttr.setDfValue(mdRelAttr.getDefaultValue());
	    		relAttr.setDjValue(mdRelAttr.getDefaultValue());
	    		newRel.addAttribute(relAttr);
	    	}
	    }
    
	    newRel.setFromCiId(fromCiId);
	    newRel.setToCiId(toCiId);
		return newRel;
		
	}

	public CmsCI bootstrapCi(String ciClassName, String ciName, String user, String nsPath, String desc, Set<String> attrs) {
		
		CmsClazz cl = mdProcessor.getClazz(ciClassName);
		
		CmsCI newCi = new CmsCI();
		newCi.setCiName(ciName);
		newCi.setNsPath(nsPath);
		newCi.setCiClassName(ciClassName);
		newCi.setCiClassId(cl.getClassId());
		newCi.setComments(desc);
		newCi.setCreatedBy(user);
		newCi.setUpdatedBy(user);
		
		//bootstrap the attrs from the set so the client can populate the values
	    for (String attrName : attrs) {
	    	CmsCIAttribute attr = new CmsCIAttribute();
	    	attr.setAttributeName(attrName);
	    	newCi.addAttribute(attr);
	    }
    
		return newCi;
		
	}

	
	public CmsCI getOrgByScope(String scope) {
		
		String[] scopeParts = scope.split("/");
		String orgName = scopeParts[1];

		List<CmsCI> orgs = cmProcessor.getCiBy3("/", "account.Organization", orgName);
		
		if (orgs.size()>0) {
			return orgs.get(0);
		} else {
			return null;
		}
	}

	public CmsCI getAssemblyByPlatformNsPath(String nsPath) {
		
		String[] pathParts = nsPath.split("/");
		if (pathParts.length<3) {
			String errorStr = "Bad nsPath for paltform (needs to be in form /org/assembly): " + nsPath;
			logger.error(errorStr);
			throw new TransistorException(CmsError.TRANSISTOR_BAD_NS_PATH, errorStr);
		}
		String orgName = pathParts[1];
		String assemblyName = pathParts[2];

		List<CmsCI> assemblies = cmProcessor.getCiBy3("/" + orgName, "account.Assembly", assemblyName);
		
		if (assemblies.size()>0) {
			return assemblies.get(0);
		} else {
			return null;
		}
	}
	
	public String getPlatformBaseNS(String nsPath) {
		String baseNsPath = "";
		if (nsPath != null) {
			String[] nsParts = nsPath.split("/");
			for (int i=1; i< nsParts.length-1; i++) {
				baseNsPath += "/" + nsParts[i];
			}
		}
		return baseNsPath;
	}
	
	public void verifyScope(CmsRfcCI ci, String scope) {
		if (scope != null && !ci.getNsPath().startsWith(scope)) {
			String error = "bad scope";
			logger.error(error);
			throw new TransistorException(CmsError.TRANSISTOR_BAD_SCOPE, error);
		}		
	}
	
	public void verifyScope(CmsCI ci, String scope) {
		if (scope != null && !ci.getNsPath().startsWith(scope)) {
			String error = "bad scope";
			logger.error(error);
			throw new TransistorException(CmsError.TRANSISTOR_BAD_SCOPE, error);
		}		
	}
	
	public void lockNS(String nsPath) {
		nsManager.lockNs(nsPath);
	}
	
	public Map<String,String> keyGen(String passPhrase, String pubDesc) {
		JSch jsch=new JSch();
        String passphrase= (passPhrase == null) ? "" : passPhrase;
        Map<String,String> result = new HashMap<String,String>();
        try{
	        KeyPair kpair=KeyPair.genKeyPair(jsch, KeyPair.RSA, 2048);
	        kpair.setPassphrase(passphrase);
	        OutputStream prkos = new ByteArrayOutputStream();
	        kpair.writePrivateKey(prkos);
	        String privateKey = prkos.toString();
	        //removing "\n" at the end of the string
	        result.put("private", privateKey.substring(0, privateKey.length() - 1));
	        OutputStream pubkos = new ByteArrayOutputStream();
	        kpair.writePublicKey(pubkos, pubDesc);
	        String pubKey = pubkos.toString();
	        //removing "\n" at the end of the string
	        result.put("public", pubKey.substring(0, pubKey.length() - 1));
	        kpair.dispose();
	        return result;
        } catch(Exception e){
        	System.out.println(e);
        	logger.error(e.getMessage());
        	throw new TransistorException(CmsError.TRANSISTOR_EXCEPTION, e.getMessage());
        }
	}

	public void processAllVars(CmsCI ci, Map<String,String> cloudVars, Map<String,String> globalVars, Map<String,String> localVars) {
		cmsUtil.processAllVars(ci, cloudVars, globalVars, localVars);
	}

}
