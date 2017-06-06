package com.oneops.transistor.service;

import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAttribute;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.md.domain.CmsRelationAttribute;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/*******************************************************************************
 *
 *   Copyright 2016 Walmart, Inc.
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
public class RfcUtil {
    private static Logger logger = Logger.getLogger(RfcUtil.class);
    private static final String DUMMY_ENCRYPTED_IMP_VALUE = "CHANGE ME!!!";
    private static final String ENCRYPTED_PREFIX = "::ENCRYPTED::";
    static final String DUMMY_ENCRYPTED_EXP_VALUE = ENCRYPTED_PREFIX;


    static CmsRfcAttribute getAttributeRfc(String key, String value, String owner) {
        CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
        rfcAttr.setAttributeName(key);
        rfcAttr.setNewValue(value);
        rfcAttr.setOwner(owner);
        return rfcAttr;
    }



    static CmsRfcCI bootstrapNewRfc(String name, String type, Map<String, String> attributes, String owner) {
        CmsRfcCI rfc = new CmsRfcCI();
        RfcUtil.bootstrapRfc(rfc, name, type, attributes, owner);
        return rfc;
    }

    static void bootstrapRelationRfc(CmsRfcRelation relRfc, CmsRfcCI fromRfc, CmsRfcCI toRfc, long releaseId, String userId) {
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
    }


    static void bootstrapRfc(CmsRfcCI rfc, String name, String type, Map<String, String> attributes, String owner) {
        rfc.setCiName(name);
        rfc.setCiClassName(type);
        if (attributes != null) {
            for (Map.Entry<String, String> attr : attributes.entrySet()) {
                String newValue = attr.getValue();
                if (newValue != null && newValue.startsWith(ENCRYPTED_PREFIX)) {
                    newValue = parseEncryptedImportValue(newValue);
                }
                if (rfc.getAttribute(attr.getKey()) != null) {
                    rfc.getAttribute(attr.getKey()).setNewValue(newValue);
                    rfc.getAttribute(attr.getKey()).setOwner(owner);
                } else {
                    CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
                    rfcAttr.setAttributeName(attr.getKey());
                    rfcAttr.setNewValue(newValue);
                    rfcAttr.setOwner(owner);
                    rfc.addAttribute(rfcAttr);
                }
            }
        }
    }

    static String parseEncryptedImportValue(String encValue) {
        String value = encValue.substring(ENCRYPTED_PREFIX.length());
        if (value.length() == 0) {
            value = DUMMY_ENCRYPTED_IMP_VALUE;
        }
        return value;
    }

    static boolean isEncrypted(String var) {
        return var.startsWith(ENCRYPTED_PREFIX);
    }

    static String checkEncrypted(String value) {
        return (value != null && value.startsWith(ENCRYPTED_PREFIX)) ? ENCRYPTED_PREFIX : value;
    }


    public static void bootstrapNewMandatoryAttributesFromMetadataDefaults(CmsRfcRelation rel, CmsRelation cmsRelation) {
        for (CmsRelationAttribute clAttr : cmsRelation.getMdAttributes()) {
            if (rel.getAttribute(clAttr.getAttributeName()) == null && clAttr.getIsMandatory()) {
                CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
                rfcAttr.setAttributeId(clAttr.getAttributeId());
                rfcAttr.setAttributeName(clAttr.getAttributeName());

                rel.addAttribute(rfcAttr);

                if (clAttr.getDefaultValue() != null) {
                    rfcAttr.setNewValue(clAttr.getDefaultValue());
                } else {
                    rfcAttr.setNewValue("");
                    logger.warn("Relation " + rel.getRelationName() + " metadata has mandatory attribute " + clAttr.getAttributeName() + "that has no default value");
                }
            }
        }
    }

    public static void bootstrapNewMandatoryAttributesFromMetadataDefaults(CmsRfcCI rel, CmsClazz targetClazz) {
        for (CmsClazzAttribute clAttr : targetClazz.getMdAttributes()) {
            if (rel.getAttribute(clAttr.getAttributeName()) == null && clAttr.getIsMandatory()) {
                CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
                rfcAttr.setAttributeId(clAttr.getAttributeId());
                rfcAttr.setAttributeName(clAttr.getAttributeName());

                rel.addAttribute(rfcAttr);
                if (clAttr.getDefaultValue() != null) {
                    rfcAttr.setNewValue(clAttr.getDefaultValue());
                } else {
                    rfcAttr.setNewValue("");
                    logger.warn("Ci " + rel.getCiName() + "of " + rel.getCiClassName() + "class metadata has mandatory attribute "+clAttr.getAttributeName()+" that has no default value");
                }
            }
        }
    }
}
