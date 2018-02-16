package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class DataCenter {

  @SerializedName("id")
  public abstract Integer id();

  @SerializedName("name")
  @Nullable
  public abstract String name();

  @SerializedName("clouds")
  @Nullable
  public abstract List<Cloud> clouds();

  public static DataCenter create(Integer id, String name, List<Cloud> clouds) {
    return new AutoValue_DataCenter(id, name, clouds);
  }

  public static TypeAdapter<DataCenter> typeAdapter(Gson gson) {
    return new AutoValue_DataCenter.GsonTypeAdapter(gson);
  }
}

