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

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.controller.cms.CMSClient;
import com.oneops.controller.cms.DeploymentNotifier;
import com.oneops.util.ReliableExecutor;

public class WorkFlowControllerTest {

	private WorkflowController wfc = new WorkflowController();
	private static final String PROCESS_START_KEY = "key-pst";
	private static final String RUNNER_PID = "12468!1579";
	private static final String EXECUTION_ID = "EXECA";
	private static final String NEW_PID = "1A2B3C";//when we start a process
	private static final String WAIT_TASK_ID = "11111!222222";
	RuntimeService runtimeServiceProcessMock = mock(RuntimeService.class);
	CMSClient csmClientMock = mock(CMSClient.class);
	RuntimeService runtimeServiceExecutionMock = mock(RuntimeService.class);
	
	/** used in mock operations. must have this because WorkFlowController must be able to cast
	 * our ProcessInstance to an activiti ProcessInstance
	 */
	private class ProcessInstanceStub extends org.activiti.engine.impl.persistence.entity.ExecutionEntity {
		private static final long serialVersionUID = 1L;
		private final String id;
		private final String activityId;
		private boolean isActive;
		public ProcessInstanceStub(String id, String activityId, boolean isActive ) {
			this.id=id;
			this.activityId=activityId;
			this.isActive=isActive;
		}
		@Override
		public String getId() {
			return id;
		}
		@Override
		public String getActivityId() {
			return activityId;
		}	
		@Override
		public boolean isActive(){
			return isActive;
		}
	}
	
	@BeforeClass
	public void setUp(){
		ProcessInstance runnerInstance = new ProcessInstanceStub(RUNNER_PID,"dpmtPauseWait", true);
		when(runtimeServiceProcessMock.startProcessInstanceByKey(anyString(), anyMap() )).thenReturn(runnerInstance);
				
		ProcessInstanceQuery processInstanceQuery = mock(ProcessInstanceQuery.class);
		ProcessInstanceQuery processInstanceQuery2 = mock(ProcessInstanceQuery.class);
		when(processInstanceQuery2.singleResult()).thenReturn(runnerInstance);
		when(processInstanceQuery.processInstanceId(PROCESS_START_KEY)).thenReturn(processInstanceQuery2);
		when(processInstanceQuery.processInstanceId(anyString())).thenReturn(processInstanceQuery2);

		when(runtimeServiceProcessMock.createProcessInstanceQuery()).thenReturn(processInstanceQuery);		
		//>	
		ExecutionQuery eq1 = mock(ExecutionQuery.class);
		ExecutionQuery eq2 = mock(ExecutionQuery.class);
		ExecutionQuery eq3 = mock(ExecutionQuery.class);
		ExecutionQuery eq4 = mock(ExecutionQuery.class);		
		
		when(eq1.processInstanceId(anyString())).thenReturn(eq3);
		when(eq1.processInstanceId(anyString())).thenReturn(eq2);
		
		List<Execution> subSyncExecList= new ArrayList<Execution>();
		when(eq3.list()).thenReturn(subSyncExecList);
		when(eq3.activityId(anyString())).thenReturn(eq4);
	
		List<Execution> subExecutions = new ArrayList<Execution>();
		Execution execution1 = mock(Execution.class);
		when(execution1.getId()).thenReturn(EXECUTION_ID);
		subExecutions.add(execution1);
		subExecutions.add(execution1);

		when(eq2.singleResult()).thenReturn(execution1);
		when(eq2.list()).thenReturn(subExecutions);
		when(eq2.activityId(anyString())).thenReturn(eq3);
		
		when(runtimeServiceExecutionMock.createExecutionQuery()).thenReturn(eq1);
		//>when(runtimeService.createExecutionQuery().processInstanceId(processId).activityId("pwo").list();
		
		ProcessInstance startedProcess = mock(ProcessInstance.class);
		when(startedProcess.getId()).thenReturn(NEW_PID);
		
		when(runtimeServiceExecutionMock.startProcessInstanceByKey(anyString(),anyMap())).thenReturn(startedProcess);
	}
	
	@Test (priority=1)
	/** start deployment using runtimeservice, no pre-existing process*/
	public void startDpmtProcessTest(){
		Map<String, Object> params = new HashMap<String, Object>();
		CmsDeployment theDeployment = new CmsDeployment();
		theDeployment.setProcessId(null);
		params.put("dpmt", theDeployment);
		wfc.setRuntimeService(runtimeServiceProcessMock);
		DeploymentNotifier notifier = new DeploymentNotifier();
		ReliableExecutor<NotificationMessage> antennaClient = mock(ReliableExecutor.class );

		notifier.setAntennaClient(antennaClient);

		wfc.setNotifier(notifier);
		String pid =wfc.startDpmtProcess(PROCESS_START_KEY, params); //start *new* process since pid is null
		assertEquals(pid, RUNNER_PID);
	}
	
	@Test (priority=2)
	/** start deployment params do have pre-existing process*/
	public void startDpmtProcessAgainTest(){
		
		Map<String, Object> params = new HashMap<String, Object>();
		CmsDeployment theDeployment = new CmsDeployment();
		theDeployment.setProcessId(RUNNER_PID);
		params.put("dpmt", theDeployment);
		
		wfc.setRuntimeService(runtimeServiceProcessMock);

		String pid =wfc.startDpmtProcess(PROCESS_START_KEY, params); //start *new* process since pid is null
		assertEquals(pid, RUNNER_PID);//was waiting but resumed
	}


	@Test
	public void pokeTest(){
		wfc.setRuntimeService(runtimeServiceExecutionMock);
		wfc.pokeProcess(RUNNER_PID); 
		//does nothing but log; is void method
	}

	
	@Test
	public void pokeSubprocessTest(){	
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("k", "v");
		wfc.setRuntimeService(runtimeServiceExecutionMock);
		wfc.pokeSubProcess(RUNNER_PID, EXECUTION_ID, params); 
		//does nothing but log; is void method
	}

	@Test
	/** start a release; the pid should be determined by what we pass in*/
	public void startReleaseProcessTest(){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("k", "v");
		
		wfc.setRuntimeService(runtimeServiceExecutionMock);
		String pid = wfc.startReleaseProcess(RUNNER_PID, params);
		assertEquals(pid,NEW_PID);		
 	}
	@Test
	/** start a OpsProcess and check pid as expected */
	public void startOpsProcessTest(){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("k", "v");	
		wfc.setRuntimeService(runtimeServiceExecutionMock);

		String pid = wfc.startOpsProcess(RUNNER_PID, params); 
		assertEquals(pid,NEW_PID);
	}
	
}
