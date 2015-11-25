package com.oneops.sensor.listeners;

import org.apache.log4j.Logger;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.google.gson.Gson;
import com.oneops.ops.CiOpsProcessor;
import com.oneops.ops.dao.OpsEventDao;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.ops.events.OpsCloseEvent;
import com.oneops.sensor.jms.OpsEventPublisher;
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
    private OpsEventPublisher opsEventPub;

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
            String oldCiState = coProcessor.getCIstate(event.getCiId());
            event.setTimestamp(System.currentTimeMillis());
            String payload = gson.toJson(EventConverter.convert(event));
            logger.debug(payload);
            opsEventDao.persistOpsEvent(event.getCiId(), event.getName(), event.getTimestamp(), payload);
            long lastOpenId = opsEventDao.getCiOpenEventId(event.getCiId(), event.getName());
            boolean publishedMessage = false;
            if (lastOpenId > 0) {
                opsEventDao.removeOpenEventForCi(event.getCiId(), event.getName());
                String newCiState = coProcessor.getCIstate(event.getCiId());
                CiChangeStateEvent ciEvent = new CiChangeStateEvent();
                ciEvent.setCiId(event.getCiId());
                ciEvent.setNewState(newCiState);
                ciEvent.setOldState(oldCiState);
                ciEvent.setPayLoad(payload);
                if (!newCiState.equals(oldCiState)) {
                    coProcessor.persistCiStateChange(event.getCiId(), event.getManifestId(), ciEvent, event.getTimestamp());
                }
                opsEventPub.publishCiStateMessage(ciEvent);
                publishedMessage = true;
            }
            logger.info("close event  for " + event.getCiId() + " :" + event.getName() + " :lastOpenId: " + lastOpenId + " :publishedMessage: " + publishedMessage);
        }
    }

}
