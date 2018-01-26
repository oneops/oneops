
package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class DeployVersion {

  @SerializedName("version_id")
  private Integer versionId = null;

  @SerializedName("version")
  private Integer version = null;

  @SerializedName("description")
  private String description = null;

  public DeployVersion versionId(Integer versionId) {
    this.versionId = versionId;
    return this;
  }

  public Integer getVersionId() {
    return versionId;
  }

  public void setVersionId(Integer versionId) {
    this.versionId = versionId;
  }

  public DeployVersion version(Integer version) {
    this.version = version;
    return this;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public DeployVersion description(String description) {
    this.description = description;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeployVersion apiDeployVersion = (DeployVersion) o;
    return Objects.equals(this.versionId, apiDeployVersion.versionId) &&
        Objects.equals(this.version, apiDeployVersion.version) &&
        Objects.equals(this.description, apiDeployVersion.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(versionId, version, description);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeployVersion {\n");
    
    sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

