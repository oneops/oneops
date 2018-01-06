package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MTDHosts {

  @SerializedName("mtd_host")
  private List<MTDHost> mtdHost = new ArrayList<MTDHost>();

  public MTDHosts mtdHost(List<MTDHost> mtdHost) {
    this.mtdHost = mtdHost;
    return this;
  }

  public MTDHosts addMtdHostItem(MTDHost mtdHostItem) {
    this.mtdHost.add(mtdHostItem);
    return this;
  }

  public List<MTDHost> getMtdHost() {
    return mtdHost;
  }

  public void setMtdHost(List<MTDHost> mtdHost) {
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
    MTDHosts apiMTDHostsRead = (MTDHosts) o;
    return Objects.equals(this.mtdHost, apiMTDHostsRead.mtdHost);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mtdHost);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDHosts {\n");
    
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

