package com.oneops.search.domain;

import org.springframework.data.elasticsearch.annotations.Document;

import com.oneops.cms.dj.domain.CmsDeployment;

@Document(indexName = "cms")
public class CmsDeploymentSearch extends CmsDeployment {

	private static final long serialVersionUID = 1L;

	private double activeDuration;
	private double failedDuration;
	private double pausedDuration;
	private double pendingDuration;

	private String activeStartTS;
	private String activeEndTS;
	private String pausedStartTS;
	private String pausedEndTS;
	private String pendingStartTS;
	private String pendingEndTS;
	private String failedStartTS;
	private String failedEndTS;

	private double totalTime;
	private int retryCount;
	private int pauseCnt;
	private int failureCnt;
	
	public double getActiveDuration() {
		return activeDuration;
	}
	public void setActiveDuration(double activeDuration) {
		this.activeDuration = activeDuration;
	}
	public double getFailedDuration() {
		return failedDuration;
	}
	public void setFailedDuration(double failedDuration) {
		this.failedDuration = failedDuration;
	}
	public double getPausedDuration() {
		return pausedDuration;
	}
	public void setPausedDuration(double pausedDuration) {
		this.pausedDuration = pausedDuration;
	}
	public String getActiveStartTS() {
		return activeStartTS;
	}
	public void setActiveStartTS(String activeStartTS) {
		this.activeStartTS = activeStartTS;
	}
	public String getActiveEndTS() {
		return activeEndTS;
	}
	public void setActiveEndTS(String activeEndTS) {
		this.activeEndTS = activeEndTS;
	}
	public String getPausedStartTS() {
		return pausedStartTS;
	}
	public void setPausedStartTS(String pausedStartTS) {
		this.pausedStartTS = pausedStartTS;
	}
	public String getPausedEndTS() {
		return pausedEndTS;
	}
	public void setPausedEndTS(String pausedEndTS) {
		this.pausedEndTS = pausedEndTS;
	}
	public String getFailedStartTS() {
		return failedStartTS;
	}
	public void setFailedStartTS(String failedStartTS) {
		this.failedStartTS = failedStartTS;
	}
	public String getFailedEndTS() {
		return failedEndTS;
	}
	public void setFailedEndTS(String failedEndTS) {
		this.failedEndTS = failedEndTS;
	}
	public double getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}
	public int getRetryCount() {
		return retryCount;
	}
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
	public int getPauseCnt() {
		return pauseCnt;
	}
	public void setPauseCnt(int pauseCnt) {
		this.pauseCnt = pauseCnt;
	}
	public int getFailureCnt() {
		return failureCnt;
	}
	public void setFailureCnt(int failureCnt) {
		this.failureCnt = failureCnt;
	}
	public String getPendingStartTS() {
		return pendingStartTS;
	}
	public void setPendingStartTS(String pendingStartTS) {
		this.pendingStartTS = pendingStartTS;
	}
	public String getPendingEndTS() {
		return pendingEndTS;
	}
	public void setPendingEndTS(String pendingEndTS) {
		this.pendingEndTS = pendingEndTS;
	}
	public double getPendingDuration() {
		return pendingDuration;
	}
	public void setPendingDuration(double pendingDuration) {
		this.pendingDuration = pendingDuration;
	}

}
