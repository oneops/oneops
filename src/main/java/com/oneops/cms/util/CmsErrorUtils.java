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
package com.oneops.cms.util;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class CmsErrorUtils.
 */
public class CmsErrorUtils {

    private static Map<Integer,String> messages = new HashMap<Integer, String>();
    static {
        messages.put(3001,"Could not find superClass name: %s");
    }

    /**
     * Gets the message.
     *
     * @param errorCode the error code
     * @param params the params
     * @return the message
     */
    public static String getMessage(int errorCode, Object params) {
        String format = messages.get(errorCode);
        if(params == null) {
            return format;
        } else {
            return String.format(format, params);
        }
    }
}
