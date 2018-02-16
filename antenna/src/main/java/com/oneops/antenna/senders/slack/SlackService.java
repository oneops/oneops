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
import com.oneops.notification.NotificationMessage;
import com.oneops.notification.NotificationSeverity;
import com.oneops.antenna.domain.SlackSubscriber;
import com.oneops.antenna.domain.SlackSubscriber.Format;
import com.oneops.antenna.senders.NotificationSender;
import com.oneops.util.URLUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;
import static com.oneops.antenna.senders.slack.Attachment.*;
import static com.oneops.antenna.senders.slack.SlackWebClient.BOT_NAME;
import static com.oneops.metrics.OneOpsMetrics.ANTENNA;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

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
        Attachment attach = getAttachment(msg, sub.isFieldsOn());

        // The final result would be the reduced value of all parallel post message operations.
        Optional<Boolean> result = sub.getChannels().parallelStream().map((c) -> {
            try {
                String token = slackCfg.getTeamTokenMap().get(c.getTeam());
                if (isEmpty(token)) {
                    logger.error("Slack token is not configured for " + c + ", NsPath: " + msg.getNsPath());
                    errCount.mark();
                    return false;
                }
                SlackResponse res = slackClient.postMessage(token, c.getName(), text, attach).execute().body();
                if (res.isOk()) {
                    msgCount.mark();
                } else {
                    logger.error("Slack msg post failed for " + c + ", NsPath: " + msg.getNsPath() + ", Error: " + res.getError());
                    errCount.mark();
                    return false;
                }
            } catch (Exception ex) {
                // Throws exception if there is some major issue with msg/transport. Needs to log exception here.
                logger.error("Slack msg post failed for " + c + ", NsPath: " + msg.getNsPath(), ex);
                errCount.mark();
                return false;
            }
            return true;
        }).reduce((a, b) -> a & b);
        return result.orElse(false);
    }

    /**
     * Get the formatted text message by applying all format patterns.
     * <b>${text}</b> is the place holder (Case-Insensitive) for current
     * message subject to which the pattern is applied.
     *
     * @param msg  OneOps notification message.
     * @param fmts List of formats configured in sink.
     * @return formatted text message. Returns empty text if {@link NotificationMessage#getSubject()} is <code>null</code>.
     */
    private String getText(NotificationMessage msg, List<Format> fmts) {
        String text = msg.getSubject();
        // Apply message formats
        if (text != null) {
            for (Format fmt : fmts) {
                if (fmt.getLevel() == msg.getSeverity()) {
                    if (text.contains(fmt.getPattern())) {
                        text = fmt.getMsg().replaceAll("(?i)\\$\\{text}", text);
                    }
                }
            }
        } else {
            text = "";
        }
        return text;
    }

    /**
     * Creates an attachment from the notification message.
     *
     * @param msg           OneOps notification message.
     * @param includeFields <code>true</code> if the raw {@link NotificationMessage}
     *                      fields need to be included in the attachment.
     * @return {@link Attachment}
     * @see <a href="https://goo.gl/c4JCBG">Slack Formatting doc</a>
     */
    private Attachment getAttachment(NotificationMessage msg, boolean includeFields) {
        String env = BOT_NAME;
        String[] paths = msg.getNsPath().split("/");
        if (paths.length >= 4) {
            env = paths[3];
        }

        String color = getColor(msg.getSeverity());
        StringBuilder buf = new StringBuilder();
        buf.append(String.format("`%s` | %s | <%s|%s>", env, msg.getType(), URLUtil.getNotificationUrl(msg), msg.getNsPath()));
        String text = msg.getText();
        if (isNotEmpty(text)) {
            buf.append('\n').append(text);
        }

        Attachment attachment = new Attachment().color(color).text(buf.toString());
        // Add notification fields if enabled.
        if (includeFields) {
            // Filter fields with non empty value.
            List<Field> nonEmptyFields = new ArrayList<Field>() {
                {
                    long epocTs = msg.getTimestamp() / 1000;
                    add(new Field("CmsId", msg.getCmsId() + "", true));
                    add(new Field("Cloud", msg.getCloudName(), true));
                    add(new Field("EnvProfile", msg.getEnvironmentProfileName(), true));
                    add(new Field("Severity", msg.getSeverity().getName(), true));
                    add(new Field("Source", msg.getSource(), true));
                    add(new Field("TemplateName", msg.getTemplateName(), true));
                    add(new Field("ManifestCiId", msg.getManifestCiId() + "", true));
                    add(new Field("AdminStatus", msg.getAdminStatus(), true));
                    add(new Field("Timestamp", String.format("<!date^%d^{date_num} {time_secs}|%d>", epocTs, epocTs), true));
                    Map<String, Object> payload = msg.getPayload();
                    if (payload != null) {
                        payload.forEach((key, value) -> add(new Field(key, String.valueOf(value), true)));
                    }
                }
            }.stream().filter((f) -> isNotEmpty(f.getValue())).collect(Collectors.toList());

            attachment.fields(nonEmptyFields);
        }
        return attachment;
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

