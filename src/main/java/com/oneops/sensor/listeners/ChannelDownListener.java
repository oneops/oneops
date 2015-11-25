package com.oneops.sensor.listeners;

import org.apache.log4j.Logger;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.oneops.sensor.util.ChannelDownEvent;
import com.oneops.sensor.util.SensorHeartBeat;

public class ChannelDownListener implements UpdateListener {

    private static Logger logger = Logger.getLogger(ChannelDownListener.class);

    private SensorHeartBeat sensorHeartBeat;

    public void setSensorHeartBeat(SensorHeartBeat sensorHeartBeat) {
        this.sensorHeartBeat = sensorHeartBeat;
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        for (EventBean eBean : newEvents) {
            ChannelDownEvent event = (ChannelDownEvent) eBean.getUnderlying();
            logger.warn(">>>>>>>>>>>> CHANNEL " + event.getChannel() + "is down");
            sensorHeartBeat.markDown(event.getChannel());
        }
    }

}
