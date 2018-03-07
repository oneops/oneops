package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class MtdBaseHostResponse extends BaseResponse {

  @SerializedName("version")
  @Nullable
  public abstract Version version();

  @SerializedName("deployment")
  @Nullable
  public abstract MtdDeployment deployment();

  public static MtdBaseHostResponse create(Links links, Metadata metadata, List<ResponseError> errors, Version version, MtdDeployment deployment) {
    return new AutoValue_MtdBaseHostResponse(links, metadata, errors, version, deployment);
  }

  public static TypeAdapter<MtdBaseHostResponse> typeAdapter(Gson gson) {
    return new AutoValue_MtdBaseHostResponse.GsonTypeAdapter(gson);
  }
  
}

