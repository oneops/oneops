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
/**
 * 
 */
package com.oneops.opamp.service;

import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.util.domain.CmsVar;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Testing class for EnvPropsProcessor
 *
 */
public class CachedEnvPropsProcessorTest {
	private static final String VARIABLE_NAME = "test";
	private CmsVar var = new CmsVar();
	
	private CmsCmProcessor mockCmProcessor;

	
	@BeforeClass
	public void init(){
		this.mockCmProcessor =mock(CmsCmProcessor.class);
		when(mockCmProcessor.getCmSimpleVar(VARIABLE_NAME)).thenReturn(var);
		var.setValue("true");
	}
	
	
	@Test
	public void cacheTest() throws InterruptedException {
		CachedEnvPropsProcessor envPropsProcessor=new CachedEnvPropsProcessor();
		envPropsProcessor.setTtlInSeconds(1);
		envPropsProcessor.setCmProcessor(mockCmProcessor);
		
		assertTrue(envPropsProcessor.getBooleanVariable(VARIABLE_NAME)); // initial value should be true
		verify(mockCmProcessor, times(1)).getCmSimpleVar(VARIABLE_NAME); // variable lookup should have happened 1 times
		var.setValue("false");  // setting to false, but should still be cached
		assertTrue(envPropsProcessor.getBooleanVariable(VARIABLE_NAME)); // still true because it is cached
		verify(mockCmProcessor, times(1)).getCmSimpleVar(VARIABLE_NAME); // variable lookup should still have happened only 1 times

		Thread.sleep(2 *1000);
		assertFalse(envPropsProcessor.getBooleanVariable(VARIABLE_NAME)); // no longer true
		verify(mockCmProcessor, times(2)).getCmSimpleVar(VARIABLE_NAME); // variable lookup should have happened 2 times because of TTL
	}
}
