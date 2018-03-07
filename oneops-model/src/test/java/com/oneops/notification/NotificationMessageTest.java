package com.oneops.notification;


import org.junit.Test;

import java.util.HashMap;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

/*******************************************************************************
 *
 *   Copyright 2016 Walmart, Inc.
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
public class NotificationMessageTest {

    private static final String TEST_VALUE = "test";
    private static final String ENTRY_NAME = "test";

    @Test
    public void testGetPayloadStringForNull() {
        assertNull(new NotificationMessage().getPayloadString(ENTRY_NAME));
    }


    @Test
    public void testGetPayloadStringForNotNull() {
        NotificationMessage notificationMessage = new NotificationMessage();
        HashMap<String, Object> payloadEntries = new HashMap<>();
        payloadEntries.put(ENTRY_NAME, TEST_VALUE);
        notificationMessage.putPayloadEntries(payloadEntries);
        assertEquals(TEST_VALUE, notificationMessage.getPayloadString(ENTRY_NAME));
    }
}