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
import java.util.Date;

/**
 * The Class CmsRfcBasicAttribute.
 */
public class CmsRfcBasicAttribute implements Serializable {

  private static final long serialVersionUID = 1L;

  private int attributeId;
  private String attributeName;
  private String oldValue;
  private String newValue;
  private String comments;

  private String owner;
  private Date created;

  /**
   * Default constructor.
   */
  public CmsRfcBasicAttribute() {
  }

  /**
   * Convenience constructor for a typical operation of adding a RFC attribute specified by name
   * with a given new value.
   */
  public CmsRfcBasicAttribute(String attributeName, String newValue) {
    this.attributeName = attributeName;
    this.newValue = newValue;
  }

  /**
   * Convenience constructor for a typical operation of adding a RFC attribute specified by name
   * with given new and old values.
   */
  public CmsRfcBasicAttribute(String attributeName, String newValue, String oldValue) {
    this.attributeName = attributeName;
    this.newValue = newValue;
    this.oldValue = oldValue;
  }

  /**
   * Convenience constructor for a typical operation of adding a RFC attribute specified by name.
   */
  CmsRfcBasicAttribute(String attributeName) {
    this.attributeName = attributeName;
  }

  /**
   * Gets the attribute id.
   *
   * @return the attribute id
   */
  public int getAttributeId() {
    return attributeId;
  }

  /**
   * Sets the attribute id.
   *
   * @param attributeId the new attribute id
   */
  public void setAttributeId(int attributeId) {
    this.attributeId = attributeId;
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
   * Gets the old value.
   *
   * @return the old value
   */
  public String getOldValue() {
    return oldValue;
  }

  /**
   * Sets the old value.
   *
   * @param oldValue the new old value
   */
  public void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }

  /**
   * Gets the new value.
   *
   * @return the new value
   */
  public String getNewValue() {
    return newValue;
  }

  /**
   * Sets the new value.
   *
   * @param newValue the new new value
   */
  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  /**
   * Gets the comments.
   *
   * @return the comments
   */
  public String getComments() {
    return comments;
  }

  /**
   * Sets the comments.
   *
   * @param comments the new comments
   */
  public void setComments(String comments) {
    this.comments = comments;
  }

  /**
   * Gets the created.
   *
   * @return the created
   */
  public Date getCreated() {
    return created;
  }

  /**
   * Sets the created.
   *
   * @param created the new created
   */
  public void setCreated(Date created) {
    this.created = created;
  }

  /**
   * Gets the owner.
   *
   * @return the owner
   */
  public String getOwner() {
    return owner;
  }

  /**
   * Sets the owner.
   *
   * @param owner the new owner
   */
  public void setOwner(String owner) {
    this.owner = owner;
  }

}
