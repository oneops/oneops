package com.oneops.cms.util.domain;

import java.util.List;

public class CmsStuckDpmtCollection {
	
	List<CmsStuckDpmt> cmsStuckDpmts;
	
	List<CmsStuckDpmt> inProgressStuckDpmts;
	
	List<CmsStuckDpmt> pausedStuckDpmts;

	public List<CmsStuckDpmt> getCmsStuckDpmts() {
		return cmsStuckDpmts;
	}

	public void setCmsStuckDpmts(List<CmsStuckDpmt> cmsStuckDpmts) {
		this.cmsStuckDpmts = cmsStuckDpmts;
	}

	public List<CmsStuckDpmt> getInProgressStuckDpmts() {
		return inProgressStuckDpmts;
	}

	public void setInProgressStuckDpmts(List<CmsStuckDpmt> inProgressStuckDpmts) {
		this.inProgressStuckDpmts = inProgressStuckDpmts;
	}

	public List<CmsStuckDpmt> getPausedStuckDpmts() {
		return pausedStuckDpmts;
	}

	public void setPausedStuckDpmts(List<CmsStuckDpmt> pausedStuckDpmts) {
		this.pausedStuckDpmts = pausedStuckDpmts;
	}
	
	
	
}
