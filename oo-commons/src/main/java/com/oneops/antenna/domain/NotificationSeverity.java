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
package com.oneops.antenna.domain;

/**
 * Notification message type severity
 */
public enum NotificationSeverity {

    /**
     * Critical event type
     */
    critical("critical", 3),
    /**
     * Warning event type
     */
    warning("warning", 2),
    /**
     * Info event type
     */
    info("info", 1),
    /**
     * None. Added for filtering
     */
    none("none", 0);

    /**
     * Severity name
     */
    private String name;

    /**
     * Severity log level
     */
    private int level;

    /**
     * Enum constructor
     *
     * @param name  severity name
     * @param level severity level
     */
    NotificationSeverity(String name, int level) {
        this.name = name;
        this.level = level;
    }

    /**
     * Returns the notification severity name
     *
     * @return severity name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns notification severity level
     *
     * @return severity level
     */
    public int getLevel() {
        return level;
    }
}
