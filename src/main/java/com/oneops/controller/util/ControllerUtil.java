package com.oneops.controller.util;

import java.util.Map.Entry;

import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsConstants;

public class ControllerUtil {

	public CmsWorkOrderSimple stripWO(CmsWorkOrderSimple wo) {
		return stripWO(wo, false);
	}	
	public CmsWorkOrderSimple stripWO(CmsWorkOrderSimple wo, boolean resetEncryptedValues) {
        CmsWorkOrderSimple strippedWo = new CmsWorkOrderSimple();
        strippedWo.setComments(wo.getComments());
        strippedWo.setCreated(wo.getCreated());
        strippedWo.setDeploymentId(wo.getDeploymentId());
        strippedWo.setDpmtRecordId(wo.getDpmtRecordId());
        strippedWo.setDpmtRecordState(wo.getDpmtRecordState());
        strippedWo.setOps(wo.getOps());
        strippedWo.setRfcId(wo.getRfcId());
        if (resetEncryptedValues) {
        	strippedWo.setRfcCi(resetEncVarValues(wo.getRfcCi()));
		} else {
			strippedWo.setRfcCi(wo.getRfcCi());
		}	
        if (wo.getResultCi() != null) {
        	if (resetEncryptedValues) {
        		strippedWo.setResultCi(resetEncVarValues(wo.getResultCi()));
        	} else {
        		strippedWo.setResultCi(wo.getResultCi());
        	}
        }
        strippedWo.setAdditionalInfo(wo.getAdditionalInfo());
        strippedWo.setConfig(wo.getConfig());
        return strippedWo;
    }

    public CmsCISimple resetEncVarValues(CmsCISimple ci) {
        if (ci.getAttrProps() != null && ci.getAttrProps().containsKey(CmsConstants.ENCRYPTED_ATTR_VALUE)) {
            for (Entry<String, String> encAttr : ci.getAttrProps().get(CmsConstants.ENCRYPTED_ATTR_VALUE).entrySet()) {
                ci.getCiAttributes().put(encAttr.getKey(), encAttr.getValue());
            }
        }
        return ci;
    }

    public CmsRfcCISimple resetEncVarValues(CmsRfcCISimple rfc) {
        if (rfc.getCiAttrProps() != null && rfc.getCiAttrProps().containsKey(CmsConstants.ENCRYPTED_ATTR_VALUE)) {
            for (Entry<String, String> encAttr : rfc.getCiAttrProps().get(CmsConstants.ENCRYPTED_ATTR_VALUE).entrySet()) {
                rfc.getCiAttributes().put(encAttr.getKey(), encAttr.getValue());
            }
        }
        return rfc;
    }
  
    
}
