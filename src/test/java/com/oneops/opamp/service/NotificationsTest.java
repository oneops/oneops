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
package com.oneops.opamp.service;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.NotificationSeverity;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.opamp.service.EnvPropsProcessor;
import com.oneops.opamp.service.Notifications;
import com.oneops.opamp.util.EventUtil;
import com.oneops.ops.events.CiChangeStateEvent;
import com.oneops.ops.events.OpsBaseEvent;
import com.oneops.sensor.events.PerfEventPayload;
import com.oneops.util.ReliableExecutor;

/**
 * tester methods for Notification
 *
 */
public class NotificationsTest {
	
	private static final String TEXT_NOTE_SEPERATOR = "; ";

	private static final String SUBJECT_SUFFIX_OPEN_EVENT = " is violated.";
	private static final String SUBJECT_SUFFIX_CLOSE_EVENT = " recovered.";

	private static final String MOCK_CI_CLASS_NAME = "bom.Lb";

	private static final String CI_NAME = "mock-ci-name";

	private ReliableExecutor<NotificationMessage> antennaClientMock;
	
	private CmsCmProcessor cmProcessorMock;
	private EnvPropsProcessor envProcessorMock;
	private static final long CMS_KEY = 123;
	private static final long NOT_FOUND_KEY = 13;

	private static final String NOT_CLOSE = "BAD";


	@BeforeClass
	public void initFields(){
		this.antennaClientMock=new SimpleReliableExecutor();
		this.cmProcessorMock= mock(CmsCmProcessor.class);
		when(cmProcessorMock.getCiById(NOT_FOUND_KEY)).thenReturn(null);
		CmsCI envCi = new CmsCI();
		envCi.setAttributes(new HashMap<String, CmsCIAttribute>());
		envProcessorMock= mock(EnvPropsProcessor.class);
		when(envProcessorMock.getEnv4Bom(anyLong())).thenReturn(envCi);
		CmsCI cmsci = new CmsCI();
		cmsci.setNsPath("/mock/ns/path/test");
		cmsci.setCiName(CI_NAME);
		cmsci.setCiClassName(MOCK_CI_CLASS_NAME);
		when(cmProcessorMock.getCiById(CMS_KEY)).thenReturn(cmsci);
	}

	@Test
	public void opsBaseEventTest(){
		Notifications noti = new Notifications();
		noti.setAntennaClient(antennaClientMock);
		noti.setCmProcessor(cmProcessorMock);
		noti.setEnvProcessor(envProcessorMock);
		EventUtil eventUtil = mock(EventUtil.class);
		noti.setEventUtil(eventUtil);
		//good->notify
		CiChangeStateEvent event = getCiChangeEvent(NOTIFY, GOOD, OPEN, NEW);
		noti.sendOpsEventNotification(event);
		
		CiChangeStateEvent closeEvent = getCiChangeEvent(NOTIFY, GOOD, CLOSE, NEW);
		noti.sendOpsEventNotification(closeEvent);	
		CiChangeStateEvent notValidEvent = getCiChangeEvent(NOTIFY, GOOD, NOT_CLOSE, NEW);
		
		noti.sendOpsEventNotification(notValidEvent);	
		
		noti.sendOpsEventNotification(null);	

	}
	
	@Test
	public void unknownIdTests(){
		Notifications noti = new Notifications();
		noti.setAntennaClient(antennaClientMock);
		noti.setCmProcessor(cmProcessorMock);
		EventUtil eventUtil = mock(EventUtil.class);
		noti.setEventUtil(eventUtil);
		// each of these return. each method is called and each 
		//method first does a lookup finds nothing and just returns
		noti.sendOpsEventNotification(null);	

		CiChangeStateEvent event = new CiChangeStateEvent();
		event.setCiId(NOT_FOUND_KEY);

		noti.sendUnhealthyNotificationNoRepair(event);
		noti.sendFlexNotificationNoRepair(event,"");
		noti.sendFlexNotificationLimitIsReached(event,"");
		noti.sendFlexNotificationLimitIsReached(event,"overutilized");

		noti.sendFlexNotificationProcessing(event,"",1);
		noti.sendFlexNotificationProcessing(event,"overutilized",1);

		noti.sendFlexNotificationErrorProcessing(event,"","");
		noti.sendFlexNotificationPostponeProcessing(event,"");
		noti.sendGoodNotification(event);
		noti.sendDependsOnUnhealthyNotification(event);
		noti.sendRepairNotification(event, null);
		noti.sendRepairCriticalNotification(event, null);
		noti.sendPostponedRepairNotification(event, null);
		
	}
	@Test
	public void testWithCiMock(){
		Notifications noti = new Notifications();
		EventUtil eventUtil = new EventUtil();
		eventUtil.setGson(new Gson());
		noti.setEventUtil(eventUtil);
		noti.setAntennaClient(antennaClientMock);
		noti.setCmProcessor(cmProcessorMock);
		noti.setEnvProcessor(envProcessorMock);

		noti.sendUnhealthyNotificationNoRepair(getCiChangeEvent(UNHEALTHY, GOOD, OPEN, NEW));
 		noti.sendFlexNotificationNoRepair(getCiChangeEvent(OVERUTILIZED, GOOD, OPEN, NEW),"");
 		noti.sendFlexNotificationLimitIsReached(getCiChangeEvent(OVERUTILIZED, GOOD, OPEN, NEW),"");
 		noti.sendFlexNotificationProcessing(getCiChangeEvent(OVERUTILIZED, GOOD, OPEN, NEW),"",1);
 		noti.sendFlexNotificationErrorProcessing(getCiChangeEvent(OVERUTILIZED, GOOD, OPEN, NEW),"","");
 		noti.sendFlexNotificationPostponeProcessing(getCiChangeEvent(OVERUTILIZED, GOOD, OPEN, NEW),"");
 		noti.sendGoodNotification(getCiChangeEvent(OVERUTILIZED, GOOD, OPEN, NEW));
 		noti.sendDependsOnUnhealthyNotification(getCiChangeEvent(UNHEALTHY, GOOD, OPEN, NEW));
 		noti.sendRepairNotification(getCiChangeEvent(UNHEALTHY, GOOD, OPEN, NEW), null);
 		noti.sendRepairCriticalNotification(getCiChangeEvent(UNHEALTHY, GOOD, OPEN, NEW), null);
 		noti.sendPostponedRepairNotification(getCiChangeEvent(UNHEALTHY, GOOD, OPEN, NEW),null);
 }
	
	
	
	
	
	
	private class SimpleReliableExecutor extends ReliableExecutor<NotificationMessage> {

		@Override
		protected boolean process(NotificationMessage arg0) {
			// do nothing
			return false;
		}
		
        public void executeAsync(NotificationMessage param) {
    }

    public boolean executeSync(NotificationMessage param) {

		return false; 
		}
		
	}
	
	@Test
    public void testReplaceNotificationForOpenEvent(){
		Notifications noti = new Notifications();
		noti.setAntennaClient(antennaClientMock);
		noti.setCmProcessor(cmProcessorMock);
		noti.setEnvProcessor(envProcessorMock);
		EventUtil eventUtil = new EventUtil();
		eventUtil.setGson(new Gson());
		noti.setEventUtil(eventUtil);
		CiChangeStateEvent event = getCiChangeEvent(UNHEALTHY, NOTIFY, OPEN, NEW);
		OpsBaseEvent opsEvent = eventUtil.getOpsEvent(event);
		NotificationMessage message =  noti.sendReplaceNotification(event);
		String subject =  message.getSubject();
		String text = message.getText();
		Assert.assertEquals(subject, getSubjPrefix() +opsEvent.getName()+SUBJECT_SUFFIX_OPEN_EVENT);
		Assert.assertEquals(text, CI_NAME +" is in "+event.getNewState()+" state"+"; Could not repair, attempting to replace." );
		Assert.assertEquals(message.getSource(),"ops");
		Assert.assertEquals(message.getPayload().get(Notifications.CLASS_NAME),MOCK_CI_CLASS_NAME);
		Assert.assertEquals(message.getPayload().get(Notifications.OLD_STATE),NOTIFY);
		Assert.assertEquals(message.getPayload().get(Notifications.NEW_STATE),UNHEALTHY);
		Assert.assertEquals(message.getPayload().get(Notifications.STATE),OPEN);
		Assert.assertEquals(message.getSeverity(), NotificationSeverity.warning);
	}

	private String getSubjPrefix() {
		return NotificationMessage.buildSubjectPrefix(cmProcessorMock.getCiById(CMS_KEY).getNsPath());
	}

	@Test
    public void testPostponedReplaceNotification(){
		Notifications noti = new Notifications();
		noti.setAntennaClient(antennaClientMock);
		noti.setCmProcessor(cmProcessorMock);
		noti.setEnvProcessor(envProcessorMock);
		EventUtil eventUtil = new EventUtil();
		eventUtil.setGson(new Gson());
		noti.setEventUtil(eventUtil);
		CiChangeStateEvent event = getCiChangeEvent(UNHEALTHY, NOTIFY, OPEN, NEW);
		OpsBaseEvent opsEvent = eventUtil.getOpsEvent(event);
		NotificationMessage message = noti.sendPostponedReplaceNotification(event);
		String subject = message.getSubject();
		String text = message.getText();
		Assert.assertEquals(subject, getSubjPrefix()+opsEvent.getName()+SUBJECT_SUFFIX_OPEN_EVENT);
		Assert.assertEquals(text, CI_NAME +" is in "+event.getNewState()+" state"+TEXT_NOTE_SEPERATOR+Notifications.REPLACE_POSTPONED_NOTIFICATION);
		Assert.assertEquals(message.getSource(),"ops");
		Assert.assertEquals(message.getPayload().get(Notifications.CLASS_NAME),MOCK_CI_CLASS_NAME);
		Assert.assertEquals(message.getPayload().get(Notifications.OLD_STATE),NOTIFY);
		Assert.assertEquals(message.getPayload().get(Notifications.NEW_STATE),UNHEALTHY);
		Assert.assertEquals(message.getPayload().get(Notifications.STATUS),NEW);
		Assert.assertEquals(message.getPayload().get(Notifications.STATE),OPEN);
		Assert.assertEquals(message.getSeverity(), NotificationSeverity.critical);
	}

	@Test
    public void testRepairNotification(){
		Notifications noti = new Notifications();
		noti.setAntennaClient(antennaClientMock);
		noti.setCmProcessor(cmProcessorMock);
		noti.setEnvProcessor(envProcessorMock);
		EventUtil eventUtil = new EventUtil();
		eventUtil.setGson(new Gson());
		noti.setEventUtil(eventUtil);
		CiChangeStateEvent event = getCiChangeEvent(UNHEALTHY, NOTIFY, OPEN, NEW);
		OpsBaseEvent opsEvent = eventUtil.getOpsEvent(event);
		NotificationMessage message = noti.sendRepairNotification(event, null);
		String subject = message.getSubject();
		String text = message.getText();
		Assert.assertEquals(subject, getSubjPrefix()+opsEvent.getName()+SUBJECT_SUFFIX_OPEN_EVENT);
		Assert.assertEquals(text, CI_NAME +" is in "+event.getNewState()+" state"+TEXT_NOTE_SEPERATOR+Notifications.REPAIR_IN_PROGRESS);
		Assert.assertEquals(message.getSource(),"ops");
		Assert.assertEquals(message.getPayload().get(Notifications.CLASS_NAME),MOCK_CI_CLASS_NAME);
		Assert.assertEquals(message.getPayload().get(Notifications.OLD_STATE),NOTIFY);
		Assert.assertEquals(message.getPayload().get(Notifications.NEW_STATE),UNHEALTHY);
		Assert.assertEquals(message.getPayload().get(Notifications.STATUS),NEW);
		Assert.assertEquals(message.getPayload().get(Notifications.STATE),OPEN);
		Assert.assertEquals(message.getSeverity(), NotificationSeverity.warning);
	}
	@Test
    public void testPostPonedRepairNotification(){
		Notifications noti = new Notifications();
		noti.setAntennaClient(antennaClientMock);
		
		noti.setCmProcessor(cmProcessorMock);
		noti.setEnvProcessor(envProcessorMock);
		EventUtil eventUtil = new EventUtil();
		eventUtil.setGson(new Gson());
		noti.setEventUtil(eventUtil);
		CiChangeStateEvent event = getCiChangeEvent(UNHEALTHY, NOTIFY, OPEN, NEW);
		OpsBaseEvent opsEvent = eventUtil.getOpsEvent(event);
		NotificationMessage message = noti.sendPostponedRepairNotification(event, null);
		String subject = message.getSubject();
		String text = message.getText();
		Assert.assertEquals(subject, getSubjPrefix()+opsEvent.getName()+SUBJECT_SUFFIX_OPEN_EVENT);
		Assert.assertEquals(text, CI_NAME +" is in "+event.getNewState()+" state"+TEXT_NOTE_SEPERATOR+Notifications.REPAIR_POSTPONED);
		Assert.assertEquals(message.getSource(),"ops");
		Assert.assertEquals(message.getPayload().get(Notifications.CLASS_NAME),MOCK_CI_CLASS_NAME);
		Assert.assertEquals(message.getPayload().get(Notifications.OLD_STATE),NOTIFY);
		Assert.assertEquals(message.getPayload().get(Notifications.NEW_STATE),UNHEALTHY);
		Assert.assertEquals(message.getPayload().get(Notifications.STATUS),NEW);
		Assert.assertEquals(message.getSeverity(), NotificationSeverity.warning);
		Assert.assertEquals(message.getPayload().get(Notifications.STATE),OPEN);
	}
	@Test
    public void testSendDependsOnUnhealthyNotification(){
		Notifications noti = new Notifications();
		noti.setAntennaClient(antennaClientMock);
		noti.setCmProcessor(cmProcessorMock);
		noti.setEnvProcessor(envProcessorMock);
		EventUtil eventUtil = new EventUtil();
		eventUtil.setGson(new Gson());
		noti.setEventUtil(eventUtil);
		CiChangeStateEvent event = getCiChangeEvent(UNHEALTHY, NOTIFY, OPEN, NEW);
		OpsBaseEvent opsEvent = eventUtil.getOpsEvent(event);
		NotificationMessage message = noti.sendDependsOnUnhealthyNotification(event);
		String subject = message.getSubject();
		String text = message.getText();
		Assert.assertEquals(subject, getSubjPrefix()+opsEvent.getName()+SUBJECT_SUFFIX_OPEN_EVENT);
		Assert.assertEquals(text, CI_NAME +" is in "+event.getNewState()+" state"+TEXT_NOTE_SEPERATOR+Notifications.WAITING_ON_DEPENDENT_REPAIR);
		Assert.assertEquals(message.getSource(),"ops");
		Assert.assertEquals(message.getPayload().get(Notifications.CLASS_NAME),MOCK_CI_CLASS_NAME);
		Assert.assertEquals(message.getPayload().get(Notifications.OLD_STATE),NOTIFY);
		Assert.assertEquals(message.getPayload().get(Notifications.NEW_STATE),UNHEALTHY);
		Assert.assertEquals(message.getPayload().get(Notifications.STATUS),NEW);
		Assert.assertEquals(message.getSeverity(), NotificationSeverity.warning);
		Assert.assertEquals(message.getPayload().get(Notifications.STATE),OPEN);
	}




    @Test
    public void testSendOpenEventNotificationWithDefault(){
		Notifications noti = new Notifications();
		noti.setAntennaClient(antennaClientMock);
		noti.setCmProcessor(cmProcessorMock);
		noti.setEnvProcessor(envProcessorMock);
		EventUtil eventUtil = new EventUtil();
		eventUtil.setGson(new Gson());
		noti.setEventUtil(eventUtil);
		CiChangeStateEvent event = getCiChangeEvent(NOTIFY, NOTIFY, OPEN, EXISTING);
		OpsBaseEvent opsEvent = eventUtil.getOpsEvent(event);
		NotificationMessage message = noti.sendOpsEventNotification(event);
		String subject = message.getSubject();
		String text = message.getText();
		Assert.assertEquals(subject,getSubjPrefix()+ opsEvent.getName()+SUBJECT_SUFFIX_OPEN_EVENT);
		Assert.assertEquals(text, CI_NAME +" is in "+event.getNewState()+" state.");
		Assert.assertEquals(message.getSource(),"ops");
		Assert.assertEquals(message.getPayload().get(Notifications.CLASS_NAME),MOCK_CI_CLASS_NAME);
		Assert.assertEquals(message.getPayload().get(Notifications.OLD_STATE),NOTIFY);
		Assert.assertEquals(message.getPayload().get(Notifications.NEW_STATE),NOTIFY);
		Assert.assertEquals(message.getPayload().get(Notifications.STATUS),EXISTING);
		Assert.assertEquals(message.getSeverity(), NotificationSeverity.warning);
		Assert.assertEquals(message.getPayload().get(Notifications.STATE),OPEN);
	}

    @Test
    public void testSendCloseEventNotificationWithDefault(){
        Notifications noti = new Notifications();
        noti.setAntennaClient(antennaClientMock);
        noti.setCmProcessor(cmProcessorMock);
        noti.setEnvProcessor(envProcessorMock);
        EventUtil eventUtil = new EventUtil();
        eventUtil.setGson(new Gson());
        noti.setEventUtil(eventUtil);
        CiChangeStateEvent event = getCiChangeEvent(NOTIFY, GOOD, CLOSE, EXISTING);
        OpsBaseEvent opsEvent = eventUtil.getOpsEvent(event);
        NotificationMessage message = noti.sendOpsEventNotification(event);
        String subject = message.getSubject();
        String text = message.getText();
        //Assert.assertEquals(subject, opsEvent.getName()+SUBJECT_SUFFIX_CLOSE_EVENT);
        Assert.assertEquals(text, CI_NAME +" is in "+event.getNewState()+" state.");
        Assert.assertEquals(message.getSource(),"ops");
        Assert.assertEquals(message.getPayload().get(Notifications.CLASS_NAME),MOCK_CI_CLASS_NAME);
        Assert.assertEquals(message.getPayload().get(Notifications.NEW_STATE),NOTIFY);
        Assert.assertEquals(message.getPayload().get(Notifications.OLD_STATE),GOOD);
        Assert.assertEquals(message.getPayload().get(Notifications.STATUS),EXISTING);
        Assert.assertEquals(message.getSeverity(), NotificationSeverity.info);
        Assert.assertEquals(message.getPayload().get(Notifications.STATE),CLOSE);
    }
    private static final String EXISTING = "existing";
    private static final String NOTIFY = "notify";
    private static final String CLOSE = "close";
    private static final String NEW = "new";
    private static final String OPEN = "open";
    private static final String UNHEALTHY = "unhealthy";
    private static final String UNDERUTILIZED = "underutilized";
    private static final String OVERUTILIZED = "overutilized";
    private static final String GOOD = "good";

    private CiChangeStateEvent getCiChangeEvent(String newState, String oldState, String state, String status)
    {
        CiChangeStateEvent ccse = new CiChangeStateEvent();
        ccse.setCiId(CMS_KEY);
        ccse.setNewState(newState);
        ccse.setOldState(oldState);

        OpsBaseEvent obe = new OpsBaseEvent();
        obe.setBucket("mockBucket");
        obe.setName("app-tomcat-daemon-tomcatprocess:P:APP:US:Pr:Prc:java");
        obe.setStatus(status);
        obe.setState(state);
        obe.setCiId(CMS_KEY);

        obe.setManifestId(75l);
        obe.setSource("source");
        Gson gson = new Gson();
        PerfEventPayload metrics =new PerfEventPayload();
        metrics.addAvg("total", 5.609462539);
        metrics.addAvg("total", 1.198);
        metrics.addAvg("max", 1.104);
        metrics.addAvg("percentUsed", 88.52);
		obe.setMetrics(metrics);
        ccse.setPayLoad(gson.toJson(obe));
        return ccse;
    }

}