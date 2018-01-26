package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class DCFallbackTarget {

  @SerializedName("fallback_target")
  private String fallbackTarget = null;

  @SerializedName("data_center_id")
  private Integer dataCenterId = null;

  public DCFallbackTarget fallbackTarget(String fallbackTarget) {
    this.fallbackTarget = fallbackTarget;
    return this;
  }

  public String getFallbackTarget() {
    return fallbackTarget;
  }

  public void setFallbackTarget(String fallbackTarget) {
    this.fallbackTarget = fallbackTarget;
  }

  public DCFallbackTarget dataCenterId(Integer dataCenterId) {
    this.dataCenterId = dataCenterId;
    return this;
  }

  public Integer getDataCenterId() {
    return dataCenterId;
  }

  public void setDataCenterId(Integer dataCenterId) {
    this.dataCenterId = dataCenterId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DCFallbackTarget modelsMTDDCFallbackTarget = (DCFallbackTarget) o;
    return Objects.equals(this.fallbackTarget, modelsMTDDCFallbackTarget.fallbackTarget) &&
        Objects.equals(this.dataCenterId, modelsMTDDCFallbackTarget.dataCenterId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fallbackTarget, dataCenterId);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDDCFallbackTarget {\n");
    
    sb.append("    fallbackTarget: ").append(toIndentedString(fallbackTarget)).append("\n");
    sb.append("    dataCenterId: ").append(toIndentedString(dataCenterId)).append("\n");
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

