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
package com.oneops.opamp.rs;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.opamp.service.BadStateProcessor;
import com.oneops.opamp.service.EnvPropsProcessor;

/**
 * @author dsing17
 *
 */
public class OpampRsControllerTest {

	long ciId = 999L;
	String userId = "TestUser";
	String description = "TestDescription";

	OpampRsController opampRsController;
	BadStateProcessor bsProcessor;
	EnvPropsProcessor envProcessor;
	RestTemplate restTemplate;
	CmsCmManager cmManager;
	CmsCI platform;
	CmsCI env;

	@BeforeMethod
	private void init() {
		opampRsController = new OpampRsController();
		bsProcessor = new BadStateProcessor();
		envProcessor = mock(EnvPropsProcessor.class);
		restTemplate = mock(RestTemplate.class);
		cmManager = mock(CmsCmManager.class);
		platform = mock(CmsCI.class);
		env = mock(CmsCI.class);

	}

	@Test(enabled = true)
	public void opampRsController_replaceByCid_OpenRelease() {

		when(envProcessor.isOpenRelease4Env(anyObject())).thenReturn(true);
		when(envProcessor.getPlatform4Bom(anyLong())).thenReturn(new CmsCI());
		when(envProcessor.getEnv4Platform(anyObject())).thenReturn(new CmsCI());
		when(envProcessor.isAutoReplaceEnabled(anyObject())).thenReturn(true);

		bsProcessor.setEnvProcessor(envProcessor);
		opampRsController.setBsProcessor(bsProcessor);

		Map<String, Integer> responseFromTransistor = new HashMap<String, Integer>(1);
		Integer expectedDeploymentId = Integer.valueOf(1);
		responseFromTransistor.put("deploymentId", expectedDeploymentId);

		when(restTemplate.postForObject(anyString(), anyObject(), any(), anyMapOf(String.class, String.class)))
				.thenReturn(responseFromTransistor);

		Map<String, Integer> result = opampRsController.replaceByCid(ciId, userId, description);
		assertEquals(result.size(), 1);
		assertEquals(result.get("deploymentId"), expectedDeploymentId);

	}

	@Test(enabled = true)
	public void opampRsController_replaceByCid_ClosedRelease() {

		when(envProcessor.getPlatform4Bom(ciId)).thenReturn(platform);
		when(envProcessor.getEnv4Platform(platform)).thenReturn(env);
		when(envProcessor.isOpenRelease4Env(env)).thenReturn(false);
		when(envProcessor.isAutoReplaceEnabled(platform)).thenReturn(true);

		bsProcessor.setEnvProcessor(envProcessor);
		bsProcessor.setCmManager(cmManager);

		Map<String, Integer> responseFromTransistor = new HashMap<String, Integer>(1);
		Integer expectedDeploymentId = Integer.valueOf(0);
		responseFromTransistor.put("deploymentId", expectedDeploymentId);

		when(restTemplate.postForObject(anyString(), anyObject(), anyObject(), anyMapOf(String.class, String.class)))
				.thenReturn(responseFromTransistor);

		bsProcessor.setRestTemplate(restTemplate);

		opampRsController.setBsProcessor(bsProcessor);

		Map<String, Integer> result = opampRsController.replaceByCid(ciId, userId, description);
		assertEquals(result.size(), 1);
		assertEquals(result.get("deploymentId"), expectedDeploymentId);
	}

	@Test(enabled = true)
	public void opampRsController_replaceByCid_AutoReplaceEnabled() {

		when(envProcessor.getPlatform4Bom(ciId)).thenReturn(platform);
		when(envProcessor.getEnv4Platform(platform)).thenReturn(env);
		when(envProcessor.isOpenRelease4Env(env)).thenReturn(false);
		when(envProcessor.isAutoReplaceEnabled(platform)).thenReturn(true);

		bsProcessor.setEnvProcessor(envProcessor);
		bsProcessor.setCmManager(cmManager);

		Map<String, Integer> responseFromTransistor = new HashMap<String, Integer>(1);
		Integer expectedDeploymentId = Integer.valueOf(0);
		responseFromTransistor.put("deploymentId", expectedDeploymentId);

		when(restTemplate.postForObject(anyString(), anyObject(), anyObject(), anyMapOf(String.class, String.class)))
				.thenReturn(responseFromTransistor);

		bsProcessor.setRestTemplate(restTemplate);

		opampRsController.setBsProcessor(bsProcessor);

		Map<String, Integer> result = opampRsController.replaceByCid(ciId, userId, description);
		assertEquals(result.size(), 1);
		assertEquals(result.get("deploymentId"), expectedDeploymentId);
	}

	@Test(enabled = true)
	public void opampRsController_replaceByCid_AutoReplaceDisabled() {

		when(envProcessor.getPlatform4Bom(ciId)).thenReturn(platform);
		when(envProcessor.getEnv4Platform(platform)).thenReturn(env);
		when(envProcessor.isOpenRelease4Env(env)).thenReturn(false);
		when(envProcessor.isAutoReplaceEnabled(platform)).thenReturn(false);

		bsProcessor.setEnvProcessor(envProcessor);
		bsProcessor.setCmManager(cmManager);

		Map<String, Integer> responseFromTransistor = new HashMap<String, Integer>(1);
		Integer expectedDeploymentId = Integer.valueOf(1);
		responseFromTransistor.put("deploymentId", expectedDeploymentId);

		when(restTemplate.postForObject(anyString(), anyObject(), anyObject(), anyMapOf(String.class, String.class)))
				.thenReturn(responseFromTransistor);

		bsProcessor.setRestTemplate(restTemplate);

		opampRsController.setBsProcessor(bsProcessor);

		Map<String, Integer> result = opampRsController.replaceByCid(ciId, userId, description);
		assertEquals(result.size(), 1);
		assertEquals(result.get("deploymentId"), expectedDeploymentId);

	}

}