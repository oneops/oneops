package com.oneops.cms.transmitter;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import com.oneops.cms.transmitter.domain.CMSEvent;

public class CIEventPublisher extends MessagePublisher {

    private final String searchQueue = "search.stream";

    private MessageProducer producer = null;

	@Override
	protected String getDestinationName() {
		return searchQueue;
	}

	@Override
	protected String getDestinationType() {
		return "queue";
	}

	@Override
	protected Destination createDestination(Session session) throws JMSException {
		return session.createQueue(searchQueue);
	}

	@Override
	protected void createProducers(Session session, Destination destination) throws JMSException {
		producer = session.createProducer(destination);
		setProducerProperties(producer);
	}

	@Override
	protected MessageProducer getProducer(CMSEvent event) {
		return producer;
	}

	@Override
	protected void closeProducers() throws JMSException {
		producer.close();
	}

}