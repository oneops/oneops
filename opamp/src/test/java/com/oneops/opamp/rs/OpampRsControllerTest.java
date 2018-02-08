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

	@Test
	public void OpampRsController_replaceComputeByCid_TestFunction() throws OpampException {

		OpampRsController opampRsController = new OpampRsController();
		opampRsController.setComputeProcessor(mock(ComputeProcessor.class));
		Map<String, Integer> result = opampRsController.replaceComputeByCid(anyLong());
		assertEquals(result.size(), 0, "Failed, expected size 0 found : " + result.size());

	}

	@Test
	public void OpampRsController_replaceComputeByCid_TestValidResponse() throws OpampException {

		Map<String, Integer> defaultResponse = new HashMap<>(1);
		defaultResponse.put("deploymentId", 0);
		OpampRsController opampRsController = new OpampRsController();
		ComputeProcessor computeProcessor = mock(ComputeProcessor.class);
		opampRsController.setComputeProcessor(computeProcessor);
		when(computeProcessor.replaceComputeByCid(anyLong())).thenReturn(defaultResponse);
		Map<String, Integer> result = opampRsController.replaceComputeByCid(anyLong());
		assertEquals(result.size(), 1, "Failed, expected size 0 found : " + result.size());

	}

	@Test
	public void OpampRsController_replaceComputeByCid_TestException() throws OpampException {

		Map<String, Integer> defaultResponse = new HashMap<>(1);
		defaultResponse.put("deploymentId", 1);
		OpampRsController opampRsController = new OpampRsController();
		ComputeProcessor computeProcessor = mock(ComputeProcessor.class);
		opampRsController.setComputeProcessor(computeProcessor);
		when(computeProcessor.replaceComputeByCid(anyLong())).thenThrow(new NullPointerException("Test Exception"));
		Map<String, Integer> result = opampRsController.replaceComputeByCid(anyLong());
		assertEquals(result.size(), 1, "Failed, expected size 1 found : " + result.size());
		assertEquals(result.get("deploymentId"), Integer.valueOf(1),
				"Deployment Must have failed and returned response code: 1 , result.entrySet(): " + result.entrySet());

	}

}