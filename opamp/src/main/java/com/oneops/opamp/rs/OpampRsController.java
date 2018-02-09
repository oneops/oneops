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

package com.oneops.opamp.rs;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.oneops.opamp.service.BadStateProcessor;

/**
 * @author dsing17
 *
 */
@RestController

public class OpampRsController {

	private static Logger logger = Logger.getLogger(OpampRsController.class);
	BadStateProcessor bsProcessor;

	@RequestMapping(value = "/replace/cid/{ciId}", method = RequestMethod.PUT)
	@ResponseBody

	public Map<String, Integer> replaceByCid(@PathVariable long ciId,
			@RequestHeader(value = "userId", required = true) String userId,
			@RequestHeader(value = "description", required = false, defaultValue = "auto-replace by opamp service call") String description) {

		logger.info("Starting to replace ciId : " + ciId + ", requested by user " + userId + " , description: "
				+ description);
		Map<String, Integer> result = new HashMap<>(1);
		try {
			result = bsProcessor.replaceByCid(ciId, userId, description);
			return result;

		} catch (Exception e) {

			logger.error("Exception while processing replaceByCid - API for Cid: {}" + ciId + " :" + e);
			result.put("deploymentId", 1);
			return result;
		}

	}

	public BadStateProcessor getBsProcessor() {
		return bsProcessor;
	}

	public void setBsProcessor(BadStateProcessor bsProcessor) {
		this.bsProcessor = bsProcessor;
	}
}