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
    private NotificationSeverity(String name, int level) {
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
