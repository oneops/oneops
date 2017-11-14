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


import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;

/**
 * The Class CollectionRfcLink.
 */
public class CollectionRfcLink extends CmsRfcRelation {

  private static final long serialVersionUID = 1L;

  private CollectionRfcNode linkedNode;

  /**
   * Gets the linked node.
   *
   * @return the linked node
   */
  public CollectionRfcNode getLinkedNode() {
    return linkedNode;
  }

  /**
   * Sets the linked node.
   *
   * @param linkedNode the new linked node
   */
  public void setLinkedNode(CollectionRfcNode linkedNode) {
    this.linkedNode = linkedNode;
  }

  /**
   * Gets the to rfc ci.
   *
   * @return the to rfc ci
   */
  @Override
  public CmsRfcCI getToRfcCi() {
    return linkedNode;
  }

}
