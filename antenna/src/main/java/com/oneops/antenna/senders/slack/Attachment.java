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

import com.google.gson.annotations.SerializedName;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Structured slack message attachment.
 * ToDo - check other libs to reduce these boilerplate.
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 * @see <a href="https://api.slack.com/docs/message-attachments">Slack message attachments</a>
 */
public class Attachment {

    // Attachment message color codes.
    public static final String GOOD = "good";
    public static final String WARN = "warning";
    public static final String DANGER = "danger";

    private String fallback;

    private String color;

    private String pretext;

    @SerializedName("author_name")
    private String authorName;

    @SerializedName("author_link")
    private String authorLink;

    @SerializedName("author_icon")
    private String authorIcon;

    private String title;

    @SerializedName("title_link")
    private String titleLink;

    private String text;

    private List<Field> fields;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("thumb_url")
    private String thumbUrl;

    private String footer;

    @SerializedName("footer_icon")
    private String footerIcon;

    @SerializedName("mrkdwn_in")
    private List<String> mrkdwnIn = asList("text", "pretext", "fields");


    public Attachment fallback(String fallback) {
        this.fallback = fallback;
        return this;
    }

    public Attachment color(String color) {
        this.color = color;
        return this;
    }

    public Attachment pretext(String pretext) {
        this.pretext = pretext;
        return this;
    }

    public Attachment authorName(String authorName) {
        this.authorName = authorName;
        return this;
    }

    public Attachment authorLink(String authorLink) {
        this.authorLink = authorLink;
        return this;
    }

    public Attachment authorIcon(String authorIcon) {
        this.authorIcon = authorIcon;
        return this;
    }

    public Attachment title(String title) {
        this.title = title;
        return this;
    }

    public Attachment titleLink(String titleLink) {
        this.titleLink = titleLink;
        return this;
    }

    public Attachment text(String text) {
        this.text = text;
        return this;
    }

    public Attachment fields(List<Field> fields) {
        this.fields = fields;
        return this;
    }

    public Attachment imageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public Attachment thumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
        return this;
    }

    public Attachment footer(String footer) {
        this.footer = footer;
        return this;
    }

    public Attachment footerIcon(String footerIcon) {
        this.footerIcon = footerIcon;
        return this;
    }

    public Attachment mrkdwnIn(List<String> mrkdwnIn) {
        this.mrkdwnIn = mrkdwnIn;
        return this;
    }

    public String getFallback() {
        return fallback;
    }

    public String getColor() {
        return color;
    }

    public String getPretext() {
        return pretext;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorLink() {
        return authorLink;
    }

    public String getAuthorIcon() {
        return authorIcon;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleLink() {
        return titleLink;
    }

    public String getText() {
        return text;
    }

    public List<Field> getFields() {
        return fields;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public String getFooter() {
        return footer;
    }

    public String getFooterIcon() {
        return footerIcon;
    }

    public List<String> getMrkdwnIn() {
        return mrkdwnIn;
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "fallback='" + fallback + '\'' +
                ", color='" + color + '\'' +
                ", pretext='" + pretext + '\'' +
                ", authorName='" + authorName + '\'' +
                ", authorLink='" + authorLink + '\'' +
                ", authorIcon='" + authorIcon + '\'' +
                ", title='" + title + '\'' +
                ", titleLink='" + titleLink + '\'' +
                ", text='" + text + '\'' +
                ", fields=" + fields +
                ", imageUrl='" + imageUrl + '\'' +
                ", thumbUrl='" + thumbUrl + '\'' +
                ", footer='" + footer + '\'' +
                ", footerIcon='" + footerIcon + '\'' +
                ", mrkdwnIn=" + mrkdwnIn +
                '}';
    }

    public static class Field {

        private String title;

        private String value;

        @SerializedName("short")
        private boolean shortVal;

        public Field(String title, String value) {
            this(title, value, false);
        }

        public Field(String title, String value, boolean shortVal) {
            this.title = title;
            this.value = value;
            this.shortVal = shortVal;
        }

        public String getTitle() {
            return title;
        }

        public String getValue() {
            return value;
        }

        public boolean isShortVal() {
            return shortVal;
        }

        @Override
        public String toString() {
            return "Field{" +
                    "title='" + title + '\'' +
                    ", value='" + value + '\'' +
                    ", shortVal=" + shortVal +
                    '}';
        }
    }
}
