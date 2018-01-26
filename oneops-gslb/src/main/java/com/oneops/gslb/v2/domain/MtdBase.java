package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class MtdBase {

  @SerializedName("mtd_base_id")
  private Integer mtdBaseId = null;

  @SerializedName("mtd_base_name")
  private String mtdBaseName = null;

  @SerializedName("version")
  private MtdBaseVersion version = null;

  public MtdBase mtdBaseId(Integer mtdBaseId) {
    this.mtdBaseId = mtdBaseId;
    return this;
  }

  public Integer getMtdBaseId() {
    return mtdBaseId;
  }

  public void setMtdBaseId(Integer mtdBaseId) {
    this.mtdBaseId = mtdBaseId;
  }

  public MtdBase mtdBaseName(String mtdBaseName) {
    this.mtdBaseName = mtdBaseName;
    return this;
  }

  public String getMtdBaseName() {
    return mtdBaseName;
  }

  public void setMtdBaseName(String mtdBaseName) {
    this.mtdBaseName = mtdBaseName;
  }

  public MtdBase version(MtdBaseVersion version) {
    this.version = version;
    return this;
  }

  public MtdBaseVersion getVersion() {
    return version;
  }

  public void setVersion(MtdBaseVersion version) {
    this.version = version;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MtdBase modelsMTDBase = (MtdBase) o;
    return Objects.equals(this.mtdBaseId, modelsMTDBase.mtdBaseId) &&
        Objects.equals(this.mtdBaseName, modelsMTDBase.mtdBaseName) &&
        Objects.equals(this.version, modelsMTDBase.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mtdBaseId, mtdBaseName, version);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDBase {\n");
    
    sb.append("    mtdBaseId: ").append(toIndentedString(mtdBaseId)).append("\n");
    sb.append("    mtdBaseName: ").append(toIndentedString(mtdBaseName)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
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

