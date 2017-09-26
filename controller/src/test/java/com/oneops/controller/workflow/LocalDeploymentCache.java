package com.oneops.controller.workflow;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class LocalDeploymentCache implements DeploymentCache {

  Map<Long, DeploymentExecution> dpmtMap = new HashMap<>();

  Map<String, DeploymentRfc> dpmtRfcMap = new ConcurrentHashMap<>();

  Map<Long, Lock> dpmtLockMap = new HashMap<>();

  Map<String, Lock> dpmtRfcLockMap = new HashMap<>();

  @Override
  public Map<Long, DeploymentExecution> getDeploymentMap() {
    return dpmtMap;
  }

  @Override
  public void updateDeploymentMap(long dpmtId, DeploymentExecution dpmtExec) {
    if (!dpmtLockMap.containsKey(dpmtId)) {
      dpmtLockMap.put(dpmtId, new ReentrantLock());
    }
    dpmtMap.put(dpmtId, dpmtExec);

  }

  @Override
  public DeploymentExecution getDeploymentFromMap(long dpmtId) {
    return dpmtMap.get(dpmtId);
  }

  @Override
  public void removeDeploymentFromMap(long dpmtId) {
    dpmtMap.remove(dpmtId);
    dpmtLockMap.remove(dpmtId);
  }

  @Override
  public DeploymentRfc getRfcFromMap(String key) {
    return dpmtRfcMap.get(key);
  }

  @Override
  public void updateRfcMap(String key, DeploymentRfc rfc) {
    if (!dpmtRfcMap.containsKey(key)) {
      dpmtRfcLockMap.put(key, new ReentrantLock());
    }
    dpmtRfcMap.put(key, rfc);
  }

  @Override
  public void removeRfcFromMap(String key) {
    dpmtRfcMap.remove(key);
    dpmtRfcLockMap.remove(key);
  }

  @Override
  public void lockDpmt(long dpmtId) {
    dpmtLockMap.get(dpmtId).lock();
  }

  @Override
  public void unlockDpmt(long dpmtId) {
    dpmtLockMap.get(dpmtId).unlock();
  }

  @Override
  public void lockRfc(String key) {
    dpmtRfcLockMap.get(key).lock();
  }

  @Override
  public void unlockRfc(String key) {
    dpmtRfcLockMap.get(key).unlock();
  }
}
