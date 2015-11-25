package com.oneops.sensor.util;

public class ChannelState {
	private long lastProcessed;
	private long upSince;
	private long downSince;
	private boolean isUp;
	
	public long getLastProcessed() {
		return lastProcessed;
	}
	public void setLastProcessed(long lastProcessed) {
		this.lastProcessed = lastProcessed;
	}
	public long getUpSince() {
		return upSince;
	}
	public void setUpSince(long upSince) {
		this.upSince = upSince;
	}
	public long getDownSince() {
		return downSince;
	}
	public void setDownSince(long downSince) {
		this.downSince = downSince;
	}
	public boolean isUp() {
		return isUp;
	}
	public void setUp(boolean isUp) {
		this.isUp = isUp;
	}
}
