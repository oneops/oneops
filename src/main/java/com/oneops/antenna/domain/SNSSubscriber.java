package com.oneops.antenna.domain;

/**
 * The Class SNSSubscriber.
 */
public class SNSSubscriber extends BasicSubscriber {

    private String snsEndpoint;
    private String awsAccessKey;
    private String awsSecretKey;
    private String snsTopic;

    /**
     * Gets the sns endpoint.
     *
     * @return the sns endpoint
     */
    public String getSnsEndpoint() {
        return snsEndpoint;
    }

    /**
     * Sets the sns endpoint.
     *
     * @param snsEndpoint the new sns endpoint
     */
    public void setSnsEndpoint(String snsEndpoint) {
        this.snsEndpoint = snsEndpoint;
    }

    /**
     * Gets the aws access key.
     *
     * @return the aws access key
     */
    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    /**
     * Sets the aws access key.
     *
     * @param awsAccessKey the new aws access key
     */
    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }

    /**
     * Gets the aws secret key.
     *
     * @return the aws secret key
     */
    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    /**
     * Sets the aws secret key.
     *
     * @param awsSecretKey the new aws secret key
     */
    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    /**
     * Gets the sns topic.
     *
     * @return the sns topic
     */
    public String getSnsTopic() {
        return snsTopic;
    }

    /**
     * Sets the sns topic.
     *
     * @param snsTopic the new sns topic
     */
    public void setSnsTopic(String snsTopic) {
        this.snsTopic = snsTopic;
    }

}
