package com.oneops.cms.snapshot.domain;

import java.util.ArrayList;
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
public class NamespaceContent {
    private List<ExportCi> cis = new ArrayList<>();
    private List<ExportRelations> relations = new ArrayList<>();

    public List<ExportCi> getCis() {
        return cis;
    }

    public void setCis(List<ExportCi> cis) {
        this.cis = cis;
    }

    public List<ExportRelations> getRelations() {
        return relations;
    }

    public void setRelations(List<ExportRelations> relations) {
        this.relations = relations;
    }
}
