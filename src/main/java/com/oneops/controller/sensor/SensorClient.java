/*******************************************************************************
 *
 * Copyright 2015 Walmart, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.oneops.controller.sensor;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.sensor.client.MonitorPublisher;
import com.oneops.sensor.client.SensorClientException;

/**
 * The Class SensorClient.
 */
public class SensorClient {
	//private static final String WATCHED_BY = "WatchedBy";

	private static Logger logger = Logger.getLogger(SensorClient.class);
	
	//private RestTemplate restTemplate;
    //private String serviceUrl; 
    //private String cmsServiceUrl; 
    private MonitorPublisher monPublisher;
    final private Gson gson = new Gson();
    //private static final String FAILED = "failed";

	/**
	 * Sets the monitorPublisher.
	 *
	 * @param MonitorPublisher monitorPublisher
	 */
	public void setMonPublisher(MonitorPublisher monPublisher) {
		this.monPublisher = monPublisher;
	}


	/**
	 * Process monitors.
	 *
	 * @param exec the exec
	 * @param wo the wo
	 */
	/*
	public void processMonitors(DelegateExecution exec, CmsWorkOrderSimple wo) {
    	try {
    		String skipSensor = System.getenv("SKIP_SENSOR");
    		if (skipSensor == null || skipSensor.equalsIgnoreCase("no")) {
    			if (wo.getPayLoad().get("Environment") != null 
    					&& wo.getPayLoad().get("Environment").get(0).getCiAttributes().get("monitoring") != null
    					&& wo.getPayLoad().get("Environment").get(0).getCiAttributes().get("monitoring").equals("true")) {
    				
    				monPublisher.processMonitorWo(wo);
				    logger.debug("Client: sesnor post: " + gson.toJson(wo));
				    
    			}
    		}
	        exec.createVariableLocal("wostate", "sensor-complete");
    	} catch (SensorClientException rce) {
    		logger.error(rce.getMessage());
    		exec.createVariableLocal("wostate", "failed");
    		CmsDeployment dpmt = (CmsDeployment)exec.getVariable("dpmt");
    		dpmt.setDeploymentState(FAILED);
    		exec.setVariable("dpmt", dpmt);
    		setWoFailed(wo);
    	}
    }
    */

	/**
	 * Process monitors.
	 *
	 * @param wo the wo
	 * @throws SensorClientException 
	 */
	public void processMonitors(CmsWorkOrderSimple wo) throws SensorClientException {
		String skipSensor = System.getenv("SKIP_SENSOR");
		if (skipSensor == null || skipSensor.equalsIgnoreCase("no")) {
			if (wo.getPayLoad().get("Environment") != null 
					&& wo.getPayLoad().get("Environment").get(0).getCiAttributes().get("monitoring") != null
					&& wo.getPayLoad().get("Environment").get(0).getCiAttributes().get("monitoring").equals("true")) {
				
				monPublisher.processMonitorWo(wo);

				if(logger.isDebugEnabled())logger.debug("Client: sensor post: " + gson.toJson(wo));
			    
			}
		}
    }
	
	
	/*
	public void processMonitorsSync(DelegateExecution exec, CmsWorkOrderSimple wo) {
    	try {
    		String skipSensor = System.getenv("SKIP_SENSOR");
    		if (skipSensor == null || skipSensor.equalsIgnoreCase("no")) {
    			//Environment
    			if (wo.getPayLoad().get("Environment") != null 
    					&& wo.getPayLoad().get("Environment").get(0).getCiAttributes().get("monitoring") != null
    					&& wo.getPayLoad().get("Environment").get(0).getCiAttributes().get("monitoring").equals("true")) {
		    		if (wo.getPayLoad().get(WATCHED_BY) != null && wo.getPayLoad().get(WATCHED_BY).size()>0) {
			    		restTemplate.postForLocation(serviceUrl, wo);
				        logger.debug("Client: sesnor post: " + gson.toJson(wo));
		    		}
    			}
    		}
	        exec.createVariableLocal("wostate", "sensor-complete");
    	} catch (RestClientException rce) {
    		logger.error(rce.getMessage());
    		exec.createVariableLocal("wostate", "failed");
    		CmsDeployment dpmt = (CmsDeployment)exec.getVariable("dpmt");
    		dpmt.setDeploymentState(FAILED);
    		exec.setVariable("dpmt", dpmt);
    		setWoFailed(wo);
    	}
    }

	*/

	/*
    public void setWoFailed(CmsWorkOrderSimple wo) {

		wo.setDpmtRecordState(FAILED);
		wo.setComments("Sensor call failed!");

    	CmsDpmtRecord dpmtRec = new CmsDpmtRecord();
        dpmtRec.setDpmtRecordId(wo.getDpmtRecordId());
        dpmtRec.setDeploymentId(wo.getDeploymentId());
        dpmtRec.setDpmtRecordState(FAILED);
		dpmtRec.setComments("Error processing monitors. Sensor call failed");

        restTemplate.put(cmsServiceUrl + "dj/simple/deployments/{deploymentId}/records", dpmtRec, dpmtRec.getDeploymentId());
        logger.info("Client: put:" +  "update record id " + wo.getDpmtRecordId() + " to state " + FAILED);
    }    
    */
   
    
    /**
     * Start tracking.
     *
     * @param exec the exec
     * @param wo the wo
     */
    /*
    public void startTracking(DelegateExecution exec, CmsWorkOrderSimple wo) {
    	try {
    		String skipSensor = System.getenv("SKIP_SENSOR");
    		if (skipSensor == null || skipSensor.equalsIgnoreCase("no")) {
	    		if (wo.getRfcCi().getRfcAction().equals("add") && wo.getPayLoad().get(WATCHED_BY) != null && wo.getPayLoad().get(WATCHED_BY).size()>0) {
		    		restTemplate.postForLocation(serviceUrl+"/start", wo);
			        logger.info("Client: sesnor post: " + gson.toJson(wo));
	    		}
    		}
	        //exec.createVariableLocal("wostate", "sensor-complete");
    	} catch (RestClientException rce) {
    		logger.error(rce.getMessage());
    		wo.setComments("Sensor call failed!");
    		exec.createVariableLocal("wostate", "failed");
    	}
    }
    */
}
