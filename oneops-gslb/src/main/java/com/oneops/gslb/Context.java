package com.oneops.gslb;

import java.util.List;
import java.util.Map;

public class Context {

  private String platform;
  private String environment;
  private String assembly;
  private String org;
  private String cloud;
  private String subdomain;
  private String baseGslbDomain;
  private String mtdBaseHost;
  private TorbitClient torbitClient;
  private TorbitApi torbitApi;
  private Config config;
  private String logKey;
  private Map<String, String> dnsAttrs;
  private List<String> primaryTargets;
  private boolean isPlatformDisabled;

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public String getAssembly() {
    return assembly;
  }

  public void setAssembly(String assembly) {
    this.assembly = assembly;
  }

  public String getOrg() {
    return org;
  }

  public void setOrg(String org) {
    this.org = org;
  }

  public String getCloud() {
    return cloud;
  }

  public void setCloud(String cloud) {
    this.cloud = cloud;
  }

  public String getSubdomain() {
    return subdomain;
  }

  public void setSubdomain(String subdomain) {
    this.subdomain = subdomain;
  }

  public String getBaseGslbDomain() {
    return baseGslbDomain;
  }

  public void setBaseGslbDomain(String baseGslbDomain) {
    this.baseGslbDomain = baseGslbDomain;
  }

  public String getMtdBaseHost() {
    return mtdBaseHost;
  }

  public void setMtdBaseHost(String mtdBaseHost) {
    this.mtdBaseHost = mtdBaseHost;
  }

  public TorbitClient getTorbitClient() {
    return torbitClient;
  }

  public void setTorbitClient(TorbitClient torbitClient) {
    this.torbitClient = torbitClient;
  }

  public TorbitApi getTorbitApi() {
    return torbitApi;
  }

  public void setTorbitApi(TorbitApi torbitApi) {
    this.torbitApi = torbitApi;
  }

  public Config getConfig() {
    return config;
  }

  public void setConfig(Config config) {
    this.config = config;
  }

  public String getLogKey() {
    return logKey;
  }

  public void setLogKey(String logKey) {
    this.logKey = logKey;
  }

  public Map<String, String> getDnsAttrs() {
    return dnsAttrs;
  }

  public void setDnsAttrs(Map<String, String> dnsAttrs) {
    this.dnsAttrs = dnsAttrs;
  }

  public List<String> getPrimaryTargets() {
    return primaryTargets;
  }

  public void setPrimaryTargets(List<String> primaryTargets) {
    this.primaryTargets = primaryTargets;
  }

  public boolean isPlatformDisabled() {
    return isPlatformDisabled;
  }

  public void setPlatformDisabled(boolean platformDisabled) {
    isPlatformDisabled = platformDisabled;
  }
}
