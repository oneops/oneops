package com.oneops.cms.snapshot.domain;

import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;

import java.util.ArrayList;
import java.util.HashMap;
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
public class ExportRelations extends ExportBaseEntity {
    private Long from;
    private Long to;


    public ExportRelations() {
    }

    public ExportRelations(CmsCIRelation rel) {
        this.setFrom(rel.getFromCiId());
        this.setTo(rel.getToCiId());
        this.setComments(rel.getComments());
        this.setType(rel.getRelationName());
        rel.getAttributes().values().forEach(this::addAttribute);
    }

    private void addAttribute(CmsCIRelationAttribute attr) {
        attributes.put(attr.getAttributeName(), attr.getDfValue());
        addOwner(attr.getOwner(), attr.getAttributeName());
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }


}
