package com.oneops.workflow;

import java.io.Serializable;

public class WorkflowMessage implements Serializable {

  private long dpmtId;

  public long getDpmtId() {
    return dpmtId;
  }

  public void setDpmtId(long dpmtId) {
    this.dpmtId = dpmtId;
  }

}
