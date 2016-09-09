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
package com.oneops.antenna.domain.filter;

import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.NotificationSeverity;
import com.oneops.antenna.domain.NotificationType;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;

/**
 * Notification message filter implementation.
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 * @version 1.0
 */
public class NotificationFilter implements MessageFilter {
    /**
     * Logger instance
     */
    private static Logger logger = Logger.getLogger(NotificationFilter.class);

    /**
     * Json Object mapper
     */
    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * Notification event type
     */
    private NotificationType eventType;

    /**
     * Notification event severity
     */
    private NotificationSeverity eventSeverity;

    /**
     * Clouds to be applicable for message filtering
     */
    private String[] clouds;

    /**
     * NS Paths to be used for message filtering
     */
    private String[] nsPaths;

    /**
     * Message selection pattern. May be we can use it in future.
     */
    private String selectorPattern;

    /**
     * Env Profiles to be used for message filtering based on type of env the notification is coming from
     */
    private String envProfilePattern;


    @Override
    public boolean accept(NotificationMessage msg) {
        String envProfile = msg.getEnvironmentProfileName();
        // Filter ALL (ie, filtering == none) or that specific event type
        if (NotificationType.none == this.eventType || msg.getType() == this.eventType) {
            // Filter ALL (ie, filtering == none) or >= specific event severity
            if (msg.getSeverity().getLevel() >= this.eventSeverity.getLevel()) {
                if (hasValidNSPath(msg.getNsPath())) {
                    if (envProfile == null || envProfile.trim().equals("")
                            || envProfilePattern == null || envProfilePattern.trim().equals("")
                            || envProfile.matches(envProfilePattern.trim())) {
                        // ToDo - add cloud and selector pattern check once it is finalized.
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Converts the json string to java string array.
     *
     * @param jsonString json array string
     * @return java array. Returns <code>null</code>, if there is any error parsing the json string or not of type array.
     */
    private static String[] toArray(String jsonString) {
        try {
            return mapper.readValue(jsonString, String[].class);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Build a new message notification filter from the sink CI.
     *
     * @param sink {@link CmsCI} sink CI
     * @return newly built NotificationFilter. <code>null</code> if the message
     * filter is not enabled or N/A.
     */
    public static NotificationFilter fromSinkCI(CmsCI sink) {
        // For backward compatibility, check if the filter attributes are present.
        CmsCIAttribute attr = sink.getAttribute("filter_enabled");
        if (attr != null) {
            boolean filterEnabled = Boolean.valueOf(attr.getDjValue());
            if (filterEnabled) {
                NotificationType eventType = NotificationType.valueOf(sink.getAttribute("event_type").getDjValue());
                NotificationSeverity eventSeverity = NotificationSeverity.valueOf(sink.getAttribute("severity_level").getDjValue());

                // NS Paths
                attr = sink.getAttribute("ns_paths");
                String[] nsPaths = null;
                if (attr != null) {
                    nsPaths = toArray(attr.getDjValue());
                }
                // Monitoring clouds
                attr = sink.getAttribute("monitoring_clouds");
                String[] clouds = null;
                if (attr != null) {
                    clouds = toArray(attr.getDjValue());
                }
                // Message selector pattern
                attr = sink.getAttribute("msg_selector_regex");
                String pattern = null;
                if (attr != null) {
                    pattern = attr.getDjValue();
                }
                //Env profile
                attr = sink.getAttribute("env_profile");
                String envProfilePattern = null;
                if (attr != null) {
                    envProfilePattern = attr.getDjValue();
                }
                NotificationFilter filter = new NotificationFilter().eventType(eventType)
                        .eventSeverity(eventSeverity)
                        .clouds(clouds)
                        .nsPaths(nsPaths)
                        .selectorPattern(pattern)
                        .envProfilePattern(envProfilePattern);
                logger.info("Notification filter : " + filter);
                return filter;
            }
        }
        return null;
    }


    private NotificationFilter envProfilePattern(String envProfilePattern) {
        this.envProfilePattern = envProfilePattern;
        return this;
    }

    /**
     * Checks whether the message nspath is a valid one for filtering.
     *
     * @param nsPath notification message nspath
     * @return <code>false</code> if it's need to be filtered, else return <code>true</code>
     */
    public boolean hasValidNSPath(String nsPath) {
        if (this.nsPaths == null) {
            return true;
        } else {
            for (String nsp : nsPaths) {
                if (nsPath.startsWith(nsp)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* Fluent interfaces */

    public NotificationType eventType() {
        return this.eventType;
    }

    public NotificationSeverity eventSeverity() {
        return this.eventSeverity;
    }

    public String[] clouds() {
        return this.clouds;
    }

    public String selectorPattern() {
        return this.selectorPattern;
    }

    public NotificationFilter eventType(final NotificationType eventType) {
        this.eventType = eventType;
        return this;
    }

    public NotificationFilter eventSeverity(final NotificationSeverity eventSeverity) {
        this.eventSeverity = eventSeverity;
        return this;
    }

    public NotificationFilter clouds(final String[] clouds) {
        this.clouds = clouds;
        return this;
    }

    public NotificationFilter selectorPattern(final String selectorPattern) {
        this.selectorPattern = selectorPattern;
        return this;
    }

    public String[] nsPaths() {
        return this.nsPaths;
    }

    public NotificationFilter nsPaths(final String[] nsPaths) {
        this.nsPaths = nsPaths;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NotificationFilter{");
        sb.append("eventType=").append(eventType);
        sb.append(", eventSeverity=").append(eventSeverity);
        sb.append(", clouds=").append(Arrays.toString(clouds));
        sb.append(", nsPaths=").append(Arrays.toString(nsPaths));
        sb.append(", selectorPattern='").append(selectorPattern).append('\'');
        sb.append(", envProfilePattern='").append(envProfilePattern).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

