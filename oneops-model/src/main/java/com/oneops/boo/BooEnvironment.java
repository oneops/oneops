package com.oneops.boo;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BooEnvironment {

  private String name;
  private String global_dns;
  private String availability;
  private String subdomain;
  private String profile;
  private Map<String, BooCloud> clouds;
  private Map<String, BooPlatform> platforms;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getGlobal_dns() {
    return global_dns;
  }

  public void setGlobal_dns(String global_dns) {
    this.global_dns = global_dns;
  }

  public String getAvailability() {
    return availability;
  }

  public void setAvailability(String availability) {
    this.availability = availability;
  }

  public String getSubdomain() {
    return subdomain;
  }

  public void setSubdomain(String subdomain) {
    this.subdomain = subdomain;
  }

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public Map<String, BooCloud> getClouds() {
    return clouds;
  }

  public void setClouds(Map<String, BooCloud> clouds) {
    this.clouds = clouds;
  }

  public List<BooCloud> getCloudList() {
    List<BooCloud> cloudList = Lists.newArrayList();
    for (Entry<String, BooCloud> entry : clouds.entrySet()) {
      BooCloud cloud = entry.getValue();
      cloud.setName(entry.getKey());
      cloudList.add(cloud);
    }
    return cloudList;
  }

  public List<BooPlatform> getPlatformList() {
    List<BooPlatform> platformList = Lists.newArrayList();
    for (Entry<String, BooPlatform> entry : platforms.entrySet()) {
      BooPlatform platform = entry.getValue();
      platform.setName(entry.getKey());
      platformList.add(platform);
    }
    return platformList;
  }

  public Map<String, BooPlatform> getPlatforms() {
    return platforms;
  }

  public void setPlatforms(Map<String, BooPlatform> platforms) {
    this.platforms = platforms;
  }
}
