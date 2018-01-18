package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class MtdHostResponse extends BaseResponse {

  @SerializedName("mtd_host")
  private MtdHost mtdHost = null;

  public MtdHostResponse links(Links links) {
    this.links = links;
    return this;
  }

  public MtdHostResponse mtdHost(MtdHost mtdHost) {
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
    MtdHostResponse mtdHostResponse = (MtdHostResponse) o;
    return Objects.equals(this.links, mtdHostResponse.links) &&
        Objects.equals(this.metadata, mtdHostResponse.metadata) &&
        Objects.equals(this.errors, mtdHostResponse.errors) &&
        Objects.equals(this.mtdHost, mtdHostResponse.mtdHost);
  }

  @Override
  public int hashCode() {
    return Objects.hash(links, metadata, errors, mtdHost);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MtdHostResponse {\n");
    
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
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

