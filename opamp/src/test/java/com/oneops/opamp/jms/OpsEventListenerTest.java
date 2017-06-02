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
package com.oneops.opamp.jms;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.TextMessage;

import org.mockito.ArgumentMatcher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.oneops.cms.exceptions.OpsException;
import com.oneops.opamp.cache.WatchedByAttributeCache;
import com.oneops.opamp.exceptions.OpampException;
import com.oneops.opamp.jms.OpsEventListener;
import com.oneops.opamp.service.BadStateProcessor;
import com.oneops.opamp.service.EnvPropsProcessor;
import com.oneops.opamp.service.FlexStateProcessor;
import com.oneops.opamp.service.Notifications;
import com.oneops.opamp.util.EventUtil;
import com.oneops.ops.CiOpsProcessor;
import com.oneops.ops.dao.OpsCiStateDao;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.ops.events.OpsBaseEvent;

public class OpsEventListenerTest
{
    private static final String EXISTING = "existing";
    private static final String NOTIFY = "notify";
    private static final String CLOSE = "close";
    private static final String NEW = "new";
    private static final String OPEN = "open";
    private static final String UNHEALTHY = "unhealthy";
    private EventUtil evtUtil = mock(EventUtil.class);
    private OpsEventListener opsEventListener = new OpsEventListener();

    Gson gson = new Gson();

    BadStateProcessor bsp = mock(BadStateProcessor.class);
    
    EnvPropsProcessor epp = mock(EnvPropsProcessor.class);

    Notifications notifier = mock(Notifications.class);
    OpsCiStateDao stateDao = mock(OpsCiStateDao.class);
    CiOpsProcessor ciOpsProcessor = mock(CiOpsProcessor.class);

    @BeforeClass
    public void setup()
    {
        opsEventListener = new OpsEventListener();
        opsEventListener.setGson(new Gson());
        opsEventListener.setBsProcessor(bsp);
        opsEventListener.setEnvProcessor(epp);
        opsEventListener.setFsProcessor(mock(FlexStateProcessor.class));
        opsEventListener.setNotifier(notifier);
        opsEventListener.setEventUtil(evtUtil);
        opsEventListener.setOpsCiStateDao(stateDao);
        opsEventListener.setCiOpsProcessor(ciOpsProcessor);
    }

    @Test
    public void testUnhealthy2UnhealthyOpenShouldCallBadStateProcessor() throws OpampException
    {
        CiChangeStateEvent event = getCiChangeEvent(UNHEALTHY, UNHEALTHY, OPEN, NEW);
        TextMessage message = mock(TextMessage.class);
        try
        {
            when(message.getStringProperty("type")).thenReturn("ci-change-state");
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        try
        {
            when(message.getText()).thenReturn(gson.toJson(event));
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        EventUtil evtUtil1 = new EventUtil();
        evtUtil1.setGson(new Gson());
        opsEventListener.setEventUtil(evtUtil1);
        opsEventListener.onMessage(message);// should be no exception, not even
                                            // JSMException

        verify(bsp).processUnhealthyState(argThat(new ObjectEqualityArgumentMatcher<CiChangeStateEvent>(event)));

    }

    @Test
    public void testUnhealthy2UnhealthyCloseShouldNotify() throws OpampException
    {
        setupMocks();
        CiChangeStateEvent event = getCiChangeEvent(UNHEALTHY, UNHEALTHY, CLOSE, NEW);
        TextMessage message = mock(TextMessage.class);
        try
        {
            when(message.getStringProperty("type")).thenReturn("ci-change-state");
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        try
        {
            when(message.getText()).thenReturn(gson.toJson(event));
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        EventUtil evtUtil1 = new EventUtil();
        evtUtil1.setGson(new Gson());
        opsEventListener.setEventUtil(evtUtil1);
        opsEventListener.onMessage(message);// should be no exception, not even
                                            // JSMException

        verify(notifier).sendOpsEventNotification(argThat(new ObjectEqualityArgumentMatcher<CiChangeStateEvent>(event)));
    }

    @Test
    public void testUnhealthy2NotifyCloseOpenShouldNotify() throws OpampException
    {
        setupMocks();
        CiChangeStateEvent event = getCiChangeEvent(NOTIFY, UNHEALTHY, CLOSE, NEW);

        TextMessage message = mock(TextMessage.class);

        try
        {
            when(message.getStringProperty("type")).thenReturn("ci-change-state");
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        try
        {
            when(message.getText()).thenReturn(gson.toJson(event));
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        EventUtil evtUtil1 = new EventUtil();
        evtUtil1.setGson(new Gson());
        opsEventListener.setEventUtil(evtUtil1);
        opsEventListener.onMessage(message);
        OpsBaseEvent opsEvent = evtUtil1.getOpsEvent(event);
        verify(notifier).sendOpsEventNotification(argThat(new ObjectEqualityArgumentMatcher<CiChangeStateEvent>(event)));
    }

    @BeforeMethod
    private void setupMocks()
    {
        bsp = mock(BadStateProcessor.class);
        notifier = mock(Notifications.class);
        opsEventListener.setBsProcessor(bsp);
        opsEventListener.setNotifier(notifier);
    }

    @Test
    public void testNotify2NotifyNewCloseOpenShouldNotify() throws OpampException
    {
        setupMocks();
        CiChangeStateEvent event = getCiChangeEvent(NOTIFY, NOTIFY, CLOSE, NEW);
        TextMessage message = mock(TextMessage.class);
        try
        {
            when(message.getStringProperty("type")).thenReturn("ci-change-state");
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        try
        {
            when(message.getText()).thenReturn(gson.toJson(event));
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        EventUtil evtUtil1 = new EventUtil();
        evtUtil1.setGson(new Gson());
        opsEventListener.setEventUtil(evtUtil1);
        opsEventListener.onMessage(message);
        OpsBaseEvent opsEvent = evtUtil1.getOpsEvent(event);
        verify(notifier).sendOpsEventNotification(argThat(new ObjectEqualityArgumentMatcher<CiChangeStateEvent>(event)));
    }

    // close events always notify
    @Test
    public void testNotify2NotifyExistingCloseShouldNotify() throws OpampException
    {
        setupMocks();
        CiChangeStateEvent event = getCiChangeEvent(NOTIFY, NOTIFY, CLOSE, EXISTING);
        TextMessage message = mock(TextMessage.class);
        try
        {
            when(message.getStringProperty("type")).thenReturn("ci-change-state");
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        try
        {
            when(message.getText()).thenReturn(gson.toJson(event));
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        EventUtil evtUtil1 = new EventUtil();
        evtUtil1.setGson(new Gson());
        opsEventListener.setEventUtil(evtUtil1);
        opsEventListener.onMessage(message);
        OpsBaseEvent opsEvent = evtUtil1.getOpsEvent(event);
        verify(notifier).sendOpsEventNotification(argThat(new ObjectEqualityArgumentMatcher<CiChangeStateEvent>(event)));
    }

    // open -existing events notify only if "notifyOnlyOnStateChange" is false
    @Test
    public void testNotify2NotifyExistingOpenShouldNotNotify() throws OpampException
    {
        setupMocks();
        CiChangeStateEvent event = getCiChangeEvent(NOTIFY, NOTIFY, OPEN, EXISTING);
        TextMessage message = mock(TextMessage.class);
        try
        {
            when(message.getStringProperty("type")).thenReturn("ci-change-state");
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        try
        {
            when(message.getText()).thenReturn(gson.toJson(event));
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        EventUtil evtUtil1 = new EventUtil();
        evtUtil1.setGson(new Gson());
        opsEventListener.setEventUtil(evtUtil1);
        opsEventListener.onMessage(message);
        OpsBaseEvent opsEvent = evtUtil1.getOpsEvent(event);
        verify(notifier, never()).sendOpsEventNotification(
                argThat(new ObjectEqualityArgumentMatcher<CiChangeStateEvent>(event)));
    }

    // open -existing events notify only if "notifyOnlyOnStateChange" is false
    @Test
    public void testNotify2NotifyExistingOpenShouldNotifyOnNotifyOnStateChangeIsFalse() throws OpampException
    {
        setupMocks();
        CiChangeStateEvent event = getCiChangeEvent(NOTIFY, NOTIFY, OPEN, EXISTING);
        TextMessage message = mock(TextMessage.class);
        try
        {
            when(message.getStringProperty("type")).thenReturn("ci-change-state");
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        try
        {
            when(message.getText()).thenReturn(gson.toJson(event));
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        EventUtil evtUtil1 = new EventUtil();
        evtUtil1.setGson(new Gson());
        opsEventListener.setEventUtil(evtUtil1);
        opsEventListener.onMessage(message);
        WatchedByAttributeCache cacheWithNotifyOnlyOnStateChangeFalse = mock(WatchedByAttributeCache.class);
        LoadingCache<String, String> cache = mock(LoadingCache.class);
        evtUtil1.setCache(cacheWithNotifyOnlyOnStateChangeFalse);
        try
        {
            when(cache.get(any(String.class))).thenReturn(String.valueOf("false"));
            when(cacheWithNotifyOnlyOnStateChangeFalse.instance()).thenReturn(cache);
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        verify(notifier, never()).sendOpsEventNotification(
                argThat(new ObjectEqualityArgumentMatcher<CiChangeStateEvent>(event)));

    }

    private class ObjectEqualityArgumentMatcher<T> extends ArgumentMatcher<T>
    {
        T thisObject;

        public ObjectEqualityArgumentMatcher(T thisObject)
        {
            this.thisObject = thisObject;
        }

        @Override
        public boolean matches(Object argument)
        {
            if (argument instanceof CiChangeStateEvent)
            {
                CiChangeStateEvent arg = (CiChangeStateEvent) argument;
                CiChangeStateEvent thisObject1 = (CiChangeStateEvent) thisObject;

                return thisObject1.getCiId() == arg.getCiId();
            }
            else
            {
                OpsBaseEvent arg = (OpsBaseEvent) argument;
                OpsBaseEvent thisObject1 = (OpsBaseEvent) thisObject;

                return thisObject1.getCiId() == arg.getCiId();
            }
        }
    }

    // @Test (expectedExceptions=OpsException.class)
    /**
     * Exceptions test.
     * 
     * @throws OpampException
     */
    public void exceptionsTest() throws OpampException
    {
        OpsEventListener oel = new OpsEventListener();
        opsEventListener.setGson(new Gson());

        when(opsEventListener.getEventUtil().getGson()).thenReturn(new Gson());
        String eventJson = makeJson(UNHEALTHY, "oldstate");
        TextMessage message = mock(TextMessage.class);
        try
        {
            when(message.getStringProperty("type")).thenReturn("ci-change-state");
            when(message.getText()).thenReturn(eventJson);

        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }

        BadStateProcessor bsProcessorMock = mock(BadStateProcessor.class);
        CiChangeStateEvent changeEvent = new CiChangeStateEvent();
        OpsBaseEvent event = new OpsBaseEvent();
        event.setCiId(anyLong());

        doThrow(new OpsException(1, "expected")).when(bsProcessorMock).processUnhealthyState(changeEvent);
        oel.setBsProcessor(bsProcessorMock);
        oel.onMessage(message);
        // /further test coverage could be done with this mixture. save for
        // later
        // when(bsProcessorMock.processUnhealthyState(anyLong());
        // doThrow(new
        // OpampException("expected")).when(bsProcessorMock).processUnhealthyState(anyLong());
        // oel.setBsProcessor(bsProcessorMock);
        // bsProcessor.processUnhealthyState(event.getCiId());

    }

    @Test
    public void nonTextMessageTest()
    {
        MapMessage message = mock(MapMessage.class);

        OpsEventListener oel = new OpsEventListener();
        oel.setEnvProcessor(epp);
        oel.onMessage(message);// should be no exception, not even JSMException
        // message will have its ack method call

    }

    @Test
    public void testOnUnehealthyMessageCloseEvent()
    {
        String eventJson = makeJson(UNHEALTHY, "oldstate");
    }

    @Test
    public void testOnMessage()
    {

        String eventJson = makeJson(UNHEALTHY, "oldstate");
        String eventJson1 = makeJson(NOTIFY, "oldstate");
        String eventJson2 = makeJson("overutilized", "oldstate");

        String eventJson3 = makeJson("underutilized", "oldstate");
        String eventJson4 = makeJson("good", UNHEALTHY);

        String eventJson5 = makeJson("good", NOTIFY);

        TextMessage message = mock(TextMessage.class);
        evtUtil.setGson(new Gson());
        try
        {
            when(message.getStringProperty("type")).thenReturn("ci-change-state");
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }

        try
        {
            when(message.getText()).thenReturn(eventJson);
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }

        try
        {
            when(message.getText()).thenReturn(eventJson1);
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }

        opsEventListener.onMessage(message);

        try
        {
            when(message.getText()).thenReturn(eventJson2);
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        opsEventListener.onMessage(message);
        try
        {
            when(message.getText()).thenReturn(eventJson3);
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        opsEventListener.onMessage(message);
        try
        {
            when(message.getText()).thenReturn(eventJson4);
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        opsEventListener.onMessage(message);
        try
        {
            when(message.getText()).thenReturn(eventJson5);
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        opsEventListener.onMessage(message);
    }

    /** makes a json string that is usefule for a CiChangeStateEvent */
    private String makeJson(String newState, String oldState)
    {
        CiChangeStateEvent ccse = getCiChangeEvent(newState, oldState, null, null);
        Gson gson = new Gson();
        return gson.toJson(ccse);
    }

    private CiChangeStateEvent getCiChangeEvent(String newState, String oldState, String state, String status)
    {
        CiChangeStateEvent ccse = new CiChangeStateEvent();
        ccse.setCiId(2L);
        ccse.setNewState(newState);
        ccse.setOldState(oldState);

        OpsBaseEvent obe = new OpsBaseEvent();
        obe.setBucket("mockBucket");
        obe.setStatus(status);
        obe.setState(state);
        obe.setManifestId(75l);
        Gson gson = new Gson();

        ccse.setPayLoad(gson.toJson(obe));
        return ccse;
    }

}
