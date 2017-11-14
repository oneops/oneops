package com.oneops;

public class OneOps {

  private String host;
  private String apikey;
  private String organization;

  private Assembly assembly;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getApikey() {
    return apikey;
  }

  public void setApikey(String apikey) {
    this.apikey = apikey;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public Assembly getAssembly() {
    return assembly;
  }

  public void setAssembly(Assembly assembly) {
    this.assembly = assembly;
  }
}
