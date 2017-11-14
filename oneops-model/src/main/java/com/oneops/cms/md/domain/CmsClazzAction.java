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
package com.oneops.cms.md.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * The Class CmsClazzAction.
 */
public class CmsClazzAction implements Serializable {

  private static final long serialVersionUID = 1L;

  private int actionId;
  private int classId;
  private String actionName;
  private boolean isInheritable;
  private String description;
  private Date created;
  private Date updated;
  private boolean isInherited;
  private String inheritedFrom;
  private String arguments;

  public String getArguments() {
    return arguments;
  }

  public void setArguments(String arguments) {
    this.arguments = arguments;
  }

  /**
   * Gets the action id.
   *
   * @return the action id
   */
  public int getActionId() {
    return actionId;
  }

  /**
   * Sets the action id.
   *
   * @param actionId the new action id
   */
  public void setActionId(int actionId) {
    this.actionId = actionId;
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
   * Gets the class id.
   *
   * @return the class id
   */
  public int getClassId() {
    return classId;
  }

  /**
   * Sets the class id.
   *
   * @param classId the new class id
   */
  public void setClassId(int classId) {
    this.classId = classId;
  }

  /**
   * Gets the checks if is inheritable.
   *
   * @return the checks if is inheritable
   */
  public boolean getIsInheritable() {
    return isInheritable;
  }

  /**
   * Sets the checks if is inheritable.
   *
   * @param inheritable the new checks if is inheritable
   */
  public void setIsInheritable(boolean inheritable) {
    isInheritable = inheritable;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description the new description
   */
  public void setDescription(String description) {
    this.description = description;
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
   * Checks if is inherited.
   *
   * @return true, if is inherited
   */
  public boolean isInherited() {
    return isInherited;
  }

  /**
   * Sets the inherited.
   *
   * @param inherited the new inherited
   */
  public void setInherited(boolean inherited) {
    isInherited = inherited;
  }

  /**
   * Gets the inherited from.
   *
   * @return the inherited from
   */
  public String getInheritedFrom() {
    return inheritedFrom;
  }

  /**
   * Sets the inherited from.
   *
   * @param inheritedFrom the new inherited from
   */
  public void setInheritedFrom(String inheritedFrom) {
    this.inheritedFrom = inheritedFrom;
  }

  public boolean isInheritable() {
    return isInheritable;
  }

  public void setInheritable(boolean inheritable) {
    isInheritable = inheritable;
  }
}
