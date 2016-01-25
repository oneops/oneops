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

//by action or workorder id
/**
 * The Class GetLogDataByIdResponse.
 */
public class GetLogDataByIdResponse {
	
	/**
	 * Instantiates a new gets the log data by id response.
	 *
	 * @param req the req
	 */
	public GetLogDataByIdResponse (GetLogDataByIdRequest req) {
		setId(req.getId());
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

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(long id) {
		this.id = id;
	}

	private long id;
	private List<LogData> logData;

}
