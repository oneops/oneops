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
package com.oneops.sensor.domain;

/**
 * The Class SensorStatement.
 */
public class SensorStatement {
	private String stmtName;
	private String stmtText;
	private String listenerName;
	
	/**
	 * Instantiates a new sensor statement.
	 *
	 * @param stmtName the stmt name
	 * @param stmtText the stmt text
	 * @param listenerName the listener name
	 */
	public SensorStatement(String stmtName, String stmtText, String listenerName) {
		super();
		this.stmtName = stmtName;
		this.stmtText = stmtText;
		this.listenerName = listenerName;
	}

	/**
	 * Instantiates a new sensor statement.
	 */
	public SensorStatement() {
		super();
	}
	
	/**
	 * Gets the stmt name.
	 *
	 * @return the stmt name
	 */
	public String getStmtName() {
		return stmtName;
	}
	
	/**
	 * Sets the stmt name.
	 *
	 * @param stmtName the new stmt name
	 */
	public void setStmtName(String stmtName) {
		this.stmtName = stmtName;
	}
	
	/**
	 * Gets the stmt text.
	 *
	 * @return the stmt text
	 */
	public String getStmtText() {
		return stmtText;
	}
	
	/**
	 * Sets the stmt text.
	 *
	 * @param stmtText the new stmt text
	 */
	public void setStmtText(String stmtText) {
		this.stmtText = stmtText;
	}
	
	/**
	 * Gets the listener name.
	 *
	 * @return the listener name
	 */
	public String getListenerName() {
		return listenerName;
	}
	
	/**
	 * Sets the listener name.
	 *
	 * @param listenerName the new listener name
	 */
	public void setListenerName(String listenerName) {
		this.listenerName = listenerName;
	}
}
