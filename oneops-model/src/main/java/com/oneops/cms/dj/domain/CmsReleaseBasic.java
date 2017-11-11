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
 * The Class CmsReleaseBasic.
 */
public class CmsReleaseBasic implements Serializable {

  private static final long serialVersionUID = 1L;

  private long releaseId;
  private String nsPath;
  private String releaseName;
  private String createdBy;
  private String commitedBy;
  private String releaseState;
  private String releaseType;
  private String description;
  private int revision;
  private Long parentReleaseId;
  private Date created;
  private Date updated;

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
   * Gets the release name.
   *
   * @return the release name
   */
  public String getReleaseName() {
    return releaseName;
  }

  /**
   * Sets the release name.
   *
   * @param releaseName the new release name
   */
  public void setReleaseName(String releaseName) {
    this.releaseName = releaseName;
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
   * Gets the release state.
   *
   * @return the release state
   */
  public String getReleaseState() {
    return releaseState;
  }

  /**
   * Sets the release state.
   *
   * @param releaseState the new release state
   */
  public void setReleaseState(String releaseState) {
    this.releaseState = releaseState;
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
   * Gets the revision.
   *
   * @return the revision
   */
  public int getRevision() {
    return revision;
  }

  /**
   * Sets the revision.
   *
   * @param revision the new revision
   */
  public void setRevision(int revision) {
    this.revision = revision;
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
   * Gets the parent release id.
   *
   * @return the parent release id
   */
  public Long getParentReleaseId() {
    return parentReleaseId;
  }

  /**
   * Sets the parent release id.
   *
   * @param parentReleaseId the new parent release id
   */
  public void setParentReleaseId(Long parentReleaseId) {
    this.parentReleaseId = parentReleaseId;
  }

  /**
   * Gets the commited by.
   *
   * @return the commited by
   */
  public String getCommitedBy() {
    return commitedBy;
  }

  /**
   * Sets the commited by.
   *
   * @param commitedBy the new commited by
   */
  public void setCommitedBy(String commitedBy) {
    this.commitedBy = commitedBy;
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
}
