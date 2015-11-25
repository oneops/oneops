package com.oneops.cms.transmitter;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.jms.JMSException;

import org.apache.log4j.Logger;

import com.oneops.cms.transmitter.domain.CMSEvent;
import com.oneops.cms.transmitter.domain.PubStatus;

public class MainScheduler {

	static Logger logger = Logger.getLogger(MainScheduler.class);

	private boolean isRunning = false;
	private long lastRun;
	private ControllerEventPublisher controllerEventPublisher;
	private CIEventPublisher ciEventPublisher;
	private ControllerEventReader controllerEventReader;
	private CIEventReader ciEventReader;
	
	private final ScheduledExecutorService controllerEventScheduler = Executors.newScheduledThreadPool(1);
	private final ScheduledExecutorService ciEventScheduler = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> jobHandle = null;
	private ScheduledFuture<?> ciEventsJobHandle = null;
	
	
	public void startTheJob() {
        final Runnable controllerEventsBatchProcessor = new Runnable() {
                public void run() { publishControllerPendingEvents(); }
            };
        final Runnable ciEventsBatchProcessor = new Runnable() {
            public void run() { publishCIPendingEvents(); }
        };    
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
						controllerEventPublisher.publishMessage(event);
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
				try {
					if (event.getPayload() != null) {
						ciEventPublisher.publishMessage(event);
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
	
	public void setControllerEventPublisher(ControllerEventPublisher controllerEventPublisher) {
		this.controllerEventPublisher = controllerEventPublisher;
	}

	public void setCiEventPublisher(CIEventPublisher ciEventPublisher) {
		this.ciEventPublisher = ciEventPublisher;
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
		int backLog = controllerEventReader.getQueueBacklog();
		stat.setQueueBacklog(backLog);
		stat.setCiEventsQueueBacklog(ciEventReader.getCiEventsQueueBacklog());
		stat.setRunning(isRunning);
		Date dateLastRun = new Date(lastRun);
		stat.setLastRun(dateLastRun);
		return stat;
	}

}
