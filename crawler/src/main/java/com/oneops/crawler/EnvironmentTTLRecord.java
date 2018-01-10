package com.oneops.crawler;

import com.oneops.Environment;

import java.io.Serializable;
import java.util.Date;

public class EnvironmentTTLRecord implements Serializable {
    Environment environment;
    Date lastProcessedAt;
    boolean scanOnly;
    int userNotifiedTimes;
    Date plannedDestroyDate;

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Date getLastProcessedAt() {
        return lastProcessedAt;
    }

    public void setLastProcessedAt(Date lastProcessedAt) {
        this.lastProcessedAt = lastProcessedAt;
    }

    public boolean isScanOnly() {
        return scanOnly;
    }

    public void setScanOnly(boolean scanOnly) {
        this.scanOnly = scanOnly;
    }

    public int getUserNotifiedTimes() {
        return userNotifiedTimes;
    }

    public void setUserNotifiedTimes(int userNotifiedTimes) {
        this.userNotifiedTimes = userNotifiedTimes;
    }

    public Date getPlannedDestroyDate() {
        return plannedDestroyDate;
    }

    public void setPlannedDestroyDate(Date plannedDestroyDate) {
        this.plannedDestroyDate = plannedDestroyDate;
    }
}
