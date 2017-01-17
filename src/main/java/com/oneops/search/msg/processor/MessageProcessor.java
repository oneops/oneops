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
package com.oneops.search.msg.processor;

import com.google.gson.*;
import com.oneops.cms.util.CmsConstants;

import java.lang.reflect.Type;

/**
 * @author ranand
 */
public interface MessageProcessor {
    String EXPJSON_SUFFIX = "_jexp";
    Gson GSON = new Gson();
    Gson GSON_ES = new GsonBuilder().setDateFormat(CmsConstants.SEARCH_TS_PATTERN).registerTypeAdapter(String.class, (JsonDeserializer<String>) (jsonElement, type, jsonDeserializationContext) -> jsonElement.toString()).setDateFormat(CmsConstants.SEARCH_TS_PATTERN).create();


    /**
     * @param message
     * @param msgType
     * @param msgId
     */
    public void processMessage(String message, String msgType, String msgId);

}
