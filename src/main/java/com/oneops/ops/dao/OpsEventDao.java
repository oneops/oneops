package com.oneops.ops.dao;

import com.google.gson.Gson;
import com.oneops.cassandra.ClusterBootstrap;
import com.oneops.ops.events.CiOpenEvent;
import com.oneops.ops.events.OpsEvent;
import com.oneops.sensor.schema.SchemaBuilder;
import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.*;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.*;
import org.apache.log4j.Logger;
import rx.Observable;

import java.util.*;

import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;

public class OpsEventDao {

    private static Logger logger = Logger.getLogger(OpsEventDao.class);
    protected static final StringSerializer stringSerializer = StringSerializer.get();
    protected static final BytesArraySerializer bytesSerializer = BytesArraySerializer.get();
    protected static final LongSerializer longSerializer = LongSerializer.get();

    private String clusterName;
    private String keyspaceName;
    private Keyspace keyspace;
    protected Mutator<byte[]> eventMutator;
    protected Mutator<Long> ciMutator;
    private ClusterBootstrap cb;
    private Gson gson = new Gson();

    /**
     * Sets the cassandra cluster name
     *
     * @param clusterName
     */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * Sets the keyspace to query.
     *
     * @param keyspaceName
     */
    public void setKeyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
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
     * for initializing the OpsEventDao cassandra cluster,
     * <p>
     * <ul>
     * <li>Active clients per node - 4</li>
     * <li>Cassandra Thrift timeout - 5 sec </li>
     * </ul>
     */
    public void init() {
        logger.info("Initializing OpsEvent Dao...");
        Cluster cluster = cb.getCluster(clusterName, 4, 5 * 1000);
        logger.info("Connected to cluster : " + clusterName);

        SchemaBuilder.createSchema(cluster, keyspaceName);
        ConfigurableConsistencyLevel cl = new ConfigurableConsistencyLevel();
        cl.setDefaultWriteConsistencyLevel(HConsistencyLevel.ONE);
        cl.setDefaultReadConsistencyLevel(HConsistencyLevel.ONE);
        keyspace = createKeyspace(keyspaceName, cluster, cl);
        eventMutator = HFactory.createMutator(keyspace, bytesSerializer);
        ciMutator = HFactory.createMutator(keyspace, longSerializer);
    }


    /**
     * Persists OpsEvent to the keyspace.
     *
     * @param ciId
     * @param eventName
     * @param timestamp
     * @param payload
     */
    public void persistOpsEvent(long ciId, String eventName, long timestamp, String payload) {
        String key = ciId + eventName;

        eventMutator.addInsertion(
                key.getBytes(),
                SchemaBuilder.OPS_EVENTS_CF,
                createDataColumn(timestamp, payload));

        eventMutator.execute();

    }

    /**
     * Remove OpenEvent with the given ciId
     *
     * @param ciId
     * @param eventName
     */
    public void removeOpenEventForCi(long ciId, String eventName) {
        ciMutator.delete(ciId, SchemaBuilder.CI_OPEN_EVENTS_CF, eventName, stringSerializer);
        checkAndRemoveEmptyRow(ciId);
    }

    /**
     * Add OpsEvent for the given ciId
     *
     * @param ciId
     * @param eventName
     * @param eventId
     * @param ciState
     * @return
     */
    public boolean addOpenEventForCi(long ciId, String eventName, long eventId, String ciState) {
        boolean isNew = false;
        long lastOpenId = getCiOpenEventId(ciId, eventName);

        if (lastOpenId == 0) {
            List<HColumn<String, byte[]>> subCols = new ArrayList<>();

            HColumn<String, byte[]> eventIdCol = HFactory.createColumn("id", longSerializer.toBytes(eventId), stringSerializer, bytesSerializer);
            HColumn<String, byte[]> eventStateCol = HFactory.createColumn("state", ciState.getBytes(), stringSerializer, bytesSerializer);
            subCols.add(eventIdCol);
            subCols.add(eventStateCol);

            ciMutator.insert(ciId,
                    SchemaBuilder.CI_OPEN_EVENTS_CF,
                    HFactory.createSuperColumn(eventName, subCols, stringSerializer, stringSerializer, bytesSerializer));
            isNew = true;
        }
        logger.debug("there is already an open event for" + ciId + " " + eventName + " lastOpenId " + lastOpenId);

        return isNew;
    }

    /**
     * Query open events with given ciId
     *
     * @param ciId
     * @param eventName
     * @return
     */
    public long getCiOpenEventId(long ciId, String eventName) {
        SubColumnQuery<Long, String, String, byte[]> scolq = HFactory.createSubColumnQuery(
                keyspace,
                longSerializer,
                stringSerializer,
                stringSerializer,
                bytesSerializer)
                .setColumnFamily(SchemaBuilder.CI_OPEN_EVENTS_CF)
                .setKey(ciId)
                .setSuperColumn(eventName)
                .setColumn("id");

        HColumn<String, byte[]> resultCol = scolq.execute().get();
        if (resultCol != null) {
            return longSerializer.fromBytes(resultCol.getValue());
        }

        return 0;
    }

    /**
     * Remove the OpenEvent with the given ciId
     *
     * @param ciId
     */
    private void checkAndRemoveEmptyRow(long ciId) {
        CountQuery<Long, String> cq = HFactory.createCountQuery(
                keyspace,
                longSerializer,
                stringSerializer)
                .setColumnFamily(SchemaBuilder.CI_OPEN_EVENTS_CF)
                .setKey(ciId)
                .setRange(null, null, 1000);

        QueryResult<Integer> r = cq.execute();
        if (r.get() == 0) {
            //remove the whole row
            removeCi(ciId);
        }
    }

    /**
     * Remove ci
     *
     * @param ciId
     */
    public void removeCi(long ciId) {
        // Remove the whole row
        ciMutator.addDeletion(ciId, SchemaBuilder.CI_OPEN_EVENTS_CF);
        ciMutator.execute();
    }

    /**
     * Query and returns list of Open Events.
     *
     * @param ciId
     * @return
     */
    public List<CiOpenEvent> getCiOpenEvents(long ciId) {
        SuperSliceQuery<Long, String, String, byte[]> ssQuery = HFactory.createSuperSliceQuery(
                keyspace,
                longSerializer,
                stringSerializer,
                stringSerializer,
                bytesSerializer)
                .setColumnFamily(SchemaBuilder.CI_OPEN_EVENTS_CF)
                .setKey(ciId)
                .setRange(null, null, false, 1000);

        SuperSlice<String, String, byte[]> sSlice = ssQuery.execute().get();
        List<CiOpenEvent> openEvents = new ArrayList<>();

        if (sSlice != null) {
            for (HSuperColumn<String, String, byte[]> sCol : sSlice.getSuperColumns()) {
                CiOpenEvent event = parseSuperCol(sCol);
                openEvents.add(event);
            }
        }
        return openEvents;
    }

    /**
     * Query Open events util method.
     *
     * @param ciIds
     * @return
     */
    public Map<Long, List<CiOpenEvent>> getCiOpenEvents(List<Long> ciIds) {

        Map<Long, List<CiOpenEvent>> openEvents = new HashMap<>();
        MultigetSuperSliceQuery<Long, String, String, byte[]> mssQuery = HFactory.createMultigetSuperSliceQuery(
                keyspace,
                longSerializer,
                stringSerializer,
                stringSerializer,
                bytesSerializer)
                .setKeys(ciIds)
                .setColumnFamily(SchemaBuilder.CI_OPEN_EVENTS_CF)
                .setRange(null, null, false, 1000);

        SuperRows<Long, String, String, byte[]> sRows = mssQuery.execute().get();
        for (SuperRow<Long, String, String, byte[]> row : sRows) {
            List<CiOpenEvent> ciOpenEvents = new ArrayList<>();
            for (HSuperColumn<String, String, byte[]> sCol : row.getSuperSlice().getSuperColumns()) {
                CiOpenEvent event = parseSuperCol(sCol);
                ciOpenEvents.add(event);
            }
            openEvents.put(row.getKey(), ciOpenEvents);
        }
        return openEvents;
    }

    /**
     * Creates a Ci OpenEvent from the given super column.
     *
     * @param sCol super column
     * @return CiOpenEvent
     */
    private CiOpenEvent parseSuperCol(HSuperColumn<String, String, byte[]> sCol) {
        CiOpenEvent event = null;
        if (sCol != null) {
            event = new CiOpenEvent();
            event.setName(sCol.getName());

            for (HColumn<String, byte[]> col : sCol.getColumns()) {
                if (col.getName().equals("id")) {
                    event.setTimestamp(longSerializer.fromBytes(col.getValue()));
                } else if (col.getName().equals("state")) {
                    event.setState(stringSerializer.fromBytes(col.getValue()));
                }
            }
        }
        return event;
    }

    /**
     * Creates an Open OpsEvent stream by reading cassandra in batches.
     *
     * @param batchSize cassandra row count size to read
     * @return Observable stream of OpsEvent
     */
    public Observable<OpsEvent> getOpenEvents(final int batchSize) {
        logger.info("Creating open OpsEvent observable.");
        return Observable.create(sub -> {
            logger.info("Starting the OpsEvent subscription with batchSize: " + batchSize);
            try {
                RangeSuperSlicesQuery<Long, String, String, byte[]> rssQuery = HFactory.createRangeSuperSlicesQuery(
                        keyspace,
                        longSerializer,
                        stringSerializer,
                        stringSerializer,
                        bytesSerializer)
                        .setColumnFamily(SchemaBuilder.CI_OPEN_EVENTS_CF)
                        .setRange(null, null, false, batchSize)
                        .setRowCount(batchSize);

                int readEvents = 1;
                Long lastKey = null;

                while (true) {
                    // Do this only if the subscription is active.
                    if (sub.isUnsubscribed()) {
                        logger.error("Observer seems unsubscribed due to cancel/error. Exiting the loop.");
                        break;
                    }

                    rssQuery.setKeys(lastKey, null);
                    OrderedSuperRows<Long, String, String, byte[]> sRows = rssQuery.execute().get();
                    Iterator<SuperRow<Long, String, String, byte[]>> rowsIt = sRows.iterator();
                    if (lastKey != null && rowsIt != null) rowsIt.next();

                    while (rowsIt.hasNext()) {
                        SuperRow<Long, String, String, byte[]> sRow = rowsIt.next();
                        // Shift start key for the next batch.
                        lastKey = sRow.getKey();
                        for (HSuperColumn<String, String, byte[]> sCol : sRow.getSuperSlice().getSuperColumns()) {
                            if (sCol.getColumns().size() > 0) {
                                CiOpenEvent ciEvent = parseSuperCol(sCol);
                                String key = lastKey + ciEvent.getName();
                                OpsEvent event = getOpsEvent(key, ciEvent.getTimestamp());
                                if (event != null) sub.onNext(event);
                            }
                        }
                    }

                    readEvents += (sRows.getCount() - 1);
                    logger.info("Read " + readEvents + " OpsEvents.");
                    if (sRows.getCount() < batchSize) {
                        break;
                    }
                }

                logger.info(">>> Completed the OpsEvent subscription. Read " + readEvents + " OpsEvents");
                sub.onCompleted();

            } catch (Exception ex) {
                logger.error("Error reading open OpsEvents: " + ex.getMessage());
                sub.onError(ex);
            }
        });
    }

    /**
     * Query the ops events with given eventId
     *
     * @param key
     * @param eventId
     * @return
     */
    public OpsEvent getOpsEvent(String key, long eventId) {
        SliceQuery<String, Long, String> sliceQuery = HFactory.createSliceQuery(
                keyspace,
                stringSerializer,
                longSerializer,
                stringSerializer)
                .setColumnFamily(SchemaBuilder.OPS_EVENTS_CF)
                .setColumnNames(eventId)
                .setKey(key);

        ColumnSlice<Long, String> resultCols = sliceQuery.execute().get();
        if (resultCols.getColumns().size() > 0) {
            String eventJson = resultCols.getColumns().get(0).getValue();
            OpsEvent event = gson.fromJson(eventJson, OpsEvent.class);
            return event;
        }
        return null;
    }

    /**
     * Query ops event history
     *
     * @param key
     * @param startTime
     * @param endTime
     * @param count
     * @return
     */
    public List<OpsEvent> getOpsEventHistory(String key, Long startTime, Long endTime, int count) {
        List<OpsEvent> events = new ArrayList<>();

        SliceQuery<String, Long, String> sliceQuery = HFactory.createSliceQuery(
                keyspace,
                stringSerializer,
                longSerializer,
                stringSerializer)
                .setColumnFamily(SchemaBuilder.OPS_EVENTS_CF)
                .setRange(startTime, endTime, false, count)
                .setKey(key);

        ColumnSlice<Long, String> resultCols = sliceQuery.execute().get();
        for (HColumn<Long, String> col : resultCols.getColumns()) {
            OpsEvent event = gson.fromJson(col.getValue(), OpsEvent.class);
            events.add(event);
        }
        return events;
    }

    /**
     * Query ops event history util method
     *
     * @param ciId
     * @param startTime
     * @param endTime
     * @param count
     * @return
     */
    public List<OpsEvent> getOpsEventHistory(long ciId, Long startTime, Long endTime, int count) {
        List<OpsEvent> events = new ArrayList<>();

        RangeSlicesQuery<String, Long, String> query = HFactory.createRangeSlicesQuery(
                keyspace,
                stringSerializer,
                longSerializer,
                stringSerializer)
                .setColumnFamily(SchemaBuilder.OPS_EVENTS_CF)
                .setKeys(String.valueOf(ciId) + Character.MIN_VALUE, String.valueOf(ciId) + Character.MAX_VALUE)
                .setRange(startTime, endTime, false, count);

        OrderedRows<String, Long, String> rows = query.execute().get();
        for (Row<String, Long, String> row : rows) {
            ColumnSlice<Long, String> resultCols = row.getColumnSlice();
            for (HColumn<Long, String> col : resultCols.getColumns()) {
                OpsEvent event = gson.fromJson(col.getValue(), OpsEvent.class);
                events.add(event);
            }
        }
        return events;
    }

    /**
     * Sets the long timestamp, string value of a given event
     *
     * @param timestamp
     * @param value
     * @return hector data column
     */
    private HColumn<Long, String> createDataColumn(Long timestamp, String value) {
        return HFactory.createColumn(timestamp, value, longSerializer, stringSerializer);
    }

}
