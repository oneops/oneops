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
 * Base exception that contains CmsError.
 */
public class CmsBaseException extends RuntimeException implements CmsError {

  private static final long serialVersionUID = -223420208060578824L;
  protected int error;
  protected Object params;

  /**
   * Instantiates a new cms base exception.
   */
  public CmsBaseException() {}

  public CmsBaseException(String message, Throwable e) {
    super(message, e);
  }

  /**
   * Instantiates a new cms base exception.
   *
   * @param message the message
   */
  public CmsBaseException(String message) {
    super(message);
  }

  /**
   * Instantiates a new cms base exception.
   *
   * @param errorCode the error code
   * @param mess the mess
   */
  public CmsBaseException(int errorCode, String mess) {
    super(mess);
    this.error = errorCode;
  }

  /**
   * Gets the error code.
   *
   * @return the error code
   */
  public int getErrorCode() {
    return error;
  }

}
