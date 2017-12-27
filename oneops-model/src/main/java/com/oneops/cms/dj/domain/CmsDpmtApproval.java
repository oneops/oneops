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
package com.oneops.cms.dj.domain;

import java.util.Date;

public class CmsDpmtApproval {

  private long approvalId;
  private long deploymentId;
  private long governCiId;
  private String governCiJson;
  private int stateId;
  private String state;
  private String updatedBy;
  private Date created;
  private Date updated;
  private int expiresIn = -1;
  private Boolean isExpired;
  private String comments;

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  public long getApprovalId() {
    return approvalId;
  }

  public void setApprovalId(long approvalId) {
    this.approvalId = approvalId;
  }

  public long getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(long deploymentId) {
    this.deploymentId = deploymentId;
  }

  public long getGovernCiId() {
    return governCiId;
  }

  public void setGovernCiId(long governCiId) {
    this.governCiId = governCiId;
  }

  public String getGovernCiJson() {
    return governCiJson;
  }

  public void setGovernCiJson(String governCiJson) {
    this.governCiJson = governCiJson;
  }

  public int getStateId() {
    return stateId;
  }

  public void setStateId(int stateId) {
    this.stateId = stateId;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public int getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(int expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public Boolean getIsExpired() {
    return isExpired;
  }

  public void setIsExpired(Boolean isExpired) {
    this.isExpired = isExpired;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

}
