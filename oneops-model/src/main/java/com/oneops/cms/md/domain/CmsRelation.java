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
package com.oneops.cms.md.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Class CmsRelation.
 */
public class CmsRelation implements Serializable {

  private static final long serialVersionUID = 1L;

  private int relationId;
  private String relationName;
  private String shortRelationName;
  private String description;
  private Date created;
  private List<CmsRelationAttribute> mdAttributes = new ArrayList<CmsRelationAttribute>();
  private List<CmsClazzRelation> targets = new ArrayList<CmsClazzRelation>();

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
   * Gets the relation name.
   *
   * @return the relation name
   */
  public String getRelationName() {
    return relationName;
  }

  /**
   * Sets the relation name.
   *
   * @param relationName the new relation name
   */
  public void setRelationName(String relationName) {
    this.relationName = relationName;
  }

  /**
   * Gets the short relation name.
   *
   * @return the short relation name
   */
  public String getShortRelationName() {
    return shortRelationName;
  }

  /**
   * Sets the short relation name.
   *
   * @param shortRelationName the new short relation name
   */
  public void setShortRelationName(String shortRelationName) {
    this.shortRelationName = shortRelationName;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description the new description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the created.
   *
   * @return the created
   */
  public Date getCreated() {
    return created;
  }

  /**
   * Sets the created.
   *
   * @param created the new created
   */
  public void setCreated(Date created) {
    this.created = created;
  }

  /**
   * Gets the md attributes.
   *
   * @return the md attributes
   */
  public List<CmsRelationAttribute> getMdAttributes() {
    return mdAttributes;
  }

  /**
   * Sets the md attributes.
   *
   * @param attributes the new md attributes
   */
  public void setMdAttributes(List<CmsRelationAttribute> attributes) {
    this.mdAttributes = attributes;
  }

  /**
   * Adds the attribute.
   *
   * @param attribute the attribute
   */
  public void addAttribute(CmsRelationAttribute attribute) {
    this.mdAttributes.add(attribute);
  }

  /**
   * Gets the targets.
   *
   * @return the targets
   */
  public List<CmsClazzRelation> getTargets() {
    return targets;
  }

  /**
   * Sets the targets.
   *
   * @param targets the new targets
   */
  public void setTargets(List<CmsClazzRelation> targets) {
    this.targets = targets;
  }

  /**
   * Adds the target.
   *
   * @param target the target
   */
  public void addTarget(CmsClazzRelation target) {
    this.targets.add(target);
  }

}
