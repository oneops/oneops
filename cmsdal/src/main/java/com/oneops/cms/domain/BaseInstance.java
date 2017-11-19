package com.oneops.cms.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BaseInstance implements Serializable {

  private long ciId;
  private String ciName;
  private String impl;
  private String ciClassName;

  private Map<String,String> ciAttributes = new HashMap<>();
  private Map<String,String> ciBaseAttributes = new HashMap<>();
  private Map<String,Map<String,String>> ciAttrProps = new HashMap<>();

  public long getCiId() {
    return ciId;
  }

  public void setCiId(long ciId) {
    this.ciId = ciId;
  }

  public String getCiName() {
    return ciName;
  }

  public void setCiName(String ciName) {
    this.ciName = ciName;
  }

  public String getImpl() {
    return impl;
  }

  public void setImpl(String impl) {
    this.impl = impl;
  }

  public String getCiClassName() {
    return ciClassName;
  }

  public void setCiClassName(String ciClassName) {
    this.ciClassName = ciClassName;
  }

  public Map<String, String> getCiAttributes() {
    return ciAttributes;
  }

  public void setCiAttributes(Map<String, String> ciAttributes) {
    this.ciAttributes = ciAttributes;
  }

  public Map<String, String> getCiBaseAttributes() {
    return ciBaseAttributes;
  }

  public void setCiBaseAttributes(Map<String, String> ciBaseAttributes) {
    this.ciBaseAttributes = ciBaseAttributes;
  }

  public Map<String, Map<String, String>> getCiAttrProps() {
    return ciAttrProps;
  }

  public void setCiAttrProps(Map<String, Map<String, String>> ciAttrProps) {
    this.ciAttrProps = ciAttrProps;
  }
}
