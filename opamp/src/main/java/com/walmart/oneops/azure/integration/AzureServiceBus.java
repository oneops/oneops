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
import org.springframework.stereotype.Component;

@Component
public class AzureServiceBus {
	private static Logger logger = Logger.getLogger(AzureServiceBus.class);

	@Value("${AzureServiceBus.ConnectionString}")
	private String CONNECTION_NAME;
	@Value("${azureServiceBus.sasKeyName}")
	private String sasKeyName;
	@Value("${azureServiceBus.sasKey}")
	private String sasKey;
	@Value("${AzureServiceBus.MonitoringQueue}")
	private String QUEUE_NAME;

/*	private String CONNECTION_NAME="amqpwss://wmoneopsmonitoringdev01.servicebus.windows.net:443/$servicebus/websocket?amqp.idleTimeout=150000";
	private String QUEUE_NAME = "q.com.wm.oneops.monitoring";
	private String sasKeyName="OneOpsSharedAccessKey";	
	private String sasKey="nYMwPqMBa27yko1t7BHt6rDFGoqqCo6YE7er6VOI6vI=";
	*/
	
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
	public void init() throws JMSException, NamingException {

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
		azureServiceBusReceiveSession = azureServiceBusConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		logger.info("Starting receiver..");
		azureServiceBusReceiver = azureServiceBusReceiveSession.createConsumer(queue);

		azureServiceBusReceiver.setMessageListener(azureServiceBusEventsListner);
		azureServiceBusConnection.start();
		logger.info("Azure Service Bus Connection started");

	}
	
	public void destroy() {
		try {
			if (azureServiceBusConnection!=null) {
				azureServiceBusConnection.close();
			}
			
		} catch (JMSException e) {
			logger.error("Error while closing AzureServiceBus connection: "+e);
			e.printStackTrace();
		}
		
		
	}
}