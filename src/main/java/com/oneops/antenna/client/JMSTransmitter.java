package com.oneops.antenna.client;

import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.NotificationType;
import com.oneops.util.ReliableExecutor;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;


public class JMSTransmitter extends ReliableExecutor<NotificationMessage> {

    //private static final String JMS_QUEUE_PROP = "oo.antenna.client.jms.queue";

    private long timeToLive;
    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private String url = ActiveMQConnection.DEFAULT_BROKER_URL + "?connectionTimeout=1000";
    private String queue = "NOTIFICATIONS";
    private boolean persistent = true;

    private ConnectionFactory connFactory;
    private Connection connection = null;
    private Session session = null;
    private MessageProducer producer = null;

    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public void setConnFactory(ConnectionFactory connFactory) {
        this.connFactory = connFactory;
    }

    private void closeConnection() {
        try {
            logger.debug("Closing AMQ connection");
            producer.close();
            session.close();
            connection.close();
        } catch (Throwable ignore) {
        }
    }

    public void init() {
        try {
            if (this.connFactory == null) {
                connFactory = new ActiveMQConnectionFactory(user, password, url);
            }
            connection = connFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queue);
            producer = session.createProducer(destination);

            if (persistent) {
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            } else {
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            }
            if (timeToLive != 0) {
                producer.setTimeToLive(timeToLive);
            }

        } catch (JMSException e) {
            logger.error(e.getMessage());
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        super.init();
        logger.info(">>>>>>>>>>>>>>Antenna client initialized!");
    }


    public void destroy() {
        super.destroy();
        closeConnection();
    }

    @Override
    protected boolean process(NotificationMessage notificationMessage) {
        try {
            TextMessage message = session.createTextMessage(gson.toJson(notificationMessage));
            message.setStringProperty("source", "notification");
            producer.send(message);
            logger.debug("Published: {}", message.getText());
            return true;
        } catch (JMSException e) {
            logger.error(e.getMessage());
            logger.debug(e.getMessage(), e);
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("Start ReliableExecutor");
        JMSTransmitter t = new JMSTransmitter();
        t.setScanFolder("/projects/oneops/antenna-scan");
        t.init();
        NotificationMessage nm = new NotificationMessage();
        nm.setCmsId(999);
        nm.setSubject("Test Subj");
        nm.setType(NotificationType.ci);
        t.executeSync(nm);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t.destroy();
        System.out.println("Stop ReliableExecutor");
    }
}
