package com.oneops.inductor.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

public class JSONUtils {

  public static Map<String, Object> convertJsonToMap(String jsonContent) {
    Map<String, Object> jsonMap;
    try {
      Type type = new TypeToken<Map<String, Object>>() {
      }.getType();
      jsonMap = new Gson().fromJson(jsonContent, type);
    } catch (JsonSyntaxException e) {
      jsonMap = Collections.emptyMap();
    }
    if(jsonMap == null)
            jsonMap = Collections.emptyMap();
    return jsonMap;
  }

  public static boolean isJSONValid(String jsonInString) {
    try {
      new Gson().fromJson(jsonInString, Object.class);
      return true;
    } catch (JsonSyntaxException ex) {
      return false;
    }
  }
}