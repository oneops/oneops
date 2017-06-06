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
package com.oneops.daq;

/*import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;
import static me.prettyprint.hector.api.factory.HFactory.getOrCreateCluster;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.spi.LoggingEvent;


import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
*/

/**
 * The Class LogParseTest.
 */
public class LogParseTest {

/*	private static final StringSerializer stringSerializer = StringSerializer.get();
	private static final LongSerializer longSerializer = LongSerializer.get();
	private static final DoubleSerializer doubleSerializer = DoubleSerializer.get();
	
	private static Cluster cluster;
	private static Keyspace keyspace;
	
	private static final String DATA_CF = "data";
	private static final String CLUSTER_NAME = "PerfAndLogCluster";
	private static final String KEYSPACE_NAME = "mdb";*/
	
	
	/**
 * The main method.
 *
 * @param args the arguments
 */
public static void main(String [ ] args)
	{

		//String logMessage = "2011-08-20 17:06:31,984 INFO  [listenerContainer-3] 5075:7278:3627 cmd out: terminating node: be-compute-3702-1.dev.a6.Company";
	//	String logMessage = "2011-08-20 18:08:58,135	 INFO	Inductor:492	5094:7278:3627 cmd took: 11235 ms";
		
/*	    // given
	    Properties p = new Properties();
	    p.put("type", "log4j");
	    p.put("pattern", "TIMESTAMP LEVEL [THREAD]  MESSAGE");
	    p.put("dateFormat", "yyyy-MM-dd HH:mm:ss,SSS");

	    Log4jPatternMultilineLogParser logParser = new Log4jPatternMultilineLogParser();
	    ParsingContext context = new ParsingContext();
	    context.setCustomConextProperties(customConextProperties)
	    
//        LoggingEvent event = (LoggingEvent) object;
        try {
        	logParser.setLogFormat("TIMESTAMP LEVEL [THREAD]  MESSAGE");
        	logParser.setTimestampFormat("yyyy-MM-dd HH:mm:ss,SSS");
			LogData logdata = logParser.parse(logMessage, context);
			
		    System.out.println("log: class:"+logdata.getClazz()+" severity:"+logdata.getLevel()+" msg:"+logdata.getMessage());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        */
	    
	    // then	    
	    
//	    LogData logDa
//	    p.put("name", "windows-1250");
//	    p.put("charset", "windows-1250");

		
//		cluster = getOrCreateCluster(CLUSTER_NAME, "192.168.0.3:9160");
//		keyspace = createKeyspace(KEYSPACE_NAME, cluster);
		


/*		
		String startStr = "1312991000"; //args[1];
		String endStr = "1312999200"; //args[2];
		Long start = new Long(startStr);
		Long end = Long.parseLong(endStr);
		int maxColumns = (int) (end - start);
		
        try {

    		long startTime = System.currentTimeMillis();

        	// serializers:
        	// string = key
        	// col (timestamp) int
        	// value double
        	MultigetSliceQuery<String, Long, Double> multigetSliceQuery =
                HFactory.createMultigetSliceQuery(keyspace, stringSerializer, longSerializer, doubleSerializer);
        	multigetSliceQuery.setColumnFamily(DATA_CF);
        	multigetSliceQuery.setKeys(key);
        	multigetSliceQuery.setRange(start, end, false, maxColumns);

            QueryResult<Rows<String, Long, Double>> result = multigetSliceQuery.execute();
            Rows<String, Long, Double> rows = result.get();
            

            Map<String,Object> valMap = new HashMap<String,Object>();
            Map<Long,Integer> timeMap = new TreeMap<Long,Integer>();
            String[] rowKeys = new String[5];

            String jsonOut = "{[\"time\"";

            int rowCount = 0;
            for (Row<String, Long, Double> row : rows) {

            	String rowKey = row.getKey();
            	
                Map<Long,Double> colMap = new HashMap<Long,Double>();
                valMap.put(row.getKey(), colMap);
                rowKeys[rowCount] = rowKey;
                
                jsonOut += ",\""+rowKey+"\"";
                
                List<HColumn<Long, Double>> cols = row.getColumnSlice().getColumns();
                Iterator<HColumn<Long, Double>> listIter = cols.listIterator();
                while (listIter.hasNext()) {
                	
                	HColumn<Long,Double> c = (HColumn<Long, Double>) listIter.next();
                	colMap.put(c.getName(),c.getValue());
                	timeMap.put(c.getName(), 1);
                }
                rowCount++;                
            }
            jsonOut += "]\n";
           
            for (Long time : timeMap.keySet()) {

                jsonOut += ",["+time;
            	
            	for (int i=0; i<rowCount; i++) {
            		String rowKey = rowKeys[i];
            		Map<Long,Double> colMap = (Map<Long, Double>) valMap.get(rowKey);
            		Double val = colMap.get(time);
        			jsonOut += ",";
            		if (val != null)
            			jsonOut += val;
            	}
                jsonOut += "]\n";
            }
            
            System.out.println("{\"data\":"+jsonOut+"}");
            
            
            long endTime = System.currentTimeMillis();
            long duration = endTime-startTime;
            
            System.out.println("took "+duration+" ms");
            
            System.exit(0);
           
        } catch (HectorException he) {
            he.printStackTrace();
        }		
*/	
	}

	
}
