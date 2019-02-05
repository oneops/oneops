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
package com.oneops.cms.dj.domain;

import java.io.Serializable;

/**
 * The Class CmsRfcAttribute.
 */
public class CmsRfcAttribute extends CmsRfcBasicAttribute implements Serializable {

  private static final long serialVersionUID = 1L;

  private long rfcAttributeId;
  private long rfcId;

  /**
   * Default constructor.
   */
  public CmsRfcAttribute() {
  }

  /**
   * Convenience constructor for a typical operation of adding a RFC attribute specified by name.
   */
  public CmsRfcAttribute(String attributeName) {
    super(attributeName);
  }

  /**
   * Convenience constructor for a typical operation of adding a RFC attribute specified by name
   * with a given new value.
   */
  public CmsRfcAttribute(String attributeName, String newValue) {
    super(attributeName, newValue);
  }

  /**
   * Convenience constructor for a typical operation of adding a RFC attribute specified by name
   * with given new and old values.
   */
  public CmsRfcAttribute(String attributeName, String newValue, String oldValue) {
    super(attributeName, newValue, oldValue);
  }

  /**
   * Gets the rfc attribute id.
   *
   * @return the rfc attribute id
   */
  public long getRfcAttributeId() {
    return rfcAttributeId;
  }

  /**
   * Sets the rfc attribute id.
   *
   * @param rfcAttributeId the new rfc attribute id
   */
  public void setRfcAttributeId(long rfcAttributeId) {
    this.rfcAttributeId = rfcAttributeId;
  }

  /**
   * Gets the rfc id.
   *
   * @return the rfc id
   */
  public long getRfcId() {
    return rfcId;
  }

  /**
   * Sets the rfc id.
   *
   * @param rfcId the new rfc id
   */
  public void setRfcId(long rfcId) {
    this.rfcId = rfcId;
  }

@Override
public String toString() {
	return "[rfcAttributeId=" + rfcAttributeId + ", rfcId=" + rfcId + super.toString();
}

  
  
}
