package com.oneops.sensor.client;

import java.util.List;

import com.oneops.cms.simple.domain.CmsRfcCISimple;

public class MonitorRequest {
	private long ciId;
	private long manifestId;
	private boolean monitoringEnabled;
	private String action;
	private List<CmsRfcCISimple> monitors;
	
	public boolean isMonitoringEnabled() {
		return monitoringEnabled;
	}
	public void setMonitoringEnabled(boolean monitoringEnabled) {
		this.monitoringEnabled = monitoringEnabled;
	}
	public long getCiId() {
		return ciId;
	}
	public void setCiId(long ciId) {
		this.ciId = ciId;
	}
	public long getManifestId() {
		return manifestId;
	}
	public void setManifestId(long manifestId) {
		this.manifestId = manifestId;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public List<CmsRfcCISimple> getMonitors() {
		return monitors;
	}
	public void setMonitors(List<CmsRfcCISimple> monitors) {
		this.monitors = monitors;
	}
	
}
