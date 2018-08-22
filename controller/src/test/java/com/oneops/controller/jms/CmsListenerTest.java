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
package com.oneops.controller.jms;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import com.oneops.cms.util.CmsConstants;
import com.oneops.controller.cms.CMSClient;
import com.oneops.controller.workflow.ExecutionManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.controller.workflow.WorkflowController;

public class CmsListenerTest {
	
	private static ApplicationContext context ;
	private CmsListener listener;
	private Gson gson = new Gson();

	
	@BeforeClass
	public void setUp() throws JMSException{
		context = new ClassPathXmlApplicationContext("**/test-app-context.xml");
		//load instance of listener, with dependencies injected
		listener = (CmsListener) context
				.getBean("cmsListener");

		WorkflowController wfController = mock(WorkflowController.class);
		listener.setWfController(wfController);
		listener.init();
		listener.getConnectionStats(); //exercise the Util class here

	}
	//test for source=deployment opsprocedure release and-eac time some other//
	//--------------------------------------------------------------//
	@Test 
	public void msgDeployment() throws Exception{
		TextMessage message = mock(TextMessage.class);
		when(message.getStringProperty("source")).thenReturn("deployment");
		String msgJson = gson.toJson(createCmsDeployment("active"));
		when(message.getText()).thenReturn(msgJson);

		listener.onMessage(message);
		//once more to get a SKIP
		when(message.getText()).thenReturn(gson.toJson(createCmsDeployment("is-not-active")));	
		listener.onMessage(message);
		
		listener.cleanup();

		listener.getConnectionStats();
	}
	@Test 
	public void msgOpsProcedure() throws Exception{
		TextMessage message = mock(TextMessage.class);
		when(message.getStringProperty("source")).thenReturn("opsprocedure");
		when(message.getJMSCorrelationID()).thenReturn(null);

		String msgJson = gson.toJson(createCmsOpsProcedure(OpsProcedureState.active));
		when(message.getText()).thenReturn(msgJson);

		listener.onMessage(message);
		//once more to get a SKIP
		when(message.getText()).thenReturn(gson.toJson(createCmsOpsProcedure(OpsProcedureState.discarded)));	
		listener.onMessage(message);
	}	
	@Test 
	public void msgRelease() throws Exception{
		TextMessage message = mock(TextMessage.class);
		when(message.getStringProperty("source")).thenReturn("release");
		String msgJson = gson.toJson(createCmsRelease("active"));
		when(message.getText()).thenReturn(msgJson);

		listener.onMessage(message);
		//once more to get a SKIP
		when(message.getText()).thenReturn(gson.toJson(createCmsRelease("aintactive")));	
		listener.onMessage(message);
	}		
	
	@Test
	/** test with message where JMSException is forced to happend
	 * but we effectively are asserting it must get swallowed*/
	public void testBadMessage() throws Exception{
		TextMessage message = mock(TextMessage.class);
		when (message.getText()).thenThrow(new JMSException("mock-forces-errorJMS"));
		listener.onMessage(message);


	}
	
	private CmsDeployment createCmsDeployment(String state){
		CmsDeployment cd = new CmsDeployment();
		cd.setDeploymentId(13579);
		cd.setDeploymentState(state);
		return cd;
		
	}
	private CmsOpsProcedure createCmsOpsProcedure(OpsProcedureState state){
		CmsOpsProcedure co = new CmsOpsProcedure();
		co.setProcedureState(state);
		return co;
		
	}
	private CmsOpsProcedure createCmsOpsProcedure(OpsProcedureState state, String nsPath){
		CmsOpsProcedure co = new CmsOpsProcedure();
		co.setProcedureState(state);
		co.setNsPath(nsPath);
		return co;

	}
	private CmsRelease createCmsRelease(String releaseType){
		CmsRelease cr = new CmsRelease();
		cr.setReleaseType(releaseType);
		return cr;
		
	}

	@Test
	public void isProcDeployerEnabled() throws Exception{
		TextMessage message = mock(TextMessage.class);
		when(message.getStringProperty("source")).thenReturn("opsprocedure");
		when(message.getJMSCorrelationID()).thenReturn(null);
		CMSClient cmsClient = mock(CMSClient.class);
		when(cmsClient.getVarByMatchingCriteriaBoolean(CmsConstants.PROC_DEPLOYER_ENABLED_PROPERTY, "/")).thenReturn(true);
		listener.setCmsClient(cmsClient);
		ExecutionManager executionManager = mock(ExecutionManager.class);
		listener.setExecutionManager(executionManager);
		String msgJson = gson.toJson(createCmsOpsProcedure(OpsProcedureState.active, "/"));
		when(message.getText()).thenReturn(msgJson);
		listener.setProcDeployerEnabledFlag(true);
		listener.onMessage(message);
	}
}
