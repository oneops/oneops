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
package com.oneops.cms.collections.def;

import java.util.List;


/**
 * The Class BasicLinkDefinition.
 */
public class BasicLinkDefinition {

  private String relationName;
  private String relationShortName;
  private String direction;
  private String targetClassName;
  private String targetCiName;
  private List<Long> targetIds;

  /**
   * Gets the target ids.
   *
   * @return the target ids
   */
  public List<Long> getTargetIds() {
    return targetIds;
  }

  /**
   * Sets the target ids.
   *
   * @param targetIds the new target ids
   */
  public void setTargetIds(List<Long> targetIds) {
    this.targetIds = targetIds;
  }

  /**
   * Gets the relation name.
   *
   * @return the relation name
   */
  public String getRelationName() {
    return relationName;
  }

  /**
   * Sets the relation name.
   *
   * @param relationName the new relation name
   */
  public void setRelationName(String relationName) {
    this.relationName = relationName;
  }

  /**
   * Gets the relation short name.
   *
   * @return the relation short name
   */
  public String getRelationShortName() {
    return relationShortName;
  }

  /**
   * Sets the relation short name.
   *
   * @param relationShortName the new relation short name
   */
  public void setRelationShortName(String relationShortName) {
    this.relationShortName = relationShortName;
  }

  /**
   * Gets the direction.
   *
   * @return the direction
   */
  public String getDirection() {
    return direction;
  }

  /**
   * Sets the direction.
   *
   * @param direction the new direction
   */
  public void setDirection(String direction) {
    this.direction = direction;
  }

  /**
   * Gets the target class name.
   *
   * @return the target class name
   */
  public String getTargetClassName() {
    return targetClassName;
  }

  /**
   * Sets the target class name.
   *
   * @param targetClassName the new target class name
   */
  public void setTargetClassName(String targetClassName) {
    this.targetClassName = targetClassName;
  }

  /**
   * Gets the target ci name.
   *
   * @return the target ci name
   */
  public String getTargetCiName() {
    return targetCiName;
  }

  /**
   * Sets the target ci name.
   *
   * @param targetCiName the new target ci name
   */
  public void setTargetCiName(String targetCiName) {
    this.targetCiName = targetCiName;
  }

}
