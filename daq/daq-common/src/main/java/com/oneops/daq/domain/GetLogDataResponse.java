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
package com.oneops.daq.domain;

import java.util.List;

/**
 * The Class GetLogDataResponse.
 */
public class GetLogDataResponse {
	
	/**
	 * Instantiates a new gets the log data response.
	 *
	 * @param req the req
	 */
	public GetLogDataResponse (GetLogDataRequest req) {
		ci_id = req.getCi_id();
		zone_ci_id = req.getZone_ci_id();
	}
	
	/**
	 * Gets the ci_id.
	 *
	 * @return the ci_id
	 */
	public long getCi_id() {
		return ci_id;
	}
	
	/**
	 * Sets the ci_id.
	 *
	 * @param ci_id the new ci_id
	 */
	public void setCi_id(long ci_id) {
		this.ci_id = ci_id;
	}
	
	/**
	 * Gets the zone_ci_id.
	 *
	 * @return the zone_ci_id
	 */
	public long getZone_ci_id() {
		return zone_ci_id;
	}
	
	/**
	 * Sets the zone_ci_id.
	 *
	 * @param zone_ci_id the new zone_ci_id
	 */
	public void setZone_ci_id(long zone_ci_id) {
		this.zone_ci_id = zone_ci_id;
	}
	
	/**
	 * Gets the log data.
	 *
	 * @return the log data
	 */
	public List<LogData> getLogData() {
		return logData;
	}

	/**
	 * Sets the log data.
	 *
	 * @param logData the new log data
	 */
	public void setLogData(List<LogData> logData) {
		this.logData = logData;
	}

	private long ci_id;
	private long zone_ci_id;
	private List<LogData> logData;

}
