package com.oneops.cms.domain;

public class WoComponent extends BaseInstance {

  private long rfcId;
  private String rfcAction;
  private String nsPath;
  private int execOrder;
  private String ciGoid;

  public long getRfcId() {
    return rfcId;
  }

  public void setRfcId(long rfcId) {
    this.rfcId = rfcId;
  }

  public String getRfcAction() {
    return rfcAction;
  }

  public void setRfcAction(String rfcAction) {
    this.rfcAction = rfcAction;
  }

  public String getNsPath() {
    return nsPath;
  }

  public void setNsPath(String nsPath) {
    this.nsPath = nsPath;
  }

  public int getExecOrder() {
    return execOrder;
  }

  public void setExecOrder(int execOrder) {
    this.execOrder = execOrder;
  }

  public String getCiGoid() {
    return ciGoid;
  }

  public void setCiGoid(String ciGoid) {
    this.ciGoid = ciGoid;
  }
}
