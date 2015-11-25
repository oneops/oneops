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
	 * @param state the state
	 * @param procedureName the procedure name
	 * @return the cms ops procedure for ci
	 */
	@Override
	public List<CmsOpsProcedure> getCmsOpsProcedureForCi(long ciId, OpsProcedureState state, String procedureName, Integer limit) {
		return procProcessor.getCmsOpsProceduresForCi(ciId, state, procedureName, limit);
	}

	/**
	 * Gets the cms ops procedure for ci by proc action.
	 *
	 * @param ciId the ci id
	 * @param state the state
	 * @param procedureName the procedure name
	 * @return the cms ops procedure for ci
	 */
	@Override
	public List<CmsOpsProcedure> getCmsOpsProcedureForCiByAction(long ciId,
			OpsProcedureState state, String procedureName, Integer limit) {
		// TODO Auto-generated method stub
		return procProcessor.getCmsOpsProceduresForCiByAction(ciId, state, procedureName, limit);
	}

    /**
     * Gets the cms ops procedure for namespace.
     *
     * @param nsPath the ns path
     * @param state the state
     * @param procedureName the procedure name
     * @return the cms ops procedure for namespace
     */
    @Override
    public List<CmsOpsProcedure> getCmsOpsProcedureForNamespace(String nsPath, OpsProcedureState state, String procedureName) {
        return procProcessor.getCmsOpsProceduresForNamespace(nsPath, state, procedureName);
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
	public List<CmsOpsProcedure> getCmsOpsProcedureForNamespaceLike(String nsPath,
			OpsProcedureState state, String procedureName, Integer limit,Boolean actions){
		
		return procProcessor.getCmsOpsProcedureForNamespaceLike(nsPath, state, procedureName,limit,actions);		
	}

	@Override
	public List<CmsOpsProcedure> getCmsOpsProceduresForCiFromTime(long ciId,
			String procedureName, Date timestamp) {
		return procProcessor.getCmsOpsProceduresForCiFromTime(ciId, procedureName, timestamp);
	}	

}
