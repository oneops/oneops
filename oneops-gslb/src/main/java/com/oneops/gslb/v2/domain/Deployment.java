package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.Objects;

public class Deployment {

  @SerializedName("deployment_id")
  private Integer deploymentId = null;

  @SerializedName("deployment_type")
  private String deploymentType = null;

  @SerializedName("deployment_method")
  private String deploymentMethod = null;

  @SerializedName("is_live")
  private Boolean isLive = null;

  @SerializedName("is_staging")
  private Boolean isStaging = null;

  @SerializedName("version")
  private DeployVersion version = null;

  @SerializedName("origin_id")
  private Integer originId = null;

  @SerializedName("lua_tenant_id")
  private Integer luaTenantId = null;

  @SerializedName("mtd_base_id")
  private Integer mtdBaseId = null;

  @SerializedName("user")
  private User user = null;

  @SerializedName("created")
  private Long created = null;

  @SerializedName("created_time")
  private Date createdTime = null;

  @SerializedName("created_unix_ms")
  private Long createdUnixMs = null;

  public Deployment deploymentId(Integer deploymentId) {
    this.deploymentId = deploymentId;
    return this;
  }

  public Integer getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(Integer deploymentId) {
    this.deploymentId = deploymentId;
  }

  public Deployment deploymentType(String deploymentType) {
    this.deploymentType = deploymentType;
    return this;
  }

  public String getDeploymentType() {
    return deploymentType;
  }

  public void setDeploymentType(String deploymentType) {
    this.deploymentType = deploymentType;
  }

  public Deployment deploymentMethod(String deploymentMethod) {
    this.deploymentMethod = deploymentMethod;
    return this;
  }

  public String getDeploymentMethod() {
    return deploymentMethod;
  }

  public void setDeploymentMethod(String deploymentMethod) {
    this.deploymentMethod = deploymentMethod;
  }

  public Deployment isLive(Boolean isLive) {
    this.isLive = isLive;
    return this;
  }

  public Boolean getIsLive() {
    return isLive;
  }

  public void setIsLive(Boolean isLive) {
    this.isLive = isLive;
  }

  public Deployment isStaging(Boolean isStaging) {
    this.isStaging = isStaging;
    return this;
  }

  public Boolean getIsStaging() {
    return isStaging;
  }

  public void setIsStaging(Boolean isStaging) {
    this.isStaging = isStaging;
  }

  public Deployment version(DeployVersion version) {
    this.version = version;
    return this;
  }

  public DeployVersion getVersion() {
    return version;
  }

  public void setVersion(DeployVersion version) {
    this.version = version;
  }

  public Deployment originId(Integer originId) {
    this.originId = originId;
    return this;
  }

  public Integer getOriginId() {
    return originId;
  }

  public void setOriginId(Integer originId) {
    this.originId = originId;
  }

  public Deployment luaTenantId(Integer luaTenantId) {
    this.luaTenantId = luaTenantId;
    return this;
  }

  public Integer getLuaTenantId() {
    return luaTenantId;
  }

  public void setLuaTenantId(Integer luaTenantId) {
    this.luaTenantId = luaTenantId;
  }

  public Deployment mtdBaseId(Integer mtdBaseId) {
    this.mtdBaseId = mtdBaseId;
    return this;
  }

  public Integer getMtdBaseId() {
    return mtdBaseId;
  }

  public void setMtdBaseId(Integer mtdBaseId) {
    this.mtdBaseId = mtdBaseId;
  }

  public Deployment user(User user) {
    this.user = user;
    return this;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Deployment created(Long created) {
    this.created = created;
    return this;
  }

  public Long getCreated() {
    return created;
  }

  public void setCreated(Long created) {
    this.created = created;
  }

  public Deployment createdTime(Date createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  public Date getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
  }

  public Deployment createdUnixMs(Long createdUnixMs) {
    this.createdUnixMs = createdUnixMs;
    return this;
  }

  public Long getCreatedUnixMs() {
    return createdUnixMs;
  }

  public void setCreatedUnixMs(Long createdUnixMs) {
    this.createdUnixMs = createdUnixMs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Deployment apiReadDeployment = (Deployment) o;
    return Objects.equals(this.deploymentId, apiReadDeployment.deploymentId) &&
        Objects.equals(this.deploymentType, apiReadDeployment.deploymentType) &&
        Objects.equals(this.deploymentMethod, apiReadDeployment.deploymentMethod) &&
        Objects.equals(this.isLive, apiReadDeployment.isLive) &&
        Objects.equals(this.isStaging, apiReadDeployment.isStaging) &&
        Objects.equals(this.version, apiReadDeployment.version) &&
        Objects.equals(this.originId, apiReadDeployment.originId) &&
        Objects.equals(this.luaTenantId, apiReadDeployment.luaTenantId) &&
        Objects.equals(this.mtdBaseId, apiReadDeployment.mtdBaseId) &&
        Objects.equals(this.user, apiReadDeployment.user) &&
        Objects.equals(this.created, apiReadDeployment.created) &&
        Objects.equals(this.createdTime, apiReadDeployment.createdTime) &&
        Objects.equals(this.createdUnixMs, apiReadDeployment.createdUnixMs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deploymentId, deploymentType, deploymentMethod, isLive, isStaging, version, originId, luaTenantId, mtdBaseId, user, created, createdTime, createdUnixMs);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Deployment {\n");
    
    sb.append("    deploymentId: ").append(toIndentedString(deploymentId)).append("\n");
    sb.append("    deploymentType: ").append(toIndentedString(deploymentType)).append("\n");
    sb.append("    deploymentMethod: ").append(toIndentedString(deploymentMethod)).append("\n");
    sb.append("    isLive: ").append(toIndentedString(isLive)).append("\n");
    sb.append("    isStaging: ").append(toIndentedString(isStaging)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    originId: ").append(toIndentedString(originId)).append("\n");
    sb.append("    luaTenantId: ").append(toIndentedString(luaTenantId)).append("\n");
    sb.append("    mtdBaseId: ").append(toIndentedString(mtdBaseId)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("    created: ").append(toIndentedString(created)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    createdUnixMs: ").append(toIndentedString(createdUnixMs)).append("\n");
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

