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

import com.oneops.cassandra.ClusterBootstrap;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.ops.PerfArchive;
import com.oneops.ops.PerfDatasource;
import com.oneops.ops.PerfHeader;

import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.HSuperColumn;
import me.prettyprint.hector.api.beans.SuperSlice;
import me.prettyprint.hector.api.ddl.ColumnType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.exceptions.HInvalidRequestException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SuperSliceQuery;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;
import static me.prettyprint.hector.api.factory.HFactory.createMutator;

@Component
public class PerfHeaderDao {

    private static Logger logger = Logger.getLogger(PerfHeaderDao.class);
    private static ObjectMapper mapper = new ObjectMapper();

    protected static final String HEADER_SCF = "header";
    protected static final StringSerializer stringSerializer = StringSerializer.get();
    protected static final BytesArraySerializer bytesSerializer = BytesArraySerializer.get();

    // Keys used to set the cassandra hashes
    private static final String UPDATED = "updated";
    private static final String HEARTBEAT = "heartbeat";
    private static final String HEADER = "header";
    private static final String INPUT = "input";
    private static final String INFO = "info";
    private static final String LAST = "last";
    private static final String STEP = "step";
    private static final String STEPS = "steps";
    private static final String TYPE = "type";
    private static final String CDP = "cdp";
    private static final String PDP = "pdp";
    private static final String XFF = "xff";
    private static final String ROWS = "rows";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String IP = "ip";
    private static final String CF = "cf";
    private static final String DS_TYPE = "dstype";
    private static final String NAN = "NaN";
    private static final String LOGBUCKET = "average-5m";

    private String clusterName;
    private String keyspaceName;
    protected Cluster cluster;
    protected Keyspace keyspace;
    private ConcurrentHashMap<String, PerfHeader> headerCache = new ConcurrentHashMap<String, PerfHeader>();

    private ClusterBootstrap cb;

    /**
     * Sets the cluster bootstrap
     *
     * @param setClusterBootstrap
     */
    public void setClusterBootstrap(ClusterBootstrap cb) {
    	this.cb = cb;
    }

    /**
     * Mutator for cassandra cluster name
     *
     * @param name cluster name
     */
    public void setClusterName(String name) {
        clusterName = name;
    }

    /**
     * Mutator for keyspace name
     *
     * @param name cassandra keyspace name
     */
    public void setKeyspaceName(String name) {
        keyspaceName = name;
    }   

    /**
     * Bean post init method. The following configuration is used
     * for initializing the PerfHeaderDao cassandra cluster,
     * <p/>
     * <ul>
     * <li>Active clients per node - 4</li>
     * <li>Cassandra Thrift timeout - 5 sec </li>
     * </ul>
     */
    public void init() {
        logger.info("Initializing PerfHeader Dao...");
        Cluster cluster = cb.getCluster(clusterName, 4, 5 * 1000);
        logger.info("Connected to cluster : " + clusterName);

        try {
            cluster.addKeyspace(new ThriftKsDef(keyspaceName, "org.apache.cassandra.locator.SimpleStrategy", 1, null));
            logger.info("adding keyspace: " + keyspaceName);
        } catch (HInvalidRequestException e) {
            logger.info(" adding " + keyspaceName + " keyspace: " + e.getMessage());
        }

        ConfigurableConsistencyLevel cl = new ConfigurableConsistencyLevel();
        cl.setDefaultWriteConsistencyLevel(HConsistencyLevel.ONE);
        cl.setDefaultReadConsistencyLevel(HConsistencyLevel.ONE);
        keyspace = createKeyspace(keyspaceName, cluster, cl);

        BasicColumnFamilyDefinition cfo = new BasicColumnFamilyDefinition();
        cfo.setColumnType(ColumnType.SUPER);
        cfo.setName(HEADER_SCF);
        cfo.setComparatorType(ComparatorType.BYTESTYPE);
        cfo.setSubComparatorType(ComparatorType.BYTESTYPE);
        cfo.setKeyspaceName(keyspaceName);

        try {
            cluster.addColumnFamily(new ThriftCfDef((cfo)));
            logger.info("adding cf: " + HEADER_SCF);
        } catch (HInvalidRequestException e) {
            logger.info("adding " + HEADER_SCF + " " + e.getMessage());
        }

    }


    public static void logMapString(String name, Map<String, String> h, Logger logger) {
		String logStr = "{";
		for (String k : h.keySet()) {
			logStr += k + ":" + h.get(k) + ",";
		}
		logStr += "}";
		logger.debug(name + " : " + logStr);

	}		

	public void createHeader(String key, CmsRfcCISimple monitor) throws IOException {
		createHeader(key, monitor.getCiAttributes().get("metrics"));
	}

	@SuppressWarnings("unchecked")
	public void createHeader(String key, String metricsJson)
		throws IOException {
		
		PerfHeader header = new PerfHeader(); 
		header.setStep(60);
		PerfArchive.setDefaultArchives(header);
		logger.info("createHeader:"+key);
		
		// write info header
		List<HColumn<String, String>> columns = Arrays.asList(
				HFactory.createStringColumn(UPDATED, "0" ),
				HFactory.createStringColumn(STEP, new Integer(header.getStep()).toString() ),
				HFactory.createStringColumn(IP, "" )
		);
						
		Mutator<byte[]> mutator = createMutator(keyspace, bytesSerializer);			

		mutator.insert(key.getBytes(), HEADER, HFactory.createSuperColumn(INFO,
				columns, stringSerializer, stringSerializer, stringSerializer));

		logger.debug("write INFO :: UPDATED:"+new Long(header.getUpdated()).toString()+
				     " STEP:"+new Integer(header.getStep()).toString() +
				     " IP:"+ header.getIp());
		
		String keysPending = "info";		
		// update datasource headers
		
		HashMap<String,HashMap<String,String>> metricMap = 
				mapper.readValue(metricsJson, new TypeReference<Map<String,Map<String,String>>>() { });
		
		for (String metricKey : metricMap.keySet() ) {

			HashMap<String,String> attrs = metricMap.get(metricKey);
			String dsType = PerfDatasource.GAUGE;
			if (attrs.containsKey(DS_TYPE)) {
				dsType = attrs.get(DS_TYPE);
			}
			
			columns = Arrays.asList(
					HFactory.createStringColumn(TYPE, dsType ), 
					HFactory.createStringColumn(HEARTBEAT, "300"), 
					HFactory.createStringColumn(MIN, NAN ), 
					HFactory.createStringColumn(MAX, NAN ), 
					HFactory.createStringColumn(LAST, NAN ),
					HFactory.createStringColumn(INPUT, NAN ),
					HFactory.createStringColumn(PDP, NAN )
			);
			
			mutator.insert(key.getBytes(), HEADER, HFactory
					.createSuperColumn(metricKey, columns, stringSerializer, stringSerializer, stringSerializer));
			keysPending += ", "+metricKey;		
		}
		
		Map<String,PerfArchive> rraMap = header.getRraMap();
		
		for (String rraKey : rraMap.keySet() ) {
			PerfArchive rra = rraMap.get(rraKey);

			columns = Arrays.asList(
					HFactory.createStringColumn(CF, rra.getConsolidationFunction() ),
					HFactory.createStringColumn(XFF, new Double(rra.getXff()).toString() ),
					HFactory.createStringColumn(STEPS, new Integer(rra.getSteps()).toString() ),
					HFactory.createStringColumn(ROWS, new Integer(rra.getRows()).toString()  )
			);
			
			mutator.insert(key.getBytes(), HEADER, HFactory.createSuperColumn(
					rraKey, columns, stringSerializer, stringSerializer,
					stringSerializer));
			keysPending += ", "+rraKey;		
		}					
		
		logger.debug("write keys:"+keysPending);

		// perform the insert
		mutator.execute();		
	}

	
	
	@SuppressWarnings("unchecked")
	public String createSingleHeader(String key, String step, String metricsJson)
			throws IOException {

			logger.info("createHeader: "+key+" step: "+step+" metrics: "+metricsJson);

			PerfHeader header = new PerfHeader(); 
			header.setStep(60);
			PerfArchive.setDefaultArchives(header);
			
			// write info header
			List<HColumn<String, String>> columns = Arrays.asList(
					HFactory.createStringColumn(UPDATED, "0"),
					HFactory.createStringColumn(STEP, "60"),
					HFactory.createStringColumn(IP, "" )
			);
			
			Mutator<byte[]> mutator = createMutator(keyspace, bytesSerializer);			

			mutator.insert(key.getBytes(), HEADER, HFactory.createSuperColumn(INFO,
					columns, stringSerializer, stringSerializer, stringSerializer));

			logger.debug("write INFO :: UPDATED:"+new Long(header.getUpdated()).toString()+
					     " STEP:"+new Integer(header.getStep()).toString() +
					     " IP:"+ header.getIp());
			
			String keysPending = "info";		
			
			//"metrics":"{\"WriteOperations\":{\"display\":true,\"unit\":\"Per Second\",\"dstype\":\"DERIVE\",\"description\":\"Write Operations\"}}"
			//"metrics":"{\"ReadOperations\":{\"display\":true,\"unit\":\"per second\",\"dstype\":\"DERIVE\",\"description\":\"Read Operations\"}}"
			HashMap<String,HashMap<String,String>> metricMap = 
					mapper.readValue(metricsJson, new TypeReference<Map<String,Map<String,String>>>() { });
			
			for (String metricKey : metricMap.keySet() ) {

				HashMap<String,String> attrs = metricMap.get(metricKey);
				String dsType = PerfDatasource.GAUGE;
				if (attrs.containsKey(DS_TYPE)) {
					dsType = attrs.get(DS_TYPE);
				}
				
				columns = Arrays.asList(
						HFactory.createStringColumn(TYPE, dsType ), 
						HFactory.createStringColumn(HEARTBEAT, "300" ), 
						HFactory.createStringColumn(MIN, NAN ), 
						HFactory.createStringColumn(MAX, NAN ), 
						HFactory.createStringColumn(LAST, NAN ),
						HFactory.createStringColumn(INPUT, NAN ),
						HFactory.createStringColumn(PDP, NAN )
				);
				
				mutator.insert(key.getBytes(), HEADER, HFactory
						.createSuperColumn(metricKey, columns, stringSerializer, stringSerializer, stringSerializer));
				keysPending += ", "+metricKey;		
			}
			
			Map<String,PerfArchive> rraMap = header.getRraMap();
			
			for (String rraKey : rraMap.keySet() ) {
				PerfArchive rra = rraMap.get(rraKey);

				columns = Arrays.asList(
						HFactory.createStringColumn(CF, rra.getConsolidationFunction() ),
						HFactory.createStringColumn(XFF, new Double(rra.getXff()).toString() ),
						HFactory.createStringColumn(STEPS, new Integer(rra.getSteps()).toString() ),
						HFactory.createStringColumn(ROWS, new Integer(rra.getRows()).toString()  )
				);
				
				mutator.insert(key.getBytes(), HEADER, HFactory.createSuperColumn(
						rraKey, columns, stringSerializer, stringSerializer,
						stringSerializer));
				keysPending += ", "+rraKey;		
			}					
			
			logger.debug("write keys:"+keysPending);

			// perform the insert
			mutator.execute();	
			
			return "{\"result_code\":200}\n";
		}
	
	public String removeHeader(String key) {

		Mutator<byte[]> mutator = createMutator(keyspace, bytesSerializer);			

		mutator.delete(key.getBytes(), HEADER, INFO, stringSerializer);
		// perform the insert
		mutator.execute();	
		
		return "{\"result_code\":200}\n";		
	}


	private PerfHeader getHeaderFromCache(String key) {		
		PerfHeader header = null;
		if (headerCache.containsKey(key))
			header = headerCache.get(key);
		return header;
	}
	
	
	public PerfHeader getHeader(String key) {
		
		logger.debug("getting header for: "+key);		

		long startTime = System.currentTimeMillis();
		
		PerfHeader header = getHeaderFromCache(key);
		
		if (header!=null) {
			long endTime = System.currentTimeMillis();
			long duration = endTime-startTime;
			logger.debug("cached get header took "+duration+" ms");
			return header;
		}
		
		
		// create maps for ds,rra,cdp (aggregator will use the zoneMap)
		header = new PerfHeader();
		Map<String,PerfDatasource> dsMap = new HashMap<String,PerfDatasource>();
		Map<String,PerfArchive> rraMap = new HashMap<String,PerfArchive>();
		Map<String,Double> cdpMap = new HashMap<String,Double>();		
		header.setDsMap(dsMap);
		header.setRraMap(rraMap);
		header.setCdpMap(cdpMap);				
		
		SuperSliceQuery<String, String, String, String> q = HFactory
				.createSuperSliceQuery(keyspace, stringSerializer,
						stringSerializer, stringSerializer, stringSerializer);
		q.setColumnFamily(HEADER_SCF).setKey(key)
				.setRange(null, null, false, 100);

		//execute query and get result list
		QueryResult<SuperSlice<String, String, String>> result = q.execute();
		List<HSuperColumn<String, String, String>> superColumns = result.get().getSuperColumns();
		
		for (int i = 0; i < superColumns.size(); i++) {
			List<HColumn<String, String>> columns = superColumns.get(i).getColumns();
			
			// SC to Map<String,String>
			String scName = superColumns.get(i).getName();
			
			if (scName.contains("average-5m"))
				logger.debug("sc name: "+scName);

			Map<String, String> sc = new HashMap<String, String>();
			for (int j = 0; j < columns.size(); j++) {
				sc.put(columns.get(j).getName(), columns.get(j).getValue());
			}
			
			// Map to Typed objects
			if (scName.equalsIgnoreCase(INFO)) {
				header.setStep(Integer.parseInt(sc.get(STEP) ) );
				header.setUpdated( Long.parseLong( sc.get(UPDATED) ) );
				header.setIp( sc.get(IP) );
			
			// cdp
			} else if (scName.equalsIgnoreCase(CDP)) {
				// tmp 
				for (String cdpKey : sc.keySet() ) {
					if (cdpKey.contains("average-5m")) {
						logger.debug("CDP GET:"+cdpKey+ " val:"+sc.get(cdpKey));
					}
					cdpMap.put(cdpKey, new Double(sc.get(cdpKey)) );
				}

			} else if (scName.indexOf("rra") == 0) {
				PerfArchive rra = new PerfArchive();
				rra.setRows( Integer.parseInt( sc.get(ROWS) ) );
				rra.setSteps( Integer.parseInt( sc.get(STEPS) ) );
				rra.setConsolidationFunction( sc.get(CF) );
				rra.setXff( Double.parseDouble( sc.get(XFF) ) );				
				rraMap.put(scName, rra);

			} else if (scName.indexOf(":") == -1) {
				PerfDatasource ds = new PerfDatasource();
				ds.setType(sc.get(TYPE));
				ds.setHeartbeat(Integer.parseInt(sc.get(HEARTBEAT) ) );
				ds.setLast( Double.parseDouble( sc.get(LAST) ) );
				ds.setInput( Double.parseDouble( sc.get(INPUT) ) );
				ds.setMax( Double.parseDouble( sc.get(MAX) ) );
				ds.setMin( Double.parseDouble( sc.get(MIN) ) );
				ds.setPdp( Double.parseDouble( sc.get(PDP) ) );
				dsMap.put(scName, ds);
			
			} else {
				String logStr = "{";
				for (String k : sc.keySet()) {
					logStr += k + ":" + sc.get(k) + ",";
				}
				logStr += "}";
				logger.debug(scName + " : " + logStr);
			}
			
		}

		long endTime = System.currentTimeMillis();
		long duration = endTime-startTime;
		logger.debug("non-cached get header took "+duration+" ms");
		
		return header;
	}
	
	
	@SuppressWarnings("unchecked")
	public void putHeader(String key, PerfHeader header, Mutator<byte[]> mutator) {
		
		headerCache.put(key, header);

		// write info header
		List<HColumn<String, String>> columns = Arrays.asList(
				HFactory.createStringColumn(UPDATED,  Long.valueOf(header.getUpdated()).toString() ),
				HFactory.createStringColumn(STEP,  Integer.valueOf(header.getStep()).toString() ),
				HFactory.createStringColumn(IP, header.getIp() ));
		
		mutator.insert(key.getBytes(), HEADER, HFactory.createSuperColumn(INFO,
				columns, stringSerializer, stringSerializer, stringSerializer));
		logger.debug("write INFO :: UPDATED:"+  Long.valueOf(header.getUpdated()) +
				     " STEP:"+ Integer.valueOf(header.getStep()).toString() +
				     " IP:"+ header.getIp());
		
		// update datasource headers
		Map<String,PerfDatasource> dsMap = header.getDsMap();		
		for (String dsKey : dsMap.keySet() ) {

			PerfDatasource ds = dsMap.get(dsKey);
			columns = Arrays.asList(
					HFactory.createStringColumn(TYPE, ds.getType() ), 
					HFactory.createStringColumn(HEARTBEAT,  Integer.valueOf(ds.getHeartbeat()).toString() ), 
					HFactory.createStringColumn(MIN,  Double.valueOf(ds.getMin() ).toString() ), 
					HFactory.createStringColumn(MAX,   Double.valueOf(ds.getMax() ).toString() ), 
					HFactory.createStringColumn(LAST,   Double.valueOf(ds.getLast() ).toString() ),
					HFactory.createStringColumn(INPUT,   Double.valueOf(ds.getInput() ).toString() ),
					HFactory.createStringColumn(PDP,   Double.valueOf(ds.getPdp() ).toString() )
			);
			
			logger.debug("setting ds header last:"+ ds.getLast());
			
			mutator.insert(key.getBytes(), HEADER, HFactory
					.createSuperColumn(dsKey, columns, stringSerializer, stringSerializer, stringSerializer));

		}		
		
		Map<String,Double> cdpMap = header.getCdpMap();		
		columns = new ArrayList<HColumn<String, String>>();
		
		for (String cdpKey: cdpMap.keySet()) {
			String value = cdpMap.get(cdpKey).toString();
			if (value != null)
			   columns.add(HFactory.createStringColumn(cdpKey, value ));
			if (cdpKey.contains(LOGBUCKET)) {
				logger.debug("CDP PUT: "+cdpKey +" "+cdpMap.get(cdpKey).toString());		
			}
		}		
		mutator.insert(key.getBytes(), HEADER, HFactory.createSuperColumn(
				CDP, columns, stringSerializer, stringSerializer,
				stringSerializer));		
		
	}
	
	
	public void deleteHeader(String key) {
		Mutator<byte[]> mutator = createMutator(keyspace, bytesSerializer);			
		logger.info("deleting header for: "+key);
		mutator.superDelete(key.getBytes(), HEADER, INFO, stringSerializer);			
		mutator.execute();
	}


}
