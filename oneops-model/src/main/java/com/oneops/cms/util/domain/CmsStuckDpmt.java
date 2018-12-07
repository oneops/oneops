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
package com.oneops.cms.util.domain;

import java.util.Date;

public class CmsStuckDpmt {

  private Long deploymentId;

  private Double stuckMinsBack;

  private Date stuckAt;

  private String path;

  public Long getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(Long deploymentId) {
    this.deploymentId = deploymentId;
  }

  public Double getStuckMinsBack() {
    return stuckMinsBack;
  }

  public void setStuckMinsBack(Double stuckMinsBack) {
    this.stuckMinsBack = stuckMinsBack;
  }

  public Date getStuckAt() {
    return stuckAt;
  }

  public void setStuckAt(Date stuckAt) {
    this.stuckAt = stuckAt;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }


}
