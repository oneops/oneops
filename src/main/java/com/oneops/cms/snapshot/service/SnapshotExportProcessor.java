package com.oneops.cms.snapshot.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.exceptions.OpsException;
import com.oneops.cms.snapshot.domain.ExportCi;
import com.oneops.cms.snapshot.domain.ExportRelations;
import com.oneops.cms.snapshot.domain.Snapshot;
import com.oneops.cms.util.CmsError;

import java.util.List;

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
public class SnapshotExportProcessor {
    private CmsCmProcessor cmProcessor;


    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }

    public Snapshot export(String ns) {
        int index = ns.lastIndexOf("/");
        String name = ns.substring(index + 1);
        String parentNs = ns.substring(0, index);
        List<CmsCI> list = cmProcessor.getCiBy3NsLike(parentNs, null, name);
        Snapshot snapshot = new Snapshot();
        snapshot.setNamespace(parentNs);
        if (list.size()==1) {
            CmsCI ci = list.get(0);
            snapshot.setId(ci.getCiId());
            snapshot.setName(ci.getCiName());
        }
        
        
        List<CmsCI> cis = cmProcessor.getCiBy3NsLike(ns, null, null);
        for (CmsCI ci: cis) {
            snapshot.addExportCi(ci.getNsPath(), new ExportCi(ci));
        }
        
        List<CmsCIRelation> relations = cmProcessor.getCIRelationsNsLikeNaked(ns, null, null, null,null);
        for (CmsCIRelation rel: relations) {
            snapshot.addExportRelations(rel.getNsPath(), new ExportRelations(rel));
        }
        return snapshot;
    }
}
