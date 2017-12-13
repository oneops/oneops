package com.oneops.opamp.rs;

import org.junit.Ignore;
import org.testng.annotations.Test;

import com.oneops.opamp.exceptions.OpampException;
import com.oneops.opamp.service.ComputeProcessor;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;


import java.util.Map;
public class OpampRsControllerTest {
	
	@Ignore
	@Test
	public void OpampRsControllerReplace() throws OpampException
	{
		OpampRsController opampRsControllerTest = new OpampRsController();

		opampRsControllerTest.setComputeProcessor(mock(ComputeProcessor.class));
		
		Map<String, Integer> result= opampRsControllerTest.replaceComputeByCid(anyLong());
		assertEquals(1, result.size());
		
	}
	


}