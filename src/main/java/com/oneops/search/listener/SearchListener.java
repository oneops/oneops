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
package com.oneops.search.listener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.oneops.search.msg.processor.MessageProcessor;

/**
 * 
 * @author ranand
 *
 */
public class SearchListener implements MessageListener {

	private static Logger logger = Logger.getLogger(SearchListener.class);
	
	private MessageProcessor msgProcessor;
	
	@Override
	public void onMessage(Message message) {
		
		try {
	    	if (message instanceof TextMessage) { 
				logger.debug("got message: " + ((TextMessage)message).getJMSCorrelationID());
				processMessage((TextMessage)message);
	    	}
		} catch (JMSException e) {
			logger.error("JMSException in onMessage",e);
		}
	}

	/**
	 * 
	 * @param message
	 * @throws JMSException
	 */
	private void processMessage(TextMessage message) throws JMSException {
		//System.out.println(message);
		String jsonMsg = message.getText();
		String type = message.getStringProperty("type");
		//Check if message is coming from Dead Letter Queue
		if(message.getStringProperty("dlqDeliveryFailureCause") != null){
			type = "dlq";
		}
		if(type == null){type = message.getStringProperty("source");};
		String msgId = message.getStringProperty("msgId");
		if(msgId == null){msgId = message.getStringProperty("sourceId");};
		String action = message.getStringProperty("action");
		if("delete".equals(action) && "cm_ci".equals(type)){
			msgId = message.getStringProperty("sourceId");
		}
		else if("delete".equals(action) && "namespace".equals(type)){
			msgId = message.getStringProperty("sourceId");
		}
		
		if(StringUtils.isNotBlank(jsonMsg)){
			msgProcessor.processMessage(jsonMsg, type, msgId);
		}
		else{
			if(StringUtils.isNotBlank(type) && StringUtils.isNotBlank(msgId)){
				msgProcessor.deleteMessage(type, msgId);
			}
			
			logger.warn("Received blank message for message type::" + type +", id ::" + msgId);
		}
	}
	
	
	/**
	 * 
	 * @param woProcessor
	 */
	public void setMsgProcessor(MessageProcessor msgProcessor) {
		this.msgProcessor = msgProcessor;
	}
	

	/**
	 * allow it to run via cmdline
	 */
	public static void main(String[] args) throws JMSException {
		
//		if (args.length < 2) {
//			System.out.println("Usage: java -jar search.jar cluster-name index");
//			return;
//		}

		@SuppressWarnings("unused")
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"search-context.xml");		
	}

}
