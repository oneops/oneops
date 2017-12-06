package com.oneops;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TestCase {

  private String host;
  private String apikey;
  private String organization;
  private Map<String, Template> templates;
  private String description;
  private Map<String, Cloud> clouds;
  private String randomcloud;
  private List<String> tags;
  private String name;

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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getRandomcloud() {
    return randomcloud;
  }

  public void setRandomcloud(String randomcloud) {
    this.randomcloud = randomcloud;
  }

  public Map<String, Template> getTemplates() {
    return templates;
  }

  public void setTemplates(Map<String, Template> templates) {
    this.templates = templates;
  }

  public List<Template> getTemplateList() {
    List<Template> templateList = Lists.newArrayList();
    for (Entry<String, Template> entry : templates.entrySet()) {
      Template template = entry.getValue();
      template.setId(entry.getKey());
      templateList.add(template);
    }
    return templateList;
  }

  public Map<String, Cloud> getClouds() {
    return clouds;
  }

  public void setClouds(Map<String, Cloud> clouds) {
    this.clouds = clouds;
  }

  public List<Cloud> getCloudList() {
    List<Cloud> cloudList = Lists.newArrayList();
    for (Entry<String, Cloud> entry : clouds.entrySet()) {
      Cloud cloud = entry.getValue();
      cloud.setId(entry.getKey());
      cloudList.add(cloud);
    }
    return cloudList;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
