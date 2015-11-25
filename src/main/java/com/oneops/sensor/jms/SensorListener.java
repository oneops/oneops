package com.oneops.sensor.jms;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.sensor.Sensor;
import com.oneops.sensor.events.BasicEvent;
import com.oneops.sensor.util.SensorHeartBeat;

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
    private Set<Long> cisToLog = Collections.synchronizedSet(new HashSet<Long>()); 
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
