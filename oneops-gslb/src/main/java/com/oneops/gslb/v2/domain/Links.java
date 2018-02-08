package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;

@AutoValue
public abstract class Links {

  @SerializedName("self")
  @Nullable
  public abstract String self();

  public static Links create(String self) {
    return new AutoValue_Links(self);
  }

  public static TypeAdapter<Links> typeAdapter(Gson gson) {
    return new AutoValue_Links.GsonTypeAdapter(gson);
  }
  
}

