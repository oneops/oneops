package com.oneops.opamp.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

//import org.junit.Test;
import org.testng.annotations.Test;
import org.springframework.web.client.RestTemplate;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmManager;

public class ComputeServiceTest {

	@Test
	public void ComputeServiceTest_replaceComputeByCid() {

		ComputeService computeProcessor = new ComputeService();
		EnvPropsProcessor envProcessor = mock(EnvPropsProcessor.class);
		CmsCmManager cmManager = mock(CmsCmManager.class);
	
		RestTemplate restTemplate = mock(RestTemplate.class);

		long ciId= 999L;
		CmsCI platform = mock(CmsCI.class);
		when(envProcessor.getPlatform4Bom(ciId)).thenReturn(platform);
		CmsCI env= mock(CmsCI.class);
		when(envProcessor.getEnv4Platform(platform)).thenReturn(env);
		when(envProcessor.isAutoReplaceEnabled(platform)).thenReturn(true);
		
		
		Map<String, Integer> responseFromTransistor= new HashMap<String, Integer>(1);
		Integer expectedDeploymentId=Integer.valueOf(0);
		responseFromTransistor.put("deploymentId", expectedDeploymentId);
				
		when(restTemplate.postForObject(anyString(), anyObject(), any(), anyMapOf(String.class, String.class)))
		.thenReturn(responseFromTransistor);
		
		
		computeProcessor.setCmManager(cmManager);
		computeProcessor.setEnvProcessor(envProcessor);
		computeProcessor.setRestTemplate(restTemplate);
		computeProcessor.setRestTemplate(restTemplate);
		
		
		//TODO: implement OpAmp Exception in Service class
		Map<String, Integer> result = computeProcessor.replaceComputeByCid(ciId);
		assertEquals(result.size(), 1,
				"expected size 1, Actual Size: " + result.size() + " ,result.entrySet()" + result.entrySet());
		assertEquals(result.get("deploymentId"), expectedDeploymentId,
				"Deployment while OpenRelease status must have failed with response code: 1 , result.entrySet(): "
						+ result.entrySet());

	}
	

	@Test
	
	public void ComputeServiceTest_replaceComputeByCid_OpenRelease() {

		ComputeService computeProcessor = new ComputeService();
		EnvPropsProcessor envProcessor = mock(EnvPropsProcessor.class);
		CmsCmManager cmManager = mock(CmsCmManager.class);
		String transistorUrl = "TestTransistorURL";
		RestTemplate restTemplate = mock(RestTemplate.class);

	
		long ciId= 999L;
		CmsCI platform = mock(CmsCI.class);
		when(envProcessor.getPlatform4Bom(ciId)).thenReturn(platform);
		CmsCI env= mock(CmsCI.class);
		when(envProcessor.getEnv4Platform(platform)).thenReturn(env);
		when(envProcessor.isAutoReplaceEnabled(platform)).thenReturn(false);
		
		
		Map<String, Integer> responseFromTransistor= new HashMap<String, Integer>(1);
		Integer expectedDeploymentId=Integer.valueOf(1);
		responseFromTransistor.put("deploymentId", expectedDeploymentId);
		when(restTemplate.postForObject(anyString(), anyObject(), any(), anyMapOf(String.class, String.class)))
		.thenReturn(responseFromTransistor);
		
		computeProcessor.setCmManager(cmManager);
		computeProcessor.setEnvProcessor(envProcessor);
		computeProcessor.setRestTemplate(restTemplate);
		computeProcessor.setTransistorUrl(transistorUrl);
		computeProcessor.setRestTemplate(restTemplate);
		
		
		//TODO: implement OpAmp Exception in Service class
		Map<String, Integer> result = computeProcessor.replaceComputeByCid(ciId);
		assertEquals(result.size(), 1,
				"expected size 1, Actual Size: " + result.size() + " ,result.entrySet()" + result.entrySet());
		assertEquals(result.get("deploymentId"), expectedDeploymentId,
				"Deployment while OpenRelease status must have failed with response code: 1 , result.entrySet(): "
						+ result.entrySet());

	}
	

	@Test

	public void ComputeServiceTest_replaceComputeByCid_AutoReplaceDisabled() {

		ComputeService computeProcessor = new ComputeService();
		EnvPropsProcessor envProcessor = mock(EnvPropsProcessor.class);
		CmsCmManager cmManager = mock(CmsCmManager.class);
		String transistorUrl = "TestTransistorURL";
		RestTemplate restTemplate = mock(RestTemplate.class);

	
		long ciId= 999L;
		CmsCI platform = mock(CmsCI.class);
		when(envProcessor.getPlatform4Bom(ciId)).thenReturn(platform);
		CmsCI env= mock(CmsCI.class);
		when(envProcessor.getEnv4Platform(platform)).thenReturn(env);
		when(envProcessor.isAutoReplaceEnabled(platform)).thenReturn(true);
		
		
		Map<String, Integer> responseFromTransistor= new HashMap<String, Integer>(1);
		Integer expectedDeploymentId=Integer.valueOf(1);
		responseFromTransistor.put("deploymentId", expectedDeploymentId);
		when(restTemplate.postForObject(anyString(), anyObject(), any(), anyMapOf(String.class, String.class)))
		.thenReturn(responseFromTransistor);
		
		computeProcessor.setCmManager(cmManager);
		computeProcessor.setEnvProcessor(envProcessor);
		computeProcessor.setRestTemplate(restTemplate);
		computeProcessor.setTransistorUrl(transistorUrl);
		computeProcessor.setRestTemplate(restTemplate);
		
		
		//TODO: implement OpAmp Exception in Service class
		Map<String, Integer> result = computeProcessor.replaceComputeByCid(ciId);
		assertEquals(result.size(), 1,
				"expected size 1, Actual Size: " + result.size() + " ,result.entrySet()" + result.entrySet());
		assertEquals(result.get("deploymentId"), expectedDeploymentId,
				"Deployment while OpenRelease status must have failed with response code: 1 , result.entrySet(): "
						+ result.entrySet());

	}
}