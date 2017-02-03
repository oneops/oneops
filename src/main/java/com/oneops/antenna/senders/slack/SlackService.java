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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.oneops.antenna.domain.BasicSubscriber;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.NotificationSeverity;
import com.oneops.antenna.domain.SlackSubscriber;
import com.oneops.antenna.domain.SlackSubscriber.Format;
import com.oneops.antenna.senders.NotificationSender;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;
import static com.oneops.antenna.senders.slack.Attachment.*;
import static com.oneops.antenna.senders.slack.SlackWebClient.BOT_NAME;
import static com.oneops.metrics.OneOpsMetrics.ANTENNA;
import static java.util.Collections.singletonList;

/**
 * Slack message dispatcher. Used to send OneOps notifications
 * to slack public/private channels/groups.
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 */
public class SlackService implements NotificationSender {

    private static Logger logger = Logger.getLogger(SlackService.class);

    // Metrics
    private final MetricRegistry metrics;
    private Meter msgCount;
    private Meter errCount;
    private final SlackConfig slackCfg;
    private final SlackWebClient slackClient;


    @Autowired
    public SlackService(MetricRegistry metrics,
                        SlackConfig slackCfg,
                        SlackWebClient slackClient) {
        this.metrics = metrics;
        this.slackCfg = slackCfg;
        this.slackClient = slackClient;
    }

    @PostConstruct
    public void init() {
        // Meter to measure the rate of messages.
        msgCount = metrics.meter(name(ANTENNA, "slack.count"));
        errCount = metrics.meter(name(ANTENNA, "slack.error"));
    }

    /**
     * Post messages in <b>parallel</b> to all configured slack channels.
     *
     * @param msg        the msg
     * @param subscriber the subscriber
     * @return the accumulated boolean result of all the parallel operations.
     */
    @Override
    public boolean postMessage(NotificationMessage msg, BasicSubscriber subscriber) {
        SlackSubscriber sub = (SlackSubscriber) subscriber;
        String text = getText(msg, sub.getFormats());
        Attachment attch = getAttachment(msg);
        // The final result would be the reduced value of all parallel post message operations.
        Optional<Boolean> result = sub.getChannels().parallelStream().map((c) -> {
            try {
                String token = slackCfg.getTokenMap().get(c.getTeam());
                SlackResponse res = slackClient.postMessage(token, c.getName(), text, attch).execute().body();
                if (res.isOk()) {
                    msgCount.mark();
                } else {
                    logger.error("Slack message posting failed for nsPath: "
                            + msg.getNsPath() + ", Channel: " + c.getName()
                            + ", Error: " + res.getError());
                    errCount.mark();
                }
                return true;
            } catch (Exception e) {
                logger.error("Slack message posting failed for " + msg.getNsPath() + ", Channel: " + c.getName());
                errCount.mark();
                return false;
            }
        }).reduce((a, b) -> a & b);
        return result.orElse(false);
    }

    /**
     * Get the formatted text message by applying all format patterns.
     * <b>${text}</b> is the place holder for current message subject
     * to which the pattern is applied.
     *
     * @param msg  OneOps notification message.
     * @param fmts List of formats configured in sink.
     * @return formatted text message.
     */
    private String getText(NotificationMessage msg, List<Format> fmts) {
        String text = msg.getSubject();
        // Apply message formats
        for (Format fmt : fmts) {
            if (fmt.getLevel() == msg.getSeverity()) {
                if (text.contains(fmt.getPattern())) {
                    text = fmt.getMsg().replaceAll("(?i)\\$\\{text}", text);
                }
            }
        }
        return text;
    }

    /**
     * Creates an attachment from the notification message.
     *
     * @param msg OneOps notification message.
     * @return {@link Attachment}
     */
    private Attachment getAttachment(NotificationMessage msg) {
        String env = BOT_NAME;
        String[] paths = msg.getNsPath().split("/");
        if (paths.length >= 4) {
            env = paths[3];
        }
        String color = getColor(msg.getSeverity());
        String fmtMsg = String.format("`%s` | %s | <%s|%s> \n %s", env, msg.getType(), msg.getNotificationUrl(), msg.getNsPath(), msg.getText());
        return new Attachment()
                .fallback(fmtMsg)
                .color(color)
                .fields(singletonList(new Attachment.Field("", fmtMsg, false)));
    }

    /**
     * Notification message -> Attachment color mapping.
     *
     * @param level notification level
     * @return slack color string.
     */
    private String getColor(NotificationSeverity level) {
        String color;
        switch (level) {
            case warning:
                color = WARN;
                break;
            case critical:
                color = DANGER;
                break;
            case info:
            case none:
            default:
                color = GOOD;
                break;

        }
        return color;
    }
}

