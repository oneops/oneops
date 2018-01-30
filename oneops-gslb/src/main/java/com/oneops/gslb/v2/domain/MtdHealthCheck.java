package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MtdHealthCheck {

  @SerializedName("name")
  private String name = null;

  @SerializedName("protocol")
  private String protocol = null;

  @SerializedName("port")
  private Integer port = null;

  @SerializedName("test_object_path")
  private String testObjectPath = null;

  @SerializedName("headers")
  private List<MtdHealthCheckHeader> headers = new ArrayList<MtdHealthCheckHeader>();

  @SerializedName("expected_status")
  private Integer expectedStatus = null;

  @SerializedName("expected_body")
  private String expectedBody = null;

  @SerializedName("fails_for_down")
  private Integer failsForDown = null;

  @SerializedName("interval")
  private String interval = null;

  @SerializedName("retry_delay")
  private String retryDelay = null;

  @SerializedName("timeout")
  private String timeout = null;

  @SerializedName("pass")
  private Boolean pass = null;

  public MtdHealthCheck name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MtdHealthCheck protocol(String protocol) {
    this.protocol = protocol;
    return this;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public MtdHealthCheck port(Integer port) {
    this.port = port;
    return this;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public MtdHealthCheck testObjectPath(String testObjectPath) {
    this.testObjectPath = testObjectPath;
    return this;
  }

  public String getTestObjectPath() {
    return testObjectPath;
  }

  public void setTestObjectPath(String testObjectPath) {
    this.testObjectPath = testObjectPath;
  }

  public MtdHealthCheck headers(List<MtdHealthCheckHeader> headers) {
    this.headers = headers;
    return this;
  }

  public MtdHealthCheck addHeadersItem(MtdHealthCheckHeader headersItem) {
    this.headers.add(headersItem);
    return this;
  }

  public List<MtdHealthCheckHeader> getHeaders() {
    return headers;
  }

  public void setHeaders(List<MtdHealthCheckHeader> headers) {
    this.headers = headers;
  }

  public MtdHealthCheck expectedStatus(Integer expectedStatus) {
    this.expectedStatus = expectedStatus;
    return this;
  }

  public Integer getExpectedStatus() {
    return expectedStatus;
  }

  public void setExpectedStatus(Integer expectedStatus) {
    this.expectedStatus = expectedStatus;
  }

  public MtdHealthCheck expectedBody(String expectedBody) {
    this.expectedBody = expectedBody;
    return this;
  }

  public String getExpectedBody() {
    return expectedBody;
  }

  public void setExpectedBody(String expectedBody) {
    this.expectedBody = expectedBody;
  }

  public MtdHealthCheck failsForDown(Integer failsForDown) {
    this.failsForDown = failsForDown;
    return this;
  }

  public Integer getFailsForDown() {
    return failsForDown;
  }

  public void setFailsForDown(Integer failsForDown) {
    this.failsForDown = failsForDown;
  }

  public MtdHealthCheck interval(String interval) {
    this.interval = interval;
    return this;
  }

  public String getInterval() {
    return interval;
  }

  public void setInterval(String interval) {
    this.interval = interval;
  }

  public MtdHealthCheck retryDelay(String retryDelay) {
    this.retryDelay = retryDelay;
    return this;
  }

  public String getRetryDelay() {
    return retryDelay;
  }

  public void setRetryDelay(String retryDelay) {
    this.retryDelay = retryDelay;
  }

  public MtdHealthCheck timeout(String timeout) {
    this.timeout = timeout;
    return this;
  }

  public String getTimeout() {
    return timeout;
  }

  public void setTimeout(String timeout) {
    this.timeout = timeout;
  }

  public MtdHealthCheck pass(Boolean pass) {
    this.pass = pass;
    return this;
  }

  public Boolean getPass() {
    return pass;
  }

  public void setPass(Boolean pass) {
    this.pass = pass;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MtdHealthCheck apiMTDHealthCheckRead = (MtdHealthCheck) o;
    return Objects.equals(this.name, apiMTDHealthCheckRead.name) &&
        Objects.equals(this.protocol, apiMTDHealthCheckRead.protocol) &&
        Objects.equals(this.port, apiMTDHealthCheckRead.port) &&
        Objects.equals(this.testObjectPath, apiMTDHealthCheckRead.testObjectPath) &&
        Objects.equals(this.headers, apiMTDHealthCheckRead.headers) &&
        Objects.equals(this.expectedStatus, apiMTDHealthCheckRead.expectedStatus) &&
        Objects.equals(this.expectedBody, apiMTDHealthCheckRead.expectedBody) &&
        Objects.equals(this.failsForDown, apiMTDHealthCheckRead.failsForDown) &&
        Objects.equals(this.interval, apiMTDHealthCheckRead.interval) &&
        Objects.equals(this.retryDelay, apiMTDHealthCheckRead.retryDelay) &&
        Objects.equals(this.timeout, apiMTDHealthCheckRead.timeout) &&
        Objects.equals(this.pass, apiMTDHealthCheckRead.pass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, protocol, port, testObjectPath, headers, expectedStatus, expectedBody, failsForDown, interval, retryDelay, timeout, pass);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDHealthCheck {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    port: ").append(toIndentedString(port)).append("\n");
    sb.append("    testObjectPath: ").append(toIndentedString(testObjectPath)).append("\n");
    sb.append("    headers: ").append(toIndentedString(headers)).append("\n");
    sb.append("    expectedStatus: ").append(toIndentedString(expectedStatus)).append("\n");
    sb.append("    expectedBody: ").append(toIndentedString(expectedBody)).append("\n");
    sb.append("    failsForDown: ").append(toIndentedString(failsForDown)).append("\n");
    sb.append("    interval: ").append(toIndentedString(interval)).append("\n");
    sb.append("    retryDelay: ").append(toIndentedString(retryDelay)).append("\n");
    sb.append("    timeout: ").append(toIndentedString(timeout)).append("\n");
    sb.append("    pass: ").append(toIndentedString(pass)).append("\n");
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

