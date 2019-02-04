package com.oneops.capacity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class NoOpTektonClient implements TektonClient {
	private static Logger logger = Logger.getLogger(NoOpTektonClient.class);
	
	public Map<String, String> precheckReservation(Map<String, Map<String, Integer>> capacity, String nsPath,
			String createdBy) {
		return new HashMap<>();
	}

	public void reserveQuota(Map<String, Map<String, Integer>> capacity, String nsPath, String createdBy) throws ReservationException {
		return;
	}

	public void commitReservation(Map<String, Integer> capacity, String nsPath, String subscriptinoId) {
		return;
	}

	public void releaseResources(Map<String, Integer> capacity, String nsPath, String subscriptionId) {
		return;
	}

	public void deleteReservations(String nsPath, Set<String> subscriptionIds) {
		return;
	}

    public NoOpTektonClient() {
    	logger.info("Starting NoOpTektonClient");
    }

}
