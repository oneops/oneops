package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class MtdHealthCheckHeader {

  @SerializedName("name")
  public abstract String name();

  @SerializedName("value")
  @Nullable
  public abstract List<String> value();

  public static MtdHealthCheckHeader create(String name, List<String> value) {
    return new AutoValue_MtdHealthCheckHeader(name, value);
  }

  public static TypeAdapter<MtdHealthCheckHeader> typeAdapter(Gson gson) {
    return new AutoValue_MtdHealthCheckHeader.GsonTypeAdapter(gson);
  }
  
}

