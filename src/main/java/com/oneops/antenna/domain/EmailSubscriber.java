package com.oneops.antenna.domain;

/**
 * The Class EmailSubscriber.
 */
public class EmailSubscriber extends BasicSubscriber {

    private String email;

    /**
     * Gets the email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email the new email
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
