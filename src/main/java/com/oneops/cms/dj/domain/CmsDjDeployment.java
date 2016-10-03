package com.oneops.cms.dj.domain;

public class CmsDjDeployment extends CmsDeployment implements CmsDjBase {

	private static final long serialVersionUID = 1L;
	
	private long parentReleaseId;

	public long getParentReleaseId() {
		return parentReleaseId;
	}

	public void setParentReleaseId(long parentReleaseId) {
		this.parentReleaseId = parentReleaseId;
	}

}
