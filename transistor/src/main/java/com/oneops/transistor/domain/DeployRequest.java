package com.oneops.transistor.domain;

import com.oneops.cms.dj.domain.CmsDeployment;

import java.io.Serializable;

public class DeployRequest implements Serializable {
    private String exclude;
    private CmsDeployment deployment;

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    public CmsDeployment getDeployment() {
        return deployment;
    }

    public void setDeployment(CmsDeployment deployment) {
        this.deployment = deployment;
    }
}
