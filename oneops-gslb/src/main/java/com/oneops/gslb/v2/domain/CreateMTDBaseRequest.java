package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class CreateMTDBaseRequest {

  @SerializedName("mtd_base")
  private MTDBaseRequest mtdBase = null;

  public CreateMTDBaseRequest mtdBase(MTDBaseRequest mtdBase) {
    this.mtdBase = mtdBase;
    return this;
  }

  public MTDBaseRequest getMtdBase() {
    return mtdBase;
  }

  public void setMtdBase(MTDBaseRequest mtdBase) {
    this.mtdBase = mtdBase;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateMTDBaseRequest createMTDBaseRequest = (CreateMTDBaseRequest) o;
    return Objects.equals(this.mtdBase, createMTDBaseRequest.mtdBase);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mtdBase);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateMTDBaseRequest {\n");
    
    sb.append("    mtdBase: ").append(toIndentedString(mtdBase)).append("\n");
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

