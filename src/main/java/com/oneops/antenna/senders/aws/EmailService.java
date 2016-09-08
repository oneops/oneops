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
package com.oneops.antenna.senders.aws;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.oneops.antenna.domain.BasicSubscriber;
import com.oneops.antenna.domain.EmailMessage;
import com.oneops.antenna.domain.EmailSubscriber;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.senders.NotificationSender;

/**
 * The Class EmailService.
 */
public class EmailService implements NotificationSender {

    private static Logger logger = Logger.getLogger(EmailService.class);

    private static final String fromAddress = "info@oneops.com";

    private String awsAccessKey;
    private String awsSecretKey;
    private AmazonSimpleEmailServiceClient emailClient;


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
     * Inits the.
     */
    public void init() {
        if (this.awsAccessKey == null) {
            this.awsAccessKey = System.getenv("AWS_ACCESS_KEY");
            if (this.awsAccessKey == null) {
                this.awsAccessKey = System.getProperty("com.oneops.aws.accesskey");
            }
        }

        if (this.awsSecretKey == null) {
            this.awsSecretKey = System.getenv("AWS_SECRET_KEY");
            if (this.awsSecretKey == null) {
                this.awsSecretKey = System.getProperty("com.oneops.aws.secretkey");
            }
        }

        emailClient = new AmazonSimpleEmailServiceClient(
                new BasicAWSCredentials(awsAccessKey, awsSecretKey));
    }

    /**
     * Posts the message which must be be given a Subscriber of type EmailSubscriber
     */
    @Override
    public boolean postMessage(NotificationMessage msg,
                               BasicSubscriber subscriber) {

        EmailSubscriber esub;
        if (subscriber instanceof EmailSubscriber) {
            esub = (EmailSubscriber) subscriber;
        } else {
            throw new ClassCastException("invalid subscriber " + subscriber.getClass().getName());
        }

        List<String> emails = new ArrayList<>();
        if (esub.getEmail() != null) {
            emails.add(esub.getEmail());
        }
        if (emails.size() > 0) {
            EmailMessage emsg = buildEMessage(msg, emails);
            return sendEmail(emsg);
        }

        return true;
    }


    /**
     * Send email.
     *
     * @param eMsg the e msg
     * @return true, if successful
     */
    public boolean sendEmail(EmailMessage eMsg) {

        SendEmailRequest request = new SendEmailRequest().withSource(eMsg.getFromAddress());
        Destination dest = new Destination().withToAddresses(eMsg.getToAddresses());
        dest.setCcAddresses(eMsg.getToCcAddresses());
        request.setDestination(dest);
        Content subjContent = new Content().withData(eMsg.getSubject());
        Message msg = new Message().withSubject(subjContent);
        Content textContent = new Content().withData(eMsg.getTxtMessage());
        Body body = new Body().withText(textContent);
        if (eMsg.getHtmlMessage() != null) {
            Content htmlContent = new Content().withData(eMsg.getHtmlMessage());
            body.setHtml(htmlContent);
        }
        msg.setBody(body);
        request.setMessage(msg);
        try {
            emailClient.sendEmail(request);
            logger.debug(msg);
        } catch (AmazonClientException e) {
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }

    private EmailMessage buildEMessage(NotificationMessage nmsg, List<String> toAddresses) {
        if (nmsg.getTemplateName() != null && nmsg.getTemplateName().length() > 0) {
            processTemplate(nmsg);
        }
        EmailMessage emsg = new EmailMessage();
        emsg.setFromAddress(fromAddress);
        emsg.setToAddresses(toAddresses);
        emsg.setSubject(nmsg.getSubject());
        emsg.setTxtMessage(nmsg.getText());

        return emsg;
    }

    private void processTemplate(NotificationMessage msg) {
        // process template and set text prop of a msg
    }

}
