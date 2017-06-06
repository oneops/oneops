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
package com.oneops.ops.events;

import com.oneops.sensor.events.BasicEvent;

public class OpsBaseEvent extends BasicEvent{

	private static final long serialVersionUID = 1L;
	private String state;
	private String ciState;
	private String name;
	private long count;
	private String type;
	private int coolOff;

	//NEW |EXISTING
	private String status = Status.NEW;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public void setCiState(String ciState) {
		this.ciState = ciState;
	}
	public String getCiState() {
		return ciState;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getCoolOff() {
		return coolOff;
	}
	public void setCoolOff(int coolOff) {
		this.coolOff = coolOff;
	}


}
