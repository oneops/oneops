package com.oneops.cms.snapshot.domain;

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
public class Snapshot {
    private long id;
    private String name;
    private String namespace;
    private String tag;
    private Map<String, List<ExportCi>> components = new HashMap<>();
    private Map<Long, ExportCi> ciMap= new HashMap<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    
    public void addExportCi(String namespace, ExportCi exportCi){
        ciMap.put(exportCi.getId(), exportCi);
        List<ExportCi> list = components.get(namespace);
        if (list==null){
            list = new ArrayList<>();
            components.put(namespace, list);
        }
        list.add(exportCi);
    }

    public void addExportRelations(long from, ExportRelation exportRelation) {
        ExportCi exportCi = ciMap.get(from);
        if (exportCi!=null) {
            exportCi.addRelation(exportRelation);
        }
    }

  

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Map<String, List<ExportCi>> getComponents() {
        return components;
    }

    public void setComponents(Map<String, List<ExportCi>> components) {
        this.components = components;
    }
}
