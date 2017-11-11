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
 * The Class CmsClazzRelation.
 */
public class CmsClazzRelation implements Serializable {

  private static final long serialVersionUID = 1L;

  private int linkId;
  private int fromClassId;
  private String fromClassName;
  private int relationId;
  private String relationName;
  private int toClassId;
  private String toClassName;
  private boolean isStrong;
  private String linkType;
  private String description;
  private Date created;

  /**
   * Gets the serialversionuid.
   *
   * @return the serialversionuid
   */
  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  /**
   * Gets the link id.
   *
   * @return the link id
   */
  public int getLinkId() {
    return linkId;
  }

  /**
   * Sets the link id.
   *
   * @param linkId the new link id
   */
  public void setLinkId(int linkId) {
    this.linkId = linkId;
  }

  /**
   * Gets the from class id.
   *
   * @return the from class id
   */
  public int getFromClassId() {
    return fromClassId;
  }

  /**
   * Sets the from class id.
   *
   * @param fromClassId the new from class id
   */
  public void setFromClassId(int fromClassId) {
    this.fromClassId = fromClassId;
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
   * Gets the to class id.
   *
   * @return the to class id
   */
  public int getToClassId() {
    return toClassId;
  }

  /**
   * Sets the to class id.
   *
   * @param toClassId the new to class id
   */
  public void setToClassId(int toClassId) {
    this.toClassId = toClassId;
  }

  /**
   * Gets the checks if is strong.
   *
   * @return the checks if is strong
   */
  public boolean getIsStrong() {
    return isStrong;
  }

  /**
   * Sets the checks if is strong.
   *
   * @param isStrong the new checks if is strong
   */
  public void setIsStrong(boolean isStrong) {
    this.isStrong = isStrong;
  }

  /**
   * Gets the link type.
   *
   * @return the link type
   */
  public String getLinkType() {
    return linkType;
  }

  /**
   * Sets the link type.
   *
   * @param linkType the new link type
   */
  public void setLinkType(String linkType) {
    this.linkType = linkType;
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
   * Gets the from class name.
   *
   * @return the from class name
   */
  public String getFromClassName() {
    return fromClassName;
  }

  /**
   * Sets the from class name.
   *
   * @param fromClassName the new from class name
   */
  public void setFromClassName(String fromClassName) {
    this.fromClassName = fromClassName;
  }

  /**
   * Gets the relation name.
   *
   * @return the relation name
   */
  public String getRelationName() {
    return relationName;
  }

  /**
   * Sets the relation name.
   *
   * @param relationName the new relation name
   */
  public void setRelationName(String relationName) {
    this.relationName = relationName;
  }

  /**
   * Gets the to class name.
   *
   * @return the to class name
   */
  public String getToClassName() {
    return toClassName;
  }

  /**
   * Sets the to class name.
   *
   * @param toClassName the new to class name
   */
  public void setToClassName(String toClassName) {
    this.toClassName = toClassName;
  }

  public boolean isStrong() {
    return isStrong;
  }

  public void setStrong(boolean strong) {
    isStrong = strong;
  }
}
