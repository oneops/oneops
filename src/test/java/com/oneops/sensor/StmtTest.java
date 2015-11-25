package com.oneops.sensor;

import static org.testng.Assert.assertTrue;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.oneops.sensor.events.PerfEvent;

public class StmtTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Configuration config = new Configuration();
		//config.addEventType("BasicEvent", BasicEvent.class);
		config.addEventType("PerfEvent", PerfEvent.class);

		/* If we gonna use custom timestamps - use this
		ConfigurationEventTypeLegacy cetl = new  ConfigurationEventTypeLegacy();
		cetl.setStartTimestampPropertyName("timestamp");
		cetl.setEndTimestampPropertyName("timestamp");
		config.addEventType("PerfEvent", PerfEvent.class.getName(), cetl);
		*/
		// Get engine instance
		EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider(config);

		String stmt = "insert into OpsEvent select ciId, 'cpu' as name, 'open' as state, 'count:' || cast(count(1),string) as cnt from PerfEvent.win:time(1 min) where metrics('cpu') > 10 group by ciId having count(1) > 0 output first every 3 minutes";
		
		EPStatement statement = epService.getEPAdministrator().createEPL(stmt, "test");
		assertTrue(statement != null);		

	}

}
