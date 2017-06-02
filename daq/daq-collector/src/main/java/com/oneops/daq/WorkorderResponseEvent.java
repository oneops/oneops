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
package com.oneops.daq;

import com.oneops.cms.simple.domain.CmsWorkOrderSimple;

/**
 * The Class WorkorderResponseEvent.
 */
class WorkorderResponseEvent {

	/**
	 * Gets the result code.
	 *
	 * @return the result code
	 */
	public int getResultCode() {
		return resultCode;
	}
	
	/**
	 * Sets the result code.
	 *
	 * @param resultCode the new result code
	 */
	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
	
	/**
	 * Gets the jMS correlation id.
	 *
	 * @return the jMS correlation id
	 */
	public String getJMSCorrelationID() {
		return JMSCorrelationID;
	}
	
	/**
	 * Sets the jMS correlation id.
	 *
	 * @param jMSCorrelationID the new jMS correlation id
	 */
	public void setJMSCorrelationID(String jMSCorrelationID) {
		JMSCorrelationID = jMSCorrelationID;
	}
	
	/**
	 * Gets the monitor config.
	 *
	 * @return the monitor config
	 */
	public String getMonitorConfig() {
		return monitorConfig;
	}
	
	/**
	 * Sets the monitor config.
	 *
	 * @param monitorConfig the new monitor config
	 */
	public void setMonitorConfig(String monitorConfig) {
		this.monitorConfig = monitorConfig;
	}
	
	/**
	 * Gets the response workorder.
	 *
	 * @return the response workorder
	 */
	public CmsWorkOrderSimple getResponseWorkorder() {
		return responseWorkorder;
	}
	
	/**
	 * Sets the response workorder.
	 *
	 * @param responseWorkorder the new response workorder
	 */
	public void setResponseWorkorder(CmsWorkOrderSimple responseWorkorder) {
		this.responseWorkorder = responseWorkorder;
	}
	
	/**
	 * Gets the log config.
	 *
	 * @return the log config
	 */
	public String getLogConfig() {
		return logConfig;
	}
	
	/**
	 * Sets the log config.
	 *
	 * @param logConfig the new log config
	 */
	public void setLogConfig(String logConfig) {
		this.logConfig = logConfig;
	}
	private int resultCode;
	private String JMSCorrelationID;
	private String monitorConfig;
	private String logConfig;
	CmsWorkOrderSimple responseWorkorder;

}
