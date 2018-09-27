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

import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.exceptions.CmsBaseException;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.exceptions.TransistorException;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BomAsyncProcessor {

    private static final String THREAD_PREFIX_BOM = "async-env-";
    private static final String THREAD_PREFIX_FLEX = "async-flex-";
    static Logger logger = Logger.getLogger(BomAsyncProcessor.class);

    private CmsCmProcessor cmProcessor;
    private BomManager bomManager;
    private FlexManager flexManager;
    private EnvSemaphore envSemaphore;
    private Gson gson = new Gson();

    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }

    public void setFlexManager(FlexManager flexManager) {
        this.flexManager = flexManager;
    }

    public void setBomManager(BomManager bomManager) {
        this.bomManager = bomManager;
    }

    public void setEnvSemaphore(EnvSemaphore envSemaphore) {
        this.envSemaphore = envSemaphore;
    }

    public void compileEnv(long envId, String userId, Set<Long> excludePlats, CmsDeployment dpmt, String desc, boolean commit) {
        final String processId = UUID.randomUUID().toString();
        envSemaphore.lockEnv(envId, EnvSemaphore.LOCKED_STATE, processId);

        Thread t = new Thread(() -> {
            String envMsg = null;
            try {
                long startTime = System.currentTimeMillis();
                CmsCI environment = cmProcessor.getCiById(envId);
                bomManager.check4openDeployment(environment.getNsPath() + "/" + environment.getCiName() + "/bom");
                Map bomInfo;
                boolean deploy = (dpmt != null);
                if (deploy) {
                    bomInfo = bomManager.generateAndDeployBom(envId, userId, excludePlats, dpmt, commit);
                }
                else {
                    bomInfo = bomManager.generateBom(envId, userId, excludePlats, desc, commit);
                }
                Map<String, Object> bomGenerationInfo = new HashMap<>();
                bomGenerationInfo.put("rfcCiCount", bomInfo.get("rfcCiCount"));
                bomGenerationInfo.put("rfcRelationCount", bomInfo.get("rfcRelationCount"));
                bomGenerationInfo.put("manifestCommit", bomInfo.get("manifestCommit"));
                bomGenerationInfo.put("createdBy", userId);
                bomGenerationInfo.put("mode", "persistent");
                bomGenerationInfo.put("autoDeploy", deploy);
                CmsRelease bomRelease = (CmsRelease) bomInfo.get("release");
                if (bomRelease != null) {
                    bomGenerationInfo.put("releaseId", bomRelease.getReleaseId());
                }
                envMsg = EnvSemaphore.SUCCESS_PREFIX + " Generation time taken: " + ((System.currentTimeMillis() - startTime) / 1000.0) + " seconds. bomGenerationInfo=" + gson.toJson(bomGenerationInfo);
            } catch (Exception e) {
                logger.error("Exception in build bom ", e);
                envMsg = EnvSemaphore.BOM_ERROR + e.getMessage();
                throw new TransistorException(CmsError.TRANSISTOR_BOM_GENERATION_FAILED, envMsg);
            } finally {
                envSemaphore.unlockEnv(envId, envMsg, processId);
            }
        }, getThreadName(THREAD_PREFIX_BOM, envId));
        t.start();
    }

    public void processFlex(long envId, long flexRelId, int step, boolean scaleUp) {
        final String processId = UUID.randomUUID().toString();
        envSemaphore.lockEnv(envId, EnvSemaphore.LOCKED_STATE, processId);
        Thread t = new Thread(() -> {
            String envMsg = null;
            try {
                flexManager.processFlex(flexRelId, step, scaleUp, envId);
                envMsg = "";
            } catch (CmsBaseException e) {
                logger.error("Exception occurred while flexing the ", e);
                envMsg = EnvSemaphore.BOM_ERROR + e.getMessage();
            } finally {
                envSemaphore.unlockEnv(envId, envMsg, processId);
            }
        }, getThreadName(THREAD_PREFIX_FLEX, envId));
        t.start();
    }

    public void resetEnv(long envId) {
        envSemaphore.resetEnv(envId);
    }

    private String getThreadName(String prefix, long envId) {
        return prefix + String.valueOf(envId);
    }

    public CmsDeployment scaleDown(long platformId, int scaleDownBy, int minComputesInEachCloud,
                                   boolean ensureEvenScale, String userId) {
        CmsCI platformCi = cmProcessor.getCiById(platformId);
        List<CmsCIRelation> rels = cmProcessor.getToCIRelations(platformId, "manifest.ComposedOf", null);
        if (rels == null || rels.size() == 0) {
            throw new TransistorException(CmsError.TRANSISTOR_BOM_GENERATION_FAILED, "Platform does not exist. id :"
                    + platformId);
        }
        CmsCI envCi = rels.get(0).getFromCi();
        String envMsg = null;

        final String processId = UUID.randomUUID().toString();
        envSemaphore.lockEnv(envCi.getCiId(), EnvSemaphore.LOCKED_STATE, processId);
        CmsDeployment deployment = null;
        try {
            Map<String, Object> bomGenerationInfo = bomManager.scaleDown(platformCi, envCi, scaleDownBy,
                    minComputesInEachCloud, ensureEvenScale, userId);
            bomGenerationInfo.put("createdBy", userId);
            bomGenerationInfo.put("mode", "persistent");
            bomGenerationInfo.put("autoDeploy", true);

            if (bomGenerationInfo.get("deployment") != null) {
                deployment = (CmsDeployment) bomGenerationInfo.get("deployment");
            }

            envMsg = EnvSemaphore.SUCCESS_PREFIX + " Generation time taken: "
                    + ((Long) bomGenerationInfo.get("generationTime") / 1000.0)
                    + " seconds. bomGenerationInfo=" + gson.toJson(bomGenerationInfo);
        } catch (Exception e) {
            logger.error("Exception in scale down ", e);
            //not setting error message as comment for next time it should not block user from doing regular deployment
            envMsg = envCi.getComments();
            throw new TransistorException(CmsError.TRANSISTOR_BOM_GENERATION_FAILED, e.getMessage());
        } finally {
            envSemaphore.unlockEnv(envCi.getCiId(), envMsg, processId);
        }

        return deployment;
    }
}
