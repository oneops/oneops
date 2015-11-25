package com.oneops.antenna.senders.generic;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.oneops.antenna.domain.BasicSubscriber;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.URLSubscriber;
import com.oneops.antenna.senders.NotificationSender;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import static com.codahale.metrics.MetricRegistry.name;
import static com.oneops.metrics.OneOpsMetrics.ANTENNA;
import static org.springframework.http.HttpStatus.OK;

/**
 * Http message dispatcher. This is basically used to send
 * the notifications to web and other url sinks.
 */
public class HTTPMsgService implements NotificationSender {

    private static Logger logger = Logger.getLogger(HTTPMsgService.class);

    // Metrics
    @Autowired
    private MetricRegistry metrics;
    private Meter http;
    private Meter httpErr;
    private Meter hpom;
    private Meter hpomErr;

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

        boolean isHpom = false;
        try {
            URLSubscriber urlSub = (URLSubscriber) sub;
            isHpom = urlSub.hasHpomXfmr();

            DefaultHttpClient httpClient = new DefaultHttpClient();
            if (urlSub.getUserName() != null) {
                BasicCredentialsProvider cp = new BasicCredentialsProvider();
                cp.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(urlSub.getUserName(),
                                urlSub.getPassword()));
                httpClient.setCredentialsProvider(cp);
            }

            HttpComponentsClientHttpRequestFactory rf = new HttpComponentsClientHttpRequestFactory(httpClient);
            rf.setReadTimeout(2000);
            if (urlSub.getTimeout() > 0) {
                rf.setConnectTimeout(urlSub.getTimeout());
            } else {
                rf.setConnectTimeout(2000);
            }

            RestTemplate restTemplate = new RestTemplate(rf);
            ResponseEntity<String> res = restTemplate.postForEntity(urlSub.getUrl(), msg, String.class);
            if (res.getStatusCode() == OK) {
                countOK(isHpom);
                return true;
            } else {
                logger.warn(isHpom ? "HPOM" : "HTTP" + " message post response code: "
                        + res.getStatusCode()
                        + " for URL sink: "
                        + urlSub.getName());
            }
        } catch (Exception ex) {
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
        if (!isHpom) http.mark();
        else hpom.mark();
    }

    /**
     * Metrics counters for error http and hpom messages.
     *
     * @param isHpom {@code true} if the sink has HPOM transformer configured.
     */
    private void countErr(boolean isHpom) {
        if (!isHpom) httpErr.mark();
        else hpomErr.mark();
    }
}
