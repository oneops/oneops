package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.Objects;

public class Version {

  @SerializedName("version_id")
  private Integer versionId = null;

  @SerializedName("based_on")
  private Integer basedOn = null;

  @SerializedName("version")
  private Integer version = null;

  @SerializedName("description")
  private String description = null;

  @SerializedName("data")
  private String data = null;

  @SerializedName("version_type")
  private String versionType = null;

  @SerializedName("user_id")
  private Integer userId = null;

  @SerializedName("created")
  private Date created = null;

  public Version versionId(Integer versionId) {
    this.versionId = versionId;
    return this;
  }

  public Integer getVersionId() {
    return versionId;
  }

  public void setVersionId(Integer versionId) {
    this.versionId = versionId;
  }

  public Version basedOn(Integer basedOn) {
    this.basedOn = basedOn;
    return this;
  }

  public Integer getBasedOn() {
    return basedOn;
  }

  public void setBasedOn(Integer basedOn) {
    this.basedOn = basedOn;
  }

  public Version version(Integer version) {
    this.version = version;
    return this;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public Version description(String description) {
    this.description = description;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Version data(String data) {
    this.data = data;
    return this;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public Version versionType(String versionType) {
    this.versionType = versionType;
    return this;
  }

  public String getVersionType() {
    return versionType;
  }

  public void setVersionType(String versionType) {
    this.versionType = versionType;
  }

  public Version userId(Integer userId) {
    this.userId = userId;
    return this;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public Version created(Date created) {
    this.created = created;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Version modelsVersion = (Version) o;
    return Objects.equals(this.versionId, modelsVersion.versionId) &&
        Objects.equals(this.basedOn, modelsVersion.basedOn) &&
        Objects.equals(this.version, modelsVersion.version) &&
        Objects.equals(this.description, modelsVersion.description) &&
        Objects.equals(this.data, modelsVersion.data) &&
        Objects.equals(this.versionType, modelsVersion.versionType) &&
        Objects.equals(this.userId, modelsVersion.userId) &&
        Objects.equals(this.created, modelsVersion.created);
  }

  @Override
  public int hashCode() {
    return Objects.hash(versionId, basedOn, version, description, data, versionType, userId, created);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModelsVersion {\n");
    
    sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
    sb.append("    basedOn: ").append(toIndentedString(basedOn)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    versionType: ").append(toIndentedString(versionType)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    created: ").append(toIndentedString(created)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
  
}

