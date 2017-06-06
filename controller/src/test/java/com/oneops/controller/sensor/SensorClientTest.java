/*******************************************************************************
 *
 * Copyright 2015 Walmart, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.oneops.controller.sensor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.testng.annotations.BeforeClass;

import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;

public class SensorClientTest {
	private final SensorClient sensorClient = new SensorClient();
	private DelegateExecution delegateExecution ;
	private CmsWorkOrderSimple cmsWorkOrderSimple;

	private static final String WATCHED_BY = "WatchedBy";

	@BeforeClass
	public void setUpMocks(){
		CmsDeployment cmsDeployment = new CmsDeployment();

		delegateExecution=mock(DelegateExecution.class);
		when(delegateExecution.getVariable("dpmt")).thenReturn(cmsDeployment);
		
		
		Map<String, List<CmsRfcCISimple>> payloadMapOfList=new HashMap<String, List<CmsRfcCISimple>>();
		
		List<CmsRfcCISimple> theListOfSimple=new ArrayList<CmsRfcCISimple>();
		CmsRfcCISimple cmsRfcCISimple = new CmsRfcCISimple();
		cmsRfcCISimple.setCreatedBy(this.getClass().getName());
		theListOfSimple.add(cmsRfcCISimple);

		payloadMapOfList.put(WATCHED_BY, theListOfSimple);

		
 		CmsRfcCISimple rfcCi = new CmsRfcCISimple() ;
 		rfcCi.setRfcAction("add");
 		
 		
		cmsWorkOrderSimple = new CmsWorkOrderSimple();
		cmsWorkOrderSimple.setRfcCi(rfcCi);
		cmsWorkOrderSimple.setPayLoad(payloadMapOfList);
		
		
//		cmsWorkOrderSimple = mock(CmsWorkOrderSimple.class);

//		when(cmsWorkOrderSimple.getRfcCi()).thenReturn(rfcCi);
//		when(cmsWorkOrderSimple.getPayLoad()).thenReturn(payloadMapOfList);
		
	}

/*	
	@Test
	public void startTracking(){
		RestTemplate restTemplate = mock(RestTemplate.class);
		//		when(restTemplate.postForLocation(anyString(), any(CmsWorkOrderSimple.class))).thenReturn(foo.....)
		// class does not care about the response so no nded
		sensorClient.setRestTemplate(restTemplate);
		sensorClient.startTracking(delegateExecution, cmsWorkOrderSimple);
	}
	@Test
	public void startTrackingHttpError(){
		
		RestTemplate restTemplate = mock(RestTemplate.class);
		when(restTemplate.postForLocation(anyString(), any(CmsWorkOrderSimple.class))).thenThrow(new RestClientException("mock"));
		sensorClient.setRestTemplate(restTemplate);
		//even though internally he gets the rest exception it will not show here; if it does its wrong.
		sensorClient.startTracking(delegateExecution, cmsWorkOrderSimple);
	}
*/	
	
    /**
     * places the name/values into the runtime environment
     * @param newenv
     * @throws Exception
     */
    private void setEnvironmentVariable() {
            //force some entry into the env variables
            //otherwise construction will fail
            Map<String,String> requiredMap = new HashMap<String,String>(1);
            requiredMap.put("SKIP_SENSOR", "no");
            try {
                    setIntoEnvVariables(requiredMap);
            } catch (Exception e) {
                    e.printStackTrace();
                    throw(new UnsupportedOperationException(e));
            }
    }

    /** code that can inject a new env variable into this jvm */
    private static void setIntoEnvVariables(Map<String, String> newenv) throws Exception {
        Class[] classes = Collections.class.getDeclaredClasses();
        Map<String, String> env = System.getenv();
        for(Class cl : classes) {
            if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {

                    Field field = cl.getDeclaredField("m");
                field.setAccessible(true);
                Object obj = field.get(env);
                Map<String, String> map = (Map<String, String>) obj;
                map.clear();
                map.putAll(newenv);
            }
        }
    }
}
