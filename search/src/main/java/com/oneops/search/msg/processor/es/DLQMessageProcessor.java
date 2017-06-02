package com.oneops.search.msg.processor.es;

import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oneops.search.msg.index.Indexer;

/*******************************************************************************
 *
 *   Copyright 2016 Walmart, Inc.
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
public class DLQMessageProcessor {

	private static Logger logger = Logger.getLogger(DLQMessageProcessor.class);

	@Autowired
    private Indexer indexer;

	private static final String PAYLOAD_ELEMENT_KEY = "payLoad";
	private final Gson GSON = new Gson();
	private JsonParser parser = new JsonParser();

    public void processMessage(String message, String msgId, Map<String, String> headers) {
        message = convertMessage(message, headers);
        indexer.index(msgId, "dlq", message);
    }

    private String convertMessage(String message, Map<String, String> headers) {
        String newMessage = message;
        JsonElement msgRootElement = parser.parse(message);
        if (msgRootElement instanceof JsonObject) {
            JsonObject msgRoot = (JsonObject)msgRootElement;

            JsonElement element = msgRoot.get(PAYLOAD_ELEMENT_KEY);
            if (element != null) {
                if (!element.isJsonObject()) {
                    //convert payLoad to a json object if it is not already
                    msgRoot.remove(PAYLOAD_ELEMENT_KEY);
                    String realPayload = element.getAsString();
                    String escapedPayload = StringEscapeUtils.unescapeJava(realPayload);
                    msgRoot.add(PAYLOAD_ELEMENT_KEY, parser.parse(escapedPayload));
                }
            }
            JsonElement hdrElement = GSON.toJsonTree(headers);
            msgRoot.add("msgHeaders", hdrElement);
            newMessage = GSON.toJson(msgRoot);
            if (logger.isDebugEnabled()) {
                logger.debug("message to be indexed " + newMessage);
            }
        }
        return newMessage;
    }

}
