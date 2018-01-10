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

import com.google.gson.Gson;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.controller.cms.CMSClient;
import com.oneops.controller.domain.WoProcessRequest;
import com.oneops.controller.domain.WoProcessResponse;
import com.oneops.controller.jms.InductorPublisher;
import com.oneops.controller.plugin.WoProcessor;
import com.oneops.controller.sensor.SensorClient;
import com.oneops.sensor.client.SensorClientException;
import java.util.HashMap;
import java.util.Map;
import javax.jms.JMSException;
import org.activiti.engine.delegate.DelegateExecution;
import org.apache.log4j.Logger;

/**
 * The Class WoDispatcher.
 */
public class WoDispatcher {
	
	private static final String ERROR_MESSAGE = "error-message";

	private static Logger logger = Logger.getLogger(WoDispatcher.class);
	
	private InductorPublisher inductorPublisher;
	private WorkflowController wfController;
	private CMSClient cmsClient;
	private SensorClient sensorClient;
	
	private static String WAIT_STATE_NODE = "pwoWaitResponse";
	private static String NR_OF_INSTANCES = "nrOfInstances";
	private static String LOOP_COUNTER = "loopCounter";

	
	final private Gson gson = new Gson();

	public void setCmsClient(CMSClient cmsClient) {
		this.cmsClient = cmsClient;
	}

	/**
	 * Sets the inductor publisher.
	 *
	 * @param inductorPublisher the new inductor publisher
	 */
	public void setInductorPublisher(InductorPublisher inductorPublisher) {
		this.inductorPublisher = inductorPublisher;
	}

	/**
	 * Sets the wf controller.
	 *
	 * @param wfController the new wf controller
	 */
	public void setWfController(WorkflowController wfController) {
		this.wfController = wfController;
	}

	public void dispatchAndUpdate(CmsDeployment dpmt, WorkOrderContext woContext) {
		try {
			CmsWorkOrderSimple assembledWo = cmsClient.getWorkOrder(dpmt, woContext);
			assembledWo.getSearchTags().put(CmsConstants.DEPLOYMENT_MODEL, CmsConstants.DEPLOYMENT_MODEL_DEPLOYER);
			dispatchWO(woContext, assembledWo);
			cmsClient.updateWoState(dpmt, assembledWo, CMSClient.INPROGRESS, null);
			handleReplace(assembledWo);
		} catch(Exception e) {
			logger.error("Exception dispatching workorder rfcId : " +
					woContext.getWoSimple().getRfcId() + " dpmtId " + woContext.getWoSimple().getDeploymentId(), e);
			cmsClient.updateWoState(dpmt, woContext.getWoSimple(), CMSClient.FAILED, woContext.getWoDispatchError());
		}
	}


	public void dispatchWO(WorkOrderContext woContext, CmsWorkOrderSimple assembledWo) throws Exception {
		try {
			if (assembledWo.rfcCi.getImpl() == null) {
				inductorPublisher.publishMessage(
						Long.toString(assembledWo.getDeploymentId()), Long.toString(assembledWo.getDpmtRecordId()),
						assembledWo, "", "deploybom");
			} else {
				String[] implParts = assembledWo.rfcCi.getImpl().split("::");
				if ("class".equalsIgnoreCase(implParts[0])) {
					WoProcessor wop = (WoProcessor) Class.forName(implParts[1]).newInstance();
					String processComplexId = Integer.toString(woContext.getExecOrder());
					WoProcessRequest wopr = new WoProcessRequest();
					wopr.setProcessId(processComplexId);
					wopr.setWo(assembledWo);
					wop.processWo(wopr);
				} else {
					inductorPublisher.publishMessage(
							Long.toString(assembledWo.getDeploymentId()), Long.toString(assembledWo.getDpmtRecordId()),
							assembledWo, "", "deploybom");
				}
			}
		} catch (Exception e) {
			logger.error("unable to dispatch", e);
			woContext.setWoDispatchError(e.getMessage());
			throw e;
		}
	}

	public void publishMessage(CmsActionOrderSimple ao, String woType) throws JMSException {
		inductorPublisher.publishMessage(Long.toString(ao.getProcedureId()), Long.toString(ao.getActionId()), ao, "", woType);
	}

	/**
	 * Dispatch wo.
	 *
	 * @param exec the exec
	 * @param waitTaskName the wait task name
	 */
	public void dispatchWO(DelegateExecution exec, CmsWorkOrderSimple wo, String waitTaskName) {

		try {
	    	String processId = exec.getProcessInstanceId();
	    	String execId = exec.getId();
			
	    	if (wo.rfcCi.getImpl() == null) {
	    		inductorPublisher.publishMessage(processId, execId, wo, waitTaskName, "deploybom");
	    	} else {
	    		String[] implParts = wo.rfcCi.getImpl().split("::");
	    		if ("class".equalsIgnoreCase(implParts[0])) {
						WoProcessor wop = (WoProcessor) Class.forName(implParts[1]).newInstance();
				    	String processInstanceId = exec.getProcessInstanceId();
				    	String processComplexId = processInstanceId + "!" + execId + "!" + waitTaskName;
				    	WoProcessRequest wopr = new WoProcessRequest();
				    	wopr.setProcessId(processComplexId);
				    	wopr.setWo((CmsWorkOrderSimple)exec.getVariable("wo"));
						wop.processWo(wopr);
	    		} else {
	    			inductorPublisher.publishMessage(processId, execId, wo, waitTaskName, "deploybom");
	    		}
	    	}
	    	setOrCreateLocalVar(exec, WorkflowController.WO_STATE, WorkflowController.WO_SUBMITTED);
		} catch (JMSException e) {
			logger.error("unable to dispactch" , e);
			setOrCreateLocalVar(exec, WorkflowController.WO_STATE, WorkflowController.WO_FAILED);
			setOrCreateLocalVar(exec, ERROR_MESSAGE, e.getMessage());
		} catch (InstantiationException e) {
			logger.error("unable to dispactch, instantiation problem" , e);
			setOrCreateLocalVar(exec, WorkflowController.WO_STATE, WorkflowController.WO_FAILED);
			setOrCreateLocalVar(exec, ERROR_MESSAGE, e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error("unable to dispactch, access problem" , e);
			setOrCreateLocalVar(exec, WorkflowController.WO_STATE, WorkflowController.WO_FAILED);
			setOrCreateLocalVar(exec, ERROR_MESSAGE, e.getMessage());
		} catch (ClassNotFoundException e) {
			logger.error("unable to dispactch, class problem" , e);
			setOrCreateLocalVar(exec, WorkflowController.WO_STATE, WorkflowController.WO_FAILED);
			setOrCreateLocalVar(exec, ERROR_MESSAGE, e.getMessage());
		}
    }

	private void setOrCreateLocalVar(DelegateExecution exec, String name, Object value) {
		if (exec.getVariableNamesLocal().contains(name)) {
			exec.setVariableLocal(name, value);
		} else {
			exec.createVariableLocal(name, value);
		}
	}
	
	/**
	 * Process wo result.
	 *
	 * @param woResult the wo result
	 */
	public void processWOResult(WoProcessResponse woResult) {
    	String[] props = woResult.getProcessId().split("!");
    	String processId = props[0];
    	String executionId = props[1];
    	//String taskName = props[2];
    	
    	logger.info("Got inductor respose");
    	logger.info(gson.toJson(woResult));

    	Map<String, Object> params = new HashMap<String, Object>();

    	params.put("wo", woResult.getWo());
    	params.put(WorkflowController.WO_STATE, woResult.getWoProcessResult());
    	
    	wfController.pokeSubProcess(processId, executionId, params);

	}

	public void getAndDispatch(DelegateExecution exec, CmsWorkOrderSimple dpmtRec) {
		try {
			CmsWorkOrderSimple wo = cmsClient.getWorkOrder(exec, dpmtRec);
			if (wo != null) {
				cmsClient.updateWoState(exec, wo, CMSClient.INPROGRESS);
				dispatchWO(exec, wo, WAIT_STATE_NODE);
				if ("replace".equals(wo.getRfcCi().getRfcAction())) {
					try {
						sensorClient.processMonitors(wo);
					} catch (SensorClientException e) {
						logger.error("Error while sending replaced instance " + wo.getRfcCi().getCiId() + " to sensor ", e);
					}
				}
			} else {
				cmsClient.updateWoState(exec, dpmtRec, CMSClient.FAILED);
				//this is really a hack I need to release the stack here for process to get to syncWait node, but I also need something to poke it
				//if all WOs for the step failed nothing will poke the syncWait
				if (((Integer)exec.getVariable(NR_OF_INSTANCES)).intValue() == ((Integer)exec.getVariable(LOOP_COUNTER)).intValue() + 1) {
					pokeSubProcAsync(exec.getProcessInstanceId(), exec.getId(), exec.getVariables());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void handleReplace(CmsWorkOrderSimple wo) {
		if ("replace".equals(wo.getRfcCi().getRfcAction())) {
			try {
				sensorClient.processMonitors(wo);
			} catch (SensorClientException e) {
				logger.error("Error while sending replaced instance " + wo.getRfcCi().getCiId() + " to sensor ", e);
			}
		}
	}

	private void  pokeSubProcAsync(final String procId, final String execId, final Map<String,Object> params) {
		final Runnable pocker = new Runnable() {
	        public void run() {
	        	try {
					Thread.sleep(1000);
					wfController.checkSyncWait(procId, execId);
	        	} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
	    };
		Thread t = new Thread(pocker);
		t.start();
	}
	/*
	public void updateWoState(DelegateExecution exec, CmsWorkOrderSimple wo, String newState) {
		cmsClient.updateWoState(exec, wo, newState);
		wfController.checkSyncWait(exec.getProcessInstanceId(), exec.getId());
	}	
	*/

	public void setSensorClient(SensorClient sensorClient) {
		this.sensorClient = sensorClient;
	}
}
