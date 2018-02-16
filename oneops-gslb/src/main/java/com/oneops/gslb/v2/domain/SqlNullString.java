package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;

@AutoValue
public abstract class SqlNullString {

  @SerializedName("String")
  public abstract String string();

  @SerializedName("Valid")
  @Nullable
  public abstract Boolean valid();

  public static SqlNullString create(String string, Boolean valid) {
    return new AutoValue_SqlNullString(string, valid);
  }

  public static TypeAdapter<SqlNullString> typeAdapter(Gson gson) {
    return new AutoValue_SqlNullString.GsonTypeAdapter(gson);
  }
  
}

