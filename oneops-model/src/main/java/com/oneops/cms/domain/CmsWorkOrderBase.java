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

import com.oneops.cms.cm.domain.CmsCI;
import java.util.Map;

/**
 * The Interface CmsWorkOrderBase.
 */
public interface CmsWorkOrderBase {

  CmsCI getBox();

  void setBox(CmsCI box);

  CmsCI getResultCi();

  void setResultCi(CmsCI resultCi);

  CmsCI getCloud();

  void setCloud(CmsCI cloud);

  Map<String, Map<String, CmsCI>> getServices();

  void setServices(Map<String, Map<String, CmsCI>> services);

  void setConfig(Map<String, String> config);

  Map<String, String> getConfig();
}
