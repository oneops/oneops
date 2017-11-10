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
package com.oneops.cms.simple.domain;


import com.oneops.cms.dj.domain.CmsDpmtRecord;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CmsWorkOrderSimple.
 */
public class CmsWorkOrderSimple extends CmsDpmtRecord implements
  CmsWorkOrderSimpleBase<CmsRfcCISimple> {

  private static final long serialVersionUID = 1L;

  public CmsRfcCISimple rfcCi;
  public CmsCISimple cloud;
  public CmsCISimple resultCi;
  public CmsCISimple box;
  public Map<String, List<CmsRfcCISimple>> payLoad;
  public Map<String, Map<String, CmsCISimple>> services;
  public Map<String, String> searchTags = new HashMap<String, String>();
  public Map<String, String> additionalInfo = new HashMap<String, String>();
  public Map<String, String> config;


  /**
   * Gets the cloud.
   *
   * @return the cloud
   */
  public CmsCISimple getCloud() {
    return cloud;
  }

  /**
   * Sets the cloud.
   *
   * @param cloud the new cloud
   */
  public void setCloud(CmsCISimple cloud) {
    this.cloud = cloud;
  }

  /**
   * Gets the services.
   *
   * @return the services
   */
  public Map<String, Map<String, CmsCISimple>> getServices() {
    return services;
  }

  /**
   * Sets the services.
   *
   * @param services the services
   */
  public void setServices(Map<String, Map<String, CmsCISimple>> services) {
    this.services = services;
  }

  /**
   * Gets the box.
   *
   * @return the box
   */
  public CmsCISimple getBox() {
    return box;
  }

  /**
   * Sets the box.
   *
   * @param box the new box
   */
  public void setBox(CmsCISimple box) {
    this.box = box;
  }

  /**
   * Adds the pay load entry.
   *
   * @param key the key
   * @param value the value
   */
  public void addPayLoadEntry(String key, CmsRfcCISimple value) {
    if (this.payLoad == null) {
      this.payLoad = new HashMap<String, List<CmsRfcCISimple>>();
    }
    if (!this.payLoad.containsKey(key)) {
      this.payLoad.put(key, new ArrayList<CmsRfcCISimple>());
    }
    this.payLoad.get(key).add(value);
  }

  /**
   * Gets the rfc ci.
   *
   * @return the rfc ci
   */
  public CmsRfcCISimple getRfcCi() {
    return rfcCi;
  }

  /**
   * Sets the rfc ci.
   *
   * @param rfcCi the new rfc ci
   */
  public void setRfcCi(CmsRfcCISimple rfcCi) {
    this.rfcCi = rfcCi;
  }

  /**
   * Gets the result ci.
   *
   * @return the result ci
   */
  public CmsCISimple getResultCi() {
    return resultCi;
  }

  /**
   * Sets the result ci.
   *
   * @param resultCi the new result ci
   */
  public void setResultCi(CmsCISimple resultCi) {
    this.resultCi = resultCi;
  }

  /**
   * Gets the search tags
   */
  public Map<String, String> getSearchTags() {
    return searchTags;
  }

  /**
   * Sets the search tags
   */
  public void setSearchTags(Map<String, String> searchTags) {
    this.searchTags = searchTags;
  }

  public Map<String, String> getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(Map<String, String> additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  /**
   * Gets the pay load.
   *
   * @return the pay load
   */
  public Map<String, List<CmsRfcCISimple>> getPayLoad() {
    return payLoad;
  }

  /**
   * Sets the pay load.
   *
   * @param payLoad the pay load
   */
  public void setPayLoad(Map<String, List<CmsRfcCISimple>> payLoad) {
    this.payLoad = payLoad;
  }

  @Override
  public List<CmsRfcCISimple> getPayLoadEntry(String payloadKey) {
    return getPayLoad().get(payloadKey);
  }

  /**
   * returns null
   */

  @Override
  public CmsRfcCISimple getPayLoadEntryAt(String payloadKey, int indx) {
    if (isPayLoadEntryPresent(payloadKey)) {
      return getPayLoad().get(payloadKey).get(indx);
    }
    return null;
  }

  /**
   * returns null , in case the attribute value can
   */
  @Override
  public String getPayLoadAttribute(String payloadEntry, String attributeName) {
    if (getPayLoadEntryAt(payloadEntry, 0) != null) {
      return getPayLoadEntryAt(payloadEntry, 0).getCiAttributes().get(attributeName);
    }
    return null;
  }

  @Override
  public String getAction() {
    return getRfcCi().getRfcAction();
  }

  @Override
  public String getNsPath() {
    return getRfcCi().getNsPath();
  }

  @Override
  public String getClassName() {
    return getRfcCi().getCiClassName();
  }

  @Override
  public long getCiId() {
    return getRfcCi().getCiId();
  }

  @Override
  public String getCiName() {
    return getRfcCi().getCiName();
  }

  @Override
  public Map<String, String> getCiAttributes() {
    return getRfcCi().getCiAttributes();
  }

  @Override
  public void putPayLoadEntry(String payloadEntry, List<CmsRfcCISimple> rfcCISimples) {
    if (this.payLoad == null) {
      this.payLoad = new HashMap<>();
    }
    this.payLoad.put(payloadEntry, rfcCISimples);
  }

  @Override
  public Map<String, String> getConfig() {
    return config;
  }

  @Override
  public void setConfig(Map<String, String> config) {
    this.config = config;
  }
}
