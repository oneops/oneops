package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;

@AutoValue
public abstract class Metadata {

  @SerializedName("pong")
  @Nullable
  public abstract Integer pong();

  @SerializedName("method")
  public abstract String method();

  @SerializedName("referer")
  @Nullable
  public abstract String referer();

  @SerializedName("start")
  @Nullable
  public abstract String start();

  @SerializedName("duration")
  @Nullable
  public abstract String duration();

  public static Metadata create(Integer pong, String method, String referer, String start, String duration) {
    return new AutoValue_Metadata(pong, method, referer, start, duration);
  }

  public static TypeAdapter<Metadata> typeAdapter(Gson gson) {
    return new AutoValue_Metadata.GsonTypeAdapter(gson);
  }
  
}

