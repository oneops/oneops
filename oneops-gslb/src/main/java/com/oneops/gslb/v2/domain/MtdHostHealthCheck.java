package com.oneops.gslb.v2.domain;

import java.util.Objects;
import com.google.gson.annotations.SerializedName;

public class MtdHostHealthCheck {

  @SerializedName("name")
  private String name = null;

  @SerializedName("protocol")
  private String protocol = null;

  @SerializedName("port")
  private Integer port = null;

  @SerializedName("test_object_path")
  private String testObjectPath = null;

  @SerializedName("headers")
  private Object headers = null;

  @SerializedName("expected_status")
  private Integer expectedStatus = null;

  @SerializedName("expected_body")
  private String expectedBody = null;

  @SerializedName("fails_for_down")
  private Integer failsForDown = null;

  @SerializedName("pass")
  private Boolean pass = null;

  @SerializedName("tls")
  private Boolean tls = null;

  @SerializedName("request_body")
  private String requestBody = null;

  @SerializedName("network")
  private String network = null;

  @SerializedName("interval")
  private String interval = null;

  @SerializedName("retry_delay")
  private String retryDelay = null;

  @SerializedName("timeout")
  private String timeout = null;

  public MtdHostHealthCheck name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MtdHostHealthCheck protocol(String protocol) {
    this.protocol = protocol;
    return this;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public MtdHostHealthCheck port(Integer port) {
    this.port = port;
    return this;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public MtdHostHealthCheck testObjectPath(String testObjectPath) {
    this.testObjectPath = testObjectPath;
    return this;
  }

  public String getTestObjectPath() {
    return testObjectPath;
  }

  public void setTestObjectPath(String testObjectPath) {
    this.testObjectPath = testObjectPath;
  }

  public MtdHostHealthCheck headers(Object headers) {
    this.headers = headers;
    return this;
  }

  public Object getHeaders() {
    return headers;
  }

  public void setHeaders(Object headers) {
    this.headers = headers;
  }

  public MtdHostHealthCheck expectedStatus(Integer expectedStatus) {
    this.expectedStatus = expectedStatus;
    return this;
  }

  public Integer getExpectedStatus() {
    return expectedStatus;
  }

  public void setExpectedStatus(Integer expectedStatus) {
    this.expectedStatus = expectedStatus;
  }

  public MtdHostHealthCheck expectedBody(String expectedBody) {
    this.expectedBody = expectedBody;
    return this;
  }

  public String getExpectedBody() {
    return expectedBody;
  }

  public void setExpectedBody(String expectedBody) {
    this.expectedBody = expectedBody;
  }

  public MtdHostHealthCheck failsForDown(Integer failsForDown) {
    this.failsForDown = failsForDown;
    return this;
  }

  public Integer getFailsForDown() {
    return failsForDown;
  }

  public void setFailsForDown(Integer failsForDown) {
    this.failsForDown = failsForDown;
  }

  public MtdHostHealthCheck pass(Boolean pass) {
    this.pass = pass;
    return this;
  }

  public Boolean getPass() {
    return pass;
  }

  public void setPass(Boolean pass) {
    this.pass = pass;
  }

  public MtdHostHealthCheck tls(Boolean tls) {
    this.tls = tls;
    return this;
  }

  public Boolean getTls() {
    return tls;
  }

  public void setTls(Boolean tls) {
    this.tls = tls;
  }

  public MtdHostHealthCheck requestBody(String requestBody) {
    this.requestBody = requestBody;
    return this;
  }

  public String getRequestBody() {
    return requestBody;
  }

  public void setRequestBody(String requestBody) {
    this.requestBody = requestBody;
  }

  public MtdHostHealthCheck network(String network) {
    this.network = network;
    return this;
  }

  public String getNetwork() {
    return network;
  }

  public void setNetwork(String network) {
    this.network = network;
  }

  public MtdHostHealthCheck interval(String interval) {
    this.interval = interval;
    return this;
  }

  public MtdHostHealthCheck retryDelay(String retryDelay) {
    this.retryDelay = retryDelay;
    return this;
  }

  public MtdHostHealthCheck timeout(String timeout) {
    this.timeout = timeout;
    return this;
  }

  public String getInterval() {
    return interval;
  }

  public void setInterval(String interval) {
    this.interval = interval;
  }

  public String getRetryDelay() {
    return retryDelay;
  }

  public void setRetryDelay(String retryDelay) {
    this.retryDelay = retryDelay;
  }

  public String getTimeout() {
    return timeout;
  }

  public void setTimeout(String timeout) {
    this.timeout = timeout;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MtdHostHealthCheck modelsMTDHealthCheck = (MtdHostHealthCheck) o;
    return Objects.equals(this.name, modelsMTDHealthCheck.name) &&
        Objects.equals(this.protocol, modelsMTDHealthCheck.protocol) &&
        Objects.equals(this.port, modelsMTDHealthCheck.port) &&
        Objects.equals(this.testObjectPath, modelsMTDHealthCheck.testObjectPath) &&
        Objects.equals(this.headers, modelsMTDHealthCheck.headers) &&
        Objects.equals(this.expectedStatus, modelsMTDHealthCheck.expectedStatus) &&
        Objects.equals(this.expectedBody, modelsMTDHealthCheck.expectedBody) &&
        Objects.equals(this.failsForDown, modelsMTDHealthCheck.failsForDown) &&
        Objects.equals(this.pass, modelsMTDHealthCheck.pass) &&
        Objects.equals(this.tls, modelsMTDHealthCheck.tls) &&
        Objects.equals(this.requestBody, modelsMTDHealthCheck.requestBody) &&
        Objects.equals(this.network, modelsMTDHealthCheck.network);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, protocol, port, testObjectPath, headers, expectedStatus, expectedBody, failsForDown, pass, tls, requestBody, network);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDHostHealthCheck {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    port: ").append(toIndentedString(port)).append("\n");
    sb.append("    testObjectPath: ").append(toIndentedString(testObjectPath)).append("\n");
    sb.append("    headers: ").append(toIndentedString(headers)).append("\n");
    sb.append("    expectedStatus: ").append(toIndentedString(expectedStatus)).append("\n");
    sb.append("    expectedBody: ").append(toIndentedString(expectedBody)).append("\n");
    sb.append("    failsForDown: ").append(toIndentedString(failsForDown)).append("\n");
    sb.append("    pass: ").append(toIndentedString(pass)).append("\n");
    sb.append("    tls: ").append(toIndentedString(tls)).append("\n");
    sb.append("    requestBody: ").append(toIndentedString(requestBody)).append("\n");
    sb.append("    network: ").append(toIndentedString(network)).append("\n");
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

