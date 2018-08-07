/**
 * *****************************************************************************
 *
 * <p>Copyright 2015 Walmart, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>*****************************************************************************
 */
package com.oneops.cms.simple.domain;

import com.oneops.cms.cm.ops.domain.CmsOpsAction;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The Class CmsActionOrderSimple. */
public class CmsActionOrderSimple extends CmsOpsAction
    implements CmsWorkOrderSimpleBase<CmsCISimple> {

  private static final long serialVersionUID = 1L;

  public CmsCISimple ci;
  public CmsCISimple cloud;
  public CmsCISimple resultCi;
  public CmsCISimple box;
  public Map<String, List<CmsCISimple>> payLoad;
  public Map<String, Map<String, CmsCISimple>> services;
  public Map<String, String> searchTags = new HashMap<>();
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
   * Gets the pay load.
   *
   * @return the pay load
   */
  public Map<String, List<CmsCISimple>> getPayLoad() {
    return payLoad;
  }

  /**
   * Sets the pay load.
   *
   * @param payLoad the pay load
   */
  public void setPayLoad(Map<String, List<CmsCISimple>> payLoad) {
    this.payLoad = payLoad;
  }

  @Override
  public List<CmsCISimple> getPayLoadEntry(String payloadKey) {
    return getPayLoad().get(payloadKey);
  }

  @Override
  public CmsCISimple getPayLoadEntryAt(String payloadKey, int indx) {
    if (isPayLoadEntryPresent(payloadKey)) {
      return getPayLoad().get(payloadKey).get(indx);
    }

    return null;
  }

  @Override
  public String getPayLoadAttribute(String payloadEntry, String attributeName) {
    if (getPayLoadEntryAt(payloadEntry, 0) != null) {
      return getPayLoadEntryAt(payloadEntry, 0).getCiAttributes().get(attributeName);
    }
    return null;
  }

  @Override
  public Map<String, String> getCiAttributes() {
    return getCi().getCiAttributes();
  }

  @Override
  public String getAction() {
    return getActionName();
  }

  @Override
  public String getNsPath() {
    return getCi().getNsPath();
  }

  @Override
  public String getClassName() {
    return getCi().getCiClassName();
  }

  @Override
  public boolean isPayloadEntryEqual(
      String payloadEntry, String attributeName, String valueToBeCompared) {
    return false;
  }

  /**
   * Put pay load entry.
   *
   * @param key the key
   * @param value the value
   */
  public void putPayLoadEntry(String key, List<CmsCISimple> value) {
    if (value != null && value.size() > 0) {
      if (this.payLoad == null) {
        this.payLoad = new HashMap<>();
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
  public void addPayLoadEntry(String key, CmsCISimple value) {
    if (value != null) {
      if (this.payLoad == null) {
        this.payLoad = new HashMap<>();
      }
      if (!this.payLoad.containsKey(key)) {
        this.payLoad.put(key, new ArrayList<>());
      }
      this.payLoad.get(key).add(value);
    }
  }

  /**
   * Gets the ci.
   *
   * @return the ci
   */
  public CmsCISimple getCi() {
    return ci;
  }

  /**
   * Sets the ci.
   *
   * @param ci the new ci
   */
  public void setCi(CmsCISimple ci) {
    this.ci = ci;
  }

  /** Gets the search tags */
  public Map<String, String> getSearchTags() {
    return searchTags;
  }

  /** Sets the search tags */
  public void setSearchTags(Map<String, String> searchTags) {
    this.searchTags = searchTags;
  }

  @Override
  public String getCiName() {
    return getCi().getCiName();
  }

  @Override
  public long getRecordId() {
    return getActionId();
  }

  @Override
  public long getExecutionId() {
    return getProcedureId();
  }

  @Override
  public void setComments(String comments) {
    // No-op
  }

  /** Accessor for config variables. */
  public Map<String, String> getConfig() {
    return config;
  }

  /** Mutator for config variables. */
  public void setConfig(Map<String, String> config) {
    this.config = config;
  }
}
