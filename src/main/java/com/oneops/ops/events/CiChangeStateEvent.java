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

	private Map<String, Long> componentStatesCounters;
	
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
	public Map<String, Long> getComponentStatesCounters() {
		return componentStatesCounters;
	}
	public void setComponentStatesCounters(Map<String, Long> componentStatesCounters) {
		this.componentStatesCounters = componentStatesCounters;
	}

	public boolean hasStateChanged(){
		boolean hasStateChanged = true;
		if (newState != null && newState.equals(oldState)) {
			hasStateChanged = false;
		}
		return hasStateChanged;
	}	
}
