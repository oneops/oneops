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
package com.oneops.controller.cms;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.dj.domain.CmsWorkOrder;
import com.oneops.cms.domain.CmsWorkOrderBase;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsUtil;

/**
 * This class is used to evaluate expression filter. The expression to be evaluated should follow 
 * the <a href="http://docs.spring.io/spring/docs/current/spring-framework-reference/html/expressions.html">SPEL</a> (Spring Expression Language) syntax.
 *
 * <p> The expression is evaluated against {@link com.oneops.cms.dj.domain.CmsRfcCI CmsRfcCI} for work orders and 
 * {@link com.oneops.cms.cm.domain.CmsCI CmsCI} for action orders.
 *
 * <p> Example:
 * <pre>ciClassName matches 'bom(\\..*\\.[0-9]+)?\\.Compute' and ciAttributes['size'] == 'M'</pre>
 *
 */
public class ExpressionEvaluator {
	
	private Logger logger = Logger.getLogger(this.getClass());
	private ExpressionParser exprParser;
	private CmsUtil cmsUtil;
	
	public static final String ATTR_NAME_FILTER = "filter";

	public boolean isExpressionMatching(CmsCI complianceCi, CmsWorkOrderBase wo) {
		
		CmsCIAttribute attr = complianceCi.getAttribute(ATTR_NAME_FILTER);
		if (attr == null) {
			return false;
		}
		String filter = attr.getDjValue();
		
		try {
			if (StringUtils.isNotBlank(filter)) {
				Expression expr = exprParser.parseExpression(filter);
				EvaluationContext context = getEvaluationContext(wo);
				//parse the filter expression and check if it matches this ci/rfc
				Boolean match = expr.getValue(context, Boolean.class);
				if (logger.isDebugEnabled()) {
					logger.debug("Expression " + filter + " provided by compliance ci " + complianceCi.getCiId() + " not matched for ci " + getCiName(wo));	
				}
				
				return match;
			}
		} catch (ParseException | EvaluationException e) {
			String error = "Error in evaluating expression " + filter +" provided by compliance ci " + complianceCi.getCiId() + ", target ci :" + getCiName(wo); 
			logger.error(error, e);
		}
		return false;
	}
	
	private EvaluationContext getEvaluationContext(CmsWorkOrderBase woBase) {
		StandardEvaluationContext context = new StandardEvaluationContext();
		if (woBase instanceof CmsWorkOrder) {
			CmsRfcCISimple rfcSimple = cmsUtil.custRfcCI2RfcCISimple(((CmsWorkOrder)woBase).getRfcCi());
			context.setRootObject(rfcSimple);
		} else if (woBase instanceof CmsActionOrder) {
			CmsCISimple ciSimple = cmsUtil.custCI2CISimple(((CmsActionOrder)woBase).getCi(), CmsConstants.ATTR_VALUE_TYPE_DF, true);
			context.setRootObject(ciSimple);
		}
		return context;
	}
	
	private String getCiName(CmsWorkOrderBase woBase) {
		String ciName = null;
		if (woBase instanceof CmsWorkOrder) {
			ciName = ((CmsWorkOrder)woBase).getRfcCi().getCiName();
		} else if (woBase instanceof CmsActionOrder) {
			ciName = ((CmsActionOrder)woBase).getCi().getCiName();
		}
		return ciName;
	}

	public void setExprParser(ExpressionParser exprParser) {
		this.exprParser = exprParser;
	}

	public void setCmsUtil(CmsUtil cmsUtil) {
		this.cmsUtil = cmsUtil;
	}
	
}
