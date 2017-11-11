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
package com.oneops.cms.cm.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class CmsCIRelation.
 */
public class CmsCIRelation extends CmsCIRelationBasic implements Serializable {

  private static final long serialVersionUID = 1L;

  private int relationId;
  private int relationStateId;
  private long nsId;
  private CmsCI fromCi;
  private CmsCI toCi;
  private Map<String, CmsCIRelationAttribute> attributes = new HashMap<String, CmsCIRelationAttribute>();

  /**
   * Gets the relation id.
   *
   * @return the relation id
   */
  public int getRelationId() {
    return relationId;
  }

  /**
   * Sets the relation id.
   *
   * @param relationId the new relation id
   */
  public void setRelationId(int relationId) {
    this.relationId = relationId;
  }

  /**
   * Gets the relation state id.
   *
   * @return the relation state id
   */
  public int getRelationStateId() {
    return relationStateId;
  }

  /**
   * Sets the relation state id.
   *
   * @param relationStateId the new relation state id
   */
  public void setRelationStateId(int relationStateId) {
    this.relationStateId = relationStateId;
  }

  /**
   * Gets the attributes.
   *
   * @return the attributes
   */
  public Map<String, CmsCIRelationAttribute> getAttributes() {
    return attributes;
  }

  /**
   * Sets the attributes.
   *
   * @param attributes the attributes
   */
  public void setAttributes(Map<String, CmsCIRelationAttribute> attributes) {
    this.attributes = attributes;
  }

  /**
   * Gets the attribute.
   *
   * @param attrName the attr name
   * @return the attribute
   */
  public CmsCIRelationAttribute getAttribute(String attrName) {
    return attributes.get(attrName);
  }

  /**
   * Adds the attribute.
   *
   * @param attribute the attribute
   */
  public void addAttribute(CmsCIRelationAttribute attribute) {
    this.attributes.put(attribute.getAttributeName(), attribute);
  }

  /**
   * Gets the from ci.
   *
   * @return the from ci
   */
  public CmsCI getFromCi() {
    return fromCi;
  }

  /**
   * Sets the from ci.
   *
   * @param fromCi the new from ci
   */
  public void setFromCi(CmsCI fromCi) {
    this.fromCi = fromCi;
  }

  /**
   * Gets the to ci.
   *
   * @return the to ci
   */
  public CmsCI getToCi() {
    return toCi;
  }

  /**
   * Sets the to ci.
   *
   * @param toCi the new to ci
   */
  public void setToCi(CmsCI toCi) {
    this.toCi = toCi;
  }

  /**
   * Gets the ns id.
   *
   * @return the ns id
   */
  public long getNsId() {
    return nsId;
  }

  /**
   * Sets the ns id.
   *
   * @param nsId the new ns id
   */
  public void setNsId(long nsId) {
    this.nsId = nsId;
  }

}
