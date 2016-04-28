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
package com.oneops.sensor.util;

import com.oneops.cms.util.service.CmsUtilManager;
import com.oneops.sensor.OrphanEventHandler;
import com.oneops.sensor.Sensor;
import com.oneops.sensor.jms.SensorListener;
import com.oneops.sensor.jms.SensorListenerContainer;
import com.oneops.sensor.jms.SensorMonListenerContainer;
import com.oneops.util.AMQConnectorURI;
import com.oneops.util.DNSUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionException;

import javax.jms.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

public class InstanceManager {
	
	private static final String LOCK_NAME_PREFIX = "SENSOR_INSTANCE_LOCK_";
	private static final int LOCK_TIMEOUT_SEC = 30;
	private static final int LOCK_REFRESH_SEC = 10;
	private static final long LOCK_RETRY_SLEEP_MSEC = 30000;
	private static final String OPSMQ_HOST_PARAM = "com.oneops.sensor.opsmq.host";
	private static final String OPSMQ_PORT_PARAM = "com.oneops.sensor.opsmq.port";
	private static final String OPSMQ_MAX_SESSIONS = "com.oneops.sensor.opsmq.sessions";
	private static final String OPSMQ_USER = "superuser";
	private static final String OPSMQ_PASS_ENV_VAR = "KLOOPZ_AMQ_PASS";

	
	private Logger logger = Logger.getLogger(this.getClass());
	private CmsUtilManager utilManager;
	private List<SensorListenerContainer> sensorListenerContainers = new ArrayList<SensorListenerContainer>();
	private SensorMonListenerContainer monListenerContainer;
	private Sensor sensor;
	private int instanceId = 0;
	private int poolSize = 1;
	private String processId;
	private AMQConnectorURI opsMQURI;
	private SensorListener sensorListener;
	private OrphanEventHandler orphanEventHandler;
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> jobLockRefreshHandle = null;

	public void setMonListenerContainer(SensorMonListenerContainer monListenerContainer) {
		this.monListenerContainer = monListenerContainer;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public void setOpsMQURI(AMQConnectorURI opsMQURI) {
		this.opsMQURI = opsMQURI;
	}

	public void setSensorListener(SensorListener sensorListener) {
		this.sensorListener = sensorListener;
	}

	private void startTheLockRefresh() {
        final Runnable lockRefresher = new Runnable() {
                public void run() { refreshLock(); }
            };
            jobLockRefreshHandle = scheduler.scheduleWithFixedDelay(lockRefresher, 0, LOCK_REFRESH_SEC, SECONDS);
    }

	private void lockRetry() {
        final Runnable lockAqcuirer = new Runnable() {
                public void run() { 
                	while (!acquireLock()) {
                		try {
                			logger.info("Could not aquire lock, will sleep for " + LOCK_RETRY_SLEEP_MSEC + " ms and try again.");
							Thread.sleep(LOCK_RETRY_SLEEP_MSEC);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                	}
                }
            };
        Thread t = new Thread(lockAqcuirer);
        t.start();
    }
	
	public void setUtilManager(CmsUtilManager utilManager) {
		this.utilManager = utilManager;
	}

	public void init() {
		this.processId = UUID.randomUUID().toString();
		
		logger.info("Sensor started with generated processId " + this.processId);
		
		String poolSizeParam = System.getProperty("com.oneops.sensor.PoolSize");
		if (poolSizeParam == null) {
			this.poolSize = 1;
		} else {
			this.poolSize = Integer.valueOf(poolSizeParam);
		}
		logger.info("Sensor pool size is set to " + this.poolSize);
		
		if (!acquireLock()) {
			lockRetry();
		}
	}
	
	private boolean acquireLock() {
		try {
			for (int i = 1; i <= this.poolSize; i++) {
				if (utilManager.acquireLock(LOCK_NAME_PREFIX + i, this.processId, LOCK_TIMEOUT_SEC)) {
					this.instanceId = i;
					logger.info(">>>>>>>Sensor process " + this.processId + " will be running as #" + i);
					startTheLockRefresh();
					try {
						sensor.init(instanceId, poolSize);
						initJmsListeners(instanceId, poolSize);
						orphanEventHandler.start();
					} catch (Exception e) {
						cleanup();
						throw new RuntimeException(e);
					}
					//startTheLockRefresh();
					return true;
				}
			}
		} catch (TransactionException te) {
			//seems like DB is not accessible will retry
			logger.error(te);
		}

		return false;
	}

	private void initJmsListeners(int instanseId, int poolSize) {
		String host = System.getProperty(OPSMQ_HOST_PARAM, "opsmq");

		for (String opsmqHost : DNSUtil.resolve(host)) {
			// need to make initial connect assync since it will block the execution if the first broker is down
			OpsMqConnector omqConnect = new OpsMqConnector(opsmqHost, poolSize, instanseId);
            Thread t = new Thread(omqConnect);
            t.start();
		}
		monListenerContainer.initWithSelector(instanceId, poolSize);
		monListenerContainer.start();
	}

	class OpsMqConnector implements Runnable {
		
		private String host;
		private int poolSize;
		private int instanceId;
		
		public OpsMqConnector(String host, int poolSize, int instanceId) {
			this.host = host;
			this.poolSize = poolSize;
			this.instanceId = instanceId;
		}
		
        public void run() { 
        	logger.info("OPSMQ>>>>>>>>>>>>Connecting to :" + this.host);
			SensorListenerContainer listenerContainer = buildOpsMQListenerContainer(this.host);
			listenerContainer.initWithSelector(this.instanceId, this.poolSize);
			listenerContainer.start();
			sensorListenerContainers.add(listenerContainer);
        	logger.info("OPSMQ>>>>>>>>>>>Connected to :" + this.host);

        }
    };

	
	private SensorListenerContainer buildOpsMQListenerContainer(String host) {
		
		int port = Integer.valueOf(System.getProperty(OPSMQ_PORT_PARAM, "61616"));
		String opsmqPass = System.getenv(OPSMQ_PASS_ENV_VAR);
		if (opsmqPass == null) {
			throw new RuntimeException(OPSMQ_PASS_ENV_VAR + " env var needs to be set!");
		}

		ActiveMQConnectionFactory opsmqConnectionFactory = new ActiveMQConnectionFactory();
		opsmqConnectionFactory.setBrokerURL(opsMQURI.build(host, port));
		opsmqConnectionFactory.setUserName(OPSMQ_USER);
		opsmqConnectionFactory.setPassword(opsmqPass);
		
		SensorListenerContainer listenerContainer = new SensorListenerContainer();
		
		listenerContainer.setConnectionFactory(opsmqConnectionFactory);
		listenerContainer.setMaxConcurrentConsumers(Integer.valueOf(System.getProperty(OPSMQ_MAX_SESSIONS, "24")));
		listenerContainer.setConcurrentConsumers(Integer.valueOf(System.getProperty(OPSMQ_MAX_SESSIONS, "24")));
		listenerContainer.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
		listenerContainer.setMessageListener(this.sensorListener);
		
		return listenerContainer;
		
	}
	
	private void refreshLock() {
		try {
			if (!utilManager.refreshLock(LOCK_NAME_PREFIX+this.instanceId, this.processId)) {
				cleanAndReinit();
			}
		} catch (Exception e) {
			logger.error("Exception in refreshLock", e);
			cleanAndReinit();
		}
	}
	
	private void cleanAndReinit() {
		logger.error(">>>>>>>>>This Sensor instance lost the lock, will shutdown processing and re-init!");
		for (SensorListenerContainer listenerContainer : sensorListenerContainers) {
			listenerContainer.stop();
			listenerContainer.shutdown();
		}
		monListenerContainer.stop();
		monListenerContainer.shutdown();
		sensor.stop();
		jobLockRefreshHandle.cancel(true);
		init();
	}
	
	public void cleanup() {
		for (SensorListenerContainer listenerContainer : sensorListenerContainers) {
			listenerContainer.stop();
			listenerContainer.shutdown();
		}
		monListenerContainer.stop();
		monListenerContainer.shutdown();
		jobLockRefreshHandle.cancel(true);
		sensor.stop();
		utilManager.releaseLock(LOCK_NAME_PREFIX+this.instanceId, this.processId);
	}
	
	public int getInstanceId() {
		return instanceId;
	}

	public int getPoolSize() {
		return poolSize;
	}

	public String getProcessId() {
		return processId;
	}

	public void setOrphanEventHandler(OrphanEventHandler orphanEventHandler) {
		this.orphanEventHandler = orphanEventHandler;
	}

}
