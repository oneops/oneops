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
import java.util.Date;

/**
 * The Class CmsBasicAttribute.
 */
public class CmsBasicAttribute implements Serializable {

  private static final long serialVersionUID = 1L;

  private int attributeId;
  private String attributeName;
  private String dfValue;
  private String djValue;
  private String comments;
  private String owner;
  private Date created;
  private Date updated;

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
   * Gets the df value.
   *
   * @return the df value
   */
  public String getDfValue() {
    return dfValue;
  }

  /**
   * Sets the df value.
   *
   * @param dfValue the new df value
   */
  public void setDfValue(String dfValue) {
    this.dfValue = dfValue;
  }

  /**
   * Gets the dj value.
   *
   * @return the dj value
   */
  public String getDjValue() {
    return djValue;
  }

  /**
   * Sets the dj value.
   *
   * @param djValue the new dj value
   */
  public void setDjValue(String djValue) {
    this.djValue = djValue;
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
   * Gets the updated.
   *
   * @return the updated
   */
  public Date getUpdated() {
    return updated;
  }

  /**
   * Sets the updated.
   *
   * @param updated the new updated
   */
  public void setUpdated(Date updated) {
    this.updated = updated;
  }

@Override
public String toString() {
	return "attributeId=" + attributeId + ", attributeName=" + attributeName + ", dfValue=" + dfValue
			;
}
  
  
}
