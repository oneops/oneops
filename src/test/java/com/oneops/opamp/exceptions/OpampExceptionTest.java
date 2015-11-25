package com.oneops.opamp.exceptions;
import org.testng.annotations.Test;
import  org.testng.Assert;

import com.oneops.opamp.exceptions.OpampException;

/**
 * Simple tests of construction
 *
 */
public class OpampExceptionTest {

	@Test
	public void constructionTests(){
		OpampException o1 = new OpampException();
		Assert.assertNotNull(o1);

		OpampException o2 = new OpampException(new RuntimeException("foo"));
		String s2 = o2.getMessage();
		Assert.assertEquals("java.lang.RuntimeException: foo",s2);

		OpampException o4 = new OpampException("mock");
		Assert.assertNotNull(o4);

	}
	

}
