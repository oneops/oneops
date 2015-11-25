package com.oneops.cms.util;

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
		this.validator.setMdManager(mock(CmsMdManager.class));
		this.validator.setNsManager(mock(CmsNsManager.class));
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
		CmsMdManager mdm = mock(CmsMdManager.class);
 		when(mdm.getClazz(null)).thenReturn(null);
		myValidator.setMdManager(mdm);
		//
		CmsNsManager nsManager= mock(CmsNsManager.class);
		CmsNamespace cmsNameSpace = new CmsNamespace();
		cmsNameSpace.setNsId(222);
		when(nsManager.getNs(anyString())).thenReturn(cmsNameSpace);
		myValidator.setNsManager(nsManager);

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
		CmsMdManager mdm = mock(CmsMdManager.class);
		CmsClazz aClazz = new CmsClazz();
		aClazz.setClassId(111111);
		when(mdm.getClazz("catalog.Platform")).thenReturn(aClazz);
		myValidator.setMdManager(mdm);
		myValidator.setNsManager(mock(CmsNsManager.class));

		
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
