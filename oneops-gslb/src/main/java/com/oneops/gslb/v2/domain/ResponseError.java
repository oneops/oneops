package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;

@AutoValue
public abstract class ResponseError {

  @SerializedName("severity")
  @Nullable
  public abstract Integer severity();

  @SerializedName("error_code")
  public abstract String errorCode();

  @SerializedName("short_message")
  @Nullable
  public abstract String shortMessage();

  @SerializedName("details")
  @Nullable
  public abstract String details();

  public static ResponseError create(Integer severity, String errorCode, String shortMessage, String details) {
    return new AutoValue_ResponseError(severity, errorCode, shortMessage, details);
  }

  public static TypeAdapter<ResponseError> typeAdapter(Gson gson) {
    return new AutoValue_ResponseError.GsonTypeAdapter(gson);
  }
  
}

