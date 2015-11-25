package com.oneops.cms.transmitter;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.IndentPrinter;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.NotificationSeverity;
import com.oneops.antenna.domain.NotificationType;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.transmitter.domain.CMSEvent;
import com.oneops.cms.transmitter.domain.EventSource;
import com.oneops.util.ReliableExecutor;


public abstract class MessagePublisher {

	static Logger logger = Logger.getLogger(MessagePublisher.class);
	
	private long timeToLive;
	
    private boolean persistent = true;
    private ActiveMQConnectionFactory connFactory;

    public void setConnFactory(ActiveMQConnectionFactory connFactory) {
        this.connFactory = connFactory;
    }

    private Connection connection = null;
    private Session session = null; 

    final private Gson gson = new Gson();

    private ReliableExecutor<NotificationMessage> antennaClient;

    private NotificationConfigurator notificationConfig;

    public void setAntennaClient(ReliableExecutor<NotificationMessage> antennaClient) {
        this.antennaClient = antennaClient;
    }

    public void setNotificationConfig(NotificationConfigurator notificationConfig) {
        this.notificationConfig = notificationConfig;
    }

    private void showParameters() {
    	logger.info("Connecting to URL: " + connFactory.getBrokerURL());
    	logger.info("Publishing a Message  to " + getDestinationType() + " : " + getDestinationName());
    	logger.info("Using " + (persistent ? "persistent" : "non-persistent") + " messages");

        if (timeToLive != 0) {
        	logger.info("Messages time to live " + timeToLive + " ms");
        }
    }

    protected abstract String getDestinationName();

    protected abstract String getDestinationType();
    
    protected abstract Destination createDestination(Session session) throws JMSException;

    protected abstract void createProducers(Session session, Destination destination) throws JMSException;
    
    protected abstract void closeProducers() throws JMSException;
    
    protected abstract MessageProducer getProducer(CMSEvent event);

    public void init() throws JMSException {
    		showParameters();
            // Create the connection.
            connection = connFactory.createConnection();
            connection.start();

            // Create the session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = createDestination(session);
            // Create the producer
            createProducers(session, destination);
    }
    
    protected void setProducerProperties(MessageProducer producer) throws JMSException {
        if (persistent) {
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        } else {
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        }
        if (timeToLive != 0) {
            producer.setTimeToLive(timeToLive);
        }
    }

    public void publishMessage(CMSEvent event) throws JMSException {

        NotificationMessage notify = createNotificationMessage(event);
        if(notify != null) {
            antennaClient.executeAsync( notify );
        }

    	TextMessage message = session.createTextMessage(gson.toJson(event.getPayload()));
    	for (String key : event.getHeaders().keySet()) {
    		message.setStringProperty(key, event.getHeaders().get(key));
    	}
    	MessageProducer producer = getProducer(event);
    	producer.send(message);
    	logger.info("Published msg " + gson.toJson(event.getHeaders()) + " with priority " + producer.getPriority());
    	logger.debug("Published: "+message.getText());
    }
    
    public void getConnectionStats() {
        ActiveMQConnection c = (ActiveMQConnection) connection;
        c.getConnectionStats().dump(new IndentPrinter());
    }

    public void cleanup() {
    	logger.info("Closing AMQ connection");
    	closeConnection();
    }
    
    public void closeConnection() {
        try {
        	closeProducers();
        	session.close();
        	connection.close();
        } catch (Throwable ignore) {
        }
    }

	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

    private NotificationMessage createNotificationMessage(CMSEvent event) {
        //no config - no notification
        if(!notificationConfig.isConfigured()) {
            return null;
        }
        NotificationMessage notify = new NotificationMessage();
        notify.setTimestamp(System.currentTimeMillis());
        String s = event.getHeaders().get("source");
        String c = event.getHeaders().get("clazzName");
        EventSource source = EventSource.toEventSource(s);
        NotificationType type = null;
        NotificationRule rule = notificationConfig.getRule(source, c);
        if(rule == null) {
            return null;
        }
        if(source.equals(EventSource.deployment)) {
            type = NotificationType.deployment;
            CmsDeployment dp = (CmsDeployment)event.getPayload();
            notify.setSubject("Deployment" );
            notify.setText("Deployment: "+dp.getDescription());
        } else if(source.equals(EventSource.opsprocedure)) {
            type = NotificationType.procedure;
            CmsOpsProcedure proc = (CmsOpsProcedure)event.getPayload();
            notify.setSubject("Procedure");
            notify.setText("Procedure: " + proc.getProcedureName() + " is " + proc.getProcedureState());
        } else if(source.equals(EventSource.cm_ci)) {
            CmsCI ci = (CmsCI)event.getPayload();
            // todo: selection by Ci name
            type = NotificationType.ci;
            notify.setCmsId(ci.getCiId());
            notify.setNsPath(ci.getNsPath());
            notify.setSubject("ci:" + ci.getCiName() );
            notify.setText(ci.getNsPath() + "ci:" + ci.getCiName() );
        } else if(source.equals(EventSource.cm_ci_rel)) {
            type = NotificationType.ci;
            notify.setSubject("ci_rel:" );
            notify.setText("Ci Relation");
        }
        if(rule.getSubject() != null) {
            notify.setSubject(rule.getSubject());
        }
        if(rule.getMessage() != null) {
            notify.setText(rule.getMessage());
        }
        notify.setSource(s);
        notify.setType( type );
        //todo: severity selection is ?
        notify.setSeverity(NotificationSeverity.info);

        return notify;
    }

}
