package com.oneops.sensor.domain;

import org.testng.annotations.*;

import com.oneops.sensor.domain.SensorStatement;
import com.oneops.sensor.domain.ThresholdStatements;

import static org.testng.Assert.*;

public class ThresholdStatementsTest {

	private static final long CHECK_SUM = 101010100;
	private static final String HB_DURATION = "13579";
	private static final boolean IS_HEARTBEAT = true;
	
	private static final String LISTENER = "mock-listener";
	private static final String STMT_NAME = "statement-sensor-g";
	private static final String STMT_TEXT = "sourcea-manifestb-duration13579-unhealthy";
	
	private static final SensorStatement STATEMENT = new SensorStatement(STMT_NAME,STMT_TEXT,LISTENER);


	@Test
	/** constuct and then check consistency */
	public void testSimple(){
	ThresholdStatements ttt = new ThresholdStatements();

	ttt.setChecksum(CHECK_SUM);
	ttt.setHbDuration(HB_DURATION);
	ttt.setHeartbeat(IS_HEARTBEAT);
	ttt.addStatement(STATEMENT);
	
	assertEquals(ttt.getChecksum(),CHECK_SUM);
	assertEquals(ttt.getHbDuration(),HB_DURATION);
	assertEquals(ttt.isHeartbeat(),IS_HEARTBEAT);
//	does work; not clear if contractual though 
	//-- assertTrue(ttt.getStatements().containsValue(STATEMENT));
	
	assertTrue(ttt.getStatements().size() == 1);
	assertTrue(ttt.getStmtNames().size() == 1);

	
	
	}
}