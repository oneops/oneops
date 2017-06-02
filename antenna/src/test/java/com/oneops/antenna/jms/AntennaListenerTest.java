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


import com.codahale.metrics.MetricRegistry;
import com.google.gson.Gson;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.service.Dispatcher;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
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

    @Spy
    private MetricRegistry metric = new MetricRegistry();

    @InjectMocks
    private AntennaListener listener = new AntennaListener(mock(Dispatcher.class), new Gson(), metric);

    /**
     * Set up the instance
     */
    @BeforeClass
    public void config() {
        System.setProperty("oneops.url", "http://oneops.com");
        MockitoAnnotations.initMocks(this);
        this.listener.setDmlc(mock(DefaultMessageListenerContainer.class));
        this.listener.init();
    }

    /**
     * Forces a bad JSON format message into on Message
     */
    @Test
    public void onBadMessage() {
        TextMessage mockMessage = mock(TextMessage.class);

        try {
            when(mockMessage.getText()).thenReturn(""); //not good json
        } catch (JMSException e) {
            ;//swallow it is the mock
        }
        this.listener.onMessage(mockMessage);//method does not throw anything it is ok

    }

    /**
     * sends an Object message instead of Text; should be ok
     * it will get logged
     */
    @Test
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
