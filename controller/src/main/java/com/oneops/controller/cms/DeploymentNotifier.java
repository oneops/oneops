/*******************************************************************************
 *
 * Copyright 2015 Walmart, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.oneops.controller.cms;

import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.NotificationSeverity;
import com.oneops.antenna.domain.NotificationType;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsDpmtRecord;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.util.ReliableExecutor;
import org.activiti.engine.delegate.DelegateExecution;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


public class DeploymentNotifier {

    private static Logger logger = Logger.getLogger(CMSClient.class);
    private static final String SCOLON_PROCEDURE = "; Procedure ";


    private String serviceUrl;
    private RestTemplate restTemplate;
    private ReliableExecutor<NotificationMessage> antennaClient;
    private static final String CI = "ci:";


    protected void sendDpmtNotification(CmsDeployment dpmt) {
        try {
            if (dpmt.getDeploymentState().equalsIgnoreCase(CMSClient.FAILED)) {
                Map<String, String> payloadEntries = new HashMap<String, String>();
                try {
                    CmsDpmtRecord[] drs = restTemplate.getForObject(serviceUrl + "/dj/simple/deployments/{deploymentId}/cis?state=failed", CmsDpmtRecord[].class, dpmt.getDeploymentId());
                    if (drs != null) {
                        for (CmsDpmtRecord dr : drs) {
                            CmsRfcCISimple rfc = restTemplate.getForObject(serviceUrl + "/dj/simple/rfc/cis/{rfcId}", CmsRfcCISimple.class, dr.getRfcId());
                            if (rfc != null) {
                                payloadEntries.put(rfc.getCiClassName() + "::" + rfc.getCiName(), dr.getComments());
                            }
                        }
                    }
                } catch (HttpClientErrorException e) {
                    logger.error("HttpClientErrorException in sendDpmtNotification", e);
                }
                sendDeploymentNotification(dpmt, "Deployment failed.",
                        payloadEntries.toString() + createDeploymentNotificationText(dpmt), NotificationSeverity.critical, payloadEntries);
            } else if (dpmt.getDeploymentState().equalsIgnoreCase(CMSClient.COMPLETE)) {
                sendDeploymentNotification(dpmt, "Deployment completed successfully.", createDeploymentNotificationText(dpmt), NotificationSeverity.info, null);
            } else if (dpmt.getDeploymentState().equalsIgnoreCase("active")) {
                sendDeploymentNotification(dpmt, "Deployment started by "
                                + (StringUtils.isBlank(dpmt.getUpdatedBy()) ? dpmt.getCreatedBy() : dpmt.getUpdatedBy()),
                        createDeploymentNotificationText(dpmt), NotificationSeverity.info, null);
            } else {
                return;
            }
        } catch (Exception e) {
            //we don't want to end-up with stuck deployment if the issue with sending notification, just log it and move on
            logger.error("Exception in sendDpmtNotification", e);
        }
    }

    public String createDeploymentNotificationText(CmsDeployment dpmt) {
        String text = "";
        String ops = dpmt.getOps();
        String comments = dpmt.getComments();
        if (!StringUtils.isBlank(comments)) {
            text = comments + ". ";
        }
        if (!StringUtils.isBlank(ops)) {
            text = text + ops;
        }
        return text;
    }

    public void sendDeploymentNotification(CmsDeployment dpmt, String subject, String text,
                                           NotificationSeverity severity, Map<String, String> payloadEntries) {
        NotificationMessage notify = new NotificationMessage();
        notify.setType(NotificationType.deployment);
        notify.setCmsId(dpmt.getDeploymentId());
        notify.setSource("deployment");
        notify.setNsPath(dpmt.getNsPath());
        notify.setTimestamp(System.currentTimeMillis());
        notify.setSeverity(severity);
        notify.setSubject(NotificationMessage.buildSubjectPrefix(dpmt.getNsPath()) + subject);
        notify.setText(text);
        notify.getPayload().put("deploymentId", "" + dpmt.getDeploymentId());
        notify.getPayload().put("deploymentState", dpmt.getDeploymentState());
        notify.getPayload().put("updatedBy", dpmt.getUpdatedBy());
        notify.getPayload().put("createdBy", dpmt.getCreatedBy());
        notify.getPayload().put("description", dpmt.getDescription());
        notify.getPayload().put("comments", dpmt.getComments());
        notify.getPayload().put("ops", dpmt.getOps());
        if (payloadEntries != null) {
            notify.getPayload().putAll(payloadEntries);
        }
        antennaClient.executeAsync(notify);
    }

    /**
     * Sets the antenna client.
     *
     * @param antennaClient the new antenna client
     */
    public void setAntennaClient(ReliableExecutor<NotificationMessage> antennaClient) {
        this.antennaClient = antennaClient;
    }

    protected void sendProcNotification(CmsOpsProcedure proc, DelegateExecution exec) {
        try {
            CmsCI anchorCi = (CmsCI) exec.getVariable("procanchor");
            NotificationMessage notify = new NotificationMessage();
            notify.setType(NotificationType.procedure);
            notify.setCmsId(anchorCi.getCiId());
            notify.setSource("procedure");
            notify.setNsPath(anchorCi.getNsPath());
            notify.setTimestamp(System.currentTimeMillis());

            if (proc.getArglist() != null) {
                Map<String, String> payloadEntries = new HashMap<String, String>();
                payloadEntries.put("repeatCount", String.valueOf(proc.getArglist()));
                notify.putPayloadEntries(payloadEntries);
            }
            String subjectPrefix = NotificationMessage.buildSubjectPrefix(anchorCi.getNsPath());
            
            if (proc.getProcedureState().equals(OpsProcedureState.failed)) {
                notify.setSeverity(NotificationSeverity.critical);
                notify.setSubject(subjectPrefix + CI + anchorCi.getCiName() + SCOLON_PROCEDURE + proc.getProcedureName() + " failed.");
                notify.setText(buildNotificationPrefix(anchorCi.getNsPath()) + CI + anchorCi.getCiName() + SCOLON_PROCEDURE + proc.getProcedureName() + " failed! Please check the ci status page.");
            } else if (proc.getProcedureState().equals(OpsProcedureState.complete)) {
                notify.setSeverity(NotificationSeverity.info);
                notify.setSubject(subjectPrefix + CI + anchorCi.getCiName() + SCOLON_PROCEDURE + proc.getProcedureName() + " complete.");
                notify.setText(buildNotificationPrefix(anchorCi.getNsPath()) + CI + anchorCi.getCiName() + SCOLON_PROCEDURE + proc.getProcedureName() + " complete!");
            } else {
                return;
            }

            if (!anchorCi.getCiClassName().startsWith(CmsConstants.CLOUDSERVICEPREFIX)) {
                antennaClient.executeAsync(notify);
            }
        } catch (Exception e) {
            //just log the error and continue
            logger.error("Exception in sendProcNotification", e);
        }
    }

    private String buildNotificationPrefix(String nsPath) {
        String[] parts = nsPath.split("/");
        if (parts.length == 0) {
        	return "";
        }
        String prefix = "Assembly: " + parts[2] + "; ";
        if (parts.length > 3) {
            prefix += "Environment: " + parts[3] + "; ";
        }
        return prefix;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

}
