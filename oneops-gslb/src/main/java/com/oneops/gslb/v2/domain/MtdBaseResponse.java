package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class MtdBaseResponse extends BaseResponse {
  
  @SerializedName("mtd_base")
  @Nullable
  public abstract MtdBase mtdBase();

  public static MtdBaseResponse create(Links links, Metadata metadata, List<ResponseError> errors, MtdBase mtdBase) {
    return new AutoValue_MtdBaseResponse(links, metadata, errors, mtdBase);
  }

  public static TypeAdapter<MtdBaseResponse> typeAdapter(Gson gson) {
    return new AutoValue_MtdBaseResponse.GsonTypeAdapter(gson);
  }
  
}

