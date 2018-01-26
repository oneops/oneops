package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class Metadata {

  @SerializedName("pong")
  private Integer pong = null;

  @SerializedName("method")
  private String method = null;

  @SerializedName("referer")
  private String referer = null;

  @SerializedName("start")
  private String start = null;

  @SerializedName("duration")
  private String duration = null;

  public Metadata pong(Integer pong) {
    this.pong = pong;
    return this;
  }

  public Integer getPong() {
    return pong;
  }

  public void setPong(Integer pong) {
    this.pong = pong;
  }

  public Metadata method(String method) {
    this.method = method;
    return this;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public Metadata referer(String referer) {
    this.referer = referer;
    return this;
  }

  public String getReferer() {
    return referer;
  }

  public void setReferer(String referer) {
    this.referer = referer;
  }

  public Metadata start(String start) {
    this.start = start;
    return this;
  }

  public String getStart() {
    return start;
  }

  public void setStart(String start) {
    this.start = start;
  }

  public Metadata duration(String duration) {
    this.duration = duration;
    return this;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Metadata apiMetadata = (Metadata) o;
    return Objects.equals(this.pong, apiMetadata.pong) &&
        Objects.equals(this.method, apiMetadata.method) &&
        Objects.equals(this.referer, apiMetadata.referer) &&
        Objects.equals(this.start, apiMetadata.start) &&
        Objects.equals(this.duration, apiMetadata.duration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pong, method, referer, start, duration);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Metadata {\n");
    
    sb.append("    pong: ").append(toIndentedString(pong)).append("\n");
    sb.append("    method: ").append(toIndentedString(method)).append("\n");
    sb.append("    referer: ").append(toIndentedString(referer)).append("\n");
    sb.append("    start: ").append(toIndentedString(start)).append("\n");
    sb.append("    duration: ").append(toIndentedString(duration)).append("\n");
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

