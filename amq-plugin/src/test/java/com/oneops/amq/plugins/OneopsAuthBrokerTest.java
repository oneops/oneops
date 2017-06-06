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
package com.oneops.amq.plugins;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.Connection;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.ProducerInfo;
import org.apache.log4j.Logger;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.oneops.amq.plugins.CMSClient;
import com.oneops.amq.plugins.CmsAuthException;
import com.oneops.amq.plugins.OneopsAuthBroker;
import com.oneops.cms.simple.domain.CmsCISimple;

import static org.mockito.Mockito.*;

public class OneopsAuthBrokerTest {
	

	//the instance we will be testing across some methods 
	private OneopsAuthBroker oneopsAuthBroker;
	
	//shall be mocked
	private CMSClient cmsClient;
	private ConnectionInfo connectionInfoSystem;
	private ConnectionInfo connectionInfoUser;
	private ConnectionInfo connectionBadInfo;

	private ConnectionContext connectionContextSystem;
	private ConnectionContext connectionContextMock;

	private ConsumerInfo consumerInfo;
	private ActiveMQDestination  activeMQDestination;
	
	private ProducerInfo producerInfo;
	
	private static final String AUTH_KEY_VALUE = "blahblah";
	private static final String CONN_INFO_PASS_BAD = "foobarbizbat";
	private static final String CLOUD_NAME = "stubbedCloudName";
	private static final String SYSTEM_CLIENT_ID = "system";
	private static final String MOCK_CLIENT_ID = "stubbedClientId";
	private static final String MOCK_REMOTE_ADDR = "stubbedRemoteAddr";
	private static final String MOCK_PHYSICAL_NAME = "controller.response";
	private static final String MQ_PHYSICAL_NAME = "ActiveMQ.dest";
	private static final String AUTH_ATTR_KEY = "auth";
	private static Logger logger = Logger.getLogger(OneopsAuthBrokerTest.class);

	@BeforeClass
	/**
	 * sets up mocks which get re-used in the tests
	 */
	public void setUp()  {		
	
		//set up the mock for CMSClient
		cmsClient = mock(CMSClient.class);
		//- and the mock data that mock will return
		CmsCISimple cmsCISimple = new CmsCISimple();
		Map<String,String> ciAttributes = new HashMap<String,String>(1);
		ciAttributes.put(AUTH_ATTR_KEY, AUTH_KEY_VALUE);
		cmsCISimple.setCiAttributes(ciAttributes);
		//for getCloud(user-name,cloud-name)
		when(cmsClient.getCloudCi(CLOUD_NAME, CLOUD_NAME)).thenReturn(cmsCISimple);
		when(cmsClient.getCloudCi("user", CLOUD_NAME)).thenReturn(cmsCISimple);
		
		//set up the mock for ConnectionInfo:  system/blahblah
		connectionInfoSystem = mock(ConnectionInfo.class);
		when(connectionInfoSystem.getClientId()).thenReturn(SYSTEM_CLIENT_ID);
		when(connectionInfoSystem.getUserName()).thenReturn(SYSTEM_CLIENT_ID + ":" + CLOUD_NAME);
		when(connectionInfoSystem.getPassword()).thenReturn(AUTH_KEY_VALUE);
		
		//and another ConnectionInfo stubbedClientId/foobarbizbat
		connectionBadInfo = mock(ConnectionInfo.class);
		when(connectionBadInfo.getClientId()).thenReturn(MOCK_CLIENT_ID);
		when(connectionBadInfo.getUserName()).thenReturn(CLOUD_NAME + ":" + CLOUD_NAME);
		when(connectionBadInfo.getPassword()).thenReturn(CONN_INFO_PASS_BAD); //to cause fail paasword check		
		
		//and another ConnectionInfo stubbedClientId/blahblah
		connectionInfoUser = mock(ConnectionInfo.class);
		when(connectionInfoUser.getClientId()).thenReturn(MOCK_CLIENT_ID);
		when(connectionInfoUser.getUserName()).thenReturn("user" + ":" + CLOUD_NAME);
		when(connectionInfoUser.getPassword()).thenReturn("blahblah"); //to cause fail paasword check		
		
		// construct ConnectionContext, passing it the system's connection info
		connectionContextSystem = new ConnectionContext(connectionInfoSystem);
		
		//create a mock ConnectionContext
		connectionContextMock = mock(ConnectionContext.class);
		 final Connection connectionMock = mock(Connection.class);
		when(connectionMock.getRemoteAddress()).thenReturn(MOCK_REMOTE_ADDR);
				
		when(connectionContextMock.getClientId()).thenReturn(MOCK_CLIENT_ID);
		when(connectionContextMock.getConnection()).thenAnswer(new Answer<Connection>() {
			@Override
			public Connection answer(InvocationOnMock invocation)
					throws Throwable {
				return connectionMock;
			}
		});
		
		//set up mock for ConsumerInfo
		activeMQDestination =  ActiveMQDestination.createDestination("mockMQDestionation",  (byte) 1 );		
		activeMQDestination.setPhysicalName(MOCK_PHYSICAL_NAME);
		
		consumerInfo = mock(ConsumerInfo.class);
		when(consumerInfo.getDestination()).thenReturn(activeMQDestination);
	}
	
	
	
	@Test(priority=2)
	/**
	 * construct object ok. Note - it becomes the instance variable used from now on
	 * succeeds because of the Env setup that's done off the bat
	 */
	public void constructorTest(){
		setEnvironmentVariable();

		Broker broker = mock(Broker.class);
		this.oneopsAuthBroker = new OneopsAuthBroker(broker, cmsClient); 
	}

	@Test(priority=3, expectedExceptions = CmsAuthException.class)
	public void addConnectionBadPasswordTest() throws Exception{
		//try to add connection but the ConnectionInfo will not have a good password
			try {
				this.oneopsAuthBroker.addConnection(connectionContextMock, connectionBadInfo);
			} catch (CmsAuthException e) {
				throw(e);
			} catch (Exception ee){
				//here only if failure in Broker ie.  super to addProducer()
				throw(ee);
			}
	}
	@Test(priority=3, expectedExceptions = CmsAuthException.class)
	/** force result as if null user passed in; should throw*/
	public void addConnectionNullUserTest() throws Exception{

		ConnectionInfo connWithNullUser = mock(ConnectionInfo.class);
		when(connWithNullUser.getClientId()).thenReturn(MOCK_CLIENT_ID);
		when(connWithNullUser.getUserName()).thenReturn(null); // !
		when(connWithNullUser.getPassword()).thenReturn(CONN_INFO_PASS_BAD); 
		this.oneopsAuthBroker.addConnection(connectionContextMock, connWithNullUser);

	}
	@Test(priority=3, expectedExceptions = CmsAuthException.class)
	public void addConnectionNullCloudTest() throws Exception{
		Broker broker = mock(Broker.class);

		CMSClient oddClient = mock(CMSClient.class);
		CmsCISimple cmsCISimple = new CmsCISimple();
		Map<String, String> ciAttributes = new HashMap<String, String>(1);
		ciAttributes.put(AUTH_ATTR_KEY, AUTH_KEY_VALUE);
		cmsCISimple.setCiAttributes(ciAttributes);
		when(oddClient.getCloudCi(CLOUD_NAME, CLOUD_NAME)).thenReturn(
				cmsCISimple);
		when(oddClient.getCloudCi(anyString(), anyString())).thenReturn(null); // causes the exception
		OneopsAuthBroker oneopsAuthBroker = new OneopsAuthBroker(broker,oddClient);
		oneopsAuthBroker.addConnection(connectionContextMock, connectionBadInfo); //bad cloud, is error

	}
	
	@Test(priority=4)
	/** 
	 * add connection 
	 */
	public void addConnectionTest(){

		try {
			this.oneopsAuthBroker.addConnection(connectionContextMock, connectionInfoUser);
		} catch (Exception e) {
			logger.warn("caught exception, make sure Broker is mocked",e);
			throw new RuntimeException(e);
		}
		
	}
	
	@Test(priority=5, expectedExceptions=RuntimeException.class)
	public void addConsumerTestDenied(){
		
		try {
			this.oneopsAuthBroker.addConsumer(connectionContextMock, consumerInfo);
		} catch (Exception e) {
			logger.warn("caught exception, make sure Broker is mocked",e);
			throw new RuntimeException(e);
		}
		
	}
	
	@Test(priority=5)
	public void addConsumerTest(){
		//set up a mock for ConsumerInfo
		ActiveMQDestination activeMQDestinationMQ =  ActiveMQDestination.createDestination("mockMQDestionation",  (byte) 1 );		
		activeMQDestinationMQ.setPhysicalName(MQ_PHYSICAL_NAME);
		
		ConsumerInfo consumerInfoActiveMQ = mock(ConsumerInfo.class);
		when(consumerInfoActiveMQ.getDestination()).thenReturn(activeMQDestinationMQ);
		
		//set up mock for ProducerInfo
		producerInfo = mock(ProducerInfo.class);
		when(producerInfo.getDestination()).thenReturn(activeMQDestination);	
		
		try {
			this.oneopsAuthBroker.addConsumer(connectionContextMock, consumerInfoActiveMQ);
		} catch (Exception e) {
			logger.warn("caught exception, make sure Broker is mocked",e);
			throw new RuntimeException(e);
		}
		
	}
	@Test(priority = 6)
	public void addProducerTest() {

		try {
			this.oneopsAuthBroker.addProducer(connectionContextMock, producerInfo);
		} catch (Exception e) {
			logger.warn("caught exception, make sure Broker is mocked",e);
			throw new RuntimeException(e);
		}
	
	}
 
	@Test(priority = 6, expectedExceptions = RuntimeException.class)
	public void addProducerTestProducerDenied() {

		//create a mock ConnectionContext which was not used when setting up user map
		ConnectionContext ccForbidden = mock(ConnectionContext.class);
		when(ccForbidden.getClientId()).thenReturn("this-is-not-in-user-map");
		 final Connection connectionMock = mock(Connection.class);
			when(connectionMock.getRemoteAddress()).thenReturn(MOCK_REMOTE_ADDR);
				when(ccForbidden.getConnection()).thenAnswer(new Answer<Connection>() {
			@Override
			public Connection answer(InvocationOnMock invocation)
					throws Throwable {
				return connectionMock;
			}
		});
		
		try {
			this.oneopsAuthBroker.addProducer(ccForbidden, producerInfo);
		} catch (Exception e) {
			logger.warn("caught exception, make sure Broker is mocked",e);
			throw new RuntimeException(e);
		}
	}

	
	/** 
	 * places the name/values into the runtime environment
	 * @param newenv
	 * @throws Exception
	 */
	private void setEnvironmentVariable() {
		//force some entry into the env variables
		//otherwise construction will fail
		Map<String,String> requiredMap = new HashMap<String,String>(1);
		requiredMap.put("KLOOPZ_AMQ_PASS", "leanne-rymes");
		try {
			setIntoEnvVariables(requiredMap);
		} catch (Exception e) {
			e.printStackTrace();
			throw(new UnsupportedOperationException(e));
		}
	}   
	
	/** code that can inject a new env variable into this jvm */
	private static void setIntoEnvVariables(Map<String, String> newenv) throws Exception {
	    Class[] classes = Collections.class.getDeclaredClasses();
	    Map<String, String> env = System.getenv();
	    for(Class cl : classes) {
	        if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {        

		    	Field field = cl.getDeclaredField("m");
	            field.setAccessible(true);
	            Object obj = field.get(env);
	            Map<String, String> map = (Map<String, String>) obj;
	            map.clear();
	            map.putAll(newenv);
	        }
	    }
	}


}
