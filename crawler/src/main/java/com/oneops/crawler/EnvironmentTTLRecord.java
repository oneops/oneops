package com.oneops.crawler;

import com.oneops.Environment;
import com.oneops.Platform;

import java.io.Serializable;
import java.util.Date;

public class EnvironmentTTLRecord implements Serializable {
    String environmentProfile;
    long environmentId;
    Platform platform;
    Date lastProcessedAt;
    boolean scanOnly;
    int userNotifiedTimes;
    Date plannedDestroyDate;
    Date actualDestroyDate;

    public String getEnvironmentProfile() {
        return environmentProfile;
    }

    public void setEnvironmentProfile(String environmentProfile) {
        this.environmentProfile = environmentProfile;
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

    public Date getActualDestroyDate() {
        return actualDestroyDate;
    }

    public void setActualDestroyDate(Date actualDestroyDate) {
        this.actualDestroyDate = actualDestroyDate;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public long getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(long environmentId) {
        this.environmentId = environmentId;
    }
}
