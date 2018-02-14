package com.oneops.crawler.plugins.hadr;

import java.util.Map;

public class CCount {

	private Map<String, Integer> activeProdClouds;

	public Map<String, Integer> getActiveProdClouds() {
		return activeProdClouds;
	}

	public void setActiveProdClouds(Map<String, Integer> activeProdClouds) {
		this.activeProdClouds = activeProdClouds;
	}

	@Override
	public String toString() {

		return activeProdClouds.keySet().toString();
	}

}