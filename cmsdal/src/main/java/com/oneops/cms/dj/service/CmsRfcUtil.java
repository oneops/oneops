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
package com.oneops.cms.dj.service;

import com.oneops.cms.cm.domain.CmsBasicAttribute;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;

/**
 * The Class CmsRfcUtil.
 */
public class CmsRfcUtil {

	/**
	 * Merge rfc rel and ci rel.
	 *
	 * @param rfcRel the rfc rel
	 * @param ciRel the ci rel
	 * @param cmAttrValue the cm attr value
	 * @return the cms rfc relation
	 */
	public CmsRfcRelation mergeRfcRelAndCiRel(CmsRfcRelation rfcRel, CmsCIRelation ciRel, String cmAttrValue) {
		return mergeRfcRelAndCiRel(rfcRel, ciRel, cmAttrValue, false);
	}	
	
	/**
	 * Merge rfc rel and ci rel.
	 *
	 * @param rfcRel the rfc rel
	 * @param ciRel the ci rel
	 * @param cmAttrValue the cm attr value
	 * @param applyDeletes the apply deletes
	 * @return the cms rfc relation
	 */
	public CmsRfcRelation mergeRfcRelAndCiRel(CmsRfcRelation rfcRel, CmsCIRelation ciRel, String cmAttrValue, boolean applyDeletes) {
		if (rfcRel == null && ciRel == null) return null;
		
		if (applyDeletes && rfcRel != null) {
			if ("delete".equals(rfcRel.getRfcAction())) return null;
		}
		
		if (ciRel != null) {

			if (rfcRel == null) {
				rfcRel = new CmsRfcRelation();
				rfcRel.setCiRelationId(ciRel.getCiRelationId());
				rfcRel.setFromCiId(ciRel.getFromCiId());
				rfcRel.setRelationName(ciRel.getRelationName());
				rfcRel.setRelationId(ciRel.getRelationId());
				rfcRel.setToCiId(ciRel.getToCiId());
				rfcRel.setRelationGoid(ciRel.getRelationGoid());
				rfcRel.setCreated(ciRel.getCreated());
				rfcRel.setNsId(ciRel.getNsId());
				rfcRel.setNsPath(ciRel.getNsPath());
			}
			
			if (rfcRel.getComments() == null) rfcRel.setComments(ciRel.getComments());
			
			for (CmsBasicAttribute attr : ciRel.getAttributes().values()) {
				if (!rfcRel.getAttributes().containsKey(attr.getAttributeName())) {
					CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
					rfcAttr.setAttributeId(attr.getAttributeId());
					rfcAttr.setAttributeName(attr.getAttributeName());
					rfcAttr.setOwner(attr.getOwner());
					if ("df".equalsIgnoreCase(cmAttrValue)) {
						rfcAttr.setNewValue(attr.getDfValue());
					} else {
						rfcAttr.setNewValue(attr.getDjValue());
					}
					rfcRel.addAttribute(rfcAttr);
				}
			}
		}
		return rfcRel;
	}

	/**
	 * Merge rfc and ci.
	 *
	 * @param rfcCi the rfc ci
	 * @param ci the ci
	 * @param cmAttrValue the cm attr value
	 * @return the cms rfc ci
	 */
	public CmsRfcCI mergeRfcAndCi(CmsRfcCI rfcCi, CmsCI ci, String cmAttrValue) {
		if (rfcCi == null && ci == null) return null;
		
		//TODO check if we need to remove this
		//if (rfcCi != null) {
		//	if ("delete".equals(rfcCi.getRfcAction())) return null;
		//}
		
		if (ci != null) {
			if (rfcCi == null) rfcCi = new CmsRfcCI();
			rfcCi.setCiId(ci.getCiId());
			rfcCi.setCiClassName(ci.getCiClassName());
			rfcCi.setCiClassId(ci.getCiClassId());
			
			if (rfcCi.getCiName() == null) rfcCi.setCiName(ci.getCiName());
			if (rfcCi.getNsPath() == null) rfcCi.setNsPath(ci.getNsPath());
			if (rfcCi.getNsId() == 0) rfcCi.setNsId(ci.getNsId());
			if (rfcCi.getCiGoid() == null) rfcCi.setCiGoid(ci.getCiGoid());
			if (rfcCi.getComments() == null) rfcCi.setComments(ci.getComments());
			
			rfcCi.setLastAppliedRfcId(ci.getLastAppliedRfcId());
			rfcCi.setCiState(ci.getCiState());
			
			rfcCi.setRfcUpdated(rfcCi.getUpdated());
			rfcCi.setRfcCreated(rfcCi.getCreated());
			rfcCi.setRfcUpdatedBy(rfcCi.getUpdatedBy());
			rfcCi.setRfcCreatedBy(rfcCi.getCreatedBy());
			
			rfcCi.setCreated(ci.getCreated());
			rfcCi.setUpdated(ci.getUpdated());
			rfcCi.setCreatedBy(ci.getCreatedBy());
			rfcCi.setUpdatedBy(ci.getUpdatedBy());
			
	
			for (CmsBasicAttribute attr : ci.getAttributes().values()) {
				if (!rfcCi.getAttributes().containsKey(attr.getAttributeName())) {
					CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
					rfcAttr.setAttributeId(attr.getAttributeId());
					rfcAttr.setAttributeName(attr.getAttributeName());
					rfcAttr.setOwner(attr.getOwner());
					if ("df".equalsIgnoreCase(cmAttrValue)) {
						rfcAttr.setNewValue(attr.getDfValue());
					} else {
						rfcAttr.setNewValue(attr.getDjValue());
					}
					rfcCi.addAttribute(rfcAttr);
				} else {
					if ("df".equalsIgnoreCase(cmAttrValue)) {
						rfcCi.getAttributes().get(attr.getAttributeName()).setOldValue(attr.getDfValue());
					} else {
						rfcCi.getAttributes().get(attr.getAttributeName()).setOldValue(attr.getDjValue());
					}
				}
			}
		}
		return rfcCi;
	}

}
