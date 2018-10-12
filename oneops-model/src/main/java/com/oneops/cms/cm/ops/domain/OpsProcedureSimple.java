package com.oneops.cms.cm.ops.domain;

public class OpsProcedureSimple {

  private long procedureId;
  private String nsPath;
  private OpsProcedureState state;

  public long getProcedureId() {
    return procedureId;
  }

  public OpsProcedureSimple procedureId(long procedureId) {
    this.procedureId = procedureId;
    return this;
  }

  public String getNsPath() {
    return nsPath;
  }

  public OpsProcedureSimple nsPath(String nsPath) {
    this.nsPath = nsPath;
    return this;
  }

  public OpsProcedureState getState() {
    return state;
  }

  public OpsProcedureSimple state(OpsProcedureState state) {
    this.state = state;
    return this;
  }
}
