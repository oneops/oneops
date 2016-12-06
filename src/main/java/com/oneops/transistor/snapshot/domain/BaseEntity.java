package com.oneops.transistor.snapshot.domain;

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
public class BaseEntity {
    private String type;
    private Map<String, String> attributes = new HashMap<>();
    private Map<String, List<String>> owners = new HashMap<>();

    void addOwner(String owner, String name) {
        if (owner != null && !owner.isEmpty()) {
            List<String> ownerList = owners.get(owner);
            if (ownerList == null) {
                ownerList = new ArrayList<>();
                owners.put(owner, ownerList);
            }
            ownerList.add(name);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Map<String, List<String>> getOwners() {
        return owners;
    }

    public void setOwners(Map<String, List<String>> owners) {
        this.owners = owners;
    }

    public String getOwner(String key) {
        for (String owner:owners.keySet()){
            if (owners.get(owner).contains(key)) return owner;
        }
        return null;
    }
}
