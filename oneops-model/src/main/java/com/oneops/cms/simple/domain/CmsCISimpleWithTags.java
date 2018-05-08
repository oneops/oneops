package com.oneops.cms.simple.domain;

import java.util.HashMap;
import java.util.Map;

public class CmsCISimpleWithTags extends CmsCISimple {

  private static final long serialVersionUID = 1L;

  private String org;
  private String assembly;
  private String env;
  private String platform;
  private Map<String, String> tags = new HashMap<>();

  public String getOrg() {
    return org;
  }

  public String getAssembly() {
    return assembly;
  }

  public void setAssembly(String assembly) {
    this.assembly = assembly;
  }

  public void setOrg(String org) {
    this.org = org;
  }

  public String getEnv() {
    return env;
  }

  public void setEnv(String env) {
    this.env = env;
  }

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public void setTags(Map<String, String> tags) {
    this.tags = tags;
  }
}
