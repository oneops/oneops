package com.oneops.controller.workflow;

import java.io.Serializable;

public class DeploymentRfc implements Serializable {

  private long rfcId;

  private String state;

  private int step;

  public DeploymentRfc(long rfcId, String state, int step) {
    this.rfcId = rfcId;
    this.state = state;
    this.step = step;
  }

  public long getRfcId() {
    return rfcId;
  }

  public void setRfcId(long rfcId) {
    this.rfcId = rfcId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public int getStep() {
    return step;
  }

  public void setStep(int step) {
    this.step = step;
  }

}
