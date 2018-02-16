package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class Cloud {

  @SerializedName("id")
  public abstract Integer id();

  @SerializedName("name")
  public abstract String name();

  @SerializedName("data_center_id")
  public abstract Integer dataCenterId();

  @SerializedName("cidrs")
  @Nullable
  public abstract List<String> cidrs();

  public static Cloud create(Integer id, String name, Integer dataCenterId, @Nullable List<String> cidrs) {
    return new AutoValue_Cloud(id, name, dataCenterId, cidrs);
  }

  public static TypeAdapter<Cloud> typeAdapter(Gson gson) {
    return new AutoValue_Cloud.GsonTypeAdapter(gson);
  }

}

