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

import com.google.gson.Gson;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.ValueType;
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

import java.util.*;

import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;
import static me.prettyprint.hector.api.factory.HFactory.getOrCreateCluster;

/**
 * The Class PerfDaoDataTableTest.
 */
public class PerfDaoDataTableTest {

	private static final StringSerializer stringSerializer = StringSerializer.get();
	private static final LongSerializer longSerializer = LongSerializer.get();
	private static final DoubleSerializer doubleSerializer = DoubleSerializer.get();
	
	private static Cluster cluster;
	private static Keyspace keyspace;
	
	private static final String DATA_CF = "data";
	private static final String CLUSTER_NAME = "PerfAndLogCluster";
	private static final String KEYSPACE_NAME = "mdb";
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws TypeMismatchException the type mismatch exception
	 */
	@SuppressWarnings("unchecked")
	public static void main(String [ ] args) throws TypeMismatchException
	{

		
		cluster = getOrCreateCluster(CLUSTER_NAME, "192.168.0.3:9160");
		keyspace = createKeyspace(KEYSPACE_NAME, cluster);
		
		String key = "/nagios/localhost/localhost/Current Load:load1:rra0"; //args[0];
		String key2 = "/nagios/localhost/localhost/Current Load:load5:rra0"; //args[0];
		String key3 = "/nagios/localhost/localhost/Current Load:load15:rra0"; //args[0];
		
		Gson gson = new Gson();
		
		String startStr = "1312859100"; //args[1];
		String endStr = "1312879200"; //args[2];
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
        	multigetSliceQuery.setKeys(key,key2,key3);
        	multigetSliceQuery.setRange(start, end, false, maxColumns);

            QueryResult<Rows<String, Long, Double>> result = multigetSliceQuery.execute();
            Rows<String, Long, Double> rows = result.get();
            

            Map<String,Object> valMap = new HashMap<String,Object>();
            Map<Long,Integer> timeMap = new TreeMap<Long,Integer>();
            String[] rowKeys = new String[5];
            
		    DataTable data = new DataTable();
		    data.addColumn(new ColumnDescription("time",ValueType.NUMBER,"timestamp"));            
            
            int rowCount = 0;
            for (Row<String, Long, Double> row : rows) {

                Map<Long,Double> colMap = new HashMap<Long,Double>();
                valMap.put(row.getKey(), colMap);
                rowKeys[rowCount] = row.getKey();
                
                data.addColumn(new ColumnDescription(row.getKey(),ValueType.NUMBER,row.getKey()));

                List<HColumn<Long, Double>> cols = row.getColumnSlice().getColumns();
                Iterator<HColumn<Long, Double>> listIter = cols.listIterator();
                while (listIter.hasNext()) {
                	
                	HColumn<Long,Double> c = (HColumn<Long, Double>) listIter.next();
                	colMap.put(c.getName(),c.getValue());
                	timeMap.put(c.getName(), 1);
                }
                rowCount++;
                
            }
            
            for (Long time : timeMap.keySet()) {
            	TableRow row = new TableRow();
            	data.addRow(row);
            	TableCell cell = new TableCell(time);
            	row.addCell(cell);
            	for (int i=0; i<rowCount; i++) {
            		String rowKey = rowKeys[i];
            		Map<Long,Double> colMap = (Map<Long, Double>) valMap.get(rowKey);
            		Double val = colMap.get(time);
            		if (val != null)
            			row.addCell(val);
            		else 
            			row.addCell("");
            	}
            }
            
            System.out.println("{\"data\":"+gson.toJson(data)+"}");
            
            
            long endTime = System.currentTimeMillis();
            
            long duration = endTime-startTime;
            System.out.println("took "+duration+" usec");
            
            System.exit(0);
           
        } catch (HectorException he) {
            he.printStackTrace();
        }		
	
	}

	
}
