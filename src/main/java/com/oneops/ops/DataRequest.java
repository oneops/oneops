package com.oneops.ops;

/**
 * @author jpise
 * 
 * A generica class for any time series kind of data request for a ci
 */
public class DataRequest {
	
	private long ci_id;
	private long start;
	private long end;
	
	public long getCi_id() {
		return ci_id;
	}
	public void setCi_id(long ci_id) {
		this.ci_id = ci_id;
	}
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public long getEnd() {
		return end;
	}
	public void setEnd(long end) {
		this.end = end;
	}

}
