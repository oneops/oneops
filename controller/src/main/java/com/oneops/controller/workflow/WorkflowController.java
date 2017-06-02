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

import com.oneops.antenna.domain.NotificationSeverity;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.controller.cms.DeploymentNotifier;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The Class WorkflowController.
 */
public class WorkflowController {

	public static final String WO_SUBMITTED = "submitted";
	public static final String WO_FAILED = "failed";
	public static final String WO_RECEIVED = "received";
	public static final String WO_STATE = "wostate";
	public static final String SUB_PROC_END_VAR = "sub_proc_end";
	
	
	private static Logger logger = Logger.getLogger(WorkflowController.class);
	
	private static final int STEP_FINISH_RETRIES = 3;
	
	private RuntimeService runtimeService;

	public DeploymentNotifier getNotifier() {
		return notifier;
	}

	public void setNotifier(DeploymentNotifier notifier) {
		this.notifier = notifier;
	}

	private DeploymentNotifier notifier;
	
	/**
	 * Sets the runtime service.
	 *
	 * @param runtimeService the new runtime service
	 */
	public void setRuntimeService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}

	/**
	 * Start dpmt process.
	 *
	 * @param processKey the process key
	 * @param params the params
	 * @return the string
	 */
	public String startDpmtProcess(String processKey, Map<String,Object> params){
		CmsDeployment dpmt = (CmsDeployment)params.get("dpmt");
		String processId = dpmt.getProcessId();
		if (processId == null) {
			logger.info("starting new process for " + processKey + " with params: " + params.toString());
			ProcessInstance pi = runtimeService.startProcessInstanceByKey(processKey, params);
			logger.info("started process with id - " + pi.getId());
			return pi.getId();
		} else {
			String[] procParts = processId.split("!");
			String procInstanceId = procParts[0]; 
			ProcessInstance process = runtimeService.createProcessInstanceQuery().processInstanceId(procInstanceId).singleResult();
			if (process != null) {
				ExecutionEntity exec = (ExecutionEntity)process;
				//if the process is in waiting for pause state - resume
				if (exec.isActive() && exec.getActivityId().equals("dpmtPauseWait")) {
					//sedn resume notification
					notifier.sendDeploymentNotification(dpmt, "Deployment resumed by " + dpmt.getUpdatedBy(),
							notifier.createDeploymentNotificationText(dpmt), NotificationSeverity.info, null);
					return exec.getId();
					//runtimeService.signal(exec.getId());
				} else {
					//lets check if there are any workorders wating for completion
					List<Execution> subExecutions = runtimeService.createExecutionQuery().processInstanceId(procInstanceId).activityId("pwo").list();
					if (subExecutions.size() > 0) {
						//seems like the process is waiting for WOs to complete, lets sleep for 5 sec and try again, if subExecs are not decreasing it's stuck deployment
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						List<Execution> newSubExecutions = runtimeService.createExecutionQuery().processInstanceId(procInstanceId).activityId("pwo").list();
						if (subExecutions.size() == newSubExecutions.size()) {
					    	String executionId = runtimeService.startProcessInstanceByKey(processKey, params).getId();
							notifier.sendDeploymentNotification(dpmt,  "Deployment being retried by " + dpmt.getUpdatedBy(),
							notifier.createDeploymentNotificationText(dpmt), NotificationSeverity.info, null);
							return executionId;
						}
						return "skip";
					} else {
						//seems like we got stuck deployment lets see if it's stucked at subJoined
					    Execution exSubJoin = runtimeService.createExecutionQuery()
							      .processInstanceId(processId)
							      .activityId("subJoined").singleResult();
					    
					    if (exSubJoin != null) {
					    	return exSubJoin.getId();
					    } else {
					    	// if we got here it's a stuck deployment and we need to create a new process
					    	String executionId = runtimeService.startProcessInstanceByKey(processKey, params).getId();
							notifier.sendDeploymentNotification(dpmt,  "Deployment being retried by " + dpmt.getUpdatedBy(),
							notifier.createDeploymentNotificationText(dpmt), NotificationSeverity.info, null);
							return executionId;
					    }
					}
				}
			} else {
				//the old process ended there is an attempt to restart the deployment (normally should not happened)
				//will create new process instance
				String executionId = runtimeService.startProcessInstanceByKey(processKey, params).getId();
				notifier.sendDeploymentNotification(dpmt,  "Deployment being retried by " + dpmt.getUpdatedBy(),
				notifier.createDeploymentNotificationText(dpmt), NotificationSeverity.info, null);
				return executionId;
			}
		}
		//return null;
	};

	/**
	 * Start release process.
	 *
	 * @param processKey the process key
	 * @param params the params
	 * @return the string
	 */
	public String startReleaseProcess(String processKey, Map<String,Object> params){
		logger.info("starting process for " + processKey + " with params: " + params.toString());
		ProcessInstance pi = runtimeService.startProcessInstanceByKey(processKey, params);
		logger.info("started process with id - " + pi.getId());
		return pi.getId();
	};
	
	/**
	 * Start ops process.
	 *
	 * @param processKey the process key
	 * @param params the params
	 * @return the string
	 */
	public String startOpsProcess(String processKey, Map<String,Object> params){

		logger.info("starting process for " + processKey + " with params: " + params.toString());
		ProcessInstance pi = runtimeService.startProcessInstanceByKey(processKey, params);
		logger.info("started process with id - " + pi.getId());
		return pi.getId();
	};

	
	/**
	 * Poke process.
	 *
	 * @param processId the process id
	 */
	public void pokeProcess(String processId){

	    Execution execution = runtimeService.createExecutionQuery()
	      .processInstanceId(processId)
	      .singleResult();

	    runtimeService
	    .signal(execution.getId());    	
		
		logger.info("Poked process with id - " + processId);
	};


	/**
	 * Poke process.
	 *
	 * @param processId the process id
	 */
/*
	public void pokeWithSubProcess(String processId){
	    Execution execution = runtimeService.createExecutionQuery()
	      .processInstanceId(processId)
	      .singleResult();
	    runtimeService
	    .signal(execution.getId());    	
		
		logger.info("Poked process with id - " + processId);
		
		
		List<Execution> subExecutions = runtimeService.createExecutionQuery().processInstanceId(processId).activityId("pwo").list();
	    if (subExecutions.size()>0) {
		    List<Execution> execsSyncWait = runtimeService.createExecutionQuery()
				      .processInstanceId(processId)
				      .activityId("subSync").list();
		    logger.info("Number of subprocesses - " + subExecutions.size());
		    logger.info("Number of subprocesses waiting in sync block - " + execsSyncWait.size());
		    if (execsSyncWait.size() == subExecutions.size()) {
		        
		    	logger.info("All sub processes waiting in sync block, will poke all of them now.");
		        int pokesCounter = 0;
		        for (Execution syncExec : execsSyncWait) {
	 	    		//logger.info("Poking sync sub process with id - " + syncExec.getId()+" deployment: " +isDeployment(params)+" id "+getIdTobeLogged(params));
	 	    		try {
	 	    			runtimeService.signal(syncExec.getId());
	 	    			pokesCounter++;
	 	    		} catch (ActivitiOptimisticLockingException aole) {
		 	   			//this is ok, some other process beat this on completion
		 	   			logger.warn(aole);
	 	    		} catch (ActivitiObjectNotFoundException aonfe) {
	 	    			//other process beats us on this just ignore
	 	    			logger.warn(aonfe);
	 	    		} catch (PersistenceException pe) {
	 	    			pe.printStackTrace();
	 	    			logger.error(pe.getMessage());
	 	    		}
		        }
		        
		        logger.info(">>>>>>Completed " + pokesCounter + " subprocesses out of " + execsSyncWait.size() + " waiting!");
		       //logger.info(">>>>>>Completed step ExecOrder " + getExecOrder(params)+ "  deployment " +isDeployment(params) +" id: " +getIdTobeLogged(params));
		    }
	    }
	};
	*/
	
	/**
	 * Poke sub process.
	 *
	 * @param processId the process id
	 * @param executionId the execution id
	 * @param waitTaskId the wait task id
	 * @param params the params
	 */
	public void pokeSubProcess(String processId, String executionId, Map<String,Object> params){

        List<Execution> subExecutions = runtimeService.createExecutionQuery().processInstanceId(processId).activityId("pwo").list();
        
		int retries = subExecutions.size() > 0 ? subExecutions.size() + 1 : 3;
		
		for (int i=1; i<=retries ; i++) {
			try {
				Set<Entry<String, Object>> enstrySetInMap = params.entrySet();
			    for (Entry<String, Object> aMapEntry : enstrySetInMap) {
				    runtimeService.setVariableLocal(executionId, aMapEntry.getKey(), aMapEntry.getValue());
			    }
			    logger.info("Poking sub process with id - " + executionId+" deployment : "+isDeployment(params)+" id: "+getIdTobeLogged(params));
			    runtimeService.signal(executionId);  
			    break;
			} catch (ActivitiOptimisticLockingException aole) {
				//this is ok, some other process beat this on completion
				logger.warn(aole);
				// but what seems to be the issues - transaction is being rolled back and the subprocess is not completed
				//so in this case we will retry subprocess
				//go to retry
			} catch (ActivitiObjectNotFoundException aonf) {
				logger.warn("seems like this is a dup on inductor response and this was already processed deployment : "+isDeployment(params)+" id: "+getIdTobeLogged(params));
				logger.warn(aonf);
				//seems like this is a dup on inductor reponse and this was already processed
				// to avoid clogging the queue we will ignore this
				break;
			} catch (PersistenceException pe) {
				//some activiti race condition need to retry
				logger.error("Error on processing inductor responce deployment : "+isDeployment(params)+" id: "+getIdTobeLogged(params));
				logger.error(pe);
				//go to retry
			}
		}
		
	    if (subExecutions.size()>0) {
		    List<Execution> execsSyncWait = runtimeService.createExecutionQuery()
				      .processInstanceId(processId)
				      .activityId("subSync").list();

		    logger.info("Number of subprocesses - " + subExecutions.size());
		    logger.info("Number of subprocesses waiting in sync block - " + execsSyncWait.size());
		    
	        Boolean reachedSubProcEnd = (Boolean)runtimeService.getVariable(processId, SUB_PROC_END_VAR);
		    
		    if (execsSyncWait.size() == subExecutions.size() || (reachedSubProcEnd != null && reachedSubProcEnd)) {
		        
		    	logger.info("All sub processes waiting in sync block, will poke all of them now.");
		    	
		    	runtimeService.setVariable(processId, SUB_PROC_END_VAR, new Boolean(true));
		    	
		        int pokesCounter = 0;
		        
		        for (Execution syncExec : execsSyncWait) {
		        	boolean needReTry = true;
		        	for (int i=1; i<=STEP_FINISH_RETRIES && needReTry; i++) {
			        	needReTry = false;
			        	logger.info("Poking sync sub process with id - " + syncExec.getId()+" deployment: " +isDeployment(params)+" id "+getIdTobeLogged(params));
		 	    		try {
		 	    			runtimeService.signal(syncExec.getId());
		 	    			pokesCounter++;
		 	    		} catch (ActivitiException aole) {
			 	   			//this is ok, some other process beat this on completion
			 	   			logger.warn(aole);
			 	   			needReTry = true;
		 	    		} catch (PersistenceException pe) {
		 	    			pe.printStackTrace();
		 	    			logger.error("Error on joing sub processes");
		 	    			logger.error(pe.getMessage());
		 	    			//workaround of activi bug when multiple instances of the same var is created
		 	    			String[] badVars = {"wostate", "wo"};
		 	    			for (String varName : badVars) {
		 	    				runtimeService.removeVariableLocal(syncExec.getId(), varName);
		 	    			}
		 	    			needReTry = true;
		 	    		}
		        	}
		        }
		        
		        logger.info(">>>>>>Completed " + pokesCounter + " subprocesses out of " + execsSyncWait.size() + " waiting!");
		        logger.info(">>>>>>Completed step ExecOrder " + getExecOrder(params)+ "  deployment " +isDeployment(params) +" id: " +getIdTobeLogged(params));
		        
		        signalSubJoin(processId);
		    }
	    } else {
	    	signalSubJoin(processId);
	    }
	}

    private void signalSubJoin(String processId) {
	    Execution exSubJoin = runtimeService.createExecutionQuery()
			      .processInstanceId(processId)
			      .activityId("subJoined").singleResult();

	    if (runtimeService.createExecutionQuery().processInstanceId(processId).count() > 0) {
	    	runtimeService.setVariable(processId, SUB_PROC_END_VAR, new Boolean(false));
	    }
	    
	    if (exSubJoin != null) {
	    	runtimeService.signal(exSubJoin.getId());
	    }
    }
	
	/**
	 * Poke sub process.
	 *
	 * @param processId the process id
	 */
	public void checkSyncWait(String processId, String executionId){
		
        List<Execution> subExecutions = runtimeService.createExecutionQuery().processInstanceId(processId).activityId("pwo").list();
		    
	    if (subExecutions.size()>0) {
		    List<Execution> execsSyncWait = runtimeService.createExecutionQuery()
				      .processInstanceId(processId)
				      .activityId("subSync").list();

		    logger.info("Number of subprocesses - " + subExecutions.size());
		    logger.info("Number of subprocesses waiting in sync block - " + execsSyncWait.size());
		    if (execsSyncWait.size() == subExecutions.size()) {
		        
		    	logger.info("All sub processes waiting in sync block, will poke all of them now.");
		        
		        int pokesCounter = 0;
		        
		        for (Execution syncExec : execsSyncWait) {
		        	boolean needReTry = true;
		        	for (int i=1; i<=STEP_FINISH_RETRIES && needReTry; i++) {
			        	needReTry = false;
			        	logger.info("Poking sync sub process with id - " + syncExec.getId());
		 	    		try {
		 	    			runtimeService.signal(syncExec.getId());
		 	    			pokesCounter++;
		 	    		} catch (ActivitiOptimisticLockingException aole) {
			 	   			//this is ok, some other process beat this on completion
			 	   			logger.warn(aole);
		 	    		} catch (ActivitiObjectNotFoundException aonfe) {
		 	    			//other process beats us on this just ignore
		 	    			logger.warn(aonfe);
		 	    		} catch (PersistenceException pe) {
		 	    			pe.printStackTrace();
		 	    			logger.error(pe.getMessage());
		 	    			//woraround of activi bug when multiple instances of the same var is created
		 	    			String[] badVars = {"wostate", "wo"};
		 	    			for (String varName : badVars) {
		 	    				runtimeService.removeVariableLocal(syncExec.getId(), varName);
		 	    				runtimeService.removeVariableLocal(syncExec.getId(), varName);

		 	    			}
		 	    			needReTry = true;
		 	    		}
		        	}
		        }
		        
		        logger.info(">>>>>>Completed " + pokesCounter + " subprocesses out of " + execsSyncWait.size() + " waiting!");
		        signalSubJoin(processId);
		    }
	    }
	}
	
    private long getIdTobeLogged(final Map<String, Object> params) {
        long id = 0;
        CmsWorkOrderSimple wo = params.get("wo") instanceof CmsWorkOrderSimple ? ((CmsWorkOrderSimple) params.get("wo")) : null;
        CmsActionOrderSimple ao = params.get("wo") instanceof CmsActionOrderSimple ? ((CmsActionOrderSimple) params.get("wo")) : null;
        //procedure id is used on UI
        id = (wo != null) ? wo.getDeploymentId() : (ao != null) ? ao.getProcedureId() : 0;
        logger.debug("id :" + id + " wo:" + wo + " ao:" + ao);
        return id;
    }

    private long getExecOrder(final Map<String, Object> params) {
        long id = 0;
        CmsWorkOrderSimple wo = params.get("wo") instanceof CmsWorkOrderSimple ? ((CmsWorkOrderSimple) params.get("wo")) : null;
        CmsActionOrderSimple ao = params.get("wo") instanceof CmsActionOrderSimple ? ((CmsActionOrderSimple) params.get("wo")) : null;
        id = (wo != null) ? wo.rfcCi.getExecOrder() : (ao != null) ? ao.getExecOrder() : 0;
        logger.debug("exec order :" + id + " wo:" + wo + " ao:" + ao);
        return id;
    }

    private boolean isDeployment(final Map<String, Object> params) {
        CmsWorkOrderSimple wo = params.get("wo") instanceof CmsWorkOrderSimple ? ((CmsWorkOrderSimple) params.get("wo")) : null;
        return (wo != null) ? true : false;
    }

}