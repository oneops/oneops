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
 * The Class CmsCIBasic.
 */
public class CmsCIBasic implements Serializable {

  public static final int MAX_LENGTH = 2000;
  private static final long serialVersionUID = 1L;
  private long ciId;
  private String ciName;
  private String ciClassName;
  private String impl;
  private String nsPath;
  private String ciGoid;
  private String comments;
  private String ciState;
  private long lastAppliedRfcId;
  private String createdBy;
  private String updatedBy;
  private Date created;
  private Date updated;

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
    if (comments != null && comments.length() > MAX_LENGTH) {
      comments = comments.substring(0, MAX_LENGTH);
    }
    this.comments = comments;
  }

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

}
