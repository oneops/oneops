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
package com.oneops.sensor.listeners;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.ops.CiOpsProcessor;
import com.oneops.ops.PerfData;
import com.oneops.ops.PerfDataRequest;
import com.oneops.ops.PerfDatasource;
import com.oneops.ops.PerfHeader;
import com.oneops.ops.dao.OpsEventDao;
import com.oneops.ops.dao.PerfDataAccessor;
import com.oneops.ops.dao.PerfHeaderDao;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.ops.events.OpsEvent;
import com.oneops.ops.events.Status;
import com.oneops.sensor.CiStateProcessor;
import com.oneops.sensor.jms.OpsEventPublisher;
import com.oneops.sensor.util.EventContext;
import com.oneops.sensor.util.EventConverter;
import com.oneops.sensor.util.ReplacedInstances;
import com.oneops.sensor.util.SensorHeartBeat;

/**
 * The listener interface for receiving opsEvent events.
 * The class that is interested in processing a opsEvent
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addOpsEventListener<code> method. When
 * the opsEvent event occurs, that object's appropriate
 * method is invoked.
 *
 * @see OpsEvent
 */
public class OpsEventListener implements UpdateListener {

    private static Logger logger = Logger.getLogger(OpsEventListener.class);
    private static final long MAX_LENGTH_2_CENTURIES = 9999999999999l;
    private static final String WATCHED_BY_REL_NAME = "manifest.WatchedBy";
    private static final String MONITOR_CLASS = "manifest.Monitor";

    private int heartbeatPerfDataPoints = 2;
    private OpsEventDao opsEventDao;
    private PerfDataAccessor perfDataAccessor;
    private Gson gson = new Gson();
    private CiOpsProcessor coProcessor;
    private OpsEventPublisher opsEventPub;
    private SensorHeartBeat sensorHeartBeat;
    private PerfHeaderDao phDao;
    private CmsCmProcessor cmProcessor;
    private CiStateProcessor ciStateProcessor;
    private long hbChannelUpTimeout = 90;
    private ReplacedInstances replacedInstances;

    public void init() {
        String chdelay = System.getProperty("com.oneops.sensor.channel.uptimedelay");
        if (chdelay != null) {
            this.hbChannelUpTimeout = Long.valueOf(chdelay);
        }
        String heartbeatCheckProperty = System.getProperty("sensor.heartbeat.datacheck.minperfevents");
        if (NumberUtils.isNumber(heartbeatCheckProperty)) {
            heartbeatPerfDataPoints = Integer.valueOf(heartbeatCheckProperty.trim());
            logger.info("using property : sensor.heartbeat.datacheck.minperfevents = " + heartbeatCheckProperty);
        }
    }

    /**
     * @param cmProcessor
     */
    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }

    /**
     * Sets the sensor heart beat.
     *
     * @param sensorHeartBeat the new sensor heart beat
     */
    public void setSensorHeartBeat(SensorHeartBeat sensorHeartBeat) {
        this.sensorHeartBeat = sensorHeartBeat;
    }

    /**
     * Sets the ops event pub.
     *
     * @param opsEventPub the new ops event pub
     */
    public void setOpsEventPub(OpsEventPublisher opsEventPub) {
        this.opsEventPub = opsEventPub;
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
     * Sets the ops event dao.
     *
     * @param opsEventsDao the new ops event dao
     */
    public void setOpsEventDao(OpsEventDao opsEventsDao) {
        this.opsEventDao = opsEventsDao;
    }

    /**
     * Mutator for perf header dao.
     *
     * @param phDao
     */
    public void setPhDao(PerfHeaderDao phDao) {
        this.phDao = phDao;
    }

    /**
     * Get perf data accessor
     *
     * @return
     */
    public PerfDataAccessor getPerfDataAccessor() {
        return perfDataAccessor;
    }

    /**
     * Set perf data accessor
     *
     * @param pdAccessor
     */
    public void setPerfDataAccessor(PerfDataAccessor pdAccessor) {
        this.perfDataAccessor = pdAccessor;
    }

    /**
     * publishes stat change message with payload from the underlying event
     */
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        logger.debug("in " + OpsEventListener.class.getSimpleName());
        logger.debug("size=" + newEvents.length);

        long timeShifter = 0;
        for (EventBean eBean : newEvents) {
            OpsEvent event = (OpsEvent) eBean.getUnderlying();

            logger.info(" Recieved event for ciId :" + event.getCiId());

            if (!coProcessor.isCiActive(event.getCiId())) {
                logger.warn("ignoring cid as it is not active" + event.getCiId());
                continue;
            }
            if ("heartbeat".equals(event.getType())) {

                // Lets check if the stream is good and it's not just a connection issue
                // if there is no metrics coming in within 1 min (HEARTBEAT_OFFTIME_MS)
                // there is a connection issue

                if (!sensorHeartBeat.isUp(event.getChannel()) ||
                        (System.currentTimeMillis() - sensorHeartBeat.getUpSince(event.getChannel())) < (hbChannelUpTimeout * 1000)) {
                    //the stream is broken will skip this event
                    logger.error("got HEARTBEAT event - " + gson.toJson(event));
                    if (!sensorHeartBeat.isUp(event.getChannel())) {
                        logger.error("BUT seems like the metrics channel " + event.getChannel() + " is down since "
                                + (new Date(sensorHeartBeat.getDownSince(event.getChannel()))).toString());
                    } else {
                        logger.error("BUT seems like the metrics channel " + event.getChannel() + " is just recently recovered since "
                                + (new Date(sensorHeartBeat.getUpSince(event.getChannel()))).toString());

                    }
                    logger.error("Will skip this event!");

                    // we need to set the state to notify
                    //event.setCiState("notify");
                    continue;
                }

                if (replacedInstances.isReplaced(event.getCiId())) {
                    logger.info("HEARTBEAT event for " + event.getCiId() + " ignored as this ci is getting replaced");
                    continue;
                }

                // if everything is ok lets check if we have the headers and if not lets create one
                String phKey = event.getCiId() + ":" + event.getSource();
                logger.info(phKey);
                PerfHeader ph = phDao.getHeader(phKey);
                if (ph.getDsMap().isEmpty()) {
                    logger.info("Can not get header for " + phKey + "; will try to recreate them!");
                    // try to create headers
                    createMissingHeader(event.getManifestId(), event.getCiId(), event.getSource());
                    continue;
                }
                String perfRequestParams = "";
                try {
                    PerfDataRequest perfRequest = createPerfDataRequest(event, ph);
                    perfRequestParams = "ci_id=" + event.getCiId() + ", metrics=" + perfRequest.getMetrics()[0]
                            + " startTime=" + perfRequest.getStart() + " endTime=" + perfRequest.getEnd();

                    String perfDataJson = perfDataAccessor.getPerfDataSeries(perfRequest);
                    PerfData perfData = gson.fromJson(perfDataJson, PerfData.class);
                    if (perfData != null && perfData.getData() != null
                            && perfData.getData().length > heartbeatPerfDataPoints) {
                        logger.info("avoiding a false heartbeat alarm for " + perfRequestParams + ". perf data array size: " + perfData.getData().length);
                        continue;
                    }
                } catch (Exception e) {
                    logger.error("Error while calling perfDao for checking heartbeat data. PerfDataRequest: " + perfRequestParams, e);
                }
            }

            //we need to alter current time since we can get multi events fired at the same milisec - this will affect cassandra persistence that is based on the timestamp
            long threadId = Thread.currentThread().getId();
            long reminder = (threadId % 100) + (timeShifter * 100);
            long timestamp = System.currentTimeMillis() + reminder;
            //greater than 13 digits; adding more logging.
            if (timestamp > MAX_LENGTH_2_CENTURIES) {
                logger.warn(" Shifting time by more than reasonable for :" + event.getCiId() + ": " + event.getSource() + "timestamp : " + timestamp + " reminder: " + reminder);
            }
            event.setTimestamp(timestamp);
            EventContext eventContext = new EventContext(event);
            handleEvent(eventContext);
            timeShifter++;
        }
    }

    private void handleEvent(EventContext eventContext) {
        OpsEvent event = (OpsEvent)eventContext.getEvent();
        ciStateProcessor.updateState4OpenEvent(eventContext);
        boolean isNew = opsEventDao.addOpenEventForCi(event.getCiId(), event.getName(), event.getTimestamp(), event.getCiState());
        if (logger.isDebugEnabled()) {
        	logger.debug("persisted open event ->" + event.getCiId() + " name : " + event.getName() + " state : " + event.getState() + " isNew " + isNew);
        }
        //set the status whether its new or existing status in base event based on whether already an open event exists for this
        //ci and eventName (monitor-threshold eg<montitorName>:definitionName. p1-compute-postfixprocess:PostfixProcessLow\\).
        if (isNew) {
            event.setStatus(Status.NEW);
        } else {
        	eventContext.emptyStateCounterDelta();
            event.setStatus(Status.EXISTING);
        }

        String payload = gson.toJson(EventConverter.convert(event));
        logger.debug(payload);
        opsEventDao.persistOpsEvent(event.getCiId(), event.getName(), event.getTimestamp(), payload);

        CiChangeStateEvent ciEvent = new CiChangeStateEvent();
        ciEvent.setCiId(event.getCiId());
        ciEvent.setNewState(eventContext.getNewState());
        ciEvent.setOldState(eventContext.getOldState());
        //TODO change ciEvent to have notify state ?
        ciEvent.setPayLoad(payload);
        ciEvent.setTimestamp(System.currentTimeMillis());

        if (eventContext.isStateChanged()) {
            if (logger.isDebugEnabled()) {
                logger.debug("state changed ci -> " + event.getCiId() + ", old state : " + eventContext.getOldState() + ", new state : " + eventContext.getNewState());
            }
            coProcessor.persistCiStateChange(event.getCiId(), event.getManifestId(), ciEvent, event.getTimestamp(), eventContext.getStateCounterDelta());
        }
        opsEventPub.publishCiStateMessage(ciEvent);
    }

    private PerfDataRequest createPerfDataRequest(OpsEvent event, PerfHeader ph) {
        PerfDataRequest request = new PerfDataRequest();
        Map<String, PerfDatasource> map = ph.getDsMap();
        if (map == null || map.size() == 0) {
            return null;
        }
        long ciId = event.getCiId();
        String metric = map.keySet().iterator().next();
        //PerfDatasource perfDs = map.get(metric);
        int heartBeatDurationSecs = 180; //TODO: use actual hertbeat seconds entered by user
        String eventSource = event.getSource();
        String[] metrics = {eventSource + ":" + metric};
        long endTime = (System.currentTimeMillis() / 1000);//that is current time in seconds
        long startTime = endTime - heartBeatDurationSecs;//end time is determined by heartbeat
        int step = 60;//1m bucket

        request.setCi_id(ciId);
        request.setMetrics(metrics);
        request.setEnd(endTime);
        request.setStart(startTime);
        request.setStep(step);
        return request;
    }

    private void createMissingHeader(long manifestId, long ciId, String monitoName) {
        if (manifestId == 0) {
            List<CmsCIRelation> realizedRels = cmProcessor.getToCIRelationsNakedNoAttrs(ciId, "base.RealizedAs", null, null);
            if (realizedRels.size() > 0) {
                manifestId = realizedRels.get(0).getFromCiId();
            }
        }
        List<CmsCIRelation> monitorRels = cmProcessor.getFromCIRelations(manifestId, WATCHED_BY_REL_NAME, MONITOR_CLASS);
        for (CmsCIRelation monitorRel : monitorRels) {
            CmsCI monitor = monitorRel.getToCi();
            String key = ciId + ":" + monitor.getCiName();
            try {
                if (monitor.getAttribute("metrics") != null && monitor.getAttribute("metrics").getDjValue() != null) {
                    logger.info("Creating header for " + key);
                    phDao.createHeader(key, monitor.getAttribute("metrics").getDjValue());
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("IOException in createMissingHeader", e);
            }
        }

    }

	public void setCiStateProcessor(CiStateProcessor ciStateProcessor) {
		this.ciStateProcessor = ciStateProcessor;
	}

	public void setReplacedInstances(ReplacedInstances replacedInstances) {
		this.replacedInstances = replacedInstances;
	}

}
