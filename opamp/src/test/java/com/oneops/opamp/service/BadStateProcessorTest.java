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
package com.oneops.opamp.service;

import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsProcedureDefinition;
import com.oneops.cms.cm.ops.service.OpsManager;
import com.oneops.cms.cm.ops.service.OpsProcedureProcessor;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.exceptions.OpsException;
import com.oneops.opamp.exceptions.OpampException;
import com.oneops.opamp.util.EventUtil;
import com.oneops.ops.CiOpsProcessor;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.ops.events.CiOpenEvent;
import com.oneops.ops.events.OpsBaseEvent;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

import static org.mockito.Matchers.*;
import static org.testng.Assert.assertEquals;

import com.oneops.cms.cm.service.CmsCmManager;


public class BadStateProcessorTest {

	@Test
	public void constructs(){
		BadStateProcessor badStateProcessor = new BadStateProcessor();
		badStateProcessor.setCmProcessor(new CmsCmProcessor());
		badStateProcessor.setCoProcessor(new CiOpsProcessor());
		badStateProcessor.setEnvProcessor(new EnvPropsProcessor());
		badStateProcessor.setNotifier(new Notifications());
		badStateProcessor.setOpsProcProcessor(new OpsProcedureProcessor());
    	
	}

    @Test(expectedExceptions = OpsException.class)
    public void simulateJmsError() throws OpampException {
        Long aCid = 678L;

        BadStateProcessor bad = new BadStateProcessor();
        //this mock will throw exception when asked to processProcedureRequest
        OpsProcedureProcessor opsProcedureProcessor = mock(OpsProcedureProcessor.class);
        EventUtil eventUtil = mock(EventUtil.class);
        bad.setEventUtil(eventUtil);
        when(opsProcedureProcessor.processProcedureRequest((CmsOpsProcedure) anyObject(), (OpsProcedureDefinition) anyObject()))
                .thenThrow(new OpsException(0, "mock-exception"));
        //wire the mocks here
        bad.setOpsProcProcessor(opsProcedureProcessor);
        OpsBaseEvent event = new OpsBaseEvent();
        event.setCiId(111L);
        CiOpsProcessor copMock = mock(CiOpsProcessor.class);
        when(copMock.getCIstate(aCid)).thenReturn("unhealthy");
        when(copMock.getCisOpenEvents(anyList())).thenReturn(null);

        bad.setCoProcessor(copMock);
        CiChangeStateEvent ciChangeStateEvent = new CiChangeStateEvent();
        long unhealthyStartTime = System.currentTimeMillis() - 1 * 60 *60 * 1000;
        long coolOffPeriodMillis = 15 * 60 * 1000;
        bad.submitRepairProcedure(ciChangeStateEvent, false, unhealthyStartTime, 1, coolOffPeriodMillis);
    }

	@Test
	/** where we send a good state request */
	public void processGoodStateTest(){
		Long aCid = 678L;

		BadStateProcessor bsp = new BadStateProcessor();
		
		bsp.setNotifier(mock(Notifications.class));
		
		EnvPropsProcessor envProcessorMock= mock(EnvPropsProcessor.class);
		CmsCI cmsCI = new CmsCI();

        when(envProcessorMock.isAutorepairEnabled(aCid)).thenReturn(true);
		bsp.setEnvProcessor(envProcessorMock);
		
		CmsCmProcessor cmProcessor = mock(CmsCmProcessor.class);
		List<CmsCIRelation> dependsList =new ArrayList<CmsCIRelation>();
		CmsCIRelation relation=new CmsCIRelation();
		relation.setFromCiId(aCid );
		dependsList.add(relation);
		
		when(cmProcessor.getToCIRelationsNakedNoAttrs(aCid, null, "DependsOn", null)).thenReturn(dependsList);
		bsp.setCmProcessor(cmProcessor);
		
		when(cmProcessor.getFromCIRelationsNakedNoAttrs(aCid, null, "DependsOn", null)).thenReturn(dependsList);

		CiOpsProcessor copMock=mock(CiOpsProcessor.class);
		when(copMock.getCIstate(aCid)).thenReturn("unhealthy");
		
		bsp.setCoProcessor(copMock);
		CiChangeStateEvent changeEvent = new CiChangeStateEvent();
		OpsBaseEvent event = new OpsBaseEvent();
		event.setCiId(aCid);
		EventUtil eventUtil = mock(EventUtil.class);
		bsp.setEventUtil(eventUtil);
		when(eventUtil.getOpsEvent(changeEvent)).thenReturn(event);
		bsp.processGoodState(changeEvent);
	}
	@Test
	public void processUnhealthyStateTest() throws OpampException{
		Long aCid = 321L;
		BadStateProcessor bsp = new BadStateProcessor();
		bsp.setNotifier(mock(Notifications.class));
			
		CiOpsProcessor copMock=mock(CiOpsProcessor.class);
		when(copMock.getCIstate(aCid)).thenReturn("unhealthy");
		bsp.setCoProcessor(copMock);
		
		EnvPropsProcessor envProcessorMock= mock(EnvPropsProcessor.class);
        when(envProcessorMock.isAutorepairEnabled(aCid)).thenReturn(false);
		bsp.setEnvProcessor(envProcessorMock);
		//autoRepair is not enabled, and so....here
		CiChangeStateEvent changeEvent = new CiChangeStateEvent();
	
		OpsBaseEvent event = new OpsBaseEvent();
		event.setCiId(aCid);
		EventUtil eventUtil = mock(EventUtil.class);
		bsp.setEventUtil(eventUtil);
		when(eventUtil.getOpsEvent(changeEvent)).thenReturn(event);

	
		
		bsp.processUnhealthyState(changeEvent); //notifier gets asked sendUnhealthyNotificationNoRepair		
		
	}
	@Test
	public void processUnhealthStateTestAR() throws OpampException{
		Long aCid = 1234L;
		BadStateProcessor bsp = new BadStateProcessor();
		List<CmsCIRelation> singleRel = new ArrayList<CmsCIRelation>(1);
		CmsCIRelation cmsCiRelation = new CmsCIRelation();
		
		singleRel.add(cmsCiRelation);

		CmsCmProcessor cmsCmProcessorMock = mock(CmsCmProcessor.class);
		when(cmsCmProcessorMock.getFromCIRelationsNakedNoAttrs(aCid,null, "DependsOn",null)).thenReturn(singleRel);
//			
		EnvPropsProcessor envProcessorYesMock= mock(EnvPropsProcessor.class);
        when(envProcessorYesMock.isAutorepairEnabled(aCid)).thenReturn(true);

		CiOpsProcessor coProcessor = mock(CiOpsProcessor.class);
		when(coProcessor.getCIstate(anyLong())).thenReturn("actually-bad"); 
		//repairBad, but does not isDependsOnGood so just will tell the notifier
		bsp.setEnvProcessor(envProcessorYesMock);
		bsp.setCmProcessor(cmsCmProcessorMock);
		bsp.setCoProcessor(coProcessor);
		bsp.setNotifier(mock(Notifications.class));
		OpsBaseEvent event = new OpsBaseEvent();
		event.setCiId(aCid);
		CiChangeStateEvent changeEvent = new CiChangeStateEvent();
		
		EventUtil eventUtil = mock(EventUtil.class);
		bsp.setEventUtil(eventUtil);
		when(eventUtil.getOpsEvent(changeEvent)).thenReturn(event);
		bsp.processUnhealthyState(changeEvent);
	}
	
	@Test
	public void testExponentialDelayFunction() throws Exception {
		int coolOff = 15 * 60 * 1000;
		int exponentialFactor = 2;
		int repairRetriesCountSinceDelay = 6;
		int maxDaysRepair = 11;
		Calendar calendar_Oct05_0000_2016 = new GregorianCalendar(2016, 9, 5);
		long maxRepairRetryPeriod = maxDaysRepair * 24 * 60 * 60 * 1000; 
		
		long nextTime = BadStateProcessor.getNextRepairTime(calendar_Oct05_0000_2016.getTimeInMillis(), coolOff, exponentialFactor, repairRetriesCountSinceDelay, maxRepairRetryPeriod);
		System.out.println(" For startTime " + calendar_Oct05_0000_2016.getTime() + " next time : " + new Date(nextTime));
		Calendar calendar_Oct06_074500_2016 = new GregorianCalendar(2016, 9, 6, 7, 45, 0);//Oct 06 07:45:00 2016
		Assert.assertEquals(calendar_Oct06_074500_2016.getTimeInMillis(), nextTime);

		nextTime = BadStateProcessor.getNextRepairTime(calendar_Oct05_0000_2016.getTimeInMillis(), coolOff, exponentialFactor, 10, maxRepairRetryPeriod);
		System.out.println(" For startTime " + calendar_Oct05_0000_2016.getTime() + " next time : " + new Date(nextTime));
		Calendar calendar_Oct26_074500_2016 = new GregorianCalendar(2016, 9, 26, 07, 45, 0);//Oct 26 07:45:00 2016
		Assert.assertEquals(calendar_Oct26_074500_2016.getTimeInMillis(), nextTime);

		nextTime = BadStateProcessor.getNextRepairTime(calendar_Oct05_0000_2016.getTimeInMillis(), coolOff, exponentialFactor, 40, maxRepairRetryPeriod);
		System.out.println(" For startTime " + calendar_Oct05_0000_2016.getTime() + " next time : " + new Date(nextTime));
		Assert.assertEquals(calendar_Oct26_074500_2016.getTimeInMillis(), nextTime);

	}

	@Test
	public void testProcQueryCount4UnhealthyState() throws OpampException {
		Long ci1 = 321L;
		Long ci2 = 123L;
		BadStateProcessor bsp = new BadStateProcessor();
		bsp.setNotifier(mock(Notifications.class));
		OpsProcedureProcessor opsProcedureProcessor = mock(OpsProcedureProcessor.class);
		CmsOpsProcedure proc = new CmsOpsProcedure();
		proc.setProcedureId(1000L);
		when(opsProcedureProcessor.processProcedureRequest(any(), any())).thenReturn(proc);
		bsp.setOpsProcProcessor(opsProcedureProcessor);

		CiOpsProcessor copMock=mock(CiOpsProcessor.class);
		when(copMock.getCIstate(anyLong())).thenReturn("unhealthy");
		CiOpenEvent openEvent = new CiOpenEvent();
		openEvent.setState("unhealthy");
		//very old (60 days) unhealthy start event
		long ts = System.currentTimeMillis() - (60 * 24 * 60 * 60 * 1000L);
		openEvent.setTimestamp(ts);

		CiOpenEvent openEvent1 = new CiOpenEvent();
		openEvent1.setState("unhealthy");
		//unhealthy start event 2 days back
		long ts1 = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L);
		openEvent1.setTimestamp(ts1);

		Map<Long, List<CiOpenEvent>> openEvents = new HashMap<>();
		openEvents.put(ci1, Collections.singletonList(openEvent));
		openEvents.put(ci2, Collections.singletonList(openEvent1));
		when(copMock.getCisOpenEvents(any())).thenReturn(openEvents);
		bsp.setCoProcessor(copMock);

		List<CmsCIRelation> emptyList = Collections.emptyList();
		CmsCmProcessor cmsCmProcessorMock = mock(CmsCmProcessor.class);
		when(cmsCmProcessorMock.getFromCIRelationsNakedNoAttrs(anyLong(), any(), eq("DependsOn"),any())).thenReturn(emptyList);
		bsp.setCmProcessor(cmsCmProcessorMock);

		EnvPropsProcessor envProcessorMock = mock(EnvPropsProcessor.class);
		when(envProcessorMock.isAutorepairEnabled(anyLong())).thenReturn(true);
		when(envProcessorMock.isCloudActive4Bom(anyLong(), any())).thenReturn(true);
		when(envProcessorMock.getPlatform4Bom(anyLong())).thenReturn(new CmsCI());
		when(envProcessorMock.isRepairDelayEnabled(any())).thenReturn(true);
		bsp.setEnvProcessor(envProcessorMock);

		CiChangeStateEvent changeEvent = new CiChangeStateEvent();
		changeEvent.setCiId(ci1);
		changeEvent.setPayLoad("{\"coolOff\" : 15}");

		OpsBaseEvent event = new OpsBaseEvent();
		event.setCiId(ci1);
		EventUtil eventUtil = mock(EventUtil.class);
		bsp.setEventUtil(eventUtil);
		when(eventUtil.getOpsEvent(changeEvent)).thenReturn(event);
		when(eventUtil.getGson()).thenReturn(new Gson());

		OpsManager opsMock = mock(OpsManager.class);
		bsp.setOpsManager(opsMock);
		long minCheckTime4ProcCount = System.currentTimeMillis() - (15 * 24 * 60 * 60 * 1000L);

		bsp.processUnhealthyState(changeEvent);
		event.setCiId(ci2);
		changeEvent.setCiId(ci2);

		bsp.processUnhealthyState(changeEvent);

		ArgumentCaptor<Date> captor = ArgumentCaptor.forClass(Date.class);
		verify(opsMock, times(1)).getCmsOpsProceduresCountForCiFromTime(anyLong(), any(), eq("ci_repair"), captor.capture());
		List<Date> values = captor.getAllValues();
		Assert.assertTrue(values.get(0).getTime() - ts1 < (60 * 1000L));
	}
	
	@Test(enabled = true)
	public void OpampRsController_replaceByCid_AutoReplaceEnabled() {
		long ciId = 999L;
		String userId = "TestUser";
		String description = "TestDescription";

		BadStateProcessor bsProcessor = new BadStateProcessor();

		EnvPropsProcessor envProcessor = mock(EnvPropsProcessor.class);
		CmsCmManager cmManager = mock(CmsCmManager.class);
		;
		RestTemplate restTemplate = mock(RestTemplate.class);

		CmsCI platform = mock(CmsCI.class);
		CmsCI env = mock(CmsCI.class);

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

		Map<String, Integer> result = bsProcessor.replaceByCid(ciId, userId, description);
		assertEquals(result.size(), 1);
		assertEquals(result.get("deploymentId"), expectedDeploymentId);
	}

	@Test(enabled = true)
	public void OpampRsController_replaceByCid_AutoReplaceDisabled() {
		long ciId = 999L;
		String userId = "TestUser";
		String description = "TestDescription";

		BadStateProcessor bsProcessor = new BadStateProcessor();

		EnvPropsProcessor envProcessor = mock(EnvPropsProcessor.class);
		CmsCmManager cmManager = mock(CmsCmManager.class);
		;
		RestTemplate restTemplate = mock(RestTemplate.class);

		CmsCI platform = mock(CmsCI.class);
		CmsCI env = mock(CmsCI.class);

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

		Map<String, Integer> result = bsProcessor.replaceByCid(ciId, userId, description);
		assertEquals(result.size(), 1);
		assertEquals(result.get("deploymentId"), expectedDeploymentId);
	}

	@Test(enabled = true)
	public void OpampRsController_replaceByCid_isOpenRelease() {
		long ciId = 999L;
		String userId = "TestUser";
		String description = "TestDescription";

		BadStateProcessor bsProcessor = new BadStateProcessor();

		EnvPropsProcessor envProcessor = mock(EnvPropsProcessor.class);
		CmsCmManager cmManager = mock(CmsCmManager.class);
		;
		RestTemplate restTemplate = mock(RestTemplate.class);

		CmsCI platform = mock(CmsCI.class);
		CmsCI env = mock(CmsCI.class);

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

		Map<String, Integer> result = bsProcessor.replaceByCid(ciId, userId, description);
		assertEquals(result.size(), 1);
		assertEquals(result.get("deploymentId"), expectedDeploymentId);
	}

	@Test(enabled = true)
	public void OpampRsController_replaceByCid_isClosedRelease() {
		long ciId = 999L;
		String userId = "TestUser";
		String description = "TestDescription";

		BadStateProcessor bsProcessor = new BadStateProcessor();

		EnvPropsProcessor envProcessor = mock(EnvPropsProcessor.class);
		CmsCmManager cmManager = mock(CmsCmManager.class);
		;
		RestTemplate restTemplate = mock(RestTemplate.class);

		CmsCI platform = mock(CmsCI.class);
		CmsCI env = mock(CmsCI.class);

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

		Map<String, Integer> result = bsProcessor.replaceByCid(ciId, userId, description);
		assertEquals(result.size(), 1);
		assertEquals(result.get("deploymentId"), expectedDeploymentId);
	}
}
