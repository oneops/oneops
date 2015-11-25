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
