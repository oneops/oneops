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
package com.oneops.inductor;

import java.util.*;

import com.google.gson.Gson;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import org.junit.Before;
import org.junit.Test;

import com.oneops.cms.simple.domain.CmsRfcCISimple;

import junit.framework.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

public class WoExecutorTest {
	
	WorkOrderExecutor woExecutor;
	final protected Gson gson = new Gson();

	@Before
	public void init() {
		Config config = new Config();
		config.setCircuitDir("/opt/oneops/inductor/packer");
		woExecutor = new WorkOrderExecutor(config, null);
	}
	
	@Test
	public void testExtraRunList4Wo() {
		List<String> classes = new ArrayList<>();
		classes.add("cloud.compliance.Security");
		classes.add("cloud.compliance.Security");
		classes.add("cloud.compliance.Dummy");
		classes.add("cloud.compliance.Dummy");
		CmsWorkOrderSimple wo = new CmsWorkOrderSimple();
		wo.putPayLoadEntry(InductorConstants.EXTRA_RUN_LIST,getRfcCiForExtraRunList(classes));
		CmsRfcCISimple rfci = new CmsRfcCISimple();
		rfci.setRfcAction("add");
		wo.setRfcCi(rfci);
		List<String> runList = woExecutor.getExtraRunListClasses(wo);

		Assert.assertFalse(runList.isEmpty());
		Assert.assertEquals(2, runList.size());

		String securityRecipe = AbstractOrderExecutor.RUN_LIST_PREFIX + "security" + AbstractOrderExecutor.RUN_LIST_SEPARATOR + 
				"add" + AbstractOrderExecutor.RUN_LIST_SUFFIX;
		String dummyRecipe = AbstractOrderExecutor.RUN_LIST_PREFIX + "dummy" + AbstractOrderExecutor.RUN_LIST_SEPARATOR + 
				"add" + AbstractOrderExecutor.RUN_LIST_SUFFIX;
		Assert.assertTrue(runList.contains(securityRecipe));
		Assert.assertTrue(runList.contains(dummyRecipe));
		
		classes = null;
        wo.putPayLoadEntry(InductorConstants.EXTRA_RUN_LIST,getRfcCiForExtraRunList(classes));

        List<String> runList1 = woExecutor.getExtraRunListClasses(wo);
		Assert.assertTrue(runList1.isEmpty());
	}
	
	private List<CmsRfcCISimple> getRfcCiForExtraRunList(List<String> classes) {
		List<CmsRfcCISimple> list = new ArrayList<>();
		if (classes != null) {
			for (String clazz : classes) {
				CmsRfcCISimple rfc = new CmsRfcCISimple();
				rfc.setCiClassName(clazz);
				list.add(rfc);
			}
		}
		return list;
	}

	@Test
	public void testCreateCookbookSearchPath() {
		String cloudName = "stg-dfw1";
		Map<String, Map<String, CmsCISimple>> cloudServices = new HashMap<>();
		Map<String, CmsCISimple> serviceCis = new HashMap<>();
		CmsCISimple cloudServiceCi = new CmsCISimple();
		cloudServiceCi.setCiClassName("cloud.service.oneops.1.Keywhiz-cloud-service");
		serviceCis.put(cloudName, cloudServiceCi);
		cloudServices.put("secret", serviceCis);

		LinkedHashSet<String> searchPaths = woExecutor.createCookbookSearchPath("circuit-main-1", cloudServices, cloudName);
		LinkedHashSet<String> expectedResult = new LinkedHashSet<>();

		expectedResult.add("/opt/oneops/inductor/circuit-oneops-1/components/cookbooks");
		expectedResult.add("/opt/oneops/inductor/circuit-main-1/components/cookbooks");
		expectedResult.add("/opt/oneops/inductor/shared/cookbooks");

		Assert.assertEquals(gson.toJson(searchPaths), gson.toJson(expectedResult));

		//now try the case where there is no cloud service involved
		searchPaths = woExecutor.createCookbookSearchPath("circuit-main-1", new HashMap<>(), cloudName);
		expectedResult = new LinkedHashSet<>();

		expectedResult.add("/opt/oneops/inductor/circuit-main-1/components/cookbooks");
		expectedResult.add("/opt/oneops/inductor/shared/cookbooks");

		Assert.assertEquals(gson.toJson(searchPaths), gson.toJson(expectedResult));
	}
}
