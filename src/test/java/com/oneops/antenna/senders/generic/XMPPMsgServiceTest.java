/**
 * Copyright 2015 Walmart, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package com.oneops.antenna.senders.generic;

import com.codahale.metrics.MetricRegistry;
import com.oneops.antenna.domain.EmailSubscriber;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.NotificationType;
import com.oneops.antenna.domain.XMPPSubscriber;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.*;

/**
 * Test case class
 */
public class XMPPMsgServiceTest {

    @Spy
    private MetricRegistry metric = new MetricRegistry();

    @InjectMocks
    private XMPPMsgService svc = new XMPPMsgService(metric);

    private static final int timeout = Integer.MAX_VALUE;

    @BeforeClass
    public void init() {
        System.setProperty("oneops.url", "https://oneops.com");
        MockitoAnnotations.initMocks(this);
        svc.init();
    }

    @Test
    public void mutabilityTest() {
        svc.setBotName("RobotX");
        svc.setTimeout(timeout);
        assertEquals("RobotX", svc.getBotName());
        assertEquals(timeout, svc.getTimeout());
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void negativeCastTest() throws Exception {
        this.svc.postMessage(new NotificationMessage(), new EmailSubscriber());
    }

    @Test
    public void postMessageExerciseTest() {
        svc.setBotName("postMessageExerciseTest");
        svc.setTimeout(1);

        boolean b1 = false;
        boolean b2 = false;
        XMPPSubscriber sub1 = new XMPPSubscriber();
        sub1.setChatServer("notarealaddressOK");
        sub1.setChatPort(9);
        XMPPSubscriber sub2 = new XMPPSubscriber();
        sub2.setChatServer("notarealaddressOK");
        sub2.setChatPort(7);
        try {
            b1 = svc.postMessage(null, sub1); //illegal argument exception
            b2 = svc.postMessage(null, sub2);
        } catch (IllegalArgumentException e) {
        }
        assertFalse(b1);
        assertFalse(b2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void postNullMessageTest() {
        svc.setBotName("postMessageExerciseTest");
        svc.setTimeout(1);
        XMPPSubscriber sub = new XMPPSubscriber();
        sub.setChatServer("notarealaddressOK");
        sub.setChatPort(9);
        svc.postMessage(null, sub);
    }

    @Test
    public void linkFormatTest() {
        String deploymentNsp = "/oneops/forge/cd1/bom";
        // https://oneops.com/org/assemblies/forge/transition/environments/paas#summary
        String deploymentUrlEnd = "https*://\\S+/oneops/assemblies/forge/transition/environments/\\w+#summary";

        String repairNsp = "/platform/LMS/prod/bom/LMS/1";
        // https://oneops.com/org/assemblies/LMS/operations/environments/prod/platforms/LMS#summary
        String repairUrlEnd = "https*://\\S+/r/ci/\\w+";

        NotificationMessage nMessage = new NotificationMessage();
        nMessage.setNsPath(deploymentNsp);
        nMessage.setType(NotificationType.deployment);
        URL url = nMessage.getNotificationUrl();
        assertNotNull(url, "DEPLOYMNENT URL unexpected null in place of a good url for nspath: " + deploymentNsp);
        assertTrue(url.toString().startsWith("http"), "DEPLOYMNENT URL h-t-t-p " +
                "are not first letters of this bad URL: " + url);

        Pattern pattern = Pattern.compile(deploymentUrlEnd);
        Matcher matcher = pattern.matcher(url.toString());
        assertTrue(matcher.matches(), "NSPATH-" + deploymentNsp + " DEPLOYMNENT URL: " + url + " does not match: " + deploymentUrlEnd);

        //Continue to next type....
        nMessage.setNsPath(repairNsp);
        nMessage.setType(NotificationType.procedure);
        url = nMessage.getNotificationUrl();
        assertNotNull(url, "OPERATION URL unexpected null in place of a good url for nspath: " + deploymentNsp);
        assertTrue(url.toString().startsWith("http"), "OPERATION URL h-t-t-p are not first letters of this bad URL: " + url);

        pattern = Pattern.compile(repairUrlEnd);
        matcher = pattern.matcher(url.toString());
        assertTrue(matcher.matches(), "NSPATH-" + repairNsp + " OPERATION URL: " + url + " does not match: " + repairUrlEnd);
    }
}
