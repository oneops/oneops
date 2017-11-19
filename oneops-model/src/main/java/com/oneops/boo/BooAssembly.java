package com.oneops.boo;

import java.util.Map;

public class BooAssembly {

  private String name;
  private Boolean auto_gen;
  private String description;
  private Map<String, String> tags;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getAuto_gen() {
    return auto_gen;
  }

  public void setAuto_gen(Boolean auto_gen) {
    this.auto_gen = auto_gen;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public void setTags(Map<String, String> tags) {
    this.tags = tags;
  }
}
