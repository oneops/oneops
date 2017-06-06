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
package com.oneops.cms.transmitter;

import javax.jms.*;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.transmitter.domain.CMSEvent;
import com.oneops.cms.transmitter.domain.EventSource;
import com.oneops.util.AbstractMessagePublisher;

public class CmsPublisher extends AbstractMessagePublisher {

    private final Logger logger = Logger.getLogger(CmsPublisher.class);

    private MessageProducer regularProducer;
    private MessageProducer priorityProducer;

    final private Gson gson = new Gson();

    @Override
    protected void createProducers(Session session) throws JMSException {
        String cmsTopic = "CMS.ALL";
        Destination destination = session.createTopic(cmsTopic);
        regularProducer = session.createProducer(destination);
        setProducerProperties(regularProducer);
        priorityProducer = session.createProducer(destination);
        setProducerProperties(priorityProducer);
        // set a higher priority for this producer. this will be used for manual
        // repair events
        priorityProducer.setPriority(6);
    }

    public void publishMessage(CMSEvent event) throws JMSException {
        MessageProducer producer = getProducer(event);
        TextMessage message = createTextMessage(event);
        producer.send(message);
        logger.info("Published msg " + getHeaders(event) + " with priority " + producer.getPriority());
        logger.debug("Published: " + message.getText());
    }

    private MessageProducer getProducer(CMSEvent event) {
        MessageProducer producer = regularProducer;
        String src = event.getHeaders().get("source");
        EventSource source = EventSource.toEventSource(src);
        if (source.equals(EventSource.opsprocedure)) {
            CmsOpsProcedure proc = (CmsOpsProcedure) event.getPayload();
            // use priorityProducer if this is an opsprocedure and not an auto
            // repair
            if (!"oneops-autorepair".equals(proc.getCreatedBy())) {
                producer = priorityProducer;
            }
        }
        return producer;
    }

    private TextMessage createTextMessage(CMSEvent event) throws JMSException {
        TextMessage message = session.createTextMessage(gson.toJson(event.getPayload()));
        for (String key : event.getHeaders().keySet()) {
            message.setStringProperty(key, event.getHeaders().get(key));
        }
        return message;
    }

    private String getHeaders(CMSEvent event) {
        return gson.toJson(event.getHeaders());
    }

    @Override
    protected void closeProducers() throws JMSException {
        regularProducer.close();
        priorityProducer.close();
    }

}