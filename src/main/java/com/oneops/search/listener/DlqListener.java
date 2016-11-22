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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.oneops.search.msg.processor.es.DLQMessageProcessor;

public class DlqListener implements MessageListener {

    private static Logger logger = Logger.getLogger(DlqListener.class);

    private DLQMessageProcessor msgProcessor;

    private String getMessageId(TextMessage message) throws JMSException {
        String msgId = message.getStringProperty("msgId");
        if (msgId == null) {
        	msgId = message.getStringProperty("sourceId");
        }
        return msgId;
    }
    
    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                logger.debug("got message: " + message.getJMSCorrelationID());
                TextMessage textMessage = (TextMessage) message;
                msgProcessor.processMessage(textMessage.getText(), getMessageId(textMessage), getMessageHeaders(textMessage));
            }
        } catch (JMSException e) {
            logger.error("JMSException in onMessage", e);
        }
    }

    private Map<String, String> getMessageHeaders(TextMessage message) throws JMSException {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> names = message.getPropertyNames();
        while (names.hasMoreElements()) {
            String key = names.nextElement();
            map.put(key, message.getStringProperty(key));
        }
        return map;
   }

    /**
     * @param msgProcessor
     */
    public void setMsgProcessor(DLQMessageProcessor msgProcessor) {
        this.msgProcessor = msgProcessor;
    }
    
}
