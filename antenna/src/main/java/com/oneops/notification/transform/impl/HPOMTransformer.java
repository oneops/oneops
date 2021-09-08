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
package com.oneops.notification.transform.impl;

import com.oneops.notification.NotificationMessage;
import com.oneops.notification.NotificationSeverity;
import com.oneops.notification.transform.Transformer;

import static com.oneops.util.URLUtil.getInstanceRedirectUrl;
import static com.oneops.util.URLUtil.getMonitorRedirectUrl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Message transformer for HP Operations manager agents.
 *
 * @author <a href="mailto:sgopal1@xxx.com">Suresh G</a>
 */
public class HPOMTransformer extends Transformer {

    private static Logger logger = Logger.getLogger(HPOMTransformer.class);

    /**
     * Transforms the notification message to HPOM webhook compliant object.
     * Specific transformation behavior is determined by the transformer
     * implementation and transformer context settings.
     *
     * @param msg notification message to be transformed
     * @param ctx transformer context
     * @return transformed object
     */
    @Override
    protected NotificationMessage apply(NotificationMessage msg, Context ctx) {
        String nsPath = msg.getNsPath();
        String[] nsPathTokens = nsPath.split("/");
        String org = null;
        String assembly = null;
        String platform = null;
        String env = null;
        String eventName = null;
        String ciName = null;
        String threshold = "";
        int totalAlertPercentage = 0; // this count is for both unhealthy and notify

        if (nsPathTokens.length > 5) {
            org = nsPathTokens[1];
            assembly = nsPathTokens[2];
            env = nsPathTokens[3];
            platform = nsPathTokens[5];
        }
        if (msg.getPayload() != null) {
            eventName = msg.getPayloadString("eventName");
            ciName = msg.getPayloadString("ciName");
            threshold = msg.getPayloadString("threshold");
            String totalInstances = msg.getPayloadString("total");
            String unhealthyInstances = msg.getPayloadString("unhealthy");
            String notifyInstances = msg.getPayloadString("notify");
            
            int totalCount = 0;
            int unhealthyCount = 0;
            int notifyCount = 0;
            
            if (NumberUtils.isNumber(totalInstances)) {
            	totalCount = Integer.valueOf(totalInstances.trim());
            }

            if (NumberUtils.isNumber(unhealthyInstances)) {
            	unhealthyCount = Integer.valueOf(unhealthyInstances.trim());
            }

            if (NumberUtils.isNumber(notifyInstances)) {
            	notifyCount = Integer.valueOf(notifyInstances.trim());
            }
            
            int totalAlerting = unhealthyCount + notifyCount;
            
            if (totalCount > 0) {
            	totalAlertPercentage = (100 * totalAlerting)/totalCount;
            }
        }

        String msgGroup = getMsgGroup(org, assembly);

        String text = new StringBuilder()
                .append(msg.getSeverity())
                .append(" | ")
                .append(msg.getType())
                .append(" | ")
                .append(msg.getTimestamp())
                .append(" | MonitorName: ")
                .append(eventName + ":" + threshold)
                .append(" | ")
                .append("Profile: " + msg.getEnvironmentProfileName())
                .append("; AdminStatus: " + msg.getAdminStatus())
                .append("; ComponentId: " + msg.getManifestCiId())
                .append("; ComponentName: " + getComponentName(ciName) + "; ")
                .append(msg.getText())
                .append(" | ")
                .append(msg.getNsPath()).toString();
        logger.info("hpom message text content: " + text);
        HPOMMessage hpomMsg = new HPOMMessage()
                .setLevel(HPOMMessage.Level.from(msg.getSeverity()))
                .setMessageContent(text + " OneOps-Operations: " + getInstanceRedirectUrl(msg) + " TotalAlertPercentage: " + totalAlertPercentage)
                .setCategory(msg.getType().getName())
                .setNode(ciName)
                .setSenderSystem("antenna")
                .setSenderOrg("OneOps")
                .setMessageGroup(msgGroup) //ESM-Only Needs to be changed to <tenant>:<assembly>
                .setMonitorName(env + ":" + platform + ":" + ciName + ":" + eventName)//platform:component:monitor
                .setBpv(getMonitorRedirectUrl(msg));
        hpomMsg.setNsPath(msg.getNsPath());
        hpomMsg.setType(msg.getType());
        return hpomMsg;
    }

    public String getComponentName(String ciName) {

        int index = StringUtils.lastOrdinalIndexOf(ciName, "-", 2);
        if (!(index > 0)) {
            return null;
        }
        return ciName.substring(0, index);
    }

    private String getMsgGroup(String org, String assembly) {
        String msgGroup;
        String msgGroupSysProperty = System.getProperty("hpom.msg.group");
        if (!StringUtils.isEmpty(msgGroupSysProperty)) {
            msgGroup = msgGroupSysProperty;
        } else {
            msgGroup = org + ":" + assembly;
        }
        return msgGroup;
    }
}

/**
 * HPOM message
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
class HPOMMessage extends NotificationMessage {

    private static final long serialVersionUID = 1L;

    private Level level;//CRITICAL

    private String messageContent;

    private String category;

    private String node;

    private String senderSystem;//monitor.oneops.com

    private String senderOrg;//like mobile/oneops etc

    private String messageGroup;//like nagios or ESM-Only

    private String monitorName;//like App:Mobile:procs_ldap_cachemgr

    private String bpv;

    /* Fluent interfaces*/

    public Level getLevel() {
        return level;
    }

    public HPOMMessage setLevel(Level level) {
        this.level = level;
        return this;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public HPOMMessage setMessageContent(String messageContent) {
        this.messageContent = messageContent;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public HPOMMessage setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getNode() {
        return node;
    }

    public HPOMMessage setNode(String node) {
        this.node = node;
        return this;
    }

    public String getSenderSystem() {
        return senderSystem;
    }

    public HPOMMessage setSenderSystem(String senderSystem) {
        this.senderSystem = senderSystem;
        return this;
    }

    public String getSenderOrg() {
        return senderOrg;
    }

    public HPOMMessage setSenderOrg(String senderOrg) {
        this.senderOrg = senderOrg;
        return this;
    }

    public String getMessageGroup() {
        return messageGroup;
    }

    public HPOMMessage setMessageGroup(String messageGroup) {
        this.messageGroup = messageGroup;
        return this;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public HPOMMessage setMonitorName(String monitorName) {
        this.monitorName = monitorName;
        return this;
    }

    public String getBpv() {
        return bpv;
    }

    public HPOMMessage setBpv(String bpv) {
        this.bpv = bpv;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HPOMMessage{");
        sb.append("level=").append(level);
        sb.append(", messageContent='").append(messageContent).append('\'');
        sb.append(", category='").append(category).append('\'');
        sb.append(", node='").append(node).append('\'');
        sb.append(", senderSystem='").append(senderSystem).append('\'');
        sb.append(", senderOrg='").append(senderOrg).append('\'');
        sb.append(", messageGroup='").append(messageGroup).append('\'');
        sb.append(", monitorName='").append(monitorName).append('\'');
        sb.append(", bpv='").append(bpv).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * HPOM Message level enum
     */
    public static enum Level {

        /**
         * Message level
         */
        CRITICAL("CRITICAL"), MAJOR("MAJOR"), NORMAL("NORMAL");

        /**
         * Message code
         */
        private String code;

        /**
         * Constructor
         */
        private Level(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        /**
         * Convert from OneOps severity level to hpom message level
         */
        public static Level from(NotificationSeverity severity) {

            Level level;

            switch (severity) {
                case critical:
                    level = CRITICAL;
                    break;
                case warning:
                    level = MAJOR;
                    break;
                case info:
                    level = NORMAL;
                    break;
                default:
                    level = NORMAL;
                    break;
            }

            return level;
        }


    }
}
