package com.oneops.gslb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GslbConfig {

  @Bean
  public Gson gson() {
    return new Gson();
  }

  @Bean
  public JsonParser jsonParser() {
    return new JsonParser();
  }

  @Bean
  public Gson gsonPretty() {
    return new GsonBuilder().setPrettyPrinting().create();
  }

}
