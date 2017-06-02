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
package com.oneops.controller.web;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.oneops.controller.workflow.WoDispatcher;


public class CtrlrServletTest {
	private final CtrlrServlet servlet = new CtrlrServlet();

	
	@BeforeTest
	public void setUp(){
		RuntimeService rts = mock(RuntimeService.class);
		List<ProcessInstance> processList = new ArrayList<ProcessInstance>();
		ExecutionEntity executionEntity = new  ExecutionEntity();
		executionEntity.setActive(false);
		processList.add(executionEntity);
		
		ProcessInstanceQuery pQuery = mock(ProcessInstanceQuery.class);
		when(pQuery.active()).thenReturn(pQuery);
		when(pQuery.list()).thenReturn(processList);
		when(rts.createProcessInstanceQuery()).thenReturn(pQuery);

		this.servlet.setRuntimeService(rts);
		this.servlet.setWoDispatcher(mock(WoDispatcher.class));
		this.servlet.init();
	}
	@Test
	/** create cirelation*/
	public void createCIRTest(){
		this.servlet.createCIRelation(null);
	}
	@Test
	/**test the test!*/
	public void test(){
		String message=	this.servlet.test();
		assertEquals(message,"Resumed all processes");
	}
}
