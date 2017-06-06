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
package com.oneops.cms.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oneops.cms.crypto.CmsCrypto;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAttribute;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.md.domain.CmsRelationAttribute;
import com.oneops.cms.md.service.CmsMdManager;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.ns.domain.CmsNamespace;
import com.oneops.cms.ns.service.CmsNsManager;
import com.oneops.cms.ns.service.CmsNsProcessor;

/**
 * The Class CmsDJValidator.
 */
public class CmsDJValidator {

	private CmsMdProcessor cmsMdProcessor;
    private CmsNsProcessor cmsNsProcessor;
    private CmsCrypto cmsCrypto;

	public void setCmsMdProcessor(CmsMdProcessor cmsMdProcessor) {
		this.cmsMdProcessor = cmsMdProcessor;
	}

	public void setCmsNsProcessor(CmsNsProcessor cmsNsProcessor) {
		this.cmsNsProcessor = cmsNsProcessor;
	}

	/**
	 * Sets the cms crypto.
	 *
	 * @param cmsCrypto the new cms crypto
	 */
	public void setCmsCrypto(CmsCrypto cmsCrypto) {
		this.cmsCrypto = cmsCrypto;
	}
	
	/**
	 * Validate rfc ci.
	 *
	 * @param rfcCi the rfc ci
	 * @return the cI validation result
	 */
	public CIValidationResult validateRfcCi(CmsRfcCI rfcCi) {
		
		CIValidationResult result = new CIValidationResult();
		result.setValidated(true);

		if (rfcCi.getCiName() == null || rfcCi.getCiName().length()==0) {
			result.setValidated(false);
			result.setErrorMsg("CI Name can not be empty!");
			return result;
		}
		
		CmsClazz clazz = getClazz(rfcCi.getCiClassName());
		if (clazz == null) {
			result.setValidated(false);
			result.setErrorMsg("There is no class definition for " + rfcCi.getCiClassName());
			return result;
		}
		rfcCi.setCiClassId(clazz.getClassId());
		
		if (rfcCi.getNsId() == 0) {
			CmsNamespace ns = getNs(rfcCi.getNsPath());
			if (ns == null) {
				result.setValidated(false);
				result.setErrorMsg("The namespace must be specified");
				return result;
			}
			rfcCi.setNsId(ns.getNsId());
		}
		Map<String, CmsClazzAttribute> clazzAttrsMap = new HashMap<String, CmsClazzAttribute>(); 
		for (CmsClazzAttribute clazzAttr : clazz.getMdAttributes()) {
			clazzAttrsMap.put(clazzAttr.getAttributeName(), clazzAttr);
		}

		//validate attributes
		// if it's add then first lets check if all required attrs present
		if ("add".equalsIgnoreCase(rfcCi.getRfcAction())) {
			for (CmsClazzAttribute clazzAttr : clazz.getMdAttributes()) {
				if (clazzAttr.getIsMandatory() && (rfcCi.getAttribute(clazzAttr.getAttributeName()) == null)) {
					result.setValidated(false);
					result.addErrorMsg("Attribute " + clazzAttr.getAttributeName() + " is required for ci type of " + clazz.getClassName());
				}
			}
		}
		// now lets check if some attrs does not belongs here
		for (CmsRfcAttribute attr : rfcCi.getAttributes().values()) {
			if (!clazzAttrsMap.containsKey(attr.getAttributeName())) {
				result.setValidated(false);
				result.addErrorMsg("Attribute " + attr.getAttributeName() + " is not defined in class " + clazz.getClassName());
			} else {
				attr.setAttributeId(clazzAttrsMap.get(attr.getAttributeName()).getAttributeId());
			}
		}
		
		if (!result.isValidated()) return result;
		
		encryptRfcAttrs(clazzAttrsMap, rfcCi, result);		

		return result;
	}

	/**
	 * Validate rfc ci attrs.
	 *
	 * @param rfcCi the rfc ci
	 * @return the cI validation result
	 */
	public CIValidationResult validateRfcCiAttrs(CmsRfcCI rfcCi) {
		
		CIValidationResult result = new CIValidationResult();
		result.setValidated(true);
		
		CmsClazz clazz = getClazz(rfcCi.getCiClassName());
		
		Map<String, CmsClazzAttribute> clazzAttrsMap = new HashMap<String, CmsClazzAttribute>(); 
		for (CmsClazzAttribute clazzAttr : clazz.getMdAttributes()) {
			clazzAttrsMap.put(clazzAttr.getAttributeName(), clazzAttr);
		}

		// now lets check if some attrs does not belongs here
		for (CmsRfcAttribute attr : rfcCi.getAttributes().values()) {
			if (!clazzAttrsMap.containsKey(attr.getAttributeName())) {
				result.setValidated(false);
				result.addErrorMsg("Attribute " + attr.getAttributeName() + " is not defined in class " + clazz.getClassName());
			} else {
				attr.setAttributeId(clazzAttrsMap.get(attr.getAttributeName()).getAttributeId());
			}
		}
		
		if (!result.isValidated()) return result;
		
		encryptRfcAttrs(clazzAttrsMap,rfcCi, result);
		return result;
	}
	
	
	/**
	 * Validate rfc relation.
	 *
	 * @param rfcRelation the rfc relation
	 * @param fromClassId the from class id
	 * @param toClassId the to class id
	 * @return the cI validation result
	 */
	public CIValidationResult validateRfcRelation(CmsRfcRelation rfcRelation, int fromClassId, int toClassId) {

		CIValidationResult result = new CIValidationResult();
		result.setValidated(true);

		CmsRelation relationClazz = getRelationClazzWithTargets(rfcRelation.getRelationName(), fromClassId, toClassId);
		if (relationClazz == null) {
			result.setValidated(false);
			result.setErrorMsg("There is no relation type definition for " + rfcRelation.getRelationName());
			return result;
		}
		
		if (relationClazz.getTargets().size()==0) {
			result.setValidated(false);
			CmsClazz fromClazz = cmsMdProcessor.getClazz(fromClassId);
			CmsClazz toClazz = cmsMdProcessor.getClazz(toClassId);
			
			result.setErrorMsg(" Targets do not exists for relation type " + rfcRelation.getRelationName() + "; fromClass=" + fromClazz.getClassName() + "; toClass=" + toClazz.getClassName());
			return result;
		}

		if (rfcRelation.getNsId() == 0) {
			CmsNamespace ns = getNs(rfcRelation.getNsPath());
			if (ns == null) {
				result.setValidated(false);
				result.setErrorMsg("The namespace must be specified");
				return result;
			}
			rfcRelation.setNsId(ns.getNsId());
		}
		
		Map<String, CmsRelationAttribute> clazzAttrsMap = new HashMap<String, CmsRelationAttribute>(); 
		for (CmsRelationAttribute clazzAttr : relationClazz.getMdAttributes()) {
			clazzAttrsMap.put(clazzAttr.getAttributeName(), clazzAttr);
		}
		
		//validate attributes
		// if it's add then first lets check if all required attrs present
		if ("add".equalsIgnoreCase(rfcRelation.getRfcAction())) {
			for (CmsRelationAttribute clazzAttr : relationClazz.getMdAttributes()) {
				if (clazzAttr.isMandatory() && (rfcRelation.getAttribute(clazzAttr.getAttributeName()) == null)) {
					result.setValidated(false);
					result.addErrorMsg("Attribute " + clazzAttr.getAttributeName() + " is required for relation type of " + relationClazz.getRelationName());
				}
			}
		}
		// now lets check if some attrs does not belongs here
		for (CmsRfcAttribute attr : rfcRelation.getAttributes().values()) {
			if (!clazzAttrsMap.containsKey(attr.getAttributeName())) {
				result.setValidated(false);
				result.addErrorMsg("Attribute " + attr.getAttributeName() + " is not defined in class " + relationClazz.getRelationName());
			} else {
				attr.setAttributeId(clazzAttrsMap.get(attr.getAttributeName()).getAttributeId());
			}
		}
		
		if (!result.isValidated()) return result;
		
		rfcRelation.setRelationId(relationClazz.getRelationId());
		
		return result;
	}

	/**
	 * Validate rfc relation attrs.
	 *
	 * @param rfcRelation the rfc relation
	 * @return the cI validation result
	 */
	public CIValidationResult validateRfcRelationAttrs(CmsRfcRelation rfcRelation) {

		CIValidationResult result = new CIValidationResult();
		result.setValidated(true);
	
		CmsRelation relationClazz = getRelationClazz(rfcRelation.getRelationName());
		
		Map<String, CmsRelationAttribute> clazzAttrsMap = new HashMap<String, CmsRelationAttribute>(); 
		for (CmsRelationAttribute clazzAttr : relationClazz.getMdAttributes()) {
			clazzAttrsMap.put(clazzAttr.getAttributeName(), clazzAttr);
		}
		
		// now lets check if some attrs does not belongs here
		for (CmsRfcAttribute attr : rfcRelation.getAttributes().values()) {
			if (!clazzAttrsMap.containsKey(attr.getAttributeName())) {
				result.setValidated(false);
				result.addErrorMsg("Attribute " + attr.getAttributeName() + " is not defined in class " + relationClazz.getRelationName());
			} else {
				attr.setAttributeId(clazzAttrsMap.get(attr.getAttributeName()).getAttributeId());
			}
		}
		
		if (!result.isValidated()) return result;
		
		return result;
	}
	
	private void encryptRfcAttrs(Map<String, CmsClazzAttribute> clazzAttrsMap, CmsRfcCI rfcCi, CIValidationResult encResult) {

		List <String> noUpdateAttrs = new ArrayList<String>();

		for (CmsRfcAttribute attr : rfcCi.getAttributes().values()) {
			try {
				if (clazzAttrsMap.get(attr.getAttributeName()).getIsEncrypted()) {
					if (attr.getNewValue() != null ) { 
						if (attr.getNewValue().equalsIgnoreCase(CmsCrypto.ENC_DUMMY)) {
							if (rfcCi.getRfcAction().equalsIgnoreCase("add")) {
								encResult.setValidated(false);
								encResult.addErrorMsg("Bad encrypted value passed on add rfc " + attr.getAttributeName() + "!");
							} else {
								noUpdateAttrs.add(attr.getAttributeName());
							}
						} else if (!attr.getNewValue().startsWith(CmsCrypto.ENC_PREFIX)) {
							attr.setNewValue(cmsCrypto.encrypt(attr.getNewValue()));
						}
					}
				}
			} catch (GeneralSecurityException | IOException e) {
				encResult.setValidated(false);
				encResult.addErrorMsg("Can not encrypt attribute " + attr.getAttributeName() + " " + e.getMessage());
			}
		}

		for (String noUpdateAttr : noUpdateAttrs) {
			rfcCi.getAttributes().remove(noUpdateAttr);
		}
	
	}

	
	/**
	 * Rfc cis equal.
	 *
	 * @param ci1 the ci1
	 * @param ci2 the ci2
	 * @return true, if successful
	 */
	public boolean rfcCisEqual(CmsRfcCI ci1, CmsRfcCI ci2) {
		if (ci1 == null || ci2 == null) return false;
		if (!(ci1.getCiName().equals(ci2.getCiName()))) return false;
		if (!(ci2.getComments() != null && ci2.getComments().equals(ci1.getComments()))) return false;
		return ci1.getExecOrder() == ci2.getExecOrder();
	}
	
	/**
	 * Rfc attrs equal.
	 *
	 * @param attr1 the attr1
	 * @param attr2 the attr2
	 * @return true, if successful
	 */
	public boolean rfcAttrsEqual(CmsRfcAttribute attr1, CmsRfcAttribute attr2) {
		if (attr1 == null || attr2 == null) return false;
		if (attr1.getOwner() != null && attr1.getOwner().length()==0) attr1.setOwner(null);
		if (attr2.getOwner() != null && attr2.getOwner().length()==0) attr2.setOwner(null);
		if (!equalStrs(attr1.getOwner(), attr2.getOwner())) return false;
		if (!equalStrs(attr1.getNewValue(), attr2.getNewValue())) return false;
		return true;
	}

	/**
	 * Compares 2 attr values.
	 *
	 * @param str1 the attr1
	 * @param str2 the attr2
	 * @return true, if equal
	 */

	public boolean equalStrs(String str1, String str2) {
		if (str1 == null && str2 == null) return true;
		if (str1 != null && str1.equals(str2)) return true;
		if (str2 != null && str2.equals(str1)) return true;
		return false;
	}
	
	
	
	private CmsClazz getClazz(String clazzName) {
		return cmsMdProcessor.getClazz(clazzName);
	}

	private CmsRelation getRelationClazz(String relationName) {
		return cmsMdProcessor.getRelation(relationName);
	}
	
	/*
	private CmsRelation getRelationClazzWithTargets(String relationName) {
		return cmsMdProcessor.getRelationWithTargets(relationName);
	}
	*/
	
	private CmsRelation getRelationClazzWithTargets(String relationName, int fromClassId, int toClassId) {
		return cmsMdProcessor.getRelationWithTargets(relationName, fromClassId, toClassId);
	}

	
	private CmsNamespace getNs(String nsPath){
		return cmsNsProcessor.getNs(nsPath);
	}

	
}
