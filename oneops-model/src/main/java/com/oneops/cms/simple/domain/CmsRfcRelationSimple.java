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


import com.oneops.cms.dj.domain.CmsRfcRelationBasic;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class CmsRfcRelationSimple.
 */
public class CmsRfcRelationSimple extends CmsRfcRelationBasic implements Serializable {

  private static final long serialVersionUID = 1L;

  private CmsRfcCISimple fromCi;
  private CmsRfcCISimple toCi;

  private Map<String, String> relationAttributes = new HashMap<String, String>();
  private Map<String, String> relationBaseAttributes = new HashMap<String, String>();
  private Map<String, Map<String, String>> relationAttrProps = new HashMap<String, Map<String, String>>();

  /**
   * Gets the relation attributes.
   *
   * @return the relation attributes
   */
  public Map<String, String> getRelationAttributes() {
    return relationAttributes;
  }

  /**
   * Sets the relation attributes.
   *
   * @param relationAttributes the relation attributes
   */
  public void setRelationAttributes(Map<String, String> relationAttributes) {
    this.relationAttributes = relationAttributes;
  }

  /**
   * Adds the relation attribute.
   *
   * @param key the key
   * @param value the value
   */
  public void addRelationAttribute(String key, String value) {
    this.relationAttributes.put(key, value);
  }

  /**
   * Gets the from ci.
   *
   * @return the from ci
   */
  public CmsRfcCISimple getFromCi() {
    return fromCi;
  }

  /**
   * Sets the from ci.
   *
   * @param fromCi the new from ci
   */
  public void setFromCi(CmsRfcCISimple fromCi) {
    this.fromCi = fromCi;
  }

  /**
   * Gets the to ci.
   *
   * @return the to ci
   */
  public CmsRfcCISimple getToCi() {
    return toCi;
  }

  /**
   * Sets the to ci.
   *
   * @param toCi the new to ci
   */
  public void setToCi(CmsRfcCISimple toCi) {
    this.toCi = toCi;
  }

  /**
   * Gets the relation base attributes.
   *
   * @return the relation base attributes
   */
  public Map<String, String> getRelationBaseAttributes() {
    return relationBaseAttributes;
  }

  /**
   * Sets the relation base attributes.
   *
   * @param relationBaseAttributes the relation base attributes
   */
  public void setRelationBaseAttributes(Map<String, String> relationBaseAttributes) {
    this.relationBaseAttributes = relationBaseAttributes;
  }

  /**
   * Adds the relation base attribute.
   *
   * @param key the key
   * @param value the value
   */
  public void addRelationBaseAttribute(String key, String value) {
    this.relationBaseAttributes.put(key, value);
  }

  /**
   * Gets the relation attr props.
   *
   * @return the relation attr props
   */
  public Map<String, Map<String, String>> getRelationAttrProps() {
    return relationAttrProps;
  }

  /**
   * Sets the relation attr props.
   *
   * @param relationAttrProps the relation attr props
   */
  public void setRelationAttrProps(Map<String, Map<String, String>> relationAttrProps) {
    this.relationAttrProps = relationAttrProps;
  }

  /**
   * Adds the relation attr prop.
   *
   * @param propName the prop name
   * @param attrName the attr name
   * @param value the value
   */
  public void addRelationAttrProp(String propName, String attrName, String value) {
    if (relationAttrProps.get(propName) == null) {
      relationAttrProps.put(propName, new HashMap<String, String>());
    }
    if (attrName != null && value != null) {
      relationAttrProps.get(propName).put(attrName, value);
    }
  }

}
