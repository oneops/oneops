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
import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.dj.domain.CmsWorkOrder;
import com.oneops.cms.domain.CmsWorkOrderBase;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;

public class ExpressionEvaluator {
	
	private Logger logger = Logger.getLogger(this.getClass());
	private ExpressionParser exprParser;
	private CmsUtil cmsUtil;
	
	public static final String ATTR_NAME_FILTER = "filter";

	public boolean isExpressionMatching(CmsCI complianceCi, CmsWorkOrderBase wo) {
		
		String filter = complianceCi.getAttribute(ATTR_NAME_FILTER).getDjValue();
		
		try {
			if (StringUtils.isNotBlank(filter)) {
				Expression expr = exprParser.parseExpression(filter);
				EvaluationContext context = getEvaluationContext(wo);
				//parse the filter expression and check if it matches this ci/rfc
				boolean match = expr.getValue(context, Boolean.class);
				if (logger.isDebugEnabled()) {
					logger.debug("Expression " + filter + " provided by compliance ci " + complianceCi.getCiId() + " not matched for ci " + getCiName(wo));	
				}
				
				return match;
			}
			return false;
		} catch (ParseException | EvaluationException e) {
			String error = "Error in evaluating expression " + filter +" provided by compliance ci " + complianceCi.getCiId() + ", target ci :" + getCiName(wo); 
			logger.error(error, e);
			throw new DJException(CmsError.DJ_EXPR_EVAL_ERROR, error);
		}
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
