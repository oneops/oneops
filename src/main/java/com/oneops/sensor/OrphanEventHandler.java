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
package com.oneops.sensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.ops.OrphanCloseEvent;
import com.oneops.ops.dao.OpsEventDao;
import com.oneops.ops.events.OpsEvent;

public class OrphanEventHandler {
	
	private Logger logger = Logger.getLogger(OrphanEventHandler.class);

	private Sensor sensor;
	private OpsEventDao opsEventDao;

	private final ScheduledExecutorService orphanEventScheduler = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> jobHandle;
	private int initialDelay = 120;
	private int delay = 60;
	
	private Gson gson = new Gson();
	
	private boolean orphanHandlerEnabled = false;
	
	public void start() {
		//TODO: delete all data for this instance from orphan_close_events CF once while starting?
		if (orphanHandlerEnabled) {
			jobHandle = orphanEventScheduler.scheduleWithFixedDelay(new EventResenderTask(), initialDelay, delay, TimeUnit.MINUTES);	
		}
	}
	
	class EventResenderTask implements Runnable {

		@Override
		public void run() {
			try {
				List<OrphanCloseEvent> orphans = opsEventDao.getAllOrphanCloseEvents(100);
				Map<Long, List<OrphanCloseEvent>> eventsToDeleteMap = new HashMap<Long, List<OrphanCloseEvent>>();
				for (OrphanCloseEvent orphanEvent : orphans) {
					if (orphanEvent.getManifestId() == 0) {
						logger.info("manifestId is empty for the ci " + orphanEvent.getCiId() + ", removing the orphan close event.");
						addToRemoveMap(eventsToDeleteMap, orphanEvent);
					}
					else {
						if (sensor.isManagedByThisInstance(orphanEvent.getManifestId())) {
							//check if there is an open event for this ci, if it exists then send the open event to Esper
							long openEventId = opsEventDao.getCiOpenEventId(orphanEvent.getCiId(), orphanEvent.getName());
							if (openEventId > 0) {
								OpsEvent event = gson.fromJson(orphanEvent.getOpenEventPayload(), OpsEvent.class);
								sensor.sendOpsEvent(event);
							}
							else {
								logger.info("there is no open event found for the ci " + orphanEvent.getCiId() + ", removing the orphan close event.");
							}	
							addToRemoveMap(eventsToDeleteMap, orphanEvent);
						}
					}
				}
				opsEventDao.removeOrphanCloseEvents(eventsToDeleteMap);
				logger.info("number of orphan close events handled : " + orphans.size());
			} catch(Exception e) {
				logger.error("Exception while hanlding orphan close events", e);
			}		
		}
		
		private void addToRemoveMap(Map<Long, List<OrphanCloseEvent>> eventsToDeleteMap, OrphanCloseEvent orphanEvent) {
			List<OrphanCloseEvent> list = eventsToDeleteMap.get(orphanEvent.getCiId());
			if (list == null ) {
				list = new ArrayList<OrphanCloseEvent>();
				eventsToDeleteMap.put(orphanEvent.getCiId(), list);
			}
			list.add(orphanEvent);
		}
				
	}
	
	public void stop() {
		if (orphanHandlerEnabled) {
			jobHandle.cancel(true);			
		}
	}
	
	public void cleanup() {
		stop();
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public void setOpsEventDao(OpsEventDao opsEventDao) {
		this.opsEventDao = opsEventDao;
	}

	public void setInitialDelay(int initialDelay) {
		this.initialDelay = initialDelay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public void setOrphanHandlerEnabled(boolean orphanHandlerEnabled) {
		this.orphanHandlerEnabled = orphanHandlerEnabled;
	}
	
}
