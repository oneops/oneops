package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;

@AutoValue
public abstract class MtdBase {

  @SerializedName("mtd_base_id")
  public abstract Integer mtdBaseId();

  @SerializedName("mtd_base_name")
  public abstract String mtdBaseName();

  @SerializedName("version")
  @Nullable
  public abstract MtdBaseVersion version();

  public static MtdBase create(Integer mtdBaseId, String mtdBaseName, MtdBaseVersion version) {
    return new AutoValue_MtdBase(mtdBaseId, mtdBaseName, version);
  }

  public static TypeAdapter<MtdBase> typeAdapter(Gson gson) {
    return new AutoValue_MtdBase.GsonTypeAdapter(gson);
  }
  
}

