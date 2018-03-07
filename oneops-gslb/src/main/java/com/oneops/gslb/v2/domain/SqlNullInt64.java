package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;

@AutoValue
public abstract class SqlNullInt64 {

  @SerializedName("Int64")
  public abstract Long int64();

  @SerializedName("Valid")
  @Nullable
  public abstract Boolean valid();

  public static SqlNullInt64 create(Long int64, Boolean valid) {
    return new AutoValue_SqlNullInt64(int64, valid);
  }
  
  public static TypeAdapter<SqlNullInt64> typeAdapter(Gson gson) {
    return new AutoValue_SqlNullInt64.GsonTypeAdapter(gson);
  }
  
}

