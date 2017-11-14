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

import com.oneops.cms.collections.def.BasicLinkDefinition;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class OpsProcedureFlow.
 */
public class OpsProcedureFlow extends BasicLinkDefinition implements Serializable {

  private static final long serialVersionUID = 1L;

  private String execStrategy;

  private List<OpsProcedureFlow> flow = new ArrayList<OpsProcedureFlow>();
  private List<OpsFlowAction> actions = new ArrayList<OpsFlowAction>();
  private String nsPath;

  public String getNsPath() {
    return nsPath;
  }

  public void setNsPath(String nsPath) {
    this.nsPath = nsPath;
  }

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
