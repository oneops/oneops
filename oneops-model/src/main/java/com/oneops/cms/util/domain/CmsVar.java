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
package com.oneops.cms.util.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * CmsVar class : Will be used to store util configuration data
 */
public class CmsVar implements Serializable {

  private static final long serialVersionUID = 1L;

  private int id;
  private String name;
  private String value;
  private String criteria;
  private Date created;
  private Date updated;

  public CmsVar() {
  }

  public CmsVar(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public CmsVar(String name, String value, String criteria) {
    this.name = name;
    this.value = value;
    this.criteria = criteria;
  }

  /**
   *
   * @return
   */
  public int getId() {
    return id;
  }

  /**
   *
   * @param id
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   *
   * @return
   */
  public String getValue() {
    return value;
  }

  /**
   *
   * @param value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   *
   * @return
   */
  public Date getCreated() {
    return created;
  }

  /**
   *
   * @param created
   */
  public void setCreated(Date created) {
    this.created = created;
  }

  /**
   *
   * @return
   */
  public Date getUpdated() {
    return updated;
  }

  /**
   *
   * @param updated
   */
  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  public String getCriteria() {
    return criteria;
  }

  public void setCriteria(String criteria) {
    this.criteria = criteria;
  }


}
