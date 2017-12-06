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
import com.oneops.cms.util.CmsError;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManifestAsyncProcessor {
    private static final Logger logger = Logger.getLogger(ManifestAsyncProcessor.class);

    private ManifestManager manifestManager;
    private EnvSemaphore envSemaphore;
    private ExecutorService executor;

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public void setManifestManager(ManifestManager manifestManager) {
        this.manifestManager = manifestManager;
    }

    public void setEnvSemaphore(EnvSemaphore envSemaphore) {
        this.envSemaphore = envSemaphore;
    }

    public long generateEnvManifest(List<Long> envIds, String userId, Map<String, String> platModes) {
        
        String oldThreadName = Thread.currentThread().getName();
        try {
            for (long envId : envIds) {
                final String processId = UUID.randomUUID().toString();
                envSemaphore.lockEnv(envId, EnvSemaphore.MANIFEST_LOCKED_STATE, processId);
                executor.submit(() -> {
                    String envMsg = null;
                    try {
                        Thread.currentThread().setName(manifestManager.getProcessingThreadName(oldThreadName, envId));
                        return  manifestManager.generateEnvManifest(envId, userId, platModes);
                    } catch (CmsBaseException e) {
                        logger.error("CmsBaseException occurred", e);
                        envMsg = EnvSemaphore.MANIFEST_ERROR + e.getMessage();
                        // if services are missing, design pull cannot be completed, so we have to delete all previously created rfcs and release
                        if (e.getErrorCode()== CmsError.TRANSISTOR_MISSING_CLOUD_SERVICES) {
                            manifestManager.rollbackOpenRelease(envId);
                        }
                        throw e;
                    } finally {
                        envSemaphore.unlockEnv(envId, envMsg, processId);//error in design pull
                    }
                });
            }
        } finally {
            Thread.currentThread().setName(oldThreadName);
        }
        return 0; //Asynchronous processing will return release id of 0.
    }

}
