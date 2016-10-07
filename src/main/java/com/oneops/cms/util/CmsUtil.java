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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.crypto.CmsCrypto;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.domain.CmsWorkOrder;
import com.oneops.cms.dj.service.CmsRfcUtil;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.exceptions.ExceptionConsolidator;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCIRelationSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsRfcRelationSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.domain.AttrQueryCondition;

/**
 * The Class CmsUtil.
 */
public class CmsUtil {

	public static final String CLOUD_VARS_PAYLOAD_NAME = "OO_CLOUD_VARS";
	public static final String GLOBAL_VARS_PAYLOAD_NAME = "OO_GLOBAL_VARS";
	public static final String LOCAL_VARS_PAYLOAD_NAME = "OO_LOCAL_VARS";

	public static final String VAR_SEC_ATTR_FLAG = "secure";
	public static final String VAR_SEC_ATTR_VALUE = "encrypted_value";
	public static final String VAR_UNSEC_ATTR_VALUE = "value";

	
	//private static final String GLOBALVARREGEX = "\\$OO_GLOBAL";
    //private static Pattern globalVarPattern = Pattern.compile(GLOBALVARREGEX);
	private static final String GLOBALVARPFX = "$OO_GLOBAL{";
	private static final String GLOBALVARRPL = "\\$OO_GLOBAL\\{";


	//private static final String LOCALVARREGEX = "\\$OO_LOCAL";
    //private static Pattern localVarPattern = Pattern.compile(LOCALVARREGEX);
	private static final String LOCALVARPFX = "$OO_LOCAL{";
	private static final String LOCALVARRPL = "\\$OO_LOCAL\\{";


	//private static final String CLOUDVARREGEX = "\\$OO_CLOUD";
    //private static Pattern cloudVarPattern = Pattern.compile(CLOUDVARREGEX);
	private static final String CLOUDVARPFX = "$OO_CLOUD{";
	private static final String CLOUDVARRPL = "\\$OO_CLOUD\\{";
	private static final String ATTR_PROP_OWNER = "owner";
	
	public static final String MASK = "##############";
	public static final String WORK_ORDER_TYPE = "deploybom";
	public static final String ACTION_ORDER_TYPE = "opsprocedure";
	
	public static final String DJ_ATTR = "dj";
	public static final String DF_ATTR = "df";
	
	private CmsCmProcessor cmProcessor;
	private CmsRfcUtil rfcUtil;
	
	private static final Logger logger = Logger.getLogger(CmsUtil.class);
	
	/**
	 * Sets the cm processor.
	 *
	 * @param cmProcessor the new cm processor
	 */
	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	/**
	 * Sets the rfc util.
	 *
	 * @param rfcUtil the new rfc util
	 */
	public void setRfcUtil(CmsRfcUtil rfcUtil){
		this.rfcUtil = rfcUtil;
	}

	public CmsRfcUtil getRfcUtil() {
		return rfcUtil;
	}
	/**
	 * Cust ci simple2 ci.
	 *
	 * @param ciSimple the ci simple
	 * @param valueType the value type
	 * @return the cms ci
	 */
	public CmsCI custCISimple2CI (CmsCISimple ciSimple, String valueType) {
		// TODO get the conversion right
		if (ciSimple == null) {
			return null;
		}
		CmsCI ci = new CmsCI();
		ci.setCiId(ciSimple.getCiId());
		ci.setCiClassName(ciSimple.getCiClassName());
		ci.setImpl(ciSimple.getImpl());
		ci.setCiGoid(ciSimple.getCiGoid());
		ci.setCiName(ciSimple.getCiName());
		ci.setCiState(ciSimple.getCiState());
		ci.setComments(ciSimple.getComments());
		ci.setLastAppliedRfcId(ciSimple.getLastAppliedRfcId());
        ci.setNsPath(ciSimple.getNsPath());
        ci.setCreatedBy(ciSimple.getCreatedBy());
        ci.setUpdatedBy(ciSimple.getUpdatedBy());
        
        for(String attrSimpleName: ciSimple.getCiAttributes().keySet()){
        	CmsCIAttribute attr = new CmsCIAttribute();
        	attr.setAttributeName(attrSimpleName);
        	if ("dj".equalsIgnoreCase(valueType)) {
        		attr.setDjValue(ciSimple.getCiAttributes().get(attrSimpleName));
        	} else if ("df".equalsIgnoreCase(valueType)){
        		attr.setDfValue(ciSimple.getCiAttributes().get(attrSimpleName));
        	} else {
        		attr.setDjValue(ciSimple.getCiAttributes().get(attrSimpleName));
        		attr.setDfValue(ciSimple.getCiAttributes().get(attrSimpleName));
        	}
        	ci.addAttribute(attr);
        }
        
		if (ciSimple.getAttrProps() != null) {
			for (String attrProp : ciSimple.getAttrProps().keySet()) {
				if (attrProp.equalsIgnoreCase(ATTR_PROP_OWNER)) {
					for (String attrName : ciSimple.getAttrProps().get(attrProp).keySet()) {
						ci.getAttribute(attrName).setOwner(ciSimple.getAttrProps().get(attrProp).get(attrName));
					}
				}
			}
		}
        
        return ci;
	}

	/**
	 * Cust c i2 ci simple.
	 *
	 * @param ci the ci
	 * @param valueType the value type
	 * @return the cms ci simple
	 */
	public CmsCISimple custCI2CISimple(CmsCI ci, String valueType) {
		return custCI2CISimple(ci, valueType, false);
	}	

	public CmsCISimple custCI2CISimple(CmsCI ci, String valueType, boolean getEncrypted) {
		return custCI2CISimple(ci, valueType, null, getEncrypted);
	}	

	public CmsCISimple custCI2CISimple(CmsCI ci, String valueType, String attrProps, boolean getEncrypted) {
		if (attrProps != null) {
			return custCI2CISimpleLocal(ci, valueType, attrProps.split(","), getEncrypted);
		} else {
			return custCI2CISimpleLocal(ci, valueType, null, getEncrypted);
		}

	}	
	/**
	 * Cust c i2 ci simple.
	 *
	 * @param ci the ci
	 * @param valueType the value type
	 * @param getEncrypted the get encrypted
	 * @return the cms ci simple
	 */
	private CmsCISimple custCI2CISimpleLocal(CmsCI ci, String valueType, String[] attrProps, boolean getEncrypted) {
		if (ci == null) {
			return null;
		}
		CmsCISimple ciSimple = new CmsCISimple();
		ciSimple.setCiId(ci.getCiId());
		ciSimple.setNsId(ci.getNsId());
		ciSimple.setCiClassName(ci.getCiClassName());
		ciSimple.setImpl(ci.getImpl());
		ciSimple.setCiGoid(ci.getCiGoid());
		ciSimple.setCiName(ci.getCiName());
		ciSimple.setCiState(ci.getCiState());
		ciSimple.setComments(ci.getComments());
		ciSimple.setLastAppliedRfcId(ci.getLastAppliedRfcId());
		ciSimple.setNsPath(ci.getNsPath());
		ciSimple.setCreatedBy(ci.getCreatedBy());
		ciSimple.setUpdatedBy(ci.getUpdatedBy());
		ciSimple.setCreated(ci.getCreated());
		ciSimple.setUpdated(ci.getUpdated());
		
        for(CmsCIAttribute attr : ci.getAttributes().values()){
        	if ("dj".equalsIgnoreCase(valueType)) {
        		if (getEncrypted) {
        			ciSimple.addCiAttribute(attr.getAttributeName(), attr.getDjValue());
        		} else {
        			ciSimple.addCiAttribute(attr.getAttributeName(), checkEncrypt(attr.getDjValue()));
        		}
        	} else {
        		if (getEncrypted) {
        			ciSimple.addCiAttribute(attr.getAttributeName(), attr.getDfValue());
        		} else {	
        			ciSimple.addCiAttribute(attr.getAttributeName(), checkEncrypt(attr.getDfValue()));
        		}
        	}
        	
			if (attrProps != null) {
				for (String attrProp : attrProps) {
					if (attrProp.equalsIgnoreCase(ATTR_PROP_OWNER)) {
						ciSimple.addAttrProps(attrProp, attr.getAttributeName(), attr.getOwner());
					}
				}
			}
        }
        
        return ciSimple;
	}

	/**
	 * Cust ci relation2 ci relation simple.
	 *
	 * @param rel the rel
	 * @param valueType the value type
	 * @param getEncrypted the get encrypted
	 * @return the cms ci relation simple
	 */
	public CmsCIRelationSimple custCIRelation2CIRelationSimple (CmsCIRelation rel, String valueType, boolean getEncrypted) {
		// TODO get the conversion right
		if (rel == null) {
			return null;
		}
		CmsCIRelationSimple relSimple = new CmsCIRelationSimple();
		
		relSimple.setCiRelationId(rel.getCiRelationId());
		relSimple.setComments(rel.getComments());
		relSimple.setCreated(rel.getCreated());
		relSimple.setFromCiId(rel.getFromCiId());
		relSimple.setToCiId(rel.getToCiId());
		relSimple.setLastAppliedRfcId(rel.getLastAppliedRfcId());
		relSimple.setRelationGoid(rel.getRelationGoid());
		relSimple.setRelationName(rel.getRelationName());
		relSimple.setRelationState(rel.getRelationState());
		relSimple.setUpdated(rel.getUpdated());
		relSimple.setNsPath(rel.getNsPath());
		
        for(CmsCIRelationAttribute attr : rel.getAttributes().values()){
        	if ("dj".equalsIgnoreCase(valueType)) {
        		relSimple.addRelationAttribute(attr.getAttributeName(), attr.getDjValue());
        	} else {
        		relSimple.addRelationAttribute(attr.getAttributeName(), attr.getDfValue());
        	}
        }
        
        if (rel.getFromCi() != null) {
        	relSimple.setFromCi(custCI2CISimple(rel.getFromCi(), valueType, getEncrypted));
        }
        
        if (rel.getToCi() != null) {
        	relSimple.setToCi(custCI2CISimple(rel.getToCi(), valueType, getEncrypted));
        }
        
        
        return relSimple;
	}
	
	/**
	 * Cust ci relation simple2 ci relation.
	 *
	 * @param relSimple the rel simple
	 * @param valueType the value type
	 * @return the cms ci relation
	 */
	public CmsCIRelation custCIRelationSimple2CIRelation (CmsCIRelationSimple relSimple, String valueType) {
		// TODO get the conversion right
		if (relSimple == null) {
			return null;
		}
		
		CmsCIRelation rel = new CmsCIRelation();
		
		rel.setCiRelationId(relSimple.getCiRelationId());
		rel.setComments(relSimple.getComments());
		rel.setCreated(relSimple.getCreated());
		rel.setFromCiId(relSimple.getFromCiId());
		rel.setToCiId(relSimple.getToCiId());
		rel.setLastAppliedRfcId(relSimple.getLastAppliedRfcId());
		rel.setRelationGoid(relSimple.getRelationGoid());
		rel.setRelationName(relSimple.getRelationName());
		rel.setRelationState(relSimple.getRelationState());
		rel.setUpdated(relSimple.getUpdated());
		rel.setNsPath(relSimple.getNsPath());
		rel.setCreatedBy(relSimple.getCreatedBy());
		rel.setUpdatedBy(relSimple.getUpdatedBy());
		
		
        for(String attrSimpleName: relSimple.getRelationAttributes().keySet()){
        	CmsCIRelationAttribute attr = new CmsCIRelationAttribute();
        	attr.setAttributeName(attrSimpleName);
        	if ("dj".equalsIgnoreCase(valueType)) {
        		attr.setDjValue(relSimple.getRelationAttributes().get(attrSimpleName));
        	} else if ("df".equalsIgnoreCase(valueType)) {
        		attr.setDfValue(relSimple.getRelationAttributes().get(attrSimpleName));
        	} else {
        		attr.setDjValue(relSimple.getRelationAttributes().get(attrSimpleName));
        		attr.setDfValue(relSimple.getRelationAttributes().get(attrSimpleName));
        	}
        	rel.addAttribute(attr);
        }
        
        if (relSimple.getFromCi() != null) {
        	rel.setFromCi(custCISimple2CI(relSimple.getFromCi(), valueType));
        }
        
        if (relSimple.getToCi() != null) {
        	rel.setToCi(custCISimple2CI(relSimple.getToCi(), valueType));
        }
        
        return rel;
	}
	
	/**
	 * Cust rfc ci simple2 rfc ci.
	 *
	 * @param rfcSimple the rfc simple
	 * @return the cms rfc ci
	 */
	public CmsRfcCI custRfcCISimple2RfcCI (CmsRfcCISimple rfcSimple) {
		
		if (rfcSimple == null) {
			return null;
		}

		CmsRfcCI rfc = new CmsRfcCI();
		
		rfc.setRfcId(rfcSimple.getRfcId());
		rfc.setReleaseId(rfcSimple.getReleaseId());
		rfc.setCiId(rfcSimple.getCiId());
		rfc.setNsPath(rfcSimple.getNsPath());
		rfc.setCiClassName(rfcSimple.getCiClassName());
		rfc.setImpl(rfcSimple.getImpl());
		rfc.setCiName(rfcSimple.getCiName());
		rfc.setCiGoid(rfcSimple.getCiGoid());
		rfc.setCiState(rfcSimple.getCiState());
		rfc.setRfcAction(rfcSimple.getRfcAction());
		rfc.setExecOrder(rfcSimple.getExecOrder());
		rfc.setLastAppliedRfcId(rfcSimple.getLastAppliedRfcId());
		rfc.setReleaseType(rfcSimple.getReleaseType());
		rfc.setComments(rfcSimple.getComments());
		rfc.setIsActiveInRelease(rfcSimple.getIsActiveInRelease());
		
		rfc.setCreated(rfcSimple.getCreated());
		rfc.setCreatedBy(rfcSimple.getCreatedBy());
		rfc.setUpdated(rfcSimple.getUpdated());
		rfc.setUpdatedBy(rfcSimple.getUpdatedBy());

		rfc.setRfcCreated(rfcSimple.getRfcCreated());
		rfc.setRfcCreatedBy(rfcSimple.getRfcCreatedBy());
		rfc.setRfcUpdated(rfcSimple.getRfcUpdated());
		rfc.setRfcUpdatedBy(rfcSimple.getRfcUpdatedBy());
		
		
		for(String attrSimpleName: rfcSimple.getCiAttributes().keySet()){
        	CmsRfcAttribute attr = new CmsRfcAttribute();
        	attr.setAttributeName(attrSimpleName);
        	attr.setNewValue(rfcSimple.getCiAttributes().get(attrSimpleName));
        	rfc.addAttribute(attr);
        }

		if (rfcSimple.getCiAttrProps() != null) {
			for (String attrProp : rfcSimple.getCiAttrProps().keySet()) {
				if (attrProp.equalsIgnoreCase(ATTR_PROP_OWNER)) {
					for (String attrName : rfcSimple.getCiAttrProps().get(attrProp).keySet()) {
						rfc.getAttribute(attrName).setOwner(rfcSimple.getCiAttrProps().get(attrProp).get(attrName));
					}
				}
			}
		}
		
        return rfc;
	}

	/**
	 * Cust rfc c i2 rfc ci simple.
	 *
	 * @param rfc the rfc
	 * @return the cms rfc ci simple
	 */
	public CmsRfcCISimple custRfcCI2RfcCISimple (CmsRfcCI rfc) {
		return custRfcCI2RfcCISimpleLocal (rfc, null, false);
	}	

	/**
	 * Cust rfc c i2 rfc ci simple.
	 *
	 * @param rfc the rfc
	 * @param attrProps the attr props
	 * @return the cms rfc ci simple
	 */
	public CmsRfcCISimple custRfcCI2RfcCISimple (CmsRfcCI rfc, String attrProps) {
		if (attrProps != null) {
			return custRfcCI2RfcCISimpleLocal (rfc, attrProps.split(","), false);
		} else {
			return custRfcCI2RfcCISimpleLocal (rfc, null, false);
		}
	}

	/**
	 * Cust rfc c i2 rfc ci simple.
	 *
	 * @param rfc the rfc
	 * @param attrProps the attr props
	 * @return the cms rfc ci simple
	 */
	public CmsRfcCISimple custRfcCI2RfcCISimple (CmsRfcCI rfc, String[] attrProps) {
		return custRfcCI2RfcCISimpleLocal(rfc, attrProps, false);
	}	
	
	private CmsRfcCISimple custRfcCI2RfcCISimpleLocal (CmsRfcCI rfc, String[] attrProps, boolean getEncrepted) {
		
		if (rfc == null) {
			return null;
		}

		CmsRfcCISimple rfcSimple = new CmsRfcCISimple();
		
		rfcSimple.setRfcId(rfc.getRfcId());
		rfcSimple.setReleaseId(rfc.getReleaseId());
		rfcSimple.setCiId(rfc.getCiId());
		rfcSimple.setNsPath(rfc.getNsPath());
		rfcSimple.setCiClassName(rfc.getCiClassName());
		rfcSimple.setImpl(rfc.getImpl());
		rfcSimple.setCiName(rfc.getCiName());
		rfcSimple.setCiGoid(rfc.getCiGoid());
		rfcSimple.setCiState(rfc.getCiState());
		rfcSimple.setRfcAction(rfc.getRfcAction());
		rfcSimple.setExecOrder(rfc.getExecOrder());
		rfcSimple.setLastAppliedRfcId(rfc.getLastAppliedRfcId());
		rfcSimple.setReleaseType(rfc.getReleaseType());
		rfcSimple.setComments(rfc.getComments());
		rfcSimple.setIsActiveInRelease(rfc.getIsActiveInRelease());
		rfcSimple.setCreated(rfc.getCreated());
		rfcSimple.setUpdated(rfc.getUpdated());
		rfcSimple.setRfcCreated(rfc.getRfcCreated());
		rfcSimple.setRfcUpdated(rfc.getRfcUpdated());
		
		rfcSimple.setCreatedBy(rfc.getCreatedBy());
		rfcSimple.setUpdatedBy(rfc.getUpdatedBy());
		
		rfcSimple.setRfcCreatedBy(rfc.getRfcCreatedBy());
		rfcSimple.setRfcUpdatedBy(rfc.getRfcUpdatedBy());
		
		for(CmsRfcAttribute attr : rfc.getAttributes().values()) {
			
			if (getEncrepted) {
				rfcSimple.addCiAttribute(attr.getAttributeName(), attr.getNewValue());
			} else {
				rfcSimple.addCiAttribute(attr.getAttributeName(), checkEncrypt(attr.getNewValue()));
			}
			if (attr.getOldValue() != null) {
				if (getEncrepted) {
					rfcSimple.addCiBaseAttribute(attr.getAttributeName(), attr.getOldValue());
				} else {	
					rfcSimple.addCiBaseAttribute(attr.getAttributeName(), checkEncrypt(attr.getOldValue()));
				}
			}

			if (attrProps != null) {
				for (String attrProp : attrProps) {
					if (attrProp.equalsIgnoreCase(ATTR_PROP_OWNER)) {
						rfcSimple.addCiAttrProp(attrProp, attr.getAttributeName(), attr.getOwner());
					}
				}
			}
		}
        return rfcSimple;
	}
	
	/**
	 * Cust rfc rel2 rfc rel simple.
	 *
	 * @param relation the relation
	 * @return the cms rfc relation simple
	 */
	public CmsRfcRelationSimple custRfcRel2RfcRelSimple (CmsRfcRelation relation) {
		return custRfcRel2RfcRelSimpleLocal(relation, null);
	}	

	/**
	 * Cust rfc rel2 rfc rel simple.
	 *
	 * @param relation the relation
	 * @param attrProps the attr props
	 * @return the cms rfc relation simple
	 */
	public CmsRfcRelationSimple custRfcRel2RfcRelSimple (CmsRfcRelation relation, String[] attrProps) {
		return custRfcRel2RfcRelSimpleLocal(relation, attrProps);
	}	
	
	/**
	 * Cust rfc rel2 rfc rel simple.
	 *
	 * @param relation the relation
	 * @param attrProps the attr props
	 * @return the cms rfc relation simple
	 */
	public CmsRfcRelationSimple custRfcRel2RfcRelSimple (CmsRfcRelation relation, String attrProps) {
		if (attrProps != null) {
			return custRfcRel2RfcRelSimpleLocal(relation, attrProps.split(","));
		} else {
			return custRfcRel2RfcRelSimpleLocal(relation, null);
		}
	}	
	
	
	private CmsRfcRelationSimple custRfcRel2RfcRelSimpleLocal (CmsRfcRelation relation, String[] attrProps) {
		
		if (relation == null) {
			return null;
		}

		CmsRfcRelationSimple relationSimple = new CmsRfcRelationSimple();
		
		relationSimple.setRfcId(relation.getRfcId());
		relationSimple.setReleaseId(relation.getReleaseId());
		relationSimple.setFromCiId(relation.getFromCiId());
		relationSimple.setRelationName(relation.getRelationName());
		relationSimple.setToCiId(relation.getToCiId());
		relationSimple.setRelationGoid(relation.getRelationGoid());
		relationSimple.setRfcAction(relation.getRfcAction());
		relationSimple.setExecOrder(relation.getExecOrder());
		relationSimple.setLastAppliedRfcId(relation.getLastAppliedRfcId());
		relationSimple.setReleaseType(relation.getReleaseType());
		relationSimple.setIsActiveInRelease(relation.getIsActiveInRelease());
		relationSimple.setComments(relation.getComments());
		relationSimple.setCiRelationId(relation.getCiRelationId());
		relationSimple.setNsPath(relation.getNsPath());

		relationSimple.setCreated(relation.getCreated());
		relationSimple.setCreatedBy(relation.getCreatedBy());
		relationSimple.setUpdated(relation.getUpdated());
		relationSimple.setUpdatedBy(relation.getUpdatedBy());
		
		relationSimple.setRfcCreated(relation.getRfcCreated());
		relationSimple.setRfcCreatedBy(relation.getRfcCreatedBy());
		relationSimple.setRfcUpdated(relation.getRfcUpdated());
		relationSimple.setRfcUpdatedBy(relation.getRfcUpdatedBy());
		
		
		for(CmsRfcAttribute attr : relation.getAttributes().values()) {
			relationSimple.addRelationAttribute(attr.getAttributeName(), attr.getNewValue());
			if (attr.getOldValue() != null) {
				relationSimple.addRelationBaseAttribute(attr.getAttributeName(), attr.getOldValue());
			}
			if (attrProps != null) {
				for (String attrProp : attrProps) {
					if (attrProp.equalsIgnoreCase(ATTR_PROP_OWNER)) {
						relationSimple.addRelationAttrProp(attrProp, attr.getAttributeName(), attr.getOwner());
					}
				}
			}
		}
		
		if (relation.getToRfcCi() != null) {
			relationSimple.setToCi(custRfcCI2RfcCISimple(relation.getToRfcCi()));
		}
		if (relation.getFromRfcCi() != null) {
			relationSimple.setFromCi(custRfcCI2RfcCISimple(relation.getFromRfcCi()));
		}
		
        return relationSimple;
	}

	/**
	 * Cust rfc rel simple2 rfc rel.
	 *
	 * @param relationSimple the relation simple
	 * @return the cms rfc relation
	 */
	public CmsRfcRelation custRfcRelSimple2RfcRel (CmsRfcRelationSimple relationSimple) {

		if (relationSimple == null) {
			return null;
		}

		CmsRfcRelation relation = new CmsRfcRelation();
		
		relation.setRfcId(relationSimple.getRfcId());
		relation.setReleaseId(relationSimple.getReleaseId());
		relation.setFromCiId(relationSimple.getFromCiId());
		relation.setRelationName(relationSimple.getRelationName());
		relation.setToCiId(relationSimple.getToCiId());
		relation.setRelationGoid(relationSimple.getRelationGoid());
		relation.setRfcAction(relationSimple.getRfcAction());
		relation.setExecOrder(relationSimple.getExecOrder());
		relation.setLastAppliedRfcId(relationSimple.getLastAppliedRfcId());
		relation.setReleaseType(relationSimple.getReleaseType());
		relation.setIsActiveInRelease(relationSimple.getIsActiveInRelease());
		relation.setComments(relationSimple.getComments());
		relation.setCiRelationId(relationSimple.getCiRelationId());
		relation.setNsPath(relationSimple.getNsPath());

		relation.setCreated(relationSimple.getCreated());
		relation.setUpdated(relationSimple.getUpdated());
		relation.setCreatedBy(relationSimple.getCreatedBy());
		relation.setUpdatedBy(relationSimple.getUpdatedBy());
	
		relation.setRfcCreated(relationSimple.getRfcCreated());
		relation.setRfcUpdated(relationSimple.getRfcUpdated());
		relation.setRfcCreatedBy(relationSimple.getRfcCreatedBy());
		relation.setRfcUpdatedBy(relationSimple.getRfcUpdatedBy());
		
		
		for(String attrSimpleName: relationSimple.getRelationAttributes().keySet()){
        	CmsRfcAttribute attr = new CmsRfcAttribute();
        	attr.setAttributeName(attrSimpleName);
        	attr.setNewValue(relationSimple.getRelationAttributes().get(attrSimpleName));
        	relation.addAttribute(attr);
        }
		
		if (relationSimple.getRelationAttrProps() != null) {
			for (String attrProp : relationSimple.getRelationAttrProps().keySet()) {
				if (attrProp.equalsIgnoreCase(ATTR_PROP_OWNER)) {
					for (String attrName : relationSimple.getRelationAttrProps().get(attrProp).keySet()) {
						relation.getAttribute(attrName).setOwner(relationSimple.getRelationAttrProps().get(attrProp).get(attrName));
					}
				}
			}
		}
		
        return relation;
	}
	
	/**
	 * Cust work order2 simple.
	 *
	 * @param wo the wo
	 * @return the cms work order simple
	 */
	public CmsWorkOrderSimple custWorkOrder2Simple(CmsWorkOrder wo) {
		CmsWorkOrderSimple wos = new CmsWorkOrderSimple();
		wos.setDpmtRecordId(wo.getDpmtRecordId());
		wos.setDeploymentId(wo.getDeploymentId());
		wos.setDpmtRecordState(wo.getDpmtRecordState());
		wos.setCreated(wo.getCreated());
		wos.setComments(wo.getComments());
		wos.setRfcId(wo.getRfcId());
		wos.setRfcCi(custRfcCI2RfcCISimpleLocal(wo.getRfcCi(),null,true));
		wos.setBox(custCI2CISimple(wo.getBox(), "df", true));
		wos.setCloud(custCI2CISimple(wo.getCloud(), "df", true));
		
		if (wo.getServices() != null) {
			Map<String,Map<String, CmsCISimple>> simpleServs = new HashMap<String,Map<String, CmsCISimple>>();
			for (Entry<String,Map<String, CmsCI>> serviceEntry : wo.getServices().entrySet()) {
				simpleServs.put(serviceEntry.getKey(), new LinkedHashMap<String,CmsCISimple>());
				for (Entry<String,CmsCI> cloudEntry : serviceEntry.getValue().entrySet()) {
					simpleServs.get(serviceEntry.getKey()).put(cloudEntry.getKey(), custCI2CISimple(cloudEntry.getValue(),"df",true));
				}
			}
			wos.setServices(simpleServs);
		}
		
		if (wo.getPayLoad() != null) {
		for (String key : wo.getPayLoad().keySet()) {
			for (CmsRfcCI rfc : wo.getPayLoad().get(key)) {
				wos.addPayLoadEntry(key, custRfcCI2RfcCISimpleLocal(rfc, null, true));
			}
		}
		}
		return wos;
	}
	
	/**
	 * Cust simple2 work order.
	 *
	 * @param wos the wos
	 * @return the cms work order
	 */
	public CmsWorkOrder custSimple2WorkOrder(CmsWorkOrderSimple wos) {
		CmsWorkOrder wo = new CmsWorkOrder();
		wo.setDpmtRecordId(wos.getDpmtRecordId());
		wo.setDeploymentId(wos.getDeploymentId());
		wo.setDpmtRecordState(wos.getDpmtRecordState());
		wo.setCreated(wos.getCreated());
		wo.setComments(wos.getComments());
		wo.setRfcId(wos.getRfcId());
		
		if (wos.getRfcCi() != null) {
			wo.setRfcCi(custRfcCISimple2RfcCI(wos.getRfcCi()));
		}
		
		if (wos.getResultCi() != null) {
			wo.setResultCi(custCISimple2CI(wos.getResultCi(), "df"));
		}

		wo.setAdditionalInfo(wos.getAdditionalInfo());
		return wo;
	}

    /**
     * Cust action order2 simple.
     *
     * @param ao the ao
     * @return the cms action order simple
     */
    public CmsActionOrderSimple custActionOrder2Simple(CmsActionOrder ao) {
        CmsActionOrderSimple aos = new CmsActionOrderSimple();
        aos.setActionId(ao.getActionId());
        aos.setProcedureId(ao.getProcedureId());
        aos.setActionState(ao.getActionState());
        aos.setActionName(ao.getActionName());
        aos.setCreated(ao.getCreated());
        aos.setArglist(ao.getArglist());
        aos.setExtraInfo(ao.getExtraInfo());
        aos.setCiId(ao.getCiId());
        aos.setIsCritical(ao.getIsCritical());
        aos.setExecOrder(ao.getExecOrder());
        aos.setCi(custCI2CISimple(ao.getCi(), null, true));
        aos.setBox(custCI2CISimple(ao.getBox(), "df", true));
        aos.setCloud(custCI2CISimple(ao.getCloud(), "df", true));
        //Auto-repair
        aos.setCreatedBy(ao.getCreatedBy());
		
        if (ao.getServices() != null) {
			Map<String,Map<String, CmsCISimple>> simpleServs = new HashMap<String,Map<String, CmsCISimple>>();
			for (Entry<String,Map<String, CmsCI>> serviceEntry : ao.getServices().entrySet()) {
				simpleServs.put(serviceEntry.getKey(), new HashMap<String,CmsCISimple>());
				for (Entry<String,CmsCI> cloudEntry : serviceEntry.getValue().entrySet()) {
					simpleServs.get(serviceEntry.getKey()).put(cloudEntry.getKey(), custCI2CISimple(cloudEntry.getValue(),"df",true));
				}
			}
			aos.setServices(simpleServs);
		}
        
        
        if (ao.getPayLoad() != null) {
        for (String key : ao.getPayLoad().keySet()) {
            for (CmsCI rfc : ao.getPayLoad().get(key)) {
                aos.addPayLoadEntry(key, custCI2CISimple(rfc, "df", true));
            }
        }
        }
        return aos;
    }

    /**
     * Cust simple2 action order.
     *
     * @param aos the aos
     * @return the cms action order
     */
    public CmsActionOrder custSimple2ActionOrder(CmsActionOrderSimple aos) {
        CmsActionOrder ao = new CmsActionOrder();
        ao.setActionId(aos.getActionId());
        ao.setProcedureId(aos.getProcedureId());
        ao.setActionState(aos.getActionState());
        ao.setCreated(aos.getCreated());
        ao.setExtraInfo(aos.getExtraInfo());
        ao.setArglist(aos.getArglist());
        ao.setCiId(aos.getCiId());

        if (aos.getCi() != null) {
            ao.setCi(custCISimple2CI(aos.getCi(),"df"));
        }

        if (aos.getResultCi() != null) {
            ao.setResultCi(custCISimple2CI(aos.getResultCi(), "df"));
        }

        return ao;
    }

	private String checkEncrypt(String val) {
		if (val != null && val.startsWith(CmsCrypto.ENC_PREFIX)) {
			return CmsCrypto.ENC_DUMMY;
		} else {
			return val;
		}
	}

	/**
	 * Parses the conditions.
	 *
	 * @param attrs the attrs
	 * @return the list
	 */
	public List<AttrQueryCondition> parseConditions(String[] attrs) {
		List<AttrQueryCondition> attrConds = new ArrayList<AttrQueryCondition>();
		for(String attrStr : attrs) {
			String[] attrArray = attrStr.split(":");
			AttrQueryCondition attrCondition = new AttrQueryCondition();
			attrCondition.setAttributeName(attrArray[0]);
			attrCondition.setCondition(attrArray[1]);
			attrCondition.setAvalue(attrArray[2]);
			attrConds.add(attrCondition);
		}
		return attrConds;
	}

	/**
	 * Gets the short clazz name.
	 *
	 * @param fullClazzName the full clazz name
	 * @return the short clazz name
	 */
	public String getShortClazzName(String fullClazzName) {
		String[] parts = fullClazzName.split("\\.");
		if (parts.length == 0) return null;
		return parts[parts.length - 1];
	}

	/**
	 * Gets the long short clazz name.
	 *
	 * @param fullClazzName the full clazz name
	 * @return the long short clazz name
	 */
	public String getLongShortClazzName(String fullClazzName) {
		if (fullClazzName.startsWith("account")) {
			String[] nameParts = fullClazzName.split("\\.");
			return nameParts[nameParts.length-1];
		} else {
			return fullClazzName.replaceAll("base.|mgmt.catalog.|catalog.|mgmt.manifest.|manifest.|bom.|mgmt.|", "");
		}
	}
	
	/**
	 * Update CmsCI so that any CmsCIAttribute that has a variable has the variable replaced
	 * from the approp Cloud, Global, or Local var maps.
	 * A Global variable can refer to a Cloud variable, eg
	 * given Cloud variable X1=abc , and Global variable foo=$GLOBAL{X1} would result in foo getting
	 * set to 'abc'. 
	 * And Local variables can refer to *either* Cloud or Global variables
	 * @param ci the CmsCI which may have attributes that need variable resolution
	 * @param cloudVars the cloud vars map
	 * @param globalVars the global vars map
	 * @param localVars the local vars map
	 */
	public void processAllVars_(CmsCI ci, Map<String,String> cloudVars, Map<String,String> globalVars, Map<String,String> localVars) {

		if (logger.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder("Processing vars for Ci [")
			.append(ci.getCiId()).append("] CmsCIAttributes [");
			for (Entry<String, CmsCIAttribute> e : ci.getAttributes().entrySet()) {
				sb.append(e.getKey()).append(":dj:").append(e.getValue().getDjValue());
			};
			sb.append("] Cloud vars [").append(cloudVars).append("]");
			sb.append("] Global vars [").append(globalVars).append("]");
			sb.append("] Local vars [").append(localVars).append("]");	
			logger.info(sb.toString());
		}
     
		String variableToResolve="";
		String resolvedValue="";
    	for (CmsCIAttribute manifestAttr : ci.getAttributes().values()) {
    		if (manifestAttr.getDjValue() != null) {
    			if (manifestAttr.getDjValue().contains(CLOUDVARPFX)){
    				List<String> varStructures = splitAttrValue(manifestAttr.getDjValue(),CLOUDVARPFX);
    				for (String varStructure : varStructures) {
						variableToResolve = stripSymbolics(varStructure);
						if (cloudVars!=null) {
							resolvedValue = cloudVars.get(variableToResolve);//ez lookup in 1 Cloud Map
						} else {
							resolvedValue=null;
						}
							checkAndSetAttrValue(ci, resolvedValue,
									manifestAttr, variableToResolve,
									CLOUDVARRPL);
					}
    			} 
    					
        		if (manifestAttr.getDjValue().contains(GLOBALVARPFX)){
        			List<String> varStructures = splitAttrValue(manifestAttr.getDjValue(),GLOBALVARPFX);
    				for (String varStructure : varStructures) {
    					variableToResolve=stripSymbolics(varStructure);
    					//lookup in Global Map; may refer to Cloud in turn but handled there
    					resolvedValue = resolveGlobalVar(cloudVars, globalVars, variableToResolve);
    					checkAndSetAttrValue(ci, resolvedValue, manifestAttr, variableToResolve, GLOBALVARRPL);
    				}
        		}
        		
           		if (manifestAttr.getDjValue().contains(LOCALVARPFX)){
           			List<String>  varStructures = splitAttrValue(manifestAttr.getDjValue(),LOCALVARPFX);
    				for (String varStructure : varStructures) {
    					variableToResolve=stripSymbolics(varStructure);
    					if (localVars!=null) {
							resolvedValue = localVars.get(variableToResolve);
						} else {
							resolvedValue=null;
						}
						if (resolvedValue!=null) {
							if (resolvedValue.contains(CLOUDVARPFX)) {// ez lookup in Cloud Map
								resolvedValue = cloudVars
										.get(stripSymbolics(resolvedValue));
							} else {
								if (resolvedValue.contains(GLOBALVARPFX)) {
									resolvedValue = resolveGlobalVar(cloudVars,
											globalVars,
											stripSymbolics(resolvedValue)); // lookup in Global Map; it may refer to Cloud in turn but handled there
								}
							}
						}
						checkAndSetAttrValue(ci, resolvedValue, manifestAttr, variableToResolve, LOCALVARRPL);
    				}
           		}	
    			
    		}
    	}
    	
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("Processing vars complete for Ci [")
			.append(ci.getCiId()).append("] CmsCIAttributes [");
			for (Entry<String, CmsCIAttribute> e : ci.getAttributes().entrySet()) {
				sb.append(e.getKey()).append(":dj:").append(e.getValue().getDjValue());
			}		
			logger.info(sb.toString());
		}
	}

	public void processAllVars(CmsCI ci, Map<String,String> cloudVars, Map<String,String> globalVars, Map<String,String> localVars) {

		if (logger.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder("Processing vars for Ci [")
			.append(ci.getCiId()).append("] CmsCIAttributes [");
			for (Entry<String, CmsCIAttribute> e : ci.getAttributes().entrySet()) {
				sb.append(e.getKey()).append(":dj:").append(e.getValue().getDjValue());
			};
			sb.append("] Cloud vars [").append(cloudVars).append("]");
			sb.append("] Global vars [").append(globalVars).append("]");
			sb.append("] Local vars [").append(localVars).append("]");	
			logger.info(sb.toString());
		}
     

        ExceptionConsolidator ec = CIValidationException.consolidator(CmsError.TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF);
    	for (CmsCIAttribute manifestAttr : ci.getAttributes().values()) {
            ec.invokeChecked(()->
            {
    		manifestAttr.setDjValue(processAllVarsForString(ci.getCiId(),ci.getCiName(),ci.getNsPath(),manifestAttr.getAttributeName(),manifestAttr.getDjValue(),cloudVars,globalVars,localVars));
    		manifestAttr.setDfValue(processAllVarsForString(ci.getCiId(),ci.getCiName(),ci.getNsPath(),manifestAttr.getAttributeName(),manifestAttr.getDfValue(),cloudVars,globalVars,localVars));
            });
    	}
        ec.rethrowExceptionIfNeeded();
    	
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("Processing vars complete for Ci [")
			.append(ci.getCiId()).append("] CmsCIAttributes [");
			for (Entry<String, CmsCIAttribute> e : ci.getAttributes().entrySet()) {
				sb.append(e.getKey()).append(":dj:").append(e.getValue().getDjValue());
			}		
			logger.info(sb.toString());
		}
	}


	private String processAllVarsForString(long ciId, String ciName, String nsPath, String attrName, String unresolvedAttrValue, Map<String,String> cloudVars, Map<String,String> globalVars, Map<String,String> localVars) {

		String attrValue = unresolvedAttrValue;
		String variableToResolve="";
		String resolvedValue="";
		if (attrValue != null) {
			if (attrValue.contains(CLOUDVARPFX)){
				List<String> varStructures = splitAttrValue(attrValue,CLOUDVARPFX);
				for (String varStructure : varStructures) {
					variableToResolve = stripSymbolics(varStructure);
					if (cloudVars!=null) {
						resolvedValue = cloudVars.get(variableToResolve);//ez lookup in 1 Cloud Map
					} else {
						resolvedValue=null;
					}
					attrValue = subVarValue(ciId, ciName, nsPath, attrName, attrValue, resolvedValue, variableToResolve, CLOUDVARRPL);
				}
			}

    		if (attrValue.contains(GLOBALVARPFX)){
    			List<String> varStructures = splitAttrValue(attrValue,GLOBALVARPFX);
				for (String varStructure : varStructures) {
					variableToResolve=stripSymbolics(varStructure);
					//lookup in Global Map; may refer to Cloud in turn but handled there
					resolvedValue = resolveGlobalVar(cloudVars, globalVars, variableToResolve);
					attrValue = subVarValue(ciId, ciName, nsPath, attrName, attrValue, resolvedValue, variableToResolve, GLOBALVARRPL);
				}
    		}
    		
       		if (attrValue.contains(LOCALVARPFX)){
       			List<String>  varStructures = splitAttrValue(attrValue,LOCALVARPFX);
				for (String varStructure : varStructures) {
					variableToResolve=stripSymbolics(varStructure);
					if (localVars!=null) {
						resolvedValue = localVars.get(variableToResolve);
					} else {
						resolvedValue=null;
					}
					if (resolvedValue!=null) {
						while (resolvedValue.contains(CLOUDVARPFX)) {// ez lookup in Cloud Map
							String varName = stripSymbolicsWithPrefix(resolvedValue, CLOUDVARPFX);
							String varValue = cloudVars.get(varName);
							resolvedValue = resolvedValue.replaceAll(CLOUDVARRPL + varName + "}", varValue);
						}

						while (resolvedValue.contains(GLOBALVARPFX)) {
							String varName = stripSymbolicsWithPrefix(resolvedValue, GLOBALVARPFX);
							String varValue = resolveGlobalVar(cloudVars,
									globalVars,
									varName); // lookup in Global Map; it may refer to Cloud in turn but handled there
							resolvedValue = resolvedValue.replaceAll(GLOBALVARRPL + varName + "}", varValue);
						}
					}
					attrValue = subVarValue(ciId, ciName, nsPath, attrName, attrValue, resolvedValue, variableToResolve, LOCALVARRPL);
				}
       		}	
			
		}
		return attrValue;
	}


	/**
	 * Update CmsRfcCI so that any CmsRfcAttribute that has a variable has the variable replaced
	 * from the approp Cloud, Global, or Local var maps.
	 * A Global variable can refer to a Cloud variable, eg
	 * given Cloud variable X1=abc , and Global variable foo=$GLOBAL{X1} would result in foo getting
	 * set to 'abc'. 
	 * And Local variables can refer to *either* Cloud or Global variables
	 * @param ci the CmsRfcCI which may have attributes that need variable resolution
	 * @param cloudVars the cloud vars map
	 * @param globalVars the global vars map
	 * @param localVars the local vars map
	 */
	public void processAllVars_(CmsRfcCI ci, Map<String,String> cloudVars, Map<String,String> globalVars, Map<String,String> localVars) {

		if (logger.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder("Processing vars for Ci [")
			.append(ci.getCiId()).append("] CmsRfcAttribute [");
			for (Entry<String, CmsRfcAttribute> e : ci.getAttributes().entrySet()) {
				sb.append(e.getKey()).append(":new:").append(e.getValue().getOldValue());
			};
			sb.append("] Cloud vars [").append(cloudVars).append("]");
			sb.append("] Global vars [").append(globalVars).append("]");
			sb.append("] Local vars [").append(localVars).append("]");	
			logger.info(sb.toString());
		}
     
		String variableToResolve="";
		String resolvedValue="";
    	for (CmsRfcAttribute rfcAttr : ci.getAttributes().values()) {
    		if (rfcAttr.getNewValue() != null) {
    			if (rfcAttr.getNewValue().contains(CLOUDVARPFX)){
    				List<String> varStructures = splitAttrValue(rfcAttr.getNewValue(),CLOUDVARPFX);
    				for (String varStructure : varStructures) {
						variableToResolve = stripSymbolics(varStructure);
						if (cloudVars!=null) {
							resolvedValue = cloudVars.get(variableToResolve);//ez lookup in 1 Cloud Map
						} else {
							resolvedValue=null;
						}
						checkAndSetAttrValue(ci, resolvedValue,
								rfcAttr, variableToResolve,
								CLOUDVARRPL);
					}
    			} 
    					
        		if (rfcAttr.getNewValue().contains(GLOBALVARPFX)){
        			List<String> varStructures = splitAttrValue(rfcAttr.getNewValue(),GLOBALVARPFX);
    				for (String varStructure : varStructures) {
    					variableToResolve=stripSymbolics(varStructure);
    					//lookup in Global Map; may refer to Cloud in turn but handled there
    					resolvedValue = resolveGlobalVar(cloudVars, globalVars, variableToResolve);
    					checkAndSetAttrValue(ci, resolvedValue, rfcAttr, variableToResolve, GLOBALVARRPL);
    				}
        		}
        		
           		if (rfcAttr.getNewValue().contains(LOCALVARPFX)){
           			List<String>  varStructures = splitAttrValue(rfcAttr.getNewValue(),LOCALVARPFX);
    				for (String varStructure : varStructures) {
    					variableToResolve=stripSymbolics(varStructure);
    					if (localVars!=null) {
							resolvedValue = localVars.get(variableToResolve);
						} else {
							resolvedValue=null;
						}
						if (resolvedValue!=null) {
							if (resolvedValue.contains(CLOUDVARPFX)) {// ez lookup in Cloud Map
								resolvedValue = cloudVars
										.get(stripSymbolics(resolvedValue));
							} else {
								if (resolvedValue.contains(GLOBALVARPFX)) {
									resolvedValue = resolveGlobalVar(cloudVars,
											globalVars,
											stripSymbolics(resolvedValue)); // lookup in Global Map; it may refer to Cloud in turn but handled there
								}
							}
						}
						checkAndSetAttrValue(ci, resolvedValue, rfcAttr, variableToResolve, LOCALVARRPL);
    				}
           		}	
    			
    		}
    	}
    	
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("Processing vars complete for RfcCi [")
			.append(ci.getCiId()).append("] CmsRfcAttribute [");
			for (Entry<String, CmsRfcAttribute> e : ci.getAttributes().entrySet()) {
				sb.append(e.getKey()).append(":new:").append(e.getValue().getNewValue());
			}		
			logger.info(sb.toString());
		}
	}

	public void processAllVars(CmsRfcCI ci, Map<String,String> cloudVars, Map<String,String> globalVars, Map<String,String> localVars) {

		if (logger.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder("Processing vars for Ci [")
			.append(ci.getCiId()).append("] CmsRfcAttribute [");
			for (Entry<String, CmsRfcAttribute> e : ci.getAttributes().entrySet()) {
				sb.append(e.getKey()).append(":new:").append(e.getValue().getOldValue());
			};
			sb.append("] Cloud vars [").append(cloudVars).append("]");
			sb.append("] Global vars [").append(globalVars).append("]");
			sb.append("] Local vars [").append(localVars).append("]");	
			logger.info(sb.toString());
		}
     
    	for (CmsRfcAttribute rfcAttr : ci.getAttributes().values()) {
    		rfcAttr.setNewValue(processAllVarsForString(ci.getCiId(),ci.getCiName(),ci.getNsPath(),rfcAttr.getAttributeName(),rfcAttr.getNewValue(),cloudVars,globalVars,localVars));
    		rfcAttr.setOldValue(processAllVarsForString(ci.getCiId(),ci.getCiName(),ci.getNsPath(),rfcAttr.getAttributeName(),rfcAttr.getOldValue(),cloudVars,globalVars,localVars));
    	}
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("Processing vars complete for RfcCi [")
			.append(ci.getCiId()).append("] CmsRfcAttribute [");
			for (Entry<String, CmsRfcAttribute> e : ci.getAttributes().entrySet()) {
				sb.append(e.getKey()).append(":new:").append(e.getValue().getNewValue());
			}		
			logger.info(sb.toString());
		}
	}
	
	
	/**
	 * Take a string wich has $OO.. type variable(s) and splits them into a list
	 *ex input: "$OO_LOCAL{groupId}:$OO_LOCAL{artifactId}:$OO_LOCAL{extension}" input comes back out as 
	 * a list with the three....[$OO_LOCAL{groupId}, $OO_LOCAL{artifactId}, $OO_LOCAL{extension}]
	 * @param inputString
	 * @param prefix
	 * @return essentially inputString tokenized by prefix as a List
	 */
	private List<String> splitAttrValue(String inputString, String prefix){
	     int i =0;
	     int loc=0;
	     List<String> elements = new ArrayList<String>();
	     while (i < inputString.length()){
		     loc = inputString.indexOf( prefix, i);
		     logger.debug("i="+i+"~~j is where "+ prefix+ " starts ~~j="+ loc);
		     if(loc >-1 ){
		    	 elements.add(inputString.substring( loc ,inputString.indexOf('}',loc)+1 ));
			     i =  loc + prefix.length() ;
		     } else {
		    	 break;
		     }
	     }
		return elements;
	}

	/** sets the Attributes Dj and Df value, but ensures it is not an unresolved variable reference
	 * runtime exceptions stem from here if that is the case*/
	private void checkAndSetAttrValue(CmsCI ci, String resolvedValue, CmsCIAttribute manifestAttr, String varName, String replPrefix) {
		
		if (resolvedValue==null ||   		//fix, it is actually okay if resolvedValue equals("") 
				resolvedValue.contains(LOCALVARPFX) ||   
				resolvedValue.contains(GLOBALVARPFX)||
				resolvedValue.contains(LOCALVARPFX) ) {//substituion did not happen: bad.
			StringBuilder sb = new StringBuilder("error processVars CI-")
			.append(ci.getCiId())
			.append(" the attribute- ")
			.append(manifestAttr.getAttributeName())
			.append(" is a bad ").append(guessVariableType(replPrefix)).append(" var reference! value [").append(resolvedValue).append("] Atts:");
			for (Entry<String, CmsCIAttribute> e : ci.getAttributes().entrySet()) {
				sb.append(e.getKey()).append(":dj:").append(e.getValue().getDjValue());
			}
			logger.warn(sb.toString());						
			throw new CIValidationException(
					CmsError.TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF,
					getErrorMessage(ci.getCiName(), ci.getNsPath(), manifestAttr.getAttributeName(), resolvedValue, varName, replPrefix));
		}
			
		//prefix.$OO_LOCAL{x}.suffix in Dj to-> prefix.RR.suffix
		StringBuilder pattToReplace = new StringBuilder(replPrefix).append(varName).append("\\}");
		String resAfter = manifestAttr.getDjValue().replaceAll(pattToReplace.toString(), resolvedValue);
		
		manifestAttr.setDjValue(resAfter);
		manifestAttr.setDfValue(resAfter);

		if(logger.isDebugEnabled()){
			logger.debug("Dj/Dfvalue set to :"+resAfter+ " in Ci "+ci.getCiId());
		}
	}



	/** sets the Attributes Dj and Df value, but ensures it is not an unresolved variable reference
	 * runtime exceptions stem from here if that is the case*/
	private String subVarValue(long ciId, String ciName, String nsPath, String attrName, String attrValue, String resolvedValue, String varName, String replPrefix) {

		if (resolvedValue==null ||   		//fix, it is actually okay if resolvedValue equals("")
				resolvedValue.contains(LOCALVARPFX) ||
				resolvedValue.contains(GLOBALVARPFX)||
				resolvedValue.contains(LOCALVARPFX) ) {//substituion did not happen: bad.
            String sb = "error processVars CI-" +
                    ciName + " id-" + ciId +
                    " the attribute- " +
                    attrName +
                    " has a bad " + guessVariableType(replPrefix) + " var reference! value [" + resolvedValue;
            logger.warn(sb);
			throw new CIValidationException(
					CmsError.TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF,
					getErrorMessage(ciName, nsPath, attrName, resolvedValue, varName, replPrefix));
		}
			
		//prefix.$OO_LOCAL{x}.suffix in Dj to-> prefix.RR.suffix
        String resAfter = attrValue.replaceAll(replPrefix + varName + "\\}", Matcher.quoteReplacement(resolvedValue));
		if(logger.isDebugEnabled()){
			logger.debug("Resolved value set to :"+resAfter+ " in Ci "+ciName);
		}
		return resAfter;
	}

	protected String getErrorMessage(String ciName, String nsPath, String attrName, String resolvedValue, String varName, String prefix) {
        String attributeDescription = "";
        try {
            attributeDescription = cmProcessor.getAttributeDescription(nsPath, ciName, attrName);
        } catch (Exception ignore) {  
            // ignore all errors while retrieving description from meta, it should never fail and affect error message generation.
            // also tests do not inject cmProcessor, so description lookup will throw NPE
        }

        return String.format("%s@%s attribute '%s' [%s] references unknown %s variable '%s'",
				ciName,
				truncateNS(nsPath),
                attributeDescription,
				attrName,
				guessVariableType(prefix),
                varName);
	}

    private String guessVariableType(String prefix) {
        String varType = "local";
        if (prefix!=null){
            switch (prefix) {
                case CLOUDVARRPL:
                    varType = "cloud";
                    break;
                case GLOBALVARRPL:
                    varType = "global";
                    break;
                default:
                    varType = "local";
                    break;
            }
        }
        return varType;
    }

    private String truncateNS(String nsPath) {
        if (nsPath!=null) {
            Matcher matcher = Pattern.compile("(/[^/]+){2}$").matcher(nsPath);
            if (matcher.find()) {
                return matcher.group().substring(1);
            }
        }
        return nsPath;
    }


	/** sets the Attributes old and new value, but ensures it is not an unresolved variable reference
	 * runtime exceptions stem from here if that is the case*/
	private void checkAndSetAttrValue(CmsRfcCI ci, String resolvedValue, CmsRfcAttribute manifestAttr, String varName, String replPrefix) {
		
		if (resolvedValue==null ||   		//fix, it is actually okay if resolvedValue equals("") 
				resolvedValue.contains(LOCALVARPFX) ||   
				resolvedValue.contains(GLOBALVARPFX)||
				resolvedValue.contains(LOCALVARPFX) ) {//substituion did not happen: bad.
			StringBuilder sb = new StringBuilder("error processVars CI-")
			.append(ci.getCiId())
			.append(" the attribute- ")
			.append(manifestAttr.getAttributeName())
			.append(" is a bad ").append(guessVariableType(replPrefix)).append(" var reference! value [").append(resolvedValue).append("] Atts:");
			for (Entry<String, CmsRfcAttribute> e : ci.getAttributes().entrySet()) {
				sb.append(e.getKey()).append(":new:").append(e.getValue().getNewValue());
			}
			logger.warn(sb.toString());						
			throw new CIValidationException(
					CmsError.TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF,
					getErrorMessage(ci.getCiName(), ci.getReleaseNsPath(), manifestAttr.getAttributeName(), resolvedValue, varName, replPrefix));
		}
			
		//prefix.$OO_LOCAL{x}.suffix in Dj to-> prefix.RR.suffix
		StringBuilder pattToReplace = new StringBuilder(replPrefix).append(varName).append("\\}");
		String resAfter = manifestAttr.getNewValue().replaceAll(pattToReplace.toString(), resolvedValue);
		
		manifestAttr.setOldValue(resAfter);
		manifestAttr.setNewValue(resAfter);

		if(logger.isDebugEnabled()){
			logger.debug("old/new value set to :"+resAfter+ " in RfcCi "+ci.getCiId());
		}
	}


	/** take a variable name, look it up in the globalVars Map. If the value is a simple value return that. If the value
	 * is a reference to a variable in the Cloud Map, look it up in the clodVars Map and return the value */
	private String resolveGlobalVar(Map<String, String> cloudVars, Map<String, String> globalVars, String variableToResolve) {
		//resolving either in the form of - $OO_GLOBAL{xyz} or $OO_GLOBAL{$OO_CLOUD{jkl}}
		//i.e    either a value, or a pointer to Cloud	
		if(globalVars==null || variableToResolve==null){
			return null;
		}
		String resolvedValue=globalVars.get(variableToResolve);
		
		if(resolvedValue!=null && resolvedValue.startsWith(CLOUDVARPFX) && cloudVars!=null){
			resolvedValue=cloudVars.get(stripSymbolics(resolvedValue));
		}
		
		return resolvedValue;
	}	
	
	/** $OO_CLOUD{xyz} returned as xyz */
	private String stripSymbolics(String variableReference) {
		return variableReference.substring(variableReference.indexOf("{")+1, variableReference.indexOf("}"));
	}

	private String stripSymbolicsWithPrefix(String variableReference, String prefix) {
		int startIndex = variableReference.indexOf(prefix) + prefix.length();
		return variableReference.substring(startIndex, variableReference.indexOf("}", startIndex));
	}

	/*
	public static String _likefy(String queryParam) {
		return queryParam.replace("_","\\_") + "%";
	}
	*/
	public static String likefyNsPath(String queryParam) {
		if (queryParam.endsWith("/")) {
			return queryParam.replace("_","\\_") + "%";
		} else {
			return queryParam.replace("_","\\_") + "/%";
		}
	}

	public static String likefyNsPathWithFilter(String envNs, String type, String filter) {
		String resultNs = envNs;
		if (StringUtils.isNotBlank(type)) {
			resultNs = appendToNs(resultNs, type);
		}

		if (StringUtils.isBlank(filter)) {
			resultNs = likefyNsPath(resultNs);
		}
		else {
			resultNs = appendToNs(resultNs, filter);
			resultNs = resultNs.replace("_","\\_");
		}

		return resultNs;
	}

	private static String appendToNs(String nsPath, String suffix) {
		if (nsPath.endsWith("/")) {
			return nsPath + suffix;
		}
		else {
			return nsPath + "/" + suffix;
		}
	}

	/**
     * Masks the secured attributes in work-orders and action-orders
     * 
     * @param cmsWoSimpleBase
     * @param type
     * @return
     */
	public static CmsWorkOrderSimpleBase maskSecuredFields(CmsWorkOrderSimpleBase cmsWoSimpleBase,String type) {
		
		//service CIs
		if (cmsWoSimpleBase.getServices() != null) {
			for (Entry<String,Map<String, CmsCISimple>> serviceEntry : cmsWoSimpleBase.getServices().entrySet()) {
				for (CmsCISimple ci : serviceEntry.getValue().values()) {
					maskSecure(ci);
				}
			}
		}
		
	    //result CI
		if(cmsWoSimpleBase.getResultCi()!=null){
			CmsCISimple resultCi = cmsWoSimpleBase.getResultCi();
			maskSecure(resultCi);
		}
		
		//cloud CI
		if(cmsWoSimpleBase.getCloud()!=null){
			CmsCISimple cloudCi = cmsWoSimpleBase.getCloud();
			maskSecure(cloudCi);
		}
		
		//box CI
		if(cmsWoSimpleBase.getBox()!=null){
			CmsCISimple boxCi = cmsWoSimpleBase.getBox();
			maskSecure(boxCi);
		}
		
		//work-order: pay-load and rfcCI
		if(WORK_ORDER_TYPE.equals(type)){
			CmsWorkOrderSimple cmsWo = (CmsWorkOrderSimple)cmsWoSimpleBase;
			if (cmsWo.getPayLoad() != null) {
				for (Entry<String, List<CmsRfcCISimple>> payloadEntry : cmsWo.getPayLoad().entrySet()) {
					for (CmsRfcCISimple rfcCi : payloadEntry.getValue()) {
						maskSecure(rfcCi);
					}
				}
			}
			
			if(cmsWo.getRfcCi() != null){
				maskSecure(cmsWo.getRfcCi());
			}
		}
		
		
		//action-order: pay-load and CI
		if(ACTION_ORDER_TYPE.equals(type)){
			CmsActionOrderSimple cmsAo = (CmsActionOrderSimple)cmsWoSimpleBase;
			if (cmsAo.getPayLoad() != null) {
				for (Entry<String, List<CmsCISimple>> payloadEntry : cmsAo.getPayLoad().entrySet()) {
					for (CmsCISimple ci : payloadEntry.getValue()) {
						maskSecure(ci);
					}
				}
			}
			
			if(cmsAo.getCi() != null){
				maskSecure(cmsAo.getCi());
			}
		}
		
		return cmsWoSimpleBase;
	}
	
	
	/**
	 * Masks the secured CI attributes
	 * 
	 * @param ci
	 */
	private static void maskSecure(CmsCISimple ci) {
		if(ci.getAttrProps() !=null && ci.getAttrProps().get(CmsConstants.SECURED_ATTRIBUTE) != null) {
				//"true".equals(ci.getAttrProps().get(attrName).get(CmsConstants.SECURED_ATTRIBUTE))){
			for (Entry<String,String> secAttr : ci.getAttrProps().get(CmsConstants.SECURED_ATTRIBUTE).entrySet()) {
				if ("true".equals(secAttr.getValue())) {
					ci.getCiAttributes().put(secAttr.getKey(), MASK);
				}
			}
			ci.getAttrProps().remove(CmsConstants.ENCRYPTED_ATTR_VALUE);
		}		
	}
	
	/**
	 * Masks the secured RfcCI attributes
	 * 
	 * @param rfcCI
	 */
	private static void maskSecure(CmsRfcCISimple rfcCI) {
		if(rfcCI.getCiAttrProps()!=null && rfcCI.getCiAttrProps().get(CmsConstants.SECURED_ATTRIBUTE) != null) {
				//"true".equals(rfcCI.getCiAttrProps().get(attrName).get(CmsConstants.SECURED_ATTRIBUTE))){
			for (Entry<String,String> secAttr : rfcCI.getCiAttrProps().get(CmsConstants.SECURED_ATTRIBUTE).entrySet()) {
				if ("true".equals(secAttr.getValue())) {
					rfcCI.getCiAttributes().put(secAttr.getKey(), MASK);
				}
			}
		}
	}

	private CmsRfcCI newRfcVar(String name, String className, String value) {
		CmsRfcCI var = new CmsRfcCI();
		var.setCiName(name);
		var.setCiClassName(className);
		CmsRfcAttribute valueAttr = new CmsRfcAttribute();
		valueAttr.setAttributeName("value");
		valueAttr.setNewValue(value);
		var.addAttribute(valueAttr);
		return var;
	}
	
	public Map<String,String> getGlobalVars(CmsCI env) {
		return getVarValuesMap(getGlobalVarsRfcs(env));
		/*
		Map<String,String> vars = new HashMap<String,String>();
		vars.put("env_name", env.getCiName());

		List<CmsCIRelation> varRels = cmProcessor.getToCIRelations(env.getCiId(), "manifest.ValueFor", null);

		for (CmsCIRelation varRel : varRels) {
			CmsCI globalVar = varRel.getFromCi();
			vars.put(globalVar.getCiName(), globalVar.getAttribute("value").getDjValue());
		}
		return vars;
		*/
	}
	
	public List<CmsRfcCI> getGlobalVarsRfcs(CmsCI env) {
		List<CmsRfcCI> vars = new ArrayList<CmsRfcCI>();
		CmsRfcCI envNameVar = newRfcVar("env_name","manifest.Globalvar", env.getCiName()); 
		vars.add(envNameVar);
		List<CmsCIRelation> varRels = cmProcessor.getToCIRelations(env.getCiId(), "manifest.ValueFor", null);
		
		for (CmsCIRelation varRel : varRels) {
			vars.add(rfcUtil.mergeRfcAndCi(null, varRel.getFromCi(), DJ_ATTR));
		}
		return vars;
	}

	public Map<String,String> getLocalVars(CmsCI plat) {
		return getVarValuesMap(getLocalVarsRfcs(plat));
		/*
		Map<String,String> vars = new HashMap<String,String>();
		vars.put("platform_name", plat.getCiName());
		
		List<CmsCIRelation> varRels = cmProcessor.getToCIRelations(plat.getCiId(), "manifest.ValueFor", null);
		
		for (CmsCIRelation varRel : varRels) {
			CmsCI globalVar = varRel.getFromCi();
			vars.put(globalVar.getCiName(), globalVar.getAttribute("value").getDjValue());
		}
		return vars;
		*/
	}

	public List<CmsRfcCI> getLocalVarsRfcs(CmsCI plat) {
		List<CmsRfcCI> vars = new ArrayList<CmsRfcCI>();
		CmsRfcCI platNameVar = newRfcVar("platform_name","manifest.Localvar", plat.getCiName()); 
		vars.add(platNameVar);
		
		List<CmsCIRelation> varRels = cmProcessor.getToCIRelations(plat.getCiId(), "manifest.ValueFor", null);
		
		for (CmsCIRelation varRel : varRels) {
			vars.add(rfcUtil.mergeRfcAndCi(null, varRel.getFromCi(), DJ_ATTR));
		}
		return vars;
	}
	
	
	public Map<String,String> getCloudVars(CmsCI cloud) {
		
		return getVarValuesMap(getCloudVarsRfcs(cloud));
		/*
		Map<String,String> vars = new HashMap<String,String>();
		vars.put("cloud_name", cloud.getCiName());
		
		List<CmsCIRelation> varRels = cmProcessor.getToCIRelations(cloud.getCiId(), "account.ValueFor", null);
		
		for (CmsCIRelation varRel : varRels) {
			CmsCI cloudVar = varRel.getFromCi();
			vars.put(cloudVar.getCiName(), cloudVar.getAttribute("value").getDjValue());
		}
		return vars;
		*/
	}
	
	public List<CmsRfcCI> getCloudVarsRfcs(CmsCI cloud) {
		List<CmsRfcCI> vars = new ArrayList<CmsRfcCI>();
		CmsRfcCI cloudNameVar = newRfcVar("cloud_name","account.Cloudvar", cloud.getCiName()); 
		vars.add(cloudNameVar);
		
		List<CmsCIRelation> varRels = cmProcessor.getToCIRelations(cloud.getCiId(), "account.ValueFor", null);
		
		for (CmsCIRelation varRel : varRels) {
			vars.add(rfcUtil.mergeRfcAndCi(null, varRel.getFromCi(), DJ_ATTR));
		}
		return vars;
	}

    public Map<String,String> getVarValuesMap(List<CmsRfcCI> vars) {
    	Map<String,String> varsMap = new HashMap<String, String>();
    	if (vars != null) {
	    	for (CmsRfcCI var : vars) {
	    		if (var.getAttribute(VAR_SEC_ATTR_FLAG) != null &&"true".equals(var.getAttribute(VAR_SEC_ATTR_FLAG).getNewValue()))  {
	    			varsMap.put(var.getCiName(), CmsCrypto.ENC_VAR_PREFIX + var.getAttribute(VAR_SEC_ATTR_VALUE).getNewValue().substring(CmsCrypto.ENC_PREFIX.length()) + CmsCrypto.ENC_VAR_SUFFIX);
	    		} else {
	    			varsMap.put(var.getCiName(), var.getAttribute(VAR_UNSEC_ATTR_VALUE).getNewValue());
	    		}
	    	}
    	}
    	return varsMap;
    }
	
}
