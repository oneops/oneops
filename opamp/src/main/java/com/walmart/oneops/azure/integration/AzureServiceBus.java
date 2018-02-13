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

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author dsing17
 *
 */

public class AzureServiceBus {
	private static Logger logger = Logger.getLogger(AzureServiceBus.class);

	@Value("${AzureServiceBus.ConnectionString:}")
	private String CONNECTION_NAME;
	@Value("${AzureServiceBus.SasKeyName:}")
	private String sasKeyName;
	@Value("${AzureServiceBus.SasKey:}")
	private String sasKey;
	@Value("${AzureServiceBus.MonitoringQueue:}")
	private String QUEUE_NAME;
	@Value("${AzureServiceBus.IsAzureServiceBusIntegrationEnabled:false}")
	private boolean isAzureServiceBusIntegrationEnabled;
	

	private String CONNECTION_JNDI_NAME = "azureServiceBusConnectionString";
	private String QUEUE_JNDI_NAME = "QUEUE";
	private String INITIAL_CONTEXT_FACTORY_NAME = "org.apache.qpid.jms.jndi.JmsInitialContextFactory";

	private Connection azureServiceBusConnection;
	private Session azureServiceBusReceiveSession;
	private MessageConsumer azureServiceBusReceiver;

	private AzureServiceBusEventsListner azureServiceBusEventsListner;

	public Connection getAzureServiceBusConnection() {
		return azureServiceBusConnection;
	}

	public void setAzureServiceBusConnection(Connection azureServiceBusConnection) {
		this.azureServiceBusConnection = azureServiceBusConnection;
	}

	public Session getAzureServiceBusReceiveSession() {
		return azureServiceBusReceiveSession;
	}

	public void setAzureServiceBusReceiveSession(Session azureServiceBusReceiveSession) {
		this.azureServiceBusReceiveSession = azureServiceBusReceiveSession;
	}

	public AzureServiceBusEventsListner getAzureServiceBusEventsListner() {
		return azureServiceBusEventsListner;
	}

	public void setAzureServiceBusEventsListner(AzureServiceBusEventsListner azureServiceBusEventsListner) {
		this.azureServiceBusEventsListner = azureServiceBusEventsListner;
	}

	public MessageConsumer getAzureServiceBusReceiver() {
		return azureServiceBusReceiver;
	}

	public void setAzureServiceBusReceiver(MessageConsumer azureServiceBusReceiver) {
		this.azureServiceBusReceiver = azureServiceBusReceiver;
	}

	/**
	 * @throws JMSException
	 * @throws NamingException
	 */

	public void init() {

		try {
			if (isAzureServiceBusIntegrationEnabled) {

				logger.info("intializing Azure Service Bus...");

				Properties properties = new Properties();
				properties.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY_NAME);

				properties.put("connectionfactory." + CONNECTION_JNDI_NAME, CONNECTION_NAME);
				properties.put("queue." + QUEUE_JNDI_NAME, QUEUE_NAME);

				logger.info("Iniializing Context..");
				Context context = new InitialContext(properties);
				logger.info("Iniialized Context..");

				ConnectionFactory cf = (ConnectionFactory) context.lookup(CONNECTION_JNDI_NAME);

				logger.info("Created connection Factory..");
				Destination queue = (Destination) context.lookup(QUEUE_JNDI_NAME);

				logger.info("Created connection..");
				azureServiceBusConnection = cf.createConnection(sasKeyName, sasKey);

				logger.info("Starting Receive session..");
				azureServiceBusReceiveSession = azureServiceBusConnection.createSession(false,
						Session.CLIENT_ACKNOWLEDGE);
				logger.info("Starting receiver..");
				azureServiceBusReceiver = azureServiceBusReceiveSession.createConsumer(queue);

				azureServiceBusReceiver.setMessageListener(azureServiceBusEventsListner);
				azureServiceBusConnection.start();
				logger.info("Azure Service Bus Connection started");
			} else {
				logger.warn("Azure Service Bus integration is disabled.");

			}

		} catch (Exception e) {
			logger.warn("Error while initializing Azure Service Bus", e);
		}

	}

	public void destroy() {
		try {
			if (azureServiceBusConnection != null) {
				azureServiceBusConnection.close();
			}

		} catch (JMSException e) {
			logger.error("Error while closing AzureServiceBus connection: ", e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Error while closing AzureServiceBus connection: ", e);
			e.printStackTrace();
		}

	}
}