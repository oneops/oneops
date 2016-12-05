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
package com.oneops.cms.cm.ops.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.cm.ops.domain.CmsOpsAction;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;

/**
 * The Class OpsManagerImpl.
 */
public class OpsManagerImpl implements OpsManager {
	
	private OpsProcedureProcessor procProcessor;

    /**
     * Sets the proc processor.
     *
     * @param procProcessor the new proc processor
     */
    public void setProcProcessor(OpsProcedureProcessor procProcessor) {
        this.procProcessor = procProcessor;
    }

    /**
     * Creates the cms ops procedure.
     *
     * @param proc the proc
     * @return the cms ops procedure
     */
    @Override
    public CmsOpsProcedure createCmsOpsProcedure(CmsOpsProcedure proc) {
        return procProcessor.createCmsOpsProcedure(proc);
    }

    /**
     * Gets the cms ops procedure.
     *
     * @param procedureId the procedure id
     * @param definitionIncludes the definition includes
     * @return the cms ops procedure
     */
    @Override
    public CmsOpsProcedure getCmsOpsProcedure(long procedureId, boolean definitionIncludes) {
        return procProcessor.getCmsOpsProcedure(procedureId, definitionIncludes);
    }

    /**
     * Update ops procedure.
     *
     * @param proc the proc
     * @return the cms ops procedure
     */
    @Override
    public CmsOpsProcedure updateOpsProcedure(CmsOpsProcedure proc) {
        return procProcessor.updateOpsProcedure(proc);
    }

    /**
     * Update ops action.
     *
     * @param action the action
     * @return the cms ops action
     */
    @Override
    public CmsOpsAction updateOpsAction(CmsOpsAction action) {
        return procProcessor.updateOpsAction(action);
    }

    /**
     * Gets the action orders.
     *
     * @param procedureId the procedure id
     * @param state the state
     * @param execOrder the exec order
     * @return the action orders
     */
//    @Override
//    public List<CmsActionOrder> getActionOrders(long procedureId, OpsProcedureState state, Integer execOrder) {
//        return woProvider.getActionOrders(procedureId, state, execOrder);
//    }

    /**
     * Complete action order.
     *
     * @param ao the ao
     */
    @Override
    public void completeActionOrder(CmsActionOrder ao) {
        procProcessor.completeActionOrder(ao);
    }

	/**
	 * Gets the cms ops procedure for ci.
	 *
	 * @param ciId the ci id
	 * @param stateList the list of states
	 * @param procedureName the procedure name
	 * @return the cms ops procedure for ci
	 */
	@Override
	public List<CmsOpsProcedure> getCmsOpsProcedureForCi(long ciId, List<OpsProcedureState> stateList, String procedureName, Integer limit) {
		return procProcessor.getCmsOpsProceduresForCi(ciId, stateList, procedureName, limit);
	}

	/**
	 * Gets the cms ops procedure for ci by proc action.
	 *
	 * @param ciId the ci id
	 * @param stateList the list of states
	 * @param procedureName the procedure name
	 * @return the cms ops procedure for ci
	 */
	@Override
	public List<CmsOpsProcedure> getCmsOpsProcedureForCiByAction(long ciId,
																 List<OpsProcedureState> stateList, String procedureName, Integer limit) {
		// TODO Auto-generated method stub
		return procProcessor.getCmsOpsProceduresForCiByAction(ciId, stateList, procedureName, limit);
	}

    /**
     * Gets the cms ops procedure for namespace.
     *
     * @param nsPath the ns path
     * @param stateList the list of states
     * @param procedureName the procedure name
     * @return the cms ops procedure for namespace
     */
    @Override
    public List<CmsOpsProcedure> getCmsOpsProcedureForNamespace(String nsPath, List<OpsProcedureState> stateList, String procedureName) {
        return procProcessor.getCmsOpsProceduresForNamespace(nsPath, stateList, procedureName);
    }

    /**
     * Submit procedure.
     *
     * @param proc the proc
     * @return the cms ops procedure
     */
    @Override
	public CmsOpsProcedure submitProcedure(CmsOpsProcedure proc) {
		return procProcessor.processProcedureRequest(proc);
	}

	/**
	 * Submit simple action.
	 *
	 * @param user the user
	 * @param anchorCiId the anchor ci id
	 * @param action the action
	 * @param argList the arg list
	 * @return the cms ops procedure
	 */
	@Override
	public CmsOpsProcedure submitSimpleAction(String user, long anchorCiId,
			String action, Map<String, String> argList) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Update procedure state.
	 *
	 * @param procId the proc id
	 * @param state the state
	 * @return the cms ops procedure
	 */
	@Override
	public CmsOpsProcedure updateProcedureState(long procId, OpsProcedureState state) {
		return procProcessor.updateProcedureState(procId, state);
	}

	/**
	 * Retry ops procedure.
	 *
	 * @param procId the proc id
	 * @return the cms ops procedure
	 */
	@Override
	public CmsOpsProcedure retryOpsProcedure(long procId) {
		return procProcessor.retryProcedure(procId);
	}

	@Override
	public List<CmsOpsProcedure> getCmsOpsProcedureForNamespaceLike(String nsPath, List<OpsProcedureState> stateList, String procedureName, Integer limit,Boolean actions){
		
		return procProcessor.getCmsOpsProcedureForNamespaceLike(nsPath, stateList, procedureName,limit,actions);		
	}

	@Override
	public long  getCmsOpsProceduresCountForCiFromTime(long ciId, List<OpsProcedureState> stateList,
			String procedureName, Date timestamp) {
		return procProcessor.getCmsOpsProceduresCountForCiFromTime(ciId, stateList, procedureName, timestamp);
	}	

}
