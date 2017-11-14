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
import java.util.Map;

/**
 * The Class OpsFlowAction.
 */
public class OpsFlowAction implements Serializable {

  private static final long serialVersionUID = 1L;

  private String actionName;
  private int stepNumber;
  private boolean isCritical;
  private Map<String, String> argMap;
  private String extraInfo;

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
   * @param isCritical the new checks if is critical
   */
  public void setIsCritical(boolean isCritical) {
    this.isCritical = isCritical;
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
   * Gets the step number.
   *
   * @return the step number
   */
  public int getStepNumber() {
    return stepNumber;
  }

  /**
   * Sets the step number.
   *
   * @param stepNumber the new step number
   */
  public void setStepNumber(int stepNumber) {
    this.stepNumber = stepNumber;
  }

  /**
   * Gets the arg map.
   *
   * @return the arg map
   */
  public Map<String, String> getArgMap() {
    return argMap;
  }

  /**
   * Sets the arg map.
   *
   * @param argMap the arg map
   */
  public void setArgMap(Map<String, String> argMap) {
    this.argMap = argMap;
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

}
