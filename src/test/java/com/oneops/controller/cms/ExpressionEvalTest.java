package com.oneops.controller.cms;

import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsWorkOrder;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.util.CmsUtil;

public class ExpressionEvalTest {

	ExpressionEvaluator expressionEvaluator;
	
	@BeforeClass
	public void setup() {
		expressionEvaluator = new ExpressionEvaluator();
		expressionEvaluator.setExprParser(new SpelExpressionParser());
		expressionEvaluator.setCmsUtil(new CmsUtil());
	}
	
	@Test
	public void testExpressionOnWo() {
		
		CmsCI complianceCi = createComplianceCIForExpr("(ciClassName matches 'bom(\\..*\\.[0-9]+)?\\.Compute' and ciAttributes['size'] == 'M' "
				+ "and ciAttributes['ostype'].trim() == 'CentOS 6.5')");
		
		CmsRfcCI rfcCi = new CmsRfcCI();
		rfcCi.setCiClassName("bom.Compute");
		rfcCi.setCiName("compute-1231999");
		rfcCi.addAttribute(createRfcAttribute("size", "M", null));
		rfcCi.addAttribute(createRfcAttribute("ostype", "CentOS 6.5", null));
		CmsWorkOrder wo = new CmsWorkOrder();
		wo.setRfcCi(rfcCi);
		
		Assert.assertTrue(expressionEvaluator.isExpressionMatching(complianceCi, wo));
		
		rfcCi.setCiClassName("bom.oneops.1.Compute");
		Assert.assertTrue(expressionEvaluator.isExpressionMatching(complianceCi, wo));
		
		rfcCi.addAttribute(createRfcAttribute("size", "L", null));
		Assert.assertFalse(expressionEvaluator.isExpressionMatching(complianceCi, wo));
		
		rfcCi.addAttribute(createRfcAttribute("ostype", "CentOS 7.0", null));
		Assert.assertFalse(expressionEvaluator.isExpressionMatching(complianceCi, wo));
		
		CmsRfcCI osRfcCi = new CmsRfcCI();
		osRfcCi.setCiClassName("bom.Os");
		osRfcCi.setCiName("os");
		wo.setRfcCi(osRfcCi);
		
		Assert.assertFalse(expressionEvaluator.isExpressionMatching(complianceCi, wo));
	}
	
	@Test
	public void testEmptyExpression() {
		
		CmsRfcCI rfcCi = new CmsRfcCI();
		rfcCi.setCiClassName("bom.Compute");
		rfcCi.setCiName("compute-453222");
		rfcCi.addAttribute(createRfcAttribute("size", "M", null));
		CmsWorkOrder wo = new CmsWorkOrder();
		wo.setRfcCi(rfcCi);
		
		CmsCI complianceCi = createComplianceCIForExpr(null);
		Assert.assertFalse(expressionEvaluator.isExpressionMatching(complianceCi, wo));
		complianceCi = createComplianceCIForExpr(" ");
		Assert.assertFalse(expressionEvaluator.isExpressionMatching(complianceCi, wo));
		
	}
	
	@Test(expectedExceptions = { DJException.class })
	public void testWrongSyntax() {
		CmsRfcCI rfcCi = new CmsRfcCI();
		rfcCi.setCiClassName("bom.Compute");
		rfcCi.setCiName("compute-453222");
		rfcCi.addAttribute(createRfcAttribute("size", "M", null));
		CmsWorkOrder wo = new CmsWorkOrder();
		wo.setRfcCi(rfcCi);
		
		//invalid expression: missing ' in the end, should fail on parsing
		CmsCI complianceCi = createComplianceCIForExpr("ciClassName == 'bom.Os");
		expressionEvaluator.isExpressionMatching(complianceCi, wo);
	}
	
	@Test(expectedExceptions = { DJException.class })
	public void testInvalidExpression() {
		CmsRfcCI rfcCi = new CmsRfcCI();
		rfcCi.setCiClassName("bom.Compute");
		rfcCi.setCiName("compute-453222");
		rfcCi.addAttribute(createRfcAttribute("size", "M", null));
		CmsWorkOrder wo = new CmsWorkOrder();
		wo.setRfcCi(rfcCi);
		
		//invalid expression: no field className in CmsRfcCI bean, should fail in evaluation
		CmsCI complianceCi = createComplianceCIForExpr("className == 'bom.Os'");
		expressionEvaluator.isExpressionMatching(complianceCi, wo);
	}
	
	@Test
	public void testExpressionOnAo() {
		
		CmsCI complianceCi = createComplianceCIForExpr("(ciClassName matches 'bom.*\\.Compute' and ciAttributes['size'] == 'M' "
				+ "and ciAttributes['ostype'].trim() == 'CentOS 6.5')");
		
		CmsCI ci = new CmsCI();
		ci.setCiClassName("bom.Compute");
		ci.setCiName("compute-1231999");
		ci.addAttribute(createCiAttribute("size", "M", "M"));
		ci.addAttribute(createCiAttribute("ostype", "CentOS 6.5", "CentOS 6.5"));
		CmsActionOrder ao = new CmsActionOrder();
		ao.setCi(ci);
		
		Assert.assertTrue(expressionEvaluator.isExpressionMatching(complianceCi, ao));
		
		CmsCI osCi = new CmsCI();
		osCi.setCiClassName("bom.Os");
		osCi.setCiName("os");
		ao.setCi(osCi);
		
		Assert.assertFalse(expressionEvaluator.isExpressionMatching(complianceCi, ao));
	}
	
	private CmsRfcAttribute createRfcAttribute(String name, String newValue, String oldValue) {
		CmsRfcAttribute rfcAttribute = new CmsRfcAttribute();
		rfcAttribute.setAttributeName(name);
		rfcAttribute.setNewValue(newValue);
		rfcAttribute.setOldValue(oldValue);
		return rfcAttribute;
	}
	
	private CmsCI createComplianceCIForExpr(String expr) {
		CmsCI exprCi = new CmsCI();
		exprCi.setCiClassName("base.Compliance");
		CmsCIAttribute filterAttribute = new CmsCIAttribute();
		filterAttribute.setAttributeName(ExpressionEvaluator.ATTR_NAME_FILTER);
		filterAttribute.setDjValue(expr);
		exprCi.addAttribute(filterAttribute);
		return exprCi;
	}
	
	private CmsCIAttribute createCiAttribute(String name, String dfValue, String djValue) {
		CmsCIAttribute attr = new CmsCIAttribute();
		attr.setAttributeName(name);
		attr.setDfValue(dfValue);
		attr.setDfValue(djValue);
		return attr;
	}
	
	public void setExpressionEvaluator(ExpressionEvaluator expressionEvaluator) {
		this.expressionEvaluator = expressionEvaluator;
	}
	
}
