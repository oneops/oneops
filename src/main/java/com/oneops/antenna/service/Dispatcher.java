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
package com.oneops.antenna.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.antenna.domain.BasicSubscriber;
import com.oneops.antenna.domain.EmailSubscriber;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.SNSSubscriber;
import com.oneops.antenna.domain.URLSubscriber;
import com.oneops.antenna.domain.XMPPSubscriber;
import com.oneops.antenna.senders.NotificationSender;
import com.oneops.antenna.subscriptions.SubscriberService;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.service.OpsProcedureProcessor;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.service.CmsDpmtProcessor;

/**
 * The Class Dispatcher.
 */
public class Dispatcher {

    private static Logger logger = Logger.getLogger(Dispatcher.class);

    private SubscriberService sbrService;
    private NotificationSender eSender;
    private NotificationSender snsSender;
    private NotificationSender urlSender;
    private NotificationSender xmppSender;
    private CmsCmProcessor cmProcessor;
    private CmsDpmtProcessor dpmtProcessor;
    private OpsProcedureProcessor procProcessor;
    private NotificationMessageDao nmDao;
    private Gson gson;

    /**
     * Sets the gson.
     *
     * @param gson the new gson
     */
    public void setGson(Gson gson) {
        this.gson = gson;
    }

    /**
     * Sets the sns sender.
     *
     * @param snsSender the new sns sender
     */
    public void setSnsSender(NotificationSender snsSender) {
        this.snsSender = snsSender;
    }

    /**
     * Sets the url sender.
     *
     * @param urlSender the new url sender
     */
    public void setUrlSender(NotificationSender urlSender) {
        this.urlSender = urlSender;
    }

    /**
     * Sets the e sender.
     *
     * @param eSender the new e sender
     */
    public void seteSender(NotificationSender eSender) {
        this.eSender = eSender;
    }

    /**
     * @return the xmppSender
     */
    public NotificationSender getXmppSender() {
        return xmppSender;
    }

    /**
     * @param xmppSender the xmppSender to set
     */
    public void setXmppSender(NotificationSender xmppSender) {
        this.xmppSender = xmppSender;
    }

    /**
     * Sets the cm processor.
     *
     * @param cmProcessor the new cm processor
     */
    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }

    /**
     * Sets the dpmt processor.
     *
     * @param dpmtProcessor the new dpmt processor
     */
    public void setDpmtProcessor(CmsDpmtProcessor dpmtProcessor) {
        this.dpmtProcessor = dpmtProcessor;
    }

    /**
     * Sets the proc processor.
     *
     * @param procProcessor the new proc processor
     */
    public void setProcProcessor(OpsProcedureProcessor procProcessor) {
        this.procProcessor = procProcessor;
    }

    /**
     * Sets the sbr service.
     *
     * @param sbrService the new sbr service
     */
    public void setSbrService(SubscriberService sbrService) {
        this.sbrService = sbrService;
    }

    /**
     * Sets the nm dao.
     *
     * @param nmDao the new nm dao
     */
    public void setNmDao(NotificationMessageDao nmDao) {
        this.nmDao = nmDao;
    }

    /**
     * Message dispatcher logic.
     *
     * @param msg the msg
     */
    public void dispatch(NotificationMessage msg) {
        if (msg.getNsPath() == null) {
            String nsPath = getNsPath(msg);
            if (nsPath != null) {
                msg.setNsPath(nsPath);
            } else {
                logger.error("Can not figure out nsPath for msg " + gson.toJson(msg));
                return;
            }
        }
        // Persist the msg
        nmDao.addNotificationMessage(msg);

        List<BasicSubscriber> subscribers = sbrService.getSubscribersForNs(msg.getNsPath());
        try {
            for (BasicSubscriber sub : subscribers) {
                NotificationMessage nMsg = msg;
                if (sub.hasFilter() && !sub.getFilter().accept(nMsg)) {
                    continue;
                }
                if (sub.hasTransformer()) {
                    nMsg = sub.getTransformer().transform(nMsg);
                }
                if (sub instanceof EmailSubscriber) {
                    eSender.postMessage(nMsg, sub);
                } else if (sub instanceof SNSSubscriber) {
                    snsSender.postMessage(nMsg, sub);
                } else if (sub instanceof URLSubscriber) {
                    urlSender.postMessage(nMsg, sub);
                } else if (sub instanceof XMPPSubscriber) {
                    xmppSender.postMessage(nMsg, sub);
                }
            }
        } catch (Exception e) {
            logger.error("Message dispatching failed.", e);
        }
    }

    /**
     * Get nspath based on the notification message type.
     *
     * @param msg notification message
     * @return nspath string
     */
    private String getNsPath(NotificationMessage msg) {
        String nsPath = null;
        switch (msg.getType()) {
            case ci:
                nsPath = getNsPathForCi(msg.getCmsId());
                break;
            case deployment:
                nsPath = getNsPathForDpmt(msg.getCmsId());
                break;
            case procedure:
                nsPath = getNsPathForProc(msg.getCmsId());
                break;
            default:
                nsPath = null;
                break;
        }
        return nsPath;
    }

    /**
     * Nspath for ci message type
     *
     * @param ciId ci id
     * @return nspath
     */
    private String getNsPathForCi(long ciId) {
        CmsCI ci = cmProcessor.getCiById(ciId);
        if (ci == null) {
            logger.error("Can not get ci with id - " + ciId);
            return null;
        }
        return ci.getNsPath();
    }

    /**
     * Nspath for deployment message type
     *
     * @param dpmtId deployment id
     * @return nspath
     */
    private String getNsPathForDpmt(long dpmtId) {
        CmsDeployment dpmt = dpmtProcessor.getDeployment(dpmtId);
        if (dpmt == null) {
            logger.error("Can not get deployment with id - " + dpmtId);
            return null;
        }
        return dpmt.getNsPath();
    }

    /**
     * Nspath for procedure action type
     *
     * @param procId proc id
     * @return nspath
     */
    private String getNsPathForProc(long procId) {
        CmsOpsProcedure proc = procProcessor.getCmsOpsProcedure(procId, false);
        if (proc == null) {
            logger.error("Can not get procedure with id - " + procId);
            return null;
        }
        return getNsPathForCi(proc.getCiId());
    }


    /**
     * Notification event dispatch methods.
     */
    public static enum Method {

        /**
         * Dispatching methods
         */
        SYNC("sync"), ASYNC("async");

        /**
         * Dispatch method name
         */
        private String name;

        /**
         * Enum constructor
         *
         * @param name
         */
        private Method(String name) {
            this.name = name;
        }

        /**
         * Returns the dispatch method name.
         *
         * @return name
         */
        String getName() {
            return name;
        }
    }
}

