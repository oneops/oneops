package com.oneops.workflow;

import java.io.Serializable;

public class WorkflowMessage implements Serializable {

    private long dpmtId;

    private boolean checkProcessDelay;

    public long getDpmtId() {
        return dpmtId;
    }

    public void setDpmtId(long dpmtId) {
        this.dpmtId = dpmtId;
    }

    public boolean isCheckProcessDelay() {
        return checkProcessDelay;
    }

    public void setCheckProcessDelay(boolean checkProcessDelay) {
        this.checkProcessDelay = checkProcessDelay;
    }
}
