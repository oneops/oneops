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
package com.oneops.transistor.util;

import java.util.Map;

import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;

public class ExpEvaluator {
	
	public void processExpressions(Map<String,String> exps, CmsRfcCI rfc) {
		for (Map.Entry<String, String> attr : exps.entrySet()) {
			if (attr.getKey().startsWith("value")) {
				processValue(attr.getKey(), attr.getValue(), rfc.getAttributes());
			}
		}
	}

	public void processExpressions(Map<String,String> exps, CmsRfcRelation rfc) {
		for (Map.Entry<String, String> attr : exps.entrySet()) {
			if (attr.getKey().startsWith("value")) {
				processValue(attr.getKey(), attr.getValue(), rfc.getAttributes());
			}
		}
	}

	
	private void processValue(String attrName, String exp, Map<String,CmsRfcAttribute> attributes) {
		int beginIndex = exp.indexOf("(");
		if (beginIndex == -1) return;

		int endIndex = exp.lastIndexOf(")");
		if (endIndex < beginIndex) return;

		attributes.get(attrName).setNewValue(attributes.get(exp.substring(beginIndex, endIndex)).getNewValue());
		
	}
}
