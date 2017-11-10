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

/**
 * The Class CmsRelease.
 */
public class CmsRelease extends CmsReleaseBasic implements Serializable {

  private static final long serialVersionUID = 1L;

  private long nsId;
  private int releaseStateId;
  private long ciRfcCount;
  private long relationRfcCount;

  /**
   * Gets the ns id.
   *
   * @return the ns id
   */
  public long getNsId() {
    return nsId;
  }

  /**
   * Sets the ns id.
   *
   * @param nsId the new ns id
   */
  public void setNsId(long nsId) {
    this.nsId = nsId;
  }

  /**
   * Gets the release state id.
   *
   * @return the release state id
   */
  public int getReleaseStateId() {
    return releaseStateId;
  }

  /**
   * Sets the release state id.
   *
   * @param releaseStateId the new release state id
   */
  public void setReleaseStateId(int releaseStateId) {
    this.releaseStateId = releaseStateId;
  }

  public long getCiRfcCount() {
    return ciRfcCount;
  }

  public void setCiRfcCount(long ciRfcCount) {
    this.ciRfcCount = ciRfcCount;
  }

  public long getRelationRfcCount() {
    return relationRfcCount;
  }

  public void setRelationRfcCount(long relationRfcCount) {
    this.relationRfcCount = relationRfcCount;
  }

}
