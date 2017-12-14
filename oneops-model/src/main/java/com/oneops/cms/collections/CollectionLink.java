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
package com.oneops.cms.collections;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;

/**
 * The Class CollectionLink.
 */
public class CollectionLink extends CmsCIRelation {

  private static final long serialVersionUID = -8581563728690157243L;
  private CollectionNode linkedNode;

  /**
   * Gets the linked node.
   *
   * @return the linked node
   */
  public CollectionNode getLinkedNode() {
    return linkedNode;
  }

  /**
   * Sets the linked node.
   *
   * @param linkedNode the new linked node
   */
  public void setLinkedNode(CollectionNode linkedNode) {
    this.linkedNode = linkedNode;
  }

  /**
   * Gets the to ci.
   *
   * @return the to ci
   */
  @Override
  public CmsCI getToCi() {
    return linkedNode;
  }
}
