package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class DeployMTDConfig {

  @SerializedName("mtd_base_id")
  private Integer mtdBaseId = null;

  @SerializedName("version_id")
  private Integer versionId = null;

  @SerializedName("deployment_type")
  private String deploymentType = null;

  @SerializedName("deployment_method")
  private String deploymentMethod = null;

  public DeployMTDConfig mtdBaseId(Integer mtdBaseId) {
    this.mtdBaseId = mtdBaseId;
    return this;
  }

  public Integer getMtdBaseId() {
    return mtdBaseId;
  }

  public void setMtdBaseId(Integer mtdBaseId) {
    this.mtdBaseId = mtdBaseId;
  }

  public DeployMTDConfig versionId(Integer versionId) {
    this.versionId = versionId;
    return this;
  }

  public Integer getVersionId() {
    return versionId;
  }

  public void setVersionId(Integer versionId) {
    this.versionId = versionId;
  }

  public DeployMTDConfig deploymentType(String deploymentType) {
    this.deploymentType = deploymentType;
    return this;
  }

  public String getDeploymentType() {
    return deploymentType;
  }

  public void setDeploymentType(String deploymentType) {
    this.deploymentType = deploymentType;
  }

  public DeployMTDConfig deploymentMethod(String deploymentMethod) {
    this.deploymentMethod = deploymentMethod;
    return this;
  }

  public String getDeploymentMethod() {
    return deploymentMethod;
  }

  public void setDeploymentMethod(String deploymentMethod) {
    this.deploymentMethod = deploymentMethod;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeployMTDConfig deployMTDConfig = (DeployMTDConfig) o;
    return Objects.equals(this.mtdBaseId, deployMTDConfig.mtdBaseId) &&
        Objects.equals(this.versionId, deployMTDConfig.versionId) &&
        Objects.equals(this.deploymentType, deployMTDConfig.deploymentType) &&
        Objects.equals(this.deploymentMethod, deployMTDConfig.deploymentMethod);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mtdBaseId, versionId, deploymentType, deploymentMethod);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeployMTDConfig {\n");
    
    sb.append("    mtdBaseId: ").append(toIndentedString(mtdBaseId)).append("\n");
    sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
    sb.append("    deploymentType: ").append(toIndentedString(deploymentType)).append("\n");
    sb.append("    deploymentMethod: ").append(toIndentedString(deploymentMethod)).append("\n");
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

