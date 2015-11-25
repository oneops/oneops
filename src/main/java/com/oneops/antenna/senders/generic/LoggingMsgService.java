/*
 * Copyright 2014-2015 WalmartLabs.
 */
package com.oneops.antenna.senders.generic;

import org.apache.log4j.Logger;

import com.oneops.antenna.domain.BasicSubscriber;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.senders.NotificationSender;

/**
 * Simple implementation that just logs the message
 */
public class LoggingMsgService implements NotificationSender {

    private static Logger logger = Logger.getLogger(LoggingMsgService.class);

    /**
     * Just writes to the logger
     */
    @Override
    public boolean postMessage(NotificationMessage msg, BasicSubscriber subscriber) {

        StringBuilder sb = new StringBuilder("postMessage for Subscriber[");
        sb.append(subscriber.getClass().getName()).append("] Message [")
                .append(" -nsPath: ").append(msg.getNsPath())
                .append(" -source: ").append(msg.getSource())
                .append(" -subject: ").append(msg.getSubject())
                .append(" -text: ").append(msg.getText())
                .append(" -type: ").append(msg.getType().toString())
                .append(" -severity: ").append(msg.getSeverity().toString())
                .append(" -templateName: ").append(msg.getTemplateName())
                .append(" -timestamp: ").append(msg.getTimestamp())
                .append("]");
        logger.info(sb.toString());
        return true;
    }

}
