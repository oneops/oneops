package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;


public class OriginVersion {

  @SerializedName("version_id")
  private Integer versionId = null;

  @SerializedName("version")
  private Integer version = null;

  @SerializedName("description")
  private String description = null;

  @SerializedName("staging_version_id")
  private Integer stagingVersionId = null;

  public OriginVersion versionId(Integer versionId) {
    this.versionId = versionId;
    return this;
  }

  public Integer getVersionId() {
    return versionId;
  }

  public void setVersionId(Integer versionId) {
    this.versionId = versionId;
  }

  public OriginVersion version(Integer version) {
    this.version = version;
    return this;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public OriginVersion description(String description) {
    this.description = description;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public OriginVersion stagingVersionId(Integer stagingVersionId) {
    this.stagingVersionId = stagingVersionId;
    return this;
  }

  public Integer getStagingVersionId() {
    return stagingVersionId;
  }

  public void setStagingVersionId(Integer stagingVersionId) {
    this.stagingVersionId = stagingVersionId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OriginVersion modelsOriginVersion = (OriginVersion) o;
    return Objects.equals(this.versionId, modelsOriginVersion.versionId) &&
        Objects.equals(this.version, modelsOriginVersion.version) &&
        Objects.equals(this.description, modelsOriginVersion.description) &&
        Objects.equals(this.stagingVersionId, modelsOriginVersion.stagingVersionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(versionId, version, description, stagingVersionId);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OriginVersion {\n");
    
    sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    stagingVersionId: ").append(toIndentedString(stagingVersionId)).append("\n");
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

