package com.oneops;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Assembly {

  private String name;
  private String email;
  private String description;
  private Map<String, String> tags;
  private Map<String, String> variables;
  private Map<String, String> encryptedvariables;
  private Map<String, Platform> platforms;
  private Map<String, Environment> environments;
  private String autogen;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
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

  public Map<String, String> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, String> variables) {
    this.variables = variables;
  }

  public Map<String, Platform> getPlatforms() {
    return platforms;
  }

  public void setPlatforms(Map<String, Platform> platforms) {
    this.platforms = platforms;
  }

  public List<Platform> getPlatformList() {
    List<Platform> platformList = Lists.newArrayList();
    for (Entry<String, Platform> entry : platforms.entrySet()) {
      Platform platform = entry.getValue();
      platform.setName(entry.getKey());
      platformList.add(platform);
    }
    return platformList;
  }

  public Map<String, Environment> getEnvironments() {
    return environments;
  }

  public void setEnvironments(Map<String, Environment> environments) {
    this.environments = environments;
  }

  public List<Environment> getEnvironmentList() {
    List<Environment> environmentList = Lists.newArrayList();
    for (Entry<String, Environment> entry : environments.entrySet()) {
      Environment environment = entry.getValue();
      environment.setName(entry.getKey());
      environmentList.add(environment);
    }
    return environmentList;

  }

  public Map<String, String> getEncryptedvariables() {
    return encryptedvariables;
  }

  public void setEncryptedvariables(Map<String, String> encryptedvariables) {
    this.encryptedvariables = encryptedvariables;
  }

  public String getAutogen() {
    return autogen;
  }

  public void setAutogen(String autogen) {
    this.autogen = autogen;
  }

}
