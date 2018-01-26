package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class MtdBaseHostRequest {

  @SerializedName("mtd_host")
  private MtdHost mtdHost = null;

  public MtdBaseHostRequest mtdHost(MtdHost mtdHost) {
    this.mtdHost = mtdHost;
    return this;
  }

  public MtdHost getMtdHost() {
    return mtdHost;
  }

  public void setMtdHost(MtdHost mtdHost) {
    this.mtdHost = mtdHost;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MtdBaseHostRequest apiWriteNewMTDBaseHostRequest = (MtdBaseHostRequest) o;
    return Objects.equals(this.mtdHost, apiWriteNewMTDBaseHostRequest.mtdHost);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mtdHost);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDBaseHostRequest {\n");
    
    sb.append("    mtdHost: ").append(toIndentedString(mtdHost)).append("\n");
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

