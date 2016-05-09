package com.oneops.sensor.domain;

import com.oneops.ops.events.OpsEvent;

public class OpenEvent {
	
	private OpsEvent opsEvent;
	private long timestamp;
	
	public OpsEvent getOpsEvent() {
		return opsEvent;
	}
	public void setOpsEvent(OpsEvent opsEvent) {
		this.opsEvent = opsEvent;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
