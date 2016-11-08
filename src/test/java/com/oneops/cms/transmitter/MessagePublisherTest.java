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

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Matchers.any;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.transmitter.domain.CMSEvent;
import com.oneops.util.MessageData;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MessagePublisherTest {

    private ClassPathXmlApplicationContext context;

    private CmsPublisher cmsPublisher;
    private SearchSender searchSender;
    private JMSConsumer topicConsumer;
    private JMSConsumer searchConsumer;
    private MainScheduler scheduler;

    private final Gson gson = new Gson();

    private void init() {
        context = new ClassPathXmlApplicationContext("**/test-app-context.xml");
        cmsPublisher = context.getBean("cmsPublisherSpy", CmsPublisher.class);
        searchSender = context.getBean("searchSenderSpy", SearchSender.class);
        topicConsumer = context.getBean("topicConsumer", JMSConsumer.class);
        searchConsumer = context.getBean("searchConsumer", JMSConsumer.class);
        scheduler = context.getBean(MainScheduler.class);
        while (!(topicConsumer.isStarted() && searchConsumer.isStarted())) {
            //wait until the consumers are started
        }
    }

    @Before
    public void setUp() {
        init();
    }

    @After
    public void tearDown() {
        context.close();
    }

    @Test
    public void testDeploymentEvents() {
        ControllerEventReader controllerEventReader = context.getBean(ControllerEventReader.class);
        List<CMSEvent> depEvents = getDeploymentEvents();
        when(controllerEventReader.getEvents()).thenReturn(depEvents).thenReturn(null);
        scheduler.startTheJob();
        try {
            await().atMost(5, TimeUnit.SECONDS).until(() -> (topicConsumer.getCounter() == depEvents.size()));
            scheduler.stopPublishing();
            Thread.sleep(1000);
            verify(cmsPublisher).publishMessage(depEvents.get(0));
            verify(searchSender, times(depEvents.size())).publishMessage(any(CMSEvent.class));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            scheduler.stopPublishing();
        }
    }

    @Test
    public void testCiEvents() {
        CIEventReader ciEventReader = context.getBean(CIEventReader.class);
        CiEventData ciEventData = getCiEvents();
        int ciCount = ciEventData.getCmsCiCMSEvents().size() + ciEventData.getCmsReleaseCMSEvents().size()
                + ciEventData.getRfcCiCMSEvents().size();
        when(ciEventReader.getEvents()).thenReturn(ciEventData.getCmsCiCMSEvents())
                .thenReturn(ciEventData.getCmsReleaseCMSEvents()).thenReturn(ciEventData.getRfcCiCMSEvents())
                .thenReturn(null);

        scheduler.startTheJob();
        try {
            await().atMost(5, TimeUnit.SECONDS).until(() -> (searchConsumer.getCounter() == ciCount));
            scheduler.stopPublishing();
            Thread.sleep(1000);
            verify(searchSender, times(ciCount)).publishMessage(any(CMSEvent.class));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            scheduler.stopPublishing();
        }
    }

    @Test
    public void testSearchQDown() {
        BrokerService searchBroker = context.getBean("searchBroker", BrokerService.class);
        try {
            searchBroker.stop();
        } catch (Exception e) {
            Assert.fail();
        }

        searchBroker.waitUntilStopped();
        if (searchBroker.isStopped()) {
            ControllerEventReader controllerEventReader = context.getBean(ControllerEventReader.class);
            List<CMSEvent> depEvents = getDeploymentEvents();
            when(controllerEventReader.getEvents()).thenReturn(depEvents).thenReturn(depEvents).thenReturn(null);
            scheduler.startTheJob();
            try {
                int count = (depEvents.size() * 2);
                await().atMost(7, TimeUnit.SECONDS).until(() -> (topicConsumer.getCounter() == count));

                for (CMSEvent event1 : depEvents) {
                    verify(cmsPublisher, times(2)).publishMessage(event1);
                }

                verify(searchSender, times(count)).publishMessage(any(CMSEvent.class));
                Assert.assertEquals(searchConsumer.getCounter(), 0);
                searchConsumer.startRecording();

                searchBroker.start(true);
                searchBroker.waitUntilStarted(5000);
                if (searchBroker.isStarted()) {
                    await().atMost(10, TimeUnit.SECONDS).until(() -> (searchConsumer.getCounter() == count));
                    List<MessageData> eventDataList = searchConsumer.getMessages();
                    Assert.assertEquals(count, eventDataList.size());
                    assertMessages(depEvents, eventDataList);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail();
            } finally {
                scheduler.stopPublishing();
            }
        }
    }

    private void assertMessages(List<CMSEvent> expected, List<MessageData> actual) {
        Gson gson = new Gson();
        Map<String, CMSEvent> expectedMap = new HashMap<>(expected.size());
        for (CMSEvent data : expected) {
            expectedMap.put(gson.toJson(data.getPayload()), data);
        }
        for (MessageData data : actual) {
            CMSEvent matching = expectedMap.get(data.getPayload());
            Assert.assertNotNull(matching);
            Assert.assertEquals(matching.getHeaders(), data.getHeaders());
        }
    }


    @Test
    public void testCiEventsWhenAMQDown() {
        BrokerService amqBroker = context.getBean("amqBroker", BrokerService.class);
        try {
            amqBroker.stop();
        } catch (Exception e) {
            Assert.fail();
        }

        amqBroker.waitUntilStopped();
        if (amqBroker.isStopped()) {
            ControllerEventReader controllerEventReader = context.getBean(ControllerEventReader.class);
            CIEventReader ciEventReader = context.getBean(CIEventReader.class);
            List<CMSEvent> depEvents = getDeploymentEvents();
            when(controllerEventReader.getEvents()).thenReturn(depEvents).thenReturn(depEvents).thenReturn(null);
            CiEventData ciEventData = getCiEvents();
            int ciCount = ciEventData.getCmsCiCMSEvents().size() + ciEventData.getCmsReleaseCMSEvents().size()
                    + ciEventData.getRfcCiCMSEvents().size();
            // test ci events flow when AMQ is down
            when(ciEventReader.getEvents()).thenReturn(ciEventData.getCmsCiCMSEvents())
                    .thenReturn(ciEventData.getCmsReleaseCMSEvents()).thenReturn(ciEventData.getRfcCiCMSEvents())
                    .thenReturn(null);
            scheduler.startTheJob();
            try {
                Assert.assertEquals(topicConsumer.getCounter(), 0);
                await().atMost(5, TimeUnit.SECONDS).until(() -> (searchConsumer.getCounter() == ciCount));

            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail();
            } finally {
                scheduler.stopPublishing();
            }
        }
    }

    private List<CMSEvent> getDeploymentEvents() {
        List<CMSEvent> events = new ArrayList<>();
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("deployment-events.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while (line != null) {
                CMSEvent event = new CMSEvent();
                event.addHeaders("sourceId", "546589");
                event.addHeaders("action", "update");
                event.addHeaders("source", "deployment");
                event.addHeaders("clazzName", "Deployment");
                CmsDeployment deployment = gson.fromJson(line, CmsDeployment.class);
                event.setPayload(deployment);
                events.add(event);
                line = reader.readLine();
            }
        } catch (JsonSyntaxException | IOException e) {
            e.printStackTrace();
        }
        return events;
    }

    private CiEventData getCiEvents() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("ci-events.json");
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = (JsonElement) parser.parse(new InputStreamReader(is));
        CiEventData ciEventData = gson.fromJson(jsonElement, CiEventData.class);

        List<CmsRfcCI> rfcCis = ciEventData.getRfcCiEvents();
        List<CMSEvent> rfcCiEvents = new ArrayList<>();
        for (CmsRfcCI rfcCi : rfcCis) {
            CMSEvent event = new CMSEvent();
            event.setPayload(rfcCi);
            event.addHeaders("source", "rfcCi");
            event.setEventId(1110000);
            rfcCiEvents.add(event);
        }
        ciEventData.setRfcCiCMSEvents(rfcCiEvents);

        List<CmsCI> cmsCis = ciEventData.getCmsCiEvents();
        List<CMSEvent> cmsCiEvents = new ArrayList<>();
        for (CmsCI cmsCi : cmsCis) {
            CMSEvent event = new CMSEvent();
            event.setPayload(cmsCi);
            event.addHeaders("source", "ci");
            event.setEventId(2220000);
            cmsCiEvents.add(event);
        }
        ciEventData.setCmsCiCMSEvents(cmsCiEvents);

        List<CmsRelease> cmsRels = ciEventData.getCmsReleaseEvents();
        List<CMSEvent> cmsRelEvents = new ArrayList<>();
        for (CmsRelease cmsRel : cmsRels) {
            CMSEvent event = new CMSEvent();
            event.setPayload(cmsRel);
            event.addHeaders("source", "release");
            event.setEventId(3330000);
            cmsRelEvents.add(event);
        }
        ciEventData.setCmsReleaseCMSEvents(cmsRelEvents);

        return ciEventData;
    }

}
