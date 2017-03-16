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
package com.oneops.sensor.ws;

import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.ops.CiOpsProcessor;
import com.oneops.ops.dao.OpsEventDao;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.ops.events.CiOpenEvent;
import com.oneops.ops.events.OpsEvent;
import com.oneops.sensor.util.MonitorRestorer;
import com.oneops.sensor.Sensor;
import com.oneops.sensor.jms.SensorListener;
import com.oneops.sensor.jms.SensorListenerContainer;
import com.oneops.sensor.thresholds.ThresholdsDao;
import com.oneops.sensor.util.ChannelState;
import com.oneops.sensor.util.SensorHeartBeat;
import com.oneops.sensor.util.SensorTools;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.Executors;

/**
 * The Class SensorWsController.
 */
@Controller
public class SensorWsController {

    //private static final long MILLIS_PER_SECOND = 1000L;

	private static final int LENGTH_OF_TIMESTAMP_FOR_TWO_CENTURIES = 13;

    private static final int EVNT_CNT_DFLT = 50;

    private static Logger logger = Logger.getLogger(SensorWsController.class);

    private Sensor sensor;
    private OpsEventDao oeDao;
    private ThresholdsDao tsDao;
    private CiOpsProcessor coProcessor;
    private SensorHeartBeat sensorHeartBeat;
    private SensorListener sensorListener;
    private SensorTools sensorTools;
    private MonitorRestorer restorer;

    public void setSensorListener(SensorListener sensorListener) {
        this.sensorListener = sensorListener;
    }

    public void setTsDao(ThresholdsDao tsDao) {
        this.tsDao = tsDao;
    }

    public void setSensorTools(SensorTools sensorTools) {
        this.sensorTools = sensorTools;
    }

    public void setRestorer(MonitorRestorer restorer) {
		this.restorer = restorer;
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
     * Sets the oe dao.
     *
     * @param oeDao the new oe dao
     */
    public void setOeDao(OpsEventDao oeDao) {
        this.oeDao = oeDao;
    }


    /**
     * Sets the sensor.
     *
     * @param sensor the new sensor
     */
    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Sets the sensor heart beat.
     *
     * @param sensorHeartBeat the new sensor heart beat
     */
    public void setSensorHeartBeat(SensorHeartBeat sensorHeartBeat) {
        this.sensorHeartBeat = sensorHeartBeat;
    }	

	
/*	
    @RequestMapping(method=RequestMethod.POST, value="/monitors")
	@ResponseBody
	public String processMonitorWo(
			@RequestBody CmsWorkOrderSimple woSimple,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) throws SensorException {	
		
		logger.info("Got request to add monitors for " + woSimple.rfcCi.getCiName() + "; ciId=" + woSimple.rfcCi.getCiId());
		
		if (woSimple.getPayLoad().get("WatchedBy") != null 
			&& woSimple.getPayLoad().get("WatchedBy").size()>0
			&& woSimple.getPayLoad().get("RealizedAs") != null
			&& woSimple.getPayLoad().get("RealizedAs").size()>0) {
			long ciId = woSimple.rfcCi.getCiId();
			long manifestId = woSimple.getPayLoad().get("RealizedAs").get(0).getCiId();
			
			String baseKey  = new Long(ciId).toString() +":";
		
			if (woSimple.getRfcCi().getRfcAction().equals("add") || 
				woSimple.getRfcCi().getRfcAction().equals("update") ||
				woSimple.getRfcCi().getRfcAction().equals("replace") ) {
				for (CmsRfcCISimple monitor : woSimple.getPayLoad().get("WatchedBy")) {
					//sensor.addCiThresholds(ciId, source, manifestId, thresholdsJson);
					sensor.addCiThresholds(ciId, manifestId, monitor);
					try {
						phDao.createHeader(baseKey+monitor.getCiName(), monitor);
					} catch (IOException e) {
						logger.error("Unable to create perf header for:"+monitor.getCiName(),e);
					}
				}
			} else {
				sensor.removeCi(ciId, manifestId);
			}
		}
		return "{\"success\"}";
	}
	*/


    /**
     * Start tracking.
     *
     * @param woSimple the wo simple
     * @param scope    the scope
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST, value = "/monitors/start")
    @ResponseBody
    public String startTracking(
            @RequestBody CmsWorkOrderSimple woSimple,
            @RequestHeader(value = "X-Cms-Scope", required = false) String scope) {

        logger.info("Inserting fake events for " + woSimple.rfcCi.getCiName() + "; ciId=" + woSimple.rfcCi.getCiId());

        if (woSimple.getPayLoad().get("WatchedBy") != null
                && woSimple.getPayLoad().get("WatchedBy").size() > 0
                && woSimple.getPayLoad().get("RealizedAs") != null
                && woSimple.getPayLoad().get("RealizedAs").size() > 0) {
            long ciId = woSimple.rfcCi.getCiId();
            long manifestId = woSimple.getPayLoad().get("RealizedAs").get(0).getCiId();

            if (woSimple.getRfcCi().getRfcAction().equals("add") || woSimple.getRfcCi().getRfcAction().equals("update")) {
                for (CmsRfcCISimple monitor : woSimple.getPayLoad().get("WatchedBy")) {
                    String source = monitor.getCiName();
                    sensor.insertFakeEvent(ciId, manifestId, source);
                }
            }
        }
        return "{\"success\"}";
    }


    @RequestMapping(method = RequestMethod.GET, value = "/monitors/restore")
    @ResponseBody
    public String restoreMonitors(
            @RequestParam(value = "writeMode", required = false, defaultValue = "false") boolean writeMode,
            @RequestParam(value = "manifestId", required = false) Long manifestId) {
        Executors.newSingleThreadExecutor().execute(() -> restorer.restore(writeMode, manifestId));
        return "Restore process started. WriteMode:" + writeMode + ". Monitor log files";
    }


    /**
     * Gets the cI open events.
     *
     * @param ciId the ci id
     * @return the cI open events
     */
    @RequestMapping(value = "/ops/events/{ciId}", method = RequestMethod.GET)
    @ResponseBody
    public Map<Long, Map<String, List<CiOpenEvent>>> getCIOpenEvents(@PathVariable long ciId) {
        List<CiOpenEvent> openEvents = oeDao.getCiOpenEvents(ciId);
        Map<Long, Map<String, List<CiOpenEvent>>> result = new HashMap<Long, Map<String, List<CiOpenEvent>>>();
        result.put(ciId, convertOpenEvents(openEvents));
        return result;
    }

    /**
     * Gets the open events.
     *
     * @param ciIdsStr the ci ids str
     * @return the open events
     */
    @RequestMapping(value = "/ops/events", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<Long, Map<String, List<CiOpenEvent>>>> getOpenEvents(
            @RequestParam(value = "ciIds", required = true) String ciIdsStr) {

        String[] ciIdsAr = ciIdsStr.split(",");
        List<Long> ciIds = new ArrayList<Long>();
        for (String ciId : ciIdsAr) {
            ciIds.add(Long.valueOf(ciId));
        }
        return getOpenEventsForList(ciIds);
    }

    private List<Map<Long, Map<String, List<CiOpenEvent>>>> getOpenEventsForList(List<Long> ciIds) {
        Map<Long, List<CiOpenEvent>> openEvents = oeDao.getCiOpenEvents(ciIds);
        List<Map<Long, Map<String, List<CiOpenEvent>>>> result = new ArrayList<Map<Long, Map<String, List<CiOpenEvent>>>>();
        for (Long key : openEvents.keySet()) {
            Map<Long, Map<String, List<CiOpenEvent>>> entry = new HashMap<Long, Map<String, List<CiOpenEvent>>>();
            entry.put(key, convertOpenEvents(openEvents.get(key)));
            result.add(entry);
        }
        return result;
    }

    /**
     * Process open events.
     *
     * @param ciIdsAr the ci ids
     * @return the list
     */
    @RequestMapping(method = RequestMethod.POST, value = "/ops/events")
    @ResponseBody
    public List<Map<Long, Map<String, List<CiOpenEvent>>>> processOpenEvents(
            @RequestBody Long[] ciIdsAr) {
        List<Long> ciIds = new ArrayList<>();
        Collections.addAll(ciIds, ciIdsAr);
        return getOpenEventsForList(ciIds);
    }


    /**
     * Gets the c istate.
     *
     * @param ciId the ci id
     * @return the c istate
     */
    @RequestMapping(value = "/ops/states/{ciId}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getCIstate(@PathVariable long ciId) {
        String ciState = coProcessor.getCIstate(ciId);
        Map<String, String> result = new HashMap<String, String>();
        result.put("id", String.valueOf(ciId));
        result.put("state", ciState);
        return result;
    }

    /**
     * Gets the c istate hist.
     *
     * @param ciId  the ci id
     * @param start the start
     * @param end   the end
     * @param count the count
     * @return the c istate hist
     */
    @RequestMapping(value = "/ops/states/{ciId}/history", method = RequestMethod.GET)
    @ResponseBody
    public List<CiChangeStateEvent> getCIstateHist(@PathVariable long ciId,
                                                   @RequestParam(value = "start", required = false) Long start,
                                                   @RequestParam(value = "end", required = false) Long end,
                                                   @RequestParam(value = "count", required = false) Integer count) {

        return coProcessor.getCiStateHistory(ciId, start, end, count);
    }


    /**
     * Gets the cis states.
     *
     * @param ciIdsStr the ci ids str
     * @return the cis states
     */
    @RequestMapping(value = "/ops/states", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, String>> getCisStatesGet(
            @RequestParam(value = "ciIds", required = true) String ciIdsStr) {

        String[] ciIdsAr = ciIdsStr.split(",");
        List<Long> ciIds = new ArrayList<Long>();
        for (String ciId : ciIdsAr) {
            try {
                ciIds.add(Long.valueOf(ciId));
            } catch (NumberFormatException ex) { //Silently ignore format errors
            }
        }

        return getCisStates(ciIds);
    }

    /**
     * Gets the cis states.
     *
     * @param ciIdsAr array of ci ids
     * @return the cis states
     */
    @RequestMapping(value = "/ops/states", method = RequestMethod.POST)
    @ResponseBody
    public List<Map<String, String>> getCisStatesPost(
            @RequestBody Long[] ciIdsAr) {
        List<Long> ciIds = new ArrayList<>();
        Collections.addAll(ciIds, ciIdsAr);
        return getCisStates(ciIds);
    }

    private List<Map<String, String>> getCisStates(List<Long> ciIds) {

        Map<Long, String> states = coProcessor.getCisStates(ciIds);

        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        for (Long ciId : states.keySet()) {
            Map<String, String> entry = new HashMap<String, String>();
            entry.put("id", String.valueOf(ciId));
            entry.put("state", states.get(ciId));
            result.add(entry);
        }

        return result;
    }

    /**
     * Gets the cis states.
     *
     * @param ciIdsStr the ci ids str
     * @return the cis states
     */
    @RequestMapping(value = "/ops/components/states/counts", method = RequestMethod.GET)
    @ResponseBody
    public Map<Long, Map<String, Long>> getManifestStatesCountsGet(
            @RequestParam(value = "ids", required = false) String ciIdsStr) {

        if (ciIdsStr != null) {
            String[] ciIdsAr = ciIdsStr.split(",");
            List<Long> ciIds = new ArrayList<Long>();
            for (String ciId : ciIdsAr) {
                try {
                    ciIds.add(Long.valueOf(ciId));
                } catch (NumberFormatException ex) { //Silently ignore format errors
                }
            }

            return coProcessor.getComponentStates(ciIds);
        } else {
            return coProcessor.getAllComponentStates();
        }
    }

    /**
     * Gets the all componentsstates.
     *
     * @param ciIdsStr the ci ids str
     * @return the cis states
     */
    @RequestMapping(value = "/ops/components/all/states", method = RequestMethod.GET)
    @ResponseBody
    public Map<Long, Map<String, Long>> getAllManifestStates() {
        return coProcessor.getAllComponentStates();
    }

    /**
     * Gets the cis states.
     *
     * @param ciIdsStr the ci ids str
     * @return the cis states
     */
    @RequestMapping(value = "/ops/components/states/reset", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> resetManifestStates(
            @RequestParam(value = "ids", required = false) String ciIdsStr) {
        if (ciIdsStr != null) {
            String[] ciIdsAr = ciIdsStr.split(",");
            List<Long> ciIds = new ArrayList<Long>();
            for (String ciId : ciIdsAr) {
                try {
                    ciIds.add(Long.valueOf(ciId));
                } catch (NumberFormatException ex) { //Silently ignore format errors
                }
            }
            coProcessor.resetManifestStates(ciIds);
        } else {
            coProcessor.resetManifestStates();
        }
        Map<String, String> result = new HashMap<String, String>();
        result.put("result", "states recalculated");
        return result;
    }


    /**
     * Gets the cis states.
     *
     * @param ciIdsStr the ci ids str
     * @return the cis states
     */
    @RequestMapping(value = "/ops/components/states/count", method = RequestMethod.POST)
    @ResponseBody
    public Map<Long, Map<String, Long>> getManifestStatesCounts(
            @RequestBody Long[] idsAr) {

        List<Long> ciIds = new ArrayList<Long>();
        for (Long ciId : idsAr) {
            ciIds.add(ciId);
        }
        return coProcessor.getComponentStates(ciIds);
    }


    /**
     * Gets the cis states.
     *
     * @param ciIdsStr the ci ids str
     * @return the cis states
     */
    @RequestMapping(value = "/ops/components/states", method = RequestMethod.POST)
    @ResponseBody
    public Map<Long, Map<String, Integer>> getManifestStatesPost(
            @RequestBody Long[] idsAr) {

        List<Long> ciIds = new ArrayList<Long>();
        for (Long ciId : idsAr) {
            ciIds.add(ciId);
        }
        return coProcessor.getManifestStates(ciIds);
    }

    /**
     * Gets the cis states.
     *
     * @param ciIdsStr the ci ids str
     * @return the cis states
     */
    @RequestMapping(value = "/ops/components/old/states", method = RequestMethod.GET)
    @ResponseBody
    public Map<Long, Map<String, Integer>> getManifestStatesGet(
            @RequestParam(value = "ids", required = true) String ciIdsStr) {
        String[] ciIdsAr = ciIdsStr.split(",");
        List<Long> ciIds = new ArrayList<Long>();
        for (String ciId : ciIdsAr) {
            ciIds.add(Long.valueOf(ciId));
        }
        return coProcessor.getManifestStates(ciIds);
    }

    /**
     * Close c ievent.
     *
     * @param ciId      the ci id
     * @param eventName the event name
     * @return the list
     */
    @RequestMapping(value = "/ops/events/{ciId}/close/{eventName}", method = RequestMethod.GET)
    @ResponseBody
    public List<CiOpenEvent> closeCIevent(@PathVariable long ciId, @PathVariable String eventName) {
        oeDao.removeOpenEventForCi(ciId, eventName);
        return oeDao.getCiOpenEvents(ciId);
    }


    /**
     * Gets the event history.
     *
     * @param ciId       the ci id
     * @param source     the source
     * @param thresholds the thresholds
     * @param start      the start
     * @param end        the end
     * @param count      the count
     * @return the event history
     */
    @RequestMapping(value = "/ops/events/{ciId}/history/{source}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<OpsEvent>> getEventHistory(@PathVariable long ciId,
                                                       @PathVariable String source,
                                                       @RequestParam(value = "threshold", required = true) String[] thresholds,
                                                       @RequestParam(value = "start", required = false) Long start,
                                                       @RequestParam(value = "end", required = false) Long end,
                                                       @RequestParam(value = "count", required = false) Integer count) {

        int countInt = EVNT_CNT_DFLT;
        if (count != null) {
            countInt = count.intValue();
        }
        Map<String, List<OpsEvent>> eventsMap = new HashMap<String, List<OpsEvent>>();

        for (String threshold : thresholds) {
            List<OpsEvent> events = oeDao.getOpsEventHistory(ciId + source + ":" + threshold, start, end, countInt);
            eventsMap.put(threshold, events);
        }

        return eventsMap;
    }

    /**
     * Gets the event history.
     *
     * @param ciId       the ci id
     * @param source     the source
     * @param thresholds the thresholds
     * @param start      the start
     * @param end        the end
     * @param count      the count
     * @return the event history
     */
    @RequestMapping(value = "/ops/events/{ciId}/history", method = RequestMethod.GET)
    @ResponseBody
    public List<OpsEvent> getAllEventHistory(@PathVariable long ciId,
                                             @RequestParam(value = "start", required = false) Long start,
                                             @RequestParam(value = "end", required = false) Long end,
                                             @RequestParam(value = "count", required = false) Integer count) {

        int countInt = EVNT_CNT_DFLT;
        if (count != null) {
            countInt = count.intValue();
        }
        List<OpsEvent> events = oeDao.getOpsEventHistory(ciId, start, end, countInt);

        return events;
    }


    /**
     * Gets the loaded stmts.
     *
     * @return the loaded stmts
     */
    @RequestMapping(value = "/esper/stmts", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getLoadedStmts() {
        return sensor.getAllLoadedStmts();
    }

    private Map<String, List<CiOpenEvent>> convertOpenEvents(List<CiOpenEvent> events) {

        Map<String, List<CiOpenEvent>> openEvents = new HashMap<String, List<CiOpenEvent>>();

        for (CiOpenEvent event : events) {
            String[] parts = event.getName().split(":");
            if (!openEvents.containsKey(parts[0])) {
                openEvents.put(parts[0], new ArrayList<CiOpenEvent>());
            }
            event.setName(parts[1]);
            fixTS(event);
            openEvents.get(parts[0]).add(event);

        }
        return openEvents;
    }


    //Fixing timestamps for CIs with values less than 13 digits which implies bad values may be 
    // in cassandra. eg . 
	/*Eg. 
	<?xml version="1.0" encoding="UTF-8"?>
        <map>
          <entry>
            <long>7193613</long>
            <map>
              <entry>
                <string>-compute-cpu</string>
                <list>
                  <com.oneops.ops.events.CiOpenEvent>
                    <name>HighCpuUsage</name>
                    <state>notify</state>
                    <timestamp>1394079689</timestamp>
                  </com.oneops.ops.events.CiOpenEvent>
                </list>
              </entry>
            </map>
          </entry>
        </map>
	*/
    private void fixTS(CiOpenEvent event) {
        String timestampS = String.valueOf(event.getTimestamp());
        if (StringUtils.length(timestampS) < LENGTH_OF_TIMESTAMP_FOR_TWO_CENTURIES) {
            String timeStamp = StringUtils.rightPad(timestampS, LENGTH_OF_TIMESTAMP_FOR_TWO_CENTURIES, '0');
            logger.info("Corrected TS for  " + event.getName() + "oldTs: " + timestampS + " :correctedValue: " + timeStamp);
            event.setTimestamp(Long.valueOf(timeStamp));
        } else if (StringUtils.length(timestampS) > LENGTH_OF_TIMESTAMP_FOR_TWO_CENTURIES) {
            String timeStamp = StringUtils.substring(timestampS, 0, LENGTH_OF_TIMESTAMP_FOR_TWO_CENTURIES);
            event.setTimestamp(Long.valueOf(timeStamp));
            logger.info("Corrected TS for" + event.getName() + " timestamp fixed with " + timeStamp);
        }
    }

    /**
     * Gets the loaded stmts.
     *
     * @return the loaded stmts
     */
    @RequestMapping(value = "/esper/stmts/count", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Integer> getLoadedStmtsCount() {

        Map<String, Integer> result = new HashMap<String, Integer>();
        result.put("Statements loaded:", sensor.getAllLoadedStmts().size());
        return result;
    }

    /**
     * Load stmts.
     *
     * @param ciId   the ci id
     * @param source the source
     * @return the map
     */
    @RequestMapping(value = "/esper/stmts/{ciId}/load/{source}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> loadStmts(@PathVariable long ciId,
                                         @PathVariable String source) {
        sensor.loadStatements(ciId, source);
        return sensor.getAllLoadedStmts();
    }

    /**
     * Send empty event.
     *
     * @param ciId       the ci id
     * @param source     the source
     * @param manifestId the manifest id
     * @return the string
     */
    @RequestMapping(value = "/ops/events/{ciId}/send", method = RequestMethod.GET)
    @ResponseBody
    public String sendEmptyEvent(@PathVariable long ciId,
                                 @RequestParam(value = "source", required = true) String source,
                                 @RequestParam(value = "manifestId", required = true) long manifestId) {

        sensor.insertFakeEvent(ciId, manifestId, source);
        return "Sent";
    }

    /**
     * Add ciId to Log set.
     *
     * @param ciId the ci id
     */
    @RequestMapping(value = "/log/{ciId}/{action}", method = RequestMethod.GET)
    @ResponseBody
    public String setLogingForCi(@PathVariable long ciId,
                                 @PathVariable String action) {
        if ("remove".equals(action)) {
            sensorListener.removeCiIdToLog(ciId);
            return "removed";
        } else {
            sensorListener.addCiIdToLog(ciId);
            return "added ciId=" + ciId + " to loging set, check /tmp/sensor.log";
        }
    }


    /**
     * Check timestamp.
     *
     * @return the string
     */
    @RequestMapping(value = "/channels/status", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, ChannelState> getChannelStates() {
        return sensorHeartBeat.getChannelsStatus();
    }

    @RequestMapping(value = "/utils/validatethresholds", method = RequestMethod.GET)
    @ResponseBody
    public String validateThresholds() {
        sensorTools.validateThresholds();
        return "Done";
    }


    @RequestMapping(value = "/status", method = RequestMethod.GET)
    @ResponseBody
    public String checkStatus() {
        return "Sensor is up";
    }

    @RequestMapping(value = "/events/count", method = RequestMethod.GET)
    @ResponseBody
    public String getEventProcessingCount() {
        return "<count>" + SensorListenerContainer.COUNT.get() + "</count>";
    }
}
