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

package com.oneops.inductor;

import com.oneops.cms.domain.CmsWorkOrderSimpleBase;

public class ExecutionContext {

  private final CmsWorkOrderSimpleBase wo;
  private int retryCount;
  private String[] cmd;
  private String logKey;
  private String host;
  private String keyFile;

  public ExecutionContext(CmsWorkOrderSimpleBase wo, String[] cmd, String logKey, int retryCount) {
    this.wo = wo;
    this.cmd = cmd;
    this.logKey = logKey;
    this.retryCount = retryCount;
  }

  /**
   * @param wo work order
   * @param cmd command to execute
   * @param logKey log key
   */
  public ExecutionContext(CmsWorkOrderSimpleBase wo, String[] cmd, String logKey, String host,
      String keyFile, int retryCount) {
    this.wo = wo;
    this.cmd = cmd;
    this.logKey = logKey;
    this.host = host;
    this.keyFile = keyFile;
    this.retryCount = retryCount;
  }

  public CmsWorkOrderSimpleBase getWo() {
    return wo;
  }

  public String[] getCmd() {
    return cmd;
  }

  public void setCmd(String[] cmd) {
    this.cmd = cmd;
  }

  public String getLogKey() {
    return logKey;
  }

  public String getHost() {
    return host;
  }

  public String getKeyFile() {
    return keyFile;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }
}
