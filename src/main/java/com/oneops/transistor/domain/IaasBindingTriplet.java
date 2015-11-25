package com.oneops.transistor.domain;

import java.io.Serializable;

public class IaasBindingTriplet implements Serializable{
	private static final long serialVersionUID = 1L;
	
	String serviceType;
	long tmplIaasId;
	long bindingId;

	public String getServiceType() {
		return serviceType;
	}
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	public long getTmplIaasId() {
		return tmplIaasId;
	}
	public void setTmplIaasId(long tmplIaasId) {
		this.tmplIaasId = tmplIaasId;
	}
	public long getBindingId() {
		return bindingId;
	}
	public void setBindingId(long bindingId) {
		this.bindingId = bindingId;
	}

}