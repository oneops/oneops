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
package com.oneops.cms.md.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * The Class CmsRelationAttribute.
 */
public class CmsRelationAttribute implements Serializable {

  private static final long serialVersionUID = 1L;

  private int attributeId;
  private int relationId;
  private String attributeName;
  private String dataType;
  private boolean isMandatory;
  private boolean isEncrypted;
  private String defaultValue;
  private String valueFormat;
  private String description;
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
   * Gets the data type.
   *
   * @return the data type
   */
  public String getDataType() {
    return dataType;
  }

  /**
   * Sets the data type.
   *
   * @param dataType the new data type
   */
  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  /**
   * Checks if is mandatory.
   *
   * @return true, if is mandatory
   */
  public boolean isMandatory() {
    return isMandatory;
  }

  /**
   * Sets the mandatory.
   *
   * @param isMandatory the new mandatory
   */
  public void setMandatory(boolean isMandatory) {
    this.isMandatory = isMandatory;
  }

  /**
   * Gets the checks if is mandatory.
   *
   * @return the checks if is mandatory
   */
  public boolean getIsMandatory() {
    return isMandatory;
  }

  /**
   * Sets the checks if is mandatory.
   *
   * @param isMandatory the new checks if is mandatory
   */
  public void setIsMandatory(boolean isMandatory) {
    this.isMandatory = isMandatory;
  }

  /**
   * Gets the checks if is encrypted.
   *
   * @return the checks if is encrypted
   */
  public boolean getIsEncrypted() {
    return isEncrypted;
  }

  /**
   * Checks if is encrypted.
   *
   * @return true, if is encrypted
   */
  public boolean isEncrypted() {
    return isEncrypted;
  }

  /**
   * Sets the encrypted.
   *
   * @param isEncrypted the new encrypted
   */
  public void setEncrypted(boolean isEncrypted) {
    this.isEncrypted = isEncrypted;
  }

  /**
   * Gets the default value.
   *
   * @return the default value
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * Sets the default value.
   *
   * @param defaultValue the new default value
   */
  public void setDefaultValue(String defaultValue) {
    if (defaultValue != null && defaultValue.length() == 0) {
      defaultValue = null;
    }
    this.defaultValue = defaultValue;
  }

  /**
   * Gets the value format.
   *
   * @return the value format
   */
  public String getValueFormat() {
    return valueFormat;
  }

  /**
   * Sets the value format.
   *
   * @param valueFormat the new value format
   */
  public void setValueFormat(String valueFormat) {
    this.valueFormat = valueFormat;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description the new description
   */
  public void setDescription(String description) {
    this.description = description;
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

}
