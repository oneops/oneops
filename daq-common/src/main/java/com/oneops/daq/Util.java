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
package com.oneops.daq;


import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.sensor.events.PerfEvent;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class Util.
 */
public class Util {
	private static Logger logger = Logger.getLogger(Util.class);

	protected static PerfEvent parsePerfEvent(String msg) {
		PerfEvent pe = new PerfEvent();
		String[] parts = msg.split("\t");

		// verify structure
		if (parts.length < 4) {
			logger.error("invalid msg - needs min 4 tab delimited values in:"+msg);
			return null;
		}
		
		String perfBlob = parts[3];
		// verify length
		String[] metrics = perfBlob.split(" ");
		if (metrics.length < 1) {
			logger.error("invalid sample: " + perfBlob);
			return null;
		}				

		pe.setTimestamp(new Long(parts[0]).longValue());	
		Map<String,Double> metricMap = new HashMap<String,Double>();		

		String[] keyParts = parts[2].split(":");		
		pe.setCiId(Long.parseLong(keyParts[0]));

		if (keyParts.length <3) {
		    pe.setSource(parts[4]);
		    pe.setGrouping(parts[4]);
			pe.getMetrics().setAvg(metricMap);
			pe.setBucket("1m");

		} else { 
			// aggregated
			pe.setAggregate(true);
		    pe.setSource(keyParts[1]);
		    pe.setGrouping(keyParts[1]);
		    pe.setBucket(keyParts[2]);

			String[] aggParts = keyParts[2].split("-");
			String stat = aggParts[1];
			if (stat.equals("avg")) 
				pe.getMetrics().setAvg(metricMap);
			else if (stat.equals("max"))
			    pe.getMetrics().setMax(metricMap);
			else if (stat.equals("min"))
			    pe.getMetrics().setMin(metricMap);
		}
						
		logger.debug(pe.toString());		

		for (int i=0; i<metrics.length; i++) {
			String[] metricParts = new String(metrics[i]).split("=");
			if (metricParts.length<2) {
				logger.info("ci_id: "+ pe.getCiId()+" bad metric:"+metrics[i]);
			} else {
				
				// handle bad check script when they have missing metric key like " =1 "
				if (metricParts[0].isEmpty()) {
					logger.info("ci_id: "+ pe.getCiId()+" bad metric:"+metrics[i]);
					continue;
				}
				
				String[] valueParts = new String(metricParts[1]).split(";");
		
				if (valueParts.length == 0) {
					logger.info("ci_id: "+pe.getCiId()+"bad value: "+metricParts[1]);									
				} else {
					// could have unit-of-measure appended, ex) 1.00ms
					String value = valueParts[0].replaceAll("[^\\d.-]", "");					
					metricMap.put(metricParts[0], Double.parseDouble(value));
				}
			}
		}
		
		return pe;
	}
	
	
	/**
	 * Log map double.
	 *
	 * @param name the name
	 * @param h the h
	 * @param logger the logger
	 */
	public static void logMapDouble(String name, Map<String, Double> h, Logger logger) {
		StringBuilder sber = new StringBuilder("{");
		for (String k : h.keySet()) {
			sber.append(k).append(":").append(h.get(k)).append(",");
		}
		sber.append("}");
		logger.debug(name + " : " + sber);

	}

	/**
	 * Log map string.
	 *
	 * @param name the name
	 * @param h the h
	 * @param logger the logger
	 */
	public static void logMapString(String name, Map<String, String> h, Logger logger) {
		StringBuilder sb = new StringBuilder("{");
		for (String k : h.keySet()) {
			
			sb.append(k).append(":").append(h.get(k)).append(",");
			
		}
		sb.append("}");
		logger.debug(name + " : " + sb);

	}	
	
	/**
	 * Calc rate.
	 *
	 * @param type the type
	 * @param currentValue the current value
	 * @param lastValue the last value
	 * @param delta the delta
	 * @return the double
	 */
	public static double calcRate (String type, double currentValue, double lastValue, long delta) {
		
	    double nv;
	    if ( type.equalsIgnoreCase("ABSOLUTE") ) {
	        nv = currentValue / delta;
	    } else {    // default is COMPUTE/DERIVE
	        nv = ( currentValue - lastValue ) / delta;
	    }
	    return nv;
	}	
	
	/**
	 * Normalize class name.
	 *
	 * @param wo the wo
	 * @return the string
	 */
	public static String normalizeClassName(CmsWorkOrderSimple wo) {
		String appName = wo.getRfcCi().getCiClassName();
		return appName.replace("bom.", "").toLowerCase();
	}	
	



}
