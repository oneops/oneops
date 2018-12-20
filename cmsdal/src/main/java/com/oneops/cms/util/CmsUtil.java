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


import com.google.gson.Gson;
import com.oneops.cms.cm.domain.*;
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
import com.oneops.cms.simple.domain.*;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.cms.util.domain.CmsVar;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.oneops.cms.util.CmsError.TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF;
import static com.oneops.cms.util.CmsError.TRANSISTOR_CM_ATTRIBUTE_HAS_CYCLIC_REF;

/**
 * The Class CmsUtil.
 */
public class CmsUtil {

    public static final String CLOUD_VARS_PAYLOAD_NAME = "OO_CLOUD_VARS";
    public static final String GLOBAL_VARS_PAYLOAD_NAME = "OO_GLOBAL_VARS";
    public static final String LOCAL_VARS_PAYLOAD_NAME = "OO_LOCAL_VARS";
    public static final String WORK_ORDER_TYPE = "deploybom";
    public static final String ACTION_ORDER_TYPE = "opsprocedure";
    public static final String CLOUD_SYSTEM_VARS = "CLOUD_SYSTEM_VARS";

    protected static final String GLOBALVARPFX = "$OO_GLOBAL{";
    protected static final String LOCALVARPFX = "$OO_LOCAL{";
    protected static final String CLOUDVARPFX = "$OO_CLOUD{";
    protected static final String MASK = "##############";

    private static final String VAR_SEC_ATTR_FLAG = "secure";
    private static final String VAR_SEC_ATTR_VALUE = "encrypted_value";
    private static final String VAR_UNSEC_ATTR_VALUE = "value";
    private static final String GLOBALVARRPL = "\\$OO_GLOBAL\\{";
    private static final String LOCALVARRPL = "\\$OO_LOCAL\\{";
    private static final String CLOUDVARRPL = "\\$OO_CLOUD\\{";
    private static final String VARSUFFIX ="}";
    private static final String ATTR_PROP_OWNER = "owner";
    private static final String DJ_ATTR = "dj";

    private static final String MANIFEST_PREFIX = "manifest.";
    private static final String BOM_PREFIX = "bom.";
    private static final String CATALOG_PREFIX = "catalog.";
    private static final String CLOUDS_NS_IDENTIFIER = "_clouds";

    private static final Logger logger = Logger.getLogger(CmsUtil.class);
    private static Gson gson = new Gson();

    private CmsCmProcessor cmProcessor;
    private CmsRfcUtil rfcUtil;
    private CmsCrypto cmsCrypto;


    private int countOfErrorsToReport = Integer.valueOf(System.getProperty("cms.countOfErrorsToReport", "10"));

    public static String likefyNsPath(String queryParam) {
		if (queryParam.endsWith("/")) {
			return queryParam.replace("_","\\_") + "%";
		} else {
			return queryParam.replace("_","\\_") + "/%";
		}
	}

    public static String likefyNsPathWithBom(String queryParam) {
		if (queryParam.endsWith("/")) {
			return queryParam.replace("_","\\_") + "%bom/%";
		} else {
			return queryParam.replace("_","\\_") + "%/bom/%";
		}
	}

	public static String likefyNsPathWithoutEndingSlash(String queryParam) {
		if (queryParam.endsWith("/") && queryParam.length() > 1) {
			return queryParam.substring(0, queryParam.length()-1).replace("_","\\_") + "%";
		} else {
			return queryParam.replace("_","\\_") + "%";
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

	public static String likefyNsPathWithTypeNoEndingSlash(String envNs, String type) {
		String resultNs = envNs;
		if (StringUtils.isNotBlank(type)) {
			resultNs = appendToNs(resultNs, type);
		}
		return likefyNsPathWithoutEndingSlash(resultNs);
	}

	public static String appendToNs(String nsPath, String suffix) {
		if (nsPath.endsWith("/")) {
			return nsPath + suffix;
		}
		else {
			return nsPath + "/" + suffix;
		}
	}

	public static boolean isOrgLevel(String nsPath) {
		String trimmedPath = null;
		if (nsPath.length() > 1) {
			if (nsPath.endsWith("/")) {
				trimmedPath = nsPath.substring(1, nsPath.length() - 1);
          } else if (nsPath.startsWith("/")) {
				trimmedPath = nsPath.substring(1);
			}
          if (trimmedPath != null && trimmedPath.split("/").length == 1) {
              return true;
          }

		}
		return false;
	}

    /**
     * Masks the secured attributes in work-orders and action-orders
     *
     * @param cmsWoSimpleBase simple work order for which attributes need to be secured
     * @param type            of the workOrder or actionOrder
     * @return secured workOrder.
     */
    public static <T> CmsWorkOrderSimpleBase maskSecuredFields(CmsWorkOrderSimpleBase<T> cmsWoSimpleBase, String type) {

        //service CIs
        if (cmsWoSimpleBase.getServices() != null) {
            for (Entry<String, Map<String, CmsCISimple>> serviceEntry : cmsWoSimpleBase.getServices().entrySet()) {
                for (CmsCISimple ci : serviceEntry.getValue().values()) {
                    maskSecure(ci);
                }
            }
        }

        //result CI
        if (cmsWoSimpleBase.getResultCi() != null) {
            CmsCISimple resultCi = cmsWoSimpleBase.getResultCi();
            maskSecure(resultCi);
        }

        //cloud CI
        if (cmsWoSimpleBase.getCloud() != null) {
            CmsCISimple cloudCi = cmsWoSimpleBase.getCloud();
            maskSecure(cloudCi);
        }

        //box CI
        if (cmsWoSimpleBase.getBox() != null) {
            CmsCISimple boxCi = cmsWoSimpleBase.getBox();
            maskSecure(boxCi);
        }

        //work-order: pay-load and rfcCI
        if (WORK_ORDER_TYPE.equals(type)) {
            CmsWorkOrderSimple cmsWo = (CmsWorkOrderSimple) cmsWoSimpleBase;
            if (cmsWo.getPayLoad() != null) {
                for (Entry<String, List<CmsRfcCISimple>> payloadEntry : cmsWo.getPayLoad().entrySet()) {
                    for (CmsRfcCISimple rfcCi : payloadEntry.getValue()) {
                        maskSecure(rfcCi);
                    }
                }
            }

            if (cmsWo.getRfcCi() != null) {
                maskSecure(cmsWo.getRfcCi());
            }
        }


        //action-order: pay-load and CI
        if (ACTION_ORDER_TYPE.equals(type)) {
            CmsActionOrderSimple cmsAo = (CmsActionOrderSimple) cmsWoSimpleBase;
            if (cmsAo.getPayLoad() != null) {
                for (Entry<String, List<CmsCISimple>> payloadEntry : cmsAo.getPayLoad().entrySet()) {
                    for (CmsCISimple ci : payloadEntry.getValue()) {
                        maskSecure(ci);
                    }
                }
            }

            if (cmsAo.getCi() != null) {
                maskSecure(cmsAo.getCi());
            }
        }

        return cmsWoSimpleBase;
    }

    /**
     * Masks the secured CI attributes
     *
     * @param ci the ci whose attributes need to be secured.
     */
    private static void maskSecure(CmsCISimple ci) {
        if (ci.getAttrProps() != null && ci.getAttrProps().get(CmsConstants.SECURED_ATTRIBUTE) != null) {
            for (Entry<String, String> secAttr : ci.getAttrProps().get(CmsConstants.SECURED_ATTRIBUTE).entrySet()) {
                if ("true".equals(secAttr.getValue())
			&& ! StringUtils.isEmpty(ci.getCiAttributes().get(secAttr.getKey()))) {
                    ci.getCiAttributes().put(secAttr.getKey(), MASK);
                }
            }
            ci.getAttrProps().remove(CmsConstants.ENCRYPTED_ATTR_VALUE);
        }
    }

    /**
     * Masks the secured RfcCI attributes
     *
     * @param rfcCI rfcCI for which attributes need to be secured.
     */
    protected static void maskSecure(CmsRfcCISimple rfcCI) {
        if (rfcCI.getCiAttrProps() != null && rfcCI.getCiAttrProps().get(CmsConstants.SECURED_ATTRIBUTE) != null) {
            for (Entry<String, String> secAttr : rfcCI.getCiAttrProps().get(CmsConstants.SECURED_ATTRIBUTE).entrySet()) {
                if ("true".equals(secAttr.getValue())) {
                    rfcCI.getCiAttributes().put(secAttr.getKey(), MASK);
                    String ciBaseAttributeName = rfcCI.getCiBaseAttributes().get(secAttr.getKey());
                    if (! StringUtils.isEmpty(ciBaseAttributeName)) {
                    	rfcCI.getCiBaseAttributes().put(secAttr.getKey(), MASK);
                    }
                }
            }
        }
    }

    public static String generateRelComments(String fromCiName, String fromCiClass, String toCiName, String toCiClass) {
        Map<String, String> strMap = new HashMap<>();
        strMap.put("fromCiName", fromCiName);
        strMap.put("fromCiClass", fromCiClass);
        strMap.put("toCiName", toCiName);
        strMap.put("toCiClass", toCiClass);
        return gson.toJson(strMap);
    }

    /**
     * Sets the cm processor.
     *
     * @param cmProcessor the new cm processor
     */
    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }

    public void setCmsCrypto(CmsCrypto cmsCrypto) {
        this.cmsCrypto = cmsCrypto;
    }

    /**
     * Sets the rfc util.
     *
     * @param rfcUtil the new rfc util
     */
    public void setRfcUtil(CmsRfcUtil rfcUtil) {
        this.rfcUtil = rfcUtil;
    }

    /**
     * Cust ci simple2 ci.
     *
     * @param ciSimple  the ci simple
     * @param valueType the value type
     * @return the cms ci
     */
    public CmsCI custCISimple2CI(CmsCISimple ciSimple, String valueType) {
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

        for (String attrSimpleName : ciSimple.getCiAttributes().keySet()) {
            CmsCIAttribute attr = new CmsCIAttribute();
            attr.setAttributeName(attrSimpleName);
            if ("dj".equalsIgnoreCase(valueType)) {
                attr.setDjValue(ciSimple.getCiAttributes().get(attrSimpleName));
            } else if ("df".equalsIgnoreCase(valueType)) {
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
     * @param ci        the ci
     * @param valueType the value type
     * @return the cms ci simple
     */
    public CmsCISimple custCI2CISimple(CmsCI ci, String valueType) {
        return custCI2CISimple(ci, valueType, false);
    }

    public CmsCISimpleWithTags cmsCISimpleWithTags(CmsCI ci, String valueType) {
        return custCI2CISimpleWithTagsLocal(ci, valueType, null, false, null);
    }

    public CmsCISimple custCI2CISimple(CmsCI ci, String valueType, boolean getEncrypted) {
        return custCI2CISimple(ci, valueType, null, getEncrypted, null);
    }

    public CmsCISimple custCI2CISimple(CmsCI ci, String valueType, String attrProps, boolean getEncrypted, String includeAltNs) {
        if (attrProps != null) {
            return custCI2CISimpleLocal(ci, valueType, attrProps.split(","), getEncrypted, includeAltNs);
        } else {
            return custCI2CISimpleLocal(ci, valueType, null, getEncrypted, includeAltNs);
        }

    }

    /**
     * Cust c i2 ci simple.
     *
     * @param ci           the ci
     * @param valueType    the value type
     * @param getEncrypted the get encrypted
     * @return the cms ci simple
     */
    private CmsCISimple custCI2CISimpleLocal(CmsCI ci, String valueType, String[] attrProps, boolean getEncrypted, String includeAltNs) {
        if (ci == null) {
            return null;
        }
        CmsCISimple ciSimple = new CmsCISimple();
        addCiDetails(ciSimple, ci, valueType, attrProps, getEncrypted, includeAltNs);
        return ciSimple;
    }

    private CmsCISimpleWithTags custCI2CISimpleWithTagsLocal(CmsCI ci, String valueType, String[] attrProps, boolean getEncrypted, String includeAltNs) {
        if (ci == null) {
            return null;
        }
        CmsCISimpleWithTags ciSimple = new CmsCISimpleWithTags();
        addCiDetails(ciSimple, ci, valueType, attrProps, getEncrypted, includeAltNs);
        String[] nsElements = ciSimple.getNsPath().split("/");
        if (nsElements.length >= 2) {
            ciSimple.setOrg(nsElements[1]);
        }
        if (nsElements.length >= 3 && !CLOUDS_NS_IDENTIFIER.equals(nsElements[2])) {
            ciSimple.setAssembly(nsElements[2]);
            if (ci.getCiClassName().startsWith(MANIFEST_PREFIX) || ci.getCiClassName().startsWith(BOM_PREFIX)) {
                if (nsElements.length >= 4) {
                    ciSimple.setEnv(nsElements[3]);
                }
                if (nsElements.length >= 6) {
                    ciSimple.setPlatform(nsElements[5]);
                }
            }
            else if (ci.getCiClassName().startsWith(CATALOG_PREFIX)) {
                if (nsElements.length >= 5) {
                    ciSimple.setPlatform(nsElements[4]);
                }
            }
        }
        return ciSimple;
    }

    private void addCiDetails(CmsCISimple ciSimple, CmsCI ci, String valueType, String[] attrProps, boolean getEncrypted, String includeAltNs) {
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

        for (CmsCIAttribute attr : ci.getAttributes().values()) {
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
        if (includeAltNs!=null && includeAltNs.length()>0) {
            List<CmsAltNs> altNsList = cmProcessor.getAltNsByCiAndTag(ci.getCiId(), null);
            for (CmsAltNs altNs: altNsList){
                if (includeAltNs.equalsIgnoreCase("*") || includeAltNs.equalsIgnoreCase(altNs.getTag())){
                    ciSimple.addAltNs(altNs.getTag(), altNs.getNsPath());
                }
            }
        }
    }

    /**
     * Cust ci relation2 ci relation simple.
     *
     * @param rel          the rel
     * @param valueType    the value type
     * @param getEncrypted the get encrypted
     * @return the cms ci relation simple
     */
    public CmsCIRelationSimple custCIRelation2CIRelationSimple(CmsCIRelation rel, String valueType, boolean getEncrypted) {
        return custCIRelation2CIRelationSimple(rel, valueType, getEncrypted, null);
    }

    public CmsCIRelationSimple custCIRelation2CIRelationSimple(CmsCIRelation rel, String valueType, boolean getEncrypted, String[] attrProps) {
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

        for (CmsCIRelationAttribute attr : rel.getAttributes().values()) {
            if ("dj".equalsIgnoreCase(valueType)) {
                relSimple.addRelationAttribute(attr.getAttributeName(), attr.getDjValue());
            } else {
                relSimple.addRelationAttribute(attr.getAttributeName(), attr.getDfValue());
            }
            if (attrProps != null) {
                for (String attrProp : attrProps) {
                    if (attrProp.equalsIgnoreCase(ATTR_PROP_OWNER)) {
                        relSimple.addRelationAttrProp(attrProp, attr.getAttributeName(), attr.getOwner());
                    }
                }
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
    public CmsCIRelation custCIRelationSimple2CIRelation(CmsCIRelationSimple relSimple, String valueType) {
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


        for (String attrSimpleName : relSimple.getRelationAttributes().keySet()) {
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

        if (relSimple.getRelationAttrProps() != null) {
            for (String attrProp : relSimple.getRelationAttrProps().keySet()) {
                if (attrProp.equalsIgnoreCase(ATTR_PROP_OWNER)) {
                    for (String attrName : relSimple.getRelationAttrProps().get(attrProp).keySet()) {
                        String owner = relSimple.getRelationAttrProps().get(attrProp).get(attrName);
                        rel.getAttribute(attrName).setOwner(StringUtils.isEmpty(owner) ? null : owner);
                    }
                }
            }
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
    public CmsRfcCI custRfcCISimple2RfcCI(CmsRfcCISimple rfcSimple) {

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

        rfc.setHint(rfcSimple.getHint());

        for (String attrSimpleName : rfcSimple.getCiAttributes().keySet()) {
            rfc.addAttribute(new CmsRfcAttribute(attrSimpleName, rfcSimple.getCiAttributes().get(attrSimpleName)));
        }

        if (rfcSimple.getCiAttrProps() != null) {
            for (String attrProp : rfcSimple.getCiAttrProps().keySet()) {
                if (attrProp.equalsIgnoreCase(ATTR_PROP_OWNER)) {
                    for (String attrName : rfcSimple.getCiAttrProps().get(attrProp).keySet()) {
                        CmsRfcAttribute rfcAttribute = rfc.getAttribute(attrName);
                        if (rfcAttribute == null) {
                            rfcAttribute = new CmsRfcAttribute(attrName);
                            rfc.addAttribute(rfcAttribute);
                        }
                        rfcAttribute.setOwner(rfcSimple.getCiAttrProps().get(attrProp).get(attrName));
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
    public CmsRfcCISimple custRfcCI2RfcCISimple(CmsRfcCI rfc) {
        return custRfcCI2RfcCISimpleLocal(rfc, null, false);
    }

    /**
     * Cust rfc c i2 rfc ci simple.
     *
     * @param rfc       the rfc
     * @param attrProps the attr props
     * @return the cms rfc ci simple
     */
    public CmsRfcCISimple custRfcCI2RfcCISimple(CmsRfcCI rfc, String attrProps) {
        if (attrProps != null) {
            return custRfcCI2RfcCISimpleLocal(rfc, attrProps.split(","), false);
        } else {
            return custRfcCI2RfcCISimpleLocal(rfc, null, false);
        }
    }

    /**
     * Cust rfc c i2 rfc ci simple.
     *
     * @param rfc       the rfc
     * @param attrProps the attr props
     * @return the cms rfc ci simple
     */
    public CmsRfcCISimple custRfcCI2RfcCISimple(CmsRfcCI rfc, String[] attrProps) {
        return custRfcCI2RfcCISimpleLocal(rfc, attrProps, false);
    }

    private CmsRfcCISimple custRfcCI2RfcCISimpleLocal(CmsRfcCI rfc, String[] attrProps, boolean getEncrepted) {

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

        rfcSimple.setHint(rfc.getHint());

        for (CmsRfcAttribute attr : rfc.getAttributes().values()) {

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
    public CmsRfcRelationSimple custRfcRel2RfcRelSimple(CmsRfcRelation relation) {
        return custRfcRel2RfcRelSimpleLocal(relation, null);
    }

    /**
     * Cust rfc rel2 rfc rel simple.
     *
     * @param relation  the relation
     * @param attrProps the attr props
     * @return the cms rfc relation simple
     */
    public CmsRfcRelationSimple custRfcRel2RfcRelSimple(CmsRfcRelation relation, String[] attrProps) {
        return custRfcRel2RfcRelSimpleLocal(relation, attrProps);
    }

    /**
     * Cust rfc rel2 rfc rel simple.
     *
     * @param relation  the relation
     * @param attrProps the attr props
     * @return the cms rfc relation simple
     */
    public CmsRfcRelationSimple custRfcRel2RfcRelSimple(CmsRfcRelation relation, String attrProps) {
        if (attrProps != null) {
            return custRfcRel2RfcRelSimpleLocal(relation, attrProps.split(","));
        } else {
            return custRfcRel2RfcRelSimpleLocal(relation, null);
        }
    }

    private CmsRfcRelationSimple custRfcRel2RfcRelSimpleLocal(CmsRfcRelation relation, String[] attrProps) {

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


        for (CmsRfcAttribute attr : relation.getAttributes().values()) {
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
    public CmsRfcRelation custRfcRelSimple2RfcRel(CmsRfcRelationSimple relationSimple) {

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


        for (String attrSimpleName : relationSimple.getRelationAttributes().keySet()) {
            relation.addAttribute(new CmsRfcAttribute(attrSimpleName, relationSimple.getRelationAttributes().get(attrSimpleName)));
        }

        if (relationSimple.getRelationAttrProps() != null) {
            for (String attrProp : relationSimple.getRelationAttrProps().keySet()) {
                if (attrProp.equalsIgnoreCase(ATTR_PROP_OWNER)) {
                    for (String attrName : relationSimple.getRelationAttrProps().get(attrProp).keySet()) {
                        CmsRfcAttribute rfcAttribute = relation.getAttribute(attrName);
                        if (rfcAttribute == null) {
                            rfcAttribute = new CmsRfcAttribute(attrName);
                            relation.addAttribute(rfcAttribute);
                        }
                        rfcAttribute.setOwner(relationSimple.getRelationAttrProps().get(attrProp).get(attrName));
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
        wos.setRfcCi(custRfcCI2RfcCISimpleLocal(wo.getRfcCi(), null, true));
        wos.setBox(custCI2CISimple(wo.getBox(), "df", true));
        wos.setCloud(custCI2CISimple(wo.getCloud(), "df", true));

        if (wo.getServices() != null) {
            Map<String, Map<String, CmsCISimple>> simpleServs = new HashMap<>();
            for (Entry<String, Map<String, CmsCI>> serviceEntry : wo.getServices().entrySet()) {
                simpleServs.put(serviceEntry.getKey(), new LinkedHashMap<>());
                for (Entry<String, CmsCI> cloudEntry : serviceEntry.getValue().entrySet()) {
                    simpleServs.get(serviceEntry.getKey()).put(cloudEntry.getKey(), custCI2CISimple(cloudEntry.getValue(), "df", true));
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
        wos.setConfig(wo.getConfig());
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
        wo.setConfig(wos.getConfig());
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
            Map<String, Map<String, CmsCISimple>> simpleServs = new HashMap<>();
            for (Entry<String, Map<String, CmsCI>> serviceEntry : ao.getServices().entrySet()) {
                simpleServs.put(serviceEntry.getKey(), new HashMap<>());
                for (Entry<String, CmsCI> cloudEntry : serviceEntry.getValue().entrySet()) {
                    simpleServs.get(serviceEntry.getKey()).put(cloudEntry.getKey(), custCI2CISimple(cloudEntry.getValue(), "df", true));
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
        aos.setConfig(ao.getConfig());
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
        ao.setActionName(aos.getActionName());
        ao.setActionId(aos.getActionId());
        ao.setProcedureId(aos.getProcedureId());
        ao.setActionState(aos.getActionState());
        ao.setCreated(aos.getCreated());
        ao.setExtraInfo(aos.getExtraInfo());
        ao.setArglist(aos.getArglist());
        ao.setCiId(aos.getCiId());

        if (aos.getCi() != null) {
            ao.setCi(custCISimple2CI(aos.getCi(), null));
        }

        if (aos.getResultCi() != null) {
            ao.setResultCi(custCISimple2CI(aos.getResultCi(), null));
        }
        aos.setConfig(ao.getConfig());
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
     * @param conditions
     * @return the list
     */
    public List<AttrQueryCondition> parseConditions(String[] conditions) {
        if (conditions == null) return null;

        List<AttrQueryCondition> attrConds = new ArrayList<>();
        for (String condition : conditions) {
            for (String attrStr : condition.split(" AND ")) {
                String[] attrArray = attrStr.split(":");
                AttrQueryCondition attrCondition = new AttrQueryCondition();
                attrCondition.setAttributeName(attrArray[0]);
                attrCondition.setCondition(attrArray[1]);
                attrCondition.setAvalue(attrArray[2]);
                attrConds.add(attrCondition);
            }
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
            return nameParts[nameParts.length - 1];
        } else {
            return fullClazzName.replaceAll("base.|mgmt.catalog.|catalog.|mgmt.manifest.|manifest.|bom.|mgmt.|", "");
        }
    }

    public void processAllVars(CmsCI ci,CmsCI env , CmsCI cloud, CmsCI plat) {
        processAllVars(ci, getCloudVars(cloud), getGlobalVars(env), getLocalVars(plat));
    }

    public void processAllVars(CmsCI ci, Map<String, String> cloudVars, Map<String, String> globalVars, Map<String, String> localVars) {
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Processing vars for Ci [")
                    .append(ci.getCiId()).append("] CmsCIAttributes [");
            for (Entry<String, CmsCIAttribute> e : ci.getAttributes().entrySet()) {
                sb.append(e.getKey()).append(":dj:").append(e.getValue().getDjValue());
            }

            sb.append("] Cloud vars [").append(cloudVars).append("]");
            sb.append("] Global vars [").append(globalVars).append("]");
            sb.append("] Local vars [").append(localVars).append("]");

            logger.info(sb.toString());
        }
        //create varContext once
        VariableContext vContext = new VariableContext(ci.getCiId(), ci.getCiName(), ci.getNsPath(), cloudVars, globalVars, localVars);

        ExceptionConsolidator ec = CIValidationException.consolidator(TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF,getCountOfErrorsToReport());
        for (CmsCIAttribute manifestAttr : ci.getAttributes().values()) {
            ec.invokeChecked(() ->
            {
                vContext.setAttrName(manifestAttr.getAttributeName());
                vContext.setUnresolvedAttrValue(manifestAttr.getDjValue());
                String djDfValue = processAllVarsForString(vContext);
                manifestAttr.setDjValue(djDfValue);
                vContext.setUnresolvedAttrValue(manifestAttr.getDfValue());
                manifestAttr.setDfValue(djDfValue);
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

    private String processAllVarsForString(VariableContext variableContext) {
        final String unresolvedAttributeValue = variableContext.getUnresolvedAttrValue();
        //attribute Value to do var interpolation.
        String attrValue = variableContext.getUnresolvedAttrValue();
        boolean isEncrypted = false;
        //check is attribute is used

        if (cmsCrypto.isEncrypted(attrValue)) {
            try {
                attrValue = cmsCrypto.decrypt(attrValue);
            } catch (GeneralSecurityException e) {
                logger.error("Error in decrypting attr: " + variableContext.getAttrName());
                throw new CIValidationException(
                        TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF,
                        getErrorMessage(variableContext.getCiName(), variableContext.getNsPath(), variableContext.getAttrName(), "", "", ""));
            }
            isEncrypted = true;
        }

        if (attrValue != null) {
            if (isCloudVar(attrValue)) {
                attrValue = resolve(variableContext, attrValue, CLOUDVARPFX, CLOUDVARRPL);
            }
            if (isGlobalVar(attrValue)) {
                attrValue = resolve(variableContext, attrValue, GLOBALVARPFX, GLOBALVARRPL);
            }
            if (isLocalVar(attrValue)) {
                attrValue = resolve(variableContext, attrValue, LOCALVARPFX, LOCALVARRPL);
            }
            if (isEncrypted) {
                //is resolved value encrypted , dont encrypt again  .
                if (!cmsCrypto.isVarEncrypted(attrValue)) {
                    try {
                        //encrypt the resolved Value
                        attrValue = cmsCrypto.encrypt(attrValue);
                    } catch (GeneralSecurityException | IOException e) {
                        logger.error("Error in encrypting the var " + variableContext.getAttrName(), e);
                        throw new CIValidationException(
                                TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF,
                                "Error in attribute value for  " + variableContext.getAttrName());
                    }

                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("input>>>>>" + unresolvedAttributeValue + " output>>>" + attrValue);
        }
        return attrValue;
    }

    private String resolve(VariableContext variableContext, String attrValue, String localvarpfx, String localvarrpl) {
        String resolvedValue;
        String variableToResolve;
        List<String> varStructures = splitAttrValue(attrValue, localvarpfx);
        for (String varStructure : varStructures) {
            if (isVarSuffixMissing(varStructure)) {
                throw new CIValidationException(
                        TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF,
                        "Please check the variable syntax  :" + getErrorMessage(variableContext,
                                attrValue,
                                StringUtils.isEmpty(varStructure) ? attrValue : varStructure,
                                localvarpfx));
            }
            variableToResolve = stripSymbolics(varStructure);

            resolvedValue = variableContext.get(variableToResolve, localvarpfx);
            if (resolvedValue == null) {
                check4ValidVariable(variableContext, null, variableToResolve, localvarrpl);
            }
            if (resolvedValue != null) {
                while (isCloudVar(resolvedValue)) {// ez lookup in Cloud Map
                    resolvedValue = performCloudResolution(variableContext, resolvedValue);
                }
                while (isGlobalVar(resolvedValue)) {
                    resolvedValue = performGlobalResolution(variableContext, resolvedValue);
                }
                while (isLocalVar(resolvedValue)) {
                    resolvedValue = performLocalResolution(variableContext, resolvedValue);
                }
                attrValue = subVarValue(variableContext, attrValue, resolvedValue, variableToResolve, localvarrpl);
            } else {
                check4ValidVariable(variableContext, null, variableToResolve, localvarrpl);
            }
        }
        return attrValue;
    }

    private boolean isLocalVar(String attrValue) {
        return attrValue.contains(LOCALVARPFX);
    }

    private boolean isGlobalVar(String attrValue) {
        return attrValue.contains(GLOBALVARPFX);
    }

    private boolean isCloudVar(String attrValue) {
        return attrValue.contains(CLOUDVARPFX);
    }

    private String performLocalResolution(VariableContext variableContext, String resolvedValue) {
        List<String> list = new ArrayList<>();
        while (isLocalVar(resolvedValue))
            resolvedValue = getVar(variableContext, resolvedValue, list, LOCALVARPFX, LOCALVARRPL);
        list.clear();
        while (isGlobalVar(resolvedValue))
            resolvedValue = getVar(variableContext, resolvedValue, list, GLOBALVARPFX, GLOBALVARRPL);
        list.clear();
        while (isCloudVar(resolvedValue)) {
            resolvedValue = getVar(variableContext, resolvedValue, list, CLOUDVARPFX, CLOUDVARRPL);
        }
        return resolvedValue;
    }

    private String performGlobalResolution(VariableContext variableContext, String resolvedValue) {
        List<String> list = new ArrayList<>();
        while (isGlobalVar(resolvedValue))
            resolvedValue = getVar(variableContext, resolvedValue, list, GLOBALVARPFX, GLOBALVARRPL);
        list.clear();
        while (isCloudVar(resolvedValue))
            resolvedValue = getVar(variableContext, resolvedValue, list, CLOUDVARPFX, CLOUDVARRPL);
        return resolvedValue;
    }

    private String performCloudResolution(VariableContext variableContext, String resolvedValue) {
        List<String> list = new ArrayList<>();
        while (isCloudVar(resolvedValue))
            resolvedValue = getVar(variableContext, resolvedValue, list, CLOUDVARPFX, CLOUDVARRPL);
        return resolvedValue;
    }

    private String getVar(VariableContext variableContext, String resolvedValue, List<String> list, String varPfx, String varRPl) {

        if (isVarSuffixMissing(resolvedValue)) {
            throw new CIValidationException(
                    TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF,
                    "Please check the variable syntax  :" + getErrorMessage(variableContext,
                            resolvedValue,
                            resolvedValue,
                            varPfx));
        }

        String varName = stripSymbolicsWithPrefix(resolvedValue, varPfx);
        if (list.contains(varName)) {
            throw new CIValidationException(TRANSISTOR_CM_ATTRIBUTE_HAS_CYCLIC_REF,
                    "Please check Variable declaration, there is a cyclic reference :" + getErrorMessage(variableContext,
                            resolvedValue,
                            varName,
                            varPfx));
        }
        resolvedValue = getResolved(variableContext, resolvedValue, varPfx, varRPl);
        list.add(varName);
        return resolvedValue;
    }

    private boolean isVarSuffixMissing(String resolvedValue) {
        return indexOfVarSuffix(resolvedValue, 0) == -1;
    }

    private String getErrorMessage(VariableContext variableContext, String resolvedValue, String varName, String varPfx) {
        return getErrorMessage(variableContext.getCiName(), variableContext.getNsPath(), variableContext.getAttrName(),  resolvedValue, varName, varPfx) ;
    }

    private String getResolved(VariableContext variableContext, String resolvedValue, String prefix, String regex) {
        String varName = stripSymbolicsWithPrefix(resolvedValue, prefix);
        String varValue = variableContext.get(varName, prefix);
        if (varValue == null) {
            check4ValidVariable(variableContext, varValue, varName, regex);
        }
        resolvedValue = resolvedValue.replaceAll(regex + varName + "}", Matcher.quoteReplacement(varValue));
        return resolvedValue;
    }

    public void processAllVars(CmsRfcCI ci, Map<String, String> cloudVars, Map<String, String> globalVars, Map<String, String> localVars) {
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Processing vars for Ci [")
                    .append(ci.getCiId()).append("] CmsRfcAttribute [");
            for (Entry<String, CmsRfcAttribute> e : ci.getAttributes().entrySet()) {
                sb.append(e.getKey()).append(":new:").append(e.getValue().getOldValue());
            }
            sb.append("] Cloud vars [").append(cloudVars).append("]");
            sb.append("] Global vars [").append(globalVars).append("]");
            sb.append("] Local vars [").append(localVars).append("]");
            logger.info(sb.toString());
        }
        VariableContext vContext = new VariableContext(ci.getCiId(), ci.getCiName(), ci.getNsPath(), cloudVars, globalVars, localVars);

        for (CmsRfcAttribute rfcAttr : ci.getAttributes().values()) {
            vContext.setAttrName(rfcAttr.getAttributeName());
            vContext.setUnresolvedAttrValue(rfcAttr.getNewValue());
            String djDfValue = processAllVarsForString(vContext);
            rfcAttr.setNewValue(djDfValue);
            vContext.setUnresolvedAttrValue(rfcAttr.getOldValue());
            rfcAttr.setOldValue(djDfValue);
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
     * ex input: "$OO_LOCAL{groupId}:$OO_LOCAL{artifactId}:$OO_LOCAL{extension}" input comes back out as
     * a list with the three....[$OO_LOCAL{groupId}, $OO_LOCAL{artifactId}, $OO_LOCAL{extension}]
     *
     * @param inputString the attribute which contains variable
     * @param prefix      prefix to the attribute
     * @return essentially inputString tokenized by prefix as a List
     */
    private List<String> splitAttrValue(String inputString, String prefix) {
        int i = 0;
        int loc = 0;
        List<String> elements = new ArrayList<>();
        while (i < inputString.length()) {
            loc = inputString.indexOf(prefix, i);
            logger.debug("i=" + i + "~~j is where " + prefix + " starts ~~j=" + loc);
            if (loc > -1) {
                elements.add(inputString.substring(loc, inputString.indexOf('}', loc) + 1));
                i = loc + prefix.length();
            } else {
                break;
            }
        }
        return elements;

    }

    /**
     * sets the Attributes Dj and Df value, but ensures it is not an unresolved variable reference
     * runtime exceptions stem from here if that is the case
     */
    private String subVarValue(VariableContext variableContext, String attrValue, String resolvedValue, String varName, String replPrefix) {

        String ciName = variableContext.getCiName();

        if(isCloudVar(attrValue)){
            if(resolvedValue.indexOf(':') != -1) {
                String[] values = resolvedValue.split(":");
                if (values.length > 0 && variableContext.getCloudVar(values[0]) != null) {
                    resolvedValue = variableContext.getCloudVar(values[0]);
                } else if (values.length > 1 && values[1] != "") {
                    resolvedValue = values[1];
                } else {
                    resolvedValue = null;
                }
            }else{
                if (variableContext.getCloudVar(resolvedValue) != null) {
                    resolvedValue = variableContext.getCloudVar(resolvedValue);
                }
            }
        }

        check4ValidVariable(variableContext, resolvedValue, varName, replPrefix);

        //prefix.$OO_LOCAL{x}.suffix in Dj to-> prefix.RR.suffix
        StringBuilder pattToReplace = new StringBuilder(replPrefix).append(varName).append("\\}");
        String resAfter = attrValue.replaceAll(pattToReplace.toString(), Matcher.quoteReplacement(resolvedValue));
        if (logger.isDebugEnabled()) {
            logger.debug("Resolved value set to :" + resAfter + " in Ci " + ciName);
        }
        return resAfter;
    }

    private void check4ValidVariable(VariableContext variableContext, String resolvedValue, String varName, String replPrefix) {
        String ciName = variableContext.getCiName();
        String nsPath = variableContext.getNsPath();
        String attrName = variableContext.getAttrName();
        if (resolvedValue == null ||        //fix, it is actually okay if resolvedValue equals("")
                isCloudVar(resolvedValue) ||
                isGlobalVar(resolvedValue) ||
                isLocalVar(resolvedValue)) {//substituion did not happen: bad.
            String errorMessage = getErrorMessage(ciName, nsPath, attrName, resolvedValue, varName, replPrefix).toString();
            logger.warn(errorMessage);
            throw new CIValidationException(TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF,
                    errorMessage);

        }
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
        if (prefix != null) {
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
        if (nsPath != null) {
            Matcher matcher = Pattern.compile("(/[^/]+){2}$").matcher(nsPath);
            if (matcher.find()) {
                return matcher.group().substring(1);
            }
        }
        return nsPath;
    }

    /**
     * $OO_CLOUD{xyz} returned as xyz
     */
    private String stripSymbolics(String variableReference) {
        return variableReference.substring(variableReference.indexOf("{") + 1, indexOfVarSuffix(variableReference,0));

    }

    private String stripSymbolicsWithPrefix(String variableReference, String prefix) {
        int startIndex = variableReference.indexOf(prefix) + prefix.length();
        return variableReference.substring(startIndex, indexOfVarSuffix(variableReference,startIndex));
    }

    private int indexOfVarSuffix(String variableReference, int startIndex) {
        return variableReference.indexOf(VARSUFFIX,startIndex);
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

	private List<CmsCI> getVarsCIs(CmsCI ci, String relationName) {
		List<CmsCI> vars = new ArrayList<CmsCI>();
		List<CmsCIRelation> varRels = cmProcessor.getToCIRelations(ci.getCiId(), relationName, null);

		for (CmsCIRelation varRel : varRels) {
			vars.add(varRel.getFromCi());
		}
		return vars;
	}

	private CmsCI newVarCi(String name, String className, String value) {
		CmsCI var = new CmsCI();
		var.setCiName(name);
		var.setCiClassName(className);
		CmsCIAttribute valueAttr = new CmsCIAttribute();
		valueAttr.setAttributeName("value");
		valueAttr.setDfValue(value);
		var.addAttribute(valueAttr);
		return var;
	}

    public Map<String, String> getGlobalVars(CmsCI env) {
        return getVarValuesMap(getGlobalVarsRfcs(env));
    }

    public List<CmsRfcCI> getGlobalVarsRfcs(CmsCI env) {
        List<CmsRfcCI> vars = new ArrayList<>();
        CmsRfcCI envNameVar = newRfcVar("env_name", "manifest.Globalvar", env.getCiName());
        vars.add(envNameVar);
        List<CmsCIRelation> varRels = cmProcessor.getToCIRelations(env.getCiId(), "manifest.ValueFor", null);

        for (CmsCIRelation varRel : varRels) {
            vars.add(rfcUtil.mergeRfcAndCi(null, varRel.getFromCi(), DJ_ATTR));
        }
        return vars;
    }

    public Map<String, String> getLocalVars(CmsCI plat) {
        return getVarValuesMap(getLocalVarsRfcs(plat));
    }

    public List<CmsRfcCI> getLocalVarsRfcs(CmsCI plat) {
        List<CmsRfcCI> vars = new ArrayList<>();
        CmsRfcCI platNameVar = newRfcVar("platform_name", "manifest.Localvar", plat.getCiName());
        vars.add(platNameVar);

        List<CmsCIRelation> varRels = cmProcessor.getToCIRelations(plat.getCiId(), "manifest.ValueFor", null);

        for (CmsCIRelation varRel : varRels) {
            vars.add(rfcUtil.mergeRfcAndCi(null, varRel.getFromCi(), DJ_ATTR));
        }
        return vars;
    }

    public Map<String, String> getCloudVars(CmsCI cloud) {
        return getVarValuesMap(getCloudVarsRfcs(cloud));
    }

    public List<CmsRfcCI> getCloudVarsRfcs(CmsCI cloud) {
        List<CmsRfcCI> vars = new ArrayList<>();
        CmsRfcCI cloudNameVar = newRfcVar("cloud_name", "account.Cloudvar", cloud.getCiName());
        vars.add(cloudNameVar);
        List<CmsCIRelation> varRels = cmProcessor.getToCIRelations(cloud.getCiId(), "account.ValueFor", null);
        for (CmsCIRelation varRel : varRels) {
            vars.add(rfcUtil.mergeRfcAndCi(null, varRel.getFromCi(), DJ_ATTR));
        }
        return vars;
    }

    private Map<String, String> getVarValuesMap(List<CmsRfcCI> vars) {
        Map<String, String> varsMap = new HashMap<>();
        if (vars != null) {
            for (CmsRfcCI var : vars) {
                if (var.getAttribute(VAR_SEC_ATTR_FLAG) != null && "true".equals(var.getAttribute(VAR_SEC_ATTR_FLAG).getNewValue())) {
                    varsMap.put(var.getCiName(), CmsCrypto.ENC_VAR_PREFIX + var.getAttribute(VAR_SEC_ATTR_VALUE).getNewValue().substring(CmsCrypto.ENC_PREFIX.length()) + CmsCrypto.ENC_VAR_SUFFIX);
                } else {
                    varsMap.put(var.getCiName(), var.getAttribute(VAR_UNSEC_ATTR_VALUE).getNewValue());
                }
            }

            if(varsMap.get("cloud_name") != null) {
                Map<String, Object> mappings = getCloudSystemVars();
                if (mappings != null) {
                    Set<String> mappingCloudsKey = (mappings.keySet()
                            .stream()
                            .filter(s -> Pattern.compile(s).matcher(varsMap.get("cloud_name")).matches())
                            .collect(Collectors.toSet()));
                    if (mappingCloudsKey != null && mappingCloudsKey.size() > 0) {
                        varsMap.putAll((Map<String, String>) mappings.get(mappingCloudsKey.toArray()[0]));
                    }
                }
            }
        }
        return varsMap;
    }

    private Map<String, Object> getCloudSystemVars() {
        Map<String, Object> mappings = null;
        CmsVar cmsVar = cmProcessor.getCmSimpleVar(CLOUD_SYSTEM_VARS);
        if (cmsVar != null) {
            String json = cmsVar.getValue();
            if (json != null && !json.isEmpty()) {
                mappings = gson.fromJson(cmsVar.getValue(), Map.class);
            }
        }

        if (mappings == null || mappings.size() == 0) {
            logger.warn("Cloud provider mappings is not set.");
            mappings = null;
        }

        return mappings;
    }

    private Map<String, String> getVarCiValuesMap(List<CmsCI> vars) {
    	Map<String,String> varsMap = new HashMap<>();
    	if (vars != null) {
	    	for (CmsCI var : vars) {
	    		if (var.getAttribute(VAR_SEC_ATTR_FLAG) != null &&"true".equals(var.getAttribute(VAR_SEC_ATTR_FLAG).getDfValue()))  {
	    			varsMap.put(var.getCiName(), CmsCrypto.ENC_VAR_PREFIX + var.getAttribute(VAR_SEC_ATTR_VALUE).getDfValue().substring(CmsCrypto.ENC_PREFIX.length()) + CmsCrypto.ENC_VAR_SUFFIX);
	    		} else {
	    			varsMap.put(var.getCiName(), var.getAttribute(VAR_UNSEC_ATTR_VALUE).getDfValue());
	    		}
	    	}
    	}
    	return varsMap;
    }

    /**
     * Returns a map of OO_CLOUD_VARS,OO_GLOBAL_VARS,LOCAL_VARS_PAYLOAD_NAME
     "OO_GLOBAL_VARS": [
     {
     "nsId": 0,
     "ciAttributes": {
     "value": "r1"
     },
     "attrProps": {},
     "ciId": 0,
     "ciName": "env_name",
     "ciClassName": "manifest.Globalvar",
     "lastAppliedRfcId": 0
     },
     * @param cloud for which cloud vars need to be resolved
     * @param env for which global vars need to be resolved
     * @param platform for which local vars need to be resolved
     * @return
     */
    public Map<String, List<CmsCI>> getResolvedVariableCIs(CmsCI cloud, CmsCI env, CmsCI platform) {
        List<CmsCI> cloudVarCis = new ArrayList<>();
        CmsCI cloudNameVar = newVarCi("cloud_name", "account.Cloudvar", cloud.getCiName());
        cloudVarCis.add(cloudNameVar);
        cloudVarCis.addAll(getVarsCIs(cloud, "account.ValueFor"));
        Map<String, String> cloudVars = getVarCiValuesMap(cloudVarCis);

        List<CmsCI> globalVarCis = new ArrayList<>();
        CmsCI envNameVar = newVarCi("env_name", "manifest.Globalvar", env.getCiName());
        globalVarCis.add(envNameVar);
        globalVarCis.addAll(getVarsCIs(env, "manifest.ValueFor"));
        Map<String, String> globalVars = getVarCiValuesMap(globalVarCis);

        List<CmsCI> localVarCis = new ArrayList<>();
        CmsCI platformNameVar = newVarCi("platform_name", "manifest.Localvar", platform.getCiName());
        localVarCis.add(platformNameVar);
        localVarCis.addAll(getVarsCIs(platform, "manifest.ValueFor"));
        Map<String, String> localVars = getVarCiValuesMap(localVarCis);

        //Cloud vars
        VariableContext context = new VariableContext(0, null, null, cloudVars, globalVars, localVars);

        processVarCi(cloudVarCis, cloudVars, context);

        //Global vars
        processVarCi(globalVarCis, globalVars, context);

        //Local vars
        processVarCi(localVarCis, localVars, context);

        Map<String, List<CmsCI>> resolvedVariableCIs = new HashMap<>();
        resolvedVariableCIs.put(CLOUD_VARS_PAYLOAD_NAME, cloudVarCis);
        resolvedVariableCIs.put(GLOBAL_VARS_PAYLOAD_NAME, globalVarCis);
        resolvedVariableCIs.put(LOCAL_VARS_PAYLOAD_NAME, localVarCis);

        return resolvedVariableCIs;
    }

    private void processVarCi(List<CmsCI> varCis, Map<String, String> varMap, VariableContext context) {
        for (CmsCI varCi : varCis) {
            String varName = varCi.getCiName();
            context.setUnresolvedAttrValue(varMap.get(varName));
            String value = processAllVarsForString(context);
            varCi.getAttribute("value").setDfValue(value);
        }
    }
    public int getCountOfErrorsToReport() {
        return countOfErrorsToReport;
    }

    public void setCountOfErrorsToReport(int countOfErrorsToReport) {
        this.countOfErrorsToReport = countOfErrorsToReport;
    }
}
