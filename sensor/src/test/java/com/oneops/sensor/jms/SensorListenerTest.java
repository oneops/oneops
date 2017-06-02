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
