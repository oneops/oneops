package com.oneops.boo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BooPlatform {

  private int deploy_order;
  private String name;
  private String pack_version;
  private String pack;
  private String source;
  private Map<String, String> variables;
  private Map<String, String> encrypted_variables;
  private Map<String, Map<String, Object>> components;
  private List<String> links;
  private Map<String, String> auto_healing;
  private Map<String, Map<String, String>> scale;

  public int getDeploy_order() {
    return deploy_order;
  }

  public void setDeploy_order(int deploy_order) {
    this.deploy_order = deploy_order;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    String[] split = pack.split("[\\/\\s]");
    this.source = split[0];
    this.pack = split[1];
  }

  public String getPack() {
    return pack;
  }

  public void setPack(String pack) {
    String[] split = pack.split("[\\/\\s]");
    this.source = split[0];
    this.pack = split[1];
  }

  public String getPack_version() {
    return pack_version;
  }

  public void setPack_version(String pack_version) {
    this.pack_version = pack_version;
  }

  public Map<String, String> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, String> variables) {
    this.variables = variables;
  }

  public Map<String, String> getEncrypted_variables() {
    return encrypted_variables;
  }

  public void setEncrypted_variables(Map<String, String> encrypted_variables) {
    this.encrypted_variables = encrypted_variables;
  }

  public Map<String, Map<String, Object>> getComponents() {
    return components;
  }

  public void setComponents(Map<String, Map<String, Object>> components) {
    this.components = components;
  }

  public List<String> getLinks() {
    return links;
  }

  public void setLinks(List<String> links) {
    this.links = links;
  }

  public Map<String, String> getAuto_healing() {
    return auto_healing;
  }

  public void setAuto_healing(Map<String, String> auto_healing) {
    this.auto_healing = auto_healing;
  }

  public Map<String, Map<String, String>> getScale() {
    return scale;
  }

  public void setScale(Map<String, Map<String, String>> scale) {
    this.scale = scale;
  }

  public List<BooScale> getScaleList() {
    List<BooScale> booScaleList = Lists.newArrayList();
    for (Entry<String, Map<String, String>> entry0 : scale.entrySet()) {
      String componentName = entry0.getKey();
      Map<String, String> entry = entry0.getValue();
      BooScale booScale = new BooScale();
      booScale.setPlatformName(this.name);
      booScale.setComponentName(componentName);
      booScale.setCurrent(entry.get("current"));
      booScale.setMin(entry.get("min"));
      booScale.setMax(entry.get("max"));
      booScaleList.add(booScale);
    }
    return booScaleList;
  }

  public Map<String, BooAttachment> getAttachments() {
    Map<String, BooAttachment> attachmentMap = Maps.newHashMap();
    if (components != null) {
      for (Entry<String, Map<String, Object>> entry : components.entrySet()) {
        String componentName = entry.getKey();

        if (entry.getValue() != null && entry.getValue().containsKey("attachments")) {
          @SuppressWarnings("unchecked")
          Map<String, Map<String, String>> map = (Map<String, Map<String, String>>) entry.getValue()
              .get("attachments");
          for (Entry<String, Map<String, String>> attachmentEntry : map.entrySet()) {
            String attachmentName = attachmentEntry.getKey();
            BooAttachment attachment = new BooAttachment();
            attachment.setName(attachmentName);
            attachment.setConfiguration(attachmentEntry.getValue());
            attachmentMap.put(componentName, attachment);
          }
        }
      }
    }
    return attachmentMap;
  }

}
