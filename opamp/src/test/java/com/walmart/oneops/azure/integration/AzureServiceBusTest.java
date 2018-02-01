package com.walmart.oneops.azure.integration;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;


public class AzureServiceBusTest {


	AzureServiceBus azureServiceBus = new AzureServiceBus();
	AzureServiceBusEventsListner azureServiceBusEventsListner = new AzureServiceBusEventsListner();

	@Before
	public void before() throws Exception {

		azureServiceBus.setAzureServiceBusEventsListner(azureServiceBusEventsListner);
		ReflectionTestUtils.setField(azureServiceBus, "CONNECTION_NAME", "amqpwss://localhost:444", String.class);
		ReflectionTestUtils.setField(azureServiceBus, "sasKeyName", "TESTsasKeyName", String.class);
		ReflectionTestUtils.setField(azureServiceBus, "sasKey", "TESTsasKey", String.class);
		ReflectionTestUtils.setField(azureServiceBus, "QUEUE_NAME", "TESTQUEUE_NAME", String.class);
		
		
		
		org.apache.qpid.jms.JmsConnection azureServiceBusConnection = mock(org.apache.qpid.jms.JmsConnection.class, Mockito.RETURNS_DEEP_STUBS);
		org.apache.qpid.jms.JmsSession azureServiceBusReceiveSession= mock(org.apache.qpid.jms.JmsSession.class, Mockito.RETURNS_DEEP_STUBS);
		org.apache.qpid.jms.JmsMessageConsumer azureServiceBusReceiver=mock(org.apache.qpid.jms.JmsMessageConsumer.class, Mockito.RETURNS_DEEP_STUBS);
		
		azureServiceBus.setAzureServiceBusConnection(azureServiceBusConnection);
		azureServiceBus.setAzureServiceBusReceiveSession(azureServiceBusReceiveSession);
		azureServiceBus.setAzureServiceBusReceiver(azureServiceBusReceiver);


	}

	@Test
	public void testAzureServiceInitializationUT() {

		
		System.out.println(azureServiceBus);
		try {
			azureServiceBus.init();
			assertNotNull("Test AzureSewrviceBus Connection", azureServiceBus.getAzureServiceBusConnection());
			assertNotNull(azureServiceBus.getAzureServiceBusReceiveSession());
			assertNotNull(azureServiceBus.getAzureServiceBusReceiver());

		} catch (JMSException e) {
			
			System.out.println("Exception while initializing Service Bus " + e);
			e.printStackTrace();
		} catch (NamingException e) {
			
			System.out.println("Exception while initializing Service Bus " + e);
			e.printStackTrace();
		}

	}

}
