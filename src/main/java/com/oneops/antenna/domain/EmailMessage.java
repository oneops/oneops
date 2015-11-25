package com.oneops.antenna.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class EmailMessage.
 */
public class EmailMessage extends BasicMessage {

    private List<String> toAddresses;
    private List<String> toCcAddresses;
    private String htmlMessage;

    /**
     * Gets the to addresses.
     *
     * @return the to addresses
     */
    public List<String> getToAddresses() {
        return toAddresses;
    }

    /**
     * Sets the to addresses.
     *
     * @param toAddresses the new to addresses
     */
    public void setToAddresses(List<String> toAddresses) {
        this.toAddresses = toAddresses;
    }

    /**
     * Adds the to address.
     *
     * @param toAddress the to address
     */
    public void addToAddress(String toAddress) {
        if (this.toAddresses == null) {
            this.toAddresses = new ArrayList<String>();
        }
        this.toAddresses.add(toAddress);
    }

    /**
     * Gets the to cc addresses.
     *
     * @return the to cc addresses
     */
    public List<String> getToCcAddresses() {
        return toCcAddresses;
    }

    /**
     * Sets the to cc addresses.
     *
     * @param toCcAddresses the new to cc addresses
     */
    public void setToCcAddresses(List<String> toCcAddresses) {
        this.toCcAddresses = toCcAddresses;
    }

    /**
     * Adds the to cc addresses.
     *
     * @param toCcAddress the to cc address
     */
    public void addToCcAddresses(String toCcAddress) {
        if (this.toCcAddresses == null) {
            this.toCcAddresses = new ArrayList<String>();
        }
        this.toCcAddresses.add(toCcAddress);
    }

    /**
     * Gets the html message.
     *
     * @return the html message
     */
    public String getHtmlMessage() {
        return htmlMessage;
    }

    /**
     * Sets the html message.
     *
     * @param htmlMessage the new html message
     */
    public void setHtmlMessage(String htmlMessage) {
        this.htmlMessage = htmlMessage;
    }
}
