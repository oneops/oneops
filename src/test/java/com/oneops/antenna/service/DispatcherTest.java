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
package com.oneops.antenna.service;

import com.google.gson.Gson;
import com.oneops.antenna.domain.*;
import com.oneops.antenna.senders.NotificationSender;
import com.oneops.antenna.subscriptions.SubscriberService;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.ops.service.OpsProcedureProcessor;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;


public class DispatcherTest {
    private static final long TEST_CID = 0;
    private Dispatcher dispatcher;
    private CmsCmProcessor cmProcessor;
    private SubscriberService subService;

    @BeforeClass
    public void init() {

        cmProcessor = mock(CmsCmProcessor.class);
        CmsCI ci = new CmsCI();
        ci.setNsPath("/a/b");
        when(cmProcessor.getCiById(anyLong())).thenReturn(ci);

        subService = mock(SubscriberService.class);
        List<BasicSubscriber> bsList = new ArrayList<>(4);
        BasicSubscriber basic1 = new EmailSubscriber();
        BasicSubscriber basic2 = new SNSSubscriber();
        BasicSubscriber basic3 = new URLSubscriber();
        BasicSubscriber basic4 = new BasicSubscriber();
        bsList.add(basic1);
        bsList.add(basic2);
        bsList.add(basic3);
        bsList.add(basic4);
        when(subService.getSubscribersForNs(anyString())).thenReturn(bsList);
        SubscriberService subService = mock(SubscriberService.class);
        NotificationSender notMock = mock(NotificationSender.class);
        this.dispatcher = new Dispatcher(new Gson(), subService, notMock, notMock, notMock, notMock, notMock, cmProcessor, mock(CmsDpmtProcessor.class), mock(OpsProcedureProcessor.class));
    }

    /**
     * Runs dispatch with mocks mainly expect normal flow no runtime exceptions
     */
    @Test
    public void testDispatch() {

        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setNsPath("/a/b");
        notificationMessage.setType(NotificationType.ci); //deployment procedure and OTHER
        notificationMessage.setCmsId(TEST_CID);
        this.dispatcher.dispatch(notificationMessage);
    }

    /**
     * Runs dispatch with mocks mainly again with null nspath
     */
    @Test
    public void testDispatchNulNs() {
        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setNsPath(null);
        notificationMessage.setCmsId(TEST_CID);
        notificationMessage.setType(NotificationType.deployment); //deployment procedure and OTHER
        this.dispatcher.dispatch(notificationMessage);
        notificationMessage.setType(NotificationType.procedure);
        this.dispatcher.dispatch(notificationMessage);
        notificationMessage.setType(NotificationType.ci);
        this.dispatcher.dispatch(notificationMessage);
    }
}


