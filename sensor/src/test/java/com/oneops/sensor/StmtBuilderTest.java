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
import com.oneops.sensor.domain.ThresholdStatements;
import com.oneops.sensor.thresholds.ThresholdDef;
import com.oneops.sensor.thresholds.ThresholdDef.StmtParams;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class StmtBuilderTest {

    private static final long MANI_ID = 0;
    private static final int DURATION = 10;
    private static final int OCC = 20;
    private static final double THRESH_VALUE = 30;
    private static final String BUCKET = "bucket_a";
    private static final String COOLOFF = "66";
    private static final String METRIC = "avg";
    private static final String NAME_THRESH = "sum";
    private static final String STAT = "max";
    private static final String STATE = "open";
    private static final String OPERATOR = "anybody";
    private static final String SOURCE = "sink";
    private static final ThresholdDef THRESH_DEF = new ThresholdDef();


    @BeforeClass
    public void setupParams() {

        THRESH_DEF.setBucket(BUCKET);
        THRESH_DEF.setCooloff(COOLOFF);
        THRESH_DEF.setMetric(METRIC);
        THRESH_DEF.setName(NAME_THRESH);
        THRESH_DEF.setStat(STAT);
        THRESH_DEF.setState(STATE);

        StmtParams params = THRESH_DEF.new StmtParams();
        params.setNumocc(OCC);
        params.setOperator(OPERATOR);
        params.setValue(THRESH_VALUE);
        params.setDuration(DURATION);
        THRESH_DEF.setReset(params);
        THRESH_DEF.setTrigger(params);

    }

    @Test
    public void getThreshStmtTest() {
        StmtBuilder builder = new StmtBuilder();
        Gson gson = new Gson();

        Map<String, ThresholdDef> trsholds = new HashMap<>();
        trsholds.put(NAME_THRESH, THRESH_DEF);

        String thresholdsJson = gson.toJson(trsholds);
        ThresholdStatements stmt = builder.getThresholdStatements(MANI_ID, SOURCE, 1414141410, thresholdsJson, true, String.valueOf(DURATION));

        assertTrue(stmt.getStatements().size() > 1);
    }



    @Test
    public void buildTriggerStmtTest() {

        String triggerStmt = "insert into OpsEvent select ciId, manifestId, timestamp, bucket, metrics, 'open' as state, 'metric' "
                + "as type, source, 15 as coolOff, 'opsmq-compute-cpu:HighCpuUtil' as name, 'notify' as ciState, count(1) as count from "
                + "PerfEvent(manifestId = 4823266 and bucket = '1h' and source = 'opsmq-compute-cpu' and (metrics.avg('CpuIdle') "
                + "<= 20.0)).win:time(5 min) group by ciId having count(1)>=1 output first every 15 minutes";


        ThresholdDef thrDef = new ThresholdDef();
        thrDef.setBucket("1h");
        thrDef.setCooloff("15");
        thrDef.setMetric("CpuIdle");
        thrDef.setName("sum");
        thrDef.setStat("avg");
        thrDef.setState("notify");

        StmtParams params = thrDef.new StmtParams();
        params.setNumocc(1);
        params.setOperator("<=");
        params.setValue(20.0);
        params.setDuration(5);
        thrDef.setTrigger(params);

        StmtBuilder builder = new StmtBuilder();
        String stmt = builder.buildTriggerStmt(4823266, "opsmq-compute-cpu", "HighCpuUtil", thrDef);
        System.out.println(stmt); 
        assertEquals(triggerStmt, stmt);

    }

    @Test
    public void buildResetStmtTest() {

        String resetStmt = "insert into OpsEvent select ciId, manifestId, timestamp, bucket, metrics, 'reset' as state, 'metric' "
                + "as type, source, 'daqws-compute-disk:LowDiskSpace' as name, 'notify' as ciState, count(1) as count from "
                + "PerfEvent(manifestId = 5351305 and bucket = '5m' and source = 'daqws-compute-disk' and (metrics.avg('space_used') "
                + "< 90.0)).win:time(5 min) group by ciId having count(1)>=1";


        ThresholdDef thrDef = new ThresholdDef();
        thrDef.setBucket("5m");
        thrDef.setMetric("space_used");
        thrDef.setName("sum");
        thrDef.setStat("avg");
        thrDef.setState("notify");

        StmtParams params = thrDef.new StmtParams();
        params.setNumocc(1);
        params.setOperator("<");
        params.setValue(90.0);
        params.setDuration(5);
        thrDef.setReset(params);

        StmtBuilder builder = new StmtBuilder();
        String stmt = builder.buildResetStmt(5351305, "daqws-compute-disk", "LowDiskSpace", thrDef);

        assertEquals(resetStmt, stmt);

    }

    @Test
    public void buildHeartbeatStmtTest() {

        String hbStmt = "insert into OpsEvent select lastEvent.ciId as ciId, lastEvent.manifestId as manifestId, lastEvent.channel "
                + "as channel, lastEvent.timestamp as timestamp, 'open' as state, 'heartbeat' as type, lastEvent.source as source, "
                + "lastEvent.source || ':Heartbeat' as name, 'unhealthy' as ciState from pattern [(every lastEvent=PerfEvent(source = "
                + "'inductor-compute-load' and manifestId = 4815892)) -> (timer:interval(5 min) and not PerfEvent(ciId = lastEvent.ciId"
                + " and source = lastEvent.source))]";

        StmtBuilder builder = new StmtBuilder();
        String stmt = builder.buildHeartbeatStmt(4815892, "inductor-compute-load", "5");

        assertEquals(hbStmt, stmt);
    }

    private String showJson() {
        Gson gson = new Gson();
        setupParams();
        String p = gson.toJson(THRESH_DEF);
        return p;

    }

    public static void main(String[] args) {
        StmtBuilderTest t = new StmtBuilderTest();
        String jsonParams = t.showJson();
        assertTrue(jsonParams.length() > 0);
    }


}
