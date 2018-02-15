package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.Date;
import javax.annotation.Nullable;

@AutoValue
public abstract class MtdDeployment {

  @SerializedName("deployment_id")
  @Nullable
  public abstract Integer deploymentId();

  @SerializedName("deployment_type")
  @Nullable
  public abstract String deploymentType();

  @SerializedName("deployment_method")
  @Nullable
  public abstract String deploymentMethod();

  @SerializedName("origin_id")
  @Nullable
  public abstract SqlNullInt64 originId();

  @SerializedName("lua_tenant_id")
  @Nullable
  public abstract SqlNullInt64 luaTenantId();

  @SerializedName("lua_tenant_db_tenant_id")
  @Nullable
  public abstract SqlNullInt64 luaTenantDbTenantId();

  @SerializedName("mtd_base_id")
  @Nullable
  public abstract SqlNullInt64 mtdBaseId();

  @SerializedName("created")
  @Nullable
  public abstract Date created();

  @SerializedName("created_unix_ms")
  @Nullable
  public abstract Long createdUnixMs();

  @SerializedName("User")
  @Nullable
  public abstract ShortUser user();

  @SerializedName("Version")
  @Nullable
  public abstract Version version();

  public static MtdDeployment create(Integer deploymentId, String deploymentType, String deploymentMethod,
      SqlNullInt64 originId, SqlNullInt64 luaTenantId, SqlNullInt64 luaTenantDbTenantId, SqlNullInt64 mtdBaseId,
      Date created, Long createdUnixMs, ShortUser user, Version version) {
    return new AutoValue_MtdDeployment(deploymentId, deploymentType, deploymentMethod, originId, luaTenantId,
        luaTenantDbTenantId, mtdBaseId, created, createdUnixMs, user, version);
  }
  
  public static TypeAdapter<MtdDeployment> typeAdapter(Gson gson) {
    return new AutoValue_MtdDeployment.GsonTypeAdapter(gson);
  }
  

}
