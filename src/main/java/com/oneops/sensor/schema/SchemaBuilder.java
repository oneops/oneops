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
package com.oneops.sensor.schema;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.log4j.Logger;

public class SchemaBuilder {
	
	public static final String THRESHOLDS_CF = "thresholds_scf";
	public static final String MANIFESTMAP_CF = "manifestmap";
	public static final String REALIZED_AS_CF = "realized_as_map";
	public static final String OPS_EVENTS_CF = "ops_events";
	public static final String CI_OPEN_EVENTS_CF = "ci_open_events";
	public static final String CI_STATE_HIST_CF = "ci_state_hist";
	public static final String COMPONENT_STATE_CF = "component_states";

	
	private static Logger logger = Logger.getLogger(SchemaBuilder.class);
	
	public static void createSchema(Cluster cluster, String keyspaceName) {
		
		KeyspaceDefinition keyspaceDef = cluster.describeKeyspace(keyspaceName);
		
		if (keyspaceDef == null) {
			createFreshSchema(cluster, keyspaceName);
			return;
		}
		
		List<ColumnFamilyDefinition> existingCFs = keyspaceDef.getCfDefs();
		
		Set<String> existingCFNames = new HashSet<String>();
		for (ColumnFamilyDefinition existingCF : existingCFs) {
			existingCFNames.add(existingCF.getName());
		}
		
		if (!existingCFNames.contains(THRESHOLDS_CF)) {
			ColumnFamilyDefinition cfThresholdsDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
					THRESHOLDS_CF, 
	                ComparatorType.BYTESTYPE);
			cfThresholdsDef.setColumnType(ColumnType.SUPER);
			cfThresholdsDef.setSubComparatorType(ComparatorType.BYTESTYPE);
			cluster.addColumnFamily(cfThresholdsDef,true);
			logger.info("Added column family " + THRESHOLDS_CF);
		}

		if (!existingCFNames.contains(MANIFESTMAP_CF)) {
			ColumnFamilyDefinition cfManifestMapDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
					MANIFESTMAP_CF, 
	                ComparatorType.BYTESTYPE);
			cluster.addColumnFamily(cfManifestMapDef,true);
			logger.info("Added column family " + MANIFESTMAP_CF);
		}

		if (!existingCFNames.contains(REALIZED_AS_CF)) {
			ColumnFamilyDefinition cfRealizedAsMapDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
					REALIZED_AS_CF, 
	                ComparatorType.LONGTYPE);
			cluster.addColumnFamily(cfRealizedAsMapDef,true);
			logger.info("Added column family " + REALIZED_AS_CF);
		}
		
		
		if (!existingCFNames.contains(OPS_EVENTS_CF)) {
			ColumnFamilyDefinition cfOpsDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
					OPS_EVENTS_CF, 
	                ComparatorType.LONGTYPE);
			cluster.addColumnFamily(cfOpsDef,true);
			logger.info("Added column family " + OPS_EVENTS_CF);
		}
		
		if (!existingCFNames.contains(CI_STATE_HIST_CF)) {
			ColumnFamilyDefinition cfCiStateHistDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
					CI_STATE_HIST_CF, 
	                ComparatorType.LONGTYPE);
			cluster.addColumnFamily(cfCiStateHistDef,true);
			logger.info("Added column family " + CI_STATE_HIST_CF);
		}
		
		if (!existingCFNames.contains(CI_OPEN_EVENTS_CF)) {
			ColumnFamilyDefinition cfCiOpenEventsDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
					CI_OPEN_EVENTS_CF, 
	                ComparatorType.LONGTYPE);
			cfCiOpenEventsDef.setColumnType(ColumnType.SUPER);
			cfCiOpenEventsDef.setSubComparatorType(ComparatorType.BYTESTYPE);
			cluster.addColumnFamily(cfCiOpenEventsDef,true);
			logger.info("Added column family " + CI_OPEN_EVENTS_CF);
		}

		if (!existingCFNames.contains(COMPONENT_STATE_CF)) {
			ColumnFamilyDefinition cfComponentStateDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
					COMPONENT_STATE_CF, 
	                ComparatorType.BYTESTYPE);
			cfComponentStateDef.setColumnType(ColumnType.STANDARD);
			cfComponentStateDef.setDefaultValidationClass(ComparatorType.COUNTERTYPE.getClassName());
			cfComponentStateDef.setKeyValidationClass(ComparatorType.LONGTYPE.getClassName());
			cluster.addColumnFamily(cfComponentStateDef,true);
			logger.info("Added column family " + COMPONENT_STATE_CF);
		}

	}

	private static void createFreshSchema(Cluster cluster, String keyspaceName) {
		ColumnFamilyDefinition cfThresholdsDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
				THRESHOLDS_CF, 
                ComparatorType.BYTESTYPE);
		cfThresholdsDef.setColumnType(ColumnType.SUPER);
		cfThresholdsDef.setSubComparatorType(ComparatorType.BYTESTYPE);
		
		ColumnFamilyDefinition cfManifestMapDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
				MANIFESTMAP_CF, 
                ComparatorType.BYTESTYPE);
	
		ColumnFamilyDefinition cfRealizedAsMapDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
				REALIZED_AS_CF, 
                ComparatorType.LONGTYPE);
		
		ColumnFamilyDefinition cfOpsDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
				OPS_EVENTS_CF, 
                ComparatorType.LONGTYPE);
	
		ColumnFamilyDefinition cfCiStateHistDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
				CI_STATE_HIST_CF, 
                ComparatorType.LONGTYPE);
	
		ColumnFamilyDefinition cfCiOpenEventsDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
				CI_OPEN_EVENTS_CF, 
                ComparatorType.BYTESTYPE);
		cfCiOpenEventsDef.setColumnType(ColumnType.SUPER);
		cfCiOpenEventsDef.setSubComparatorType(ComparatorType.BYTESTYPE);
	
		ColumnFamilyDefinition cfComponentStateDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
				COMPONENT_STATE_CF, 
                ComparatorType.BYTESTYPE);
		cfComponentStateDef.setColumnType(ColumnType.STANDARD);
		cfComponentStateDef.setDefaultValidationClass(ComparatorType.COUNTERTYPE.getClassName());
		cfComponentStateDef.setKeyValidationClass(ComparatorType.LONGTYPE.getClassName());
		
		
		KeyspaceDefinition newKeyspace = HFactory.createKeyspaceDefinition(keyspaceName,                 
		              ThriftKsDef.DEF_STRATEGY_CLASS,  
		              1,
		              Arrays.asList(cfThresholdsDef,cfManifestMapDef, cfRealizedAsMapDef, cfOpsDef, cfCiStateHistDef, cfCiOpenEventsDef, cfComponentStateDef));
		//Add the schema to the cluster.
		//"true" as the second param means that Hector will block until all nodes see the change.
		cluster.addKeyspace(newKeyspace, true);
		logger.info("Added keyspace " + keyspaceName);
	}
	
	
	public static void addCiStateHistCF(Cluster cluster, String keyspaceName) {
		ColumnFamilyDefinition cfCiStateHistDef = HFactory.createColumnFamilyDefinition(keyspaceName,                              
				CI_STATE_HIST_CF, 
                ComparatorType.LONGTYPE);
		cluster.addColumnFamily(cfCiStateHistDef);
	}	
	
	
}
