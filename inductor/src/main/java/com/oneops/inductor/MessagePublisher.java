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
package com.oneops.inductor;

import java.util.Map;
import javax.jms.*;
import javax.jms.IllegalStateException;

import org.apache.activemq.ActiveMQConnection;
import org.springframework.jms.connection.CachingConnectionFactory;

import com.oneops.util.ReliableExecutor;

import org.apache.activemq.util.IndentPrinter;
import org.apache.log4j.Logger;

public class MessagePublisher extends ReliableExecutor<MessageHolder> {

	static Logger logger = Logger.getLogger(MessagePublisher.class);

	private long timeToLive;
	private boolean persistent = true;
	private String topic = null;

	private CachingConnectionFactory connectionFactory = null;
	private Connection connection = null;
	private Session session = null;
	private MessageProducer regularProducer = null;
	private MessageProducer priorityProducer = null;

	public void init() {
		try {
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination queue = session.createQueue(topic);
			regularProducer = session.createProducer(queue);
			priorityProducer = session.createProducer(queue);
			priorityProducer.setPriority(6);

			if (persistent) {
				regularProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
				priorityProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
			} else {
				regularProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				priorityProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			}
			if (timeToLive != 0) {
				regularProducer.setTimeToLive(timeToLive);
				priorityProducer.setTimeToLive(timeToLive);
			}
		} catch (JMSException e) {
			logger.error(e.getMessage());
			logger.error(e.getMessage(), e);
		}
		super.init();
	}

	@Override
	protected boolean process(MessageHolder holder) {
		Map<String, String> event = holder.getMap();
		try {
			TextMessage message = session.createTextMessage(event.get("body"));
			message.setJMSCorrelationID(event.get("correlationID"));

			for (Map.Entry<String, String> kv : event.entrySet()) {
				if (!kv.getKey().equals("body")) {
					message.setStringProperty(kv.getKey(), kv.getValue());
				}
			}

			MessageProducer producer = regularProducer;
			if ("high".equals(event.get("priority"))) {
				producer = priorityProducer;
				logger.debug("using priority producer to publish message");
			}
			
			producer.send(message);
			logger.debug("Published: " + message.toString());
			return true;
		} catch (NullPointerException npe) {
			// happens when amq session is null
			logger.warn("caught NullPointerException - reconnecting to broker");
			waitSome();
			init();
			return false;

		} catch (IllegalStateException e) {
			// this happens when connection is hosed - lets re-init
			logger.warn("caught IllegalStateException - reconnecting to broker");
			init();
			return false;

		} catch (JMSException e) {
			logger.error(e.getMessage());
			logger.debug(e.getMessage(), e);
			return false;
		}
	}
	
	
	private void waitSome() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	public void publishMessage(Map<String, String> event) {
		this.executeSync(new MessageHolder(event));
	}

	public void getConnectionStats() {
		ActiveMQConnection c = (ActiveMQConnection) connection;
		c.getConnectionStats().dump(new IndentPrinter());
	}

	public void cleanup() {
		System.out.println("Closing AMQ connection");
		closeConnection();
	}

	public void closeConnection() {
		try {
			regularProducer.close();
			priorityProducer.close();
			session.close();
			connection.close();
		} catch (Exception ignore) {
		}
	}

	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	public void setConnectionFactory(CachingConnectionFactory cf) {
		this.connectionFactory = cf;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

}
