package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MTDHost {

  @SerializedName("mtd_host_name")
  private String mtdHostName = null;

  @SerializedName("fallback_target")
  private String fallbackTarget = null;

  @SerializedName("mtd_health_checks")
  private List<MTDHostHealthCheck> mtdHealthChecks = new ArrayList<>();

  @SerializedName("mtd_targets")
  private List<MTDTarget> mtdTargets = new ArrayList<>();

  @SerializedName("is_dc_failover")
  private Boolean isDcFailover = null;

  @SerializedName("load_balancing_distribution")
  private Integer loadBalancingDistribution = null;

  @SerializedName("dc_fallback_targets")
  private List<DCFallbackTarget> dcFallbackTargets = new ArrayList<>();

  public MTDHost mtdHostName(String mtdHostName) {
    this.mtdHostName = mtdHostName;
    return this;
  }

  public String getMtdHostName() {
    return mtdHostName;
  }

  public void setMtdHostName(String mtdHostName) {
    this.mtdHostName = mtdHostName;
  }

  public MTDHost fallbackTarget(String fallbackTarget) {
    this.fallbackTarget = fallbackTarget;
    return this;
  }

  public String getFallbackTarget() {
    return fallbackTarget;
  }

  public void setFallbackTarget(String fallbackTarget) {
    this.fallbackTarget = fallbackTarget;
  }

  public MTDHost mtdHealthChecks(List<MTDHostHealthCheck> mtdHealthChecks) {
    this.mtdHealthChecks = mtdHealthChecks;
    return this;
  }

  public MTDHost addMtdHealthChecksItem(MTDHostHealthCheck mtdHealthChecksItem) {
    this.mtdHealthChecks.add(mtdHealthChecksItem);
    return this;
  }

  public List<MTDHostHealthCheck> getMtdHealthChecks() {
    return mtdHealthChecks;
  }

  public void setMtdHealthChecks(List<MTDHostHealthCheck> mtdHealthChecks) {
    this.mtdHealthChecks = mtdHealthChecks;
  }

  public MTDHost mtdTargets(List<MTDTarget> mtdTargets) {
    this.mtdTargets = mtdTargets;
    return this;
  }

  public MTDHost addMtdTargetsItem(MTDTarget mtdTargetsItem) {
    this.mtdTargets.add(mtdTargetsItem);
    return this;
  }

  public List<MTDTarget> getMtdTargets() {
    return mtdTargets;
  }

  public void setMtdTargets(List<MTDTarget> mtdTargets) {
    this.mtdTargets = mtdTargets;
  }

  public MTDHost isDcFailover(Boolean isDcFailover) {
    this.isDcFailover = isDcFailover;
    return this;
  }

  public Boolean getIsDcFailover() {
    return isDcFailover;
  }

  public void setIsDcFailover(Boolean isDcFailover) {
    this.isDcFailover = isDcFailover;
  }

  public MTDHost loadBalancingDistribution(Integer loadBalancingDistribution) {
    this.loadBalancingDistribution = loadBalancingDistribution;
    return this;
  }

  public Integer getLoadBalancingDistribution() {
    return loadBalancingDistribution;
  }

  public void setLoadBalancingDistribution(Integer loadBalancingDistribution) {
    this.loadBalancingDistribution = loadBalancingDistribution;
  }

  public MTDHost dcFallbackTargets(List<DCFallbackTarget> dcFallbackTargets) {
    this.dcFallbackTargets = dcFallbackTargets;
    return this;
  }

  public MTDHost addDcFallbackTargetsItem(DCFallbackTarget dcFallbackTargetsItem) {
    this.dcFallbackTargets.add(dcFallbackTargetsItem);
    return this;
  }

  public List<DCFallbackTarget> getDcFallbackTargets() {
    return dcFallbackTargets;
  }

  public void setDcFallbackTargets(List<DCFallbackTarget> dcFallbackTargets) {
    this.dcFallbackTargets = dcFallbackTargets;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MTDHost apiMTDHostRead = (MTDHost) o;
    return Objects.equals(this.mtdHostName, apiMTDHostRead.mtdHostName) &&
        Objects.equals(this.fallbackTarget, apiMTDHostRead.fallbackTarget) &&
        Objects.equals(this.mtdHealthChecks, apiMTDHostRead.mtdHealthChecks) &&
        Objects.equals(this.mtdTargets, apiMTDHostRead.mtdTargets) &&
        Objects.equals(this.isDcFailover, apiMTDHostRead.isDcFailover) &&
        Objects.equals(this.loadBalancingDistribution, apiMTDHostRead.loadBalancingDistribution) &&
        Objects.equals(this.dcFallbackTargets, apiMTDHostRead.dcFallbackTargets);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mtdHostName, fallbackTarget, mtdHealthChecks, mtdTargets, isDcFailover, loadBalancingDistribution, dcFallbackTargets);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDHost {\n");
    
    sb.append("    mtdHostName: ").append(toIndentedString(mtdHostName)).append("\n");
    sb.append("    fallbackTarget: ").append(toIndentedString(fallbackTarget)).append("\n");
    sb.append("    mtdHealthChecks: ").append(toIndentedString(mtdHealthChecks)).append("\n");
    sb.append("    mtdTargets: ").append(toIndentedString(mtdTargets)).append("\n");
    sb.append("    isDcFailover: ").append(toIndentedString(isDcFailover)).append("\n");
    sb.append("    loadBalancingDistribution: ").append(toIndentedString(loadBalancingDistribution)).append("\n");
    sb.append("    dcFallbackTargets: ").append(toIndentedString(dcFallbackTargets)).append("\n");
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

