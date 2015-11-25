package com.oneops.search.domain;

import org.springframework.data.elasticsearch.annotations.Document;

import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;

@Document(indexName = "cms")
public class CmsOpsProcedureSearch extends CmsOpsProcedure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private double activeDuration;
	private double failedDuration;

	private String activeStartTS;
	private String activeEndTS;
	private String failedStartTS;
	private String failedEndTS;

	private double totalTime;
	private int retryCount;
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

	public int getFailureCnt() {
		return failureCnt;
	}

	public void setFailureCnt(int failureCnt) {
		this.failureCnt = failureCnt;
	}

}
