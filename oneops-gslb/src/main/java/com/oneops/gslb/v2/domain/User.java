package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class User {

  @SerializedName("id")
  public abstract Integer id();

  @SerializedName("name")
  @Nullable
  public abstract String name();

  @SerializedName("is_enabled")
  @Nullable
  public abstract Boolean isEnabled();

  @SerializedName("is_root")
  @Nullable
  public abstract Boolean isRoot();

  @SerializedName("is_user_manager")
  @Nullable
  public abstract Boolean isUserManager();

  @SerializedName("is_origin_manager")
  @Nullable
  public abstract Boolean isOriginManager();

  @SerializedName("is_api_auth")
  @Nullable
  public abstract Boolean isApiAuth();

  @SerializedName("groups")
  @Nullable
  public abstract List<ShortGroup> groups();

  public static User create(Integer id, String name, Boolean isEnabled, Boolean isRoot,
      Boolean isUserManager, Boolean isOriginManager, Boolean isApiAuth, List<ShortGroup> groups) {
    return new AutoValue_User(id, name, isEnabled, isRoot, isUserManager, isOriginManager, isApiAuth, groups);
  }

  public static TypeAdapter<User> typeAdapter(Gson gson) {
    return new AutoValue_User.GsonTypeAdapter(gson);
  }
  
}

