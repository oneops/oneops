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
package com.oneops.antenna.ws;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.NotificationSeverity;
import com.oneops.antenna.domain.NotificationType;
import com.oneops.antenna.service.NotificationMessageDao;
import com.oneops.antenna.ws.AntennaWsController;

/** simple tests to run through controller
 * add more to this framework
 */
public class AntennaWsControllerTest {
	
	private AntennaWsController controller;
	private NotificationMessageDao mockDao;


	@BeforeClass
	public void setUp(){
		this.mockDao=mock(NotificationMessageDao.class);
		List<NotificationMessage> notificationsList = new ArrayList<NotificationMessage>();		
		
		List<NotificationMessage> notificationsByRangeList = new ArrayList<NotificationMessage>();

		when(mockDao.getLastNotifications(anyLong(), any(NotificationType.class), any(NotificationSeverity.class), anyString(),anyString(), anyInt(),any(Boolean.class))).thenReturn(notificationsList);
		when(mockDao.getNotificationsByRange(anyLong(), any(NotificationType.class), any(NotificationSeverity.class), anyString(), anyLong(),anyLong(), anyString(), anyInt(),any(Boolean.class))).thenReturn(notificationsByRangeList);
		
		
		this.controller=new AntennaWsController();
		this.controller.setNmDao(mockDao);
		
	}
	

	@Test
	public void getNotificationsForDpmtTest(){
		String source = "mock-source";
		Integer count = 10;
		Long start = 0L;
		Long end = 100L;
		List<NotificationMessage>  outList = this.controller.getNotificationsForDpmt(0, NotificationSeverity.info,source, null, count, start, end, false);
		
	}

	@Test
	public void getNotificationsForProcTest(){
		long procId=13579;
		String source="mock-source";
		Integer count=1;
		Long start=100L;
		Long end=200L;
		List<NotificationMessage> outList = this.controller.getNotificationsForProc(procId, NotificationSeverity.info, source, null, count, start, end, false);
		
	}
	

	@Test
	public void getNotificationForCiTest(){
		String source="mock-source";;
		Integer count=1;
		Long start=100L;
		Long end=200L;
		List<NotificationMessage> outList = this.controller.getNotificationsForCi(0, NotificationSeverity.info, source, null, count, start, end, false);
	}
	

	@Test
	public void getNotificationForNsTest(){
		String source="mock-source";
		Integer count=1;
		Long start=100L;
		Long end=200L;
		String nsPath = "/s/t/u";
		List<NotificationMessage> outList = this.controller.getNotificationsForNS(nsPath, null, null, source, null, count, start, end, false);
	}
	
	
}
