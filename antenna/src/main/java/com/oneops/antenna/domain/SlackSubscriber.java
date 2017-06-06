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
package com.oneops.antenna.domain;

import java.util.List;

/**
 * Slack sink subscriber.
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 */
public class SlackSubscriber extends BasicSubscriber {

    private List<Channel> channels;
    private List<Format> formats;
    private boolean fieldsOn;

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public List<Format> getFormats() {
        return formats;
    }

    public void setFormats(List<Format> formats) {
        this.formats = formats;
    }

    public boolean isFieldsOn() {
        return fieldsOn;
    }

    public void setFieldsOn(boolean fieldsOn) {
        this.fieldsOn = fieldsOn;
    }

    @Override
    public String toString() {
        return "SlackSubscriber{" +
                "channels=" + channels +
                ", formats=" + formats +
                ", fieldsOn=" + fieldsOn +
                '}';
    }

    /**
     * Holds oneops slack sink channel config.
     */
    public static class Channel {
        private String team;
        private String name;

        public Channel(String team, String name) {
            this.team = team;
            this.name = name;
        }

        public String getTeam() {
            return team;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Channel{" +
                    "team='" + team + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    /**
     * Holds oneops slack sink message format.
     */
    public static class Format {

        private NotificationSeverity level;
        private String pattern;
        private String msg;

        public Format(NotificationSeverity level, String pattern, String msg) {
            this.level = level;
            this.pattern = pattern;
            this.msg = msg;
        }

        public NotificationSeverity getLevel() {
            return level;
        }

        public String getPattern() {
            return pattern;
        }

        public String getMsg() {
            return msg;
        }

        @Override
        public String toString() {
            return "Format{" +
                    "level=" + level +
                    ", pattern='" + pattern + '\'' +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }
}
