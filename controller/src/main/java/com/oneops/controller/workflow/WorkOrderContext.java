package com.oneops.controller.workflow;

import com.oneops.cms.simple.domain.CmsWorkOrderSimple;

public class WorkOrderContext {

    private int execOrder;
    private int batchNumber;
    private CmsWorkOrderSimple woSimple;
    private String woDispatchError;

    public WorkOrderContext(CmsWorkOrderSimple woSimple, int execOrder, int batchNumber) {
        this.woSimple = woSimple;
        this.execOrder = execOrder;
        this.batchNumber = batchNumber;
    }

    public int getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(int batchNumber) {
        this.batchNumber = batchNumber;
    }

    public CmsWorkOrderSimple getWoSimple() {
        return woSimple;
    }

    public void setWoSimple(CmsWorkOrderSimple woSimple) {
        this.woSimple = woSimple;
    }

    public int getExecOrder() {

        return execOrder;
    }

    public void setExecOrder(int execOrder) {
        this.execOrder = execOrder;
    }

    public String getWoDispatchError() {
        return woDispatchError;
    }

    public void setWoDispatchError(String woDispatchError) {
        this.woDispatchError = woDispatchError;
    }
}
