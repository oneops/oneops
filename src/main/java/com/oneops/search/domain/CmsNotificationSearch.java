package com.oneops.search.domain;

import java.util.Map;

import com.oneops.antenna.domain.NotificationMessage;

public class CmsNotificationSearch extends NotificationMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String ts;
	
	private String hypervisor;

	public String getHypervisor() {
		return hypervisor;
	}

	public void setHypervisor(String hypervisor) {
		this.hypervisor = hypervisor;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}
	
	/**
	 * 
	 * @param payload
	 */
	public void setPayload(Map<String,String> payload){
		this.getPayload().putAll(payload);
	}

}
