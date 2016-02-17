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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.oneops.util.MessageData;

public class JMSConsumer implements ExceptionListener {
	
	private ActiveMQConnectionFactory connectionFactory;
	private String destinationName;
	private String destinationType;
	
	private MessageConsumer consumer;
	private Session session;
	private Connection connection;
	
	private AtomicInteger counter = new AtomicInteger(0);
	
	private LinkedList<MessageData> messages;
	private AtomicBoolean isRecording = new AtomicBoolean(false);
	
	public void init() {
		new Thread(() -> startConsumer()).start();
	}
	
	public void startConsumer() {
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination destination = null;
			if ("topic".equalsIgnoreCase(destinationType)) {
				destination = session.createTopic(destinationName);	
			}
			else {
				destination = session.createQueue(destinationName);
			}
			
			consumer = session.createConsumer(destination);
			while (true) {
				Message message = consumer.receive();

				if (message instanceof TextMessage) {
					TextMessage textMessage = (TextMessage) message;
					String text = textMessage.getText();
					//System.out.println("Received: " + text);
					counter.incrementAndGet();
					if (isRecording.get()) {
						addData(message, text);
					}
				} 
			}

		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			terminate();
		}
	}
	
	private void addData(Message message, String text) throws JMSException {
		MessageData data = new MessageData();
		data.setPayload(text);
		Map<String, String> headers = new HashMap<String, String>();
		Enumeration<String> names = message.getPropertyNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			String value = message.getStringProperty(name);
			headers.put(name, value);
		}
		data.setHeaders(headers);
		messages.add(data);
	}
	
	public void startRecording() {
		messages = new LinkedList<MessageData>();
		isRecording.getAndSet(true);
	}
	
	public void stopRecording() {
		messages = null;
		isRecording.getAndSet(false);
	}
	
	public void terminate() {
		try {
			consumer.close();
			session.close();
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void reset() {
		counter.getAndSet(0);
		messages = new LinkedList<MessageData>();
	}

	@Override
	public void onException(JMSException exception) {
		exception.printStackTrace();
	}

	public int getCounter() {
		return counter.get();
	}

	public void setConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	public void setDestinationType(String destinationType) {
		this.destinationType = destinationType;
	}

	public LinkedList<MessageData> getMessages() {
		return messages;
	}

}
