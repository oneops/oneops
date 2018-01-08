/*******************************************************************************
 *
 *   Copyright 2015 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
package com.oneops.notification;

import com.oneops.cms.simple.domain.CmsCISimple;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationMessage implements Serializable {

  private static final long serialVersionUID = 1L;

  private long cmsId;
  private String cloudName;
  private NotificationSeverity severity;
  private NotificationType type;
  private String source;
  private String subject;
  private String templateName;
  private String templateParams;
  private String text;
  private String nsPath;
  private Map<String, Object> payload = new HashMap<>();
  private long timestamp;
  private String environmentProfileName;
  private String adminStatus;
  private long manifestCiId;
  private List<CmsCISimple> cis;

  public static String buildSubjectPrefix(String nsPath) {
    String prefix = "";
    if (nsPath != null) {
      String[] nsPathTokens = nsPath.split("/");
      if (nsPathTokens.length > 3) {
        prefix = nsPathTokens[1] + "/" + nsPathTokens[2] + "/" + nsPathTokens[3] + " : ";
      }
    }
    return prefix;
  }

  public Map<String, Object> getPayload() {
    return payload;
  }
  
  public String getPayloadString(String name){
    return String.valueOf(payload.get(name));
  }

  public void putPayloadEntry(String name, Object value) {
    this.payload.put(name, value);
  }

  public void putPayloadEntries(Map<String, Object> payloadEntries) {
    this.payload.putAll(payloadEntries);
  }

  public String getNsPath() {
    return nsPath;
  }

  public void setNsPath(String nsPath) {
    this.nsPath = nsPath;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public long getCmsId() {
    return cmsId;
  }

  public void setCmsId(long cmsId) {
    this.cmsId = cmsId;
  }

  public NotificationSeverity getSeverity() {
    return severity;
  }

  public void setSeverity(NotificationSeverity severity) {
    this.severity = severity;
  }

  public NotificationType getType() {
    return type;
  }

  public void setType(NotificationType type) {
    this.type = type;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  public String getTemplateParams() {
    return templateParams;
  }

  public void setTemplateParams(String templateParams) {
    this.templateParams = templateParams;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getEnvironmentProfileName() {
    return environmentProfileName;
  }

  public void setEnvironmentProfileName(String envProfile) {
    this.environmentProfileName = envProfile;
  }

  public String getAdminStatus() {
    return adminStatus;
  }

  public void setAdminStatus(String adminStatus) {
    this.adminStatus = adminStatus;
  }

  public long getManifestCiId() {
    return manifestCiId;
  }

  public void setManifestCiId(long manifestCiId) {
    this.manifestCiId = manifestCiId;
  }

  public String getCloudName() {
    return cloudName;
  }

  public void setCloudName(String cloudName) {
    this.cloudName = cloudName;
  }

  public void appendText(String notes) {
    StringBuilder sb = new StringBuilder();
    sb.append(text).append(notes);
    this.text = sb.toString();
  }

  /**
   * Returns string representation od notification message
   *
   * @return notification message as string.
   */
  public String asString() {
    return "NotificationMessage{" +
        "cmsId=" + cmsId +
        ", cloudName='" + cloudName + '\'' +
        ", severity=" + severity +
        ", type=" + type +
        ", source='" + source + '\'' +
        ", subject='" + subject + '\'' +
        ", templateName='" + templateName + '\'' +
        ", templateParams='" + templateParams + '\'' +
        ", text='" + text + '\'' +
        ", nsPath='" + nsPath + '\'' +
        ", payload=" + payload +
        ", timestamp=" + timestamp +
        ", environmentProfileName='" + environmentProfileName + '\'' +
        ", adminStatus='" + adminStatus + '\'' +
        ", manifestCiId=" + manifestCiId +
        '}';
  }

  public List<CmsCISimple> getCis() {
    return cis;
  }

  public void setCis(List<CmsCISimple> cis) {
    this.cis = cis;
  }
}