package com.oneops.sensor.listeners;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.event.bean.BeanEventBean;
import com.oneops.ops.CiOpsProcessor;
import com.oneops.ops.dao.OpsEventDao;
import com.oneops.ops.events.OpsEvent;
import com.oneops.sensor.jms.OpsEventPublisher;
import com.oneops.sensor.listeners.OpsEventListener;
import com.oneops.sensor.util.SensorHeartBeat;
public class OpsEventListenerTest {

	private  OpsEventListener listener = new OpsEventListener();
	
	@BeforeTest
	public void init(){
		//listener.setOpsCiStateDao(mock(OpsCiStateDao.class));
		listener.setOpsEventDao(mock(OpsEventDao.class));
		listener.setOpsEventPub(mock(OpsEventPublisher.class));
	
		SensorHeartBeat sensorHeartBeat = mock(SensorHeartBeat.class);
		when(sensorHeartBeat.getLatestHearBeatTime(anyString())).thenReturn(1L);
		listener.setSensorHeartBeat(sensorHeartBeat);

	}
	@Test
	public void updateOpsEvtListnerTest(){
	
		EventBean[] newEvents = new EventBean[1];
		EventBean[] oldEvents = null; //keeping null;
		
		OpsEvent underlyingEvent = new OpsEvent();
		underlyingEvent.setType("heartbeat");
		underlyingEvent.setBucket("bucket");
		underlyingEvent.setChecksum(1357888);
		underlyingEvent.setCiId(12);
		underlyingEvent.setCiState("open");
		underlyingEvent.setCount(3);
		underlyingEvent.setGrouping("group-a");
		underlyingEvent.setManifestId(5);
		underlyingEvent.setName("fire");
		underlyingEvent.setSource("computeY");
		underlyingEvent.setCiId(14144);
		underlyingEvent.setTimestamp(123111555);
		underlyingEvent.setCiState("pending");
		
		BeanEventBean eventBean = new BeanEventBean(underlyingEvent, null);
		
		newEvents[0] = eventBean;
				
		CiOpsProcessor coProcessor = mock(CiOpsProcessor.class);
		when(coProcessor.getCIstate(anyLong())).thenReturn("open");
		listener.setCoProcessor(coProcessor);
		listener.update(newEvents, oldEvents);
		
		//no mutation
		assert(newEvents.length==1);
		
		//attempt again, this time the sensSorHeartBeat will appear up to date
		SensorHeartBeat sensorHeartBeatMockTimeStamper = mock(SensorHeartBeat.class);
		when(sensorHeartBeatMockTimeStamper.getLatestHearBeatTime("metrics")).thenReturn(Long.MIN_VALUE);
		listener.setSensorHeartBeat(sensorHeartBeatMockTimeStamper);
		
		when(coProcessor.getCIstate(anyLong())).thenReturn("not-open");

		listener.update(newEvents, oldEvents);
		
		//no mutation
		assert(newEvents.length==1);

	}
 
	 
}
