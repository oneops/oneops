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
package com.oneops.ops;

import java.util.HashMap;
import java.util.Map;



public class PerfArchive {

	public static final String AVERAGE = "average";
	public static final String MIN = "min";
	public static final String MAX = "max";
	public static final String SUM = "sum";
	public static final String COUNT = "count";	
		
	public double getXff() {
		return xff;
	}

	public void setXff(double xff) {
		this.xff = xff;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public String getConsolidationFunction() {
		return consolidationFunction;
	}

	public void setConsolidationFunction(String consolidationFunction) {
		this.consolidationFunction = consolidationFunction;
	}
	
	public String toLogString() {
		return "{ steps:"+steps+ " cf:"+consolidationFunction+"}";		
	}


	private double xff;
	private int rows;
	private int steps;
	private String consolidationFunction;

	
	public static void setDefaultArchives(PerfHeader header) {
		String[] statSet = new String[5];
		statSet[0] = AVERAGE;
		statSet[1] = MIN;
		statSet[2] = MAX;
		statSet[3] = SUM;
		statSet[4] = COUNT;

		Map<String,PerfArchive> rraMap = new HashMap<String,PerfArchive>();
		header.setRraMap(rraMap);
		
		for (int i=0; i<statSet.length; i++) {
			
			String stat=statSet[i];

			PerfArchive rra1min = new PerfArchive();
			rra1min.setXff(0.5);
			rra1min.setConsolidationFunction(stat);
			rra1min.setRows(0);
			rra1min.setSteps(1);

			PerfArchive rra5min = new PerfArchive();
			rra5min.setXff(0.5);
			rra5min.setConsolidationFunction(stat);
			rra5min.setRows(0);
			rra5min.setSteps(5);

			PerfArchive rra15min = new PerfArchive();
			rra15min.setXff(0.5);
			rra15min.setConsolidationFunction(stat);
			rra15min.setRows(0);
			rra15min.setSteps(15);		
			
			PerfArchive rra1hr = new PerfArchive();
			rra1hr.setXff(0.5);
			rra1hr.setConsolidationFunction(stat);
			rra1hr.setRows(0);
			rra1hr.setSteps(60);

			PerfArchive rra6hr = new PerfArchive();
			rra6hr.setXff(0.5);
			rra6hr.setConsolidationFunction(stat);
			rra6hr.setRows(0);
			rra6hr.setSteps(360);		
			
			PerfArchive rra1day = new PerfArchive();
			rra1day.setXff(0.5);
			rra1day.setConsolidationFunction(stat);
			rra1day.setRows(0);
			rra1day.setSteps(1440);
			
			rraMap.put("rra-"+stat+"-1m", rra1min);
			rraMap.put("rra-"+stat+"-5m", rra5min);
			rraMap.put("rra-"+stat+"-15m", rra15min);
			rraMap.put("rra-"+stat+"-1h", rra1hr);
			rraMap.put("rra-"+stat+"-6h", rra6hr);		
			rraMap.put("rra-"+stat+"-1d", rra1day);						
		}
		
		header.setRraCount(statSet.length*6);
		
	}
}
