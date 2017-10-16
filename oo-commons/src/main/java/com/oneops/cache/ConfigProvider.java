package com.oneops.cache;

import java.util.List;

public interface ConfigProvider {

  public CacheConfig getCacheConfig(List<String> cacheHosts);

}
