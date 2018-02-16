package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.Date;
import javax.annotation.Nullable;

@AutoValue
public abstract class Version {

  @SerializedName("version_id")
  public abstract Integer versionId();

  @SerializedName("based_on")
  @Nullable
  public abstract Integer basedOn();

  @SerializedName("version")
  @Nullable
  public abstract Integer version();

  @SerializedName("description")
  @Nullable
  public abstract String description();

  @SerializedName("data")
  @Nullable
  public abstract String data();

  @SerializedName("version_type")
  @Nullable
  public abstract String versionType();

  @SerializedName("user_id")
  @Nullable
  public abstract Integer userId();

  @SerializedName("created")
  @Nullable
  public abstract Date created();

  public static Version create(Integer versionId, Integer basedOn, Integer version,
      String description, String data, String versionType, Integer userId, Date created) {
    return new AutoValue_Version(versionId, basedOn, version, description, data, versionType, userId, created);
  }
  
  public static TypeAdapter<Version> typeAdapter(Gson gson) {
    return new AutoValue_Version.GsonTypeAdapter(gson);
  }
  
}

