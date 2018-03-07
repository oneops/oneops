package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;

@AutoValue
public abstract class MtdTarget {

  @SerializedName("mtd_target_host")
  public abstract String mtdTargetHost();

  @SerializedName("data_center_id")
  public abstract Integer dataCenterId();

  @SerializedName("cloud_id")
  public abstract Integer cloudId();

  @SerializedName("enabled")
  @Nullable
  public abstract Boolean enabled();

  @SerializedName("weight_percent")
  @Nullable
  public abstract Integer weightPercent();

  public static MtdTarget create(String mtdTargetHost, Integer dataCenterId,
      Integer cloudId, @Nullable Boolean enabled, @Nullable Integer weightPercent) {
    return new AutoValue_MtdTarget(mtdTargetHost, dataCenterId, cloudId, enabled, weightPercent);
  }

  public static TypeAdapter<MtdTarget> typeAdapter(Gson gson) {
    return new AutoValue_MtdTarget.GsonTypeAdapter(gson);
  }
  
}

