package com.oneops.capacity;

import java.util.Map;
import java.util.Set;

public interface TektonClient {
	public Map<String, String> precheckReservation(Map<String, Map<String, Integer>> capacity, String nsPath, String createdBy);

	public void reserveQuota(Map<String, Map<String, Integer>> capacity, String nsPath, String createdBy) throws ReservationException;
	public void commitReservation(Map<String, Integer> capacity, String nsPath, String subscriptinoId);
	public void releaseResources(Map<String, Integer> capacity, String nsPath, String subscriptionId);
	public void deleteReservations(String nsPath, Set<String> subscriptionIds);
}
