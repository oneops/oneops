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
 * The Class CmsCI.
 */
public class CmsCI extends CmsCIBasic implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Ci class id of the metadata class (md_classes)
   */
  private int ciClassId;

  /**
   * Namespace id (ns_namespaces)
   */
  private long nsId;

  /**
   * CI state id (cm_ci_state). The possible states are,
   * <ul>
   * <li>default
   * <li>pending_deletion
   * <li>replace
   * <li>locked
   * </ul>
   */
  private int ciStateId;

  private Map<String, CmsCIAttribute> attributes = new HashMap<String, CmsCIAttribute>();

  /**
   * Gets the ci class id.
   *
   * @return the ci class id
   */
  public int getCiClassId() {
    return ciClassId;
  }

  /**
   * Sets the ci class id.
   *
   * @param ciClassId the new ci class id
   */
  public void setCiClassId(int ciClassId) {
    this.ciClassId = ciClassId;
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

  /**
   * Gets the ci state id.
   *
   * @return the ci state id
   */
  public int getCiStateId() {
    return ciStateId;
  }

  /**
   * Sets the ci state id.
   *
   * @param ciStateId the new ci state id
   */
  public void setCiStateId(int ciStateId) {
    this.ciStateId = ciStateId;
  }

  /**
   * Gets the attributes.
   *
   * @return the attributes
   */
  public Map<String, CmsCIAttribute> getAttributes() {
    return attributes;
  }

  /**
   * Sets the attributes.
   *
   * @param attributes the attributes
   */
  public void setAttributes(Map<String, CmsCIAttribute> attributes) {
    this.attributes = attributes;
  }

  /**
   * Gets the attribute.
   *
   * @param attrName the attr name
   * @return the attribute
   */
  public CmsCIAttribute getAttribute(String attrName) {
    return attributes.get(attrName);
  }

  /**
   * Adds the attribute.
   *
   * @param attribute the attribute
   */
  public void addAttribute(CmsCIAttribute attribute) {
    this.attributes.put(attribute.getAttributeName(), attribute);
  }
}
