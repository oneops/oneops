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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import org.mockito.Mockito;

/**
 * @author dsing17
 *
 */
public class AzureServiceBusEventsListnerTest {

	private static Logger logger = Logger.getLogger(AzureServiceBusEventsListnerTest.class);

	private String jmsQueue = "TESTQUEUE";
	private String url = "vm://localhost?broker.persistent=false";
	AzureServiceBusEventsListner azureServiceBusEventsListner;
	ConnectionFactory connectionFactory;
	AzureEventsHandler azureEventsHandler;

	@BeforeTest
	public void init() {
		azureServiceBusEventsListner = new AzureServiceBusEventsListner();
		connectionFactory = new ActiveMQConnectionFactory(url);
		azureEventsHandler = mock(AzureEventsHandler.class, Mockito.RETURNS_DEEP_STUBS);
	}

	@Test(enabled = true)
	public void testAzureServiceBusEventsListnerStartup() {
		logger.info("Testing testAzureServiceBusEventsListnerStartup()");
		try {

			sendMessageToServer();
			startListner();

		} catch (Exception e) {
			logger.error("Exception while running test case: " + e);
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

		InputStream is = ClassLoader.getSystemResourceAsStream("AzureServiceBus_Event.json");
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
