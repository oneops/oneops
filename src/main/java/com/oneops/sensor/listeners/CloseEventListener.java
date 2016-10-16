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
package com.oneops.sensor.listeners;

import org.apache.log4j.Logger;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.google.gson.Gson;
import com.oneops.ops.CiOpsProcessor;
import com.oneops.ops.dao.OpsEventDao;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.ops.events.OpsCloseEvent;
import com.oneops.ops.events.OpsEvent;
import com.oneops.sensor.CiStateProcessor;
import com.oneops.sensor.domain.OpenEvent;
import com.oneops.sensor.jms.OpsEventPublisher;
import com.oneops.sensor.util.EventContext;
import com.oneops.sensor.util.EventConverter;

/**
 * The listener interface for receiving closeEvent events.
 * The class that is interested in processing a closeEvent
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addCloseEventListener<code> method. When
 * the closeEvent event occurs, that object's appropriate
 * method is invoked.
 *
 * @see OpsCloseEvent
 */
public class CloseEventListener implements UpdateListener {

    private static Logger logger = Logger.getLogger(CloseEventListener.class);

    private OpsEventDao opsEventDao;
    private Gson gson = new Gson();
    private CiOpsProcessor coProcessor;
    private CiStateProcessor ciStateProcessor;
    private OpsEventPublisher opsEventPub;
    private boolean orphanEventEnabled = true;

    /**
     * Sets the ops event pub.
     *
     * @param opsEventPub the new ops event pub
     */
    public void setOpsEventPub(OpsEventPublisher opsEventPub) {
        this.opsEventPub = opsEventPub;
    }


    /**
     * Sets the co processor.
     *
     * @param coProcessor the new co processor
     */
    public void setCoProcessor(CiOpsProcessor coProcessor) {
        this.coProcessor = coProcessor;
    }

    /**
     * Sets the ops event dao.
     *
     * @param opsEventsDao the new ops event dao
     */
    public void setOpsEventDao(OpsEventDao opsEventsDao) {
        this.opsEventDao = opsEventsDao;
    }

    /**
     * Event update callback.
     *
     * @param newEvents
     * @param oldEvents
     */
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        logger.debug("in " + CloseEventListener.class.getSimpleName());
        for (EventBean eBean : newEvents) {
            OpsCloseEvent event = (OpsCloseEvent) eBean.getUnderlying();
            OpsEvent openEvent = event.getOpenEvent();
            event.setOpenEvent(null);
            event.setTimestamp(System.currentTimeMillis());
            String payload = gson.toJson(EventConverter.convert(event));
            logger.debug(payload);
            opsEventDao.persistOpsEvent(event.getCiId(), event.getName(), event.getTimestamp(), payload);
            long lastOpenId = opsEventDao.getCiOpenEventId(event.getCiId(), event.getName());
            boolean publishedMessage = false;
            if (lastOpenId > 0) {
                EventContext eventContext = new EventContext(event);
                eventContext.setPayload(payload);
                handleEvent(eventContext);
                publishedMessage = true;
            }
            else {
            	if (orphanEventEnabled) {
            		//if there was no open event to close, it could mean that the OpsEventListener is not executed yet.
                	//this may lead to an inconsistency between esper and cassandra states for this event. 
                	//so save this event as orphan so that the OrphanEventHandler will process this later
                	logger.warn("no open event found to close - ciId : " + event.getCiId() + 
                			", eventName : " + event.getName() + ", marking this as orphan close event");
                	OpenEvent opsOpenEvent = new OpenEvent();
                	opsOpenEvent.setOpsEvent(openEvent);
                	opsOpenEvent.setTimestamp(System.currentTimeMillis());
                	String openEventPayload = gson.toJson(opsOpenEvent);
                	opsEventDao.addOrphanCloseEventForCi(event.getCiId(), event.getName(), event.getManifestId(), openEventPayload);	
            	}
            }
            logger.info("close event  for " + event.getCiId() + " :" + event.getName() + " :lastOpenId: " + lastOpenId + " :publishedMessage: " + publishedMessage);
        }
    }

    private void handleEvent(EventContext eventContext) {
        OpsCloseEvent event = (OpsCloseEvent) eventContext.getEvent();
        ciStateProcessor.updateState4CloseEvent(eventContext);
        opsEventDao.removeOpenEventForCi(event.getCiId(), event.getName());
        if (logger.isDebugEnabled()) {
        	logger.debug("removed open event ci: " + event.getCiId() + " name : " + event.getName() + " state : " + event.getState());
        }
        CiChangeStateEvent ciEvent = new CiChangeStateEvent();
        ciEvent.setCiId(event.getCiId());
        ciEvent.setNewState(eventContext.getNewState());
        ciEvent.setOldState(eventContext.getOldState());
        ciEvent.setPayLoad(eventContext.getPayload());
        if (eventContext.isStateChanged()) {
            if (logger.isDebugEnabled()) {
                logger.debug("state changed ci -> " + event.getCiId() + ", old state : " + eventContext.getOldState() + ", new state : " + eventContext.getNewState());
            }
            coProcessor.persistCiStateChange(event.getCiId(), event.getManifestId(), ciEvent, event.getTimestamp(), eventContext.getStateCounterDelta());
        }
        opsEventPub.publishCiStateMessage(ciEvent);
    }   

	public void setOrphanEventEnabled(boolean orphanEventEnabled) {
		this.orphanEventEnabled = orphanEventEnabled;
	}

	public void setCiStateProcessor(CiStateProcessor ciStateProcessor) {
		this.ciStateProcessor = ciStateProcessor;
	}

}
