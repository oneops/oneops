package com.oneops.transistor.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.transistor.snapshot.domain.ExportCi;
import com.oneops.transistor.snapshot.domain.ExportRelation;
import com.oneops.transistor.snapshot.domain.Part;
import com.oneops.transistor.snapshot.domain.Snapshot;
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
public class SnapshotProcessor {
    public static final String SNAPSHOT_RESTORE = "restore";
    private static Logger logger = Logger.getLogger(SnapshotProcessor.class);

    private CmsCmProcessor cmProcessor;
    private CmsRfcProcessor rfcProcessor;
    private CmsCmRfcMrgProcessor rfcMrgProcessor;


    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }

    public Snapshot exportSnapshot(String[] namespaces, String[] classNames) {
        Snapshot snapshot = new Snapshot();
        for (int i = 0; i < namespaces.length; i++) {
            String namespace = namespaces[i];
            String clazzName = classNames.length - 1 < i ? null : classNames[i];
            List<CmsCI> cis = cmProcessor.getCiBy3NsLike(namespace, clazzName, null);
            Part part = new Part(namespace, clazzName);

            for (CmsCI ci : cis) {
                part.addExportCi(ci.getNsPath(), new ExportCi(ci));
            }

            List<CmsCIRelation> relations = cmProcessor.getCIRelationsNsLikeNaked(namespace, null, null, clazzName, null);
            for (CmsCIRelation rel : relations) {
                part.addExportRelations(rel.getFromCiId(), new ExportRelation(rel));
            }
            snapshot.add(part);
        }
        return snapshot;
    }

    public void importSnapshot(Snapshot snapshot) {
        snapshot.getParts().forEach(this::restoreCis);
    }

    private void restoreCis(Part part) {
        List<CmsCI> existingCis = cmProcessor.getCiBy3NsLike(part.getNs(), part.getClassName(), null);
        for (String actualNs : part.getCis().keySet()) {
            for (ExportCi eci: part.getCis().get(actualNs)) {
                CmsCI ci = findMatchingCi(actualNs, eci, existingCis);
                if (ci == null) {
                    addCi(actualNs, eci);
                } else {
                    updateCi(ci, eci);
                }
            }
        }
        existingCis.forEach(this::remove);     // remove remaining CIs that aren't a part of the snapshot
        
    }

    private void addCi(String ns, ExportCi eci) {
        
        CmsRfcCI rfcCi = newFromExportCi(ns, eci);
        logger.info("adding ci:" + rfcCi.getCiName()+"@" +rfcCi.getNsPath());
        rfcMrgProcessor.upsertCiRfc(rfcCi, SNAPSHOT_RESTORE);
//        for (ExportRelation relation :eci.getRelations()){
//            
//        }
    }

    private static CmsRfcCI newFromExportCi(String ns, ExportCi eCi) {
        CmsRfcCI rfc = newFromExportCiWithoutAttr(ns, eCi);
        if (eCi.getAttributes() != null) {
            for (Map.Entry<String, String> attr : eCi.getAttributes().entrySet()) {
                CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
                rfcAttr.setAttributeName(attr.getKey());
                rfcAttr.setNewValue(attr.getValue());
                rfcAttr.setOwner(eCi.getOwner(attr.getKey()));
                rfc.addAttribute(rfcAttr);
            }
        }
        return rfc;
    }

    private static CmsRfcCI newFromExportCiWithoutAttr(String ns, ExportCi eCi) {
        CmsRfcCI rfc = new CmsRfcCI();
        rfc.setCiName(eCi.getName());
        rfc.setCiClassName(eCi.getType());
        rfc.setNsPath(ns);
        return rfc;
    }

    private void remove(CmsCI ci) {
        logger.info("remove ci:" + ci.getCiName());
        rfcMrgProcessor.requestCiDelete(ci.getCiId(), "restore");
    }

    private void updateCi(CmsCI ci, ExportCi eci) {
        logger.info("Update:" + ci.getCiName());
        Map<String, CmsCIAttribute> existingAttributes = ci.getAttributes();
        Map<String, String> snapshotAttributes = eci.getAttributes();
        CmsRfcCI rfcCI = newFromExportCiWithoutAttr(ci.getNsPath(), eci);
        rfcCI.setCiId(ci.getCiId());
        for (String key : snapshotAttributes.keySet()) {
            CmsCIAttribute ciAttribute = existingAttributes.remove(key);
            String value = snapshotAttributes.get(key);

            if (ciAttribute == null || (ciAttribute.getDfValue() == null && value != null) || (ciAttribute.getDfValue() != null && !ciAttribute.getDfValue().equals(value))) {
                createRfcAttribute(rfcCI, key, value, eci.getOwner(key));
            }
        }

        if (!rfcCI.getAttributes().isEmpty()) {
            rfcMrgProcessor.upsertCiRfc(rfcCI, SNAPSHOT_RESTORE);
        }
    }

    private static void createRfcAttribute(CmsRfcCI rfcCi, String key, String value, String owner) {
        CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
        rfcAttr.setAttributeName(key);
        rfcAttr.setNewValue(value);
        rfcAttr.setOwner(owner);
        rfcCi.addAttribute(rfcAttr);
    }

    private static CmsCI findMatchingCi(String ns, ExportCi eci, List<CmsCI> cis) {
        for (CmsCI ci : cis) {
            if (eci.getName().equals(ci.getCiName()) && eci.getType().equals(ci.getCiClassName()) && ns.equals(ci.getNsPath())) {
                cis.remove(ci);
                return ci;
            }
        }
        return null;
    }


    public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
        this.rfcProcessor = rfcProcessor;
    }

    public void setRfcMrgProcessor(CmsCmRfcMrgProcessor rfcMrgProcessor) {
        this.rfcMrgProcessor = rfcMrgProcessor;
    }

}