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
package com.oneops.opamp.ws;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oneops.opamp.exceptions.OpampException;
import com.oneops.opamp.service.BadStateProcessor;
import com.oneops.opamp.service.FlexStateProcessor;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.ops.events.OpsBaseEvent;


/**
 * testing utility class
 */
@Controller
public class OpampWsTestController {
	private static Logger logger = Logger.getLogger(OpampWsTestController.class);
	
	private BadStateProcessor bsProcessor;
	private FlexStateProcessor flexStateProcessor;
	
	/**
	 * Sets the bs processor.
	 *
	 * @param bsProcessor the new bs processor
	 */
	public void setBsProcessor(BadStateProcessor bsProcessor) {
		this.bsProcessor = bsProcessor;
	}
	
	/**
	 * Sets the flex state processor.
	 *
	 * @param flexStateProcessor the new flex state processor
	 */
	public void setFlexStateProcessor(FlexStateProcessor flexStateProcessor) {
		this.flexStateProcessor = flexStateProcessor;
	}

	/**
	 * Test proc.
	 *
	 * @param ciId the ci id
	 * @return the string
	 * @throws OpampException 
	 */
	@RequestMapping(value="/test/proc/{ciId}", method = RequestMethod.GET)
	@ResponseBody
	public String testProc(@PathVariable int ciId) throws OpampException {
		OpsBaseEvent opsEvent = new OpsBaseEvent();
		opsEvent.setCiId(ciId);
		logger.info("TESTING>>>>> ciId" + ciId);
		CiChangeStateEvent event = new CiChangeStateEvent();
        long unhealthyStartTime = System.currentTimeMillis() - 1 * 60 *60 * 1000;
        long coolOffPeriodMillis = 15 * 60 * 1000;
		bsProcessor.submitRepairProcedure(event, false, unhealthyStartTime, 1, coolOffPeriodMillis);
		return null;
	}	

	/**
	 * Test felx up.
	 *
	 * @param ciId the ci id
	 * @return the string
	 */
	@RequestMapping(value="/test/flex/up/{ciId}", method = RequestMethod.GET)
	@ResponseBody
	public String testFelxUp(@PathVariable int ciId) {
		logger.info("TESTING>>>>> flexing up ciId" + ciId);
		
		CiChangeStateEvent opsEvent = new CiChangeStateEvent();
		opsEvent.setCiId(ciId);
		try {
			flexStateProcessor.processOverutilized(opsEvent, true);
		} catch (OpampException e) {
			logger.error("OpampException in testFelxUp", e);
		}
		return "extended pool";
	}	
	
	/**
	 * Test felx down.
	 *
	 * @param ciId the ci id
	 * @return the string
	 */
	@RequestMapping(value="/test/flex/down/{ciId}", method = RequestMethod.GET)
	@ResponseBody
	public String testFelxDown(@PathVariable int ciId) {
		logger.info("TESTING>>>>> flexing down ciId" + ciId);
		CiChangeStateEvent opsEvent = new CiChangeStateEvent();
		opsEvent.setCiId(ciId);
		try {
			flexStateProcessor.processUnderutilized(opsEvent, true, System.currentTimeMillis());
		} catch (OpampException e) {
			logger.error("OpampException in testFelxDown", e);		}
		return "shrinked pool";
	}	
	
}
