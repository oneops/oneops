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
package com.oneops.sensor.thresholds;

public class Threshold {
	long manifestId;
	long crc;
	String source;
	String historics;
	String thresholdJson;
	boolean isHeartbeat;
	String hbDuration;

	public boolean isHeartbeat() {
		return isHeartbeat;
	}
	public void setHeartbeat(boolean isHeartbeat) {
		this.isHeartbeat = isHeartbeat;
	}
	public String getHbDuration() {
		return hbDuration;
	}
	public void setHbDuration(String hbDuration) {
		this.hbDuration = hbDuration;
	}
	public long getManifestId() {
		return manifestId;
	}
	public void setManifestId(long manifestId) {
		this.manifestId = manifestId;
	}
	public long getCrc() {
		return crc;
	}
	public void setCrc(long crc) {
		this.crc = crc;
	}
	public String getThresholdJson() {
		return thresholdJson;
	}
	public void setThresholdJson(String thresholdJson) {
		this.thresholdJson = thresholdJson;
	}
	public String getHistorics() {
		return historics;
	}
	public void setHistorics(String historics) {
		this.historics = historics;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
}
