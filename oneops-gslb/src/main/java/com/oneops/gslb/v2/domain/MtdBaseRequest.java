package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class MtdBaseRequest {

  @SerializedName("mtd_base_name")
  public abstract String mtdBaseName();

  @SerializedName("type")
  public abstract String type();

  public static MtdBaseRequest create(String mtdBaseName, String type) {
    return new AutoValue_MtdBaseRequest(mtdBaseName, type);
  }

  public static TypeAdapter<MtdBaseRequest> typeAdapter(Gson gson) {
    return new AutoValue_MtdBaseRequest.GsonTypeAdapter(gson);
  }

}

