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

import static org.mockito.Mockito.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Semaphore;
import javax.jms.*;

import org.junit.Assert;
import org.junit.Test;

import com.mockrunner.mock.jms.MockTextMessage;


public class InductorTest {

	private String testWo = "";
	private String testAo = "";
  final protected Gson gson = new Gson();

  @Test
  public  void testProvider(){
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("testWorkorder.json");
    JsonParser parser = new JsonParser();
    JsonElement jsonElement = parser.parse(new InputStreamReader(is));
    CmsWorkOrderSimple wo = gson.fromJson(jsonElement, CmsWorkOrderSimple.class);
    WorkOrderExecutor executor = new WorkOrderExecutor(mock(Config.class), mock(
        Semaphore.class));
    final String provider = executor.getProvider(wo);
    Assert.assertTrue(provider.equals("azure"));
  }

	public void init() {

		String line = null;
		BufferedReader br;
		try {

      br = new BufferedReader(new FileReader(
					"src/test/resources/testWorkorder.json"));
			while ((line = br.readLine()) != null) {
				testWo += line + "\n";
			}
			br = new BufferedReader(new FileReader(
					"src/test/resources/testActionorder.json"));
			while ((line = br.readLine()) != null) {
				testAo += line + "\n";
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testWorkOrder() {
		init();
		MockTextMessage m = new MockTextMessage();
		try {
			m.setText(testWo);
			m.setJMSCorrelationID("test");
			m.setStringProperty("type", InductorConstants.WORK_ORDER_TYPE);
		} catch (JMSException e) {
			e.printStackTrace();
		}

		Listener i = new Listener();
		Config config = new Config();
		try {
			config.setEnv("");
			config.init();
			i.setConfig(config);
			i.init();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testActionOrder() {
		init();
		MockTextMessage m = new MockTextMessage();
		try {
			m.setText(testAo);
			m.setJMSCorrelationID("test");
			m.setStringProperty("type", InductorConstants.ACTION_ORDER_TYPE);
		} catch (JMSException e) {
			e.printStackTrace();
		}

		Listener i = new Listener();
		Config config = new Config();
		try {
			config.setEnv("");
			config.init();
			i.setConfig(config);
			i.init();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testBomClass() {
		String bomPrefix = "bom\\.(.*\\.)*";
		String fqdnBomClass = bomPrefix + "Fqdn";
		Assert.assertTrue("bom.Fqdn".matches(fqdnBomClass));
		Assert.assertTrue("bom.oneops.1.Fqdn".matches(fqdnBomClass));
		Assert.assertTrue("bom.main.Fqdn".matches(fqdnBomClass));
		Assert.assertFalse("bomFqdn".matches(fqdnBomClass));
		Assert.assertFalse("bom.Compute".matches(fqdnBomClass));
		
		String ringBomClass = bomPrefix + "Ring";
		Assert.assertTrue("bom.Ring".matches(ringBomClass));
		Assert.assertTrue("bom.oneops.1.Ring".matches(ringBomClass));
		Assert.assertTrue("bom.main.Ring".matches(ringBomClass));
		Assert.assertFalse("bomRing".matches(ringBomClass));
		Assert.assertFalse("bom.Compute".matches(ringBomClass));
		
		String clusterBomClass = bomPrefix + "Cluster";
		Assert.assertTrue("bom.Cluster".matches(clusterBomClass));
		Assert.assertTrue("bom.oneops.1.Cluster".matches(clusterBomClass));
		Assert.assertTrue("bom.main.Cluster".matches(clusterBomClass));
		Assert.assertFalse("bomCluster".matches(clusterBomClass));
		Assert.assertFalse("bom.Compute".matches(clusterBomClass));
	}
	
}
