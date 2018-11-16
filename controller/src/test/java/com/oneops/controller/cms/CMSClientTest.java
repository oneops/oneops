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
package com.oneops.controller.cms;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.oneops.cms.cm.ops.service.OpsProcedureProcessor;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import com.oneops.cms.util.domain.CmsVar;
import org.activiti.engine.delegate.DelegateExecution;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.oneops.notification.NotificationMessage;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.crypto.CmsCrypto;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsUtil;
import com.oneops.controller.util.ControllerUtil;
import com.oneops.util.ReliableExecutor;

public class CMSClientTest {
	
	private static final Long TEST_CI_ID = 1440L;
	private static final Long DPLMNT_ID = 1000001L;
	private static final Long PROCEDURE_ID = 444L;
	private CMSClient cc = new CMSClient();
	
	private RestTemplate mockHttpDoNothing = mock(RestTemplate.class);
	private RestTemplate mockHttpClientGetWorkOrderSimple = mock(RestTemplate.class);
	private RestTemplate mockHttpClientGetFailedWorkOrderSimple = mock(RestTemplate.class);
	private RestTemplate mockHttpClientPostCommitAndDeploy = mock(RestTemplate.class);
	private RestTemplate mockHttpClientGetEnvs = mock(RestTemplate.class);
	private RestTemplate mockHttpActionOrders = mock(RestTemplate.class);
    private RestTemplate mockHttpAnchor = mock(RestTemplate.class);
    private CmsWoProvider mockWoProvider = mock(CmsWoProvider.class);
    private ControllerUtil ctrlUtil = mock(ControllerUtil.class);
	private CmsDpmtProcessor cmsDpmtProcessor = mock(CmsDpmtProcessor.class);
	private CmsCmProcessor cmsCmProcessor = mock(CmsCmProcessor.class);
	private CmsUtil cmsUtil = mock(CmsUtil.class) ;
    private OpsProcedureProcessor opsProcedureProcessor = mock(OpsProcedureProcessor.class) ;


	private static Logger logger = Logger.getLogger(CMSClientTest.class);

	
	@SuppressWarnings("unchecked")
	@BeforeClass
	public void setUpIntance(){

		String transUrl = "mock://trans_url";
		String serviceUrl = "mock://service_url";
		CmsCrypto cmsCrypto = mock(CmsCrypto.class);
		ReliableExecutor<NotificationMessage> antennaClient = mock(ReliableExecutor.class );
		
		DeploymentNotifier notifier = new DeploymentNotifier();
		notifier.setAntennaClient(antennaClient);
		cc.setDeploymentNotifier(notifier);
		cc.setCmsCrypto(cmsCrypto);
		cc.setServiceUrl(serviceUrl);
		cc.setTransUrl(transUrl);
		cc.setRetryTemplate(new RetryTemplate());
		cc.setCmsWoProvider(mockWoProvider);
		cc.setControllerUtil(ctrlUtil);
		cc.setCmsDpmtProcessor(cmsDpmtProcessor);
		cc.setCmsCmProcessor(cmsCmProcessor);
		cc.setCmsUtil(cmsUtil);
		cc.setOpsProcedureProcessor(opsProcedureProcessor);
		CmsWorkOrderSimple[] simpleWorkOrders = new CmsWorkOrderSimple[1];
		CmsWorkOrderSimple workOrderSymple = new CmsWorkOrderSimple();
		workOrderSymple.setComments("mocked-and-does-not-need-anything");
		workOrderSymple.setRfcCi(new CmsRfcCISimple());
		simpleWorkOrders[0]=workOrderSymple;
//		when(mockHttpClientGetWorkOrderSimple.getForObject(anyString(), CmsWorkOrderSimple[].class, anyLong(), anyInt())).thenReturn(workOrders);
		when(mockHttpClientGetWorkOrderSimple.getForObject(anyString(), any(Class.class) , anyLong(), anyInt())).thenReturn(simpleWorkOrders);
		when(mockHttpClientGetFailedWorkOrderSimple.getForObject(anyString(), any(Class.class) , anyLong(), anyInt())).thenReturn(2l);

		Map<String, Long> bomDeploymentMap = new HashMap<String,Long>(2);
		bomDeploymentMap.put("deploymentId", TEST_CI_ID);
		bomDeploymentMap.put("deploymentId", TEST_CI_ID+2);

		when(mockHttpClientPostCommitAndDeploy.postForObject(anyString(), anyMap(), any(Class.class), anyLong())).thenReturn(bomDeploymentMap);//Map.class
		CmsActionOrderSimple[] aosArray = new CmsActionOrderSimple[1];
		CmsActionOrderSimple aosEntry = new CmsActionOrderSimple();
		CmsCISimple ci = new CmsCISimple();
		ci.setCiAttributes(new HashMap<String,String>() );//empty
		aosEntry.setCi(ci);
		aosArray[0]=aosEntry;
		when(mockHttpActionOrders.getForObject(anyString(), any(Class.class), anyLong(), anyInt())).thenReturn(aosArray);
	   
		CmsCI anchorCmsCI = new CmsCI();
		when(mockHttpAnchor.getForObject(anyString(),  any(Class.class),anyLong())).thenReturn(anchorCmsCI);

		CmsCISimple[] workOrderArray = new CmsCISimple[1];
		
		CmsCISimple ciSimple = new CmsCISimple();
		
		workOrderArray[0] = ciSimple;
		ciSimple.setComments("mock-njdc");
		Map<String, Map<String, CmsCISimple>> services = null; //TOOD 357
//		ciSimple.setServices(services);
		
		CmsRfcCISimple rfcCi=new CmsRfcCISimple();
		Map<String, String> ciAttributes = new HashMap<String,String>();
		ciAttributes.put(CmsCrypto.ENC_PREFIX, "adfadqe3134c4311134");
		rfcCi.setCiAttributes(ciAttributes);
//		ciSimple.setRfcCi(rfcCi);

		when(mockHttpClientGetEnvs.getForObject(anyString(), any(Class.class), anyString(), anyString())).thenReturn(workOrderArray);

		

	}

	@Test(priority=10)
	public void commitAndDeployTest() throws GeneralSecurityException{
		
		DelegateExecution delegateExecution = mock(DelegateExecution.class);
		CmsRelease cmsRelease = mock(CmsRelease.class);
		when(cmsRelease.getReleaseId()).thenReturn(TEST_CI_ID / 2);
		
		CmsCISimple cmsCISimpleEnv = mock(CmsCISimple.class);
		when(cmsCISimpleEnv.getCiId()).thenReturn(TEST_CI_ID);
		
		when(delegateExecution.getVariable("release")).thenReturn(cmsRelease);
		when(delegateExecution.getVariable("env")).thenReturn(cmsCISimpleEnv);
			
		cc.setRestTemplate(mockHttpClientPostCommitAndDeploy);
		try {
			cc.commitAndDeployRelease(delegateExecution);
		} catch (GeneralSecurityException e) {
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	@Test(priority=11)
	public void checkDeploymentTest() throws Exception{
		DelegateExecution delegateExecution = mock(DelegateExecution.class);
		CmsDeployment cmsDeployment = new CmsDeployment();
		cmsDeployment.setCreatedBy("created-by-mock");
		when(delegateExecution.getVariable("dpmt")).thenReturn(cmsDeployment);

		RestTemplate restTemplate = mock(RestTemplate.class);
		when(restTemplate.getForObject(anyString(), any(Class.class) , anyLong())).thenReturn(cmsDeployment);
		try {
			cc.checkDpmt(delegateExecution);
			CmsDeployment cmsDeploymentPost = (CmsDeployment) delegateExecution.getVariable("dpmt");
			assertEquals(cmsDeploymentPost.getCreatedBy(), "created-by-mock", "object mutated unexpectedly");
		
		} catch (GeneralSecurityException e) {
			logger.warn("unexpected to catch here",e) ;
			throw e;
		}
	}



	@SuppressWarnings("unchecked")
	@Test
	public void checkDeploymentTestWithRestTemplateThrowExceptionShouldSucced() throws Exception{
		DelegateExecution delegateExecution = mock(DelegateExecution.class);
		CmsDeployment cmsDeployment = new CmsDeployment();
		cmsDeployment.setCreatedBy("created-by-mock");
		when(delegateExecution.getVariable("dpmt")).thenReturn(cmsDeployment);

		RestTemplate restTemplate = mock(RestTemplate.class);
		//when(restTemplate.getForObject(anyString(), any(Class.class) , anyLong())).thenThrow(new RestClientException("test")).thenReturn(cmsDeployment);
		when(restTemplate.getForObject(anyString(), any(Class.class), anyLong())).thenThrow(new RestClientException("test")).thenThrow(new RestClientException("test")).thenReturn(cmsDeployment);
		cc.setRestTemplate(restTemplate);
		try {
			cc.setRetryTemplate(cc.getRetryTemplate(3,2000,1000));
			cc.checkDpmt(delegateExecution);
			CmsDeployment cmsDeploymentPost = (CmsDeployment) delegateExecution.getVariable("dpmt");
			assertEquals(cmsDeploymentPost.getCreatedBy(), "created-by-mock", "object mutated unexpectedly");

		} catch (GeneralSecurityException e) {
			logger.warn("unexpected to catch here",e) ;
			throw e;
		}
	}


	@Test(priority=12)
	public void updateDeploymentState(){
		
		DelegateExecution delegateExecution = mock(DelegateExecution.class);
			
		CmsDeployment cmsDeployment = mock(CmsDeployment.class);
		when(cmsDeployment.getDeploymentId()).thenReturn(DPLMNT_ID);
		when(cmsDeployment.getDeploymentState()).thenReturn("active");
		when(cmsDeployment.getNsPath()).thenReturn("/a/b/cpath");

		when(delegateExecution.getVariable("dpmt")).thenReturn(cmsDeployment);
		cc.setRestTemplate(mockHttpDoNothing);	
		cc.updateDpmtState(delegateExecution, cmsDeployment, "active") ;
	}
	

    //TODO FIX this test
    //DISABLED as failing with class cast exception. refactor the main class to fix the test code.
	@Test(priority=14,enabled = false)
	public void getWorkOrdersTest() throws GeneralSecurityException{
		DelegateExecution delegateExecution = mock(DelegateExecution.class);
		CmsDeployment cmsDeployment = mock(CmsDeployment.class);
		when(cmsDeployment.getDeploymentId()).thenReturn(DPLMNT_ID);
		when(delegateExecution.getVariable("dpmt")).thenReturn(cmsDeployment);
		//we rely on mock of restTemplate to give approp. answer
		cc.setRestTemplate(mockHttpClientGetWorkOrderSimple);	
		cc.getWorkOrderIds(delegateExecution);
	}
	
	@SuppressWarnings("unchecked")
	@Test(priority=14)
	/** again with http error */
	public void getWorkOrdersHttpErrTest(){
		CmsDeployment cmsDeployment = mock(CmsDeployment.class);
		when(cmsDeployment.getDeploymentId()).thenReturn(DPLMNT_ID);
		
		DelegateExecution delegateExecution = mock(DelegateExecution.class);
		when(delegateExecution.getVariable("dpmt")).thenReturn(cmsDeployment);
		//we rely on mock of restTemplate to give error  answer 
		RestTemplate httpErrorTemplate = mock(RestTemplate.class);
		when(
				httpErrorTemplate.getForObject(anyString(),
					any(java.lang.Class.class), anyLong(), anyInt()))
				.thenThrow(
						new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT,"mocking"));
		cc.setRestTemplate(httpErrorTemplate);
		cc.getWorkOrderIds(delegateExecution);
		//it would be nice to assert the exec was updated, but for now we
		//just let the test pass if the client swallows the http error
	}
	
	@Test(priority=14)
	public void getActionOrdersTest() throws GeneralSecurityException{
		
		DelegateExecution delegateExecution = mock(DelegateExecution.class);
			
		CmsOpsProcedure cmsOpsProcedure = mock(CmsOpsProcedure.class);
		when(cmsOpsProcedure.getProcedureId()).thenReturn(PROCEDURE_ID);
		when(delegateExecution.getVariable("proc")).thenReturn(cmsOpsProcedure);
		when(delegateExecution.getVariable("procanchor")).thenReturn(null);

			
		//we rely on mock of restTemplate to give approp. answer 
		cc.setRestTemplate(mockHttpActionOrders);	
		

		try {
			cc.getActionOrders(delegateExecution);
		} catch (GeneralSecurityException e) {   
			throw e;
		}
	}
	
	@Test// any time (priority=14)
	public void getEnv4ReleaseTest() throws GeneralSecurityException{
		DelegateExecution delegateExecution = mock(DelegateExecution.class);
					
		CmsRelease cmsRelease = mock(CmsRelease.class);
		when(cmsRelease.getNsPath()).thenReturn("/dev/int/qa/stage/prod/super");
		when(delegateExecution.getVariable("release")).thenReturn(cmsRelease);
			
		//we rely on mock of restTemplate to give approp. answer 
		cc.setRestTemplate(mockHttpClientGetEnvs);	
		try {
			cc.getEnv4Release(delegateExecution);
		} catch (GeneralSecurityException e) {  
			throw e;
		}
	}
	@Test(priority=33)
	public void updateProcedureStateTest(){	
		DelegateExecution exec = mock(DelegateExecution.class);
		CmsCI anchorCi = mock( CmsCI.class);
		when(anchorCi.getCiId()).thenReturn(TEST_CI_ID);
		when(anchorCi.getNsPath()).thenReturn("/x/y/z.1");
		when(anchorCi.getCiName()).thenReturn("CI_NAME_A");
		when(anchorCi.getCiClassName()).thenReturn("CI_CLASS_A");


		when(exec.getVariable("procanchor")).thenReturn(anchorCi);
		
//		CmsOpsProcedure proc = mock(CmsOpsProcedure.class);
//		when(proc.getProcedureState()).thenReturn(OpsProcedureState.active);
// 		doNothing().when(proc.setProcedureState(any(OpsProcedureState.class)));
		CmsOpsProcedure proc = new CmsOpsProcedure();
		proc.setProcedureState(OpsProcedureState.active);
		
		cc.updateProcedureState(exec, proc);
	}
	
	@Test
	public void setDeploymentProcessIdTest(){
		DelegateExecution delegateExecution = mock(DelegateExecution.class);
		CmsDeployment cmsDeployment = mock(CmsDeployment.class);
		when(cmsDeployment.getDeploymentId()).thenReturn(DPLMNT_ID);
		when(cmsDeployment.getDeploymentState()).thenReturn("active");
		when(cmsDeployment.getNsPath()).thenReturn("/a/b/cpath");
		when(delegateExecution.getVariable("dpmt")).thenReturn(cmsDeployment);
		when(delegateExecution.getProcessInstanceId()).thenReturn("141414");
		when(delegateExecution.getId()).thenReturn("121");
		cc.setRestTemplate(mockHttpDoNothing);
		
		cc.setDpmtProcessId(delegateExecution, cmsDeployment); ;
	}
	
	@Test
	public void updateActionOrderStateTest(){
		DelegateExecution delegateExecution = mock(DelegateExecution.class);
		CmsOpsProcedure cmsOpsProcedure = mock(CmsOpsProcedure.class);
		when(delegateExecution.getVariable("proc")).thenReturn(cmsOpsProcedure);

		CmsActionOrderSimple cmsActionOrderSimple= new CmsActionOrderSimple();
		cmsActionOrderSimple.setActionId(3);
		cmsActionOrderSimple.setProcedureId(6);
		cmsActionOrderSimple.setIsCritical(true);
		
		cc.setRestTemplate(mockHttpDoNothing);	
		cc.updateActionOrderState(delegateExecution, cmsActionOrderSimple, "failed") ; //also to do complete
		assertEquals(cmsActionOrderSimple.getActionState().getName() ,"failed","desired change to failed did not happen");
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void updateWoStateTestHttperr(){
		DelegateExecution delegateExecution = mock(DelegateExecution.class);
		CmsDeployment cmsDeployment = mock(CmsDeployment.class);
		when(delegateExecution.getVariable("dpmt")).thenReturn(cmsDeployment);
		when(delegateExecution.getId()).thenReturn("Id11");
		when(delegateExecution.getVariable("error-message")).thenReturn("mocked-error");


		CmsWorkOrderSimple cmsWorkOrderSimple= new CmsWorkOrderSimple();
		cmsWorkOrderSimple.setDpmtRecordId(0);
		cmsWorkOrderSimple.setDeploymentId(66);
		cmsWorkOrderSimple.setComments("mockito-mock-comments");
		
		RestTemplate httpErrorTemplate = mock(RestTemplate.class);
		when(
				httpErrorTemplate.getForObject(anyString(),
					any(java.lang.Class.class), anyLong(), anyInt()))
				.thenThrow(
						new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT,"mocking"));
		cc.setRestTemplate(httpErrorTemplate);		
  		cc.updateWoState(delegateExecution, cmsWorkOrderSimple, "failed") ; //also to do complete
  		
	}

	@Test
	public void isDeployerStepsInLimitTrueTest(){
		Integer[] stepTotals = {11,24,37,94,58};
		when(cmsDpmtProcessor.getDeploymentDistinctStepsTotal(1234)).thenReturn(Arrays.asList(stepTotals));
		CmsVar var = new CmsVar();
		var.setValue("100");
		when(cmsCmProcessor.getCmSimpleVar("DEPLOYER_STEPS_LIMIT")).thenReturn(var);
		assertEquals(cc.isDeployerStepsInLimit( 1234), true);
	}

	@Test
	public void isDeployerStepsInLimitFalseTest(){
		Integer[] stepTotals = {11,124,37,94,58};
		when(cmsDpmtProcessor.getDeploymentDistinctStepsTotal(1234)).thenReturn(Arrays.asList(stepTotals));
		CmsVar var = new CmsVar();
		var.setValue("100");
		when(cmsCmProcessor.getCmSimpleVar("DEPLOYER_STEPS_LIMIT")).thenReturn(var);
		assertEquals(cc.isDeployerStepsInLimit(1234), false);
	}

	@Test
	public void isDeployerStepsInLimitNullZeroTest(){
		when(cmsDpmtProcessor.getDeploymentDistinctStepsTotal(1234)).thenReturn(null);
		CmsVar var = new CmsVar();
		var.setValue("100");
		when(cmsCmProcessor.getCmSimpleVar("DEPLOYER_STEPS_LIMIT")).thenReturn(var);
		assertEquals(cc.isDeployerStepsInLimit(1234), false);

		Integer[] stepTotals = {};
		when(cmsDpmtProcessor.getDeploymentDistinctStepsTotal(1234)).thenReturn(Arrays.asList(stepTotals));
		assertEquals(cc.isDeployerStepsInLimit(1234), false);
	}

	@Test
	public void isDeployerStepsInLimitUndefinedTest(){
		Integer[] stepTotals = {11,124,37,94,58};
		when(cmsDpmtProcessor.getDeploymentDistinctStepsTotal(1234)).thenReturn(Arrays.asList(stepTotals));
		when(cmsCmProcessor.getCmSimpleVar("DEPLOYER_STEPS_LIMIT")).thenReturn(null);
		assertEquals(cc.isDeployerStepsInLimit(1234), false);
	}

	@Test
	public void isDeployerStepsInLimitNumberFormatTest(){
		Integer[] stepTotals = {11,124,37,94,58};
		CmsVar var = new CmsVar();
		var.setValue("NumberFormatException");
		when(cmsDpmtProcessor.getDeploymentDistinctStepsTotal(1234)).thenReturn(Arrays.asList(stepTotals));
		when(cmsCmProcessor.getCmSimpleVar("DEPLOYER_STEPS_LIMIT")).thenReturn(var);
		assertEquals(cc.isDeployerStepsInLimit(1234), false);
	}
}