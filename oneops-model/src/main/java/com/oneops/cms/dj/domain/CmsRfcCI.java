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

import com.oneops.Util;
import com.oneops.cms.cm.domain.CmsCI;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class CmsRfcCI.
 */
public class CmsRfcCI extends CmsRfcCIBasic implements CmsRfcContainer {

  private static final long serialVersionUID = 1L;

  private long nsId;
  private int ciClassId;
  private int rfcActionId;
  private String releaseNsPath;
  private Map<String, CmsRfcAttribute> attributes = new HashMap<>();

	public CmsRfcCI() {}

  public CmsRfcCI(CmsCI ci, String createdBy) {
    setCiId(ci.getCiId());
    setCiClassName(ci.getCiClassName());
    setCiClassId(ci.getCiClassId());
    setCiName(ci.getCiName());
    setNsPath(ci.getNsPath());
    setNsId(ci.getNsId());
    setCiGoid(ci.getCiGoid());
    setCiState(ci.getCiState());
    setComments(ci.getComments());
    setLastAppliedRfcId(ci.getLastAppliedRfcId());
    setCreated(ci.getCreated());
    setCreatedBy(ci.getCreatedBy());
    setUpdated(ci.getUpdated());
    setUpdatedBy(ci.getUpdatedBy());
    setRfcCreatedBy(createdBy);
    setRfcUpdatedBy(createdBy);
  }

  public CmsRfcCI(CmsCI ci, String createdBy, Map<String, String> changes) {
    this(ci, createdBy);
    setRfcAction("update");
    changes.forEach((key, value) -> {
      CmsRfcAttribute attr = new CmsRfcAttribute();
      attr.setAttributeName(key);
      attr.setNewValue(value);
      addAttribute(attr);
    });
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
   * Gets the ci class id.
   *
   * @return the ci class id
   */
  public int getCiClassId() {
    return ciClassId;
  }

  /**
   * Sets the ci class id.
   *
   * @param ciClassId the new ci class id
   */
  public void setCiClassId(int ciClassId) {
    this.ciClassId = ciClassId;
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
   * Gets the attribute.
   *
   * @param attributeName the attribute name
   * @return the attribute
   */
  public CmsRfcAttribute getAttribute(String attributeName) {
    return this.attributes.get(attributeName);
  }

  /**
   * Gets the attributes.
   *
   * @return the attributes
   */
  @Override
  public Map<String, CmsRfcAttribute> getAttributes() {
    return attributes;
  }

  /**
   * Sets the attributes.
   *
   * @param attributes the attributes
   */
  @Override
  public void setAttributes(Map<String, CmsRfcAttribute> attributes) {
    this.attributes = attributes;
  }

  /**
   * Adds the attribute.
   *
   * @param attribute the attribute
   */
  @Override
  public void addAttribute(CmsRfcAttribute attribute) {
    this.attributes.put(attribute.getAttributeName(), attribute);
  }

  /**
   * Sets the ns path.
   *
   * @param nsPath the new ns path
   */
  public void setNsPath(String nsPath) {
    super.setNsPath(nsPath);
    if (this.releaseNsPath == null) {
      this.releaseNsPath = Util.getReleaseNsPath(nsPath);
    }
  }

	public CmsRfcAttribute addOrUpdateAttribute(String attrName, String attrValue) {
		CmsRfcAttribute attr = getAttribute(attrName);
		if (attr == null) {
			attr = new CmsRfcAttribute();
			attr.setAttributeName(attrName);
			addAttribute(attr);
		}
		attr.setNewValue(String.valueOf(attrValue));
		return attr;
	}

	public CmsRfcAttribute addOrUpdateAttribute(String attrName, String attrValue, String comments) {
		CmsRfcAttribute attr = addOrUpdateAttribute(attrName, attrValue);
		attr.setComments(comments);
		return attr;
	}

	@Override
	public String toString() {
		return "cmsRfc: "+ super.toString()+" attributes=" + attributes;
	}
	
	
}
