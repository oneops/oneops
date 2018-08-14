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
package com.oneops.controller.cms;

import static com.oneops.cms.cm.ops.domain.OpsActionState.failed;

import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.cm.ops.domain.CmsOpsAction;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsActionState;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.cm.ops.service.OpsProcedureProcessor;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.crypto.CmsCrypto;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsDpmtRecord;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsWorkOrder;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import com.oneops.cms.exceptions.CmsBaseException;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.cms.util.domain.CmsVar;
import com.oneops.controller.util.ControllerUtil;
import com.oneops.controller.workflow.WorkOrderContext;
import com.oneops.controller.workflow.WorkflowController;
import com.oneops.notification.NotificationMessage;
import com.oneops.notification.NotificationType;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.activiti.engine.delegate.DelegateExecution;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * The Class CMSClient.
 */

public class CMSClient {
    private static Logger logger = Logger.getLogger(CMSClient.class);

    private static final int NSPATH_ENV_ELEM_NO = 3;
    private static final int NSPARTS_SIZE_MIN = 5;
    public static final String INPROGRESS = "inprogress";
    public static final String CANCELLED = "canceled";
    public static final String COMPLETE = "complete";
    public static final String FAILED = "failed";
    public static final String PAUSED = "paused";
    public static final String PENDING = "pending";
    public static final String DPMT = "dpmt";

    private RestTemplate restTemplate;
    @SuppressWarnings("unused")
	private String serviceUrl; // = "http://localhost:8080/adapter/rest/";
    private String transUrl = "http://cmsapi:8080/transistor/rest/";
    private CmsCrypto cmsCrypto;
    private Gson gson = new Gson();
    private DeploymentNotifier deploymentNotifier;
    private int stepWoLimit = 100;
    private CmsWoProvider cmsWoProvider;
	private CmsDpmtProcessor cmsDpmtProcessor;
	private CmsCmProcessor cmsCmProcessor;
	private CmsUtil cmsUtil;
    private ControllerUtil controllerUtil;
    private OpsProcedureProcessor opsProcedureProcessor;

    public static final String ONEOPS_SYSTEM_USER = "oneops-system";

    public void setCmsDpmtProcessor(CmsDpmtProcessor cmsDpmtProcessor) {
        this.cmsDpmtProcessor = cmsDpmtProcessor;
    }

    public void setCmsCmProcessor(CmsCmProcessor cmsCmProcessor) {
        this.cmsCmProcessor = cmsCmProcessor;
    }

    public void setOpsProcedureProcessor(OpsProcedureProcessor opsProcedureProcessor) {
        this.opsProcedureProcessor = opsProcedureProcessor;
    }

    public void setCmsUtil(CmsUtil cmsUtil) {
		this.cmsUtil = cmsUtil;
	}

	public void setControllerUtil(ControllerUtil controllerUtil) {
		this.controllerUtil = controllerUtil;
	}

	public void setCmsWoProvider(CmsWoProvider cmsWoProvider) {
		this.cmsWoProvider = cmsWoProvider;
	}

    public void setStepWoLimit(int stepWoLimit) {
        this.stepWoLimit = stepWoLimit;
    }

    public void setRetryTemplate(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    @Autowired
    private RetryTemplate retryTemplate;

    /**
     * Sets the rest template.
     *
     * @param restTemplate the new rest template
     */
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sets the service url.
     *
     * @param serviceUrl the new service url
     */
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * Sets the trans url.
     *
     * @param transUrl the new trans url
     */
    public void setTransUrl(String transUrl) {
        this.transUrl = transUrl;
    }

    /**
     * Sets the cms crypto.
     *
     * @param cmsCrypto the new cms crypto
     */
    public void setCmsCrypto(CmsCrypto cmsCrypto) {
        this.cmsCrypto = cmsCrypto;
    }


    /**
     * Gets the work orders.
     *
     * @param exec the exec
     * @return the work orders
     * @throws GeneralSecurityException the general security exception
     */
    public void getWorkOrderIds(DelegateExecution exec) {
        CmsDeployment dpmt = (CmsDeployment) exec.getVariable(DPMT);
        Integer execOrder = (Integer) exec.getVariable(CmsConstants.EXEC_ORDER);
        long startTime = System.currentTimeMillis();
        try {
            // if not - resubmit them, this is not normal situation but sometimes activiti completes subprocess
            // without calling final step, not sure how or why
            List<CmsWorkOrderSimple> recList = null;
            boolean pendingList = false;
            recList = cmsWoProvider.getWorkOrderIdsSimple(dpmt.getDeploymentId(), "inprogress", execOrder, this.stepWoLimit);
            if (recList.size() == 0) {
              pendingList= true;
            	recList = cmsWoProvider.getWorkOrderIdsSimple(dpmt.getDeploymentId(), "pending", execOrder, this.stepWoLimit);
            }
            logger.info(dpmt.getDeploymentId()+"-"+execOrder +" Got workOrderIds size: " + recList.size()
                + " took " +(System.currentTimeMillis()-startTime) +"ms pendingList "+ pendingList );
            exec.setVariable("dpmtrecs", recList);
        } catch (CmsBaseException e) {
            logger.error(dpmt.getDeploymentId()+"-"+execOrder +" CmsException :", e);
            String descr = dpmt.getDescription();
            if (descr == null) {
                descr = "";
            }
            descr += "\n Can not get workorderIds for step #" + execOrder + ";\n " + e.getMessage();
            handleWoError2(exec, dpmt, descr);
        }
    }

    public List<CmsWorkOrderSimple> getWorkOrderIdsNoLimit(CmsDeployment dpmt, int execOrder) {
        long startTime = System.currentTimeMillis();
        try {
            List<CmsWorkOrderSimple> recList = cmsWoProvider.getWorkOrderIdsSimple(dpmt.getDeploymentId(), PENDING,
                    execOrder, null);
            logger.info(dpmt.getDeploymentId()+"-"+execOrder +" Got workOrderIds size: " + recList.size()
                    + " took " +(System.currentTimeMillis()-startTime) +"ms");
            return recList;
        } catch (CmsBaseException e) {
            logger.error(dpmt.getDeploymentId()+"-"+execOrder +" CmsException :", e);
            handleWoError(dpmt, execOrder, e);
            throw e;
        }
    }

    private void handleWoError(CmsDeployment dpmt, int execOrder, CmsBaseException e) {
        String descr = dpmt.getDescription();
        if (descr == null) {
            descr = "";
        }
        descr += "\n Can not get workorderIds for step #" + execOrder + ";\n " + e.getMessage();
        dpmt.setDeploymentState(FAILED);
        dpmt.setDescription(descr);
        cmsDpmtProcessor.updateDeployment(dpmt);
    }

    private void handleWoError(CmsDeployment dpmt, String descr) {
        dpmt.setDeploymentState(FAILED);
        dpmt.setDescription(descr);
        cmsDpmtProcessor.updateDeployment(dpmt);
    }

    /**
     * Gets the work order.
     *
     * @param exec the exec
     * @return the work orders
     * @throws GeneralSecurityException the general security exception
     */
    public CmsWorkOrderSimple getWorkOrder(DelegateExecution exec, CmsWorkOrderSimple dpmtRec) {
        Integer execOrder = (Integer) exec.getVariable(CmsConstants.EXEC_ORDER);
        CmsDeployment dpmt = (CmsDeployment) exec.getVariable(DPMT);
        logger.info("Geting work order pmtRec = " + dpmtRec.getDpmtRecordId() + " for dpmt id = " + dpmtRec.getDeploymentId() + " rfcId =  " + dpmtRec.getRfcId() + " step #" + execOrder);
        long startTime = System.currentTimeMillis();
        try {
            //CmsWorkOrderSimple wo = retryTemplate.execute(retryContext -> restTemplate.getForObject(serviceUrl + "dj/simple/deployments/{deploymentId}/workorders/{dpmtRecId}?execorder={execOrder}", CmsWorkOrderSimple.class, dpmtRec.getDeploymentId(), dpmtRec.getDpmtRecordId(), execOrder));

        	CmsWorkOrderSimple wo  = cmsWoProvider.getWorkOrderSimple(dpmtRec.getDpmtRecordId(), null, execOrder);
            final long woCreationtime = System.currentTimeMillis() - startTime;
            wo.getSearchTags().put("woCrtTime",String.valueOf(woCreationtime));
            logger.info("Time taked to get wo - " + woCreationtime + "ms; pmtRec = " + dpmtRec.getDpmtRecordId() + " for dpmt id = " + dpmtRec.getDeploymentId() + " rfcId =  " + dpmtRec.getRfcId() + " step #" + execOrder);

        	if (wo != null) {
	        	decryptWo(wo);
	        	CmsWorkOrderSimple strippedWo = controllerUtil.stripWO(wo);
	            setOrCreateLocalVar(exec, "wo", strippedWo);
	            setOrCreateLocalVar(exec, WorkflowController.WO_STATE, WorkflowController.WO_RECEIVED);
	        	logger.info("Set WO as activiti local var; pmtRec = " + dpmtRec.getDpmtRecordId() + " for dpmt id = " + dpmtRec.getDeploymentId() + " rfcId =  " + dpmtRec.getRfcId() + " step #" + execOrder);
	            return wo;
        	} else {
                String descr = dpmt.getDescription();
                if (descr == null) {
                    descr = "";
                }
                descr += "\n Can not get workorder for rfc : " + dpmtRec.getRfcId() + "; execOrder : " + execOrder + ";\n ";
                logger.error(descr);
                handleGetWoError(exec, dpmt, dpmtRec, descr);
        	}
        } catch (CmsBaseException e) {
            logger.error("RestClientException rfc : " + dpmtRec.getRfcId() + "; execOrder : " + execOrder, e);
            logger.error(e.getMessage());
            String descr = dpmt.getDescription();
            if (descr == null) {
                descr = "";
            }
            descr += "\n Can not get workorder for rfc : " + dpmtRec.getRfcId() + "; execOrder : " + execOrder + ";\n " + e.getMessage();
            handleGetWoError(exec, dpmt, dpmtRec, descr);
        } catch (GeneralSecurityException e) {
            logger.error("Failed to decrypt workorder for rfc : " + dpmtRec.getRfcId() + "; execOrder : " + execOrder, e);
            String descr = dpmt.getDescription();
            if (descr == null) {
                descr = "";
            }
            descr += "\n Can not decrypt workorder for rfc : " + dpmtRec.getRfcId() + "; execOrder : " + execOrder + ";";
            handleGetWoError(exec, dpmt, dpmtRec, descr);
        }
        return null;
    }

    public CmsWorkOrderSimple getWorkOrder(CmsDeployment dpmt, WorkOrderContext woContext) {
        CmsWorkOrderSimple dpmtRec = woContext.getWoSimple();
        int execOrder = woContext.getExecOrder();
        logger.info("Geting work order pmtRec = " + dpmtRec.getDpmtRecordId() + " for dpmt id = " + dpmtRec.getDeploymentId() + " rfcId =  " + dpmtRec.getRfcId() + " step #" + execOrder);
        long startTime = System.currentTimeMillis();
        try {

            CmsWorkOrderSimple wo  = cmsWoProvider.getWorkOrderSimple(dpmtRec.getDpmtRecordId(), null, execOrder);
            final long woCreationtime = System.currentTimeMillis() - startTime;
            wo.getSearchTags().put("woCrtTime",String.valueOf(woCreationtime));
            wo.getSearchTags().put(CmsConstants.DEPLOYMENT_MODEL, CmsConstants.DEPLOYMENT_MODEL_DEPLOYER);
            logger.info("Time taken to get wo - " + woCreationtime + "ms; pmtRec = " + dpmtRec.getDpmtRecordId() +
                    " for dpmt id = " + dpmtRec.getDeploymentId() + " rfcId =  " + dpmtRec.getRfcId() + " step #" + execOrder);

            if (wo != null) {
                decryptWo(wo);
                CmsWorkOrderSimple strippedWo = controllerUtil.stripWO(wo);
                return wo;
            } else {
                String descr = dpmt.getDescription();
                if (descr == null) {
                    descr = "";
                }
                descr += "\n Can not get workorder for rfc : " + dpmtRec.getRfcId() + "; execOrder : " + execOrder + ";\n ";
                logger.error(descr);
                woContext.setWoDispatchError(descr);
                throw new RuntimeException(descr);
            }
        } catch (CmsBaseException e) {
            String message = "CmsBaseException rfc : " + dpmtRec.getRfcId() + "; execOrder : " + execOrder;
            logger.error(message, e);
            woContext.setWoDispatchError(message);
            throw e;
        } catch (GeneralSecurityException e) {
            String message = "Failed to decrypt workorder for rfc : " + dpmtRec.getRfcId() + "; execOrder : " + execOrder;
            logger.error(message, e);
            woContext.setWoDispatchError(message);
            throw new RuntimeException(e);
        }
    }

    private void handleWoError2(DelegateExecution exec, CmsDeployment dpmt, String descr) {
        dpmt.setDeploymentState(FAILED);
        dpmt.setDescription(descr);
        exec.setVariable(DPMT, dpmt);
        exec.setVariable("dpmtrecs", new ArrayList<CmsWorkOrderSimple>());
    }

    private void handleGetWoError(DelegateExecution exec, CmsDeployment dpmt, CmsWorkOrderSimple dpmtRec, String descr) {
        dpmt.setDeploymentState(FAILED);
        dpmt.setDescription(descr);
        exec.setVariable(DPMT, dpmt);
        setOrCreateLocalVar(exec, WorkflowController.WO_STATE, WorkflowController.WO_FAILED);
        setOrCreateLocalVar(exec, "wo", dpmtRec);
    }

    /**
     * Check dpmt.
     *
     * @param exec the exec
     * @throws GeneralSecurityException the general security exception
     */
    public void checkDpmt(DelegateExecution exec) throws GeneralSecurityException {
        CmsDeployment dpmt = (CmsDeployment) exec.getVariable(DPMT);
        try {
            //CmsDeployment cmsDpmt = retryTemplate.execute(retryContext -> restTemplate.getForObject(serviceUrl + "dj/simple/deployments/{deploymentId}", CmsDeployment.class, dpmt.getDeploymentId()));
        	CmsDeployment cmsDpmt = cmsDpmtProcessor.getDeployment(dpmt.getDeploymentId());
        	if (cmsDpmt == null) {
        		throw new DJException(CmsError.DJ_NO_DEPLOYMENT_WITH_GIVEN_ID_ERROR,"Cant get deployment with id = " + dpmt.getDeploymentId());
        	}
            exec.setVariable(DPMT, cmsDpmt);
        } catch (CmsBaseException e) {
            logger.error("CmsBaseException on check deployment state : dpmtId=" + dpmt.getDeploymentId(), e);
            logger.error(e.getMessage());
            String descr = dpmt.getDescription();
            if (descr == null) {
                descr = "";
            }
            descr += "\n Can not check deployment state for dpmtId : " + dpmt.getDeploymentId() + ";\n " + e.getMessage();
            dpmt.setDeploymentState(FAILED);
            dpmt.setDescription(descr);
            exec.setVariable(DPMT, dpmt);
        }
    }

    /**
     * Update wo state.
     *
     * @param exec     the exec
     * @param wo       the wo
     * @param newState the new state
     */
    public void updateWoState(DelegateExecution exec, CmsWorkOrderSimple wo, String newState) {
        CmsDeployment dpmt = (CmsDeployment) exec.getVariable(DPMT);
        updateWoState(wo, newState, dpmt, exec.getId(),
                (String)exec.getVariable("error-message"),
                d -> {
                    exec.setVariable(DPMT, dpmt);
                });
    }

    public void updateWoState(CmsDeployment dpmt, CmsWorkOrderSimple wo, String newState, String error) {
        updateWoState(wo, newState, dpmt, "", error, null);
    }

    public void updateWoState(CmsWorkOrderSimple wo, String newState, CmsDeployment dpmt, String execContextName,
                              String error, Consumer<CmsDeployment> updateDpmtFunc) {
        wo.setDpmtRecordState(newState);
        if (newState.equalsIgnoreCase(COMPLETE)) {
            completeWO(wo);
        } else {
            CmsDpmtRecord dpmtRec = new CmsDpmtRecord();
            if (newState.equalsIgnoreCase(INPROGRESS)) {
                dpmtRec.setComments("start processing with task Id = " + execContextName);
            }
            dpmtRec.setDpmtRecordId(wo.getDpmtRecordId());
            dpmtRec.setDeploymentId(wo.getDeploymentId());
            dpmtRec.setDpmtRecordState(newState);
            dpmtRec.setComments(wo.getComments());
            if (newState.equalsIgnoreCase(FAILED) && dpmt != null) {
                if (dpmt.getContinueOnFailure() && !isDeleteWO(wo)) {  // we've failed and continue on failure flag is on, so we need to fail all linked managedVia orders. Otherwise if "compute" provisioning fails everything else will get stuck. We can't continue on failure however, if current order is delete RFC to prevent orphan instances.
                    failAllManagedViaWorkOrders(wo);
                } else {
                    dpmt.setDeploymentState(FAILED);
                    if (updateDpmtFunc != null) {
                        updateDpmtFunc.accept(dpmt);
                    }
                    if (error != null) {
                        dpmtRec.setComments(error);
                    }
                }
            }
            try {
                cmsDpmtProcessor.updateDpmtRecordSimple(dpmtRec);
            } catch (CmsBaseException ce) {
                logger.error(ce.getMessage(), ce);
                throw ce;
            }
            logger.info("Client: put:update record id " + wo.getDpmtRecordId() + " to state " + newState);
        }
    }

    private boolean isDeleteWO(CmsWorkOrderSimple wo) {
        return wo.getRfcCi()!=null && "delete".equalsIgnoreCase(wo.getRfcCi().getRfcAction());
    }

    private void failAllManagedViaWorkOrders(CmsWorkOrderSimple wo) {
        try {
            List<CmsRfcCI> cis = cmsWoProvider.getRfcCIRelatives(wo.getRfcCi().getCiId(), "bom.ManagedVia", "to", null, "df");
            List<Long> list = cis.stream().map(CmsRfcCI::getRfcId).collect(Collectors.toList());
            List<CmsWorkOrder> pendingWos = cmsWoProvider.getWorkOrderIds(wo.getDeploymentId(), PENDING, null, null);
            for (CmsWorkOrder pendingWo : pendingWos) {
                if (list.contains(pendingWo.getRfcId())) {
                    pendingWo.setDpmtRecordState(FAILED);
                    CmsDpmtRecord pendingDpmtRec = new CmsDpmtRecord();
                    pendingDpmtRec.setDpmtRecordId(pendingWo.getDpmtRecordId());
                    pendingDpmtRec.setDeploymentId(pendingWo.getDeploymentId());
                    pendingDpmtRec.setDpmtRecordState(FAILED);
                    pendingDpmtRec.setComments("Automatically failed due to failed work-order for managing instance " + wo.getRfcCi().getCiId());
                    cmsDpmtProcessor.updateDpmtRecord(pendingDpmtRec);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }



    private void completeWO(CmsWorkOrderSimple woSimple) {
        try {
        	CmsWorkOrderSimple responseWo = controllerUtil.stripWO(woSimple, true);
        	CmsWorkOrder wo = cmsUtil.custSimple2WorkOrder(responseWo);
        	cmsDpmtProcessor.completeWorkOrder(wo);
            logger.info(">>>>>>>>>>>>>>>>>>>>>completed wo " + wo.getDpmtRecordId() + "! Rfc_Id = " + wo.getRfcId());

        } catch (CmsBaseException ce) {
            logger.error(ce.getMessage(), ce);
            throw ce;
        }
    }

    /**
     * Sets the dpmt process id.
     *
     * @param exec the exec
     * @param dpmt the dpmt
     */
    public void setDpmtProcessId(DelegateExecution exec, CmsDeployment dpmt) {

        String processId = exec.getProcessInstanceId();
        String execId = exec.getId();
        // lets create strip down dpmt to update just a processId and updatedBy
        updateDeploymentAndNotify(dpmt, processId + "!" + execId, "Activiti");
    }

    public void updateDeploymentAndNotify(CmsDeployment dpmt, String processId, String dpmtModel) {
        CmsDeployment dpmtParam = new CmsDeployment();
        dpmtParam.setDeploymentId(dpmt.getDeploymentId());
        dpmtParam.setProcessId(processId);
        dpmtParam.setUpdatedBy(ONEOPS_SYSTEM_USER);
        dpmtParam.setExecModel(dpmtModel);
        try {
            cmsDpmtProcessor.updateDeploymentExecInfo(dpmtParam);
            deploymentNotifier.sendDpmtNotification(dpmt);
        } catch (CmsBaseException e) {
            logger.error("CmsBaseException in updateDeployment", e);
            throw e;
        }

    }


    /**
     * Update dpmt state.
     *
     * @param exec     the exec
     * @param dpmt     the dpmt
     * @param newState the new state
     * @throws InterruptedException
     */
    public void updateDpmtState(DelegateExecution exec, CmsDeployment dpmt, String newState) {

        if (dpmt.getDeploymentState().equalsIgnoreCase(CmsDpmtProcessor.DPMT_STATE_ACTIVE)) {
            CmsDeployment clone = new CmsDeployment();
            BeanUtils.copyProperties(dpmt, clone);
            dpmt = clone;  // Need to clone dpmt before setting the state, otherwise mybatis pulls the same instance that doesn't reflect actual db state
            checkWoStateAndUpdateDpmt(dpmt);
        }
        // lets do the retries here
        logger.info("Client: put:update deployment " + dpmt.getDeploymentId() + " state to " + dpmt.getDeploymentState());
        updateDeploymentEndState(dpmt);
    }

    private void updateDeploymentEndState(CmsDeployment dpmt) {
        try {
            dpmt.setFlagsToNull();
            cmsDpmtProcessor.updateDeployment(dpmt);
            deploymentNotifier.sendDpmtNotification(dpmt);
        } catch (CmsBaseException e) {
            logger.error("CmsBaseException in updateDeployment", e);
            e.printStackTrace();
            throw e;
        }
    }

    private void checkWoStateAndUpdateDpmt(CmsDeployment dpmt) {
        long failedWos = 0;
        try {
            failedWos = cmsDpmtProcessor.getDeploymentRecordCount(dpmt.getDeploymentId(), "failed", null);
        } finally {
            if (failedWos>0){
                dpmt.setDeploymentState(FAILED);
            } else {
                dpmt.setDeploymentState(COMPLETE);
            }
        }
    }

    public void updateDpmtState(CmsDeployment dpmt, String newState) {
        if (dpmt.getDeploymentState().equalsIgnoreCase(CmsDpmtProcessor.DPMT_STATE_ACTIVE)) {
            checkWoStateAndUpdateDpmt(dpmt);
        }
        logger.info("Client: put:update deployment " + dpmt.getDeploymentId() + " state to " + dpmt.getDeploymentState());

        updateDeploymentEndState(dpmt);
    }

    /**
     * Inc exec order.
     *
     * @param exec the exec
     */
    public void incExecOrder(DelegateExecution exec) {
        Integer newExecOrder = (Integer) exec.getVariable(CmsConstants.EXEC_ORDER) + 1;
        if (exec.hasVariable(DPMT)) {
            CmsDeployment dpmt = (CmsDeployment) exec.getVariable(DPMT);
            Set<Integer> autoPauseExecOrders = dpmt.getAutoPauseExecOrders();
            if (autoPauseExecOrders != null && autoPauseExecOrders.contains(newExecOrder)) {
                logger.info("pausing deployment " + dpmt.getDeploymentId() + " before step " + newExecOrder);
                CmsDeployment clone = new CmsDeployment(); // cannot update existing instance need to clone deployment first
                clone.setDeploymentId(dpmt.getDeploymentId());
                clone.setDeploymentState(PAUSED);
                clone.setUpdatedBy(ONEOPS_SYSTEM_USER);
                clone.setComments("deployment paused at step " + newExecOrder + " on " + new Date());
                try {
                    cmsDpmtProcessor.updateDeployment(clone);
                } catch (CmsBaseException e) {
                    logger.error("CmsBaseException in incExecOrder", e);
                    throw e;
                }
            }
        }
        exec.setVariable(CmsConstants.EXEC_ORDER, newExecOrder);
    }

    /**
     * Gets the action orders.
     *
     * @return the action orders
     * @throws GeneralSecurityException the general security exception
     */
    public List<CmsActionOrderSimple>  getActionOrders(CmsOpsProcedure proc, int execOrder) throws GeneralSecurityException {
        logger.info("Geting action orders for procedure id = " + proc.getProcedureId());
        long startTime = System.currentTimeMillis();
        try {
            List<CmsActionOrderSimple> aoList = cmsWoProvider.getActionOrdersSimple(proc.getProcedureId(), OpsProcedureState.pending, execOrder);
            logger.info("Got " + aoList.size() + " action orders for procedure id = " + proc.getProcedureId() + "; Time taken: " + (System.currentTimeMillis() - startTime) + "ms"  );
            for (CmsActionOrderSimple ao : aoList) {
                decryptAo(ao);
            }
            return aoList;
        } catch (CmsBaseException rce) {
            logger.error(rce);
        }
        return Collections.emptyList();
    }

    /**
     * Gets the action orders.
     *
     * @param exec the exec
     * @return the action orders
     * @throws GeneralSecurityException the general security exception
     */

    public void getActionOrders(DelegateExecution exec) throws GeneralSecurityException {
        CmsOpsProcedure proc = (CmsOpsProcedure) exec.getVariable("proc");
        Integer execOrder = (Integer) exec.getVariable(CmsConstants.EXEC_ORDER);
        logger.info("Geting action orders for procedure id = " + proc.getProcedureId());
        long startTime = System.currentTimeMillis();
        try {
            //CmsActionOrderSimple[] aos = retryTemplate.execute(retryContext -> restTemplate.getForObject(serviceUrl + "/cm/ops/procedures/{procedureId}/actionorders?execorder={execOrder}&state=pending", CmsActionOrderSimple[].class, proc.getProcedureId(), execOrder));
        	List<CmsActionOrderSimple> aoList = cmsWoProvider.getActionOrdersSimple(proc.getProcedureId(), OpsProcedureState.pending, execOrder);
        	logger.info("Got " + aoList.size() + " action orders for procedure id = " + proc.getProcedureId() + "; Time taken: " + (System.currentTimeMillis() - startTime) + "ms"  );
        	for (CmsActionOrderSimple ao : aoList) {
              logger.info("Testing ao  " + ao.getCiId() + " bytes length : " + gson.toJson(ao).getBytes().length);
                decryptAo(ao);
            }
            exec.setVariable("cmsaos", aoList);
            if (exec.getVariable("procanchor") == null) {
                CmsCI procAnchorCI = cmsCmProcessor.getCiById(proc.getCiId());
                		//retryTemplate.execute(retryContext -> restTemplate.getForObject(serviceUrl + "/cm/cis/{ciId}", CmsCI.class, proc.getCiId()));
                exec.setVariable("procanchor", procAnchorCI);
            }
        } catch (CmsBaseException rce) {
            logger.error(rce);
            rce.printStackTrace();
            proc.setProcedureState(OpsProcedureState.failed);
            exec.setVariable("proc", proc);
        }
    }



    /**
     * Update procedure state.
     *
     * @param exec the exec
     * @param proc the proc
     */
    public void updateProcedureState(DelegateExecution exec, CmsOpsProcedure proc) {
        NotificationMessage notify = new NotificationMessage();
        notify.setType(NotificationType.procedure);
        if (proc.getProcedureState().equals(OpsProcedureState.active)) {
            proc.setProcedureState(OpsProcedureState.complete);
        }
        logger.info("Client: put:update ops procedure state to " + proc.getProcedureState());
        try {
	        opsProcedureProcessor.updateOpsProcedure(proc);
	        deploymentNotifier.sendProcNotification(proc, exec);
        } catch (CmsBaseException e) {
			logger.error("CmsBaseException in updateProcedureState", e);
			e.printStackTrace();
			throw e;
		}
    }


    /**
     * Update action order state.
     *
     * @param exec     the exec
     * @param aos       the aos
     * @param newState the new state
     */
    public void updateActionOrderState(DelegateExecution exec, CmsActionOrderSimple aos, String newState) {

        aos.setActionState(OpsActionState.valueOf(newState));
        try {
            if (newState.equalsIgnoreCase(COMPLETE)) {
                CmsActionOrder ao = cmsUtil.custSimple2ActionOrder(aos);
                opsProcedureProcessor.completeActionOrder(ao);
                /*
                
                retryTemplate.execute(retryContext -> {
                    restTemplate.put(serviceUrl + "cm/ops/procedures/{procedureId}/actionorders", ao, ao.getProcedureId());
                    return null;
                });
                */
            } else {
                CmsOpsAction action = new CmsOpsAction();
                action.setActionId(aos.getActionId());
                action.setProcedureId(aos.getProcedureId());
                action.setActionState(OpsActionState.valueOf(newState));
                if (newState.equalsIgnoreCase(FAILED) && aos.getIsCritical()) {
                    CmsOpsProcedure proc = (CmsOpsProcedure) exec.getVariable("proc");
                    proc.setProcedureState(OpsProcedureState.failed);
                    exec.setVariable("proc", proc);
                }
                opsProcedureProcessor.updateOpsAction(action);
                /*
                retryTemplate.execute(retryContext -> {
                    restTemplate.put(serviceUrl + "cm/ops/procedures/{procedureId}/actions", action, action.getProcedureId());
                    return null;
                });
                */
            }
        } catch (CmsBaseException rce) {
            logger.error(rce);
            CmsOpsProcedure proc = (CmsOpsProcedure) exec.getVariable("proc");
            proc.setProcedureState(OpsProcedureState.failed);
            exec.setVariable("proc", proc);
        }
        logger.info("Client: put:ops action id " + aos.getActionId() + " to state " + newState);
    }


    public void updateActionOrderState(CmsActionOrderSimple aos, OpsActionState newState) {

        aos.setActionState(newState);
        try {
            if (newState.getName().equalsIgnoreCase(COMPLETE)) {
                CmsActionOrder ao = cmsUtil.custSimple2ActionOrder(aos);
                opsProcedureProcessor.completeActionOrder(ao);
            } else {
                updateAoWithState(aos.getProcedureId(), aos.getActionId(), newState);
            }
            logger.info("Client: put:ops action id " + aos.getActionId() + " to state " + newState);
        } catch (CmsBaseException rce) {
            logger.error("failed while updating action order " + aos.getActionId() + " state with " + newState + " procedure : " + aos.getProcedureId(), rce);
            CmsOpsAction action = new CmsOpsAction();
            action.setActionId(aos.getActionId());
            action.setProcedureId(aos.getProcedureId());
            action.setActionState(failed);
            opsProcedureProcessor.updateOpsAction(action);
        }
    }

    public void failActionOrder(CmsActionOrder ao) {
        updateAoWithState(ao.getProcedureId(), ao.getActionId(), failed);
    }

    private void updateAoWithState(long procedureId, long actionId, OpsActionState state) {
        CmsOpsAction action = new CmsOpsAction();
        action.setProcedureId(procedureId);
        action.setActionId(actionId);
        action.setActionState(state);
        opsProcedureProcessor.updateOpsAction(action);
    }

    /**
     * Gets the env4 release.
     *
     * @param exec the exec
     * @return the env4 release
     * @throws GeneralSecurityException the general security exception
     */
    public void getEnv4Release(DelegateExecution exec) throws GeneralSecurityException {
        CmsRelease release = (CmsRelease) exec.getVariable("release");

        String[] nsParts = release.getNsPath().split("/");
        if (nsParts.length < NSPARTS_SIZE_MIN) {
            return;
        }
        String envNsPath = "/" + nsParts[1] + "/" + nsParts[2];
        String envName = nsParts[NSPATH_ENV_ELEM_NO];

        //CmsCISimple[] envs = retryTemplate.execute(retryContext -> restTemplate.getForObject(serviceUrl + "/cm/simple/cis?ciClassName=manifest.Environment&nsPath={envNsPath}&ciName={envName}", CmsCISimple[].class, envNsPath, envName));

        List<CmsCI> envs = cmsCmProcessor.getCiBy3(envNsPath, "manifest.Environment", envName);

        CmsCISimple env = null;
        if (envs.size() > 0) {
            env = cmsUtil.custCI2CISimple(envs.get(0), "df") ;
            logger.info("Got env for release : " + env.getCiName());
            exec.setVariable("env", env);
            if (env.getCiAttributes().get("dpmtdelay") != null) {
                exec.setVariable("delay", "PT" + env.getCiAttributes().get("dpmtdelay") + "S");
            } else {
                exec.setVariable("delay", "PT60S");
            }
        } else {
            logger.info("Can not figure out env for release : " + release.getReleaseId());
        }
    }

    /**
     * Commit and deploy release.
     *
     * @param exec the exec
     * @throws GeneralSecurityException the general security exception
     */
    public void commitAndDeployRelease(DelegateExecution exec) throws GeneralSecurityException {
        CmsRelease release = (CmsRelease) exec.getVariable("release");
        CmsCISimple env = (CmsCISimple) exec.getVariable("env");
        logger.info("Committing and deploying manifest release with id = " + release.getReleaseId());
        Map<String, String> descMap = new HashMap<String, String>();
        descMap.put("description", "oneops autodeploy");
        try {
            @SuppressWarnings("unchecked")
            Map<String, Long> bomDpmtMap = retryTemplate.execute(retryContext -> restTemplate.postForObject(transUrl + "environments/{envId}/deployments/deploy", descMap, Map.class, env.getCiId()));
            logger.info("BOM deployment id = " + bomDpmtMap.get("deploymentId"));
        } catch (RestClientException e) {
            //should
            logger.error("DeploymentExecution of manifest release " + release.getReleaseId() + " failed with error:\n" + e.getMessage());
        }

    }


    private void decryptWo(CmsWorkOrderSimple wo) throws GeneralSecurityException {
        decryptRfc(wo.getRfcCi(), true);
        if (wo.getServices() != null) {
            for (Entry<String, Map<String, CmsCISimple>> serviceEntry : wo.getServices().entrySet()) {
                for (Entry<String, CmsCISimple> cloudEntry : serviceEntry.getValue().entrySet()) {
                    decryptCI(cloudEntry.getValue());
                }
            }
        }
        Map<String, List<CmsRfcCISimple>> payLoad = wo.getPayLoad();
        if (payLoad != null) {
            for (Map.Entry<String, List<CmsRfcCISimple>> entriesInMap : payLoad.entrySet()) {
                List<CmsRfcCISimple> theListOfCmsRfcCISimple = entriesInMap.getValue();
                for (CmsRfcCISimple cmsRfcCISimple : theListOfCmsRfcCISimple) {
                    decryptRfc(cmsRfcCISimple, false);
                }
            }
        }
    }

    private void decryptAo(CmsActionOrderSimple ao) throws GeneralSecurityException {
        decryptCI(ao.getCi());
        if (ao.getServices() != null) {
            for (Entry<String, Map<String, CmsCISimple>> serviceEntry : ao.getServices().entrySet()) {
                for (Entry<String, CmsCISimple> cloudEntry : serviceEntry.getValue().entrySet()) {
                    decryptCI(cloudEntry.getValue());
                }
            }
        }

        Map<String, List<CmsCISimple>> payLoad = ao.getPayLoad();
        if (payLoad != null) {
            for (String key : payLoad.keySet()) {
                for (CmsCISimple ci : payLoad.get(key)) {
                    decryptCI(ci);
                }
            }
        }

    }

    private void decryptCI(CmsCISimple ci) throws GeneralSecurityException {
        for (String attrName : ci.getCiAttributes().keySet()) {
            String val = ci.getCiAttributes().get(attrName);
            if (val != null) {
                try {
                    if (val.startsWith(CmsCrypto.ENC_PREFIX)) {
                        ci.getCiAttributes().put(attrName, cmsCrypto.decrypt(val));
                        ci.addAttrProps(CmsConstants.SECURED_ATTRIBUTE, attrName, "true");
                    } else if (val.contains(CmsCrypto.ENC_VAR_PREFIX)) {
                        ci.getCiAttributes().put(attrName, cmsCrypto.decryptVars(val));
                        ci.addAttrProps(CmsConstants.SECURED_ATTRIBUTE, attrName, "true");
                    }
                } catch (GeneralSecurityException ce) {
                    logger.error("Error decrypting attribute " + attrName + "; value - " + val + "\n"
                            + "ci:" + gson.toJson(ci));
                    throw ce;
                }
            }
        }
    }

    private void decryptRfc(CmsRfcCISimple rfc, boolean keepEncVarsValues) throws GeneralSecurityException {
        for (String attrName : rfc.getCiAttributes().keySet()) {
            String val = rfc.getCiAttributes().get(attrName);
            if (val != null) {
                try {
                    if (val.startsWith(CmsCrypto.ENC_PREFIX)) {
                        rfc.getCiAttributes().put(attrName, cmsCrypto.decrypt(val));
                        rfc.addCiAttrProp(CmsConstants.SECURED_ATTRIBUTE, attrName, "true");
                    } else if (val.contains(CmsCrypto.ENC_VAR_PREFIX)) {
                        rfc.getCiAttributes().put(attrName, cmsCrypto.decryptVars(val));
                        rfc.addCiAttrProp(CmsConstants.SECURED_ATTRIBUTE, attrName, "true");
                        if (keepEncVarsValues) {
                            rfc.addCiAttrProp(CmsConstants.ENCRYPTED_ATTR_VALUE, attrName, val);
                        }
                    }
                } catch (GeneralSecurityException ce) {
                    logger.error("Error decrypting attribute " + attrName + "; value - " + val + "\n"
                            + "rfc:" + gson.toJson(rfc));
                    throw ce;
                }
            }
        }
        //Now decrypt ciBaseAttributes
        for (String attrName : rfc.getCiBaseAttributes().keySet()) {
            String val = rfc.getCiBaseAttributes().get(attrName);
            if (val != null) {
                try {
                    if (val.startsWith(CmsCrypto.ENC_PREFIX)) {
                        rfc.getCiBaseAttributes().put(attrName, cmsCrypto.decrypt(val));
			rfc.addCiAttrProp(CmsConstants.SECURED_ATTRIBUTE, attrName, "true");
                    } else if (val.contains(CmsCrypto.ENC_VAR_PREFIX)) {
                        rfc.getCiBaseAttributes().put(attrName, cmsCrypto.decryptVars(val));
			rfc.addCiAttrProp(CmsConstants.SECURED_ATTRIBUTE, attrName, "true");
                    }
                } catch (GeneralSecurityException ce) {
                    logger.error("Error decrypting ciBaseAttribute " + attrName + "; value - " + val + "\n"
                            + "rfc:" + gson.toJson(rfc));
                    throw ce;
                }
            }
        }
    }


    private void setOrCreateLocalVar(DelegateExecution exec, String name, Object value) {
        if (exec.getVariableNamesLocal().contains(name)) {
            exec.setVariableLocal(name, value);
        } else {
            exec.createVariableLocal(name, value);
        }
    }


    public void setDeploymentNotifier(DeploymentNotifier deploymentNotifier) {
        this.deploymentNotifier = deploymentNotifier;
    }


    @Bean
    protected RetryTemplate getRetryTemplate(@Value("${controller.retryCount:3}") int retryCount, @Value("${controller.intial_delay:1000}") int initialDelay, @Value("${controller.maxInterval:10000}") int maxInterval) {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(retryCount, Collections.singletonMap(RestClientException.class, true)));
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialDelay);
        backOffPolicy.setMaxInterval(maxInterval);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setThrowLastExceptionOnExhausted(true);


        retryTemplate.registerListener(new DefaultListenerSupport());

        return retryTemplate;
    }


    private static class DefaultListenerSupport extends RetryListenerSupport {
        @Override
        public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            logger.info("Remote call failed, Will retry (count = " + context.getRetryCount()+" ) exception :" +throwable.getClass().getSimpleName());
            super.onError(context, callback, throwable);
        }

        @Override
        public <T, E extends Throwable> void close(final RetryContext context, final RetryCallback<T, E> callback, final Throwable throwable) {
            if (throwable != null) {
                logger.info("Final  retry attempt failed,  ", throwable);
            }
        }

    }

    public boolean getVarByMatchingCriteriaBoolean(String varNameLike, String criteria) {
        List<CmsVar> vars = cmsCmProcessor.getCmVarByLongestMatchingCriteria(varNameLike, criteria);
        if (vars != null && !vars.isEmpty()) {
            CmsVar var = vars.get(0);
            return Boolean.valueOf(var.getValue());
        }
        return false;
    }

    public boolean isDeployerStepsInLimit(int deploymentStepsLimit, long deploymentId) {
        List<Integer> stepsTotal = cmsDpmtProcessor.getDeploymentDistinctStepsTotal(deploymentId);
        if(stepsTotal == null || (stepsTotal !=null && stepsTotal.size() == 0)){
            return false;
        }
        for(Integer st: stepsTotal){
            if(st > deploymentStepsLimit){
                return false;
            }
        }
        return true;
    }
}

