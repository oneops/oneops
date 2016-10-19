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

import com.oneops.cms.cm.domain.CmsBasicAttribute;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.crypto.CmsCrypto;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAttribute;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.md.domain.CmsRelationAttribute;
import com.oneops.cms.md.service.CmsMdManager;
import com.oneops.cms.ns.domain.CmsNamespace;
import com.oneops.cms.ns.service.CmsNsManager;

/**
 * The Class CmsCmValidator.
 */
public class CmsCmValidator {

	private static final String CAN_NOT_ENCRYPT_ATTRIBUTE = "Can not encrypt attribute ";
	private static final int STATE_100 = 100;
	private static final String ATTRIBUTE = "Attribute ";
	private CmsMdManager mdManager;
    private CmsNsManager nsManager;
    private CmsCrypto cmsCrypto;
	
	
	/**
	 * Sets the md manager.
	 *
	 * @param mdManager the new md manager
	 */
	public void setMdManager(CmsMdManager mdManager) {
		this.mdManager = mdManager;
	}

	/**
	 * Sets the ns manager.
	 *
	 * @param nsManager the new ns manager
	 */
	public void setNsManager(CmsNsManager nsManager) {
		this.nsManager = nsManager;
	}

	/**
	 * Sets the cms crypto.
	 *
	 * @param cmsCrypto the new cms crypto
	 */
	public void setCmsCrypto(CmsCrypto cmsCrypto) {
		this.cmsCrypto = cmsCrypto;
	}

	private static final String reservedCiNames = "_design|bom|manifest"; 
	
	/**
	 * Validate relation.
	 *
	 * @param ciRelation the ci relation
	 * @return the cI validation result
	 */
	public CIValidationResult validateRelation(CmsCIRelation ciRelation) {

		CIValidationResult result = new CIValidationResult();
		result.setValidated(true);

		CmsRelation relationClazz = getRelationClazz(ciRelation.getRelationName());
		if (relationClazz == null) {
			result.setValidated(false);
			result.setErrorMsg("There is no relation type definition for " + ciRelation.getRelationName());
			return result;
		}

		if (ciRelation.getFromCiId() == 0) {
			result.setValidated(false);
			result.setErrorMsg("There is no from ciId on relation " + ciRelation.getRelationName());
			return result;
		}
		
		if (ciRelation.getToCiId() == 0) {
			result.setValidated(false);
			result.setErrorMsg("There is no to ciId on relation " + ciRelation.getRelationName());
			return result;
		}

		if (ciRelation.getNsPath() == null) {
			result.setValidated(false);
			result.setErrorMsg("The namespace must be specified");
			return result;
		}
		
		CmsNamespace ns = getNs(ciRelation.getNsPath());
		if (ns == null) {
			result.setValidated(false);
			result.setErrorMsg("Can not find namespace id for namespace " + ciRelation.getNsPath());
			return result;
		}
		ciRelation.setNsId(ns.getNsId());
		
		//validate attributes
		// first lets check if all required attrs present
		Map<String, CmsRelationAttribute> relationAttrsMap = new HashMap<String, CmsRelationAttribute>(); 
		for (CmsRelationAttribute relationAttr : relationClazz.getMdAttributes()) {
			relationAttrsMap.put(relationAttr.getAttributeName(), relationAttr);
			if (relationAttr.isMandatory() && (ciRelation.getAttribute(relationAttr.getAttributeName()) == null)) {
				result.setValidated(false);
				result.addErrorMsg(ATTRIBUTE + relationAttr.getAttributeName() + " is required for the relation type of " + relationClazz.getRelationName());
			}
		}
		// now lets check if some attrs does not belongs here
		for (CmsCIRelationAttribute ciRelAttr : ciRelation.getAttributes().values()) {
			if (!relationAttrsMap.containsKey(ciRelAttr.getAttributeName())) {
				result.setValidated(false);
				result.addErrorMsg(ATTRIBUTE + ciRelAttr.getAttributeName() + " is not defined in relation " + relationClazz.getRelationName());
			} else {
				ciRelAttr.setAttributeId(relationAttrsMap.get(ciRelAttr.getAttributeName()).getAttributeId());
			}
			
		}
		
		if (!result.isValidated()){
			return result;
		}
		
		ciRelation.setRelationId(relationClazz.getRelationId());
		ciRelation.setRelationStateId(STATE_100);
		
		return result;
	}
	
	/**
	 * Validate new ci.
	 *
	 * @param ci the ci
	 * @return the cI validation result
	 */
	public CIValidationResult validateNewCI(CmsCI ci) {
		
		CIValidationResult result = new CIValidationResult();
		result.setValidated(true);
		
		CmsClazz clazz = getClazz(ci.getCiClassName());
		if (clazz == null) {
			result.setValidated(false);
			result.setErrorMsg("There is no class definition for " + ci.getCiClassName());
			return result;
		}
		
		if (clazz.getIsNamespace() && ci.getCiName().matches(reservedCiNames)) {
			result.setValidated(false);
			result.setErrorMsg("The ci name " + ci.getCiName() + " is a OneOps reserved word!");
			return result;
		}
		
		CmsNamespace ns = getNs(ci.getNsPath());
		if (ns == null) {
			result.setValidated(false);
			result.setErrorMsg("The namespace must be specified");
			return result;
		}
	
		//validate attributes
		// first lets check if all required attrs present
		Map<String, CmsClazzAttribute> clazzAttrsMap = new HashMap<String, CmsClazzAttribute>(); 
		for (CmsClazzAttribute clazzAttr : clazz.getMdAttributes()) {
			clazzAttrsMap.put(clazzAttr.getAttributeName(), clazzAttr);
			
			if (clazzAttr.getIsMandatory() && clazzAttr.getDefaultValue() == null && (ci.getAttribute(clazzAttr.getAttributeName()) == null)) {
				result.setValidated(false);
				result.addErrorMsg(ATTRIBUTE + clazzAttr.getAttributeName() + " is required for ci type of " + clazz.getClassName());
			} else if(clazzAttr.getDefaultValue() != null && (ci.getAttribute(clazzAttr.getAttributeName()) == null)) {
				CmsCIAttribute ciAttr = new CmsCIAttribute();
				ciAttr.setAttributeId(clazzAttr.getAttributeId());
				ciAttr.setAttributeName(clazzAttr.getAttributeName());
				ciAttr.setCiId(ci.getCiId());
				ciAttr.setDfValue(clazzAttr.getDefaultValue());
				ciAttr.setDjValue(clazzAttr.getDefaultValue());
			}
		}
		// now lets check if some attrs does not belongs here
		for (CmsCIAttribute attr : ci.getAttributes().values()) {
			if (!clazzAttrsMap.containsKey(attr.getAttributeName())) {
				result.setValidated(false);
				result.addErrorMsg(ATTRIBUTE + attr.getAttributeName() + " is not defined in class " + clazz.getClassName());
			} else {
				attr.setAttributeId(clazzAttrsMap.get(attr.getAttributeName()).getAttributeId());
			}
		}

		if (!result.isValidated()) {
			return result;
		}
	
		// now lets encrypt the attrs that needs to be encrypted
		encryptNewCiAttrs(clazzAttrsMap, ci, result);
		if (!result.isValidated()){
			return result;
		}
		
		ci.setCiClassId(clazz.getClassId());
		ci.setNsId(ns.getNsId());
		result.setNeedNScreation(clazz.getIsNamespace());
		result.setUseClassNameInNS(clazz.getUseClassNameNS());
		
		return result;
	}

	private void encryptNewCiAttrs(Map<String, CmsClazzAttribute> clazzAttrsMap, CmsCI ci, CIValidationResult encResult) {
		for (CmsCIAttribute attr : ci.getAttributes().values()) {
			try {
				if (clazzAttrsMap.get(attr.getAttributeName()).getIsEncrypted()) {
					if (attr.getDfValue() != null) {
						if (attr.getDfValue().equalsIgnoreCase(CmsCrypto.ENC_DUMMY)) {
							encResult.setValidated(false);
							encResult.addErrorMsg("Bad DF value passed for encryption on new CI attribute" + attr.getAttributeName() + "!");
						} else if (!attr.getDfValue().startsWith(CmsCrypto.ENC_PREFIX)) {
							attr.setDfValue(cmsCrypto.encrypt(attr.getDfValue()));
						}	
					}
					if (attr.getDjValue() != null) {
						if (attr.getDjValue().equalsIgnoreCase(CmsCrypto.ENC_DUMMY)) {
							encResult.setValidated(false);
							encResult.addErrorMsg("Bad DJ value passed for encryption on new CI attribute" + attr.getAttributeName() + "!");
						} else if (!attr.getDjValue().startsWith(CmsCrypto.ENC_PREFIX)) {
							attr.setDjValue(cmsCrypto.encrypt(attr.getDjValue()));
						}	
					}
				}
			} catch (GeneralSecurityException | IOException e) {
				encResult.setValidated(false);
				encResult.addErrorMsg(CAN_NOT_ENCRYPT_ATTRIBUTE + attr.getAttributeName() + " " + e.getMessage());
			}
		}
	}
	
	/**
	 * Validate update ci.
	 *
	 * @param ci the ci
	 * @return the cI validation result
	 */
	public CIValidationResult validateUpdateCI(CmsCI ci) {
		
		CIValidationResult result = new CIValidationResult();
		result.setValidated(true);
		
		CmsClazz clazz = getClazz(ci.getCiClassName());
		if (clazz == null) {
			result.setValidated(false);
			result.setErrorMsg("There is no class definition for " + ci.getCiClassName());
			return result;
		}
	
		//validate attributes
		// first lets create a map of clazz attributes
		Map<String, CmsClazzAttribute> clazzAttrsMap = new HashMap<String, CmsClazzAttribute>(); 
		for (CmsClazzAttribute clazzAttr : clazz.getMdAttributes()) {
			clazzAttrsMap.put(clazzAttr.getAttributeName(), clazzAttr);
		}
		// now lets check if some attrs does not belongs here
		for (CmsCIAttribute attr : ci.getAttributes().values()) {
			if (!clazzAttrsMap.containsKey(attr.getAttributeName())) {
				result.setValidated(false);
				result.addErrorMsg(ATTRIBUTE + attr.getAttributeName() + " is not defined in class " + clazz.getClassName());
			} else {
				attr.setAttributeId(clazzAttrsMap.get(attr.getAttributeName()).getAttributeId());
			}
			
		}
		
		if (!result.isValidated()) {
			return result;
		}
		
		encryptUpdateCiAttrs(clazzAttrsMap, ci, result);
		
		return result;
	}

	private void encryptUpdateCiAttrs(Map<String, CmsClazzAttribute> clazzAttrsMap, CmsCI ci, CIValidationResult encResult) {
		
		List <String> noUpdateAttrs = new ArrayList<String>();
		
		for (CmsCIAttribute attr : ci.getAttributes().values()) {
			try {
				if (clazzAttrsMap.get(attr.getAttributeName()).getIsEncrypted()) {
					if (attr.getDfValue() != null ) {
						if (attr.getDfValue().equalsIgnoreCase(CmsCrypto.ENC_DUMMY)) {
							noUpdateAttrs.add(attr.getAttributeName());
						} else if (!attr.getDfValue().startsWith(CmsCrypto.ENC_PREFIX) ) {
							if(!attr.getDfValue().startsWith(CmsCrypto.ENC_VAR_PREFIX))
								attr.setDfValue(cmsCrypto.encrypt(attr.getDfValue()));
						}
					}
					if (attr.getDjValue() != null) {
						if (attr.getDjValue().equalsIgnoreCase(CmsCrypto.ENC_DUMMY)) {
							noUpdateAttrs.add(attr.getAttributeName());
						} else if (!attr.getDjValue().startsWith(CmsCrypto.ENC_PREFIX)) {
							if(!attr.getDfValue().startsWith(CmsCrypto.ENC_VAR_PREFIX))
								attr.setDjValue(cmsCrypto.encrypt(attr.getDjValue()));
						}
					}
				}
			} catch (GeneralSecurityException | IOException   e) {
				encResult.setValidated(false);
				encResult.addErrorMsg(CAN_NOT_ENCRYPT_ATTRIBUTE + attr.getAttributeName() + " " + e.getMessage());
			}
		}
		
		for (String noUpdateAttr : noUpdateAttrs) {
			ci.getAttributes().remove(noUpdateAttr);
		}
	}
	
	
	private CmsClazz getClazz(String clazzName) {
		return mdManager.getClazz(clazzName);
	}

	private CmsRelation getRelationClazz(String relationName) {
		return mdManager.getRelation(relationName);
	}

	
	private CmsNamespace getNs(String nsPath){
		return nsManager.getNs(nsPath);
	}
	
	/**
	 * Cis equal.
	 *
	 * @param ci1 the ci1
	 * @param ci2 the ci2
	 * @return true, if successful
	 */
	public boolean cisEqual(CmsCI ci1, CmsCI ci2) {
		if (ci1 == null || ci2 == null){
			return false;
		}
		if (!(ci1.getCiName().equals(ci2.getCiName()))) {
			return false;
		}
		if (!(ci2.getComments() != null && ci2.getComments().equals(ci1.getComments()))){
			return false;
		}
		return ci1.getCiStateId() == ci2.getCiStateId();
	}
	
	
	/**
	 * Attrs equal.
	 *
	 * @param attr1 the attr1
	 * @param attr2 the attr2
	 * @return true, if successful
	 */
	public boolean attrsEqual(CmsBasicAttribute attr1, CmsBasicAttribute attr2, boolean checkOwner) {
		if (attr1 == null || attr2 == null) {
			return false;
		}
		if (!equalStrs(attr1.getDfValue(), attr2.getDfValue())){
			return false;
		}
		if (!equalStrs(attr1.getDjValue(), attr2.getDjValue())){
			return false;
		}
		if (checkOwner && !equalStrs(attr1.getOwner(), attr2.getOwner())){
			return false;
		}
		
		return true;
	}
	
	
	public boolean equalStrs(String str1, String str2) {
		if (str1 == null && str2 == null) {
			return true;
		}
		if (str1 != null && str1.equals(str2)) {
			return true;
		}
		if (str2 != null && str2.equals(str1)) {
			return true;
		}
		return false;
	}
	
	
}
