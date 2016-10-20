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
package com.oneops.sensor.util;

import java.util.List;
import java.util.Map;

import com.oneops.ops.events.CiOpenEvent;
import com.oneops.ops.events.OpsBaseEvent;

public class EventContext {

	private OpsBaseEvent event;
	private boolean isStateChanged;
	private Map<String, Long> stateCounterDelta;
	private List<CiOpenEvent> openEvents;
	private String oldState;
	private String newState;
	private String payload;

	public void emptyStateCounterDelta() {
		if (stateCounterDelta != null) {
			stateCounterDelta.clear();
		}
	}

	public EventContext(OpsBaseEvent event) {
		this.event = event;
	}

	public OpsBaseEvent getEvent() {
		return event;
	}

	public void setEvent(OpsBaseEvent event) {
		this.event = event;
	}

	public boolean isStateChanged() {
		return isStateChanged;
	}

	public void setStateChanged(boolean isStateChanged) {
		this.isStateChanged = isStateChanged;
	}

	public Map<String, Long> getStateCounterDelta() {
		return stateCounterDelta;
	}

	public void setStateCounterDelta(Map<String, Long> manifestStates) {
		this.stateCounterDelta = manifestStates;
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

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public List<CiOpenEvent> getOpenEvents() {
		return openEvents;
	}

	public void setOpenEvents(List<CiOpenEvent> openEvents) {
		this.openEvents = openEvents;
	}
	
}
