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
 * URL sink subscriber. Normally used as HTTP sink.
 */
public class URLSubscriber extends BasicSubscriber {

    /**
     * Http url endpoint
     */
    public String url;

    /**
     * Basic auth user name
     */
    private String userName;

    /**
     * Basic auth password
     */
    public String password;

    /**
     * URL connection timeout
     */
    public int timeout;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("URLSubscriber{ ");
        sb.append("name='").append(getName()).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", userName='").append(userName).append('\'');
        sb.append(", password='").append("*no*").append('\'');
        sb.append(", timeout=").append(timeout);
        sb.append('}');
        return sb.toString();
    }
}
