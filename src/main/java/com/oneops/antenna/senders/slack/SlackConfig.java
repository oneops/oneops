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

import okhttp3.OkHttpClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toMap;

/**
 * Slack java configuration holds system wide config for slack web api client
 * like, slack web api endpoint, slack tokens maps for all required teams,
 * slack web api client initialization etc. It uses two system/env properties
 * for slack web api call, namely
 * <p>
 * <ul>
 * <li>slack.url - Slack API end point.
 * <li>slack.tokens - A comma separated list of "team=token" string. Eg: walmart1=xoxb-xxxxx,walmart2=xoxb-xxxxx
 * </ul>
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 */
@Configuration
public class SlackConfig {

    private static Logger logger = Logger.getLogger(SlackConfig.class);

    @Autowired
    private Environment env;

    @Value("${slack.url:https://slack.com}")
    private String slackUrl;

    @Value("${slack.timeout.sec:5}")
    private int slackTimeout;

    // A mapping between slack team name and token id.
    private Map<String, String> teamTokenMap = new HashMap<>(2);

    @PostConstruct
    void init() {
        logger.info("Slack Web API Url: " + slackUrl);
        String tokens = env.getProperty("slack.tokens");
        if (tokens != null) {
            teamTokenMap = Arrays.stream(tokens.trim().split(","))
                    .map(s -> s.split("="))
                    .filter(s -> s.length == 2)
                    .collect(toMap(e -> e[0].toLowerCase(), e -> e[1], (a, b) -> a));
        }
        // Don't log slack tokens.
        logger.info("Initialized slack tokens for, " + teamTokenMap.keySet());
    }

    /**
     * Initialize slack web api client.
     *
     * @return {@link SlackWebClient}
     */
    @Bean
    public SlackWebClient getWebClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(slackTimeout, SECONDS)
                .connectTimeout(slackTimeout, SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(slackUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(SlackWebClient.gson))
                .build();
        return retrofit.create(SlackWebClient.class);
    }

    /**
     * Slack Web API base url
     *
     * @return base url.
     */
    public String getSlackUrl() {
        return slackUrl;
    }

    /**
     * Slack token maps with team=token entries.
     */
    public Map<String, String> getTeamTokenMap() {
        return teamTokenMap;
    }
}
