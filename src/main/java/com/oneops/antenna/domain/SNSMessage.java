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
package com.oneops.antenna.domain;

/**
 * The Class SNSMessage.
 */
public class SNSMessage extends BasicMessage {

    private String topicName;

    /**
     * Gets the topic name.
     *
     * @return the topic name
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Sets the topic name.
     *
     * @param topicName the new topic name
     */
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}
