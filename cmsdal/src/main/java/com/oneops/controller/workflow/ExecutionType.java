package com.oneops.controller.workflow;

public enum ExecutionType {

  DEPLOYMENT("deployment", (short)100),
  PROCEDURE("procedure", (short)200);

  private String name;
  private short id;

  ExecutionType(String name, short id) {
    this.name = name;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public short getId() {
    return id;
  }
}
