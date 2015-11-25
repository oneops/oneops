package com.oneops.ops.events;

import java.io.Serializable;

public class CiOpenEvent implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private String state;
	private long timestamp;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
}
