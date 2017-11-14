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
 * The Class CmsClazzAttribute.
 */
public class CmsClazzAttribute implements Serializable {

  private static final long serialVersionUID = 1L;

  private int attributeId;
  private int classId;
  private String attributeName;
  private String dataType;
  private boolean isMandatory;
  private boolean isInheritable;
  private boolean isEncrypted;
  private boolean isImmutable;
  private boolean forceOnDependent;
  private String defaultValue;
  private String valueFormat;
  private String description;
  private Date created;
  private Date updated;
  private boolean isInherited;
  private String inheritedFrom;

  /**
   * Checks if is inherited.
   *
   * @return true, if is inherited
   */
  public boolean isInherited() {
    return isInherited;
  }

  /**
   * Sets the inherited.
   *
   * @param isInherited the new inherited
   */
  public void setInherited(boolean isInherited) {
    this.isInherited = isInherited;
  }

  /**
   * Gets the inherited from.
   *
   * @return the inherited from
   */
  public String getInheritedFrom() {
    return inheritedFrom;
  }

  /**
   * Sets the inherited from.
   *
   * @param inheritedFrom the new inherited from
   */
  public void setInheritedFrom(String inheritedFrom) {
    this.inheritedFrom = inheritedFrom;
  }

  /**
   * Gets the class id.
   *
   * @return the class id
   */
  public int getClassId() {
    return classId;
  }

  /**
   * Sets the class id.
   *
   * @param classId the new class id
   */
  public void setClassId(int classId) {
    this.classId = classId;
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
    this.attributeName = attributeName.toLowerCase();
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
    this.dataType = dataType.toLowerCase();
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
   * Gets the checks if is inheritable.
   *
   * @return the checks if is inheritable
   */
  public boolean getIsInheritable() {
    return isInheritable;
  }

  /**
   * Sets the checks if is inheritable.
   *
   * @param isInheritable the new checks if is inheritable
   */
  public void setIsInheritable(boolean isInheritable) {
    this.isInheritable = isInheritable;
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
   * Sets the checks if is encrypted.
   *
   * @param isEncrypted the new checks if is encrypted
   */
  public void setIsEncrypted(boolean isEncrypted) {
    this.isEncrypted = isEncrypted;
  }

  /**
   * Checks if the <b>md_class_attribute</b> is immutable or not.
   *
   * @return <code>true</code> if the attribute {@link #attributeName} is immutable, else return
   * <code>false</code>.
   */
  public boolean getIsImmutable() {
    return isImmutable;
  }

  /**
   * Sets the <b>md_class_attribute</b> immutable value.
   *
   * @param isImmutable the immutable value to set (<code>true</code> or <code>false</code>)
   */
  public void setIsImmutable(boolean isImmutable) {
    this.isImmutable = isImmutable;
  }

  /**
   * Checks if is force on dependent.
   *
   * @return true, if is force on dependent
   */
  public boolean isForceOnDependent() {
    return forceOnDependent;
  }

  /**
   * Sets the force on dependent.
   *
   * @param forceOnDependent the new force on dependent
   */
  public void setForceOnDependent(boolean forceOnDependent) {
    this.forceOnDependent = forceOnDependent;
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

  public boolean isMandatory() {
    return isMandatory;
  }

  public void setMandatory(boolean mandatory) {
    isMandatory = mandatory;
  }

  public boolean isInheritable() {
    return isInheritable;
  }

  public void setInheritable(boolean inheritable) {
    isInheritable = inheritable;
  }

  public boolean isEncrypted() {
    return isEncrypted;
  }

  public void setEncrypted(boolean encrypted) {
    isEncrypted = encrypted;
  }

  public boolean isImmutable() {
    return isImmutable;
  }

  public void setImmutable(boolean immutable) {
    isImmutable = immutable;
  }
}
