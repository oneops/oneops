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
package com.oneops.cms.util;

import com.oneops.cms.util.domain.AttrQueryCondition;

import java.util.List;

/**
 * The Class QueryConditionMapper.
 */
public class QueryConditionMapper {
	
	/**
	 * Convert conditions.
	 *
	 * @param aqc the aqc
	 */
	public void convertConditions(AttrQueryCondition aqc) {
		if ("eq".equalsIgnoreCase(aqc.getCondition())) {
			aqc.setCondition("=");
		} else if ("neq".equalsIgnoreCase(aqc.getCondition())) {
			aqc.setCondition("!=");
		} 
	}
	
	/**
	 * Convert conditions.
	 *
	 * @param aqcs the aqcs
	 */
	public void convertConditions(List<AttrQueryCondition> aqcs) {
		if (aqcs == null) return;
		for (AttrQueryCondition aqc : aqcs) {
			convertConditions(aqc);
		}
	}
}
