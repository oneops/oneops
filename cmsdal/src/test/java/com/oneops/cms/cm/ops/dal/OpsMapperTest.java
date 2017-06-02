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
package com.oneops.cms.cm.ops.dal;

import static org.junit.Assert.*;
import static java.util.Collections.singletonList;

import com.oneops.cms.cm.ops.domain.OpsActionState;
import com.oneops.cms.cm.ops.domain.*;
import org.junit.*;

import java.io.Reader;

import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;

/**
 * The Class OpsMapperTest.
 */
public class OpsMapperTest {

	private static OpsMapper opsMapper; 
	private static SqlSession session; 

    private long procedureId = 1;
    private long ciId = 1001;
    private String nsPath = "/public";
    private String procedureName = "test 1";

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
		System.out.println("Starting up OpsMapper tests");

		String resource = "mybatis-config.xml";
		Reader reader = Resources.getResourceAsReader(resource);
		SqlSessionFactory sf = new SqlSessionFactoryBuilder().build(reader);
        session = sf.openSession();
		opsMapper = session.getMapper(OpsMapper.class);

	}

	/**
	 * Tear down.
	 *
	 * @throws Exception the exception
	 */
	@AfterClass 
	public static void tearDown() throws Exception {
		session.rollback();
		session.close();
		System.out.println("Closing down OpsMapper tests");
	} 


    /**
     * Creates the and get cms ops procedure test.
     *
     * @throws Exception the exception
     */
    @Test
	public void createAndGetCmsOpsProcedureTest() throws Exception{
        String createdBy = "admin";
        String definition = "definition test";
        long procCiId = 12345;
		CmsOpsProcedure newProc = new CmsOpsProcedure();
		newProc.setProcedureId(procedureId);
        newProc.setCiId(ciId);
		newProc.setProcedureName(procedureName);
        newProc.setProcedureState(OpsProcedureState.active);
        newProc.setCreatedBy(createdBy);
        newProc.setDefinition(definition);
        newProc.setProcedureCiId(procCiId);

		opsMapper.createCmsOpsProcedure(newProc);

        CmsOpsProcedure proc = opsMapper.getCmsOpsProcedure(procedureId);
		Assert.assertNotNull("Created ops procedure is null", proc);
        Assert.assertEquals(proc.getProcedureId(), procedureId);
        Assert.assertEquals(proc.getCiId(), ciId);
		Assert.assertEquals(proc.getProcedureName(), procedureName);
        Assert.assertEquals(proc.getProcedureState(), OpsProcedureState.active);
        Assert.assertEquals(proc.getCreatedBy(), createdBy);
        Assert.assertNull(proc.getDefinition());
        Assert.assertEquals(proc.getProcedureCiId(), procCiId);
        Assert.assertNotNull("Created date ops procedure is null", proc.getCreated());

        proc = opsMapper.getCmsOpsProcedureWithDefinition(procedureId);
		Assert.assertNotNull("Created ops procedure is null", proc);
        Assert.assertEquals(proc.getProcedureId(), procedureId);
        Assert.assertEquals(proc.getCiId(), ciId);
		Assert.assertEquals(proc.getProcedureName(), procedureName);
        Assert.assertEquals(proc.getProcedureState(), OpsProcedureState.active);
        Assert.assertEquals(proc.getCreatedBy(), createdBy);
        Assert.assertEquals(proc.getDefinition(), definition);
        Assert.assertEquals(proc.getProcedureCiId(), procCiId);
        Assert.assertNotNull("Created date ops procedure is null", proc.getCreated());

	}


    /**
     * Creates the and get cms ops action test.
     *
     * @throws Exception the exception
     */
    @Test
    public void createAndGetCmsOpsActionTest() throws Exception {
        String actionName = "action 1";
        CmsOpsAction newAction = new CmsOpsAction();
        newAction.setActionName(actionName);
        newAction.setProcedureId(procedureId);
        newAction.setActionState(OpsActionState.pending);
        newAction.setCiId(ciId);
        newAction.setExecOrder(1);

        opsMapper.createCmsOpsAction(newAction);
        List<CmsOpsAction> actions = opsMapper.getCmsOpsActions(procedureId);
        Assert.assertNotNull("Retrieved action list is null", actions);
        Assert.assertEquals(actions.size(), 1);
        CmsOpsAction action = actions.get(0);
        Assert.assertNotNull("Retrieved action list null", action);
        Assert.assertEquals(action.getActionName(), actionName);
        Assert.assertEquals(action.getActionState(), OpsActionState.pending);
        Assert.assertEquals(action.getProcedureId(), procedureId);
        Assert.assertEquals(action.getExecOrder(), 1);
	}


    /**
     * Update cms ops procedure state test.
     *
     * @throws Exception the exception
     */
    @Test
    public void updateCmsOpsProcedureStateTest() throws Exception  {
        CmsOpsProcedure proc = opsMapper.getCmsOpsProcedure(procedureId);
        Assert.assertEquals(proc.getProcedureState(), OpsProcedureState.active);
        opsMapper.updateCmsOpsProcedureState(procedureId, OpsProcedureState.complete);
        proc = opsMapper.getCmsOpsProcedure(procedureId);
        Assert.assertEquals(proc.getProcedureState(), OpsProcedureState.complete);
    }

    /**
     * Update cms ops action state test.
     *
     * @throws Exception the exception
     */
    @Test
    public void updateCmsOpsActionStateTest() throws Exception  {
        List<CmsOpsAction> actions = opsMapper.getCmsOpsActions(procedureId);
        CmsOpsAction action = actions.get(0);
        Assert.assertEquals(action.getActionState(), OpsActionState.pending);
        long actionId = action.getActionId();
        opsMapper.updateCmsOpsActionState(actionId, OpsActionState.complete);
        actions = opsMapper.getCmsOpsActions(procedureId);
        action = actions.get(0);
        Assert.assertEquals(action.getActionState(), OpsActionState.complete);
	}

    /**
     * Checks if is active ops procedure exist for ci test.
     *
     * @throws Exception the exception
     */
    @Test
    public void isActiveOpsProcedureExistForCiTest() throws Exception  {
        opsMapper.updateCmsOpsProcedureState(procedureId, OpsProcedureState.active);
        assertEquals(opsMapper.isActiveOpsProcedureExistForCi(ciId),true);
        assertEquals(opsMapper.isActiveOpsProcedureExistForCi(2287), false);
    }

	/**
	 * Checks if is opened release exist for ci test.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void isOpenedReleaseExistForCiTest() throws Exception  {
	    assertEquals(opsMapper.isOpenedReleaseExistForCi(11635),true);
		assertEquals(opsMapper.isOpenedReleaseExistForCi(11570),false);
	}

    /**
     * Gets the procedure for ci test.
     *
     * @return the procedure for ci test
     * @throws Exception the exception
     */
    @Test
    public void getProcedureForCiTest() throws Exception  {
        opsMapper.updateCmsOpsProcedureState(procedureId, OpsProcedureState.active);
        List<CmsOpsProcedure> procs = opsMapper.getProcedureForCi(ciId, singletonList(OpsProcedureState.active), procedureName, null);
        Assert.assertNotNull("Retrieved procedure list is null", procs);
        Assert.assertEquals(procs.size(), 1);
        CmsOpsProcedure proc = procs.get(0);
        Assert.assertNotNull("Retrieved procedure is null", proc);

        procs = opsMapper.getProcedureForCi(ciId, null, null, null);
        Assert.assertNotNull("Retrieved procedure list is null", procs);
        Assert.assertEquals(procs.size(), 1);
        proc = procs.get(0);
        Assert.assertNotNull("Retrieved procedure is null", proc);
    }

    /**
     * Gets the procedure for namespace test.
     *
     * @return the procedure for namespace test
     * @throws Exception the exception
     */
    @Test
    public void getProcedureForNamespaceTest() throws Exception  {
        opsMapper.updateCmsOpsProcedureState(procedureId, OpsProcedureState.active);
        List<CmsOpsProcedure> procs = opsMapper.getProcedureForNamespace(nsPath, singletonList(OpsProcedureState.active), procedureName);
        Assert.assertNotNull("Retrieved procedure list is null", procs);
        Assert.assertEquals(procs.size(), 1);
        CmsOpsProcedure proc = procs.get(0);
        Assert.assertNotNull("Retrieved procedure is null", proc);

        procs = opsMapper.getProcedureForNamespace(nsPath, null, null);
        Assert.assertNotNull("Retrieved procedure list is null", procs);
        Assert.assertEquals(procs.size(), 1);
        proc = procs.get(0);
        Assert.assertNotNull("Retrieved procedure is null", proc);
    }

    /**
     * Gets the procedure for namespace test.
     *
     * @return the procedure for namespace test
     * @throws Exception the exception
     */
    @Test
    public void getProcedureForNamespaceLikeTest() throws Exception  {
        opsMapper.updateCmsOpsProcedureState(procedureId, OpsProcedureState.active);
        List<CmsOpsProcedure> procs = opsMapper.getProcedureForNamespaceLike(nsPath, nsPath, singletonList(OpsProcedureState.active), procedureName,10);
        Assert.assertNotNull("Retrieved procedure list is null", procs);
        CmsOpsProcedure proc = procs.get(0);
        Assert.assertNotNull("Retrieved procedure is null", proc);
        Assert.assertNotNull("Retrieved procedure list is null", procs);
    }
    /**
     * Gets the action orders test.
     *
     * @return the action orders test
     * @throws Exception the exception
     */
    @Ignore("Not worked yet")
    @Test
    public void getActionOrdersTest() throws Exception  {
        List<CmsActionOrder> orders = opsMapper.getActionOrders(procedureId, OpsProcedureState.active,1);
        Assert.assertNotNull("Retrieved action order list is null", orders);
        Assert.assertEquals(orders.size(), 1);

    }

}
