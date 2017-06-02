package com.oneops.sensor.domain;

import java.io.Serializable;

import com.oneops.sensor.events.PerfEvent;

public class DelayedPerfEvent implements Serializable {
	/**
	 * this is event that should be sent by daq perf sink 
	 */
	private static final long serialVersionUID = 1L;
	
	private int delay;
	
	private PerfEvent perfEvent;

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public PerfEvent getPerfEvent() {
		return perfEvent;
	}

	public void setPerfEvent(PerfEvent perfEvent) {
		this.perfEvent = perfEvent;
	}

}
