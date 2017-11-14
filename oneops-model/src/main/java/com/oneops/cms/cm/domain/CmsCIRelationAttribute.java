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
 * The Class CmsCIRelationAttribute.
 */
public class CmsCIRelationAttribute extends CmsBasicAttribute implements Serializable {

  private static final long serialVersionUID = 1L;

  private long ciRelationAttributeId;
  private long ciRelationId;

  /**
   * Gets the ci relation attribute id.
   *
   * @return the ci relation attribute id
   */
  public long getCiRelationAttributeId() {
    return ciRelationAttributeId;
  }

  /**
   * Sets the ci relation attribute id.
   *
   * @param ciRelationAttributeId the new ci relation attribute id
   */
  public void setCiRelationAttributeId(long ciRelationAttributeId) {
    this.ciRelationAttributeId = ciRelationAttributeId;
  }

  /**
   * Gets the ci relation id.
   *
   * @return the ci relation id
   */
  public long getCiRelationId() {
    return ciRelationId;
  }

  /**
   * Sets the ci relation id.
   *
   * @param ciRelationId the new ci relation id
   */
  public void setCiRelationId(long ciRelationId) {
    this.ciRelationId = ciRelationId;
  }

}
