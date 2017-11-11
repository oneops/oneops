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

import java.util.HashMap;
import java.util.Map;

/**
 * The Class CmsRfcRelation.
 */
public class CmsRfcRelation extends CmsRfcRelationBasic implements CmsRfcContainer {

  private static final long serialVersionUID = 1L;

  private int relationId;
  private long nsId;
  private int rfcActionId;
  private Long fromRfcId;
  private Long toRfcId;
  private String releaseNsPath;
  private CmsRfcCI fromRfcCi;
  private CmsRfcCI toRfcCi;
  private boolean isValidated = false;
  private Map<String, CmsRfcAttribute> attributes = new HashMap<String, CmsRfcAttribute>();

  /**
   * Checks if is validated.
   *
   * @return true, if is validated
   */
  public boolean isValidated() {
    return isValidated;
  }

  /**
   * Sets the validated.
   *
   * @param isValidated the new validated
   */
  public void setValidated(boolean isValidated) {
    this.isValidated = isValidated;
  }

  /**
   * Gets the release ns path.
   *
   * @return the release ns path
   */
  public String getReleaseNsPath() {
    return releaseNsPath;
  }

  /**
   * Sets the release ns path.
   *
   * @param releaseNsPath the new release ns path
   */
  public void setReleaseNsPath(String releaseNsPath) {
    this.releaseNsPath = releaseNsPath;
  }

  /**
   * Gets the from rfc ci.
   *
   * @return the from rfc ci
   */
  public CmsRfcCI getFromRfcCi() {
    return fromRfcCi;
  }

  /**
   * Sets the from rfc ci.
   *
   * @param fromRfcCi the new from rfc ci
   */
  public void setFromRfcCi(CmsRfcCI fromRfcCi) {
    this.fromRfcCi = fromRfcCi;
  }

  /**
   * Gets the to rfc ci.
   *
   * @return the to rfc ci
   */
  public CmsRfcCI getToRfcCi() {
    return toRfcCi;
  }

  /**
   * Sets the to rfc ci.
   *
   * @param toRfcCi the new to rfc ci
   */
  public void setToRfcCi(CmsRfcCI toRfcCi) {
    this.toRfcCi = toRfcCi;
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
   * Gets the rfc action id.
   *
   * @return the rfc action id
   */
  public int getRfcActionId() {
    return rfcActionId;
  }

  /**
   * Sets the rfc action id.
   *
   * @param rfcActionId the new rfc action id
   */
  public void setRfcActionId(int rfcActionId) {
    this.rfcActionId = rfcActionId;
  }

  /**
   * Gets the from rfc id.
   *
   * @return the from rfc id
   */
  public Long getFromRfcId() {
    return fromRfcId;
  }

  /**
   * Sets the from rfc id.
   *
   * @param fromRfcId the new from rfc id
   */
  public void setFromRfcId(Long fromRfcId) {
    this.fromRfcId = fromRfcId;
  }

  /**
   * Gets the to rfc id.
   *
   * @return the to rfc id
   */
  public Long getToRfcId() {
    return toRfcId;
  }

  /**
   * Sets the to rfc id.
   *
   * @param toRfcId the new to rfc id
   */
  public void setToRfcId(Long toRfcId) {
    this.toRfcId = toRfcId;
  }

  /**
   * Gets the attributes.
   *
   * @return the attributes
   */
  public Map<String, CmsRfcAttribute> getAttributes() {
    return attributes;
  }

  /**
   * Sets the attributes.
   *
   * @param attributes the attributes
   */
  public void setAttributes(Map<String, CmsRfcAttribute> attributes) {
    this.attributes = attributes;
  }

  /**
   * Adds the attribute.
   *
   * @param attribute the attribute
   */
  public void addAttribute(CmsRfcAttribute attribute) {
    this.attributes.put(attribute.getAttributeName(), attribute);
  }

  /**
   * Gets the attribute.
   *
   * @param attrName the attr name
   * @return the attribute
   */
  public CmsRfcAttribute getAttribute(String attrName) {
    return this.attributes.get(attrName);
  }

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
   * Sets the ns path.
   *
   * @param nsPath the new ns path
   */
  public void setNsPath(String nsPath) {
    super.setNsPath(nsPath);
    if (this.releaseNsPath == null) {
      //  /oneops/montest/mtest/bom/custom/1
      // Lets strip off platform parts for the release
      String[] nsParts = nsPath.split("/");
      String releaseNs = "";
      for (int i = 1; i < nsParts.length; i++) {
        if (nsParts[i].equals("_design")) {
          break;
        }
        releaseNs += "/" + nsParts[i];
        if (nsParts[i].equals("bom")) {
          break;
        }
        if (nsParts[i].equals("manifest")) {
          break;
        }
      }
      this.releaseNsPath = releaseNs;
    }
  }

}
