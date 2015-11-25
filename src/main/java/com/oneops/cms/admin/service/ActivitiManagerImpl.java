package com.oneops.cms.admin.service;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;

import java.util.List;

public class ActivitiManagerImpl implements ActivitiManager {

    private ProcessEngine processEngine;

    public ActivitiManagerImpl() {
        this.processEngine = ProcessEngines.getDefaultProcessEngine();
    }

    @Override
    public List getProcesses() {
        //this.processEngine;
        return null;
    }
}
