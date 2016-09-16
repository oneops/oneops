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
package com.oneops.opamp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsFlowAction;
import com.oneops.cms.cm.ops.domain.OpsProcedureDefinition;
import com.oneops.cms.cm.ops.domain.OpsProcedureFlow;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.cm.ops.service.OpsManager;
import com.oneops.cms.cm.ops.service.OpsProcedureProcessor;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.exceptions.OpsException;
import com.oneops.opamp.exceptions.OpampException;
import com.oneops.opamp.util.EventUtil;
import com.oneops.ops.CiOpsProcessor;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.ops.events.CiOpenEvent;

/**
 * Class with behavior for unhealthy and repair
 * operations
 */
public class BadStateProcessor {

    private static final int DEFAULT_MIN_REPAIRS_BEFORE_REPLACE = 9999999;
	private static final int DEFAULT_UNHEALTHY_TIME_BEFORE_REPLACE = 9999999;
    protected static final String X_CMS_USER = "X-Cms-User";
    protected static final String ONEOPS_AUTO_REPLACE_USER_PROP_NAME = "oneops-auto-replace-user";
    protected static final String ONEOPS_AUTOREPLACE_USER = System.getProperty(ONEOPS_AUTO_REPLACE_USER_PROP_NAME,"oneops-autoreplace");
	private static final String CI_OPS_STATE_UNHEALTHY = "unhealthy";

    private static Logger logger = Logger.getLogger(BadStateProcessor.class);
	
	private CmsCmProcessor cmProcessor;
	private EnvPropsProcessor envProcessor;
	private CiOpsProcessor coProcessor;
	private CmsCmManager cmManager;
	private OpsManager opsManager;
	private OpsProcedureProcessor opsProcProcessor;
	private RestTemplate restTemplate;
    private String transistorUrl;
	private Notifications notifier;
	private Set<Long> postponedRepairCi = Collections.synchronizedSet(new HashSet<Long>());
	private EventUtil eventUtil;

	/**
	 * Sets the ops proc processor.
	 *
	 * @param opsProcProcessor the new ops proc processor
	 */
	public void setOpsProcProcessor(OpsProcedureProcessor opsProcProcessor) {
		this.opsProcProcessor = opsProcProcessor;
	}
	
	/**
	 * Sets the co processor.
	 *
	 * @param coProcessor the new co processor
	 */
	public void setCoProcessor(CiOpsProcessor coProcessor) {
		this.coProcessor = coProcessor;
	}

	/**
	 * Sets the env processor.
	 *
	 * @param envProcessor the new env processor
	 */
	public void setEnvProcessor(EnvPropsProcessor envProcessor) {
		this.envProcessor = envProcessor;
	}

	/**
	 * Sets the cm processor.
	 *
	 * @param cmProcessor the new cm processor
	 */
	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	public OpsManager getOpsManager() {
		return opsManager;
	}

	public void setOpsManager(OpsManager opsManager) {
		this.opsManager = opsManager;
	}

	public CmsCmManager getCmManager() {
		return cmManager;
	}

	public void setCmManager(CmsCmManager cmManager) {
		this.cmManager = cmManager;
	}

	/**
	 * Sets the notifier.
	 *
	 * @param notifier the new notifier
	 */
	public void setNotifier(Notifications notifier) {
		this.notifier = notifier;
	}

	public RestTemplate getRestTemplate() {

        return restTemplate;
	}


    public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public String getTransistorUrl() {
		return transistorUrl;
	}

	public void setTransistorUrl(String transistorUrl) {
		this.transistorUrl = transistorUrl;
	}


	/**
	 * Process unhealthy state.
	 *
	 * @param ciId the ci id
	 * @param event 
	 * @throws OpampException 
	 */
	public void processUnhealthyState(CiChangeStateEvent event) throws OpampException {
		long ciId = event.getCiId();
		String ciOpsState = coProcessor.getCIstate(ciId);
		if (!CI_OPS_STATE_UNHEALTHY.equalsIgnoreCase(ciOpsState)) {
			logger.info("CmsCi id - " + ciId + " already good.");
			return;
		}
		if (envProcessor.isAutorepairEnbaled4bom(ciId) != null) {
			List<CmsCIRelation> deployedToRels = envProcessor.fetchDeployedToRelations(ciId);
			if(envProcessor.isOpAmpSuspendedForCloud(deployedToRels)){
				return;
			}
			if (envProcessor.isCloudActive4Bom(ciId,deployedToRels)) {
				repairBad(event);
			} else {
				// seems like the cloud is not in active state, we need to skip
				// autorepair just send notification
				if (eventUtil.shouldNotify(event)) {
					notifier.sendUnhealthyNotificationInactiveCloud(event);
				}
			}
		} else {
			notifier.sendUnhealthyNotificationNoRepair(event);

		}
	}
	
	private void repairBad(CiChangeStateEvent event) throws OpampException {
		long ciId = event.getCiId();
		if (isDependsOnGood(ciId)) {
			CmsCI env = envProcessor.isAutoReplaceEnbaled4bom(ciId);
			if (env != null  && timeToAutoReplace(ciId, env)) {
				if (envProcessor.isOpenRelease4Env(env)) {
					logger.info("There is an open release or undeployed changes for the env => " 
				+ env.getNsPath() + "/" + env.getCiName()+ ". Can not auto-replace.");	
					notifier.sendPostponedReplaceNotification(event);
					submitRepairProcedure(event);
				} else {
					logger.info("ciId: [" + ciId + "] is being auto-replaced");
					notifier.sendReplaceNotification(event);
					replace(ciId, env);
				}
			} else {
				//we are clear to create repair procedure
				submitRepairProcedure(event);
			}
		} else {
				notifier.sendDependsOnUnhealthyNotification(event);
		}
	}

	private boolean timeToAutoReplace(long ciId, CmsCI env) {
		//Now lets find the first unhealthy "open" event
		long unhealthyStartTime = getUnhealthyStartTime(ciId);
		
		if (unhealthyStartTime == 0) {
			logger.info("ci id " + ciId + " does not have any open unhealthy event. It will not be replaced");
			return false;
		} 

		CmsCI platform = envProcessor.getPlatform4Bom(ciId);
		int repairRetries = getMinNumberOfRepairs(platform);
		int replaceAfterMins = getReplaceAfterMins(platform);

		if (platform != null) {
			if ((System.currentTimeMillis() - unhealthyStartTime) < (replaceAfterMins * 60 * 1000L)) {
				//unhealthy for not long enough yet
				logger.info("ci " + ciId + " is unhealthy but for less than " + replaceAfterMins 
						+ " minutes. Not triggering replace. Env => " + env.getNsPath() + "/" + env.getCiName());
				return false;
			}
			
			List<CmsOpsProcedure> opsProcedures = opsManager.getCmsOpsProcedureForCi(ciId, Collections.singletonList(OpsProcedureState.complete), "ci_repair", repairRetries);
			opsProcedures.addAll(opsManager.getCmsOpsProcedureForCi(ciId, Collections.singletonList(OpsProcedureState.failed), "ci_repair", repairRetries));
			// this list is already ordered by procedureId descending
			int totalRepairs = 0;
			if (opsProcedures != null && opsProcedures.size() >= repairRetries) {
				for (CmsOpsProcedure proc : opsProcedures) {
					if (proc.getCreated().getTime() > unhealthyStartTime) {
						totalRepairs++;
					}
				}
			} 
			if (totalRepairs >= repairRetries) {
				return true;
			}
		}
		return false;
	}

	private long getUnhealthyStartTime(long ciId) {
		ArrayList<Long> param = new ArrayList<Long>();
		param.add(ciId);
		Map<Long, List<CiOpenEvent>> openEvents = coProcessor.getCisOpenEvents(param);
		if (openEvents == null || openEvents.get(ciId) == null) {
			return 0;
		}
		//Now lets find the first unhealthy "open" event
		long unhealthyStartTime = 0;
		for (CiOpenEvent openEvent : openEvents.get(ciId)) {
			if (openEvent.getState().equals(CI_OPS_STATE_UNHEALTHY)) {
				if (unhealthyStartTime == 0 || openEvent.getTimestamp() < unhealthyStartTime) {
					unhealthyStartTime = openEvent.getTimestamp();
				}
			}
		}
		return unhealthyStartTime;
	}

	private int getReplaceAfterMins(CmsCI platform) {
		if (platform != null) {
			CmsCIAttribute attribute = platform.getAttribute("replace_after_repairs");			
			attribute = platform.getAttribute("replace_after_minutes");			
			if (attribute != null) {
				String value = attribute.getDfValue();
				if (value != null && value.trim().length() > 0) {
					return Integer.parseInt(value);
				}
			}
		}
		return DEFAULT_UNHEALTHY_TIME_BEFORE_REPLACE;
	}

	private int getMinNumberOfRepairs(CmsCI platform) {
		if (platform != null) {
			CmsCIAttribute attribute = platform.getAttribute("replace_after_repairs");			
			if (attribute != null) {
				String value = attribute.getDfValue();
				if (value != null && value.trim().length() > 0) {
					return Integer.parseInt(value);
				}
			}
		}
		return DEFAULT_MIN_REPAIRS_BEFORE_REPLACE;
	}

	private void replace(long ciId, CmsCI env) throws OpampException {
		try {
			// first mark the ci state as "replace"
			cmManager.updateCiState(ciId, "replace", "bom.ManagedVia", "to",
					false, ONEOPS_AUTOREPLACE_USER);
            logger.info("marked the ciId [" + ciId + "] for replace using headers using user"+ONEOPS_AUTOREPLACE_USER);
			// now submit the deployment
			Map<String, String> params = new HashMap<String, String>();
			params.put("envId", String.valueOf(env.getCiId()));



			Map<String, String> request = new HashMap<String, String>();
			request.put("description", "Auto-Replace by OneOps ["+env.getNsPath()+"]");
			
			
			CmsCI platformOfBomCi = envProcessor.getPlatform4Bom(ciId);
			List<CmsCI> platformsOfEnv =  envProcessor.getPlatformsForEnv(env.getCiId());
			if (platformsOfEnv.size() > 1) {
				StringBuffer excludePlatforms = new StringBuffer();
				for (CmsCI platform : platformsOfEnv) {
					if (platform.getCiId() != platformOfBomCi.getCiId()) {
						if (excludePlatforms.length() > 0) excludePlatforms.append(","); 
						excludePlatforms.append(platform.getCiId());
					}
				}
				request.put("exclude", excludePlatforms.toString());				
			}
            //TODO move it to the bean
            HttpHeaders headers = new HttpHeaders();
            headers.set(X_CMS_USER, ONEOPS_AUTOREPLACE_USER);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestWitHeaders = new HttpEntity<Map<String,String>>(request, headers);

            @SuppressWarnings("unchecked")
			Map<String, Integer> response = restTemplate
					.postForObject(
							transistorUrl + "environments/" + env.getCiId()
									+ "/deployments/deploy", requestWitHeaders,
							Map.class, params);
			Integer exitCode = response.get("deploymentId");
			if (exitCode != null && exitCode == 0) {
				logger.info("auto-replace deployment submitted successfully by opamp. Env: "
						+ env.getNsPath() + "/" + env.getCiName() + " Env ciId: " + env.getCiId());
			} else {
				logger.error("Transistor returned non-zero response for auto-replace deployment. Env: "
						+ env.getNsPath() + "/" + env.getCiName() + + env.getCiId());
				throw new OpampException(
						"Auto-Replace Could not be submitted. Transistor threw error. Env - "
								+ env.getNsPath() + "/" + env.getCiName());
			}
		} catch (RestClientException e) {
			logger.error("Error while submitting auto-replace deployment to transistor", e);
			throw new OpampException(e);
		} catch (CIValidationException cive) {
			logger.error("Error updating ci state to replace, ci_id = " + ciId, cive);
			throw new OpampException(cive);
		}
	}

	/**
	 * Process good state.
	 *
	 * @param ciId the ci id
	 * @param event 
	 */
	public void processGoodState( CiChangeStateEvent event) {
		notifier.sendOpsEventNotification(event);
	}
	
	private boolean isDependsOnGood(long ciId) {
		
		List<CmsCIRelation> dependsOnRels = cmProcessor.getFromCIRelationsNakedNoAttrs(ciId, null, "DependsOn",null);
		if (dependsOnRels.size()==0) {
			return true;
		} else {
			for (CmsCIRelation rel : dependsOnRels) {
				String ciOpsState = coProcessor.getCIstate(rel.getToCiId());
				if (CI_OPS_STATE_UNHEALTHY.equalsIgnoreCase(ciOpsState)) {
					return false;
				} else {
					return isDependsOnGood(rel.getToCiId());
				}
			}
		}
		return false;
	}
	
	/*
	private List<Long> getBadDependents(long ciId) {
		
		List<CmsCIRelation> dependsOnRels = cmProcessor.getToCIRelationsNakedNoAttrs(ciId, null, "DependsOn",null);
		List<Long> badDependents = new ArrayList<Long>();
		if (dependsOnRels.size() >0) {
			for (CmsCIRelation rel : dependsOnRels) {
				String ciOpsState = coProcessor.getCIstate(rel.getFromCiId());
				if ("unhealthy".equalsIgnoreCase(ciOpsState)) {
					badDependents.add(rel.getFromCiId());
				} else {
					badDependents.addAll(getBadDependents(rel.getFromCiId()));
				}
			}
		}	
		return badDependents;
	}
	*/
	
	/**
	 * Submit repair procedure.
	 *
	 * @param ciId the ci id
	 * @param opsEvent 
	 * @param shouldSendOpsNotification 
	 * @throws OpampException 
	 */
	public void submitRepairProcedure(CiChangeStateEvent event) throws OpampException {
		long ciId = event.getCiId(); 
		OpsProcedureDefinition procDef = new OpsProcedureDefinition();
		OpsFlowAction actionDef = new OpsFlowAction();
		actionDef.setActionName("repair");
		actionDef.setIsCritical(true);
		actionDef.setStepNumber(1);
		List<OpsFlowAction> actions = new ArrayList<OpsFlowAction>();
		actions.add(actionDef);
		procDef.setActions(actions);
		procDef.setName("ci_repair");
		procDef.setFlow(new ArrayList<OpsProcedureFlow>());
		
		CmsOpsProcedure procRequest = new CmsOpsProcedure();
		procRequest.setCiId(ciId);
		procRequest.setCreatedBy("oneops-autorepair");
		procRequest.setProcedureState(OpsProcedureState.active);
		
		if(isRepairAlreadyPostponed(ciId)) {
			String ciOpsState = coProcessor.getCIstate(ciId);
			if (!CI_OPS_STATE_UNHEALTHY.equalsIgnoreCase(ciOpsState)) {
			    logger.info( "CmsCi id - " + ciId + " already good." );
			    postponedRepairCi.remove(ciId);
			    return;
			}
		}
		long unhealthyStartTime = getUnhealthyStartTime(ciId);
		long repairRetriesCount = 0;
		Map<String, String> payloadEntries = new HashMap<String, String>();
		if (unhealthyStartTime != 0) {
			List<CmsOpsProcedure> opsProcedures = opsManager.getCmsOpsProceduresForCiFromTime(ciId, "ci_repair", new Date(unhealthyStartTime));
			if (opsProcedures != null && opsProcedures.size() > 0) {
				logger.info("CiId " + ciId +  " Unhealthy start time for the open unhealthy event in millisecond : " 
			+ unhealthyStartTime + ". Total repairs executed in this state: " + opsProcedures.size());
				repairRetriesCount = opsProcedures.size();
				payloadEntries.put("repeatCount", String.valueOf(repairRetriesCount));
			}
		}
		procRequest.setArglist(String.valueOf(repairRetriesCount));
		try {
			CmsOpsProcedure submittedProc = opsProcProcessor.processProcedureRequest(procRequest, procDef);
			//Inc
			if (repairRetriesCount >= 1) {
				 notifier.sendRepairCriticalNotification(event, payloadEntries);
			}else if(eventUtil.shouldNotify(event)){
			   notifier.sendRepairNotification(event, payloadEntries);
			}	
		    postponedRepairCi.remove(ciId);
			logger.info("Submitted Repair procedure request for ci - " + ciId + "; procedure id = " + submittedProc.getProcedureId());
		} catch(OpsException e) {
			postponedRepairCi.add(ciId);
			logger.info("Got Exception Repair procedure request for ci - " + ciId + " " +e.getMessage());

			if(eventUtil.shouldNotify(event))
				notifier.sendPostponedRepairNotification(event, payloadEntries);
			throw e;
		}
	}

	private boolean isRepairAlreadyPostponed(long ciId) {
		return postponedRepairCi.contains(ciId);
	}

	public EventUtil getEventUtil() {
		return eventUtil;
	}

	public void setEventUtil(EventUtil eventUtil) {
		this.eventUtil = eventUtil;
	}


}
