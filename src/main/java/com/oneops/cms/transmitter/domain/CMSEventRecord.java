package com.oneops.cms.transmitter.domain;

import java.util.Date;

public class CMSEventRecord {

	private long eventId; 
	private long sourcePk; 
	private String sourceName; 
	private String eventType; 
	private Date created;
	
	
	public long getEventId() {
		return eventId;
	}
	public void setEventId(long eventId) {
		this.eventId = eventId;
	}
	public long getSourcePk() {
		return sourcePk;
	}
	public void setSourcePk(long sourcePk) {
		this.sourcePk = sourcePk;
	}
	public String getSourceName() {
		return sourceName;
	}
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	
}
