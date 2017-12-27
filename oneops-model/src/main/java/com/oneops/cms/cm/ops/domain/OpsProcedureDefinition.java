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
import java.util.List;

/**
 * The Class OpsProcedureDefinition.
 */
public class OpsProcedureDefinition implements Serializable {

  private static final long serialVersionUID = 1L;
  private String name;
  private List<OpsProcedureFlow> flow;
  private String execStrategy;
  private List<OpsFlowAction> actions = new ArrayList<OpsFlowAction>();

  /**
   * Gets the exec strategy.
   *
   * @return the exec strategy
   */
  public String getExecStrategy() {
    return execStrategy;
  }

  /**
   * Sets the exec strategy.
   *
   * @param execStrategy the new exec strategy
   */
  public void setExecStrategy(String execStrategy) {
    this.execStrategy = execStrategy;
  }

  /**
   * Gets the flow.
   *
   * @return the flow
   */
  public List<OpsProcedureFlow> getFlow() {
    return flow;
  }

  /**
   * Sets the flow.
   *
   * @param flow the new flow
   */
  public void setFlow(List<OpsProcedureFlow> flow) {
    this.flow = flow;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the actions.
   *
   * @return the actions
   */
  public List<OpsFlowAction> getActions() {
    return actions;
  }

  /**
   * Sets the actions.
   *
   * @param actions the new actions
   */
  public void setActions(List<OpsFlowAction> actions) {
    this.actions = actions;
  }

}
