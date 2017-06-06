package com.oneops.transistor.snapshot.domain;

import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;

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
public class ExportRelation extends BaseEntity {
    private Long to;
    private Long from;


    public ExportRelation(){}

    public ExportRelation(CmsCIRelation rel) {
        this.setTo(rel.getToCiId());
        this.setFrom(rel.getFromCiId());
        this.setType(rel.getRelationName());
        rel.getAttributes().values().forEach(this::addAttribute);
    }

    private void addAttribute(CmsCIRelationAttribute attr) {
        getAttributes().put(attr.getAttributeName(), attr.getDfValue());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExportRelation that = (ExportRelation) o;

        return to.equals(that.to) && from.equals(that.from) && getType().equals(that.getType());

    }

    @Override
    public int hashCode() {
        int result = to.hashCode();
        result = 31 * result + from.hashCode();
        result = 31 * result + getType().hashCode();
        return result;
    }
}
