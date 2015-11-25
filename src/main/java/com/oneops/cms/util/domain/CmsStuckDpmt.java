package com.oneops.cms.util.domain;

import java.util.Date;

public class CmsStuckDpmt {
	
	private Long deploymentId;
	
	private Double stuckMinsBack;
	
	private Date stuckAt;
	
	public Long getDeploymentId() {
		return deploymentId;
	}
	public void setDeploymentId(Long deploymentId) {
		this.deploymentId = deploymentId;
	}
	public Double getStuckMinsBack() {
		return stuckMinsBack;
	}
	public void setStuckMinsBack(Double stuckMinsBack) {
		this.stuckMinsBack = stuckMinsBack;
	}
	public Date getStuckAt() {
		return stuckAt;
	}
	public void setStuckAt(Date stuckAt) {
		this.stuckAt = stuckAt;
	}


}
