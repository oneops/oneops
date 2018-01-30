package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {

  @SerializedName("id")
  private Integer id = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("is_enabled")
  private Boolean isEnabled = null;

  @SerializedName("is_root")
  private Boolean isRoot = null;

  @SerializedName("is_user_manager")
  private Boolean isUserManager = null;

  @SerializedName("is_origin_manager")
  private Boolean isOriginManager = null;

  @SerializedName("is_api_auth")
  private Boolean isApiAuth = null;

  @SerializedName("groups")
  private List<ShortGroup> groups = new ArrayList<ShortGroup>();

  public User id(Integer id) {
    this.id = id;
    return this;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public User name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public User isEnabled(Boolean isEnabled) {
    this.isEnabled = isEnabled;
    return this;
  }

  public Boolean getIsEnabled() {
    return isEnabled;
  }

  public void setIsEnabled(Boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public User isRoot(Boolean isRoot) {
    this.isRoot = isRoot;
    return this;
  }

  public Boolean getIsRoot() {
    return isRoot;
  }

  public void setIsRoot(Boolean isRoot) {
    this.isRoot = isRoot;
  }

  public User isUserManager(Boolean isUserManager) {
    this.isUserManager = isUserManager;
    return this;
  }

  public Boolean getIsUserManager() {
    return isUserManager;
  }

  public void setIsUserManager(Boolean isUserManager) {
    this.isUserManager = isUserManager;
  }

  public User isOriginManager(Boolean isOriginManager) {
    this.isOriginManager = isOriginManager;
    return this;
  }

  public Boolean getIsOriginManager() {
    return isOriginManager;
  }

  public void setIsOriginManager(Boolean isOriginManager) {
    this.isOriginManager = isOriginManager;
  }

  public User isApiAuth(Boolean isApiAuth) {
    this.isApiAuth = isApiAuth;
    return this;
  }

  public Boolean getIsApiAuth() {
    return isApiAuth;
  }

  public void setIsApiAuth(Boolean isApiAuth) {
    this.isApiAuth = isApiAuth;
  }

  public User groups(List<ShortGroup> groups) {
    this.groups = groups;
    return this;
  }

  public User addGroupsItem(ShortGroup groupsItem) {
    this.groups.add(groupsItem);
    return this;
  }

  public List<ShortGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<ShortGroup> groups) {
    this.groups = groups;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User modelsUser = (User) o;
    return Objects.equals(this.id, modelsUser.id) &&
        Objects.equals(this.name, modelsUser.name) &&
        Objects.equals(this.isEnabled, modelsUser.isEnabled) &&
        Objects.equals(this.isRoot, modelsUser.isRoot) &&
        Objects.equals(this.isUserManager, modelsUser.isUserManager) &&
        Objects.equals(this.isOriginManager, modelsUser.isOriginManager) &&
        Objects.equals(this.isApiAuth, modelsUser.isApiAuth) &&
        Objects.equals(this.groups, modelsUser.groups);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, isEnabled, isRoot, isUserManager, isOriginManager, isApiAuth, groups);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class User {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    isEnabled: ").append(toIndentedString(isEnabled)).append("\n");
    sb.append("    isRoot: ").append(toIndentedString(isRoot)).append("\n");
    sb.append("    isUserManager: ").append(toIndentedString(isUserManager)).append("\n");
    sb.append("    isOriginManager: ").append(toIndentedString(isOriginManager)).append("\n");
    sb.append("    isApiAuth: ").append(toIndentedString(isApiAuth)).append("\n");
    sb.append("    groups: ").append(toIndentedString(groups)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
  
}

