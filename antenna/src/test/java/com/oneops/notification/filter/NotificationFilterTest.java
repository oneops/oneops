/*******************************************************************************
 *
 *   Copyright 2017 Walmart, Inc.
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
package com.oneops.notification.filter;

import static com.oneops.notification.NotificationSeverity.critical;
import static com.oneops.notification.NotificationSeverity.info;
import static com.oneops.notification.NotificationType.ci;
import static com.oneops.notification.NotificationType.deployment;
import static com.oneops.notification.NotificationType.none;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.oneops.notification.NotificationMessage;
import com.oneops.notification.NotificationSeverity;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * {@link NotificationFilter} tests.
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 */
public class NotificationFilterTest {

    private NotificationFilter filter;

    private NotificationMessage msg;

    /**
     * Create default filter and notification message used for testing.
     *
     * @throws Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        filter = new NotificationFilter();
        filter.eventType(none);
        filter.eventSeverity(NotificationSeverity.none);

        msg = new NotificationMessage();
        msg.setEnvironmentProfileName("prod");
        msg.setNsPath("/test/ns/path");
        msg.setSubject("Test message");
        msg.setType(ci);
        msg.setSeverity(info);
    }

    @Test
    public void testTypeFilter() throws Exception {

        filter.eventType(none);
        msg.setType(ci);
        assertTrue(filter.accept(msg), "None type filter should pass through all type of messages.");

        filter.eventType(ci);
        msg.setType(ci);
        assertTrue(filter.accept(msg), "Ci type filter should pass through only Ci messages.");

        filter.eventType(ci);
        msg.setType(deployment);
        assertFalse(filter.accept(msg), "Ci type filter shouldn't pass Deployment messages.");
    }


    @Test
    public void testSeverityFilter() throws Exception {

        filter.eventSeverity(NotificationSeverity.none);
        msg.setSeverity(info);
        assertTrue(filter.accept(msg), "None severity filter should pass through all type of messages.");

        filter.eventSeverity(info);
        msg.setSeverity(critical);
        assertTrue(filter.accept(msg), "Info severity filter should pass through all critical messages.");

        filter.eventSeverity(critical);
        msg.setSeverity(info);
        assertFalse(filter.accept(msg), "Critical severity filter shouldn't pass any info messages.");
    }

    @Test
    public void testEnvProfFilter() throws Exception {

        filter.envProfilePattern("PROD");
        msg.setEnvironmentProfileName("PROD");
        assertTrue(filter.accept(msg), "PROD env filter should pass through all PROD env messages.");

        filter.envProfilePattern("PROD");
        msg.setEnvironmentProfileName("STG");
        assertFalse(filter.accept(msg), "PROD env filter shouldn't pass STG env messages.");

        filter.envProfilePattern("prod");
        msg.setEnvironmentProfileName("non-prod");
        assertFalse(filter.accept(msg), "prod env filter shouldn't pass non-prod env messages.");

        filter.envProfilePattern("prod");
        msg.setEnvironmentProfileName("PROD");
        assertTrue(filter.accept(msg), "prod (Case-Insensitive) env filter should pass through all PROD env messages.");

        filter.envProfilePattern("Prod|Production");
        msg.setEnvironmentProfileName("PROD");
        assertTrue(filter.accept(msg), "Prod|Production env filter should pass through all PROD env messages.");

        filter.envProfilePattern("Prod|Production");
        msg.setEnvironmentProfileName("Prod1");
        assertFalse(filter.accept(msg), "Prod|Production env filter shouldn't pass Prod1 env messages.");

        filter.envProfilePattern("");
        msg.setEnvironmentProfileName("Prod");
        assertTrue(filter.accept(msg), "Empty env filter should pass through all env messages.");

        filter.envProfilePattern(null);
        msg.setEnvironmentProfileName("Prod");
        assertTrue(filter.accept(msg), "Null env filter should pass through all env messages.");

        filter.envProfilePattern("prod");
        msg.setEnvironmentProfileName(null);
        assertFalse(filter.accept(msg), "prod env filter shouldn't pass env message with null profile.");

    }

    @Test
    public void testNsPathFilter() throws Exception {
        filter.nsPaths(new String[]{});
        assertTrue(filter.accept(msg), "Empty nsPath filter should pass through all messages.");

        filter.nsPaths(null);
        assertTrue(filter.accept(msg), "Null nsPath filter should pass through all messages.");

        filter.nsPaths(new String[]{"/"});
        msg.setNsPath("/test/ns/path");
        assertTrue(filter.accept(msg), "'/' nsPath filter should pass through '/test/ns/path' messages.");

        filter.nsPaths(new String[]{"/test/ns/"});
        msg.setNsPath("/test/ns/path");
        assertTrue(filter.accept(msg), "'/test/ns/' nsPath filter should pass through '/test/ns/path' messages.");

        filter.nsPaths(new String[]{"/test1/ns"});
        msg.setNsPath("/test/ns/path");
        assertFalse(filter.accept(msg), "'/test1/ns' nsPath filter shouldn't pass '/test/ns/path' messages.");
    }

    @Test
    public void testCloudFilter() throws Exception {
        filter.clouds(new String[]{});
        assertTrue(filter.accept(msg), "Empty cloud filter should pass through all messages.");

        filter.clouds(null);
        assertTrue(filter.accept(msg), "Null cloud filter should pass through all messages.");

        filter.clouds(new String[]{"dal-prod", "dfw-prod"});
        msg.setCloudName("dfw-prod");
        assertTrue(filter.accept(msg), "dfw-prod cloud filter should pass through all dfw-prod messages.");

        filter.clouds(new String[]{"dfw-prod"});
        msg.setCloudName(null);
        assertFalse(filter.accept(msg), "dfw-prod cloud filter shouldn't pass messages with null cloud.");

        filter.clouds(new String[]{"(dfw|dal)-prod.*"});
        msg.setCloudName("dal-prod");
        assertTrue(filter.accept(msg), "'(dfw|dal)-prod*' cloud filter should pass through messages with 'dal-prod' cloud.");
    }

    @Test
    public void testMessageFilter() throws Exception {
      filter.selectorPattern("*");
      assertTrue(filter.accept(msg), "Empty cloud filter should pass through all messages.");

      filter.selectorPattern(null);
      assertTrue(filter.accept(msg), "Null  filter should pass through all messages.");

      filter.selectorPattern(".* Deployment completed successfully");
      msg.setSubject(
          "stgqe/Aenterprise-VvkFH/Eenterprise-VvkFH : Deployment completed successfully");
      assertTrue(filter.accept(msg),
          "<Deployment completed successfully>  filter should pass this messages.");

      filter.selectorPattern("Deployment completed successfully");
      msg.setSubject("stgqe/Aenterprise-VvkFH/Eenterprise-VvkFH : Deployment Failed");
      assertFalse(filter.accept(msg),
          "<Deployment completed successfully>  filter should NOT pass through all dfw-prod messages.");

    }
    @AfterMethod
    public void tearDown() throws Exception {
    }

}