package com.oneops.controller.workflow;

import java.io.Serializable;

public class DeploymentStep implements Serializable {

  private int step;
  private boolean isCompleted;
  private int woCount;
  private int woFailedDispatchCount;
  private long scheduledTs;

  public DeploymentStep(int step, int woCount, long scheduledTs) {
    this.step = step;
    this.woCount = woCount;
    this.scheduledTs = scheduledTs;
    isCompleted = false;
  }

  public DeploymentStep(int step) {
    this(step, 0, 0);
  }

  public DeploymentStep() {

  }

  public int getStep() {
    return step;
  }

  public void setStep(int step) {
    this.step = step;
  }

  public boolean isCompleted() {
    return isCompleted;
  }

  public void setCompleted(boolean completed) {
    isCompleted = completed;
  }

  public int getWoCount() {
    return woCount;
  }

  public void setWoCount(int woCount) {
    this.woCount = woCount;
  }

  public long getScheduledTs() {
    return scheduledTs;
  }

  public void setScheduledTs(long scheduledTs) {
    this.scheduledTs = scheduledTs;
  }

  public int getWoFailedDispatchCount() {
    return woFailedDispatchCount;
  }

  public void setWoFailedDispatchCount(int woFailedDispatchCount) {
    this.woFailedDispatchCount = woFailedDispatchCount;
  }

  public String toString() {
    return "DeploymentStep : [step : " + step + ", isCompleted : " + isCompleted + ", woCount : " + woCount + "]";
  }
}
