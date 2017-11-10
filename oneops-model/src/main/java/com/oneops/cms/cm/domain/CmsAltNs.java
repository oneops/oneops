package com.oneops.cms.cm.domain;

/*******************************************************************************
 *
 *   Copyright 2016 Walmart, Inc.
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
public class CmsAltNs {

  private String tag;
  private long nsId;
  private String nsPath;

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public long getNsId() {
    return nsId;
  }

  public void setNsId(long nsId) {
    this.nsId = nsId;
  }

  public String getNsPath() {
    return nsPath;
  }

  public void setNsPath(String nsPath) {
    this.nsPath = nsPath;
  }
}
