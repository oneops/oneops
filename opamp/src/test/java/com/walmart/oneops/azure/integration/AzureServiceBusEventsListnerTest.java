package com.walmart.oneops.azure.integration;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
//import org.junit.Before;
//import org.junit.Test;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import org.mockito.Mockito;

public class AzureServiceBusEventsListnerTest {

	private static Logger logger = Logger.getLogger(AzureServiceBusEventsListnerTest.class);

	private String jmsQueue = "TESTQUEUE";
	private String url = "vm://localhost?broker.persistent=false";
	AzureServiceBusEventsListner azureServiceBusEventsListner;
	ConnectionFactory connectionFactory;
	AzureEventsHandler azureEventsHandler;

	//@Before
	@BeforeTest
	public void init() {
		azureServiceBusEventsListner = new AzureServiceBusEventsListner();
		connectionFactory = new ActiveMQConnectionFactory(url);
		azureEventsHandler = mock(AzureEventsHandler.class, Mockito.RETURNS_DEEP_STUBS);
	}

	@Test(enabled=true)
	public void testAzureServiceBusEventsListnerStartup() {
		logger.info("Testing testAzureServiceBusEventsListnerStartup()");
		try
		{
			
		
		sendMessageToServer();
		startListner();
		
		} catch (Exception e) {
			logger.error("Exception while running test case: "+e);
			fail();
			
		}

	}

	private void sendMessageToServer() throws JMSException, IOException {
		

			QueueConnection queueConn = (QueueConnection) connectionFactory.createConnection();
			queueConn.start();

			QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);

			Destination destination = queueSession.createQueue(jmsQueue);

			MessageProducer queueSender = queueSession.createProducer(destination);
			queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			Message message = queueSession.createTextMessage(createMessage());
			queueSender.send(message);
		
	}

	public void startListner() throws JMSException {
	

			QueueConnection queueConn = (QueueConnection) connectionFactory.createConnection();
			QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);

			Destination destination = queueSession.createQueue("TESTQUEUE");

			MessageConsumer consumer = queueSession.createConsumer(destination);

			azureServiceBusEventsListner.setAzureEventsHandler(azureEventsHandler);
			consumer.setMessageListener(azureServiceBusEventsListner);
			queueConn.start();


	}

	public String createMessage() throws IOException {

		InputStream is= ClassLoader.getSystemResourceAsStream("AzureServiceBus_TestMessage.json");
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
