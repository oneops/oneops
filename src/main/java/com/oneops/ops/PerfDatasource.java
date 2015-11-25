package com.oneops.ops;

public class PerfDatasource {

	// types
	public static final String COUNTER = "COUNTER";
	public static final String DERIVE = "DERIVE";
	public static final String GAUGE = "GAUGE";
	
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getHeartbeat() {
		return heartbeat;
	}
	public void setHeartbeat(int heartbeat) {
		this.heartbeat = heartbeat;
	}
	public Double getLast() {
		return last;
	}
	public void setLast(Double last) {
		this.last = last;
	}
	public double getMin() {
		return min;
	}
	public void setMin(double min) {
		this.min = min;
	}
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
	public double getPdp() {
		return pdp;
	}
	public void setPdp(double pdp) {
		this.pdp = pdp;
	}
	public double getInput() {
		return input;
	}
	public void setInput(double input) {
		this.input = input;
	}
	
	public String toLogString() {
		return "{ type:"+type+ " pdp:"+pdp+" last:"+last+" }";		
	}
	
	
	private String type;
	private int heartbeat;
	private Double last;
	private double min;
	private double max;
	private double pdp;
	private double input;
	
}
