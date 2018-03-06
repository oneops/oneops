package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;

@AutoValue
public abstract class MtdBaseVersion {

  @SerializedName("version_id")
  @Nullable
  public abstract SqlNullInt64 versionI();

  @SerializedName("version")
  @Nullable
  public abstract SqlNullInt64 version();

  @SerializedName("description")
  @Nullable
  public abstract SqlNullString description();

  public static MtdBaseVersion create(SqlNullInt64 versionI, SqlNullInt64 version, SqlNullString description) {
    return new AutoValue_MtdBaseVersion(versionI, version, description);
  }
  
  public static TypeAdapter<MtdBaseVersion> typeAdapter(Gson gson) {
    return new AutoValue_MtdBaseVersion.GsonTypeAdapter(gson);
  }
  
}

