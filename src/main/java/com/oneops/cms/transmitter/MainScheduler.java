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
package com.oneops.cms.transmitter;

import com.oneops.cms.transmitter.domain.CMSEvent;
import com.oneops.cms.transmitter.domain.PubStatus;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MainScheduler {

	private static Logger logger = Logger.getLogger(MainScheduler.class);

	private boolean isRunning = false;
	private long lastRun;
	private EventPublisher eventPublisher;
	private ControllerEventReader controllerEventReader;
	private CIEventReader ciEventReader;
	
	private final ScheduledExecutorService controllerEventScheduler = Executors.newScheduledThreadPool(1);
	private final ScheduledExecutorService ciEventScheduler = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> jobHandle = null;
	private ScheduledFuture<?> ciEventsJobHandle = null;
	
	
	public void startTheJob() {
        final Runnable controllerEventsBatchProcessor = this::publishControllerPendingEvents;
        final Runnable ciEventsBatchProcessor = this::publishCIPendingEvents;    
        jobHandle = controllerEventScheduler.scheduleWithFixedDelay(controllerEventsBatchProcessor, 0, 3, SECONDS);
        ciEventsJobHandle = ciEventScheduler.scheduleWithFixedDelay(ciEventsBatchProcessor, 0, 3, SECONDS);
    }
	
	private void publishControllerPendingEvents() {
		//logger.info("start reading" + eventType);
		isRunning = true;
		lastRun = System.currentTimeMillis();
		List<CMSEvent> events = controllerEventReader.getEvents();
		while (events.size()>0) {
			logger.info("Got " + events.size() + " controller events; Using ControllerEventPublisher");
			for (CMSEvent event : events) {
				try {
					if (event.getPayload() != null) {
						eventPublisher.publishControllerEvents(event);
					} else {
						logger.info("Event payload found null for " + event.getHeaders());
					}
					controllerEventReader.removeEvent(event.getEventId());
				} catch (JMSException e) {
					e.printStackTrace();
					logger.error(e.getMessage(),e);
					//stopPublishing();
					return;
				}
			}
			events = controllerEventReader.getEvents();
		}
		//System.out.println("Done;");
	}
	
	private void publishCIPendingEvents() {
		//logger.info("start reading" + eventType);
		isRunning = true;
		lastRun = System.currentTimeMillis();
		List<CMSEvent> events = ciEventReader.getEvents();
		while (events.size()>0) {
			logger.info("Got " + events.size() + " ci events; Using CIEventPublisher");
			for (CMSEvent event : events) {
				String action = event.getHeaders().get("action");
				try {
					if (event.getPayload() != null || "delete".equals(action)) {
						eventPublisher.publishCIEvents(event);
					} else {
						logger.info("Event payload found null for " + event.getHeaders());
					}
					ciEventReader.removeEvent(event.getEventId());
				} catch (JMSException e) {
					e.printStackTrace();
					logger.error(e.getMessage(),e);
					//stopPublishing();
					return;
				}
			}
			events = ciEventReader.getEvents();
		}
		//System.out.println("Done;");
	}
	
	public void stopPublishing() {
		jobHandle.cancel(true);
		ciEventsJobHandle.cancel(true);
		isRunning = false;
	}

	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	public void setControllerEventReader(ControllerEventReader controllerEventReader) {
		this.controllerEventReader = controllerEventReader;
	}

	public void setCiEventReader(CIEventReader ciEventReader) {
		this.ciEventReader = ciEventReader;
	}

	
	public void cleanup() {
		logger.info("canceling the jobs");
		jobHandle.cancel(true);
		ciEventsJobHandle.cancel(true);
		logger.info("shuting down the scheduler");
		ciEventScheduler.shutdown();
		controllerEventScheduler.shutdown();
	}
	
	public PubStatus getStatus() {
		PubStatus stat = new PubStatus();
		stat.setQueueBacklog(controllerEventReader.getQueueBacklog());
		stat.setCiEventsQueueBacklog(ciEventReader.getQueueBacklog());
		stat.setRunning(isRunning);
		Date dateLastRun = new Date(lastRun);
		stat.setLastRun(dateLastRun);
		return stat;
	}

}
