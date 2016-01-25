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

import com.oneops.cassandra.ClusterBootstrap;
import com.oneops.daq.jms.SensorPublisher;
import com.oneops.ops.dao.PerfDataAccessor;
import com.oneops.sensor.events.PerfEvent;
import com.oneops.sensor.thresholds.ThresholdsDao;
import org.apache.log4j.Logger;

import java.util.concurrent.*;

public class PerfEventProcessor {
	PerfWriter perfDao;
	private static Logger logger = Logger.getLogger(PerfEventProcessor.class);
	private String hostAndPort;
	private String clusterName;
	private String keyspaceName;
	private String sensorClusterName;
	private String sensorKsName;
	private String sensorHostAndPort;
	private static int queueSize = Integer.parseInt(System.getProperty("queuesize", "5000")); 
	private static int threadPoolSize = Integer.parseInt(System.getProperty("threads", "40")); 
	private static long threadKeepAliveSec = Long.parseLong(System.getProperty("thread_keepalive_sec", "600")); 
	private static String stateFile = System.getProperty("state_file", "/opt/oneops/log/lsperfcollector.state"); 	
	private BlockingQueue<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<Runnable>(queueSize);
	
	private	ExecutorService executor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 
			threadKeepAliveSec, TimeUnit.SECONDS, linkedBlockingDeque,
		    new ThreadPoolExecutor.CallerRunsPolicy());	
	
	//constructor to create perfdao out of arguments
	public PerfEventProcessor(String hostAndPort, 
			String clusterName, 
			String keyspaceName, 
			String sensorClusterName, 
			String sensorKsName,
			String sensorHostAndPort) {
		
		System.setProperty("sun.net.spi.nameservice.provider.1" , "dns,sun");
		System.setProperty("sun.net.spi.nameservice.provider.2" , "default");
				
		this.hostAndPort = hostAndPort;
		this.clusterName = clusterName;
		this.keyspaceName = keyspaceName;
		this.sensorClusterName = sensorClusterName;
		this.sensorKsName = sensorKsName;
		this.sensorHostAndPort = sensorHostAndPort;	

		logger.info("PerfEventProcessor ( hostAndPort:"+hostAndPort+
				" clusterName:"+ clusterName +" keyspaceName:"+keyspaceName+ 
				" sensorHostAndPort: "+sensorHostAndPort+")");

		ClusterBootstrap daqCluster = new ClusterBootstrap();
		daqCluster.setHostPort(this.hostAndPort);						
		ClusterBootstrap opsdbCluster = new ClusterBootstrap();
		opsdbCluster.setHostPort(this.sensorHostAndPort);
		
		
		ThresholdsDao thrDao = new ThresholdsDao();
		thrDao.setClusterBootstrap(opsdbCluster);
		thrDao.setClusterName(this.sensorClusterName);
		thrDao.setKeyspaceName(this.sensorKsName);
		try {
			thrDao.init();
		} catch (Exception e){
			e.printStackTrace();
			logger.error("Could not init thrDAO, EXITING VM", e);
			System.exit(1);
		}

		SensorPublisher sensorPub = new SensorPublisher();
		sensorPub.setThresholdDao(thrDao);
		try {
			sensorPub.init();
		} catch (Exception e) {
			// log and ignore
			e.printStackTrace();
			logger.error("Could not init SensorPublisher", e);
			System.exit(1);
		}				
		
		PerfDataAccessor perfDataAccessor = new PerfDataAccessor();
		perfDataAccessor.setClusterBootstrap(daqCluster);
		perfDataAccessor.setClusterName(this.clusterName);
		perfDataAccessor.setKeyspaceName(this.keyspaceName);
		try{
			perfDataAccessor.init();
		}catch (Exception e){
			e.printStackTrace();
			logger.error("Could not init perfDataAccessor, EXITING VM", e);
			System.exit(1);
		}


		perfDao = new PerfWriter();
		perfDao.setSensorPublisher(sensorPub);
		perfDao.setPerfDataAccessor(perfDataAccessor);
		perfDao.setStateFilename(stateFile);
	}

	public void process(String event, String ip) {
		PerfEvent perfEvent = Util.parsePerfEvent(event);
		process(perfEvent, ip);
	}
	
	private void process(PerfEvent perfEvent, String ip) {
		AsyncPerfWorker worker = new AsyncPerfWorker(perfDao, perfEvent, ip, logger);
		FutureTask<Void> task = new FutureTask<>(worker);
		executor.execute(task);
	}
	
	
	public static void main1(String[] a) {
		new PerfEventProcessor("daq:9160", "PerfAndLogCluster", "mdb", "sensor_ksp", "sensor_ksp","opsdb:9160")
		.process("1425524559	03-05-2015 12:48:00	401114:401114-zk-compute-load	"
		+ "load1=0.000;10.000;10.000;0; load5=0.000;8.000;9.000;0; load15=0.222;8.000;9.000;0;	zk-compute-load", "10.242.181.159");
	}
}
