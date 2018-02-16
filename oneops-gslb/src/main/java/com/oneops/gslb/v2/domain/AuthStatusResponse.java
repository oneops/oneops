package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class AuthStatusResponse extends BaseResponse {

  @SerializedName("user")
  @Nullable
  public abstract User user();

  @SerializedName("client_ip_address")
  public abstract String clientIpAddress();

  @SerializedName("authenticated")
  public abstract Boolean authenticated();

  @SerializedName("credentials_validators")
  @Nullable
  public abstract String credentialsValidators();

  public static AuthStatusResponse create(Links links, Metadata metadata, List<ResponseError> errors, User user, String clientIpAddress, Boolean authenticated, String credentialsValidators) {
    return new AutoValue_AuthStatusResponse(links, metadata, errors, user, clientIpAddress, authenticated, credentialsValidators);
  }

  public static TypeAdapter<AuthStatusResponse> typeAdapter(Gson gson) {
    return new AutoValue_AuthStatusResponse.GsonTypeAdapter(gson);
  }
  
}

