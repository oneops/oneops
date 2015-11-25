package com.oneops.antenna.jms;


import com.codahale.metrics.MetricRegistry;
import com.google.gson.Gson;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.jms.AntennaListener;
import com.oneops.antenna.service.Dispatcher;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The Class AntennaListenerTest.
 */
public class AntennaListenerTest {

    @InjectMocks
    private AntennaListener listener = new AntennaListener();

    @Spy
    private MetricRegistry metric = new MetricRegistry();

    @BeforeClass
    /** set up the instance */
    public void config() {
        System.setProperty("oneops.url", "http://oneops.prod.walmart.com");
        MockitoAnnotations.initMocks(this);
        this.listener.init();
        this.listener.setGson(new Gson());
        this.listener.setDispatcher(mock(Dispatcher.class));
    }


    @Test
    /** forces a bad JSON format message into on Message*/
    public void onBadMessage() {
        TextMessage mockMessage = mock(TextMessage.class);

        try {
            when(mockMessage.getText()).thenReturn(""); //not good json
        } catch (JMSException e) {
            ;//swallow it is the mock
        }
        this.listener.onMessage(mockMessage);//method does not throw anything it is ok

    }


    @Test
    /** sends an Object message instead of Text; should be ok
     * it will get logged */
    public void onObjectMessage() {
        ObjectMessage objectMessage = mock(ObjectMessage.class);

        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setTimestamp(1L);
        notificationMessage.setText("mock-text");

        try {
            when(objectMessage.getObject()).thenReturn(notificationMessage);
        } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
