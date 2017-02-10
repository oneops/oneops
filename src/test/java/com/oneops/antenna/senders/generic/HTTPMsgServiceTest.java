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
package com.oneops.antenna.senders.generic;

import com.codahale.metrics.MetricRegistry;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.URLSubscriber;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.oneops.antenna.domain.NotificationSeverity.info;
import static com.oneops.antenna.domain.NotificationType.ci;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * The Class HTTPMsgServiceTest.
 */
public class HTTPMsgServiceTest {

    @Spy
    private MetricRegistry metric = new MetricRegistry();

    @InjectMocks
    private HTTPMsgService service = new HTTPMsgService(metric);


    @BeforeClass
    public void init() {
        System.setProperty("oneops.url", "http://oneops.com");
        MockitoAnnotations.initMocks(this);
        service.init();
    }


    /**
     * Send a message using a bogus URL make sure to get false result.
     */
    @Test
    public void badHttpMessageUrlPost() {
        URLSubscriber sub = new URLSubscriber();
        sub.setUserName(this.getClass().getName());
        sub.setUrl("this-is-not-valid://mock.foo?tryingToThrow");
        boolean result = service.postMessage(null, sub);
        assertEquals(result, false);
    }

    /**
     * Send a message using a bogus URL make sure to get false result
     */
    @Test
    public void testEmailNotification() {
        URLSubscriber sub = new URLSubscriber();
        sub.setUserName("admin");
        sub.setUrl("http://localhost:8018/");
        sub.setPassword("****");
        NotificationMessage n = new NotificationMessage();
        n.setSeverity(info);
        n.setNsPath("/testing/rel0402/testmon/bom/tom/1");
        n.setCmsId(20401024);
        n.setSubject("TEST :ci:artifact-app-3529131-1; Procedure ci_repair complete");
        n.setSource("procedure");
        n.setTimestamp(1405569317585l);
        n.setType(ci);
        n.setText("Assembly: rel0402; Environment: testmon; ci:artifact-app-3529131-1; " +
                "Procedure ci_repair complete!");
        assertFalse(service.postMessage(n, sub));
    }

}
