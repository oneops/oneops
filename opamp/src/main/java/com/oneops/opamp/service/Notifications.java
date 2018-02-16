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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.notification.NotificationMessage;
import com.oneops.notification.NotificationSeverity;
import com.oneops.notification.NotificationType;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.opamp.util.EventUtil;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.ops.events.OpsBaseEvent;
import com.oneops.util.ReliableExecutor;

/**
 * Notifications takes care of sending events
 */
public class Notifications {
	protected static final String THRESHOLD = "threshold";

	protected static final String COLON = ":";

	protected static final String ADMINSTATUS_ATTRIBUTE_NAME = "adminstatus";

	protected static final String PROFILE_ATTRIBUTE_NAME = "profile";

	protected static final String STATE = "state";

	protected static final String CLASS_NAME = "className";

	protected static final String NEW_STATE = "newState";

	protected static final String OLD_STATE = "oldState";

	private static final String METRICS = "metrics";

	protected static final String STATUS = "status";

	protected static final String CI_NAME = "ciName";

	protected static final String EVENT_NAME = "eventName";
	
	private static final String MANAGED_VIA_IP = "IP";

	protected static final String REPAIR_IN_PROGRESS = "Starting repair";

	protected static final String NOTIFICATION_SOURCE = "ops";

	protected static final String REPAIR_POSTPONED = "Repair procedure postponed due to open release or another procedure.";

	protected static final String REPLACE_NOTIFICATION = "Could not repair, attempting to replace.";

	protected static final String WAITING_ON_DEPENDENT_REPAIR = "Waiting on dependency repair!";

	protected static final String REPAIR_NOT_PERFORMED_INACTIVE_CLOUD = "The Cloud is marked as inactive. No repair performed.";

	protected static final String REPAIR_DISABLED_IN_THIS_ENVIRONMENT = "Auto-Repair is disabled. No repair performed.";

	protected static final String REPLACE_POSTPONED_NOTIFICATION = "Auto-replace will be postponed due to an open release or an ongoing deployment";

	protected static Logger logger = Logger.getLogger(Notifications.class);

    protected static final String SUBJECT_SUFFIX_OPEN_EVENT = " is violated.";

    protected static final String SUBJECT_SUFFIX_CLOSE_EVENT = " recovered.";

	private static final String CI_IN_DEFUNCT_STATE_NOTIFICATION = "This component is in defunct state. Trying to auto-replace.";


    private ReliableExecutor<NotificationMessage> antennaClient;
	private CmsCmProcessor cmProcessor;
	private EnvPropsProcessor envProcessor;
	private EventUtil eventUtil;
    private Gson gson = new Gson();

	public NotificationMessage sendOpsEventNotification(CiChangeStateEvent event) {
		return sendOpsEventNotification(event, null, null);
	}

	private NotificationMessage sendOpsEventNotification(
			CiChangeStateEvent event, String note,
			NotificationSeverity severity) {
		return sendOpsEventNotification(event, note, severity, null);
	}

	private NotificationMessage sendOpsEventNotification(CiChangeStateEvent event, String note,
			NotificationSeverity severity, Object object) {
		return sendOpsEventNotification(event, note, severity, null, null, null);
	}

	/**
	 * Send ops event notification.
	 *
	 * @param event change event for which the notification needs to be sent.
     * @param note Additional note for notification.
     * @param severity severity of notification
	 *
	 */
	public NotificationMessage sendOpsEventNotification(CiChangeStateEvent event, String note, 
			NotificationSeverity severity, String subject, String text, Map<String, Object> payloadEntries) {
		OpsBaseEvent oEvent = getEventUtil().getOpsEvent(event);
		if (oEvent == null)
			return null;
		CmsCI ci = cmProcessor.getCiById(oEvent.getCiId());
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + oEvent.getCiId());
			return null;
		}
		
		NotificationMessage notify = new NotificationMessage();
		notify.setType(NotificationType.ci);
		notify.setCmsId(oEvent.getCiId());
		notify.setSource(NOTIFICATION_SOURCE);
		notify.setNsPath(ci.getNsPath());
		notify.setTimestamp(oEvent.getTimestamp());
		notify.setCloudName(event.getCloudName());
        notify.putPayloadEntry(EVENT_NAME, oEvent.getSource());
        notify.putPayloadEntry(CI_NAME, ci.getCiName());
        notify.putPayloadEntry(STATUS, oEvent.getStatus());
		notify.putPayloadEntry(OLD_STATE, event.getOldState());
		notify.putPayloadEntry(NEW_STATE, event.getNewState());
		notify.putPayloadEntry(CLASS_NAME, ci.getCiClassName());
		notify.putPayloadEntry(STATE, oEvent.getState());
		if (oEvent.getMetrics() != null) {
			notify.putPayloadEntry(METRICS, gson.toJson(oEvent.getMetrics()));	
		}
		
		addIpToNotification(ci, notify);
		
		if (payloadEntries != null && payloadEntries.size() > 0) {
			notify.putPayloadEntries(payloadEntries);
		}
		notify.setManifestCiId(oEvent.getManifestId());
		if (event.getComponentStatesCounters() != null) {
			for (String counterName : event.getComponentStatesCounters().keySet()) {
				notify.putPayloadEntry(counterName, String.valueOf(event.getComponentStatesCounters().get(counterName)));
			}
		}
		// tomcat-compute-cpu:HighCpuUsage->split.
		// app-tomcat-JvmInfo:PANGAEA:APP:US:Taxo_Svc:jvm:memory
		int index = StringUtils.ordinalIndexOf(oEvent.getName(), COLON, 1);
		if (index != StringUtils.INDEX_NOT_FOUND) {
			String threshHoldName = oEvent.getName().substring(index+1);
			notify.putPayloadEntry(THRESHOLD, threshHoldName);
		}
		CmsCI envCi = envProcessor.getEnv4Bom(ci.getCiId());
		CmsCIAttribute envProfileAttrib = envCi.getAttribute(PROFILE_ATTRIBUTE_NAME);
		if (envProfileAttrib != null) {
			notify.setEnvironmentProfileName(envProfileAttrib.getDfValue());
		}
		CmsCIAttribute adminStatusAttrib = envCi.getAttribute(ADMINSTATUS_ATTRIBUTE_NAME);
		if (adminStatusAttrib != null) {
			notify.setAdminStatus(envCi.getAttribute(ADMINSTATUS_ATTRIBUTE_NAME).getDfValue());
		}
		notify.setManifestCiId(oEvent.getManifestId());
		String subjectPrefix = NotificationMessage.buildSubjectPrefix(ci.getNsPath());
		
		if (oEvent.getState().equalsIgnoreCase("open")) {
            if (severity == null){
                notify.setSeverity(NotificationSeverity.warning);
            } else {
                notify.setSeverity(severity);
            }
            if (StringUtils.isNotEmpty(subject)) {
            	notify.setSubject(subjectPrefix + subject);
            } else {
            	notify.setSubject(subjectPrefix + oEvent.getName()+  SUBJECT_SUFFIX_OPEN_EVENT);	
            }
            
			//subject = <monitorName:[threshold_name|heartbeat]> [is violated|recovered]
             //	text = <ciName> is <newState>, <<opamp action/notes>>
            if (StringUtils.isNotEmpty(text)) {
            	notify.setText(text);
            } else {
            	notify.setText(ci.getCiName() + " is in " + event.getNewState()+" state");	
            }
		} else if (oEvent.getState().equalsIgnoreCase("close")) {
    		// close events go on INFO
			notify.setSeverity(NotificationSeverity.info);
            if (StringUtils.isNotEmpty(subject)) {
            	notify.setSubject(subjectPrefix + subject);
            } else {
            	notify.setSubject(subjectPrefix + oEvent.getName()+ SUBJECT_SUFFIX_CLOSE_EVENT);
            }
            if (StringUtils.isNotEmpty(text)) {
            	notify.setText(text);
            } else {
            	notify.setText(ci.getCiName() + " is in " + event.getNewState()+" state");
            }
		}
		if (StringUtils.isNotEmpty(note)) {
			notify.appendText("; " + note);
		}else {
			notify.appendText("." );
		}
		if (logger.isDebugEnabled()) {
			Gson gson = new Gson();
			logger.debug(gson.toJson(notify));
		}

		antennaClient.executeAsync(notify);
		return notify;
	}

	private void addIpToNotification(CmsCI ci, NotificationMessage notificationMessage) {
		// First check if the ci is a compute
		CmsCIAttribute attribute = ci.getAttribute("private_ip");
		if (attribute == null) {
			attribute = ci.getAttribute("public_ip");
		}

		if (attribute == null && !envProcessor.excludeIpInNotifications()) { // The ci is not a compute. Find its managed-via compute
			List<CmsCIRelation> relations = cmProcessor.getFromCIRelations(ci.getCiId(), "bom.ManagedVia", null, null);
			if (relations != null && relations.size() > 0) {
				CmsCI computeCi = relations.get(0).getToCi();
				attribute = computeCi.getAttribute("private_ip");
				if (attribute == null || StringUtils.isEmpty(attribute.getDfValue())) {
					attribute = computeCi.getAttribute("public_ip");
				}
			}
		}
		if (attribute != null && StringUtils.isNotEmpty(attribute.getDfValue())) {
			notificationMessage.putPayloadEntry(MANAGED_VIA_IP, attribute.getDfValue());
		}
	}

	/**
	 * Send unhealthy notification no repair.
	 * @param opsEvent
	 */
	public void sendUnhealthyNotificationNoRepair(OpsBaseEvent opsEvent) {
		long ciId = opsEvent.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}

		String text = buildNotificationPrefix(ci.getNsPath()) + " ci: " + ci.getCiName() + " is unhealthy! Repair is not enabled on this environment.";
		long manifestCiId = opsEvent.getManifestId();
		sendSimpleCiNotification(ci, NotificationSeverity.critical, ci.getCiName() + " is unhealthy!", text, (opsEvent != null) ? opsEvent.getSource() : null, manifestCiId);
	}

	/**
	 * Send unhealthy notification for inactive cloud.
	 *
	 * @param opsEvent for which notification needs to be sent.
	 *
	 */
	public void sendUnhealthyNotificationInactiveCloud(OpsBaseEvent opsEvent) {
		long ciId = opsEvent.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}

		String text = buildNotificationPrefix(ci.getNsPath()) + " ci: " + ci.getCiName() + " is unhealthy! The Cloud is marked as inactive. No repair will be performed.";
		long manifestCiId = opsEvent.getManifestId();

		sendSimpleCiNotification(ci, NotificationSeverity.critical, ci.getCiName() + " is unhealthy!", text, null, manifestCiId);
	}

	/**
	 * Send flex notification for inactive cloud.
	 *
	 * @param opsEvent
     * @param  state
	 */
	public void sendFlexNotificationInactiveCloud(CiChangeStateEvent event, String state) {
		long ciId = event.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}
		String text = buildNotificationPrefix(ci.getNsPath()) + " ci: " + ci.getCiName() + " is " + state + "! The Cloud is marked as inactive. No autoscaling will be performed.";
		NotificationSeverity severity;
		if (FlexStateProcessor.CI_STATE_OVERUTILIZED.equals(state)) {
			severity = NotificationSeverity.critical;
		} else {
			severity = NotificationSeverity.warning;
		}
		String subject = ci.getCiName() + " is " + state + "!";
		sendOpsEventNotification(event, null, severity, subject, text, null);
	}

	/**
	 * Send flex notification no repair.
	 *
	 * @param event
	 * @param state
	 */
	public void sendFlexNotificationNoRepair(CiChangeStateEvent event, String state) {
		long ciId = event.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}

		String text = buildNotificationPrefix(ci.getNsPath()) + " ci: " + ci.getCiName() + " is " + state + "! Autoscale is not enabled on this environment.";
		NotificationSeverity severity;
		if (FlexStateProcessor.CI_STATE_OVERUTILIZED.equals(state)) {
			severity = NotificationSeverity.critical;
		} else {
			severity = NotificationSeverity.warning;
		}
		String subject = ci.getCiName() + " is " + state + "!";
		sendOpsEventNotification(event, null, severity, subject, text, null);
	}

	/**
	 * Send flex notification limit is reached.
	 *
	 * @param event
	 * @param state
	 */
	public void sendFlexNotificationLimitIsReached(CiChangeStateEvent event, String state) {
		long ciId = event.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}
		String text = buildNotificationPrefix(ci.getNsPath());
		String subject = ci.getCiName() + " ci is " + state + "!";
		if (FlexStateProcessor.CI_STATE_OVERUTILIZED.equals(state)) {
			text += " ci: " + ci.getCiName() + " is " + state + "! Can not add more hosts - the max pool size is reached.";
			sendOpsEventNotification(event, null, NotificationSeverity.critical, subject, text, null);
		} else {
			text += " ci: " + ci.getCiName() + " is " + state + "! Can not remove more hosts - the min pool size is reached.";
			sendOpsEventNotification(event, null, NotificationSeverity.info, subject, text, null);
		}
		
	}

	/**
	 * Send flex notification processing.
	 *
	 * @param event
	 * @param state
	 *            the state
	 * @param step
	 *            the step
	 */
	public void sendFlexNotificationProcessing(CiChangeStateEvent event, String state, int step) {
		long ciId = event.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}

		String text = buildNotificationPrefix(ci.getNsPath());
		if ("overutilized".equals(state)) {
			text += " ci: " + ci.getCiName() + " is " + state + "! Going to add " + step + " more host(s) to the pool!";
		} else {
			text += " ci: " + ci.getCiName() + " is " + state + "! Going to remove " + step + " host(s) from the pool.";
		}

		String subject = ci.getCiName() + " is " + state + "!";
		
		sendOpsEventNotification(event, null, NotificationSeverity.info, subject, text, null);
	}

	/**
	 * Send flex notification error processing.
	 *
	 * @param event
	 * @param state
	 * @param error
	 */
	public void sendFlexNotificationErrorProcessing(CiChangeStateEvent event, String state, String error) {
		long ciId = event.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}

		String text = buildNotificationPrefix(ci.getNsPath());
		text += " ci: " + ci.getCiName() + " is " + state + "! Can not process due to this error: " + error;
		String subject = ci.getCiName() + " is " + state + "! Error processing";
		sendOpsEventNotification(event, null, NotificationSeverity.critical, subject, text, null);
	}

	/**
	 * Send flex notification postpone processing.
	 *
	 * @param event
	 * @param state
	 */
	public void sendFlexNotificationPostponeProcessing(CiChangeStateEvent event, String state) {
		long ciId = event.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}

		String text = buildNotificationPrefix(ci.getNsPath());
		text += " ci: " + ci.getCiName() + " is " + state + "! Can not change the pool size because there is an open release for this environment, will try later!";
		String subject = ci.getCiName() + " is " + state + "!";
		
		sendOpsEventNotification(event, null, NotificationSeverity.warning, subject, text, null);
	}

	/**
	 * Send good notification.
	 *
	 * @param opsEvent
	 * @deprecated
	 */
	public void sendGoodNotification(CiChangeStateEvent event) {
		long ciId = event.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}

		String text = buildNotificationPrefix(ci.getNsPath()) + " ci: " + ci.getCiName() + " is recovered!";

		sendOpsEventNotification(event, null, NotificationSeverity.info, ci.getCiName() + " recovered.", text, null);
	}

	/**
	 * Send depends on unhealthy notification.
	 *
	 * @param opsEvent
	 * @deprecated
	 */
	public void sendDependsOnUnhealthyNotification(OpsBaseEvent opsEvent) {
		long ciId = opsEvent.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}

		String text = buildNotificationPrefix(ci.getNsPath()) + " ci: " + ci.getCiName() + " is unhealthy,  waiting on dependency repair!";
		long manifestCiId = opsEvent.getManifestId();

		sendSimpleCiNotification(ci, NotificationSeverity.warning, ci.getCiName() + " is unhealthy!", text, (opsEvent != null) ? opsEvent.getSource() : null, manifestCiId);
	}

	/**
	 * Send repair notification.
	 *
	 * @param opsEvent
	 * @deprecated
	 */
	public void sendRepairNotification(OpsBaseEvent opsEvent) {
		long ciId = opsEvent.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}
		String text = buildNotificationPrefix(ci.getNsPath()) + " ci: " + ci.getCiName() + " is unhealthy and will be repaired.";
		long manifestCiId = opsEvent.getManifestId();
		sendSimpleCiNotification(ci, NotificationSeverity.warning, ci.getCiName() + " under repair", text, (opsEvent != null) ? opsEvent.getSource() : null, manifestCiId);
	}

	/**
	 * Send replace notification.
	 *
	 * @param opsEvent
	 * @deprecated
	 */
	public void sendReplaceNotification(OpsBaseEvent opsEvent) {
		long ciId = opsEvent.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}

		String text = buildNotificationPrefix(ci.getNsPath()) + " ci: " + ci.getCiName() + " could not be repaired. Preparing to replace.";
		long manifestCiId = opsEvent.getManifestId();
		sendSimpleCiNotification(ci, NotificationSeverity.warning, ci.getCiName() + " being replaced", text, (opsEvent != null) ? opsEvent.getSource() : null, manifestCiId);
	}

	/**
	 * Send postponed repair notification.
	 *
	 * @param event
	 * @deprecated
	 */
	public void sendPostponedRepairNotification(OpsBaseEvent event) {
		long ciId = event.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}

		String text = buildNotificationPrefix(ci.getNsPath()) + " ci: " + ci.getCiName() + " repair procedure will be postpone due to open release or another procedure.";
		long manifestCiId = event.getManifestId();

		sendSimpleCiNotification(ci, NotificationSeverity.warning, ci.getCiName() + " repair", text, (event != null) ? event.getSource() : null, manifestCiId);
	}

	/**
	 * Send postponed repair notification.
	 *
	 * @param event
	 * @deprecated
	 */
	public void sendPostponedReplaceNotification(OpsBaseEvent event) {
		long ciId = event.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}

		String text = buildNotificationPrefix(ci.getNsPath()) + " ci: " + ci.getCiName() 
				+ " auto-replace will be postponed due to an open release or an ongoing deployment or changes committed but pending deployment";
		long manifestCiId = event.getManifestId();

		sendSimpleCiNotification(ci, NotificationSeverity.warning, ci.getCiName() + " replace postponed", text, (event != null) ? event.getSource() : null, manifestCiId);
	}

	/**
	 *
	 * @param ci
	 * @param severity
	 * @param subject
	 * @param text
	 * @param eventName
	 * @param manifestCiId
	 * @deprecated
	 */
	private void sendSimpleCiNotification(CmsCI ci, NotificationSeverity severity, String subject, String text, String eventName, long manifestCiId) {

		CmsCI envCi = envProcessor.getEnv4Bom(ci.getCiId());
		NotificationMessage notify = new NotificationMessage();
		notify.setType(NotificationType.ci);
		notify.setCmsId(ci.getCiId());
		notify.setSource(NOTIFICATION_SOURCE);
		notify.setNsPath(ci.getNsPath());
		notify.setTimestamp(System.currentTimeMillis());

		notify.setSeverity(severity);
		notify.setSubject(subject);
		notify.setText(text);
		notify.putPayloadEntry(EVENT_NAME, eventName);
		notify.putPayloadEntry(CI_NAME, ci.getCiName());
		CmsCIAttribute envProfileAttrib = envCi.getAttribute(PROFILE_ATTRIBUTE_NAME);
		if (envProfileAttrib != null) {
			notify.setEnvironmentProfileName(envProfileAttrib.getDfValue());
		}
		CmsCIAttribute adminStatusAttrib = envCi.getAttribute(ADMINSTATUS_ATTRIBUTE_NAME);
		if (adminStatusAttrib != null) {
			notify.setAdminStatus(envCi.getAttribute(ADMINSTATUS_ATTRIBUTE_NAME).getDfValue());
		}
		antennaClient.executeAsync(notify);
	}

	/**
	 * @deprecated
	 * @param nsPath
	 * @return
	 */
	private String buildNotificationPrefix(String nsPath) {
		String[] parts = nsPath.split("/");
		String prefix = "Assembly: " + parts[2] + "; ";
		if (parts.length > 2) {
			prefix += "Environment: " + parts[3] + "; ";
		}
		return prefix;
	}



	/**
	 * Notification after first repair
	 *
	 * @param opsEvent
	 * @deprecated
	 */
	public void sendRepairCriticalNotification(OpsBaseEvent opsEvent) {
		long ciId = opsEvent.getCiId();
		CmsCI ci = cmProcessor.getCiById(ciId);
		if (ci == null) {
			logger.error("Can not get CmsCI for id - " + ciId);
			return;
		}

		String text = buildNotificationPrefix(ci.getNsPath()) + " ci: " + ci.getCiName() + " is unhealthy and will be repaired.";
		long manifestCiId = opsEvent.getManifestId();

		sendSimpleCiNotification(ci, NotificationSeverity.critical, ci.getCiName() + " under repair", text, (opsEvent != null) ? opsEvent.getSource() : null, manifestCiId);
	}

	public NotificationMessage sendRepairNotification(CiChangeStateEvent event, Map<String, String> payloadEntries) {
		return sendOpsEventNotification(event, REPAIR_IN_PROGRESS, NotificationSeverity.warning, payloadEntries);
	}

	public NotificationMessage sendPostponedRepairNotification(CiChangeStateEvent event, Map<String, String> payloadEntries) {
		return sendOpsEventNotification(event, REPAIR_POSTPONED, NotificationSeverity.warning, payloadEntries);
	}

	public NotificationMessage sendDependsOnUnhealthyNotification(CiChangeStateEvent event) {
		return sendOpsEventNotification(event, WAITING_ON_DEPENDENT_REPAIR, NotificationSeverity.warning);
	}

	public NotificationMessage sendUnhealthyNotificationInactiveCloud(CiChangeStateEvent event) {
		return sendOpsEventNotification(event, REPAIR_NOT_PERFORMED_INACTIVE_CLOUD, NotificationSeverity.critical);
	}

	public NotificationMessage sendUnhealthyNotificationNoRepair(CiChangeStateEvent event) {
		return sendOpsEventNotification(event, REPAIR_DISABLED_IN_THIS_ENVIRONMENT, NotificationSeverity.critical);
	}

	public NotificationMessage sendRepairCriticalNotification(CiChangeStateEvent event, Map<String, String> payloadEntries) {
		return sendOpsEventNotification(event, REPAIR_IN_PROGRESS, NotificationSeverity.critical, payloadEntries);
	}


	public NotificationMessage sendPostponedReplaceNotification(CiChangeStateEvent event) {
		return sendOpsEventNotification(event, REPLACE_POSTPONED_NOTIFICATION, NotificationSeverity.critical);
	}
	public NotificationMessage sendReplaceNotification(CiChangeStateEvent event) {
		return sendOpsEventNotification(event, REPLACE_NOTIFICATION, NotificationSeverity.warning);
	}

	public EventUtil getEventUtil() {
		return eventUtil;
	}

	public void setEventUtil(EventUtil eventUtil) {
		this.eventUtil = eventUtil;
	}

	/**
	 * Sets the antenna client.
	 *
	 * @param antennaClient the new antenna client
	 */
	public void setAntennaClient(ReliableExecutor<NotificationMessage> antennaClient) {
		this.antennaClient = antennaClient;
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

	public EnvPropsProcessor getEnvProcessor() {
		return envProcessor;
	}

	public void setEnvProcessor(EnvPropsProcessor envProcessor) {
		this.envProcessor = envProcessor;
	}

	public NotificationMessage sendDefunctNotification(CiChangeStateEvent event) {
		return sendOpsEventNotification(event, CI_IN_DEFUNCT_STATE_NOTIFICATION, NotificationSeverity.warning);		
	}
}