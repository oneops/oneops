package com.oneops.transistor.snapshot.domain;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;

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
public class ExportCi extends BaseEntity {
    private long id;
    private String name;
    private String comments;

    public ExportCi() {
    }

    public ExportCi(CmsCI ci) {
        id = ci.getCiId();
        name = ci.getCiName();
        setType(ci.getCiClassName());
        setComments(ci.getComments());
        ci.getAttributes().values().forEach(this::addAttribute);
    }

    private void addAttribute(CmsCIAttribute attr) {
        getAttributes().put(attr.getAttributeName(), attr.getDfValue());
        addOwner(attr.getOwner(), attr.getAttributeName());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
