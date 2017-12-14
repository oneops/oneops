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

public class CmsLink {

  private long fromCiId;
  private String fromClazzName;
  private String relationName;
  private long toCiId;
  private String toClazzName;

  public long getFromCiId() {
    return fromCiId;
  }

  public void setFromCiId(long fromCiId) {
    this.fromCiId = fromCiId;
  }

  public String getFromClazzName() {
    return fromClazzName;
  }

  public void setFromClazzName(String fromClazzName) {
    this.fromClazzName = fromClazzName;
  }

  public String getRelationName() {
    return relationName;
  }

  public void setRelationName(String relationName) {
    this.relationName = relationName;
  }

  public long getToCiId() {
    return toCiId;
  }

  public void setToCiId(long toCiId) {
    this.toCiId = toCiId;
  }

  public String getToClazzName() {
    return toClazzName;
  }

  public void setToClazzName(String toClazzName) {
    this.toClazzName = toClazzName;
  }


}
