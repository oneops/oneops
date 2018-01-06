package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class MTDBaseVersion {

  @SerializedName("version_id")
  private SqlNullInt64 versionId = null;

  @SerializedName("version")
  private SqlNullInt64 version = null;

  @SerializedName("description")
  private SqlNullString description = null;

  public MTDBaseVersion versionId(SqlNullInt64 versionId) {
    this.versionId = versionId;
    return this;
  }

  public SqlNullInt64 getVersionId() {
    return versionId;
  }

  public void setVersionId(SqlNullInt64 versionId) {
    this.versionId = versionId;
  }

  public MTDBaseVersion version(SqlNullInt64 version) {
    this.version = version;
    return this;
  }

  public SqlNullInt64 getVersion() {
    return version;
  }

  public void setVersion(SqlNullInt64 version) {
    this.version = version;
  }

  public MTDBaseVersion description(SqlNullString description) {
    this.description = description;
    return this;
  }

  public SqlNullString getDescription() {
    return description;
  }

  public void setDescription(SqlNullString description) {
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
    MTDBaseVersion modelsMTDVersion = (MTDBaseVersion) o;
    return Objects.equals(this.versionId, modelsMTDVersion.versionId) &&
        Objects.equals(this.version, modelsMTDVersion.version) &&
        Objects.equals(this.description, modelsMTDVersion.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(versionId, version, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDVersion {\n");
    
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

