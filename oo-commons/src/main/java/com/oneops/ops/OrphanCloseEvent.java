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
package com.oneops.ops;

public class OrphanCloseEvent {

	private long ciId;
	private long manifestId;
	private String name;
	private String openEventPayload;
	
	public OrphanCloseEvent() {

	}
	
	public OrphanCloseEvent(long ciId, long manifestId, String name, String openEventPayload) {
		this.ciId = ciId;
		this.manifestId = manifestId;
		this.name = name;
		this.openEventPayload = openEventPayload;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOpenEventPayload() {
		return openEventPayload;
	}
	public void setOpenEventPayload(String openEventPayload) {
		this.openEventPayload = openEventPayload;
	}
	
}
