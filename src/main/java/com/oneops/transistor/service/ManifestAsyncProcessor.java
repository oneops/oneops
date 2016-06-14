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

import com.oneops.cms.exceptions.CmsBaseException;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManifestAsyncProcessor {
	static Logger logger = Logger.getLogger(ManifestAsyncProcessor.class);
	
	private ManifestManager manifestManager;
	private EnvSemaphore envSemaphore;

	public void setManifestManager(ManifestManager manifestManager) {
		this.manifestManager = manifestManager;
	}

	public void setEnvSemaphore(EnvSemaphore envSemaphore) {
		this.envSemaphore = envSemaphore;
	}

	public long generateEnvManifest(long envId, String userId, Map<String, String> platModes) {
		String oldThreadName = Thread.currentThread().getName();
		Thread.currentThread().setName(manifestManager.getProcessingThreadName(oldThreadName,envId));
		envSemaphore.lockEnv(envId, EnvSemaphore.MANIFEST_LOCKED_STATE);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		long releaseId = 0;
		executor.submit(() -> {
			long relId = 0;
			String envMsg = null;
			try {
				relId = manifestManager.generateEnvManifest(envId, userId, platModes);
			} catch (CmsBaseException e) {
				logger.error("CmsBaseException occurred", e);
				envMsg = EnvSemaphore.ERROR_PREFIX + e.getMessage();
				throw e;
			} finally {
				envSemaphore.unlockEnv(envId, envMsg);
			}
			return relId;
		});
		executor.shutdown();
		Thread.currentThread().setName(oldThreadName);
		return releaseId;
	}

}
