package com.oneops;

import java.util.Map;

public class Attachment {

  private String id;
  private Map<String, String> configuration;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Map<String, String> getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Map<String, String> configuration) {
    this.configuration = configuration;
  }

}
