package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class CreateMtdBaseRequest {

  @SerializedName("mtd_base")
  public abstract MtdBaseRequest mtdBase();

  public static CreateMtdBaseRequest create(MtdBaseRequest mtdBase) {
    return new AutoValue_CreateMtdBaseRequest(mtdBase);
  }

  public static TypeAdapter<CreateMtdBaseRequest> typeAdapter(Gson gson) {
    return new AutoValue_CreateMtdBaseRequest.GsonTypeAdapter(gson);
  }
  
}

