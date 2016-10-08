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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.ops.dal.OpsMapper;
import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.cm.ops.domain.CmsOpsAction;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsActionState;
import com.oneops.cms.cm.ops.domain.OpsFlowAction;
import com.oneops.cms.cm.ops.domain.OpsProcedureDefinition;
import com.oneops.cms.cm.ops.domain.OpsProcedureFlow;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.exceptions.OpsException;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
/**
 * The Class OpsProcedureProcessor.
 */
public class OpsProcedureProcessor {

	private static final String WITH_PROCEDURE_ACTION_NAME = " with procedure/action name ";
	private OpsMapper opsMapper;
	private CmsCmProcessor cmProcessor;
	private Gson gson = new Gson();
	private static final Logger logger = Logger.getLogger(OpsProcedureProcessor.class);
    private static final String ALREADY_HAS_ACTIVE_OPS_PROCEDURE = " already has active ops procedure: ";
	private static final String GIVEN_CI = "Given ci ";
	private static final String PROCEDURE_NAME = " procedure name: ";
	private static final String CURRENT_STATUS = " current status: ";
    private static final String ALREADY_HAS_ACTIVE_OPS_ACTION = " already has active ops action: ";
    private static final String NO_ACTION_ITH_ID = "There is no action with this id: ";
    private static final String ACTION_IS_IN_WRONG_STATE = "Action is in wrong state id:";

    
	/**
	 * Sets the ops mapper.
	 * 
	 * @param opsMapper
	 *            the new ops mapper
	 */
	public void setOpsMapper(OpsMapper opsMapper) {
		this.opsMapper = opsMapper;
	}

	/**
	 * Sets the cm processor.
	 * 
	 * @param cmProcessor
	 *            the new cm processor
	 */
	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}
	        
	/**
	 * Gets the procedure def.
	 *
	 * @param procCiId the proc ci id
	 * @return the procedure def
	 */
	public OpsProcedureDefinition getProcedureDef(long procCiId) {
		CmsCI procDefCi = cmProcessor.getCiById(procCiId);
		String procDefString = procDefCi.getAttribute("flow").getDfValue(); 
		return gson.fromJson(procDefString, OpsProcedureDefinition.class);
	}
	
	/**
	 * Gets the procedure def.
	 *
	 * @param procDefinition the proc definition
	 * @return the procedure def
	 */
	public OpsProcedureDefinition getProcedureDef(String procDefinition) {
		return gson.fromJson(procDefinition, OpsProcedureDefinition.class);
	}

	
	/**
	 * Process procedure request.
	 *
	 * @param proc the proc
	 * @return the cms ops procedure
	 */
	public CmsOpsProcedure processProcedureRequest(CmsOpsProcedure proc) {
		OpsProcedureDefinition procDef = null;
		if (proc.getProcedureCiId()>0) {
			CmsCI procCi = cmProcessor.getCiById(proc.getProcedureCiId());
			if (procCi == null) {
				throw new OpsException(CmsError.OPS_THERE_IS_NO_PROCEDURE_DEFINITION_ERROR,
                        "There is no procedure definition CI with ciId - " + proc.getProcedureCiId());
			}
			proc.setDefinition(procCi.getAttribute("definition").getDfValue());
			procDef = getProcedureDef(proc.getDefinition());
			procDef.setName(procCi.getCiName());
		} else {
			procDef = getProcedureDef(proc.getDefinition());
		}
		return processProcedureRequest(proc, procDef);
	}
	
	/**
	 * Process procedure request.
	 *
	 * @param procRequest the proc request
	 * @param procDef the proc def
	 * @return the cms ops procedure
	 */
	public CmsOpsProcedure processProcedureRequest(CmsOpsProcedure procRequest, OpsProcedureDefinition procDef) {
		List<CmsOpsAction> actions = getProcedureActions(procDef, procRequest.getCiId(), procRequest.getArglist());
		CmsOpsProcedure proc = new CmsOpsProcedure();
		proc.setProcedureName(procDef.getName());
		proc.setCiId(procRequest.getCiId());
		if (procRequest.getProcedureState() != null) {
			proc.setProcedureState(procRequest.getProcedureState());
		} else {
			proc.setProcedureState(OpsProcedureState.pending);
		}
		proc.setActions(actions);
		proc.setCreatedBy(procRequest.getCreatedBy());
		proc.setArglist(procRequest.getArglist());
		proc.setProcedureCiId(procRequest.getProcedureCiId());
		proc.setDefinition(procRequest.getDefinition());
		proc.setForceExecution(procRequest.getForceExecution());
		return createCmsOpsProcedure(proc);
	}
	
	private List<CmsOpsAction> getProcedureActions(OpsProcedureDefinition procDef, long anchorCiId, String argList) {

		List<CmsOpsAction> actions = new ArrayList<CmsOpsAction>();
		for (OpsFlowAction actionDef : procDef.getActions()) {
			int execOrder = actionDef.getStepNumber();
			actions.add(bootStrapAction(anchorCiId, actionDef, execOrder));
		}
		
		int offset = 0;
		for (OpsProcedureFlow childFlow : procDef.getFlow()) {
			actions.addAll(processBranch(anchorCiId,childFlow, offset));
			offset = getMaxExecOrder(actions);
		}
		
		for (CmsOpsAction action : actions) {
			action.setArglist(argList);
		}
		
		return actions;
	}
	
	
	
	private List<CmsOpsAction> processBranch(long anchorCiId, OpsProcedureFlow flow, int offset) {
		List<CmsCIRelation> links = null;
		Set<Long> ciIds = new HashSet<Long>();
		//process optional nspath and target class name
		if (flow.getNsPath() != null && flow.getNsPath().trim().length() > 0) {
			CmsCI anchorCi = cmProcessor.getCiById(anchorCiId);
			if (anchorCi == null) {
				throw new OpsException(CmsError.CMS_NO_CI_WITH_GIVEN_ID_ERROR,
                        "There is no CI with ciId - " + anchorCiId);
			}
			String anchorCiNsPath = anchorCi.getNsPath();
			String fullNsPath = anchorCiNsPath.endsWith("/") ? anchorCiNsPath + flow.getNsPath() : anchorCiNsPath + "/" + flow.getNsPath();
			
			List<CmsCI> cis = cmProcessor.getCiBy3Naked(fullNsPath, flow.getTargetClassName(), null);
			for (CmsCI ci : cis) {
				ciIds.add(ci.getCiId());
			}
		} else if (flow.getDirection().equalsIgnoreCase("from")) {
			if (flow.getTargetIds() != null && flow.getTargetIds().size()>0) {
				links = cmProcessor.getFromCIRelationsByToCiIdsNaked(anchorCiId, flow.getRelationName(), flow.getRelationShortName(), flow.getTargetIds());
			} else {
				links = cmProcessor.getFromCIRelationsNaked(anchorCiId, flow.getRelationName(), flow.getRelationShortName(), flow.getTargetClassName());
			}
		} else {
			if (flow.getTargetIds() != null && flow.getTargetIds().size()>0) {
				links = cmProcessor.getToCIRelationsByFromCiIdsNaked(anchorCiId, flow.getRelationName(), flow.getRelationShortName(), flow.getTargetIds());
			} else {
				links = cmProcessor.getToCIRelationsNaked(anchorCiId, flow.getRelationName(), flow.getRelationShortName(), flow.getTargetClassName());
			}
		}
		
		List<CmsOpsAction> actions = new ArrayList<CmsOpsAction>();
		if (links != null) {
			for (CmsCIRelation rel : links) {
				long ciId = 0;
				if (flow.getDirection().equalsIgnoreCase("from")) {
					ciId = rel.getToCiId();  
				} else {
					ciId = rel.getFromCiId();
				}
				ciIds.add(ciId);
			}
		}
		
		int innerLoopStep = 0;

		for (long ciId : ciIds) {
			if (flow.getFlow().size()>0) {
				for (OpsFlowAction actionDef : flow.getActions()) {
					int execOrder = actionDef.getStepNumber() + offset + innerLoopStep;
					actions.add(bootStrapAction(ciId, actionDef, execOrder));
				}

				List<CmsOpsAction> childActions = new ArrayList<CmsOpsAction>();
				for (OpsProcedureFlow childFlow : flow.getFlow()) {
					childActions.addAll(processBranch(ciId,childFlow, offset));
					if ("one-by-one".equalsIgnoreCase(flow.getExecStrategy())) {
						offset = getMaxExecOrder(childActions);
					}
				}
				actions.addAll(childActions);
					
			} else {
				for (OpsFlowAction actionDef : flow.getActions()) {
					int execOrder = actionDef.getStepNumber() + offset + innerLoopStep; 
					actions.add(bootStrapAction(ciId, actionDef, execOrder));
				}
			}
			if ("one-by-one".equalsIgnoreCase(flow.getExecStrategy())) {
				innerLoopStep++;
			}	
		}
		
		return actions;
	}
	/*
	private int getMinExecOrder(List<CmsOpsAction> actions) {
		int min = 10000;
		for (CmsOpsAction action : actions) {
			min = min > action.getExecOrder() ? action.getExecOrder() : min; 
		}
		return min;
	}
	*/
	
	private int getMaxExecOrder(List<CmsOpsAction> actions) {
		int max = 0;
		for (CmsOpsAction action : actions) {
			max = max < action.getExecOrder() ? action.getExecOrder() : max; 
		}
		return max;
	}
	
	
	private CmsOpsAction bootStrapAction(long ciId, OpsFlowAction actionDef, int execOrder) {
		CmsOpsAction action = new CmsOpsAction();
		action.setActionName(actionDef.getActionName());
		action.setCiId(ciId);
		action.setIsCritical(actionDef.getIsCritical());
		action.setExecOrder(execOrder);
		action.setActionState(OpsActionState.pending);
		action.setExtraInfo(actionDef.getExtraInfo());
		return action;
	}
	
	private OpsException createExceptionForAnchorBlock(long blockeeCiId, String blockeeName, CmsOpsProcedure blockingProcedure){
		StringBuilder sb = new StringBuilder(GIVEN_CI);///String for front end error msg
		sb.append(blockeeCiId)
		.append(WITH_PROCEDURE_ACTION_NAME)
		.append(blockeeName)
		.append(ALREADY_HAS_ACTIVE_OPS_PROCEDURE)
		.append(blockingProcedure.getCiId())
		.append(PROCEDURE_NAME)
		.append(blockingProcedure.getProcedureName())
		.append(CURRENT_STATUS)
		.append(blockingProcedure.getProcedureState().name());
				
		return new OpsException(
				CmsError.OPS_ALREADY_HAVE_ACTIVE_PROCEDURE_ERROR, sb.toString())		
				.set(OpsException.ExceptionDetailKey.BLOCKING_PROCEDURE_CI_ID.name(),///fill details Map in exception...
						String.valueOf(blockingProcedure.getCiId()))
				.set(OpsException.ExceptionDetailKey.PROCEDURE_ID.name(),
						String.valueOf(blockingProcedure.getProcedureId())) 
				.set(OpsException.ExceptionDetailKey.PROCEDURE_NAME.name(),
						String.valueOf(blockingProcedure.getProcedureName()))
				.set(OpsException.ExceptionDetailKey.PROCEDURE_STATE.name(),
						String.valueOf(blockingProcedure.getProcedureState())) 
				.set(OpsException.ExceptionDetailKey.ARG_LIST.name(),
						String.valueOf(blockingProcedure.getArglist())) ;
	}
	private OpsException createExceptionForActionBlock(long blockeeCiId, String blockeeName, CmsOpsProcedure blockingProcedure){
		StringBuilder sb = new StringBuilder(GIVEN_CI);///String for front end error msg
		sb.append(blockeeCiId)
		.append(WITH_PROCEDURE_ACTION_NAME)
		.append(blockeeName)
		.append(ALREADY_HAS_ACTIVE_OPS_ACTION)
		.append(blockingProcedure.getCiId())
		.append(PROCEDURE_NAME)
		.append(blockingProcedure.getProcedureName())
		.append(CURRENT_STATUS)
		.append(blockingProcedure.getProcedureState().name());
			
		return new OpsException(
				CmsError.OPS_ALREADY_HAVE_ACTIVE_ACTION_ERROR, sb.toString())
		.set(OpsException.ExceptionDetailKey.BLOCKING_PROCEDURE_CI_ID.name(),///fill details Map in exception...
				String.valueOf(blockingProcedure.getCiId()))
		.set(OpsException.ExceptionDetailKey.PROCEDURE_ID.name(),
				String.valueOf(blockingProcedure.getProcedureId())) 
		.set(OpsException.ExceptionDetailKey.MAX_EXEC_ORDER.name(),
				String.valueOf(blockingProcedure.getMaxExecOrder()))
		.set(OpsException.ExceptionDetailKey.PROCEDURE_NAME.name(),
				String.valueOf(blockingProcedure.getProcedureName()))
		.set(OpsException.ExceptionDetailKey.PROCEDURE_STATE.name(),
				String.valueOf(blockingProcedure.getProcedureState()))
		.set(OpsException.ExceptionDetailKey.ARG_LIST.name(),
				String.valueOf(blockingProcedure.getArglist()));
	}
    /**
     * Creates the cms ops procedure.
     * Can throw an OpsException if the proc argument lacks an action, or if there
     * exists already a conflicting procedure
     * @param proc the proc
     * @return the cms ops procedure
     */
    public CmsOpsProcedure createCmsOpsProcedure(CmsOpsProcedure proc) {

        if(proc.getActions().isEmpty()) {
        	logger.warn("throwing exception, CmsOpsProcedure contains zero actions for proc ciId:" + proc.getCiId());
            throw new OpsException(CmsError.OPS_ONE_ACTION_MUST_BE_ERROR,
                                        "Ops Procedure must contains at least one action.");
        }
        
        CmsOpsProcedure blockingProcedure = getBlockingOpsProcedure(proc);
        if (blockingProcedure != null) {
        	logger.warn("throwing exception, procedure blocked by proc: "+blockingProcedure.getCiId());
        	throw createExceptionForAnchorBlock(proc.getCiId(), proc.getProcedureName(), blockingProcedure);
		}
        
        blockingProcedure = getActionHoldingOpsProcedure(proc);
        if (blockingProcedure != null){
        	logger.warn("throwing exception, procedure blocked by action: "+blockingProcedure.getCiId());
        	throw(createExceptionForActionBlock(proc.getCiId(), proc.getProcedureName(), blockingProcedure));
    
        }
        
		for (CmsOpsAction action: proc.getActions()) {
		   blockingProcedure = getBlockingOpsProcedure(action);
           if(blockingProcedure != null) {
           		logger.warn("throwing exception, procedure's action blocked by proc: "+blockingProcedure.getCiId());
            	throw createExceptionForAnchorBlock(action.getCiId(), action.getActionName(), blockingProcedure);
           }
           blockingProcedure = getActionHoldingOpsProcedure(action);
       		if (blockingProcedure!=null){
            	logger.warn("throwing exception, procedure's action blocked by action: "+blockingProcedure.getCiId());
            	throw(createExceptionForActionBlock(action.getCiId(), action.getActionName(), blockingProcedure));

           }
				/*
				        if(opsMapper.isOpenedReleaseExistForCi(action.getCiId())) {
				            throw new OpsException(CmsError.OPS_ALREADY_HAVE_OPENED_RELEASE_ERROR,
				                                        "Given ci " + action.getCiId() + " already has opened release in rfc.");
				        }
				 */
       		
			if (Boolean.FALSE == proc.getForceExecution()
					&& isActiveDeploymentExistsForCi(action.getCiId())) {
				logger.warn("throwing exception, CmsOpsProcedure has already active deployment");
				throw new OpsException(
						CmsError.OPS_ALREADY_HAVE_ACTIVE_DEPLOYMENT_ERROR,
						GIVEN_CI + action.getCiId() + " has active deployment.");
			}
		}
		
		

        proc.setProcedureId(opsMapper.getNextCmOpsProcedureId());
        opsMapper.createCmsOpsProcedure(proc);

        for(CmsOpsAction action: proc.getActions()) {
            action.setProcedureId(proc.getProcedureId());
            opsMapper.createCmsOpsAction(action);
        }
		logger.info("Created ops procedure procID: " + proc.getProcedureId()
				+ " procName :" + proc.getProcedureName() + " forceExecution :"
				+ proc.getForceExecution());
        return getCmsOpsProcedure(proc.getProcedureId(), false);
    }

    
    /**
     * Checks for active or pending ops procedures that have an ops action associated
     * with the input proc's CiId
     * @param proc CmsOpsProcedure to be checked
     * @return CmsOpsProcedure that has such action, or null if none fit criteria
     */
    private CmsOpsProcedure getActionHoldingOpsProcedure(CmsOpsProcedure proc) {
		return getActionHoldingOpsProcedureForCi(proc.getCiId());
	}
    /**
     * Checks for active or pending ops procedures that have an ops action associated
     * with the input proc's CiId
     * @param proc CmsOpsProcedure to be checked
     * @return CmsOpsProcedure that has such action, or null if none fit criteria
     */
    private CmsOpsProcedure getActionHoldingOpsProcedure(CmsOpsAction action) {
		return getActionHoldingOpsProcedureForCi(action.getCiId());
	}
    /** implementation for getActionHoldingOpsProcedure(s) */
	private CmsOpsProcedure getActionHoldingOpsProcedureForCi(long ciId) {
		logger.info("checking ci "+ciId+" actions with opsMapper.getProcedureForCiByAction...");
		List<CmsOpsProcedure> cmsOpsProcedures = opsMapper.getProcedureForCiByAction(ciId, null, null, null);
		//we got most recent procedures for the proc's Ci - regardless of procedure state - 
		// up to the default max result set limit (10). Another approach would have been to call the query two
		// times, looking for pending and then looking for active, but chose to do one call then weed out.
		if (cmsOpsProcedures!=null && cmsOpsProcedures.size() > 0){
			for (CmsOpsProcedure anOpsProcedure : cmsOpsProcedures){
				if (anOpsProcedure.getProcedureState().equals(OpsProcedureState.pending)
					|| anOpsProcedure.getProcedureState().equals(OpsProcedureState.active)){
					return anOpsProcedure;
				}
			}		
		}
		return null;
	}



    /**
     * checks for any active or pending procedures that block execution of input
     * CmsOpsProcedure argument. If any such blockers exist, one of them will be returned
     * otherwise if no such blocking exists the method returns null
     * @return CmsOpsProcedure which blocks, or else null if no blocking
     */
    private CmsOpsProcedure getBlockingOpsProcedure(CmsOpsProcedure proc) {
		return getBlockingOpsProcedureForCi(proc.getCiId());
	}
    /**
     * checks for any active or pending procedures in the system that block execution
     * of input CmsOpsAction argument. If any such blockers exist, one of them will be returned
     * otherwise if no such blocking exists the method returns null
     * @return CmsOpsProcedure which blocks, or else null if no blocking
     */
    private CmsOpsProcedure getBlockingOpsProcedure(CmsOpsAction action) {
		return getBlockingOpsProcedureForCi(action.getCiId());
	}
    /** implementation for getBlockingOpsProcedure(s) */
	private CmsOpsProcedure getBlockingOpsProcedureForCi(long ciId) {
		List<CmsOpsProcedure> cmsOpsProcedures = opsMapper.getProcedureForCi(ciId, null, null, null);
		//we got most recent procedures for the proc's Ci - regardless of procedure state - 
		// up to the default max result set limit (10). Another approach would have been to call the query two
		// times, looking for pending and then looking for active, but chose to do one call then weed out.
		if (cmsOpsProcedures!=null && cmsOpsProcedures.size() > 0){
			for (CmsOpsProcedure anOpsProcedure : cmsOpsProcedures){
				if (anOpsProcedure.getProcedureState().equals(OpsProcedureState.pending)
					|| anOpsProcedure.getProcedureState().equals(OpsProcedureState.active)){
					return anOpsProcedure;
				}
			}		
		}
		return null;
	}

	private boolean isActiveDeploymentExistsForCi(long ciId){
    	CmsCI ci = cmProcessor.getCiByIdNaked(ciId);
    	int bomIndex = ci.getNsPath().lastIndexOf("/bom/");
    	if (bomIndex == -1) {
    		return false;
    	}
    	String dpmtNsPath = ci.getNsPath().substring(0, bomIndex + 4);
    	return opsMapper.isActiveDeploymentExistForNsPath(dpmtNsPath);
    }
    
    /**
     * Gets the cms ops procedure.
     *
     * @param procedureId the procedure id
     * @param definitionIncludes the definition includes
     * @return the cms ops procedure
     */
    public CmsOpsProcedure getCmsOpsProcedure(long procedureId, boolean definitionIncludes) {
        CmsOpsProcedure procedure;
        if(definitionIncludes) {
            procedure = opsMapper.getCmsOpsProcedureWithDefinition(procedureId);
        } else {
            procedure = opsMapper.getCmsOpsProcedure(procedureId);
        }
        if(procedure == null) {
            throw new OpsException(CmsError.OPS_PROCEDURE_NOT_FOUND_ERROR,
                                            "Ops Procedure with Id ="+procedureId+" not found.");
        }
        procedure.setActions(getCmsOpsActions(procedureId));
        return procedure;
    }

    /**
     * Gets the cms ops procedures for ci.
     *
     * @param ciId the ci id
     * @param stateList the list of states
     * @param procedureName the procedure name
     * @return the cms ops procedures for ci
     */
    public List<CmsOpsProcedure> getCmsOpsProceduresForCi(long ciId, List<OpsProcedureState> stateList, String procedureName, Integer limit) {
        List<CmsOpsProcedure> procedures = opsMapper.getProcedureForCi(ciId, stateList, procedureName, limit);
        for (CmsOpsProcedure procedure : procedures) {
        	procedure.setActions(getCmsOpsActions(procedure.getProcedureId()));
        }
        return procedures;
    }

    /**
     * Gets the cms ops procedures for ci search proc actions (not the procedure anchor ci).
     *
     * @param ciId the ci id
     * @param stateList the list of states
     * @param procedureName the procedure name
     * @return the cms ops procedures for ci
     */
    public List<CmsOpsProcedure> getCmsOpsProceduresForCiByAction(long ciId, List<OpsProcedureState> stateList, String procedureName, Integer limit) {
        List<CmsOpsProcedure> procedures = opsMapper.getProcedureForCiByAction(ciId, stateList, procedureName, limit);
        for (CmsOpsProcedure procedure : procedures) {
        	procedure.setActions(opsMapper.getCmsOpsActionsForCi(procedure.getProcedureId(), ciId));
        }
        return procedures;
    }
  
    
    /**
     * Gets the cms ops procedures for namespace.
     *
     * @param nsPath the ns path
     * @param stateList the list of states
     * @param procedureName the procedure name
     * @return the cms ops procedures for namespace
     */
    public List<CmsOpsProcedure> getCmsOpsProceduresForNamespace(String nsPath, List<OpsProcedureState> stateList, String procedureName) {
        List<CmsOpsProcedure> procedures = opsMapper.getProcedureForNamespace(nsPath, stateList, procedureName);
        setCmsOpsProcedureActions(procedures);
        return procedures;
    }

	/**
	 * sets the actions on the procedures
	 * @param procedures
	 */
    private void setCmsOpsProcedureActions(List<CmsOpsProcedure> procedures) {
		for (CmsOpsProcedure procedure : procedures) {
            procedure.setActions(getCmsOpsActions(procedure.getProcedureId()));
        }
	}


	private List<CmsOpsAction> getCmsOpsActions(long procedureId ) {
		return opsMapper.getCmsOpsActions(procedureId);
	}

	
	public List<CmsOpsProcedure> getCmsOpsProcedureForNamespaceLike(
			String nsPath, List<OpsProcedureState> stateList, String procedureName,
			Integer limit, Boolean actions) {
		String nsLike = CmsUtil.likefyNsPath(nsPath);
		List<CmsOpsProcedure> procedures = opsMapper
				.getProcedureForNamespaceLike(nsPath, nsLike, stateList, procedureName,
						limit);
		// populating the actions by default,
		if (Boolean.TRUE.equals(actions)) {
			setCmsOpsProcedureActions(procedures);
		}
		return procedures;

	}
    /**
     * Creates the cms ops action.
     *
     * @param action the action
     */
    public void createCmsOpsAction(CmsOpsAction action) {
        opsMapper.createCmsOpsAction(action);
    }

    /**
     * Update ops procedure.
     *
     * @param proc the proc
     * @return the cms ops procedure
     */
    public CmsOpsProcedure updateOpsProcedure(CmsOpsProcedure proc) {
    	if (OpsProcedureState.active == proc.getProcedureState()) {
    		CmsOpsProcedure oldProc = opsMapper.getCmsOpsProcedure(proc.getProcedureId());
    		//if the user tries to retry the existing proc
    		if (oldProc != null && OpsProcedureState.failed == oldProc.getProcedureState()) {
    			//check if there is any active ones
    			if(opsMapper.isActiveOpsProcedureExistForCi(proc.getCiId())) {
    	            throw new OpsException(CmsError.OPS_ALREADY_HAVE_ACTIVE_PROCEDURE_ERROR,
    	                                            GIVEN_CI + proc.getCiId() + ALREADY_HAS_ACTIVE_OPS_PROCEDURE);
    	        }
    		}
    		
    	}
        opsMapper.updateCmsOpsProcedureState(proc.getProcedureId(),proc.getProcedureState());
        return proc;
    }

    /**
     * Update procedure state.
     *
     * @param procId the proc id
     * @param state the state
     * @return the cms ops procedure
     */
    public CmsOpsProcedure updateProcedureState(long procId, OpsProcedureState state) {
    	if (OpsProcedureState.active == state) {
    		CmsOpsProcedure proc = opsMapper.getCmsOpsProcedure(procId);
    		//if the user tries to retry the existing proc
    		if (proc != null && OpsProcedureState.failed == proc.getProcedureState()) {
    			//check if there is any active ones
    			if(opsMapper.isActiveOpsProcedureExistForCi(proc.getCiId())) {
    	            throw new OpsException(CmsError.OPS_ALREADY_HAVE_ACTIVE_PROCEDURE_ERROR,
    	                                            GIVEN_CI + proc.getCiId() + ALREADY_HAS_ACTIVE_OPS_PROCEDURE);
    	        }
    		}
    	}
        opsMapper.updateCmsOpsProcedureState(procId, state);
        return opsMapper.getCmsOpsProcedure(procId);
    }
    
    
    /**
     * Update ops action.
     *
     * @param action the action
     * @return the cms ops action
     */
    public CmsOpsAction updateOpsAction(CmsOpsAction action) {
    	CmsOpsAction existingAction = opsMapper.getCmsOpsActionById(action.getActionId());
    	if (existingAction == null) {
    		throw new OpsException(CmsError.OPS_PROCEDURE_NOT_FOUND_ERROR, NO_ACTION_ITH_ID + action.getActionId());
    	} 
    	if ((action.getActionState().equals(OpsActionState.complete) ||
    			action.getActionState().equals(OpsActionState.failed)) && !existingAction.getActionState().equals(OpsActionState.inprogress)) {
    		throw new OpsException(CmsError.OPS_ACTION_IS_NOT_IN_PROGRESS, ACTION_IS_IN_WRONG_STATE + action.getActionId() + "; old state " + existingAction.getActionState() + "; new state " + action.getActionState());
    	}
        opsMapper.updateCmsOpsActionState(action.getActionId(),action.getActionState());
        return action;
    }

    /**
     * Complete action order.
     *
     * @param ao the ao
     */
    public void completeActionOrder(CmsActionOrder ao) {
        opsMapper.updateCmsOpsActionState(ao.getActionId(),ao.getActionState());
        if (ao.getResultCi() != null) {
            cmProcessor.updateCI(ao.getResultCi());
        }
    }

    /**
     * Retry procedure.
     *
     * @param procId the proc id
     * @return the cms ops procedure
     */
    public CmsOpsProcedure retryProcedure(long procId) {
        opsMapper.updateCmsOpsProcedureState(procId, OpsProcedureState.active);
        
        for (CmsOpsAction action : getCmsOpsActions(procId)) {
        	if (action.getActionState().equals(OpsActionState.failed)) {
        		 opsMapper.updateCmsOpsActionState(action.getActionId(),OpsActionState.pending);
        	}
        }
        
        CmsOpsProcedure procedure = opsMapper.getCmsOpsProcedure(procId);
        procedure.setActions(getCmsOpsActions(procId));
        return procedure;
    }

	public long getCmsOpsProceduresCountForCiFromTime(long ciId,
			List<OpsProcedureState> stateList, String procedureName, Date timestamp) {
		return opsMapper.getCmsOpsProceduresCountForCiFromTime(ciId, stateList, procedureName, timestamp);
	}
}
