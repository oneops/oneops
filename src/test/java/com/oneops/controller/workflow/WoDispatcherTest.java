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
package com.oneops.controller.workflow;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.activiti.engine.delegate.DelegateExecution;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.controller.domain.WoProcessResponse;
import com.oneops.controller.jms.InductorPublisher;

public class WoDispatcherTest {

	private final WoDispatcher wod = new WoDispatcher();

	
	@BeforeClass
	public void setUp(){
		wod.setInductorPublisher(mock(InductorPublisher.class));
		wod.setWfController(mock(WorkflowController.class));
		
	}
	
	@Test
	public void processWORResultTest(){
		WoProcessResponse woResultOut = new WoProcessResponse();
		woResultOut.setWoProcessResult("result-mocked");
		woResultOut.setProcessId("11!22!33");
		
		CmsWorkOrderSimple woResult = new CmsWorkOrderSimple();		
		woResultOut.setWo(woResult);
		
		wod.processWOResult(woResultOut);
	
	}
	
	@Test
	public void distachWOTest(){
		DelegateExecution delegateExecution = mock(DelegateExecution.class);

		
		CmsWorkOrderSimple cmsWorkOrderSimple = new CmsWorkOrderSimple();
		CmsRfcCISimple rfcCi = new CmsRfcCISimple();
		rfcCi.setImpl("class::com.oneops.controller.workflow.WoProcessorMockImpl::rfc::impl");
		cmsWorkOrderSimple.rfcCi= rfcCi;
		
		when(delegateExecution.getVariable("wo")).thenReturn(cmsWorkOrderSimple);
		
		when(delegateExecution.getId()).thenReturn("88888");

		when(delegateExecution.getProcessInstanceId()).thenReturn("55555");
		
		wod.dispatchWO(delegateExecution, cmsWorkOrderSimple, "mock-waiter");
		
	}

	
}

