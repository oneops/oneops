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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.oneops.controller.sensor.SensorClient;
import com.oneops.controller.util.ControllerUtil;
import com.oneops.controller.workflow.WorkflowController;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class InductorListenerTest {

	private static ApplicationContext context;
	private InductorListener listener;

	@BeforeClass
	/** use spring to inject conn factory */
	public void setUp() {
		context = new ClassPathXmlApplicationContext("**/test-app-context.xml");
		//load instance of listener, with dependencies injected
		listener = (InductorListener) context
				.getBean("inductorListener");

		WorkflowController wfController = mock(WorkflowController.class);
		ControllerUtil ctrlrUtil = mock(ControllerUtil.class);
		SensorClient sensorClient = mock(SensorClient.class);
		listener.setWfController(wfController);
		listener.setControllerUtil(ctrlrUtil);
		listener.setSensorClient(sensorClient);

	}

	@Test
	/** test the message impl */
	public void testListening() throws JMSException {
		try {
			listener.init();

			TextMessage message = mock(TextMessage.class);
			when(message.getText()).thenReturn("{messgetext:true}");
			when(message.getStringProperty("task_id")).thenReturn("corel-id");
			when(message.getStringProperty("task_result_code")).thenReturn(
					"200");
			when(message.getStringProperty("type")).thenReturn("deploybom");
			when(message.getJMSCorrelationID()).thenReturn("jms|cor!rel!ation!id");

			listener.onMessage(message);
			listener.cleanup();
			listener.getConnectionStats();
		} catch (JMSException e) {
			System.out.println("CAUTH EXCEPTION " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

	}

	@Test
	/** test with message where JMSException is forced to happend
	 * but we effectively are asserting it must get swallowed*/
	public void testBadMessage() throws JMSException {
		TextMessage message = mock(TextMessage.class);
		when(message.getJMSCorrelationID()).thenThrow(new JMSException("mock-force"));
		listener.onMessage(message);
	}
}