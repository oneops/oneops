package com.oneops.cms.ds;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DataSourceTest {

  private DataSourceConfig config;

  @Before
  public void setup() {
    config = new DataSourceConfig();
    config.setApplicationName("adapter");
  }

  @Test
  public void testJdbcUrl() {
    String host = "localhost";
    String jdbcUrl = config.jdbcUrl(host, null);
    Assert.assertEquals("jdbc:postgresql://localhost/kloopzdb?autoReconnect=true&ApplicationName=adapter", jdbcUrl);
    Map<String, String> map = new LinkedHashMap<>();
    map.put("connectTimeout", "3");
    jdbcUrl = config.jdbcUrl(host, map);
    Assert.assertEquals("jdbc:postgresql://localhost/kloopzdb?autoReconnect=true&ApplicationName=adapter&connectTimeout=3", jdbcUrl);
    map.put("socketTimeout", "5");
    map.put("reWriteBatchedInserts", "true");
    map.put("loadBalanceHosts", "true");
    jdbcUrl = config.jdbcUrl(host, map);
    Assert.assertEquals("jdbc:postgresql://localhost/kloopzdb?autoReconnect=true&ApplicationName=adapter&connectTimeout=3&socketTimeout=5&reWriteBatchedInserts=true&loadBalanceHosts=true", jdbcUrl);
  }
}
