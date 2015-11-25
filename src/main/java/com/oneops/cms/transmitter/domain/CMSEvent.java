package com.oneops.cms.transmitter.domain;

import java.util.HashMap;
import java.util.Map;

public class CMSEvent {
	private long eventId;
	private Map<String,String> headers;
	private Object payload;
	
	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	public void addHeaders(String name, String value) {
		if ( this.headers == null ) {
			this.headers = new HashMap<String,String>();
		}
		this.headers.put(name, value);	
	}
	
	public Object getPayload() {
		return payload;
	}
	public void setPayload(Object payload) {
		this.payload = payload;
	}

	public long getEventId() {
		return eventId;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}
	
}
