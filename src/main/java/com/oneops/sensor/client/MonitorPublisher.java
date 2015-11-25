package com.oneops.sensor.client;

import java.util.ArrayList;

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
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;

public class MonitorPublisher {
	static Logger logger = Logger.getLogger(MonitorPublisher.class);
	
	private Gson gson = new Gson();
	
	private ActiveMQConnectionFactory connectionFactory;
	public final static String QUEUE = "sensor-mgmt";
	public final static String MSG_TYPE = "msg_type";
	public final static String MSG_TYPE_MONITOR = "sensor-monitor";
	public final static String MSG_PROP_MANIFEST_ID = "manifestId";
	public final static String MSG_PROP_CI_ID = "ciId";

	public final static String MONITOR_ACTION_UPDATE = "update";
	public final static String MONITOR_ACTION_DELETE = "delete";
	
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
	 * Inits the.
	 *
	 * @throws JMSException the jMS exception
	 */
	public void init() throws JMSException {

		connection = connectionFactory.createConnection();
		connection.start();
		
		// Create the session
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createQueue(QUEUE);
		// Create the producer.
		producer = session.createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
    }
	
    private void publishMonitor(MonitorRequest mr) throws SensorClientException {
    	
		try {
			TextMessage message = null;
			message = session.createTextMessage(gson.toJson(mr));
			
			message.setStringProperty(MSG_TYPE, MSG_TYPE_MONITOR);
			message.setLongProperty(MSG_PROP_MANIFEST_ID, mr.getManifestId());
			message.setLongProperty(MSG_PROP_CI_ID, mr.getCiId());

			producer.send(message);
	    	logger.info("Published: monitor for ciId:" + mr.getCiId() + "; manifestId:" + mr.getManifestId() + "; action:" + mr.getAction());
	    	logger.info(message.getText());
		} catch (JMSException e) {
			logger.error("caught Exception publishing",e);
			throw new SensorClientException(e);
		}
    	
    }

    
    /**
     * Publish monitor request message.
     *
     * @param woSimple the event
     * @throws SensorClientException 
     */
	public void processMonitorWo(
			CmsWorkOrderSimple woSimple) throws SensorClientException {	
		
		logger.info("Got request to add monitors for " + woSimple.rfcCi.getCiName() + "; ciId=" + woSimple.rfcCi.getCiId());
		
		if (woSimple.getPayLoad().get("RealizedAs") != null
			&& woSimple.getPayLoad().get("RealizedAs").size()>0) {
			
			long ciId = woSimple.rfcCi.getCiId();
			long manifestId = woSimple.getPayLoad().get("RealizedAs").get(0).getCiId();
			
			MonitorRequest mr = new MonitorRequest();
			mr.setCiId(ciId);
			mr.setManifestId(manifestId);
			
			if (woSimple.getRfcCi().getRfcAction().equals("add") || 
				woSimple.getRfcCi().getRfcAction().equals("update") ||
				woSimple.getRfcCi().getRfcAction().equals("replace") ) {
    			
				if (woSimple.getPayLoad().get("Environment") != null 
    				&& woSimple.getPayLoad().get("Environment").get(0).getCiAttributes().get("monitoring") != null
    				&& woSimple.getPayLoad().get("Environment").get(0).getCiAttributes().get("monitoring").equals("true")) {
    				
    				mr.setMonitoringEnabled(true);
    			} else {
    				mr.setMonitoringEnabled(false);
    			}
				
				mr.setAction(MONITOR_ACTION_UPDATE);
				if (woSimple.getPayLoad().get("WatchedBy") != null) {
					mr.setMonitors(woSimple.getPayLoad().get("WatchedBy"));
				} else {
					mr.setMonitors(new ArrayList<CmsRfcCISimple>());
				}
				publishMonitor(mr);
			} else if (woSimple.getRfcCi().getRfcAction().equals("delete")){
				mr.setAction(MONITOR_ACTION_DELETE);
				publishMonitor(mr);
			}
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
