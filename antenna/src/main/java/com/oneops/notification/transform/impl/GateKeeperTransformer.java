/*******************************************************************************
 *
 *   Copyright 2018 Walmart, Inc.
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
package com.oneops.notification.transform.impl;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsDpmtRecord;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.notification.NotificationMessage;
import com.oneops.notification.NotificationType;
import com.oneops.notification.transform.Transformer;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Message transformer for GateKeeper.
 *
 * @author <a href="mailto:lkhusid@walmartlabs.com">Suresh G</a>
 */
public class GateKeeperTransformer extends Transformer {

    private static Logger logger = Logger.getLogger(HPOMTransformer.class);

    private static final String[] ARTIFACT_ATTRS = new String[] {"checksum", "location", "path", "repository", "version", "url"};

    /**
     * Transforms the deployment notification message only by adding additional information about artifacts
     * being deployment to the message payload.
     *
     * @param msg notification message to be transformed
     * @param ctx transformer context
     * @return transformed object
     */
    @Override
    protected NotificationMessage apply(NotificationMessage msg, Context ctx) {
        if (!msg.getType().getName().equals(NotificationType.deployment.getName())) return msg;

        Object deploymentState = msg.getPayload().get("deploymentState");
        if (!("pending".equals(deploymentState) || "complete".equals(deploymentState) || "canceled".equals(deploymentState))) return msg;

        long t = System.currentTimeMillis();
        Long deploymentId = Long.parseLong((String) msg.getPayload().get("deploymentId"));
        CmsDeployment deployment = dpmtProcessor.getDeployment(deploymentId);
        List<CmsRfcCI> artifactRfcs = rfcProcessor.getRfcCIByReleaseAndClass(deployment.getReleaseId(), "Artifact");
        if (artifactRfcs.isEmpty()) return msg;

        Map<Long, CmsCI> artifactCis = cmProcessor.getCiByIdList(artifactRfcs.stream()
                                                                 .map(CmsRfcCI::getCiId)
                                                                 .collect(toList())).stream()
                .collect(toMap(CmsCI::getCiId, Function.identity()));

        List<Long> artifactRfcIds = artifactRfcs.stream().map(CmsRfcCI::getRfcId).collect(toList());
        Map<Long, String> dpmtRecordStateMap = dpmtProcessor.getDeploymentRecordCisByRfcIds(deploymentId, artifactRfcIds).stream()
                .collect(toMap(CmsDpmtRecord::getRfcId, CmsDpmtRecord::getDpmtRecordState));

        Map<Long, Long> artifactToCloudMap = artifactRfcs.stream().collect(toMap(CmsRfcCI::getCiId, rfc -> {
            String[] split = rfc.getCiName().split("-");
            return Long.parseLong(split[split.length - 2]);
        }));

        List<Long> cloudIds = artifactToCloudMap.values().stream().distinct().collect(toList());
        Map<Long, CmsCI> cloudMap = cmProcessor.getCiByIdList(cloudIds).stream()
                .collect(toMap(CmsCI::getCiId, Function.identity()));

        List<Map<String, Object>> artifactPayload = artifactRfcs.stream()
                .map(artifactRfc -> {
                    CmsCI cloudCi = cloudMap.get(artifactToCloudMap.get(artifactRfc.getCiId()));
                    Map<String, Object> cloud = new HashMap<>();
                    cloud.put("ciId", cloudCi.getCiId());
                    cloud.put("name", cloudCi.getCiName());

                    Map<String, Object> artifact = new HashMap<>();
                    artifact.put("ciId", artifactRfc.getCiId());
                    artifact.put("name", artifactRfc.getCiName());
                    artifact.put("nsPath", artifactRfc.getNsPath());
                    artifact.put("action", artifactRfc.getRfcAction());
                    artifact.put("deploymentState", dpmtRecordStateMap.get(artifactRfc.getRfcId()));
                    CmsCI artifactCi = artifactCis.get(artifactRfc.getCiId());
                    for (String attrName : ARTIFACT_ATTRS) {
                        CmsRfcAttribute rfcAttr = artifactRfc.getAttribute(attrName);
                        if (rfcAttr != null) {
                            artifact.put(attrName, rfcAttr.getNewValue());
                        }
                        else if (artifactCi != null) {
                            CmsCIAttribute ciAttr = artifactCi.getAttribute(attrName);
                            if (ciAttr != null) {
                                artifact.put(attrName, ciAttr.getDfValue());
                            }
                        }
                    }
                    artifact.put("cloud", cloud);
                    return artifact;
                })
                .collect(toList());
        msg.getPayload().put("artifacts", artifactPayload);
        logger.info("Done in " + (System.currentTimeMillis() - t));
        logger.info("Artifact payload: " + artifactPayload);
        return msg;
    }
}
