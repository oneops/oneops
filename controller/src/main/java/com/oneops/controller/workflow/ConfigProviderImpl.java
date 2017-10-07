package com.oneops.controller.workflow;

import com.hazelcast.config.MapConfig;
import com.hazelcast.config.QuorumConfig;
import com.oneops.cache.CacheConfig;
import com.oneops.cache.ConfigProvider;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

public class ConfigProviderImpl implements ConfigProvider {

  private Logger logger = Logger.getLogger(DeployerImpl.class);

  @Value("#{'${CONTROLLER_QUORUM_SIZE:0}'}")
  private int quorumSize;

  @Override
  public CacheConfig getCacheConfig(List<String> cacheHosts) {
    CacheConfig cacheConfig = new CacheConfig();
    if (quorumSize == 0) {
      quorumSize = cacheHosts.size() > 1 ? (cacheHosts.size()/2) + 1 : 1;
    }
    logger.info("configuring cache with quorumSize " + quorumSize);
    QuorumConfig quorumConfig = new QuorumConfig(HazelcastDpmtCache.CACHE_QUORUM_NAME, false, quorumSize);
    cacheConfig.addQuorumConfig(quorumConfig);
    MapConfig mapConfig = new MapConfig("controller.*");
    mapConfig.setBackupCount(1);
    mapConfig.setQuorumName(HazelcastDpmtCache.CACHE_QUORUM_NAME);
    cacheConfig.addMapConfig(mapConfig);
    return cacheConfig;
  }
}
