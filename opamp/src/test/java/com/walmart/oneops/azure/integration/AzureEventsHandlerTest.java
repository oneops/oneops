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
package com.walmart.oneops.azure.integration;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
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

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.opamp.exceptions.AzureEventsHandlerException;
import com.oneops.opamp.service.BadStateProcessor;
import com.oneops.opamp.util.IConstants;

/**
 * @author dsing17
 *
 */
public class AzureEventsHandlerTest {
	private static Logger logger = Logger.getLogger(AzureEventsHandlerTest.class);
	@Test(enabled = true)
	public void testAzureEventsHandler_submitEventAction() {

		AzureEventsHandler azureEventsHandler = new AzureEventsHandler();
		CmsCmManager cmManager = mock(com.oneops.cms.cm.service.CmsCmManagerImpl.class);

		BadStateProcessor bsProcessor = mock(com.oneops.opamp.service.BadStateProcessor.class);

		List<CmsCI> ciList = new ArrayList<CmsCI>();
		CmsCI cmsCI = new CmsCI();
		long cId = 999L;
		cmsCI.setCiId(cId);
		ciList.add(cmsCI);

		when(cmManager.getCiByAttributes(eq("/"), eq(null), anyList(), eq(true))).thenReturn(ciList);
		azureEventsHandler.setCmManager(cmManager);

		Map<String, Integer> replaceByCidResponeMap = new HashMap<String, Integer>(1);
		replaceByCidResponeMap.put("deploymentId", 0);

		when(bsProcessor.replaceByCid(anyLong(), anyString(), anyString())).thenReturn(replaceByCidResponeMap);
		azureEventsHandler.setBsProcessor(bsProcessor);

		ObjectMapper objectMapper = new ObjectMapper();
		azureEventsHandler.setObjectMapper(objectMapper);

		try {
			azureEventsHandler.submitEventAction(createMessage("AzureServiceBus_Event.json"));

		} catch (AzureEventsHandlerException e) {
			fail();

			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}

	}

	@Test(enabled = true)
	public void testAzureEventsHandler_parseEventForEventAttributes() {

		try {
			String event = createMessage("AzureServiceBus_Event.json");
			AzureEventsHandler azureEventsHandler = new AzureEventsHandler();

			ObjectMapper objectMapper = new ObjectMapper();

			azureEventsHandler.setObjectMapper(objectMapper);

			String resourceId = azureEventsHandler.parseEventForEventAttribute(event,
					IConstants.AzureServiceBus_Event_attribute_resourceId);
			String status = azureEventsHandler.parseEventForEventAttribute(event,
					IConstants.AzureServiceBus_Event_attribute_status);
			String resourceProviderName = azureEventsHandler.parseEventForEventAttribute(event,
					IConstants.AzureServiceBus_Event_attribute_resourceProviderName);

			assertEquals(resourceId,
					"/subscriptions/dc9b1404-d1f8-42be-af6c-b2a6d307d094/resourceGroups/boititba-TestAzureMonitor-304243215-DEV-eus2/providers/Microsoft.Compute/virtualMachines/tomcat-DEV-TestAzureMonitoring-boititba-304249915");

			assertEquals(status, IConstants.AzureServiceBus_Event_attribute_status_failed);
			assertEquals(resourceProviderName, IConstants.AzureServiceBus_Event_attribute_resourceProviderName_Value);

		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}

	}

	@Test(enabled = true)
	public void testAzureEventsHandler_getCidForAzureResourceID() {

		AzureEventsHandler azureEventsHandler;
		CmsCmManager cmManager;
		ObjectMapper objectMapper;

		cmManager = mock(com.oneops.cms.cm.service.CmsCmManagerImpl.class);
		List<CmsCI> ciList = new ArrayList<CmsCI>();
		CmsCI cmsCI = new CmsCI();
		long expectedCiD = 999L;
		cmsCI.setCiId(expectedCiD);
		ciList.add(cmsCI);
		when(cmManager.getCiByAttributes(eq("/"), eq(null), anyList(), eq(true))).thenReturn(ciList);

		BadStateProcessor bsProcessor = mock(com.oneops.opamp.service.BadStateProcessor.class);

		objectMapper = new ObjectMapper();

		azureEventsHandler = new AzureEventsHandler();
		azureEventsHandler.setCmManager(cmManager);
		azureEventsHandler.setObjectMapper(objectMapper);
		azureEventsHandler.setBsProcessor(bsProcessor);
		String resourceId = "/subscriptions/dc9b1404-d1f8-42be-af6c-b2a6d307d094/resourceGroups/boititba-TestAzureMonitor-304243215-DEV-eus2/providers/Microsoft.Compute/virtualMachines/tomcat-DEV-TestAzureMonitoring-boititba-304249915";

		try {
			List<CmsCI> cidList = azureEventsHandler.getCidForAzureResourceID(resourceId);

			assertEquals(cidList.get(0).getCiId(), expectedCiD);
		} catch (Exception e) {
			fail();

			e.printStackTrace();
		}

	}

	@Test(enabled = true)
	public void testAzureEventsHandler_submitEventAction_WithEventAttributeStatus_NotFailed() {

		AzureEventsHandler azureEventsHandler = new AzureEventsHandler();
		CmsCmManager cmManager = mock(com.oneops.cms.cm.service.CmsCmManagerImpl.class);

		List<CmsCI> ciList = new ArrayList<CmsCI>();
		CmsCI cmsCI = new CmsCI();
		long cId = 999L;
		cmsCI.setCiId(cId);
		ciList.add(cmsCI);

		when(cmManager.getCiByAttributes(eq("/"), eq(null), anyList(), eq(true))).thenReturn(ciList);
		azureEventsHandler.setCmManager(cmManager);

		Map<String, Integer> replaceByCidResponeMap = new HashMap<String, Integer>(1);
		replaceByCidResponeMap.put("deploymentId", 0);

		BadStateProcessor bsProcessor = mock(com.oneops.opamp.service.BadStateProcessor.class);
		when(bsProcessor.replaceByCid(anyLong(), anyString(), anyString()))
				.thenThrow(new RuntimeException("Cid replacement method should not have been called"));
		azureEventsHandler.setBsProcessor(bsProcessor);

		ObjectMapper objectMapper = new ObjectMapper();
		azureEventsHandler.setObjectMapper(objectMapper);

		try {
			azureEventsHandler.submitEventAction(createMessage("AzureServiceBus_Event_Status_NotFailed.json"));

		} catch (AzureEventsHandlerException e) {
			fail();

			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}

	}

	@Test(enabled = true)
	public void testAzureEventsHandler_submitEventAction_WithEventAttribute_ResourceProviderName_NotMS() {

		AzureEventsHandler azureEventsHandler = new AzureEventsHandler();
		CmsCmManager cmManager = mock(com.oneops.cms.cm.service.CmsCmManagerImpl.class);

		BadStateProcessor bsProcessor = mock(com.oneops.opamp.service.BadStateProcessor.class);

		List<CmsCI> ciList = new ArrayList<CmsCI>();
		CmsCI cmsCI = new CmsCI();
		long cId = 999L;
		cmsCI.setCiId(cId);
		ciList.add(cmsCI);

		when(cmManager.getCiByAttributes(eq("/"), eq(null), anyList(), eq(true))).thenReturn(ciList);
		azureEventsHandler.setCmManager(cmManager);

		Map<String, Integer> replaceByCidResponeMap = new HashMap<String, Integer>(1);
		replaceByCidResponeMap.put("deploymentId", 0);

		when(bsProcessor.replaceByCid(anyLong(), anyString(), anyString()))
				.thenThrow(new RuntimeException("Cid replacement method should not have been called"));
		azureEventsHandler.setBsProcessor(bsProcessor);

		ObjectMapper objectMapper = new ObjectMapper();
		azureEventsHandler.setObjectMapper(objectMapper);

		try {
			azureEventsHandler
					.submitEventAction(createMessage("AzureServiceBus_Event_resourceProviderName_NotMS.json"));

		} catch (AzureEventsHandlerException e) {
			fail();

			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}

	}

	public String createMessage(String testEventfileName) throws IOException {

		InputStream is = ClassLoader.getSystemResourceAsStream(testEventfileName);
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		String line = buf.readLine();
		StringBuilder sb = new StringBuilder();
		while (line != null) {
			sb.append(line).append("\n");
			line = buf.readLine();
		}
		String fileAsString = sb.toString();
		logger.info("Contents : " + fileAsString);
		buf.close();
		return fileAsString;

	}

}
