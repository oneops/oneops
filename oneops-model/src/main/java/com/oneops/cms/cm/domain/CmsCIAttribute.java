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

/**
 * The Class CmsCIAttribute.
 */
public class CmsCIAttribute extends CmsBasicAttribute implements Serializable {

  private static final long serialVersionUID = 1L;

  private long ciAttributeId;
  private long ciId;

  /**
   * Gets the ci attribute id.
   *
   * @return the ci attribute id
   */
  public long getCiAttributeId() {
    return ciAttributeId;
  }

  /**
   * Sets the ci attribute id.
   *
   * @param ciAttributeId the new ci attribute id
   */
  public void setCiAttributeId(long ciAttributeId) {
    this.ciAttributeId = ciAttributeId;
  }

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

}
