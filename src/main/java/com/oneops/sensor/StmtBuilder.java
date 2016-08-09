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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oneops.sensor.domain.SensorStatement;
import com.oneops.sensor.domain.ThresholdStatements;
import com.oneops.sensor.thresholds.ThresholdDef;
import org.apache.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Map;

import static java.lang.System.getProperty;

/**
 * Esper EPL statement builder
 */
public class StmtBuilder {

    private static final Logger logger = Logger.getLogger(StmtBuilder.class);

    public static final int THRESHOLDS_JSON_SIZE_FLOOR = 3;
    private static final int CHANNEL_DOWN_INTERVAL = Integer.valueOf(getProperty("com.oneops.sensor.chdowntime", "15"));
    private static final int WNDW_SIZE_MAX = Integer.valueOf(getProperty("com.oneops.sensor.MaxDuration", "5"));

    public final static String STMT_RESET = "insert into OpsCloseEvent select trigger.ciId as ciId, trigger.manifestId as manifestId, trigger.name as name, trigger.source as source, reset.timestamp as timestamp, reset.metrics as metrics, 'close' as state, trigger as openEvent from pattern [every trigger=OpsEvent(state = 'open') -> reset=OpsEvent(ciId = trigger.ciId and name = trigger.name and state = 'reset')]";
    public final static String STMT_RESET_HEARTBEAT = "insert into OpsCloseEvent select trigger.ciId as ciId, trigger.manifestId as manifestId, trigger.name as name, trigger.source as source, reset.timestamp as timestamp, 'close' as state, trigger as openEvent from pattern [every trigger=OpsEvent(state = 'open' and type = 'heartbeat') -> reset=PerfEvent(ciId = trigger.ciId and source = trigger.source)]";
    public final static String STMT_RETRIGGER_HEARTBEAT = "insert into OpsEvent select hbtrigger.ciId as ciId,  hbtrigger.manifestId as manifestId,hbtrigger.channel as channel, hbtrigger.timestamp as timestamp, hbtrigger.state as state, 'heartbeat' as type, hbtrigger.source as source, hbtrigger.name as name, hbtrigger.ciState as ciState  from pattern [every hbtrigger=OpsEvent(state = 'open' and type = 'heartbeat') -> (timer:interval(15 min) and not PerfEvent(ciId = hbtrigger.ciId and source = hbtrigger.source))]";
    public final static String STMT_TRIGGER_CHANNELDOWN = "insert into ChannelDownEvent select lastEvent.channel as channel from pattern [(every lastEvent=PerfEvent()) -> (timer:interval(" + CHANNEL_DOWN_INTERVAL + " sec) and not PerfEvent(channel = lastEvent.channel))]";
    public final static String STMT_DELAY_PERF_EVENT = "insert into PerfEvent select delayedEvent.perfEvent from pattern [(every delayedEvent=DelayedPerfEvent -> timer:interval(delayedEvent.delay sec))]";

    private final Gson gson = new Gson();

    /**
     * Statement constructor
     */
    public StmtBuilder() {
        logger.info("Statement Builder initialized with Max Duration: " + WNDW_SIZE_MAX + ", ChannelDownInterval: " + CHANNEL_DOWN_INTERVAL);
    }

    /**
     * Builds the trigger stmt.
     *
     * @param manifestId manifest id
     * @param source     metric source
     * @param trsName    metric name
     * @param def        {@link ThresholdDef}
     * @return Trigger statement
     */
    public String buildTriggerStmt(long manifestId, String source, String trsName, ThresholdDef def) {

        String name = source + ":" + trsName;

        // Set max window size to 5 mins to avoid OOM
        int duration = def.getTrigger().getDuration();
        duration = (duration > WNDW_SIZE_MAX) ? WNDW_SIZE_MAX : duration;

        // Set default cooloff to 15 mins
        String coolOff = def.getCooloff();
        coolOff = (coolOff != null) ? coolOff : "15";

        // Set bigger capacity (450) to reduce internal buffer allocation.
        return new StringBuilder(450)
                .append("insert into OpsEvent select ciId, manifestId, timestamp, bucket, metrics, 'open' as state, 'metric' as type, source, ")
                .append(coolOff).append(" as coolOff, ")
                .append("'").append(name).append("'")
                .append(" as name, ")
                .append("'").append(def.getState()).append("'")
                .append(" as ciState, count(1) as count from PerfEvent(manifestId = ")
                .append(manifestId)
                .append(" and bucket = ")
                .append("'").append(def.getBucket()).append("'")
                .append(" and source = ")
                .append("'").append(source).append("'")
                .append(" and (metrics.")
                .append(def.getStat())
                .append("('").append(def.getMetric()).append("') ")
                .append(def.getTrigger().getOperator())
                .append(" ").append(def.getTrigger().getValue()).append(")).")
                .append("win:time(")
                .append(duration)
                .append(" min) group by ciId having count(1)>=")
                .append(def.getTrigger().getNumocc())
                .append(" output first every ")
                .append(coolOff)
                .append(" minutes").toString();
    }

    /**
     * Builds the reset stmt.
     *
     * @param manifestId manifest id
     * @param source     metric source
     * @param trsName    metric name
     * @param def        {@link ThresholdDef}
     * @return Reset statement.
     */
    public String buildResetStmt(long manifestId, String source, String trsName, ThresholdDef def) {

        String name = source + ":" + trsName;

        // Set max window size to 5 mins to avoid OOM
        int duration = def.getReset().getDuration();
        duration = (duration > WNDW_SIZE_MAX) ? WNDW_SIZE_MAX : duration;

        return new StringBuilder(400)
                .append("insert into OpsEvent select ciId, manifestId, timestamp, bucket, metrics, 'reset' as state, 'metric' as type, source, ")
                .append("'").append(name).append("'")
                .append(" as name, ")
                .append("'").append(def.getState()).append("'")
                .append(" as ciState, count(1) as count from PerfEvent(manifestId = ")
                .append(manifestId)
                .append(" and bucket = ")
                .append("'").append(def.getBucket()).append("'")
                .append(" and source = ")
                .append("'").append(source).append("'")
                .append(" and (metrics.")
                .append(def.getStat())
                .append("('").append(def.getMetric()).append("') ")
                .append(def.getReset().getOperator())
                .append(" ").append(def.getReset().getValue()).append(")).")
                .append("win:time(")
                .append(duration)
                .append(" min) group by ciId having count(1)>=")
                .append(def.getReset().getNumocc()).toString();
    }

    /**
     * Builds the heartbeat stmt.
     *
     * @param manifestId the manifest id
     * @param source     the source
     * @param duration   the duration
     * @return heartbeat statement.
     */
    public String buildHeartbeatStmt(long manifestId, String source, String duration) {
        return new StringBuilder(100)
                .append("insert into OpsEvent select lastEvent.ciId as ciId, lastEvent.manifestId as manifestId, lastEvent.channel as channel, lastEvent.timestamp as timestamp, 'open' as state, 'heartbeat' as type, lastEvent.source as source, lastEvent.source || ':Heartbeat' as name, ")
                .append("'unhealthy'")
                .append(" as ciState from pattern [(every lastEvent=PerfEvent(source = ")
                .append("'").append(source).append("'")
                .append(" and manifestId = ")
                .append(manifestId)
                .append(")) -> (timer:interval(")
                .append(duration)
                .append(" min) and not PerfEvent(ciId = lastEvent.ciId and source = lastEvent.source))]").toString();
    }


    /**
     * Gets the threshold statements.
     *
     * @param manifestId     the manifest id
     * @param source         the source
     * @param checksum       the checksum
     * @param thresholdsJson the thresholds json
     * @param isHeartbeat    the is heartbeat
     * @param hbDuration     the hb duration
     * @return the threshold statements
     */
    public ThresholdStatements getThresholdStatements(long manifestId, String source,
                                                      long checksum, String thresholdsJson,
                                                      boolean isHeartbeat,
                                                      String hbDuration) {

        ThresholdStatements trStatements = new ThresholdStatements();
        trStatements.setChecksum(checksum);

        if (thresholdsJson != null && thresholdsJson.length() > THRESHOLDS_JSON_SIZE_FLOOR) {

            Type mapType = new TypeToken<Map<String, ThresholdDef>>() {
            }.getType();
            Map<String, ThresholdDef> trsholds = gson.fromJson(thresholdsJson, mapType);

            for (String trsName : trsholds.keySet()) {
                ThresholdDef trDef = trsholds.get(trsName);
                // Build trigger statement
                if (trDef.getTrigger() != null) {
                    String eplStmt = buildTriggerStmt(manifestId, source, trsName, trDef);
                    String eplName = source + ":" + trsName + "-" + manifestId + "-trigger";
                    SensorStatement stmt = new SensorStatement(eplName, eplStmt, "OpsEventListener");
                    trStatements.addStatement(stmt);
                }
                // Build reset statement
                if (trDef.getReset() != null) {
                    String eplStmt = buildResetStmt(manifestId, source, trsName, trDef);
                    String eplName = source + ":" + trsName + "-" + manifestId + "-reset";
                    SensorStatement stmt = new SensorStatement(eplName, eplStmt, null);
                    trStatements.addStatement(stmt);
                }
            }
        }
        // Build heartbeat statement
        if (isHeartbeat) {
            String eplStmt = buildHeartbeatStmt(manifestId, source, hbDuration);
            trStatements.setHeartbeat(true);
            trStatements.setHbDuration(hbDuration);
            String eplName = source + ":Heartbeat-" + manifestId;
            SensorStatement stmt = new SensorStatement(eplName, eplStmt, "OpsEventListener");
            trStatements.addStatement(stmt);
        }
        return trStatements;
    }
}
