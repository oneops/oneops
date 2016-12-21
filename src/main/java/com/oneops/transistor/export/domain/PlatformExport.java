package com.oneops.transistor.export.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlatformExport extends ExportCi {
    private Map<String, String> variables;
    private List<String> links;
    private List<ComponentExport> components;
    private List<ExportRelation> consumes;
  
    public List<ExportRelation> getConsumes() {
        return consumes;
    }

    public void setConsumes(List<ExportRelation> consumes) {
        this.consumes = consumes;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> localVars) {
        this.variables = localVars;
    }

    public void addVariable(String name, String value) {
        if (value != null) {
            if (this.variables == null) {
                this.variables = new HashMap<>();
            }
            this.variables.put(name, value);
        }
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> linksTos) {
        this.links = linksTos;
    }

    public void addLink(String linksTo) {
        if (this.links == null) {
            this.links = new ArrayList<String>();
        }
        this.links.add(linksTo);
    }

    public List<ComponentExport> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentExport> components) {
        this.components = components;
    }

    public void addComponent(ComponentExport component) {
        if (this.components == null) {
            this.components = new ArrayList<ComponentExport>();
        }
        this.components.add(component);
    }

    public void addConsume(ExportRelation relation) {
        if (this.consumes == null) {
            this.consumes = new ArrayList<>();
        }
        this.consumes.add(relation);
    }
}
