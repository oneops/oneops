package com.oneops;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

public class Component {

  private String id;
  private String version;
  private String type;
  private String touch;
  private List<String> links;
  private Map<String, String> configuration;
  private Map<String, Attachment> attachments;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Map<String, String> getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Map<String, String> configuration) {
    this.configuration = configuration;
  }

  public List<String> getLinks() {
    return links;
  }

  public void setLinks(List<String> links) {
    this.links = links;
  }

  public Map<String, Attachment> getAttachments() {
    return attachments;
  }

  public void setAttachments(Map<String, Attachment> attachments) {
    this.attachments = attachments;
  }

  public List<Attachment> getAttachmentList() {
    List<Attachment> attachmentList = Lists.newArrayList();
    for (Entry<String, Attachment> entry : attachments.entrySet()) {
      Attachment attachment = entry.getValue();
      attachment.setId(entry.getKey());
      attachmentList.add(attachment);
    }
    return attachmentList;
  }

  public String getTouch() {
    return touch;
  }

  public void setTouch(String touch) {
    this.touch = touch;
  }

}
