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
package com.oneops.antenna.senders.generic;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.gson.Gson;
import com.oneops.antenna.domain.BasicSubscriber;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.URLSubscriber;
import com.oneops.antenna.senders.NotificationSender;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import javax.annotation.PostConstruct;
import java.io.IOException;

import static com.codahale.metrics.MetricRegistry.name;
import static com.oneops.metrics.OneOpsMetrics.ANTENNA;

/**
 * Http message dispatcher. This is basically used to send
 * the notifications to web and other url sinks.
 */
public class HTTPMsgService implements NotificationSender {

    private static Logger logger = Logger.getLogger(HTTPMsgService.class);
    private final Gson gson = new Gson();

    // Metrics
    private final MetricRegistry metrics;
    private Meter http;
    private Meter httpErr;
    private Meter hpom;
    private Meter hpomErr;

    @Autowired
    public HTTPMsgService(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    @PostConstruct
    public void init() {
        // Meter to measure the rate of messages.
        http = metrics.meter(name(ANTENNA, "http.count"));
        httpErr = metrics.meter(name(ANTENNA, "http.error"));
        hpom = metrics.meter(name(ANTENNA, "hpom.count"));
        hpomErr = metrics.meter(name(ANTENNA, "hpom.error"));
    }

    /**
     * Posts the message to http endpoint
     *
     * @param msg the notification message
     * @param sub URL subscriber
     * @return <code>true</code> if response code is 200, else return <code>false</code>
     */
    @Override
    public boolean postMessage(NotificationMessage msg, BasicSubscriber sub) {
        URLSubscriber urlSub = (URLSubscriber) sub;
        boolean isHpom = urlSub.hasHpomXfmr();

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost req = new HttpPost(urlSub.getUrl());
        req.setEntity(new StringEntity(gson.toJson(msg), ContentType.APPLICATION_JSON));

        int timeout = urlSub.getTimeout();
        req.setConfig(RequestConfig.custom().setSocketTimeout(timeout > 0 ? timeout : 2000).build());
        String userName = urlSub.getUserName();
        if (userName != null) {
            String auth = userName + ":" + urlSub.getPassword();
            req.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.encodeBase64(auth.getBytes())));
        }

        try (CloseableHttpResponse res = httpClient.execute(req)) {
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                countOK(isHpom);
                return true;
            } else {
                logger.warn(isHpom ? "HPOM" : "HTTP" + " message post response code: "
                        + res.getStatusLine().getStatusCode()
                        + " for URL sink: "
                        + urlSub.getName());
            }
        } catch (IOException ex) {
            logger.error(isHpom ? "HPOM" : "HTTP" + " message post failed." + ex.getMessage());
        }

        countErr(isHpom);
        return false;
    }

    /**
     * Metrics counters for successful http and hpom messages.
     * Assuming the http url end point would be of HPOM's if
     * there is an HPOM transformer configured in the sink.
     *
     * @param isHpom {@code true} if the sink has HPOM transformer configured.
     */
    private void countOK(boolean isHpom) {
        if (isHpom) {
            hpom.mark();
        } else {
            http.mark();
        }
    }

    /**
     * Metrics counters for error http and hpom messages.
     *
     * @param isHpom {@code true} if the sink has HPOM transformer configured.
     */
    private void countErr(boolean isHpom) {
        if (isHpom) {
            hpomErr.mark();
        } else {
            httpErr.mark();
        }
    }
}
