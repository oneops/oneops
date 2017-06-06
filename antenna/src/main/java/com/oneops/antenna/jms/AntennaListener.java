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
package com.oneops.antenna.jms;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import com.codahale.metrics.*;
import org.apache.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.service.Dispatcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import static com.codahale.metrics.MetricRegistry.name;
import static com.codahale.metrics.Timer.Context;
import static com.oneops.metrics.OneOpsMetrics.*;
import static com.oneops.util.URLUtil.*;

/**
 * The listener interface for receiving antenna events. The class that is interested in
 * processing a antenna event implements this interface, and the object created with that
 * class is registered with a component using the component's <code>addAntennaListener<code>
 * method. When the antenna event occurs, that object's appropriate method is invoked.
 */
public class AntennaListener implements MessageListener {

    private static Logger logger = Logger.getLogger(AntennaListener.class);
    private DefaultMessageListenerContainer dmlc;
    private Dispatcher dispatcher;
    private Gson gson;
    private MetricRegistry metrics;
    private Meter msgs;
    private Timer msgTime;

    @Autowired
    public AntennaListener(Dispatcher dispatcher,
                           Gson gson,
                           MetricRegistry metrics) {
        this.dispatcher = dispatcher;
        this.gson = gson;
        this.metrics = metrics;
    }

    @Autowired
    public void setDmlc(DefaultMessageListenerContainer dmlc) {
        this.dmlc = dmlc;
    }

    /**
     * Post construct initialization
     */
    @PostConstruct
    public void init() {
        logger.info("OneOps Base URL: " + ONEOPS_BASE_URL);
        logger.info(this);
        metricInit();
    }

    /**
     * Antenna metrics measuring instruments.
     */
    private void metricInit() {
        msgs = metrics.meter(name(ANTENNA, "msg.count"));
        msgTime = metrics.timer(name(ANTENNA, "msg.time"));
        metrics.register(name(ANTENNA, "dmlc.active.consumers"), (Gauge<Integer>) dmlc::getActiveConsumerCount);
    }


    /**
     * (non-Javadoc)
     *
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message msg) {
        msgs.mark();
        Context tc = msgTime.time();
        try {
            NotificationMessage notify = null;
            if (msg instanceof TextMessage) {
                try {
                    notify = parseMsg((TextMessage) msg);
                } catch (JsonParseException e) {
                    logger.error("Got the bad message, not a valid json format - \n"
                            + ((TextMessage) msg).getText() + "\n" + e.getMessage());
                    msg.acknowledge();
                }
            } else if (msg instanceof ObjectMessage) {
                notify = (NotificationMessage) ((ObjectMessage) msg).getObject();
            }
            if (notify != null) {
                logger.debug("Notification message received: " + notify.getText());
                if (notify.getTimestamp() == 0) {
                    notify.setTimestamp(System.currentTimeMillis());
                }
                dispatcher.dispatch(notify);
            }
            msg.acknowledge();

        } catch (Exception ex) {
            logger.error("Can't process the notification message.", ex);
        } finally {
            tc.stop();
        }
    }

    /**
     * Parse json  from JMS text message
     *
     * @param message JMS message
     * @return parsed notification message
     * @throws JMSException
     */
    private NotificationMessage parseMsg(TextMessage message) throws JMSException {
        return gson.fromJson(message.getText(), NotificationMessage.class);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Antenna-DMLC {");
        sb.append("ActiveConsumers=").append(dmlc.getActiveConsumerCount());
        sb.append(", ConcurrentConsumers=").append(dmlc.getConcurrentConsumers());
        sb.append(", MaxConcurrentConsumers=").append(dmlc.getMaxConcurrentConsumers());
        sb.append(", ScheduledConsumerCount=").append(dmlc.getScheduledConsumerCount());
        sb.append(", IdleConsumerLimit=").append(dmlc.getIdleConsumerLimit());
        sb.append(", MaxMessagesPerTask=").append(dmlc.getMaxMessagesPerTask());
        sb.append(", Destination=").append(dmlc.getDestination());
        sb.append('}');
        return sb.toString();
    }
}
