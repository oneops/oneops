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

import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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
    private static final Logger logger = Logger.getLogger(NotificationFilter.class);

    /**
     * Json Object mapper
     */
    private static final ObjectMapper mapper = new ObjectMapper();

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


    /**
     * Check whether or not the given ${@link NotificationMessage} needs
     * to be accepted based on this filter rules. The filtering logic is
     * a simple check of the filter rules in the following order.
     * <p>
     * 1. Filter ALL (ie, filtering == none) or a specific event type
     * 2. Filter ALL (ie, filtering == none) or >= specific event severity
     * 3. Filter msgs with nsPaths configured in sink.
     * 4. Filter msgs with clouds configured in sink.
     * 5. Filter msgs with env profile pattern(Case-Insensitive) configured in sink.
     *
     * @param msg {@link  com.oneops.antenna.domain.NotificationMessage}
     * @return <code>true</code> if message pass the filter rules.
     */
    @Override
    public boolean accept(NotificationMessage msg) {
        if (NotificationType.none == this.eventType || msg.getType() == this.eventType) {
            if (msg.getSeverity().getLevel() >= this.eventSeverity.getLevel()) {
                if (hasValidNSPath(msg.getNsPath())) {
                    if (allowCloud(msg.getCloudName())) {
                        if (isNotEmpty(envProfilePattern)) {
                            String envProfile = msg.getEnvironmentProfileName();
                            // ProfilePattern regex match is Case-Insensitive.
                            return envProfile != null && envProfile.matches("(?i)" + envProfilePattern);
                        } else {
                            // Pass through all messages because env profile pattern is empty.
                            return true;
                        }
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
                NotificationFilter filter = new NotificationFilter()
                        .eventType(eventType)
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


    /**
     * Checks whether or not the message nspath is a valid one for filtering.
     *
     * @param nsPath notification message nspath
     * @return <code>false</code> if it's need to be filtered, else return <code>true</code>
     */
    public boolean hasValidNSPath(String nsPath) {
        if (isNotEmpty(this.nsPaths)) {
            for (String nsp : nsPaths) {
                if (nsPath.startsWith(nsp)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Checks whether or not the given cloud is allowed for filtering.
     *
     * @param cloudName cloud name from ${@link NotificationMessage}
     * @return <code>true</code> if the cloud matches the one in the filter list
     * or if there are no cloud configured in the sink.
     */
    public boolean allowCloud(String cloudName) {
        if (isNotEmpty(this.clouds)) {
            for (String cloud : clouds) {
                if (cloudName != null && cloudName.matches(cloud)) {
                    return true;
                }
            }
            return false;
        }
        return true;
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

    public NotificationFilter envProfilePattern(String envProfilePattern) {
        this.envProfilePattern = envProfilePattern;
        return this;
    }

    public String envProfilePattern() {
        return this.envProfilePattern;
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
        String sb = "NotificationFilter{" + "eventType=" + eventType +
                ", eventSeverity=" + eventSeverity +
                ", clouds=" + Arrays.toString(clouds) +
                ", nsPaths=" + Arrays.toString(nsPaths) +
                ", selectorPattern='" + selectorPattern + '\'' +
                ", envProfilePattern='" + envProfilePattern + '\'' +
                '}';
        return sb;
    }
}

