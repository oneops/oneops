package com.oneops.cache;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.QuorumConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = "file:${CACHE_PROPERTIES_FILE}", ignoreResourceNotFound = true)
public class CacheInitializer {

  private static final Logger logger = Logger.getLogger(CacheInitializer.class);

  @Value("#{'${CACHE_HOST:cache}'}")
  private List<String> cacheHosts;

  @Autowired
  private ConfigProvider configProvider;

  private HazelcastInstance hazelcastInstance;

  @PostConstruct
  public void init() {
    logger.info("initializing hazelcast cache with hosts " + cacheHosts);
    hazelcastInstance = Hazelcast.newHazelcastInstance(getConfig());
  }

  private Config getConfig() {
    Config config = new Config();
    JoinConfig join = config.getNetworkConfig().getJoin();
    join.getTcpIpConfig().setEnabled(true);
    join.getTcpIpConfig().setMembers(cacheHosts);
    join.getMulticastConfig().setEnabled(false);
    join.getAwsConfig().setEnabled(false);
    CacheConfig cacheConfig = configProvider.getCacheConfig(cacheHosts);
    List<QuorumConfig> quorumConfigs = cacheConfig.getQuorumConfigs();
    quorumConfigs.stream().forEach(q -> config.addQuorumConfig(q));
    List<MapConfig> mapConfigs = cacheConfig.getMapConfigs();
    mapConfigs.stream().forEach(c -> config.addMapConfig(c));
    return config;
  }

  @PreDestroy
  public void destroy() {
    hazelcastInstance.shutdown();
  }

  public HazelcastInstance getHazelcastInstance() {
    return hazelcastInstance;
  }
}
