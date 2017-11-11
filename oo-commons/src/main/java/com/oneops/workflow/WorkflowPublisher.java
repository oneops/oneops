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
package com.oneops.workflow;

import com.google.gson.Gson;
import com.oneops.util.AbstractMessagePublisher;
import com.oneops.util.MessageData;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Map;

public class WorkflowPublisher extends AbstractMessagePublisher {

	private String queue = "controller.workflow";

	private MessageProducer producer;

	private Gson gson = new Gson();

	public void init() {
		try {
			super.init();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void createProducers(Session session) throws JMSException {
		Destination destination = session.createQueue(queue);
		producer = session.createProducer(destination);
		setProducerProperties(producer);
	}

	protected TextMessage createTextMessage(MessageData data) throws JMSException {
		TextMessage message = session.createTextMessage(data.getPayload());
		Map<String, String> headers = data.getHeaders();
		if (headers != null) {
			for (String key : headers.keySet()) {
				message.setStringProperty(key, headers.get(key));
			}
		}
		return message;
	}

	public void publish(MessageData data) throws JMSException {
		super.send(producer, createTextMessage(data));
	}

	public void sendWorkflowMessage(long dpmtId, Map<String, String> headers) throws JMSException {
		WorkflowMessage wfMessage = new WorkflowMessage();
		wfMessage.setDpmtId(dpmtId);
		String message = gson.toJson(wfMessage);
		MessageData data = new MessageData();
		data.setPayload(message);
		data.setHeaders(headers);
		publish(data);
	}

	@Override
	protected void closeProducers() throws JMSException {
		producer.close();
	}

	public void destroy() {
		super.destroy();
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}
}
