package com.oneops.controller.workflow;

import java.util.Map;

public interface DeploymentCache {

  public Map<Long, DeploymentExecution> getDeploymentMap();

  public void updateDeploymentMap(long dpmtId, DeploymentExecution dpmtExec);

  public DeploymentExecution getDeploymentFromMap(long dpmtId);

  public void removeDeploymentFromMap(long dpmtId);

  public void lockDpmt(long dpmtId);

  public void unlockDpmt(long dpmtId);

  public DeploymentRfc getRfcFromMap(String key);

  public void updateRfcMap(String key, DeploymentRfc rfc);

  public void removeRfcFromMap(String key);

  public void lockRfc(String key);

  public void unlockRfc(String key);

}
