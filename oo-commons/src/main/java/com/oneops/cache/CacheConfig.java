package com.oneops.cache;

import com.hazelcast.config.MapConfig;
import com.hazelcast.config.QuorumConfig;
import java.util.ArrayList;
import java.util.List;

public class CacheConfig {

  private List<MapConfig> mapConfigs = new ArrayList<>();
  private List<QuorumConfig> quorumConfigs = new ArrayList<>();

  public void addMapConfig(MapConfig mapConfig) {
    mapConfigs.add(mapConfig);
  }

  public void addQuorumConfig(QuorumConfig quorumConfig) {
    quorumConfigs.add(quorumConfig);
  }

  public List<MapConfig> getMapConfigs() {
    return mapConfigs;
  }

  public void setMapConfigs(List<MapConfig> mapConfigs) {
    this.mapConfigs = mapConfigs;
  }

  public List<QuorumConfig> getQuorumConfigs() {
    return quorumConfigs;
  }

  public void setQuorumConfigs(List<QuorumConfig> quorumConfigs) {
    this.quorumConfigs = quorumConfigs;
  }
}
