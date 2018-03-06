package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;

@AutoValue
public abstract class DCFallbackTarget {

  @SerializedName("fallback_target")
  @Nullable
  public abstract String fallbackTarget();

  @SerializedName("data_center_id")
  @Nullable
  public abstract Integer dataCenterId();

  public static DCFallbackTarget create(String fallbackTarget, Integer dataCenterId) {
    return new AutoValue_DCFallbackTarget(fallbackTarget, dataCenterId);
  }

  public static TypeAdapter<DCFallbackTarget> typeAdapter(Gson gson) {
    return new AutoValue_DCFallbackTarget.GsonTypeAdapter(gson);
  }
  
}

