package com.oneops.controller.workflow;

import java.io.Serializable;
import java.util.List;

public class DeploymentStep implements Serializable {

    private int step;
    private int batch;
    private boolean isCompleted;
    private int woScheduledCount;
    private int woExecutedCount;
    private int woFailedDispatchCount;
    private long scheduledTs;
    private List<Long> dpmtRecordIds;

    public DeploymentStep(int step, int batch, int woScheduledCount, long scheduledTs) {
        this.step = step;
        this.batch = batch;
        this.woScheduledCount = woScheduledCount;
        this.scheduledTs = scheduledTs;
        isCompleted = false;
    }

    public DeploymentStep(int step, int batch) {
        this(step, batch, 0, 0);
    }

    public DeploymentStep() {

    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getBatch() {
        return batch;
    }

    public void setBatch(int batch) {
        this.batch = batch;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getWoScheduledCount() {
        return woScheduledCount;
    }

    public void setWoScheduledCount(int woScheduledCount) {
        this.woScheduledCount = woScheduledCount;
    }

    public long getScheduledTs() {
        return scheduledTs;
    }

    public void setScheduledTs(long scheduledTs) {
        this.scheduledTs = scheduledTs;
    }

    public int getWoExecutedCount() {
        return woExecutedCount;
    }

    public void setWoExecutedCount(int woExecutedCount) {
        this.woExecutedCount = woExecutedCount;
    }

    public List<Long> getDpmtRecordIds() {
        return dpmtRecordIds;
    }

    public void setDpmtRecordIds(List<Long> dpmtRecordIds) {
        this.dpmtRecordIds = dpmtRecordIds;
    }

    public int getWoFailedDispatchCount() {
        return woFailedDispatchCount;
    }

    public void setWoFailedDispatchCount(int woFailedDispatchCount) {
        this.woFailedDispatchCount = woFailedDispatchCount;
    }
}
