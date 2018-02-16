package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.Nullable;

public abstract class BaseResponse {

  @SerializedName("links")
  @Nullable
  public abstract Links links();

  @SerializedName("metadata")
  @Nullable
  public abstract Metadata metadata();

  @SerializedName("errors")
  @Nullable
  public abstract List<ResponseError> errors();

}
