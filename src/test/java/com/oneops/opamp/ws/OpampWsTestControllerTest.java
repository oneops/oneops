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
package com.oneops.opamp.ws;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.oneops.opamp.exceptions.OpampException;
import com.oneops.opamp.service.BadStateProcessor;
import com.oneops.opamp.service.FlexStateProcessor;
import com.oneops.ops.events.OpsBaseEvent;


public class OpampWsTestControllerTest {
	
	private static final String SHRINKED_POOL = "shrinked pool";
	private static final String EXTENDED_POOL = "extended pool";

	@Test
	/**exercises the one method in the WsContorller */
	public void testController(){
		OpampWsController wsController = new OpampWsController();
		String eventsOut = wsController.getCIOpenEvents(-1);
		assertNull(eventsOut);
		
		
	}
	@Test
	/** ensures Strings out of the testController are correct */
	public void testWs() throws OpampException{
		OpampWsTestController wsTest = new OpampWsTestController();
		wsTest.setBsProcessor(mock(BadStateProcessor.class));
		wsTest.setFlexStateProcessor(mock(FlexStateProcessor.class));
		
		String testOut = wsTest.testProc(0); //method logs and submits repair asynch
		assertNull(testOut);
		
		String testFlexUp = wsTest.testFelxUp(0);
		assertEquals(EXTENDED_POOL,testFlexUp);
		
		String testFlexDown = wsTest.testFelxDown(0);
		assertEquals(SHRINKED_POOL,testFlexDown);
		
		FlexStateProcessor fspThrowing = mock(FlexStateProcessor.class);
		OpsBaseEvent event = mock(OpsBaseEvent.class);
		event.setCiId(anyLong());

 			try {
				doThrow(new OpampException("expected")).when(fspThrowing).processOverutilized(event, true);	
				doThrow(new OpampException("expected")).when(fspThrowing).processUnderutilized(event, true, System.currentTimeMillis());

			} catch (OpampException e) {
				e.printStackTrace();
			}

 			
 			wsTest.setFlexStateProcessor(fspThrowing);
 			String testFlexUpEx = wsTest.testFelxUp(0);
 			assertEquals(EXTENDED_POOL,testFlexUpEx);
 			
 			String testFlexDownEx = wsTest.testFelxDown(0);
 			assertEquals(SHRINKED_POOL,testFlexDownEx);
		 
	}

}
