package com.oneops.sensor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.oneops.ops.CiOpsProcessor;
import com.oneops.ops.dao.OpsCiStateDao;
import com.oneops.ops.dao.OpsEventDao;
import com.oneops.ops.events.CiOpenEvent;
import com.oneops.ops.events.OpsBaseEvent;
import com.oneops.ops.events.OpsCloseEvent;
import com.oneops.ops.events.OpsEvent;
import com.oneops.sensor.thresholds.ThresholdsDao;
import com.oneops.sensor.util.EventContext;

public class CiStateProcessor {

	private static Logger logger = Logger.getLogger(CiStateProcessor.class);

	private OpsEventDao opsEventDao;
	private OpsCiStateDao opsCiStateDao;
	private ThresholdsDao tsDao;
	private CiOpsProcessor coProcessor;

	public void updateState4OpenEvent(EventContext eventContext) {
        OpsEvent event = (OpsEvent)eventContext.getEvent();
        lookupCurrentState(eventContext);
        List<CiOpenEvent> openEvents = eventContext.getOpenEvents();
        openEvents.add(constructOpenEvent(event));
        processCounters(eventContext, openEvents);
        if (logger.isDebugEnabled()) {
        	logger.debug("state counter delta for open event for ci " + event.getCiId() + " : " + eventContext.getStateCounterDelta());
        }
    }

	private CiOpenEvent constructOpenEvent(OpsEvent event) {
        CiOpenEvent openEvent = new CiOpenEvent();
        openEvent.setName(event.getName());
        openEvent.setState(event.getCiState());
        openEvent.setTimestamp(event.getTimestamp());
        return openEvent;
    }

	private void lookupCurrentState(EventContext eventContext) {
		OpsBaseEvent event = eventContext.getEvent();
		List<CiOpenEvent> openEvents = opsEventDao.getCiOpenEvents(event.getCiId());
		eventContext.setOpenEvents(openEvents);
        String oldState = coProcessor.getState(openEvents);
        eventContext.setOldState(oldState);
	}

	private void processCounters(EventContext eventContext, List<CiOpenEvent> openEvents) {
		String newState = coProcessor.getState(openEvents);
		String oldState = eventContext.getOldState();
        eventContext.setNewState(newState);
        if (!oldState.equals(newState)) {
            eventContext.setStateChanged(true);
            Map<String, Long> stateDelta = new HashMap<String, Long>();
            stateDelta.put(oldState, -1L);
            stateDelta.put(newState, 1L);
            eventContext.setStateCounterDelta(stateDelta);
        }
	}

	public void updateState4CloseEvent(EventContext eventContext) {
        OpsCloseEvent event = (OpsCloseEvent)eventContext.getEvent();
        lookupCurrentState(eventContext);
        List<CiOpenEvent> openEvents = eventContext.getOpenEvents();
        openEvents.removeIf(openEvent -> event.getName().equals(openEvent.getName()));
        processCounters(eventContext, openEvents);
        if (logger.isDebugEnabled()) {
        	logger.debug("state counter delta for open event for ci " + event.getCiId() + " : " + eventContext.getStateCounterDelta());
        }
    }

    public void updateState4MonitorRemoval(long manifestId, String source) {
        List<Long> ciIds = tsDao.getManifestCiIds(manifestId, 3, 10000, true);
        Map<Long, List<CiOpenEvent>> openEvents = opsEventDao.getCiOpenEvents(ciIds);
        Map<String, Long> countersDeltaMap = new HashMap<String, Long>();
        for (Map.Entry<Long, List<CiOpenEvent>> entry : openEvents.entrySet()) {
            long ciId = entry.getKey();
            List<CiOpenEvent> bomOpenEvents = entry.getValue();
            Map<String, List<CiOpenEvent>> eventGroupMap = bomOpenEvents.stream().collect(
            		Collectors.groupingBy(ciOpenEvent -> ciOpenEvent.getName().startsWith(source + ":") ? "expiredOpenEvents" : "validOpenEvents"));
            List<CiOpenEvent> expiredOpenEvents = eventGroupMap.get("expiredOpenEvents");
            
            if (expiredOpenEvents != null && !expiredOpenEvents.isEmpty()) {
                String oldState = coProcessor.getState(bomOpenEvents);
            	expiredOpenEvents.stream().forEach(ciOpenEvent -> opsEventDao.removeOpenEventForCi(ciId, ciOpenEvent.getName()));
            	List<CiOpenEvent> validOpenEvents = eventGroupMap.get("validOpenEvents");
            	if (validOpenEvents == null) 
            		validOpenEvents = Collections.emptyList();
                String newState = coProcessor.getState(validOpenEvents);
                if (!oldState.equals(newState)) {
                	countersDeltaMap.merge(oldState, -1L, Long::sum);
                	countersDeltaMap.merge(newState, 1L, Long::sum);
                }
            }
        }
        if (logger.isDebugEnabled()) {
        	logger.debug("state counter delta for monitor removal for manifest " + manifestId + "-" + source + " : " + countersDeltaMap);
        }
        if (!openEvents.isEmpty()) {
            coProcessor.resetManifestStateCounters(manifestId, ciIds, countersDeltaMap);
        }
    }

    public void updateState4CiRemoval(long ciId, long manifestId) {
    	String state = coProcessor.getCIstate(ciId);
		opsCiStateDao.decrComponentsStateCounter(manifestId, OpsCiStateDao.COMPONENT_STATE_TOTAL, 1);
		opsCiStateDao.decrComponentsStateCounter(manifestId, state, 1);
		if (logger.isDebugEnabled()) {
        	logger.debug("decremented state counter for ci removal " + ciId + " : " + state);
        }
    }

	public void setOpsEventDao(OpsEventDao opsEventDao) {
		this.opsEventDao = opsEventDao;
	}

	public void setOpsCiStateDao(OpsCiStateDao opsCiStateDao) {
		this.opsCiStateDao = opsCiStateDao;
	}

	public void setTsDao(ThresholdsDao tsDao) {
		this.tsDao = tsDao;
	}

	public void setCoProcessor(CiOpsProcessor coProcessor) {
		this.coProcessor = coProcessor;
	}
}
