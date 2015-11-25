package com.oneops.transistor.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.exceptions.TransistorException;

public class EnvSemaphore {
	
	protected static final String DEFAULT_STATE = "default";
	protected static final String LOCKED_STATE = "locked";
	protected static final String MANIFEST_LOCKED_STATE = "manifest_locked";
	protected static final String ERROR_PREFIX = "ERROR:";
	protected static final String SUCCESS_PREFIX = "SUCCESS:";
	protected static final String COMPILE_INTERRUPTED = "Environment compilation was interrupted, please recompile!";

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
		envUnderProcess.add(envId);
	}
	
	public void unlockEnv(long envId, String envMsg) {
		CmsCI env = cmManager.getCiById(envId);
		env.setCiState(DEFAULT_STATE);
		env.setComments(envMsg);
		cmManager.updateCI(env);
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
