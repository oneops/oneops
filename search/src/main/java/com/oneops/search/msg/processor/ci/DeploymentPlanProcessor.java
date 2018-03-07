package com.oneops.search.msg.processor.ci;

import com.google.gson.Gson;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.search.domain.CmsDeploymentPlan;
import com.oneops.search.msg.index.Indexer;
import com.oneops.search.msg.processor.CIMessageProcessor;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.oneops.search.msg.processor.MessageProcessor.GSON_ES;

/*******************************************************************************
 *
 *   Copyright 2016 Walmart, Inc.
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

@Service
public class DeploymentPlanProcessor implements CISImpleProcessor{
    private Logger logger = Logger.getLogger(this.getClass());
    private static Gson gson = new Gson();

    @Autowired
    private Indexer indexer;

    public void process(CmsCISimple ci) {
        CmsDeploymentPlan deploymentPlan;

        try {
            String comments = ci.getComments();
            double genTime = extractTimeTaken(comments);
            Map releaseInfo = extractReleaseInfo(comments);
            if (releaseInfo != null) {
                deploymentPlan = new CmsDeploymentPlan();
                deploymentPlan.setNsPath(ci.getNsPath() + "/" + ci.getCiName() + "/bom");
                deploymentPlan.setCreatedBy(ci.getCreatedBy());
                deploymentPlan.setCiId(ci.getCiId());
                deploymentPlan.setCreated(ci.getUpdated());
                deploymentPlan.setPlanGenerationTime(genTime);
                deploymentPlan.setCreatedBy((String) releaseInfo.get("createdBy"));
                deploymentPlan.setMode((String) releaseInfo.get("mode"));
                deploymentPlan.setManifestCommit((Boolean) releaseInfo.get("manifestCommit"));
                deploymentPlan.setCiRfcCount(((Double) releaseInfo.get("rfcCiCount")).intValue());
                deploymentPlan.setRelationRfcCount(((Double) releaseInfo.get("rfcRelationCount")).intValue());
                Object releaseId = releaseInfo.get("releaseId");
                if (releaseId != null) {
                    deploymentPlan.setReleaseId(((Double) releaseId).longValue());
                    deploymentPlan.setAutoDeploy((Boolean) releaseInfo.get("autoDeploy"));
                }
                indexer.index(null, "plan", GSON_ES.toJson(deploymentPlan));
            }
        } catch (Exception e) {
            logger.error("Exception in processing deployment message: " + ExceptionUtils.getMessage(e), e);
        }
    }

    private static Double extractTimeTaken(String comments) {
        String timeTaken = comments.substring(CIMessageProcessor.ENV_SUCCESS_PREFIX.length(), comments.indexOf(" seconds.")).trim();
        double planGentime = Double.parseDouble(timeTaken);
        return planGentime == 0 ? 1 : planGentime;
    }

    private static Map extractReleaseInfo(String comments) {
        String prefix = "bomGenerationInfo=";
        int startingIndex = comments.indexOf(prefix) + prefix.length();
        int endIndex = comments.lastIndexOf("}") + 1;
        String releaseInfoJson = comments.substring(startingIndex, endIndex);
        return gson.fromJson(releaseInfoJson, HashMap.class);
    }
}
