package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class MtdbHostsVersionRequest {

  @SerializedName("mtd_hosts")
  private MtdHosts mtdHosts = null;

  @SerializedName("mtd_base_id")
  private Integer mtdBaseId = null;

  @SerializedName("version")
  private VersionRequest version = null;

  public MtdbHostsVersionRequest mtdHosts(MtdHosts mtdHosts) {
    this.mtdHosts = mtdHosts;
    return this;
  }

  public MtdHosts getMtdHosts() {
    return mtdHosts;
  }

  public void setMtdHosts(MtdHosts mtdHosts) {
    this.mtdHosts = mtdHosts;
  }

  public MtdbHostsVersionRequest mtdBaseId(Integer mtdBaseId) {
    this.mtdBaseId = mtdBaseId;
    return this;
  }

  public Integer getMtdBaseId() {
    return mtdBaseId;
  }

  public void setMtdBaseId(Integer mtdBaseId) {
    this.mtdBaseId = mtdBaseId;
  }

  public MtdbHostsVersionRequest version(VersionRequest version) {
    this.version = version;
    return this;
  }

  public VersionRequest getVersion() {
    return version;
  }

  public void setVersion(VersionRequest version) {
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
    MtdbHostsVersionRequest mtdHostsVersionRequest = (MtdbHostsVersionRequest) o;
    return Objects.equals(this.mtdHosts, mtdHostsVersionRequest.mtdHosts) &&
        Objects.equals(this.mtdBaseId, mtdHostsVersionRequest.mtdBaseId) &&
        Objects.equals(this.version, mtdHostsVersionRequest.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mtdHosts, mtdBaseId, version);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDBHostsVersionRequest {\n");
    
    sb.append("    mtdHosts: ").append(toIndentedString(mtdHosts)).append("\n");
    sb.append("    mtdBaseId: ").append(toIndentedString(mtdBaseId)).append("\n");
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

