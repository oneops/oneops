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
package com.oneops.antenna.senders.aws;


import com.oneops.antenna.domain.EmailSubscriber;
import com.oneops.antenna.domain.NotificationMessage;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

/**
 * test class to cover EmailService
 */
public class EmailServiceTest {

    EmailService emailService;

    private static final String STUB_SECRET = "stub-secret";
    private static final String STUB_KEY = "stub-key";

    /**
     * Setting up test
     */
    @BeforeClass
    public void init() {
        this.emailService = new EmailService();
        ;
        emailService.setAwsAccessKey(STUB_KEY);
        emailService.setAwsSecretKey(STUB_SECRET);
        emailService.init();
    }

    /**
     * Send noop request to postMessage note the send email method cannot be mocked so not tested
     */
    @Test
    public void postMessage() {
        NotificationMessage msg = new NotificationMessage();
        EmailSubscriber sub = mock(EmailSubscriber.class);
        when(sub.getEmail()).thenReturn(null);
        boolean result = this.emailService.postMessage(msg, sub);
        assertTrue(result);
    }
}
