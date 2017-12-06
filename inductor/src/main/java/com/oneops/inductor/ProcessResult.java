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
package com.oneops.inductor;

import java.util.HashMap;

public class ProcessResult {

  private String stdOut = "";
  private String stdErr = "";
  private int resultCode = -1;
  private boolean isRebooting = false;
  private HashMap<String, String> resultMap = new HashMap<>();
  private HashMap<String, String> faultMap = new HashMap<>();
  private HashMap<String, String> tagMap = new HashMap<>();
  private HashMap<String, String> additionInfoMap = new HashMap<>();

  private String lastError = "";

  public String getLastError() {
    return lastError;
  }

  public void setLastError(String lastError) {
    this.lastError = lastError;
  }

  public String getStdOut() {
    return stdOut;
  }

  public void setStdOut(String stdOut) {
    this.stdOut = stdOut;
  }

  public String getStdErr() {
    return stdErr;
  }

  public void setStdErr(String stdErr) {
    this.stdErr = stdErr;
  }

  public void appendStdErr(String line) {
    this.stdErr += line;
  }

  public void appendStdOut(String line) {
    this.stdOut += line;
  }

  public int getResultCode() {
    return resultCode;
  }

  public void setResultCode(int resultCode) {
    this.resultCode = resultCode;
  }

  public HashMap<String, String> getResultMap() {
    return resultMap;
  }

  public void setResultMap(HashMap<String, String> resultMap) {
    this.resultMap = resultMap;
  }

  public boolean isRebooting() {
    return isRebooting;
  }

  public void setRebooting(boolean isRebooting) {
    this.isRebooting = isRebooting;
  }

  public HashMap<String, String> getFaultMap() {
    return faultMap;
  }

  public void setFaultMap(HashMap<String, String> faultMap) {
    this.faultMap = faultMap;
  }

  public HashMap<String, String> getTagMap() {
    return tagMap;
  }

  public void setTagMap(HashMap<String, String> tagMap) {
    this.tagMap = tagMap;
  }

  public HashMap<String, String> getAdditionInfoMap() {
    return additionInfoMap;
  }

  public void setAdditionInfoMap(HashMap<String, String> additionInfoMap) {
    this.additionInfoMap = additionInfoMap;
  }

}
