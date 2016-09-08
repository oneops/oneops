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

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.oneops.antenna.domain.BasicSubscriber;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.SNSMessage;
import com.oneops.antenna.domain.SNSSubscriber;
import com.oneops.antenna.senders.NotificationSender;

/**
 * The Class SNSService.
 */
public class SNSService implements NotificationSender {

    private static Logger logger = Logger.getLogger(SNSService.class);

    /**
     * Sends using the sns publisher
     */
    @Override
    public boolean postMessage(NotificationMessage nMsg,
                               BasicSubscriber subscriber) {

        SNSSubscriber sub;
        if (subscriber instanceof SNSSubscriber) {
            sub = (SNSSubscriber) subscriber;
        } else {
            throw new ClassCastException("invalid subscriber " + subscriber.getClass().getName());
        }

        SNSMessage msg = buildSNSMessage(nMsg);
        AmazonSNS sns = new AmazonSNSClient(new BasicAWSCredentials(sub.getAwsAccessKey(), sub.getAwsSecretKey()));
        if (sub.getSnsEndpoint() != null) {
            sns.setEndpoint(sub.getSnsEndpoint());
        }

        CreateTopicRequest tRequest = new CreateTopicRequest();
        tRequest.setName(msg.getTopicName());
        CreateTopicResult result = sns.createTopic(tRequest);

        PublishRequest pr = new PublishRequest(result.getTopicArn(), msg.getTxtMessage()).withSubject(msg.getSubject());

        try {
            PublishResult pubresult = sns.publish(pr);
            logger.info("Published msg with id - " + pubresult.getMessageId());
        } catch (AmazonClientException ace) {
            logger.error(ace.getMessage());
            return false;
        }
        return true;
    }

    private SNSMessage buildSNSMessage(NotificationMessage nmsg) {
        if (nmsg.getTemplateName() != null && nmsg.getTemplateName().length() > 0) {
            processTemplate(nmsg);
        }
        SNSMessage msg = new SNSMessage();
        msg.setTopicName(buildTopicName(nmsg.getNsPath()));
        msg.setSubject(nmsg.getSubject());
        msg.setTxtMessage(nmsg.getText());
        return msg;
    }

    private String buildTopicName(String nsPath) {
        String[] parts = nsPath.split("/");
        String topic = "oneops-" + parts[1] + "-" + parts[2];
        if (parts.length > 3) {
            topic += "-" + parts[3];
        }
        return topic;
    }

    private void processTemplate(NotificationMessage msg) {
        // process template and set text prop of a msg
    }

}
