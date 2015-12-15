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
import static me.prettyprint.hector.api.factory.HFactory.createMutator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.HSuperColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.beans.SuperSlice;
import me.prettyprint.hector.api.ddl.ColumnType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.exceptions.HInvalidRequestException;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.CountQuery;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import me.prettyprint.hector.api.query.SuperSliceQuery;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.oneops.cassandra.ClusterBootstrap;
import com.oneops.ops.PerfDataRequest;
import com.oneops.ops.PerfHeader;
import com.oneops.sensor.events.PerfEvent;
import com.oneops.sensor.events.PerfEventPayload;

/**
 * @author mike
 * 
 *         PerfDataAccessor - encapsulates cassandra data access for performance
 *         data clients: write: PerfSink uses process() and constructor or
 *         property setters for cassandra connection read:
 *         web.PerfAndLogController uses getPerfData() getLogData() ;
 *         getPerfAvailable() getLogAvailable()
 * 
 */
@Component
public class PerfDataAccessor implements CassandraConstants {

	//Defulting the value to be greater than default timeseconds for hector to connect to cassandra.
	/**
	 * @see ClusterBootstrap
	 */
	protected static final int TIMEOUT_IN_SECONDS = Integer.valueOf(System.getProperty("dao.timeToWaitForCassandra", "7"));;

	private String clusterName;
	private String keyspaceName;
	private String hostName;
	private boolean isTestMode = false;

	protected static final StringSerializer stringSerializer = StringSerializer
			.get();
	protected static final BytesArraySerializer bytesSerializer = BytesArraySerializer
			.get();
	protected static final LongSerializer longSerializer = LongSerializer.get();
	protected static final DoubleSerializer doubleSerializer = DoubleSerializer
			.get();

	protected Cluster cluster;
	protected Keyspace keyspace;

	protected static final String DATA_CF = "data";
	protected static final String HEADER_SCF = "header";
	protected static final String CI_METRIC_CF = "ci_metric";
	protected static final String CHART_SCF = "chart";
	protected static final String[] buckets = { "1m", "5m", "15m", "1h", "6h",
			"1d" };

	private static Logger logger = Logger.getLogger(PerfDataAccessor.class);
	private static PerfHeaderDao phd = null;

	public static int MAX_EXCEPTION_COUNT = 10;


	private ClusterBootstrap cb;

	// few setters and init outside of constructor to workaround spring @Value
	// no worky issue
	/**
	 * Sets the cluster name.
	 * 
	 * @param name
	 *            the new cluster name
	 */
	public void setClusterName(String name) {
		clusterName = name;
	}

	/**
	 * Sets the keyspace name.
	 * 
	 * @param name
	 *            the new keyspace name
	 */
	public void setKeyspaceName(String name) {
		keyspaceName = name;
	}

    /**
     * Sets the cluster bootstrap
     *
     * @param setClusterBootstrap
     */
    public void setClusterBootstrap(ClusterBootstrap cb) {
    	this.cb = cb;
    }

	class Task implements Callable<String> {
		private ClusterBootstrap cb;

		public Task(ClusterBootstrap cb) {
			this.cb = cb;
		}

		@Override
		public String call() throws Exception {
			logger.info("Initializing PerfDataAccessor cluster...");
			cluster = cb.getCluster(clusterName);// # of connections and
													// timeouts are configured
													// as part of system
													// property
			logger.info("Connected to cluster : " + clusterName);
			return "Ready!";
		}
	}

	/**
	 * Inits the DAOs/connections
	 */
	public void init() {
		logger.info("PerfDataAccessor: " + ":" + clusterName
				+ ":" + keyspaceName);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> future = executor.submit(new Task(cb));

		try {
			logger.info("Started connecting.. with timeOut " + TIMEOUT_IN_SECONDS);
			logger.info(future.get(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
			logger.info("Finished connecting!");

		} catch (TimeoutException e) {
			logger.error("no cassandra hosts available - shutting down");
			throw new HectorException("TimeOut occured in getting the cassandra connection");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		executor.shutdownNow();

		if (cluster.getConnectionManager().getActivePools().isEmpty()) {
			logger.error("no cassandra hosts available - shutting down");
			throw new HectorException("no cassandra hosts available ");
		} else {
			logger.info("hosts: "
					+ cluster.getConnectionManager().getHosts().toString());
			logger.info("downed hosts: "
					+ cluster.getConnectionManager().getDownedHosts()
							.toString());
		}

		try {
			cluster.addKeyspace(new ThriftKsDef(keyspaceName,
					"org.apache.cassandra.locator.SimpleStrategy", 1, null));
			logger.info("adding keyspace: " + keyspaceName);
		} catch (HInvalidRequestException e) {
			logger.info(" adding " + keyspaceName + " keyspace: "
					+ e.getMessage());
		}

		ConfigurableConsistencyLevel cl = new ConfigurableConsistencyLevel();
		cl.setDefaultWriteConsistencyLevel(HConsistencyLevel.ONE);
		cl.setDefaultReadConsistencyLevel(HConsistencyLevel.ONE);
		keyspace = createKeyspace(keyspaceName, cluster, cl);

		BasicColumnFamilyDefinition cfo = new BasicColumnFamilyDefinition();

		// add sharded data cf
		for (String bucket : buckets) {
			cfo = new BasicColumnFamilyDefinition();
			cfo.setColumnType(ColumnType.STANDARD);
			String bucketCf = DATA_CF + "_" + bucket;
			if (isTestMode)
				bucketCf += "_test";
			
			cfo.setName(bucketCf);
			cfo.setComparatorType(ComparatorType.BYTESTYPE);
			cfo.setKeyspaceName(keyspaceName);

			try {
				cluster.addColumnFamily(new ThriftCfDef((cfo)));
				logger.info("adding cf: " + bucketCf);
			} catch (HInvalidRequestException e) {
				logger.info("adding " + bucketCf + " " + e.getMessage());
			}
		}

		// header
		cfo = new BasicColumnFamilyDefinition();
		cfo.setColumnType(ColumnType.SUPER);
		String headerCfName = HEADER_SCF;
		if (isTestMode)
			headerCfName += "_test";
		cfo.setName(headerCfName);
		cfo.setComparatorType(ComparatorType.BYTESTYPE);
		cfo.setSubComparatorType(ComparatorType.BYTESTYPE);
		cfo.setKeyspaceName(keyspaceName);

		try {
			cluster.addColumnFamily(new ThriftCfDef((cfo)));
			logger.info("adding cf: " + headerCfName);
		} catch (HInvalidRequestException e) {
			logger.info("adding " + headerCfName + " " + e.getMessage());
		}

		// chart
		cfo = new BasicColumnFamilyDefinition();
		cfo.setColumnType(ColumnType.SUPER);
		cfo.setName(CHART_SCF);
		cfo.setComparatorType(ComparatorType.ASCIITYPE);
		cfo.setSubComparatorType(ComparatorType.ASCIITYPE);
		cfo.setKeyspaceName(keyspaceName);

		try {
			cluster.addColumnFamily(new ThriftCfDef((cfo)));
			logger.info("adding cf: " + CHART_SCF);
		} catch (HInvalidRequestException e) {
			logger.info("adding " + CHART_SCF + " " + e.getMessage());
		}

		phd = new PerfHeaderDao();
		phd.setClusterBootstrap(cb);
		phd.setClusterName(clusterName);
		phd.setKeyspaceName(keyspaceName);
		phd.init();

		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			try {
				hostName = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
				logger.error("could not get hostname or ip...fail");
				//Wrapping it with Hector exception as it causes Cassandra Connection Failures
				throw new HectorException("could not get hostname or ip...fail");
			}
		}

	}

	private String getRraByStat(String stat, int reqStep) {
		return stat +"-"+ getShard(reqStep);
	}
	
	private String getShard(int reqStep) {
		String rra = null;

		if (reqStep < 300)
			rra = "1m";
		else if (reqStep < 900)
			rra = "5m";
     	else if (reqStep < 3600)
			rra = "15m";
     	else if (reqStep < 21600)
			rra = "1h";
     	else if (reqStep < 86000)
     		rra = "6h";
     	else
     		rra = "1d";
		
		return rra;
	}	
	

	private int alignRraStep(int reqStep) {
		int step = 60;

		// 1 day
		if (reqStep > 86000) {
			step = 86400;
			// 6hr
		} else if (reqStep > 21599) {
			step = 21600;
			// 1hr
		} else if (reqStep > 3599) {
			step = 3600;
			// 15min
		} else if (reqStep > 899) {
			step = 900;
			// 5min
		} else if (reqStep > 299) {
			step = 300;
		}
		return step;
	}

	/**
	 * Gets the perf data series. Contains tmp code for conversion of data cf to sharded data cfs
	 * 
	 * @param req the request object
	 * @return the perf data series
	 */
	public String getPerfDataSeries(PerfDataRequest req) {

		Long start = Long.valueOf(req.getStart());
		Long end = Long.valueOf(req.getEnd());
		int maxColumns = (int) (end - start);
		StringBuilder jsonOut = new StringBuilder("");

		try {
			long startTime = System.currentTimeMillis();
			String stat = "rra-average";
			if (req.getStat_function() != null) {
				stat = "rra-" + req.getStat_function();
			}

			String rra = getRraByStat(stat, req.getStep());
			int step = alignRraStep(req.getStep());
			List<byte[]> keys = new ArrayList<byte[]>();
			StringBuilder sb = new StringBuilder("");			
			long adjustedStart = start - start % step;
			String dataCF = DATA_CF + "_" + getShard(step);
			if (isTestMode)
				dataCF += "_test";

			MultigetSliceQuery<byte[], Long, Double> multigetSliceQuery = HFactory
					.createMultigetSliceQuery(keyspace, bytesSerializer,
							longSerializer, doubleSerializer);
			multigetSliceQuery.setColumnFamily(dataCF);
			keys = new ArrayList<byte[]>();
			sb = new StringBuilder("");
			for (int i = 0; i < req.getMetrics().length; i++) {
				String metricDs = req.getMetrics()[i];
				String key = Long.valueOf(req.getCi_id()).toString() + ":"+ metricDs + ":" + rra;
				keys.add(key.getBytes());
				sb.append(" " + key);
			}
			multigetSliceQuery.setKeys(keys);
			multigetSliceQuery.setRange(adjustedStart, end, false, maxColumns);

			logger.info("start:" + start + " end:" + end + " for: " + sb);
			long cassStart = System.currentTimeMillis();

			QueryResult<Rows<byte[], Long, Double>> result = multigetSliceQuery.execute();
			Rows<byte[], Long, Double> rows = result.get();
			
			long cassEnd = System.currentTimeMillis();
			long cassDuration = cassEnd - cassStart;

			// put the by-metric results into 1 csv-like table
			// (time,metric1,metric2,etc)
			// ... should find faster way to do this, but still 10x faster than
			// gwt DataTable serialization
			int rowCount = 0;
			int totalSampleCount = 0;
			HashMap<String,HashMap<Long,Double>> resultMap = new HashMap<String,HashMap<Long,Double>>();
						
			for (Row<byte[], Long, Double> row : rows) {
				
				String rowKey = new String(row.getKey());

				HashMap<Long,Double> results = null;
				if (resultMap.containsKey(rowKey)) {
					results = resultMap.get(rowKey);
			    } else {
			    	results = new HashMap<Long,Double>();
			    	resultMap.put(rowKey,results);
			    }
				
				List<HColumn<Long, Double>> cols = row.getColumnSlice().getColumns();
				Iterator<HColumn<Long, Double>> listIter = cols.listIterator();

				while (listIter.hasNext()) {
					HColumn<Long, Double> c = (HColumn<Long, Double>) listIter.next();
					results.put(c.getName(), c.getValue());
				}
			}

			for (String rowKey : resultMap.keySet()) {
				
				HashMap<Long,Double> results = resultMap.get(rowKey);
				
				if (rowCount > 0) {
					jsonOut.append(",\n");
				}
				
				String[] keyParts = rowKey.split(":");
				String ciId = keyParts[0];
				String metric = keyParts[1] + ":" + keyParts[2];				
	
				jsonOut.append("{ \"header\":{\"ci_id\":" + ciId
						+ ", \"metric\":\"" + metric + "\", \"step\":" + step
						+ ", \"start\":" + adjustedStart + "},\n ");
				jsonOut.append("\"data\":[");			
	
				long currentBucket = adjustedStart;
				int sampleCount = 0;
				int emptyCount = 0;
				
				SortedSet<Long> sortedKeys = new TreeSet<Long>(results.keySet());
				
				for (long sampleBucket : sortedKeys) {
					double value = results.get(sampleBucket);
					if (sampleBucket != currentBucket) {
						while (sampleBucket > currentBucket) {
							if (sampleCount > 0) {
								jsonOut.append(",");
							}
							jsonOut.append("null");
							currentBucket += step;
							emptyCount++;
							sampleCount++;
						}
					}			
					
					if (sampleCount > 0) {
						jsonOut.append(",");
					}
					jsonOut.append((Math.round(value * 1000.0) / 1000.0));			
		
					currentBucket += step;
					totalSampleCount++;
					sampleCount++;
				}
				
				jsonOut.append("]}");
				rowCount++;			
				logger.debug("got samples:" + sampleCount + " gaps:"
						+ emptyCount);			
			}
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;

			logger.debug("getPerfData took: " + duration + " ms (cass query: "
					+ cassDuration + " ms) returning: " + totalSampleCount
					+ " rows of " + rowCount + " metrics");

		} catch (HectorException he) {
			he.printStackTrace();
		}

		return jsonOut.toString();
	}

	public void reportMetricCounts() {

		int rowCount = 0;
		int totalColCount = 0;
		int pageSize = 1000;

		RangeSlicesQuery<String, Long, Double> query = HFactory
				.createRangeSlicesQuery(keyspace, stringSerializer,
						longSerializer, doubleSerializer)
				.setColumnFamily(DATA_CF).setReturnKeysOnly()
				.setRowCount(pageSize);

		String lastKey = null;

		while (true) {
			query.setKeys(lastKey, null);

			QueryResult<OrderedRows<String, Long, Double>> result = query
					.execute();
			OrderedRows<String, Long, Double> orderedRows = result.get();
			Iterator<Row<String, Long, Double>> rowsIterator = orderedRows
					.iterator();

			// we'll skip this first one, since it is the same as the last one
			// from previous time we executed
			if (lastKey != null && rowsIterator != null)
				rowsIterator.next();

			// each row / rra
			while (rowsIterator.hasNext()) {
				Row<String, Long, Double> row = rowsIterator.next();

				// count values
				CountQuery<byte[], Long> cq = HFactory
						.createCountQuery(keyspace, bytesSerializer,
								longSerializer).setColumnFamily(DATA_CF)
						.setKey(row.getKey().getBytes())
						.setRange(1L, null, Integer.MAX_VALUE);

				QueryResult<Integer> r = cq.execute();
				logger.info(row.getKey() + ": " + r.get());

				rowCount++;
				totalColCount += r.get();
				lastKey = row.getKey();
			}
			logger.info("rows: " + rowCount + " cols: " + totalColCount);

			if (orderedRows.getCount() < pageSize)
				break;

		}

	}

	public void purgeMetrics(long time) {
		purgeMetrics(time, "1m");
	}

	public void purgeMetrics(long time, String bucket) {

		int rowCount = 0;
		int totalColCount = 0;
		int totalColDeleted = 0;

		Long start = 1L;
		Long end = time;

		// safeguard not to delete anything in past week
		long now = System.currentTimeMillis() / 1000;
		logger.info("       now: " + now);
		logger.info("startEpoch: " + time);

		if (time + (60 * 60 * 24 * 7) > now) {
			logger.error("input time too soon - cannot be within past week");
			return;
		}

		int maxColumns = (int) (end - start);
		int pageSize = 1000;
		String lastKey = null;

		Mutator<byte[]> mutator = HFactory.createMutator(keyspace,
				bytesSerializer);

		RangeSlicesQuery<String, Long, Double> query = HFactory
				.createRangeSlicesQuery(keyspace, stringSerializer,
						longSerializer, doubleSerializer)
				.setColumnFamily(DATA_CF).setReturnKeysOnly()
				.setRowCount(pageSize);

		while (true) {
			query.setKeys(lastKey, null);

			QueryResult<OrderedRows<String, Long, Double>> result = query
					.execute();
			OrderedRows<String, Long, Double> orderedRows = result.get();
			Iterator<Row<String, Long, Double>> rowsIterator = orderedRows
					.iterator();

			// we'll skip this first one, since it is the same as the last one
			// from previous time we executed
			if (lastKey != null && rowsIterator != null)
				rowsIterator.next();

			while (rowsIterator.hasNext()) {
				Row<String, Long, Double> row = rowsIterator.next();

				if (!row.getKey().endsWith("-" + bucket)) {
					continue;
				}

				rowCount++;
				lastKey = row.getKey();

				List<byte[]> keys = new ArrayList<byte[]>();
				keys.add(row.getKey().getBytes());

				MultigetSliceQuery<byte[], Long, Double> multigetSliceQuery = HFactory
						.createMultigetSliceQuery(keyspace, bytesSerializer,
								longSerializer, doubleSerializer)
						.setColumnFamily(DATA_CF).setKeys(keys)
						.setRange(start, end, false, maxColumns);

				QueryResult<Rows<byte[], Long, Double>> colResult = multigetSliceQuery
						.execute();
				Rows<byte[], Long, Double> rows = colResult.get();

				int sampleCount = 0;
				int deletedCount = 0;
				for (Row<byte[], Long, Double> rowResult : rows) {

					List<HColumn<Long, Double>> cols = rowResult
							.getColumnSlice().getColumns();
					Iterator<HColumn<Long, Double>> listIter = cols
							.listIterator();

					while (listIter.hasNext()) {
						HColumn<Long, Double> c = (HColumn<Long, Double>) listIter
								.next();

						if (c.getName() < time) {

							mutator.addDeletion(row.getKey().getBytes(),
									DATA_CF, c.getName(), longSerializer);

							deletedCount++;
						}
						sampleCount++;
					}

					totalColDeleted += deletedCount;
					totalColCount += sampleCount;

					mutator.execute();
				}

				logger.info(row.getKey() + ": " + sampleCount + " deleted: "
						+ deletedCount);
				if (rows.getCount() < pageSize)
					break;

			}
			logger.info("rows: " + rowCount + " cols: " + totalColCount
					+ " deleted: " + totalColDeleted);

			if (orderedRows.getCount() < pageSize)
				break;

		}

	}

	public List<HSuperColumn<String, String, String>> getChart(String key) {
		SuperSliceQuery<String, String, String, String> q = HFactory
				.createSuperSliceQuery(keyspace, stringSerializer,
						stringSerializer, stringSerializer, stringSerializer);
		q.setColumnFamily(CHART).setKey(key).setRange(null, null, false, 100);

		// execute query and get result list
		QueryResult<SuperSlice<String, String, String>> result = q.execute();
		return result.get().getSuperColumns();
	}

	public void insert(Mutator<String> mutator, String key,
			Map<String, String> columnsMap, String columnFamily,
			String superColumn) {
		List<HColumn<String, String>> columns = new ArrayList<HColumn<String, String>>();
		for (String name : columnsMap.keySet()) {
			HFactory.createStringColumn(name, columnsMap.get(name));
		}
		mutator.insert(key, columnFamily, HFactory.createSuperColumn(
				superColumn, columns, stringSerializer, stringSerializer,
				stringSerializer));
	}

	public Mutator<String> newMutator() {
		return createMutator(keyspace, stringSerializer);
	}

	public void execute(Mutator<String> mutator) {
		mutator.execute();
	}

	public void writeSampleToHeaderAndBuckets(String key, long endTime,
			PerfHeader header, Map<String, Double> data,
			HashMap<String, PerfEvent> perfEventMap)

	throws IOException {

		StringBuilder pendingKeys = new StringBuilder("");
		Mutator<byte[]> mutator = createMutator(keyspace, bytesSerializer);

		phd.putHeader(key, header, mutator);

		// write the buckets / archives
		for (String dsRraTime : data.keySet()) {
			// only supporting avg due to volume
			if (!dsRraTime.contains("rra-average"))
				continue;

			String[] dsRraTimeParts = dsRraTime.split("::");
			String dsRra = dsRraTimeParts[0];
			String bucketKey = key + ":" + dsRra;
			long bucketEndTime = Long.parseLong(dsRraTimeParts[1]);

			Double cdpValue = Math
					.round(data.get(dsRraTime).doubleValue() * 1000.0) / 1000.0;
			if (dsRra.endsWith(LOGBUCKET)) {
				logger.info("write " + bucketKey + " : " + cdpValue);
			}
			String shard = dsRra.substring(dsRra.length() - 3).replace("-", "");
			int ttl = getTTL(shard);
			HColumn<Long, Double> column = createDataColumn(bucketEndTime,
					cdpValue.doubleValue());
			column.setTtl(ttl);

			String dataCF = DATA_CF + "_" + shard;
			if (isTestMode)
				dataCF += "_test";

			mutator.addInsertion(bucketKey.getBytes(), dataCF, column);
			pendingKeys.append(" ," + bucketKey);

			// send the consolidated perf event to sensor
			PerfEvent pe = null;
			String[] rraParts = dsRra.split("-");
			String eventBucket = rraParts[rraParts.length - 1];
			if (perfEventMap.containsKey(eventBucket)) {
				pe = perfEventMap.get(eventBucket);
			} else {
				pe = setEventBucket(perfEventMap, eventBucket);
			}

			String ds = rraParts[0].replace(":rra", "");
			String rraType = rraParts[1];

			if (rraType.equalsIgnoreCase(AVERAGE)) {
				pe.getMetrics().addAvg(ds, cdpValue);
			} else if (rraType.equalsIgnoreCase(COUNT)) {
				pe.getMetrics().addCount(ds, cdpValue);
			} else if (rraType.equalsIgnoreCase(MAX)) {
				pe.getMetrics().addMax(ds, cdpValue);
			} else if (rraType.equalsIgnoreCase(MIN)) {
				pe.getMetrics().addMin(ds, cdpValue);
			} else if (rraType.equalsIgnoreCase(SUM)) {
				pe.getMetrics().addSum(ds, cdpValue);
			}

		}

		logger.debug("write keys:" + pendingKeys);

		// perform the insert/updates
		mutator.execute();
	}
	
	private String translateBucket(String bucket) {		
		String parts[] = bucket.split("-");		
		if (parts[1].equals("avg"))
			return "rra-average-"+parts[0];
		else if (parts[1].equals("min"))
			return "rra-min-"+parts[0];
		else if (parts[1].equals("max"))
			return "rra-max-"+parts[0];
		
		return "";
	}
	
	
	public void writeBucket(PerfEvent perfEvent)
	throws IOException {
		Mutator<byte[]> mutator = createMutator(keyspace, bytesSerializer);
		
		String columnKey = perfEvent.getCiId()+":"+perfEvent.getGrouping();
		String aggregate = translateBucket(perfEvent.getBucket());
		String shard = aggregate.substring(aggregate.length() - 3).replace("-", "");
		int ttl = getTTL(shard);
		String dataCF = DATA_CF + "_" + shard;
		if (isTestMode)
			dataCF += "_test";
	    
		if (perfEvent.getMetrics().getAvg() != null) {
			for (String key : perfEvent.getMetrics().getAvg().keySet()) {
				Double value = perfEvent.getMetrics().getAvg().get(key);
				String bucketKey = columnKey + ":" + key+":"+ aggregate;
				long bucketEndTime = perfEvent.getTimestamp();
	
				logger.debug("write "+dataCF+' ' + bucketKey + " "+bucketEndTime+":" + value);
				HColumn<Long, Double> column = createDataColumn(bucketEndTime,value.doubleValue());
				column.setTtl(ttl);	
				mutator.addInsertion(bucketKey.getBytes(), dataCF, column);			
			}
		}
		
		if (perfEvent.getMetrics().getMin() != null) {
			for (String key : perfEvent.getMetrics().getMin().keySet()) {
				Double value = perfEvent.getMetrics().getMin().get(key);
				String bucketKey = columnKey + ":" + key+":"+ aggregate;
				long bucketEndTime = perfEvent.getTimestamp();
	
				logger.debug("write "+dataCF+' ' + bucketKey + " "+bucketEndTime+":" + value);
				HColumn<Long, Double> column = createDataColumn(bucketEndTime,value.doubleValue());
				column.setTtl(ttl);	
				mutator.addInsertion(bucketKey.getBytes(), dataCF, column);			
			}
		}

		if (perfEvent.getMetrics().getMax() != null) {
			for (String key : perfEvent.getMetrics().getMax().keySet()) {
				Double value = perfEvent.getMetrics().getMax().get(key);
				String bucketKey = columnKey + ":" + key+":"+ aggregate;
				long bucketEndTime = perfEvent.getTimestamp();
	
				logger.debug("write "+dataCF+' ' + bucketKey + " "+bucketEndTime+":" + value);
				HColumn<Long, Double> column = createDataColumn(bucketEndTime,value.doubleValue());
				column.setTtl(ttl);	
				mutator.addInsertion(bucketKey.getBytes(), dataCF, column);			
			}
		}		
		
		// perform the insert/updates
		mutator.execute();
	}
	
	

	private int getTTL(String dsRra) {
		// 1m - 2d
		// 5m - 10d
		// 15m- 30d
		// 1h - 120d
		// 6h - 720d
		// 1d - 2880d
		int ttl = 86400 *2;
		if (dsRra.equals("1m")) {
			return ttl;
		} else if (dsRra.equals("5m")) {
			ttl = 86400 * 10;
		} else if (dsRra.equals("15m")) {
			ttl = 86400 * 30;
		} else if (dsRra.equals("1h")) {
			ttl = 86400 * 120;
		} else if (dsRra.equals("6h")) {
			ttl = 86400 * 720;
		} else {
			ttl = 86400 * 2880;
		}
		return ttl;
	}

	private PerfEvent setEventBucket(HashMap<String, PerfEvent> perfEventMap,
			String bucketName) {
		PerfEvent pe = new PerfEvent();
		PerfEvent base = perfEventMap.get("1m");
		pe.setBucket(bucketName);

		pe.setCiId(base.getCiId());
		pe.setGrouping(base.getGrouping());
		pe.setManifestId(base.getManifestId());
		pe.setSource(base.getSource());
		pe.setTimestamp(base.getTimestamp());

		PerfEventPayload load = new PerfEventPayload();
		load.setAvg(new HashMap<String, Double>());
		load.setMax(new HashMap<String, Double>());
		load.setMin(new HashMap<String, Double>());
		load.setSum(new HashMap<String, Double>());
		load.setCount(new HashMap<String, Double>());
		pe.setMetrics(load);
		perfEventMap.put(bucketName, pe);
		return pe;
	}

	/*
	 * sets the long timestamp, double value of a given metric-instance:rra
	 */
	private HColumn<Long, Double> createDataColumn(long name, double value) {
		return HFactory.createColumn(name, value, longSerializer,
				doubleSerializer);
	}

	/*
	 * given a set of metric,start,end,step does a cassandra query and returns
	 * json;
	 */
	/**
	 * Gets the perf data table.
	 * 
	 * @param req
	 *            the req
	 * @return the perf data table
	 */
	@SuppressWarnings("unchecked")
	public String getPerfDataTable(PerfDataRequest req) {

		Long start = Long.valueOf(req.getStart());
		Long end = Long.valueOf(req.getEnd());
		int maxColumns = (int) (end - start);
		String jsonOut = "";

		try {
			long startTime = System.currentTimeMillis();
			String stat = "rra-average";
			if (req.getStat_function() != null) {
				stat = "rra-" + req.getStat_function();
			}

			String rra = getRraByStat(stat, req.getStep());
			int step = alignRraStep(req.getStep());

			MultigetSliceQuery<byte[], Long, Double> multigetSliceQuery = HFactory
					.createMultigetSliceQuery(keyspace, bytesSerializer,
							longSerializer, doubleSerializer);
			multigetSliceQuery.setColumnFamily(DATA_CF);
			List<byte[]> keys = new ArrayList<byte[]>();
			StringBuilder keyString = new StringBuilder("");
			for (int i = 0; i < req.getMetrics().length; i++) {
				String metricDs = req.getMetrics()[i];
				String key = Long.valueOf(req.getCi_id()).toString() + ":"
						+ metricDs + ":" + rra;
				keys.add(key.getBytes());
				keyString.append(" ").append(key);
			}
			multigetSliceQuery.setKeys(keys);
			long adjustedStart = bucketize(start, step);
			multigetSliceQuery.setRange(adjustedStart, end, false, maxColumns);

			multigetSliceQuery.setRange(start, end, false, maxColumns);

			logger.debug("start:" + start + " end:" + end + " for: "
					+ keyString);
			long cassStart = System.currentTimeMillis();

			QueryResult<Rows<byte[], Long, Double>> result = multigetSliceQuery
					.execute();
			Rows<byte[], Long, Double> rows = result.get();

			long cassEnd = System.currentTimeMillis();
			long cassDuration = cassEnd - cassStart;

			Map<String, Object> valMap = new HashMap<String, Object>();
			Map<Long, Integer> timeMap = new TreeMap<Long, Integer>();
			List<String> rowKeys = new ArrayList<String>();

			// put the by-metric results into 1 csv-like table
			// (time,metric1,metric2,etc)
			// ... should find faster way to do this, but still 10x faster than
			// gwt DataTable serialization
			int rowCount = 0;
			for (Row<byte[], Long, Double> row : rows) {

				String rowKey = new String(row.getKey());
				Map<Long, Double> colMap = new HashMap<Long, Double>();
				valMap.put(rowKey, colMap);
				rowKeys.add(rowKey);
				jsonOut += ",\"" + rowKey + "\"";

				List<HColumn<Long, Double>> cols = row.getColumnSlice()
						.getColumns();
				Iterator<HColumn<Long, Double>> listIter = cols.listIterator();
				while (listIter.hasNext()) {
					HColumn<Long, Double> c = (HColumn<Long, Double>) listIter
							.next();
					colMap.put(c.getName(), c.getValue());
					timeMap.put(c.getName(), 1);
				}
				rowCount++;
			}
			jsonOut += "]\n";

			int resultRowCount = 0;
			for (Long time : timeMap.keySet()) {
				jsonOut += ",[" + time;

				for (int i = 0; i < rowCount; i++) {
					String rowKey = rowKeys.get(i);
					Map<Long, Double> colMap = (Map<Long, Double>) valMap
							.get(rowKey);
					Double val = colMap.get(time);
					jsonOut += ",";
					// round to 1000ths ; client doesn't need to process all the
					// 10 decimal point resolution
					if (val != null) {
						jsonOut += Math.round(val * 1000.0) / 1000.0;
					}
				}
				jsonOut += "]\n";
				resultRowCount++;
			}

			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;

			logger.info("getPerfData took: " + duration + " ms (cass query: "
					+ cassDuration + " ms) returning: " + resultRowCount
					+ " rows of " + rowCount + " metrics");

		} catch (HectorException he) {
			he.printStackTrace();
		}

		return jsonOut;
	}

	/**
	 * Bucketize.
	 * 
	 * @param timestamp
	 *            the timestamp
	 * @param step
	 *            the step
	 * @return the long
	 */
	public static long bucketize(long timestamp, long step) {
		return timestamp - timestamp % step;
	}

	public PerfHeader getHeader(String columnKey) {
		return phd.getHeader(columnKey);
	}

	public String getHostName() {
		return hostName;
	}

	public boolean isTestMode() {
		return isTestMode;
	}

	public void setTestMode(boolean isTestMode) {
		this.isTestMode = isTestMode;
	}	
	
}
