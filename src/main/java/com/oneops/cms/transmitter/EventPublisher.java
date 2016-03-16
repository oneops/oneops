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
package com.oneops.cms.transmitter;

import javax.jms.JMSException;

import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.NotificationSeverity;
import com.oneops.antenna.domain.NotificationType;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.transmitter.domain.CMSEvent;
import com.oneops.cms.transmitter.domain.EventSource;
import com.oneops.util.ReliableExecutor;

public class EventPublisher {

	private CmsPublisher cmsPublisher;
	
	private SearchSender searchPublisher;
	
    private ReliableExecutor<NotificationMessage> antennaClient;

    private NotificationConfigurator notificationConfig;
    
    protected boolean publishControllerEventsAsync;

    public void publishControllerEvents(CMSEvent event) throws JMSException {
    	notifyEvent(event);
    	cmsPublisher.publishMessage(event);
    	//publish to search only if it is async
    	if (publishControllerEventsAsync) {
    		searchPublisher.publishMessage(event);	
    	}
    }
    
    public void publishCIEvents(CMSEvent event) throws JMSException {
    	notifyEvent(event);
    	searchPublisher.publishMessage(event);
    }
    
    public void notifyEvent(CMSEvent event) throws JMSException {

        NotificationMessage notify = createNotificationMessage(event);
        if(notify != null) {
            antennaClient.executeAsync( notify );
        }
    }
    
    private NotificationMessage createNotificationMessage(CMSEvent event) {
        //no config - no notification
        if(!notificationConfig.isConfigured()) {
            return null;
        }
        NotificationMessage notify = new NotificationMessage();
        notify.setTimestamp(System.currentTimeMillis());
        String s = event.getHeaders().get("source");
        String c = event.getHeaders().get("clazzName");
        EventSource source = EventSource.toEventSource(s);
        NotificationType type = null;
        NotificationRule rule = notificationConfig.getRule(source, c);
        if(rule == null) {
            return null;
        }
        if(source.equals(EventSource.deployment)) {
            type = NotificationType.deployment;
            CmsDeployment dp = (CmsDeployment)event.getPayload();
            notify.setSubject("Deployment" );
            notify.setText("Deployment: "+dp.getDescription());
        } else if(source.equals(EventSource.opsprocedure)) {
            type = NotificationType.procedure;
            CmsOpsProcedure proc = (CmsOpsProcedure)event.getPayload();
            notify.setSubject("Procedure");
            notify.setText("Procedure: " + proc.getProcedureName() + " is " + proc.getProcedureState());
        } else if(source.equals(EventSource.cm_ci)) {
            CmsCI ci = (CmsCI)event.getPayload();
            // todo: selection by Ci name
            type = NotificationType.ci;
            notify.setCmsId(ci.getCiId());
            notify.setNsPath(ci.getNsPath());
            notify.setSubject("ci:" + ci.getCiName() );
            notify.setText(ci.getNsPath() + "ci:" + ci.getCiName() );
        } else if(source.equals(EventSource.cm_ci_rel)) {
            type = NotificationType.ci;
            notify.setSubject("ci_rel:" );
            notify.setText("Ci Relation");
        }
        if(rule.getSubject() != null) {
            notify.setSubject(rule.getSubject());
        }
        if(rule.getMessage() != null) {
            notify.setText(rule.getMessage());
        }
        notify.setSource(s);
        notify.setType( type );
        //todo: severity selection is ?
        notify.setSeverity(NotificationSeverity.info);

        return notify;
    }
    
    public void setCmsPublisher(CmsPublisher cmsPublisher) {
		this.cmsPublisher = cmsPublisher;
	}

	public void setSearchPublisher(SearchSender searchPublisher) {
		this.searchPublisher = searchPublisher;
	}

	public void setAntennaClient(ReliableExecutor<NotificationMessage> antennaClient) {
        this.antennaClient = antennaClient;
    }

    public void setNotificationConfig(NotificationConfigurator notificationConfig) {
        this.notificationConfig = notificationConfig;
    }

	public void setPublishControllerEventsAsync(boolean isSearchPublishAsync) {
		this.publishControllerEventsAsync = isSearchPublishAsync;
	}
	
}
