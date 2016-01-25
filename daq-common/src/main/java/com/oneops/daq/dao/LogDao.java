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
package com.oneops.daq.dao;

import com.eaio.uuid.UUID;
import com.eaio.uuid.UUIDGen;
import com.oneops.cassandra.ClusterBootstrap;
import com.oneops.daq.domain.*;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.TimeUUIDSerializer;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.ddl.ColumnType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.exceptions.HInvalidRequestException;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;

/**
 *         LogDao - encapsulates cassandra data access for log data 
 *         clients:
 *         write: LogSink uses process() and constructor or property setters for cassandra connection
 *         read: web.PerfAndLogController uses getLogData()
 *         
 */
@Component
public class LogDao {

	private String clusterName;	
	private String clusterHostPort;	
	private String keyspaceName;
				
	protected static final StringSerializer stringSerializer = StringSerializer.get();
	protected static final BytesArraySerializer bytesSerializer = BytesArraySerializer.get();
	protected static final TimeUUIDSerializer timeuuidSerializer = TimeUUIDSerializer.get();

	protected Cluster cluster;
	protected Keyspace keyspace;
    private ClusterBootstrap daqCluster;
	


	protected static final String LOG_AUTH_CF = "log_auth";
	protected static final String LOG_DATA_CF = "log_data";
	protected static final String CI_LOG_TYPE_CF = "ci_log_type";
	protected static final String LOG_ACTION_WORKORDER_MAP_CF = "log_action_workorder_map";
	
	private static final String[] basicLogClasses = {"Inductor","Syslog","AuthLog"};
	private static final String[] allLogLevels = {"DEBUG","INFO","WARN","CRITICAL","FATAL"};
		
	private static Logger logger = Logger.getLogger(LogDao.class);
	
	// few setters and init outside of constructor to workaround spring @Value no worky issue
	/**
	 * Sets the cluster name.
	 *
	 * @param name the new cluster name
	 */
	public void setClusterName(String name) {
		clusterName = name;
	}
	
	/**
	 * Sets the keyspace name.
	 *
	 * @param name the new keyspace name
	 */
	public void setKeyspaceName(String name) {
		keyspaceName = name;
	}
	
	/**
	 * Sets the cluster host port.
	 *
	 * @param cb the new cluster host port
	 */
	public void setClusterBootstrap(ClusterBootstrap cb) {
		daqCluster = cb;
	}
	
	
	/**
	 * Inits the.
	 */
	public void init() {

		logger.info("LogDao: "+clusterHostPort+":"+clusterName+":"+keyspaceName);		
		cluster = daqCluster.getCluster(clusterName, 4, 5 * 1000);
		
		try {
			cluster.addKeyspace(new ThriftKsDef(keyspaceName, "org.apache.cassandra.locator.SimpleStrategy", 1, null) );
			logger.info("adding keyspace: "+keyspaceName);
		} catch (HInvalidRequestException e) {
			logger.info(" adding "+keyspaceName+" keyspace: "+e.getMessage());			
		}
		
		ConfigurableConsistencyLevel cl = new ConfigurableConsistencyLevel();
		cl.setDefaultWriteConsistencyLevel(HConsistencyLevel.QUORUM);
		cl.setDefaultReadConsistencyLevel(HConsistencyLevel.ONE);
		keyspace = createKeyspace(keyspaceName, cluster, cl);		
		
		// log data CF
		BasicColumnFamilyDefinition cfo = new BasicColumnFamilyDefinition();
		cfo = new BasicColumnFamilyDefinition();
		cfo.setColumnType(ColumnType.STANDARD);
		cfo.setName(LOG_DATA_CF);
		cfo.setComparatorType(ComparatorType.BYTESTYPE);
		cfo.setKeyspaceName(keyspaceName);

		try {
			cluster.addColumnFamily(new ThriftCfDef((cfo)));
			logger.info("adding cf: "+LOG_DATA_CF);
		} catch (HInvalidRequestException e) {
			logger.info("adding "+ LOG_DATA_CF +" "+e.getMessage());
		}		
		
		// ci_log_type		
		cfo = new BasicColumnFamilyDefinition();
		cfo.setColumnType(ColumnType.STANDARD);
		cfo.setName(CI_LOG_TYPE_CF);
		cfo.setComparatorType(ComparatorType.BYTESTYPE);
		cfo.setKeyspaceName(keyspaceName);

		try {
			cluster.addColumnFamily(new ThriftCfDef((cfo)));
			logger.info("adding cf: "+CI_LOG_TYPE_CF);
		} catch (HInvalidRequestException e) {
			logger.info("adding "+ CI_LOG_TYPE_CF +" "+e.getMessage());
		}		

		// log_auth
		cfo = new BasicColumnFamilyDefinition();
		cfo.setColumnType(ColumnType.STANDARD);
		cfo.setName(LOG_AUTH_CF);
		cfo.setComparatorType(ComparatorType.BYTESTYPE);
		cfo.setKeyspaceName(keyspaceName);

		try {
			cluster.addColumnFamily(new ThriftCfDef((cfo)));
			logger.info("adding cf: "+LOG_AUTH_CF);
		} catch (HInvalidRequestException e) {
			logger.info("adding "+ LOG_AUTH_CF +" "+e.getMessage());
		}		

		// log_action_workorder_map
		cfo = new BasicColumnFamilyDefinition();
		cfo.setColumnType(ColumnType.STANDARD);
		cfo.setName(LOG_ACTION_WORKORDER_MAP_CF);
		cfo.setComparatorType(ComparatorType.BYTESTYPE);
		cfo.setKeyspaceName(keyspaceName);

		try {
			cluster.addColumnFamily(new ThriftCfDef((cfo)));
			logger.info("adding cf: "+LOG_ACTION_WORKORDER_MAP_CF);
		} catch (HInvalidRequestException e) {
			logger.info("adding "+ LOG_ACTION_WORKORDER_MAP_CF +" "+e.getMessage());
		}				
	
	}	
	
	
	/*
	 *  sets the long timestamp, string value of a given ci_id
	 */
	protected HColumn<UUID, String> createDataColumn(UUID uuid, String value) {
		return HFactory.createColumn(uuid, value, timeuuidSerializer, stringSerializer);
	}
	
	
	
	/*
	 * process - called from flume sink. Given time, key, entry persists into cassandra
	 */

	
	/*
	 * checks to see if ip can write to the key
	 * TODO: add caching so only does 1 lookup
	 */
	/**
	 * Authz.
	 *
	 * @param key the key
	 * @param ip the ip
	 * @return the boolean
	 */
	public Boolean authz(String key,String ip) {
	    try {
			long startTime = System.currentTimeMillis();
	
			SliceQuery<byte[], String, String> query =
	            HFactory.createSliceQuery(keyspace, bytesSerializer, stringSerializer, stringSerializer);
			query.setColumnFamily(LOG_AUTH_CF);

	    	QueryResult<ColumnSlice<String, String>> result = query.setKey(key.getBytes()).setColumnNames(ip).execute();
	    	
	    	long duration = System.currentTimeMillis() - startTime;
	    	logger.debug("authz lookup took: "+duration+"ms");
	    	
	    	if (result.get() != null) {
	    		return true;
	    	}
	    	return false;
	    }
	    catch (Exception e) {
	    	return false;	    	
	    }
	}
	
	/**
	 * Gets the log data by action or workorder.
	 *
	 * @param req the req
	 * @return the log data by action or workorder
	 */
	public GetLogDataByIdResponse getLogDataByActionOrWorkorder(GetLogDataByIdRequest req) {
		GetLogDataByIdResponse resp = new GetLogDataByIdResponse(req);
		
		int maxColumns = 10000;	

		List<LogData> logData = new ArrayList<LogData>();
	    try {
			long startTime = System.currentTimeMillis();
	

			MultigetSliceQuery<byte[], com.eaio.uuid.UUID, String> multigetSliceQuery =
	            HFactory.createMultigetSliceQuery(keyspace, bytesSerializer, timeuuidSerializer, stringSerializer);
	    	multigetSliceQuery.setColumnFamily(LOG_ACTION_WORKORDER_MAP_CF);
	    	List<byte[]> keys = new ArrayList<byte[]>();
	    	
//	    	keys.add(new Long(req.getId()).toString().getBytes());
	    	keys.add(Long.valueOf(req.getId()).toString().getBytes());
	    	
	    	multigetSliceQuery.setKeys(keys);	    	
	    	UUID startUuid = new UUID(0,UUIDGen.getClockSeqAndNode());
	    	UUID endUuid = new UUID(99992313900691274L,UUIDGen.getClockSeqAndNode());	    	
	    	multigetSliceQuery.setRange(startUuid, endUuid, false, maxColumns);
	    		    	
            QueryResult<Rows<byte[], UUID, String>> result = multigetSliceQuery.execute();
            Rows<byte[], UUID, String> rows = result.get();
                        
            long endTime = System.currentTimeMillis();
            long cassDuration = endTime - startTime;


            List<UUID> columnList = new ArrayList<UUID>();
            String dataRowKey = null;
            for (Row<byte[], UUID, String> row : rows) {                            	
            	List<HColumn<UUID, String>> cols = row.getColumnSlice().getColumns();
                Iterator<HColumn<UUID, String>> listIter = cols.listIterator();
                while (listIter.hasNext()) {                	
                	HColumn<UUID,String> c = (HColumn<UUID, String>) listIter.next();
                	columnList.add(c.getName());
                	if (dataRowKey==null) {
                		dataRowKey = c.getValue();
                	}
                }
            }    		    	
            
            if (dataRowKey==null) {
            	logger.debug("no results for id:"+req.getId());
            	return resp;
            }
                        
            
	    	SliceQuery<byte[], com.eaio.uuid.UUID, String>  sliceQuery
	    		= HFactory.createSliceQuery(keyspace, bytesSerializer, timeuuidSerializer, stringSerializer);
	    	sliceQuery.setColumnFamily(LOG_DATA_CF).setKey(dataRowKey.getBytes());

	    	// setColumnNames needs UUID[]
	    	UUID[] columns = new UUID[columnList.size()];
	    	int i = 0;
	    	for (UUID col : columnList) {
	    		columns[i] = col;
	    		i++;
	    	}
	    	sliceQuery.setColumnNames(columns);

	    	QueryResult<ColumnSlice<UUID, String>> res = sliceQuery.execute();
	    	ColumnSlice<UUID, String> columnSlice = res.get();	    	
	    	
        	String[] keyParts = dataRowKey.split(":");
        	String logClass = keyParts[1];
        	String level = keyParts[2];	    	
	    	
            int colCount = 0;                        	
        	List<HColumn<UUID, String>> cols = columnSlice.getColumns();
            Iterator<HColumn<UUID, String>> listIter = cols.listIterator();
            while (listIter.hasNext()) {                	
            	HColumn<UUID,String> c = (HColumn<UUID, String>) listIter.next();
            	LogData logEntry = new LogData();
            	UUID uuid = c.getName();
            	logEntry.setTimestamp(uuid.getTime());
            	logEntry.setMessage(c.getValue());
            	logEntry.setLevel(level);
            	logEntry.setLogClass(logClass);
            	logData.add(logEntry);
            	colCount++;                
            }
            resp.setLogData(logData);
            
            endTime = System.currentTimeMillis();
            long duration = endTime-startTime;
            
	    	logger.debug("getLogData took: "+duration+" ms (cass query:"+cassDuration+" ms) returning:"+colCount+" rows");
	    	
	    } catch (HectorException he) {
	        he.printStackTrace();
	    }		
		
		
		return resp;
	}
	
	
	/*
	 * given a set of key,start,end (typed request) does a cassandra query and returns typed response;
	 */
	/**
	 * Gets the log data.
	 *
	 * @param req the req
	 * @return the log data
	 */
	public GetLogDataResponse getLogData(GetLogDataRequest req) {

//		Long start = new Long(req.getStart());
		Long start =  Long.valueOf(req.getStart());

//		Long end = new Long(req.getEnd());
		Long end =  Long.valueOf(req.getEnd());

		int maxColumns = 10000;		
		
		GetLogDataResponse resp = new GetLogDataResponse(req);
		List<LogData> logData = new ArrayList<LogData>();
	    try {
			long startTime = System.currentTimeMillis();
	

			MultigetSliceQuery<byte[], com.eaio.uuid.UUID, String> multigetSliceQuery =
	            HFactory.createMultigetSliceQuery(keyspace, bytesSerializer, timeuuidSerializer, stringSerializer);
	    	multigetSliceQuery.setColumnFamily(LOG_DATA_CF);
	    	List<byte[]> keys = new ArrayList<byte[]>();
	    	
	    	
	    	// can be null=all or debug, info, warn, error, fatal
	    	String levelList = req.getLevelList();
	    	List<String> levels = null;
	    	if (levelList == null || levelList.equalsIgnoreCase("all")) {
	    		levels = Arrays.asList(allLogLevels);
	    	} else {
	    		levels = Arrays.asList(levelList.split(","));
	    	}
//	    	String keyBase = new Long(req.getCi_id()).toString();
	    	String keyBase =  Long.valueOf(req.getCi_id()).toString();


	    	String classList = req.getClassList();
	    	List<String> classes = null;
	    	if (classList == null) {
	    		classes = Arrays.asList(basicLogClasses);
	    	} else {
	    		classes = Arrays.asList(levelList.split(","));
	    	}
	    		    	
	    	for (String logClass : classes)  {

/*	    		if ( logClass.equalsIgnoreCase("all") ) {
	    			for (int j=0; j<basicLogClasses.length; j++) {
    					classes.add(basicLogClasses[j]);	    				
	    			}	   
	    			continue;
	    		}
*/
	    		
	    		for (String level : levels) {		    		

/*		    		if ( level.contains("+") ) {
		    			String startingLevel = logClass.replace("+","");
	    				Boolean logLevelPassed = false;
		    			for (int j=0; j<allLogTypes.length; j++) {
		    				String lc = allLogTypes[j];
		    				if (lc.equalsIgnoreCase(startingLevel)) {
		    					logLevelPassed = true;
		    				}
		    				if (logLevelPassed) {
		    					levels.add(lc);
		    				}
		    				
		    			}	   
		    			continue;
		    		}		  */ 		    		
		    		
		    		logger.debug("appending level:"+level);
		    		keys.add( (keyBase + ":" + logClass + ":" + level ).getBytes() );
	    		
	    		}
	    		
	    	}
	    	
	    	multigetSliceQuery.setKeys(keys);	    	
	    	UUID startUuid = new UUID(start,UUIDGen.getClockSeqAndNode());
	    	UUID endUuid = new UUID(end,UUIDGen.getClockSeqAndNode());	    	
	    	multigetSliceQuery.setRange(startUuid, endUuid, false, maxColumns);
	    	
	    	logger.debug("start:"+start+" end:"+end+" for: "+keyBase+":"+levelList);
	    	
            QueryResult<Rows<byte[], UUID, String>> result = multigetSliceQuery.execute();
            Rows<byte[], UUID, String> rows = result.get();
                        
            long endTime = System.currentTimeMillis();
            long cassDuration = endTime - startTime;


            // put the by-metric results into 1 csv-like table (time,metric1,metric2,etc) 
            // ... should find faster way to do this, but still 10x faster than gwt DataTable serialization
            int rowCount = 0;            
            for (Row<byte[], UUID, String> row : rows) {                
            	
            	String rowName = new String(row.getKey());
            	String[] keyParts = rowName.split(":");
            	String logClass = keyParts[1];
            	String level = keyParts[2];

            	List<HColumn<UUID, String>> cols = row.getColumnSlice().getColumns();
                Iterator<HColumn<UUID, String>> listIter = cols.listIterator();
                while (listIter.hasNext()) {                	
                	HColumn<UUID,String> c = (HColumn<UUID, String>) listIter.next();
                	LogData logEntry = new LogData();
                	UUID uuid = c.getName();
                	logEntry.setTimestamp(uuid.getTime());
                	logEntry.setMessage(c.getValue());
                	logEntry.setLevel(level);
                	logEntry.setLogClass(logClass);
                	logData.add(logEntry);
                    rowCount++;                
                }
            }
            resp.setLogData(logData);
            
            endTime = System.currentTimeMillis();
            long duration = endTime-startTime;
            
	    	logger.debug("getLogData took: "+duration+" ms (cass query:"+cassDuration+" ms) returning:"+rowCount+" rows");
	    	
	    } catch (HectorException he) {
	        he.printStackTrace();
	    }		
	    
	    return resp;    
	}


	/*
	 * given a ciId does a cassandra query and returns json (array if multiple) ci_id,metrics[string of metric:datasource]
	 */
	/**
	 * Gets the available log types.
	 *
	 * @param ciList the ci list
	 * @return the available log types
	 */
	public String getAvailableLogTypes(String[] ciList) {

		StringBuilder jsonOut = new StringBuilder("");
	    try {
			long startTime = System.currentTimeMillis();
	
			MultigetSliceQuery<byte[], String, String> multigetSliceQuery =
	            HFactory.createMultigetSliceQuery(keyspace, bytesSerializer, stringSerializer, stringSerializer);
	    	multigetSliceQuery.setColumnFamily(CI_LOG_TYPE_CF);
	    	List<byte[]> keys = new ArrayList<byte[]>();
	    	for (int i=0; i<ciList.length; i++) {
	    		String ciId = ciList[i];
	    		keys.add(ciId.getBytes());
	    		
	    	}
	    	multigetSliceQuery.setKeys(keys);	    	
	    	multigetSliceQuery.setRange("", "", false, 100000);
	    	
	    	long cassStart = System.currentTimeMillis();
	    	
            QueryResult<Rows<byte[], String, String>> result = multigetSliceQuery.execute();
            Rows<byte[], String, String> rows = result.get();
            
            long cassEnd = System.currentTimeMillis();
            long cassDuration = cassEnd - cassStart;

            // put the by-metric results into 1 csv-like table (time,metric1,metric2,etc) 
            // ... should find faster way to do this, but still 10x faster than gwt DataTable serialization
            int rowCount = 0;
            for (Row<byte[], String, String> row : rows) {

            	if (rowCount >0) {
            		jsonOut.append(",");
            	}
            	jsonOut.append( "{\"ci_id\":"+ new String(row.getKey()) +", \"metrics\":[");
                List<HColumn<String, String>> cols = row.getColumnSlice().getColumns();
                Iterator<HColumn<String, String>> listIter = cols.listIterator();
                int colCount = 0;
                while (listIter.hasNext()) {                	
                	HColumn<String,String> c = (HColumn<String, String>) listIter.next();
                    if (colCount>0) {
                    	jsonOut.append(",");
                	}
                    String dsAttributes = c.getValue();
                    String[] dsAtrributeParts = dsAttributes.split(":");
                    String unit = dsAtrributeParts[0];
                    String min = dsAtrributeParts[1];
                    String max = dsAtrributeParts[2];
                    // nan is not json compliant so using 'null'
                    if (min.length()==0) {
                    	min = "null";
                    }
                    if (max.length()==0) {
                    	max = "null";
                    }
                    // make json compliant null
                    if (unit.equalsIgnoreCase("")) {
                    	unit="null";
                    } else {
                    	unit = "\""+unit+"\"";
                    }
                    jsonOut.append( "{\"metricDs\":\""+c.getName()+"\",\"unit\":"+unit+"\",\"min\":"+min+",\"max\":"+max+"}");           
                    colCount++;                
                }
                jsonOut.append( "]}\n");
                rowCount++;
            }
                
            long endTime = System.currentTimeMillis();
            long duration = endTime-startTime;
            
	    	logger.debug("getAvailableMetrics took: "+duration+" ms (cass query:"+cassDuration+" ms) returning: "+rowCount+" rows of ci metric lists");
	    	
	    } catch (HectorException he) {
	        he.printStackTrace();
	    }		
	    
	    return jsonOut.toString();    
	}		
			
	

	/*
	 * write ci_metric map
	 */


}
