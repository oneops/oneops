package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;

@AutoValue
public abstract class MtdHostHealthCheck {

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
  public abstract Object headers();

  @SerializedName("expected_status")
  @Nullable
  public abstract Integer expectedStatus();

  @SerializedName("expected_body")
  @Nullable
  public abstract String expectedBody();

  @SerializedName("fails_for_down")
  @Nullable
  public abstract Integer failsForDown();

  @SerializedName("pass")
  @Nullable
  public abstract Boolean pass();

  @SerializedName("tls")
  @Nullable
  public abstract Boolean tls();

  @SerializedName("request_body")
  @Nullable
  public abstract String requestBody();

  @SerializedName("network")
  @Nullable
  public abstract String network();

  @SerializedName("interval")
  public abstract String interval();

  @SerializedName("retry_delay")
  public abstract String retryDelay();

  @SerializedName("timeout")
  public abstract String timeout();

  public static MtdHostHealthCheck create(String name, String protocol, Integer port, @Nullable String testObjectPath,
      @Nullable Object headers, @Nullable Integer expectedStatus, @Nullable String expectedBody,
      @Nullable Integer failsForDown, @Nullable Boolean pass, @Nullable Boolean tls,
      @Nullable String requestBody, @Nullable String network, String interval,
      String retryDelay, String timeout) {
    return new AutoValue_MtdHostHealthCheck(name, protocol, port, testObjectPath, headers,
        expectedStatus, expectedBody, failsForDown, pass, tls, requestBody, network, interval, retryDelay, timeout);
  }

  public static TypeAdapter<MtdHostHealthCheck> typeAdapter(Gson gson) {
    return new AutoValue_MtdHostHealthCheck.GsonTypeAdapter(gson);
  }

}

