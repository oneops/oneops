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

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.ops.domain.*;
import com.oneops.cms.cm.ops.service.OpsManager;
import com.oneops.cms.cm.ops.service.OpsProcedureProcessor;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.exceptions.OpsException;
import com.oneops.opamp.exceptions.OpampException;
import com.oneops.opamp.util.EventUtil;
import com.oneops.opamp.util.IConstants;
import com.oneops.ops.CiOpsProcessor;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.ops.events.CiOpenEvent;
import com.oneops.ops.events.OpsBaseEvent;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
	private static final int DEFAULT_COOLOFF_PERIOD_MILLIS = 15 * 60 * 1000; //default to 15 mins	

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
	private EventUtil eventUtil;
	private Set<Long> postponedRepairCi = ConcurrentHashMap.newKeySet();

	//below variables are initialized through spring xml
	private int startExponentialDelayAfterProcedures = 4;
	private double exponentialBackoffFactor = 2;
	//Max Days limit of 11 makes the final repair attempt timing close to the limit of days (11 in this case) for the common cool-off of 15 mins1476110700000 
	private int maxDaysRepair = 11;

	private int maxPastDaysForProcedureCount = 15;
	private boolean checkOpsStateBeforeTriggeringRepair = true;

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
	 * @param event
	 * @throws OpampException
	 */
	public void processUnhealthyState(CiChangeStateEvent event) throws OpampException {
		long ciId = event.getCiId();

		//first check if the ci is unhealthy for very long, If yes, just return
		long unhealthyStartTime = getUnhealthyStartTime(ciId);
		long currentTimeMillis = System.currentTimeMillis();
		long unhealthySinceMillis = (currentTimeMillis - unhealthyStartTime);
		long repairRetriesMaxDaysMillis = maxDaysRepair * 24 * 60 * 60 * 1000;

		if (unhealthySinceMillis > repairRetriesMaxDaysMillis) { //unhealthy since more than "maxDaysRepair" days
			logger.info("CI " + ciId + " unhealthy since " + maxDaysRepair + " days - not doing auto-repair");
			return;
		}
		String ciOpsState = coProcessor.getCIstate(ciId);


		if(getCheckOpsStateBeforeTriggeringRepair()){
			logger.info("State computed from opsDB - " + ciOpsState);
			if (!CI_OPS_STATE_UNHEALTHY.equalsIgnoreCase(ciOpsState)) {
				logger.info("CmsCi id - " + ciId + " already good.");
				return;
			}
		}else {
			logger.info("Skipping recheck of opsState.");
		}
		if (envProcessor.isAutorepairEnabled(ciId)) {
			List<CmsCIRelation> deployedToRels = envProcessor.fetchDeployedToRelations(ciId);
			if(envProcessor.isOpAmpSuspendedForCloud(deployedToRels)){
				return;
			}
			if (envProcessor.isCloudActive4Bom(ciId,deployedToRels)) {
				repairBad(event, unhealthyStartTime);
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

	private void repairBad(CiChangeStateEvent event, long unhealthyStartTime) throws OpampException {
		long ciId = event.getCiId();

		if (isDependsOnGood(ciId)) {
			CmsCI platform = envProcessor.getPlatform4Bom(ciId);

			if (platform == null) {
				logger.error("can not get platform for ciid " + ciId);
				return;
			}

			List<OpsProcedureState> procedureFinishedStates = new ArrayList<>();
			procedureFinishedStates.add(OpsProcedureState.complete);
			procedureFinishedStates.add(OpsProcedureState.failed);

			long minCheckTime4ProcCount = System.currentTimeMillis() - (maxPastDaysForProcedureCount * 24 * 60 * 60 * 1000L);
			long proceduresCount = opsManager.getCmsOpsProceduresCountForCiFromTime(ciId, procedureFinishedStates, "ci_repair",
					new Date(Math.max(unhealthyStartTime, minCheckTime4ProcCount)));

			boolean isAutoReplaceEnabled = envProcessor.isAutoReplaceEnabled(platform);

			OpsBaseEvent opsEvent = eventUtil.getGson().fromJson(event.getPayLoad(), OpsBaseEvent.class);
			int coolOffPeriodMillis = DEFAULT_COOLOFF_PERIOD_MILLIS;
			if (opsEvent.getCoolOff() > 0) {
				coolOffPeriodMillis = opsEvent.getCoolOff() * 60 * 1000;
			}

			if (isAutoReplaceEnabled) {//Check if auto-replace config is insanely long
				int replaceAfterMins = getReplaceAfterMins(platform);
				int replaceAfterRepairs = getMinNumberOfRepairs(platform);
				isAutoReplaceEnabled = (replaceAfterMins < maxDaysRepair * 24 * 60) 
						&& (replaceAfterRepairs < (maxDaysRepair * 24 * 60 * 1000)/coolOffPeriodMillis);
			}
			
			if (isAutoReplaceEnabled && timeToAutoReplace(ciId, platform, unhealthyStartTime, proceduresCount)) {
				CmsCI env = envProcessor.getEnv4Platform(platform);
				if (envProcessor.isOpenRelease4Env(env)) {
					logger.info("There is an open release or undeployed changes for the env => "
							+   env.getNsPath() + "/" + env.getCiName()+ ". Can not auto-replace.");
					notifier.sendPostponedReplaceNotification(event);
					submitRepairProcedure(event, envProcessor.isRepairDelayEnabled(platform), unhealthyStartTime, proceduresCount, coolOffPeriodMillis) ;
				} else {
					logger.info("ciId: [" + ciId + "] is being auto-replaced");
					notifier.sendReplaceNotification(event);
					logger.info("ciId: [" + ciId + "] is being auto-replaced");
					notifier.sendReplaceNotification(event);
					String userId = IConstants.ONEOPS_AUTOREPLACE_USER;
					String description = "Auto-Replace by OneOps";
					replace(ciId, env, userId, description);
				}
			} else {
				submitRepairProcedure(event, ! isAutoReplaceEnabled && envProcessor.isRepairDelayEnabled(platform), unhealthyStartTime, proceduresCount, coolOffPeriodMillis);
			}
		} else {
			notifier.sendDependsOnUnhealthyNotification(event);
		}
	}

	private boolean timeToAutoReplace(long ciId, CmsCI platform, long unhealthyStartTime, long proceduresCount) {
		if (unhealthyStartTime == 0) {
			logger.info("ci id " + ciId + " does not have any open unhealthy event. It will not be replaced");
			return false;
		}

		int repairRetries = getMinNumberOfRepairs(platform);
		int replaceAfterMins = getReplaceAfterMins(platform);

		if (platform != null) {
			if ((System.currentTimeMillis() - unhealthyStartTime) < (replaceAfterMins * 60 * 1000L)) {
				//unhealthy for not long enough yet
				logger.info("ci " + ciId + " is unhealthy but for less than " + replaceAfterMins
						+ " minutes. Not triggering replace. Platform => " + platform.getNsPath());
				return false;
			}

			if (proceduresCount >= repairRetries) {
				return true;
			}
		}
		return false;
	}

	private long getUnhealthyStartTime(long ciId) {
		ArrayList<Long> param = new ArrayList<>();
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
			CmsCIAttribute attribute = platform.getAttribute("replace_after_minutes");
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

	public void replace(long ciId, CmsCI env, String userId, String description) throws OpampException {
		try {
			// first mark the ci state as "replace"
			cmManager.updateCiState(ciId, "replace", "bom.ManagedVia", "to", false, userId);
			logger.info("marked the ciId [" + ciId + "] for replace using headers using user" + userId);
			// now submit the deployment
			Map<String, String> params = new HashMap<>();
			params.put("envId", String.valueOf(env.getCiId()));



			Map<String, String> request = new HashMap<>();
			request.put("description", description + ", user " + userId + ", [" + env.getNsPath() + "]");


			CmsCI platformOfBomCi = envProcessor.getPlatform4Bom(ciId);
			List<CmsCI> platformsOfEnv =  envProcessor.getPlatformsForEnv(env.getCiId());
			if (platformsOfEnv.size() > 1) {
				StringBuilder excludePlatforms = new StringBuilder();
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
			headers.set(X_CMS_USER, userId);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> requestWitHeaders = new HttpEntity<>(request, headers);

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
	 * @param event
	 * @param exponentialDelay
	 * @param repairRetriesCount
	 * @throws OpampException
	 */
	public void submitRepairProcedure(CiChangeStateEvent event, boolean exponentialDelay, long unhealthyStartTime, long repairRetriesCount, long coolOffPeriodMillis) throws OpampException {
		long ciId = event.getCiId();

		logger.info("CiId " + ciId +  " Unhealthy start time for the open unhealthy event in millisecond : "
				+ unhealthyStartTime + ". Total repairs executed in this state: " + repairRetriesCount);
		if (unhealthyStartTime != 0) {
			long currentTimeMillis = System.currentTimeMillis();
			long repairRetriesMaxDaysMillis = maxDaysRepair * 24 * 60 * 60 * 1000;

			if (exponentialDelay) { //add exponential delay after initial regular interval

				if (repairRetriesCount >= startExponentialDelayAfterProcedures) {
					long delayStartTime = unhealthyStartTime + (coolOffPeriodMillis * startExponentialDelayAfterProcedures);

					long nextRepairTime = getNextRepairTime(delayStartTime, coolOffPeriodMillis, exponentialBackoffFactor,
							repairRetriesCount - startExponentialDelayAfterProcedures, repairRetriesMaxDaysMillis);

					if (currentTimeMillis < nextRepairTime) {
						//next exponential delay is not yet complete
						logger.info("Exponential back-off - Skipping the auto-repair till " + new Date(nextRepairTime));
						return;
					}
				}
			}
		}

		OpsProcedureDefinition procDef = new OpsProcedureDefinition();
		OpsFlowAction actionDef = new OpsFlowAction();
		actionDef.setActionName("repair");
		actionDef.setIsCritical(true);
		actionDef.setStepNumber(1);
		List<OpsFlowAction> actions = new ArrayList<>();
		actions.add(actionDef);
		procDef.setActions(actions);
		procDef.setName("ci_repair");
		procDef.setFlow(new ArrayList<>());

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
		Map<String, String> payloadEntries = new HashMap<>();
		payloadEntries.put("repeatCount", String.valueOf(repairRetriesCount));

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

	public static long getNextRepairTime(long delayStartTime, long coolOffPeriod, double exponentialFactor, long repairRetriesCountSinceDelay, long repairRetriesMaxPeriod) {
		long max = Math.min(repairRetriesCountSinceDelay + 1, (long) Math.ceil((Math.log(1 + repairRetriesMaxPeriod  / coolOffPeriod) / Math.log(exponentialFactor))));
		return (long) (delayStartTime + (coolOffPeriod * (Math.pow(exponentialFactor, max) - 1)));
	}

	private boolean isRepairAlreadyPostponed(long ciId) {
		return postponedRepairCi.contains(ciId);
	}

	public void processDefunctState(CiChangeStateEvent event) throws OpampException {
		CmsCI platform = envProcessor.getPlatform4Bom(event.getCiId());

		if (platform == null) {
			logger.error("can not get platform for CI id " + event.getCiId() + " while handling defunct ops state change event");
			return;
		}
		
		CmsCI env = envProcessor.getEnv4Platform(platform);
		
		if (envProcessor.isOpenRelease4Env(env)) {
			logger.info("There is an open release or undeployed changes for the env => "
					+   env.getNsPath() + "/" + env.getCiName()+ ". Can not auto-replace for defunct ci with CI Id: " + event.getCiId());
			notifier.sendPostponedReplaceNotification(event);
		} else {		
			notifier.sendDefunctNotification(event);		
			String userId = IConstants.ONEOPS_AUTOREPLACE_USER;
			String description = "Auto-Replace by OneOps";
			replace(event.getCiId(), env, userId, description);
		}
	}
	
	
	public Map<String, Integer> replaceByCid(long ciId, String XCmsUser, String description) {
		Map<String, Integer> result = new HashMap<>(1);

		CmsCI platform = envProcessor.getPlatform4Bom(ciId);
		logger.info("Platform name for ciId : " + platform.getCiName());

		CmsCI env = envProcessor.getEnv4Platform(platform);
		logger.info("Oneops environment for ciId : " + env.getCiId() + ": " + env.getCiName());

		boolean isAutoReplaceEnabledForPlatform = envProcessor.isAutoReplaceEnabled(platform);
		logger.info("isAutoReplaceEnabledForPlatform: " + isAutoReplaceEnabledForPlatform);

		if (!isAutoReplaceEnabledForPlatform) {
			logger.error("Auto Replace not enabled for CiId: " + ciId);
			result.put("deploymentId", 1);
			return result;
		}
		boolean releaseStatus = envProcessor.isOpenRelease4Env(env);
		logger.info("releaseStatus: " + releaseStatus);

		if (!releaseStatus) {
			try {
				replace(ciId, env, XCmsUser, description);
				result.put("deploymentId", 0);
				return result;
			} catch (OpampException e) {

				logger.error("Exception while processing replaceByCid for Cid: " + ciId + " :" + e);
				result.put("deploymentId", 1);
				return result;

			}

		}
		result.put("deploymentId", 1);
		return result;

	}

	
	
	public EventUtil getEventUtil() {
		return eventUtil;
	}

	public void setEventUtil(EventUtil eventUtil) {
		this.eventUtil = eventUtil;
	}

	public int getStartExponentialDelayAfterProcedures() {
		return startExponentialDelayAfterProcedures;
	}

	public void setStartExponentialDelayAfterProcedures(int startExponentialDelayAfterProcedures) {
		this.startExponentialDelayAfterProcedures = startExponentialDelayAfterProcedures;
	}

	public double getExponentialBackoffFactor() {
		return exponentialBackoffFactor;
	}

	public void setExponentialBackoffFactor(double exponentialBackoffFactor) {
		this.exponentialBackoffFactor = exponentialBackoffFactor;
	}

	public int getMaxDaysRepair() {
		return maxDaysRepair;
	}

	public void setMaxDaysRepair(int maxDaysRepair) {
		this.maxDaysRepair = maxDaysRepair;
	}

	public void setMaxPastDaysForProcedureCount(int maxPastDaysForProcedureCount) {
		this.maxPastDaysForProcedureCount = maxPastDaysForProcedureCount;
	}

	public void setCheckOpsStateBeforeTriggeringRepair(Boolean checkOpsStateBeforeTriggeringRepair) {
		this.checkOpsStateBeforeTriggeringRepair = checkOpsStateBeforeTriggeringRepair;
	}

	public boolean getCheckOpsStateBeforeTriggeringRepair() {
		return checkOpsStateBeforeTriggeringRepair;
	}
}
