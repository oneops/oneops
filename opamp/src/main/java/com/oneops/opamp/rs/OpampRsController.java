package com.oneops.opamp.rs;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.oneops.opamp.service.ComputeProcessor;

@RestController

@RequestMapping(value = "/compute")
public class OpampRsController {

	private static Logger logger = Logger.getLogger(OpampRsController.class);

	ComputeProcessor computeProcessor;

	public ComputeProcessor getComputeProcessor() {
		return computeProcessor;
	}

	public void setComputeProcessor(ComputeProcessor computeProcessor) {
		this.computeProcessor = computeProcessor;
	}

	@RequestMapping(value = "/replaceCompute/{ciId}", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Integer> replaceComputeByCid(@PathVariable long ciId) {

		logger.info("Starting to replace computeID : " + ciId);
		Map<String, Integer> result = new HashMap<>(1);
		try {
			result = computeProcessor.replaceComputeByCid(ciId);
			return result;

		} catch (Exception e) {

			logger.error("Exception while processing replaceComputeByCidAPI for Cid: {}" + ciId + " :" + e);
			result.put("deploymentId", 1);
			return result;
		}

	}

}
