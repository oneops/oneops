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

import java.io.IOException;
import java.util.*;

import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.tekton.TektonUtils;
import org.apache.log4j.Logger;
import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.tekton.TektonClient;
import com.oneops.tekton.CloudProviderMapping;
import com.oneops.transistor.service.peristenceless.BomData;
import com.oneops.transistor.service.peristenceless.InMemoryBomProcessor;
import com.oneops.cms.exceptions.CmsBaseException;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.exceptions.TransistorException;

public class BomAsyncProcessor {

    private static final String THREAD_PREFIX_BOM = "async-env-";
    private static final String THREAD_PREFIX_FLEX = "async-flex-";
    static Logger logger = Logger.getLogger(BomAsyncProcessor.class);

    private CmsCmProcessor cmProcessor;
    private BomManager bomManager;
    private FlexManager flexManager;
    private EnvSemaphore envSemaphore;
    private InMemoryBomProcessor imBomProcessor;
    private TektonClient tektonClient;
    private TektonUtils tektonUtils;
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
                    BomData bomData = imBomProcessor.compileEnv(envId, userId, excludePlats, null, commit);
                    String orgName = dpmt.getNsPath().split("/")[1];
                    List<CloudProviderMapping> cloudProviderMappings = tektonUtils.getCloudProviderMappings();
                    if (cloudProviderMappings != null && cloudProviderMappings.size() > 0) {
                        long deploymentId = reserveQuota(bomData, orgName, userId, cloudProviderMappings);
                        dpmt.setDeploymentId(deploymentId);
                    }
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

    long reserveQuota(BomData bomData, String orgName, String userId, List<CloudProviderMapping> mappings) throws IOException {

        Collection<CmsRfcCI> rfcCIs = bomData.getCis();
        int totalCores = 0;
        Map<String, Map<String, Integer>> quotaNeeded = new HashMap<>();

        for (CmsRfcCI ciRfc : rfcCIs) {
            String className = ciRfc.getCiClassName();
            long rfcCiID = ciRfc.getCiId();
            if (ciRfc.getRfcAction().equals("add")) {
                CmsRfcRelation deployedToRelation = null;
                for (CmsRfcRelation relationRfc : bomData.getRelations()) {
                    if (relationRfc.getFromCiId() != null && relationRfc.getFromCiId() == rfcCiID
                            && "base.DeployedTo".equals(relationRfc.getRelationName())) {
                        deployedToRelation = relationRfc;
                        break;
                    }
                }
                String provider = tektonUtils.findProvider(deployedToRelation);

                String subscriptionId = tektonUtils.findSubscriptionId(deployedToRelation.getToCiId());
                CloudProviderMapping cloudProviderMapping = tektonUtils.getCloudProviderMapping(provider, mappings);

                if (cloudProviderMapping == null) {
                    logger.info("Soft quota check: no provider mapping found for provider " + provider);
                    continue;
                }

                if (className.endsWith(".Compute")) {
                    String size = ciRfc.getAttribute("size").getNewValue();
                    int cores = tektonUtils.getTotalCores(size, cloudProviderMapping);
                    totalCores = totalCores + cores;
                    Map<String, Integer> resourcesNeeded = quotaNeeded.get(subscriptionId);

                    if (resourcesNeeded == null) {
                        resourcesNeeded = new HashMap();
                        quotaNeeded.put(subscriptionId, resourcesNeeded);
                    }

                    resourcesNeeded.put("cores", totalCores);
                }
            }
        }

        long deploymentId = cmProcessor.getNextDjId();
        if (totalCores > 0) {
            tektonClient.reserveQuota(quotaNeeded, String.valueOf(deploymentId), orgName, userId);
        }
        return deploymentId;
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

    public void setImBomProcessor(InMemoryBomProcessor imBomProcessor) {
        this.imBomProcessor = imBomProcessor;
    }

    public void setTektonClient(TektonClient tektonClient) {
        this.tektonClient = tektonClient;
    }

    public void setTektonUtils(TektonUtils tektonUtils) {
        this.tektonUtils = tektonUtils;
    }
}
