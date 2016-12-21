package com.oneops.transistor.service;

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
public class DesignExportFlavor {
    private static final String VAR_RELATION = "ValueFor";
    private static final String COMPOSED_OF_RELATION = "ComposedOf";
    private static final String CONSUMES_RELATION = "Consumes";
    private static final String LINKS_TO_RELATION = "LinksTo";
    private static final String REQUIRES_RELATION = "Requires";
    private static final String ESCORTED_RELATION = "EscortedBy";
    private static final String DEPENDS_ON_RELATION = "DependsOn";
    private static final String WATCHEDBY_RELATION = "WatchedBy";

    private static final String GLOBAL_VAR_CLASS = "Globalvar";
    private static final String LOCAL_VAR_CLASS = "Localvar";
    private static final String PLATFORM_CLASS = "Platform";

    private static final String ATTACHMENT_CLASS = "Attachment";
    private static final String BASE_PREFIX = "base.";
    private static final String MANIFEST_PREFIX = "manifest.";
    private static final String CATALOG_PREFIX  = "catalog.";

    private static final String OWNER_DESIGN = "design";
    private static final String OWNER_MANIFEST = "manifest";
    private String owner;
    private String  relPrefix;
    private String classPrefix;
    
    public static DesignExportFlavor MANIFEST = new DesignExportFlavor(true);
    
    public static DesignExportFlavor DESIGN = new DesignExportFlavor(false);

    
    private DesignExportFlavor(boolean manifest) {
        if (manifest) {
            owner = OWNER_MANIFEST;
            relPrefix = MANIFEST_PREFIX;
            classPrefix = MANIFEST_PREFIX;
        } else {
            owner = OWNER_DESIGN;
            relPrefix = BASE_PREFIX;
            classPrefix = CATALOG_PREFIX;
        }
    }
    
    public String getWatchedByRelation() {return classPrefix+WATCHEDBY_RELATION;}
    
    public String getGlobalVarRelation(){
        return relPrefix + VAR_RELATION;
    }
    
    public String getGlobalVarClass(){
        return classPrefix +GLOBAL_VAR_CLASS;
    }
    
    public String getComposedOfRelation(){
        return relPrefix+COMPOSED_OF_RELATION;
    }
    
    public String getPlatformClass(){
        return classPrefix+PLATFORM_CLASS;
    }
    
    public String getLinksToRelation(){
        return classPrefix+LINKS_TO_RELATION;
    }
    
    public String getLocalVarRelation(){
        return classPrefix+VAR_RELATION;
    }
    
    
    public String getLocalVarClass(){
        return classPrefix +LOCAL_VAR_CLASS;
    }
    
    public String getRequiresRelation(){
        return relPrefix+REQUIRES_RELATION;
    }
    
    
    public String getEscortedRelation(){
        return classPrefix+ESCORTED_RELATION;
    }
    
    
    public String getAttachmentClass(){
        return classPrefix+ ATTACHMENT_CLASS;
    }
    
    public String getDependsOnRelation(){
        return classPrefix+DEPENDS_ON_RELATION;
    }

    public String getOwner() {
        return owner;
    }
    
    public String getConsumesRelation(){
        return BASE_PREFIX+CONSUMES_RELATION;
    }
}