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
package com.oneops.sensor.jms;

import com.google.gson.Gson;
import com.oneops.sensor.Sensor;
import com.oneops.sensor.events.BasicEvent;
import com.oneops.sensor.util.SensorHeartBeat;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The listener interface for receiving sensor events.
 * The class that is interested in processing a sensor
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addSensorListener<code> method. When
 * the sensor event occurs, that object's appropriate
 * method is invoked.
 *
 * @see SensorEvent
 */
public class SensorListener implements MessageListener {

	private static Logger logger = Logger.getLogger(SensorListener.class);

    private Sensor sensor;
    private SensorHeartBeat sensorHeartBeat;
    private Set<Long> cisToLog = ConcurrentHashMap.newKeySet();
    private Gson gson = new Gson();
	/**
	 * Sets the sensor.
	 *
	 * @param sensor the new sensor
	 */
	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	/**
	 * Sets the sensor heart beat.
	 *
	 * @param sensorHeartBeat the new sensor heart beat
	 */
	public void setSensorHeartBeat(SensorHeartBeat sensorHeartBeat) {
		this.sensorHeartBeat = sensorHeartBeat;
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	/**
	 * takes the jms message
	 */
	public void onMessage(Message msg) {
		try {
			
			if (msg instanceof ObjectMessage) {
				BasicEvent event = (BasicEvent)((ObjectMessage)msg).getObject();
				if (logger.isDebugEnabled() || cisToLog.contains(event.getCiId())) {
					logger.info(gson.toJson(event));
				}
				sensorHeartBeat.timeStampIt(event.getChannel());
				sensorHeartBeat.timeStampIt(BasicEvent.DEFAULT_CHANNEL);
				sensor.sendCEPEvent(event);
			}
			msg.acknowledge();
		} catch (JMSException e) {
			logger.info("caught Exception in onMessage",e);

		}
	}
	
	public void addCiIdToLog(long ciId) {
		cisToLog.add(ciId);
	}

	public void removeCiIdToLog(long ciId) {
		cisToLog.remove(ciId);
	}
	
}
