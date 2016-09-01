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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsWorkOrder;
import com.oneops.cms.util.CmsConstants;

import static org.mockito.Mockito.*;

public class WoProviderTest {

	private static ApplicationContext context;
	
	private CmsCmProcessor cmProcessor;
	private CmsWoProvider woProvider;

	private Gson gson = new Gson();

	private static final String EXPR_WO = "(ciClassName matches 'bom(\\..*\\.[0-9]+)?\\.Os')";

	private static final String EXPR_AO = "(ciClassName matches 'bom(\\..*\\.[0-9]+)?\\.Compute' and ciAttributes['ostype'] == 'centos-7.0')";

	@BeforeClass
	public void setUp() throws JMSException{
		context = new ClassPathXmlApplicationContext("**/test-wo-context.xml");
		cmProcessor = (CmsCmProcessor) context.getBean(CmsCmProcessor.class);
		woProvider = (CmsWoProvider) context.getBean(CmsWoProvider.class);
	}
	
	@Test
	public void testWoComplianceObject() {
		CmsWorkOrder wo = getTestWorkOrder();
		List<CmsCIRelation> list = new ArrayList<>();
		String version = "1.0";
		list.add(createComplianceRelForExpr(EXPR_WO, "true", version));
		
		when(cmProcessor.getFromCIRelations(eq(wo.getCloud().getCiId()), anyString(), anyString())).thenReturn(list);
		List<CmsRfcCI> complList = woProvider.getMatchingCloudCompliance(wo);
		Assert.assertNotNull(complList);
		Assert.assertEquals(complList.size(), 1);
		CmsRfcAttribute filterAttr = complList.get(0).getAttribute(ExpressionEvaluator.ATTR_NAME_FILTER);
		Assert.assertNotNull(filterAttr);
		Assert.assertEquals(filterAttr.getNewValue(), EXPR_WO);
		CmsRfcAttribute versionAttr = complList.get(0).getAttribute("version");
		Assert.assertNotNull(versionAttr);
		Assert.assertEquals(versionAttr.getNewValue(), version);
	}
	
	@Test
	public void testAoComplianceObject() {
		CmsActionOrder ao = getTestActionOrder();
		List<CmsCIRelation> list = new ArrayList<>();
		String version = "1.0";
		list.add(createComplianceRelForExpr(EXPR_AO, "true", version));
		
		when(cmProcessor.getFromCIRelations(eq(ao.getCloud().getCiId()), anyString(), anyString())).thenReturn(list);
		List<CmsCI> complList = woProvider.getMatchingCloudCompliance(ao);
		Assert.assertNotNull(complList);
		Assert.assertEquals(complList.size(), 1);
		CmsCIAttribute filterAttr = complList.get(0).getAttribute(ExpressionEvaluator.ATTR_NAME_FILTER);
		Assert.assertNotNull(filterAttr);
		Assert.assertEquals(filterAttr.getDfValue(), EXPR_AO);
		CmsCIAttribute versionAttr = complList.get(0).getAttribute("version");
		Assert.assertNotNull(versionAttr);
		Assert.assertEquals(versionAttr.getDfValue(), version);
	}
	
	@Test
	public void testAoWithComplianceDisabled() {
		CmsActionOrder ao = getTestActionOrder();
		List<CmsCIRelation> list = new ArrayList<>();
		String version = "1.0";
		list.add(createComplianceRelForExpr(EXPR_AO, "false", version));
		
		when(cmProcessor.getFromCIRelations(eq(ao.getCloud().getCiId()), anyString(), anyString())).thenReturn(list);
		List<CmsCI> complList = woProvider.getMatchingCloudCompliance(ao);
		Assert.assertNotNull(complList);
		Assert.assertEquals(complList.size(), 1);
	}
	
	@Test
	public void testWoWithComplianceDisabed() {
		CmsWorkOrder wo = getTestWorkOrder();
		List<CmsCIRelation> list = new ArrayList<>();
		String expr = "(ciClassName matches 'bom(\\..*\\.[0-9]+)?\\.Os')";
		String version = "1.0";
		list.add(createComplianceRelForExpr(expr, "false", version));
		
		when(cmProcessor.getFromCIRelations(eq(wo.getCloud().getCiId()), anyString(), anyString())).thenReturn(list);
		List<CmsRfcCI> complList = woProvider.getMatchingCloudCompliance(wo);
		Assert.assertTrue(complList != null && complList.size() == 0);
	}
	
	@Test
	public void testWoWithAutoComplyDisabled() {
		CmsWorkOrder wo = getTestWorkOrder();
		List<CmsCIRelation> list = new ArrayList<>();
		String expr = "(ciClassName matches 'bom(\\..*\\.[0-9]+)?\\.Os')";
		String version = "1.0";
		list.add(createComplianceRelForExpr(expr, "false", version));
		
		//disable auto comply in rfc
		wo.getBox().getAttribute(CmsConstants.ATTR_NAME_AUTO_COMPLY).setDfValue("false");
		wo.getBox().getAttribute(CmsConstants.ATTR_NAME_AUTO_COMPLY).setDjValue("false");
		
		when(cmProcessor.getFromCIRelations(eq(wo.getCloud().getCiId()), anyString(), anyString())).thenReturn(list);
		List<CmsRfcCI> complList = woProvider.getMatchingCloudCompliance(wo);
		Assert.assertTrue(complList != null && complList.size() == 0);
	}
	
	private CmsWorkOrder getTestWorkOrder() {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("test-wo.json");
		JsonParser parser = new JsonParser();
		JsonElement jsonElement = (JsonElement) parser.parse(new InputStreamReader(is));
		return gson.fromJson(jsonElement, CmsWorkOrder.class);
	}
	
	private CmsActionOrder getTestActionOrder() {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("test-ao.json");
		JsonParser parser = new JsonParser();
		JsonElement jsonElement = (JsonElement) parser.parse(new InputStreamReader(is));
		return gson.fromJson(jsonElement, CmsActionOrder.class);
	}
	
	private CmsCIRelation createComplianceRelForExpr(String expr, String enabled, String version) {
		
		CmsCI exprCi = new CmsCI();
		exprCi.setCiClassName("base.Compliance");
		CmsCIAttribute filterAttribute = new CmsCIAttribute();
		filterAttribute.setAttributeName(ExpressionEvaluator.ATTR_NAME_FILTER);
		filterAttribute.setDjValue(expr);
		filterAttribute.setDfValue(expr);
		exprCi.addAttribute(filterAttribute);
		
		CmsCIAttribute enabledAttribute = new CmsCIAttribute();
		enabledAttribute.setAttributeName(CmsConstants.ATTR_NAME_ENABLED);
		enabledAttribute.setDjValue(enabled);
		enabledAttribute.setDfValue(enabled);
		exprCi.addAttribute(enabledAttribute);
		
		CmsCIAttribute versionAttribute = new CmsCIAttribute();
		versionAttribute.setAttributeName("version");
		versionAttribute.setDjValue(version);
		versionAttribute.setDfValue(version);
		exprCi.addAttribute(versionAttribute);
		exprCi.setNsPath("/org1/test");
		
		CmsCIRelation rel = new CmsCIRelation();
		rel.setToCi(exprCi);
		return rel;
	}
	
}
