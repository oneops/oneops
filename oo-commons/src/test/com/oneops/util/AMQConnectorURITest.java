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
package com.oneops.util;

import org.testng.annotations.Test;

import com.oneops.util.AMQConnectorURI;

import java.util.*;

import static org.testng.Assert.*;

/**
 * ActiveMQ connection broker url builder test class.
 *
 * @author Suresh G
 */
public class AMQConnectorURITest {

    @Test
    public void testBuild() throws Exception {
        String host = "kloopzmq1";
        int port = 61616;

        AMQConnectorURI acu = new AMQConnectorURI();
        acu.setTransport("failover");
        acu.setHost(host);
        acu.setPort(port);
        acu.setProtocol("tcp");
        acu.setDnsResolve(false);
        acu.setKeepAlive(true);

        String uri = acu.build();
        System.out.println(uri);
        assertEquals(uri, "failover:(tcp://" + host + ":" + port + "?keepAlive=true)");

        Map<String, String> optm = new LinkedHashMap<String, String>();
        optm.put("initialReconnectDelay", "100");
        optm.put("maxReconnectDelay", "30000");
        optm.put("maxReconnectAttempts", "-1");
        optm.put("backup", "false");
        optm.put("randomize", "false");
        optm.put("jms.prefetchPolicy.queuePrefetch", "10");
        optm.put("jms.redeliveryPolicy.maximumRedeliveries", "10000");
        optm.put("jms.redeliveryPolicy.initialRedeliveryDelay", "3000");
        optm.put("jms.redeliveryPolicy.useExponentialBackOff", "true");
        optm.put("jms.redeliveryPolicy.backOffMultiplier", "2");
        acu.setTransportOptions(optm);

        uri = acu.build();
        System.out.println(uri);
        assertNotNull(uri, "AMQ connection uri is empty");

        String res = "failover:(tcp://%1$s:61616?keepAlive=true)?" +
                "initialReconnectDelay=100&" +
                "maxReconnectDelay=30000&" +
                "maxReconnectAttempts=-1&" +
                "backup=false&" +
                "randomize=false&" +
                "jms.prefetchPolicy.queuePrefetch=10&" +
                "jms.redeliveryPolicy.maximumRedeliveries=10000&" +
                "jms.redeliveryPolicy.initialRedeliveryDelay=3000&" +
                "jms.redeliveryPolicy.useExponentialBackOff=true&" +
                "jms.redeliveryPolicy.backOffMultiplier=2";

        assertEquals(uri, String.format(res, host));
    }
}