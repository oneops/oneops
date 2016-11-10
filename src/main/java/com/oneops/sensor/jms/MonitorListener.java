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

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.ops.dao.PerfHeaderDao;
import com.oneops.sensor.Sensor;
import com.oneops.sensor.client.MonitorPublisher;
import com.oneops.sensor.client.MonitorRequest;
import com.oneops.sensor.exceptions.SensorException;
import com.oneops.sensor.thresholds.ThresholdsDao;
import com.oneops.sensor.util.ReplacedInstances;

public class MonitorListener implements MessageListener {

	private static Logger logger = Logger.getLogger(MonitorListener.class);
	private Sensor sensor;
    private Gson gson = new Gson();
    private PerfHeaderDao phDao;
    private ThresholdsDao tsDao;
    private ReplacedInstances replacedInstances;

    /**
	 * Sets the ph dao.
	 *
	 * @param phDao the new ph dao
	 */
	public void setPhDao(PerfHeaderDao phDao) {
		this.phDao = phDao;
	}	
	
	/**
	 * Sets the ts dao.
	 *
	 * @param tsDao the new ts dao
	 */
	public void setTsDao(ThresholdsDao tsDao) {
		this.tsDao = tsDao;
	}
	
    /**
	 * Sets thesensor.
	 *
	 * @param Sensor sensor
	 */

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}


	@Override
	public void onMessage(Message msg) {
		try {
			if (msg instanceof TextMessage) {
				if (logger.isDebugEnabled()) {
					logger.debug(gson.toJson(msg));
				}
				
				MonitorRequest mr = gson.fromJson(((TextMessage)msg).getText(), MonitorRequest.class);
				
				long ciId = mr.getCiId();
		    	long manifestId = mr.getManifestId();
		    	
				if (MonitorPublisher.MONITOR_ACTION_UPDATE.equals(mr.getAction())) {
					replacedInstances.remove(ciId);
					if (tsDao.getManifestId(ciId) == null) {
						//add manifest mapping regardless of the monitors
						tsDao.addManifestMap(ciId, manifestId);
					}
					if (mr.isMonitoringEnabled()) {
	
						String baseKey  = new Long(ciId).toString() +":";
						for (CmsRfcCISimple monitor : mr.getMonitors()) {
							phDao.createHeader(baseKey+monitor.getCiName(), monitor);
						}
						sensor.addCiThresholdsList(ciId, manifestId, mr.getMonitors());
					}

				} else if (MonitorPublisher.MONITOR_ACTION_DELETE.equals(mr.getAction())) {
					sensor.removeCi(ciId, manifestId);
				} else if (MonitorPublisher.MONITOR_ACTION_REPLACE_INSTANCE.equals(mr.getAction())) {
					replacedInstances.add(ciId);
					sensor.handleReplace(ciId, manifestId);
				} else {
					logger.error("Unknown action on msg:" + gson.toJson(msg));
				}
			}
			msg.acknowledge();
		} catch (JMSException e) {
			e.printStackTrace();
			logger.error("caught Exception in onMessage",e);
			throw new RuntimeException(e);
		} catch (SensorException e) {
			e.printStackTrace();
			logger.error("caught Exception in onMessage",e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Unable to persist thresholds/headers for " + gson.toJson(msg),e);
			throw new RuntimeException(e);
		}	
	}

	public void setReplacedInstances(ReplacedInstances replacedInstances) {
		this.replacedInstances = replacedInstances;
	}

}
