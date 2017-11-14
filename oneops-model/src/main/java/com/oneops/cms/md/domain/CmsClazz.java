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
 * The Class CmsClazz.
 */
public class CmsClazz implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final int USE_CLASS_NAME_NS_FLAG_POS = 1;
  private int classId;
  private String className;
  private String shortClassName;
  private int superClassId;
  private String superClassName;
  private String accessLevel;
  private String impl;
  private boolean isNamespace;
  private Integer flags = 0;
  private boolean useClassNameNS;
  private String description;
  private String extFormat;
  private Date created;
  private List<CmsClazzAttribute> mdAttributes = new ArrayList<CmsClazzAttribute>();
  private List<CmsClazzRelation> fromRelations = new ArrayList<CmsClazzRelation>();
  private List<CmsClazzRelation> toRelations = new ArrayList<CmsClazzRelation>();
  private List<CmsClazzAction> actions = new ArrayList<CmsClazzAction>();

  /**
   * Gets the class id.
   *
   * @return the class id
   */
  public int getClassId() {
    return classId;
  }

  /**
   * Sets the class id.
   *
   * @param classId the new class id
   */
  public void setClassId(int classId) {
    this.classId = classId;
    for (CmsClazzAttribute attr : mdAttributes) {
      attr.setClassId(classId);
    }
    for (CmsClazzAction act : actions) {
      act.setClassId(classId);
    }
  }

  /**
   * Gets the class name.
   *
   * @return the class name
   */
  public String getClassName() {
    return className;
  }

  /**
   * Sets the class name.
   *
   * @param className the new class name
   */
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   * Gets the short class name.
   *
   * @return the short class name
   */
  public String getShortClassName() {
    return shortClassName;
  }

  /**
   * Sets the short class name.
   *
   * @param shortClassName the new short class name
   */
  public void setShortClassName(String shortClassName) {
    this.shortClassName = shortClassName;
  }

  /**
   * Gets the super class id.
   *
   * @return the super class id
   */
  public int getSuperClassId() {
    return superClassId;
  }

  /**
   * Sets the super class id.
   *
   * @param superClassId the new super class id
   */
  public void setSuperClassId(int superClassId) {
    this.superClassId = superClassId;
  }

  /**
   * Gets the super class name.
   *
   * @return the super class name
   */
  public String getSuperClassName() {
    return superClassName;
  }

  /**
   * Sets the super class name.
   *
   * @param superClassName the new super class name
   */
  public void setSuperClassName(String superClassName) {
    this.superClassName = superClassName;
  }

  /**
   * Gets the access level.
   *
   * @return the access level
   */
  public String getAccessLevel() {
    return accessLevel;
  }

  /**
   * Sets the access level.
   *
   * @param accessLevel the new access level
   */
  public void setAccessLevel(String accessLevel) {
    this.accessLevel = accessLevel;
  }

  /**
   * Gets the checks if is namespace.
   *
   * @return the checks if is namespace
   */
  public boolean getIsNamespace() {
    return isNamespace;
  }

  /**
   * Sets the checks if is namespace.
   *
   * @param isNamespace the new checks if is namespace
   */
  public void setIsNamespace(boolean isNamespace) {
    this.isNamespace = isNamespace;
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
   * gets the extFormat field
   */
  public String getExtFormat() {
    return extFormat;
  }

  /**
   * set the value of the extFormat
   */
  public void setExtFormat(String extFormat) {
    this.extFormat = extFormat;
  }

  /**
   * Gets the md attributes.
   *
   * @return the md attributes
   */
  public List<CmsClazzAttribute> getMdAttributes() {
    return mdAttributes;
  }

  /**
   * Sets the md attributes.
   *
   * @param attributes the new md attributes
   */
  public void setMdAttributes(List<CmsClazzAttribute> attributes) {
    this.mdAttributes = attributes;
  }

  /**
   * Adds the attribute.
   *
   * @param attribute the attribute
   */
  public void addAttribute(CmsClazzAttribute attribute) {
    this.mdAttributes.add(attribute);
  }

  /**
   * Gets the from relations.
   *
   * @return the from relations
   */
  public List<CmsClazzRelation> getFromRelations() {
    return fromRelations;
  }

  /**
   * Sets the from relations.
   *
   * @param fromRelations the new from relations
   */
  public void setFromRelations(List<CmsClazzRelation> fromRelations) {
    this.fromRelations = fromRelations;
  }

  /**
   * Adds the from relation.
   *
   * @param fromRelation the from relation
   */
  public void addFromRelation(CmsClazzRelation fromRelation) {
    this.fromRelations.add(fromRelation);
  }

  /**
   * Gets the to relations.
   *
   * @return the to relations
   */
  public List<CmsClazzRelation> getToRelations() {
    return toRelations;
  }

  /**
   * Sets the to relations.
   *
   * @param toRelations the new to relations
   */
  public void setToRelations(List<CmsClazzRelation> toRelations) {
    this.toRelations = toRelations;
  }

  /**
   * Adds the to relations.
   *
   * @param toRelation the to relation
   */
  public void addToRelations(CmsClazzRelation toRelation) {
    this.toRelations.add(toRelation);
  }

  /**
   * Gets the actions.
   *
   * @return the actions
   */
  public List<CmsClazzAction> getActions() {
    return actions;
  }

  /**
   * Sets the actions.
   *
   * @param actions the new actions
   */
  public void setActions(List<CmsClazzAction> actions) {
    this.actions = actions;
  }

  /**
   * Adds the action.
   *
   * @param act the act
   */
  public void addAction(CmsClazzAction act) {
    this.actions.add(act);
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "CmsClazz{" +
      "classId=" + classId +
      ", className='" + className + '\'' +
      ", shortClassName='" + shortClassName + '\'' +
      ", superClassId=" + superClassId +
      ", accessLevel='" + accessLevel + '\'' +
      ", isNamespace=" + isNamespace +
      ", description='" + description + '\'' +
      ", created=" + created +
      ", attributes=" + mdAttributes +
      ", fromRelations=" + fromRelations +
      ", toRelations=" + toRelations +
      '}';
  }

  /**
   * Gets the impl.
   *
   * @return the impl
   */
  public String getImpl() {
    return impl;
  }

  /**
   * Sets the impl.
   *
   * @param impl the new impl
   */
  public void setImpl(String impl) {
    this.impl = impl;
  }

  /**
   * Gets the flag indicating that when creating ci of this class and isNamespace set to true use
   * class name in nsPath i.e. /.../class.Name/ciName
   *
   * @return the impl
   */
  public boolean getUseClassNameNS() {
    return this.useClassNameNS;
  }

  /**
   * Sets the flag indicating that when creating ci of this class and isNamespace set to true use
   * class name in nsPath i.e. /.../class.Name/ciName
   *
   * @return the impl
   */
  public void setUseClassNameNS(boolean useClassNameNS) {
    this.useClassNameNS = useClassNameNS;
    if (useClassNameNS) {
      setFlag(USE_CLASS_NAME_NS_FLAG_POS);
    } else {
      unsetFlag(USE_CLASS_NAME_NS_FLAG_POS);
    }
  }

  /**
   * Varios CMS class flags, each bit is used for some attribute
   * i.e 010 is set for useClassNameInNS
   *
   * @return the impl
   */
  public Integer getFlags() {
    return flags;
  }

  /**
   * When class def is read from DB - parse the flags and set properties
   *
   * @return the impl
   */
  public void setFlags(Integer flags) {
    if (flags != null) {
      this.flags = flags;
      this.useClassNameNS = (this.flags & (1 << USE_CLASS_NAME_NS_FLAG_POS)) > 0;
    }
  }

  /**
   * This is needed to mask flags to the end-api calls so users can do GET, update jason (some props
   * like useClassNameInNS) without modifing flags and do PUT
   *
   * @return the impl
   */
  public void setFlagsToNull() {
    this.flags = null;
  }

  private void setFlag(int bitPos) {
    if (this.flags == null) {
      this.flags = 0;
    }
    this.flags |= 1 << bitPos;
  }

  private void unsetFlag(int bitPos) {
    if (this.flags == null) {
      this.flags = 0;
    }
    this.flags &= ~(1 << bitPos);
  }

}
