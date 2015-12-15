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
package com.oneops.antenna.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.NotificationSeverity;
import com.oneops.antenna.domain.NotificationType;
import com.oneops.cassandra.ClusterBootstrap;

import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class NotificationMessageDao.
 */
public class NotificationMessageDao {

    private static Logger logger = Logger.getLogger(NotificationMessageDao.class);

    private static final String NOTIFICATIONS_CF = "notifications";

    private static final String NS_MAP_CF = "ns2cmsidmap";

    private static final int MAX_ROWS_PER_SLICE = 10000;

    protected static final StringSerializer stringSerializer = StringSerializer.get();

    protected static final LongSerializer longSerializer = LongSerializer.get();

    private String clusterName;

    private String keyspaceName;

    private Keyspace keyspace;

    private Mutator<String> notificationMutator;

    private Mutator<String> nsmapMutator;

    @Autowired
    private Gson gson;

    private ClusterBootstrap cb;


    /**
     * Initialization.
     */
    public void init() {

        logger.info("Initializing NotificationMessage Dao");
        Cluster cluster = cb.getCluster(clusterName);
        KeyspaceDefinition keyspaceDef = cluster.describeKeyspace(keyspaceName);
        logger.info("Connected to cluster : " + clusterName);

        if (keyspaceDef == null) {
            createSchema(cluster);
        } else {

            List<ColumnFamilyDefinition> cfDefList = keyspaceDef.getCfDefs();

            if (!cfExists(cfDefList, NOTIFICATIONS_CF)) {
                addNotificationsCF(cluster, keyspaceName);
            }
            if (!cfExists(cfDefList, NS_MAP_CF)) {
            	addNs2cmsIdCF(cluster, keyspaceName);
            }

        }

        ConfigurableConsistencyLevel cl = new ConfigurableConsistencyLevel();
        cl.setDefaultWriteConsistencyLevel(HConsistencyLevel.ONE);
        cl.setDefaultReadConsistencyLevel(HConsistencyLevel.ONE);

        keyspace = HFactory.createKeyspace(keyspaceName, cluster, cl);
        notificationMutator = HFactory.createMutator(keyspace, stringSerializer);
        nsmapMutator = HFactory.createMutator(keyspace, stringSerializer);
    }


    /**
     * Sets the cluster name.
     *
     * @param clusterName the new cluster name
     */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }


    /**
     * Sets the keyspace name.
     *
     * @param keyspaceName the new keyspace name
     */
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
     * Checks whether the column family exists or not.
     *
     * @param cfDefList CF definition list
     * @param cfName    CF name
     * @return <code>true</code> if exists.
     */
    private boolean cfExists(List<ColumnFamilyDefinition> cfDefList, String cfName) {
        for (ColumnFamilyDefinition cfDef : cfDefList) {
            if (cfDef.getName().equals(cfName)) {
                return true;
            }
        }
        return false;
    }


    private void createSchema(Cluster cluster) {
		
		ColumnFamilyDefinition cfNotificationsDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
				NOTIFICATIONS_CF, 
                ComparatorType.LONGTYPE);

		ColumnFamilyDefinition cdNas2cmsIdMap = HFactory.createColumnFamilyDefinition(keyspaceName,                              
				NS_MAP_CF, 
                ComparatorType.UTF8TYPE);
		
		KeyspaceDefinition newKeyspace = HFactory.createKeyspaceDefinition(keyspaceName,
		              ThriftKsDef.DEF_STRATEGY_CLASS,
		              1,
		              Arrays.asList(cfNotificationsDef, cdNas2cmsIdMap));

		cluster.addKeyspace(newKeyspace, true);
	}

	/**
	 * Adds the notifications cf.
	 *
	 * @param cluster the cluster
	 * @param keyspaceName the keyspace name
	 */
	public static void addNotificationsCF(Cluster cluster, String keyspaceName) {
		ColumnFamilyDefinition cfNotificationsDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
				NOTIFICATIONS_CF, 
                ComparatorType.LONGTYPE);
		cluster.addColumnFamily(cfNotificationsDef);
	}	

	/**
	 * Adds the ns2cms id cf.
	 *
	 * @param cluster the cluster
	 * @param keyspaceName the keyspace name
	 */
	public static void addNs2cmsIdCF(Cluster cluster, String keyspaceName) {
		ColumnFamilyDefinition cdNas2cmsIdMap = HFactory.createColumnFamilyDefinition(keyspaceName,                              
				NS_MAP_CF, 
                ComparatorType.UTF8TYPE);
		cluster.addColumnFamily(cdNas2cmsIdMap);
	}	
	
	
	/**
	 * Adds the notification message.
	 *
	 * @param message the message
	 */
	public void addNotificationMessage(NotificationMessage message) {
		String key = message.getCmsId() + "::" + message.getType();
		String jsonMsg = gson.toJson(message);
		//Added more logging for trouble shooting the ts values.
		notificationMutator.addInsertion(
				key,
				NOTIFICATIONS_CF,
				createDataColumn(message.getTimestamp(), jsonMsg));
		
		notificationMutator.execute();
		logger.info("Added " + jsonMsg);

        if (!isNsMapExists(message.getNsPath(), message.getType(), message.getCmsId())) {
        	addNsMap(message);
        }
	}


	private boolean isNsMapExists(String nsPath, NotificationType msgType, long id) {
		String key = getOrgFromNsPath(nsPath);
		SliceQuery<String, String, String> sliceQuery = HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer, stringSerializer);
		sliceQuery.setColumnFamily(NS_MAP_CF);
		sliceQuery.setKey(key);
		sliceQuery.setColumnNames(nsPath+"::"+msgType + "::" + id);
		QueryResult<ColumnSlice<String, String>> result =  sliceQuery.execute();
		if (result != null) {
			ColumnSlice<String, String> resultCols = result.get();
			if (resultCols != null && resultCols.getColumns().size()>0) {
				return true;
			}
		}
		return false;
	}
	
	private void addNsMap(NotificationMessage message) {
		String key = getOrgFromNsPath(message.getNsPath());
		
		nsmapMutator.addInsertion(
				key,
				NS_MAP_CF,
				createDataColumn(message.getNsPath()+"::"+message.getType() + "::" + message.getCmsId(), message.getCmsId() + "::" + message.getType()));
        
		nsmapMutator.execute();
		logger.info("Added ns map" + message.getNsPath()+"::"+message.getType() + "::" + message.getCmsId());
	}
	
	/**
	 * Gets the notifications by range.
	 * @param id the id
	 * @param type the type
	 * @param source the source
	 * @param startTime the start time
	 * @param endTime the end time
	 * @param count the count
	 * @return the notifications by range
	 */
	public List<NotificationMessage> getNotificationsByRange(long id, NotificationType type, NotificationSeverity severity, String source, Long startTime, Long endTime, String textMatch, int count, Boolean isCaseSensitive) {

		String key = id + "::" + type.getName();

		SliceQuery<String, Long, String> sliceQuery = HFactory.createSliceQuery(keyspace, stringSerializer, longSerializer, stringSerializer);
		sliceQuery.setColumnFamily(NOTIFICATIONS_CF);
		sliceQuery.setRange(endTime * 1000, startTime * 1000,  true, count);
		sliceQuery.setKey(key);
		
		QueryResult<ColumnSlice<Long, String>> result =  sliceQuery.execute();
		return applyFilters(result.get(), severity, source, textMatch, isCaseSensitive);

	}
	
	/**
	 * Gets the last notifications.
	 *
	 * @param id the id
	 * @param type the type
	 * @param source the source
	 * @param count the count
	 * @return the last notifications
	 */
	public List<NotificationMessage> getLastNotifications(long id, NotificationType type, NotificationSeverity severity, String source, String textMatch, int count, Boolean isCaseSensitive) {

		String key = id + "::" + type.getName();

		SliceQuery<String, Long, String> sliceQuery = HFactory.createSliceQuery(keyspace, stringSerializer, longSerializer, stringSerializer);
		sliceQuery.setColumnFamily(NOTIFICATIONS_CF);
		sliceQuery.setRange(null, null, true, count);
		sliceQuery.setKey(key);
		
		QueryResult<ColumnSlice<Long, String>> result =  sliceQuery.execute();
		return applyFilters(result.get(), severity, source, textMatch, isCaseSensitive);
	}

	/**
	 * Gets the notifications for ns by range.
	 *
	 * @param nsPath the ns path
	 * @param type the type
	 * @param source the source
	 * @param startTime the start time
	 * @param endTime the end time
	 * @param count the limit of results to return
	 * @return the notifications for ns by range
	 */
	public List<NotificationMessage> getNotificationsForNsByRange(String nsPath, NotificationType type, NotificationSeverity severity, String source, Long startTime, Long endTime, String textMatch, int count, Boolean isCaseSensitive) {

		long timeBegin = System.nanoTime();
		List<NotificationMessage> ntfs = new ArrayList<NotificationMessage>();
		
		List<String> keys = getNsIds(nsPath, type);
		MultigetSliceQuery<String, Long, String> msQuery = HFactory.createMultigetSliceQuery(keyspace, stringSerializer, longSerializer, stringSerializer);
		msQuery.setColumnFamily(NOTIFICATIONS_CF);
		msQuery.setKeys(keys);
		msQuery.setRange(endTime, startTime, true, (count > 0 ? count : MAX_ROWS_PER_SLICE )); //limited by count if valid
		
		QueryResult<Rows<String,Long,String>> result =  msQuery.execute();
		
		Rows<String,Long,String> rows = result.get();
		
		long timeAfterQuery = System.nanoTime();

		List<NotificationEntry> entries = new ArrayList<NotificationEntry>();
		for (Row<String,Long,String> row : rows) {
			ColumnSlice<Long, String> resultCols = row.getColumnSlice();
			for (HColumn<Long,String> col : resultCols.getColumns()) {
				NotificationMessage ntf = gson.fromJson(col.getValue(), NotificationMessage.class);
				
				if (applyFilters(ntf, severity, source, textMatch, isCaseSensitive)) {
				    entries.add(new NotificationEntry(col.getName(), ntf));
				}
			}	
		}
		
		Collections.sort(entries);
		count = entries.size()<count ? entries.size() : count; 
		for (NotificationEntry entry : entries.subList(0, count)) {
			ntfs.add(entry.value);
		}
		
		if (logger.isDebugEnabled()) {
			long timeEnd = System.nanoTime();
			long filtering = (timeEnd - timeAfterQuery) / 1000000;
			long elapsed = (timeEnd - timeBegin)        / 1000000;
			StringBuilder sb = new StringBuilder("finished for nsPath [").append(nsPath)
			.append("] source [").append(source).append("] start [")
			.append(startTime).append("] end [").append(endTime).append("] textmatch[").append(textMatch).append("] total elapsed ms [")
			.append(String.format("%,d", elapsed)).append("] filtering-and-sorting ms [")
			.append(String.format("%,d", filtering)).append("]");
			logger.debug(sb.toString());
		}
		return ntfs;
	}
	
	
	/**
	 * Gets the last notifications for ns.
	 *
	 * @param nsPath the ns path
	 * @param type the type
	 * @param source the source
	 * @param count the count
	 * @return the last notifications for ns
	 */
	public List<NotificationMessage> getLastNotificationsForNs(String nsPath, NotificationType type, NotificationSeverity severity, String source, String textMatch, int count, Boolean isCaseSensitive) {

		List<NotificationMessage> ntfs = new ArrayList<NotificationMessage>();
		
		List<String> keys = getNsIds(nsPath, type);
		MultigetSliceQuery<String, Long, String> msQuery = HFactory.createMultigetSliceQuery(keyspace, stringSerializer, longSerializer, stringSerializer);
		msQuery.setColumnFamily(NOTIFICATIONS_CF);
		msQuery.setKeys(keys);
		msQuery.setRange(null, null, true, count);
		
		QueryResult<Rows<String,Long,String>> result =  msQuery.execute();
		Rows<String,Long,String> rows = result.get();
		List<NotificationEntry> entries = new ArrayList<NotificationEntry>();

		for (Row<String,Long,String> row : rows) {
			
			ColumnSlice<Long, String> resultCols = row.getColumnSlice();
			for (HColumn<Long,String> col : resultCols.getColumns()) {
				NotificationMessage ntf = gson.fromJson(col.getValue(), NotificationMessage.class);
				if (applyFilters(ntf, severity, source, textMatch, isCaseSensitive)) {
					entries.add(new NotificationEntry(col.getName(), ntf));
				}
			}	
		}
		
		Collections.sort(entries);
		count = entries.size()<count ? entries.size() : count; 
		for (NotificationEntry entry : entries.subList(0, count)) {
			ntfs.add(entry.value);
		}
		
		return ntfs;
	}

	private List<String> getNsIds(String nsPath, NotificationType type) {
		String key = getOrgFromNsPath(nsPath);

		List<String> ids = new ArrayList<String>();
		SliceQuery<String, String, String> sliceQuery = HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer, stringSerializer);
		sliceQuery.setColumnFamily(NS_MAP_CF);
		sliceQuery.setRange(nsPath + Character.MIN_VALUE, nsPath + Character.MAX_VALUE, false, 1000000);
		sliceQuery.setKey(key);

		QueryResult<ColumnSlice<String, String>> result =  sliceQuery.execute();
		ColumnSlice<String, String> resultCols = result.get();
		for (HColumn<String,String> col : resultCols.getColumns()) {
			String id = col.getValue();
			if (type == null) {
				ids.add(id);
			} else {
				if (type.equals(getTypeFromKey(id))) {
					ids.add(id);
				}
			}
		}	
		return ids;
	}
	
	private List<NotificationMessage> applyFilters(ColumnSlice<Long, String> notifications,  NotificationSeverity severity, String source, String textMatch, Boolean isCaseSensitive) {
		
		List<NotificationMessage> ntfs = new ArrayList<NotificationMessage>();
		// filter result in mem on the fileds that are not part of the cassandra keys 
		for (HColumn<Long,String> col : notifications.getColumns()) {
			NotificationMessage ntf = gson.fromJson(col.getValue(), NotificationMessage.class);
			boolean filtersOk = true;
			if (source != null && !source.equals(ntf.getSource())) {
				filtersOk = false;
			}
			if (severity != null && !severity.equals(ntf.getSeverity())) {
				filtersOk = false;
			}
			if (textMatch != null && ((isCaseSensitive!=null && isCaseSensitive)?!ntf.getText().contains(textMatch)
					:!(ntf.getText().toLowerCase().indexOf(textMatch.toLowerCase()) >-1))) {
				filtersOk = false;
			}
			if (filtersOk) {
				ntfs.add(ntf);
			}
		}	
		return ntfs;
	}



    
	private boolean applyFilters(NotificationMessage ntf,  NotificationSeverity severity, String source, String textMatch, Boolean isCaseSensitive) {

		boolean filtersOk = true;
		if (source != null && !source.equals(ntf.getSource())) {
			filtersOk = false;
		}
		if (severity != null && !severity.equals(ntf.getSeverity())) {
			filtersOk = false;
		}
		if (textMatch != null && ntf.getText() != null && ((isCaseSensitive!=null && isCaseSensitive)?!ntf.getText().contains(textMatch)
				:!(ntf.getText().toLowerCase().indexOf(textMatch.toLowerCase()) >-1))) {
			filtersOk = false;
		}

		return filtersOk;
	}

	
	private HColumn<Long, String> createDataColumn(long timestamp, String value) {
		return HFactory.createColumn(timestamp, value, longSerializer, stringSerializer);
	}

	private HColumn<String, String> createDataColumn(String colName, String value) {
		return HFactory.createColumn(colName, value, stringSerializer, stringSerializer);
	}

	private String getOrgFromNsPath(String nsPath) {
		String[] nsParts = nsPath.split("/");
		// the key will be org
		return nsParts[1];
	}

	private NotificationType getTypeFromKey(String key) {
		String[] parts = key.split("::");
		return NotificationType.valueOf(parts[1]);
	}
	
	private class NotificationEntry implements Comparable<NotificationEntry> {
		protected long id;
		protected NotificationMessage value;
		/**
		 * accessor method
		 * @param id
		 * @param value
		 */
		public NotificationEntry(long id, NotificationMessage value) {
			this.id = id;
			this.value = value;
		}
		
		@Override
		/**
		 * compareTo imple
		 */
		public int compareTo(NotificationEntry another) {
			return Long.signum(another.value.getTimestamp() - this.value.getTimestamp());
		}
		
	}
	
}
