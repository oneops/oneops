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
package com.oneops.transistor.service.peristenceless;

import com.google.gson.Gson;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.exceptions.TransistorException;
import com.oneops.transistor.service.BomManager;
import com.oneops.transistor.service.EnvSemaphore;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InMemoryBomProcessor {
    private static Logger logger = Logger.getLogger(InMemoryBomProcessor.class);

    private BomManager bomManager;
    private ThreadLocalDJMapper threadLocalDJMapper;
    private EnvSemaphore envSemaphore;
    private Gson gson = new Gson();

    public void setThreadLocalDJMapper(ThreadLocalDJMapper threadLocalDJMapper) {
        this.threadLocalDJMapper = threadLocalDJMapper;
    }

    public void setBomManager(BomManager bomManager) {
        this.bomManager = bomManager;
    }

    public void setEnvSemaphore(EnvSemaphore envSemaphore) {
        this.envSemaphore = envSemaphore;
    }

    public BomData compileEnv(long envId, String userId, Set<Long> excludePlats, String desc, boolean commit) {
        long startTime = System.currentTimeMillis();
        InMemoryDJMapper mapper = new InMemoryDJMapper();
        threadLocalDJMapper.set(mapper);

        if (commit) {
            String processId = UUID.randomUUID().toString();
            String envMsg = null;
            try {
                envSemaphore.lockEnv(envId, EnvSemaphore.LOCKED_STATE, processId);
                Map bomInfo = bomManager.generateBom(envId, userId, excludePlats, desc, true);
                Map<String, Object> bomGenerationInfo = new HashMap<>();
                bomGenerationInfo.put("rfcCiCount", bomInfo.get("rfcCiCount"));
                bomGenerationInfo.put("rfcRelationCount", bomInfo.get("rfcRelationCount"));
                bomGenerationInfo.put("manifestCommit", bomInfo.get("manifestCommit"));
                bomGenerationInfo.put("createdBy", userId);
                bomGenerationInfo.put("mode", "memory");
                envMsg = EnvSemaphore.SUCCESS_PREFIX + " Generation time taken: " + ((System.currentTimeMillis() - startTime) / 1000.0) + " seconds. bomGenerationInfo=" + gson.toJson(bomGenerationInfo);
            } catch (Exception e) {
                logger.error("Exception while generating BOM in memory: ", e);
                envMsg = EnvSemaphore.BOM_ERROR + e.getMessage();
                throw new TransistorException(CmsError.TRANSISTOR_BOM_GENERATION_FAILED, envMsg);
            } finally {
                envSemaphore.unlockEnv(envId, envMsg, processId, mapper.getCis().isEmpty() ? EnvSemaphore.DEFAULT_STATE : EnvSemaphore.REPLACE_STATE);
            }
        } else {
            try {
                bomManager.generateBom(envId, userId, excludePlats, desc, false);
                logger.info("Generation time taken: " + (System.currentTimeMillis() - startTime) + " ms");
            } catch (Exception e) {
                logger.error("Exception in build bom ", e);
                throw new TransistorException(CmsError.TRANSISTOR_BOM_GENERATION_FAILED, e.getMessage());
            }
        }

        return mapper.getBOM();
    }
}
