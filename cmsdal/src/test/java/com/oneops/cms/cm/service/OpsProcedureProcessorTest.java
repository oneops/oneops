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

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.ops.dal.OpsMapper;
import com.oneops.cms.cm.ops.domain.CmsOpsAction;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.cm.ops.service.OpsProcedureProcessor;
import com.oneops.cms.exceptions.OpsException;
import com.oneops.cms.util.CmsError;

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
}
