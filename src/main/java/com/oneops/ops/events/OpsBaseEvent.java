package com.oneops.ops.events;

import com.oneops.sensor.events.BasicEvent;

public class OpsBaseEvent extends BasicEvent{

	private static final long serialVersionUID = 1L;
	private String state;
	private String ciState;
	private String name;
	private long count;
	private String type;
	private String source;

	//NEW |EXISTING
	private String status = Status.NEW;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public void setCiState(String ciState) {
		this.ciState = ciState;
	}
	public String getCiState() {
		return ciState;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}


}
