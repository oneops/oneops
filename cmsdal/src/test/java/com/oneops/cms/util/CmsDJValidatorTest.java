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
package com.oneops.cms.util;

import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.ns.service.CmsNsProcessor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.*;

import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.service.CmsMdManager;
import com.oneops.cms.ns.domain.CmsNamespace;
import com.oneops.cms.ns.service.CmsNsManager;
import com.oneops.cms.util.CIValidationResult;
import com.oneops.cms.util.CmsDJValidator;

import static org.testng.Assert.*;
import static org.mockito.Mockito.*;


/**
 * The Class CmsDJValidatorTest.
 */
public class CmsDJValidatorTest {
	private final CmsDJValidator validator = new CmsDJValidator();
	
	/**
	 * Sets the mocks.
	 */
	@BeforeClass
	public void setMocks(){
		this.validator.setCmsMdProcessor(mock(CmsMdProcessor.class));
		this.validator.setCmsNsProcessor(mock(CmsNsProcessor.class));
	}
	
	/**
	 * Test empty name.
	 */
	@Test
	public void testEmptyName(){
		CmsRfcCI rfcCi = new CmsRfcCI();
		rfcCi.setCiName("");
		CIValidationResult result = this.validator.validateRfcCi(rfcCi);
		assertTrue(result.getErrorMsg().startsWith("CI Name can not be empty!"),"unex:"+result.getErrorMsg());
		assertEquals(result.isValidated(),false);
	}

	/**
	 * Test null class.
	 */
	@Test
	public void testNullClass(){
		CmsDJValidator myValidator = new CmsDJValidator();
		CmsMdProcessor mdp = mock(CmsMdProcessor.class);
 		when(mdp.getClazz(null)).thenReturn(null);
		myValidator.setCmsMdProcessor(mdp);
		//
		CmsNsProcessor cmsNsProcessor= mock(CmsNsProcessor.class);
		CmsNamespace cmsNameSpace = new CmsNamespace();
		cmsNameSpace.setNsId(222);
		when(cmsNsProcessor.getNs(anyString())).thenReturn(cmsNameSpace);
		myValidator.setCmsNsProcessor(cmsNsProcessor);

		CmsRfcCI rfcCi = new CmsRfcCI();
		rfcCi.setCiName("testClassName");
    	rfcCi.setNsId(0);
    	rfcCi.setNsPath("/ZikaSoft/ZikaSoftAssembly5");
    	rfcCi.setCiClassName(null);
		CIValidationResult result = myValidator.validateRfcCi(rfcCi);
		assertTrue(result.getErrorMsg().startsWith("There is no class definition for null"),"unex:"+result.getErrorMsg());
		assertEquals(result.isValidated(),false);
	}
	
	/**
	 * Test ns id.
	 */
	@Test
	public void testNsId(){
		CmsDJValidator myValidator = new CmsDJValidator();
		CmsMdProcessor mdm = mock(CmsMdProcessor.class);
		CmsClazz aClazz = new CmsClazz();
		aClazz.setClassId(111111);
		when(mdm.getClazz("catalog.Platform")).thenReturn(aClazz);
		myValidator.setCmsMdProcessor(mdm);
		myValidator.setCmsNsProcessor(mock(CmsNsProcessor.class));

		
		CmsRfcCI rfcCi = new CmsRfcCI();
		rfcCi.setCiName("testClassName");
    	rfcCi.setCiClassName("catalog.Platform");
    	rfcCi.setNsId(0);
    	rfcCi.setNsPath("foo");
		CIValidationResult result = myValidator.validateRfcCi(rfcCi);
		assertTrue(result.getErrorMsg().startsWith("The namespace must be specified"),"unex:"+result.getErrorMsg());
		assertEquals(result.isValidated(),false);
	}
}
