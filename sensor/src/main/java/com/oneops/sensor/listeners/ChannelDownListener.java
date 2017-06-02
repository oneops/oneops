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
