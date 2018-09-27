/*******************************************************************************
 *
 * Copyright 2015 Walmart, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.oneops.controller.cms;

import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.util.domain.CmsVar;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class CmsWoProviderTest {

	private CmsWoProvider ccp = new CmsWoProvider();
	private CmsVar cmsVar = new CmsVar();

	private CmsCmProcessor cmsCmProcessor = mock(CmsCmProcessor.class);

	private static Logger logger = Logger.getLogger(CmsWoProviderTest.class);


	@SuppressWarnings("unchecked")
	@BeforeClass
	public void setUpIntance(){
		cmsVar.setValue("bom.oneops.1.Compute, bom.oneops.1.OSx");
		ccp.setCmProcessor(cmsCmProcessor);
	}

	@Test
	public void requireComputesEnabledTrueTest(){
		ccp.setRequiresComputesCheckEnabled(true);
		when(cmsCmProcessor.getCmSimpleVar("REQUIRES_COMPUTES")).thenReturn(cmsVar);
		assertEquals(ccp.isClassEligibleForRequiresComputes("bom.oneops.1.Compute"), true);
	}

	@Test
	public void requireComputesEnabledFalseTest(){
		ccp.setRequiresComputesCheckEnabled(false);
		when(cmsCmProcessor.getCmSimpleVar("REQUIRES_COMPUTES")).thenReturn(cmsVar);
		assertEquals(ccp.isClassEligibleForRequiresComputes("bom.oneops.1.Compute"), true);
	}

	@Test
	public void isClassEligibleForRequiresComputesTrueTest(){
		ccp.setRequiresComputesCheckEnabled(true);
		when(cmsCmProcessor.getCmSimpleVar("REQUIRES_COMPUTES")).thenReturn(cmsVar);
		assertEquals(ccp.isClassEligibleForRequiresComputes("bom.oneops.1.OSx"), true);
	}

	@Test
	public void isClassEligibleForRequiresComputesFalseTest(){
		ccp.setRequiresComputesCheckEnabled(true);
		when(cmsCmProcessor.getCmSimpleVar("REQUIRES_COMPUTES")).thenReturn(cmsVar);
		assertEquals(ccp.isClassEligibleForRequiresComputes("bom.oneops.1.NoService"), false);
	}

	@Test
	public void isClassEligibleForRequiresComputesNullTest(){
		ccp.setRequiresComputesCheckEnabled(true);
		when(cmsCmProcessor.getCmSimpleVar("REQUIRES_COMPUTES")).thenReturn(null);
		assertEquals(ccp.isClassEligibleForRequiresComputes("bom.oneops.1.NoService"), false);

		when(cmsCmProcessor.getCmSimpleVar("REQUIRES_COMPUTES")).thenReturn(new CmsVar());
		assertEquals(ccp.isClassEligibleForRequiresComputes("bom.oneops.1.NoService"), false);
	}
}