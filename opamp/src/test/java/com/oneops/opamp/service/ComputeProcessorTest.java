package com.oneops.opamp.service;


import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.Test;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.opamp.service.ComputeProcessor;
import static org.mockito.Matchers.anyLong;
import static org.testng.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;


public class ComputeProcessorTest {

	@Test
	public void ComputeProcessorTest_replaceComputeByCid_computeRelacedSuccessFully() {
		
		ComputeProcessor computeProcessor = new ComputeProcessor();
		EnvPropsProcessor envProcessor=mock(EnvPropsProcessor.class, Mockito.RETURNS_DEEP_STUBS);
		CmsCmManager cmManager=mock(CmsCmManager.class,Mockito.RETURNS_DEEP_STUBS);
		String transistorUrl="TestTransistorURL";
		RestTemplate restTemplate=mock(RestTemplate.class,Mockito.RETURNS_DEEP_STUBS);
		
		computeProcessor.setCmManager(cmManager);
		computeProcessor.setEnvProcessor(envProcessor);
		computeProcessor.setRestTemplate(restTemplate);
		computeProcessor.setTransistorUrl(transistorUrl);
		
		Map<String, Integer> defaultResponse=new HashMap<String, Integer>(1);
		defaultResponse.put("deploymentId",0);
		
		when(restTemplate.postForObject(anyString(), anyObject(), any(), anyMap())).thenReturn(defaultResponse);
		
		Map<String, Integer> result=computeProcessor.replaceComputeByCid(anyLong());
		assertEquals(result.size(), 1,"expected size 1, Actual Size: "+result.size() +" ,result.entrySet()"+result.entrySet());
		assertEquals(result.get("deploymentId"), Integer.valueOf(0),"Deployment must have succeeded with response code: 0 , result.entrySet(): "+result.entrySet());
		
	}
	
	@Test
	public void ComputeProcessorTest_replaceComputeByCid_computeCannotBeRelaced() {
		
		ComputeProcessor computeProcessor = new ComputeProcessor();
		EnvPropsProcessor envProcessor=mock(EnvPropsProcessor.class, Mockito.RETURNS_DEEP_STUBS);
		CmsCmManager cmManager=mock(CmsCmManager.class,Mockito.RETURNS_DEEP_STUBS);
		String transistorUrl="TestTransistorURL";
		RestTemplate restTemplate=mock(RestTemplate.class,Mockito.RETURNS_DEEP_STUBS);
		
		computeProcessor.setCmManager(cmManager);
		computeProcessor.setEnvProcessor(envProcessor);
		computeProcessor.setRestTemplate(restTemplate);
		computeProcessor.setTransistorUrl(transistorUrl);
		
		Map<String, Integer> defaultResponse=new HashMap<String, Integer>(1);
		defaultResponse.put("deploymentId",1);
		
		when(restTemplate.postForObject(anyString(), anyObject(), any(), anyMap())).thenReturn(defaultResponse);
		
		Map<String, Integer> result=computeProcessor.replaceComputeByCid(anyLong());
		assertEquals(result.size(), 1,"expected size 1, Actual Size: "+result.size() +" ,result.entrySet()"+result.entrySet());
		assertEquals(result.get("deploymentId"), Integer.valueOf(1),"Deployment must have failed with response code: 1 , result.entrySet(): "+result.entrySet());
		
	}

}
