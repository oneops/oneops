package com.oneops;

import java.util.Map;

public class Assertion {

  private String id;
  private String name;
  private String component;
  private Map<String, String> others;
  private String message;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getComponent() {
    return component;
  }

  public void setComponent(String component) {
    this.component = component;
  }

  public Map<String, String> getOthers() {
    return others;
  }

  public void setOthers(Map<String, String> others) {
    this.others = others;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
