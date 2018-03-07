package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class MtdBaseHostRequest {

  @SerializedName("mtd_host")
  public abstract MtdHost mtdHost();

  public static MtdBaseHostRequest create(MtdHost mtdHost) {
    return new AutoValue_MtdBaseHostRequest(mtdHost);
  }

  public static TypeAdapter<MtdBaseHostRequest> typeAdapter(Gson gson) {
    return new AutoValue_MtdBaseHostRequest.GsonTypeAdapter(gson);
  }
  
}

