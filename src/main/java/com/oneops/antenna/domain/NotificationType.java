package com.oneops.antenna.domain;

/**
 * Notification message types
 */
public enum NotificationType {

    /**
     * Config changes
     */
    ci("ci"),
    /**
     * Deployment events
     */
    deployment("deployment"),
    /**
     * Procedural action events
     */
    procedure("procedure"),

    /**
     * None. Added for filtering
     */
    none("none");


    /**
     * Event name
     */
    private String name;

    private NotificationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
