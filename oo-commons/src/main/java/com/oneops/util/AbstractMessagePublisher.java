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
package com.oneops.util;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.IndentPrinter;
import org.apache.log4j.Logger;

public abstract class AbstractMessagePublisher {

	protected Logger logger = Logger.getLogger(AbstractMessagePublisher.class);

	private long timeToLive;
	private boolean persistent = true;

	protected ActiveMQConnectionFactory connFactory;
	protected Connection connection = null;
	protected Session session = null;

	private void showParameters() {
		logger.info("Connecting to URL: " + connFactory.getBrokerURL());
		logger.info("Using " + (persistent ? "persistent" : "non-persistent") + " messages");
		if (timeToLive != 0) {
			logger.info("Messages time to live " + timeToLive + " ms");
		}
	}

	protected abstract void createProducers(Session session) throws JMSException;

	protected abstract void closeProducers() throws JMSException;

	public void init() throws JMSException {
		showParameters();
		// Create the connection.
		connection = connFactory.createConnection();
		connection.start();

		// Create the session
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// Create the producer
		createProducers(session);
	}
	
	protected void setProducerProperties(MessageProducer producer) throws JMSException {
		if (persistent) {
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		} else {
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		}
		if (timeToLive != 0) {
			producer.setTimeToLive(timeToLive);
		}
	}

	public void getConnectionStats() {
		ActiveMQConnection c = (ActiveMQConnection) connection;
		c.getConnectionStats().dump(new IndentPrinter());
	}
	
	public void destroy() {
		cleanup();
	}

	public void cleanup() {
		logger.info("Closing AMQ connection");
		closeConnection();
	}

	public void closeConnection() {
		try {
			closeProducers();
			session.close();
			connection.close();
		} catch (Throwable ignore) {
		}
	}

	protected void send(MessageProducer producer, TextMessage message) throws JMSException {
		producer.send(message);
	}
	
	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	public void setConnFactory(ActiveMQConnectionFactory connFactory) {
		this.connFactory = connFactory;
	}

}
