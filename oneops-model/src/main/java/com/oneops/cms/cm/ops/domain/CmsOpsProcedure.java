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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Class CmsOpsProcedure.
 */
public class CmsOpsProcedure implements Serializable {

  private static final long serialVersionUID = 1L;

  private long procedureId;
  private String procedureName;
  private long ciId;
  private OpsProcedureState procedureState;
  private int maxExecOrder;
  private String arglist;
  private String createdBy;
  private String definition;
  private long procedureCiId;
  private Date created;
  private Date updated;
  private String nsPath;
  private Boolean forceExecution = Boolean.FALSE;

  private List<CmsOpsAction> actions = new ArrayList<CmsOpsAction>();

  /**
   * Gets the procedure name.
   *
   * @return the procedure name
   */
  public String getProcedureName() {
    return procedureName;
  }

  /**
   * Sets the procedure name.
   *
   * @param procedureName the new procedure name
   */
  public void setProcedureName(String procedureName) {
    this.procedureName = procedureName;
  }

  /**
   * Gets the procedure state.
   *
   * @return the procedure state
   */
  public OpsProcedureState getProcedureState() {
    return procedureState;
  }

  /**
   * Sets the procedure state.
   *
   * @param procedureState the new procedure state
   */
  public void setProcedureState(OpsProcedureState procedureState) {
    this.procedureState = procedureState;
  }

  /**
   * Gets the procedure ci id.
   *
   * @return the procedure ci id
   */
  public long getProcedureCiId() {
    return procedureCiId;
  }

  /**
   * Sets the procedure ci id.
   *
   * @param procedureCiId the new procedure ci id
   */
  public void setProcedureCiId(long procedureCiId) {
    this.procedureCiId = procedureCiId;
  }

  /**
   * Gets the procedure id.
   *
   * @return the procedure id
   */
  public long getProcedureId() {
    return procedureId;
  }

  /**
   * Sets the procedure id.
   *
   * @param procId the new procedure id
   */
  public void setProcedureId(long procId) {
    this.procedureId = procId;
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
   * Gets the actions.
   *
   * @return the actions
   */
  public List<CmsOpsAction> getActions() {
    return actions;
  }

  /**
   * Sets the actions.
   *
   * @param actions the new actions
   */
  public void setActions(List<CmsOpsAction> actions) {
    this.actions = actions;
  }

  /**
   * Adds the action.
   *
   * @param action the action
   */
  public void addAction(CmsOpsAction action) {
    this.actions.add(action);
  }

  /**
   * Gets the max exec order.
   *
   * @return the max exec order
   */
  public int getMaxExecOrder() {
    return maxExecOrder;
  }

  /**
   * Sets the max exec order.
   *
   * @param maxExecOrder the new max exec order
   */
  public void setMaxExecOrder(int maxExecOrder) {
    this.maxExecOrder = maxExecOrder;
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
   * Gets the created by.
   *
   * @return the created by
   */
  public String getCreatedBy() {
    return createdBy;
  }

  /**
   * Sets the created by.
   *
   * @param createdBy the new created by
   */
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   * Gets the definition.
   *
   * @return the definition
   */
  public String getDefinition() {
    return definition;
  }

  /**
   * Sets the definition.
   *
   * @param definition the new definition
   */
  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public String getNsPath() {
    return nsPath;
  }

  public void setNsPath(String nsPath) {
    this.nsPath = nsPath;
  }

  public Boolean getForceExecution() {
    return forceExecution;
  }

  public void setForceExecution(Boolean forceExecution) {
    this.forceExecution = forceExecution;
  }

}
