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

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.domain.CmsWorkOrderBase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CmsWorkOrder.
 */
public class CmsWorkOrder extends CmsDpmtRecord implements CmsWorkOrderBase {

  private static final long serialVersionUID = 1L;

  private CmsRfcCI rfcCi;
  private CmsCI cloud;
  private CmsCI resultCi;
  private CmsCI box;
  private Map<String, List<CmsRfcCI>> payLoad;
  private Map<String, Map<String, CmsCI>> services;
  private Map<String, String> additionalInfo;
  private Map<String, String> config;

  /**
   * Gets the cloud.
   *
   * @return the cloud
   */
  public CmsCI getCloud() {
    return cloud;
  }

  /**
   * Sets the cloud.
   *
   * @param cloud the new cloud
   */
  public void setCloud(CmsCI cloud) {
    this.cloud = cloud;
  }

  /**
   * Gets the services.
   *
   * @return the services
   */
  public Map<String, Map<String, CmsCI>> getServices() {
    return services;
  }

  /**
   * Sets the services.
   *
   * @param services the services
   */
  public void setServices(Map<String, Map<String, CmsCI>> services) {
    this.services = services;
  }

  /**
   * Gets the box.
   *
   * @return the box
   */
  public CmsCI getBox() {
    return box;
  }

  /**
   * Sets the box.
   *
   * @param box the new box
   */
  public void setBox(CmsCI box) {
    this.box = box;
  }

  /**
   * Gets the result ci.
   *
   * @return the result ci
   */
  public CmsCI getResultCi() {
    return resultCi;
  }

  /**
   * Sets the result ci.
   *
   * @param resultCi the new result ci
   */
  public void setResultCi(CmsCI resultCi) {
    this.resultCi = resultCi;
  }

  /**
   * Gets the pay load.
   *
   * @return the pay load
   */
  public Map<String, List<CmsRfcCI>> getPayLoad() {
    return payLoad;
  }

  /**
   * Sets the pay load.
   *
   * @param payLoad the pay load
   */
  public void setPayLoad(Map<String, List<CmsRfcCI>> payLoad) {
    this.payLoad = payLoad;
  }

  /**
   * Put pay load entry.
   *
   * @param key the key
   * @param value the value
   */
  public void putPayLoadEntry(String key, List<CmsRfcCI> value) {
    if (value != null) {
      if (this.payLoad == null) {
        this.payLoad = new HashMap<String, List<CmsRfcCI>>();
      }
      this.payLoad.put(key, value);
    }
  }

  /**
   * Adds the pay load entry.
   *
   * @param key the key
   * @param value the value
   */
  public void addPayLoadEntry(String key, CmsRfcCI value) {
    if (value != null) {
      if (this.payLoad == null) {
        this.payLoad = new HashMap<String, List<CmsRfcCI>>();
        this.payLoad.put(key, new ArrayList<CmsRfcCI>());
      }
      this.payLoad.get(key).add(value);
    }
  }

  /**
   * Gets the rfc ci.
   *
   * @return the rfc ci
   */
  public CmsRfcCI getRfcCi() {
    return rfcCi;
  }

  /**
   * Sets the rfc ci.
   *
   * @param rfcCi the new rfc ci
   */
  public void setRfcCi(CmsRfcCI rfcCi) {
    this.rfcCi = rfcCi;
  }

  public Map<String, String> getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(Map<String, String> additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public Map<String, String> getConfig() {
    return config;
  }

  public void setConfig(Map<String, String> config) {
    this.config = config;
  }
}
