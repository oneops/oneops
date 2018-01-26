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

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.service.CmsUtilProcessor;
import com.oneops.transistor.exceptions.TransistorException;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Transactional
public class EnvSemaphore {

	public static final String DEFAULT_STATE = "default";
	public static final String LOCKED_STATE = "locked";
	public static final String MANIFEST_LOCKED_STATE = "manifest_locked";
	public static final String REPLACE_STATE = "replace";
	protected static final String ERROR_PREFIX = "ERROR:";
	protected static final String MANIFEST_ERROR = ERROR_PREFIX + "MANIFEST:";
	public static final String BOM_ERROR = ERROR_PREFIX + "BOM:";
	public static final String SUCCESS_PREFIX = "SUCCESS:";
	protected static final String COMPILE_INTERRUPTED = "Environment compilation was interrupted, please recompile!";
	private static final Logger logger = Logger.getLogger(EnvSemaphore.class);
	private Set<Long> envUnderProcess = ConcurrentHashMap.newKeySet();

	protected CmsCmManager cmManager;
	private CmsUtilProcessor cmUtilProcessor;
	private int envTimeOutInSeconds;

	public void setCmManager(CmsCmManager cmManager) {
		this.cmManager = cmManager;
	}

	public void lockEnv(long envId, String lock, String processId) {
		if (cmUtilProcessor.acquireLock(getLockName(envId), processId, envTimeOutInSeconds)) {
			CmsCI env = cmManager.getCiById(envId);
			if (isEnvLocked(env)) {
				throw new TransistorException(CmsError.TRANSISTOR_ENVIRONMENT_IN_LOCKED_STATE, "Environment is in a locked state.");
			}
			env.setCiState(lock);
			env.setComments("");
			cmManager.updateCI(env);
			logger.info("locked env id " + envId + " state:" + env.getCiState() );
			envUnderProcess.add(envId);
		} else {
			//could not acquire lock as env is locked
			logger.info("could not acquire lock for  " + envId + " lock:" + lock);
			throw new TransistorException(CmsError.TRANSISTOR_ENVIRONMENT_IN_LOCKED_STATE, "Environment is in a locked state.");
		}
	}

	private boolean isEnvLocked(CmsCI env) {
		return LOCKED_STATE.equals(env.getCiState()) || MANIFEST_LOCKED_STATE.equals(env.getCiState());
	}

	private String getLockName(long envId) {
		return envId+"-" + LOCKED_STATE;
	}

	public void unlockEnv(long envId, String envMsg, String processId) {
		unlockEnv(envId, envMsg, processId, DEFAULT_STATE);
	}

	public void unlockEnv(long envId, String envMsg, String processId, String state) {
		cmUtilProcessor.releaseLock(getLockName(envId), processId);
		CmsCI env = cmManager.getCiById(envId);
		env.setCiState(state);

		env.setComments(envMsg);
		cmManager.updateCI(env);
		logger.info("unlocked env id " + envId + " state:" + env.getCiState());
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

	public void setCmUtilProcessor(CmsUtilProcessor cmUtilProcessor) {
		this.cmUtilProcessor = cmUtilProcessor;
	}

	public CmsUtilProcessor getCmUtilProcessor() {
		return cmUtilProcessor;
	}

	public void setEnvTimeOutInSeconds(int envTimeOutInSeconds) {
		this.envTimeOutInSeconds = envTimeOutInSeconds;
	}

	public int getEnvTimeOutInSeconds() {
		return envTimeOutInSeconds;
	}
}
