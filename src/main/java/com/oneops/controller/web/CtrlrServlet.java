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
package com.oneops.controller.web;

import java.util.List;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oneops.controller.domain.WoProcessResponse;
import com.oneops.controller.workflow.WoDispatcher;

/**
 * The Class CtrlrServlet.
 */
@Controller
public class CtrlrServlet {

	protected final Log logger = LogFactory.getLog(getClass());

	private WoDispatcher woDispatcher;
	private RuntimeService runtimeService;
	
	/**
	 * Sets the runtime service.
	 *
	 * @param runtimeService the new runtime service
	 */
	public void setRuntimeService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}
	
	/**
	 * Sets the wo dispatcher.
	 *
	 * @param woDispatcher the new wo dispatcher
	 */
	public void setWoDispatcher(WoDispatcher woDispatcher) {
		this.woDispatcher = woDispatcher;
	}
	
	/**
	 * Inits the.
	 */
	public void init() {
		logger.info("Started the CONTROLLER!!!");
	}

	/**
	 * Creates the ci relation.
	 *
	 * @param woResult the wo result
	 * @return the map
	 */
	@RequestMapping(method=RequestMethod.POST, value="/wo/result")
	@ResponseBody
	public Map<String, String> createCIRelation(
			@RequestBody WoProcessResponse woResult)
    {
		woDispatcher.processWOResult(woResult);
		return null;
	}

	/**
	 * Test.
	 *
	 * @return the string
	 */
	@RequestMapping(method=RequestMethod.GET, value="/resumeall")
	@ResponseBody
	public String test()
    {
		logger.info("Resuming crashed processes!!!");
		List<ProcessInstance> processes = runtimeService.createProcessInstanceQuery().active().list();
		
		for (ProcessInstance process : processes) {
			ExecutionEntity exec = (ExecutionEntity)process;
			if (exec.isActive() && exec.getActivityId().equals("ackStart")) {
				exec.getId();
				runtimeService.signal(exec.getId());
			}
		}
		return "Resumed all processes";
	}
	
}
