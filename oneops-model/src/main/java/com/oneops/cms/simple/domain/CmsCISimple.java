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
package com.oneops.cms.simple.domain;

import com.oneops.cms.cm.domain.CmsCIBasic;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The Class CmsCISimple.
 */
public class CmsCISimple extends CmsCIBasic implements Serializable {

  private static final long serialVersionUID = 1L;

  private long nsId;
  private Map<String, String> ciAttributes = new HashMap<>();
  private Map<String, Map<String, String>> attrProps = new HashMap<>();
  private Map<String, Set<String>> altNs = new HashMap<>();


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
   * Gets the ci attributes.
   *
   * @return the ci attributes
   */
  public Map<String, String> getCiAttributes() {
    return ciAttributes;
  }

  /**
   * Sets the ci attributes.
   *
   * @param ciAttributes the ci attributes
   */
  public void setCiAttributes(Map<String, String> ciAttributes) {
    this.ciAttributes = ciAttributes;
  }

  /**
   * Adds the ci attribute.
   *
   * @param key the key
   * @param value the value
   */
  public void addCiAttribute(String key, String value) {
    this.ciAttributes.put(key, value);
  }

  /**
   * @return the attrProps
   */
  public Map<String, Map<String, String>> getAttrProps() {
    return attrProps;
  }

  /**
   * @param attrProps the attrProps to set
   */
  public void setAttrProps(Map<String, Map<String, String>> attrProps) {
    this.attrProps = attrProps;
  }


  /**
   * @return the altNs
   */
  public Map<String, Set<String>> getAltNs() {
    return altNs;
  }

  /**
   *
   */

  /**
   * @param altNs map to set
   */
  public void setAltNs(Map<String, Set<String>> altNs) {
    this.altNs = altNs;
  }

  /**
   * Adds the CI attribute property.
   *
   * @param attrName the attribute name
   * @param propName the prop name
   * @param value the value
   */
  public void addAttrProps(String propName, String attrName, String value) {
    if (attrProps.get(propName) == null) {
      attrProps.put(propName, new HashMap<String, String>());
    }
    if (attrName != null && value != null) {
      attrProps.get(propName).put(attrName, value);
    }
  }

  /**
   * Adds the CI attribute property.
   *
   * @param tag tag name
   * @param altNsPath alternative ns
   */
  public void addAltNs(String tag, String altNsPath) {
    altNs.computeIfAbsent(tag, k -> new HashSet<>());
    if (altNsPath != null) {
      altNs.get(tag).add(altNsPath);
    }
  }
}
