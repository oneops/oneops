package com.walmart.oneops.azure.integration;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.junit.Before;
//import org.junit.Test;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.opamp.exceptions.AzureEventsHandlerException;
import com.oneops.opamp.service.ComputeService;

public class AzureEventsHandlerTest {

	String event;

	//@Before
	@BeforeTest
	public void init() throws IOException {
		event = createMessage();

	}

	@Test(enabled=true)
	public void testAzureEventsHandler_submitEventAction() {

		AzureEventsHandler azureEventsHandler = new AzureEventsHandler();
		CmsCmManager cmManager = mock(com.oneops.cms.cm.service.CmsCmManagerImpl.class);
		ComputeService computeService = mock(com.oneops.opamp.service.ComputeService.class);
		ObjectMapper objectMapper = new ObjectMapper();

		azureEventsHandler.setCmManager(cmManager);
		azureEventsHandler.setComputeService(computeService);
		azureEventsHandler.setObjectMapper(objectMapper);

		List<CmsCI> ciList = new ArrayList<CmsCI>();
		CmsCI cmsCI = new CmsCI();
		long cId=999L;
		cmsCI.setCiId(cId);
		ciList.add(cmsCI);

		when(cmManager.getCiByAttributes(eq("/"), eq(null), anyList(), eq(true))).thenReturn(ciList);
		
		Map<String, Integer> replaceComputeByCidResponeMap= new HashMap<String, Integer>(1);
		replaceComputeByCidResponeMap.put("deploymentId", 0);
		when(computeService.replaceComputeByCid(cId)).thenReturn(replaceComputeByCidResponeMap);

		try {
			azureEventsHandler.submitEventAction(event);

		} catch (AzureEventsHandlerException e) {
			fail();

			e.printStackTrace();
		}

	}

	@Test(enabled=true)
	public void testAzureEventsHandler_parseResourceIdFromEvent() {

		AzureEventsHandler azureEventsHandler = new AzureEventsHandler();

		ObjectMapper objectMapper = new ObjectMapper();
		
		azureEventsHandler.setObjectMapper(objectMapper);
		try {
			String resourceId = azureEventsHandler.parseResourceIdFromEvent(event);

			assertEquals(resourceId,
					"/subscriptions/dc9b1404-d1f8-42be-af6c-b2a6d307d094/resourceGroups/boititba-TestAzureMonitor-304243215-DEV-eus2/providers/Microsoft.Compute/virtualMachines/tomcat-DEV-TestAzureMonitoring-boititba-304249915");
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}

	}

	
	@Test(enabled=true)
	public void testAzureEventsHandler_getCidForAzureResourceID() {

		AzureEventsHandler azureEventsHandler;
		CmsCmManager cmManager;
		ComputeService computeService;
		ObjectMapper objectMapper;

		cmManager = mock(com.oneops.cms.cm.service.CmsCmManagerImpl.class);
		computeService = mock(com.oneops.opamp.service.ComputeService.class);
		objectMapper = new ObjectMapper();

		azureEventsHandler = new AzureEventsHandler();
		azureEventsHandler.setCmManager(cmManager);
		azureEventsHandler.setComputeService(computeService);
		azureEventsHandler.setObjectMapper(objectMapper);

		List<CmsCI> ciList = new ArrayList<CmsCI>();
		CmsCI cmsCI = new CmsCI();
		long expectedCiD = 999L;
		cmsCI.setCiId(expectedCiD);
		ciList.add(cmsCI);

		when(cmManager.getCiByAttributes(eq("/"), eq(null), anyList(), eq(true))).thenReturn(ciList);

		String resourceId = "/subscriptions/dc9b1404-d1f8-42be-af6c-b2a6d307d094/resourceGroups/boititba-TestAzureMonitor-304243215-DEV-eus2/providers/Microsoft.Compute/virtualMachines/tomcat-DEV-TestAzureMonitoring-boititba-304249915";

		try {
			List<CmsCI> cidList = azureEventsHandler.getCidForAzureResourceID(resourceId);

			assertEquals(cidList.get(0).getCiId(), expectedCiD);
		} catch (Exception e) {
			fail();

			e.printStackTrace();
		}

	}

	public String createMessage() throws IOException {

		InputStream is = ClassLoader.getSystemResourceAsStream("AzureServiceBus_TestMessage.json");
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		String line = buf.readLine();
		StringBuilder sb = new StringBuilder();
		while (line != null) {
			sb.append(line).append("\n");
			line = buf.readLine();
		}
		String fileAsString = sb.toString();
		System.out.println("Contents : " + fileAsString);
		buf.close();
		return fileAsString;

	}

}
