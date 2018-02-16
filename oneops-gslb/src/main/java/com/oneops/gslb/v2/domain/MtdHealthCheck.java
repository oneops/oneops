package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class MtdHealthCheck {

  @SerializedName("name")
  public abstract String name();

  @SerializedName("protocol")
  @Nullable
  public abstract String protocol();

  @SerializedName("port")
  public abstract Integer port();

  @SerializedName("test_object_path")
  @Nullable
  public abstract String testObjectPath();

  @SerializedName("headers")
  @Nullable
  public abstract List<MtdHealthCheckHeader> headers();

  @SerializedName("expected_status")
  @Nullable
  public abstract Integer expectedStatus();

  @SerializedName("expected_body")
  @Nullable
  public abstract  String expectedBody();

  @SerializedName("fails_for_down")
  @Nullable
  public abstract Integer failsForDown();

  @SerializedName("interval")
  public abstract  String interval();

  @SerializedName("retry_delay")
  public abstract String retryDelay();

  @SerializedName("timeout")
  public abstract String timeout();

  @SerializedName("pass")
  @Nullable
  public abstract Boolean pass();

  public static MtdHealthCheck create(String name, String protocol, Integer port, @Nullable String testObjectPath,
      @Nullable List<MtdHealthCheckHeader> headers,
      @Nullable Integer expectedStatus, @Nullable String expectedBody, @Nullable Integer failsForDown,
      String interval, String retryDelay, String timeout, @Nullable Boolean pass) {
    return new AutoValue_MtdHealthCheck(name, protocol, port, testObjectPath, headers,
        expectedStatus, expectedBody, failsForDown, interval, retryDelay, timeout, pass);
  }

  public static TypeAdapter<MtdHealthCheck> typeAdapter(Gson gson) {
    return new AutoValue_MtdHealthCheck.GsonTypeAdapter(gson);
  }
  
}

