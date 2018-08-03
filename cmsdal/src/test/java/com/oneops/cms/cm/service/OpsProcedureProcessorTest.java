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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
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
import org.mockito.ArgumentMatcher;
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
		when(cmProcessor.getVarByMatchingCriteriaBoolean(any(), any())).thenReturn(true);
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
		when(cmProcessor.getCiById(300)).thenReturn(new CmsCI());


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

	@Test
	public void testFqdnMigratePlatformProcedure() {
		procProcessor.setGslbMigrationEnabled(true);
		OpsMapper opsMapper = mock(OpsMapper.class);
		procProcessor.setOpsMapper(opsMapper);
		when(cmProcessor.getVarByMatchingCriteriaBoolean(any(), any())).thenReturn(true);
		CmsOpsProcedure procedure = new CmsOpsProcedure();
		procedure.setCiId(300);
		procedure.setArglist("{\"migrate\":\"torbit\"}");
		procedure.setProcedureName("gslb-migration");
		procedure.setProcedureState(OpsProcedureState.complete);
		procedure.setProcedureId(1600);

		List<CmsOpsAction> actions = new ArrayList<>();
		CmsOpsAction action1 = new CmsOpsAction();
		action1.setCiId(1005);
		action1.setActionName("migrate");
		action1.setProcedureId(1000);
		actions.add(action1);

		CmsOpsAction action2 = new CmsOpsAction();
		action2.setCiId(1010);
		action2.setActionName("migrate");
		action2.setProcedureId(1000);
		actions.add(action2);

		when(opsMapper.getCmsOpsActions(1600)).thenReturn(actions);

		CmsCI ci = new CmsCI();
		ci.setCiId(1005);
		ci.setCiClassName("bom.Fqdn");
		CmsCIAttribute attr = new CmsCIAttribute();
		attr.setAttributeName("service_type");
		attr.setDfValue("torbit");
		attr.setDjValue("torbit");
		ci.addAttribute(attr);
		when(cmProcessor.getCiById(1005)).thenReturn(ci);
		when(cmProcessor.getCiById(300)).thenReturn(new CmsCI());

		List<CmsCIRelation> realizedAsRelations = new ArrayList<>();
		CmsCIRelation realizedAs1 = new CmsCIRelation();
		realizedAs1.setToCiId(500);
		CmsCI manifestCi = new CmsCI();
		manifestCi.setCiId(500);
		manifestCi.setCiClassName("manifest.Fqdn");
		CmsCIAttribute attr1 = new CmsCIAttribute();
		attr1.setAttributeName("service_type");
		attr1.setDfValue("netscaler");
		attr1.setDjValue("netscaler");
		manifestCi.addAttribute(attr1);
		realizedAs1.setFromCi(manifestCi);
		realizedAsRelations.add(realizedAs1);
		when(cmProcessor.getToCIRelations(1005, "base.RealizedAs", null, null)).thenReturn(realizedAsRelations);

		procProcessor.updateOpsProcedure(procedure);
		verify(cmProcessor).updateCI(argThat(new CiArgumentMatcher<>(manifestCi)));
	}

	@Test
	public void shouldNotExecuteCustomUpdateForOtherProcs() {
		procProcessor.setGslbMigrationEnabled(true);
		OpsMapper opsMapper = mock(OpsMapper.class);
		procProcessor.setOpsMapper(opsMapper);
		CmsOpsProcedure procedure = new CmsOpsProcedure();
		procedure.setCiId(300);
		procedure.setProcedureName("restart");
		procedure.setProcedureState(OpsProcedureState.complete);
		procedure.setProcedureId(1500);

		List<CmsOpsAction> actions = new ArrayList<>();
		CmsOpsAction action1 = new CmsOpsAction();
		action1.setCiId(1005);
		action1.setActionName("migrate");
		action1.setProcedureId(1000);
		actions.add(action1);

		when(opsMapper.getCmsOpsActions(1500)).thenReturn(actions);
		mockCi();
		procProcessor.updateOpsProcedure(procedure);
		verify(cmProcessor, never()).updateCI(any());
	}

	private void mockCi() {
		CmsCI ci = new CmsCI();
		ci.setCiId(1005);
		ci.setCiClassName("bom.Fqdn");
		CmsCIAttribute attr = new CmsCIAttribute();
		attr.setAttributeName("service_type");
		attr.setDfValue("torbit");
		attr.setDjValue("torbit");
		ci.addAttribute(attr);
		when(cmProcessor.getCiById(1005)).thenReturn(ci);

		List<CmsCIRelation> realizedAsRelations = new ArrayList<>();
		CmsCIRelation realizedAs1 = new CmsCIRelation();
		realizedAs1.setToCiId(500);
		CmsCI manifestCi = new CmsCI();
		manifestCi.setCiId(500);
		manifestCi.setCiClassName("manifest.Fqdn");
		CmsCIAttribute attr1 = new CmsCIAttribute();
		attr1.setAttributeName("service_type");
		attr1.setDfValue("netscaler");
		attr1.setDjValue("netscaler");
		manifestCi.addAttribute(attr1);
		realizedAs1.setFromCi(manifestCi);
		realizedAsRelations.add(realizedAs1);
		when(cmProcessor.getToCIRelations(1005, "base.RealizedAs", null, null)).thenReturn(realizedAsRelations);
	}

	@Test
	public void shouldNotExecuteCustomIfArgMissing() {
		procProcessor.setGslbMigrationEnabled(true);
		OpsMapper opsMapper = mock(OpsMapper.class);
		procProcessor.setOpsMapper(opsMapper);
		CmsOpsProcedure procedure = new CmsOpsProcedure();
		procedure.setCiId(300);
		procedure.setArglist(null);
		procedure.setProcedureName("gslb-migration");
		procedure.setProcedureState(OpsProcedureState.complete);
		procedure.setProcedureId(1500);

		List<CmsOpsAction> actions = new ArrayList<>();
		CmsOpsAction action1 = new CmsOpsAction();
		action1.setCiId(1005);
		action1.setActionName("migrate");
		action1.setProcedureId(1000);
		actions.add(action1);

		CmsOpsAction action2 = new CmsOpsAction();
		action2.setCiId(1010);
		action2.setActionName("migrate");
		action2.setProcedureId(1000);
		actions.add(action2);

		when(opsMapper.getCmsOpsActions(1500)).thenReturn(actions);
		mockCi();
		procProcessor.updateOpsProcedure(procedure);
		verify(cmProcessor, never()).updateCI(any());
	}

	private class CiArgumentMatcher<T> extends ArgumentMatcher<T> {
		T thisObject;

		public CiArgumentMatcher(T thisObject) {
			this.thisObject = thisObject;
		}

		@Override
		public boolean matches(Object argument) {
			if (argument instanceof CmsCI) {
				CmsCI arg = (CmsCI) argument;
				CmsCI ci = (CmsCI) thisObject;

				CmsCIAttribute attr = ci.getAttribute("service_type");
				return ci.getCiId() == arg.getCiId() && ci.getCiClassName().equals(arg.getCiClassName())
						&&  attr != null
						&& attr.getDjValue().equals("torbit")
						&& attr.getOwner().equals("manifest");
			}
			return false;
		}
	}


}
