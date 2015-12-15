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
package com.oneops.sensor.thresholds;

import com.oneops.cassandra.ClusterBootstrap;
import com.oneops.ops.dao.OpsCiStateDao;
import com.oneops.sensor.schema.SchemaBuilder;
import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.serializers.BooleanSerializer;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.*;
import me.prettyprint.hector.api.exceptions.HTimedOutException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.*;
import org.apache.log4j.Logger;
import rx.Observable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThresholdsDao {

    private static final Logger logger = Logger.getLogger(ThresholdsDao.class);

    private static final int STEP_LIMIT = 1000;
    protected static final StringSerializer stringSerializer = StringSerializer.get();
    protected static final BytesArraySerializer bytesSerializer = BytesArraySerializer.get();
    protected static final LongSerializer longSerializer = LongSerializer.get();
    protected static final BooleanSerializer booleanSerializer = BooleanSerializer.get();
    private final Pattern histFuncPattern = Pattern.compile("historic\\('(.*?)'\\)");

    private ClusterBootstrap cb;
    private String clusterName;
    private String keyspaceName;
    private Keyspace keyspace;
    private OpsCiStateDao opsCiStateDao;
    private Mutator<Long> thresholdMutator;
    private Mutator<Long> manifestMapMutator;
    private Mutator<Long> realizedAsMutator;
    private int timeout = 300 * 1000;

    /**
     * Sets the cassandra cluster name
     *
     * @param clusterName cluster name
     */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setOpsCiStateDao(OpsCiStateDao opsCiStateDao) {
        this.opsCiStateDao = opsCiStateDao;
    }

    /**
     * Sets the cassandra keyspace name
     *
     * @param keyspaceName keyspace name
     */
    public void setKeyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    }

    /**
     * Sets the cassandra thrift timeout
     *
     * @param timeout timeout value is millis
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Sets the cluster bootstrap
     *
     * @param cb ClusterBootstrap
     */
    public void setClusterBootstrap(ClusterBootstrap cb) {
        this.cb = cb;
    }

    /**
     * Bean post init method. The following configuration is used
     * for initializing the ThresholdsDao cassandra cluster,
     * <p>
     * <ul>
     * <li>Active clients per node - 4</li>
     * <li>Cassandra Thrift timeout - 600 sec</li>
     * </ul>
     */
    public void init() {
        logger.info("Initializing Monitor Thresholds Dao...");

        Cluster cluster = cb.getCluster(clusterName, 4, timeout);
        logger.info("Connected to cluster : " + clusterName);

        SchemaBuilder.createSchema(cluster, keyspaceName);
        ConfigurableConsistencyLevel cl = new ConfigurableConsistencyLevel();
        cl.setDefaultWriteConsistencyLevel(HConsistencyLevel.ONE);
        cl.setDefaultReadConsistencyLevel(HConsistencyLevel.ONE);

        keyspace = HFactory.createKeyspace(keyspaceName, cluster, cl);
        thresholdMutator = HFactory.createMutator(keyspace, longSerializer);
        manifestMapMutator = HFactory.createMutator(keyspace, longSerializer);
        realizedAsMutator = HFactory.createMutator(keyspace, longSerializer);
    }


    public void removeManifestThreshold(long manifestId, String source) {
        logger.info("removeManifestThreshold(" + Long.toString(manifestId) + ',' + source + ')');
        thresholdMutator.delete(manifestId, SchemaBuilder.THRESHOLDS_CF, source, stringSerializer);
    }

    private void removeAllManifestThresholds(long manifestId) {
        logger.info("removeAllManifestThresholds(" + Long.toString(manifestId) + ')');
        thresholdMutator.addDeletion(manifestId, SchemaBuilder.THRESHOLDS_CF);
        thresholdMutator.execute();
    }

    public void addManifestMap(long ciId, long manifestId) {
        if (getManifestId(ciId) == null) {
            manifestMapMutator.insert(ciId, SchemaBuilder.MANIFESTMAP_CF, createDataColumn("manifestid", manifestId));
        }
        // we need to add another map manifestId -> [ciIds] so we can track if all of the bom CIs are removed and we can remove the threshold
        if (!isManifestToCiIdExists(manifestId, ciId)) {
            realizedAsMutator.insert(manifestId, SchemaBuilder.REALIZED_AS_CF, createDataColumn(ciId, null));
            opsCiStateDao.incComponentsStateCounter(manifestId, OpsCiStateDao.COMPONENT_STATE_TOTAL, 1);
            opsCiStateDao.incComponentsStateCounter(manifestId, OpsCiStateDao.COMPONENT_STATE_GOOD, 1);
        }
    }

    public int removeManifestMap(long ciId, Long manifestIdin) {
        long manifestId = 0;
        if (manifestIdin != null) {
            manifestId = manifestIdin;
        } else {
            Long manifestIdfromMap = getManifestId(ciId);
            if (manifestIdfromMap != null) {
                manifestId = manifestIdfromMap;
            }
        }
        int remainingIdsInMap = 0;
        if (manifestId > 0) {
            realizedAsMutator.addDeletion(manifestId, SchemaBuilder.REALIZED_AS_CF, ciId, longSerializer);
            realizedAsMutator.execute();
            //we don't need a full count just indication that some bom CIs exists
            remainingIdsInMap = getManifestCount(manifestId, 1);
            if (remainingIdsInMap == 0) {
                //lets remove manifest->ciIDs map
                realizedAsMutator.addDeletion(manifestId, SchemaBuilder.REALIZED_AS_CF);
                realizedAsMutator.execute();
                //and remove the thresholds
                removeAllManifestThresholds(manifestId);
            }
        }
        manifestMapMutator.addDeletion(ciId, SchemaBuilder.MANIFESTMAP_CF);
        manifestMapMutator.execute();
        return remainingIdsInMap;
    }

    public void removeRealizedAsRow(Long manifestId) {
        realizedAsMutator.addDeletion(manifestId, SchemaBuilder.REALIZED_AS_CF);
        realizedAsMutator.execute();
        //and remove the thresholds
        removeAllManifestThresholds(manifestId);
    }


    private String extractHistoricDefs(String tresholdsJson) {
        String histMetrics = null;
        Matcher histMatch = histFuncPattern.matcher(tresholdsJson);
        while (histMatch.find()) {
            logger.info("historic metric request : " + histMatch.group());
            if (histMetrics == null) {
                histMetrics = histMatch.group(1);
            } else {
                histMetrics += "," + histMatch.group(1);
            }
        }

        return histMetrics;
    }


    public Long getManifestId(long ciId) {

        SliceQuery<Long, String, Long> sliceQuery = HFactory.createSliceQuery(
                keyspace,
                longSerializer,
                stringSerializer,
                longSerializer)
                .setColumnFamily(SchemaBuilder.MANIFESTMAP_CF)
                .setColumnNames("manifestid")
                .setKey(ciId);

        QueryResult<ColumnSlice<String, Long>> result = sliceQuery.execute();
        ColumnSlice<String, Long> resultCols = result.get();
        if (resultCols.getColumnByName("manifestid") != null) {
            return resultCols.getColumnByName("manifestid").getValue();
        }
        return null;
    }


    public boolean isManifestToCiIdExists(long manifestId, long ciId) {

        SliceQuery<Long, Long, Long> sliceQuery = HFactory.createSliceQuery(keyspace, longSerializer, longSerializer, longSerializer);
        sliceQuery.setColumnFamily(SchemaBuilder.REALIZED_AS_CF);
        sliceQuery.setColumnNames(ciId);
        sliceQuery.setKey(manifestId);
        QueryResult<ColumnSlice<Long, Long>> result = sliceQuery.execute();
        ColumnSlice<Long, Long> resultCols = result.get();
        if (resultCols != null && resultCols.getColumnByName(ciId) != null) {
            return true;
        }
        return false;
    }

    public List<Long> getManifestCiIds(long manifestId) {
        return getManifestCiIds(manifestId, 1, 0, true);
    }


    public List<Long> getManifestCiIds(long manifestId, int retries, long sleep, boolean ignoreTimeOutException) {
        List<Long> ciIDs = new ArrayList<>();
        for (int i = 1; i <= retries; i++) {
            try {
                SliceQuery<Long, Long, Long> sliceQuery = HFactory.createSliceQuery(
                        keyspace,
                        longSerializer,
                        longSerializer,
                        longSerializer)
                        .setColumnFamily(SchemaBuilder.REALIZED_AS_CF)
                        .setRange(null, null, false, 10000)
                        .setKey(manifestId);

                QueryResult<ColumnSlice<Long, Long>> result = sliceQuery.execute();
                ColumnSlice<Long, Long> resultCols = result.get();
                if (resultCols != null) {
                    for (HColumn<Long, Long> col : resultCols.getColumns()) {
                        ciIDs.add(col.getName());
                    }
                }
            } catch (HTimedOutException hte) {
                logger.error("getManifestCiIds(" + manifestId + ") threw: " + hte.getLocalizedMessage());
                logger.error(hte.getMessage());
                logger.error(hte.getStackTrace());
                if (i == retries) {
                    if (!ignoreTimeOutException) {
                        throw hte;
                    }
                } else {
                    if (sleep > 0) {
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return ciIDs;
    }

    public Map<Long, List<Long>> getManifestCiIds(List<Long> manifestIds) {

        MultigetSliceQuery<Long, Long, Long> msQuery = HFactory.createMultigetSliceQuery(keyspace, longSerializer, longSerializer, longSerializer);
        msQuery.setColumnFamily(SchemaBuilder.REALIZED_AS_CF);
        msQuery.setRange(null, null, false, 10000);
        msQuery.setKeys(manifestIds);

        Rows<Long, Long, Long> rows = msQuery.execute().get();
        Map<Long, List<Long>> manifestCiIds = new HashMap<>();
        if (rows != null) {

            for (Row<Long, Long, Long> row : rows) {
                ColumnSlice<Long, Long> slice = row.getColumnSlice();

                if (slice != null && slice.getColumns().size() > 0) {
                    manifestCiIds.put(row.getKey(), new ArrayList<>());
                    for (HColumn<Long, Long> col : slice.getColumns()) {
                        manifestCiIds.get(row.getKey()).add(col.getName());
                    }
                }
            }
        }
        return manifestCiIds;
    }

    public List<Long> getAllManifestIds() {

        List<Long> manifestIds = new ArrayList<>();
        RangeSlicesQuery<Long, Long, Long> rangeSlicesQuery =
                HFactory.createRangeSlicesQuery(keyspace, longSerializer, longSerializer, longSerializer)
                        .setColumnFamily(SchemaBuilder.REALIZED_AS_CF)
                        .setRange(null, null, false, 1)
                        .setRowCount(STEP_LIMIT)
                        .setReturnKeysOnly();

        Long lastKey = null;

        while (true) {
            rangeSlicesQuery.setKeys(lastKey, null);

            OrderedRows<Long, Long, Long> rows = rangeSlicesQuery.execute().get();
            Iterator<Row<Long, Long, Long>> rowsIterator = rows.iterator();

            /**
             * we'll skip this first one, since it is the same as the last one from previous time we executed.
             */
            if (lastKey != null && rowsIterator != null) {
                rowsIterator.next();
            }
            while (rowsIterator.hasNext()) {
                Row<Long, Long, Long> row = rowsIterator.next();
                manifestIds.add(row.getKey());
                lastKey = row.getKey();
            }

            if (rows.getCount() < STEP_LIMIT) {
                break;
            }
        }

        return manifestIds;
    }

    public int getManifestCount(long manifestId, int maxCount) {

        CountQuery<Long, Long> cq = HFactory.createCountQuery(keyspace, longSerializer, longSerializer);
        cq.setColumnFamily(SchemaBuilder.REALIZED_AS_CF);
        cq.setKey(manifestId);
        cq.setRange(null, null, maxCount);

        QueryResult<Integer> result = cq.execute();
        return result.get();
    }


    public void addCiThresholds(long ciId, long manifestId, String source, long checksum, String thresholdsJson, boolean isHeartbeat, String hbDuration) {
        //addManifestMap(ciId, manifestId);
        Threshold threshold = getThresholdNoDef(manifestId, source);
        if (threshold != null && threshold.getCrc() == checksum) return;

        if (threshold != null) {
            //first delete the old value if exists
            thresholdMutator.addSuperDelete(manifestId,
                    SchemaBuilder.THRESHOLDS_CF,
                    source,
                    stringSerializer);
            thresholdMutator.execute();
        }

        List<HColumn<String, byte[]>> subCols = new ArrayList<>();

        subCols.add(HFactory.createColumn("manifestid", longSerializer.toBytes(manifestId), stringSerializer, bytesSerializer));
        subCols.add(HFactory.createColumn("source", stringSerializer.toBytes(source), stringSerializer, bytesSerializer));
        subCols.add(HFactory.createColumn("checksum", longSerializer.toBytes(checksum), stringSerializer, bytesSerializer));
        subCols.add(HFactory.createColumn("thresholds", thresholdsJson.getBytes(), stringSerializer, bytesSerializer));
        subCols.add(HFactory.createColumn("isheartbeat", booleanSerializer.toBytes(isHeartbeat), stringSerializer, bytesSerializer));
        subCols.add(HFactory.createColumn("hbduration", hbDuration.getBytes(), stringSerializer, bytesSerializer));

        String historics = extractHistoricDefs(thresholdsJson);
        if (historics != null) {
            subCols.add(HFactory.createColumn("historics", historics.getBytes(), stringSerializer, bytesSerializer));
        }

        thresholdMutator.insert(manifestId,
                SchemaBuilder.THRESHOLDS_CF,
                HFactory.createSuperColumn(source, subCols, stringSerializer, stringSerializer, bytesSerializer));

        logger.info("Added threshold for ci=" + ciId + "; source=" + source + "; manifestid=" + manifestId + "; checksum=" + checksum);
        logger.info("definition: " + thresholdsJson);
        logger.info("isHeartbeat: " + isHeartbeat);
        logger.info("hbDuration: " + hbDuration);

    }


    public Threshold getThresholdNoDef(long manifestId, String source) {

        SubSliceQuery<Long, String, String, byte[]> scolq = HFactory.createSubSliceQuery(keyspace, longSerializer, stringSerializer, stringSerializer, bytesSerializer);
        scolq.setColumnFamily(SchemaBuilder.THRESHOLDS_CF);
        scolq.setKey(manifestId).
                setSuperColumn(source).setColumnNames("manifestid", "source", "checksum");
        QueryResult<ColumnSlice<String, byte[]>> result = scolq.execute();
        ColumnSlice<String, byte[]> resultCols = result.get();

        if (resultCols.getColumns().size() > 0) {
            return getThresholdFromCols(resultCols);
        } else {
            return null;
        }
    }


    public Threshold getThreshold(long manifestId, String source) {
        SubSliceQuery<Long, String, String, byte[]> scolq = HFactory.createSubSliceQuery(
                keyspace,
                longSerializer,
                stringSerializer,
                stringSerializer,
                bytesSerializer)
                .setColumnFamily(SchemaBuilder.THRESHOLDS_CF)
                .setKey(manifestId)
                .setSuperColumn(source)
                .setRange(null, null, false, 100);
        QueryResult<ColumnSlice<String, byte[]>> result = scolq.execute();
        ColumnSlice<String, byte[]> resultCols = result.get();

        if (resultCols.getColumns().size() > 0) {
            return getThresholdFromCols(resultCols);
        } else {
            return null;
        }
    }

    private Threshold getThresholdFromCols(ColumnSlice<String, byte[]> resultCols) {
        Map<String, HColumn<String, byte[]>> map = new HashMap<>();
        for (HColumn<String, byte[]> col : resultCols.getColumns()) {
            map.put(col.getName(), col);
        }
        return getThresholdFromCols(map);
    }

    private Threshold getThresholdFromCols(List<HColumn<String, byte[]>> resultCols) {
        Map<String, HColumn<String, byte[]>> map = new HashMap<>();
        for (HColumn<String, byte[]> col : resultCols) {
            map.put(col.getName(), col);
        }
        return getThresholdFromCols(map);
    }

    private Threshold getThresholdFromCols(Map<String, HColumn<String, byte[]>> resultCols) {
        Threshold threshold = new Threshold();
        if (resultCols.get("manifestid") != null) {
            threshold.setManifestId(longSerializer.fromBytes(resultCols.get("manifestid").getValue()));
        }
        if (resultCols.get("source") != null) {
            threshold.setSource(stringSerializer.fromBytes(resultCols.get("source").getValue()));
        }
        if (resultCols.get("checksum") != null) {
            threshold.setCrc(longSerializer.fromBytes(resultCols.get("checksum").getValue()));
        }
        if (resultCols.get("thresholds") != null) {
            threshold.setThresholdJson(stringSerializer.fromBytes(resultCols.get("thresholds").getValue()));
        }
        if (resultCols.get("historics") != null) {
            threshold.setHistorics(stringSerializer.fromBytes(resultCols.get("historics").getValue()));
        }
        if (resultCols.get("isheartbeat") != null) {
            threshold.setHeartbeat(booleanSerializer.fromBytes(resultCols.get("isheartbeat").getValue()));
        }
        if (resultCols.get("hbduration") != null) {
            threshold.setHbDuration(stringSerializer.fromBytes(resultCols.get("hbduration").getValue()));
        }
        return threshold;
    }

    /**
     * Creates a threshold stream by reading cassandra in batches.
     *
     * @param batchSize cassandra row count size to read
     * @return Observable stream of Threshold
     */
    public Observable<Threshold> getAllThreshold(final int batchSize) {
        logger.info("Creating Threshold observable.");
        return Observable.create(sub -> {
            logger.info("Starting the threshold stream subscription with batchSize: " + batchSize);
            try {
                RangeSuperSlicesQuery<Long, String, String, byte[]> rssQuery = HFactory.createRangeSuperSlicesQuery(
                        keyspace,
                        longSerializer,
                        stringSerializer,
                        stringSerializer,
                        bytesSerializer)
                        .setColumnFamily(SchemaBuilder.THRESHOLDS_CF)
                        .setRange(null, null, false, STEP_LIMIT)
                        .setRowCount(batchSize);

                Long lastKey = null;
                while (true) {
                    // Do this only if the subscription is active.
                    if (sub.isUnsubscribed()) {
                        logger.error("Observer seems unsubscribed due to cancel/error. Exiting the loop.");
                        break;
                    }

                    rssQuery.setKeys(lastKey, null);
                    OrderedSuperRows<Long, String, String, byte[]> sRows = rssQuery.execute().get();
                    Iterator<SuperRow<Long, String, String, byte[]>> rowIt = sRows.iterator();
                    if (lastKey != null && rowIt != null) rowIt.next();

                    while (rowIt.hasNext()) {
                        SuperRow<Long, String, String, byte[]> sRow = rowIt.next();
                        lastKey = sRow.getKey();
                        sRow.getSuperSlice().getSuperColumns().stream()
                                .map(sCol -> getThresholdFromCols(sCol.getColumns()))
                                .forEach(sub::onNext);
                    }
                    if (sRows.getCount() < batchSize)
                        break;
                }
                logger.info("Completed the threshold stream subscription.");
                sub.onCompleted();

            } catch (Exception ex) {
                logger.error("Error reading thresholds : " + ex.getMessage());
                sub.onError(ex);
            }
        });
    }


    private HColumn<String, Long> createDataColumn(String colName, Long value) {
        return HFactory.createColumn(colName, value, stringSerializer, longSerializer);
    }

    private HColumn<Long, Long> createDataColumn(Long colName, Long value) {
        if (value != null) {
            return HFactory.createColumn(colName, value, longSerializer, longSerializer);
        } else {
            return HFactory.createColumn(colName, Long.MIN_VALUE, longSerializer, longSerializer);
        }
    }

    public Threshold _getThresholdNoDef(String key) {
        SliceQuery<String, String, byte[]> sliceQuery = HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer, bytesSerializer);
        sliceQuery.setColumnFamily(SchemaBuilder.THRESHOLDS_CF);
        sliceQuery.setColumnNames("checksum", "historics");
        sliceQuery.setKey(key);

        QueryResult<ColumnSlice<String, byte[]>> result = sliceQuery.execute();
        ColumnSlice<String, byte[]> resultCols = result.get();

        if (resultCols.getColumns().size() > 0) {
            Threshold threshold = new Threshold();
            if (resultCols.getColumnByName("checksum") != null) {
                threshold.setCrc(longSerializer.fromBytes(resultCols.getColumnByName("checksum").getValue()));
            }

            if (resultCols.getColumnByName("historics") != null) {
                threshold.setHistorics(stringSerializer.fromBytes(resultCols.getColumnByName("historics").getValue()));
            }

            return threshold;
        } else {
            return null;
        }
    }

    public void addCiThresholds(long ciId, Threshold threshold) {
        addCiThresholds(ciId,
                threshold.getManifestId(),
                threshold.getSource(),
                threshold.getCrc(),
                threshold.getThresholdJson(),
                threshold.isHeartbeat(),
                threshold.getHbDuration());
    }

	/*
    public List<Threshold> getAllThresholdBulk() {

		List<Threshold> thresholds = new ArrayList<Threshold>();
		
		RangeSlicesQuery<String, String, byte[]> rsQuery = HFactory.createRangeSlicesQuery(keyspace, stringSerializer, stringSerializer, bytesSerializer);
		
		rsQuery.setColumnFamily(SchemaBuilder.THRESHOLDS_CF);
		rsQuery.setColumnNames("manifestid", "source","checksum", "thresholds", "historics", "isheartbeat", "hbduration");
		rsQuery.setKeys(null, null);
		QueryResult<OrderedRows<String, String, byte[]>> result = rsQuery.execute();
		OrderedRows<String, String, byte[]> rows = result.get();
		
		for (Row<String, String, byte[]> row : rows.getList()) {
			ColumnSlice<String, byte[]> resultCols = row.getColumnSlice();
			thresholds.add(getThresholdFromCols(resultCols));
		}
		
		return thresholds;
	}
	*/

	/*
    private List<String> getManifestKeys(long manifestId) {

		List<String> keys = new ArrayList<String>();
		
		RangeSlicesQuery<String, String, byte[]> rsQuery = HFactory.createRangeSlicesQuery(keyspace, stringSerializer, stringSerializer, bytesSerializer);
		
		rsQuery.setColumnFamily(SchemaBuilder.THRESHOLDS_CF);
		rsQuery.setColumnNames("manifestid");
		rsQuery.setReturnKeysOnly();
		String startKey = manifestId + "::";
		String endKey = manifestId + ":";// + (char)Byte.MAX_VALUE;
		rsQuery.setKeys(startKey, endKey);
		
		QueryResult<OrderedRows<String, String, byte[]>> result = rsQuery.execute();
		OrderedRows<String, String, byte[]> rows = result.get();
		
		for (Row<String, String, byte[]> row : rows.getList()) {
			keys.add(row.getKey());
		}
		
		return keys;
	}
	*/


}
