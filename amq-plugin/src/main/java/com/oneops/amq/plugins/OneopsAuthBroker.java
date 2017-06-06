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
package com.oneops.amq.plugins;

import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.Connection;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.ProducerInfo;
import org.apache.log4j.Logger;

import com.oneops.cms.simple.domain.CmsCISimple;

/**
 * The Class OneopsAuthBroker.
 */
public class OneopsAuthBroker extends BrokerFilter {

    private static final Logger logger = Logger.getLogger(OneopsAuthBroker.class);
    private static final String SUPER_USER = "superuser";
    private static final String SYSTEM_USER = "system";
    private static final String QUEUE_SUFFIX = ".ind-wo";

    private String superpass = "-";
    private CMSClient cmsClient;
    private Map<String, String> userMap = new HashMap<String, String>();

    /**
     * Instantiates a new auth broker filter.
     *
     * @param next the next
     * @param cms  the cms
     */
    public OneopsAuthBroker(Broker next, CMSClient cms) {
        super(next);
        this.superpass = System.getenv("KLOOPZ_AMQ_PASS");
        if (this.superpass == null) {
            logger.error("The env var KLOOPZ_AMQ_PASS is not set");
            throw new CmsAuthException("The env var KLOOPZ_AMQ_PASS is not set");
        }
        this.cmsClient = cms;
        userMap.put("system", "*");
    }


    /**
     * Add new connection.
     *
     * @param context
     * @param info
     * @throws Exception
     * @see org.apache.activemq.broker.BrokerFilter#addConnection(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ConnectionInfo)
     */
    public void addConnection(ConnectionContext context, ConnectionInfo info) throws Exception {
        String clientId = info.getClientId();
        String usr = info.getUserName();
        String pass = info.getPassword();

        logger.info("Add new connection { Remote Address: " + context.getConnection().getRemoteAddress()
                + ", User: " + usr + ", ClientID: " + clientId + "  } ");

        if (usr != null && pass != null) {
            if (SYSTEM_USER.equals(usr) || (SUPER_USER.equals(usr) && superpass.equals(pass))) {
                userMap.put(clientId, "*");
            } else {
                String[] parts = usr.split(":");
                if (parts.length > 1) {
                    String ns = parts[0];/*"/public/packer/providers"*/
                    String cloudName = parts[1]; /*"ec2.us-east-1a"*/
                    CmsCISimple cloud = cmsClient.getCloudCi(ns, cloudName);
                    if (cloud != null) {
                        String authkey = cloud.getCiAttributes().get("auth");
                        if (authkey.equals(pass)) {
                            String queueName = (ns.replaceAll("/", ".") + "." + cloudName + QUEUE_SUFFIX).substring(1);
                            userMap.put(clientId, queueName);
                        } else {
                            logger.error("Got bad password for cloud " + cloudName + ", NsPath: " + ns);
                            throw new CmsAuthException("Bad password for user: " + usr);
                        }

                    } else {
                        logger.error("Got null cloud object for user: " + usr);
                        throw new CmsAuthException("Bad user/pass combination");
                    }
                } else {
                    throw new CmsAuthException("Bad user/pass combination");
                }
            }
        } else {
            logger.error("Got null user/pass");
            throw new CmsAuthException("Got null user/pass");
        }
        super.addConnection(context, info);
    }

    /**
     * Add new message consumer.
     *
     * @param context
     * @param info
     * @return
     * @throws Exception
     * @see org.apache.activemq.broker.BrokerFilter#addConsumer(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ConsumerInfo)
     */
    public Subscription addConsumer(ConnectionContext context, ConsumerInfo info) throws Exception {

        ActiveMQDestination dest = info.getDestination();
        Connection conn = context.getConnection();
        if (dest != null) {
            String destName = info.getDestination().getPhysicalName();
            String clientId = context.getClientId();
            String allowedDest = userMap.get(clientId);

            logger.info(">>> Got Consumer Add request { Destination: " + destName
                    + ", Remote Address: " + conn.getRemoteAddress()
                    + ", ClientID: " + clientId
                    + " }");
            if (allowedDest != null && (allowedDest.equals("*") || allowedDest.equals(destName) || destName.startsWith("ActiveMQ"))) {
                logger.info(">>> Subscription allowed");
            } else {
                logger.error(">>> Destination not allowed. Subscription denied!");
                throw new CmsAuthException(">>> Subscription denied!");
            }
        } else {
            logger.error("<<< Got Consumer Add request from Remote Address:" + conn.getRemoteAddress() + ". But destination is NULL.");
        }
        return super.addConsumer(context, info);
    }


    /**
     * Add message producer.
     *
     * @param context
     * @param info
     * @throws Exception
     * @see org.apache.activemq.broker.BrokerFilter#addProducer(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ProducerInfo)
     */
    public void addProducer(ConnectionContext context, ProducerInfo info) throws Exception {

        Connection conn = context.getConnection();
        ActiveMQDestination dest = info.getDestination();
        if (dest != null) {
            String destName = dest.getPhysicalName();
            String clientId = context.getClientId();

            logger.info(">>> Got Producer Add request { Destination: " + destName
                    + ", Remote Address: " + conn.getRemoteAddress()
                    + ", ClientID: " + clientId
                    + " }");

            String allowedDest = userMap.get(context.getClientId());
            if (allowedDest != null && (allowedDest.equals("*") || "controller.response".equals(destName))) {
                logger.info("<<< Producer allowed");
            } else {
                logger.error("<<< Destination not allowed. Producer denied!");
                throw new CmsAuthException("<<< Producer denied!");
            }
        } else {
            logger.error("<<< Got Producer Add request from Remote Address:" + conn.getRemoteAddress() + ". But destination is NULL.");
        }
        super.addProducer(context, info);
    }

	/*
    public void addSession(ConnectionContext context, SessionInfo info) throws Exception {

		if (info != null){
			System.out.println(">>>Got session add request, info -  " + info.toString());
			if ( info.getTo() != null ) {
				System.out.println(">>>Got session add request to - " + info.getTo().getName());
			}
		}
		super.addSession(context, info);
	}
	*/
}
