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
 * The Class CmsCIRelationBasic.
 */
public class CmsCIRelationBasic implements Serializable {

  private static final long serialVersionUID = 1L;

  private long ciRelationId;
  private String nsPath;
  private long fromCiId;
  private long toCiId;
  private String relationGoid;
  private String relationState;
  private String relationName;
  private long lastAppliedRfcId;
  private String comments;
  private String createdBy;
  private String updatedBy;
  private Date created;
  private Date updated;

  /**
   * Gets the ci relation id.
   *
   * @return the ci relation id
   */
  public long getCiRelationId() {
    return ciRelationId;
  }

  /**
   * Sets the ci relation id.
   *
   * @param ciRelationId the new ci relation id
   */
  public void setCiRelationId(long ciRelationId) {
    this.ciRelationId = ciRelationId;
  }

  /**
   * Gets the ns path.
   *
   * @return the ns path
   */
  public String getNsPath() {
    return nsPath;
  }

  /**
   * Sets the ns path.
   *
   * @param nsPath the new ns path
   */
  public void setNsPath(String nsPath) {
    this.nsPath = nsPath;
  }

  /**
   * Gets the from ci id.
   *
   * @return the from ci id
   */
  public long getFromCiId() {
    return fromCiId;
  }

  /**
   * Sets the from ci id.
   *
   * @param fromCiId the new from ci id
   */
  public void setFromCiId(long fromCiId) {
    this.fromCiId = fromCiId;
  }

  /**
   * Gets the to ci id.
   *
   * @return the to ci id
   */
  public long getToCiId() {
    return toCiId;
  }

  /**
   * Sets the to ci id.
   *
   * @param toCiId the new to ci id
   */
  public void setToCiId(long toCiId) {
    this.toCiId = toCiId;
  }

  /**
   * Gets the relation goid.
   *
   * @return the relation goid
   */
  public String getRelationGoid() {
    return relationGoid;
  }

  /**
   * Sets the relation goid.
   *
   * @param relationGoid the new relation goid
   */
  public void setRelationGoid(String relationGoid) {
    this.relationGoid = relationGoid;
  }

  /**
   * Gets the relation state.
   *
   * @return the relation state
   */
  public String getRelationState() {
    return relationState;
  }

  /**
   * Sets the relation state.
   *
   * @param relationState the new relation state
   */
  public void setRelationState(String relationState) {
    this.relationState = relationState;
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
   * Gets the last applied rfc id.
   *
   * @return the last applied rfc id
   */
  public long getLastAppliedRfcId() {
    return lastAppliedRfcId;
  }

  /**
   * Sets the last applied rfc id.
   *
   * @param lastAppliedRfcId the new last applied rfc id
   */
  public void setLastAppliedRfcId(long lastAppliedRfcId) {
    this.lastAppliedRfcId = lastAppliedRfcId;
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
   * Gets the created by.
   *
   * @return the created by
   */
  public String getCreatedBy() {
    return createdBy;
  }

  /**
   * Sets the created by.
   *
   * @param createdBy the new created by
   */
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   * Gets the updated by.
   *
   * @return the updated by
   */
  public String getUpdatedBy() {
    return updatedBy;
  }

  /**
   * Sets the updated by.
   *
   * @param updatedBy the new updated by
   */
  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
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
