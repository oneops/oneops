package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class MtdHostResponse extends BaseResponse {

  @SerializedName("mtd_host")
  @Nullable
  public abstract MtdHost mtdHost();

  public static MtdHostResponse create(Links links, Metadata metadata, List<ResponseError> errors, MtdHost mtdHost) {
    return new AutoValue_MtdHostResponse(links, metadata, errors, mtdHost);
  }

  public static TypeAdapter<MtdHostResponse> typeAdapter(Gson gson) {
    return new AutoValue_MtdHostResponse.GsonTypeAdapter(gson);
  }
  
}

