package com.oneops.cms.dj.domain;

import java.util.Date;

public class CmsDpmtStateChangeEvent {
	
	private long eventId;
	private long deploymentId;
	private String oldState;
	private String newState;
	private String description;
	private String comments;
	private String ops;
	private String updatedBy;
	private Date updated;
	
	public long getEventId() {
		return eventId;
	}
	public void setEventId(long eventId) {
		this.eventId = eventId;
	}
	public long getDeploymentId() {
		return deploymentId;
	}
	public void setDeploymentId(long deploymentId) {
		this.deploymentId = deploymentId;
	}
	public String getOldState() {
		return oldState;
	}
	public void setOldState(String oldState) {
		this.oldState = oldState;
	}
	public String getNewState() {
		return newState;
	}
	public void setNewState(String newState) {
		this.newState = newState;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getOps() {
		return ops;
	}
	public void setOps(String ops) {
		this.ops = ops;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	public Date getUpdated() {
		return updated;
	}
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	
}
