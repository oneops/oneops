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
package com.oneops.cms.util.domain;

/**
 * The Class AttrQueryCondition.
 */
public class AttrQueryCondition {

  private String attributeName;
  private String condition;
  private String avalue;

  public AttrQueryCondition() {}

  public AttrQueryCondition(String attributeName, String condition, String avalue) {
    this.attributeName = attributeName;
    this.condition = condition;
    this.avalue = avalue;
  }

  /**
   * Gets the attribute name.
   *
   * @return the attribute name
   */
  public String getAttributeName() {
    return attributeName;
  }

  /**
   * Sets the attribute name.
   *
   * @param attributeName the new attribute name
   */
  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  /**
   * Gets the condition.
   *
   * @return the condition
   */
  public String getCondition() {
    return condition;
  }

  /**
   * Sets the condition.
   *
   * @param condition the new condition
   */
  public void setCondition(String condition) {
    this.condition = condition;
  }

  /**
   * Gets the avalue.
   *
   * @return the avalue
   */
  public String getAvalue() {
    return avalue;
  }

  public long getAvalueAsLong() {
    return Long.parseLong(avalue);
  }

  /**
   * Sets the avalue.
   *
   * @param avalue the new avalue
   */
  public void setAvalue(String avalue) {
    this.avalue = avalue;
  }

}
