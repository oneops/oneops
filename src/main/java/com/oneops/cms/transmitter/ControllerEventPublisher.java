package com.oneops.cms.transmitter;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.transmitter.domain.CMSEvent;
import com.oneops.cms.transmitter.domain.EventSource;

public class ControllerEventPublisher extends MessagePublisher {

    private final String cmsTopic = "CMS.ALL";

    private MessageProducer regularProducer;
    private MessageProducer priorityProducer;

	@Override
	protected String getDestinationName() {
		return cmsTopic;
	}

	@Override
	protected String getDestinationType() {
		return "topic";
	}

	@Override
	protected Destination createDestination(Session session) throws JMSException {
		return session.createTopic(cmsTopic);
	}

	@Override
	protected void createProducers(Session session, Destination destination) throws JMSException {
		regularProducer = session.createProducer(destination);
		setProducerProperties(regularProducer);
		priorityProducer = session.createProducer(destination);
		setProducerProperties(priorityProducer);
        //set a higher priority for this producer. this will be used for manual repair events
		priorityProducer.setPriority(6);
	}

	@Override
	protected MessageProducer getProducer(CMSEvent event) {
		MessageProducer producer = regularProducer;
    	String src = event.getHeaders().get("source");
    	EventSource source = EventSource.toEventSource(src);
    	if (source.equals(EventSource.opsprocedure)) {
    		CmsOpsProcedure proc = (CmsOpsProcedure)event.getPayload();
        	//use priorityProducer if this is an opsprocedure and not an auto repair
        	if(!"oneops-autorepair".equals(proc.getCreatedBy())) {
        		producer = priorityProducer;
        	}	
    	}
    	return producer;
	}

	@Override
	protected void closeProducers() throws JMSException {
		regularProducer.close();
		priorityProducer.close();
	}

}