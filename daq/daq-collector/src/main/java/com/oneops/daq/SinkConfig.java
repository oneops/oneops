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

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
* Holds the config values that 
* are common to the Sinks
 */
public class SinkConfig {
	private String hostAndPort ;
	private String clusterName ;
	private String keyspaceName ;
	private static Logger logger = Logger.getLogger(SinkConfig.class);

	
	
	/**
	 * Instantiates a new sink config.
	 */
	public SinkConfig() {
		super();
	}

	/**
	 * Instantiates a new sink config.
	 *
	 * @param hostAndPort the host and port
	 * @param clusterName the cluster name
	 * @param keyspaceName the keyspace name
	 */
	public SinkConfig(String hostAndPort, String clusterName,
			String keyspaceName) {
		super();
		this.hostAndPort = hostAndPort;
		this.clusterName = clusterName;
		this.keyspaceName = keyspaceName;
	}
	
	/**
	 * gets the SinkConfig using the properties
	 * @return the SinkConfig or null if properties not available
	 * caller should check for null
	 */
	public SinkConfig buildFromProperties(){
		
		// TODO: fix to use spring injection, might not be possible because of flume framework
		// workaround : get runtime args from flume node cmdline
		Properties properties = new Properties();				
		try {
			properties.load(this.getClass().getResourceAsStream ("/sink.properties"));
		} catch (IOException e) {
			logger.error("got: "+e.getMessage());
			return null;
		}
		SinkConfig config =  new SinkConfig(properties.getProperty("cluster_host_port"),
				properties.getProperty("cluster_name"),
				properties.getProperty("keyspace_name"));
		
		logger.warn("SinkBuilder "+config);
		return config;
						
	}
	
	/**
	 * Gets the host and port.
	 *
	 * @return the host and port
	 */
	public String getHostAndPort() {
		return hostAndPort;
	}
	
	/**
	 * Sets the host and port.
	 *
	 * @param hostAndPort the new host and port
	 */
	public void setHostAndPort(String hostAndPort) {
		this.hostAndPort = hostAndPort;
	}
	
	/**
	 * Gets the cluster name.
	 *
	 * @return the cluster name
	 */
	public String getClusterName() {
		return clusterName;
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
	 * Gets the keyspace name.
	 *
	 * @return the keyspace name
	 */
	public String getKeyspaceName() {
		return keyspaceName;
	}
	
	/**
	 * Sets the keyspace name.
	 *
	 * @param keyspaceName the new keyspace name
	 */
	public void setKeyspaceName(String keyspaceName) {
		this.keyspaceName = keyspaceName;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SinkConfig [hostAndPort=" + hostAndPort + ", clusterName="
				+ clusterName + ", keyspaceName=" + keyspaceName + "]";
	}


}
