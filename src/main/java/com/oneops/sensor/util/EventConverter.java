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

import com.oneops.ops.events.OpsBaseEvent;
import com.oneops.ops.events.OpsCloseEvent;
import com.oneops.ops.events.OpsEvent;

/**
 * The Class EventConverter.
 */
public class EventConverter {
	
	/**
	 * Convert.
	 *
	 * @param oEvent the o event
	 * @return the ops base event
	 */
	public static OpsBaseEvent convert(OpsEvent oEvent) {
		OpsBaseEvent bEvent = new OpsBaseEvent();
		bEvent.setCiId(oEvent.getCiId());
		bEvent.setBucket(oEvent.getBucket());
		bEvent.setCiState(oEvent.getCiState());
		bEvent.setName(oEvent.getName());
		bEvent.setManifestId(oEvent.getManifestId());
		bEvent.setSource(oEvent.getSource());
		bEvent.setState(oEvent.getState());
		bEvent.setType(oEvent.getType());
		bEvent.setMetrics(oEvent.getMetrics());
		bEvent.setTimestamp(oEvent.getTimestamp());
		bEvent.setCount(oEvent.getCount());
		bEvent.setStatus(oEvent.getStatus());
		bEvent.setCoolOff(oEvent.getCoolOff());
		return bEvent;
	}
	
	/**
	 * Convert.
	 *
	 * @param oEvent the o event
	 * @return the ops base event
	 */
	public static OpsBaseEvent convert(OpsCloseEvent oEvent) {
		OpsBaseEvent bEvent = new OpsBaseEvent();
		bEvent.setCiId(oEvent.getCiId());
		bEvent.setBucket(oEvent.getBucket());
		bEvent.setCiState(oEvent.getCiState());
		bEvent.setName(oEvent.getName());
		bEvent.setManifestId(oEvent.getManifestId());
		bEvent.setSource(oEvent.getSource());
		bEvent.setState(oEvent.getState());
		bEvent.setType(oEvent.getType());
		bEvent.setMetrics(oEvent.getMetrics());		
		bEvent.setTimestamp(oEvent.getTimestamp());
		bEvent.setCount(oEvent.getCount());
		bEvent.setStatus(oEvent.getStatus());
		return bEvent;
	}
	
	
}
