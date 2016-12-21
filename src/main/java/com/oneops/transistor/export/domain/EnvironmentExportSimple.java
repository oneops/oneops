package com.oneops.transistor.export.domain;

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
public class EnvironmentExportSimple {
    private ExportCi environment;
    private DesignExportSimple manifest;
    private List<ExportRelation> consumes;
    private List<ExportCi> relays;

    public ExportCi getEnvironment() {
        return environment;
    }

    public void setEnvironment(ExportCi environment) {
        this.environment = environment;
    }

    public List<ExportRelation> getConsumes() {
        return consumes;
    }

    public void setConsumes(List<ExportRelation> consumes) {
        this.consumes = consumes;
    }


    public List<ExportCi> getRelays() {
        return relays;
    }

    public void setRelays(List<ExportCi> relays) {
        this.relays = relays;
    }

    public DesignExportSimple getManifest() {
        return manifest;
    }

    public void getDesign(DesignExportSimple design) {
        this.manifest = design;
    }
}
