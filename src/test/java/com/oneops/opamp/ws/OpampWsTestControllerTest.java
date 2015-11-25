package com.oneops.opamp.ws;

import org.testng.annotations.Test;

import com.oneops.opamp.exceptions.OpampException;
import com.oneops.opamp.service.BadStateProcessor;
import com.oneops.opamp.service.FlexStateProcessor;
import com.oneops.opamp.ws.OpampWsController;
import com.oneops.opamp.ws.OpampWsTestController;
import com.oneops.ops.events.OpsBaseEvent;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.doThrow;


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
				doThrow(new OpampException("expected")).when(fspThrowing).processOverutilized(event);	
				doThrow(new OpampException("expected")).when(fspThrowing).processUnderutilized(event);

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
