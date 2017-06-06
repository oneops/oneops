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
package com.oneops.sensor.util;

public class ChannelState {
	private long lastProcessed;
	private long upSince;
	private long downSince;
	private boolean isUp;
	
	public long getLastProcessed() {
		return lastProcessed;
	}
	public void setLastProcessed(long lastProcessed) {
		this.lastProcessed = lastProcessed;
	}
	public long getUpSince() {
		return upSince;
	}
	public void setUpSince(long upSince) {
		this.upSince = upSince;
	}
	public long getDownSince() {
		return downSince;
	}
	public void setDownSince(long downSince) {
		this.downSince = downSince;
	}
	public boolean isUp() {
		return isUp;
	}
	public boolean isDown() {
		return !isUp;
	}
	public void setUp(boolean isUp) {
		this.isUp = isUp;
	}
}
