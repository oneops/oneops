package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;

@AutoValue
public abstract class ShortGroup {

  @SerializedName("id")
  @Nullable
  public abstract Integer id();

  @SerializedName("name")
  @Nullable
  public abstract String name();

  public static ShortGroup create(Integer id, String name) {
    return new AutoValue_ShortGroup(id, name);
  }
  
  public static TypeAdapter<ShortGroup> typeAdapter(Gson gson) {
    return new AutoValue_ShortGroup.GsonTypeAdapter(gson);
  }
  
}

