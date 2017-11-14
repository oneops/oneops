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

import java.util.HashMap;
import java.util.Map;

/**
 * The Class OpsException.
 */
public class OpsException extends CmsBaseException {

  private static final long serialVersionUID = 9168282442897406033L;
  private Map<String, String> exceptionDetails = new HashMap<String, String>();

  /**
   * Instantiates a new ops exception.
   *
   * @param errorCode the error code
   * @param message the message
   */
  public OpsException(int errorCode, String message) {
    super(errorCode, message);
    this.exceptionDetails = new HashMap<String, String>();
  }

  /**
   * Instantiates new ops exception
   *
   * @param exceptionDetails map
   */
  public OpsException(int errorCode, String message, Map<String, String> exceptionDetails) {
    super(errorCode, message);
    this.exceptionDetails = exceptionDetails;
  }

  /**
   * If the exception is due to a procedure blocking, this method returns the
   * id of the blocking procedure - if no blocker, it returns null
   *
   * @return id or null
   */
  public Long getBlockingProcedureId() {
    if (this.exceptionDetails.containsKey(ExceptionDetailKey.BLOCKING_PROCEDURE_CI_ID.name())) {
      return Long
        .valueOf(this.exceptionDetails.get(ExceptionDetailKey.BLOCKING_PROCEDURE_CI_ID.name()));
    } else {
      return null;
    }
  }

  /**
   * @return the exceptionDetails Map
   */
  public Map<String, String> getExceptionDetails() {
    return exceptionDetails;
  }

  /**
   * Adds an entry into the exceptionDetails Map. Useful for clients who catch
   * this and want to see specifics about the error
   *
   * @param key Entry key in the map
   * @param value Entry value in the map
   * @return this OpsException
   */
  public OpsException set(String key, String value) {
    this.exceptionDetails.put(key, value);
    return this;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("OpsException [exceptionDetails=");
    builder.append(exceptionDetails);
    builder.append(", error=");
    builder.append(error);
    builder.append(", params=");
    builder.append(params);
    builder.append("]");
    return builder.toString();
  }

  public enum ExceptionDetailKey {
    BLOCKING_PROCEDURE_CI_ID, PROCEDURE_ID, PROCEDURE_NAME, PROCEDURE_STATE, MAX_EXEC_ORDER, ARG_LIST
  }


}
