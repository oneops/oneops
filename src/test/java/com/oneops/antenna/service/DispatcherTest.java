package com.oneops.antenna.service;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.oneops.antenna.domain.BasicSubscriber;
import com.oneops.antenna.domain.EmailSubscriber;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.NotificationType;
import com.oneops.antenna.domain.SNSSubscriber;
import com.oneops.antenna.domain.URLSubscriber;
import com.oneops.antenna.senders.NotificationSender;
import com.oneops.antenna.service.Dispatcher;
import com.oneops.antenna.service.NotificationMessageDao;
import com.oneops.antenna.subscriptions.SubscriberService;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.ops.service.OpsProcedureProcessor;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.service.CmsDpmtProcessor;

import static org.mockito.Mockito.*;


public class DispatcherTest {
	private static final long TEST_CID = 0;
	private Dispatcher dispatcher;
	private CmsCmProcessor cmProcessor;
	private SubscriberService subService;

	
	
 
	@BeforeClass
	public void init(){
		this.dispatcher=new Dispatcher();
		NotificationMessageDao ndao = mock(NotificationMessageDao.class);
		dispatcher.setNmDao(ndao); //null behavior
		
		SubscriberService subService = mock(SubscriberService.class);
		dispatcher.setSbrService(subService);

		cmProcessor = mock(CmsCmProcessor.class);
		CmsCI ci = new CmsCI();
		ci.setNsPath("/a/b");
		when(cmProcessor.getCiById(anyLong())).thenReturn(ci);
		
		subService = mock(SubscriberService.class);
		List<BasicSubscriber> bsList = new ArrayList<BasicSubscriber>(4);
		BasicSubscriber basic1 = new EmailSubscriber();
		BasicSubscriber basic2 = new SNSSubscriber();
		BasicSubscriber basic3 = new URLSubscriber();
		BasicSubscriber basic4 = new BasicSubscriber();
		bsList.add(basic1);
		bsList.add(basic2);
		bsList.add(basic3);
		bsList.add(basic4);
		when(subService.getSubscribersForNs(anyString())).thenReturn(bsList);
		
		this.dispatcher.setCmProcessor(cmProcessor);
		this.dispatcher.setGson(new Gson());
		NotificationSender notMock = mock(NotificationSender.class);
		this.dispatcher.setSnsSender(notMock);
		this.dispatcher.seteSender(notMock);
		this.dispatcher.setDpmtProcessor(mock(CmsDpmtProcessor.class));
		this.dispatcher.setProcProcessor(mock(OpsProcedureProcessor.class));


		
	}
	
	 
	@Test
	/** runs dispatch with mocks mainly
	 * expect normal flow no runtime exceptions */
	public void testDispatch(){
		
		NotificationMessage notificationMessage = new NotificationMessage();
		notificationMessage.setNsPath("/a/b");
		notificationMessage.setType(NotificationType.ci); //deployment procedure and OTHER
		notificationMessage.setCmsId(TEST_CID);
		
		this.dispatcher.dispatch(notificationMessage);
	}
	
	@Test
	/** runs dispatch with mocks mainly
	 * again with null nspath */
	public void testDispatchNulNs(){
		
		NotificationMessage notificationMessage = new NotificationMessage();
		notificationMessage.setNsPath(null);
		notificationMessage.setCmsId(TEST_CID);
		notificationMessage.setType(NotificationType.deployment); //deployment procedure and OTHER
		this.dispatcher.dispatch(notificationMessage);
		notificationMessage.setType(NotificationType.procedure);  
		this.dispatcher.dispatch(notificationMessage);
		notificationMessage.setType(NotificationType.ci);  
		this.dispatcher.dispatch(notificationMessage);
	}
}


