package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class MTDTarget {

  @SerializedName("mtd_target_host")
  private String mtdTargetHost = null;

  @SerializedName("data_center_id")
  private Integer dataCenterId = null;

  @SerializedName("cloud_id")
  private Integer cloudId = null;

  @SerializedName("enabled")
  private Boolean enabled = null;

  @SerializedName("weight_percent")
  private Integer weightPercent = null;

  public MTDTarget mtdTargetHost(String mtdTargetHost) {
    this.mtdTargetHost = mtdTargetHost;
    return this;
  }

  public String getMtdTargetHost() {
    return mtdTargetHost;
  }

  public void setMtdTargetHost(String mtdTargetHost) {
    this.mtdTargetHost = mtdTargetHost;
  }

  public MTDTarget dataCenterId(Integer dataCenterId) {
    this.dataCenterId = dataCenterId;
    return this;
  }

  public Integer getDataCenterId() {
    return dataCenterId;
  }

  public void setDataCenterId(Integer dataCenterId) {
    this.dataCenterId = dataCenterId;
  }

  public MTDTarget cloudId(Integer cloudId) {
    this.cloudId = cloudId;
    return this;
  }

  public Integer getCloudId() {
    return cloudId;
  }

  public void setCloudId(Integer cloudId) {
    this.cloudId = cloudId;
  }

  public MTDTarget enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public MTDTarget weightPercent(Integer weightPercent) {
    this.weightPercent = weightPercent;
    return this;
  }

  public Integer getWeightPercent() {
    return weightPercent;
  }

  public void setWeightPercent(Integer weightPercent) {
    this.weightPercent = weightPercent;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MTDTarget modelsMTDTarget = (MTDTarget) o;
    return Objects.equals(this.mtdTargetHost, modelsMTDTarget.mtdTargetHost) &&
        Objects.equals(this.dataCenterId, modelsMTDTarget.dataCenterId) &&
        Objects.equals(this.cloudId, modelsMTDTarget.cloudId) &&
        Objects.equals(this.enabled, modelsMTDTarget.enabled) &&
        Objects.equals(this.weightPercent, modelsMTDTarget.weightPercent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mtdTargetHost, dataCenterId, cloudId, enabled, weightPercent);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDTarget {\n");
    
    sb.append("    mtdTargetHost: ").append(toIndentedString(mtdTargetHost)).append("\n");
    sb.append("    dataCenterId: ").append(toIndentedString(dataCenterId)).append("\n");
    sb.append("    cloudId: ").append(toIndentedString(cloudId)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    weightPercent: ").append(toIndentedString(weightPercent)).append("\n");
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

