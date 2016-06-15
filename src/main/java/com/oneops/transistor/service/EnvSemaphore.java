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
package com.oneops.transistor.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.exceptions.TransistorException;
import org.apache.log4j.Logger;

public class EnvSemaphore {
	
	protected static final String DEFAULT_STATE = "default";
	protected static final String LOCKED_STATE = "locked";
	protected static final String MANIFEST_LOCKED_STATE = "manifest_locked";
	protected static final String ERROR_PREFIX = "ERROR:";
	protected  static final String MANIFEST_ERROR=ERROR_PREFIX+"MANIFEST:";
	protected  static final String BOM_ERROR=ERROR_PREFIX+"BOM:";
	protected static final String SUCCESS_PREFIX = "SUCCESS:";
	protected static final String COMPILE_INTERRUPTED = "Environment compilation was interrupted, please recompile!";
	private static final Logger logger = Logger.getLogger(EnvSemaphore.class);

	private Set<Long> envUnderProcess = Collections.synchronizedSet(new HashSet<Long>());

	protected CmsCmManager cmManager;
	
	public void setCmManager(CmsCmManager cmManager) {
		this.cmManager = cmManager;
	}
	
	public void lockEnv(long envId, String lock) {
		CmsCI env = cmManager.getCiById(envId);
		if (env.getCiState().equals(LOCKED_STATE) || env.getCiState().equals(MANIFEST_LOCKED_STATE)) {
			throw new TransistorException(CmsError.TRANSISTOR_ENVIRONMENT_IN_LOCKED_STATE, "Environment is in a locked state.");
		}
		env.setCiState(lock);
		env.setComments("");
		cmManager.updateCI(env);
		logger.info("locked env id " +envId +" state:" +env.getCiState());
		envUnderProcess.add(envId);
	}
	
	public void unlockEnv(long envId, String envMsg) {
		CmsCI env = cmManager.getCiById(envId);
		env.setCiState(DEFAULT_STATE);
		env.setComments(envMsg);
		cmManager.updateCI(env);
		logger.info("unlocked env id " +envId +" state:" +env.getCiState());
		envUnderProcess.remove(envId);
	}
	
	public void cleanup() {
		for (long envId : envUnderProcess) {
			CmsCI env = cmManager.getCiById(envId);
			env.setCiState(DEFAULT_STATE);
			env.setComments(ERROR_PREFIX + COMPILE_INTERRUPTED);
			cmManager.updateCI(env);
		}
	}
	
	public void resetEnv(long envId) {
		CmsCI env = cmManager.getCiById(envId);
		env.setCiState(DEFAULT_STATE);
		env.setComments(ERROR_PREFIX + COMPILE_INTERRUPTED);
		cmManager.updateCI(env);
	}
	
}
