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
package com.oneops.search.msg.processor.es;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.oneops.search.msg.index.Indexer;
import com.oneops.search.msg.processor.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Elastic Search message processor. Indexes the
 * work orders ,action orders, deployments, CMS releases ,
 * CMS CIs , procedures and notifications on elastic search.
 *
 * @author ranand
 */
public class ESMessageProcessor implements MessageProcessor {

    private static Logger logger = Logger.getLogger(ESMessageProcessor.class);

    @Autowired
    private DeploymentMessageProcessor deploymentMessageProcessor;
    @Autowired
    private OpsProcMessageProcessor opsProcProcessor;
    @Autowired
    private CIMessageProcessor ciMessageProcessor;
    @Autowired
    private NSMessageProcessor nsMessageProcessor;
    @Autowired
    private ReleaseMessageProcessor releaseMessageProcessor;
    @Autowired
    private NotificationMessageProcessor notificationMessageProcessor;
    @Autowired
    private WorkorderMessageProcessor workorderMessageProcessor;

    @Autowired
    private Indexer indexer;


    public void processMessage(String message, String msgType, String msgId) {
        if (StringUtils.isNotBlank(message) && !"null".equalsIgnoreCase(message)) {
            processMessageInt(message, msgType, msgId);
        } else if (StringUtils.isNotBlank(msgType) && StringUtils.isNotBlank(msgId)) {
            deleteMessage(msgType, msgId);
        } else {
            logger.warn("Received blank message for message type::" + msgType + ", id ::" + msgId);
        }
    }

    /**
     * Elastic Search message processor. Indexes events and
     * different message types.
     */

    private void processMessageInt(String message, String msgType, String msgId) {
        try {
            switch (msgType) {
                case "deployment":
                    deploymentMessageProcessor.processMessage(message, msgType, msgId);
                    break;
                case "opsprocedure":
                    opsProcProcessor.processMessage(message, msgType, msgId);
                    break;
                case "cm_ci":
                case  "cm_ci_new":
                    ciMessageProcessor.processMessage(message, msgType, msgId);
                    break;
                case "cm_ci_rel":
                    break;
                case "release":
                    releaseMessageProcessor.processMessage(message, msgType, msgId);
                    break;
                case "notification":
                    notificationMessageProcessor.processMessage(message, msgType, msgId);
                    break;
                case "workorder":
                    workorderMessageProcessor.processMessage(message, msgType, msgId);
                    break;
                case "actionorder":
                    indexer.index(msgId, msgType, message);
                    break;
                default:
                    // do not process anything else we don't use. Default clause left empty on purpose
                   // indexer.index(msgId, msgType, message);
                    logger.info("Won't process:"+msgType);
                    break;
            }
        } catch (Exception e) {
            logger.error(">>>>>>>>Error in processMessage() ESMessageProcessor for type :" + msgType+ "::msgId :"+ msgId +"::" + ExceptionUtils.getMessage(e), e);
        }
    }

    private void deleteMessage(String msgType, String msgId) {
        try {
            if ("namespace".equals(msgType)) {
                nsMessageProcessor.processNSDeleteMsg(msgId);
            } else {
                if ("cm_ci".equals(msgType)) {
                    msgType = "ci";
                   // relationMsgProcessor.processRelationDeleteMsg(msgId); //Delete all relation docs for given ci 
                    indexer.getTemplate().delete(indexer.getIndexName(), ".percolator", msgId);//TEMP code: Till ciClassName is available try to delete all ciIds from percolator type also

                    JsonObject object = new JsonObject();
                    object.add("timestamp", new JsonPrimitive(new Date().getTime()));
                    object.add("ciId", new JsonPrimitive(msgId));
                    indexer.indexEvent("ci_delete", object.toString());
                } else if ("cm_ci_rel".equals(msgType)){
                    return;                    // no longer deal with relation messages
                }
                indexer.getTemplate().delete(indexer.getIndexByType(msgType), msgType, msgId);
                logger.info("Deleted message with id::" + msgId + " and type::" + msgType + " from ES index:"+indexer.getIndexByType(msgType));
            }
        } catch (Exception e) {
            logger.error(">>>>>>>>Error in deleteMessage() ESMessageProcessorfor type :" + msgType+ " ::msgId :"+ msgId +"::" + ExceptionUtils.getMessage(e), e);
        }
    }
}
