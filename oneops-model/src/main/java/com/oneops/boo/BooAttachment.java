package com.oneops.boo;

import java.util.Map;

public class BooAttachment {
  private String name;
  private Map<String, String> configuration;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, String> getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Map<String, String> configuration) {
    this.configuration = configuration;
  }
}
