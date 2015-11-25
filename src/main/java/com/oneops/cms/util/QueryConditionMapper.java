package com.oneops.cms.util;

import java.util.List;

import com.oneops.cms.util.domain.AttrQueryCondition;

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
		for (AttrQueryCondition aqc : aqcs) {
			convertConditions(aqc);
		}
	}
}
