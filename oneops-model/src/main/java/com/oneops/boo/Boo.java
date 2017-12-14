package com.oneops.boo;

import java.util.Map;

public class Boo {

  private String oneops_host;
  private String organization;
  private String api_key;
  private String email;
  private Map<String, Object> cloud;
  private String environment_name;
  private String description;
  private boolean gzip_enabled = true;
  private String ip_output;

  public String getOneops_host() {
    return oneops_host;
  }

  public void setOneops_host(String oneops_host) {
    this.oneops_host = oneops_host;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public String getApi_key() {
    return api_key;
  }

  public void setApi_key(String api_key) {
    this.api_key = api_key;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Map<String, Object> getCloud() {
    return cloud;
  }

  public void setCloud(Map<String, Object> cloud) {
    this.cloud = cloud;
  }

  public String getEnvironment_name() {
    return environment_name;
  }

  public void setEnvironment_name(String environment_name) {
    this.environment_name = environment_name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isGzip_enabled() {
    return gzip_enabled;
  }

  public void setGzip_enabled(boolean gzip_enabled) {
    this.gzip_enabled = gzip_enabled;
  }

  public String getIp_output() {
    return ip_output;
  }

  public void setIp_output(String ip_output) {
    this.ip_output = ip_output;
  }

}
