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
package com.oneops.sensor;

import com.espertech.esper.client.*;
import com.google.gson.Gson;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.ops.CiOpsProcessor;
import com.oneops.ops.dao.OpsEventDao;
import com.oneops.ops.events.OpsCloseEvent;
import com.oneops.ops.events.OpsEvent;
import com.oneops.sensor.domain.DelayedPerfEvent;
import com.oneops.sensor.domain.SensorStatement;
import com.oneops.sensor.domain.ThresholdStatements;
import com.oneops.sensor.events.BasicEvent;
import com.oneops.sensor.events.PerfEvent;
import com.oneops.sensor.exceptions.SensorException;
import com.oneops.sensor.thresholds.Threshold;
import com.oneops.sensor.thresholds.ThresholdsDao;
import com.oneops.sensor.util.ChannelDownEvent;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;

import static com.oneops.sensor.StmtBuilder.*;
import static java.lang.System.getProperty;


/**
 * Sensor, complex event processing engine for OneOps metric events.
 */
public class Sensor {

    private static Logger logger = Logger.getLogger(Sensor.class);

    private static final String HEARTBEAT = "heartbeat";
    private static final String DURATION = "duration";
    private static final String METRIC = "metric";
    private static final String ESPER_THREADS = "com.oneops.sensor.esper.threads";
    private static final String RELOAD_TRS = "com.oneops.sensor.thresholds.checkonevent";
    private static final int ESPER_INBOUND_THREADS = Integer.valueOf(getProperty(ESPER_THREADS, "16"));
    private static final int ESPER_OUTBOUND_THREADS = Integer.valueOf(getProperty(ESPER_THREADS, "16"));
    private static final int ESPER_ROUTE_EXEC_THREADS = Integer.valueOf(getProperty(ESPER_THREADS, "16"));
    private static final int ESPER_TIMER_THREADS = Integer.valueOf(getProperty(ESPER_THREADS, "16"));
    private static final boolean CHECK_TR_ON_EVENT = Boolean.valueOf(getProperty(RELOAD_TRS, "false"));
    public static final String ROW_COUNT = "com.oneops.sensor.events.batchsize";
    public static final int READ_ROWCOUNT = Integer.valueOf(getProperty(ROW_COUNT, "1000"));


    private final Gson gson = new Gson();
    private final Random random = new Random();
    private final Map<Long, Map<String, ThresholdStatements>> loadedThresholds = new HashMap<>();

    private int instanceId;
    private int poolSize;
    private boolean isInited = false;
    private EPServiceProvider epService;
    private ThresholdsDao tsDao;
    private OpsEventDao opsEventDao;
    private StmtBuilder stmtBuilder;
    private CiOpsProcessor coProcessor;
    private CiStateProcessor ciStateProcessor;
    private Map<String, UpdateListener> listeners;
    private int minHeartbeatSeedDelay = 300;
    private int heartbeatRandomDelay = 30;


    /**
     * Sets the statement builder
     *
     * @param stmtBuilder
     */
    public void setStmtBuilder(StmtBuilder stmtBuilder) {
        this.stmtBuilder = stmtBuilder;
    }

    /**
     * Sets the CiOps processsor
     *
     * @param coProcessor
     */
    public void setCoProcessor(CiOpsProcessor coProcessor) {
        this.coProcessor = coProcessor;
    }

    /**
     * Accessor for CEP provider.
     *
     * @return {@link EPServiceProvider}
     */
    public EPServiceProvider getEpService() {
        return epService;
    }

    /**
     * Sets the threshold dao
     *
     * @param tsDao the new ts dao
     */
    public void setTsDao(ThresholdsDao tsDao) {
        this.tsDao = tsDao;
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
     * Sets the listeners.
     *
     * @param listeners the listeners
     */
    public void setListeners(Map<String, UpdateListener> listeners) {
        this.listeners = listeners;
    }


    /**
     * Initalizes the Sensor
     *
     * @param instanceId instance id where the sensor running
     * @param poolSize   sensor poolsize value
     * @throws Exception throws if any error while initializing sensor.
     */
    public void init(int instanceId, int poolSize) throws Exception {
        long start = System.currentTimeMillis();
        logger.info(">>> Sensor initialization started.");

        this.instanceId = instanceId - 1;
        this.poolSize = poolSize;

        Configuration cfg = new Configuration();
        cfg.addEventType("PerfEvent", PerfEvent.class.getName());
        cfg.addEventType("DelayedPerfEvent", DelayedPerfEvent.class.getName());
        cfg.addEventType("OpsEvent", OpsEvent.class.getName());
        cfg.addEventType("OpsCloseEvent", OpsCloseEvent.class.getName());
        cfg.addEventType("ChannelDownEvent", ChannelDownEvent.class.getName());

        ConfigurationEngineDefaults.Threading ct = cfg.getEngineDefaults().getThreading();
        ct.setThreadPoolInbound(true);
        ct.setThreadPoolInboundNumThreads(ESPER_INBOUND_THREADS);
        ct.setThreadPoolOutbound(true);
        ct.setThreadPoolOutboundNumThreads(ESPER_OUTBOUND_THREADS);
        ct.setThreadPoolRouteExec(true);
        ct.setThreadPoolRouteExecNumThreads(ESPER_ROUTE_EXEC_THREADS);
        ct.setThreadPoolTimerExec(true);
        ct.setThreadPoolTimerExecNumThreads(ESPER_TIMER_THREADS);

        this.epService = EPServiceProviderManager.getDefaultProvider(cfg);
        loadAllStatements();
        this.isInited = true;

        long tt = TimeUnit.SECONDS.convert((System.currentTimeMillis() - start), TimeUnit.MILLISECONDS);
        logger.info(">>> Sensor initialization completed. Took " + tt + " seconds!!!");
    }

    /**
     * Cleanup.
     */
    public void cleanup() {
        this.epService.destroy();
    }

    /**
     * Stops the sensor engine.
     */
    public void stop() {
        this.epService.destroy();
        this.epService = null;
    }

    /**
     * Loads the default statements.
     */
    private void initDefaultStatements() {
        addStatementToEngine("opsEventReset", STMT_RESET, "CloseEventListener");
        addStatementToEngine("opsHeartbeatReset", STMT_RESET_HEARTBEAT, "CloseEventListener");
        addStatementToEngine("opsHeartbeatReTrigger", STMT_RETRIGGER_HEARTBEAT, "OpsEventListener");
        addStatementToEngine("opsDelayPerfEvent", STMT_DELAY_PERF_EVENT, null);
    }

    /**
     * Adds channel down statement and listener.
     */
    private void initChannelDownStatement() {
        addStatementToEngine("channelDownTrigger", STMT_TRIGGER_CHANNELDOWN, "ChannelDownListener");
    }

    /**
     * Add a EPL statement and a listener to it.
     *
     * @param stmtName     statement name
     * @param stmt         statement
     * @param listenerName statement listener.
     */
    private void addStatementToEngine(String stmtName, String stmt, String listenerName) {
        EPStatement oldStmt = epService.getEPAdministrator().getStatement(stmtName);
        if (oldStmt != null) {
            if (stmt.equalsIgnoreCase(oldStmt.getText())) {
                return;
            } else {
                oldStmt.destroy();
            }
        }
        EPStatement statement = epService.getEPAdministrator().createEPL(stmt, stmtName);
        if (listenerName != null) {
            statement.addListener(listeners.get(listenerName));
        }
        logger.debug("Loaded to Esper EPL: " + stmt);
    }

    /**
     * Sends a single OpsEvent to esper engine.
     *
     * @param e OpsEvent
     */
    void sendOpsEvent(OpsEvent e) {
        logger.info("Loading OpsEvent(CiId = " + e.getCiId() + ", manifestId = " + e.getManifestId() + ", name = " + e.getName() + ", state = " + e.getState() + ")");
        this.epService.getEPRuntime().sendEvent(e);
    }


    /**
     * Get the open OpsEvent stream by setting proper manifest id.
     * OpenEvents --> Filter(validate manifest Id) --> filter (instance id).
     *
     * @return Observable of OpsEvents.
     */
    private Observable<OpsEvent> getAllOpenEvents() {
        return opsEventDao.getOpenEvents(READ_ROWCOUNT).filter(event -> {
            // Filter the events with proper manifest id.
            Long mId = tsDao.getManifestId(event.getCiId());
            if (mId == null) {
                logger.error("Orphan CI. Can not find manifestId for ciId: " + event.getCiId() + ", name:" + event.getName() + ", state: " + event.getState());
                opsEventDao.removeCi(event.getCiId());
                return false;
            }

            if (mId > 0 && ((mId % this.poolSize) == this.instanceId)) {
                event.setManifestId(mId.longValue());
                return true;
            } else {
                // Dicard messages for other instance ids
                return false;
            }
        });
    }

    /**
     * Adds the ci thresholds.
     *
     * @param ciId       the ci id
     * @param manifestId the manifest id
     * @param monitor    the monitor
     * @throws SensorException
     */
    public void addCiThresholdsList(long ciId, long manifestId, List<CmsRfcCISimple> monitors) throws SensorException {

        if (!isInited || (manifestId % this.poolSize) != this.instanceId) {
            // this is not my manifestId will post it on mgmt queue for other guy to pick up
            throw new SensorException("Got Monitor request for the wrong instance - manifestId:" + manifestId + "; pool size:" + this.poolSize + "; my insatnceId:" + this.instanceId);
        }

        Set<String> processedMonitors = new HashSet<>();

        for (CmsRfcCISimple monitor : monitors) {

            if (monitor.getCiAttributes().containsKey("enable") && monitor.getCiAttributes().get("enable").equals("false")) {
                continue;
            }

            long checksum = 0;

            String thresholdsJson = monitor.getCiAttributes().get("thresholds");
            String source = monitor.getCiName();


            if (thresholdsJson != null) {
                CRC32 crc = new CRC32();
                String crcStr = thresholdsJson + monitor.getCiAttributes().get(HEARTBEAT) + monitor.getCiAttributes().get(DURATION);
                crc.update(crcStr.getBytes());
                checksum = crc.getValue();
            } else {
                // need to clean up thresholds
                continue;
            }

            processedMonitors.add(source);

            //String key = manifestId + source;
            ThresholdStatements trStmt = loadedThresholds.containsKey(manifestId) ? loadedThresholds.get(manifestId).get(source) : null;
            if (trStmt == null) {
                //load stmts
            	boolean isHeartBeat = monitor.getCiAttributes().get(HEARTBEAT).equals("true");
            	String hbDuration = monitor.getCiAttributes().get(DURATION);
                persistAndaddToEngine(ciId,
                        manifestId,
                        source,
                        checksum,
                        thresholdsJson,
                        isHeartBeat,
                        hbDuration);
                
              //create a seed event if this is a new heartbeat monitor 
                if (isHeartBeat) {
                	int durationInSec = Integer.parseInt(hbDuration) * 60;
                	int delay = minHeartbeatSeedDelay + random.nextInt(durationInSec);
                	logger.info("creating seed event for ciId " + ciId + ", source " +  source + ", with delay : " + delay);
                	insertFakeEventWithDelay(ciId, manifestId, source, delay);
                }
            } else if (trStmt.getChecksum() != checksum || monitor.getCiAttributes().get(HEARTBEAT).equals("true") != trStmt.isHeartbeat()) {
                // if checksum is different we assume there was an monitor update
                // we need to remove old stmts and insert new ones
                // but before that lets insert fake event to clear out heart beats
                // if this new mon is not a heartbeat one
                if (!monitor.getCiAttributes().get(HEARTBEAT).equals("true")) {
                    insertFakeEvent(ciId, manifestId, source);
                }
                for (String eplName : trStmt.getStmtNames()) {
                    removeStmtFromEngine(manifestId, source, eplName);
                }
                ciStateProcessor.updateState4MonitorRemoval(manifestId, source);
                loadedThresholds.get(manifestId).remove(source);

                persistAndaddToEngine(ciId,
                        manifestId,
                        source,
                        checksum,
                        thresholdsJson,
                        monitor.getCiAttributes().get(HEARTBEAT).equals("true"),
                        monitor.getCiAttributes().get(DURATION));
            }
        }
        // now we need to clean up the deleted monitors
        if (loadedThresholds.containsKey(manifestId)) {
            Set<String> monsToRemove = new HashSet<>();
            for (String loadedMon : loadedThresholds.get(manifestId).keySet()) {
                if (!processedMonitors.contains(loadedMon)) {
                    //this is old monitor that need to be removed
                    //insert fake event to shut down Heartbeat retrigger
                    insertFakeEvent(ciId, manifestId, loadedMon);
                    //and do it for the rest bom guys
                    for (long ciMapedBomId : tsDao.getManifestCiIds(manifestId)) {
                        insertFakeEvent(ciMapedBomId, manifestId, loadedMon);
                    }

                    ThresholdStatements trStmt = loadedThresholds.get(manifestId).get(loadedMon);
                    for (String eplName : trStmt.getStmtNames()) {
                        removeStmtFromEngine(manifestId, loadedMon, eplName);
                    }
                    ciStateProcessor.updateState4MonitorRemoval(manifestId, loadedMon);
                    monsToRemove.add(loadedMon);
                    tsDao.removeManifestThreshold(manifestId, loadedMon);
                }
            }
            for (String monToRemove : monsToRemove) {
                loadedThresholds.get(manifestId).remove(monToRemove);
            }
        }
    }


    /*
     * Adds the ci thresholds.
     *
     * @param ciId       the ci id
     * @param manifestId the manifest id
     * @param monitor    the monitor
     * @throws SensorException
     */
    /*
    private void addCiThresholds(long ciId, long manifestId, CmsRfcCISimple monitor) throws SensorException {
		
		if (!isInited || (manifestId % this.poolSize) != this.instanceId) {
			// this is not my manifestId will post it on mgmt queue for other guy to pick up
			throw new SensorException("Got Monitor request for the wrong instance - manifestId:" + manifestId + "; pool size:" + this.poolSize + "; my insatnceId:" + this.instanceId);
		}
		
		
		long checksum = 0;
		
		String thresholdsJson = monitor.getCiAttributes().get("thresholds");
		String source = monitor.getCiName();
		
		
		if (thresholdsJson != null ) {
			CRC32 crc = new CRC32();
			String crcStr = thresholdsJson + monitor.getCiAttributes().get(HEARTBEAT) + monitor.getCiAttributes().get(DURATION);
			crc.update(crcStr.getBytes());
			checksum = crc.getValue();
		}
		//String key = manifestId + source;
		ThresholdStatements trStmt = loadedThresholds.containsKey(manifestId) ? loadedThresholds.get(manifestId).get(source) : null;
		if (trStmt == null) {
			//load stmts
			persistAndaddToEngine(ciId, 
						   manifestId, 
						   source, 
						   checksum, 
						   thresholdsJson,
						   monitor.getCiAttributes().get(HEARTBEAT).equals("true"),
						   monitor.getCiAttributes().get(DURATION));
		} else if(trStmt.getChecksum() != checksum || monitor.getCiAttributes().get(HEARTBEAT).equals("true") != trStmt.isHeartbeat()) {
			// if checksum is different we assume there was an monitor update
			// we need to remove old stmts and insert new ones
			for (String eplName : trStmt.getStmtNames()) {
				removeStmtFromEngine(manifestId, source, eplName);
			}
			loadedThresholds.get(manifestId).remove(source);
			persistAndaddToEngine(ciId, 
					   manifestId, 
					   source, 
					   checksum, 
					   thresholdsJson,
					   monitor.getCiAttributes().get(HEARTBEAT).equals("true"),
					   monitor.getCiAttributes().get(DURATION));
		} else {
			//seems like it's just a new ci for the same manifestId, we just need to add manifest mapping
			tsDao.addManifestMap(ciId, manifestId);
		}
		//at this point the statements already loaded
	}
*/
    private void removeStmtFromEngine(long manifestId, String source, String eplName) {
        EPStatement oldStmt = epService.getEPAdministrator().getStatement(eplName);
        if (oldStmt != null) {
            oldStmt.stop();
            oldStmt.removeAllListeners();
            oldStmt.destroy();
        }
    }


    /**
     * Removes the ci.
     *
     * @param ciId the ci id
     * @throws SensorException
     */
    public void removeCi(long ciId, long manifestId) throws SensorException {
        //Long manifestId = tsDao.getManifestId(ciId);
        if (!isInited || (manifestId % this.poolSize) != this.instanceId) {
            // this is not my manifestId will post it on mgmt queue for other guy to pick up
            throw new SensorException("Got Monitor request for the wrong instance - manifestId:" + manifestId + "; pool size:" + this.poolSize + "; my insatnceId:" + this.instanceId);
        }
        logger.info("Removing ciId = " + ciId + " from the manifest map");
        ciStateProcessor.updateState4CiRemoval(ciId, manifestId);
        int remainingBoms = coProcessor.removeManifestMap4CiRemoval(ciId, manifestId);
        opsEventDao.removeCi(ciId);
        if (remainingBoms == 0) {
            //remove thresholds form the engine
            if (loadedThresholds.containsKey(manifestId)) {
                for (String source : loadedThresholds.get(manifestId).keySet()) {
                    for (String eplName : loadedThresholds.get(manifestId).get(source).getStmtNames()) {
                        removeStmtFromEngine(manifestId, source, eplName);
                        logger.info("Removed " + eplName + " from the engine");
                    }
                    tsDao.removeManifestThreshold(manifestId, source);
                    //insert fake event to shut down Heartbeat retrigger
                    insertFakeEvent(ciId, manifestId, source);
                }
                loadedThresholds.remove(manifestId);
            }
        }
    }


    private void persistAndaddToEngine(long ciId, long manifestId, String source, long checksum, String thresholdsJson, boolean isHeartbeat, String hbDuration) {
        // This will parse the thresholds definitions, persist them in cassandra and load stmts in esper
        persistThreshold(ciId, manifestId, source, checksum, thresholdsJson, isHeartbeat, hbDuration);
        ThresholdStatements stmts = stmtBuilder.getThresholdStatements(manifestId, source, checksum, thresholdsJson, isHeartbeat, hbDuration);

        for (String stmtName : stmts.getStatements().keySet()) {
            SensorStatement stmt = stmts.getStatements().get(stmtName);
            addStatementToEngine(stmt.getStmtName(), stmt.getStmtText(), stmt.getListenerName());
        }

        if (stmts.getStmtNames().size() > 0) {
            // Register monitor
            // String key = manifestId + source;
            if (!loadedThresholds.containsKey(manifestId)) {
                loadedThresholds.put(manifestId, new HashMap<String, ThresholdStatements>());
            }
            loadedThresholds.get(manifestId).put(source, stmts);
        } else {
            logger.debug("Got empty threshols for ciId:" + ciId);
        }
    }


    /**
     * Insert fake perf event to esper engine.
     *
     * @param ciId       the ci id
     * @param manifestId the manifest id
     * @param source     the source
     */
    public void insertFakeEvent(long ciId, long manifestId, String source) {
        // Lets insert fake events so we start tracking the missing hearbeats
        PerfEvent event = new PerfEvent();
        event.setCiId(ciId);
        event.setManifestId(manifestId);
        event.setSource(source);
        event.setTimestamp(System.currentTimeMillis());
        logger.debug("Sent PerfEvent to esper :" + gson.toJson(event));
        this.epService.getEPRuntime().sendEvent(event);
    }
    
    public void insertFakeEventWithDelay(long ciId, long manifestId, String source, int delay) {
        // Lets insert fake events so we start tracking the missing hearbeats
        DelayedPerfEvent delayedEvent = new DelayedPerfEvent();
        PerfEvent event = new PerfEvent();
        event.setCiId(ciId);
        event.setManifestId(manifestId);
        event.setSource(source);
        event.setTimestamp(System.currentTimeMillis());
        delayedEvent.setPerfEvent(event);
        delayedEvent.setDelay(delay);
        logger.debug("Sent DelayedPerfEvent to esper :" + gson.toJson(delayedEvent));
        this.epService.getEPRuntime().sendEvent(delayedEvent);
    }


    /**
     * Insert fake event.
     *
     * @param ciId       the ci id
     * @param manifestId the manifest id
     * @param source     the source
     */
    public void insertOpenCloseFakeEvent(long ciId, long manifestId, String source) {
        OpsEvent hEvent = new OpsEvent();
        hEvent.setCiId(ciId);
        hEvent.setManifestId(manifestId);
        hEvent.setSource(source);
        hEvent.setTimestamp(System.currentTimeMillis());
        hEvent.setState("open");
        hEvent.setType(HEARTBEAT);
        logger.debug("Sent to esper event:" + gson.toJson(hEvent));
        this.epService.getEPRuntime().sendEvent(hEvent);


        PerfEvent pEvent = new PerfEvent();
        pEvent.setCiId(ciId);
        pEvent.setManifestId(manifestId);
        pEvent.setSource(source);
        pEvent.setTimestamp(System.currentTimeMillis());
        logger.debug("Sent to esper event:" + gson.toJson(pEvent));
        this.epService.getEPRuntime().sendEvent(pEvent);
    }


    /**
     * Send cep event.
     *
     * @param event the event
     */
    public void sendCEPEvent(BasicEvent event) {

        if (event instanceof PerfEvent) {
            if (CHECK_TR_ON_EVENT) {
                if (!loadedThresholds.containsKey(((PerfEvent) event).getManifestId())) {
                    boolean stmtDefined = loadStatements(((PerfEvent) event).getManifestId(), event.getSource());
                    if (!stmtDefined) {
                        return;
                    }
                }
            }

            isMetricsValid(event);
            logger.debug("Sent to esper event:" + event.getCiId() + "; " + event.getSource() + "; " + event.getBucket());
            this.epService.getEPRuntime().sendEvent(event);
        }
    }

    private boolean isMetricsValid(BasicEvent event) {
        return isMetricMapValid(event.getMetrics().getAvg())
                && isMetricMapValid(event.getMetrics().getCount())
                && isMetricMapValid(event.getMetrics().getMax())
                && isMetricMapValid(event.getMetrics().getMin())
                && isMetricMapValid(event.getMetrics().getSum());
    }

    private boolean isMetricMapValid(Map<String, Double> metrics) {
        if (metrics == null) {
            return true;
        }
        Set<String> invalidMetrics = new HashSet<>();
        for (String key : metrics.keySet()) {
            if (metrics.get(key).isNaN()) {
                invalidMetrics.add(key);
                logger.warn("Got NaN value for metric: " + key);
            }
        }
        for (String key : invalidMetrics) {
            metrics.remove(key);
        }
        return invalidMetrics.size() == 0;
    }

    /**
     * Load statements.
     *
     * @param manifestId the manifest id
     * @param source     the source
     * @return true, if successful
     */
    public boolean loadStatements(long manifestId, String source) {
        Threshold tr = getThreshold(manifestId, source);
        if (tr == null) {
            //no thresholds defined
            return false;
        }
        ThresholdStatements stmts = stmtBuilder.getThresholdStatements(
                manifestId,
                source,
                tr.getCrc(),
                tr.getThresholdJson(),
                tr.isHeartbeat(),
                tr.getHbDuration());

        for (String stmtName : stmts.getStatements().keySet()) {
            SensorStatement stmt = stmts.getStatements().get(stmtName);
            addStatementToEngine(stmt.getStmtName(), stmt.getStmtText(), stmt.getListenerName());
        }

        if (!loadedThresholds.containsKey(manifestId)) {
            loadedThresholds.put(manifestId, new HashMap<String, ThresholdStatements>());
        }
        loadedThresholds.get(manifestId).put(source, stmts);
        return true;
    }

    /**
     * Load all valid threshold statements into esper engine and emit fake events for each heartbeat thresholds.
     *
     * @return a stream of fake events
     */
    private Observable<FakeEvent> loadThresholds() {

        AtomicInteger ldStmts = new AtomicInteger(0);

        return tsDao.getAllThreshold(READ_ROWCOUNT)
                .filter(this::validateThreshold)
                .map(tr -> {
                    ThresholdStatements stmts = stmtBuilder.getThresholdStatements(
                            tr.getManifestId(),
                            tr.getSource(),
                            tr.getCrc(),
                            tr.getThresholdJson(),
                            tr.isHeartbeat(),
                            tr.getHbDuration());

                    for (String stmtName : stmts.getStatements().keySet()) {
                        SensorStatement stmt = stmts.getStatements().get(stmtName);
                        addStatementToEngine(stmt.getStmtName(), stmt.getStmtText(), stmt.getListenerName());
                        ldStmts.incrementAndGet();
                        if (ldStmts.get() % READ_ROWCOUNT == 0) {
                            logger.info("Loaded " + ldStmts.get() + " threshold statements.");
                        }
                    }

                    if (!loadedThresholds.containsKey(tr.getManifestId())) {
                        loadedThresholds.put(tr.getManifestId(), new HashMap<String, ThresholdStatements>());
                    }
                    loadedThresholds.get(tr.getManifestId()).put(tr.getSource(), stmts);
                    return tr;

                }).filter(tr -> tr.isHeartbeat()).flatMap(tr -> {

                    // Fake events for missing heartbeat
                    List<Long> mIds = tsDao.getManifestCiIds(tr.getManifestId());
                    List<FakeEvent> fes = new ArrayList<>(mIds.size());
                    for (long ciId : mIds) {
                        FakeEvent fe = new FakeEvent();
                        fe.ciId = ciId;
                        fe.manifestId = tr.getManifestId();
                        fe.source = tr.getSource();
                        fes.add(fe);
                    }
                    return Observable.from(fes);

                }).doOnCompleted(() -> logger.info(">>> Loaded total " + ldStmts.get() + " threshold statements."));
    }

    /**
     * Handle stream subscription errors
     *
     * @param t Throwable
     */
    private void handleError(Throwable t) {
        logger.error("Subscription failed.", t);
        throw new RuntimeException(t);
    }

    /**
     * Loading sensor statements and seeding heartbeat and perf events.
     * The current logic implemented using Rx Observable is,
     * <lo>
     * <li> Get a stream of all the Ci OpenEvents from cassandra
     * <li> Filter the events with proper sensor instance id.
     * <li> Bifurcate ops event stream into heartbeat and metric.
     * <li> Load all sensor statements and create a stream of fake events curresponding to heartbeat thresholds.
     * <li> Start processing all three streams in parallel (with a timeout of max 30 mins)
     * <li> Once the stream processing is complete, start seeding esper to restore the state for heart beat
     * (MHB) and open hb thresholds (RMHB)
     * </lo>
     * <p>
     * <p>
     * <pre>
     * <--Perf Events-->  <-- 5m (a) -->     <---- 15m (b) ---->       <---- 15m (b) ---->
     * o-o-o-o-o-o-o-o-o-o|.............|MHB|....................|RMHB|....................|RMHB|
     * </pre>
     * <p>
     * MHB -  Missing heartbeat
     * RMHB - Retrigger MHB
     * <p>
     */
    private void loadAllStatements() throws InterruptedException {

        initDefaultStatements();

        // Bifurcate ops events stream into heartbeat and metric.
        ConnectableObservable<OpsEvent> openEvents = getAllOpenEvents().publish();
        Observable<OpsEvent> hbeat = openEvents.filter(e -> HEARTBEAT.equals(e.getType()));
        Observable<OpsEvent> metric = openEvents.filter(e -> METRIC.equals(e.getType()));


        // Subscribe to all three streams.
        final CountDownLatch lock = new CountDownLatch(3);
        final List<FakeEvent> fes = new ArrayList<>();
        final Map<String, OpsEvent> hbOpenEvents = new HashMap<>();

        loadThresholds().subscribeOn(Schedulers.io())
                .subscribe(fes::add, this::handleError,
                        () -> {
                            logger.info("Loading threshold statements completed!");
                            lock.countDown();
                        });

        hbeat.subscribeOn(Schedulers.io())
                .subscribe((he) -> hbOpenEvents.put(he.getCiId() + he.getSource(), he),
                        this::handleError,
                        () -> {
                            logger.info("Loading Heartbeat OpsEvents completed!");
                            lock.countDown();
                        });

        metric.subscribeOn(Schedulers.io())
                .subscribe(this::sendOpsEvent,
                        this::handleError,
                        () -> {
                            logger.info("Loading Metric OpsEvents completed!");
                            lock.countDown();
                        });

        // Starts the pipeline and wait for it to complete processing
        logger.info("Starting stream processing pipeline...");
        openEvents.connect();
        logger.info("Waiting to complete the OpsEvent stream processing.");
        lock.await(30, TimeUnit.MINUTES);

        // Finally insert the fake events to satisfy the open hb conditions.
        fes.stream().forEach(fe -> {
            OpsEvent event = hbOpenEvents.get(fe.ciId + fe.source);
            if (event != null) {
                // If there is an open hb event lets just reinsert it so hb event will gets retriggered if no metrics coming in.
                logger.info("Seeding OpenHbEvent(ciId = " + event.getCiId() + ", manifestId = " + event.getManifestId() + ", name = "
                        + event.getName() + ", state = " + event.getState() + ")");
                this.epService.getEPRuntime().sendEvent(event);
            } else {
                // If there is no open heartbeat event lets insert fake perf event to seed hb threshold.
                logger.info("Seeding PerfEvent(ciId = " + fe.ciId + ", manifestId = " + fe.manifestId + ", source = " + fe.source + ")");
                int delay = random.nextInt(heartbeatRandomDelay);
                insertFakeEventWithDelay(fe.ciId, fe.manifestId, fe.source, delay);
            }

        });

        initChannelDownStatement();
    }


    private boolean validateThreshold(Threshold tr) {
        if (!(tr.getManifestId() > 0 && tr.getSource() != null)) {
            return false;
        }

        if ((tr.getManifestId() % this.poolSize) != this.instanceId) {
            return false;
        }

        return true;

		/*
        List<Long> bomCiIds = tsDao.getManifestCiIds(tr.getManifestId());
		if (bomCiIds.size()==0) {
			return false;
		}
		
		List<CmsCI> bomCIs = cmProcessor.getCiByIdListNaked(bomCiIds);
		// convert to set
		Set<Long> bomCiIdsSet = new HashSet<Long>();
		for (CmsCI bomCi : bomCIs) {
			bomCiIdsSet.add(bomCi.getCiId());
		}
		
		// now lets check maped cassnadra CiIds with CMS ciIds
		int remainingBomsCount = 0;
		for (Long mapedCiId : bomCiIds) {
			if (!bomCiIdsSet.contains(mapedCiId)) {
				logger.warn("Found orphan ciId = " + mapedCiId + " it is not in cms but in the map, will remove");
				opsEventDao.removeCi(mapedCiId);
				coProcessor.removeManifestMap(mapedCiId, tr.getManifestId());
				//logger.info("Removing ciId = " + mapedCiId + " from the manifest map");
			} else {
				remainingBomsCount++;
			}
		}
		if (remainingBomsCount >0) {
			return true;
		} else {
			// no boms left need to clean up the thresholds
			logger.error("No instances for this manifet id found in CMS, manifestId = " + tr.getManifestId() + ", the threshold " + tr.getSource() + " will be removed");
			tsDao.removeManifestThreshold(tr.getManifestId(), tr.getSource());
			return false;
		}
		*/
    }

	/*
    private boolean validateThresholdRequest(long ciId, long manifestId, CmsRfcCISimple monitor) {
		// just a simple validation of the format before we send it out to other instances
		String thresholdsJson = monitor.getCiAttributes().get("thresholds");
		String source = monitor.getCiName();
		stmtBuilder.getThresholdStatements(manifestId, source, 0, thresholdsJson, monitor.getCiAttributes().get(HEARTBEAT).equals("true"), monitor.getCiAttributes().get(DURATION));
		return true;
	}
	*/


    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        @SuppressWarnings("unused")
        ApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");
    }

    /**
     * Gets the all loaded stmts.
     *
     * @return the all loaded stmts
     */
    public Map<String, String> getAllLoadedStmts() {
        Map<String, String> stmts = new HashMap<>();
        for (String stmtName : epService.getEPAdministrator().getStatementNames()) {
            stmts.put(stmtName, epService.getEPAdministrator().getStatement(stmtName).getText());
        }
        return stmts;
    }


    private Threshold getThreshold(long manifestId, String source) {
        return tsDao.getThreshold(manifestId, source);
    }


    private void persistThreshold(long ciId,
                                  long manifestId,
                                  String source,
                                  long checksum,
                                  String thresholdsJson,
                                  boolean isHeartbeat,
                                  String hbDuration) {
        if (thresholdsJson == null || thresholdsJson.length() <= THRESHOLDS_JSON_SIZE_FLOOR) {
            thresholdsJson = "n";
        }
        tsDao.addCiThresholds(ciId, manifestId, source, checksum, thresholdsJson, isHeartbeat, hbDuration);
    }
    
    public boolean isManagedByThisInstance(long manifestId) {
    	return (manifestId % poolSize) == instanceId;
    }

    private class FakeEvent {
        long ciId;
        long manifestId;
        String source;
    }

	public void setMinHeartbeatSeedDelay(int minHeartbeatSeedDelay) {
		this.minHeartbeatSeedDelay = minHeartbeatSeedDelay;
	}

	public void setHeartbeatRandomDelay(int heartbeatRandomDelay) {
		this.heartbeatRandomDelay = heartbeatRandomDelay;
	}

	public void setCiStateProcessor(CiStateProcessor ciStateProcessor) {
		this.ciStateProcessor = ciStateProcessor;
	}
}
