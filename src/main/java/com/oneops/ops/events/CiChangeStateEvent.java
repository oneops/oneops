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
package com.oneops.ops.events;

import java.io.Serializable;
import java.util.Map;

public class CiChangeStateEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private long ciId;
	private String cloudName;
	private String oldState;
	private String newState;
	private String payLoad;
	private long timestamp;

	private Map<String, Integer> componentStatesCounters;
	
	public long getCiId() {
		return ciId;
	}
	public void setCiId(long ciId) {
		this.ciId = ciId;
	}
	public String getCloudName() {
		return cloudName;
	}
	public void setCloudName(String cloudName) {
		this.cloudName = cloudName;
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
	public String getPayLoad() {
		return payLoad;
	}
	public void setPayLoad(String payLoad) {
		this.payLoad = payLoad;
	}
	public Map<String, Integer> getComponentStatesCounters() {
		return componentStatesCounters;
	}
	public void setComponentStatesCounters(Map<String, Integer> componentStatesCounters) {
		this.componentStatesCounters = componentStatesCounters;
	}

	public boolean hasStateChanged(){
		boolean hasStateChanged = true;
		if (newState != null && newState.equals(oldState)) {
			hasStateChanged = false;
		}
		return hasStateChanged;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}	
}
