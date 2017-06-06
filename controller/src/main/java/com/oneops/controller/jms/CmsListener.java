/*******************************************************************************
 *
 * Copyright 2015 Walmart, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.oneops.controller.jms;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.oneops.antenna.domain.NotificationSeverity;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.controller.cms.DeploymentNotifier;
import com.oneops.controller.workflow.WorkflowController;
import org.activiti.engine.ActivitiException;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.IndentPrinter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;


/**
 * The listener interface for receiving cms events.
 * The class that is interested in processing a cms
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addCmsListener<code> method. When
 * the cms event occurs, that object's appropriate
 * method is invoked.
 *
 * @see CmsEvent
 */
public class CmsListener implements MessageListener {
	private static Logger logger = Logger.getLogger(CmsListener.class);

	private static final String DPMT_STATE_PENDING = "pending";
	private static final String DPMT_STATE_CANCELED = "canceled";
	//private static final String DPMT_STATE_FAILED = "failed";
	private static final String DPMT_STATE_PAUSED = "paused";
	//private static final String DPMT_STATE_COMPLETE = "complete";
	private static final String dpmtProcessVersion = System.getProperty("controller.dpmt.version", "2");
	private static final String processName = "1".equals(dpmtProcessVersion) ? "deploybom" : "deploybom" + dpmtProcessVersion;
	
	
    private String queueName = "CONTROLLER.WO";
    //private String selector = "source = 'deployment' OR source = 'opsprocedure' OR source = 'release'";
    private Connection connection = null;
    private Session session = null; 
    private String consumerName = "ControllerCMSListener";
	final private Gson gson = new Gson();
	private WorkflowController wfController;
	private ActiveMQConnectionFactory connFactory;
    private DeploymentNotifier notifier;


    public void setNotifier(DeploymentNotifier notifier) {
        this.notifier = notifier;
    }
    /**
     * Sets the conn factory.
     *
     * @param connFactory the new conn factory
     */
    public void setConnFactory(ActiveMQConnectionFactory connFactory) {
		this.connFactory = connFactory;
	}

	/**
	 * Sets the wf controller.
	 *
	 * @param wfController the new wf controller
	 */
	public void setWfController(WorkflowController wfController) {
		this.wfController = wfController;
	}
	

	
	/**
	 * Inits the.
	 *
	 * @throws JMSException the jMS exception
	 */
	public void init() throws JMSException {

		connection = connFactory.createConnection();
		connection.setClientID(consumerName);
		session = connection.createSession(true, Session.SESSION_TRANSACTED);
		
        Queue queue = session.createQueue(queueName);

        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(this);
        connection.start();

        logger.info(">>>>>>>>>>>>>>CmsListener Waiting for messages...");
    }
	
    /* (non-Javadoc)
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
	/**
	 * handles the message; designed for TextMessage types
	 */
    public void onMessage(Message message) {
		try {
	    	if (message instanceof TextMessage) {
	    		try {
					logger.info("got message: " + ((TextMessage)message).getText());
					String procId = processMessage((TextMessage) message);
					if (procId != null) {
	                    if (!procId.equals("skip")) {
	                    	//wfController.pokeWithSubProcess(procId);
	                    	wfController.pokeProcess(procId);
	                    }
					}
					//session.commit();
	    		} catch (ActivitiException ae) {
	    			logger.error("ActivityException in onMessage", ae);
	    			//session.rollback();
	    			throw ae;
	    		}
	    	}
		} catch (JMSException e) {
			logger.error("JMSException in onMessage",e);
		} 
	}	
    
    private String processMessage(TextMessage message) throws JsonSyntaxException, JMSException {
     	String source = message.getStringProperty("source");
        logger.info("Message from source: " + source);
     	String processKey;
        Map<String, Object> wfParams = new HashMap<String, Object>();
        if(source.equals("deployment")) {
            CmsDeployment dpmt = gson.fromJson(message.getText(), CmsDeployment.class);
             if (dpmt == null) {
                 logger.error("Got bad message:" + message.getText() + "/n end msg");
                 return null;
             }
            processKey = getDpmtProcessKey(dpmt);

            if (processKey != null && !processKey.equals("skip")) {
                wfParams.put("dpmt", dpmt);
                wfParams.put("execOrder", 1);
                return wfController.startDpmtProcess(processKey, wfParams);
            } else if (dpmt.getDeploymentState().equalsIgnoreCase(DPMT_STATE_PAUSED)) {
            	sendDeploymentPausedNotification(dpmt);
            } else if (dpmt.getDeploymentState().equalsIgnoreCase(DPMT_STATE_CANCELED)) {
            	sendDeploymentCancelleddNotification(dpmt);
            } else if (dpmt.getDeploymentState().equalsIgnoreCase(DPMT_STATE_PENDING)) {
            	sendDeploymentPendingNotification(dpmt);
            }
            return "skip";
        } else if(source.equals("opsprocedure")) {
            CmsOpsProcedure proc = gson.fromJson(message.getText(), CmsOpsProcedure.class);
             if (proc == null) {
                 logger.error("Got bad message:" + message.getText() + "/n end msg");
                 return null;
             }
            processKey = getOpsProcedureProcessKey(proc);
            if (processKey != null && !processKey.equals("skip")) {
                wfParams.put("proc", proc);
                wfParams.put("execOrder", 1);
            } else {
                return "skip";
            }
            return wfController.startOpsProcess(processKey, wfParams);
            
        } else if(source.equals("release")) {
            CmsRelease release = gson.fromJson(message.getText(), CmsRelease.class);
            if (release == null) {
                logger.error("Got bad message:" + message.getText() + "/n end msg");
                return null;
            }
           processKey = getDeployReleaseProcessKey(release);
           if (processKey != null && !processKey.equals("skip")) {
               wfParams.put("release", release);
           } else {
               return "skip";
           }
           return wfController.startReleaseProcess(processKey, wfParams);
       } else {
            logger.error("Unsupported source:" + source);
            return null;
        }
    }

    private void sendDeploymentPendingNotification(CmsDeployment dpmt) {
    	notifier.sendDeploymentNotification(dpmt, "Deployment in pending state. Initiated by "
    			+ (StringUtils.isBlank(dpmt.getUpdatedBy())?dpmt.getCreatedBy():dpmt.getUpdatedBy()),
        notifier.createDeploymentNotificationText(dpmt), NotificationSeverity.info, null);
	}

	private void sendDeploymentCancelleddNotification(CmsDeployment dpmt) {
		notifier.sendDeploymentNotification(dpmt, "Deployment cancelled by " + dpmt.getUpdatedBy(),
        notifier.createDeploymentNotificationText(dpmt), NotificationSeverity.warning, null);
	}

	private void sendDeploymentPausedNotification(CmsDeployment dpmt) {
        notifier.sendDeploymentNotification(dpmt, "Deployment paused by " + dpmt.getUpdatedBy(),
        notifier.createDeploymentNotificationText(dpmt), NotificationSeverity.info, null);
	}

	private String getDpmtProcessKey(CmsDeployment dpmt) {
    	if ("active".equalsIgnoreCase(dpmt.getDeploymentState())) {
    		return processName;
    	} else {
    		return "skip";
    	}
    }
    
    private String getOpsProcedureProcessKey(CmsOpsProcedure proc) {
    	if (proc.getProcedureState().equals(OpsProcedureState.active)) {
            return "opsprocedure";
        } else {
            return "skip";
        }
    }

    private String getDeployReleaseProcessKey(CmsRelease release) {
    	if (release.getReleaseType() != null && "open".equals(release.getReleaseState()) && release.getReleaseType().equals("oneops::autodeploy")) {
            return "deployrelease";
        } else {
            return "skip";
        }
    }
   
    
    /**
     * Gets the connection stats.
     *
     * @return the connection stats
     */
    public void getConnectionStats() {
        ActiveMQConnection c = (ActiveMQConnection) connection;
        c.getConnectionStats().dump(new IndentPrinter());
    }

    /**
     * Cleanup.
     */
    public void cleanup() {
    	logger.info("Closing AMQ connection");
    	closeConnection();
    }
    
    private void closeConnection() {
        try {
        	session.close();
        	connection.close();
        } catch (Exception ignore) {
        }
    }

	/**
     * Sets the consumer name.
     *
     * @param consumerName the new consumer name
     */
    public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}
	
}