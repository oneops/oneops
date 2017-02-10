/*******************************************************************************
 *
 *   Copyright 2017 Walmart, Inc.
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
package com.oneops.antenna.senders.slack;

import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

/**
 * An http client for slack web api.
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 * @see <a href="https://api.slack.com/web">Slack Web API</a>
 */
public interface SlackWebClient {

    // Default slack bot name
    String BOT_NAME = "OneOps";

    // For json serialization.
    Gson gson = new Gson();

    /**
     * Sends a message to a channel.
     *
     * @param token   Authentication token of slack user or slack bot that used
     *                for posting the message. Requires <b>chat.write</b> scope.
     * @param channel public/private channel or group to send messages to. Channel
     *                name can be encoded ID or name. Always make sure the user/bot
     *                associated with the {@code token} is part of channel/group.
     * @param text    Text of the message to send.
     * @param options Optional query arguments map for <b>chat.postMessage</b> Web API.
     * @return Slack Web API response call object.
     * @see <a href="https://api.slack.com/methods/chat.postMessage">Chat.postMessage Web API</a>
     */
    @POST("api/chat.postMessage")
    Call<SlackResponse> postMessage(@Query("token") String token,
                                    @Query("channel") String channel,
                                    @Query("text") String text,
                                    @QueryMap Map<String, Object> options);

    /**
     * @see #postMessage(String, String, String, Map)
     */
    default Call<SlackResponse> postMessage(String token, String channel, String text) {
        return postMessage(token, channel, text, emptyMap());
    }

    /**
     * Sends a message to a channel.
     *
     * @param attachments Structured message attachments, {@link Attachment}
     * @see #postMessage(String, String, String, Map)
     */
    default Call<SlackResponse> postMessage(String token, String channel, String text, List<Attachment> attachments) {
        Map<String, Object> options = new HashMap<>(5);
        options.put("username", BOT_NAME);
        options.put("as_user", false);
        options.put("icon_emoji", ":oneops:");
        options.put("mrkdwn", true);
        options.put("attachments", gson.toJson(attachments));
        // options.put("parse", "full");
        return postMessage(token, channel, text, options);
    }

    /**
     * @see #postMessage(String, String, String, List)
     */
    default Call<SlackResponse> postMessage(String token, String channel, String text, Attachment attachment) {
        return postMessage(token, channel, text, singletonList(attachment));
    }
}

