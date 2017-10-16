package com.oneops.cms.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkWorkOrder implements Serializable {

  public List<WoComponent> instances;
  public BaseInstance cloud;
  public BaseInstance box;
  public Map<String, List<BaseInstance>> payLoad;
  public Map<String, Map<String, BaseInstance>> services;
  public Map<String, String> searchTags = new HashMap<>();
  public Map<String, String> config;

  public List<WoComponent> getInstances() {
    return instances;
  }

  public void setInstances(List<WoComponent> instances) {
    this.instances = instances;
  }

  public BaseInstance getCloud() {
    return cloud;
  }

  public void setCloud(BaseInstance cloud) {
    this.cloud = cloud;
  }

  public BaseInstance getBox() {
    return box;
  }

  public void setBox(BaseInstance box) {
    this.box = box;
  }

  public Map<String, List<BaseInstance>> getPayLoad() {
    return payLoad;
  }

  public void setPayLoad(Map<String, List<BaseInstance>> payLoad) {
    this.payLoad = payLoad;
  }

  public Map<String, Map<String, BaseInstance>> getServices() {
    return services;
  }

  public void setServices(Map<String, Map<String, BaseInstance>> services) {
    this.services = services;
  }

  public Map<String, String> getSearchTags() {
    return searchTags;
  }

  public void setSearchTags(Map<String, String> searchTags) {
    this.searchTags = searchTags;
  }

  public Map<String, String> getConfig() {
    return config;
  }

  public void setConfig(Map<String, String> config) {
    this.config = config;
  }

  public void putPayLoadEntry(String payloadEntry, List<BaseInstance> instances) {
    if (payLoad == null) {
      payLoad = new HashMap<>();
    }
    payLoad.put(payloadEntry, instances);
  }

}
