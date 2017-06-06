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
package com.oneops.antenna.domain.filter;

import com.oneops.antenna.domain.NotificationMessage;

/**
 * A filter interface for notification messages.
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 * @version 1.0
 */
public interface MessageFilter {
    /**
     * Tests whether or not the specified notification message to be filtered out.
     *
     * @param msg {@link  com.oneops.antenna.domain.NotificationMessage}
     * @return true if and only if message should be included
     */
    boolean accept(NotificationMessage msg);
}
