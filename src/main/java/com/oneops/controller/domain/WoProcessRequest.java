/*******************************************************************************
 *
 * Copyright 2015 Walmart, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.oneops.controller.domain;

import com.oneops.cms.simple.domain.CmsWorkOrderSimple;

/**
 * The Class WoProcessRequest.
 */
public class WoProcessRequest {

	private String processId;
	private CmsWorkOrderSimple wo;

	/**
	 * Gets the process id.
	 *
	 * @return the process id
	 */
	public String getProcessId() {
		return processId;
	}
	
	/**
	 * Sets the process id.
	 *
	 * @param processId the new process id
	 */
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	
	/**
	 * Gets the wo.
	 *
	 * @return the wo
	 */
	public CmsWorkOrderSimple getWo() {
		return wo;
	}
	
	/**
	 * Sets the wo.
	 *
	 * @param wo the new wo
	 */
	public void setWo(CmsWorkOrderSimple wo) {
		this.wo = wo;
	}
}
