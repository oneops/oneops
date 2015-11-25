package com.oneops.cms.dj.domain;

import com.oneops.cms.cm.domain.CmsLink;

public class CmsRfcLink extends CmsLink {
	private long rfcId;
	private String action;

	public long getRfcId() {
		return rfcId;
	}
	public void setRfcId(long rfcId) {
		this.rfcId = rfcId;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
}
