package com.oneops;

public class Cloud {

  private String id; 
  private int priority;
  private int deploymentorder;
  private int scalepercentage;
  private String adminstatus;
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public int getDeploymentorder() {
    return deploymentorder;
  }

  public void setDeploymentorder(int deploymentorder) {
    this.deploymentorder = deploymentorder;
  }

  public int getScalepercentage() {
    return scalepercentage;
  }

  public void setScalepercentage(int scalepercentage) {
    this.scalepercentage = scalepercentage;
  }

public String getAdminstatus() {
	return adminstatus;
}

public void setAdminstatus(String adminstatus) {
	this.adminstatus = adminstatus;
}

}
