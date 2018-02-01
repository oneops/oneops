package com.walmart.oneops.azure.integration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.oneops.opamp.exceptions.AzureEventsHandlerException;

public class AzureServiceBusEventsListner implements MessageListener {
	private static Logger logger = Logger.getLogger(AzureServiceBusEventsListner.class);
	
	AzureEventsHandler AzureEventsHandler;
	
	public AzureEventsHandler getAzureEventsHandler() {
		return AzureEventsHandler;
	}

	public void setAzureEventsHandler(AzureEventsHandler azureEventsHandler) {
		AzureEventsHandler = azureEventsHandler;
	}

	public void onMessage(Message message) {
		logger.info("Received AzureServieBus message");

		if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) message;
			try {
				String event=textMessage.getText();
				logger.info("AzureServiceBus message contents: "+event);
				try {
					AzureEventsHandler.submitEventAction(event);
				} catch (AzureEventsHandlerException e) {
					logger.error("Eror while processing event: "+ event);
					e.printStackTrace();
				}
				
				
			} catch (JMSException e) {
				logger.error("Error while fetching message contents: "+e);
				e.printStackTrace();
			}

		} else {
			logger.error(message.getClass() + ": Message format is not accepted");

		}
		
		try {
			message.acknowledge();
		} catch (JMSException e) {
			
			logger.error(message + " "+message.getClass() );
			logger.error("Unable to acknowledge AzureServiceBusMessage: "+e);
			e.printStackTrace();
		}

	}
}
