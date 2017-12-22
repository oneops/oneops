package com.oneops.transistor.service.peristenceless;

import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;

import java.util.Collection;
import java.util.HashMap;
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
public class BomData {
    private Collection<CmsRfcCI> cis;
    private Collection<CmsRfcRelation> relations;
    private CmsRelease release;
    private Map<String, Object> data = new HashMap<>();

    BomData(CmsRelease release, Collection<CmsRfcCI> cis, Collection<CmsRfcRelation> relations) {
        this.release = release;
        this.cis = cis;
        this.relations = relations;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public void addExtraData(String key, Object data){
        this.data.put(key, data);
    }

    public Collection<CmsRfcCI> getCis() {
        return cis;
    }

    public void setCis(Collection<CmsRfcCI> cis) {
        this.cis = cis;
    }

    public Collection<CmsRfcRelation> getRelations() {
        return relations;
    }

    public void setRelations(Collection<CmsRfcRelation> relations) {
        this.relations = relations;
    }

    public CmsRelease getRelease() {
        return release;
    }

    public void setRelease(CmsRelease release) {
        this.release = release;
    }
}
