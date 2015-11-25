package com.oneops.antenna.domain;

/**
 * The Class SNSMessage.
 */
public class SNSMessage extends BasicMessage {

    private String topicName;

    /**
     * Gets the topic name.
     *
     * @return the topic name
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Sets the topic name.
     *
     * @param topicName the new topic name
     */
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}
