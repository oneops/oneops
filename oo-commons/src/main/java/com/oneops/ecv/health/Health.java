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
package com.oneops.ecv.health;


import com.oneops.util.Version;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by glall on 10/30/14.
 */
public class Health implements IHealth {


    private int statusCode = HttpServletResponse.SC_OK;
    private boolean isOKstatus = Boolean.TRUE;
    private String message;
    private String name;

    @Autowired
    private transient Version version;

    public Health() {
        this.name = this.getClass().getName();
        this.message = "Default health : oK ";
    }

    public Health(String message, String name) {
        this.message = message;
        this.name = name;
    }


    public Health(String message) {
        this.message = message;
        this.name = this.getClass().getName();
    }

    public Health(int statusCode, boolean isOKstatus, String message, String name) {
        this.statusCode = statusCode;
        this.isOKstatus = isOKstatus;
        this.message = message;
        this.name = name;
    }

    public Health(int statusCode, boolean isOKstatus) {
        this.statusCode = statusCode;
        this.isOKstatus = isOKstatus;
    }


    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public boolean isOK() {
        return isOKstatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        if (version != null)
            return version.getGitVersion();
        return VERSION;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Health{");
        sb.append("statusCode=").append(statusCode);
        sb.append(", isOKstatus=").append(isOKstatus);
        sb.append(", message='").append(message).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", version='").append(getVersion()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
