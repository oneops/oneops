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


import com.oneops.ops.LogDataRequest;

/**
 * The Class GetLogDataRequest.
 */
public class GetLogDataRequest extends LogDataRequest {
	
	// can be null=all or debug, info, warn, error, fatal
	/**
	 * Gets the level list.
	 *
	 * @return the level list
	 */
	public String getLevelList() {
		return levelList;
	}
	
	/**
	 * Sets the level list.
	 *
	 * @param levelList the new level list
	 */
	public void setLevelList(String levelList) {
		this.levelList = levelList;
	}

	/**
	 * Gets the class list.
	 *
	 * @return the class list
	 */
	public String getClassList() {
		return classList;
	}
	
	/**
	 * Sets the class list.
	 *
	 * @param classList the new class list
	 */
	public void setClassList(String classList) {
		this.classList = classList;
	}
	
	
	private String classList;
	private String levelList;

}
