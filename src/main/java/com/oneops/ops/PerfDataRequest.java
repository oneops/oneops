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
