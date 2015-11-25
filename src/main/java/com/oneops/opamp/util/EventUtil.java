package com.oneops.opamp.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.util.CmsConstants;
import com.oneops.opamp.cache.WatchedByAttributeCache;
import com.oneops.opamp.exceptions.OpampException;
import com.oneops.opamp.service.Notifications;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.ops.events.OpsBaseEvent;
import com.oneops.ops.events.Status;

/**
 * This is utility to have one place to convert CiChangeStateEvent to ops event.
 * Also have some general purpose methods to check if notification should be
 * sent or not.
 * 
 * @author glall
 *
 */
@Service
public class EventUtil {

	private static final String TRUE_VALUE = "true";
	private static Logger logger = Logger.getLogger(EventUtil.class);
	private static final String KEY_DELIMITER = ":";
	private static final String NOTIFY_ON_STATE_CHANGE_ATTR = "notifyOnlyOnStateChange";

	private Gson gson;
	private Notifications notifier;

	/**
	 * The cache for attribute
	 */
	@Autowired
	private WatchedByAttributeCache cache;

	/**
	 * Cms CI processor
	 */
	@Autowired
	private CmsCmProcessor cmProcessor;

	public OpsBaseEvent getOpsEvent(CiChangeStateEvent event) {
		if (event == null) {
			throw new IllegalArgumentException("event can not be null");
		}
		OpsBaseEvent opsEvent = null;
		if (event.getPayLoad() != null) {
			opsEvent = getGson().fromJson(event.getPayLoad(), OpsBaseEvent.class);
		} else {
			logger.info("event payload null. CiId: " + event.getCiId() + " oldState: " + event.getOldState() + " newState: " + event.getNewState());
			opsEvent = new OpsBaseEvent();
			opsEvent.setCiId(event.getCiId());
		}
		return opsEvent;
	}

	/**
	 * This is a convenient api to determine, if the event should trigger
	 * notification. True only if there is a change in state of CI or relation
	 * attribute of 'watchedBy' on ci is set to true.
	 * 
	 * @param event
	 *            The change event got from the sensor.
	 * @param oEvent
	 *            the Underlying opsEvent which resulted this event.
	 * @return true if notification needs to be generated ,false otherwise.
	 * @throws OpampException
	 *             If manifestId could not be determined for some reason and
	 *             hence the NOTE: The oEvent could be derived from
	 *             <code>CiChangeStateEvent</code>,this provides where you have
	 *             marshaled the payload already into ops event.
	 */
	public boolean shouldNotify(CiChangeStateEvent event, OpsBaseEvent oEvent) throws OpampException {
		// default to Notify true;dont skip notification i
		boolean shouldNotify = true;
		String notifyOnStateChangeEvent = null;
		// change in event state; trigger notification
		// if new event notify.
		logger.debug(gson.toJson(oEvent));
		if (oEvent.getStatus().equals(Status.NEW)) {
			shouldNotify = true;
		} else {
			// check if notifyOnStateChangeEvent is set on the relation,
			// notify only if state changes
			String key = getKey(oEvent);
			try {
				notifyOnStateChangeEvent = getCache().instance().get(key);
			} catch (Exception e) {
				// in case the attribute is not obtained, the default value is
				// to notifyOnlyOnStateChange
				notifyOnStateChangeEvent = TRUE_VALUE;
				logger.warn("Could not obtain the attribute " + NOTIFY_ON_STATE_CHANGE_ATTR + " for key:" + key);
				logger.error("Error occured while getting the attribute value", e.getCause());
			}
			// by default notify only when there are state changes.
			shouldNotify = (StringUtils.isEmpty(notifyOnStateChangeEvent) || TRUE_VALUE.equalsIgnoreCase(notifyOnStateChangeEvent)) ? false : true;
		}

		logger.info("notify " + shouldNotify + " for cid: " + event.getCiId() + " " + oEvent.getSource() +" state "+oEvent.getState()+ " status " + oEvent.getStatus() + " ostate:" + event.getOldState()
		        + " nstate: " + event.getNewState() + " relattrval: " + notifyOnStateChangeEvent);
		return shouldNotify;
	}

	public String getKey(OpsBaseEvent oEvent) throws OpampException {
		// In ideal scenario, manifestId should never be zero, since threshold
		// definition is on monitors
		long manifestId = oEvent.getManifestId();
		if (manifestId == 0) {
			// try to get manifestId using ciId
			manifestId = getManifestId(oEvent.getCiId());
			logger.warn("The event has missing manifestId Got manifestId " + manifestId + " with lookup on  cid " + oEvent.getCiId() + " source " + oEvent.getSource());
			if (manifestId == 0) {
				// if manifestId can not be found, the event can not be
				// processed throw exception
				throw new OpampException("Could not determine the manifestId for ciId" + oEvent.getCiId());
			}
		}

		return String.valueOf(manifestId) + KEY_DELIMITER + oEvent.getSource() + KEY_DELIMITER + NOTIFY_ON_STATE_CHANGE_ATTR;

	}

	public Gson getGson() {
		return gson;
	}

	public void setGson(Gson gson) {
		this.gson = gson;
	}

	public Notifications getNotifier() {
		return notifier;
	}

	public void setNotifier(Notifications notifier) {
		this.notifier = notifier;
	}

	public WatchedByAttributeCache getCache() {
		return cache;
	}

	public void setCache(WatchedByAttributeCache cache) {
		this.cache = cache;
	}

	public long getManifestId(long ciId) {
		List<CmsCIRelation> manifestCiRels = getCmProcessor().getToCIRelationsNakedNoAttrs(ciId, CmsConstants.BASE_REALIZED_AS, null, null);
		long manifestCiId = 0;
		if (manifestCiRels.size() > 0) {
			manifestCiId = manifestCiRels.get(0).getFromCiId();
		}
		return manifestCiId;
	}

	public CmsCmProcessor getCmProcessor() {
		return cmProcessor;
	}

	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	public boolean shouldNotify(CiChangeStateEvent event) throws OpampException {
	    return shouldNotify(event,getOpsEvent(event));
    }
	
	/**
	 * Sets the cloudName for the ops event
	 * @param event
	 */
	public void addCloudName(CiChangeStateEvent event,List<CmsCIRelation> deployedToRels) {
		if (deployedToRels.size()>0) {
			CmsCIRelation ciRelation = deployedToRels.get(0);
			String cloudCiName = ciRelation.getToCi().getCiName();
			event.setCloudName(cloudCiName);
		}
	}

}
