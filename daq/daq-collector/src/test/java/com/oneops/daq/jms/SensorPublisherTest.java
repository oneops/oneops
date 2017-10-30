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
package com.oneops.daq.jms;

import com.oneops.sensor.events.PerfEvent;
import com.oneops.sensor.thresholds.Threshold;
import com.oneops.sensor.thresholds.ThresholdsDao;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.testng.annotations.Test;

import javax.jms.JMSException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;


/**
 * tester for SensorPublisher
 * 
 */
public class SensorPublisherTest {

	public static final int LOOKUP_THRESHOLD = 20;
	private SensorPublisher publisher = new SensorPublisher();

	private static final Logger logger = Logger.getLogger(SensorPublisherTest.class);
	
	private static final String NO_ENV_EXC_MSG="missing KLOOPZ_AMQ_PASS env var";

	
	@Test
	public void testLookupThreshold() throws JMSException {
		SensorPublisher publisher = new SensorPublisher();
		PerfEvent event = new PerfEvent();
		ThresholdsDao dao = mock(ThresholdsDao.class);
		when(dao.getManifestId(100)).thenReturn(null);
		publisher.setThresholdDao(dao);
		event.setCiId(100);
		for (int i=0;i<LOOKUP_THRESHOLD+5;i++) {
			publisher.enrichAndPublish(event);
			if (i< LOOKUP_THRESHOLD) {  // even though we called publish 25 times, we should only do lookup 20 times
				verify(dao).getManifestId(100);
			}
		}
		verifyNoMoreInteractions(dao);
		assertEquals(25, publisher.missingManifestCounter.get());
		assertEquals(0, publisher.publishedCounter.get());
	}


	@Test
	public void testLookupThresholdNotReached() throws JMSException {
		SensorPublisher publisher = new SensorPublisher();
		JmsTemplate[] array = {mock(JmsTemplate.class)};
		publisher.setProducers(array);
		PerfEvent event = new PerfEvent();
		ThresholdsDao dao = mock(ThresholdsDao.class);
		when(dao.getManifestId(100)).thenReturn(1L);
		Threshold t = new Threshold();
		t.setThresholdJson("{}");
		t.setHeartbeat(true);
		when(dao.getThreshold(1, "null")).thenReturn(t);
		publisher.setThresholdDao(dao);
		event.setCiId(100);
		publisher.enrichAndPublish(event);
		publisher.enrichAndPublish(event);
		verify(dao).getManifestId(100);
		
		verify(dao).getThreshold(1,"null");
		verify(dao).getThreshold(1,"null");
		assertEquals(2, publisher.publishedCounter.get());
		verifyNoMoreInteractions(dao);
	}
	/**
	 * test publishing messages though we can only set up the message as we have
	 * not mocked the channel
	 * 
	 * @throws JMSException
	 */
	@Test(priority=1, expectedExceptions = JMSException.class ,enabled = false)
	public void testInitWithNoSetup() throws JMSException {
		// MessageProducer producerMock = mock(MessageProducer.class);
		// BasicEvent event = new BasicEvent();
		// event.setBucket("bucket");
		// event.setChecksum(125791113);
		// event.setCiId(1L);
		// event.setManifestId(2L);
		// event.setSource(this.getClass().getName());
		//
		try {
			publisher.init();
		} catch (JMSException e) {
			logger.info("I expected an exception, and yes I caught it "
					+ e.getMessage());
			assertEquals(e.getMessage(),NO_ENV_EXC_MSG);
			throw e;
		}
	}
	
	/**
	 * Test init after setup.
	 *
	 * @throws Exception the exception
	 */
	@Test(priority=2)
	public void testInitAfterSetup() throws Exception {
		setIntoEnvVariables();

		try {
			publisher.init();
			
			
		} catch (JMSException e) {
			logger.info("I expected this, and yes I caught it "
					+ e.getMessage());
			throw e;
		} finally {
			setIntoEnvVariables(); 	//run through some more with impunity
		}
	}

	/**
	 * places the name/values into the runtime environment
	 * 
	 * @param newenv
	 * @throws Exception
	 */
//	private void setEnvironmentVariable() {
//		// force some entry into the env variables
//		// otherwise construction will fail
//		Map<String, String> requiredMap = new HashMap<String, String>(1);
//		requiredMap.put("KLOOPZ_AMQ_PASS", "leanne-rymes");
//		try {
//			setIntoEnvVariables(requiredMap);
//			
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw (new UnsupportedOperationException(e));
//		}
//	}

	/** code that can inject a new env variable into this jvm */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void setIntoEnvVariables() throws Exception {
		Map<String, String> requiredMap = new HashMap<String, String>(1);
		requiredMap.put("KLOOPZ_AMQ_PASS", "leanne-rymes");
		
		Class[] classes = Collections.class.getDeclaredClasses();
		Map<String, String> env = System.getenv();
		for (Class cl : classes) {
			if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {

				Field field = cl.getDeclaredField("m");
				field.setAccessible(true);
				Object obj = field.get(env);
				Map<String, String> map = (Map<String, String>) obj;
				map.clear();
				map.putAll(requiredMap);
			}
		}
	}

}
