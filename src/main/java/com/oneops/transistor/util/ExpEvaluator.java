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
		String targetAttr = exp.substring(exp.indexOf("("), exp.lastIndexOf(")"));
		
		attributes.get(attrName).setNewValue(attributes.get(targetAttr).getNewValue());
		
	}
}
