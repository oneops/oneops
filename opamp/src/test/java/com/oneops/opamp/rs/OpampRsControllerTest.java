package com.oneops.opamp.rs;


import org.testng.annotations.Test;

import com.oneops.opamp.exceptions.OpampException;
import com.oneops.opamp.service.ComputeProcessor;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

public class OpampRsControllerTest {
	
	ComputeProcessor computeProcessor=null;
	
	
	@Test
	public void OpampRsController_replaceComputeByCid_TestFunction() throws OpampException
	{
		
		OpampRsController opampRsControllerTest = new OpampRsController();
		opampRsControllerTest.setComputeProcessor(mock(ComputeProcessor.class));
		Map<String, Integer> result= opampRsControllerTest.replaceComputeByCid(anyLong());
		assertEquals(result.size(), 0,"OpampRsControllerReplace Failed, expected size 0 of result HashMap found size: "+result.size());
		
	}
	

	@Test
	public void OpampRsController_replaceComputeByCid_TestValidResponse() throws OpampException
	{
		
		Map<String, Integer> defaultResponse=new HashMap<>(1);
		defaultResponse.put("deploymentId",0);
		OpampRsController opampRsControllerTest = new OpampRsController();
		ComputeProcessor computeProcessor = mock(ComputeProcessor.class);
		opampRsControllerTest.setComputeProcessor(computeProcessor);
		when(computeProcessor.replaceComputeByCid(anyLong())).thenReturn(defaultResponse);
		Map<String, Integer> result= opampRsControllerTest.replaceComputeByCid(anyLong());
		assertEquals(result.size(), 1,"OpampRsControllerReplace Failed, expected size 1 of result HashMap found size: "+result.size());
		
	}

	@Test
	public void OpampRsController_replaceComputeByCid_TestException() throws OpampException
	{
		
		Map<String, Integer> defaultResponse=new HashMap<>(1);
		defaultResponse.put("deploymentId",1);
		OpampRsController opampRsControllerTest = new OpampRsController();
		ComputeProcessor computeProcessor = mock(ComputeProcessor.class);
		opampRsControllerTest.setComputeProcessor(computeProcessor);
		when(computeProcessor.replaceComputeByCid(anyLong())).thenThrow(new NullPointerException("Test Exception"));
		Map<String, Integer> result= opampRsControllerTest.replaceComputeByCid(anyLong());
		assertEquals(result.size(), 1,"OpampRsControllerReplace Failed, expected size 1 of result HashMap found size: "+result.size());
		assertEquals(result.get("deploymentId"), Integer.valueOf(1),"Deployment Must have failed and returned response code: 1 , result.entrySet(): "+result.entrySet());
		
	}
}