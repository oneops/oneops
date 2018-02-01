package com.oneops.opamp.rs;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.oneops.opamp.service.ComputeService;

public class OpampRsControllerTest {

	@Test
	public void OpampRsController_replaceComputeByCid_TestFunction()  {

		OpampRsController opampRsController = new OpampRsController();
		opampRsController.setComputeService(mock(ComputeService.class));
		Map<String, Integer> result = opampRsController.replaceComputeByCid(anyLong());
		assertEquals(result.size(), 0, "Failed, expected size 0 found : " + result.size());

	}

	@Test
	public void OpampRsController_replaceComputeByCid_TestValidResponse()  {

		Map<String, Integer> defaultResponse = new HashMap<>(1);
		defaultResponse.put("deploymentId", 0);
		OpampRsController opampRsController = new OpampRsController();
		ComputeService computeService = mock(ComputeService.class);
		opampRsController.setComputeService(computeService);
		when(computeService.replaceComputeByCid(anyLong())).thenReturn(defaultResponse);
		Map<String, Integer> result = opampRsController.replaceComputeByCid(anyLong());
		assertEquals(result.size(), 1, "Failed, expected size 0 found : " + result.size());

	}

	@Test
	public void OpampRsController_replaceComputeByCid_TestException() {

		Map<String, Integer> defaultResponse = new HashMap<>(1);
		defaultResponse.put("deploymentId", 1);
		OpampRsController opampRsController = new OpampRsController();
		ComputeService computeService = mock(ComputeService.class);
		opampRsController.setComputeService(computeService);
		when(computeService.replaceComputeByCid(anyLong())).thenThrow(new RuntimeException("Test Exception"));
		Map<String, Integer> result = opampRsController.replaceComputeByCid(anyLong());
		assertEquals(result.size(), 1, "Failed, expected size 1 found : " + result.size());
		assertEquals(result.get("deploymentId"), Integer.valueOf(1),
				"Deployment Must have failed and returned response code: 1 , result.entrySet(): " + result.entrySet());

	}

}