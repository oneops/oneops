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
package com.oneops.cms.cm.ops.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * The Class CmsOpsAction.
 */
public class CmsOpsAction implements Serializable {

  private static final long serialVersionUID = 1L;

  private long actionId;
  private long procId;
  private String actionName;
  private long ciId;
  private OpsActionState actionState;
  private int execOrder;
  private boolean isCritical;
  private String extraInfo;
  private String arglist;
  private String payLoadDef;
  //created by 
  private String createdBy;
  private Date created;
  private Date updated;

  /**
   * Gets the action id.
   *
   * @return the action id
   */
  public long getActionId() {
    return actionId;
  }

  /**
   * Sets the action id.
   *
   * @param actionId the new action id
   */
  public void setActionId(long actionId) {
    this.actionId = actionId;
  }

  /**
   * Gets the procedure id.
   *
   * @return the procedure id
   */
  public long getProcedureId() {
    return procId;
  }

  /**
   * Sets the procedure id.
   *
   * @param procId the new procedure id
   */
  public void setProcedureId(long procId) {
    this.procId = procId;
  }

  /**
   * Gets the action name.
   *
   * @return the action name
   */
  public String getActionName() {
    return actionName;
  }

  /**
   * Sets the action name.
   *
   * @param actionName the new action name
   */
  public void setActionName(String actionName) {
    this.actionName = actionName;
  }

  /**
   * Gets the ci id.
   *
   * @return the ci id
   */
  public long getCiId() {
    return ciId;
  }

  /**
   * Sets the ci id.
   *
   * @param ciId the new ci id
   */
  public void setCiId(long ciId) {
    this.ciId = ciId;
  }

  /**
   * Gets the action state.
   *
   * @return the action state
   */
  public OpsActionState getActionState() {
    return actionState;
  }

  /**
   * Sets the action state.
   *
   * @param actionState the new action state
   */
  public void setActionState(OpsActionState actionState) {
    this.actionState = actionState;
  }

  /**
   * Gets the exec order.
   *
   * @return the exec order
   */
  public int getExecOrder() {
    return execOrder;
  }

  /**
   * Sets the exec order.
   *
   * @param execOrder the new exec order
   */
  public void setExecOrder(int execOrder) {
    this.execOrder = execOrder;
  }

  /**
   * Gets the checks if is critical.
   *
   * @return the checks if is critical
   */
  public boolean getIsCritical() {
    return isCritical;
  }

  /**
   * Sets the checks if is critical.
   *
   * @param critical the new checks if is critical
   */
  public void setIsCritical(boolean critical) {
    this.isCritical = critical;
  }

  /**
   * Gets the extra info.
   *
   * @return the extra info
   */
  public String getExtraInfo() {
    return extraInfo;
  }

  /**
   * Sets the extra info.
   *
   * @param extraInfo the new extra info
   */
  public void setExtraInfo(String extraInfo) {
    this.extraInfo = extraInfo;
  }

  /**
   * Gets the created.
   *
   * @return the created
   */
  public Date getCreated() {
    return created;
  }

  /**
   * Sets the created.
   *
   * @param created the new created
   */
  public void setCreated(Date created) {
    this.created = created;
  }

  /**
   * Gets the updated.
   *
   * @return the updated
   */
  public Date getUpdated() {
    return updated;
  }

  /**
   * Sets the updated.
   *
   * @param updated the new updated
   */
  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  /**
   * Gets the arglist.
   *
   * @return the arglist
   */
  public String getArglist() {
    return arglist;
  }

  /**
   * Sets the arglist.
   *
   * @param arglist the new arglist
   */
  public void setArglist(String arglist) {
    this.arglist = arglist;
  }

  /**
   * Gets the pay load def.
   *
   * @return the pay load def
   */
  public String getPayLoadDef() {
    return payLoadDef;
  }

  /**
   * Sets the pay load def.
   *
   * @param payLoadDef the new pay load def
   */
  public void setPayLoadDef(String payLoadDef) {
    this.payLoadDef = payLoadDef;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

}
