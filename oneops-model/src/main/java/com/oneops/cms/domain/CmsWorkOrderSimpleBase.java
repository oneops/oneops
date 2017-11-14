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
package com.oneops.cms.domain;

import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * The Interface CmsWorkOrderSimpleBase.
 */
public interface CmsWorkOrderSimpleBase<T> {

  CmsCISimple getBox();

  void setBox(CmsCISimple box);

  CmsCISimple getResultCi();

  void setResultCi(CmsCISimple resultCi);

  CmsCISimple getCloud();

  void setCloud(CmsCISimple token);

  Map<String, Map<String, CmsCISimple>> getServices();

  void setServices(Map<String, Map<String, CmsCISimple>> services);

  Map<String, String> getSearchTags();

  void setSearchTags(Map<String, String> searchTags);

  default void putSearchTag(String tagName, String value) {
    getSearchTags().put(tagName, value);
  }

  String getAction();

  String getNsPath();

  String getClassName();

  long getCiId();

  String getCiName();

  /**
   * Gets the pay load.
   *
   * @return the pay load
   */
  Map<String, List<T>> getPayLoad();

  /**
   * Gets the pay load.
   *
   * @return the pay load
   */
  List<T> getPayLoadEntry(String payloadKey);

  /**
   * Gets the pay load.
   *
   * @return the pay load
   */
  T getPayLoadEntryAt(String payloadKey, int indx);

  default boolean isPayLoadEntryPresent(String entry) {
    return getPayLoad() != null && getPayLoadEntry(entry) != null
      && getPayLoadEntry(entry).size() > 0;
  }

  default boolean isPayloadEntryEqual(String payloadEntry, String attributeName,
    String valueToBeCompared) {
    return !StringUtils.isEmpty(valueToBeCompared) && valueToBeCompared
      .equals(getPayLoadAttribute(payloadEntry,
        attributeName));
  }

  default boolean isAttributePresentInPayload(String payloadEntry, String atttributeName) {
    final T entry = getPayLoadEntryAt(payloadEntry, 0);
    if (entry instanceof CmsCISimple) {
      return ((CmsCISimple) entry).getCiAttributes().containsKey(atttributeName);

    } else if (entry instanceof CmsRfcCISimple) {
      return ((CmsRfcCISimple) entry).getCiAttributes().containsKey(atttributeName);
    }
    throw new IllegalArgumentException("Can not find the attribute ");
  }

  String getPayLoadAttribute(String payloadEntry, String attributeName);


  Map<String, String> getCiAttributes();

  void putPayLoadEntry(String payloadEntry, List<T> rfcCiForExtraRunList);

  Map<String, String> getConfig();

  void setConfig(Map<String, String> config);
}
