package com.oneops.controller.workflow;

public interface ExecutionContext {

  public int getMaxSteps();
  public int getCurrentStep();
  public long getExecutionId();
  public String getType();
  public void setCompleted(boolean completed);

}
