package com.oneops.sensor.util;

import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.Test;

import com.oneops.ops.events.OpsBaseEvent;
import com.oneops.ops.events.OpsCloseEvent;
import com.oneops.ops.events.OpsEvent;
import com.oneops.sensor.events.PerfEventPayload;
import com.oneops.sensor.util.EventConverter;

import static org.testng.Assert.*;

public class EventConverterTest {
	
	private static final Long CI_ID=66L;
	private static final String BUCKET = "bucket-a";
	private static final String CI_STATE = "state-pending";
	private static final Long CI_MANIFEST_ID = 4321L;
	private static final String CI_SOURCE = "source-mocked";
	private static final String CI_TYPE = "closed-state-a";
	private static final Long CI_TIMESTAMP = 102222L;
    private static final String P_KEY = "swaps";
	private static final Double P_VAL = 66D;
	

	@Test
	public void testEvent(){
		
		OpsEvent oe = new OpsEvent();
		oe.setBucket(BUCKET);
		oe.setCiId(CI_ID);
		oe.setCiState(CI_STATE);
		oe.setManifestId(CI_MANIFEST_ID);
		oe.setSource(CI_SOURCE);
		oe.setType(CI_TYPE);
		oe.setTimestamp(CI_TIMESTAMP);
		
		PerfEventPayload metrics = new PerfEventPayload();
		Map<String, Double> countMap = new HashMap<String, Double>(1);
		countMap.put(P_KEY, P_VAL);
		metrics.setCount(countMap);
		oe.setMetrics(metrics);
				
		OpsBaseEvent convertedOut = EventConverter.convert(oe);
		
		assertEquals(convertedOut.getBucket(),BUCKET);
		assertEquals(convertedOut.getCiState(),CI_STATE);
		assertEquals(convertedOut.getSource(),CI_SOURCE);
		assertEquals(convertedOut.getType(),CI_TYPE);
		assertEquals(convertedOut.getMetrics(),metrics);
		
		assert(CI_TIMESTAMP.equals(convertedOut.getTimestamp()));
		assert(CI_MANIFEST_ID.equals(convertedOut.getManifestId()));
		assert(CI_ID.equals(convertedOut.getCiId() ));	
	}
	
	@Test
	/** again and similar but pass in a close event */
	public void testEventClosed(){
		
		OpsCloseEvent opsCloseEvent = new OpsCloseEvent();
		opsCloseEvent.setBucket(BUCKET);
		opsCloseEvent.setCiId(CI_ID);
		opsCloseEvent.setCiState(CI_STATE);
		opsCloseEvent.setManifestId(CI_MANIFEST_ID);
		opsCloseEvent.setSource(CI_SOURCE);
		opsCloseEvent.setType(CI_TYPE);
		opsCloseEvent.setTimestamp(CI_TIMESTAMP);
		
		PerfEventPayload metrics = new PerfEventPayload();
		Map<String, Double> countMap = new HashMap<String, Double>(1);
		countMap.put(P_KEY, P_VAL);
		metrics.setCount(countMap);
		opsCloseEvent.setMetrics(metrics);
				
		OpsBaseEvent convertedOut = EventConverter.convert(opsCloseEvent);
		
		assertEquals(convertedOut.getBucket(),BUCKET);
		assertEquals(convertedOut.getCiState(),CI_STATE);
		assertEquals(convertedOut.getSource(),CI_SOURCE);
		assertEquals(convertedOut.getType(),CI_TYPE);
		assertEquals(convertedOut.getMetrics(),metrics);
		
		assert(CI_TIMESTAMP.equals(convertedOut.getTimestamp()));
		assert(CI_MANIFEST_ID.equals(convertedOut.getManifestId()));
		assert(CI_ID.equals(convertedOut.getCiId() ));	
	}
}
