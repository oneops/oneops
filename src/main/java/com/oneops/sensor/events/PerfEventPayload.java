package com.oneops.sensor.events;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PerfEventPayload implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, Double> avg;
	private Map<String, Double> min;
	private Map<String, Double> max;
	private Map<String, Double> sum;
	private Map<String, Double> count;
	
	public Map<String, Double> getAvg() {
		return avg;
	}
	public void setAvg(Map<String, Double> avg) {
		this.avg = avg;
	}
	public Map<String, Double> getMin() {
		return min;
	}
	public void setMin(Map<String, Double> min) {
		this.min = min;
	}
	public Map<String, Double> getMax() {
		return max;
	}
	public void setMax(Map<String, Double> max) {
		this.max = max;
	}
	public Map<String, Double> getSum() {
		return sum;
	}
	public void setSum(Map<String, Double> sum) {
		this.sum = sum;
	}
	public Map<String, Double> getCount() {
		return count;
	}
	public void setCount(Map<String, Double> count) {
		this.count = count;
	}
	
	public void addAvg(String metricName, Double value) {
		if (avg == null) {
			avg = new HashMap<String, Double>();
		}
		avg.put(metricName, value);
	}

	public void addMin(String metricName, Double value) {
		if (min == null) {
			min = new HashMap<String, Double>();
		}
		min.put(metricName, value);
	}

	public void addMax(String metricName, Double value) {
		if (max == null) {
			max = new HashMap<String, Double>();
		}
		max.put(metricName, value);
	}

	public void addSum(String metricName, Double value) {
		if (sum == null) {
			sum = new HashMap<String, Double>();
		}
		sum.put(metricName, value);
	}

	public void addCount(String metricName, Double value) {
		if (count == null) {
			count = new HashMap<String, Double>();
		}
		count.put(metricName, value);
	}
	
}
