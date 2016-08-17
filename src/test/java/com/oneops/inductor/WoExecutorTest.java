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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.oneops.cms.simple.domain.CmsRfcCISimple;

import junit.framework.Assert;

public class WoExecutorTest {
	
	WorkOrderExecutor woExecutor;
	
	@Before
	public void init() {
		Config config = new Config();
		woExecutor = new WorkOrderExecutor(config, null);
	}
	
	@Test
	public void testExtraRunList4Wo() {
		List<String> runList = new ArrayList<>();
		List<String> classes = new ArrayList<>();
		classes.add("cloud.compliance.Security");
		classes.add("cloud.compliance.Security");
		classes.add("cloud.compliance.Dummy");
		classes.add("cloud.compliance.Dummy");

		woExecutor.addExtraClassesToRunList(runList, getRfcCiForExtraRunList(classes), "add");
		Assert.assertFalse(runList.isEmpty());
		Assert.assertEquals(2, runList.size());

		String securityRecipe = AbstractOrderExecutor.RUN_LIST_PREFIX + "security" + AbstractOrderExecutor.RUN_LIST_SEPARATOR + 
				"add" + AbstractOrderExecutor.RUN_LIST_SUFFIX;
		String dummyRecipe = AbstractOrderExecutor.RUN_LIST_PREFIX + "dummy" + AbstractOrderExecutor.RUN_LIST_SEPARATOR + 
				"add" + AbstractOrderExecutor.RUN_LIST_SUFFIX;
		Assert.assertTrue(runList.contains(securityRecipe));
		Assert.assertTrue(runList.contains(dummyRecipe));
		
		classes = null;
		runList.clear();
		woExecutor.addExtraClassesToRunList(runList, getRfcCiForExtraRunList(classes), "add");
		Assert.assertTrue(runList.isEmpty());
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
}
