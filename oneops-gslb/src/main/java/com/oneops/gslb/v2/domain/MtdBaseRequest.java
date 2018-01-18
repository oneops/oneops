package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;


public class MtdBaseRequest {

  @SerializedName("mtd_base_name")
  private String mtdBaseName = null;

  @SerializedName("type")
  private String type = null;

  public MtdBaseRequest mtdBaseName(String mtdBaseName) {
    this.mtdBaseName = mtdBaseName;
    return this;
  }

  public String getMtdBaseName() {
    return mtdBaseName;
  }

  public void setMtdBaseName(String mtdBaseName) {
    this.mtdBaseName = mtdBaseName;
  }

  public MtdBaseRequest type(String type) {
    this.type = type;
    return this;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MtdBaseRequest apiNewMTDBase = (MtdBaseRequest) o;
    return Objects.equals(this.mtdBaseName, apiNewMTDBase.mtdBaseName) &&
        Objects.equals(this.type, apiNewMTDBase.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mtdBaseName, type);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDBaseRequest {\n");
    
    sb.append("    mtdBaseName: ").append(toIndentedString(mtdBaseName)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

