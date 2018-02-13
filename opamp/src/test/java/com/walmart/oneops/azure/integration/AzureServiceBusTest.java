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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.apache.log4j.Logger;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 * @author dsing17
 *
 */
public class AzureServiceBusTest {

	private static Logger logger = Logger.getLogger(AzureServiceBusTest.class);
	AzureServiceBus azureServiceBus = new AzureServiceBus();
	AzureServiceBusEventsListner azureServiceBusEventsListner = new AzureServiceBusEventsListner();

	@BeforeTest
	public void before() throws Exception {

		azureServiceBus.setAzureServiceBusEventsListner(azureServiceBusEventsListner);
		ReflectionTestUtils.setField(azureServiceBus, "CONNECTION_NAME", "amqpwss://localhost:444", String.class);
		ReflectionTestUtils.setField(azureServiceBus, "sasKeyName", "TESTsasKeyName", String.class);
		ReflectionTestUtils.setField(azureServiceBus, "sasKey", "TESTsasKey", String.class);
		ReflectionTestUtils.setField(azureServiceBus, "QUEUE_NAME", "TESTQUEUE_NAME", String.class);
		ReflectionTestUtils.setField(azureServiceBus, "isAzureServiceBusIntegrationEnabled", true, boolean.class);
		
		
		org.apache.qpid.jms.JmsConnection azureServiceBusConnection = mock(org.apache.qpid.jms.JmsConnection.class, Mockito.RETURNS_DEEP_STUBS);
		org.apache.qpid.jms.JmsSession azureServiceBusReceiveSession= mock(org.apache.qpid.jms.JmsSession.class, Mockito.RETURNS_DEEP_STUBS);
		org.apache.qpid.jms.JmsMessageConsumer azureServiceBusReceiver=mock(org.apache.qpid.jms.JmsMessageConsumer.class, Mockito.RETURNS_DEEP_STUBS);
		
		azureServiceBus.setAzureServiceBusConnection(azureServiceBusConnection);
		azureServiceBus.setAzureServiceBusReceiveSession(azureServiceBusReceiveSession);
		azureServiceBus.setAzureServiceBusReceiver(azureServiceBusReceiver);


	}

	@Test(enabled=true)
	public void testAzureServiceInitializationUT() {

		try {
			azureServiceBus.init();
			assertNotNull("Test AzureSewrviceBus Connection", azureServiceBus.getAzureServiceBusConnection());
			assertNotNull(azureServiceBus.getAzureServiceBusReceiveSession());
			assertNotNull(azureServiceBus.getAzureServiceBusReceiver());

		} catch (Exception e) {
			
			logger.error("Exception while initializing Service Bus " + e);
			e.printStackTrace();
		} 


	}

}
