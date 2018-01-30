package com.oneops.cms.execution;

import java.util.Map;

public class Response {

  private Result result;
  private Map<String, String> responseMap;

  public Result getResult() {
    return result;
  }

  public void setResult(Result result) {
    this.result = result;
  }

  public Map<String, String> getResponseMap() {
    return responseMap;
  }

  public void setResponseMap(Map<String, String> responseMap) {
    this.responseMap = responseMap;
  }

  public static Response getNotMatchingResponse() {
    Response response = new Response();
    response.result = Result.NOT_MATCHED;
    return response;
  }
}
