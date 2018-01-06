package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.Objects;

public class MTDDeployment {

  @SerializedName("deployment_id")
  private Integer deploymentId = null;

  @SerializedName("deployment_type")
  private String deploymentType = null;

  @SerializedName("deployment_method")
  private String deploymentMethod = null;

  @SerializedName("origin_id")
  private SqlNullInt64 originId = null;

  @SerializedName("lua_tenant_id")
  private SqlNullInt64 luaTenantId = null;

  @SerializedName("lua_tenant_db_tenant_id")
  private SqlNullInt64 luaTenantDbTenantId = null;

  @SerializedName("mtd_base_id")
  private SqlNullInt64 mtdBaseId = null;

  @SerializedName("created")
  private Date created = null;

  @SerializedName("created_unix_ms")
  private Long createdUnixMs = null;

  @SerializedName("User")
  private ShortUser user = null;

  @SerializedName("Version")
  private Version version = null;

  public MTDDeployment deploymentId(Integer deploymentId) {
    this.deploymentId = deploymentId;
    return this;
  }

  public Integer getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(Integer deploymentId) {
    this.deploymentId = deploymentId;
  }

  public MTDDeployment deploymentType(String deploymentType) {
    this.deploymentType = deploymentType;
    return this;
  }

  public String getDeploymentType() {
    return deploymentType;
  }

  public void setDeploymentType(String deploymentType) {
    this.deploymentType = deploymentType;
  }

  public MTDDeployment deploymentMethod(String deploymentMethod) {
    this.deploymentMethod = deploymentMethod;
    return this;
  }

  public String getDeploymentMethod() {
    return deploymentMethod;
  }

  public void setDeploymentMethod(String deploymentMethod) {
    this.deploymentMethod = deploymentMethod;
  }

  public MTDDeployment originId(SqlNullInt64 originId) {
    this.originId = originId;
    return this;
  }

  public SqlNullInt64 getOriginId() {
    return originId;
  }

  public void setOriginId(SqlNullInt64 originId) {
    this.originId = originId;
  }

  public MTDDeployment luaTenantId(SqlNullInt64 luaTenantId) {
    this.luaTenantId = luaTenantId;
    return this;
  }

  public SqlNullInt64 getLuaTenantId() {
    return luaTenantId;
  }

  public void setLuaTenantId(SqlNullInt64 luaTenantId) {
    this.luaTenantId = luaTenantId;
  }

  public MTDDeployment luaTenantDbTenantId(SqlNullInt64 luaTenantDbTenantId) {
    this.luaTenantDbTenantId = luaTenantDbTenantId;
    return this;
  }

  public SqlNullInt64 getLuaTenantDbTenantId() {
    return luaTenantDbTenantId;
  }

  public void setLuaTenantDbTenantId(SqlNullInt64 luaTenantDbTenantId) {
    this.luaTenantDbTenantId = luaTenantDbTenantId;
  }

  public MTDDeployment mtdBaseId(SqlNullInt64 mtdBaseId) {
    this.mtdBaseId = mtdBaseId;
    return this;
  }

  public SqlNullInt64 getMtdBaseId() {
    return mtdBaseId;
  }

  public void setMtdBaseId(SqlNullInt64 mtdBaseId) {
    this.mtdBaseId = mtdBaseId;
  }

  public MTDDeployment created(Date created) {
    this.created = created;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public MTDDeployment createdUnixMs(Long createdUnixMs) {
    this.createdUnixMs = createdUnixMs;
    return this;
  }

  public Long getCreatedUnixMs() {
    return createdUnixMs;
  }

  public void setCreatedUnixMs(Long createdUnixMs) {
    this.createdUnixMs = createdUnixMs;
  }

  public MTDDeployment user(ShortUser user) {
    this.user = user;
    return this;
  }

  public ShortUser getUser() {
    return user;
  }

  public void setUser(ShortUser user) {
    this.user = user;
  }

  public MTDDeployment version(Version version) {
    this.version = version;
    return this;
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
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
    MTDDeployment modelsDeployment = (MTDDeployment) o;
    return Objects.equals(this.deploymentId, modelsDeployment.deploymentId) &&
        Objects.equals(this.deploymentType, modelsDeployment.deploymentType) &&
        Objects.equals(this.deploymentMethod, modelsDeployment.deploymentMethod) &&
        Objects.equals(this.originId, modelsDeployment.originId) &&
        Objects.equals(this.luaTenantId, modelsDeployment.luaTenantId) &&
        Objects.equals(this.luaTenantDbTenantId, modelsDeployment.luaTenantDbTenantId) &&
        Objects.equals(this.mtdBaseId, modelsDeployment.mtdBaseId) &&
        Objects.equals(this.created, modelsDeployment.created) &&
        Objects.equals(this.createdUnixMs, modelsDeployment.createdUnixMs) &&
        Objects.equals(this.user, modelsDeployment.user) &&
        Objects.equals(this.version, modelsDeployment.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deploymentId, deploymentType, deploymentMethod, originId, luaTenantId, luaTenantDbTenantId, mtdBaseId, created, createdUnixMs, user, version);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDDeployment {\n");
    
    sb.append("    deploymentId: ").append(toIndentedString(deploymentId)).append("\n");
    sb.append("    deploymentType: ").append(toIndentedString(deploymentType)).append("\n");
    sb.append("    deploymentMethod: ").append(toIndentedString(deploymentMethod)).append("\n");
    sb.append("    originId: ").append(toIndentedString(originId)).append("\n");
    sb.append("    luaTenantId: ").append(toIndentedString(luaTenantId)).append("\n");
    sb.append("    luaTenantDbTenantId: ").append(toIndentedString(luaTenantDbTenantId)).append("\n");
    sb.append("    mtdBaseId: ").append(toIndentedString(mtdBaseId)).append("\n");
    sb.append("    created: ").append(toIndentedString(created)).append("\n");
    sb.append("    createdUnixMs: ").append(toIndentedString(createdUnixMs)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
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

