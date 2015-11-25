package com.oneops.cms.transmitter.domain;

import java.io.Serializable;
import java.util.Date;

public class PubStatus implements Serializable {
	private boolean isRunning;
	private int queueBacklog;
	private int ciEventsQueueBacklog;
	private Date lastRun;
	
	public boolean getIsRunning() {
		return isRunning;
	}
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	public int getQueueBacklog() {
		return queueBacklog;
	}
	public void setQueueBacklog(int queueBacklog) {
		this.queueBacklog = queueBacklog;
	}
	public int getCiEventsQueueBacklog() {
		return ciEventsQueueBacklog;
	}
	public void setCiEventsQueueBacklog(int ciEventsQueueBacklog) {
		this.ciEventsQueueBacklog = ciEventsQueueBacklog;
	}
	public Date getLastRun() {
		return lastRun;
	}
	public void setLastRun(Date lastRun) {
		this.lastRun = lastRun;
	}
}
