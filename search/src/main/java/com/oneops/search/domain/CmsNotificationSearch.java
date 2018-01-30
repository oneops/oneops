/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
package com.oneops.search.domain;

import java.util.Map;

import com.oneops.notification.NotificationMessage;

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
	public void setPayload(Map<String,Object> payload){
		this.getPayload().putAll(payload);
	}

}
