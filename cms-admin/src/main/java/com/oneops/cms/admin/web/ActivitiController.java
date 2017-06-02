/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
package com.oneops.cms.admin.web;

import org.activiti.engine.*;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


//Disabling activiti admin configuration from admin not used..  @Controller
public class ActivitiController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;


    @RequestMapping(value="/activiti/deployments.do", method = RequestMethod.GET)
    public void process1list(Model model) {
        model.addAttribute("deployments", repositoryService.createDeploymentQuery().list());
        model.addAttribute("definitions", repositoryService.createProcessDefinitionQuery().list());
        model.addAttribute("processes", runtimeService.createProcessInstanceQuery().list());
    }

    @RequestMapping(value="/activiti/processList.do", method = RequestMethod.GET)
    public void processlist(Model model) {
        model.addAttribute("unfinishedProcesses", historyService.createHistoricProcessInstanceQuery().
                unfinished().orderByProcessInstanceStartTime().asc().list());
        model.addAttribute("finishedProcesses", historyService.createHistoricProcessInstanceQuery().
                finished().orderByProcessInstanceStartTime().asc().list());
    }

    @RequestMapping(value="/activiti/activityList.do", method = RequestMethod.GET)
    public void activitylist(Model model,
                             @RequestParam(value="processInstanceId", required = true) String processInstanceId) {
        model.addAttribute("process",historyService.createHistoricProcessInstanceQuery().
                processInstanceId(processInstanceId).singleResult());
        model.addAttribute("unfinishedActivities", historyService.createHistoricActivityInstanceQuery().
                unfinished().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list());
        model.addAttribute("finishedActivities", historyService.createHistoricActivityInstanceQuery().
                finished().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list());
    }

    @RequestMapping(value="/activiti/kill.do", method = RequestMethod.GET)
    public void killProcess(@RequestParam(value="processInstanceId", required = true) String processInstanceId) {
        this.runtimeService.deleteProcessInstance(processInstanceId, "Finish by admin.");
    }

}
