package com.oneops.controller.workflow;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.oneops.cache.CacheInitializer;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

public class HazelcastDpmtCache implements DeploymentCache {

  @Autowired
  CacheInitializer cacheInitializer;

  private HazelcastInstance hazelcastInstance;

  IMap<Long, DeploymentExecution> dpmtMap;
  IMap<String, DeploymentRfc> dpmtRfcMap;

  public static final String CACHE_QUORUM_NAME = "controllerQuorum";

  @PostConstruct
  public void init() {
    hazelcastInstance = cacheInitializer.getHazelcastInstance();
    dpmtMap = hazelcastInstance.getMap("controller.dpmt");
    dpmtRfcMap = hazelcastInstance.getMap("controller.dpmtrfc");
  }

  @Override
  public Map<Long, DeploymentExecution> getDeploymentMap() {
    return dpmtMap;
  }

  @Override
  public void updateDeploymentMap(long dpmtId, DeploymentExecution dpmtExec) {
    dpmtMap.put(dpmtId, dpmtExec);
  }

  @Override
  public DeploymentExecution getDeploymentFromMap(long dpmtId) {
    return dpmtMap.get(dpmtId);
  }

  @Override
  public void removeDeploymentFromMap(long dpmtId) {
    dpmtMap.delete(dpmtId);
  }

  @Override
  public void lockDpmt(long dpmtId) {
    dpmtMap.lock(dpmtId);
  }

  @Override
  public void unlockDpmt(long dpmtId) {
    dpmtMap.unlock(dpmtId);
  }

  @Override
  public DeploymentRfc getRfcFromMap(String key) {
    return dpmtRfcMap.get(key);
  }

  @Override
  public void updateRfcMap(String key, DeploymentRfc rfc) {
    dpmtRfcMap.put(key, rfc);
  }

  @Override
  public void removeRfcFromMap(String key) {
    dpmtRfcMap.delete(key);
  }

  @Override
  public void lockRfc(String key) {
    dpmtRfcMap.lock(key);
  }

  @Override
  public void unlockRfc(String key) {
    dpmtRfcMap.unlock(key);
  }
}
