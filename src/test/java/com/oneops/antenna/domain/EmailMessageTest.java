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
package com.oneops.antenna.domain;


import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * The Class EmailMessageTest.
 */
public class EmailMessageTest {

    private final EmailMessage message = new EmailMessage();
    private List<String> toAddresses = new ArrayList<>(2);
    private List<String> toCCs = new ArrayList<>(2);


    @BeforeClass
    public void mailSetup() {
        message.setSubject("subject-mockito");
        message.setFromAddress(this.getClass().getName());
        message.setTxtMessage(this.toString());

        String[] toArray = new String[]{"adam@goog.com", "zed@food.com"};
        String[] ccArray = new String[]{"barb@evite.com", "yeti@drink.com"};

        toAddresses.addAll(Arrays.asList(toArray));
        toCCs.addAll(Arrays.asList(ccArray));
        message.setToAddresses(toAddresses);
        message.setToCcAddresses(toCCs);
    }

    /**
     * Bean type test that the access is the same as the setter
     * and that once we add more entries we do not ruin the existing
     */
    @Test
    public void accessorValidity() {
        assertTrue(message.getToAddresses().containsAll(toAddresses));
        assertTrue(message.getToCcAddresses().containsAll(toCCs));

        String newToRecipient = "chuck@raid.com";
        String newCcRecipient = "nolan@tank.com";

        message.addToAddress(newToRecipient);
        message.addToCcAddresses(newCcRecipient);
        assertTrue(message.getToAddresses().containsAll(toAddresses));
        assertTrue(message.getToCcAddresses().containsAll(toCCs));
        assertTrue(message.getToAddresses().contains(newToRecipient));
        assertTrue(message.getToCcAddresses().contains(newCcRecipient));
    }
}
