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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.testng.annotations.*;

import com.oneops.sensor.jms.OpsEventPublisher;

import static org.mockito.Mockito.*;




public class OpsEventPublisherTest {

	
	@Test(expectedExceptions=JMSException.class)
	/** negative test that JMSException happens if bad factory*/
	public void initFailTest() throws Exception {
		OpsEventPublisher oep = new OpsEventPublisher();

		ActiveMQConnectionFactory factoryMock = mock(ActiveMQConnectionFactory.class);
		try {
			when(factoryMock.createConnection()).thenThrow(
					new JMSException("from mock"));

			oep.setConnectionFactory(factoryMock);
			oep.setPersistent(false);
			oep.setQueue("mock-queue");
			oep.setTimeToLive(123L);

			oep.init();
		} catch (JMSException e) {
			throw e; //as expected, we are here
		}
	
	}

	@Test
	/** test cleanup without having set up the connection*/
	public void cleanupTest(){
		OpsEventPublisher oep = new OpsEventPublisher();
		//we did not call init, so we are setting him up for 
		//an exception, if he swallows it as expected we exit clean
		//if the exception escapes test will fail
		oep.cleanup();
		
	}
}
