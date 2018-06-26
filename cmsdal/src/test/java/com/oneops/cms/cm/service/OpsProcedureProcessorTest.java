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
package com.oneops.cms.cm.service;

import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.ops.dal.OpsMapper;
import com.oneops.cms.cm.ops.domain.CmsOpsAction;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsFlowAction;
import com.oneops.cms.cm.ops.domain.OpsProcedureDefinition;
import com.oneops.cms.cm.ops.domain.OpsProcedureFlow;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.cm.ops.service.OpsProcedureProcessor;
import com.oneops.cms.exceptions.OpsException;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class OpsProcedureProcessorTest {
	private static final Long BLOCKING_PROC_ID = 333333L;
	private final OpsProcedureProcessor procProcessor = new OpsProcedureProcessor();
	private final OpsMapper opsMapper=mock(OpsMapper.class);
	private final CmsCmProcessor cmProcessor = mock(CmsCmProcessor.class);
	private CmsCI mockCi = new CmsCI();
	private List<CmsOpsProcedure> blockingList = new ArrayList<CmsOpsProcedure>();
	
	private static final int CI_WITH_ACTIVE_PROC = 13001;
	private static final int CI_WITH_NO_ACTIVE_PROC = 3000;
	private static final int CI_WITH_ACTIVE_ACTION = 3030;
	private static final String NS_FOR_WHICH_ACTIVE_DEPLOYMENT = "/xxxxxxx/y/zzz/bom/aaaaaa/bbbbbb/ccccc";
	
	@BeforeTest
	public void init(){
		///set up the mock of OpsMapper and CmsCmProcessor
		when(opsMapper.getProcedureForCi( CI_WITH_ACTIVE_PROC ,null,null,null)).thenReturn(blockingList);
		when(opsMapper.getProcedureForCi( CI_WITH_NO_ACTIVE_PROC,null,null,null)).thenReturn(null);
		when(opsMapper.isActiveDeploymentExistForNsPath(NS_FOR_WHICH_ACTIVE_DEPLOYMENT)).thenReturn(true);
		
		when(opsMapper.getProcedureForCiByAction(CI_WITH_ACTIVE_ACTION, null, null, null)).thenReturn(blockingList);
		
		procProcessor.setOpsMapper(opsMapper);
		
		mockCi.setNsPath(NS_FOR_WHICH_ACTIVE_DEPLOYMENT);
		
		when(cmProcessor.getCiByIdNaked(anyLong())).thenReturn(mockCi);
		procProcessor.setCmProcessor(cmProcessor);

		CmsOpsProcedure opsProcThatBlocks=new CmsOpsProcedure();
		opsProcThatBlocks.setCiId(BLOCKING_PROC_ID);
		opsProcThatBlocks.setProcedureCiId(BLOCKING_PROC_ID+1);
		opsProcThatBlocks.setProcedureName("mockProcedureX");
		opsProcThatBlocks.setProcedureState(OpsProcedureState.active);
		blockingList.add(opsProcThatBlocks);
	}

	@Test(expectedExceptions=OpsException.class)
	/** exception should happen when no actions in the proc */
	public void opsProcedureEmptyOfActionTest() throws Exception{
		CmsOpsProcedure procWithNoAction = new CmsOpsProcedure();
		procWithNoAction.setActions(new ArrayList<CmsOpsAction>());
		try {
			this.procProcessor.createCmsOpsProcedure(procWithNoAction);
		} catch (OpsException e) {
			assertEquals(e.getErrorCode() , CmsError.OPS_ONE_ACTION_MUST_BE_ERROR,
					"opsProcedureEmptyOfActionTest surprised by "+e.getErrorCode());
			System.out.println("OpsProcedureProcessorTest --- exception caught - may be as expected -" + e);
			throw e;
		}		
	}
	
	

	@Test(expectedExceptions=OpsException.class)
	/** exception should happen cause Ops mapper thinks one has active proc running already */
	public void opsProcedureAlreadyActiveTest(){
		CmsOpsProcedure proceWithOneAlreadyRunning = new CmsOpsProcedure();
		proceWithOneAlreadyRunning.setCiId(CI_WITH_ACTIVE_PROC);
		//need at least 1 action , put anything here for this test
		ArrayList<CmsOpsAction> actionList = new ArrayList<CmsOpsAction>(1);
		CmsOpsAction act1 = new CmsOpsAction();
		actionList.add(act1);
		proceWithOneAlreadyRunning.setActions(actionList);
		try {
			this.procProcessor.createCmsOpsProcedure(proceWithOneAlreadyRunning);
		} catch (OpsException e) {
			System.out.println("1 opsProcedureAlreadyActiveTest **errorCode**"+ e.getErrorCode() + ">>details="+e.getExceptionDetails());
			assertEquals(e.getErrorCode() , CmsError.OPS_ALREADY_HAVE_ACTIVE_PROCEDURE_ERROR,
					"1 opsProcedureAlreadyActiveTest wrong ***"+ e.getErrorCode());
			assertEquals(e.getBlockingProcedureId() , BLOCKING_PROC_ID , " this is not what I expect is blocker!");
			assertTrue(e.getExceptionDetails().size()>0);
			System.out.println("OpsProcedureProcessorTest --- exception caught - may be as expected -" + e);

			throw e;
		}		
	}
	

	@Test(expectedExceptions=OpsException.class)
	/** exception should happen cause Ops mapper thinks one of the actions is already active
	 * seeking OPS_ALREADY_HAVE_ACTIVE_ACTION_ERROR = 4008; */
	public void opsProcedureActionActiveTest(){
		CmsOpsProcedure opsProc = new CmsOpsProcedure();
		opsProc.setCiId(CI_WITH_ACTIVE_ACTION);
		ArrayList<CmsOpsAction> actionList = new ArrayList<CmsOpsAction>(1);
		CmsOpsAction act1 = new CmsOpsAction();
		act1.setCiId(BLOCKING_PROC_ID);
		actionList.add(act1);
		opsProc.setActions(actionList);
		try {
			this.procProcessor.createCmsOpsProcedure(opsProc);
		} catch (OpsException e) {
			System.out.println("2 opsProcedureActionActiveTest **errorCode**"+ e.getErrorCode()+ ">>details="+e.getExceptionDetails());

			assertEquals(e.getErrorCode(),CmsError.OPS_ALREADY_HAVE_ACTIVE_ACTION_ERROR,
					"2 opsProcedureActionActiveTest wrong "+ e.getErrorCode());
			assertEquals(e.getBlockingProcedureId() , BLOCKING_PROC_ID , " this is not what I expect is blocker!");
			assertTrue(e.getExceptionDetails().size()>0);
			System.out.println("OpsProcedureProcessorTest --- exception caught - may be as expected -" + e);

			throw e;
		}			
	}

	@Test
	public void processProcedureRequestTest() {
		procProcessor.setGslbMigrationEnabled(true);
		CmsOpsProcedure procedure = new CmsOpsProcedure();
		procedure.setCiId(300);
		procedure.setArglist("{\"migrate\":\"true\"}");
		procedure.setProcedureName("gslb-migration");
		procedure.setProcedureState(OpsProcedureState.pending);
		procedure.setProcedureCiId(200);

		OpsProcedureDefinition procDefn = new OpsProcedureDefinition();
		procDefn.setName("gslb-migration");
		OpsProcedureFlow flow = new OpsProcedureFlow();
		flow.setExecStrategy("one-by-one");
		flow.setRelationName("manifest.Entrypoint");
		flow.setDirection("from");
		flow.setTargetClassName("manifest.oneops.1.Fqdn");
		procDefn.setFlow(Collections.singletonList(flow));

		OpsProcedureFlow subFlow = new OpsProcedureFlow();
		flow.setFlow(Collections.singletonList(subFlow));
		subFlow.setExecStrategy("one-for-all");
		subFlow.setRelationName("base.RealizedAs");
		subFlow.setDirection("from");
		subFlow.setTargetClassName("bom.oneops.1.Fqdn");

		OpsFlowAction action = new OpsFlowAction();
		action.setActionName("migrate");
		action.setStepNumber(1);
		subFlow.setActions(Collections.singletonList(action));
		List<CmsCIRelation> entryPointRelations = new ArrayList<>();
		CmsCIRelation entryPoint = new CmsCIRelation();
		entryPoint.setToCiId(400);
		entryPointRelations.add(entryPoint);
		when(cmProcessor.getFromCIRelationsNaked(300, "manifest.Entrypoint", null, "manifest.oneops.1.Fqdn")).thenReturn(entryPointRelations);

		List<CmsCIRelation> realizedAsRelations = new ArrayList<>();
		CmsCIRelation realizedAs1 = new CmsCIRelation();
		realizedAs1.setToCiId(500);
		CmsCIRelation realizedAs2 = new CmsCIRelation();
		realizedAs2.setToCiId(600);
		realizedAsRelations.add(realizedAs2);
		when(cmProcessor.getFromCIRelationsNaked(400, "base.RealizedAs", null, "bom.oneops.1.Fqdn")).thenReturn(realizedAsRelations);
		procProcessor.setOpsMapper(new MockOpsMapper());

		CmsOpsProcedure opsProcedure = procProcessor.processProcedureRequest(procedure, procDefn);
		assertNotNull(opsProcedure);
		assertTrue(opsProcedure.getCiId() == 300);
		assertTrue("gslb-migration".equals(opsProcedure.getProcedureName()));
		assertTrue("{\"migrate\":\"true\"}".equals(opsProcedure.getArglist()));
		assertTrue(opsProcedure.getActions().size() == 1);
		CmsOpsAction opsAction = opsProcedure.getActions().get(0);
		assertTrue("{\"migrate\":\"true\"}".equals(opsAction.getArglist()));
		assertTrue(CmsConstants.PLATFORM_COMMON_ACTION.equals(opsAction.getExtraInfo()));
		assertTrue("migrate".equals(opsAction.getActionName()));
	}

}
