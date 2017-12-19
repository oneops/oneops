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

import com.oneops.cms.util.CmsError;
import com.oneops.transistor.exceptions.TransistorException;
import com.oneops.transistor.service.BomManager;
import org.apache.log4j.Logger;

import java.util.Set;

public class InMemoryBomProcessor {
    static Logger logger = Logger.getLogger(InMemoryBomProcessor.class);

    private BomManager bomManager;
    private ThreadLocalDJMapper threadLocalDJMapper;

    public void setThreadLocalDJMapper(ThreadLocalDJMapper threadLocalDJMapper) {
        this.threadLocalDJMapper = threadLocalDJMapper;
    }

    public void setBomManager(BomManager bomManager) {
        this.bomManager = bomManager;
    }


    public BomData compileEnv(long envId, String userId, Set<Long> excludePlats, String desc, boolean autodeploy, boolean commit) {
        return buildBom(envId, userId, excludePlats, desc, autodeploy, commit);
    }


    private BomData buildBom(final long envId, final String userId, final Set<Long> excludePlats, final String desc, final boolean deploy, final boolean commit) {
        try {
            long startTime = System.currentTimeMillis();
            InMemoryDJMapper mapper = new InMemoryDJMapper();
            threadLocalDJMapper.set(mapper);
            bomManager.generateBom(envId, userId, excludePlats, desc, commit);
            logger.info(" Generation time taken: " + (System.currentTimeMillis() - startTime) + " ms");
            logger.info(mapper.toString());
            return mapper.getBOM();
        } catch (Exception e) {
            logger.error("Exception  in build bom ", e);
            throw new TransistorException(CmsError.TRANSISTOR_BOM_GENERATION_FAILED, e.getMessage());
        }
    }
}
