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
package com.oneops.controller.plugin;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.controller.domain.WoProcessRequest;

/**
 * The Class WoProcessorExample.
 */
public class WoProcessorExample implements WoProcessor {

	final private Gson gson = new Gson();

	private static Logger logger = Logger.getLogger(WoProcessorExample.class);

	/* (non-Javadoc)
	 * @see com.oneops.controller.plugin.WoProcessor#processWo(com.oneops.controller.domain.WoProcessRequest)
	 */
	@Override
	public void processWo(WoProcessRequest wopr) {
		logger.warn(gson.toJson(wopr));
	}

}
