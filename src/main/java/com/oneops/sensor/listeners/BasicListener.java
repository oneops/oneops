package com.oneops.sensor.listeners;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * The listener interface for receiving basic events.
 * The class that is interested in processing a basic
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addBasicListener<code> method. When
 * the basic event occurs, that object's appropriate
 * method is invoked.
 *
 * @see com.oneops.sensor.events.BasicEvent
 */
public class BasicListener implements UpdateListener {

    private static Logger logger = Logger.getLogger(BasicListener.class);

    /**
     * uses newEvents and logs them
     */
    @SuppressWarnings("rawtypes")
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        System.out.println("in " + BasicListener.class.getSimpleName());
        System.out.println("size=" + newEvents.length);
        for (EventBean eBean : newEvents) {
            Map event = (Map) eBean.getUnderlying();
            for (Object key : event.keySet()) {
                logger.info(key + "=" + event.get(key));
            }
        }
    }

}
