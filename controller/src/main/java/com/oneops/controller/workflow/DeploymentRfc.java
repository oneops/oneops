package com.oneops.controller.workflow;

import java.io.Serializable;

public class DeploymentRfc implements Serializable {

    private long rfcId;

    private String state;

    private int step;

    private int batch;

    public DeploymentRfc(long rfcId, String state, int step, int batch) {
        this.rfcId = rfcId;
        this.state = state;
        this.step = step;
        this.batch = batch;
    }

    public long getRfcId() {
        return rfcId;
    }

    public void setRfcId(long rfcId) {
        this.rfcId = rfcId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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
}
