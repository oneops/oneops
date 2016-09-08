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
