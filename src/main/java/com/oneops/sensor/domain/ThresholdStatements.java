package com.oneops.sensor.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The Class ThresholdStatements.
 */
public class ThresholdStatements {

    private long checksum;
    private boolean isHeartbeat;
    private String hbDuration;
    private Map<String, SensorStatement> statements = new HashMap<>();

    /**
     * Gets the checksum.
     *
     * @return the checksum
     */
    public long getChecksum() {
        return checksum;
    }

    /**
     * Sets the checksum.
     *
     * @param checksum the new checksum
     */
    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }

    /**
     * Gets the stmt names.
     *
     * @return the stmt names
     */
    public Set<String> getStmtNames() {
        return statements.keySet();
    }

    /**
     * Sets the heartbeat.
     *
     * @param isHeartbeat the new heartbeat
     */
    public void setHeartbeat(boolean isHeartbeat) {
        this.isHeartbeat = isHeartbeat;
    }

    /**
     * Checks if is heartbeat.
     *
     * @return true, if is heartbeat
     */
    public boolean isHeartbeat() {
        return isHeartbeat;
    }

    /**
     * Sets the hb duration.
     *
     * @param hbDuration the new hb duration
     */
    public void setHbDuration(String hbDuration) {
        this.hbDuration = hbDuration;
    }

    /**
     * Gets the hb duration.
     *
     * @return the hb duration
     */
    public String getHbDuration() {
        return hbDuration;
    }

    /**
     * Adds the statement.
     *
     * @param statement the statement
     */
    public void addStatement(SensorStatement statement) {
        this.statements.put(statement.getStmtName(), statement);
    }

    /**
     * Gets the statements.
     *
     * @return the statements
     */
    public Map<String, SensorStatement> getStatements() {
        return statements;
    }

}
