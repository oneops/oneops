package com.oneops.gslb;

import static java.util.Objects.hash;

import java.util.Objects;

public class TorbitConfig {

  private String url;

  private String user;

  private String authKey;

  private int groupId;

  public TorbitConfig user(String user) {
    setUser(user);
    return this;
  }

  public TorbitConfig authKey(String authKey) {
    setAuthKey(authKey);
    return this;
  }

  public TorbitConfig url(String url) {
    setUrl(url);
    return this;
  }

  public TorbitConfig groupId(int groupId) {
    setGroupId(groupId);
    return this;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAuthKey() {
    return authKey;
  }

  public void setAuthKey(String authKey) {
    this.authKey = authKey;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  @Override
  public int hashCode() {
    return hash(url, user, authKey, groupId);
  }

  public int getGroupId() {
    return groupId;
  }

  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }

  @Override

  public boolean equals(Object obj) {
    if (obj != null && obj instanceof TorbitConfig) {
      TorbitConfig c = (TorbitConfig)obj;
      return Objects.equals(c.url, url) && Objects.equals(c.authKey, authKey) &&
          Objects.equals(c.user, user) && Objects.equals(c.groupId, groupId);
    }
    return false;
  }
}
