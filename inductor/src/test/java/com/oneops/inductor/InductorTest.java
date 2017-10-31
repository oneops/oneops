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
package com.oneops.inductor;

import com.google.gson.*;
import com.mockrunner.mock.jms.MockTextMessage;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import org.junit.*;

import javax.jms.JMSException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.Semaphore;

import static com.oneops.inductor.InductorConstants.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class InductorTest {

    private String testWo = "";
    private String testAo = "";
    private final Gson gson = new Gson();

    private void init() {
        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader("src/test/resources/testWorkorder.json"));
            while ((line = br.readLine()) != null) {
                testWo += line + "\n";
            }
            br = new BufferedReader(new FileReader("src/test/resources/testActionorder.json"));
            while ((line = br.readLine()) != null) {
                testAo += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private <T> T getTestOrder(String message, Class<T> type) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(message);
        JsonElement jsonElement = new JsonParser().parse(new InputStreamReader(is));
        return gson.fromJson(jsonElement, type);
    }

    @Test
    public void testProvider() {
        CmsWorkOrderSimple wo = getTestOrder("testWorkorder.json", CmsWorkOrderSimple.class);
        WorkOrderExecutor executor = new WorkOrderExecutor(mock(Config.class), mock(Semaphore.class));
        final String provider = executor.getProvider(wo);
        assertTrue(provider.equals("azure"));
    }

    @Test
    public void testKitchenPath() {
        CmsWorkOrderSimple wo = getTestOrder("testWorkorder.json", CmsWorkOrderSimple.class);
        Config cfg = new Config();
        cfg.setCircuitDir("/opt/oneops/inductor/packer");

        WorkOrderExecutor executor = new WorkOrderExecutor(cfg, mock(Semaphore.class));
        final String kitchenTestPath = executor.getKitchenTestPath(wo);
        assertTrue(kitchenTestPath.equals("/opt/oneops/inductor/circuit-main-1/components/cookbooks/test_wo/test/integration/"));
        final String kitchenSpecPath = executor.getSpecFilePath(wo, kitchenTestPath);
        assertTrue(kitchenSpecPath.equals("/opt/oneops/inductor/circuit-main-1/components/cookbooks/test_wo/test/integration/add/serverspec/add_spec.rb"));
    }


    private void testMessage(String text, String type) {
        init();
        MockTextMessage m = new MockTextMessage();
        try {
            m.setText(text);
            m.setJMSCorrelationID("test");
            m.setStringProperty("type", type);
        } catch (JMSException e) {
            e.printStackTrace();
        }

        Listener i = new Listener();
        Config config = new Config();
        try {
            config.setEnv("");
            config.setVerifyConfig("");
            config.init();
            i.setConfig(config);
            i.init();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testWorkOrder() {
        testMessage(testWo, WORK_ORDER_TYPE);
    }

    @Test
    public void testActionOrder() {
        testMessage(testAo, ACTION_ORDER_TYPE);
    }

    @Test
    public void testBomClass() {
        String bomPrefix = "bom\\.(.*\\.)*";
        String fqdnBomClass = bomPrefix + "Fqdn";
        assertTrue("bom.Fqdn".matches(fqdnBomClass));
        assertTrue("bom.oneops.1.Fqdn".matches(fqdnBomClass));
        assertTrue("bom.main.Fqdn".matches(fqdnBomClass));
        assertFalse("bomFqdn".matches(fqdnBomClass));
        assertFalse("bom.Compute".matches(fqdnBomClass));

        String ringBomClass = bomPrefix + "Ring";
        assertTrue("bom.Ring".matches(ringBomClass));
        assertTrue("bom.oneops.1.Ring".matches(ringBomClass));
        assertTrue("bom.main.Ring".matches(ringBomClass));
        assertFalse("bomRing".matches(ringBomClass));
        assertFalse("bom.Compute".matches(ringBomClass));

        String clusterBomClass = bomPrefix + "Cluster";
        assertTrue("bom.Cluster".matches(clusterBomClass));
        assertTrue("bom.oneops.1.Cluster".matches(clusterBomClass));
        assertTrue("bom.main.Cluster".matches(clusterBomClass));
        assertFalse("bomCluster".matches(clusterBomClass));
        assertFalse("bom.Compute".matches(clusterBomClass));
    }

    @Test
    public void testVerificationConfig() throws Exception {
        CmsWorkOrderSimple wo = getTestOrder("testWorkorder.json", CmsWorkOrderSimple.class);
        Config cfg = new Config();
        cfg.setCircuitDir("/opt/oneops/inductor/packer");
        cfg.setVerifyConfig("http_proxy=http://httpproxy.com,https_proxy=http://httpsproxy.com");
        cfg.setIpAttribute("public_ip");
        cfg.setEnv("");
        cfg.init();

        WorkOrderExecutor executor = new WorkOrderExecutor(cfg, mock(Semaphore.class));
        String actual = executor.generateKitchenConfig(wo, "/tmp/sshkey", "logkey");
        String expected = new String(readAllBytes(Paths.get(ClassLoader.getSystemResource("verification/kitchen.yml").toURI())), UTF_8);
        assertTrue("Invalid kitchen config.", actual.equalsIgnoreCase(expected));
    }
}
