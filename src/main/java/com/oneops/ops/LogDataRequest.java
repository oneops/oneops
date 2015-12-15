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
package com.oneops.ops;
/**
 * Base class with common fields and getter , setters
 */
public class LogDataRequest extends DataRequest {

	private long zone_ci_id;

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
}