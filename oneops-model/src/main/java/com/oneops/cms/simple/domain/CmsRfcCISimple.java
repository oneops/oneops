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


import com.oneops.cms.dj.domain.CmsRfcCIBasic;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class CmsRfcCISimple.
 */
public class CmsRfcCISimple extends CmsRfcCIBasic implements Serializable, Instance {

  private static final long serialVersionUID = 1L;

  private Map<String, String> ciAttributes = new HashMap<String, String>();
  private Map<String, String> ciBaseAttributes = new HashMap<String, String>();
  private Map<String, Map<String, String>> ciAttrProps = new HashMap<String, Map<String, String>>();

  @Override
public String toString() {
	return "ciAttributes=" + ciAttributes + ", ciBaseAttributes=" + ciBaseAttributes + ", ciAttrProps="
			+ ciAttrProps + ", " + super.toString() + "]";
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
   * Gets the ci base attributes.
   *
   * @return the ci base attributes
   */
  public Map<String, String> getCiBaseAttributes() {
    return ciBaseAttributes;
  }

  /**
   * Sets the ci base attributes.
   *
   * @param ciBaseAttributes the ci base attributes
   */
  public void setCiBaseAttributes(Map<String, String> ciBaseAttributes) {
    this.ciBaseAttributes = ciBaseAttributes;
  }

  /**
   * Adds the ci base attribute.
   *
   * @param key the key
   * @param value the value
   */
  public void addCiBaseAttribute(String key, String value) {
    this.ciBaseAttributes.put(key, value);
  }

  /**
   * Gets the ci attr props.
   *
   * @return the ci attr props
   */
  public Map<String, Map<String, String>> getCiAttrProps() {
    return ciAttrProps;
  }

  /**
   * Sets the ci attr props.
   *
   * @param ciAttrProps the ci attr props
   */
  public void setCiAttrProps(Map<String, Map<String, String>> ciAttrProps) {
    this.ciAttrProps = ciAttrProps;
  }

  /**
   * Adds the ci attr prop.
   *
   * @param propName the prop name
   * @param attrName the attr name
   * @param value the value
   */
  public void addCiAttrProp(String propName, String attrName, String value) {
    if (ciAttrProps.get(propName) == null) {
      ciAttrProps.put(propName, new HashMap<String, String>());
    }
    if (attrName != null && value != null) {
      ciAttrProps.get(propName).put(attrName, value);
    }
  }

}
