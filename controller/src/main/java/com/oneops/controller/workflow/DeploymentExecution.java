package com.oneops.controller.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DeploymentExecution implements Serializable {

  private long deploymentId;

  private long updatedTime;

  private int currentStep;

  private Map<Integer, DeploymentStep> stepMap = new HashMap<>();

  public Map<Integer, DeploymentStep> getStepMap() {
    return stepMap;
  }

  public void setStepMap(Map<Integer, DeploymentStep> stepMap) {
    this.stepMap = stepMap;
  }

  public long getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(long deploymentId) {
    this.deploymentId = deploymentId;
  }

  public long getUpdatedTime() {
    return updatedTime;
  }

  public void setUpdatedTime(long updatedTime) {
    this.updatedTime = updatedTime;
  }

  public int getCurrentStep() {
    return currentStep;
  }

  public void setCurrentStep(int currentStep) {
    this.currentStep = currentStep;
  }

  public String toString() {
    return "DeploymentExec : [dpmtId : " + deploymentId + ", currentStep : " + currentStep + ", stepMap : " + stepMap + "]";
  }

}
