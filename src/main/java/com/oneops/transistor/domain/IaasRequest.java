package com.oneops.transistor.domain;

import java.io.Serializable;
import java.util.List;

public class IaasRequest   implements Serializable{

	private static final long serialVersionUID = 1L;

	private List<IaasBindingTriplet> iaasList;
	
	public void setIaasList(List<IaasBindingTriplet> iaasList) {
		this.iaasList = iaasList;
	}
	public List<IaasBindingTriplet> getIaasList() {
		return iaasList;
	}
}
