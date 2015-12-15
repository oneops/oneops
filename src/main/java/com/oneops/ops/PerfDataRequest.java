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

/**
 * The Class GetPerfDataRequest.
 */
public class PerfDataRequest extends DataRequest {
	
	/**
	 * Gets the step.
	 *
	 * @return the step
	 */
	public int getStep() {
		return step;
	}
	
	/**
	 * Sets the step.
	 *
	 * @param step the new step
	 */
	public void setStep(int step) {
		this.step = step;
	}	
	
	/**
	 * Gets the metrics.
	 *
	 * @return the metrics
	 */
	public String[] getMetrics() {
		return metrics;
	}
	
	/**
	 * Sets the metrics.
	 *
	 * @param metrics the new metrics
	 */
	public void setMetrics(String[] metrics) {
		this.metrics = metrics;
	}
	
	/**
	 * Gets the stat_function.
	 *
	 * @return the stat_function
	 */
	public String getStat_function() {
		return stat_function;
	}
	
	/**
	 * Sets the stat_function.
	 *
	 * @param statFunction the new stat_function
	 */
	public void setStat_function(String statFunction) {
		this.stat_function = statFunction;
	}

	
	private int step;
	private String stat_function;


	private String[] metrics;


}
