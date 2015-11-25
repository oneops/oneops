package com.oneops.sensor.jms;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.ops.events.CiChangeStateEvent;

/**
 * The Class OpsEventPublisher.
 */
public class OpsEventPublisher {
	
	static Logger logger = Logger.getLogger(OpsEventPublisher.class);
	
	private Gson gson = new Gson();
	
	private ActiveMQConnectionFactory connectionFactory;
	private String queue = "ops-ci-states";
	private boolean persistent = true;
	private long timeToLive = 0;
	
	private Connection connection = null;
	private Session session = null; 
	private MessageProducer producer = null;

	/**
	 * Sets the connection factory.
	 *
	 * @param connectionFactory the new connection factory
	 */
	public void setConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * Sets the queue.
	 *
	 * @param queue the new queue
	 */
	public void setQueue(String queue) {
		this.queue = queue;
	}

	/**
	 * Sets the persistent.
	 *
	 * @param persistent the new persistent
	 */
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	/**
	 * Sets the time to live.
	 *
	 * @param timeToLive the new time to live
	 */
	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}
	
	/**
	 * Inits the.
	 *
	 * @throws JMSException the jMS exception
	 */
	public void init() throws JMSException {

		connection = connectionFactory.createConnection();
		connection.start();
		
		// Create the session
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createQueue(queue);
		// Create the producer.
		producer = session.createProducer(destination);
		
		if (persistent) {
		    producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		} else {
		    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		}
		if (timeToLive != 0) {
		    producer.setTimeToLive(timeToLive);
		}
    }
	
    /**
     * Publish ci state message.
     *
     * @param event the event
     */
    public void publishCiStateMessage(CiChangeStateEvent event) {
    	
		try {
			TextMessage message = session.createTextMessage(gson.toJson(event));
	    	message.setLongProperty("ciId", event.getCiId());
	    	message.setStringProperty("type", "ci-change-state");
	    	message.setStringProperty("ciState", event.getNewState());
	    	producer.send(message);
	    	logger.info("Published: ciId:" + event.getCiId());
	    	logger.info(message.getText());
		} catch (JMSException e) {
			// TODO see if we can put some durability here
			logger.error("caught Exception publishing",e);
		}
    	
    }

    /**
     * Cleanup.
     */
    public void cleanup() {
    	logger.info("Closing AMQ connection");
    	closeConnection();
    }
    
    /**
     * Close connection.
     */
    public void closeConnection() {
        try {
        	producer.close();
        	session.close();
        	connection.close();
        } catch (Throwable ignore) {
        }
    }
	
}
