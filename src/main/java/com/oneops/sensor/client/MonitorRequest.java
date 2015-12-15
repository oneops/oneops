/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
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
