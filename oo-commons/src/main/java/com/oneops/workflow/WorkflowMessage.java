package com.oneops.workflow;

import java.io.Serializable;

public class WorkflowMessage implements Serializable {

  private long executionId;
  private String type;

  public long getExecutionId() {
    return executionId;
  }

  public void setExecutionId(long executionId) {
    this.executionId = executionId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
