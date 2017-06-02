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
package com.oneops.antenna.senders;

import com.oneops.antenna.domain.BasicSubscriber;
import com.oneops.antenna.domain.NotificationMessage;

/**
 * The Interface NotificationSender.
 */
public interface NotificationSender {

    /**
     * Post message.
     *
     * @param msg        the msg
     * @param subscriber the subscriber
     * @return true, if successful
     */
    boolean postMessage(NotificationMessage msg, BasicSubscriber subscriber);
}
