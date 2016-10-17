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
package com.oneops.ops.dao;

import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.CounterRow;
import me.prettyprint.hector.api.beans.CounterRows;
import me.prettyprint.hector.api.beans.CounterSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.HCounterColumn;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.CounterQuery;
import me.prettyprint.hector.api.query.MultigetSliceCounterQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceCounterQuery;
import me.prettyprint.hector.api.query.SliceQuery;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.cassandra.ClusterBootstrap;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.sensor.schema.SchemaBuilder;

public class OpsCiStateDao {

	public static final String COMPONENT_STATE_TOTAL = "total";
	public static final String COMPONENT_STATE_GOOD = "good";
	public static final String COMPONENT_TIMESTAMP = "updated";
	
    private static final Logger logger = Logger.getLogger(OpsCiStateDao.class);
    protected static final LongSerializer longSerializer = LongSerializer.get();
    protected static final StringSerializer stringSerializer = StringSerializer.get();

    private String clusterName;
    private String keyspaceName;
    private Keyspace keyspace;
    protected Mutator<Long> ciStateHistMutator;
    protected Mutator<Long> componentStateMutator;
    private ClusterBootstrap cb;
    private Gson gson = new Gson();

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setKeyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    }
    
    /**
     * Sets the cluster bootstrap
     *
     * @param setClusterBootstrap
     */
    public void setClusterBootstrap(ClusterBootstrap cb) {
    	this.cb = cb;
    }    

    /**
     * Bean post init method. The following configuration is used
     * for initializing the OpsCiStateDao cassandra cluster,
     * <p/>
     * <ul>
     * <li>Active clients per node - 4</li>
     * <li>Cassandra Thrift timeout - 5 sec </li>
     * </ul>
     */
    public void init() {
        logger.info("Initializing OpsCiState Dao...");
        Cluster cluster = cb.getCluster(clusterName, 4, 5 * 1000);
        logger.info("Connected to cluster : " + clusterName);

        SchemaBuilder.createSchema(cluster, keyspaceName);
        ConfigurableConsistencyLevel cl = new ConfigurableConsistencyLevel();
        HConsistencyLevel wrCL = System.getProperty("com.sensor.cistates.cl","LOCAL_QUORUM").equals("ONE") ? HConsistencyLevel.ONE :HConsistencyLevel.LOCAL_QUORUM;
        cl.setDefaultWriteConsistencyLevel(wrCL);
        cl.setDefaultReadConsistencyLevel(HConsistencyLevel.ONE);
        keyspace = createKeyspace(keyspaceName, cluster, cl);
        ciStateHistMutator = HFactory.createMutator(keyspace, longSerializer);
        componentStateMutator = HFactory.createMutator(keyspace, longSerializer);
    }

    /**
     * Persist the ci to cassandra.
     *
     * @param ciId
     * @param chStateEvent
     * @param timestamp
     */
    public void persistCiStateChange(long ciId, long manifestId, CiChangeStateEvent chStateEvent, long timestamp) {
        String payload = gson.toJson(chStateEvent);
        ciStateHistMutator.addInsertion(
                ciId,
                SchemaBuilder.CI_STATE_HIST_CF,
                createDataColumn(timestamp, payload));

        ciStateHistMutator.execute();
        /*
        incComponentsStateCounter(manifestId, chStateEvent.getNewState(), 1);
        
        if (chStateEvent.getOldState() != null) {
        	decrComponentsStateCounter(manifestId, chStateEvent.getOldState(), 1);
        }
        */
    }

    public List<CiChangeStateEvent> getCiStateHistory(long ciId, Long startTime, Long endTime, Integer count) {

        if (count == null) count = 1000;
        List<CiChangeStateEvent> states = new ArrayList<CiChangeStateEvent>();
        SliceQuery<Long, Long, String> sliceQuery = HFactory.createSliceQuery(keyspace, longSerializer, longSerializer, stringSerializer);
        sliceQuery.setColumnFamily(SchemaBuilder.CI_STATE_HIST_CF);
        sliceQuery.setRange(startTime, endTime, false, count);
        sliceQuery.setKey(ciId);
        QueryResult<ColumnSlice<Long, String>> result = sliceQuery.execute();
        ColumnSlice<Long, String> resultCols = result.get();
        for (HColumn<Long, String> col : resultCols.getColumns()) {
            CiChangeStateEvent event = gson.fromJson(col.getValue(), CiChangeStateEvent.class);
            states.add(event);
        }
        return states;
    }

    public Map<Long,Map<String,Long>> getComponentStates(List<Long> manifestIds) {
    	
    	Map<Long,Map<String,Long>> result = new HashMap<Long,Map<String,Long>>();
    	MultigetSliceCounterQuery<Long, String> query =  HFactory.createMultigetSliceCounterQuery(keyspace, longSerializer, stringSerializer);
    	
    	query.setKeys(manifestIds);		
    	query.setColumnFamily(SchemaBuilder.COMPONENT_STATE_CF);
    	query.setRange(null, null, false, 1000);
    	QueryResult<CounterRows<Long,String>> qResult = query.execute();
		
    	CounterRows<Long,String> rows = qResult.get();
		
		for (CounterRow<Long, String> row : rows) {
			if (row.getColumnSlice().getColumns().size() >0) {
				if (!result.containsKey(row.getKey())) {
					result.put(row.getKey(), new HashMap<String,Long>());
				}
				
			    for (HCounterColumn<String> col : row.getColumnSlice().getColumns()) {
			    	result.get(row.getKey()).put(col.getName(), col.getValue());
			    }
			}
		}
    	
    	return result;
    }

    public Map<String,Long> getComponentStates(Long manifestId) {
    	
    	Map<String,Long> result = new HashMap<String,Long>();
    	SliceCounterQuery<Long, String> query =  HFactory.createCounterSliceQuery(keyspace, longSerializer, stringSerializer);
    	query.setKey(manifestId);
    	query.setColumnFamily(SchemaBuilder.COMPONENT_STATE_CF);
    	query.setRange(null, null, false, 100);
    	QueryResult<CounterSlice<String>> qResult = query.execute();
		
    	CounterSlice<String> row = qResult.get();		
		if (row != null && row.getColumns().size()>0) {
			for (HCounterColumn<String> col :row.getColumns()) {
				result.put(col.getName(), col.getValue());
			}
		}
    	return result;
    }
    
    public Long getComponentStatesTimestamp(Long manifestIds) {
        CounterQuery<Long, String> query =  HFactory.createCounterColumnQuery(keyspace, longSerializer, stringSerializer);
    	query.setKey(manifestIds);
    	query.setColumnFamily(SchemaBuilder.COMPONENT_STATE_CF);
    	query.setName(COMPONENT_TIMESTAMP);
    	QueryResult<HCounterColumn<String>> qResult = query.execute();
		
    	HCounterColumn<String> col = qResult.get();
		
    	if (col != null) {
    		return col.getValue();
    	} else {
    		return null;
    	}
    }

    
    public void incComponentsStateCounter(Long manifestId, String state, long inc) {
    	componentStateMutator.incrementCounter(manifestId, SchemaBuilder.COMPONENT_STATE_CF, state, inc);
    	setTimestampOnComonentState(manifestId);
    }	
    
    public void decrComponentsStateCounter(Long manifestId, String state, long dec) {
    	componentStateMutator.decrementCounter(manifestId, SchemaBuilder.COMPONENT_STATE_CF, state, dec);
    	setTimestampOnComonentState(manifestId);
    }	

    public void changeComponentsStateCounter(Long manifestId, Map<String, Long> stateDelta) {
    	stateDelta.entrySet().stream().forEach(entry -> {
    		Long delta = entry.getValue();
    		if (delta != 0) {
    			incComponentsStateCounter(manifestId, entry.getKey(), delta);
    		}
    	});
    }

    public void resetComponentCountsToZero(Long manifestId) {
    	Map<Long,Map<String,Long>> existingStatesMap = getComponentStates(Arrays.asList(manifestId));
		Map<String,Long> existingStates = existingStatesMap.get(manifestId);
    	if (existingStates != null) {
			for (Map.Entry<String, Long> stateEntry : existingStates.entrySet()) {
	    		componentStateMutator.decrementCounter(manifestId, SchemaBuilder.COMPONENT_STATE_CF, stateEntry.getKey(), stateEntry.getValue());
	    	}
	    	setTimestampOnComonentState(manifestId);
    	}
    	//componentStateMutator.addCounterDeletion(manifestId, SchemaBuilder.COMPONENT_STATE_CF);
    	//componentStateMutator.execute();
    }	
    
    
    public void setComponentsStates(Long manifestId, Map<String, Long> states) {
    	Map<Long,Map<String,Long>> existingStatesMap = getComponentStates(Arrays.asList(manifestId));
    	if (existingStatesMap.isEmpty()) {
	    	for (Map.Entry<String, Long> newStateEntry : states.entrySet()) {
	    		componentStateMutator.incrementCounter(manifestId, SchemaBuilder.COMPONENT_STATE_CF, newStateEntry.getKey(), newStateEntry.getValue());
	    	}	
    	} else {
    		Map<String,Long> existingStates = existingStatesMap.get(manifestId);
	    	for (Map.Entry<String, Long> newStateEntry : states.entrySet()) {
	    		String state = newStateEntry.getKey();
	    		Long newCount = newStateEntry.getValue();
	    		if (existingStates.containsKey(state)) {
	    			Long existingCount = existingStates.get(state);
	    			if (newCount.longValue() != existingCount.longValue()) {
	    				long inc = newCount.longValue() - existingCount.longValue();
	    				componentStateMutator.incrementCounter(manifestId, SchemaBuilder.COMPONENT_STATE_CF, state, inc);
	    			}
	    		} else {
	    			componentStateMutator.incrementCounter(manifestId, SchemaBuilder.COMPONENT_STATE_CF, state, newCount);
	    		}
	    		existingStates.remove(state);
	    	}	
	    	//if we still have some existing states that are not in the input map - reset them
	    	for (Map.Entry<String,Long> obsoleteState : existingStates.entrySet()) {
	    		componentStateMutator.decrementCounter(manifestId, SchemaBuilder.COMPONENT_STATE_CF, obsoleteState.getKey(), obsoleteState.getValue());
	    	}
    	}
    	setTimestampOnComonentState(manifestId);
    }
    
    
    private void setTimestampOnComonentState(Long manifestId) {
    	long currentTime = System.currentTimeMillis();
    	Long existingTimeStamp = getComponentStatesTimestamp(manifestId);
    	if (existingTimeStamp != null) {
    		long inc = currentTime - existingTimeStamp;
    		componentStateMutator.incrementCounter(manifestId, SchemaBuilder.COMPONENT_STATE_CF, COMPONENT_TIMESTAMP, inc);
    	} else {
    		componentStateMutator.incrementCounter(manifestId, SchemaBuilder.COMPONENT_STATE_CF, COMPONENT_TIMESTAMP, currentTime);
    	}
    }
  
/*    
    public boolean isStateCountsValid(Long manifestId) {
    	Map<String,Long> states = getComponentStates(manifestId);
    	if (!states.isEmpty()) {
    		long total = states.get(COMPONENT_STATE_TOTAL);
    		long good = states.get(COMPONENT_STATE_GOOD);
    		states.remove(COMPONENT_STATE_TOTAL);
    		states.remove(COMPONENT_STATE_GOOD);
    		states.remove(COMPONENT_TIMESTAMP);
    		if (total < 0 || good < 0) {
    			return false;
    		}
    		long rest = 0;
    		for (Long counter : states.values()) {
    			if (counter < 0) {
    				return false;
    			}
    			rest += counter;
    		}
    		if (total != (good + rest)) {
    			return false;
    		}
    	}		
    	return true;
    }
  */  
    
    /**
     * Sets the long timestamp, string value of a given event
     *
     * @param timestamp
     * @param value
     * @return
     */
    private HColumn<Long, String> createDataColumn(Long timestamp, String value) {
        return HFactory.createColumn(timestamp, value, longSerializer, stringSerializer);
    }

}
