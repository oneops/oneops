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
 * The Class CmsRfcCIBasic.
 */
public class CmsRfcCIBasic implements Serializable {

  private static final long serialVersionUID = 1L;

  private long rfcId;
  private long releaseId;
  private long ciId;
  private String nsPath;
  private String ciClassName;
  private String impl;
  private String ciName;
  private String ciGoid;
  private String ciState;
  private String rfcAction;
  private String releaseType;
  private String createdBy;
  private String updatedBy;
  private String rfcCreatedBy;
  private String rfcUpdatedBy;
  private int execOrder;
  private Long lastAppliedRfcId;
  private String comments;
  private boolean isActiveInRelease;
  private Date rfcCreated;
  private Date rfcUpdated;
  private Date created;
  private Date updated;
  private String hint;

  /**
   * Gets the ci state.
   *
   * @return the ci state
   */
  public String getCiState() {
    return ciState;
  }

  /**
   * Sets the ci state.
   *
   * @param ciState the new ci state
   */
  public void setCiState(String ciState) {
    this.ciState = ciState;
  }

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
   * Gets the ci id.
   *
   * @return the ci id
   */
  public long getCiId() {
    return ciId;
  }

  /**
   * Sets the ci id.
   *
   * @param ciId the new ci id
   */
  public void setCiId(long ciId) {
    this.ciId = ciId;
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
   * Gets the ci class name.
   *
   * @return the ci class name
   */
  public String getCiClassName() {
    return ciClassName;
  }

  /**
   * Sets the ci class name.
   *
   * @param ciClassName the new ci class name
   */
  public void setCiClassName(String ciClassName) {
    this.ciClassName = ciClassName;
  }

  /**
   * Gets the ci name.
   *
   * @return the ci name
   */
  public String getCiName() {
    return ciName;
  }

  /**
   * Sets the ci name.
   *
   * @param ciName the new ci name
   */
  public void setCiName(String ciName) {
    this.ciName = ciName;
  }

  /**
   * Gets the ci goid.
   *
   * @return the ci goid
   */
  public String getCiGoid() {
    return ciGoid;
  }

  /**
   * Sets the ci goid.
   *
   * @param ciGoid the new ci goid
   */
  public void setCiGoid(String ciGoid) {
    this.ciGoid = ciGoid;
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
   * Gets the impl.
   *
   * @return the impl
   */
  public String getImpl() {
    return impl;
  }

  /**
   * Sets the impl.
   *
   * @param impl the new impl
   */
  public void setImpl(String impl) {
    this.impl = impl;
  }

  public String getHint() {
    return hint;
  }

  public void setHint(String hint) {
    this.hint = hint;
  }

}
