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
package com.oneops.notification;

/**
 * Notification message types
 */
public enum NotificationType {

  /**
   * Config changes
   */
  ci("ci"),
  /**
   * Deployment events
   */
  deployment("deployment"),
  /**
   * Procedural action events
   */
  procedure("procedure"),

  /**
   * None. Added for filtering
   */
  none("none");


  /**
   * Event name
   */
  private String name;

  NotificationType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
