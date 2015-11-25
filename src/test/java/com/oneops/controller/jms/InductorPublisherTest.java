/*******************************************************************************
 *
 * Copyright 2015 Walmart, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.oneops.controller.jms;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCISimple;

public class InductorPublisherTest {

	private static ApplicationContext context ;
	private InductorPublisher publisher;

	@BeforeClass
	/** set up using DI from Spring*/
	public void setUp(){
		context = new ClassPathXmlApplicationContext("**/test-app-context.xml");
		
		//load instance of publisher, with dependencies injected
		publisher = (InductorPublisher) context.getBean("inductorPublisher");
		}
	
	@Test (priority=1)
	/** run through a publish without exception */
	public void publishTest() throws Exception{
		CmsCISimple cloudCmsCISimple = new CmsCISimple();
		Map<String, String> ciAttributes = new HashMap<String, String>();
		ciAttributes.put("location", "/east/centra/locale");
		cloudCmsCISimple.setCiAttributes(ciAttributes);

		CmsActionOrderSimple workOrder = new CmsActionOrderSimple();
		workOrder.setCloud(cloudCmsCISimple);

		String processId="123";
		String execId="234";
		publisher.init();
		publisher.publishMessage(processId, execId, workOrder, "xWaiting-task-name", "x-deployment");
		
	}
	@Test (priority=2)
	/** dump stats and close conn */
	public void cleanupTest() throws Exception{
		publisher.getConnectionStats();
		publisher.cleanup();
	}
	 
}
