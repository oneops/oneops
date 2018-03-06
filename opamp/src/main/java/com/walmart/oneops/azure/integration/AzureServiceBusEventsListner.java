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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.jms.JmsException;

import com.oneops.opamp.exceptions.AzureEventsHandlerException;

/**
 * @author dsing17
 *
 */
public class AzureServiceBusEventsListner implements MessageListener {
	private static Logger logger = Logger.getLogger(AzureServiceBusEventsListner.class);

	AzureEventsHandler azureEventsHandler;

	public void onMessage(Message message) {
		logger.info("Received AzureServieBus message");

		try {
			if (message instanceof TextMessage) {
				processMessage(message);
			} else {
				logger.error(message.getClass() + ": Message format is not accepted");

			}

			message.acknowledge();
		} catch (JMSException e) {
			logger.error("Eror while processing event: ", e);
		} catch (Exception e) {

			logger.error(message + " " + message.getClass());
			logger.error("Unable to acknowledge AzureServiceBusMessage: ", e);
			e.printStackTrace();
		}

	}

	private void processMessage(Message message) {
		TextMessage textMessage = (TextMessage) message;
		String event = new String();
		try {
			event = textMessage.getText();
			logger.debug("AzureServiceBus message contents: " + event);
			azureEventsHandler.submitEventAction(event);
		} catch (JMSException e) {
			logger.error("Error while processing event: :" + event, e);
		}

	}

	public AzureEventsHandler getAzureEventsHandler() {
		return azureEventsHandler;
	}

	public void setAzureEventsHandler(AzureEventsHandler azureEventsHandler) {
		this.azureEventsHandler = azureEventsHandler;
	}
}
