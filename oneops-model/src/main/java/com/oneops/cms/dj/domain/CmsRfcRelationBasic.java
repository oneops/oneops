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
 * The Class CmsRfcRelationBasic.
 */
public class CmsRfcRelationBasic implements Serializable {

  private static final long serialVersionUID = 1L;

  private long rfcId;
  private long releaseId;
  private String nsPath;
  private long ciRelationId;
  private Long fromCiId;
  private String relationName;
  private String createdBy;
  private String updatedBy;
  private String rfcCreatedBy;
  private String rfcUpdatedBy;
  private Long toCiId;
  private String relationGoid;
  private String rfcAction;
  private int execOrder;
  private Long lastAppliedRfcId;
  private boolean isActiveInRelease;
  private String comments;
  private Date created;
  private Date updated;
  private Date rfcCreated;
  private Date rfcUpdated;
  private String releaseType;

  /**
   * Gets the last applied rfc id.
   *
   * @return the last applied rfc id
   */
  public Long getLastAppliedRfcId() {
    return lastAppliedRfcId;
  }

  /**
   * Sets the last applied rfc id.
   *
   * @param lastAppliedRfcId the new last applied rfc id
   */
  public void setLastAppliedRfcId(Long lastAppliedRfcId) {
    this.lastAppliedRfcId = lastAppliedRfcId;
  }

  /**
   * Gets the rfc created by.
   *
   * @return the rfc created by
   */
  public String getRfcCreatedBy() {
    return rfcCreatedBy;
  }

  /**
   * Sets the rfc created by.
   *
   * @param rfcCreatedBy the new rfc created by
   */
  public void setRfcCreatedBy(String rfcCreatedBy) {
    this.rfcCreatedBy = rfcCreatedBy;
  }

  /**
   * Gets the rfc updated by.
   *
   * @return the rfc updated by
   */
  public String getRfcUpdatedBy() {
    return rfcUpdatedBy;
  }

  /**
   * Sets the rfc updated by.
   *
   * @param rfcUpdatedBy the new rfc updated by
   */
  public void setRfcUpdatedBy(String rfcUpdatedBy) {
    this.rfcUpdatedBy = rfcUpdatedBy;
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

  /**
   * Gets the rfc created.
   *
   * @return the rfc created
   */
  public Date getRfcCreated() {
    return rfcCreated;
  }

  /**
   * Sets the rfc created.
   *
   * @param rfcCreated the new rfc created
   */
  public void setRfcCreated(Date rfcCreated) {
    this.rfcCreated = rfcCreated;
  }

  /**
   * Gets the rfc updated.
   *
   * @return the rfc updated
   */
  public Date getRfcUpdated() {
    return rfcUpdated;
  }

  /**
   * Sets the rfc updated.
   *
   * @param rfcUpdated the new rfc updated
   */
  public void setRfcUpdated(Date rfcUpdated) {
    this.rfcUpdated = rfcUpdated;
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

  /**
   * Gets the from ci id.
   *
   * @return the from ci id
   */
  public Long getFromCiId() {
    return fromCiId;
  }

  /**
   * Sets the from ci id.
   *
   * @param fromCiId the new from ci id
   */
  public void setFromCiId(Long fromCiId) {
    this.fromCiId = fromCiId;
  }

  /**
   * Gets the to ci id.
   *
   * @return the to ci id
   */
  public Long getToCiId() {
    return toCiId;
  }

  /**
   * Sets the to ci id.
   *
   * @param toCiId the new to ci id
   */
  public void setToCiId(Long toCiId) {
    this.toCiId = toCiId;
  }

  /**
   * Gets the release id.
   *
   * @return the release id
   */
  public long getReleaseId() {
    return releaseId;
  }

  /**
   * Sets the release id.
   *
   * @param releaseId the new release id
   */
  public void setReleaseId(long releaseId) {
    this.releaseId = releaseId;
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
   * Gets the rfc action.
   *
   * @return the rfc action
   */
  public String getRfcAction() {
    return rfcAction;
  }

  /**
   * Sets the rfc action.
   *
   * @param rfcAction the new rfc action
   */
  public void setRfcAction(String rfcAction) {
    this.rfcAction = rfcAction;
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
   * Gets the exec order.
   *
   * @return the exec order
   */
  public int getExecOrder() {
    return execOrder;
  }

  /**
   * Sets the exec order.
   *
   * @param execOrder the new exec order
   */
  public void setExecOrder(int execOrder) {
    this.execOrder = execOrder;
  }

  /**
   * Gets the checks if is active in release.
   *
   * @return the checks if is active in release
   */
  public boolean getIsActiveInRelease() {
    return isActiveInRelease;
  }

  /**
   * Sets the checks if is active in release.
   *
   * @param isActiveInRelease the new checks if is active in release
   */
  public void setIsActiveInRelease(boolean isActiveInRelease) {
    this.isActiveInRelease = isActiveInRelease;
  }

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
   * Gets the release type.
   *
   * @return the release type
   */
  public String getReleaseType() {
    return releaseType;
  }

  /**
   * Sets the release type.
   *
   * @param releaseType the new release type
   */
  public void setReleaseType(String releaseType) {
    this.releaseType = releaseType;
  }

@Override
public String toString() {
	return "CmsRfcRelationBasic [fromCiId=" + fromCiId + ", toCiId=" + toCiId + "]";
}

  
}
