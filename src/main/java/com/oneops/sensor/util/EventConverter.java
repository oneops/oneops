package com.oneops.sensor.util;

import com.oneops.ops.events.OpsBaseEvent;
import com.oneops.ops.events.OpsCloseEvent;
import com.oneops.ops.events.OpsEvent;

/**
 * The Class EventConverter.
 */
public class EventConverter {
	
	/**
	 * Convert.
	 *
	 * @param oEvent the o event
	 * @return the ops base event
	 */
	public static OpsBaseEvent convert(OpsEvent oEvent) {
		OpsBaseEvent bEvent = new OpsBaseEvent();
		bEvent.setCiId(oEvent.getCiId());
		bEvent.setBucket(oEvent.getBucket());
		bEvent.setCiState(oEvent.getCiState());
		bEvent.setName(oEvent.getName());
		bEvent.setManifestId(oEvent.getManifestId());
		bEvent.setSource(oEvent.getSource());
		bEvent.setState(oEvent.getState());
		bEvent.setType(oEvent.getType());
		bEvent.setMetrics(oEvent.getMetrics());
		bEvent.setTimestamp(oEvent.getTimestamp());
		bEvent.setCount(oEvent.getCount());
		bEvent.setStatus(oEvent.getStatus());
		return bEvent;
	}
	
	/**
	 * Convert.
	 *
	 * @param oEvent the o event
	 * @return the ops base event
	 */
	public static OpsBaseEvent convert(OpsCloseEvent oEvent) {
		OpsBaseEvent bEvent = new OpsBaseEvent();
		bEvent.setCiId(oEvent.getCiId());
		bEvent.setBucket(oEvent.getBucket());
		bEvent.setCiState(oEvent.getCiState());
		bEvent.setName(oEvent.getName());
		bEvent.setManifestId(oEvent.getManifestId());
		bEvent.setSource(oEvent.getSource());
		bEvent.setState(oEvent.getState());
		bEvent.setType(oEvent.getType());
		bEvent.setMetrics(oEvent.getMetrics());		
		bEvent.setTimestamp(oEvent.getTimestamp());
		bEvent.setCount(oEvent.getCount());
		bEvent.setStatus(oEvent.getStatus());
		return bEvent;
	}
	
	
}
