package com.oneops.transistor.snapshot.domain;

import java.util.*;

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
    private List<Part> parts = new ArrayList<>();// we need it sorted by NS for proper restore order
    private long lastAppliedCiRfc = 0;
    private long lastAppliedRelationRfc = 0;
    private long release;
    private String namespace;
    private long timestamp = System.currentTimeMillis();

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
    }

    public void add(Part part) {
        this.parts.add(part);
    }
    
    public Set<String> allNamespaces(){
        Set<String> set = new HashSet<>();
        for (Part part:parts){
            set.addAll(part.getCis().keySet());
            set.addAll(part.getRelations().keySet());
        }
        return set;
    }

    public void updateLastAppliedCiRfc(long lastAppliedRfcId) {
        if (lastAppliedRfcId>this.lastAppliedCiRfc){
            this.lastAppliedCiRfc = lastAppliedRfcId;
        }
    }


    public long getLastAppliedCiRfc() {
        return lastAppliedCiRfc;
    }

    public void setLastAppliedCiRfc(long lastAppliedCiRfc) {
        this.lastAppliedCiRfc = lastAppliedCiRfc;
    }

    public void setRelease(long release) {
        this.release = release;
    }

    public long getRelease() {
        return release;
    }

    public void updateLastAppliedRelationRfc(long lastAppliedRelationRfc) {
        if (lastAppliedRelationRfc>this.lastAppliedRelationRfc) {
            this.lastAppliedRelationRfc = lastAppliedRelationRfc;
        }
    }

    public long getLastAppliedRelationRfc() {
        return lastAppliedRelationRfc;
    }

    public void setLastAppliedRelationRfc(long lastAppliedRelationRfc) {
        this.lastAppliedRelationRfc = lastAppliedRelationRfc;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }
}
