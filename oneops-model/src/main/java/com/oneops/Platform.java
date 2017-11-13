package com.oneops;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

public class Platform {
  private String id;
  private String pack;
  private String packVersion;
  private String source;
  private String enable;
  private List<String> links;
  private Map<String, String> variables;
  private Map<String, String> encryptedvariables;
  private Map<String, Component> components;
  private Map<String, String> configuration;
  private Map<String, Map<String, String>> scale;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPack() {
    return pack;
  }

  public void setPack(String pack) {
    this.pack = pack;
  }

  public String getPackVersion() {
    return packVersion;
  }

  public void setPackVersion(String packVersion) {
    this.packVersion = packVersion;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public List<String> getLinks() {
    return links;
  }

  public void setLinks(List<String> links) {
    this.links = links;
  }

  public Map<String, String> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, String> variables) {
    this.variables = variables;
  }

  public Map<String, Component> getComponents() {
    return components;
  }

  public void setComponents(Map<String, Component> components) {
    this.components = components;
  }

  public List<Component> getComponentList() {
    List<Component> componentList = Lists.newArrayList();
    for (Entry<String, Component> entry : components.entrySet()) {
      Component component = entry.getValue();
      component.setId(entry.getKey());
      componentList.add(component);
    }
    return componentList;
  }

  public Map<String, String> getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Map<String, String> configuration) {
    this.configuration = configuration;
  }

  public String getEnable() {
    return enable;
  }

  public void setEnable(String enable) {
    this.enable = enable;
  }

  public Map<String, String> getEncryptedvariables() {
    return encryptedvariables;
  }

  public void setEncryptedvariables(Map<String, String> encryptedvariables) {
    this.encryptedvariables = encryptedvariables;
  }

  public Map<String, Map<String, String>> getScale() {
    return scale;
  }

  public void setScale(Map<String, Map<String, String>> scale) {
    this.scale = scale;
  }

}
