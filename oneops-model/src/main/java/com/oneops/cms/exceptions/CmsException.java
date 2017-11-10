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
package com.oneops.cms.exceptions;

/**
 * The Class CmsException.
 */
public class CmsException extends CmsBaseException {

  private static final long serialVersionUID = 5476850671630630581L;

  /**
   * Instantiates a new cms exception.
   *
   * @param errorCode the error code
   * @param msg the msg
   */
  public CmsException(int errorCode, String msg) {
    super(errorCode, msg);
  }
}
