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
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.opamp.exceptions.OpampException;
import com.oneops.ops.CiOpsProcessor;
import com.oneops.ops.events.CiChangeStateEvent;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** FlexStateProcessor
 */
public class FlexStateProcessor {

	private static Logger logger = Logger.getLogger(FlexStateProcessor.class);
	
	private CmsCmProcessor cmProcessor;
	private EnvPropsProcessor envProcessor;
	private CiOpsProcessor coProcessor;
	private Notifications notifier;
	private RestTemplate restTemplate;
    private String transistorUrl;
    public static final String CI_STATE_OVERUTILIZED = "overutilized";
    public static final String CI_STATE_UNDERUTILIZED = "underutilized";
	//private Set<Long> postponedCis = Collections.synchronizedSet(new HashSet<Long>());
	
	/**
	 * Sets the cm processor.
	 *
	 * @param cmProcessor the new cm processor
	 */
	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
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
	 * Sets the co processor.
	 *
	 * @param coProcessor the new co processor
	 */
	public void setCoProcessor(CiOpsProcessor coProcessor) {
		this.coProcessor = coProcessor;
	}
	
	/**
	 * Sets the notifier.
	 *
	 * @param notifier the new notifier
	 */
	public void setNotifier(Notifications notifier) {
		this.notifier = notifier;
	}
		
	/**
	 * Sets the rest template.
	 *
	 * @param restTemplate the new rest template
	 */
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	/**
	 * Sets the transistor url.
	 *
	 * @param transistorUrl the new transistor url
	 */
	public void setTransistorUrl(String transistorUrl) {
		this.transistorUrl = transistorUrl;
	}

	/**
	 * Process overutilized.
	 *
	 * @param event
	 * @throws OpampException the opamp exception
	 */
	public void processOverutilized(CiChangeStateEvent event, boolean isNewState) throws OpampException{
		long ciId = event.getCiId();
		if (CI_STATE_OVERUTILIZED.equals(coProcessor.getCIstate(ciId))) {
			CmsCI env;
			if (envProcessor.isAutoscaleEnabled(ciId) && (env = envProcessor.getEnv4Bom(ciId)) != null) {
				if (envProcessor.isCloudActive4Bom(ciId)) {
					growPool(event, env, isNewState);
				} else {
					notifier.sendFlexNotificationInactiveCloud(event, CI_STATE_OVERUTILIZED);
				}
			} else {
				notifier.sendFlexNotificationNoRepair(event, CI_STATE_OVERUTILIZED);
			}
		} else {
			// ci is already healthy
			logger.info("Ci is good now - " + ciId);
		}
	}

	/**
	 * Process underutilized.
	 *
	 * @throws OpampException the opamp exception
	 */
	public void processUnderutilized(CiChangeStateEvent event, boolean isNewState, long originalEventTimestamp) throws OpampException{
		long ciId = event.getCiId();
		//check if it still needs resize
		if ("underutilized".equals(coProcessor.getCIstate(ciId))) {
			CmsCI env;
			if (envProcessor.isAutoscaleEnabled(ciId) && (env = envProcessor.getEnv4Bom(ciId))!=null){
				if (envProcessor.isCloudActive4Bom(ciId)) {
					shrinkPool(event, env, isNewState, originalEventTimestamp);
				} else {
					notifier.sendFlexNotificationInactiveCloud(event, "underutilized");
				}
			} else {
				notifier.sendFlexNotificationNoRepair(event, "underutilized");
			}
			
		} else {
			// ci is already healthy
			logger.info("Ci is good now - " + ciId);
		}
	}
	
	
	private void growPool(CiChangeStateEvent event, CmsCI env, boolean isNewState) throws OpampException{
		long ciId = event.getCiId();
		if (! envProcessor.isOpenRelease4Env(env)) {
			//first lets get manifest compute so we can get flex relation
			long manifestCompId = findManifestComputeId(ciId);
			if (manifestCompId > 0) {
				CmsCIRelation flexRel = findFlexRelation(manifestCompId);
				if (flexRel != null) {
					if ("100".equals(flexRel.getAttribute("pct_dpmt").getDjValue())) {
						int current = Integer.valueOf(flexRel.getAttribute("current").getDjValue());
						int max = Integer.valueOf(flexRel.getAttribute("max").getDjValue());
						int step = 1;
						if (flexRel.getAttribute("step_up") != null) {
							step = Integer.valueOf(flexRel.getAttribute("step_up").getDjValue());
						}
						if (current<max) {
							step = (max-current >= step) ? step : max-current; 
							processFlexRelation(flexRel, env, step, true);
							notifier.sendFlexNotificationProcessing(event, CI_STATE_OVERUTILIZED, step);
						} else {
							notifier.sendFlexNotificationLimitIsReached(event, CI_STATE_OVERUTILIZED);
							logger.info("Max pool size reached for ci - " + ciId);
						}
					} else {
						String errText = "The platform is in partually deployed state for ci - " + ciId; 
						logger.error(errText);
						notifier.sendFlexNotificationErrorProcessing(event, CI_STATE_OVERUTILIZED, errText);
					}
				} else {
					String errText = "Can not get felx realtion for ci - " + ciId; 
					logger.error(errText);
					notifier.sendFlexNotificationErrorProcessing(event, CI_STATE_OVERUTILIZED, errText);
				}
			} else {
				String errText = "Can not get manifest Compute for ci - " + ciId;
				logger.error(errText);
				notifier.sendFlexNotificationErrorProcessing(event, CI_STATE_OVERUTILIZED, errText);
			}
			// at this point ci either processed or there is an critical error
			//postponedCis.remove(ciId);
		} else {
			if (isNewState) { 
				notifier.sendFlexNotificationPostponeProcessing(event, CI_STATE_OVERUTILIZED);
			}
			throw new OpampException("There is an open release for the env - " + env.getCiName());
		}
	}
	
	private void shrinkPool(CiChangeStateEvent event, CmsCI env, boolean isNewState, long originalEventTimestamp) throws OpampException{
		long ciId = event.getCiId();
		//String state = "underutilized";
		if (! envProcessor.isOpenRelease4Env(env)) {
			//first lets get manifest compute so we can get flex relation
			long manifestCompId = findManifestComputeId(ciId);
			
			if (manifestCompId > 0) {
				//if there is any pool member that is overutilized we postpone shrinking
				if (isAnyPoolMemberOver(event, manifestCompId, CI_STATE_UNDERUTILIZED)) {
					throw new OpampException("There is an overutilized pool member for ciId - " + ciId);
				}
				CmsCIRelation flexRel = findFlexRelation(manifestCompId);
				if (flexRel != null) {
					if ("100".equals(flexRel.getAttribute("pct_dpmt").getDjValue())) {
						int current = Integer.valueOf(flexRel.getAttribute("current").getDjValue());
						int min = Integer.valueOf(flexRel.getAttribute("min").getDjValue());
						int step = 1;
	
						if (flexRel.getAttribute("step_down") != null) {
							step = Integer.valueOf(flexRel.getAttribute("step_down").getDjValue());
						}
						if (current>min) {
							step = (current-min >= step) ? step : current-min; 
							processFlexRelation(flexRel, env, step, false);
							notifier.sendFlexNotificationProcessing(event, CI_STATE_UNDERUTILIZED, step);
						} else {
							if (isNewState || envProcessor.isFirstAfterBomReleaseClosed(env, originalEventTimestamp, 
									notifier.getEventUtil().getOpsEvent(event).getCoolOff())) {
								notifier.sendFlexNotificationLimitIsReached(event, CI_STATE_UNDERUTILIZED);
							}
							logger.info("Min pool size reached for ci - " + ciId);
						}
					} else {
						String errText = "The platform is in partually deployed state for ci - " + ciId; 
						logger.error(errText);
						notifier.sendFlexNotificationErrorProcessing(event, CI_STATE_UNDERUTILIZED, errText);
					}
				} else {
					String errText = "Can not get felx realtion for ci - " + ciId; 
					logger.error(errText);
					notifier.sendFlexNotificationErrorProcessing(event, CI_STATE_UNDERUTILIZED, errText);

				}
			} else {
				String errText = "Can not get manifest Compute for ci - " + ciId; 
				logger.error(errText);
				notifier.sendFlexNotificationErrorProcessing(event, CI_STATE_UNDERUTILIZED, errText);

			}
			// at this point ci either processed or there is an critical error
			//postponedCis.remove(ciId);
		} else {
			if (isNewState) { 
				notifier.sendFlexNotificationPostponeProcessing(event, CI_STATE_UNDERUTILIZED);
			}
			throw new OpampException("There is an open release for the env - " + env.getCiName());
		}
	}

	private boolean isAnyPoolMemberOver(CiChangeStateEvent event, long manifestCompId, String state) {
		long ciId = event.getCiId();
		if (isAnyPoolMemberOverUtil(manifestCompId)) {
			return true;
		}
		
		List<CmsCIRelation> realizedRels = cmProcessor.getToCIRelationsNaked(ciId, "base.RealizedAs", null);
		if (realizedRels.size()>0) {
			long manifestId = realizedRels.get(0).getFromCiId();
			if (manifestId == manifestCompId) {
				return false;
			} else {
				return isAnyPoolMemberOverUtil(manifestId);
			}
		} else {
			String errText = "Can not get manifest id for ci - " + ciId; 
			logger.error(errText);
			notifier.sendFlexNotificationErrorProcessing(event, state, errText);
		}
		return false;
	}
	
	private boolean isAnyPoolMemberOverUtil(long manifestId) {
		List<CmsCIRelation> bomRels = cmProcessor.getFromCIRelationsNaked(manifestId, "base.RealizedAs", null);
		List<Long> bomCiIds = new ArrayList<Long>();
		for (CmsCIRelation rel : bomRels) {
			bomCiIds.add(rel.getToCiId());
		}
		if (bomCiIds.size() > 0) {
			Map<Long, String> ciStates = coProcessor.getCisStates(bomCiIds);
			for (String state : ciStates.values()) {
				if ("overutilized".equals(state)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void processFlexRelation(CmsCIRelation flexRel, CmsCI env, int step, boolean scaleUp) throws OpampException {
		try {
			//now we need to call transistor and create deployment;
			Map<String,String> params = new HashMap<String,String>();
			params.put("envId", String.valueOf(env.getCiId()));
			params.put("relId", String.valueOf(flexRel.getCiRelationId()));
			params.put("step", String.valueOf(step));
			params.put("scaleUp", String.valueOf(scaleUp));
			Long bomReleaseId = restTemplate.getForObject(transistorUrl + "flex?envId={envId}&relId={relId}&step={step}&scaleUp={scaleUp}", Long.class, params);
			logger.info("created new bom release - " + bomReleaseId);
		
		} catch (RestClientException e) {
			logger.error("RestClientException in processFlexRelation", e);
			throw new OpampException(e);
		}
	}
		
	private long findManifestComputeId(long ciId) {
		CmsCI ci = cmProcessor.getCiById(ciId);
		long bomComputeId = 0;
		if (ci.getCiClassName().endsWith(".Compute")) {
			bomComputeId = ci.getCiId();
		} else {
			List<CmsCIRelation> bomManagedViaRels = cmProcessor.getFromCIRelations(ciId, "bom.ManagedVia", null);
			if (bomManagedViaRels.size()>0) {
				CmsCI managedViaCI = bomManagedViaRels.get(0).getToCi();
				if (managedViaCI.getCiClassName().endsWith(".Compute")) {
					bomComputeId =  managedViaCI.getCiId();
				} else {
					return 0;
				}
			} else {
				return 0;
			}
		}
		
		List<CmsCIRelation> realizedAsRels = cmProcessor.getToCIRelationsNaked(bomComputeId, "base.RealizedAs", null);
		if (realizedAsRels.size()>0) {
			return realizedAsRels.get(0).getFromCiId();
		} else {
			return 0;
		}

	}
	
	private CmsCIRelation findFlexRelation(long ciId) {
		List<CmsCIRelation> dependsOnRels = cmProcessor.getToCIRelationsNaked(ciId, "manifest.DependsOn", null);
		for (CmsCIRelation rel : dependsOnRels) {
			if ("true".equals(rel.getAttribute("flex").getDjValue())) {
				return rel;
			} else {
				CmsCIRelation nextFlex = findFlexRelation(rel.getFromCiId());
				if (nextFlex != null) {
					return nextFlex;
				}
			}
		}
		return null;
	}

}
