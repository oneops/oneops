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

import com.oneops.search.msg.processor.MessageProcessor;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * @author ranand
 */
public class SearchListener implements MessageListener {

    private static Logger logger = Logger.getLogger(SearchListener.class);

    private MessageProcessor msgProcessor;

    private static String getMessageId(TextMessage message) throws JMSException {
        String msgId = message.getStringProperty("msgId");
        String type = getMessageType(message);
        if (msgId == null || ("delete".equals(message.getStringProperty("action")) && ("cm_ci".equals(type) || "namespace".equals(type)))) {
            return message.getStringProperty("sourceId");
        }
        return msgId;
    }

    private static String getMessageType(TextMessage message) throws JMSException {
        String type = message.getStringProperty("type");
        if (type == null) {
            return message.getStringProperty("source");
        }
        return type;
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                logger.debug("got message: " + message.getJMSCorrelationID());
                TextMessage textMessage = (TextMessage) message;
                msgProcessor.processMessage(textMessage.getText(), getMessageType(textMessage), getMessageId(textMessage));
            }
        } catch (JMSException e) {
            logger.error("JMSException in onMessage", e);
        }
    }


    /**
     * @param msgProcessor
     */
    public void setMsgProcessor(MessageProcessor msgProcessor) {
        this.msgProcessor = msgProcessor;
    }


    /**
     * allow it to run via cmdline
     */
    public static void main(String[] args) throws JMSException {
        new ClassPathXmlApplicationContext("search-context.xml");
    }

}
