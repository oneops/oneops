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

import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.Destination;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.log4j.Logger;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class SensorListenerContainer extends DefaultMessageListenerContainer {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	public static final AtomicInteger COUNT = new AtomicInteger(0);  	
	@Override
	public void initialize() {
		// we need to intercept this one so we can initi it with selector
		logger.debug("Fake init");
	}

	
	public void initWithSelector(int instanceId, int poolSize) {
		//int modulus = instanceId - 1; 
		//String selector = "(manifestId % " + poolSize + ") = " + modulus;
		logger.info(">>>>>>>>>>>>>>>>>This sensor instance will use shard - " + instanceId );
		String queueName = "perf-in-q-" + instanceId;
		logger.info(">>>>>>>>>>>>>>>>>Connecting to the queue - " + queueName );
		Destination perfQ = new ActiveMQQueue(queueName);
		super.setDestination(perfQ);
		super.initialize();
	}

	@Override
	protected void messageReceived(Object invoker, Session session) {
		COUNT.getAndIncrement();
		super.messageReceived(invoker, session);
	}

}