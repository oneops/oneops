package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;


public class ResponseError {

  @SerializedName("severity")
  private Integer severity = null;

  @SerializedName("error_code")
  private String errorCode = null;

  @SerializedName("short_message")
  private String shortMessage = null;

  @SerializedName("details")
  private String details = null;

  public ResponseError severity(Integer severity) {
    this.severity = severity;
    return this;
  }

  public Integer getSeverity() {
    return severity;
  }

  public void setSeverity(Integer severity) {
    this.severity = severity;
  }

  public ResponseError errorCode(String errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public ResponseError shortMessage(String shortMessage) {
    this.shortMessage = shortMessage;
    return this;
  }

  public String getShortMessage() {
    return shortMessage;
  }

  public void setShortMessage(String shortMessage) {
    this.shortMessage = shortMessage;
  }

  public ResponseError details(String details) {
    this.details = details;
    return this;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResponseError apiResponseError = (ResponseError) o;
    return Objects.equals(this.severity, apiResponseError.severity) &&
        Objects.equals(this.errorCode, apiResponseError.errorCode) &&
        Objects.equals(this.shortMessage, apiResponseError.shortMessage) &&
        Objects.equals(this.details, apiResponseError.details);
  }

  @Override
  public int hashCode() {
    return Objects.hash(severity, errorCode, shortMessage, details);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResponseError {\n");
    
    sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
    sb.append("    errorCode: ").append(toIndentedString(errorCode)).append("\n");
    sb.append("    shortMessage: ").append(toIndentedString(shortMessage)).append("\n");
    sb.append("    details: ").append(toIndentedString(details)).append("\n");
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

