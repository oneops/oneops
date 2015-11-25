package com.oneops.sensor.jms;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import org.testng.annotations.*;

import com.oneops.sensor.Sensor;
import com.oneops.sensor.events.BasicEvent;
import com.oneops.sensor.jms.SensorListener;
import com.oneops.sensor.util.SensorHeartBeat;

import static org.mockito.Mockito.*;


public class SensorListenerTest {

	@Test
	/* simple test case, later we need to mock the jms
	 * connection factory and do more */
	public void testOnMessageMocked() throws Exception{
		SensorListener listen = new SensorListener();
		listen.setSensor(mock(Sensor.class));
		listen.setSensorHeartBeat(mock(SensorHeartBeat.class));
		ObjectMessage msg = mock(ObjectMessage.class);
		
		BasicEvent event = mock(BasicEvent.class);
		when(msg.getObject()).thenReturn(event);
		
		listen.onMessage(msg);
	}
	
	@Test
	/*exception shall not come out, we force it
	 * but the code eats it */
	public void testOnMessageExceptionCase() throws Exception{
		SensorListener listen = new SensorListener();
		Sensor sensorThrower = mock(Sensor.class);

		listen.setSensor(sensorThrower);
		listen.setSensorHeartBeat(mock(SensorHeartBeat.class));
		ObjectMessage msg = mock(ObjectMessage.class);
		doThrow(new JMSException("mock")).when(msg).acknowledge();

		BasicEvent event = mock(BasicEvent.class);
		when(msg.getObject()).thenReturn(event);
		
		listen.onMessage(msg);
	}
}
