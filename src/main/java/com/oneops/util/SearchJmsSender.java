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

import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class SearchJmsSender extends ReliableExecutor<MessageData> {
	
	private Session session;
	private MessageProducer producer;
	
	public SearchJmsSender(int threadPoolSize, boolean doSyncOnRejection) {
		super(threadPoolSize, doSyncOnRejection);
	}
	
	public SearchJmsSender(int threadPoolSize) {
		super(threadPoolSize);
	}
	
	public void initialize(Session session, MessageProducer producer) {
		this.session = session;
		this.producer = producer;
	}
	
	protected TextMessage createTextMessage(MessageData data) throws JMSException {
		TextMessage message = session.createTextMessage(data.getPayload());
		Map<String, String> headers = data.getHeaders();
		for (String key : headers.keySet()) {
			message.setStringProperty(key, headers.get(key));
		}
		return message;
	}
	
	@Override
	protected boolean process(MessageData data) {
		try {
			if (session != null && producer != null) {
				TextMessage message = createTextMessage(data);
				producer.send(message);
				if (logger.isDebugEnabled()) {
					logger.debug("message published to search.stream queue: " + message.getText());	
				}
				return true;
			}
			
		} catch (Exception e) {
			logger.error("Exception occurred while sending message to search.stream", e);
		}
		return false;
		
	}

}
